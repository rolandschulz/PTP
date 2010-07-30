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

 Classes: CtrlBlock

 Description: Internal running information management (Note: STL does not 
              guarantee the safety of several readers & one writer cowork 
              together, and user threads can query group information at 
              runtime, so it's necessary to add a lock to protect these 
              read & write operations).
   
 Author: Tu HongJ, Nicole Nie, Liu Wei

 History:
   Date     Who ID    Description
   -------- --- ---   -----------
   10/06/08 tuhongj      Initial code (D153875)

****************************************************************************/

#include "ctrlblock.hpp"
#include <assert.h>
#include <stdlib.h>
#include <string.h>

#include "stream.hpp"
#include "exception.hpp"
#include "group.hpp"
#include "log.hpp"
#include "tools.hpp"

#include "statemachine.hpp"
#include "eventntf.hpp"
#include "topology.hpp"
#include "routinglist.hpp"
#include "message.hpp"
#include "queue.hpp"
#include "listener.hpp"
#include "errinjector.hpp"
#include "processor.hpp"
#include "routerproc.hpp"
#include "filterproc.hpp"
#include "observer.hpp"
#include "parent.hpp"

const long long FLOWCTL_THRESHOLD = 1024 * 1024 * 1024 * 2LL;

CtrlBlock * CtrlBlock::instance = NULL;

CtrlBlock::CtrlBlock()
    : role(INVALID)
{
    endInfo = NULL;
    topoInfo = NULL;
    
    listener = NULL;
    errInjector = NULL;
    routerProc = NULL;
    filterProc = NULL;
    observer = NULL;

    routerInQueue = NULL;
    filterInQueue = NULL;
    filterOutQueue = NULL;
    purifierOutQueue = NULL;
    upQueue = NULL;
    pollQueue = NULL;
    monitorInQueue = NULL;
    errorQueue = NULL;

    parentStream = NULL;

    queues.clear();
    streams.clear();
    processors.clear();

    queueInfo.clear();

    enabled = false;
    ctrlID = gNotifier->allocate();

    // flow control threshold
    thresHold = FLOWCTL_THRESHOLD;
    char *envp = getenv("SCI_FLOWCTL_THRESHOLD");
    if(envp != NULL) {
        thresHold = ::atoll(envp);
    } 

    ::pthread_mutex_init(&mtx, NULL);
}

CtrlBlock::~CtrlBlock()
{
    ::pthread_mutex_destroy(&mtx);

    instance = NULL;
}

CtrlBlock::ROLE CtrlBlock::getMyRole() 
{
    return role; 
}

int CtrlBlock::getMyHandle() 
{ 
    return handle; 
}

sci_info_t * CtrlBlock::getEndInfo() 
{ 
    return endInfo; 
}

int CtrlBlock::getJobKey() 
{ 
    return jobKey; 
}

int CtrlBlock::initFE(int hndl, sci_info_t * info)
{
    role = FRONT_END;
    handle = hndl;
    endInfo = (sci_info_t *) ::malloc(sizeof(sci_info_t));
    if (NULL == endInfo) {
        return SCI_ERR_NO_MEM;
    }
    
    ::memset(endInfo, 0, sizeof(sci_info_t));
    ::memcpy(endInfo, info, sizeof(sci_info_t));

    char *envp = ::getenv("SCI_JOB_KEY");
    if (envp) {
        // use user's job key
        jobKey = ::atoi(envp);
    } else {
        // generate a random job key
        ::srand((unsigned int) ::time(NULL));
        jobKey = ::rand();
    }

    gStateMachine->reset();
    return SCI_SUCCESS;
}

int CtrlBlock::initAgent(int hndl)
{
    role = AGENT;
    handle = hndl;

    char *envp = ::getenv("SCI_JOB_KEY");
    if (envp == NULL)
        return SCI_ERR_INVALID_JOBKEY;
    jobKey = ::atoi(envp);
    
    gStateMachine->reset();
    return SCI_SUCCESS;
}

