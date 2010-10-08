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
    : listener(NULL), inStream(NULL)
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
    feAgent->getRoutingList()->getTopology()->setInitID();
    rc = feAgent->work();
    gAllocator->reset();

    Message *flistMsg = gCtrlBlock->getFilterList()->packMsg(gCtrlBlock->getEndInfo()->fe_info.filter_list);
    MessageQueue *routerInQ = feAgent->getRouterInQ();
    routerInQ->produce(flistMsg);
    Message *topoMsg = topo->packMsg();
    routerInQ->produce(topoMsg);
    feAgent->syncWait();

    return rc;
}

int Initializer::initAgent()
{ 
    string nodeAddr;
    int port = -1;
    int hndl = -1;
    EmbedAgent *agent = NULL;

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

    return SCI_SUCCESS;
}

Stream * Initializer::initStream()
{
    int rc;
    string envStr;
    Stream *stream = new Stream();  
    struct iovec token = {0};
    struct iovec sign = {0};

    stream->init(STDIN_FILENO);
    *stream >> token >> envStr >> sign >> endl;
    SSHFUNC->set_user_token(&token);
    rc = psec_verify_data(&sign, "%s", envStr.c_str());
    delete [] (char *)sign.iov_base;
    if (rc != 0)
        throw Exception(Exception::INVALID_SIGNATURE);

    parseEnvStr(envStr);

    return stream;
}

int Initializer::parseEnvStr(string &envStr)
{
    char *envp;
    int hndl = -1;
    int jobkey;
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

    return 0;
}

int Initializer::initBE()
{
    string nodeAddr;
    int port = -1;
    int hndl = gCtrlBlock->getMyHandle();
    char *envp = ::getenv("SCI_USE_EXTLAUNCHER");
    if ((envp != NULL) && (::strcasecmp(envp, "yes") == 0)) {
        int pID; 
        struct iovec sign = {0};
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
        hndl = gCtrlBlock->getMyHandle();       // hndl may change
        inStream = new Stream();
        inStream->init(nodeAddr.c_str(), port);
        psec_sign_data(&sign, "%d%d%d", gCtrlBlock->getJobKey(), hndl, pID);
        *inStream << gCtrlBlock->getJobKey() << hndl << pID << sign << endl;
        psec_free_signature(&sign);
        if (hndl < 0) {
            gCtrlBlock->setMyRole(CtrlBlock::BACK_AGENT);
        }
    } else {
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
        beAgent->getRoutingList()->getTopology()->setInitID();
        beAgent->work();
        beAgent->syncWait();
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
    char fmt[32] = {0};

    if (envp != NULL) {
        serv = getservbyname(envp, "tcp");
    } else {
        serv = getservbyname("scid", "tcp");
    }
    if (serv != NULL) {
        port = ntohs(serv->s_port);
    }
    rc = psec_sign_data(&sign, "%d%d%d", mode, jobKey, hndl);
    ::gethostname(hostname, sizeof(hostname));
    stream.init(hostname, port);
    stream << username.c_str() << usertok << sign << (int)mode << jobKey << hndl << endl;
    psec_free_signature(&sign);
    stream >> envStr >> token >> sign >> endl;
    stream.stop();
    sprintf(fmt, "%%s%%%ds", token.iov_len);
    rc = psec_verify_data(&sign, fmt, envStr.c_str(), token.iov_base);
    SSHFUNC->set_user_token(&token);
    delete [] (char *)sign.iov_base;
    if (rc != 0)
        return -1;
    
    parseEnvStr(envStr);
    
    return 0;
}
