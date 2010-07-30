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

 Classes: WriterProcessor

 Description: Properties of class 'WriterProcessor':
    input: a message queue
    output: a stream
    action: relay messages from the queue to the stream.
   
 Author: Nicole Nie

 History:
   Date     Who ID    Description
   -------- --- ---   -----------
   05/25/09 nieyy      Initial code (F156654)

****************************************************************************/

#include "writerproc.hpp"
#include <assert.h>

#include "log.hpp"
#include "exception.hpp"
#include "socket.hpp"

#include "ctrlblock.hpp"
#include "message.hpp"
#include "stream.hpp"
#include "queue.hpp"

WriterProcessor::WriterProcessor(int hndl) 
    : Processor(hndl)
{
    name = "Writer";

    inQueue = NULL;
    outStream = NULL;
}

Message * WriterProcessor::read()
{
    assert(inQueue);

    Message *msg = NULL;

    msg = inQueue->consume();

    return msg;
}

void WriterProcessor::process(Message * msg)
{
    assert(msg);
    // no action
}

void WriterProcessor::write(Message * msg)
{
    assert(outStream);
        
    *outStream << *msg;
    inQueue->remove();
}

void WriterProcessor::seize()
{
    // do nothing
}

void WriterProcessor::clean()
{
    outStream->stopWrite();
}

bool WriterProcessor::isActive()
{
    return gCtrlBlock->isEnabled() || (inQueue->getSize() > 0);
}

void WriterProcessor::setInQueue(MessageQueue * queue)
{
    inQueue = queue;
}

void WriterProcessor::setOutStream(Stream * stream)
{
    outStream = stream;
}

void WriterProcessor::stop()
{
    setState(false);
    inQueue->notify();
}

