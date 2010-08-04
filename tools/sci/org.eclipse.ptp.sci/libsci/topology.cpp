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

 Classes: BEMap, Topology, Launcher

 Description: Runtime topology manipulation.
   
 Author: Nicole Nie, Liu Wei, Tu HongJ

 History:
   Date     Who ID    Description
   -------- --- ---   -----------
   10/06/08 nieyy        Initial code (D153875)

****************************************************************************/

#ifdef HAVE_CONFIG_H
#include "config.h"
#endif
#include "topology.hpp"
#include <stdlib.h>
#include <math.h>
#include <assert.h>
#include <ctype.h>
#include <string.h>
#include <unistd.h>
#include <pwd.h>
#include <errno.h>
#include <sys/types.h>
#include <sys/wait.h>

#include "log.hpp"
#include "tools.hpp"
#include "packer.hpp"
#include "exception.hpp"
#include "ipconverter.hpp"

#include "ctrlblock.hpp"
#include "message.hpp"
#include "queue.hpp"
#include "routinglist.hpp"
#include "filterlist.hpp"
#include "processor.hpp"
#include "eventntf.hpp"
#include "listener.hpp"
#include "parent.hpp"
#include "sshfunc.hpp"

const int ONE_KK = 1024 * 1024;
const int SCI_DAEMON_PORT = 6688;

#ifdef __APPLE__
extern char **environ;
#endif

int BEMap::input(const char * filename, int num)
{
    FILE *fp = NULL;
    fp = ::fopen(filename,"r");
    if (NULL == fp) {
        return SCI_ERR_INVALID_HOSTFILE;
    }
    int rc = ::fseek(fp, 0, SEEK_END); //go to end
    if (rc != 0) {
        return SCI_ERR_INVALID_HOSTFILE;
    }
    long len = ::ftell(fp); //get position at end (length)
    if (len <= 0) {
        return SCI_ERR_INVALID_HOSTFILE;
    }
    rc = ::fseek(fp, 0, SEEK_SET); //go to begin
    if (rc != 0) {
        return SCI_ERR_INVALID_HOSTFILE;
    }
    char *text = new char[len+1]; //allocate buffer
    ::fread(text, len, 1, fp); //read into buffer
    ::fclose(fp);

    // mark end with '\n\0'
    text[len-1] = '\n'; // mark end
    text[len] = '\0';

    map<string, string> hostCache;
    map<string, string>::iterator it;
    
    log_debug("Hostlist is: ");
    int index = 0;
    char *pPrev = text, *pNext = text;
    while (pNext <= (text + len)) {
        if (index >= num) {
            break;
        }
        pNext++;
        if ((*pNext) == '\n') {
            *pNext = '\0';
            // ignore tabs
            while (((*pPrev) == ' ') || ((*pPrev) == '\t')) {
                pPrev++;
            }
            // ignore line with '#' as its first char
            if (((*pPrev) != '\0') && ((*pPrev) != '\n') && ((*pPrev) != '#')) {
                log_debug("%s", pPrev);
                string key = pPrev;
                it = hostCache.find(key);
                if (it == hostCache.end()) {
                    hostCache[key] = pPrev;
                    (*this)[index++] = pPrev;
                } else {
                    (*this)[index++] = (*it).second;
                }
            }
            
            pPrev = pNext+1;
        }
    }
    
    hostCache.clear();
    delete [] text;
    
    return SCI_SUCCESS;
}

Topology::Topology(int id)
    : agentID(id)
{
    beMap.clear();
    weightMap.clear();
}

Topology::~Topology()
{
    beMap.clear();
    weightMap.clear();
}

Message * Topology::packMsg()
{   
    Packer packer;
    packer.packInt(agentID);
    packer.packInt(fanOut);
    packer.packInt(level);
    packer.packInt(height);
    packer.packStr(bePath);
    packer.packStr(agentPath);

    BEMap::iterator it;
    packer.packInt(beMap.size());    
    for (it = beMap.begin(); it != beMap.end(); ++it) {
        packer.packInt((*it).first);
        packer.packStr((*it).second);
    }

    char *bufs[1];
    int sizes[1];

    bufs[0] = packer.getPackedMsg();
    sizes[0] = packer.getPackedMsgLen();

    Message *msg = new Message(Message::CONFIG);
    msg->build(SCI_FILTER_NULL, SCI_GROUP_ALL, 1, bufs, sizes, Message::CONFIG);
    delete [] bufs[0];

    return msg;
}

