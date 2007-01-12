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

struct svr_cmd {
	char *cmd_name;
	int (*cmd_func)(dbg_backend *, char **);
};

typedef struct svr_cmd	svr_cmd;

static int svr_start_session(dbg_backend *, char **);
static int svr_setlinebreakpoint(dbg_backend *, char **);
static int svr_setfuncbreakpoint(dbg_backend *, char **);
static int svr_deletebreakpoint(dbg_backend *, char **);
static int svr_enablebreakpoint(dbg_backend *, char **);
static int svr_disablebreakpoint(dbg_backend *, char **);
static int svr_conditionbreakpoint(dbg_backend *, char **);
static int svr_breakpointafter(dbg_backend *, char **);
static int svr_setwatchpoint(dbg_backend *, char **);
static int svr_go(dbg_backend *, char **);
static int svr_step(dbg_backend *, char **);
static int svr_terminate(dbg_backend *, char **);
static int svr_liststackframes(dbg_backend *, char **);
static int svr_setcurrentstackframe(dbg_backend *, char **);
static int svr_evaluateexpression(dbg_backend *, char **);
static int svr_gettype(dbg_backend *, char **);
static int svr_listlocalvariables(dbg_backend *, char **);
static int svr_listarguments(dbg_backend *, char **);
static int svr_listglobalvariables(dbg_backend *, char **);
static int svr_listinfothreads(dbg_backend *, char **);
static int svr_setthreadselect(dbg_backend *, char **);
static int svr_stackinfodepth(dbg_backend *, char **);
static int svr_datareadmemory(dbg_backend *, char **);
static int svr_datawritememory(dbg_backend *, char **);
static int svr_listsignals(dbg_backend *, char **);
static int svr_signalinfo(dbg_backend *, char **);
static int svr_clihandle(dbg_backend *, char **);
static int svr_quit(dbg_backend *, char **);

static int svr_dataevaluateexpression(dbg_backend *, char **);
static int svr_getpartialaif(dbg_backend *, char **);
static int svr_variabledelete(dbg_backend *, char **);

static svr_cmd svr_cmd_tab[] =
{
	{DBG_STARTSESSION_CMD,			svr_start_session},
	{DBG_SETLINEBREAKPOINT_CMD,		svr_setlinebreakpoint},
	{DBG_SETFUNCBREAKPOINT_CMD,		svr_setfuncbreakpoint},
	{DBG_DELETEBREAKPOINT_CMD,		svr_deletebreakpoint},
	{DBG_ENABLEBREAKPOINT_CMD,		svr_enablebreakpoint},
	{DBG_DISABLEBREAKPOINT_CMD,		svr_disablebreakpoint},
	{DBG_CONDITIONBREAKPOINT_CMD,	svr_conditionbreakpoint},
	{DBG_BREAKPOINTAFTER_CMD,		svr_breakpointafter},
	{DBG_SETWATCHPOINT_CMD,			svr_setwatchpoint},
	{DBG_GO_CMD,					svr_go},
	{DBG_STEP_CMD,					svr_step},
	{DBG_TERMINATE_CMD,				svr_terminate},
	{DBG_LISTSTACKFRAMES_CMD,		svr_liststackframes},
	{DBG_SETCURRENTSTACKFRAME_CMD,	svr_setcurrentstackframe},
	{DBG_EVALUATEEXPRESSION_CMD,	svr_evaluateexpression},
	{DBG_GETTYPE_CMD,				svr_gettype},
	{DBG_LISTLOCALVARIABLES_CMD,	svr_listlocalvariables},
	{DBG_LISTARGUMENTS_CMD,			svr_listarguments},
	{DBG_LISTGLOBALVARIABLES_CMD,	svr_listglobalvariables},
	{DBG_LISTINFOTHREADS_CMD,		svr_listinfothreads}, 
	{DBG_SETTHREADSELECT_CMD,		svr_setthreadselect},
	{DBG_STACKINFODEPTH_CMD,		svr_stackinfodepth}, 
	{DBG_DATAREADMEMORY_CMD,		svr_datareadmemory},
	{DBG_DATAWRITEMEMORY_CMD,		svr_datawritememory},
	{DBG_LISTSIGNALS_CMD,			svr_listsignals},
	{DBG_SIGNALINFO_CMD,			svr_signalinfo},
	{DBG_CLIHANDLE_CMD,				svr_clihandle},
	{DBG_DATAEVALUATEEXPRESSION_CMD,svr_dataevaluateexpression},
	{DBG_GETPARTIALAIF_CMD,			svr_getpartialaif},
	{DBG_VARIABLEDELETE_CMD,		svr_variabledelete},
	{"QUI",							svr_quit},
};

static int			svr_res;
static void			(*svr_event_callback)(dbg_event *, void *);
static void *		svr_data;
static char **		svr_env;

int
svr_init(dbg_backend *db, void (*cb)(dbg_event *, void *), void *data, char **env)
{
	svr_event_callback = cb;
	svr_data = data;
	svr_env = env;
	return db->db_funcs->init(cb, data);
}

int
svr_dispatch(dbg_backend *db, char *cmd)
{
	int			i;
	char **		args;
	svr_cmd *	sc;
	
	args = Str2Args(cmd);
	
	for (i = 0; i < sizeof(svr_cmd_tab) / sizeof(svr_cmd); i++) {
		sc = &svr_cmd_tab[i];
		if (strcmp(args[0], sc->cmd_name) == 0) {
			svr_res = sc->cmd_func(db, args);
			break;
		}
	}
	
	FreeArgs(args);
	
	if (i == sizeof(svr_cmd_tab) / sizeof(svr_cmd)) {
		svr_res = DBGRES_ERR;
		DbgSetError(DBGERR_DEBUGGER, "Unknown command");
	}
	
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
		svr_event_callback(e, svr_data);
		FreeDbgEvent(e);
		svr_res = DBGRES_OK;
		return 0;
	}
	
	return db->db_funcs->progress();
}

