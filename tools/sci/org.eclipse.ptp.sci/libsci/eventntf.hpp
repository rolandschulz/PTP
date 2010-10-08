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

#ifndef _EVENTNTF_HPP
#define _EVENTNTF_HPP

#include <pthread.h>

const int MAX_SERIAL_NUM = 1024;

struct serialNtfTest 
{
    bool    freezed; // freeze() been called?
    bool    notified; // notify() been called?
    bool    used;    // allocate() been called?
    void    *ret;
};

class EventNotify 
{
    private:
        static EventNotify *notifier;
        EventNotify();
        pthread_mutex_t     mtx;
        pthread_cond_t      cond;
        int                 serialNum;
        struct serialNtfTest   serialTest[MAX_SERIAL_NUM];

    public:
        ~EventNotify();
        static EventNotify * getInstance() {
            if (notifier == NULL)
                notifier = new EventNotify();
            return notifier;
        }

        int allocate();
        void freeze(int id, void *ret_val = NULL);
        void notify(int id);
        void * getRetVal(int id);
        bool getState(int id);

    private:
        bool test(int id);
        void tryFreeze();

        void lock();
        void unlock();
};

#define gNotifier EventNotify::getInstance()

#endif

