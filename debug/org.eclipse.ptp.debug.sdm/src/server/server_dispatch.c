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

struct svr_cmd {
	char *cmd_name;
	int (*cmd_func)(char **, char **);
};

typedef struct svr_cmd	svr_cmd;

static int svr_setlinebreakpoint(char **, char **);
static int svr_setfuncbreakpoint(char **, char **);
static int svr_deletebreakpoints(char **, char **);
static int svr_go(char **, char **);
static int svr_step(char **, char **);
static int svr_liststackframes(char **, char **);
static int svr_setcurrentstackframe(char **, char **);
static int svr_evaluateexpression(char **, char **);
static int svr_listlocalvariables(char **, char **);
static int svr_listarguments(char **, char **);
static int svr_listglobalvariables(char **, char **);
static int svr_quit(char **, char **);

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
	char *		result = NULL;
	svr_cmd *	sc;
	
	args = Str2Args(cmd);
	
	for (i = 0; i < sizeof(svr_cmd_tab) / sizeof(svr_cmd); i++) {
		sc = &svr_cmd_tab[i];
		if (strcmp(args[0], sc->cmd_name) == 0) {
			res = sc->cmd_func(args, &result);
			break;
		}
	}
	
	if (i == sizeof(svr_cmd_tab) / sizeof(svr_cmd)) {
		res = DBGRES_ERR;
		DbgSetError(DBGERR_PROTO, "Unknown command");
	}
	
	FreeArgs(args);
	
	if (res != DBGRES_OK)
		asprintf(resp, "%d %d \"%s\"", res, DbgGetError(), DbgGetErrorStr());
	else {
		if (result != NULL) {
			asprintf(resp, "%d %s", res, result);
			free(result);
		}
		else
			asprintf(resp, "%d", res);
	}
			
	return svr_shutdown;
}

static int 
svr_setlinebreakpoint(char **args, char **resp)
{
	return DBGRES_OK;
}

static int 
svr_setfuncbreakpoint(char **args, char **resp)
{
	return DBGRES_OK;
}

static int 
svr_deletebreakpoints(char **args, char **resp)
{
	return DBGRES_OK;
}

static int 
svr_go(char **args, char **resp)
{
	return DBGRES_OK;
}

static int 
svr_step(char **args, char **resp)
{
	return DBGRES_OK;
}

static int 
svr_liststackframes(char **args, char **resp)
{
	return DBGRES_OK;
}

static int 
svr_setcurrentstackframe(char **args, char **resp)
{
	return DBGRES_OK;
}

static int 
svr_evaluateexpression(char **args, char **resp)
{
	return DBGRES_OK;
}

static int 
svr_listlocalvariables(char **args, char **resp)
{
	return DBGRES_OK;
}

static int 
svr_listarguments(char **args, char **resp)
{
	return DBGRES_OK;
}

static int 
svr_listglobalvariables(char **args, char **resp)
{
	return DBGRES_OK;
}

static int 
svr_quit(char **args, char **resp)
{
	svr_shutdown++;
	return DBGRES_OK;
}