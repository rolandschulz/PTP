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
#include "proxy_msg.h"
#include "bitset.h"
#include "handler.h"
#include "list.h"
#include "sdm.h"

#define RUNNING				0
#define SHUTDOWN_STARTED	1
#define SHUTDOWN_COMPLETED	2

static int				dbg_state;
static sdm_idset		dbg_procs;
static sdm_idset		tmp_idset;
static bitset *			tmp_bitset;
static proxy_svr *		dbg_proxy;

/*
 * The CLIENT_TIMEOUT determines how often the debugger polls for
 * communication with the front-end. The SERVER_TIMEOUT determines
 * how often the debug servers are polled.
 *
 * Too often and it uses too much CPU, too few and things will slow down...
 *
 * Values are in microseconds.
 */
#define CLIENT_TIMEOUT	10000
#define SERVER_TIMEOUT	10000

sdm_idset
bitset_to_idset(bitset *b)
{
	int bit;

	sdm_set_clear(tmp_idset);

	for (bit = 0; bit < bitset_size(b); bit++) {
		if (bitset_test(b, bit)) {
			sdm_set_add_element(tmp_idset, bit);
		}
	}

	return tmp_idset;
}

bitset *
idset_to_bitset(sdm_idset ids)
{
	sdm_id		id;

	bitset_clear(tmp_bitset);

	for (id = sdm_set_first(ids); !sdm_set_done(ids); id = sdm_set_next(ids)) {
		bitset_set(tmp_bitset, id);
	}

	return tmp_bitset;
}

/**
 * A send command is completed. Process the result and
 * call all registered event handlers.
 */
static int
dbg_clnt_cmd_completed(sdm_message msg)
{
	int			len;
	char *		buf;
	proxy_msg *	m;

	sdm_message_get_payload(msg, &buf, &len);

 	if (proxy_deserialize_msg(buf, len, &m) < 0)
 		fprintf(stderr, "bad conversion to proxy event");
 	else {
 		/*
 		 * Insert actual bitset as first arg. The debug events sent internally
 		 * don't include the bitset, it is only added prior to sending back
 		 * to the client.
 		 */
 		DEBUG_PRINTF(DEBUG_LEVEL_MESSAGES, "[%d] dbg_clnt_cmd_completed src=%s\n", sdm_route_get_id(),
 				_set_to_str(sdm_message_get_source(msg)));
		proxy_msg_insert_bitset(m, idset_to_bitset(sdm_message_get_source(msg)), 0);
 		proxy_svr_queue_msg(dbg_proxy, m);
	}

 	sdm_message_free(msg);

 	return 0;
}

static int
send_command(sdm_idset dest, int timeout, char *cmd, int len, void *cbdata)
{
	sdm_message 	msg;

	DEBUG_PRINTF(DEBUG_LEVEL_MESSAGES, "[%d] send_command dest=%s cmd=%s\n", sdm_route_get_id(), _set_to_str(dest), cmd);

	msg = sdm_message_new(cmd, len);
	sdm_aggregate_set_value(sdm_message_get_aggregate(msg), SDM_AGGREGATE_TIMEOUT, timeout);
	sdm_set_union(sdm_message_get_destination(msg), dest);

	sdm_aggregate_message(msg, SDM_AGGREGATE_DOWNSTREAM);

	sdm_message_set_send_callback(msg, sdm_message_free);
	sdm_message_send(msg);

	return 0;
}

