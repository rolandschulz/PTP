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
 
#ifndef _BACKEND_H_
#define _BACKEND_H_

#include <stdarg.h>
 
#include "bitset.h"
#include "breakpoint.h"
#include "stackframe.h"
#include "memoryinfo.h"
#include "dbg_event.h"

struct dbg_backend_funcs {
	int (*init)(void (*)(dbg_event *, void *), void *);
	int (*progress)(void);
	int (*interrupt)(void);
	int (*start_session)(char *, char *, char *, char *, char **, char **, long);
	int (*setlinebreakpoint)(int, int, int, char *, int, char *, int, int);
	int (*setfuncbreakpoint)(int, int, int, char *, char *, char *, int, int);
	int (*deletebreakpoint)(int);
	int (*enablebreakpoint)(int);
	int (*disablebreakpoint)(int);
	int (*conditionbreakpoint)(int, char *);
	int (*breakpointafter)(int, int);
	int (*setwatchpoint)(int, char *, int, int, char *, int);
	int (*go)(void);
	int (*step)(int, int);
	int (*terminate)(void);
	int (*liststackframes)(int, int);
	int (*setcurrentstackframe)(int);
	int (*evaluateexpression)(char *);
	int (*gettype)(char *);
	int (*listlocalvariables)(void);
	int (*listarguments)(int, int);
	int (*listglobalvariables)(void);
	int (*listinfothreads)(void);
	int (*setthreadselect)(int);
	int (*stackinfodepth)(void);
	int	(*datareadmemory)(long, char*, char*, int, int, int, char*);
	int	(*datawritememory)(long, char*, char*, int, char*);
	int (*listsignals)(char*);
	int (*signalinfo)(char*);
	int (*clihandle)(char*);
	int (*dataevaluateexpression)(char*);
	int (*getpartialaif)(char*, char*, int, int);
	int (*variabledelete)(char*);
	int (*quit)(void);
};
typedef struct dbg_backend_funcs	dbg_backend_funcs;

struct dbg_backend {
	char *						db_name;
	struct dbg_backend_funcs *	db_funcs;
	char *						db_exe_path;
};
typedef struct dbg_backend	dbg_backend;

extern dbg_backend 	dbg_backends[];

extern int	find_dbg_backend(char *, dbg_backend **);
extern void	backend_set_path(dbg_backend *, char *);

#endif /* _BACKEND_H_*/
