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

 Classes: None

 Description: Tool functions.
   
 Author: Nicole Nie, Liu Wei

 History:
   Date     Who ID    Description
   -------- --- ---   -----------
   10/06/08 nieyy        Initial code (D153875)

****************************************************************************/

#ifndef _TOOLS_HPP
#define _TOOLS_HPP

#include <string>
#include <vector>

using namespace std;

class SysUtil 
{       
    public:
        static string itoa(int value);
        static string lltoa(long long value);
        static double microseconds();
        static void sleep(int nsecs);

        static string get_hostname(const char *name);
        static char * get_path_name(const char *program);
};

#endif

