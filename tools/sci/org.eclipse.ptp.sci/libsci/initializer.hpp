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

 Classes: Initializer

 Description: Prepare the environment when startup, which includes:
        1) Processor threads
        2) Message queue
        3) Others like environment variables
   
 Author: Nicole Nie

 History:
   Date     Who ID    Description
   -------- --- ---   -----------
   02/10/09 nieyy      Initial code (D153875)

****************************************************************************/

#ifndef _INITIALIZER_HPP
#define _INITIALIZER_HPP

#include <string>

using namespace std;

class Stream;

class Initializer
{
    private:
        Initializer();
        static Initializer *instance;
        
    public:
        ~Initializer();
        static Initializer* getInstance() {
            if (instance == NULL)
                instance = new Initializer();
            return instance;
        }

        int init(int hndl);

        void recoverAgent(Stream *stream);
        void recoverBE(Stream *stream);

    private:
        int initFE(int hndl);
        int initAgent(int hndl);
        int initBE(int hndl);
        int initExtBE(int hndl);
        int getIntToken();

        void initListener();
};

#define gInitializer Initializer::getInstance()

#endif

