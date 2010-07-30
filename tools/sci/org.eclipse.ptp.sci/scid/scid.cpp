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

 Description: SCI Service Daemon
   
 Author: Tu HongJ, Nicole Nie, Liu Wei

 History:
   Date     Who ID    Description
   -------- --- ---   -----------
   01/06/09 tuhongj      Initial code (D155101)

****************************************************************************/

#ifdef HAVE_CONFIG_H
#include "config.h"
#endif

#include <sys/types.h>
#include <sys/stat.h>
#include <signal.h>
#include <sys/types.h>
#include <fcntl.h>
#include <string.h>
#include <stdlib.h>

#include <fstream>

#include "tools.hpp"
#include "log.hpp"
#include "locker.hpp"

#include "extlisten.hpp"
#include "extlaunch.hpp"

const int MAXFD = 128;
JOB_INFO jobInfo;
string pidFile;

struct sigaction oldSa;

void sig_term(int sig)
{
    if (sig == SIGTERM) {
        log_crit("Terminating ... ");
        unlink(pidFile.c_str());
        exit(0);
    }
}

void daemonInit()
{
    umask(0);

    ::sigaction(SIGCHLD, NULL, &oldSa);
    
    struct sigaction sa;
    sa.sa_handler = SIG_IGN;
    sa.sa_flags = 0;
    sigemptyset(&sa.sa_mask);
    sigaction(SIGHUP, &sa, NULL);
    sigaction(SIGINT, &sa, NULL);
    sigaction(SIGPIPE, &sa, NULL);
    sigaction(SIGUSR1, &sa, NULL);
    sigaction(SIGUSR2, &sa, NULL);
    sigaction(SIGCHLD, &sa, NULL);
    sa.sa_handler = sig_term;
    sigaction(SIGTERM, &sa, NULL);

#ifdef _SCI_LINUX // Linux
    pid_t pid;
    if ((pid = fork()) < 0)
        exit(-1);
    else if (pid != 0) /* parent */
        exit(0);
    setsid();
#endif

    chdir("/");
    /* close off file descriptors */
    for (int i = 0; i < MAXFD; i++)
        close(i);

    /* redirect stdin, stdout, and stderr to /dev/null */
    open("/dev/null", O_RDONLY);
    open("/dev/null", O_RDWR);
    open("/dev/null", O_RDWR);
}

void writePidFile(string &pidf)
{
    ofstream pidfile(pidf.c_str());
    if (!pidfile) {
        printf("Cann't write pid file %s", pidf.c_str());
        return;
    }
    pidfile << (int)getpid();
}

int checkPidFile(string &pidf)
{
    ifstream pidfile(pidf.c_str());
    if (!pidfile)
        return 0;

    string line;
    pidfile >> line;
    if (line.size() == 0)
        return 0;

    int pid = atoi(line.c_str());
    if (kill(pid, 0) == 0)
        return -1;

    return 0;
}

int initService(char *prog)
{
    char *p = strrchr(prog, '/');
    if (p != NULL) 
        p++;
    else
        p = prog;
#ifdef _SCI_LINUX // Linux
    pidFile = string("/var/run/") + p + ".pid";
#else
    pidFile = string("/var/opt/") + p + ".pid";
#endif
    if (checkPidFile(pidFile) < 0) {
        printf("%s is already running...\n", p);
        return -1;
    }
    if (getuid() != 0) {
        printf("Must running as root\n");
        return -1;
    }

    daemonInit();
    writePidFile(pidFile);

    return 0;
}

int main(int argc, char *argv[])
{
    if (initService(argv[0]) != 0)
        return -1;

    Log::getInstance()->init("/tmp", "scidv1.log", 6);
    ExtListener *listener = new ExtListener();
    listener->start();

    while (1) {
        Locker::getLocker()->freeze();

        log_debug("Delete unused launcher");
        Locker::getLocker()->lock();
        vector<ExtLauncher *>::iterator lc;
        for (lc = launcherList.begin(); lc != launcherList.end(); lc++) {
            (*lc)->join();
            delete (*lc);
        }
        launcherList.clear();
        Locker::getLocker()->unlock();

        log_info("Begin to cleanup the jobInfo");
        Locker::getLocker()->lock();
        JOB_INFO::iterator it;
        for (it = jobInfo.begin(); it != jobInfo.end(); ) {
            if ((SysUtil::microseconds() - it->second.timestamp) > FIVE_MINUTES) {
                jobInfo.erase(it);
                log_crit("Erase jobInfo item %d", it->first);
            } else {
                ++it;
            }
        }
        log_info("Finish cleanup the jobInfo");
        Locker::getLocker()->unlock();
    }

    listener->join();
    delete listener;
    delete Log::getInstance();

    return 0;
}

