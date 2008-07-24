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

#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <errno.h>
#include <stdio.h>

#include "compat.h"
#include "args.h"
#include "dbg.h"
#include "dbg_event.h"
#include "dbg_proxy.h"
#include "backend.h"

#define SVR_RUNNING				0
#define SVR_SHUTDOWN_STARTED	1
#define SVR_SHUTDOWN_COMPLETED	2

typedef int (*svr_cmd)(dbg_backend *, int, char **);

static int svr_start_session(dbg_backend *, int, char **);
static int svr_setlinebreakpoint(dbg_backend *, int, char **);
static int svr_setfuncbreakpoint(dbg_backend *, int, char **);
static int svr_deletebreakpoint(dbg_backend *, int, char **);
static int svr_enablebreakpoint(dbg_backend *, int, char **);
static int svr_disablebreakpoint(dbg_backend *, int, char **);
static int svr_conditionbreakpoint(dbg_backend *, int, char **);
static int svr_breakpointafter(dbg_backend *, int, char **);
static int svr_setwatchpoint(dbg_backend *, int, char **);
static int svr_go(dbg_backend *, int, char **);
static int svr_step(dbg_backend *, int, char **);
static int svr_terminate(dbg_backend *, int, char **);
static int svr_suspend(dbg_backend *, int, char **);
static int svr_liststackframes(dbg_backend *, int, char **);
static int svr_setcurrentstackframe(dbg_backend *, int, char **);
static int svr_evaluateexpression(dbg_backend *, int, char **);
static int svr_gettype(dbg_backend *, int, char **);
static int svr_listlocalvariables(dbg_backend *, int, char **);
static int svr_listarguments(dbg_backend *, int, char **);
static int svr_listglobalvariables(dbg_backend *, int, char **);
static int svr_listinfothreads(dbg_backend *, int, char **);
static int svr_setthreadselect(dbg_backend *, int, char **);
static int svr_stackinfodepth(dbg_backend *, int, char **);
static int svr_datareadmemory(dbg_backend *, int, char **);
static int svr_datawritememory(dbg_backend *, int, char **);
static int svr_listsignals(dbg_backend *, int, char **);
static int svr_signalinfo(dbg_backend *, int, char **);
static int svr_clihandle(dbg_backend *, int, char **);
static int svr_quit(dbg_backend *, int, char **);
static int svr_dataevaluateexpression(dbg_backend *, int, char **);
static int svr_getpartialaif(dbg_backend *, int, char **);
static int svr_variabledelete(dbg_backend *, int, char **);

static svr_cmd svr_cmd_tab[] =
{
	/* DBG_QUIT_CMD */					svr_quit,
	/* DBG_STARTSESSION_CMD */			svr_start_session,
	/* DBG_SETLINEBREAKPOINT_CMD */		svr_setlinebreakpoint,
	/* DBG_SETFUNCBREAKPOINT_CMD */		svr_setfuncbreakpoint,
	/* DBG_DELETEBREAKPOINT_CMD */		svr_deletebreakpoint,
	/* DBG_ENABLEBREAKPOINT_CMD */		svr_enablebreakpoint,
	/* DBG_DISABLEBREAKPOINT_CMD */		svr_disablebreakpoint,
	/* DBG_CONDITIONBREAKPOINT_CMD */	svr_conditionbreakpoint,
	/* DBG_BREAKPOINTAFTER_CMD */		svr_breakpointafter,
	/* DBG_SETWATCHPOINT_CMD */			svr_setwatchpoint,
	/* DBG_GO_CMD */					svr_go,
	/* DBG_STEP_CMD */					svr_step,
	/* DBG_TERMINATE_CMD */				svr_terminate,
	/* DBG_SUSPEND_CMD */				svr_suspend,
	/* DBG_LISTSTACKFRAMES_CMD */		svr_liststackframes,
	/* DBG_SETCURRENTSTACKFRAME_CMD */	svr_setcurrentstackframe,
	/* DBG_EVALUATEEXPRESSION_CMD */	svr_evaluateexpression,
	/* DBG_GETTYPE_CMD */				svr_gettype,
	/* DBG_LISTLOCALVARIABLES_CMD */	svr_listlocalvariables,
	/* DBG_LISTARGUMENTS_CMD */			svr_listarguments,
	/* DBG_LISTGLOBALVARIABLES_CMD */	svr_listglobalvariables,
	/* DBG_LISTINFOTHREADS_CMD */		svr_listinfothreads,
	/* DBG_SETTHREADSELECT_CMD */		svr_setthreadselect,
	/* DBG_STACKINFODEPTH_CMD */		svr_stackinfodepth,
	/* DBG_DATAREADMEMORY_CMD */		svr_datareadmemory,
	/* DBG_DATAWRITEMEMORY_CMD */		svr_datawritememory,
	/* DBG_LISTSIGNALS_CMD */			svr_listsignals,
	/* DBG_SIGNALINFO_CMD */			svr_signalinfo,
	/* DBG_CLIHANDLE_CMD */				svr_clihandle,
	/* DBG_DATAEVALUATEEXPRESSION_CMD */svr_dataevaluateexpression,
	/* DBG_GETPARTIALAIF_CMD */			svr_getpartialaif,
	/* DBG_VARIABLEDELETE_CMD */		svr_variabledelete,
};

