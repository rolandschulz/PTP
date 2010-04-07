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

 Classes: Packer

 Description: Wrapper for various kind of information.
   
 Author: Nicole Nie, Liu Wei

 History:
   Date     Who ID    Description
   -------- --- ---   -----------
   10/06/08 nieyy        Initial code (D153875)

****************************************************************************/

#include "packer.hpp"
#include <stdlib.h>
#include <arpa/inet.h>
#include <string.h>

const int PACK_SIZE = 256;

Packer::Packer()
    : msgLen(0), bufSize(PACK_SIZE)
{
    msgBuf = new char[bufSize];
    msgPtr = msgBuf;
}

Packer::Packer(char *msg)
    : msgLen(0), bufSize(0)
{
    msgBuf = msg;
    msgPtr = msgBuf;
}

char * Packer::getPackedMsg()
{
    return msgBuf;
}

int Packer::getPackedMsgLen()
{
    return msgLen;
}


void Packer::packInt(int value)
{
    int val = htonl(value);
    int len = sizeof(val);

    checkBuffer(len);
    memcpy(msgPtr, &val, len);
    msgPtr += len;
    msgLen += len;
}

void Packer::packStr(const char *value)
{
    int len = strlen(value) + 1;
    packInt(len);

    checkBuffer(len);
    memcpy(msgPtr, value, len);
    msgPtr += len;
    msgLen += len;
}

void Packer::packStr(const string &value)
{
    int len = strlen(value.c_str()) + 1;
    packInt(len);

    checkBuffer(len);
    memcpy(msgPtr, value.c_str(), len);
    msgPtr += len;
    msgLen += len;
}

void Packer::setPackedMsg(const void * msg)
{
    msgBuf = (char *) msg;
    msgPtr = msgBuf;
}

int Packer::unpackInt()
{
    int size, value;
    memcpy(&size, msgPtr, sizeof(size));

    value = ntohl(size);
    msgPtr += sizeof(size);

    return value;
}

char * Packer::unpackStr()
{
    int len;
    char *value;

    len = unpackInt();
    value = msgPtr;
    msgPtr += len;

    return value;
}

void Packer::checkBuffer(int size)
{
    char *tmp = NULL;
    int len = msgLen + size;

    if (len <= bufSize) 
        return;

    while (bufSize < len)
        bufSize *= 2;
    tmp = new char[bufSize];
    memcpy(tmp, msgBuf, msgLen);
    msgPtr = tmp + (msgPtr - msgBuf);
    delete []msgBuf;
    msgBuf = tmp;
}

