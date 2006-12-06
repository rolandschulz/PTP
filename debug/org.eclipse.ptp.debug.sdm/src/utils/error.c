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

#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include "dbg.h"

static int 			dbg_errno = DBGRES_OK;
static char *		dbg_errstr = NULL;

static char * dbg_error_tab[] =
{
	/* 0 */						"NO_ERROR",
	/* DBGERR_NOTIMP */			"Protocol not implemented",
	/* DBGERR_PROXY_TERM */		"Proxy connection unexpectedly terminated",
	/* DBGERR_PROXY_PROTO */	"Bad message received by proxy",
	/* DBGERR_DEBUGGER */ 		"Debugger error: %s",
	/* DBGERR_NOBACKEND */ 		"Backend debugger executable \"%s\" not found",
	/* DBGERR_INPROGRESS */		"Callback in progress",
	/* DBGERR_CBCREATE */		"No server to register callback",
	/* DBGERR_NOSESSION */		"Must start session first",
	/* DBGERR_SESSION */		"Session already established",
	/* DBGERR_NOLINE */			"No line %s in file ",
	/* DBGERR_NOFUNC */			"Function \"%s\" not defined",
	/* DBGERR_NOFILE */			"No source file named \"%s\"",
	/* DBGERR_NOBP */			"No breakpoint number %s",
	/* DBGERR_NOSYM */			"No symbol \"%s\" in current context",
	/* DBGERR_NOMEM */			"Can't access memory",
	/* DBGERR_CANTRUN */		"Can't run the program",
	/* DBGERR_INVOKE */			"Could not invoke the program",
	/* DBGERR_ISRUNNING */		"Program is running",
	/* DBGERR_NOTRUN */			"The program is not being run",
	/* DBGERR_FIRSTFRAME */		"Initial frame selected; you cannot go up",
	/* DBGERR_LASTFRAME */		"Bottom (i.e., innermost) frame selected; you cannot go down",
	/* DBGERR_BADBPARG */		"Argument required (breakpoint number)",
	/* DBGERR_REGEX */			"Error in regular expression",
	/* DBGERR_NOSTACK */		"No stack.",
	/* DBGERR_OUTOFRANGE */		"Line number %s is out of range for ",
	/* DBGERR_NOFILEDIR */		"%s: No such file or directory",
	/* DBGERR_NOSYMS */			"No debugging symbols",
	/* DBGERR_TEMP */			"Could not create temporary file",
	/* DBGERR_PIPE */			"pipe: %s",
	/* DBGERR_FORK */			"fork: %s",
	/* DBGERR_SYSTEM */			"system error: %s",
	/* DBGERR_NOTEXEC */		"%s: not in executable format",
	/* DBGERR_CHDIR */			"%s",
	/* DBGERR_SOURCE */			"Source file is more recent than executable",
	/* DBGERR_SETVAR */			"Can't set variable.",
	/* DBGERR_PROCSET */		"Invalid process set",
	/* DBGERR_UNKNOWN */		"Unknown error: \"%s\"",
	/* DBGERR_UNKNOWN_TYPE */	"Unknown type: \"%s\"",
	/* DBGERR_UNKNOWN_VARIABLE */"Unknown variable: \"%s\""
};

/*
 * Error handling
 */
void
DbgSetError(int errnum, char *msg)
{
	dbg_errno = errnum;
	
	if (dbg_errstr != NULL) {
		free(dbg_errstr);
		dbg_errstr = NULL;
	}
	
	if (dbg_errno >= sizeof(dbg_error_tab)/sizeof(char *)) {
		if (msg != NULL) {
			dbg_errstr = strdup(msg);
		} else {
			asprintf(&dbg_errstr, "Error %d occurred.", dbg_errno);
		}
	} else {
		if (msg == NULL)
			msg = "<null>";
			
		asprintf(&dbg_errstr, dbg_error_tab[dbg_errno], msg);
	}
}

int
DbgGetError(void)
{
	return dbg_errno;
}

char *
DbgGetErrorStr(void)
{
	if (dbg_errstr == NULL)
		return "";
		
	return dbg_errstr;
}
