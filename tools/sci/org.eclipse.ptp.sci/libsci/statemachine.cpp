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

#include "statemachine.hpp"
#include <assert.h>
#include <stdio.h>
#include <unistd.h>
#include <string.h>

#include "sci.h"

#include "log.hpp"

#include "ctrlblock.hpp"
#include "routinglist.hpp"

const char * StateMsg[] = {
    "uninitialized",
    "starting",
    "running",
    "idling",
    "exiting"
};

const char * EventMsg[] = {
    "data structures were created",
    "data structure were cleaned",
    "a child was connected",
    "a child was disconnected",
    "connected to the parent",
    "lost connection to the parent",
    "received a quit signal from the front end",
    "a fatal exception occured",
    "connection to the parent is recovered",
    "failed recover the connection to the parent"
};

StateMachine * StateMachine::instance = NULL;

StateMachine::StateMachine()
{
    ::pthread_mutex_init(&mtx, NULL);
    reset();
}

StateMachine::~StateMachine()
{
    ::pthread_mutex_destroy(&mtx);

    instance = NULL;
}

void StateMachine::parse(StateMachine::EVENT e)
{
    lock();
    history.push_back(e);
    if (gCtrlBlock->getMyRole() == CtrlBlock::FRONT_END) {
        parseFE(e);
        if ((gCtrlBlock->getEndInfo()->fe_info.mode==SCI_POLLING) && 
            (state==IDLING)) {
            // if polling mode, need notify SCI_Poll in idling state
            gCtrlBlock->notifyPollQueue();
        }
    } else if (gCtrlBlock->getMyRole() == CtrlBlock::AGENT) {
        parseAgent(e);
    } else if (gCtrlBlock->getMyRole() == CtrlBlock::BACK_END) {
        parseBE(e);
        if ((gCtrlBlock->getEndInfo()->be_info.mode==SCI_POLLING) &&
            (state==EXITING)) {
            // if polling mode, need notify SCI_Poll in exiting state
            gCtrlBlock->notifyPollQueue();
        }
    }

    log_debug("StateMachine: current state is %s", StateMsg[state]);

    if (state == EXITING) {
        gCtrlBlock->disable();
    } else if (state != UNINITIALIZED) {
        gCtrlBlock->enable();
    }
    unlock();
}

void StateMachine::reset()
{
    lock();
    state = UNINITIALIZED;
    connected = 0;
    disconnected = 0;
    toQuit = false;
    history.clear();
    unlock();
}

StateMachine::STATE StateMachine::getState()
{
    STATE s;

    lock();
    s = state;
    unlock();
    
    return s;
}

bool StateMachine::isToQuit(int handle)
{
    bool ret;

    lock();
    if (toQuit) {
        ret = true;
    } else {
        if (handle == gCtrlBlock->getMyHandle()) {
            ret = false;
        } else {
            if (gRoutingList->isSuccessorExist(handle)) {
                ret = false;
            } else {
                ret = true;
            }
        }
    }
    unlock();

    return ret;
}

void StateMachine::dump()
{
    char outFile[256];
    int hndl = gCtrlBlock->getMyHandle();
    if (gCtrlBlock->getMyRole() == CtrlBlock::FRONT_END) {
        ::sprintf(outFile, "fe.state");
    } else if (gCtrlBlock->getMyRole() == CtrlBlock::AGENT) {
        ::sprintf(outFile, "scia.state.%d", hndl);
    } else {
        ::sprintf(outFile, "be.state.%d", hndl);
    }

    FILE *fp = ::fopen(outFile, "w");
    if (fp) {
        int size = history.size();
        for (int i=0; i<size; i++) {
            ::fprintf(fp, "Event %d: %s\n", i, EventMsg[history[i]]);
        }
        
        ::fclose(fp);
    }
}

void StateMachine::doAssert(bool expression)
{
    if (!expression) {
        dump();
        assert(!"Should not be here");
    }
}

void StateMachine::parseFE(StateMachine::EVENT e)
{
    switch (e) {
        case USER_QUIT:
            log_debug("StateMachine: user called SCI_Terminate");
            if (state == IDLING) {
                state = EXITING;
            }
            toQuit = true;
            break;
        case FATAL_EXCEPTION:
            log_debug("StateMachine: fatal exception");
            if (state != EXITING) {
                state = EXITING;
            }
            break;
        case CLIENT_CONNECTED:
            log_debug("StateMachine: a client is connected");
            connected++;
            if (state == STARTING) {
                if (connected >= gRoutingList->numOfSuccessor(SCI_GROUP_ALL)) {
                    state = RUNNING;
                }
            } else if (state == IDLING) {
                state = RUNNING;
            }
            break;
        case CLIENT_BROKEN:
            log_debug("StateMachine: a client is disconnected");
            disconnected++;
            if (state == RUNNING) {
                int numSuccessors = gRoutingList->numOfSuccessor(SCI_GROUP_ALL);
                int max = connected > numSuccessors ? connected : numSuccessors;
                if (disconnected >= max) {
                    state = IDLING;
                    if (toQuit) {
                        state = EXITING;
                    }
                }
            }
            break;
        case DATASTRUC_CREATED:
            log_debug("StateMachine: data structure created");
            if (state == UNINITIALIZED) {
                state = STARTING;
            } else {
                doAssert();
            }
            break;
        case DATASTUCT_CLEANED:
            log_debug("StateMachine: data structure cleaned");
            if (state == EXITING) {
                state = UNINITIALIZED;
            } else {
                doAssert();
            }
            break;
        default:
            log_error("StateMachine: unexpected event");
            break;
    }
}