Topology & Topology::unpackMsg(Message &msg) 
{
    int i, id, size;
    Packer packer(msg.getContentBuf());

    agentID = packer.unpackInt();
    fanOut = packer.unpackInt();
    level = packer.unpackInt();
    height = packer.unpackInt();
    bePath = packer.unpackStr();
    agentPath = packer.unpackStr();

    size = packer.unpackInt();
    for (i = 0; i < size; i++) {
        id = packer.unpackInt();
        beMap[id] = packer.unpackStr();
    }

    return *this;
}

int Topology::init()
{
    int rc;
    char *envp = NULL;

    // check host file & num of be
    char *hostfile = gCtrlBlock->getEndInfo()->fe_info.hostfile;
    if ((envp = ::getenv("SCI_HOST_FILE")) != NULL) {
        hostfile = envp;
    }
    if (hostfile == NULL) {
        hostfile = "host.list";
    }

    int numItem = ONE_KK;
    if ((envp = ::getenv("SCI_BACKEND_NUM")) != NULL) {
        numItem = ::atoi(envp);
    }
    rc = beMap.input(hostfile, numItem);
    
    if (rc != SCI_SUCCESS) {
        return rc;
    }

    // check fanout
    fanOut = 32;
    if ((envp = ::getenv("SCI_DEBUG_FANOUT")) != NULL) {
        fanOut = ::atoi(envp);
    }
    
    level = 0;
    height = (int) ::ceil(::log((double)beMap.size()) / ::log((double)fanOut));

    // check be path
    if ((envp = ::getenv("SCI_BACKEND_PATH")) != NULL) {
        bePath = envp;
    } else {
        if (gCtrlBlock->getEndInfo()->fe_info.bepath != NULL) {
            bePath = gCtrlBlock->getEndInfo()->fe_info.bepath;
        } else {
            return SCI_ERR_UNKNOWN_INFO;
        }
    }

    // check agent path
    const char *agentName = "scia";

    if ((envp = ::getenv("SCI_AGENT_PATH")) != NULL) {
        agentPath = envp;
        agentPath += "/";
        agentPath += agentName;
    } else {
        agentPath = SysUtil::get_path_name(agentName);
    }

    return SCI_SUCCESS;
}

int Topology::deploy()
{
    Launcher launcher(*this);
    nextAgentID = (agentID + 1) * fanOut - 2; // A formular to calculate the agentID of the first child
    
    int rc = launcher.launch();
    if (rc == SCI_SUCCESS) {
        // upload my hostname & port info to my parent for uncle collection purpose
        if (gCtrlBlock->getMyRole() == CtrlBlock::AGENT) {
            gCtrlBlock->genSelfInfo(gCtrlBlock->getFilterOutQueue(), true);
        }
    }
    if (rc == SCI_SUCCESS)
        rc = launcher.syncWaiting();

    return rc;
}

int Topology::addBE(Message *msg)
{
    assert(msg);

    Packer packer(msg->getContentBuf());
    char *host = packer.unpackStr();
    int lev = packer.unpackInt();
    int id = (int) msg->getGroup();

    // find the first child agent with weight < fanOut
    int aID = INVLIDSUCCESSORID;
    map<int, int>::iterator it = weightMap.begin();
    for (; it!=weightMap.end(); ++it) {
        int weight = (*it).second;
        if (!isFullTree(weight)) {
            aID = (*it).first;
            break;
        }
    }

    int rc = SCI_SUCCESS;
    if ((aID == INVLIDSUCCESSORID) && ((lev <= level) || (level == height))) {
        // if do not find
        Launcher launcher(*this);
        if (weightMap.size() == 0) { // if this agent does not have any child agents, launch a back end
            rc = launcher.launchBE(id, host);
        } else { // if this agent has child agent(s), launch an agent
            rc = launcher.launchAgent(id, host);
        }
        launcher.syncWaiting();
    } else {
        if (aID == INVLIDSUCCESSORID)
            aID = weightMap.begin()->first;
        // otherwise delegate this command
        gRoutingList->addBE(SCI_GROUP_ALL, aID, id);
        gRoutingList->ucast(aID, msg);
        incWeight(aID);
    }

    if (rc == SCI_SUCCESS) {
        beMap[id] = host;
    }

    return rc;
}

