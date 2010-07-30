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

 Classes: BEMap, Topology, Launcher

 Description: Runtime topology manipulation.
   
 Author: Nicole Nie, Liu Wei, Tu HongJ

 History:
   Date     Who ID    Description
   -------- --- ---   -----------
   10/06/08 nieyy        Initial code (D153875)

****************************************************************************/

#ifndef _TOPOLOGY_HPP
#define _TOPOLOGY_HPP

#include <vector>
#include <map>
#include <string>

using namespace std;

#include "sci.h"
#include "general.hpp"
#include "stream.hpp"
#include "envvar.hpp"

class Message;
class Launcher;

class BEMap : public map<int, string> 
{
    public:
        int input(const char *filename, int num);
};

class Topology 
{        
    private:
        // primary members
        int                  agentID;
        int                  fanOut;
        int                  level;
        int                  height;
        string               bePath;
        string               agentPath;
        BEMap                beMap;

        // other members
        int                  nextAgentID;

        // weight factors
        map<int, int> weightMap;

    public: 
        Topology(int id);
        ~Topology();

        Message * packMsg();
        Topology & unpackMsg(Message &msg);

        int init(); // only called by FE
        int deploy();

        int addBE(Message *msg);
        int removeBE(Message *msg);

        bool hasBE(int beID);
        int getBENum();
        int getLevel();
        int getFanout() { return fanOut; }

        void incWeight(int id);
        void decWeight(int id);

        friend class Launcher;

    private:
        bool isFullTree(int beNum);
};

class Launcher 
{
    public:
        enum MODE {
            INTERNAL,
            REGISTER,
            REQUEST
        };
        
    private:
        Topology        &topology;
        EnvVar          env;
        string          shell;
        string          localName;
        MODE            mode;
        bool            sync;
        vector<Stream *> initStreams;

    public:    
        Launcher(Topology &topy);
        ~Launcher();
        
        int launch();
        
        int launchBE(int beID, const char *hostname);
        int launchAgent(int beID, const char *hostname);
        int syncWaiting();
        int sendInitRet(int rc, string &retStr);

    private:
        int launchClient(int ID, string &path, string host, MODE m = INTERNAL);

        int launch_tree1(); // mininum agents
        int launch_tree2(); // maximum agents
};

const int MAX_FD = 256;
const int SCI_INIT_FD = MAX_FD + 1;

#endif

