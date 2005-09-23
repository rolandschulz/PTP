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
#include "backend.h"

struct svr_cmd {
	char *cmd_name;
	int (*cmd_func)(dbg_backend *, char **);
};

typedef struct svr_cmd	svr_cmd;

static int svr_start_session(dbg_backend *, char **);
static int svr_setlinebreakpoint(dbg_backend *, char **);
static int svr_setfuncbreakpoint(dbg_backend *, char **);
static int svr_deletebreakpoints(dbg_backend *, char **);
static int svr_go(dbg_backend *, char **);
static int svr_step(dbg_backend *, char **);
static int svr_liststackframes(dbg_backend *, char **);
static int svr_setcurrentstackframe(dbg_backend *, char **);
static int svr_evaluateexpression(dbg_backend *, char **);
static int svr_listlocalvariables(dbg_backend *, char **);
static int svr_listarguments(dbg_backend *, char **);
static int svr_listglobalvariables(dbg_backend *, char **);
static int svr_quit(dbg_backend *, char **);

static svr_cmd svr_cmd_tab[] =
{
	{"INI",	svr_start_session},
	{"SLB",	svr_setlinebreakpoint},
	{"SFB",	svr_setfuncbreakpoint},
	{"DBS",	svr_deletebreakpoints},
	{"GOP",	svr_go},
	{"STP",	svr_step},
	{"LSF",	svr_liststackframes},
	{"SCS",	svr_setcurrentstackframe},
	{"EEX",	svr_evaluateexpression},
	{"LLV",	svr_listlocalvariables},
	{"LAR",	svr_listarguments},
	{"LGV",	svr_listglobalvariables},
	{"QUI",	svr_quit},
};

static int			svr_shutdown = 0;
static int			svr_res;
static dbg_event *	svr_event;
static void			(*svr_event_callback)(dbg_event *, void *);
static void *		svr_data;

int
svr_init(dbg_backend *db, void (*cb)(dbg_event *, void *), void *data)
{
	svr_event_callback = cb;
	svr_data = data;
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
		DbgSetError(DBGERR_PROTO, "Unknown command");
	}
	
	return svr_shutdown;
}

int
svr_progress(dbg_backend *db)
{
	dbg_event *	e;
	
	if (svr_res != DBGRES_OK) {
		e = NewEvent(DBGEV_ERROR);
		e->error_code = DbgGetError();
		e->error_msg = strdup(DbgGetErrorStr());
		svr_event_callback(e, svr_data);
		FreeEvent(e);
		svr_res = DBGRES_OK;
		return 0;
	}
	
	return db->db_funcs->progress();
}

static int 
svr_start_session(dbg_backend *db, char **args)
{
	return db->db_funcs->start_session(args[1], args[2]);
}

static int 
svr_setlinebreakpoint(dbg_backend *db, char **args)
{
	return db->db_funcs->setlinebreakpoint(args[1], atoi(args[2]));
}

static int 
svr_setfuncbreakpoint(dbg_backend *db, char **args)
{
	return db->db_funcs->setfuncbreakpoint(args[1], args[2]);
}

static int 
svr_deletebreakpoints(dbg_backend *db, char **args)
{
	DbgSetError(DBGERR_NOTIMP, "Command not implemented");
	return DBGRES_ERR;
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
svr_liststackframes(dbg_backend *db, char **args)
{
	return db->db_funcs->liststackframes(atoi(args[1]));
}

static int 
svr_setcurrentstackframe(dbg_backend *db, char **args)
{
	DbgSetError(DBGERR_NOTIMP, "Command not implemented");
	return DBGRES_ERR;
}

static int 
svr_evaluateexpression(dbg_backend *db, char **args)
{
	return db->db_funcs->evaluateexpression(args[1]);
}

static int 
svr_listlocalvariables(dbg_backend *db, char **args)
{
	DbgSetError(DBGERR_NOTIMP, "Command not implemented");
	return DBGRES_ERR;
}

static int 
svr_listarguments(dbg_backend *db, char **args)
{
	DbgSetError(DBGERR_NOTIMP, "Command not implemented");
	return DBGRES_ERR;
}

static int 
svr_listglobalvariables(dbg_backend *db, char **args)
{
	DbgSetError(DBGERR_NOTIMP, "Command not implemented");
	return DBGRES_ERR;
}

static int 
svr_quit(dbg_backend *db, char **args)
{
	return db->db_funcs->quit();
}