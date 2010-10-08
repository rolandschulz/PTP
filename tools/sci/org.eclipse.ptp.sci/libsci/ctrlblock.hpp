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

 Classes: CtrlBlock

 Description: Internal running information management (Note: STL does not 
              guarantee the safety of several readers & one writer cowork 
              together, and user threads can query group information at 
              runtime, so it's necessary to add a lock to protect these 
              read & write operations).
   
 Author: Tu HongJ, Nicole Nie, Liu Wei

 History:
   Date     Who ID    Description
   -------- --- ---   -----------
   10/06/08 tuhongj      Initial code (D153875)

****************************************************************************/

#ifndef _CTRLBLOCK_HPP
#define _CTRLBLOCK_HPP

#include <pthread.h>
#include <map>

#include "sci.h"
#include "general.hpp"

using namespace std;

class MessageQueue;
class Processor;
class Stream;
class FilterProcessor;
class RouterProcessor;
class PurifierProcessor;
class Topology;
class Observer;
class RoutingList;
class EmbedAgent;
class FilterList;
class HandlerProcessor;

class CtrlBlock
{
    public:
        enum ROLE {
            INVALID,
            FRONT_END,
            AGENT,
            BACK_END,
            BACK_AGENT
        };
        
        typedef map<int, EmbedAgent *> AGENT_MAP;

    private:
        // basic information
        ROLE                 role;
        int                  handle;
        int                  jobKey;
        int                  enableID;
        pthread_mutex_t      mtx;

        sci_info_t           *endInfo;

        Observer             *observer;
        AGENT_MAP            embedAgents;

        // flow control threshold
        long long            thresHold;

        // additional information for convenient purpose
        MessageQueue         *routerInQueue;
        MessageQueue         *filterInQueue;
        MessageQueue         *filterOutQueue;
        MessageQueue         *purifierOutQueue;
        MessageQueue         *pollQueue;
        MessageQueue         *upQueue;
        
        MessageQueue         *errorQueue;
        MessageQueue         *monitorInQueue;

        RouterProcessor      *routerProc;
        FilterProcessor      *filterProc;
        PurifierProcessor    *purifierProc;
        HandlerProcessor     *handlerProc;

        Stream               *parentStream;

        CtrlBlock();
        static CtrlBlock *instance;
        
    public:
        ~CtrlBlock();
        static CtrlBlock* getInstance() {
            if (instance == NULL)
                instance = new CtrlBlock();
            return instance;
        }

        ROLE getMyRole();
        void setMyRole(CtrlBlock::ROLE ro); 
        int getMyHandle();
        void setMyHandle(int hndl);
        sci_info_t * getEndInfo();
        int getJobKey();
        void setJobKey(int key);
        void addEmbedAgent(int hndl, EmbedAgent *agent);
        EmbedAgent *getAgent(int hndl);
        
        int init(sci_info_t *info);
        void term();

        void enable();
        void disable();
        bool isEnabled();
        void releasePollQueue();

        void setObserver(Observer *ob);
        Topology * getTopology();
        Observer * getObserver();

        // main components in SCI
        void setRouterInQueue(MessageQueue *queue);
        void setFilterInQueue(MessageQueue *queue);
        void setPollQueue(MessageQueue *queue);
        void setUpQueue(MessageQueue *queue);
        void setMonitorInQueue(MessageQueue *queue);
        void setErrorQueue(MessageQueue *queue);
        MessageQueue * getRouterInQueue();
        MessageQueue * getFilterInQueue();
        MessageQueue * getPollQueue();
        MessageQueue * getUpQueue();
        MessageQueue * getErrorQueue();
        MessageQueue * getMonitorInQueue();
        
        void setRouterProcessor(RouterProcessor *proc);
        void setFilterProcessor(FilterProcessor *proc);
        void setHandlerProcessor(HandlerProcessor *proc);
        void setPurifierProcessor(PurifierProcessor *proc);
        RouterProcessor * getRouterProcessor();
        FilterProcessor * getFilterProcessor();
        PurifierProcessor * getPurifierProcessor();
        RoutingList * getRoutingList();
        FilterList * getFilterList();
        int getChildrenSockfds(int *fds);
        int numOfChildrenFds();

        void setFlowctlThreshold(long long th);
        long long getFlowctlThreshold();

        void genSelfInfo(MessageQueue *queue, bool isUncle);
        void clean();

    private:
        void lock();
        void unlock();
};

#define gCtrlBlock CtrlBlock::getInstance()

#endif

