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

#ifdef __gnu_linux__
#define _GNU_SOURCE
#endif /* __gnu_linux__ */

#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <errno.h>
#include <stdio.h>

#include "compat.h"
#include "dbg.h"
#include "dbg_proxy.h"
#include "dbg_client.h"
#include "client_srv.h"
#include "proxy_msg.h"
#include "bitset.h"
#include "handler.h"
#include "list.h"

#define RUNNING				0
#define SHUTDOWN_STARTED	1
#define SHUTDOWN_COMPLETED	2

static int				dbg_state;
static bitset *			dbg_procs = NULL;
static proxy_svr *		dbg_proxy;
/*
 * The TIMEOUT determines how often the debugger polls progress routines.
 * Too often and it uses too much CPU, too few and things will slow down...
 */
#define CLIENT_TIMEOUT	1000
#define SERVER_TIMEOUT	1000

/**
 * A send command is completed. Process the result and 
 * call all registered event handlers. 
 */
static void
dbg_clnt_cmd_completed(bitset *mask, char *msg, void *data)
{
	proxy_msg *	m;

 	if (proxy_deserialize_msg(msg, strlen(msg), &m) < 0)
 		fprintf(stderr, "bad conversion to proxy event");
 	else {
 		/*
 		 * Insert actual bitset as first arg. The debug events sent internally
 		 * don't include the bitset, it is only added prior to sending back
 		 * to the client.
 		 */
		proxy_msg_insert_bitset(m, mask, 0);
 		proxy_svr_queue_msg(dbg_proxy, m);
	}
}

int
DbgClntInit(int num_svrs, int my_id, char *name, proxy_svr_helper_funcs *funcs, proxy_commands *cmds)
{
	struct timeval	tv = { 0, CLIENT_TIMEOUT };	
	
	/*
	 * Initialize client/server interface
	 */
	ClntSvrInit(num_svrs, my_id);
	
	/*
	 * Register callback
	 */
	ClntSvrRegisterCompletionCallback(dbg_clnt_cmd_completed);
	
	/*
	 * Create a bitset containing all processes
	 */
	dbg_procs = bitset_new(num_svrs);
	bitset_invert(dbg_procs);
	
	/*
	 * Reset state
	 */
	dbg_state = RUNNING;
	
	/*
	 * Initialize proxy
	 */
	if (proxy_svr_init(name, &tv, funcs, cmds, &dbg_proxy) != PROXY_RES_OK) {
		DbgSetError(DBGERR_DEBUGGER, proxy_get_error_str());
		return DBGRES_ERR;
	}
	
	return DBGRES_OK;
}

int
DbgClntCreateSession(int num, char *host, int port)
{
	int			res;
	
	if (host != NULL)
		res = proxy_svr_connect(dbg_proxy, host, port);
	else
		res = proxy_svr_create(dbg_proxy, port);
	
	if (res == PROXY_RES_ERR) {
		DbgSetError(DBGERR_DEBUGGER, proxy_get_error_str());
		return DBGRES_ERR;
	}
	
	return DBGRES_OK;
}

void
DbgClntFinish(void)
{
	proxy_svr_finish(dbg_proxy);
}

int
DbgClntIsShutdown(void)
{
	
	/*
	 * The next event received after a quit command
	 * must be the server shutting down.
	 */
	if (dbg_state == SHUTDOWN_STARTED) {
		printf("sdm: shutdown completed\n"); fflush(stdout);//TODO
		dbg_state = SHUTDOWN_COMPLETED;
		return 0;
	}

	return (dbg_state == SHUTDOWN_COMPLETED) ? 1 : 0;
}

int 
DbgClntStartSession(int tid, int nargs, char **args)
{
	int			res;
	char *		buf;
	proxy_msg *	msg = new_proxy_msg(DBG_STARTSESSION_CMD, tid);
	
	proxy_msg_add_int(msg, SERVER_TIMEOUT);
	proxy_msg_add_args_nocopy(msg, nargs, args);
	proxy_serialize_msg(msg, &buf);
	
	res = ClntSvrSendCommand(dbg_procs, DBG_EV_WAITALL, buf, NULL);
	
	free_proxy_msg(msg);
	free(buf);
	
	return res;
}

