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

 Classes: ErrorHandler

 Description: The functions of error handler include:
     a. call user-defined error handlers to process error messages
   
 Author: Nicole Nie

 History:
   Date     Who ID    Description
   -------- --- ---   -----------
   04/28/09 nieyy       Initial code (F156654)

****************************************************************************/

#ifndef _ERRORHANDLER_HPP
#define _ERRORHANDLER_HPP

#include "sci.h"
#include "processor.hpp"

class Stream;
class MessageQueue;

class ErrorHandler : public Processor 
{
    private:
        MessageQueue        *inQueue;

        SCI_err_hndlr       *hndlr;

    public:
        ErrorHandler(int hndl = -1);

        virtual Message * read();
        virtual void process(Message *msg);
        virtual void write(Message *msg);
        virtual void seize();
        virtual void clean();

        virtual bool isActive();

        void setInQueue(MessageQueue *queue);
};

#endif

