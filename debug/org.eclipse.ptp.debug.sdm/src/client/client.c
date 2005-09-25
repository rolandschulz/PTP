/******************************************************************************
 * Copyright (c) 2005 The Regents of the University of California.
 * This material was produced under U.S. Government contract W-7405-ENG-36
 * for Los Alamos National Laboratory, which is operated by the University
 * of California for the U.S. Department of Energy. The U.S. Government has
 * rights to use, reproduce, and distribute this software. NEITHER THE
 * GOVERNMENT NOR THE UNIVERSITY MAKES ANY WARRANTY, EXPRESS OR IMPLIED, OR
 * ASSUMES ANY LIABILITY FOR THE USE OF THIS SOFTWARE. If software is modified
 * to produce derivative works, such modified software should be clearly  
 * marked, so as not to confuse it with the version available from LANL.
 *
 * Additionally, this program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * LA-CC 04-115
 ******************************************************************************/

/*
 * Client runs as task id num_procs, where num_procs are the number of processes
 * in the job being debugged, and is responsible for coordinating protocol
 * messages between the debug client interface (whatever that may be)
 * and the debug servers.
 * 
 * Note that there will be num_procs+1 [0..num_procs] processes in our 
 * communicator, where num_procs is the number of processes in the parallel 
 * job being debugged. To simplify the accounting, we use the task id of
 * num_procs as the client task id and [0..num_procs-1] for the server
 * task ids.
 */

#include <stdlib.h>
#include <string.h>
#include <unistd.h>

#include "dbg.h"
#include "dbg_client.h"
#include "procset.h"
#include "list.h"
#include "proxy.h"
#include "proxy_tcp.h"

static int num_servers;

/*
 * Called by proxy to register a file descriptor
 */
static void
reg_read_file_handler(int fd, int (*handler)(int, void *), void *data)
{
	DbgClntRegisterFileHandler(fd, READ_FILE_HANDLER, handler, data);
}

/*
 * Called by proxy when a new connection
 * is establised.
 */
static int
new_connection(void)
{
	return 0;
}

static int
server_count(void)
{
	return num_servers;
}

proxy_svr_helper_funcs helper_funcs = {
	new_connection,
	server_count,
	DbgClntIsShutdown,
	reg_read_file_handler,
	DbgClntUnregisterFileHandler,
	DbgClntRegisterEventHandler,
	DbgClntProgress,
	DbgClntStartSession,
	DbgClntSetLineBreakpoint,
	DbgClntSetFuncBreakpoint,
	DbgClntDeleteBreakpoint,
	DbgClntGo,
	DbgClntStep,
	DbgClntListStackframes,
	DbgClntSetCurrentStackframe,
	DbgClntEvaluateExpression,
	DbgClntGetType,
	DbgClntListLocalVariables,
	DbgClntListArguments,
	DbgClntListGlobalVariables,
	DbgClntQuit
};

void 
client(int svr_num, int task_id, proxy *p)
{
	void *	pc;

	num_servers = svr_num;
	
	DbgClntInit(svr_num);
	
	proxy_svr_init(p, &helper_funcs);
	
	if (proxy_svr_create(p, &pc) < 0) {
		fprintf(stderr, "proxy_svr_create failed\n");
		DbgClntQuit(); //TODO fixme!
		DbgClntProgress();
		return;
	}
	
	for (;;) {
		if (proxy_svr_progress(p, pc) < 0)
			break;
	}
		
	proxy_svr_finish(p, pc);
}