int Topology::removeBE(Message *msg)
{
    assert(msg);

    int id = (int) msg->getGroup();
    if (!hasBE(id)) {
        return SCI_ERR_BACKEND_NOTFOUND;
    }

    int aID = gRoutingList->querySuccessorId(id);
    assert(aID != INVLIDSUCCESSORID);

    gRoutingList->removeBE(id);
    if (aID == VALIDBACKENDIDS) {
        gRoutingList->ucast(id, msg);
    } else {
        gRoutingList->ucast(aID, msg);
        decWeight(aID);
    }
    
    beMap.erase(id);
    return SCI_SUCCESS;
}

int Topology::getBENum()
{
    return beMap.size();
}

int Topology::getLevel()
{
    return level;
}

bool Topology::hasBE(int beID)
{
    if (beMap.find(beID) != beMap.end())
        return true;
    else
        return false;
}

void Topology::incWeight(int id)
{
    if (weightMap.find(id) == weightMap.end()) {
        weightMap[id] = 1;
    } else {
        weightMap[id] = weightMap[id] + 1;
    }
}

void Topology::decWeight(int id)
{
    assert(weightMap.find(id) != weightMap.end());

    weightMap[id] = weightMap[id] - 1;
    if (weightMap[id] == 0) {
        weightMap.erase(id);
    }
}

bool Topology::isFullTree(int beNum)
{ 
    if (beNum >= fanOut)
        return true;

    return false;
}

Launcher::Launcher(Topology &topo)
	: topology(topo), shell(""), mode(INTERNAL), sync(false)
{
    char *envp = NULL;
    char tmp[256] = {0};
    string envStr;

    envp = ::getenv("SCI_DEVICE_NAME");
    if (envp) {
        IPConverter converter;
        string ifname = envp;
        converter.getIP(ifname, true, localName);

        env.set("SCI_DEVICE_NAME", envp);
    } else {
        ::gethostname(tmp, sizeof(tmp));
        localName = SysUtil::get_hostname(tmp);
    }
    int jobKey = gCtrlBlock->getJobKey();

    env.set("SCI_JOB_KEY", jobKey);
    if (gCtrlBlock->getMyRole() == CtrlBlock::FRONT_END) {
        env.set("SCI_WORK_DIRECTORY", ::getenv("PWD"));
    } else {
        env.set("SCI_WORK_DIRECTORY", ::getenv("SCI_WORK_DIRECTORY"));
    }

    envp = ::getenv("SCI_AGENT_PATH");
    if (envp) {
        env.set("SCI_AGENT_PATH", envp);
    }
    envp = ::getenv("SCI_LIB_PATH");
    if (envp) {
        env.set("SCI_LIB_PATH", envp);
        envStr = envp;
    }
#ifdef _SCI_LINUX
    char *library_path = "LD_LIBRARY_PATH";
#else
    char *library_path = "LIBPATH";
#endif
    envp = ::getenv(library_path);
    if (envp) {
        if (envStr.length()) {
            envStr += ":";
            envStr += envp;
        } else {
            envStr = envp;
        }
    }
    if (envStr.length()) {
        env.set(library_path, envStr);
    }
    
    env.set("SCI_LOG_DIRECTORY", Log::getInstance()->getLogDir());
    env.set("SCI_LOG_LEVEL", Log::getInstance()->getLogLevel());
    envp = ::getenv("SCI_REMOTE_SHELL");
    if (envp) {
        shell = envp;
    }
    env.set("SCI_USE_EXTLAUNCHER", "no");
    envp = ::getenv("SCI_USE_EXTLAUNCHER");
    if (envp && (::strcasecmp(envp, "yes") == 0)) {
        mode = REGISTER;
        env.set("SCI_USE_EXTLAUNCHER", "yes");
    }
    env.set("SCI_SYNC_INIT", "no");
    envp = ::getenv("SCI_SYNC_INIT");
    if (envp && (::strcasecmp(envp, "yes") == 0)) {
        sync = true;
        env.set("SCI_SYNC_INIT", "yes");
    }
    env.set("SCI_ENABLE_FAILOVER", "no");
    envp = ::getenv("SCI_ENABLE_FAILOVER");
    if (envp && (::strcasecmp(envp, "yes") == 0)) {
        env.set("SCI_ENABLE_FAILOVER", "yes");
    }
    env.set("SCI_REMOTE_SHELL", shell);
    envp = ::getenv("SCI_DEBUG_TREE");
    if (envp) {
        env.set("SCI_DEBUG_TREE", envp);
    }
    envp = ::getenv("SCI_SEGMENT_SIZE");
    if (envp) {
        env.set("SCI_SEGMENT_SIZE", envp);
    }

    // add any tool specific environment variables
    char **tool_envp = NULL;
    if (gCtrlBlock->getMyRole() == CtrlBlock::FRONT_END) {
        tool_envp = gCtrlBlock->getEndInfo()->fe_info.beenvp;
    } else if (gCtrlBlock->getMyRole() == CtrlBlock::AGENT) {
        // In <unistd.h>, the following variable:
        //     extern char **environ;
        // is initialized as a pointer to an array of character pointers 
        // to the environment strings
        tool_envp = environ;
    }
    if (tool_envp) {
        while (*tool_envp) {
            // filter out SCI_ and library path.
            if (::strncmp(*tool_envp, "SCI_", 4) && 
                ::strncmp(*tool_envp, library_path, ::strlen(library_path))) 
            {
                char *value = ::strchr(*tool_envp, '=');
                if (value) {
                    *value = '\0';
                    env.set(*tool_envp, value+1);
                    *value = '=';
                }
            }
            tool_envp++;
        }
    }
    log_debug("Launcher: env(%s)", env.getEnvString().c_str());
}

