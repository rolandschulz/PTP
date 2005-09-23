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

#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include "dbg.h"

static int 			dbg_errno = DBGRES_OK;
static char *		dbg_errstr = NULL;

static char * dbg_error_tab[] =
{
	"NO_ERROR",
	"Protocol not implemented",
	"No such protocol \"%s\"",
	"Debugger error: %s",
	"No server for this process",
	"Must register callback first",
	"Callback in progress",
	"No server to register callback",
	"Could not create callback",
	"Session already established",
	"No line %s in file ",
	"Function \"%s\" not defined",
	"No source file named \"%s\"",
	"No breakpoint number %s",
	"No symbol \"%s\" in current context",
	"Can't access memory",
	"Can't run the program",
	"Could not invoke the program",
	"Program is running",
	"The program is not being run",
	"Initial frame selected; you cannot go up",
	"Bottom (i.e., innermost) frame selected; you cannot go down",
	"Argument required (breakpoint number)",
	"Error in regular expression",
	"No stack.",
	"Line number %s is out of range for ",
	"%s: No such file or directory",
	"No debugging symbols",
	"Could not create temporary file",
	"pipe: %s",
	"fork: %s",
	"select: %s",
	"%s: not in executable format",
	"%s",
	"Source file is more recent than executable",
	"Can't set variable.",
	"Invalid process set",
	"Unkown error: \"%s\""
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