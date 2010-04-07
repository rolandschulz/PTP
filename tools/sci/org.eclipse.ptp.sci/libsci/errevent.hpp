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

 Classes: Event

 Description: Error events.
   
 Author: Nicole Nie

 History:
   Date     Who ID    Description
   -------- --- ---   -----------
   04/29/09 nieyy        Initial code (F156654)

****************************************************************************/

#ifndef _ERROREVENT_HPP
#define _ERROREVENT_HPP

#include "sci.h"

class Message;

class ErrorEvent 
{
    private:
        int                 err_code;
        int                 node_id;
        int                 num_bes;

    public:
        ErrorEvent();
        void setErrCode(int code);
        void setNodeID(int id);
        void setBENum(int num);
        
        Message * packMsg();
        void unpackMsg(Message &msg);
        
        int getErrCode();
        int getNodeID();
        int getBENum();
};

#endif

