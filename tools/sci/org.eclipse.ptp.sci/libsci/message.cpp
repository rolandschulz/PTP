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

 Classes: Message

 Description: SCI internal message
   
 Author: Tu HongJ, Nicole Nie, Liu Wei

 History:
   Date     Who ID    Description
   -------- --- ---   -----------
   10/06/08 tuhongj      Initial code (D153875)

****************************************************************************/

#include "message.hpp"
#include <stdlib.h>
#include <pthread.h>
#include <sys/time.h>
#include <time.h>
#include <errno.h>
#include <assert.h>
#include <string.h>

#include "exception.hpp"
#include "log.hpp"

#include "atomic.hpp"

Message::Message(Type t)
    : type(t)
{
    msgID = DEFAULT_MSG_ID;
    filterID = SCI_FILTER_NULL;
    group= SCI_GROUP_ALL;
    len = 0;
    buf = NULL;

    refCount = 1;
}

Message::~Message()
{
    if (buf && len) {
        delete [] buf;
    }

    buf = NULL;
    len = 0;
}

int Message::joinSegments(Message **segments, int segnum)
{
    int i;
    char **bufs = (char **)::malloc(segnum * sizeof(char *));
    int *sizes = new int[segnum];
    int fid = segments[0]->getFilterID();
    sci_group_t gid = segments[0]->getGroup();
    Type typ = segments[0]->getType();
    int id = segments[0]->getID();

    for (i = 0; i < segnum; i++) {
        bufs[i] = segments[i]->getContentBuf();
        sizes[i] = segments[i]->getContentLen();
    }
    build(fid, gid, segnum, bufs, sizes, typ, id);
    ::free(bufs);
    delete sizes;
    for (i = 0; i < segnum; i++) {
        if (segments[i]->decRefCount() == 0) {
            delete segments[i];
        }
    }

    return 0;
}

void Message::build(int fid, sci_group_t g, int num_bufs, char *bufs[], int sizes[], Type t, int id)
{
    type = t;
    msgID = id;
    
    filterID = fid;
    group = g;

    len = 0;
    for (int i=0; i<num_bufs; i++) {
        len += sizes[i];
    }

    if (len > 0) {
        buf = new char[len];
        char *ptr = buf;
        for (int i=0; i<num_bufs; i++) {
            if (sizes[i] > 0) {
                ::memcpy(ptr, bufs[i], sizes[i]);
                ptr += sizes[i];
            }
        }
    }
}

void Message::setRefCount(int cnt) 
{
    refCount = cnt;
}

int Message::getRefCount()
{
    return refCount;
}

int Message::decRefCount()
{
    int cnt = fetch_and_add(&refCount, -1);
    return (cnt - 1);
}

Stream & operator >> (Stream &stream, Message &msg)
{  
    // receive message header
    stream >> (int &) msg.type;
    stream >> msg.msgID;
    stream >> msg.filterID;
    stream >> (int &) msg.group;
    
    // receive message content
    stream >> msg.len;
    if (msg.len > 0) {
        msg.buf = new char[msg.len];
        ::memset(msg.buf, 0, msg.len);
        stream.read(msg.buf, msg.len);
    }

    return stream;
}

Stream & operator << (Stream &stream, Message &msg)
{
    // send message header
    stream << (int) msg.type;
    stream << msg.msgID;
    stream << msg.filterID;
    stream << (int) msg.group;

    // send message content
    stream << msg.len;
    if (msg.len > 0) {
        stream.write(msg.buf, msg.len);
    }
 
    return stream.flush();
}