/*
 * Breakpoint operations
 */
int 
DbgClntSetLineBreakpoint(int tid, int nargs, char **args)
{
	int			res;
	bitset *	set; 
	char *		buf;
	proxy_msg *	msg;
	
	set = str_to_bitset(args[0]);
	if (set == NULL) {
		DbgSetError(DBGERR_PROCSET, NULL);
		return DBGRES_ERR;
	}

	msg = new_proxy_msg(DBG_SETLINEBREAKPOINT_CMD, tid);
	proxy_msg_add_args_nocopy(msg, --nargs, &args[1]);
	proxy_serialize_msg(msg, &buf);

	res = ClntSvrSendCommand(set, DBG_EV_WAITALL, buf, NULL);
	
	bitset_free(set);
	free_proxy_msg(msg);
	free(buf);
		
	return res;
}

int 
DbgClntSetFuncBreakpoint(int tid, int nargs, char **args)
{
	int			res;
	char *		buf;
	proxy_msg *	msg;
	bitset *	set; 

	set = str_to_bitset(args[0]);
	if (set == NULL) {
		DbgSetError(DBGERR_PROCSET, NULL);
		return DBGRES_ERR;
	}

	msg = new_proxy_msg(DBG_SETFUNCBREAKPOINT_CMD, tid);
	proxy_msg_add_args_nocopy(msg, --nargs, &args[1]);
	proxy_serialize_msg(msg, &buf);

	res = ClntSvrSendCommand(set, DBG_EV_WAITALL, buf, NULL);
	
	bitset_free(set);
	free_proxy_msg(msg);
	free(buf);
		
	return res;
}

int 
DbgClntDeleteBreakpoint(int tid, int nargs, char **args)
{
	int			res;
	char *		buf;
	proxy_msg *	msg;
	bitset *	set; 

	set = str_to_bitset(args[0]);
	if (set == NULL) {
		DbgSetError(DBGERR_PROCSET, NULL);
		return DBGRES_ERR;
	}
	
	msg = new_proxy_msg(DBG_DELETEBREAKPOINT_CMD, tid);
	proxy_msg_add_args_nocopy(msg, --nargs, &args[1]);
	proxy_serialize_msg(msg, &buf);

	res = ClntSvrSendCommand(set, DBG_EV_WAITALL, buf, NULL);
	
	bitset_free(set);
	free_proxy_msg(msg);
	free(buf);
		
	return res;
}

int 
DbgClntEnableBreakpoint(int tid, int nargs, char **args)
{
	int			res;
	char *		buf;
	proxy_msg *	msg;
	bitset *	set; 

	set = str_to_bitset(args[0]);
	if (set == NULL) {
		DbgSetError(DBGERR_PROCSET, NULL);
		return DBGRES_ERR;
	}
	
	msg = new_proxy_msg(DBG_ENABLEBREAKPOINT_CMD, tid);
	proxy_msg_add_args_nocopy(msg, --nargs, &args[1]);
	proxy_serialize_msg(msg, &buf);

	res = ClntSvrSendCommand(set, DBG_EV_WAITALL, buf, NULL);
	
	bitset_free(set);
	free_proxy_msg(msg);
	free(buf);
		
	return res;
}

int 
DbgClntDisableBreakpoint(int tid, int nargs, char **args)
{
	int			res;
	char *		buf;
	proxy_msg *	msg;
	bitset *	set; 

	set = str_to_bitset(args[0]);
	if (set == NULL) {
		DbgSetError(DBGERR_PROCSET, NULL);
		return DBGRES_ERR;
	}
	
	msg = new_proxy_msg(DBG_DISABLEBREAKPOINT_CMD, tid);
	proxy_msg_add_args_nocopy(msg, --nargs, &args[1]);
	proxy_serialize_msg(msg, &buf);

	res = ClntSvrSendCommand(set, DBG_EV_WAITALL, buf, NULL);
	
	bitset_free(set);
	free_proxy_msg(msg);
	free(buf);
		
	return res;
}

