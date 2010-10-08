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

 Classes: MessageQueue

 Description: Messages manipulation.
   
 Author: Tu HongJ, Nicole Nie, Liu Wei

 History:
   Date     Who ID    Description
   -------- --- ---   -----------
   10/06/08 tuhongj      Initial code (D153875)

****************************************************************************/

#include "queue.hpp"
#include <stdlib.h>
#include <sys/time.h>
#include <time.h>
#include <errno.h>
#include <assert.h>

#include "exception.hpp"
#include "ctrlblock.hpp"
#include "log.hpp"

#include "atomic.hpp"
#include "message.hpp"
#include "tools.hpp"

MessageQueue::MessageQueue(bool ctl)
    : thresHold(0), flowCtl(ctl)
{
    ::pthread_mutex_init(&mtx, NULL);
    ::sem_init(&sem, 0, 0);
}

MessageQueue::~MessageQueue()
{
    Message *msg = NULL;
    while (!queue.empty()) {
        msg = queue.front();
        queue.pop_front();
        if (decRefCount(msg->getRefCount()) == 0) {
            delete msg;
        }
    }
    queue.clear();
    
    ::pthread_mutex_destroy(&mtx);
    ::sem_destroy(&sem);
}

int MessageQueue::flowControl(int size)
{
    long long flowctlThreshold = gCtrlBlock->getFlowctlThreshold();

    if(flowCtl) {
        if ((gCtrlBlock->getMyRole() != CtrlBlock::BACK_END) && (size > 0)) {
            while (thresHold > flowctlThreshold) {
                SysUtil::sleep(1000);
            }   
        }
    }

    return 0;
}

int MessageQueue::multiProduce(Message **msgs, int num)
{
    assert(msgs && (num > 0));
    int i;
    int len = 0;

    for (i = 0; i < num; i++) {
        assert(msgs[i]);
        len += msgs[i]->getContentLen();
    }
    lock();
    for (i = 0; i < num; i++) {
        queue.push_back(msgs[i]);
        ::sem_post(&sem);
    }
    
    if(flowCtl) {
        thresHold += len;
    }

    unlock();
    flowControl(len);

    return 0;
}

void MessageQueue::release()
{
    ::sem_post(&sem);
}

void MessageQueue::produce(Message *msg)
{
    int len = 0; 

    if (!msg) {
        return;
    }
    len = msg->getContentLen();
    lock();
    queue.push_back(msg);
    if(flowCtl) {
        thresHold += len;
    }

    unlock();
    ::sem_post(&sem);
    flowControl(len);

    return;
}

int  MessageQueue::multiConsume(Message **msgs, int num)
{
    int i;
    int len = 0;

    for (i = 0; i < num; i++) {
        if (sem_wait_i(&sem, -1) != 0) {
            return -1;
        }
    }
    lock();
    for (i = 0; i < num; i++) {
        msgs[i] = queue.front();
        queue.pop_front();
        len += msgs[i]->getContentLen();
    }
    if (flowCtl) {
        thresHold -= len;
    }

    unlock();

    return 0;
}

Message* MessageQueue::consume(int millisecs)
{
    int len = 0;

    if (sem_wait_i(&sem, millisecs*1000) != 0) {
        return NULL;
    }

    Message *msg = NULL;

    lock();
    if (!queue.empty()) {
        msg = queue.front();
        len = msg->getContentLen();
        if (flowCtl) {
            thresHold -= len;
        }
    }
    unlock();

    return msg;
}

void MessageQueue::remove()
{
    Message *msg = NULL;

    lock();
    if (queue.empty()) {
        unlock();
        return;
    }

    msg = queue.front();
    queue.pop_front();
    unlock();
    if (decRefCount(msg->getRefCount()) == 0) {
        delete msg;
    }
}

int MessageQueue::getSize() 
{
    int size;

    lock();
    size = queue.size();
    unlock();

    return size;
}

void MessageQueue::setName(char *str)
{
    name = str;
    if (name == "filterInQ") { 
        flowCtl = true;
    }
}

string MessageQueue::getName()
{
    return name;
}

int MessageQueue::sem_wait_i(sem_t *psem, int usecs)
{
    int rc = 0;
#ifdef __APPLE__
    int sleep_time = usecs > 10 ? 10 : usecs;
#endif

    if (usecs < 0) {
        while (((rc = ::sem_wait(psem)) != 0) && (errno == EINTR));
        return rc;
    } else { 
#ifndef __APPLE__
        timespec ts;
        ::clock_gettime(CLOCK_REALTIME, &ts);    // get current time
        ts.tv_nsec += (usecs % 1000000) * 1000;
        int ca = (ts.tv_nsec >= 1000000000) ? 1 : 0;
        ts.tv_nsec %= 1000000000;
        ts.tv_sec += (usecs / 1000000) + ca;
        
        while (((rc=::sem_timedwait(psem, &ts))!=0) && (errno == EINTR));
#else
        while (((rc = sem_trywait(psem)) != 0)
        		&& ((errno == EAGAIN) || (errno == EINTR))
        		&& (usecs > 0)) {
        	usleep(sleep_time);
        	usecs -= sleep_time;
        }
#endif
        return rc;   
    }                                                 
}

void MessageQueue::lock()
{
    ::pthread_mutex_lock(&mtx);
}

void MessageQueue::unlock()
{
    ::pthread_mutex_unlock(&mtx);
}

