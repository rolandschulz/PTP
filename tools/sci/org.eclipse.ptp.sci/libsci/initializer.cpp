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

#ifdef HAVE_CONFIG_H
#include "config.h"
#endif
#include <assert.h>
#include <unistd.h>
#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <sys/types.h>
#include <pwd.h>

#include "sci.h"

#include "log.hpp"
#include "socket.hpp"
#include "stream.hpp"
#include "exception.hpp"
#include "sshfunc.hpp"

#include "embedagent.hpp"
#include "initializer.hpp"
#include "ctrlblock.hpp"
#include "routinglist.hpp"
#include "topology.hpp"
#include "launcher.hpp"
#include "queue.hpp"
#include "message.hpp"
#include "readerproc.hpp"
#include "writerproc.hpp"
#include "filterproc.hpp"
#include "handlerproc.hpp"
#include "routerproc.hpp"
#include "purifierproc.hpp"
#include "observer.hpp"
#include "listener.hpp"
#include "eventntf.hpp"
#include "allocator.hpp"
#include "filterlist.hpp"

#define SCI_DAEMON_PORT 6688

Initializer* Initializer::instance = NULL;

Initializer::Initializer()
    : syncID(-1), listener(NULL), inStream(NULL)
{
}

Initializer::~Initializer()
{
    instance = NULL;
    if (listener) {
        listener->stop();
        delete listener;
    }
    // inStream will be deleted in Writer
}

int Initializer::init()
{
    int rc = SCI_SUCCESS;
    int level = Log::INFORMATION;
    char dir[MAX_PATH_LEN] = "/opt/sci/log";
    char *envp = NULL; 
    int hndl = -1;

    try {
        if (gCtrlBlock->getMyRole() == CtrlBlock::FRONT_END) {
            rc = initFE();
        } else if (gCtrlBlock->getMyRole() == CtrlBlock::AGENT) {
            rc = initAgent();
        } else {
            rc = initBE();
        }
    } catch (Exception &e) {
        log_error("Initializer: exception %s", e.getErrMsg());
        return SCI_ERR_INITIALIZE_FAILED;
    } catch (ThreadException &e) {
        log_error("Initializer: thread exception %d", e.getErrCode());
        return SCI_ERR_INITIALIZE_FAILED;
    } catch (SocketException &e) {
        log_error("Initializer: socket exception: %s", e.getErrMsg().c_str());
        return SCI_ERR_INITIALIZE_FAILED;
    } catch (std::bad_alloc) {
        log_error("Initializer: out of memory");
        return SCI_ERR_INITIALIZE_FAILED;
    } catch (...) {
        log_error("Initializer: unknown exception");
        return SCI_ERR_INITIALIZE_FAILED;
    }

    envp = ::getenv("SCI_LOG_DIRECTORY"); 
    if (envp != NULL) {
        ::strncpy(dir, envp, sizeof(dir));
    }
    envp = ::getenv("SCI_LOG_LEVEL"); 
    if (envp != NULL)
        level = ::atoi(envp);
    
    if (gCtrlBlock->getMyRole() == CtrlBlock::FRONT_END) {
        Log::getInstance()->init(dir, "fe.log", level);
        log_debug("I am a front end, my handle is %d", gCtrlBlock->getMyHandle());
    } else if (gCtrlBlock->getMyRole() == CtrlBlock::AGENT) {
        Log::getInstance()->init(dir, "scia.log", level);
        log_debug("I am an agent, my handle is %d", gCtrlBlock->getMyHandle());
    } else {
        Log::getInstance()->init(dir, "be.log", level);
        log_debug("I am a back end, my handle is %d", gCtrlBlock->getMyHandle());
    }

    return rc;
}

Listener * Initializer::getListener()
{
    return listener;
}

Stream * Initializer::getInStream()
{
    return inStream;
}

Listener * Initializer::initListener()
{
    if (listener)
        return listener;

    listener = new Listener(-1);
    listener->init();
    listener->start();

    return listener;
}

