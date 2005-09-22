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

struct svr_cmd {
	char *cmd_name;
	int (*cmd_func)(char **, dbg_event **);
};

typedef struct svr_cmd	svr_cmd;

static int svr_setlinebreakpoint(char **, dbg_event **);
static int svr_setfuncbreakpoint(char **, dbg_event **);
static int svr_deletebreakpoints(char **, dbg_event **);
static int svr_go(char **, dbg_event **);
static int svr_step(char **, dbg_event **);
static int svr_liststackframes(char **, dbg_event **);
static int svr_setcurrentstackframe(char **, dbg_event **);
static int svr_evaluateexpression(char **, dbg_event **);
static int svr_listlocalvariables(char **, dbg_event **);
static int svr_listarguments(char **, dbg_event **);
static int svr_listglobalvariables(char **, dbg_event **);
static int svr_quit(char **, dbg_event **);

static svr_cmd svr_cmd_tab[] =
{
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

static int	svr_shutdown = 0;

int
svr_dispatch(char *cmd, char **resp)
{
	int			i;
	int			res;
	char **		args;
	svr_cmd *	sc;
	dbg_event *	e;
	
	args = Str2Args(cmd);
	
	for (i = 0; i < sizeof(svr_cmd_tab) / sizeof(svr_cmd); i++) {
		sc = &svr_cmd_tab[i];
		if (strcmp(args[0], sc->cmd_name) == 0) {
			res = sc->cmd_func(args, &e);
			break;
		}
	}
	
	if (i == sizeof(svr_cmd_tab) / sizeof(svr_cmd)) {
		res = DBGRES_ERR;
		DbgSetError(DBGERR_PROTO, "Unknown command");
	}
	
	FreeArgs(args);
	
	if (res != DBGRES_OK) {
		e = NewEvent(DBGEV_ERROR);
		e->error_code = DbgGetError();
		e->error_msg = strdup(DbgGetErrorStr());
	}
	
	if (proxy_tcp_event_to_str(e, resp) < 0)
		*resp = strdup("ERROR");

printf("response will be <%s>\n", *resp);
	
	FreeEvent(e);
			
	return svr_shutdown;
}

static int 
svr_setlinebreakpoint(char **args, dbg_event **ev)
{
	dbg_event *	e;
	int i = rand() % 5;
	
	if (i == 0) {
		e = NewEvent(DBGEV_ERROR);
		e->error_code = i;
		e->error_msg = strdup("test error");
	}
	else
		e = NewEvent(DBGEV_OK);
		
	*ev = e;
	return DBGRES_OK;
}

static int 
svr_setfuncbreakpoint(char **args, dbg_event **ev)
{
	return DBGRES_OK;
}

static int 
svr_deletebreakpoints(char **args, dbg_event **ev)
{
	return DBGRES_OK;
}

static int 
svr_go(char **args, dbg_event **ev)
{
	return DBGRES_OK;
}

static int 
svr_step(char **args, dbg_event **ev)
{
	return DBGRES_OK;
}

static int 
svr_liststackframes(char **args, dbg_event **ev)
{
	return DBGRES_OK;
}

static int 
svr_setcurrentstackframe(char **args, dbg_event **ev)
{
	return DBGRES_OK;
}

static int 
svr_evaluateexpression(char **args, dbg_event **ev)
{
	return DBGRES_OK;
}

static int 
svr_listlocalvariables(char **args, dbg_event **ev)
{
	return DBGRES_OK;
}

static int 
svr_listarguments(char **args, dbg_event **ev)
{
	return DBGRES_OK;
}

static int 
svr_listglobalvariables(char **args, dbg_event **ev)
{
	return DBGRES_OK;
}

static int 
svr_quit(char **args, dbg_event **ev)
{
	dbg_event *	e = NewEvent(DBGEV_OK);
	*ev = e;
	
	svr_shutdown++;
	return DBGRES_OK;
}