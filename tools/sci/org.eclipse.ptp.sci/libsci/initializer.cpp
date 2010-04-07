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

 Classes: Initializer

 Description: Prepare the environment when startup, which includes:
        1) Processor threads
        2) Message queue
        3) Others like environment variables
   
 Author: Nicole Nie

 History:
   Date     Who ID    Description
   -------- --- ---   -----------
   02/10/09 nieyy      Initial code (D153875)

****************************************************************************/

#include "initializer.hpp"
#include <assert.h>
#include <unistd.h>
#include <stdlib.h>
#include <string.h>
#include <sys/types.h>
#include <pwd.h>

#include "sci.h"

#include "log.hpp"
#include "socket.hpp"
#include "stream.hpp"
#include "exception.hpp"

#include "ctrlblock.hpp"
#include "routinglist.hpp"
#include "statemachine.hpp"
#include "topology.hpp"
#include "queue.hpp"
#include "message.hpp"
#include "readerproc.hpp"
#include "writerproc.hpp"
#include "filterproc.hpp"
#include "handlerproc.hpp"
#include "routerproc.hpp"
#include "purifierproc.hpp"
#include "observer.hpp"
#include "listener.hpp"
#include "parent.hpp"
#include "errdetector.hpp"
#include "errhandler.hpp"
#include "errinjector.hpp"
#include "eventntf.hpp"
#include "allocator.hpp"

#define SCI_DAEMON_PORT 6688

Initializer* Initializer::instance = NULL;

Initializer::Initializer()
{
}

Initializer::~Initializer()
{
    instance = NULL;
}

int Initializer::init(int hndl)
{
    int rc = SCI_SUCCESS;

    int level = Log::INFORMATION;
    char dir[MAX_PATH_LEN] = "/opt/sci/log";
    
    char *envp = ::getenv("SCI_LOG_DIRECTORY");
    if (envp != NULL) {
        ::strncpy(dir, envp, sizeof(dir));
    }
    
    envp = ::getenv("SCI_LOG_LEVEL"); 
    if (envp != NULL)
        level = ::atoi(envp);

    if (gCtrlBlock->getMyRole() == CtrlBlock::FRONT_END) {
        Log::getInstance()->init(dir, "fe.log", level);
        log_debug("I am a front end, my handle is %d", hndl);
    } else if (gCtrlBlock->getMyRole() == CtrlBlock::AGENT) {
        Log::getInstance()->init(dir, "scia.log", level);
        log_debug("I am an agent, my handle is %d", hndl);
    } else {
        Log::getInstance()->init(dir, "be.log", level);
        log_debug("I am a back end, my handle is %d", hndl);
    }
    
    try {
        if (gCtrlBlock->getMyRole() == CtrlBlock::FRONT_END) {
            initListener();
            rc = initFE(hndl);
        } else if (gCtrlBlock->getMyRole() == CtrlBlock::AGENT) {
            initListener();
            rc = initAgent(hndl);
        } else {
            rc = initBE(hndl);
        }
    } catch (Exception &e) {
        log_error("Initializer: exception %s", e.getErrMsg());
        gStateMachine->parse(StateMachine::FATAL_EXCEPTION);
        return SCI_ERR_INITIALIZE_FAILED;
    } catch (ThreadException &e) {
        log_error("Initializer: thread exception %d", e.getErrCode());
        gStateMachine->parse(StateMachine::FATAL_EXCEPTION);
        return SCI_ERR_INITIALIZE_FAILED;
    } catch (SocketException &e) {
        log_error("Initializer: socket exception: %s", e.getErrMsg().c_str());
        gStateMachine->parse(StateMachine::FATAL_EXCEPTION);
        return SCI_ERR_INITIALIZE_FAILED;
    } catch (std::bad_alloc) {
        log_error("Initializer: out of memory");
        gStateMachine->parse(StateMachine::FATAL_EXCEPTION);
        return SCI_ERR_INITIALIZE_FAILED;
    } catch (...) {
        log_error("Initializer: unknown exception");
        gStateMachine->parse(StateMachine::FATAL_EXCEPTION);
        return SCI_ERR_INITIALIZE_FAILED;
    }

    return rc;
}

