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

 Classes: ErrorInjector

 Description: The functions of error handler include:
     a. accept user commands and sent to destination nodes
     b. prompt failure data to use with time stamp
     c. prompt recovery data to user with time stamp
   
 Author: SuiHe Chen

 History:
   Date     Who ID    Description
   -------- --- ---   -----------
   05/11/09 chensuih   Initial code

****************************************************************************/

#include "errmonitor.hpp"
#include <assert.h>

#include <sys/time.h> 
#include <time.h> 

#include "stream.hpp"
#include "log.hpp"
#include "errevent.hpp"
#include "ctrlblock.hpp"
#include "socket.hpp"
#include "message.hpp"
#include "queue.hpp"
#include "packer.hpp"

#define DATE_TIME_DURATION_LEN      50

ErrorMonitor::ErrorMonitor(int sockFd)
{
    socketFd = sockFd;
}

ErrorMonitor::ErrorMonitor()
{
}

ErrorMonitor::~ErrorMonitor()
{
}

void ErrorMonitor::run()
{
    log_crit("Error Monitor is running");
    Stream stream;
    stream.init(socketFd);
    Message *msg = NULL;
    ErrorEvent errEvt;
    MessageQueue  *monitorInQueue;
    monitorInQueue = gCtrlBlock->getMonitorInQueue();
    assert(monitorInQueue);
    while (getState()) {
        msg = monitorInQueue->consume();
        if (msg == NULL) 
        {
            continue;
        }

        errEvt.unpackMsg(*msg);
        //after handle msg, will remove it.
        monitorInQueue->remove();
        try{
            handleErrData(errEvt, stream);
        }
        catch (SocketException &e) {
            log_error("socket exception %s", e.getErrMsg().c_str());
            break;
        } catch (...) {
            log_error("unknown exception");
            break;
        } 
    }
    setState(false);
}

void ErrorMonitor::handleErrData(ErrorEvent &errEvent, Stream &stream)
{
    //failure or recovery time
    time_t curr_time;                                           
    struct tm *date;                         
    char dateTimeDuration[DATE_TIME_DURATION_LEN];
    curr_time = time( NULL );                                   
    date = gmtime( &curr_time );
    //stamp
    struct timeval time_v;
    ::gettimeofday(&time_v, NULL);
    double stamp = time_v.tv_sec * 1e6  + time_v.tv_usec;

    snprintf(dateTimeDuration, DATE_TIME_DURATION_LEN, "%02d:%02d:%02d(%.0lf)  ", 
             date->tm_hour + 8, date->tm_min, date->tm_sec, stamp);
    stream << errEvent.getErrCode() << errEvent.getNodeID() << errEvent.getBENum() << dateTimeDuration << endl;
}