int 
DbgClntConditionBreakpoint(int tid, int nargs, char **args)
{
	int			res;
	char *		buf;
	proxy_msg *	msg;
	bitset *	set; 

	set = str_to_bitset(args[0]);
	if (set == NULL) {
		DbgSetError(DBGERR_PROCSET, NULL);
		return DBGRES_ERR;
	}
	
	msg = new_proxy_msg(DBG_CONDITIONBREAKPOINT_CMD, tid);
	proxy_msg_add_args_nocopy(msg, --nargs, &args[1]);
	proxy_serialize_msg(msg, &buf);

	res = ClntSvrSendCommand(set, DBG_EV_WAITALL, buf, NULL);
	
	bitset_free(set);
	free_proxy_msg(msg);
	free(buf);
		
	return res;
}

int 
DbgClntBreakpointAfter(int tid, int nargs, char **args)
{
	int			res;
	char *		buf;
	proxy_msg *	msg;
	bitset *	set; 
	
	set = str_to_bitset(args[0]);
	if (set == NULL) {
		DbgSetError(DBGERR_PROCSET, NULL);
		return DBGRES_ERR;
	}
	
	msg = new_proxy_msg(DBG_BREAKPOINTAFTER_CMD, tid);
	proxy_msg_add_args_nocopy(msg, --nargs, &args[1]);
	proxy_serialize_msg(msg, &buf);

	res = ClntSvrSendCommand(set, DBG_EV_WAITALL, buf, NULL);
	
	bitset_free(set);
	free_proxy_msg(msg);
	free(buf);
		
	return res;
}

int 
DbgClntSetWatchpoint(int tid, int nargs, char **args)
{
	int			res;
	char *		buf;
	proxy_msg *	msg;
	bitset *	set; 

	set = str_to_bitset(args[0]);
	if (set == NULL) {
		DbgSetError(DBGERR_PROCSET, NULL);
		return DBGRES_ERR;
	}
	
	msg = new_proxy_msg(DBG_SETWATCHPOINT_CMD, tid);
	proxy_msg_add_args_nocopy(msg, --nargs, &args[1]);
	proxy_serialize_msg(msg, &buf);

	res = ClntSvrSendCommand(set, DBG_EV_WAITALL, buf, NULL);
	
	bitset_free(set);
	free_proxy_msg(msg);
	free(buf);
		
	return res;
}

/*
 * Process control operations
 */
int 
DbgClntGo(int tid, int nargs, char **args)
{
	int			res;
	char *		buf;
	proxy_msg *	msg;
	bitset *	set; 

	set = str_to_bitset(args[0]);
	if (set == NULL) {
		DbgSetError(DBGERR_PROCSET, NULL);
		return DBGRES_ERR;
	}
	
	msg = new_proxy_msg(DBG_GO_CMD, tid);
	proxy_msg_add_args_nocopy(msg, --nargs, &args[1]);
	proxy_serialize_msg(msg, &buf);

	res = ClntSvrSendCommand(set, DBG_EV_WAITSOME, buf, NULL);
	
	bitset_free(set);
	free_proxy_msg(msg);
	free(buf);
		
	return res;
}

int 
DbgClntStep(int tid, int nargs, char **args)
{
	int			res;
	char *		buf;
	proxy_msg *	msg;
	bitset *	set; 

	set = str_to_bitset(args[0]);
	if (set == NULL) {
		DbgSetError(DBGERR_PROCSET, NULL);
		return DBGRES_ERR;
	}
	
	msg = new_proxy_msg(DBG_STEP_CMD, tid);
	proxy_msg_add_args_nocopy(msg, --nargs, &args[1]);
	proxy_serialize_msg(msg, &buf);

	res = ClntSvrSendCommand(set, DBG_EV_WAITSOME, buf, NULL);
	
	bitset_free(set);
	free_proxy_msg(msg);
	free(buf);
	
	return res;
}

int 
DbgClntTerminate(int tid, int nargs, char **args)
{
	int			res;
	char *		buf;
	proxy_msg *	msg;
	bitset *	set; 

	set = str_to_bitset(args[0]);
	if (set == NULL) {
		DbgSetError(DBGERR_PROCSET, NULL);
		return DBGRES_ERR;
	}
	
	msg = new_proxy_msg(DBG_TERMINATE_CMD, tid);
	proxy_msg_add_args_nocopy(msg, --nargs, &args[1]);
	proxy_serialize_msg(msg, &buf);

	res = ClntSvrSendCommand(set, DBG_EV_WAITALL, buf, NULL);
	
	bitset_free(set);
	free_proxy_msg(msg);
	free(buf);
	
	return res;
}

