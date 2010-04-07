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

 Classes: ErrorDetector

 Description: The functions of error detector include:
     a. detect connection broken events from peer processes.
     b. detect heartbeat packets from peer processes
     c. propagate failure data to peer processes
     d. propagate recovery information to peer processes
     e. establish new connection dynamically
     f. accept error injection data for testing purposes
     g. delegate eror information to error handling thread (EHT)
   
 Author: Nicole Nie

 History:
   Date     Who ID    Description
   -------- --- ---   -----------
   04/28/09 nieyy       Initial code (F156654)

****************************************************************************/

#include "errdetector.hpp"
#include <assert.h>
#include <stdlib.h>

#include "log.hpp"
#include "exception.hpp"
#include "socket.hpp"
#include "packer.hpp"
#include "stream.hpp"

#include "ctrlblock.hpp"
#include "routinglist.hpp"
#include "statemachine.hpp"
#include "message.hpp"
#include "queue.hpp"
#include "parent.hpp"
#include "errevent.hpp"
#include "topology.hpp"
#include "initializer.hpp"

ErrorDetector::ErrorDetector(int hndl) 
    : Processor(hndl)
{
    name = "ErrorDetector";

    inQueue = NULL;
    outQueue = NULL;

    parent = NULL;
    uncleList = NULL;
    uncleList2 = NULL;

    needForward = false;
}

ErrorDetector::~ErrorDetector()
{
    if (parent) {
        delete parent;
    }
    if (uncleList) {
        delete uncleList;
    }
    if (uncleList2) {
        delete uncleList2;
    }
}

Message * ErrorDetector::read()
{
    assert(inQueue);

    Message *msg = NULL;

    msg = inQueue->consume();
    
    return msg;
}

void ErrorDetector::process(Message * msg)
{
    assert(msg);

    needForward = false;
    switch(msg->getType()) {
        case Message::UNCLE:
        case Message::UNCLE_LIST:
        case Message::PARENT:
            processParentInfo(msg);
            break;
        case Message::ERROR_EVENT:
            processErrorEvent(msg);
            break;
        case Message::SHUTDOWN:
        case Message::KILLNODE:
            processErrorInjection(msg);
            break;
        case Message::GROUP_MERGE:
            // transfer this message to relay processor
            msg->setRefCount(msg->getRefCount() + 1);
            gCtrlBlock->getRouterInQueue()->produce(msg);
            break;
        default:
            log_error("Processor %s: received unknown command", name.c_str());
            break;
    }
}

void ErrorDetector::write(Message * msg)
{
    if (needForward) {
        if (outQueue) {
            msg->setRefCount(msg->getRefCount() + 1);
        }
        if (gCtrlBlock->getMonitorInQueue()) {
            msg->setRefCount(msg->getRefCount() + 1);
            gCtrlBlock->getMonitorInQueue()->produce(msg);
        }
        if (outQueue) {
            outQueue->produce(msg);
        }
    }
    inQueue->remove();
}

void ErrorDetector::seize()
{
    gStateMachine->parse(StateMachine::FATAL_EXCEPTION);
}

void ErrorDetector::clean()
{
    // no action
}

bool ErrorDetector::isActive()
{
    return gCtrlBlock->isEnabled() || (inQueue->getSize() > 0);
}

void ErrorDetector::processParentInfo(Message * msg)
{
    needForward = false;
    if (msg->getType() == Message::UNCLE) {
        // accumulate uncle information of my grandsons
        Parent *p = new Parent();
        p->unpackMsg(*msg);

        if (NULL == uncleList2) {
            uncleList2 = new ParentList();
            uncleList2->setLevel(gCtrlBlock->getTopology()->getLevel());
        }
        uncleList2->add(p);

        if (uncleList2->isAllGathered()) {
            // pack message and bcast it
            Message *unclesmsg = uncleList2->packMsg();
            gCtrlBlock->getRouterInQueue()->produce(unclesmsg);
        }
    } else if (msg->getType() == Message::UNCLE_LIST) {
        ParentList *tmpList = new ParentList();
        tmpList->unpackMsg(*msg);

        bool ignore = true;
        if (gCtrlBlock->getMyRole() == CtrlBlock::BACK_END) {
            ignore = false;
        } else if (gCtrlBlock->getTopology()->getLevel() > tmpList->getLevel()+1) {
            ignore = false;
        }

        if (ignore) {
            // if it is from my parent, just bcast it and ignore it
            msg->setRefCount(msg->getRefCount() + 1);
            gCtrlBlock->getRouterInQueue()->produce(msg);
            delete tmpList;
        } else {
            // else it is from my grandparent, it's what I need
            if (uncleList) {
                delete uncleList;
                uncleList = NULL;
            }
            uncleList = tmpList;
        }
    } else {
        if (parent) {
            delete parent;
            parent = NULL;
        }
        parent = new Parent();
        parent->unpackMsg(*msg);
    }
}