int 
svr_interrupt(dbg_backend *db)
{
	return db->db_funcs->interrupt();
}

static int 
svr_start_session(dbg_backend *db, char **args)
{
	return db->db_funcs->start_session(db->db_exe_path, args[2], args[3], args[4], &args[5], svr_env, atol(args[1]));
}

static int 
svr_setlinebreakpoint(dbg_backend *db, char **args)
{
	return db->db_funcs->setlinebreakpoint(atoi(args[1]), atoi(args[2]), atoi(args[3]), args[4], atoi(args[5]), args[6], atoi(args[7]), atoi(args[8]));
}

static int 
svr_setfuncbreakpoint(dbg_backend *db, char **args)
{
	return db->db_funcs->setfuncbreakpoint(atoi(args[1]), atoi(args[2]), atoi(args[3]), args[4], args[5], args[6], atoi(args[7]), atoi(args[8]));
}

static int 
svr_deletebreakpoint(dbg_backend *db, char **args)
{
	return db->db_funcs->deletebreakpoint(atoi(args[1]));
}

static int 
svr_enablebreakpoint(dbg_backend *db, char **args)
{
	return db->db_funcs->enablebreakpoint(atoi(args[1]));
}

static int 
svr_disablebreakpoint(dbg_backend *db, char **args)
{
	return db->db_funcs->disablebreakpoint(atoi(args[1]));
}

static int 
svr_conditionbreakpoint(dbg_backend *db, char **args)
{
	return db->db_funcs->conditionbreakpoint(atoi(args[1]), args[2]);
}

static int 
svr_breakpointafter(dbg_backend *db, char **args)
{
	return db->db_funcs->breakpointafter(atoi(args[1]), atoi(args[2]));
}

static int 
svr_setwatchpoint(dbg_backend *db, char **args)
{
	return db->db_funcs->setwatchpoint(atoi(args[1]), args[2], atoi(args[3]), atoi(args[4]), args[5], atoi(args[6]));
}

static int 
svr_go(dbg_backend *db, char **args)
{
	return db->db_funcs->go();
}

static int 
svr_step(dbg_backend *db, char **args)
{
	return db->db_funcs->step(atoi(args[1]), atoi(args[2]));
}

static int 
svr_terminate(dbg_backend *db, char **args)
{
	return db->db_funcs->terminate();
}

static int 
svr_liststackframes(dbg_backend *db, char **args)
{
	return db->db_funcs->liststackframes(atoi(args[1]), atoi(args[2]));
}

static int 
svr_setcurrentstackframe(dbg_backend *db, char **args)
{
	return db->db_funcs->setcurrentstackframe(atoi(args[1]));
}

static int 
svr_evaluateexpression(dbg_backend *db, char **args)
{
	return db->db_funcs->evaluateexpression(args[1]);
}

static int 
svr_gettype(dbg_backend *db, char **args)
{
	return db->db_funcs->gettype(args[1]);
}

static int 
svr_listlocalvariables(dbg_backend *db, char **args)
{
	return db->db_funcs->listlocalvariables();
}

static int 
svr_listarguments(dbg_backend *db, char **args)
{
	return db->db_funcs->listarguments(atoi(args[1]), atoi(args[2]));
}

static int 
svr_listglobalvariables(dbg_backend *db, char **args)
{
	return db->db_funcs->listglobalvariables();
}

static int 
svr_listinfothreads(dbg_backend *db, char **args) 
{
	return db->db_funcs->listinfothreads();
}

static int 
svr_setthreadselect(dbg_backend *db, char **args) 
{
	return db->db_funcs->setthreadselect(atoi(args[1]));
}

static int 
svr_stackinfodepth(dbg_backend *db, char **args) {
	return db->db_funcs->stackinfodepth();
}

static int 
svr_datareadmemory(dbg_backend *db, char **args)  {
	return db->db_funcs->datareadmemory(atol(args[1]), args[2], args[3], atoi(args[4]), atoi(args[5]), atoi(args[6]), args[7]);
}

static int
svr_datawritememory(dbg_backend *db, char **args) {
	return db->db_funcs->datawritememory(atol(args[1]), args[2], args[3], atoi(args[4]), args[5]);
}

static int 
svr_listsignals(dbg_backend *db, char **args)
{
	return db->db_funcs->listsignals(args[1]);
}
static int
svr_signalinfo(dbg_backend *db, char **args)
{
	return db->db_funcs->signalinfo(args[1]);
}

static int
svr_clihandle(dbg_backend *db, char **args)
{
	return db->db_funcs->clihandle(args[1]);
}

static int 
svr_quit(dbg_backend *db, char **args)
{
	return db->db_funcs->quit();
}

static int
svr_dataevaluateexpression(dbg_backend *db, char **args)
{
	return db->db_funcs->dataevaluateexpression(args[1]);
}
static int
svr_getpartialaif(dbg_backend *db, char **args)
{
	return db->db_funcs->getpartialaif(args[1], args[2], atoi(args[3]), atoi(args[4]));
}
static int
svr_variabledelete(dbg_backend *db, char **args)
{
	return db->db_funcs->variabledelete(args[1]);
}
