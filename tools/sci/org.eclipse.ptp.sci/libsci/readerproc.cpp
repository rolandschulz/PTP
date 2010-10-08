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

 Classes: ReaderProcessor

 Description: Properties of class 'ReaderProcessor':
    input: a stream
    output: two message queues
    action: relay messages from the stream to the queues, normal messages to a
            queue, error handling messages to another queue
   
 Author: Nicole Nie

 History:
   Date     Who ID    Description
   -------- --- ---   -----------
   05/25/09 nieyy      Initial code (F156654)

****************************************************************************/

#include "readerproc.hpp"
#include <assert.h>

#include "log.hpp"
#include "exception.hpp"
#include "socket.hpp"

#include "ctrlblock.hpp"
#include "routinglist.hpp"
#include "message.hpp"
#include "stream.hpp"
#include "queue.hpp"
#include "writerproc.hpp"

ReaderProcessor::ReaderProcessor(int hndl) 
    : Processor(hndl)
{
    name = "Reader";

    inStream = NULL;
    outQueue = NULL;

    outErrorQueue = NULL;
}

ReaderProcessor::~ReaderProcessor()
{
}

Message * ReaderProcessor::read()
{
    assert(inStream);
    Message *msg = NULL;

    msg = new Message();
    *inStream >> *msg;

    return msg;
}

void ReaderProcessor::process(Message * msg)
{
    assert(msg);
    // no action
}

void ReaderProcessor::write(Message * msg)
{
    assert(outQueue);

    // normal and error messages to different queues
    switch (msg->getType()) {
        case Message::GROUP_MERGE:
        case Message::ERROR_EVENT:
            // use 'id' field to store child agent id information, and transfer this message
            // to router processor
            msg->setID(handle);
        case Message::UNCLE:
        case Message::UNCLE_LIST:
        case Message::PARENT:
        case Message::SHUTDOWN:
        case Message::KILLNODE:
            if (outErrorQueue) {
                outErrorQueue->produce(msg);
            } else {
                delete msg;
            }
            break;
        default:
            outQueue->produce(msg);
            break;
    }
}

void ReaderProcessor::seize()
{    
    // exit the peer relay processor thread related to the same socket
    setState(false);    
}

void ReaderProcessor::clean()
{
    inStream->stopRead();
    setState(false);    
}

void ReaderProcessor::setInStream(Stream * stream)
{
    inStream = stream;
}

void ReaderProcessor::setOutQueue(MessageQueue * queue)
{
    outQueue = queue;
}

void ReaderProcessor::setOutErrorQueue(MessageQueue * queue)
{
    outErrorQueue = queue;
}