Launcher::~Launcher()
{
    env.unsetAll();
}

int Launcher::launch()
{
    int tree = 2;
    int rc = 0;

    char *envp = ::getenv("SCI_DEBUG_TREE");
    if (envp) {
        tree = ::atoi(envp);
    }

    if (tree != 2) {
        rc = launch_tree1();
    } else {
        rc = launch_tree2();
    }

    return rc;
}

int Launcher::sendInitRet(int rc, string &retStr)
{
    if (sync && (gCtrlBlock->getMyRole() == CtrlBlock::AGENT)) {
        struct iovec vecs[2];
        struct iovec sign = {0};
        Stream stream;

        SSHFUNC->sign_data(vecs, 2, &sign);
        stream.init(SCI_INIT_FD);
        stream << rc << retStr << sign << endl;
        stream.stop();
        SSHFUNC->free_signature(&sign);
    }

    return 0;
}

int Launcher::syncWaiting()
{
    int rc = 0;
    int sockfd = -1;
    int maxfd = -1;
    string retStr;
    string backStr("OK :)");
    fd_set rset;
    struct timeval tm = {300, 0};

    if (!sync)
        return 0;

    while ((initStreams.size() > 0) && (rc == 0)) {
        FD_ZERO(&rset);
        vector<Stream *>::iterator it;
        for (it = initStreams.begin(); it != initStreams.end(); ++it) {
            sockfd = (*it)->getSocket();
            FD_SET(sockfd, &rset);
            maxfd = (maxfd > sockfd) ? maxfd : sockfd;
        }

        rc = select(maxfd+1, &rset, NULL, NULL, &tm);
        if (rc == 0)
            rc = -1;
        for (it = initStreams.begin(); it != initStreams.end(); ) {
            sockfd = (*it)->getSocket();
            if (FD_ISSET(sockfd, &rset)) {
                try {
                    int rt = -1;
                    struct iovec vecs[2];
                    struct iovec sign = {0};

                    (**it) >> rc >> retStr >> sign >> endl;
                    (*it)->stop();
                    delete *it;
                    initStreams.erase(it);
                    vecs[0].iov_base = &rc;
                    vecs[0].iov_len = sizeof(rc);
                    vecs[1].iov_base = (char *)retStr.c_str();
                    vecs[1].iov_len = retStr.size() + 1;
                    rt = SSHFUNC->verify_data(vecs, 2, &sign);
                    delete [] (char *)sign.iov_base;
                    if ((rc != 0) || (rt != 0)) {
                        rc = -1;
                        log_error("Launching init stream error, %d - %s", rc, retStr.c_str());
                        break;
                    }
                } catch (SocketException &e) {
                    delete *it;
                    initStreams.erase(it);
                    log_error("Launching init stream socket exception, %s", e.getErrMsg().c_str());
                    rc = -1;
                }
            } else {
                ++it;
            }
        }
    }

    if (rc != 0) {
        backStr = localName + "timeout or socket error";
        vector<Stream *>::iterator it;
        for (it = initStreams.begin(); it != initStreams.end(); ++it) {
            delete *it;
        }
        initStreams.clear();
    }
    assert(initStreams.size() == 0);
    sendInitRet(rc, backStr);

    return rc;
}

