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

#include <stdlib.h>
#include <pthread.h>
#include <sys/time.h>
#include <time.h>
#include <errno.h>
#include <assert.h>
#include <string.h>

#include "exception.hpp"
#include "log.hpp"
#include "sshfunc.hpp"
#include "atomic.hpp"

#include "message.hpp"
#include "queue.hpp"

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

Message * Message::joinSegments(Message *msg, Stream *inS, MessageQueue *inQ)
{
    int i = 0;
    int segnum = msg->getID() - 1; // exclude the SEGMENT header
    Message *newMsg = NULL;
    Message **segments = (Message **)::malloc(segnum * sizeof(Message *));
    newMsg = new Message();
    if (inS) {
        delete msg;
        for (i = 0; i < segnum; i++) {
            segments[i] = new Message();
            *inS >> *segments[i];
        }
    } else {
        inQ->remove();
        inQ->multiConsume(segments, segnum);
    }
    newMsg->joinSegments(segments, segnum);
    ::free(segments);

    return newMsg;
}

int Message::joinSegments(Message **segments, int segnum)
{
    int i;
    char **bufs = (char **)malloc(segnum * sizeof(char *));
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

int Message::decRefCount(int cnt)
{
    int count = fetch_and_add(&refCount, -cnt);
    return (count - cnt);
}

int Message::incRefCount(int cnt)
{
    int count = fetch_and_add(&refCount, cnt);
    return (count + cnt);
}

Stream & operator >> (Stream &stream, Message &msg)
{  
    int rc;
    struct iovec vecs[6];
    struct iovec sign = {0};

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
    stream >> sign;
    rc = SSHFUNC->verify_data(&sign, 6, &msg.type, sizeof(msg.type), &msg.msgID, sizeof(msg.msgID), &msg.filterID, sizeof(msg.filterID), &msg.group, sizeof(msg.group), &msg.len, sizeof(msg.len), msg.buf, msg.len);
    delete [] (char *)sign.iov_base;
    if (rc != 0) {
        throw Exception(Exception::INVALID_SIGNATURE);
    }

    return stream;
}

Stream & operator << (Stream &stream, Message &msg)
{
    struct iovec vecs[6];
    struct iovec sign = {0};

    SSHFUNC->sign_data(&sign, 6, &msg.type, sizeof(msg.type), &msg.msgID, sizeof(msg.msgID), &msg.filterID, sizeof(msg.filterID), &msg.group, sizeof(msg.group), &msg.len, sizeof(msg.len), msg.buf, msg.len);
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
    stream << sign;
    SSHFUNC->free_signature(&sign);
 
    return stream.flush();
}

