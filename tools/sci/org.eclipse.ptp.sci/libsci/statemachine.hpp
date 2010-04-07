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

 Classes: StateMachine

 Description: SCI state Machine
   
 Author: Nicole

 History:
   Date     Who ID    Description
   -------- --- ---   -----------
   02/25/08 nieyy         Initial code (D153875)

****************************************************************************/

#ifndef _STATEMACHINE_HPP
#define _STATEMACHINE_HPP

#include <pthread.h>

#include <vector>

using namespace std;

class StateMachine
{
    public:
        enum STATE {
            UNINITIALIZED,
            STARTING,
            RUNNING,
            IDLING,
            EXITING
        };

        enum EVENT {
            DATASTRUC_CREATED,
            DATASTUCT_CLEANED,
            CLIENT_CONNECTED,
            CLIENT_BROKEN,
            PARENT_CONNECTED,
            PARENT_BROKEN,
            USER_QUIT,
            FATAL_EXCEPTION,
            RECOVER_OK,
            RECOVER_FAILED
        };

        typedef vector<EVENT> EVENT_VEC;
        
    private:
        static StateMachine *instance;
        StateMachine();
        
        pthread_mutex_t     mtx;
        STATE               state;
        int                 connected;
        int                 disconnected;
        bool                toQuit;

        EVENT_VEC           history;

    public:
        ~StateMachine();
        static StateMachine * getInstance() {
            if (instance == NULL)
                instance = new StateMachine();
            return instance;
        }

        void parse(EVENT e);
        void reset();
        STATE getState();
        bool isToQuit(int handle);

        void dump();
        void doAssert(bool epxression = false);

    private:
        void lock() { ::pthread_mutex_lock(&mtx); }
        void unlock() { ::pthread_mutex_unlock(&mtx);}

        void parseFE(EVENT e);
        void parseAgent(EVENT e);
        void parseBE(EVENT e);
};

#define gStateMachine StateMachine::getInstance()

#endif

