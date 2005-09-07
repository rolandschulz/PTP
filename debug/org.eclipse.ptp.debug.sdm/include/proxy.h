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

struct proxy {
	char *				name;
	struct proxy_funcs *	funcs;
};
typedef struct proxy	proxy;

struct proxy_funcs {
	int (*init)(void *);
	int (*setlinebreakpoint)(struct session *, struct procset *, char *, int, struct breakpoint *);
	int (*setfuncbreakpoint)(struct session *, struct procset *, char *, char *, struct breakpoint *);
	int (*deletebreakpoints)(struct session *, struct procset *, struct breakpoint *);
	int (*go)(struct session *, struct procset *);
	int (*step)(struct session *, struct procset *, int, int);
	int (*liststackframes)(struct session *, int, struct stackframelist *);
	int (*setcurrentstackframe)(struct session *, int, int, int, struct stackframe *);
	int (*evaluateexpression)(struct session *, int, char *);
	int (*listlocalvariables)(struct session *, int, struct stackframe *);
	int (*listarguments)(struct session *, int, struct stackframe*);
	int (*listglobalvariables)(struct session *, int);
	int (*progress)(struct session *);
};
typedef struct proxy_funcs	proxy_funcs;

extern int find_proxy(char *, proxy **);

extern int proxy_init_not_imp(void *);
extern int proxy_setlinebreakpoint_not_imp(struct session *, struct procset *, char *, int, struct breakpoint *);
extern int proxy_setfuncbreakpoint_not_imp(struct session *, struct procset *, char *, char *, struct breakpoint *);
extern int proxy_deletebreakpoints_not_imp(struct session *, struct procset *, struct breakpoint *);
extern int proxy_go_not_imp(struct session *, struct procset *);
extern int proxy_step_not_imp(struct session *, struct procset *, int, int);
extern int proxy_liststackframes_not_imp(struct session *, int, stackframelist *);
extern int proxy_setcurrentstackframe_not_imp(struct session *, int, int, int, struct stackframe *);
extern int proxy_evaluateexpression_not_imp(struct session *, int, char *);
extern int proxy_listlocalvariables_not_imp(struct session *, int, struct stackframe *);
extern int proxy_listarguments_not_imp(struct session *, int, struct stackframe*);
extern int proxy_listglobalvariables_not_imp(struct session *, int);
extern int proxy_progress_not_imp(struct session *);

#endif /* _PROXY_H_*/
