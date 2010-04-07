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

#include "extlaunch.hpp"
#include <assert.h>
#include <signal.h>
#include <sys/types.h>
#include <errno.h>
#include <string.h>

#include "tools.hpp"
#include "log.hpp"
#include "exception.hpp"
#include "locker.hpp"
#include "stream.hpp"
#include "socket.hpp"

enum LAUNCH_MODE {
    INTERNAL,
    REGISTER,
    REQUEST
};

const int MAX_FD = 128;
vector<ExtLauncher *> launcherList;

const int MAX_PWD_BUF_SIZE = 10000;
const int MAX_ENV_VAR_NUM = 1024;

ExtLauncher::ExtLauncher(Stream *s)
    : stream(s)
{
}

ExtLauncher::~ExtLauncher()
{
}

void ExtLauncher::run()
{
    LAUNCH_MODE mode;
    int id, jobKey, rc;
    double starttm;
    string path, envStr, usernam;
    struct passwd pwd, *result = NULL;
    char *pwdBuf = new char[MAX_PWD_BUF_SIZE];

    try {
        *stream >> usernam >> (int &)mode >> jobKey >> id;
        while (1) {
            rc = ::getpwnam_r(usernam.c_str(), &pwd, pwdBuf, MAX_PWD_BUF_SIZE, &result);
            if ((rc == EINTR) || (rc == EMFILE)) {
                SysUtil::sleep(1000);
                continue;
            }
            if (NULL == result) {
                throw Exception(Exception::INVALID_USER);
            } else {
                break;
            }
        }
        switch (mode) {
            case INTERNAL:
                *stream >> path >> envStr >> endl;
                log_crit("[%s] Launch %d.%d %s with %s internally", pwd.pw_name, 
                    jobKey, id, path.c_str(), envStr.c_str());
                rc = launchInt((char *)path.c_str(), (char *)envStr.c_str(), &pwd);
                break;
            case REGISTER:
                *stream >> path >> envStr >> endl;
                log_crit("[%s] Receive register info %d.%d %s", pwd.pw_name, jobKey, 
                    id, envStr.c_str());
                rc = launchReg(jobKey, id, (char *)envStr.c_str());
                break;
            case REQUEST:
                *stream >> endl;
                log_crit("[%s] Handle external launching request %d.%d", pwd.pw_name,
                    jobKey, id);
                rc = launchReq(jobKey, id); 
                starttm = SysUtil::microseconds();
                while ((rc != 0) && ((SysUtil::microseconds() - starttm) < FIVE_MINUTES)) {
                    SysUtil::sleep(1000);
                    rc = launchReq(jobKey, id);
                }
                break;
            default:
                break;
        }
    } catch (SocketException &e) {
        log_error("socket exception %s", e.getErrMsg().c_str());
    } catch (Exception &e) {
        log_error("exception %s, errno = %d", e.getErrMsg(), errno);
    } catch (...) {
        log_error("unknown exception");
    }

    delete [] pwdBuf;

    stream->stop();
    delete stream;
    setState(false);

    Locker::getLocker()->lock();
    launcherList.push_back(this);
    Locker::getLocker()->unlock();
    Locker::getLocker()->notify();
}

int ExtLauncher::launchInt(char *path, char *envStr, struct passwd *pwd)
{
    pid_t pid;
    int i, len;
    char *p = NULL, *p1 = NULL, *p2 = NULL;
    char *exename = NULL;
    char *params[MAX_ENV_VAR_NUM];

    // get the exe name
    p1 = path;
    do {
        if (((*p1)==' ') || ((*p1)=='\t')) {
            p1++;
        } else if ((*p1) == '\0') {
            return -1;
        } else {
            break;
        }
    } while (1);
    p2 = p1;
    while (((*p2)!=' ') && ((*p2)!='\t') && ((*p2)!='\0')) {
        p2++;
    }
    len = p2 - p1;
    exename = new char[len+1];
    ::strncpy(exename, p1, len);
    exename[len] = '\0';
    
    if (::access(exename, F_OK | R_OK | X_OK) != 0) {
        log_error("%s is not an executable file", exename);
        return -1;
    }
    delete [] exename;

    pid = ::fork();
    if (pid < 0) { // fork failed
        return -1;
    } else if (pid == 0) { // child process
        for (i = 0; i < MAX_FD; i++) {
            ::close(i);
        }

        // the child process can't ignore SIGCHLD signal
        ::sigaction(SIGCHLD, &oldSa, NULL);

        p = envStr;
        for (i = 0; i < MAX_ENV_VAR_NUM-1; i++) {
            p = ::strchr(p, ';');
            if (NULL == p) {
                break;
            }
            *p = '\0';
            params[i] = ++p;
        }
        params[i] = NULL;
        
        ::setgid(pwd->pw_gid);
        ::setuid(pwd->pw_uid);
        ::execle("/bin/sh", "/bin/sh", "-c", path, (char *)NULL, params);
    }

    return 0;
}

int ExtLauncher::launchReg(int key, int id, const char *envStr)
{
    Locker::getLocker()->lock();
    jobInfo[key].config[id] = envStr;
    jobInfo[key].timestamp = SysUtil::microseconds();
    Locker::getLocker()->unlock();

    return 0;
}

int ExtLauncher::launchReq(int key, int id)
{
    Locker::getLocker()->lock();
    if (jobInfo.find(key) == jobInfo.end()) {
        Locker::getLocker()->unlock();
        return -1;
    }
    TASK_CONFIG &cfg = jobInfo[key].config;
    if (cfg.find(id) == cfg.end()) {
        Locker::getLocker()->unlock();
        return -1;
    }

    *stream << cfg[id] << endl;
    cfg.erase(id);
    if (cfg.size() == 0)
        jobInfo.erase(key);
    Locker::getLocker()->unlock();

    return 0;
}

