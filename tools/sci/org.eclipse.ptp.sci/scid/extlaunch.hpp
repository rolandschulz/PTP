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

 Classes: ExtLauncher

 Description: Support External Laucher such as POE
   
 Author: Tu HongJ, Nicole Nie, Liu Wei

 History:
   Date     Who ID    Description
   -------- --- ---   -----------
   01/06/09 tuhongj      Initial code (D155101)

****************************************************************************/

#ifndef _EXTLAUNCH_HPP
#define _EXTLAUNCH_HPP

#include <pwd.h>

#include "thread.hpp"

#include <string>
#include <map>

#define FIVE_MINUTES 5000000 * 60

using namespace std;

class Stream;

class ExtLauncher : public Thread 
{
    private:
        Stream         *stream;

    public:
        ExtLauncher(Stream *s);
        virtual ~ExtLauncher();

        virtual void run();

        int launchInt(char *path, char *envStr, struct passwd *pwd);
        int launchReg(int key, int id, const char *envStr);
        int launchReq(int key, int id);
        int regInfo();
};

typedef map<int, string> TASK_CONFIG;
typedef struct TASK_INFO {
    TASK_CONFIG config;
    double      timestamp;
};
typedef map<int, TASK_INFO> JOB_INFO;

extern JOB_INFO jobInfo;
extern vector<ExtLauncher *> launcherList;
extern struct sigaction oldSa;

#endif