void StateMachine::parseAgent(StateMachine::EVENT e)
{
    switch (e) {
        case PARENT_CONNECTED:
            log_debug("StateMachine: connected to the parent");
            if (state == UNINITIALIZED) {
                state = STARTING;
            } else {
                doAssert();
            }
            break;
        case USER_QUIT:
            log_debug("StateMachine: got a quit command");
            toQuit = true;
            if (gRoutingList->numOfSuccessor(SCI_GROUP_ALL) == 0) {
                state = EXITING;
            }
            break;
        case FATAL_EXCEPTION:
            log_debug("StateMachine: parent broken or fatal exception");
            if (state != EXITING) {
                state = EXITING;
            }
            break;
        case CLIENT_CONNECTED:
            log_debug("StateMachine: a client is connected");
            connected++;
            if (state == STARTING) {
                if (connected >= gRoutingList->numOfSuccessor(SCI_GROUP_ALL)) {
                    state = RUNNING;
                }
            }
            break;
        case CLIENT_BROKEN:
            log_debug("StateMachine: a client is disconnected");
            disconnected++;
            if (state == RUNNING) {
                int numSuccessors = gRoutingList->numOfSuccessor(SCI_GROUP_ALL);
                int max = connected > numSuccessors ? connected : numSuccessors;
                if ((disconnected>=max) && toQuit) {
                    state = EXITING;
                }
            }
            break;
        case DATASTRUC_CREATED:
            log_debug("StateMachine: data structure created");
            doAssert(state == STARTING);
            break;
        case DATASTUCT_CLEANED:
            log_debug("StateMachine: data structure cleaned");
            if (state == EXITING) {
                state = UNINITIALIZED;
            } else {
                doAssert();
            }
            break;
        case PARENT_BROKEN:
            log_debug("StateMachine: parent broken");
            if (state == RUNNING) {
                state = IDLING;
            } else {
                state = EXITING;
            }
            break;
        case RECOVER_OK:
            log_debug("StateMachine: recover okay");
            if (state == IDLING) {
                state = RUNNING;
            } else {
                doAssert();
            }
            break;
        case RECOVER_FAILED:
            log_debug("StateMachine: recover failed");
            if (state == IDLING) {
                state = EXITING;
            } else if (state != EXITING) {
                doAssert();
            }
            break;
        default:
            log_error("StateMachine: unexpected event");
            break;
    }
}

void StateMachine::parseBE(StateMachine::EVENT e)
{
    switch (e) {
        case PARENT_CONNECTED:
            log_debug("StateMachine: connected to the parent");
            if (state == UNINITIALIZED) {
                state = STARTING;
            } else {
                doAssert();
            }
            break;
        case USER_QUIT:
            log_debug("StateMachine: got a quit command");
            if (state == RUNNING) {
                state = EXITING;
            } else if (state != EXITING) {
                doAssert();
            }
            break;
        case FATAL_EXCEPTION:
            log_debug("StateMachine: fatal exception");
            if (state != EXITING) {
                state = EXITING;
            }
            break;
        case DATASTRUC_CREATED:
            log_debug("StateMachine: data structure created");
            if (state == STARTING) {
                state = RUNNING;
            } else {
                doAssert();
            }
            break;
        case DATASTUCT_CLEANED:
            log_debug("StateMachine: data structure cleaned");
            if (state == EXITING) {
                state = UNINITIALIZED;
            } else {
                doAssert();
            }
            break;
        case PARENT_BROKEN:
            log_debug("StateMachine: parent broken");
            if (state == RUNNING) {
                state = IDLING;
            } else {
                state = EXITING;
            }
            break;
        case RECOVER_OK:
            log_debug("StateMachine: recover okay");
            if (state == IDLING) {
                state = RUNNING;
            } else {
                doAssert();
            }
            break;
        case RECOVER_FAILED:
            log_debug("StateMachine: recover failed");
            if (state == IDLING) {
                state = EXITING;
            } else if (state != EXITING) {
                doAssert();
            }
            break;
        default:
            log_error("StateMachine: unexpected event");
            break;
    }
}

