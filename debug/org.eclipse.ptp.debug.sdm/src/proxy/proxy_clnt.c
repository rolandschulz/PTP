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

/*
 * Miscellaneous proxy functions.
 */
 
#include <stdio.h>

#include "session.h"
#include "proxy.h"
#include "proxy_tcp.h"
#include "dbg.h"
 
proxy proxies[] = {
	{"tcp", &proxy_tcp_clnt_funcs, NULL, NULL, NULL},
	{NULL, NULL, NULL, NULL, NULL}
};

int 
proxy_clnt_init(proxy *p, proxy_clnt_helper_funcs *funcs, void **data, char *attr, va_list ap)
{
	if (p != NULL)
		return p->clnt_funcs->init(funcs, data, attr, ap);
		
	return -1;
}

int 
proxy_clnt_connect(proxy *p, void *data)
{
	if (p != NULL)
		return p->clnt_funcs->connect(data);
		
	return -1;
}

int 
proxy_clnt_create(proxy *p, void *data)
{
	if (p != NULL)
		return p->clnt_funcs->create(data);
		
	return -1;
}

int
proxy_clnt_progress(proxy *p, void *data)
{
	if (p != NULL)
		return p->clnt_funcs->progress(data);
		
	return -1;
}

int
proxy_clnt_startsession(proxy *p, void *data, char *prog, char *args)
{
	if (p != NULL)
		return p->clnt_funcs->startsession(data, prog, args);
		
	return -1;
}

int 
proxy_clnt_setlinebreakpoint(proxy *p, void *data, procset *set, int bpid, char *file, int line)
{
	if (p != NULL)
		return p->clnt_funcs->setlinebreakpoint(data, set, bpid, file, line);
		
	return -1;
}

int 
proxy_clnt_setfuncbreakpoint(proxy *p, void *data, procset *set, int bpid, char *file, char *func)
{
	if (p != NULL)
		return p->clnt_funcs->setfuncbreakpoint(data, set, bpid, file, func);
		
	return -1;
}

int 
proxy_clnt_deletebreakpoint(proxy *p, void *data, procset *set, int bpid)
{
	if (p != NULL)
		return p->clnt_funcs->deletebreakpoint(data, set, bpid);
		
	return -1;
}

int
proxy_clnt_go(proxy *p, void *data, procset *set)
{
	if (p != NULL)
		return p->clnt_funcs->go(data, set);
		
	return -1;
}

int 
proxy_clnt_step(proxy *p, void *data, procset *set, int count, int type)
{
	if (p != NULL)
		return p->clnt_funcs->step(data, set, count, type);
		
	return -1;
}

int
proxy_clnt_terminate(proxy *p, void *data, procset *set)
{
	if (p != NULL)
		return p->clnt_funcs->terminate(data, set);
		
	return -1;
}

int 
proxy_clnt_liststackframes(proxy *p, void *data, procset *set, int current)
{
	if (p != NULL)
		return p->clnt_funcs->liststackframes(data, set, current);
		
	return -1;
}

int 
proxy_clnt_setcurrentstackframe(proxy *p, void *data, procset *set, int level)
{
	if (p != NULL)
		return p->clnt_funcs->setcurrentstackframe(data, set, level);
		
	return -1;
}

int 
proxy_clnt_evaluateexpression(proxy *p, void *data, procset *set, char *expr)
{
	if (p != NULL)
		return p->clnt_funcs->evaluateexpression(data, set, expr);
		
	return -1;
}

int 
proxy_clnt_gettype(proxy *p, void *data, procset *set, char *expr)
{
	if (p != NULL)
		return p->clnt_funcs->gettype(data, set, expr);
		
	return -1;
}

int 
proxy_clnt_listlocalvariables(proxy *p, void *data, procset *set)
{
	if (p != NULL)
		return p->clnt_funcs->listlocalvariables(data, set);
		
	return -1;
}

int 
proxy_clnt_listarguments(proxy *p, void *data, procset *set)
{
	if (p != NULL)
		return p->clnt_funcs->listarguments(data, set);
		
	return -1;
}

int 
proxy_clnt_listglobalvariables(proxy *p, void *data, procset *set)
{
	if (p != NULL)
		return p->clnt_funcs->listglobalvariables(data, set);
		
	return -1;
}

int 
proxy_clnt_quit(proxy *p, void *data)
{
	if (p != NULL)
		return p->clnt_funcs->quit(data);
		
	return -1;
}
