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

 Classes: WriterProcessor

 Description: Properties of class 'WriterProcessor':
    input: a message queue
    output: a stream
    action: relay messages from the queue to the stream.
   
 Author: Nicole Nie

 History:
   Date     Who ID    Description
   -------- --- ---   -----------
   05/25/09 nieyy      Initial code (F156654)

****************************************************************************/

#ifndef _WRITERPROC_HPP
#define _WRITERPROC_HPP

#include "processor.hpp"

class Stream;
class MessageQueue;

class WriterProcessor : public Processor 
{
    private:
        MessageQueue        *inQueue;
        Stream              *outStream;

    public:
        WriterProcessor(int hndl = -1);

        virtual Message * read();
        virtual void process(Message *msg);
        virtual void write(Message *msg);
        virtual void seize();
        virtual void clean();

        virtual bool isActive();

        void setInQueue(MessageQueue *queue);
        void setOutStream(Stream *stream);

        void stop();
};

#endif

