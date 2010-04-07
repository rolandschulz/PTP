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

 Classes: HandlerProcessor

 Description: Properties of class 'HandlerProcessor':
    input: a. a message queue
    output: none
    action: use handler in sci_info_t to process the messages
   
 Author: Nicole Nie

 History:
   Date     Who ID    Description
   -------- --- ---   -----------
   02/10/09 nieyy      Initial code (D153875)

****************************************************************************/

#include "handlerproc.hpp"
#include <stdlib.h>
#include <assert.h>

#include "log.hpp"
#include "exception.hpp"
#include "socket.hpp"

#include "ctrlblock.hpp"
#include "statemachine.hpp"
#include "message.hpp"
#include "stream.hpp"
#include "queue.hpp"

HandlerProcessor::HandlerProcessor(int hndl) 
    : Processor(hndl)
{
    name = "Handler";

    inQueue = NULL;

    if (gCtrlBlock->getMyRole() == CtrlBlock::FRONT_END) {
        hndlr = gCtrlBlock->getEndInfo()->fe_info.hndlr;
        param = gCtrlBlock->getEndInfo()->fe_info.param;
    } else if (gCtrlBlock->getMyRole() == CtrlBlock::BACK_END) {
        hndlr = gCtrlBlock->getEndInfo()->be_info.hndlr;
        param = gCtrlBlock->getEndInfo()->be_info.param;
    } else {
        assert(!"Should never go here!");
    }
}

Message * HandlerProcessor::read()
{
    assert(inQueue);

    Message *msg = NULL;
    msg = inQueue->consume();
    
    if (msg && (msg->getType() == Message::SEGMENT)) {
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

void HandlerProcessor::process(Message * msg)
{
    assert(msg);

    switch(msg->getType()) {
        case Message::COMMAND:
        case Message::DATA:
            hndlr(param, msg->getGroup(), msg->getContentBuf(), msg->getContentLen());
            break;
        default:
            log_error("Processor %s: received unknown command", name.c_str());
            break;
    }
}

void HandlerProcessor::write(Message * msg)
{
    // almost no action
    inQueue->remove();
}

void HandlerProcessor::seize()
{
    gStateMachine->parse(StateMachine::FATAL_EXCEPTION);
}

void HandlerProcessor::clean()
{
    // no action
}

bool HandlerProcessor::isActive()
{
    return gCtrlBlock->isEnabled() || (inQueue->getSize() > 0);
}

void HandlerProcessor::setInQueue(MessageQueue * queue)
{
    inQueue = queue;
}

