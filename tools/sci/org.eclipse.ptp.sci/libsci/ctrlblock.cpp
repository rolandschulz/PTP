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

#include "eventntf.hpp"
#include "handlerproc.hpp"
#include "embedagent.hpp"
#include "purifierproc.hpp"
#include "topology.hpp"
#include "routinglist.hpp"
#include "privatedata.hpp"
#include "message.hpp"
#include "queue.hpp"
#include "listener.hpp"
#include "processor.hpp"
#include "routerproc.hpp"
#include "filterproc.hpp"
#include "observer.hpp"

const long long FLOWCTL_THRESHOLD = 1024 * 1024 * 1024 * 2LL;

CtrlBlock * CtrlBlock::instance = NULL;
extern SCI_msg_hndlr *gHndlr;
extern void *gParam;

CtrlBlock::CtrlBlock()
    : role(INVALID)
{
    endInfo = NULL;
    
    routerProc = NULL;
    filterProc = NULL;
    purifierProc = NULL;
    handlerProc = NULL;
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
    embedAgents.clear();
    enableID = gNotifier->allocate();

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
    instance = NULL;
    ::pthread_mutex_destroy(&mtx);
}

CtrlBlock::ROLE CtrlBlock::getMyRole() 
{
    return role; 
}

void CtrlBlock::setMyRole(CtrlBlock::ROLE ro) 
{
    role = ro; 
}

int CtrlBlock::getMyHandle() 
{ 
    return handle; 
}

void CtrlBlock::setMyHandle(int hndl) 
{ 
    handle = hndl;
}

sci_info_t * CtrlBlock::getEndInfo() 
{ 
    return endInfo; 
}

int CtrlBlock::getJobKey() 
{ 
    return jobKey; 
}

void CtrlBlock::setJobKey(int key) 
{ 
    jobKey = key;
}

int CtrlBlock::init(sci_info_t * info)
{
    char *envp = NULL;

    if (info == NULL) {
        role = AGENT;
        return SCI_SUCCESS;
    } 

    endInfo = (sci_info_t *) ::malloc(sizeof(sci_info_t));
    if (NULL == endInfo) {
        return SCI_ERR_NO_MEM;
    }
    ::memset(endInfo, 0, sizeof(sci_info_t));
    ::memcpy(endInfo, info, sizeof(sci_info_t));
    gHndlr = info->be_info.hndlr;
    gParam = info->be_info.param;

    switch (info->type) {
        case SCI_FRONT_END:
            handle = -1;
            role = FRONT_END;
            envp = ::getenv("SCI_JOB_KEY");
            if (envp) {
                // use user's job key
                jobKey = ::atoi(envp);
            } else {
                // generate a random job key
                ::srand((unsigned int) ::time(NULL));
                jobKey = ::rand();
            }
            break;
        case SCI_BACK_END:
            role = BACK_END;
            envp = ::getenv("SCI_JOB_KEY");
            if (envp != NULL)
                jobKey = ::atoi(envp);
            envp = ::getenv("SCI_CLIENT_ID");
            if (envp != NULL)
                handle = ::atoi(envp);
            break;
        default:
            return SCI_ERR_INVALID_ENDTYPE;
    }

    return SCI_SUCCESS;
}

int CtrlBlock::numOfChildrenFds()
{
    int num = 0;
    RoutingList *rtList = NULL;
/*
    if (purifierProc) {
        while (!purifierProc->isLaunched()) {
            // before join, this thread should have been launched
            SysUtil::sleep(1000);
        } 
    } */
    lock();
    AGENT_MAP::iterator it;
    for (it = embedAgents.begin(); it != embedAgents.end(); it++) {
        rtList = it->second->getRoutingList();
        num += rtList->numOfStreams();
    }
    unlock();

    return num;
}

int CtrlBlock::getChildrenSockfds(int *fds)
{
    int pos = 0;
    RoutingList *rtList = NULL;
/*
    if (purifierProc) {
        while (!purifierProc->isLaunched()) {
            // before join, this thread should have been launched
            SysUtil::sleep(1000);
        } 
    } */
    lock();
    AGENT_MAP::iterator it;
    for (it = embedAgents.begin(); it != embedAgents.end(); it++) {
        rtList = it->second->getRoutingList();
        pos += rtList->getStreamsSockfds(&fds[pos]);
    }
    unlock();

    return pos;
}

