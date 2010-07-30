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

 Classes: Thread, ThreadException

 Description: Thread manipulation.
   
 Author: Tu HongJ, Liu Wei

 History:
   Date     Who ID    Description
   -------- --- ---   -----------
   10/06/08 tuhongj      Initial code (D153875)

****************************************************************************/

#include <assert.h>
#include <string.h>
#include <signal.h>

#include "thread.hpp"

using namespace std;

void* init(void * pthis)
{
    sigset_t sigs_to_block;
    sigset_t old_sigs;
    sigfillset(&sigs_to_block);
    pthread_sigmask(SIG_SETMASK, &sigs_to_block, &old_sigs);

    Thread *p = (Thread *) pthis;
    pthread_setcancelstate(PTHREAD_CANCEL_ENABLE, NULL);
    pthread_setcanceltype(PTHREAD_CANCEL_ASYNCHRONOUS, NULL);
    p->setState(true);
    p->run();
    pthread_sigmask(SIG_SETMASK, &old_sigs, NULL);

    return 0;
}

Thread::Thread(int hndl)
    : handle(hndl), launched(false), running(false)
{
}

Thread::~Thread() 
{
}

void Thread::start()
{
    if (!launched) {
        if (pthread_create(&(thread), NULL, init, this) == 0) {
            launched = true;
        } else {
            running = false;
            throw ThreadException(ThreadException::ERR_CREATE);
        }
    } else {
        throw ThreadException(ThreadException::ERR_LAUNCH);
    }
}

void Thread::join()
{
    if (!launched)
        return;
    
    pthread_join(thread, NULL);
    running = false;
}

void Thread::detach()
{
    if (launched) {
        pthread_detach(thread);
    } else {
        throw ThreadException(ThreadException::ERR_DETACH);
    }
}

void Thread::cancel()
{
    pthread_cancel(thread);
}

ThreadException::ThreadException(int code) throw()
    : errCode(code)
{
}

int ThreadException::getErrCode() const throw()
{
    return errCode;
}

