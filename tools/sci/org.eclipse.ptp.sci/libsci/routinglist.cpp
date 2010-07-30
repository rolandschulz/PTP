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

 Classes: RoutingList

 Description: Provide routing services for all threads.
   
 Author: Nicole Nie, Liu Wei

 History:
   Date     Who ID    Description
   -------- --- ---   -----------
   05/08/09 nieyy        Initial code (D156654)

****************************************************************************/

#include "routinglist.hpp"
#include <stdlib.h>
#include <assert.h>
#include <string.h>

#include <vector>

using namespace std;

#include "log.hpp"
#include "packer.hpp"
#include "group.hpp"
#include "tools.hpp"
#include "exception.hpp"
#include "stream.hpp"

#include "message.hpp"
#include "queue.hpp"
#include "ctrlblock.hpp"
#include "eventntf.hpp"
#include "dgroup.hpp"

const int MAX_SUCCESSOR_NUM = 1024;
const int TCP_ETHERNET_MTU = 1460;

RoutingList * RoutingList::instance = NULL;
RoutingList * RoutingList::getInstance()
{
    if (instance == NULL) {
        instance = new RoutingList();
    }
    return instance;
}

RoutingList::RoutingList()
    : maxSegmentSize(TCP_ETHERNET_MTU * 32)
{
    int hndl = gCtrlBlock->getMyHandle();
    char *envp = ::getenv("SCI_SEGMENT_SIZE");
    if (envp != NULL) {
        maxSegmentSize = atoi(envp);
        maxSegmentSize = maxSegmentSize > TCP_ETHERNET_MTU ? maxSegmentSize : TCP_ETHERNET_MTU * 32;
    }

    if (hndl == -1) {
        // this is a front end, not parent
        myDistriGroup = new DistributedGroup(0);
    } else {
        int pid = -1;
        char *envp = ::getenv("SCI_PARENT_ID");
        if (envp) {
            pid = ::atoi(envp);
        } else {
            throw Exception(Exception::INVALID_LAUNCH);
        }
        myDistriGroup = new DistributedGroup(pid);
    }

    successorList = new int[MAX_SUCCESSOR_NUM];
    
}

RoutingList::~RoutingList()
{  
    delete myDistriGroup;
    delete [] successorList;
    
    instance = NULL;
}

