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

#include <assert.h>
#include <stdlib.h>
#include <stdio.h>
#include <sys/socket.h>
#include <sys/wait.h>

#include "log.hpp"
#include "stream.hpp"
#include "sshfunc.hpp"
#include "exception.hpp"

#include "listener.hpp"
#include "ctrlblock.hpp"
#include "embedagent.hpp"
#include "socket.hpp"
#include "readerproc.hpp"
#include "writerproc.hpp"
#include "routinglist.hpp"
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
    char *envp = ::getenv("SCI_LISTENER_PORT");
    if (envp) {
        bindPort = atoi(envp);
    }
    envp = ::getenv("SCI_DEVICE_NAME");
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
    join();

    return 0;
}

void Listener::run()
{
    int child = -1;
    int hndl = -1;
    int pID = -1;
    int key;
    int rc;
    struct iovec sign = {0};

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

            *stream >> hndl >> pID >> sign >> endl;
            if (hndl >= 0) {
                log_debug("Listener: back end %d is connected", hndl); 
            } else {
                log_debug("Listener: agent %d is connected", hndl); 
            }
            rc = psec_verify_data(&sign, "%d%d%d", key, hndl, pID);
            delete [] (char *)sign.iov_base;
            if (rc != 0) {
                log_warn("Misleading message comes.");
                stream->stop();
                delete stream;
                continue;
            }
            gCtrlBlock->getAgent(pID)->getRoutingList()->startRouting(hndl, stream);
        } catch (Exception &e) {
            log_error("Listener: exception %s", e.getErrMsg());
            break;
        } catch (ThreadException &e) {
            log_error("Listener: thread exception %d", e.getErrCode());
            break;
        } catch (SocketException &e) {
            log_error("Listener: socket exception: %s", e.getErrMsg().c_str());
            break;
        } catch (std::bad_alloc) {
            log_error("Listener: out of memory");
            break;
        } catch (...) {
            log_error("Listener: unknown exception");
            break;
        }
    }

    ::close(sockfd);
    setState(false);
}

