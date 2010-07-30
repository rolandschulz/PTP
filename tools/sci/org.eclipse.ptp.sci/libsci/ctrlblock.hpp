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
#include <vector>

#include "sci.h"
#include "general.hpp"

using namespace std;

class MessageQueue;
class Processor;
class Stream;

class FilterProcessor;
class RouterProcessor;

class Listener;
class Topology;
class Observer;

class ErrorInjector;

class CtrlBlock
{
    public:
        enum ROLE {
            INVALID,
            FRONT_END,
            AGENT,
            BACK_END
        };
        
        typedef map<int, MessageQueue*> QUEUE_MAP;

        typedef vector<Stream*> STREAM_VEC;
        typedef vector<Processor*> PROC_VEC;
        typedef vector<MessageQueue*> QUEUE_VEC;

    private:
        // basic information
        ROLE                 role;
        int                  handle;
        int                  jobKey;
        int                  ctrlID;
        bool                 enabled;

        sci_info_t           *endInfo;
        Topology             *topoInfo;
        Listener             *listener;

        Observer             *observer;
        ErrorInjector        *errInjector;

        // flow control threshold
        long long            thresHold;

        // internal queue, processor, stream information
        QUEUE_VEC            queues;
        PROC_VEC             processors;
        STREAM_VEC           streams;

        QUEUE_MAP            queueInfo;

        // additional information for convenient purpose
        MessageQueue         *routerInQueue;
        MessageQueue         *filterInQueue;
        MessageQueue         *filterOutQueue;
        MessageQueue         *purifierOutQueue;
        MessageQueue         *upQueue;
        MessageQueue         *pollQueue;
        
        MessageQueue         *errorQueue;
        MessageQueue         *monitorInQueue;

        RouterProcessor      *routerProc;
        FilterProcessor      *filterProc;

        Stream               *parentStream;

        // lock
        pthread_mutex_t      mtx;

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
        int getMyHandle();
        sci_info_t * getEndInfo();
        int getJobKey();
        
        int initFE(int hndl, sci_info_t *info);
        int initBE(int hndl, sci_info_t *info);
        int initAgent(int hndl);
        void term();

        void enable();
        void disable();
        bool isEnabled();
        void notifyPollQueue();

        void setTopology(Topology *topo);
        void setListener(Listener *li);
        void setObserver(Observer *ob);
        void setErrorInjector(ErrorInjector *injector);
        Topology * getTopology();
        Listener * getListener();
        Observer * getObserver();

        // main components in SCI
        void registerQueue(MessageQueue *queue);
        void registerProcessor(Processor *proc);
        void registerStream(Stream *stream);

        // only these two operations required lock protection
        void mapQueue(int hndl, MessageQueue *queue);
        MessageQueue * queryQueue(int hndl);

        void setRouterInQueue(MessageQueue *queue);
        void setFilterInQueue(MessageQueue *queue);
        void setFilterOutQueue(MessageQueue *queue);
        void setPurifierOutQueue(MessageQueue *queue);
        void setUpQueue(MessageQueue *queue);
        void setPollQueue(MessageQueue *queue);
        void setMonitorInQueue(MessageQueue *queue);
        void setErrorQueue(MessageQueue *queue);
        MessageQueue * getRouterInQueue();
        MessageQueue * getFilterInQueue();
        MessageQueue * getFilterOutQueue();
        MessageQueue * getPurifierOutQueue();
        MessageQueue * getPollQueue();
        MessageQueue * getUpQueue();
        MessageQueue * getErrorQueue();
        MessageQueue * getMonitorInQueue();
        
        void setRouterProcessor(RouterProcessor *proc);
        void setFilterProcessor(FilterProcessor *proc);
        RouterProcessor * getRouterProcessor();
        FilterProcessor * getFilterProcessor();

        void setParentStream(Stream *stream);
        Stream * getParentStream();

        void setFlowctlThreshold(long long th);
        long long getFlowctlThreshold();

        void genSelfInfo(MessageQueue *queue, bool isUncle);

    private:
        void clean();

        void lock();
        void unlock();
};

#define gCtrlBlock CtrlBlock::getInstance()

#endif