void Initializer::recoverAgent(Stream * stream)
{
    assert(stream);
    
    int hndl = gCtrlBlock->getMyHandle();

    ReaderProcessor *reader = new ReaderProcessor(hndl);
    reader->setName("ReaderP");
    reader->setInStream(stream);
    reader->setOutQueue(gCtrlBlock->getRouterInQueue());
    reader->setOutErrorQueue(gCtrlBlock->getErrorQueue());
    
    WriterProcessor *writer = new WriterProcessor(hndl);
    writer->setName("WriterP");
    writer->setInQueue(gCtrlBlock->getFilterOutQueue());
    writer->setOutStream(stream);

    // writer is a peer processor of reader
    reader->setPeerProcessor(writer);

    gCtrlBlock->registerProcessor(reader);
    gCtrlBlock->registerProcessor(writer);
    
    gRoutingList->propagateGroupInfo();

    reader->start();
    writer->start();
}

void Initializer::recoverBE(Stream * stream)
{
    assert(stream);

    int hndl = gCtrlBlock->getMyHandle();
    
    WriterProcessor *writer = new WriterProcessor(hndl);
    writer->setInQueue(gCtrlBlock->getUpQueue());
    writer->setOutStream(stream);

    PurifierProcessor *purifier = new PurifierProcessor(hndl);
    purifier->setInStream(stream);
    purifier->setOutQueue(gCtrlBlock->getPurifierOutQueue());

    // writer is a peer processor of purifier
    purifier->setPeerProcessor(writer);

    if (gCtrlBlock->getEndInfo()->be_info.mode == SCI_POLLING) {
        // interrupt mode
        purifier->setObserver(gCtrlBlock->getObserver());
    }

    gCtrlBlock->registerProcessor(writer);
    gCtrlBlock->registerProcessor(purifier);

    gRoutingList->propagateGroupInfo();

    writer->start();
    purifier->start();
}