int CtrlBlock::initBE(int hndl, sci_info_t * info)
{
    role = BACK_END;
    handle = hndl;
    endInfo = (sci_info_t *) ::malloc(sizeof(sci_info_t));
    if (NULL == endInfo) {
        return SCI_ERR_NO_MEM;
    }
    
    ::memcpy(endInfo, info, sizeof(sci_info_t));
    
    handle = hndl;

    char *envp = ::getenv("SCI_JOB_KEY");
    if (envp == NULL)
        return SCI_ERR_INVALID_JOBKEY;
    jobKey = ::atoi(envp);

    gStateMachine->reset();
    return SCI_SUCCESS;
}

void CtrlBlock::term()
{
    gNotifier->freeze(ctrlID, NULL);

    // stop listener if have
    if (listener != NULL) {
        listener->stop();
        listener->join();
    }

    // stop error injector if have
    if (errInjector != NULL) {
        errInjector->stop();
        errInjector->join();
    }

    // produce a NULL message in all message queues
    QUEUE_VEC::iterator qit = queues.begin();
    for (; qit!=queues.end(); ++qit) {
        (*qit)->notify();
    }

    // close all streams
    STREAM_VEC::iterator sit = streams.begin();
    for (; sit!=streams.end(); ++sit) {
        (*sit)->stop();
    }

    // waiting for all processor threads terminate
    PROC_VEC::iterator pit = processors.begin();
    for (; pit!=processors.end(); ++pit) {
        while (!(*pit)->isLaunched()) {
            // before join, this thread should have been launched
            SysUtil::sleep(1000);
        }
        (*pit)->join();
    }

    clean();
}

void CtrlBlock::clean()
{
    // delete all registered processors
    PROC_VEC::iterator pit = processors.begin();
    for (; pit!=processors.end(); ++pit) {
        (*pit)->dump();
        delete (*pit);
    }
    processors.clear();

    // delete all registered streams
    STREAM_VEC::iterator sit = streams.begin();
    for (; sit!=streams.end(); ++sit) {
        delete (*sit);
    }
    streams.clear();

    // delete all registered message queues
    QUEUE_VEC::iterator qit = queues.begin();
    for (; qit!=queues.end(); ++qit) {
        delete (*qit);
    }
    queues.clear();

    queueInfo.clear();

    // delete listener
    if (listener != NULL) {
        delete listener;
        listener = NULL;
    }

    // delete error injector
    if (errInjector!= NULL) {
        delete errInjector;
        errInjector = NULL;
    }

    routerProc = NULL;
    filterProc = NULL;

    routerInQueue = NULL;
    filterInQueue = NULL;
    filterOutQueue = NULL;
    purifierOutQueue = NULL;
    upQueue = NULL;
    pollQueue = NULL;
    monitorInQueue = NULL;
    errorQueue = NULL;

    parentStream = NULL;

    if (observer != NULL) {
        delete observer;
        observer = NULL;
    }

    if (topoInfo) {
        delete topoInfo;
        topoInfo = NULL;
    }

    gStateMachine->parse(StateMachine::DATASTUCT_CLEANED);

    role = INVALID;
    if (endInfo) {
        ::free(endInfo);
        endInfo = NULL;
    }
}


void CtrlBlock::enable()
{
    enabled = true;
}

void CtrlBlock::disable()
{
    if (!enabled) // already disabled?
        return;
    
    gNotifier->notify(ctrlID);
    enabled = false;
}

bool CtrlBlock::isEnabled() 
{ 
    return enabled;
}

void CtrlBlock::notifyPollQueue()
{
    // so far, valid for polling mode only
    assert(role != AGENT);
    observer->notify();
    Message *msg = new Message(Message::INVALID_POLL);
    pollQueue->produce(msg);
}

void CtrlBlock::setTopology(Topology *topo) 
{ 
    topoInfo = topo; 
}

void CtrlBlock::setListener(Listener *li) 
{
    listener = li;
}

void CtrlBlock::setObserver(Observer *ob) 
{
    observer = ob;
}

void CtrlBlock::setErrorInjector(ErrorInjector *injector) 
{
    errInjector = injector;
}

Topology * CtrlBlock::getTopology() 
{ 
    return topoInfo;
}

Listener * CtrlBlock::getListener() 
{
    return listener;
}

Observer * CtrlBlock::getObserver() {
    return observer;
}

