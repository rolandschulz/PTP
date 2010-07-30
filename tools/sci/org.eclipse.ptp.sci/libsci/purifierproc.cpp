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

 Classes: PurifierProcessor

 Description: Properties of class 'PurifierProcessor':
    input: a. a stream 
    output: a. a message queue
    action: purify message, discarded useless messages
   
 Author: Nicole Nie

 History:
   Date     Who ID    Description
   -------- --- ---   -----------
   02/25/09 nieyy      Initial code (D153875)

****************************************************************************/

#include "purifierproc.hpp"
#include <assert.h>

#include "log.hpp"
#include "exception.hpp"
#include "socket.hpp"

#include "ctrlblock.hpp"
#include "statemachine.hpp"
#include "routinglist.hpp"
#include "message.hpp"
#include "stream.hpp"
#include "queue.hpp"
#include "observer.hpp"
#include "filter.hpp"
#include "filterlist.hpp"
#include "errevent.hpp"
#include "writerproc.hpp"

PurifierProcessor::PurifierProcessor(int hndl) 
    : Processor(hndl)
{
    name = "Purifier";

    inStream = NULL;
    outQueue = NULL;
    
    outErrorQueue = NULL;
    peerProcessor = NULL;

    observer = NULL;
}

Message * PurifierProcessor::read()
{
    assert(inStream);

    Message *msg = new Message();
    *inStream >> *msg;

    return msg;
}

void PurifierProcessor::process(Message * msg)
{
    assert(msg);
    isCmd = false;
    isError = false;
    
    Filter *filter = NULL;
    switch(msg->getType()) {
        case Message::SEGMENT:
        case Message::COMMAND:
            isCmd = true;
            break;
        case Message::UNCLE:
        case Message::UNCLE_LIST:
        case Message::PARENT:
        case Message::ERROR_EVENT:
        case Message::SHUTDOWN:
        case Message::KILLNODE:
            isError = true;
            msg->setID(handle);
            break;
        case Message::GROUP_CREATE:
        case Message::GROUP_OPERATE:
        case Message::GROUP_OPERATE_EXT:
            gRoutingList->addBE(msg->getGroup(), VALIDBACKENDIDS, gCtrlBlock->getMyHandle());
            break;
        case Message::GROUP_FREE:
            gRoutingList->removeGroup(msg->getGroup());
            break;
        case Message::FILTER_LOAD:
            filter = new Filter();
            filter->unpackMsg(*msg);
            gFilterList->loadFilter(filter->getId(), filter, false);
            break;
        case Message::FILTER_UNLOAD:
            gFilterList->unloadFilter(msg->getFilterID(), false);
            break;
        case Message::FILTER_LIST:
            gFilterList->loadFilterList(*msg, false);
            break;
        case Message::BE_REMOVE:
        case Message::QUIT:
            gStateMachine->parse(StateMachine::USER_QUIT);
            break;
        default:
            break;
    }
}

void PurifierProcessor::write(Message * msg)
{
    assert(outQueue);

    if (isCmd) {
        if (observer) {
            observer->notify();
        }
        outQueue->produce(msg);
    } else if (isError) {
        if (outErrorQueue) {
            outErrorQueue->produce(msg);
        } else {
            delete msg;
        }
    } else {
        delete msg;
    }
}

void PurifierProcessor::seize()
{
    gStateMachine->parse(StateMachine::PARENT_BROKEN);

    // exit the peer relay processor thread related to the same socket
    peerProcessor->stop();

    if (outErrorQueue) {
        // generate an error message and put it into error message queue
        ErrorEvent event;
        event.setErrCode(SCI_ERR_PARENT_BROKEN);
        event.setNodeID(handle);
        event.setBENum(1);

        Message *msg = event.packMsg();
        outErrorQueue->produce(msg);
    } else {
        // do not try to recover
        gStateMachine->parse(StateMachine::RECOVER_FAILED);
    }
}

void PurifierProcessor::clean()
{
    inStream->stopRead();
}

bool PurifierProcessor::isActive()
{
    return gCtrlBlock->isEnabled();
}

void PurifierProcessor::setInStream(Stream * stream)
{
    inStream = stream;
}

void PurifierProcessor::setOutQueue(MessageQueue * queue)
{
    outQueue = queue;
}

void PurifierProcessor::setOutErrorQueue(MessageQueue * queue)
{
    outErrorQueue = queue;
}

void PurifierProcessor::setPeerProcessor(WriterProcessor * processor)
{
    peerProcessor =  processor;
}

void PurifierProcessor::setObserver(Observer * ob)
{
    observer = ob;
}