int Initializer::initFE(int hndl)
{
    char *envp = NULL;
    
    Topology *topo = new Topology(hndl);
    gCtrlBlock->setTopology(topo);
    int rc = topo->init();
    if (rc != SCI_SUCCESS)
        return rc;
    gAllocator->reset();

    MessageQueue *routerInQ = new MessageQueue();
    routerInQ->setName("routerInQ");
    gCtrlBlock->registerQueue(routerInQ);
    gCtrlBlock->setRouterInQueue(routerInQ);

    RouterProcessor *router = new RouterProcessor();
    gCtrlBlock->setRouterProcessor(router);
    gCtrlBlock->registerProcessor(router);
    router->setInQueue(routerInQ);

    MessageQueue *inq = new MessageQueue();
    inq->setName("filterInQ");
    gCtrlBlock->registerQueue(inq);
    gCtrlBlock->setFilterInQueue(inq);
    
    MessageQueue *outq = new MessageQueue();
    outq->setName("filterOutQ");
    gCtrlBlock->registerQueue(outq);
    gCtrlBlock->setFilterOutQueue(outq);

    FilterProcessor *filter = new FilterProcessor();
    gCtrlBlock->registerProcessor(filter);
    gCtrlBlock->setFilterProcessor(filter);
    filter->setInQueue(inq);
    filter->setOutQueue(outq);

    ErrorDetector *errDetector = NULL;
    ErrorHandler *errHandler = NULL;
    ErrorInjector *errInjector = NULL;

    envp = ::getenv("SCI_ENABLE_FAILOVER");
    if (envp != NULL) {
        if (::strcmp(envp, "yes") == 0) {
            MessageQueue *errInQ = new MessageQueue();
            errInQ->setName("errInQ");
            gCtrlBlock->registerQueue(errInQ);
            gCtrlBlock->setErrorQueue(errInQ);

            errDetector = new ErrorDetector(hndl);
            gCtrlBlock->registerProcessor(errDetector);
            errDetector->setInQueue(errInQ);

            if (gCtrlBlock->getEndInfo()->fe_info.err_hndlr != NULL) {
                MessageQueue *errOutQ = new MessageQueue();
                errOutQ->setName("errOutQ");
                gCtrlBlock->registerQueue(errOutQ);
                errDetector->setOutQueue(errOutQ);

                errHandler = new ErrorHandler(hndl);
                gCtrlBlock->registerProcessor(errHandler);
                errHandler->setInQueue(errOutQ);
            }

            // see if we have error injection thread
            envp = ::getenv("SCI_DEBUG_USE_INJECTOR");
            if (envp != NULL) {
                if (::strcasecmp(envp, "yes") == 0) {
                    errInjector = new ErrorInjector();
                    gCtrlBlock->setErrorInjector(errInjector);
                    errInjector->setInjOutQueue(errInQ);
                }
            }
        }
    }

    HandlerProcessor *handler = NULL;
    if (gCtrlBlock->getEndInfo()->fe_info.mode == SCI_INTERRUPT) {
        // interrupt mode
        handler = new HandlerProcessor();
        gCtrlBlock->registerProcessor(handler);
        handler->setInQueue(outq);
    } else {
        // polling mode
        Observer *ob = new Observer();
        gCtrlBlock->setObserver(ob);
        gCtrlBlock->setPollQueue(outq);
        filter->setObserver(ob);
    }

    gStateMachine->parse(StateMachine::DATASTRUC_CREATED);

    router->start();
    filter->start();
    if (errDetector) {
        errDetector->start();
    }
    if (errHandler) {
        errHandler->start();
    }
    if (errInjector) {
        errInjector->start();
    }
    if (handler) {
        handler->start();
    }
    int msgID = gNotifier->allocate();
    Message *topoMsg = topo->packMsg();
    topoMsg->setID(msgID);
    routerInQ->produce(topoMsg);
    gNotifier->freeze(msgID, &rc);

    return rc;
}

