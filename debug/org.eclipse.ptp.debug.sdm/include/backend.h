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
 
#include "procset.h"
#include "breakpoint.h"
#include "stackframe.h"
#include "dbg_event.h"

struct dbg_backend_funcs {
	int (*init)(int *, int *, dbg_event **);
	int (*setlinebreakpoint)(char *, int, dbg_event **);
	int (*setfuncbreakpoint)(char *, char *, dbg_event **);
	int (*deletebreakpoints)(int);
	int (*go)(char *, int, dbg_event **);
	int (*step)(int, int, char *, int, dbg_event **);
	int (*liststackframes)(void *, int, dbg_event **);
	int (*setcurrentstackframe)(int, int, dbg_event **);
	int (*evaluateexpression)(char *, dbg_event **);
	int (*listlocalvariables)(dbg_event **);
	int (*listarguments)(dbg_event **);
	int (*listglobalvariables)(dbg_event **);
	int (*quit)(void *, dbg_event **);
	int (*progress)(void *, void (*)(dbg_event *));
};
typedef struct dbg_backend_funcs	dbg_backend_funcs;

struct dbg_backend {
	char *						dbg_name;
	struct dbg_backend_funcs *	dbg_funcs;
};
typedef struct dbg_backend	dbg_backend;

extern dbg_backend 	dbg_backends[];

extern int find_dbg_backend(char *, dbg_backend **);

#endif /* _BACKEND_H_*/