int 
DbgClntSuspend(int tid, int nargs, char **args)
{
	int			res;
	bitset *	set; 

	set = str_to_bitset(args[0]);
	if (set == NULL) {
		DbgSetError(DBGERR_PROCSET, NULL);
		return DBGRES_ERR;
	}
	
	res = ClntSvrSendInterrupt(set);
	
	bitset_free(set);
	
	return res;
}

/*
 * Stack frame operations
 */
int 
DbgClntListStackframes(int tid, int nargs, char **args)
{
	int			res;
	char *		buf;
	proxy_msg *	msg;
	bitset *	set; 

	set = str_to_bitset(args[0]);
	if (set == NULL) {
		DbgSetError(DBGERR_PROCSET, NULL);
		return DBGRES_ERR;
	}
	
	msg = new_proxy_msg(DBG_LISTSTACKFRAMES_CMD, tid);
	proxy_msg_add_args_nocopy(msg, --nargs, &args[1]);
	proxy_serialize_msg(msg, &buf);

	res = ClntSvrSendCommand(set, DBG_EV_WAITALL, buf, NULL);
	
	bitset_free(set);
	free_proxy_msg(msg);
	free(buf);
	
	return res;
}

int 
DbgClntSetCurrentStackframe(int tid, int nargs, char **args)
{
	int			res;
	char *		buf;
	proxy_msg *	msg;
	bitset *	set; 

	set = str_to_bitset(args[0]);
	if (set == NULL) {
		DbgSetError(DBGERR_PROCSET, NULL);
		return DBGRES_ERR;
	}
	
	msg = new_proxy_msg(DBG_SETCURRENTSTACKFRAME_CMD, tid);
	proxy_msg_add_args_nocopy(msg, --nargs, &args[1]);
	proxy_serialize_msg(msg, &buf);

	res = ClntSvrSendCommand(set, DBG_EV_WAITALL, buf, NULL);
	
	bitset_free(set);
	free_proxy_msg(msg);
	free(buf);
	
	return res;
}

/*
 * Expression/variable operations
 */
int 
DbgClntEvaluateExpression(int tid, int nargs, char **args)
{
	int			res;
	char *		buf;
	proxy_msg *	msg;
	bitset *	set; 

	set = str_to_bitset(args[0]);
	if (set == NULL) {
		DbgSetError(DBGERR_PROCSET, NULL);
		return DBGRES_ERR;
	}
	
	msg = new_proxy_msg(DBG_EVALUATEEXPRESSION_CMD, tid);
	proxy_msg_add_args_nocopy(msg, --nargs, &args[1]);
	proxy_serialize_msg(msg, &buf);

	res = ClntSvrSendCommand(set, DBG_EV_WAITALL, buf, NULL);
	
	bitset_free(set);
	free_proxy_msg(msg);
	free(buf);
	
	return res;
}

int 
DbgClntGetType(int tid, int nargs, char **args)
{
	int			res;
	char *		buf;
	proxy_msg *	msg;
	bitset *	set;

	set = str_to_bitset(args[0]);
	if (set == NULL) {
		DbgSetError(DBGERR_PROCSET, NULL);
		return DBGRES_ERR;
	}
	
	msg = new_proxy_msg(DBG_GETTYPE_CMD, tid);
	proxy_msg_add_args_nocopy(msg, --nargs, &args[1]);
	proxy_serialize_msg(msg, &buf);

	res = ClntSvrSendCommand(set, DBG_EV_WAITALL, buf, NULL);
	
	bitset_free(set);
	free_proxy_msg(msg);
	free(buf);
	
	return res;
}

int 
DbgClntListLocalVariables(int tid, int nargs, char **args)
{
	int			res;
	char *		buf;
	proxy_msg *	msg;
	bitset *	set; 

	set = str_to_bitset(args[0]);
	if (set == NULL) {
		DbgSetError(DBGERR_PROCSET, NULL);
		return DBGRES_ERR;
	}
	
	msg = new_proxy_msg(DBG_LISTLOCALVARIABLES_CMD, tid);
	proxy_msg_add_args_nocopy(msg, --nargs, &args[1]);
	proxy_serialize_msg(msg, &buf);

	res = ClntSvrSendCommand(set, DBG_EV_WAITALL, buf, NULL);
	
	bitset_free(set);
	free_proxy_msg(msg);
	free(buf);
	
	return res;
}

