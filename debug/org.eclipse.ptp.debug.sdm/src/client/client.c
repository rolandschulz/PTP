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
#include "dbg_proxy.h"
#include "list.h"
#include "proxy.h"
#include "proxy_tcp.h"
#include "handler.h"
#include "sdm.h"

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
	return sdm_route_get_size();
}

static proxy_svr_helper_funcs helper_funcs = {
	new_connection,
	server_count
};

static proxy_cmd cmds[] = {
	/* DBG_QUIT_CMD */						DbgClntQuit,
	/* DBG_STARTSESSION_CMD */				DbgClntStartSession,
	/* DBG_SETLINEBREAKPOINT_CMD */			DbgClntSetLineBreakpoint,
	/* DBG_SETFUNCBREAKPOINT_CMD */			DbgClntSetFuncBreakpoint,
	/* DBG_DELETEBREAKPOINT_CMD */			DbgClntDeleteBreakpoint,
	/* DBG_ENABLEBREAKPOINT_CMD */			DbgClntEnableBreakpoint,
	/* DBG_DISABLEBREAKPOINT_CMD */			DbgClntDisableBreakpoint,
	/* DBG_CONDITIONBREAKPOINT_CMD */		DbgClntConditionBreakpoint,
	/* DBG_BREAKPOINTAFTER_CMD */			DbgClntBreakpointAfter,
	/* DBG_SETWATCHPOINT_CMD */				DbgClntSetWatchpoint,
	/* DBG_GO_CMD */						DbgClntGo,
	/* DBG_STEP_CMD */						DbgClntStep,
	/* DBG_TERMINATE_CMD */					DbgClntTerminate,
	/* DBG_SUSPEND_CMD */					DbgClntSuspend,
	/* DBG_LISTSTACKFRAMES_CMD */			DbgClntListStackframes,
	/* DBG_SETCURRENTSTACKFRAME_CMD */		DbgClntSetCurrentStackframe,
	/* DBG_EVALUATEEXPRESSION_CMD */		DbgClntEvaluateExpression,
	/* DBG_GETTYPE_CMD */					DbgClntGetType,
	/* DBG_LISTLOCALVARIABLES_CMD */		DbgClntListLocalVariables,
	/* DBG_LISTARGUMENTS_CMD */				DbgClntListArguments,
	/* DBG_LISTGLOBALVARIABLES_CMD */		DbgClntListGlobalVariables,
	/* DBG_LISTINFOTHREADS_CMD */			DbgClntListInfoThreads,
	/* DBG_SETTHREADSELECT_CMD */			DbgClntSetThreadSelect,
	/* DBG_STACKINFODEPTH_CMD */			DbgClntStackInfoDepth,
	/* DBG_DATAREADMEMORY_CMD */			DbgClntDataReadMemory,
	/* DBG_DATAWRITEMEMORY_CMD */			DbgClntDataWriteMemory,
	/* DBG_LISTSIGNALS_CMD */				DbgClntListSignals,
	/* DBG_SIGNALINFO_CMD */				NULL,
	/* DBG_CLIHANDLE_CMD */					DbgClntCLIHandle,
	/* DBG_DATAEVALUATEEXPRESSION_CMD */	NULL,
	/* DBG_EVALUATEPARTIALEXPRESSION_CMD */	DbgClntEvaluatePartialExpression,
	/* DBG_DELETEPARTIALEXPRESSION_CMD */	DbgClntDeletePartialExpression
};

static proxy_commands	command_tab = {
	DBG_CMD_BASE,
	sizeof(cmds) / sizeof(proxy_cmd),
	cmds
};

void
client(char *proxy, char *host, int port)
{
	/*
	 * FIXME: size is the number of processes being debugged *not* the number of debugger
	 * processes (which includes the master). This should be supplied by the frontend or on
	 * the command line. At the moment, we just exclude the master.
	 */
	int size = sdm_route_get_size() - 1;

	if (DbgClntInit(size, sdm_route_get_id(), proxy, &helper_funcs, &command_tab) != DBGRES_OK ||
			DbgClntCreateSession(size, host, port) != DBGRES_OK) {
		fprintf(stderr, "%s\n", DbgGetErrorStr()); fflush(stderr);
		DbgClntQuit(0, 0, NULL); //TODO fixme!
		DbgClntProgress();
		return;
	}

	while (!DbgClntIsShutdown()) {
		if (DbgClntProgress() != DBGRES_OK)
			break;
	}

	DbgClntFinish();
}