static int			svr_res;
static void			(*event_callback)(dbg_event *, void *);
static void *		event_data;
static char **		svr_env;
static int			svr_last_tid;
static int			svr_state;

static void
svr_event_callback(dbg_event *e)
{
	e->trans_id = svr_last_tid;
	event_callback(e, event_data);
}

int
svr_isshutdown(void)
{
	if (svr_state == SVR_SHUTDOWN_STARTED) {
		svr_state = SVR_SHUTDOWN_COMPLETED;
		return 0;
	}

	return (svr_state == SVR_SHUTDOWN_COMPLETED) ? 1 : 0;
}

int
svr_init(dbg_backend *db, void (*cb)(dbg_event *, void *))
{
	event_callback = cb;
	svr_env = NULL;
	svr_last_tid = 0;
	svr_state = SVR_RUNNING;
	return db->db_funcs->init(svr_event_callback);
}

int
svr_dispatch(dbg_backend *db, char *cmd_str, int len, void *data)
{
	int			idx;
	proxy_msg *	msg;
	svr_cmd		cmd;

	event_data = data;

	if (proxy_deserialize_msg(cmd_str, len, &msg) < 0) {
		svr_res = DBGRES_ERR;
		DbgSetError(DBGERR_DEBUGGER, "bad debug message format");
		return 0;
	}

	idx = msg->msg_id - DBG_CMD_BASE;

	if (idx >= 0 && idx < sizeof(svr_cmd_tab)/sizeof(svr_cmd)) {
		svr_last_tid = msg->trans_id;
		cmd = svr_cmd_tab[idx];
		if (cmd != NULL) {
			svr_res = cmd(db, msg->num_args, msg->args);
		}
	} else {
		svr_res = DBGRES_ERR;
		DbgSetError(DBGERR_DEBUGGER, "Unknown command");
	}

	free_proxy_msg(msg);

	return 0;
}

int
svr_progress(dbg_backend *db)
{
	dbg_event *	e;

	if (svr_res != DBGRES_OK) {
		e = NewDbgEvent(DBGEV_ERROR);
		e->dbg_event_u.error_event.error_code = DbgGetError();
		e->dbg_event_u.error_event.error_msg = strdup(DbgGetErrorStr());
		svr_event_callback(e);
		FreeDbgEvent(e);
		svr_res = DBGRES_OK;
		return 0;
	}

	db->db_funcs->progress();

	return 0;
}

int
svr_suspend(dbg_backend *db, int nargs, char **args)
{
	return db->db_funcs->interrupt();
}

static int
svr_start_session(dbg_backend *db, int nargs, char **args)
{
	if (nargs < 4) {
		DbgSetError(DBGERR_DEBUGGER, "not enough arguments for debug cmd");
		return DBGRES_ERR;
	}

	return db->db_funcs->start_session(db->db_exe_path, args[1], args[2], args[3], &args[4], svr_env, strtol(args[0], NULL, 10));
}

static int
svr_setlinebreakpoint(dbg_backend *db, int nargs, char **args)
{
	if (nargs < 8) {
		DbgSetError(DBGERR_DEBUGGER, "not enough arguments for debug cmd");
		return DBGRES_ERR;
	}

	return db->db_funcs->setlinebreakpoint((int)strtol(args[0], NULL, 10), (int)strtol(args[1], NULL, 10), (int)strtol(args[2], NULL, 10), args[3], (int)strtol(args[4], NULL, 10), args[5], (int)strtol(args[6], NULL, 10), (int)strtol(args[7], NULL, 10));
}