int 
DbgClntListArguments(int tid, int nargs, char **args)
{
	int			res;
	char *		buf;
	proxy_msg *	msg;
	bitset *	set; 

	set = str_to_bitset(args[0]);
	if (set == NULL) {
		DbgSetError(DBGERR_PROCSET, NULL);
		return DBGRES_ERR;
	}
	
	msg = new_proxy_msg(DBG_LISTARGUMENTS_CMD, tid);
	proxy_msg_add_args_nocopy(msg, --nargs, &args[1]);
	proxy_serialize_msg(msg, &buf);

	res = ClntSvrSendCommand(set, DBG_EV_WAITALL, buf, NULL);
	
	bitset_free(set);
	free_proxy_msg(msg);
	free(buf);
	
	return res;
}

int 
DbgClntListGlobalVariables(int tid, int nargs, char **args)
{
	int			res;
	char *		buf;
	proxy_msg *	msg;
	bitset *	set; 

	set = str_to_bitset(args[0]);
	if (set == NULL) {
		DbgSetError(DBGERR_PROCSET, NULL);
		return DBGRES_ERR;
	}
	
	msg = new_proxy_msg(DBG_LISTGLOBALVARIABLES_CMD, tid);
	proxy_msg_add_args_nocopy(msg, --nargs, &args[1]);
	proxy_serialize_msg(msg, &buf);

	res = ClntSvrSendCommand(set, DBG_EV_WAITALL, buf, NULL);
	
	bitset_free(set);
	free_proxy_msg(msg);
	free(buf);
	
	return res;
}

int
DbgClntListInfoThreads(int tid, int nargs, char **args) 
{
	int			res;
	char *		buf;
	proxy_msg *	msg;
	bitset *	set; 

	set = str_to_bitset(args[0]);
	if (set == NULL) {
		DbgSetError(DBGERR_PROCSET, NULL);
		return DBGRES_ERR;
	}
	
	msg = new_proxy_msg(DBG_LISTINFOTHREADS_CMD, tid);
	proxy_msg_add_args_nocopy(msg, --nargs, &args[1]);
	proxy_serialize_msg(msg, &buf);

	res = ClntSvrSendCommand(set, DBG_EV_WAITALL, buf, NULL);

	bitset_free(set);
	free_proxy_msg(msg);
	free(buf);
	
	return res;
}

int
DbgClntSetThreadSelect(int tid, int nargs, char **args)
{
	int			res;
	char *		buf;
	proxy_msg *	msg;
	bitset *	set; 
	
	set = str_to_bitset(args[0]);
	if (set == NULL) {
		DbgSetError(DBGERR_PROCSET, NULL);
		return DBGRES_ERR;
	}
	
	msg = new_proxy_msg(DBG_SETTHREADSELECT_CMD, tid);
	proxy_msg_add_args_nocopy(msg, --nargs, &args[1]);
	proxy_serialize_msg(msg, &buf);

	res = ClntSvrSendCommand(set, DBG_EV_WAITALL, buf, NULL);
	
	bitset_free(set);
	free_proxy_msg(msg);
	free(buf);
	
	return res;
}

int
DbgClntStackInfoDepth(int tid, int nargs, char **args)
{
	int			res;
	char *		buf;
	proxy_msg *	msg;
	bitset *	set; 

	set = str_to_bitset(args[0]);
	if (set == NULL) {
		DbgSetError(DBGERR_PROCSET, NULL);
		return DBGRES_ERR;
	}
	
	msg = new_proxy_msg(DBG_STACKINFODEPTH_CMD, tid);
	proxy_msg_add_args_nocopy(msg, --nargs, &args[1]);
	proxy_serialize_msg(msg, &buf);

	res = ClntSvrSendCommand(set, DBG_EV_WAITALL, buf, NULL);

	bitset_free(set);
	free_proxy_msg(msg);
	free(buf);
	
	return res;
}