int Initializer::initFE()
{
    char *envp = NULL;
    int hndl = gCtrlBlock->getMyHandle();
    EmbedAgent *feAgent = NULL;
    
    Topology *topo = new Topology(hndl);
    int rc = topo->init();
    if (rc != SCI_SUCCESS)
        return rc;
    gCtrlBlock->enable();

    feAgent = new EmbedAgent();
    feAgent->init(-1, NULL, NULL);
    HandlerProcessor *handler = NULL;
    if (gCtrlBlock->getEndInfo()->fe_info.mode == SCI_INTERRUPT) {
        // interrupt mode
        handler = new HandlerProcessor();
        handler->setInQueue(feAgent->getUpQueue());
        handler->setSpecific(feAgent->genPrivateData());
        gCtrlBlock->setHandlerProcessor(handler);
    } else {
        // polling mode
        Observer *ob = new Observer();
        gCtrlBlock->setObserver(ob);
        gCtrlBlock->setPollQueue(feAgent->getFilterProcessor()->getOutQueue());
        feAgent->getFilterProcessor()->setObserver(ob);
    }
    if (handler) {
        handler->start();
    }
    feAgent->work();
    gAllocator->reset();
    envp = getenv("SCI_ENABLE_LISTENER");
    if ((envp != NULL) && (strcasecmp(envp, "yes") == 0)) {
        initListener();
    }

    Message *flistMsg = gCtrlBlock->getFilterList()->packMsg(gCtrlBlock->getEndInfo()->fe_info.filter_list);
    MessageQueue *routerInQ = feAgent->getRouterInQ();
    routerInQ->produce(flistMsg);
    int msgID = gNotifier->allocate();
    Message *topoMsg = topo->packMsg();
    topoMsg->setID(msgID);
    routerInQ->produce(topoMsg);
    gNotifier->freeze(msgID, &rc);

    return rc;
}

int Initializer::initAgent()
{ 
    string nodeAddr;
    int port = -1;
    int hndl = -1;
    EmbedAgent *agent = NULL;

    getIntToken();
    inStream = initStream();
    // get hostname and port no from environment variable.
    char *envp = ::getenv("SCI_WORK_DIRECTORY");
    if (envp != NULL) {
        ::chdir(envp);
        log_debug("Change working directory to %s", envp);
    }

    envp = ::getenv("SCI_PARENT_HOSTNAME");
    if (envp != NULL) {
        nodeAddr = envp;
    }

    envp = ::getenv("SCI_PARENT_PORT");
    if (envp != NULL) {
        port = ::atoi(envp);
    }
    hndl = gCtrlBlock->getMyHandle();
    log_debug("My parent host is %s, parent port id %d, my ID is %d", nodeAddr.c_str(), port, hndl);

    agent = new EmbedAgent();
    agent->init(hndl, inStream, NULL);
    gCtrlBlock->enable();
    agent->work();
    sendSyncRet(inStream);

    return SCI_SUCCESS;
}

int Initializer::getIntToken()
{
#ifndef PSEC_OPEN_SSL
    return 0;
#endif 
    Stream ss;
    struct iovec token = {0};
    ss.init(MAX_FD);
    ss >> token >> endl;
    SSHFUNC->set_user_token(&token);
    delete [] (char *)token.iov_base;

    return 0;
}

typedef struct {
    int rt;
    char retStr[256];
} syncResult;

int Initializer::syncRetBack(int rt, string &retStr)
{
    if (gCtrlBlock->getMyRole() == CtrlBlock::FRONT_END) 
        return 0;

    syncResult *ret = (syncResult *)gNotifier->getRetVal(syncID);
    ret->rt = rt;
    strncpy(ret->retStr, retStr.c_str(), sizeof(ret->retStr));
    gNotifier->notify(syncID);

    return 0;
}

int Initializer::sendSyncRet(Stream *stream) 
{
    char *envp = ::getenv("SCI_SYNC_INIT");
    if ((envp == NULL) || (strcasecmp(envp, "yes") != 0)) 
        return 0;

    try {
        struct iovec sign = {0};
        syncResult ret = { 0, "OK :)" };

        if (gCtrlBlock->getMyRole() != CtrlBlock::BACK_END) {
            syncID = gNotifier->allocate();
            gNotifier->freeze(syncID, &ret);
        }
        SSHFUNC->sign_data(&sign, 2, &ret.rt, sizeof(ret.rt), ret.retStr, strlen(ret.retStr) + 1);
        *stream << ret.rt << ret.retStr << sign << endl;
        SSHFUNC->free_signature(&sign);
    } catch (SocketException &e) {
        log_error("socket exception %s", e.getErrMsg().c_str());
    }

    return 0;
}

Stream * Initializer::initStream()
{
    char *envp;
    int jobkey;
    string envStr;
    Stream *stream = new Stream();  
    int hndl = -1;

    stream->init(STDIN_FILENO);
    *stream >> envStr >> endl;
    parseEnvStr(envStr);
    envp = getenv("SCI_CLIENT_ID");
    assert(envp != NULL);
    hndl = atoi(envp);
    gCtrlBlock->setMyHandle(hndl);
    envp = getenv("SCI_JOB_KEY");
    assert(envp != NULL);
    jobkey = atoi(envp);
    gCtrlBlock->setJobKey(jobkey);
    envp = ::getenv("SCI_EMBED_AGENT");
    if ((envp != NULL) && (strcasecmp(envp, "yes") == 0) && (hndl < 0)) {
        gCtrlBlock->setMyRole(CtrlBlock::BACK_AGENT);
    }

    return stream;
}

