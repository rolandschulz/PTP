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

#ifndef _PARENT_HPP
#define _PARENT_HPP

#include <vector>

using namespace std;

#include "sci.h"
#include "general.hpp"

class Message;
class Stream;

class Parent
{
    private:
        int               nodeID;
        char              *hostname;
        int               port;

        int               level;

    public:
        Parent(int id=-1, const char *name=NULL, int p=0);
        ~Parent();

        Message * packMsg(bool isUncle = true);
        void unpackMsg(Message &msg);

        Stream * connect();

        void setLevel(int l);

        int getNodeID();
        char *getHostName();
        int getPort();
        int getLevel();
};

class ParentList
{      
    private:
        int               level;
        vector<Parent*>   list;
        
    public:
        ParentList();
        ~ParentList();

        Message * packMsg();
        void unpackMsg(Message &msg);

        void add(Parent *p);
        Stream *select(int *nodeID);

        int numOfParents();
        bool isAllGathered();

        void setLevel(int l);
        int getLevel();
};

#endif

