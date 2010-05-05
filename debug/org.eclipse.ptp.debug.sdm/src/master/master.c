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
#include "dbg_master.h"
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
	/* DBG_QUIT_CMD */						DbgMasterQuit,
	/* DBG_STARTSESSION_CMD */				DbgMasterStartSession,
	/* DBG_SETLINEBREAKPOINT_CMD */			DbgMasterSetLineBreakpoint,
	/* DBG_SETFUNCBREAKPOINT_CMD */			DbgMasterSetFuncBreakpoint,
	/* DBG_DELETEBREAKPOINT_CMD */			DbgMasterDeleteBreakpoint,
	/* DBG_ENABLEBREAKPOINT_CMD */			DbgMasterEnableBreakpoint,
	/* DBG_DISABLEBREAKPOINT_CMD */			DbgMasterDisableBreakpoint,
	/* DBG_CONDITIONBREAKPOINT_CMD */		DbgMasterConditionBreakpoint,
	/* DBG_BREAKPOINTAFTER_CMD */			DbgMasterBreakpointAfter,
	/* DBG_SETWATCHPOINT_CMD */				DbgMasterSetWatchpoint,
	/* DBG_GO_CMD */						DbgMasterGo,
	/* DBG_STEP_CMD */						DbgMasterStep,
	/* DBG_TERMINATE_CMD */					DbgMasterTerminate,
	/* DBG_SUSPEND_CMD */					DbgMasterSuspend,
	/* DBG_LISTSTACKFRAMES_CMD */			DbgMasterListStackframes,
	/* DBG_SETCURRENTSTACKFRAME_CMD */		DbgMasterSetCurrentStackframe,
	/* DBG_EVALUATEEXPRESSION_CMD */		DbgMasterEvaluateExpression,
	/* DBG_GETTYPE_CMD */					DbgMasterGetType,
	/* DBG_LISTLOCALVARIABLES_CMD */		DbgMasterListLocalVariables,
	/* DBG_LISTARGUMENTS_CMD */				DbgMasterListArguments,
	/* DBG_LISTGLOBALVARIABLES_CMD */		DbgMasterListGlobalVariables,
	/* DBG_LISTINFOTHREADS_CMD */			DbgMasterListInfoThreads,
	/* DBG_SETTHREADSELECT_CMD */			DbgMasterSetThreadSelect,
	/* DBG_STACKINFODEPTH_CMD */			DbgMasterStackInfoDepth,
	/* DBG_DATAREADMEMORY_CMD */			DbgMasterDataReadMemory,
	/* DBG_DATAWRITEMEMORY_CMD */			DbgMasterDataWriteMemory,
	/* DBG_LISTSIGNALS_CMD */				DbgMasterListSignals,
	/* DBG_SIGNALINFO_CMD */				NULL,
	/* DBG_CLIHANDLE_CMD */					DbgMasterCLIHandle,
	/* DBG_DATAEVALUATEEXPRESSION_CMD */	NULL,
	/* DBG_EVALUATEPARTIALEXPRESSION_CMD */	DbgMasterEvaluatePartialExpression,
	/* DBG_DELETEPARTIALEXPRESSION_CMD */	DbgMasterDeletePartialExpression
};

static proxy_commands	command_tab = {
	DBG_CMD_BASE,
	sizeof(cmds) / sizeof(proxy_cmd),
	cmds
};

void
master(char *proxy, char *host, int port)
{
	/*
	 * FIXME: size is the number of processes being debugged *not* the number of debugger
	 * processes (which includes the master). This should be supplied by the frontend or on
	 * the command line. At the moment, we just exclude the master.
	 */
	int size = sdm_route_get_size() - 1;

	if (DbgMasterInit(size, sdm_route_get_id(), proxy, &helper_funcs, &command_tab) != DBGRES_OK ||
			DbgMasterCreateSession(size, host, port) != DBGRES_OK) {
		fprintf(stderr, "%s\n", DbgGetErrorStr()); fflush(stderr);
		DbgMasterQuit(0, 0, NULL); //TODO fixme!
		DbgMasterProgress();
		return;
	}

	while (!DbgMasterIsShutdown()) {
		if (DbgMasterProgress() != DBGRES_OK)
			break;
	}

	DbgMasterFinish();
}