void RoutingList::parseCmd(Message *msg)
{
    bool notify = false;
    int rc = SCI_SUCCESS;
    if (msg->getType() == Message::GROUP_CREATE) {
        Packer packer(msg->getContentBuf());

        int num_bes = packer.unpackInt();
        int be_list[num_bes];
        for (int i=0; i<num_bes; i++) {
            be_list[i] = packer.unpackInt();
        }

        myDistriGroup->create(num_bes, be_list, msg->getGroup());
        bcast(msg->getGroup(), msg);

        if (gCtrlBlock->getMyRole() == CtrlBlock::FRONT_END) {
            notify = true;
        }
    } else if (msg->getType() == Message::GROUP_FREE) {
        sci_group_t group = msg->getGroup();
        
        bcast(group, msg);
        myDistriGroup->remove(group);

        if (gCtrlBlock->getMyRole() == CtrlBlock::FRONT_END) {
            notify = true;
        }
    } else if (msg->getType() == Message::GROUP_OPERATE) {
        Packer packer(msg->getContentBuf());

        sci_op_t op = (sci_op_t) packer.unpackInt();
        sci_group_t group1 = (sci_group_t) packer.unpackInt();
        sci_group_t group2 = (sci_group_t) packer.unpackInt();

        rc = myDistriGroup->operate(group1, group2, op, msg->getGroup());
        if (rc == SCI_SUCCESS) {
            bcast(msg->getGroup(), msg);
        }

        if (gCtrlBlock->getMyRole() == CtrlBlock::FRONT_END) {
            notify = true;
        }
    } else if (msg->getType() == Message::GROUP_OPERATE_EXT) {
        Packer packer(msg->getContentBuf());

        sci_op_t op = (sci_op_t) packer.unpackInt();
        sci_group_t group = (sci_group_t) packer.unpackInt();
        int num_bes = packer.unpackInt();
        int be_list[num_bes];
        for (int i=0; i<num_bes; i++) {
            be_list[i] = packer.unpackInt();
        }

        rc = myDistriGroup->operateExt(group, num_bes, be_list, op, msg->getGroup());
        if (rc == SCI_SUCCESS) {
            bcast(msg->getGroup(), msg);
        }

        if (gCtrlBlock->getMyRole() == CtrlBlock::FRONT_END) {
            notify = true;
        }
    } else if (msg->getType() == Message::GROUP_DROP) {
        myDistriGroup->dropSuccessor(msg->getID());
    } else if (msg->getType() == Message::GROUP_MERGE) {
        DistributedGroup subDistriGroup(-1);
        subDistriGroup.unpackMsg(*msg);

        if (subDistriGroup.getPID() == gCtrlBlock->getMyHandle()) {
            // if this message is from my son
            myDistriGroup->merge(msg->getID(), subDistriGroup, false);
        } else if (isSuccessorExist(subDistriGroup.getPID())){
            // if this message is from my grandson
            myDistriGroup->merge(msg->getID(), subDistriGroup, false);
        } else {
            // if this message is from my nephew
            myDistriGroup->merge(msg->getID(), subDistriGroup, true);

            // now update its parent id to me
            subDistriGroup.setPID(gCtrlBlock->getMyHandle());

            // repack a message and send to my parent
            Message *newmsg = subDistriGroup.packMsg();
            gCtrlBlock->getFilterOutQueue()->produce(newmsg);
        }
    } else {
        assert(!"should never be here");
    }

    if (notify) {
        void *ret = gNotifier->getRetVal(msg->getID());
        *((int *) ret) = rc;
        gNotifier->notify(msg->getID());
    }
}

void RoutingList::propagateGroupInfo()
{
    // propgate my group information to my parent
    Message *msg = myDistriGroup->packMsg();
    if (gCtrlBlock->getMyRole() == CtrlBlock::AGENT) {
        gCtrlBlock->getFilterOutQueue()->produce(msg);
    } else if (gCtrlBlock->getMyRole() == CtrlBlock::BACK_END) {
        gCtrlBlock->getUpQueue()->produce(msg);
    } else {
        assert(!"should not be here");
    }
}

int RoutingList::getSegments(Message *msg, Message ***segments, int ref)
{
    int i = 0;
    int segnum = (msg->getContentLen() + maxSegmentSize - 1) / maxSegmentSize + 1;
    int size = 0;
    char *ptr = msg->getContentBuf();
    sci_group_t gid = msg->getGroup();
    Message::Type typ = msg->getType();
    int mid = msg->getID();
    int mfid = msg->getFilterID();
    int hfid = mfid;
    int mlen = msg->getContentLen();
    *segments = (Message **)::malloc(segnum * sizeof(Message *));
    Message **segs = *segments;

    if ((mfid != SCI_FILTER_NULL) || (typ != Message::COMMAND)) {
        hfid = SCI_JOIN_SEGMENT;
    }
    ::memset(segs, 0, segnum * sizeof(Message *));
    segs[0] = new Message();
    segs[0]->build(hfid, gid, 0, NULL, NULL, Message::SEGMENT, segnum);
    segs[0]->setRefCount(ref);

    for (i = 1; i < segnum; i++) {
        segs[i] = new Message();
        size = (i < (segnum - 1)) ? maxSegmentSize : (mlen % maxSegmentSize);
        segs[i]->build(mfid, gid, 1, &ptr, &size, typ, mid);
        segs[i]->setRefCount(ref);
        ptr += size;
    }

    return segnum;
}

