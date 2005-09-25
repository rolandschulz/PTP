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

#include <stdarg.h>
 
#include "procset.h"
#include "breakpoint.h"
#include "stackframe.h"
#include "dbg_event.h"

struct proxy_clnt_funcs {
	int (*init)(void **, char *, va_list);
	int (*connect)(void *);
	void (*regeventhandler)(void *, void (*)(dbg_event *, void *), void *);
	int (*startsession)(void *, char *, char *);
	int (*setlinebreakpoint)(void *, struct procset *, char *, int);
	int (*setfuncbreakpoint)(void *, struct procset *, char *, char *);
	int (*deletebreakpoint)(void *, struct procset *, int);
	int (*go)(void *, struct procset *);
	int (*step)(void *, struct procset *, int, int);
	int (*liststackframes)(void *, struct procset *, int);
	int (*setcurrentstackframe)(void *, struct procset *, int);
	int (*evaluateexpression)(void *, struct procset *, char *);
	int (*gettype)(void *, struct procset *, char *);
	int (*listlocalvariables)(void *, struct procset *);
	int (*listarguments)(void *, struct procset *);
	int (*listglobalvariables)(void *, struct procset *);
	int (*quit)(void *);
	int (*progress)(void *);
};
typedef struct proxy_clnt_funcs	proxy_clnt_funcs;

struct proxy_svr_helper_funcs {
	int (*newconn)(void);
	int (*numservers)(void);
	int (*shutdown_completed)(void);
	void (*regreadfile)(int, int (*)(int, void *), void *); 
	void (*unregreadfile)(int); 
	void (*regeventhandler)(void (*)(dbg_event *, void *), void *);
	int (*progress)(void);
	int (*startsession)(char *, char *);
	int (*setlinebreakpoint)(struct procset *, char *, int);
	int (*setfuncbreakpoint)(struct procset *, char *, char *);
	int (*deletebreakpoint)(struct procset *, int);
	int (*go)(struct procset *);
	int (*step)(struct procset *, int, int);
	int (*liststackframes)(struct procset *, int);
	int (*setcurrentstackframe)(struct procset *, int);
	int (*evaluateexpression)(struct procset *, char *);
	int (*gettype)(struct procset *, char *);
	int (*listlocalvariables)(struct procset *);
	int (*listarguments)(struct procset *);
	int (*listglobalvariables)(struct procset *);
	int (*quit)(void);
};
typedef struct proxy_svr_helper_funcs	proxy_svr_helper_funcs;

struct proxy_svr_funcs {
	int (*create)(struct proxy_svr_helper_funcs *, void **);
	int (*progress)(struct proxy_svr_helper_funcs *, void *);
	void (*finish)(struct proxy_svr_helper_funcs *, void *);
};
typedef struct proxy_svr_funcs	proxy_svr_funcs;

struct proxy {
	char *							name;
	struct proxy_clnt_funcs *			clnt_funcs;
	struct proxy_svr_funcs *			svr_funcs;
	struct proxy_svr_helper_funcs *	svr_helper_funcs;
};
typedef struct proxy	proxy;

extern proxy 	proxies[];

extern int	find_proxy(char *, proxy **);
extern void	proxy_svr_init(proxy *, proxy_svr_helper_funcs *);
extern int	proxy_svr_create(proxy *, void **);
extern int	proxy_svr_progress(proxy *, void *);
extern void	proxy_svr_finish(proxy *, void *);

extern int proxy_clnt_init_not_imp(void **, char *, va_list);
extern int proxy_clnt_setlinebreakpoint_not_imp(void *, struct procset *, char *, int);
extern int proxy_clnt_setfuncbreakpoint_not_imp(void *, struct procset *, char *, char *);
extern int proxy_clnt_deletebreakpoint_not_imp(void *, struct procset *, int);
extern int proxy_clnt_go_not_imp(void *, struct procset *);
extern int proxy_clnt_step_not_imp(void *, struct procset *, int, int);
extern int proxy_clnt_liststackframes_not_imp(void *, struct procset *, int);
extern int proxy_clnt_setcurrentstackframe_not_imp(void*, struct procset *, int);
extern int proxy_clnt_evaluateexpression_not_imp(void *, struct procset *, char *);
extern int proxy_clnt_gettype_not_imp(void *, struct procset *, char *);
extern int proxy_clnt_listlocalvariables_not_imp(void *, struct procset *);
extern int proxy_clnt_listarguments_not_imp(void *, struct procset *);
extern int proxy_clnt_listglobalvariables_not_imp(void *, struct procset *);
extern int proxy_clnt_quit_not_imp(void *);
extern int proxy_clnt_progress_not_imp(void *, void (*)(dbg_event *));

#endif /* _PROXY_H_*/