int Initializer::parseEnvStr(string &envStr)
{
    string key, val;
    char *st = (char *) envStr.c_str();
    char *p = st + envStr.size();
    while (p > st) {
        p--;
        if ((*p) == '=') {
            *p = '\0';
            val = (p+1);
        } else if ((*p) == ';') {
            *p = '\0';
            key = (p+1);
            ::setenv(key.c_str(), val.c_str(), 1);
        }
    }

    return 0;
}

int Initializer::initBE()
{
    string nodeAddr;
    int port = -1;
    int hndl = gCtrlBlock->getMyHandle();
    bool extMode = false;
    char *envp = ::getenv("SCI_USE_EXTLAUNCHER");
    if ((envp != NULL) && (::strcasecmp(envp, "yes") == 0)) {
        int pID; 
        extMode = true;
        if (!getenv("SCI_PARENT_HOSTNAME") || !getenv("SCI_PARENT_PORT") || !getenv("SCI_PARENT_ID")) {
            int rc = initExtBE(hndl);
            if (rc != 0)
                return rc;
        }
        envp = ::getenv("SCI_PARENT_HOSTNAME");
        if (envp != NULL) {
            nodeAddr = envp;
        }
        envp = ::getenv("SCI_PARENT_PORT");
        if (envp != NULL) {
            port = ::atoi(envp);
        }
        envp = ::getenv("SCI_PARENT_ID");
        if (envp != NULL) {
            pID = ::atoi(envp);
        }
        inStream = new Stream();
        inStream->init(nodeAddr.c_str(), port);
        *inStream << gCtrlBlock->getJobKey() << hndl << pID << endl;
    } else {
        getIntToken();
        inStream = initStream();
    }
    gCtrlBlock->enable();

    // get hostname and port no from environment variable.
    envp = ::getenv("SCI_WORK_DIRECTORY");
    if (envp != NULL) {
        chdir(envp);
        log_debug("Change working directory to %s", envp);
    }

    hndl = gCtrlBlock->getMyHandle();
    log_debug("My parent host is %s, parent port id %d, my ID is %d", nodeAddr.c_str(), port, hndl);

    PurifierProcessor *purifier = new PurifierProcessor(hndl);
    gCtrlBlock->setPurifierProcessor(purifier);

    if (gCtrlBlock->getEndInfo()->be_info.mode == SCI_POLLING) {
        // polling mode
        MessageQueue *sysQ = new MessageQueue();
        sysQ->setName("sysQ");

        Observer *ob = new Observer();
        gCtrlBlock->setObserver(ob);
        gCtrlBlock->setPollQueue(sysQ);
        purifier->setObserver(ob);
        purifier->setOutQueue(sysQ);
    }

    if (gCtrlBlock->getMyRole() == CtrlBlock::BACK_AGENT) {
        EmbedAgent *beAgent = new EmbedAgent();
        beAgent->init(hndl, inStream, NULL);
        beAgent->work();
    } else {
        MessageQueue *userQ = new MessageQueue();
        userQ->setName("userQ");
        gCtrlBlock->setUpQueue(userQ);

        purifier->setInStream(inStream);
        WriterProcessor *writer = new WriterProcessor(hndl);
        // writer is a peer processor of purifier
        purifier->setPeerProcessor(writer);

        writer->setInQueue(userQ);
        writer->setOutStream(inStream);
        purifier->start();
        writer->start();
    }
    if (!extMode) {
        sendSyncRet(inStream);
    }

    return SCI_SUCCESS;
}

int Initializer::initExtBE(int hndl)
{
    string envStr;
    char hostname[256];

    Stream stream;
    psec_idbuf_desc &usertok = SSHFUNC->get_token();
    struct passwd *pwd = ::getpwuid(::getuid());
    string username = pwd->pw_name;
    struct iovec sign = {0};
    struct iovec token = {0};
    int rc, tmp0, tmp1, tmp2;
    int port = SCI_DAEMON_PORT;
    Launcher::MODE mode = Launcher::REQUEST;
    int jobKey = gCtrlBlock->getJobKey();
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
    rc = SSHFUNC->sign_data(&sign, 3, &mode, sizeof(mode), &jobKey, sizeof(jobKey), &hndl, sizeof(hndl));
    ::gethostname(hostname, sizeof(hostname));
    stream.init(hostname, port);
    stream << username.c_str() << usertok << sign << (int)mode << jobKey << hndl << endl;
    SSHFUNC->free_signature(&sign);
    stream >> envStr >> token >> sign >> endl;
    stream.stop();
    rc = SSHFUNC->verify_data(&sign, 2, (char *)envStr.c_str(), envStr.size() + 1, token.iov_base, token.iov_len);
    SSHFUNC->set_user_token(&token);
    delete [] (char *)sign.iov_base;
    if (rc != 0)
        return -1;
    
    parseEnvStr(envStr);
    
    return 0;
}