void CtrlBlock::registerQueue(MessageQueue *queue) 
{
    queues.push_back(queue);
}

void CtrlBlock::registerProcessor(Processor *proc) 
{
    processors.push_back(proc);
}

void CtrlBlock::registerStream(Stream *stream) 
{
    streams.push_back(stream);
}

/* need lock protection */
void CtrlBlock::mapQueue(int hndl, MessageQueue *queue) 
{
    lock();
    queueInfo[hndl] = queue;
    unlock();
}

/* need lock protection */
MessageQueue * CtrlBlock::queryQueue(int hndl) 
{
    MessageQueue *queue = NULL;

    lock();
    QUEUE_MAP::iterator qit = queueInfo.find(hndl);
    if (qit != queueInfo.end()) {
        queue = (*qit).second;
    }
    unlock();
    
    return queue;
}

void CtrlBlock::setRouterInQueue(MessageQueue * queue)
{
    routerInQueue = queue;
}

void CtrlBlock::setFilterInQueue(MessageQueue *queue) 
{
    filterInQueue = queue;
}

void CtrlBlock::setFilterOutQueue(MessageQueue *queue) 
{
    filterOutQueue = queue;
}

void CtrlBlock::setPurifierOutQueue(MessageQueue *queue) 
{
    purifierOutQueue = queue;
}

void CtrlBlock::setUpQueue(MessageQueue * queue)
{
    upQueue = queue;
}

void CtrlBlock::setPollQueue(MessageQueue *queue) 
{
    pollQueue = queue;
}

void CtrlBlock::setMonitorInQueue(MessageQueue *queue) 
{
    monitorInQueue = queue;
}

void CtrlBlock::setErrorQueue(MessageQueue *queue) 
{
    errorQueue = queue;
}

MessageQueue * CtrlBlock::getRouterInQueue()
{
    return routerInQueue;
}

MessageQueue * CtrlBlock::getFilterInQueue() 
{
    return filterInQueue;
}

MessageQueue * CtrlBlock::getFilterOutQueue() 
{
    return filterOutQueue;
}

MessageQueue * CtrlBlock::getPurifierOutQueue() 
{
    return purifierOutQueue;
}

MessageQueue * CtrlBlock::getUpQueue()
{
    return upQueue;
}

MessageQueue * CtrlBlock::getPollQueue() 
{
    return pollQueue;
}

MessageQueue * CtrlBlock::getErrorQueue() 
{
    return errorQueue;
}

MessageQueue * CtrlBlock::getMonitorInQueue() 
{
    return monitorInQueue;
}

void CtrlBlock::setRouterProcessor(RouterProcessor *proc) 
{
    routerProc = proc;
}

void CtrlBlock::setFilterProcessor(FilterProcessor *proc) 
{
    filterProc = proc;
}
        
RouterProcessor * CtrlBlock::getRouterProcessor() 
{
    return routerProc;
}
        
FilterProcessor * CtrlBlock::getFilterProcessor() 
{
    return filterProc;
}

void CtrlBlock::setParentStream(Stream * stream)
{
    parentStream = stream;
}

Stream * CtrlBlock::getParentStream()
{
    return parentStream;
}

void CtrlBlock::setFlowctlThreshold(long long th)
{
    thresHold = th;
}

long long CtrlBlock::getFlowctlThreshold()
{
    return thresHold;
}

void CtrlBlock::genSelfInfo(MessageQueue * queue, bool isUncle)
{
    assert(queue);

    // generate this message only when turn on failover mechanism
    char *envp = ::getenv("SCI_ENABLE_FAILOVER");
    if (envp != NULL) {
        if (::strcmp(envp, "yes") == 0) {
            char tmp[256] = {0};
            string envStr;

            ::gethostname(tmp, sizeof(tmp));
            string localName = SysUtil::get_hostname(tmp);
            
            Parent parent(handle, localName.c_str(), listener->getBindPort());
            parent.setLevel(topoInfo->getLevel());

            Message *msg = parent.packMsg(isUncle);
            queue->produce(msg);
        }
    }
}

void CtrlBlock::lock()
{
    ::pthread_mutex_lock(&mtx);
}

void CtrlBlock::unlock()
{
    ::pthread_mutex_unlock(&mtx);
}

