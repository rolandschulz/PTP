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

 Classes: Launcher

 Description: Runtime Launch the clients.
   
 Author: Tu HongJ

 History:
   Date     Who ID    Description
   -------- --- ---   -----------
   06/21/10 tuhongj        Initial code (D153875)

****************************************************************************/

#ifndef _LAUNCHER_HPP
#define _LAUNCHER_HPP

#include "envvar.hpp"

#include <map>
#include <string>

using namespace std;

class Topology;
class Stream;

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
        int             scidPort;
        MODE            mode;
        bool            sync;

    public:    
        Launcher(Topology &topy);
        ~Launcher();
        
        int launch();
        
        int launchBE(int beID, const char *hostname);
        int launchAgent(int beID, const char *hostname);

    private:
        int launchClient(int ID, string &path, string host, MODE m = INTERNAL);

        int launch_tree1(); // mininum agents
        int launch_tree2(); // maximum agents
        int launch_tree3(); // maximum agents
        int launch_tree4(); // maximum agents
};

#endif
