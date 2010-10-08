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

#include "atomic.hpp"
#include "ctrlblock.hpp"
#include "routinglist.hpp"
#include "message.hpp"
#include "stream.hpp"
#include "privatedata.hpp"
#include "queue.hpp"
#include "observer.hpp"
#include "filter.hpp"
#include "filterlist.hpp"
#include "writerproc.hpp"

PurifierProcessor::PurifierProcessor(int hndl) 
    : Processor(hndl), inStream(NULL), outErrorQueue(NULL), peerProcessor(NULL), observer(NULL), joinSegs(false)
{
    name = "Purifier";
    hndlr = gCtrlBlock->getEndInfo()->be_info.hndlr;
    param = gCtrlBlock->getEndInfo()->be_info.param;
    routingList = new RoutingList(hndl);
    routingList->addBE(SCI_GROUP_ALL, VALIDBACKENDIDS, hndl);
    filterList = new FilterList();
    PrivateData *pData = new PrivateData(routingList, filterList, NULL);
    setSpecific(pData);
}

PurifierProcessor::~PurifierProcessor()
{
    if (inQueue)
        delete inQueue;
    if (routingList)
        delete routingList;
    if (filterList)
        delete filterList;
}

Message * PurifierProcessor::read()
{
    Message *msg = NULL;
    assert(inStream || inQueue);

    if (inStream != NULL) {
        msg = new Message();
        *inStream >> *msg;
    } else {
        msg = inQueue->consume();
    }

    if (msg && (msg->getType() == Message::SEGMENT)) {
        joinSegs = true;
        msg = Message::joinSegments(msg, inStream, inQueue);
    }

    return msg;
}

void PurifierProcessor::process(Message * msg)
{
    Filter *filter = NULL;
    switch(msg->getType()) {
        case Message::SEGMENT:
        case Message::COMMAND:
            if (observer) {
                observer->notify();
                incRefCount(msg->getRefCount()); // inQueue and outQueue
                outQueue->produce(msg);
            } else {
                hndlr(param, msg->getGroup(), msg->getContentBuf(), msg->getContentLen());
            }
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
            routingList->addBE(msg->getGroup(), VALIDBACKENDIDS, gCtrlBlock->getMyHandle());
            break;
        case Message::GROUP_FREE:
            routingList->removeGroup(msg->getGroup());
            break;
        case Message::FILTER_LOAD:
            filter = new Filter();
            filter->unpackMsg(*msg);
            filterList->loadFilter(filter->getId(), filter, false);
            break;
        case Message::FILTER_UNLOAD:
            filterList->unloadFilter(msg->getFilterID(), false);
            break;
        case Message::FILTER_LIST:
            filterList->loadFilterList(*msg, false);
            break;
        case Message::BE_REMOVE:
        case Message::QUIT:
            setState(false);
            break;
        default:
            break;
    }
}

void PurifierProcessor::write(Message * msg)
{
    if (joinSegs || inStream) {
        joinSegs = false;
        if (decRefCount(msg->getRefCount()) == 0)
            delete msg;
        return;
    }
    inQueue->remove();
}

void PurifierProcessor::seize()
{
    setState(false);
}

void PurifierProcessor::clean()
{
    if (inStream)
        inStream->stopRead();
    if (observer)
        gCtrlBlock->releasePollQueue();
    gCtrlBlock->disable();
    if (peerProcessor) {
        peerProcessor->release();
        delete peerProcessor;
    }
}

void PurifierProcessor::setInStream(Stream * stream)
{
    inStream = stream;
}

void PurifierProcessor::setInQueue(MessageQueue * queue)
{
    inQueue = queue;
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
