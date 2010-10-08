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

#include <stdlib.h>
#include <string.h>
#include <assert.h>

#include "sci.h"

#include "log.hpp"
#include "exception.hpp"
#include "socket.hpp"

#include "atomic.hpp"
#include "routerproc.hpp"
#include "ctrlblock.hpp"
#include "message.hpp"
#include "queue.hpp"
#include "filter.hpp"
#include "filterlist.hpp"
#include "routinglist.hpp"
#include "topology.hpp"
#include "eventntf.hpp"
#include "privatedata.hpp"

RouterProcessor::RouterProcessor(int hndl, RoutingList *rlist, FilterList *flist) 
    : Processor(hndl), curFilterID(SCI_FILTER_NULL), curGroup(SCI_GROUP_ALL), inStream(NULL), routingList(rlist), filterList(flist), joinSegs(false)
{
    name = "Router";
    PrivateData *pData = new PrivateData(routingList, filterList, NULL, this);
    setSpecific(pData);
}

RouterProcessor::~RouterProcessor()
{
    if (inQueue)
        delete inQueue;
}

Message * RouterProcessor::read()
{
    assert(inQueue || inStream);

    Message *msg = NULL;
    if (inStream) {
        msg = new Message();
        *inStream >> *msg;
    } else {
        msg = inQueue->consume();
    }

    if (msg && (msg->getType() == Message::SEGMENT) && (msg->getFilterID() == SCI_JOIN_SEGMENT)) {
        joinSegs = true;
        msg = Message::joinSegments(msg, inStream, inQueue);
    }

    return msg;
}

void RouterProcessor::process(Message * msg)
{
    Filter *filter = NULL;
    Topology *topo = routingList->getTopology();
    int rc;
    
    switch (msg->getType()) {
        case Message::SEGMENT:
            routingList->bcast(msg->getGroup(), msg);
            break;
        case Message::COMMAND:
            if (msg->getFilterID() == SCI_FILTER_NULL) {
                // bcast the message
                routingList->bcast(msg->getGroup(), msg);
            } else {
                filter = filterList->getFilter(msg->getFilterID());
                if (filter != NULL) {
                    // call user's filter handler
                    curFilterID = msg->getFilterID();
                    curGroup = msg->getGroup();
                    filter->input(msg->getGroup(), msg->getContentBuf(), msg->getContentLen());
                } else {
                    // bcast the message
                    routingList->bcast(msg->getGroup(), msg);
                }
            }
            break;
        case Message::CONFIG:
            topo->unpackMsg(*msg);
            topo->setRoutingList(routingList);
            topo->setFilterList(filterList);
            
            rc = topo->deploy();
            break;
        case Message::FILTER_LOAD:
        case Message::FILTER_UNLOAD:
            if (msg->getType() == Message::FILTER_LOAD) {
                filter = new Filter();
                filter->unpackMsg(*msg);
                rc = filterList->loadFilter(filter->getId(), filter);
            } else {
                rc = filterList->unloadFilter(msg->getFilterID());
            }

            if (gCtrlBlock->getMyRole() == CtrlBlock::FRONT_END) {
                *(int *)gNotifier->getRetVal(msg->getID()) = rc;
                gNotifier->notify(msg->getID());   
            }

            routingList->bcast(SCI_GROUP_ALL, msg);
            break;
        case Message::FILTER_LIST:
            filterList->loadFilterList(*msg);
            break;
        case Message::GROUP_CREATE:
        case Message::GROUP_FREE:
        case Message::GROUP_OPERATE:
        case Message::GROUP_OPERATE_EXT:
        case Message::GROUP_DROP:
        case Message::GROUP_MERGE:
            routingList->parseCmd(msg);
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
            routingList->bcast(SCI_GROUP_ALL, msg);
            setState(false);
            break;
        case Message::UNCLE_LIST:
        case Message::ERROR_EVENT:
        case Message::SHUTDOWN:
        case Message::KILLNODE:
            routingList->bcast(SCI_GROUP_ALL, msg);
            break;
        default:
            log_error("Processor %s: received unknown command", name.c_str());
            break;
    }
}

void RouterProcessor::write(Message * msg)
{
    if (joinSegs || inStream) {
        joinSegs = false;
        if (decRefCount(msg->getRefCount()) == 0)
            delete msg;
        return;
    }
        
    inQueue->remove();
}

void RouterProcessor::seize()
{
    Message *msg = new Message();

    msg->build(SCI_FILTER_NULL, SCI_GROUP_ALL, 0, NULL, NULL, Message::QUIT);
    routingList->bcast(SCI_GROUP_ALL, msg);
    setState(false);
}

void RouterProcessor::clean()
{
    if (inStream)
        inStream->stopRead();
    routingList->stopRouting();
    gCtrlBlock->disable();
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

MessageQueue * RouterProcessor::getInQueue()
{
    return inQueue;
}

void RouterProcessor::setInStream(Stream * stream)
{
    inStream = stream;
}

RoutingList * RouterProcessor::getRoutingList()
{
    return routingList;
}

