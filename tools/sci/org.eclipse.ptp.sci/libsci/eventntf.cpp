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

 Classes: EventNotify

 Description: Synchronization between threads
   
 Author: Tu HongJ

 History:
   Date     Who ID    Description
   -------- --- ---   -----------
   12/05/08 tuhongj      Initial code (D154660)

****************************************************************************/

#include "eventntf.hpp"
#include <unistd.h>
#include <string.h>
#include <assert.h>

#include "tools.hpp"

EventNotify * EventNotify::notifier = NULL;

EventNotify::EventNotify()
    : serialNum(0)
{
    ::pthread_mutex_init(&mtx, NULL);
    ::pthread_cond_init(&cond, NULL);
    ::memset(serialTest, 0, sizeof(serialTest));
}

EventNotify::~EventNotify()
{
    ::pthread_cond_destroy(&cond);
    ::pthread_mutex_destroy(&mtx);

    notifier = NULL;
}

int EventNotify::allocate()
{
    int num;

    lock();
    do {
        serialNum = (serialNum + 1) % MAX_SERIAL_NUM;
    } while (serialTest[serialNum].used == true);
    num = serialNum;
    serialTest[serialNum].used = true;
    unlock();

    return num;
}

void EventNotify::freeze(int id, void *ret_val)
{
    lock();
    serialTest[id].ret = ret_val;
    serialTest[id].notified = false;
    serialTest[id].freezed = true;
    while (serialTest[id].notified == false) {
        ::pthread_cond_wait(&cond, &mtx);
    }
    serialTest[id].freezed == false;
    serialTest[id].used = false;
    unlock();
}

void EventNotify::notify(int id)
{
    test(id);
    lock();
    serialTest[id].used = false;
    serialTest[id].notified = true;
    ::pthread_cond_broadcast(&cond); 
    unlock();
}

void * EventNotify::getRetVal(int id)
{
    test(id);
    return serialTest[id].ret;
}

bool EventNotify::getState(int id)
{
    bool state;

    assert((id >= 0) && (id < MAX_SERIAL_NUM));
    lock();
    state = serialTest[id].used;
    unlock();

    return state;
}

bool EventNotify::test(int id)
{
    assert((id >= 0) && (id < MAX_SERIAL_NUM));
    while (serialTest[id].freezed == false) {
        /* Almost impossible running into here */
        SysUtil::sleep(1000);
    }
    assert(serialTest[id].used = true);
    
    return true;
}

void EventNotify::tryFreeze()
{
    lock();
    while(serialTest[serialNum].freezed == true) {
        ::pthread_cond_wait(&cond, &mtx);
    }
    unlock();
}

void EventNotify::lock()
{
    ::pthread_mutex_lock(&mtx);
}

void EventNotify::unlock()
{
    ::pthread_mutex_unlock(&mtx);
}