void ErrorDetector::processErrorEvent(Message * msg)
{
    ErrorEvent event;
    event.unpackMsg(*msg);

    Message *notifymsg = NULL;

    needForward = true;
    switch(event.getErrCode()) {
        case SCI_ERR_PARENT_BROKEN:
            // if I am an agent, broadcast this message to all my children
            if (gCtrlBlock->getMyRole() == CtrlBlock::AGENT) {
                msg->setRefCount(msg->getRefCount() + 1);
                gCtrlBlock->getRouterInQueue()->produce(msg);
            }
            // and if this event is generated by myself, try to recover the connection
            if (event.getNodeID() == gCtrlBlock->getMyHandle()) {
                recover();
            }
            break;
        case SCI_ERR_CHILD_BROKEN:
            // notify router processor to drop groups related to this child, use 'id' field to
            // store the successor id information
            notifymsg = new Message();
            notifymsg->build(SCI_FILTER_NULL, SCI_GROUP_ALL, 0, NULL, NULL, Message::GROUP_DROP, 
                event.getNodeID());
            gCtrlBlock->getRouterInQueue()->produce(notifymsg);
            
            // and if I am an agent, upload this message to my parent
            if (gCtrlBlock->getMyRole() == CtrlBlock::AGENT) {
                msg->setRefCount(msg->getRefCount() + 1);
                gCtrlBlock->getFilterOutQueue()->produce(msg);
            }
            break;
        case SCI_ERR_RECOVERED:
        case SCI_ERR_RECOVER_FAILED:
            if (gCtrlBlock->getMyRole() == CtrlBlock::AGENT) {
                if (msg->getID() == gCtrlBlock->getMyHandle()) {
                    // from my parent, bcast to my children
                    msg->setRefCount(msg->getRefCount() + 1);
                    gCtrlBlock->getRouterInQueue()->produce(msg);
                } else {
                    // from my child, upload to my parent
                    msg->setRefCount(msg->getRefCount() + 1);
                    gCtrlBlock->getFilterOutQueue()->produce(msg);
                }
            }
            break;
        default:
            break;
    }
}

void ErrorDetector::processErrorInjection(Message * msg)
{
    Packer packer(msg->getContentBuf());

    int nodeId = packer.unpackInt();
    if (nodeId == gCtrlBlock->getMyHandle()) {
        if (msg->getType() == Message::SHUTDOWN) {
            // if i am not a front end, shutdown the connection between me and my parent
            if (gCtrlBlock->getMyRole() != CtrlBlock::FRONT_END) {
                gCtrlBlock->getParentStream()->stop();
            }
        } else {
            // force this node to exit
            ::_exit(1);
        }
    } else {
        // if i am not a back end, bcast messages to all my children
        if (gCtrlBlock->getMyRole() != CtrlBlock::BACK_END) {
            msg->setRefCount(msg->getRefCount() + 1);
            gCtrlBlock->getRouterInQueue()->produce(msg);
        }
    }
}

void ErrorDetector::recover()
{
    Stream *stream = NULL;
    int pid;

    if (parent) {
        // first try my parent
        stream = parent->connect();
        pid = parent->getNodeID();
    }
    if ((!stream) && uncleList) {
        // if can't connect to parent, then try my uncles
        stream = uncleList->select(&pid);
    }

    // generate an error message and put it into error message queue
    ErrorEvent event;
    event.setNodeID(gCtrlBlock->getMyHandle());
    event.setBENum(gRoutingList->numOfBE(SCI_GROUP_ALL));
    
    if (stream) {
        event.setErrCode(SCI_ERR_RECOVERED);

        // reset the corresponding processor
        if (gCtrlBlock->getMyRole() == CtrlBlock::BACK_END) {
            gInitializer->recoverBE(stream);
        } else {
            gInitializer->recoverAgent(stream);
        }

        gCtrlBlock->registerStream(stream);
        gCtrlBlock->setParentStream(stream);
        gStateMachine->parse(StateMachine::RECOVER_OK);
        gRoutingList->updateParentId(pid);
    } else {
        event.setErrCode(SCI_ERR_RECOVER_FAILED);
        gStateMachine->parse(StateMachine::RECOVER_FAILED);
    }

    Message *msg = event.packMsg();
    if (gCtrlBlock->getMyRole() == CtrlBlock::AGENT) {
        msg->setRefCount(msg->getRefCount() + 1);
        // bcast this message to all my successors
        gCtrlBlock->getRouterInQueue()->produce(msg);
        // upload this message to my parent
        gCtrlBlock->getFilterOutQueue()->produce(msg);
    } else if (gCtrlBlock->getMyRole() == CtrlBlock::BACK_END) {
        // forward this message to Error Handling Thread (EHT) if have
        if (outQueue) {
            msg->setRefCount(msg->getRefCount() + 1);
            outQueue->produce(msg);
        }
        
        // upload this message to my parent
        gCtrlBlock->getRouterInQueue()->produce(msg);
    } else {
        assert(!"Should never be here");
    }
}

void ErrorDetector::setInQueue(MessageQueue * queue)
{
    inQueue = queue;
}

void ErrorDetector::setOutQueue(MessageQueue * queue)
{
    outQueue = queue;
}

