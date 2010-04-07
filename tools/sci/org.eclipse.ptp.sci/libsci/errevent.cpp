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

 Classes: Event

 Description: Error events.
   
 Author: Nicole Nie

 History:
   Date     Who ID    Description
   -------- --- ---   -----------
   04/29/09 nieyy        Initial code (F156654)

****************************************************************************/

#include "errevent.hpp"
#include <stdlib.h>
#include <math.h>
#include <assert.h>
#include <ctype.h>
#include <string.h>

#include "packer.hpp"
#include "log.hpp"

#include "message.hpp"

ErrorEvent::ErrorEvent()
{
}

void ErrorEvent::setErrCode(int code)
{
    err_code = code;
}

void ErrorEvent::setNodeID(int id)
{
    node_id = id;
}

void ErrorEvent::setBENum(int num)
{
    num_bes = num;
}

Message * ErrorEvent::packMsg()
{
    Packer packer;

    packer.packInt(err_code);
    packer.packInt(node_id);
    packer.packInt(num_bes);

    char *bufs[1];
    int sizes[1];
    
    bufs[0] = packer.getPackedMsg();
    sizes[0] = packer.getPackedMsgLen();

    Message *msg = new Message();
    msg->build(SCI_FILTER_NULL, SCI_GROUP_ALL, 1, bufs, sizes, Message::ERROR_EVENT);
    delete [] bufs[0];
    return msg;
}

void ErrorEvent::unpackMsg(Message &msg) 
{
    Packer packer(msg.getContentBuf());

    err_code = packer.unpackInt();
    node_id = packer.unpackInt();
    num_bes = packer.unpackInt();
}

int ErrorEvent::getErrCode()
{
    return err_code;
}

int ErrorEvent::getNodeID()
{
    return node_id;
}

int ErrorEvent::getBENum()
{
    return num_bes;
}