int Initializer::initAgent(int hndl)
{ 
    string nodeAddr;
    int port = -1;

    // get hostname and port no from environment variable.
    char *envp = ::getenv("SCI_WORK_DIRECTORY");
    if (envp != NULL) {
        ::chdir(envp);
        log_debug("Change working directory to %s", envp);
    }

    envp = ::getenv("SCI_PARENT_HOSTNAME");
    if (envp != NULL) {
        nodeAddr = envp;
    }

    envp = ::getenv("SCI_PARENT_PORT");
    if (envp != NULL) {
        port = ::atoi(envp);
    }
    
    log_debug("My parent host is %s, parent port id %d, my ID is %d", nodeAddr.c_str(), port, hndl);

    Stream *stream = new Stream();   
    stream->init(nodeAddr.c_str(), port);
    *stream << gCtrlBlock->getJobKey() << hndl << endl;

    gCtrlBlock->registerStream(stream);
    gCtrlBlock->setParentStream(stream);
    gStateMachine->parse(StateMachine::PARENT_CONNECTED);

    ErrorDetector *errDetector = NULL;

    // err detector need to be created before relay processor
    envp = ::getenv("SCI_ENABLE_FAILOVER");
    if (envp != NULL) {
        if (::strcmp(envp, "yes") == 0) {
            MessageQueue *errInQ = new MessageQueue();
            errInQ->setName("errInQ");
            gCtrlBlock->registerQueue(errInQ);
            gCtrlBlock->setErrorQueue(errInQ);

            errDetector = new ErrorDetector(hndl);
            gCtrlBlock->registerProcessor(errDetector);
            errDetector->setInQueue(errInQ);
        }
    }
    
    MessageQueue *routerInQ = new MessageQueue();
    routerInQ->setName("routerInQ");
    gCtrlBlock->registerQueue(routerInQ);
    gCtrlBlock->setRouterInQueue(routerInQ);

    ReaderProcessor *reader = new ReaderProcessor(hndl);
    reader->setName("ReaderP");
    gCtrlBlock->registerProcessor(reader);
    reader->setInStream(stream);
    reader->setOutQueue(routerInQ);
    reader->setOutErrorQueue(gCtrlBlock->getErrorQueue());

    RouterProcessor *router = new RouterProcessor();
    gCtrlBlock->registerProcessor(router);
    gCtrlBlock->setRouterProcessor(router);
    router->setInQueue(routerInQ);

    MessageQueue *filterInQ = new MessageQueue();
    filterInQ->setName("filterInQ");
    gCtrlBlock->setFilterInQueue(filterInQ);
    gCtrlBlock->registerQueue(filterInQ);

    MessageQueue *filterOutQ = new MessageQueue();
    filterOutQ->setName("filterOutQ");
    gCtrlBlock->setFilterOutQueue(filterOutQ);
    gCtrlBlock->registerQueue(filterOutQ);

    WriterProcessor *writer = new WriterProcessor(hndl);
    writer->setName("WriterP");
    gCtrlBlock->registerProcessor(writer);
    writer->setInQueue(filterOutQ);
    writer->setOutStream(stream);

    // writer is a peer processor of reader
    reader->setPeerProcessor(writer);
    
    FilterProcessor *filter = new FilterProcessor();
    gCtrlBlock->registerProcessor(filter);
    gCtrlBlock->setFilterProcessor(filter);
    filter->setInQueue(filterInQ);
    filter->setOutQueue(filterOutQ);

    gStateMachine->parse(StateMachine::DATASTRUC_CREATED);

    reader->start();
    writer->start();
    router->start();
    filter->start();
    if (errDetector) {
        errDetector->start();
    }

    return SCI_SUCCESS;
}

