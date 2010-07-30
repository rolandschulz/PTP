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

#ifdef HAVE_CONFIG_H
#include "config.h"
#endif
#include <assert.h>
#include <signal.h>
#include <sys/types.h>
#include <sys/wait.h>
#include <sys/socket.h>
#include <errno.h>
#include <string.h>

#include "tools.hpp"
#include "sshfunc.hpp"
#include "log.hpp"
#include "exception.hpp"
#include "locker.hpp"
#include "stream.hpp"
#include "socket.hpp"

#include "extlaunch.hpp"

const int MAX_FD = 256;
const int SCI_INIT_FD = MAX_FD + 1;

vector<ExtLauncher *> launcherList;

const int MAX_PWD_BUF_SIZE = 1024;
const int MAX_ENV_VAR_NUM = 1024;

ExtLauncher::ExtLauncher(Stream *s)
    : stream(s)
{
    memset(&usertok, 0, sizeof(usertok));
}

ExtLauncher::~ExtLauncher()
{
}

int ExtLauncher::verifyToken(bool suser) 
{
    int rc;
    struct passwd pwd;
    struct passwd *result = NULL;
    char *pwdBuf = new char[MAX_PWD_BUF_SIZE];

    while (1) {
        rc = ::getpwnam_r(userName.c_str(), &pwd, pwdBuf, MAX_PWD_BUF_SIZE, &result);
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
    if (suser) {
        ::setgid(pwd.pw_gid);
        ::setuid(pwd.pw_uid);
    }
    ::seteuid(pwd.pw_uid);
    rc = SSHFUNC->verify_id_token(pwd.pw_name, &usertok);
    delete []pwdBuf;

    return rc;
}

int ExtLauncher::verifyData(struct iovec &sign, int jobkey, int id, char *path, char *envStr)
{
    int rc = -1;
    int tmp0, tmp1, tmp2;
    struct iovec vecs[6];
    int vsize = 3;

    ssKeyLen = sizeof(sessionKey);
    rc = SSHFUNC->get_key_from_token(NULL, &usertok, sessionKey, &ssKeyLen);
    if (rc != 0)
        return rc;

    tmp0 = htonl(mode);
    vecs[0].iov_base = &tmp0;
    vecs[0].iov_len = sizeof(tmp0);
    tmp1 = htonl(jobkey);
    vecs[1].iov_base = &tmp1;
    vecs[1].iov_len = sizeof(tmp1);
    tmp2 = htonl(id);
    vecs[2].iov_base = &tmp2;
    vecs[2].iov_len = sizeof(tmp2);
    if (path != NULL) {
        vecs[3].iov_base = &sync;
        vecs[3].iov_len = sizeof(sync);
        vecs[4].iov_base = path;
        vecs[4].iov_len = strlen(path) + 1;
        vecs[5].iov_base = envStr;
        vecs[5].iov_len = strlen(envStr) + 1;
        vsize = 6;
    }
    rc = SSHFUNC->verify_data(sessionKey, ssKeyLen, vecs, vsize, &sign);

    return rc;
}

int ExtLauncher::sendResult(Stream &s, int rc)
{
    struct iovec vecs[2];
    struct iovec sign = {0};

    if (!sync)
        return 0;

    vecs[0].iov_base = &rc;
    vecs[0].iov_len = sizeof(rc);
    vecs[1].iov_base = (char *)retStr.c_str();
    vecs[1].iov_len = retStr.size() + 1;
    SSHFUNC->sign_data(sessionKey, ssKeyLen, vecs, 2, &sign);

    s << rc << retStr << sign << endl;

    return 0;
}

void ExtLauncher::run()
{
    int id, jobKey, rc;
    string path, envStr;
    struct iovec sign;
    try {
        *stream >> userName >> usertok >> sign >> (int &)mode >> jobKey >> id;
        switch (mode) {
            case INTERNAL:
                *stream >> sync >> path >> envStr >> endl;
                log_crit("[%s] Launch %d.%d %s with %s internally", userName.c_str(), 
                    jobKey, id, path.c_str(), envStr.c_str());
                rc = launchInt(jobKey, id, (char *)path.c_str(), (char *)envStr.c_str(), sign);
                if (rc != 0)
                    sendResult(*stream, rc);
                delete stream;
                break;
            case REGISTER:
                *stream >> sync >> path >> envStr >> endl;
                log_crit("[%s] Receive register info %d.%d %s", userName.c_str(), jobKey, 
                        id, envStr.c_str());
                rc = doVerify(sign, jobKey, id, (char *)path.c_str(), (char *)envStr.c_str());
                if (rc == 0) {
                    rc = launchReg(jobKey, id, (char *)envStr.c_str());
                }
                if (rc != 0) // the result will be sent back in the launchReq thread
                    sendResult(*stream, rc);
                break;
            case REQUEST:
                *stream >> endl;
                log_crit("[%s] Handle external launching request %d.%d", userName.c_str(),
                        jobKey, id);
                rc = doVerify(sign, jobKey, id);
                if (rc == 0) {
                    double starttm = SysUtil::microseconds();
                    rc = -1;
                    while ((rc != 0) && ((SysUtil::microseconds() - starttm) < FIVE_MINUTES)) {
                        rc = launchReq(jobKey, id);
                        SysUtil::sleep(1000);
                    }
                }
                delete stream;
                break;
            default:
                break;
        }
    } catch (SocketException &e) {
        log_error("socket exception %s", e.getErrMsg().c_str());
    } catch (Exception &e) {
        retStr = "invalid user";
        sendResult(*stream, -1);
        log_error("exception %s, errno = %d", e.getErrMsg(), errno);
    } catch (...) {
        log_error("unknown exception");
    }

    delete [] (char *)usertok.iov_base;
    setState(false);

    Locker::getLocker()->lock();
    launcherList.push_back(this);
    Locker::getLocker()->unlock();
    Locker::getLocker()->notify();
}

char * ExtLauncher::getExename(char *path)
{
    int len;
    char *exename = NULL;
    char *p1 = NULL, *p2 = NULL;

    // get the exe name
    p1 = path;
    do {
        if (((*p1)==' ') || ((*p1)=='\t')) {
            p1++;
        } else if ((*p1) == '\0') {
            return NULL;
        } else {
            break;
        }
    } while (1);
    p2 = p1;
    while (((*p2)!=' ') && ((*p2)!='\t') && ((*p2)!='\0')) {
        p2++;
    }
    len = p2 - p1;
    exename = new char[len+1]; // Need to be deleted outside
    ::strncpy(exename, p1, len);
    exename[len] = '\0';

    return exename;
}

int ExtLauncher::launchInt(int jobkey, int id, char *path, char *envStr, struct iovec &signature)
{
    pid_t pid;
    int i = 0;
    int rc = 0;
    char *exename = getExename(path); // There is a new inside
    char *params[4096];
    char *p;

    if (::access(exename, F_OK | R_OK | X_OK) != 0) {
        delete [] exename;
        retStr = string(exename) + " is not an executable file";
        log_error("%s", retStr.c_str());
        return -1;
    }
#ifdef PSEC_OPEN_SSL
    int sockfd[2];
    if (socketpair(AF_UNIX, SOCK_STREAM, 0, sockfd) == -1) {
        log_error("Failed to create socketpair!");
        return -1;
    }
#endif

    pid = ::fork();
    if (pid < 0) { // fork failed
        rc =  errno;
        retStr = "fork failed";
    } else if (pid == 0) { // child process
        // the child process can't ignore SIGCHLD signal
        ::sigaction(SIGCHLD, &oldSa, NULL);
        if (sync) {
            dup2(stream->getSocket(), SCI_INIT_FD);
        }
#ifdef PSEC_OPEN_SSL
        dup2(sockfd[0], MAX_FD);
#endif
        rc = putSessionKey(MAX_FD, signature, jobkey, id, path, envStr, true);
        if (rc != 0) {
            exit(rc);
        }

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

        rc = ::execle("/bin/sh", "/bin/sh", "-c", path, (char *)NULL, params); 
        if (rc < 0) {
            exit(0);
        }
    } else {
#ifdef PSEC_OPEN_SSL
        Stream ss;
        close(sockfd[0]);
        rc = getSessionKey(sockfd[1]);
        ss.init(sockfd[1]);
        ss << usertok << endl;
        close(sockfd[1]);
#endif
    }
    delete [] exename;

    return rc;
}

int ExtLauncher::getSessionKey(int fd)
{
    int n, rc;
    struct iovec vecs[2];

    vecs[0].iov_base = &rc;
    vecs[0].iov_len = sizeof(rc);
    vecs[1].iov_base = &sessionKey;
    vecs[1].iov_len = sizeof(sessionKey);
    if ((n = readv(fd, vecs, 2)) == -1) {
        rc = -1;
    }
    ssKeyLen = n - sizeof(rc);

    return rc;
}

int ExtLauncher::putSessionKey(int fd, struct iovec &sign, int jobkey, int id, char *path, char *envStr, bool suser)
{
    int i, rc;
    struct iovec vecs[2];

    for (i = 0; i < MAX_FD; i++) {
        ::close(i);
    }
    rc = verifyToken(suser);
    if (rc == 0) {
        rc = verifyData(sign, jobkey, id, path, envStr);
    }
    vecs[0].iov_base = &rc;
    vecs[0].iov_len = sizeof(rc);
    vecs[1].iov_base = sessionKey;
    vecs[1].iov_len = ssKeyLen;
    writev(fd, vecs, 2);

    return rc;
}

int ExtLauncher::doVerify(struct iovec &sign, int jobkey, int id, char *path, char *envStr)
{
#ifndef PSEC_OPEN_SSL
    return 0;
#endif
    int rc = -1;
    pid_t pid;
    int sockfd[2];

    if (socketpair(AF_UNIX, SOCK_STREAM, 0, sockfd) == -1) {
        log_error("Failed to create socketpair!");
        return -1;
    }

    if ((pid = ::fork()) < 0) {
        rc =  errno;
        retStr = "fork failed";
        log_error("Failed to fork child!");
    } else if (pid == 0) { // child process
        close(sockfd[1]);
        dup2(sockfd[0], MAX_FD);
        rc = putSessionKey(MAX_FD, sign, jobkey, id, path, envStr);
        close(MAX_FD);
        exit(0);
    } else { // parent process
        close(sockfd[0]);
        rc = getSessionKey(sockfd[1]);
        close(sockfd[1]);
    }

    return rc;
} 


int ExtLauncher::launchReg(int jobkey, int id, const char *envStr)
{
    TASK_INFO &task = jobInfo[jobkey];
    Locker::getLocker()->lock();
    task.user = userName;
    task.sync = sync;
    task.stream = stream;
    task.config[id] = envStr;
    task.timestamp = SysUtil::microseconds();
    task.token.iov_len = usertok.iov_len;
    task.token.iov_base = new char [usertok.iov_len];
    memcpy(task.token.iov_base, usertok.iov_base, usertok.iov_len);

    Locker::getLocker()->unlock();

    return 0;
}

int ExtLauncher::launchReq(int jobkey, int id)
{
    struct iovec vecs[2];
    struct iovec sign = {0};

    Locker::getLocker()->lock();
    if (jobInfo.find(jobkey) == jobInfo.end()) {
        Locker::getLocker()->unlock();
        return -1;
    }
    TASK_INFO &task = jobInfo[jobkey];
    if (task.user != userName) {
        Locker::getLocker()->unlock();
        return -2;
    }
    TASK_CONFIG &cfg = task.config;
    if (cfg.find(id) == cfg.end()) {
        Locker::getLocker()->unlock();
        return -1;
    }

    vecs[0].iov_base = (char *)cfg[id].c_str();
    vecs[0].iov_len = cfg[id].size() + 1;
    vecs[1] = task.token;
    SSHFUNC->sign_data(sessionKey, ssKeyLen, vecs, 2, &sign);
    *stream << cfg[id] << task.token << sign << endl;
    sync = task.sync;
    if (sync) { 
        delete [] (char *)task.token.iov_base;
        retStr = "OK :)";
        sendResult(*task.stream, 0);
        task.stream->stop();
        delete task.stream;
    }

    cfg.erase(id);
    if (cfg.size() == 0)
        jobInfo.erase(jobkey);
    Locker::getLocker()->unlock();

    return 0;
}

