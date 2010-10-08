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

#ifndef _QUEUE_HPP
#define _QUEUE_HPP

#include <deque>
#include <string>

using namespace std;

#include <semaphore.h>
#include <pthread.h>

#include "sci.h"
#include "general.hpp"

#include "stream.hpp"

class Message;

class MessageQueue 
{      
    private:
        deque<Message*>                 queue;
        pthread_mutex_t                 mtx;
        sem_t                           sem;

        string                          name;
        volatile long long              thresHold;
        bool                            flowCtl;

    public:
        MessageQueue(bool ctl = false);
        ~MessageQueue();

        void produce(Message *msg);
        void release();
        int multiProduce(Message **msgs, int num);
        int multiConsume(Message **msgs, int num);
        Message *consume(int millisecs=-1);
        void remove();

        int getSize();

        void setName(char *str); 
        string getName();

    private:
        int sem_wait_i(sem_t *psem, int usecs);

        void lock();
        void unlock();
        int flowControl(int size);
};

#endif

