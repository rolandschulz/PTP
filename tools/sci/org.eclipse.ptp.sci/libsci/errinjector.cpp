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

 Classes: ErrorInjector

 Description: The functions of error handler include:
     a. accept user commands and sent to destination nodes
     b. prompt failure data to use with time stamp
     c. prompt recovery data to user with time stamp
   
 Author: CSH

 History:
   Date     Who ID    Description
   -------- --- ---   -----------
   05/11/09 CSH       Initial code (F156654)

****************************************************************************/

#include <assert.h>

#include <sys/socket.h>
#include <sys/wait.h>

#include "errinjector.hpp"
#include "errmonitor.hpp"

#include "queue.hpp"
#include "socket.hpp"
#include "stream.hpp"
#include "log.hpp"
#include "packer.hpp"
#include "message.hpp"

#include "ctrlblock.hpp"

const int INJECTION_PORT = 6689;
const int KILLNODE = 1;
const int SHUTDOWN = 2;

ErrorInjector::ErrorInjector()
{
    injOutErrQueue=NULL;
    monitor=NULL;

    MessageQueue *monitorInQ = new MessageQueue();
    monitorInQ->setName("monitorInQ");
    gCtrlBlock->setMonitorInQueue(monitorInQ);
    gCtrlBlock->registerQueue(monitorInQ);
}

ErrorInjector::~ErrorInjector()
{
}

void ErrorInjector::run()
{
    int child = -1;
    int port = INJECTION_PORT;
    int sockfd;
    try {
        sockfd = socket.listen(port);
    } catch (SocketException &e) {
        log_error("socket exception %s", e.getErrMsg().c_str());
        return;
    } catch (...) {
        log_error("unknown exception");
        return;
    }
    listenSockFd = sockfd;

    log_crit("Error injector is running");
    while (getState()) {
        try {
            child = socket.accept(sockfd);
            childSockFd = child;
        } catch (SocketException &e) {
            log_error("socket exception %s", e.getErrMsg().c_str());
            break;
        } catch (...) {
            log_error("unknown exception");
            break;
        }
        
        if (child < 0)
           continue;
        
        //create errorMonitor
        monitor = new ErrorMonitor(child);
        monitor->start();
        //create stream
        Stream *stream = new Stream();
        stream->init(child);

        // process user's commands once a connection is established
        process(stream);
        stream->stop();
        monitor->join();
        delete monitor;
        monitor=NULL;
    }

    ::close(sockfd);
    setState(false);
}

void ErrorInjector::process(Stream *stream)
{        
    int errInjectType;
    int numArrSize;
    int i;
    int temp;
    std::vector<int> numArr;
    //assert(monitorStream);
    while(1)
    {    
        try{
             *stream>>errInjectType>>numArrSize;
             for(i=0; i<numArrSize; i++)
             {
                 *stream>>temp;
                 numArr.push_back(temp);
             }
             *stream>>endl;
        }
        catch (SocketException &e) {
            log_error("error injector socket exception %s", e.getErrMsg().c_str());
            monitor->setState(false);
            gCtrlBlock->getMonitorInQueue()->notify();
            break;
        } catch (...) {
            log_error("unknown exception");
            monitor->setState(false);
            gCtrlBlock->getMonitorInQueue()->notify();
            break;
        }
        handleCommand(numArr, errInjectType);
        numArr.clear();
    }
}

void ErrorInjector::handleCommand(std::vector<int>& numArray, int errInjType)
{
    std::vector<int>::iterator it;
    for(it=numArray.begin(); it!=numArray.end(); ++it)
    {
        handleItem(*it, errInjType);
    }
}

void ErrorInjector::handleItem(int nodeId, int errInjType)
{
    Packer packer;
    packer.packInt(nodeId);

    char *bufs[1];
    int sizes[1];
    
    bufs[0] = packer.getPackedMsg();
    sizes[0] = packer.getPackedMsgLen();

    Message *msg = new Message();
    switch(errInjType){
        case KILLNODE:  
            msg->build(SCI_FILTER_NULL, SCI_GROUP_ALL, 1, bufs, sizes, Message::KILLNODE);
            break;
        case SHUTDOWN:  
            msg->build(SCI_FILTER_NULL, SCI_GROUP_ALL, 1, bufs, sizes, Message::SHUTDOWN);
            break;
        default:
            break;
    }
    assert(injOutErrQueue);
    injOutErrQueue->produce(msg);
    delete [] bufs[0];
}

int ErrorInjector::stop()
{
    ::shutdown(listenSockFd, SHUT_RDWR);
    ::close(listenSockFd);
    ::shutdown(childSockFd, SHUT_RDWR);
    ::close(childSockFd);
    setState(false);
    return 0;
}

void ErrorInjector::setInjOutQueue(MessageQueue* queue)
{
    injOutErrQueue = queue;
}


