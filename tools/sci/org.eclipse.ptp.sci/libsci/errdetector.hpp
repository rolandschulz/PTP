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

 Classes: ErrorDetector

 Description: The functions of error detector include:
     a. detect connection broken events from peer processes.
     b. detect heartbeat packets from peer processes
     c. propagate failure data to peer processes
     d. propagate recovery information to peer processes
     e. establish new connection dynamically
     f. accept error injection data for testing purposes
     g. delegate eror information to error handling thread (EHT)
   
 Author: Nicole Nie

 History:
   Date     Who ID    Description
   -------- --- ---   -----------
   04/28/09 nieyy       Initial code (F156654)

****************************************************************************/

#ifndef _ERRORDETECTOR_HPP
#define _ERRORDETECTOR_HPP

#include "processor.hpp"

class Stream;
class MessageQueue;
class Parent;
class ParentList;

class ErrorDetector : public Processor 
{
    private:
        MessageQueue        *inQueue;
        MessageQueue        *outQueue; // in queue for error handler thread

        Parent              *parent;
        ParentList          *uncleList; // uncle list of mine
        ParentList          *uncleList2; // uncle list of my grandsons

        bool                needForward;

    public:
        ErrorDetector(int hndl = -1);
        ~ErrorDetector();

        virtual Message * read();
        virtual void process(Message *msg);
        virtual void write(Message *msg);
        virtual void seize();
        virtual void clean();

        virtual bool isActive();

        void processParentInfo(Message *msg);
        void processErrorEvent(Message *msg);
        void processErrorInjection(Message *msg);

        void recover();

        void setInQueue(MessageQueue *queue);
        void setOutQueue(MessageQueue *queue);
};

#endif

