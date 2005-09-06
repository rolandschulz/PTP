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
#ifndef _PROXY_H_
#define _PROXY_H_

#include "procset.h"
#include "stackframe.h"
#include "breakpoint.h"

struct proxy_funcs {
	int (*init)(void);
	int (*setlinebreakpoint)(session *, procset *, char *, int, breakpoint *);
	int (*setfuncbreakpoint)(session *, procset *, char *, char *, breakpoint *);
	int (*deletebreakpoints)(session *, procset *, breakpoint *);
	int (*go)(session *, procset *);
	int (*step)(session *, procset *, int, int);
	int (*liststackframes)(session *, int, stackframelist *);
	int (*setcurrentstackframe)(session *, int, int, int, stackframe *);
	int (*evaluateexpression)(session *, int, char *);
	int (*listlocalvariables)(session *, int, stackframe *);
	int (*listarguments)(session *, int, stackframe*);
	int (*listglobalvariables)(session *, int);
	int (*progress)(session *);
};
typedef struct proxy_funcs	proxy_funcs;

extern int proxy_init_not_imp(void);
extern int proxy_setlinebreakpoint_not_imp(session *, procset *, char *, int, breakpoint *);
extern int proxy_setfuncbreakpoint_not_imp(session *, procset *, char *, char *, breakpoint *);
extern int proxy_deletebreakpoints_not_imp(session *, procset *, breakpoint *);
extern int proxy_go_not_imp(session *, procset *);
extern int proxy_step_not_imp(session *, procset *, int, int);
extern int proxy_liststackframes_not_imp(session *, int, stackframelist *);
extern int proxy_setcurrentstackframe_not_imp(session *, int, int, int, stackframe *);
extern int proxy_evaluateexpression_not_imp(session *, int, char *);
extern int proxy_listlocalvariables_not_imp(session *, int, stackframe *);
extern int proxy_listarguments_not_imp(session *, int, stackframe*);
extern int proxy_listglobalvariables_not_imp(session *, int);
extern int proxy_progress_not_imp(session *);

#endif /* _PROXY_H_*/