int Initializer::initBE(int hndl)
{
    char *envp = ::getenv("SCI_USE_EXTLAUNCHER");
    if (envp != NULL) {
        if (::strcasecmp(envp, "yes") == 0) {
            initExtBE(hndl);
        }
    }

    string nodeAddr;
    int port = -1;

    // get hostname and port no from environment variable.
    envp = ::getenv("SCI_WORK_DIRECTORY");
    if (envp != NULL) {
        chdir(envp);
        log_debug("Change working directory to %s", envp);
    }

    envp = ::getenv("SCI_PARENT_HOSTNAME");
    if (envp != NULL) {
        nodeAddr = envp;
    }

    envp = ::getenv("SCI_PARENT_PORT");
    if (envp != NULL) {
        port = ::atoi(envp);
    }
    
    log_debug("My parent host is %s, parent port id %d, my ID is %d", nodeAddr.c_str(), port, hndl);

    Stream *stream = new Stream();   
    stream->init(nodeAddr.c_str(), port);
    *stream << gCtrlBlock->getJobKey() << hndl << endl;

    gCtrlBlock->registerStream(stream);
    gCtrlBlock->setParentStream(stream);
    gStateMachine->parse(StateMachine::PARENT_CONNECTED);

    ErrorDetector *errDetector = NULL;
    ErrorHandler *errHandler = NULL;

    // err detector need to be created before purifier processor
    envp = ::getenv("SCI_ENABLE_FAILOVER");
    if (envp != NULL) {
        if (::strcmp(envp, "yes") == 0) {
            MessageQueue *errInQ = new MessageQueue();
            errInQ->setName("errInQ");
            gCtrlBlock->registerQueue(errInQ);
            gCtrlBlock->setErrorQueue(errInQ);

            errDetector = new ErrorDetector(hndl);
            gCtrlBlock->registerProcessor(errDetector);
            errDetector->setInQueue(errInQ);

            if (gCtrlBlock->getEndInfo()->be_info.err_hndlr != NULL) {
                MessageQueue *errOutQ = new MessageQueue();
                errOutQ->setName("errOutQ");
                gCtrlBlock->registerQueue(errOutQ);
                errDetector->setOutQueue(errOutQ);

                errHandler = new ErrorHandler(hndl);
                gCtrlBlock->registerProcessor(errHandler);
                errHandler->setInQueue(errOutQ);
            }
        }
    }
    
    MessageQueue *userQ = new MessageQueue();
    userQ->setName("userQ");
    gCtrlBlock->registerQueue(userQ);
    gCtrlBlock->setUpQueue(userQ);
    envp = ::getenv("SCI_SYNC_INIT");
    if (envp && (strcasecmp(envp, "yes") == 0)) {
        int msgID = atoi(::getenv("SCI_INIT_ACKID"));
        Message *msg = new Message();
        msg->build(SCI_FILTER_NULL, hndl, 0, NULL, NULL, Message::INIT_ACK, msgID);
        userQ->produce(msg);
    }

    MessageQueue *sysQ = new MessageQueue();
    sysQ->setName("sysQ");
    gCtrlBlock->setPurifierOutQueue(sysQ);
    gCtrlBlock->registerQueue(sysQ);

    WriterProcessor *writer = new WriterProcessor(hndl);
    gCtrlBlock->registerProcessor(writer);
    writer->setInQueue(userQ);
    writer->setOutStream(stream);

    PurifierProcessor *purifier = new PurifierProcessor(hndl);
    gCtrlBlock->registerProcessor(purifier);
    purifier->setInStream(stream);
    purifier->setOutQueue(sysQ);
    purifier->setOutErrorQueue(gCtrlBlock->getErrorQueue());

    // writer is a peer processor of purifier
    purifier->setPeerProcessor(writer);

    HandlerProcessor *handler = NULL;
    if (gCtrlBlock->getEndInfo()->be_info.mode == SCI_INTERRUPT) {
        // interrupt mode
        handler = new HandlerProcessor();
        gCtrlBlock->registerProcessor(handler);
        handler->setInQueue(sysQ);
    } else {
        // polling mode
        Observer *ob = new Observer();
        gCtrlBlock->setObserver(ob);
        gCtrlBlock->setPollQueue(sysQ);
        purifier->setObserver(ob);
    }

    gRoutingList->addBE(SCI_GROUP_ALL, VALIDBACKENDIDS, hndl);
    gStateMachine->parse(StateMachine::DATASTRUC_CREATED);

    writer->start();
    purifier->start();
    if (errDetector) {
        errDetector->start();
    }
    if (errHandler) {
        errHandler->start();
    }
    if (handler) {
        handler->start();
    }

    return SCI_SUCCESS;
}

void Initializer::initExtBE(int hndl)
{
    string envStr;
    char hostname[256];

    Stream stream;
    struct passwd *pwd = ::getpwuid(::getuid());
    string username = pwd->pw_name;

    ::gethostname(hostname, sizeof(hostname));
    stream.init(hostname, SCI_DAEMON_PORT);
    stream << username << (int) Launcher::REQUEST << gCtrlBlock->getJobKey() << hndl << endl;
    stream >> envStr >> endl;
        
    stream.stop();

    string key, val;
    char *st = (char *) envStr.c_str();
    char *p = st + envStr.size();
    while (p > st) {
        p--;
        if ((*p) == '=') {
            *p = '\0';
            val = (p+1);
        } else if ((*p) == ';') {
            *p = '\0';
            key = (p+1);
            ::setenv(key.c_str(), val.c_str(), 1);
        }
    }
}

void Initializer::initListener()
{
    Listener *listener = new Listener(gCtrlBlock->getMyHandle());
    gCtrlBlock->setListener(listener);
    
    listener->init();
    listener->start();
}