static int
svr_setfuncbreakpoint(dbg_backend *db, int nargs, char **args)
{
	if (nargs < 8) {
		DbgSetError(DBGERR_DEBUGGER, "not enough arguments for debug cmd");
		return DBGRES_ERR;
	}

	return db->db_funcs->setfuncbreakpoint((int)strtol(args[0], NULL, 10), (int)strtol(args[1], NULL, 10), (int)strtol(args[2], NULL, 10), args[3], args[4], args[5], (int)strtol(args[6], NULL, 10), (int)strtol(args[7], NULL, 10));
}

static int
svr_deletebreakpoint(dbg_backend *db, int nargs, char **args)
{
	if (nargs < 1) {
		DbgSetError(DBGERR_DEBUGGER, "not enough arguments for debug cmd");
		return DBGRES_ERR;
	}

	return db->db_funcs->deletebreakpoint((int)strtol(args[0], NULL, 10));
}

static int
svr_enablebreakpoint(dbg_backend *db, int nargs, char **args)
{
	if (nargs < 1) {
		DbgSetError(DBGERR_DEBUGGER, "not enough arguments for debug cmd");
		return DBGRES_ERR;
	}

	return db->db_funcs->enablebreakpoint((int)strtol(args[0], NULL, 10));
}

static int
svr_disablebreakpoint(dbg_backend *db, int nargs, char **args)
{
	if (nargs < 1) {
		DbgSetError(DBGERR_DEBUGGER, "not enough arguments for debug cmd");
		return DBGRES_ERR;
	}

	return db->db_funcs->disablebreakpoint((int)strtol(args[0], NULL, 10));
}

static int
svr_conditionbreakpoint(dbg_backend *db, int nargs, char **args)
{
	if (nargs < 2) {
		DbgSetError(DBGERR_DEBUGGER, "not enough arguments for debug cmd");
		return DBGRES_ERR;
	}

	return db->db_funcs->conditionbreakpoint((int)strtol(args[0], NULL, 10), args[1]);
}

static int
svr_breakpointafter(dbg_backend *db, int nargs, char **args)
{
	if (nargs < 2) {
		DbgSetError(DBGERR_DEBUGGER, "not enough arguments for debug cmd");
		return DBGRES_ERR;
	}

	return db->db_funcs->breakpointafter((int)strtol(args[0], NULL, 10), (int)strtol(args[1], NULL, 10));
}

static int
svr_setwatchpoint(dbg_backend *db, int nargs, char **args)
{
	if (nargs < 6) {
		DbgSetError(DBGERR_DEBUGGER, "not enough arguments for debug cmd");
		return DBGRES_ERR;
	}

	return db->db_funcs->setwatchpoint((int)strtol(args[0], NULL, 10), args[1], (int)strtol(args[2], NULL, 10), (int)strtol(args[3], NULL, 10), args[4], (int)strtol(args[5], NULL, 10));
}

static int
svr_go(dbg_backend *db, int nargs, char **args)
{
	return db->db_funcs->go();
}

static int
svr_step(dbg_backend *db, int nargs, char **args)
{
	if (nargs < 2) {
		DbgSetError(DBGERR_DEBUGGER, "not enough arguments for debug cmd");
		return DBGRES_ERR;
	}

	return db->db_funcs->step((int)strtol(args[0], NULL, 10), (int)strtol(args[1], NULL, 10));
}

static int
svr_terminate(dbg_backend *db, int nargs, char **args)
{
	return db->db_funcs->terminate();
}

static int
svr_liststackframes(dbg_backend *db, int nargs, char **args)
{
	if (nargs < 2) {
		DbgSetError(DBGERR_DEBUGGER, "not enough arguments for debug cmd");
		return DBGRES_ERR;
	}

	return db->db_funcs->liststackframes((int)strtol(args[0], NULL, 10), (int)strtol(args[1], NULL, 10));
}

static int
svr_setcurrentstackframe(dbg_backend *db, int nargs, char **args)
{
	if (nargs < 1) {
		DbgSetError(DBGERR_DEBUGGER, "not enough arguments for debug cmd");
		return DBGRES_ERR;
	}

	return db->db_funcs->setcurrentstackframe((int)strtol(args[0], NULL, 10));
}

static int
svr_evaluateexpression(dbg_backend *db, int nargs, char **args)
{
	if (nargs < 1) {
		DbgSetError(DBGERR_DEBUGGER, "not enough arguments for debug cmd");
		return DBGRES_ERR;
	}

	return db->db_funcs->evaluateexpression(args[0]);
}