int RoutingList::bcast(sci_group_t group, Message *msg)
{
    if (group > SCI_GROUP_ALL) {
        int hndl = querySuccessorId((int) group);
        if (hndl == INVLIDSUCCESSORID) {
            return SCI_ERR_GROUP_NOTFOUND;
        } else if (hndl == VALIDBACKENDIDS) {
            ucast((int)group, msg);
        } else {
            ucast(hndl, msg);
        }
        return SCI_SUCCESS;
    }
    
    if (!isGroupExist(group)) {
        return SCI_ERR_GROUP_NOTFOUND;
    }

    splitBcast(group, msg);
    
    return SCI_SUCCESS;
}

void RoutingList::splitBcast(sci_group_t group, Message *msg)
{
    int numSor = numOfSuccessor(group);
    retrieveSuccessorList(group, successorList);

    if (msg->getContentLen() <= (maxSegmentSize * 3 / 2)) {
        int i = 0;
        // include the original queue
        for (i = 0; i < numSor; i++) {
            ucast(successorList[i], msg, numSor);
        }
    } else {
        mcast(msg, successorList, numSor);
    }
}

void RoutingList::mcast(Message *msg, int *sorList, int num)
{
    int i = 0;
    Message **segments;
    int segnum = getSegments(msg, &segments, num);
    for (i = 0; i < num; i++) {
        gCtrlBlock->queryQueue(sorList[i])->multiProduce(segments, segnum);
    }
    ::free(segments);
    if (msg->decRefCount() == 0) {
        delete msg;
    }
}

void RoutingList::ucast(int successor_id, Message *msg, int refInc)
{
    log_debug("Processor Router: send msg to successor %d", successor_id);
    if (msg->getContentLen() <= (maxSegmentSize * 3 / 2)) {
        msg->setRefCount(msg->getRefCount() + refInc);
        gCtrlBlock->queryQueue(successor_id)->produce(msg);
    } else {
        mcast(msg, &successor_id, 1);
    }

    return;
}

void RoutingList::initSubGroup(int successor_id, int start_be_id, int end_be_id)
{
    myDistriGroup->initSubGroup(successor_id, start_be_id, end_be_id);
}

void RoutingList::addBE(sci_group_t group, int successor_id, int be_id)
{
    myDistriGroup->addBE(group, successor_id, be_id);
}

void RoutingList::removeBE(int be_id)
{
    myDistriGroup->removeBE(be_id);
}

void RoutingList::removeGroup(sci_group_t group)
{
    myDistriGroup->remove(group);
}

void RoutingList::updateParentId(int pid)
{
    ::setenv("SCI_PARENT_ID", SysUtil::itoa(pid).c_str(), 1);
    myDistriGroup->setPID(pid);
}

bool RoutingList::isGroupExist(sci_group_t group)
{
    return myDistriGroup->isGroupExist(group);
}

bool RoutingList::isSuccessorExist(int successor_id)
{
    return myDistriGroup->isSuccessorExist(successor_id);
}


int RoutingList::numOfBE(sci_group_t group)
{
    return myDistriGroup->numOfBE(group);
}

int RoutingList::numOfSuccessor(sci_group_t group)
{
    return myDistriGroup->numOfSuccessor(group);
}

int RoutingList::numOfBEOfSuccessor(int successor_id)
{
    if (successor_id >= 0) {
        // if it is a back end
        return 1;
    }

    return myDistriGroup->numOfBEOfSuccessor(successor_id);
}

int RoutingList::querySuccessorId(int be_id)
{
    return myDistriGroup->querySuccessorId(be_id);
}

void RoutingList::retrieveBEList(sci_group_t group, int * ret_val)
{
    assert(ret_val);
    myDistriGroup->retrieveBEList(group, ret_val);
}

void RoutingList::retrieveSuccessorList(sci_group_t group, int * ret_val)
{
    assert(ret_val);
    myDistriGroup->retrieveSuccessorList(group, ret_val);
}

