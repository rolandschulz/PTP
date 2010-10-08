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

 Classes: ExtListener

 Description: ...
   
 Author: Tu HongJ, Nicole Nie, Liu Wei

 History:
   Date     Who ID    Description
   -------- --- ---   -----------
   01/06/09 tuhongj      Initial code (D155101)

****************************************************************************/

#include <assert.h>
#include <netdb.h>
#include <stdlib.h>

#include "stream.hpp"
#include "log.hpp"
#include "tools.hpp"

#include "extlisten.hpp"
#include "locker.hpp"
#include "extlaunch.hpp"


const int SCID_PORT = 6688;

ExtListener::ExtListener()
{
}

ExtListener::~ExtListener()
{
}

void ExtListener::run()
{
    int child = -1;
    int port = SCID_PORT;
    int sockfd = -1;
    struct servent *serv = NULL; 
    char *envp = getenv("SCI_DAEMON_NAME");

    if (envp != NULL) {
        serv = getservbyname(envp, "tcp");
    } else {
        serv = getservbyname("scid", "tcp");
    }
    if (serv != NULL) {
        port = ntohs(serv->s_port);
    }
    sockfd = socket.listen(port);
    log_crit("Extended listener is running");

    while (getState()) {
        try {
            child = socket.accept(sockfd);
        } catch (SocketException &e) {
            log_error("socket exception %s", e.getErrMsg().c_str());
            break;
        } catch (...) {
            log_error("unknown exception");
            break;
        }
        
        if (child < 0)
           continue;

        Stream *stream = new Stream();
        stream->init(child);
        ExtLauncher *launcher = new ExtLauncher(stream);
        launcher->start();
    }

    ::close(sockfd);
    setState(false);
}