int Launcher::launchBE(int beID, const char * hostname)
{
    int rc;
    char queueName[32];
    Message *flistMsg = gFilterList->getFlistMsg();

    gRoutingList->addBE(SCI_GROUP_ALL, VALIDBACKENDIDS, beID);

    MessageQueue *queue = new MessageQueue();
    ::sprintf(queueName, "BE%d_inQ", beID);
    queue->setName(string(queueName));
    gCtrlBlock->registerQueue(queue);
    gCtrlBlock->mapQueue(beID, queue);
    queue->produce(flistMsg);

    rc = launchClient(beID, topology.bePath, hostname, mode);
    if (rc == SCI_SUCCESS) {
        gCtrlBlock->genSelfInfo(queue, false);
    } else {
        gRoutingList->removeBE(beID);
    }

    return rc;
}

int Launcher::launchAgent(int beID, const char * hostname)
{
    int rc;
    char queueName[32];
    
    Topology *childTopo = new Topology(topology.nextAgentID--);
    childTopo->fanOut = topology.fanOut;
    childTopo->level = topology.level + 1;
    childTopo->height =  topology.height + 1;
    childTopo->bePath = topology.bePath;
    childTopo->agentPath = topology.agentPath;
    childTopo->beMap[beID] = hostname;

    gRoutingList->addBE(SCI_GROUP_ALL, childTopo->agentID, beID);

    MessageQueue *queue = new MessageQueue();
    ::sprintf(queueName, "Agent%d_inQ", childTopo->agentID);
    queue->setName(string(queueName));
    gCtrlBlock->registerQueue(queue);
    gCtrlBlock->mapQueue(childTopo->agentID, queue);

    rc = launchClient(childTopo->agentID, childTopo->agentPath, hostname);
    if (rc == SCI_SUCCESS) {
        Message *flistMsg = gFilterList->getFlistMsg();
        Message *topoMsg = childTopo->packMsg();
        if (flistMsg != NULL) {
            flistMsg->setRefCount(flistMsg->getRefCount() + 1);
            queue->produce(flistMsg);
        }
        queue->produce(topoMsg);
        gCtrlBlock->genSelfInfo(queue, false);
        
        topology.incWeight(childTopo->agentID);
    } else {
        gRoutingList->removeBE(beID);
    }
    
    delete childTopo;
    return rc;
}

