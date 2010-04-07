#ifndef _PRAGMA_COPYRIGHT_
#define _PRAGMA_COPYRIGHT_
#pragma comment(copyright, "%Z% %I% %W% %D% %T%\0")
#endif /* _PRAGMA_COPYRIGHT_ */
/****************************************************************************

* Copyright (c) 2008, 2010 IBM Corporation.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0s
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html

 Classes: Listener

 Description: Listener Thread.
   
 Author: Tu HongJ, Liu Wei, Nicole Nie

 History:
   Date     Who ID    Description
   -------- --- ---   -----------
   10/06/08 tuhongj      Initial code (D153875)

****************************************************************************/

#include "listener.hpp"
#include <assert.h>
#include <stdlib.h>
#include <sys/socket.h>
#include <sys/wait.h>

#include "log.hpp"
#include "stream.hpp"
#include "exception.hpp"

#include "ctrlblock.hpp"
#include "statemachine.hpp"
#include "socket.hpp"
#include "readerproc.hpp"
#include "writerproc.hpp"
#include "queue.hpp"

Listener:: Listener(int hndl)
        : Thread(hndl), bindPort(-1)
{
    socket = new Socket();
}

Listener::~Listener()
{
    delete socket;
}

int Listener::init()
{
    bindPort = 0;
    char *envp = ::getenv("SCI_DEVICE_NAME");
    if (envp) {
        string ifname = envp;
        sockfd = socket->listen(bindPort, ifname);
    } else {
        sockfd = socket->listen(bindPort);
    }
    
    log_debug("listener binded to port %d", bindPort);

    return bindPort;
}

int Listener::stop()
{
    setState(false);
    ::shutdown(sockfd, SHUT_RDWR);
    ::close(sockfd);

    return 0;
}

void Listener::run()
{
    int child = -1;
    int hndl = -1;
    int key;

    while (getState()) {
        try {
            child = socket->accept(sockfd);
        } catch (SocketException &e) {
            log_warn("Listener: socket broken: %s", e.getErrMsg().c_str());
            break;
        } catch (...) {
            log_warn("Listener: unknown exception: %s");
            break;
        }
        if (child < 0) {
            // invalid connection
           continue;
        }
        if (!gCtrlBlock->isEnabled()) {
            log_debug("Listener: uninitialized, rejected this connection");
            break;
        }

        log_debug("Listener: accepted a child agent sockfd %d", child);

        try {
            Stream *stream = new Stream();
            stream->init(child);
            *stream >> key;
            if (key != gCtrlBlock->getJobKey()) {
                log_warn("Listener: client with invalid credential is trying to connect.");
                stream->stop();
                delete stream;
                continue;
            }

            *stream >> hndl >> endl;
            if (hndl >= 0) {
                log_debug("Listener: back end %d is connected", hndl); 
            } else {
                log_debug("Listener: agent %d is connected", hndl); 
            }

            gStateMachine->parse(StateMachine::CLIENT_CONNECTED);
        
            char name[32];
            MessageQueue *inQ = gCtrlBlock->queryQueue(hndl);
            if (NULL == inQ) {
                inQ = new MessageQueue();
                if (hndl >= 0) {
                    ::sprintf(name, "BE%d_inQ", hndl);
                } else {
                    ::sprintf(name, "Agent%d_inQ", hndl);
                }
                inQ->setName(string(name));
                gCtrlBlock->registerQueue(inQ);
                gCtrlBlock->mapQueue(hndl, inQ);
                gCtrlBlock->genSelfInfo(inQ, false);
            }

            ReaderProcessor *reader = new ReaderProcessor(hndl);
            reader->setInStream(stream);
            reader->setOutQueue(gCtrlBlock->getFilterInQueue());
            ::sprintf(name, "Reader%d", hndl);
            reader->setName(string(name));
            reader->setOutErrorQueue(gCtrlBlock->getErrorQueue());

            WriterProcessor *writer = new WriterProcessor(hndl);
            writer->setInQueue(inQ);
            writer->setOutStream(stream);
            ::sprintf(name, "Writer%d", hndl);
            writer->setName(string(name));

            // writer is a peer processor of reader
            reader->setPeerProcessor(writer);
            
            reader->start();
            writer->start(); 

            gCtrlBlock->registerProcessor(reader);
            gCtrlBlock->registerProcessor(writer);
        
            gCtrlBlock->registerStream(stream);
        } catch (Exception &e) {
            log_error("Listener: exception %s", e.getErrMsg());
            gStateMachine->parse(StateMachine::FATAL_EXCEPTION);
            break;
        } catch (ThreadException &e) {
            log_error("Listener: thread exception %d", e.getErrCode());
            gStateMachine->parse(StateMachine::FATAL_EXCEPTION);
            break;
        } catch (SocketException &e) {
            log_error("Listener: socket exception: %s", e.getErrMsg().c_str());
            gStateMachine->parse(StateMachine::FATAL_EXCEPTION);
            break;
        } catch (std::bad_alloc) {
            log_error("Listener: out of memory");
            gStateMachine->parse(StateMachine::FATAL_EXCEPTION);
            break;
        } catch (...) {
            log_error("Listener: unknown exception");
            gStateMachine->parse(StateMachine::FATAL_EXCEPTION);
            break;
        }
    }

    ::close(sockfd);
    setState(false);
}