int
DbgClntInit(int num_svrs, int my_id, char *name, proxy_svr_helper_funcs *funcs, proxy_commands *cmds)
{
	struct timeval	tv = { 0, CLIENT_TIMEOUT };

	DEBUG_PRINTF(DEBUG_LEVEL_MESSAGES, "[%d] DbgClntInit num_svrs=%d\n", sdm_route_get_id(), num_svrs);

	tmp_idset = sdm_set_new();
	tmp_bitset = bitset_new(num_svrs);

	/*
	 * Register callback
	 */
	sdm_aggregate_set_completion_callback(dbg_clnt_cmd_completed);

	/*
	 * Create a bitset containing all processes
	 */
	dbg_procs = sdm_set_new();
	sdm_set_add_all(dbg_procs, num_svrs-1);

	/*
	 * Reset state
	 */
	dbg_state = RUNNING;

	/*
	 * Initialize proxy
	 */
	if (proxy_svr_init(name, &tv, funcs, cmds, &dbg_proxy) != PTP_PROXY_RES_OK) {
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

	if (res == PTP_PROXY_RES_ERR) {
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
	int			len;
	char *		buf;
	proxy_msg *	msg = new_proxy_msg(DBG_STARTSESSION_CMD, tid);

	proxy_msg_add_int(msg, SERVER_TIMEOUT);
	proxy_msg_add_args(msg, nargs, args);
	proxy_serialize_msg(msg, &buf, &len);

	res = send_command(dbg_procs, DBG_EV_WAITALL, buf, len, NULL);

	free_proxy_msg(msg);

	return res;
}

/*
 * Breakpoint operations
 */
int
DbgClntSetLineBreakpoint(int tid, int nargs, char **args)
{
	int			res;
	int			len;
	bitset *	set;
	char *		buf;
	proxy_msg *	msg;

	set = str_to_bitset(args[0], NULL);
	if (set == NULL) {
		DbgSetError(DBGERR_PROCSET, NULL);
		return DBGRES_ERR;
	}

	msg = new_proxy_msg(DBG_SETLINEBREAKPOINT_CMD, tid);
	proxy_msg_add_args(msg, --nargs, &args[1]);
	proxy_serialize_msg(msg, &buf, &len);

	res = send_command(bitset_to_idset(set), DBG_EV_WAITALL, buf, len, NULL);

	bitset_free(set);
	free_proxy_msg(msg);

	return res;
}

int
DbgClntSetFuncBreakpoint(int tid, int nargs, char **args)
{
	int			res;
	int			len;
	char *		buf;
	proxy_msg *	msg;
	bitset *	set;

	set = str_to_bitset(args[0], NULL);
	if (set == NULL) {
		DbgSetError(DBGERR_PROCSET, NULL);
		return DBGRES_ERR;
	}

	msg = new_proxy_msg(DBG_SETFUNCBREAKPOINT_CMD, tid);
	proxy_msg_add_args(msg, --nargs, &args[1]);
	proxy_serialize_msg(msg, &buf, &len);

	res = send_command(bitset_to_idset(set), DBG_EV_WAITALL, buf, len, NULL);

	bitset_free(set);
	free_proxy_msg(msg);

	return res;
}

int
DbgClntDeleteBreakpoint(int tid, int nargs, char **args)
{
	int			res;
	int			len;
	char *		buf;
	proxy_msg *	msg;
	bitset *	set;

	set = str_to_bitset(args[0], NULL);
	if (set == NULL) {
		DbgSetError(DBGERR_PROCSET, NULL);
		return DBGRES_ERR;
	}

	msg = new_proxy_msg(DBG_DELETEBREAKPOINT_CMD, tid);
	proxy_msg_add_args(msg, --nargs, &args[1]);
	proxy_serialize_msg(msg, &buf, &len);

	res = send_command(bitset_to_idset(set), DBG_EV_WAITALL, buf, len, NULL);

	bitset_free(set);
	free_proxy_msg(msg);

	return res;
}

int
DbgClntEnableBreakpoint(int tid, int nargs, char **args)
{
	int			res;
	int			len;
	char *		buf;
	proxy_msg *	msg;
	bitset *	set;

	set = str_to_bitset(args[0], NULL);
	if (set == NULL) {
		DbgSetError(DBGERR_PROCSET, NULL);
		return DBGRES_ERR;
	}

	msg = new_proxy_msg(DBG_ENABLEBREAKPOINT_CMD, tid);
	proxy_msg_add_args(msg, --nargs, &args[1]);
	proxy_serialize_msg(msg, &buf, &len);

	res = send_command(bitset_to_idset(set), DBG_EV_WAITALL, buf, len, NULL);

	bitset_free(set);
	free_proxy_msg(msg);

	return res;
}

int
DbgClntDisableBreakpoint(int tid, int nargs, char **args)
{
	int			res;
	int			len;
	char *		buf;
	proxy_msg *	msg;
	bitset *	set;

	set = str_to_bitset(args[0], NULL);
	if (set == NULL) {
		DbgSetError(DBGERR_PROCSET, NULL);
		return DBGRES_ERR;
	}

	msg = new_proxy_msg(DBG_DISABLEBREAKPOINT_CMD, tid);
	proxy_msg_add_args(msg, --nargs, &args[1]);
	proxy_serialize_msg(msg, &buf, &len);

	res = send_command(bitset_to_idset(set), DBG_EV_WAITALL, buf, len, NULL);

	bitset_free(set);
	free_proxy_msg(msg);

	return res;
}

int
DbgClntConditionBreakpoint(int tid, int nargs, char **args)
{
	int			res;
	int			len;
	char *		buf;
	proxy_msg *	msg;
	bitset *	set;

	set = str_to_bitset(args[0], NULL);
	if (set == NULL) {
		DbgSetError(DBGERR_PROCSET, NULL);
		return DBGRES_ERR;
	}

	msg = new_proxy_msg(DBG_CONDITIONBREAKPOINT_CMD, tid);
	proxy_msg_add_args(msg, --nargs, &args[1]);
	proxy_serialize_msg(msg, &buf, &len);

	res = send_command(bitset_to_idset(set), DBG_EV_WAITALL, buf, len, NULL);

	bitset_free(set);
	free_proxy_msg(msg);

	return res;
}

int
DbgClntBreakpointAfter(int tid, int nargs, char **args)
{
	int			res;
	int			len;
	char *		buf;
	proxy_msg *	msg;
	bitset *	set;

	set = str_to_bitset(args[0], NULL);
	if (set == NULL) {
		DbgSetError(DBGERR_PROCSET, NULL);
		return DBGRES_ERR;
	}

	msg = new_proxy_msg(DBG_BREAKPOINTAFTER_CMD, tid);
	proxy_msg_add_args(msg, --nargs, &args[1]);
	proxy_serialize_msg(msg, &buf, &len);

	res = send_command(bitset_to_idset(set), DBG_EV_WAITALL, buf, len, NULL);

	bitset_free(set);
	free_proxy_msg(msg);

	return res;
}

int
DbgClntSetWatchpoint(int tid, int nargs, char **args)
{
	int			res;
	int			len;
	char *		buf;
	proxy_msg *	msg;
	bitset *	set;

	set = str_to_bitset(args[0], NULL);
	if (set == NULL) {
		DbgSetError(DBGERR_PROCSET, NULL);
		return DBGRES_ERR;
	}

	msg = new_proxy_msg(DBG_SETWATCHPOINT_CMD, tid);
	proxy_msg_add_args(msg, --nargs, &args[1]);
	proxy_serialize_msg(msg, &buf, &len);

	res = send_command(bitset_to_idset(set), DBG_EV_WAITALL, buf, len, NULL);

	bitset_free(set);
	free_proxy_msg(msg);

	return res;
}

/*
 * Process control operations
 */
int
DbgClntGo(int tid, int nargs, char **args)
{
	int			res;
	int			len;
	char *		buf;
	proxy_msg *	msg;
	bitset *	set;

	set = str_to_bitset(args[0], NULL);
	if (set == NULL) {
		DbgSetError(DBGERR_PROCSET, NULL);
		return DBGRES_ERR;
	}

	msg = new_proxy_msg(DBG_GO_CMD, tid);
	proxy_msg_add_args(msg, --nargs, &args[1]);
	proxy_serialize_msg(msg, &buf, &len);

	res = send_command(bitset_to_idset(set), DBG_EV_WAITSOME, buf, len, NULL);

	bitset_free(set);
	free_proxy_msg(msg);

	return res;
}

int
DbgClntStep(int tid, int nargs, char **args)
{
	int			res;
	int			len;
	char *		buf;
	proxy_msg *	msg;
	bitset *	set;

	set = str_to_bitset(args[0], NULL);
	if (set == NULL) {
		DbgSetError(DBGERR_PROCSET, NULL);
		return DBGRES_ERR;
	}

	msg = new_proxy_msg(DBG_STEP_CMD, tid);
	proxy_msg_add_args(msg, --nargs, &args[1]);
	proxy_serialize_msg(msg, &buf, &len);

	res = send_command(bitset_to_idset(set), DBG_EV_WAITSOME, buf, len, NULL);

	bitset_free(set);
	free_proxy_msg(msg);

	return res;
}

int
DbgClntTerminate(int tid, int nargs, char **args)
{
	int			res;
	int			len;
	char *		buf;
	proxy_msg *	msg;
	bitset *	set;

	set = str_to_bitset(args[0], NULL);
	if (set == NULL) {
		DbgSetError(DBGERR_PROCSET, NULL);
		return DBGRES_ERR;
	}

	msg = new_proxy_msg(DBG_TERMINATE_CMD, tid);
	proxy_msg_add_args(msg, --nargs, &args[1]);
	proxy_serialize_msg(msg, &buf, &len);

	res = send_command(bitset_to_idset(set), DBG_EV_WAITALL, buf, len, NULL);

	bitset_free(set);
	free_proxy_msg(msg);

	return res;
}

int
DbgClntSuspend(int tid, int nargs, char **args)
{
	int			res;
	int			len;
	char *		buf;
	proxy_msg *	msg;
	bitset *	set;

	set = str_to_bitset(args[0], NULL);
	if (set == NULL) {
		DbgSetError(DBGERR_PROCSET, NULL);
		return DBGRES_ERR;
	}

	msg = new_proxy_msg(DBG_SUSPEND_CMD, tid);
	proxy_msg_add_args(msg, --nargs, &args[1]);
	proxy_serialize_msg(msg, &buf, &len);

	res = send_command(bitset_to_idset(set), DBG_EV_WAITALL, buf, len, NULL);

	bitset_free(set);
	free_proxy_msg(msg);

	return res;
}

/*
 * Stack frame operations
 */
int
DbgClntListStackframes(int tid, int nargs, char **args)
{
	int			res;
	int			len;
	char *		buf;
	proxy_msg *	msg;
	bitset *	set;

	set = str_to_bitset(args[0], NULL);
	if (set == NULL) {
		DbgSetError(DBGERR_PROCSET, NULL);
		return DBGRES_ERR;
	}

	msg = new_proxy_msg(DBG_LISTSTACKFRAMES_CMD, tid);
	proxy_msg_add_args(msg, --nargs, &args[1]);
	proxy_serialize_msg(msg, &buf, &len);

	res = send_command(bitset_to_idset(set), DBG_EV_WAITALL, buf, len, NULL);

	bitset_free(set);
	free_proxy_msg(msg);

	return res;
}

int
DbgClntSetCurrentStackframe(int tid, int nargs, char **args)
{
	int			res;
	int			len;
	char *		buf;
	proxy_msg *	msg;
	bitset *	set;

	set = str_to_bitset(args[0], NULL);
	if (set == NULL) {
		DbgSetError(DBGERR_PROCSET, NULL);
		return DBGRES_ERR;
	}

	msg = new_proxy_msg(DBG_SETCURRENTSTACKFRAME_CMD, tid);
	proxy_msg_add_args(msg, --nargs, &args[1]);
	proxy_serialize_msg(msg, &buf, &len);

	res = send_command(bitset_to_idset(set), DBG_EV_WAITALL, buf, len, NULL);

	bitset_free(set);
	free_proxy_msg(msg);

	return res;
}

/*
 * Expression/variable operations
 */
int
DbgClntEvaluateExpression(int tid, int nargs, char **args)
{
	int			res;
	int			len;
	char *		buf;
	proxy_msg *	msg;
	bitset *	set;

	set = str_to_bitset(args[0], NULL);
	if (set == NULL) {
		DbgSetError(DBGERR_PROCSET, NULL);
		return DBGRES_ERR;
	}

	msg = new_proxy_msg(DBG_EVALUATEEXPRESSION_CMD, tid);
	proxy_msg_add_args(msg, --nargs, &args[1]);
	proxy_serialize_msg(msg, &buf, &len);

	res = send_command(bitset_to_idset(set), DBG_EV_WAITALL, buf, len, NULL);

	bitset_free(set);
	free_proxy_msg(msg);

	return res;
}

int
DbgClntGetType(int tid, int nargs, char **args)
{
	int			res;
	int			len;
	char *		buf;
	proxy_msg *	msg;
	bitset *	set;

	set = str_to_bitset(args[0], NULL);
	if (set == NULL) {
		DbgSetError(DBGERR_PROCSET, NULL);
		return DBGRES_ERR;
	}

	msg = new_proxy_msg(DBG_GETTYPE_CMD, tid);
	proxy_msg_add_args(msg, --nargs, &args[1]);
	proxy_serialize_msg(msg, &buf, &len);

	res = send_command(bitset_to_idset(set), DBG_EV_WAITALL, buf, len, NULL);

	bitset_free(set);
	free_proxy_msg(msg);

	return res;
}

int
DbgClntListLocalVariables(int tid, int nargs, char **args)
{
	int			res;
	int			len;
	char *		buf;
	proxy_msg *	msg;
	bitset *	set;

	set = str_to_bitset(args[0], NULL);
	if (set == NULL) {
		DbgSetError(DBGERR_PROCSET, NULL);
		return DBGRES_ERR;
	}

	msg = new_proxy_msg(DBG_LISTLOCALVARIABLES_CMD, tid);
	proxy_msg_add_args(msg, --nargs, &args[1]);
	proxy_serialize_msg(msg, &buf, &len);

	res = send_command(bitset_to_idset(set), DBG_EV_WAITALL, buf, len, NULL);

	bitset_free(set);
	free_proxy_msg(msg);

	return res;
}

int
DbgClntListArguments(int tid, int nargs, char **args)
{
	int			res;
	int			len;
	char *		buf;
	proxy_msg *	msg;
	bitset *	set;

	set = str_to_bitset(args[0], NULL);
	if (set == NULL) {
		DbgSetError(DBGERR_PROCSET, NULL);
		return DBGRES_ERR;
	}

	msg = new_proxy_msg(DBG_LISTARGUMENTS_CMD, tid);
	proxy_msg_add_args(msg, --nargs, &args[1]);
	proxy_serialize_msg(msg, &buf, &len);

	res = send_command(bitset_to_idset(set), DBG_EV_WAITALL, buf, len, NULL);

	bitset_free(set);
	free_proxy_msg(msg);

	return res;
}

int
DbgClntListGlobalVariables(int tid, int nargs, char **args)
{
	int			res;
	int			len;
	char *		buf;
	proxy_msg *	msg;
	bitset *	set;

	set = str_to_bitset(args[0], NULL);
	if (set == NULL) {
		DbgSetError(DBGERR_PROCSET, NULL);
		return DBGRES_ERR;
	}

	msg = new_proxy_msg(DBG_LISTGLOBALVARIABLES_CMD, tid);
	proxy_msg_add_args(msg, --nargs, &args[1]);
	proxy_serialize_msg(msg, &buf, &len);

	res = send_command(bitset_to_idset(set), DBG_EV_WAITALL, buf, len, NULL);

	bitset_free(set);
	free_proxy_msg(msg);

	return res;
}

int
DbgClntListInfoThreads(int tid, int nargs, char **args)
{
	int			res;
	int			len;
	char *		buf;
	proxy_msg *	msg;
	bitset *	set;

	set = str_to_bitset(args[0], NULL);
	if (set == NULL) {
		DbgSetError(DBGERR_PROCSET, NULL);
		return DBGRES_ERR;
	}

	msg = new_proxy_msg(DBG_LISTINFOTHREADS_CMD, tid);
	proxy_msg_add_args(msg, --nargs, &args[1]);
	proxy_serialize_msg(msg, &buf, &len);

	res = send_command(bitset_to_idset(set), DBG_EV_WAITALL, buf, len, NULL);

	bitset_free(set);
	free_proxy_msg(msg);

	return res;
}

int
DbgClntSetThreadSelect(int tid, int nargs, char **args)
{
	int			res;
	int			len;
	char *		buf;
	proxy_msg *	msg;
	bitset *	set;

	set = str_to_bitset(args[0], NULL);
	if (set == NULL) {
		DbgSetError(DBGERR_PROCSET, NULL);
		return DBGRES_ERR;
	}

	msg = new_proxy_msg(DBG_SETTHREADSELECT_CMD, tid);
	proxy_msg_add_args(msg, --nargs, &args[1]);
	proxy_serialize_msg(msg, &buf, &len);

	res = send_command(bitset_to_idset(set), DBG_EV_WAITALL, buf, len, NULL);

	bitset_free(set);
	free_proxy_msg(msg);

	return res;
}

int
DbgClntStackInfoDepth(int tid, int nargs, char **args)
{
	int			res;
	int			len;
	char *		buf;
	proxy_msg *	msg;
	bitset *	set;

	set = str_to_bitset(args[0], NULL);
	if (set == NULL) {
		DbgSetError(DBGERR_PROCSET, NULL);
		return DBGRES_ERR;
	}

	msg = new_proxy_msg(DBG_STACKINFODEPTH_CMD, tid);
	proxy_msg_add_args(msg, --nargs, &args[1]);
	proxy_serialize_msg(msg, &buf, &len);

	res = send_command(bitset_to_idset(set), DBG_EV_WAITALL, buf, len, NULL);

	bitset_free(set);
	free_proxy_msg(msg);

	return res;
}

int
DbgClntDataReadMemory(int tid, int nargs, char **args)
{
	int			res;
	int			len;
	char *		buf;
	proxy_msg *	msg;
	bitset *	set;

	set = str_to_bitset(args[0], NULL);
	if (set == NULL) {
		DbgSetError(DBGERR_PROCSET, NULL);
		return DBGRES_ERR;
	}

	msg = new_proxy_msg(DBG_DATAREADMEMORY_CMD, tid);
	proxy_msg_add_args(msg, --nargs, &args[1]);
	proxy_serialize_msg(msg, &buf, &len);

	res = send_command(bitset_to_idset(set), DBG_EV_WAITALL, buf, len, NULL);

	bitset_free(set);
	free_proxy_msg(msg);

	return res;
}

int
DbgClntDataWriteMemory(int tid, int nargs, char **args)
{
	int			res;
	int			len;
	char *		buf;
	proxy_msg *	msg;
	bitset *	set;

	set = str_to_bitset(args[0], NULL);
	if (set == NULL) {
		DbgSetError(DBGERR_PROCSET, NULL);
		return DBGRES_ERR;
	}

	msg = new_proxy_msg(DBG_DATAWRITEMEMORY_CMD, tid);
	proxy_msg_add_args(msg, --nargs, &args[1]);
	proxy_serialize_msg(msg, &buf, &len);

	res = send_command(bitset_to_idset(set), DBG_EV_WAITALL, buf, len, NULL);

	bitset_free(set);
	free_proxy_msg(msg);

	return res;
}

int
DbgClntListSignals(int tid, int nargs, char **args)
{
	int			res;
	int			len;
	char *		buf;
	proxy_msg *	msg;
	bitset *	set;

	set = str_to_bitset(args[0], NULL);
	if (set == NULL) {
		DbgSetError(DBGERR_PROCSET, NULL);
		return DBGRES_ERR;
	}

	msg = new_proxy_msg(DBG_LISTSIGNALS_CMD, tid);
	proxy_msg_add_args(msg, --nargs, &args[1]);
	proxy_serialize_msg(msg, &buf, &len);

	res = send_command(bitset_to_idset(set), DBG_EV_WAITALL, buf, len, NULL);

	bitset_free(set);
	free_proxy_msg(msg);

	return res;
}

int
DbgClntSignalInfo(int tid, int nargs, char **args)
{
	int			res;
	int			len;
	char *		buf;
	proxy_msg *	msg;
	bitset *	set;

	set = str_to_bitset(args[0], NULL);
	if (set == NULL) {
		DbgSetError(DBGERR_PROCSET, NULL);
		return DBGRES_ERR;
	}

	msg = new_proxy_msg(DBG_SIGNALINFO_CMD, tid);
	proxy_msg_add_args(msg, --nargs, &args[1]);
	proxy_serialize_msg(msg, &buf, &len);

	res = send_command(bitset_to_idset(set), DBG_EV_WAITALL, buf, len, NULL);

	bitset_free(set);
	free_proxy_msg(msg);

	return res;
}

int
DbgClntCLIHandle(int tid, int nargs, char **args)
{
	int			res;
	int			len;
	char *		buf;
	proxy_msg *	msg;
	bitset *	set;

	set = str_to_bitset(args[0], NULL);
	if (set == NULL) {
		DbgSetError(DBGERR_PROCSET, NULL);
		return DBGRES_ERR;
	}

	msg = new_proxy_msg(DBG_CLIHANDLE_CMD, tid);
	proxy_msg_add_args(msg, --nargs, &args[1]);
	proxy_serialize_msg(msg, &buf, &len);

	res = send_command(bitset_to_idset(set), DBG_EV_WAITALL, buf, len, NULL);

	bitset_free(set);
	free_proxy_msg(msg);

	return res;
}

int
DbgClntQuit(int tid, int nargs, char **args)
{
	int			res;
	int			len;
	char *		buf;
	proxy_msg *	msg;

	dbg_state = SHUTDOWN_STARTED;

	msg = new_proxy_msg(DBG_QUIT_CMD, tid);
	proxy_msg_add_args(msg, nargs, args);
	proxy_serialize_msg(msg, &buf, &len);

	res = send_command(dbg_procs, DBG_EV_WAITALL, buf, len, NULL);

	free_proxy_msg(msg);

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

	sdm_progress();

	/**************************************
	 * Check for proxy events
	 */

	return proxy_svr_progress(dbg_proxy) == PTP_PROXY_RES_OK ? DBGRES_OK : DBGRES_ERR;
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
	int			len;
	char *		buf;
	proxy_msg *	msg;
	bitset *	set;

	set = str_to_bitset(args[0], NULL);
	if (set == NULL) {
		DbgSetError(DBGERR_PROCSET, NULL);
		return DBGRES_ERR;
	}

	msg = new_proxy_msg(DBG_EVALUATEEXPRESSION_CMD, tid);
	proxy_msg_add_args(msg, --nargs, &args[1]);
	proxy_serialize_msg(msg, &buf, &len);

	res = send_command(bitset_to_idset(set), DBG_EV_WAITALL, buf, len, NULL);

	bitset_free(set);
	free_proxy_msg(msg);

	return res;
}
int
DbgClntEvaluatePartialExpression(int tid, int nargs, char **args)
{
	int			res;
	int			len;
	char *		buf;
	proxy_msg *	msg;
	bitset *	set;

	set = str_to_bitset(args[0], NULL);
	if (set == NULL) {
		DbgSetError(DBGERR_PROCSET, NULL);
		return DBGRES_ERR;
	}

	msg = new_proxy_msg(DBG_EVALUATEPARTIALEXPRESSION_CMD, tid);
	proxy_msg_add_args(msg, --nargs, &args[1]);
	proxy_serialize_msg(msg, &buf, &len);

	res = send_command(bitset_to_idset(set), DBG_EV_WAITALL, buf, len, NULL);

	bitset_free(set);
	free_proxy_msg(msg);

	return res;
}
int
DbgClntDeletePartialExpression(int tid, int nargs, char **args)
{
	int			res;
	int			len;
	char *		buf;
	proxy_msg *	msg;
	bitset *	set;

	set = str_to_bitset(args[0], NULL);
	if (set == NULL) {
		DbgSetError(DBGERR_PROCSET, NULL);
		return DBGRES_ERR;
	}

	msg = new_proxy_msg(DBG_DELETEPARTIALEXPRESSION_CMD, tid);
	proxy_msg_add_args(msg, --nargs, &args[1]);
	proxy_serialize_msg(msg, &buf, &len);

	res = send_command(bitset_to_idset(set), DBG_EV_WAITALL, buf, len, NULL);

	bitset_free(set);
	free_proxy_msg(msg);

	return res;
}
