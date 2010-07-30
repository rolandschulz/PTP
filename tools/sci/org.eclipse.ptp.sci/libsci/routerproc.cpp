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

 Classes: RouterProcessor

 Description: Properties of class 'RouterProcessor':
    input: a. a stream 
           b. a message queue
    output: a set of message queues
    action: route the message to the designated destination
   
 Author: Nicole Nie

 History:
   Date     Who ID    Description
   -------- --- ---   -----------
   02/10/09 nieyy      Initial code (D153875)

****************************************************************************/

#include "routerproc.hpp"
#include <stdlib.h>
#include <assert.h>

#include "sci.h"

#include "log.hpp"
#include "exception.hpp"
#include "socket.hpp"

#include "ctrlblock.hpp"
#include "statemachine.hpp"
#include "message.hpp"
#include "queue.hpp"
#include "filter.hpp"
#include "filterlist.hpp"
#include "routinglist.hpp"
#include "topology.hpp"
#include "eventntf.hpp"

RouterProcessor::RouterProcessor(int hndl) 
    : Processor(hndl), curFilterID(SCI_FILTER_NULL), curGroup(SCI_GROUP_ALL)
{
    name = "Router";

    inQueue = NULL;
}

Message * RouterProcessor::read()
{
    assert(inQueue);

    Message *msg = NULL;
    msg = inQueue->consume();
    
    if (msg && (msg->getType() == Message::SEGMENT) && (msg->getFilterID() == SCI_JOIN_SEGMENT)) {
        int segnum = msg->getID() - 1; // exclude the SEGMENT header
        Message **segments = (Message **)::malloc(segnum * sizeof(Message *));
        inQueue->remove();

        msg = new Message();
        inQueue->multiConsume(segments, segnum);
        msg->joinSegments(segments, segnum);
        ::free(segments);
    }

    return msg;
}

void RouterProcessor::process(Message * msg)
{
    assert(msg);

    Filter *filter = NULL;
    Topology *topo = NULL;
    int rc;
    
    switch (msg->getType()) {
        case Message::SEGMENT:
            gRoutingList->bcast(msg->getGroup(), msg);
            break;
        case Message::COMMAND:
            if (msg->getFilterID() == SCI_FILTER_NULL) {
                // bcast the message
                gRoutingList->bcast(msg->getGroup(), msg);
            } else {
                filter = gFilterList->getFilter(msg->getFilterID());
                if (filter != NULL) {
                    // call user's filter handler
                    curFilterID = msg->getFilterID();
                    curGroup = msg->getGroup();
                    filter->input(msg->getGroup(), msg->getContentBuf(), msg->getContentLen());
                } else {
                    // bcast the message
                    gRoutingList->bcast(msg->getGroup(), msg);
                }
            }
            break;
        case Message::CONFIG:
            topo = new Topology(-1);
            topo->unpackMsg(*msg);
            gCtrlBlock->setTopology(topo);
            
            rc = topo->deploy();
            if (gCtrlBlock->getMyRole() == CtrlBlock::FRONT_END) {
                *(int *)gNotifier->getRetVal(msg->getID()) = rc;
                gNotifier->notify(msg->getID());   
            }
            break;
        case Message::FILTER_LOAD:
        case Message::FILTER_UNLOAD:
            if (msg->getType() == Message::FILTER_LOAD) {
                filter = new Filter();
                filter->unpackMsg(*msg);
                rc = gFilterList->loadFilter(filter->getId(), filter);
            } else {
                rc = gFilterList->unloadFilter(msg->getFilterID());
            }

            if (gCtrlBlock->getMyRole() == CtrlBlock::FRONT_END) {
                *(int *)gNotifier->getRetVal(msg->getID()) = rc;
                gNotifier->notify(msg->getID());   
            }

            gRoutingList->bcast(SCI_GROUP_ALL, msg);
            break;
        case Message::FILTER_LIST:
            gFilterList->loadFilterList(*msg);
            break;
        case Message::GROUP_CREATE:
        case Message::GROUP_FREE:
        case Message::GROUP_OPERATE:
        case Message::GROUP_OPERATE_EXT:
        case Message::GROUP_DROP:
        case Message::GROUP_MERGE:
            gRoutingList->parseCmd(msg);
            break;
        case Message::BE_ADD:
            rc = gCtrlBlock->getTopology()->addBE(msg);
            if (gCtrlBlock->getMyRole() == CtrlBlock::FRONT_END) {
                *(int *)gNotifier->getRetVal(msg->getID()) = rc;
                gNotifier->notify(msg->getID());
            }
            break;
        case Message::BE_REMOVE:
            rc = gCtrlBlock->getTopology()->removeBE(msg);
            if (gCtrlBlock->getMyRole() == CtrlBlock::FRONT_END) {
                *(int *)gNotifier->getRetVal(msg->getID()) = rc;
                gNotifier->notify(msg->getID());
            }
            break;
        case Message::QUIT:
            gStateMachine->parse(StateMachine::USER_QUIT);
            gRoutingList->bcast(SCI_GROUP_ALL, msg);
            break;
        case Message::UNCLE_LIST:
        case Message::ERROR_EVENT:
        case Message::SHUTDOWN:
        case Message::KILLNODE:
            gRoutingList->bcast(SCI_GROUP_ALL, msg);
            break;
        default:
            log_error("Processor %s: received unknown command", name.c_str());
            break;
    }
}

void RouterProcessor::write(Message * msg)
{
    assert(msg);
    
    // almost no action
    inQueue->remove();
}

void RouterProcessor::seize()
{
    gStateMachine->parse(StateMachine::FATAL_EXCEPTION);
}

void RouterProcessor::clean()
{
    // no action
}

bool RouterProcessor::isActive()
{
    return gCtrlBlock->isEnabled() || (inQueue->getSize() > 0);
}

int RouterProcessor::getCurFilterID()
{
    return curFilterID;
}

sci_group_t RouterProcessor::getCurGroup()
{
    return curGroup;
}

void RouterProcessor::setInQueue(MessageQueue * queue)
{
    inQueue = queue;
}