static int
svr_gettype(dbg_backend *db, int nargs, char **args)
{
	if (nargs < 1) {
		DbgSetError(DBGERR_DEBUGGER, "not enough arguments for debug cmd");
		return DBGRES_ERR;
	}

	return db->db_funcs->gettype(args[0]);
}

static int
svr_listlocalvariables(dbg_backend *db, int nargs, char **args)
{
	return db->db_funcs->listlocalvariables();
}

static int
svr_listarguments(dbg_backend *db, int nargs, char **args)
{
	if (nargs < 2) {
		DbgSetError(DBGERR_DEBUGGER, "not enough arguments for debug cmd");
		return DBGRES_ERR;
	}

	return db->db_funcs->listarguments((int)strtol(args[0], NULL, 10), (int)strtol(args[1], NULL, 10));
}

static int
svr_listglobalvariables(dbg_backend *db, int nargs, char **args)
{
	return db->db_funcs->listglobalvariables();
}

static int
svr_listinfothreads(dbg_backend *db, int nargs, char **args)
{
	return db->db_funcs->listinfothreads();
}

static int
svr_setthreadselect(dbg_backend *db, int nargs, char **args)
{
	if (nargs < 1) {
		DbgSetError(DBGERR_DEBUGGER, "not enough arguments for debug cmd");
		return DBGRES_ERR;
	}

	return db->db_funcs->setthreadselect((int)strtol(args[0], NULL, 10));
}

static int
svr_stackinfodepth(dbg_backend *db, int nargs, char **args) {
	return db->db_funcs->stackinfodepth();
}

static int
svr_datareadmemory(dbg_backend *db, int nargs, char **args)  {
	if (nargs < 7) {
		DbgSetError(DBGERR_DEBUGGER, "not enough arguments for debug cmd");
		return DBGRES_ERR;
	}

	return db->db_funcs->datareadmemory(strtol(args[0], NULL, 10), args[1], args[2], (int)strtol(args[3], NULL, 10), (int)strtol(args[4], NULL, 10), (int)strtol(args[5], NULL, 10), args[6]);
}

static int
svr_datawritememory(dbg_backend *db, int nargs, char **args) {
	if (nargs < 5) {
	}

	return db->db_funcs->datawritememory(strtol(args[0], NULL, 10), args[1], args[2], (int)strtol(args[3], NULL, 10), args[4]);
}

static int
svr_listsignals(dbg_backend *db, int nargs, char **args)
{
	if (nargs < 1) {
	}

	return db->db_funcs->listsignals(args[0]);
}
static int
svr_signalinfo(dbg_backend *db, int nargs, char **args)
{
	if (nargs < 1) {
		DbgSetError(DBGERR_DEBUGGER, "not enough arguments for debug cmd");
		return DBGRES_ERR;
	}

	return db->db_funcs->signalinfo(args[0]);
}

static int
svr_clihandle(dbg_backend *db, int nargs, char **args)
{
	if (nargs < 1) {
		DbgSetError(DBGERR_DEBUGGER, "not enough arguments for debug cmd");
		return DBGRES_ERR;
	}

	return db->db_funcs->clihandle(args[0]);
}

static int
svr_quit(dbg_backend *db, int nargs, char **args)
{
	svr_state = SVR_SHUTDOWN_STARTED;
	return db->db_funcs->quit();
}

static int
svr_dataevaluateexpression(dbg_backend *db, int nargs, char **args)
{
	if (nargs < 1) {
		DbgSetError(DBGERR_DEBUGGER, "not enough arguments for debug cmd");
		return DBGRES_ERR;
	}

	return db->db_funcs->dataevaluateexpression(args[0]);
}
static int
svr_getpartialaif(dbg_backend *db, int nargs, char **args)
{
	if (nargs < 4) {
		DbgSetError(DBGERR_DEBUGGER, "not enough arguments for debug cmd");
		return DBGRES_ERR;
	}

	return db->db_funcs->getpartialaif(args[0], args[1], (int)strtol(args[2], NULL, 10), (int)strtol(args[3], NULL, 10));
}

static int
svr_variabledelete(dbg_backend *db, int nargs, char **args)
{
	if (nargs < 1) {
		DbgSetError(DBGERR_DEBUGGER, "not enough arguments for debug cmd");
		return DBGRES_ERR;
	}

	return db->db_funcs->variabledelete(args[0]);
}