int Launcher::launchClient(int ID, string &path, string host, Launcher::MODE m)
{
    int rc = 0;
    assert(!path.empty());
    env.set("SCI_PARENT_HOSTNAME", localName);
    env.set("SCI_PARENT_PORT", gCtrlBlock->getListener()->getBindPort());
    env.set("SCI_CLIENT_ID", ID);
    env.set("SCI_PARENT_ID", topology.agentID);
    // flow control threshold
    env.set("SCI_FLOWCTL_THRESHOLD", gCtrlBlock->getFlowctlThreshold());

    log_debug("Launch client: %s: %s", host.c_str(), path.c_str());
    
    if (shell.empty()) {
        struct passwd *pwd = ::getpwuid(::getuid());
        string usernam = pwd->pw_name;

        try {
            int tmp0, tmp1, tmp2;
            struct iovec sign = {0};
            struct iovec vecs[6];
            int jobKey = gCtrlBlock->getJobKey();
            psec_idbuf_desc &usertok = SSHFUNC->get_token();
            Stream *stream = new Stream();
            stream->init(host.c_str(), SCI_DAEMON_PORT);

            tmp0 = htonl(m);
            vecs[0].iov_base = &tmp0;
            vecs[0].iov_len = sizeof(tmp0);
            tmp1 = htonl(jobKey);
            vecs[1].iov_base = &tmp1;
            vecs[1].iov_len = sizeof(tmp1);
            tmp2 = htonl(ID);
            vecs[2].iov_base = &tmp2;
            vecs[2].iov_len = sizeof(tmp2);
            vecs[3].iov_base = &sync;
            vecs[3].iov_len = sizeof(sync);
            vecs[4].iov_base = (void *)path.c_str();
            vecs[4].iov_len = path.size() + 1;
            vecs[5].iov_base = (void *)env.getEnvString().c_str();
            vecs[5].iov_len = env.getEnvString().size() + 1;
            rc = SSHFUNC->sign_data(vecs, 6, &sign);
            *stream << usernam << usertok << sign << (int)m << jobKey << ID << sync << path << env.getEnvString() << endl;
            SSHFUNC->free_signature(&sign);
            if (sync) {
                initStreams.push_back(stream);
            } else {
                delete stream;
            }
        } catch (SocketException &e) {
            rc = -1;
            log_error("Launcher: socket exception: %s", e.getErrMsg().c_str());
        }
    } else {
        string cmd = shell + " " + host + " -n '" + env.getExportcmd() + path + " >&- 2>&- <&- &'";
        rc = system(cmd.c_str());
    }

    if (rc != SCI_SUCCESS) {
        string errStr = host + " failed to launch client";
        rc = SCI_ERR_LAUNCH_FAILED;
        sendInitRet(rc, errStr);
    }

    return rc;
}

int Launcher::launch_tree1()
{
    int rc;
    
    // this tree will have minimum agents
    int totalSize = (int) topology.beMap.size();
    char queueName[32];
    Message *flistMsg = gFilterList->getFlistMsg();

    if (totalSize <= topology.fanOut) {
        // no need to generate agent
        BEMap::iterator it = topology.beMap.begin();

        int initID = (*it).first;
        int startID = initID;
        int endID = initID + totalSize - 1;
        gRoutingList->initSubGroup(VALIDBACKENDIDS, startID, endID);
        if (flistMsg != NULL)
            flistMsg->setRefCount(totalSize + 1);
        
        for ( ; it != topology.beMap.end(); ++it) {
            MessageQueue *queue = new MessageQueue();
            ::sprintf(queueName, "BE%d_inQ", (*it).first);
            queue->setName(string(queueName));
            gCtrlBlock->registerQueue(queue);
            gCtrlBlock->mapQueue((*it).first, queue);
            queue->produce(flistMsg); 

            rc = launchClient((*it).first, topology.bePath, (*it).second, mode);
            if (rc != SCI_SUCCESS) {
                return rc;
            }
            gCtrlBlock->genSelfInfo(queue, false);
        }

        return SCI_SUCCESS;
    }

    int stride = (int) ::ceil (::pow(double(topology.fanOut), topology.height - topology.level - 1));
    int divf;
    if ((totalSize % stride) == 0) {
        divf = totalSize / stride;
    } else {
        divf = (totalSize - totalSize%stride) / stride + 1;
    }
    int step;
    if ((totalSize % divf) == 0) {
        step = totalSize / divf;
    } else {
        step = (totalSize - (totalSize % divf)) / divf + 1;
    }
    if (flistMsg != NULL)
        flistMsg->setRefCount((totalSize + step - 1) / step + 1);
    ::srand((unsigned int) ::time(NULL));
    BEMap::iterator it = topology.beMap.begin();
    int initID = (*it).first;
    for (int i = 0; i < totalSize; i += step) {
        it = topology.beMap.begin();
        for (int j = 0; j < i; j++) {
            ++it;
        }

        // generate an agent
        Topology *childTopo = new Topology(topology.nextAgentID--);
        childTopo->fanOut  = topology.fanOut;
        childTopo->level = topology.level + 1;
        childTopo->height = topology.height;
        childTopo->bePath = topology.bePath;
        childTopo->agentPath = topology.agentPath;
        
        int min = (totalSize - i) < step ? (totalSize - i) : step;

        int startID = initID + i;
        int endID = initID + i + min - 1;
        gRoutingList->initSubGroup(childTopo->agentID, startID, endID);

        string hostname;
        
        int pos = ::rand() % min;
        for (int j = 0; j < min; j++) {
            if (pos == j) {
                hostname = (*it).second;
            }
            childTopo->beMap[(*it).first] = (*it).second;
            topology.incWeight(childTopo->agentID);
            ++it;
        }
        
        MessageQueue *queue = new MessageQueue();
        ::sprintf(queueName, "Agent%d_inQ", childTopo->agentID);
        queue->setName(string(queueName));
        gCtrlBlock->registerQueue(queue);
        gCtrlBlock->mapQueue(childTopo->agentID, queue);

        rc = launchClient(childTopo->agentID, topology.agentPath, hostname);
        if (rc == SCI_SUCCESS) {
            Message *msg = childTopo->packMsg();
            queue->produce(flistMsg); // before topology message
            queue->produce(msg);
            gCtrlBlock->genSelfInfo(queue, false);
            delete childTopo;
        } else {
            delete childTopo;
            return rc;
        }
    }
   
    return SCI_SUCCESS;
}

