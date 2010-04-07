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

 Classes: Parent & ParentList

 Description: Parent information which can provide adoption service.
   
 Author: Nicole Nie

 History:
   Date     Who ID    Description
   -------- --- ---   -----------
   04/28/09 nieyy        Initial code (F156654)

****************************************************************************/

#include "parent.hpp"
#include <stdlib.h>
#include <math.h>
#include <assert.h>
#include <ctype.h>
#include <string.h>

#include "packer.hpp"
#include "log.hpp"

#include "message.hpp"
#include "topology.hpp"
#include "ctrlblock.hpp"
#include "routinglist.hpp"

Parent::Parent(int id, const char * name, int p)
    : nodeID(id)
{
    if (name) {
        int len = ::strlen(name) + 1;
        hostname = new char[len];
        ::memcpy(hostname, name, len);
    }
    
    port = p;
}

Parent::~Parent()
{
    delete [] hostname;
}

Message * Parent::packMsg(bool isUncle)
{
    Packer packer;

    packer.packInt(nodeID);
    packer.packStr(hostname);
    packer.packInt(port);
    packer.packInt(level);

    char *bufs[1];
    int sizes[1];
    
    bufs[0] = packer.getPackedMsg();
    sizes[0] = packer.getPackedMsgLen();

    Message *msg = new Message();
    if (isUncle) {
        msg->build(SCI_FILTER_NULL, SCI_GROUP_ALL, 1, bufs, sizes, Message::UNCLE);
    } else {
        msg->build(SCI_FILTER_NULL, SCI_GROUP_ALL, 1, bufs, sizes, Message::PARENT);
    }
    delete [] bufs[0];
    return msg;
}

void Parent::unpackMsg(Message & msg)
{
    Packer packer(msg.getContentBuf());

    nodeID = packer.unpackInt();

    char *str = packer.unpackStr();
    int len = ::strlen(str) + 1;
    hostname = new char[len];
    ::memcpy(hostname, str, len);

    port = packer.unpackInt();
    level = packer.unpackInt();
}

void Parent::setLevel(int l)
{
    level = l;
}

Stream * Parent::connect()
{
    Stream *stream = new Stream();   
    try {
        stream->init(hostname, port);
        *stream << gCtrlBlock->getJobKey() << gCtrlBlock->getMyHandle() << endl;
    } catch (...) {
        delete stream;
        stream = NULL;
    }
    
    return stream;
}

int Parent::getNodeID()
{
    return nodeID;
}

char * Parent::getHostName()
{
    return hostname;
}

int Parent::getPort()
{
    return port;
}

int Parent::getLevel()
{
    return level;
}

ParentList::ParentList()
{
    list.clear();
}

ParentList::~ParentList()
{
    vector<Parent*>::iterator it = list.begin();
    for (; it!=list.end(); ++it) {
        delete (*it);
    }
    list.clear();
}

Message * ParentList::packMsg()
{  
    Packer packer;

    int num = list.size();
    packer.packInt(num);
    for (int i=0; i<num; i++) {
        packer.packInt(list[i]->getNodeID());
        packer.packStr(list[i]->getHostName());
        packer.packInt(list[i]->getPort());
        packer.packInt(list[i]->getLevel());
    }
    packer.packInt(level);

    char *bufs[1];
    int sizes[1];
    
    bufs[0] = packer.getPackedMsg();
    sizes[0] = packer.getPackedMsgLen();

    Message *msg = new Message();
    msg->build(SCI_FILTER_NULL, SCI_GROUP_ALL, 1, bufs, sizes, Message::UNCLE_LIST);
    delete [] bufs[0];
    return msg;
}

void ParentList::unpackMsg(Message & msg)
{
    Packer packer(msg.getContentBuf());

    int num = packer.unpackInt();
    for (int i=0; i<num; i++) {
        int node_id = packer.unpackInt();
        char *hostname = packer.unpackStr();
        int port = packer.unpackInt();
        int pLevel = packer.unpackInt();

        Parent *p = new Parent(node_id, hostname, port);
        p->setLevel(pLevel);
        list.push_back(p);
    }
    level = packer.unpackInt();
}

void ParentList::add(Parent * p)
{
    assert(p);
    list.push_back(p);
}

Stream * ParentList::select(int *nodeID)
{
    assert(nodeID);
    Stream *stream = NULL;

    ::srand((unsigned int) ::time(NULL));
    int start = ::rand() % list.size();
    for (int i=0; i<(int) list.size(); i++) {
        int pos = (i + start) % list.size();
        stream = list[pos]->connect();
        if (stream) {
            *nodeID = list[pos]->getNodeID();
            break;
        }
    }

    return stream;
}

int ParentList::numOfParents()
{
    return list.size();
}

bool ParentList::isAllGathered()
{
    if ((int) list.size() >= gRoutingList->numOfSuccessor(SCI_GROUP_ALL)) {
        return true;
    } else {
        return false;
    }
}

void ParentList::setLevel(int l)
{
    level = l;
}

int ParentList::getLevel()
{
    return level;
}

