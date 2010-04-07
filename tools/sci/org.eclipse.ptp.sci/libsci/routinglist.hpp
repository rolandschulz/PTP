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

#ifndef _ROUTINGLIST_HPP
#define _ROUTINGLIST_HPP

#include "sci.h"
#include "general.hpp"

class Message;
class DistributedGroup;
class Stream;

#define SCI_ROUTE_SEGMENT -1001


class RoutingList
{      
    private:
        RoutingList();
        static RoutingList    *instance;

        DistributedGroup      *myDistriGroup;
        int                   *successorList;
        int                    maxSegmentSize;
        
    public:
        ~RoutingList();
        static RoutingList * getInstance();
        
        void parseCmd(Message *msg);
        void propagateGroupInfo();

        int getSegments(Message *msg, Message ***segments, int ref);
        int bcast(sci_group_t group, Message *msg);
        void ucast(int successor_id, Message *msg, int refInc = 1);
        void mcast(Message *msg, int *sorList, int num);
        void splitBcast(sci_group_t group, Message *msg);

        void initSubGroup(int successor_id, int start_be_id, int end_be_id);
        void addBE(sci_group_t group, int successor_id, int be_id);
        void removeBE(int be_id);
        void removeGroup(sci_group_t group);

        void updateParentId(int pid);

        bool isGroupExist(sci_group_t group);
        bool isSuccessorExist(int successor_id);

        int numOfBE(sci_group_t group);
        int numOfSuccessor(sci_group_t group);
        int numOfBEOfSuccessor(int successor_id);
        
        int querySuccessorId(int be_id);
        void retrieveBEList(sci_group_t group, int *ret_val);
        void retrieveSuccessorList(sci_group_t, int *ret_val);
};

#define gRoutingList RoutingList::getInstance()

#endif

