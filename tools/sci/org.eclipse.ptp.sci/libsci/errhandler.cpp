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

 Classes: ErrorHandler

 Description: The functions of error handler include:
     a. call user-defined error handlers to process error messages
   
 Author: Nicole Nie

 History:
   Date     Who ID    Description
   -------- --- ---   -----------
   04/28/09 nieyy       Initial code (F156654)

****************************************************************************/

#include "errhandler.hpp"
#include <assert.h>

#include "log.hpp"
#include "exception.hpp"
#include "socket.hpp"

#include "ctrlblock.hpp"
#include "statemachine.hpp"
#include "message.hpp"
#include "stream.hpp"
#include "queue.hpp"
#include "errevent.hpp"

ErrorHandler::ErrorHandler(int hndl) 
    : Processor(hndl)
{
    name = "ErrorHandler";

    inQueue = NULL;

    if (gCtrlBlock->getMyRole() == CtrlBlock::FRONT_END) {
        hndlr = gCtrlBlock->getEndInfo()->fe_info.err_hndlr;
    } else if (gCtrlBlock->getMyRole() == CtrlBlock::BACK_END) {
        hndlr = gCtrlBlock->getEndInfo()->be_info.err_hndlr;
    } else {
        assert(!"Should never go here!");
    }
}

Message * ErrorHandler::read()
{
    assert(inQueue);

    Message *msg = NULL;
    msg = inQueue->consume();
    return msg;
}

void ErrorHandler::process(Message * msg)
{
    assert(msg);

    ErrorEvent event;
    switch(msg->getType()) {
        case Message::ERROR_EVENT:
            event.unpackMsg(*msg);
            hndlr(event.getErrCode(), event.getNodeID(), event.getBENum());
            break;
        default:
            log_error("Processor %s: received unknown command", name.c_str());
            break;
    }
}

void ErrorHandler::write(Message * msg)
{
    // almost no action
    inQueue->remove();
}

void ErrorHandler::seize()
{
    gStateMachine->parse(StateMachine::FATAL_EXCEPTION);
}

void ErrorHandler::clean()
{
    // no action
}

bool ErrorHandler::isActive()
{
    return gCtrlBlock->isEnabled() || (inQueue->getSize() > 0);
}

void ErrorHandler::setInQueue(MessageQueue * queue)
{
    inQueue = queue;
}

