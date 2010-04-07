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

 Classes: Listener

 Description: Listener Thread.
   
 Author: Tu HongJ, Liu Wei, Nicole Nie

 History:
   Date     Who ID    Description
   -------- --- ---   -----------
   10/06/08 tuhongj      Initial code (D153875)

****************************************************************************/

#ifndef _LISTENER_HPP
#define _LISTENER_HPP

#include "general.hpp"
#include "thread.hpp"

class Socket;

class Listener : public Thread 
{
    private:
        int              bindPort;
        int              sockfd;
        Socket           *socket;
    
    public:
        Listener(int hndl);
        virtual ~Listener();
        
        int init();
        int stop();

        int getBindPort() { return bindPort; }
        
        virtual void run();
};

#endif