int 
DbgClntDataReadMemory(int tid, int nargs, char **args) 
{
	int			res;
	char *		buf;
	proxy_msg *	msg;
	bitset *	set; 

	set = str_to_bitset(args[0]);
	if (set == NULL) {
		DbgSetError(DBGERR_PROCSET, NULL);
		return DBGRES_ERR;
	}

	msg = new_proxy_msg(DBG_DATAREADMEMORY_CMD, tid);
	proxy_msg_add_args_nocopy(msg, --nargs, &args[1]);
	proxy_serialize_msg(msg, &buf);

	res = ClntSvrSendCommand(set, DBG_EV_WAITALL, buf, NULL);

	bitset_free(set);
	free_proxy_msg(msg);
	free(buf);
	
	return res;
}

int 
DbgClntDataWriteMemory(int tid, int nargs, char **args) 
{
	int			res;
	char *		buf;
	proxy_msg *	msg;
	bitset *	set; 

	set = str_to_bitset(args[0]);
	if (set == NULL) {
		DbgSetError(DBGERR_PROCSET, NULL);
		return DBGRES_ERR;
	}
	
	msg = new_proxy_msg(DBG_DATAWRITEMEMORY_CMD, tid);
	proxy_msg_add_args_nocopy(msg, --nargs, &args[1]);
	proxy_serialize_msg(msg, &buf);

	res = ClntSvrSendCommand(set, DBG_EV_WAITALL, buf, NULL);

	bitset_free(set);
	free_proxy_msg(msg);
	free(buf);
	
	return res;
}

int 
DbgClntListSignals(int tid, int nargs, char **args)
{
	int			res;
	char *		buf;
	proxy_msg *	msg;
	bitset *	set; 

	set = str_to_bitset(args[0]);
	if (set == NULL) {
		DbgSetError(DBGERR_PROCSET, NULL);
		return DBGRES_ERR;
	}
	
	msg = new_proxy_msg(DBG_LISTSIGNALS_CMD, tid);
	proxy_msg_add_args_nocopy(msg, --nargs, &args[1]);
	proxy_serialize_msg(msg, &buf);

	res = ClntSvrSendCommand(set, DBG_EV_WAITALL, buf, NULL);

	bitset_free(set);
	free_proxy_msg(msg);
	free(buf);
	
	return res;
}

int 
DbgClntSignalInfo(int tid, int nargs, char **args)
{
	int			res;
	char *		buf;
	proxy_msg *	msg;
	bitset *	set; 

	set = str_to_bitset(args[0]);
	if (set == NULL) {
		DbgSetError(DBGERR_PROCSET, NULL);
		return DBGRES_ERR;
	}
	
	msg = new_proxy_msg(DBG_SIGNALINFO_CMD, tid);
	proxy_msg_add_args_nocopy(msg, --nargs, &args[1]);
	proxy_serialize_msg(msg, &buf);

	res = ClntSvrSendCommand(set, DBG_EV_WAITALL, buf, NULL);

	bitset_free(set);
	free_proxy_msg(msg);
	free(buf);
	
	return res;
}

int 
DbgClntCLIHandle(int tid, int nargs, char **args)
{
	int			res;
	char *		buf;
	proxy_msg *	msg;
	bitset *	set; 

	set = str_to_bitset(args[0]);
	if (set == NULL) {
		DbgSetError(DBGERR_PROCSET, NULL);
		return DBGRES_ERR;
	}
	
	msg = new_proxy_msg(DBG_CLIHANDLE_CMD, tid);
	proxy_msg_add_args_nocopy(msg, --nargs, &args[1]);
	proxy_serialize_msg(msg, &buf);

	res = ClntSvrSendCommand(set, DBG_EV_WAITALL, buf, NULL);

	bitset_free(set);
	free_proxy_msg(msg);
	free(buf);
	
	return res;
}

int 
DbgClntQuit(int tid, int nargs, char **args)
{
	int			res;
	char *		buf;
	proxy_msg *	msg;
	
	dbg_state = SHUTDOWN_STARTED;
	
	msg = new_proxy_msg(DBG_QUIT_CMD, tid);
	proxy_msg_add_args_nocopy(msg, nargs, args);
	proxy_serialize_msg(msg, &buf);

	res = ClntSvrSendCommand(dbg_procs, DBG_EV_WAITALL, buf, NULL);

	free_proxy_msg(msg);
	free(buf);
	
	return res;
}