int Launcher::launch_tree2()
{
    // this tree will have maximum agents but supposed to have better performance
    // after evaluated by HongJun
    int i, rc;
    int left = 0;
    int totalSize = topology.beMap.size();
    int step;
    int size = 0;
    int out = topology.fanOut;
    char queueName[32];
    Message *flistMsg = gFilterList->getFlistMsg();
    int ref = 0; 

    if (totalSize == 0)
        return SCI_SUCCESS;

    ref = (totalSize > out) ? out : totalSize;
    if (flistMsg != NULL)
        flistMsg->setRefCount(ref + 1);  // Keep it undeleted
    // launch all of my back ends
    BEMap::iterator it = topology.beMap.begin();
    int initID = (*it).first;
    while (size < totalSize) {
        left = totalSize - size; 
        step = (left + out - 1) / out;
        out--;
        if (step == 1) {
            MessageQueue *queue = new MessageQueue();
            ::sprintf(queueName, "BE%d_inQ", (*it).first);
            queue->setName(string(queueName));
            gCtrlBlock->registerQueue(queue);
            gCtrlBlock->mapQueue((*it).first, queue);
            queue->produce(flistMsg); 
        
            gRoutingList->addBE(SCI_GROUP_ALL, VALIDBACKENDIDS, (*it).first);

            rc = launchClient((*it).first, topology.bePath, (*it).second, mode);
            if (rc == SCI_SUCCESS) {
                gCtrlBlock->genSelfInfo(queue, false);
            } else {
                return rc;
            }
            it++;
        } else {
            string &hostname = it->second;
            Topology *childTopo = new Topology(topology.nextAgentID--);
            childTopo->fanOut  = topology.fanOut;
            childTopo->level = topology.level + 1;
            childTopo->height = topology.height;
            childTopo->bePath = topology.bePath;
            childTopo->agentPath = topology.agentPath;

            int startID = initID + size;
            int endID = initID + size + step - 1;
            gRoutingList->initSubGroup(childTopo->agentID, startID, endID);

            for (i = 0; i < step; i++) {
                childTopo->beMap[(*it).first] = (*it).second;
                topology.incWeight(childTopo->agentID);
                it++;
            }

            MessageQueue *queue = new MessageQueue();
            ::sprintf(queueName, "Agent%d_inQ", childTopo->agentID);
            queue->setName(string(queueName));
            gCtrlBlock->registerQueue(queue);
            gCtrlBlock->mapQueue(childTopo->agentID, queue);
            
            rc = launchClient(childTopo->agentID, topology.agentPath, hostname);
            if (rc == SCI_SUCCESS) {
                Message *msg = childTopo->packMsg();
                queue->produce(flistMsg); // make the filter list loaded before topology
                queue->produce(msg);
                gCtrlBlock->genSelfInfo(queue, false);
                delete childTopo;
            } else {
                delete childTopo;
                return rc;
            }
        }
        size += step;
    }

    return SCI_SUCCESS;
}

