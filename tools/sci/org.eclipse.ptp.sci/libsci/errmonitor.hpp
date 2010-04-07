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

 Classes: ErrorInjector

 Description: The functions of error handler include:
     a. accept user commands and sent to destination nodes
     b. prompt failure data to use with time stamp
     c. prompt recovery data to user with time stamp
   
 Author: SuiHe Chen

 History:
   Date     Who ID    Description
   -------- --- ---   -----------
   05/11/09 chensuih  Initial code

****************************************************************************/

#ifndef _ERRMONITOR_HPP
#define _ERRMONITOR_HPP

#include "thread.hpp"

class Stream;
class ErrorEvent;

class ErrorMonitor : public Thread 
{
    private:
        int     socketFd;

    public:
        ErrorMonitor(int sockFd);
        ErrorMonitor();
        virtual ~ErrorMonitor();
        virtual void run();
        void handleErrData(ErrorEvent &errEvent, Stream &stream);
};

#endif