/*
 * Event handling
 */
int
DbgClntProgress(void)
{
	/*************************************
	 * Check for any server events
	 */
	 
	ClntSvrProgressCmds();
	
	/**************************************
	 * Check for proxy events
	 */
	 
	return proxy_svr_progress(dbg_proxy) == PROXY_RES_OK ? DBGRES_OK : DBGRES_ERR;
}

#if 0
void
DbgClntRegisterEventHandler(void (*event_callback)(dbg_event *, void *), void *data)
{
	RegisterEventHandler(event_callback, data);
}

/**
 * Unregister file descriptor handler
 */
void
DbgClntUnregisterEventHandler(void (*event_callback)(dbg_event *, void *))
{
	UnregisterEventHandler(event_callback);
}
#endif

/**
 * Register a handler for file descriptor events.
 * 
 * TODO: Should the handler return a value?
 */
void
DbgClntRegisterReadFileHandler(int fd, int (*file_handler)(int, void *), void *data)
{
	RegisterFileHandler(fd, READ_FILE_HANDLER, file_handler, data);
}

void
DbgClntRegisterWriteFileHandler(int fd, int (*file_handler)(int, void *), void *data)
{
	RegisterFileHandler(fd, WRITE_FILE_HANDLER, file_handler, data);
}

void
DbgClntRegisterExceptFileHandler(int fd, int (*file_handler)(int, void *), void *data)
{
	RegisterFileHandler(fd, EXCEPT_FILE_HANDLER, file_handler, data);
}

/**
 * Unregister file descriptor handler
 */
void
DbgClntUnregisterFileHandler(int fd)
{
	UnregisterFileHandler(fd);
}

int 
DbgClntDataEvaluateExpression(int tid, int nargs, char **args)
{
	int			res;
	char *		buf;
	proxy_msg *	msg;
	bitset *	set; 

	set = str_to_bitset(args[0]);
	if (set == NULL) {
		DbgSetError(DBGERR_PROCSET, NULL);
		return DBGRES_ERR;
	}
	
	msg = new_proxy_msg(DBG_EVALUATEEXPRESSION_CMD, tid);
	proxy_msg_add_args_nocopy(msg, --nargs, &args[1]);
	proxy_serialize_msg(msg, &buf);

	res = ClntSvrSendCommand(set, DBG_EV_WAITALL, buf, NULL);

	bitset_free(set);
	free_proxy_msg(msg);
	free(buf);
	
	return res;
}
int 
DbgClntGetPartialAIF(int tid, int nargs, char **args)
{
	int			res;
	char *		buf;
	proxy_msg *	msg;
	bitset *	set; 

	set = str_to_bitset(args[0]);
	if (set == NULL) {
		DbgSetError(DBGERR_PROCSET, NULL);
		return DBGRES_ERR;
	}
	
	msg = new_proxy_msg(DBG_GETPARTIALAIF_CMD, tid);
	proxy_msg_add_args_nocopy(msg, --nargs, &args[1]);
	proxy_serialize_msg(msg, &buf);

	res = ClntSvrSendCommand(set, DBG_EV_WAITALL, buf, NULL);

	bitset_free(set);
	free_proxy_msg(msg);
	free(buf);

	return res;
}
int 
DbgClntVariableDelete(int tid, int nargs, char **args)
{
	int			res;
	char *		buf;
	proxy_msg *	msg;
	bitset *	set; 

	set = str_to_bitset(args[0]);
	if (set == NULL) {
		DbgSetError(DBGERR_PROCSET, NULL);
		return DBGRES_ERR;
	}
	
	msg = new_proxy_msg(DBG_VARIABLEDELETE_CMD, tid);
	proxy_msg_add_args_nocopy(msg, --nargs, &args[1]);
	proxy_serialize_msg(msg, &buf);

	res = ClntSvrSendCommand(set, DBG_EV_WAITALL, buf, NULL);

	bitset_free(set);
	free_proxy_msg(msg);
	free(buf);

	return res;
}