void CtrlBlock::term()
{
    gNotifier->freeze(enableID, NULL);
    if (purifierProc) {
        purifierProc->release();
        delete purifierProc;
    }
    lock();
    AGENT_MAP::iterator it;
    for (it = embedAgents.begin(); it != embedAgents.end(); it++) {
        delete it->second;
    }
    embedAgents.clear();
    unlock();
    if (handlerProc) {
        handlerProc->release();
        delete handlerProc;
    }
    clean();
}

EmbedAgent * CtrlBlock::getAgent(int hndl)
{
    EmbedAgent *agent;
    lock();
    assert(embedAgents.find(hndl) != embedAgents.end());
    agent = embedAgents[hndl];
    unlock();

    return agent;
}

void CtrlBlock::clean()
{
    routerProc = NULL;
    filterProc = NULL;
    purifierProc = NULL;

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

    role = INVALID;
    if (endInfo) {
        ::free(endInfo);
        endInfo = NULL;
    }
}

void CtrlBlock::enable()
{
}

void CtrlBlock::disable()
{
    if (!isEnabled())
        return;

    gNotifier->notify(enableID);
}

bool CtrlBlock::isEnabled() 
{ 
    return gNotifier->getState(enableID);
}

void CtrlBlock::releasePollQueue()
{
    // so far, valid for polling mode only
    assert(role != AGENT);
    observer->notify();
    Message *msg = new Message(Message::INVALID_POLL);
    pollQueue->produce(msg);
}

void CtrlBlock::setObserver(Observer *ob) 
{
    observer = ob;
}

Topology * CtrlBlock::getTopology() 
{ 
    PrivateData *pData = (PrivateData *)pthread_getspecific(Thread::key);
    return pData->getRoutingList()->getTopology();
}

Observer * CtrlBlock::getObserver() {
    return observer;
}

void CtrlBlock::setRouterInQueue(MessageQueue * queue)
{
    routerInQueue = queue;
}

void CtrlBlock::setFilterInQueue(MessageQueue *queue) 
{
    filterInQueue = queue;
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
        
void CtrlBlock::setHandlerProcessor(HandlerProcessor *proc) 
{
    handlerProc = proc;
}
        
void CtrlBlock::setPurifierProcessor(PurifierProcessor *proc) 
{
    purifierProc = proc;
}
        
void CtrlBlock::setUpQueue(MessageQueue * queue)
{
    upQueue = queue;
}

MessageQueue * CtrlBlock::getUpQueue()
{
        return upQueue;
}

RouterProcessor * CtrlBlock::getRouterProcessor() 
{
    PrivateData *pData = (PrivateData *)pthread_getspecific(Thread::key);
    return pData->getRouterProcessor();
}
        
FilterProcessor * CtrlBlock::getFilterProcessor() 
{
    PrivateData *pData = (PrivateData *)pthread_getspecific(Thread::key);
    return pData->getFilterProcessor();
}

FilterList * CtrlBlock::getFilterList() 
{
    PrivateData *pData = (PrivateData *)pthread_getspecific(Thread::key);
    return pData->getFilterList();
}

PurifierProcessor * CtrlBlock::getPurifierProcessor() 
{
    return purifierProc;
}

void CtrlBlock::setFlowctlThreshold(long long th)
{
    thresHold = th;
}

long long CtrlBlock::getFlowctlThreshold()
{
    return thresHold;
}

RoutingList * CtrlBlock::getRoutingList()
{
    PrivateData *pData = (PrivateData *)pthread_getspecific(Thread::key);
    return pData->getRoutingList();
}

void CtrlBlock::addEmbedAgent(int hndl, EmbedAgent *agent)
{
    lock();
    embedAgents[hndl] = agent;
    unlock();
}

void CtrlBlock::lock()
{
    ::pthread_mutex_lock(&mtx);
}

void CtrlBlock::unlock()
{
    ::pthread_mutex_unlock(&mtx);
}

