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
#include <string.h>

#include "dbg.h"
#include "session.h"
#include "proxy.h"
#include "procset.h"

/*
 * Process set operations
 */
void 
DbgCreateProcSet(int nprocs, procset **set)
{
	*set = procset_new(nprocs);
}

void 
DbgDestroyProcSet(procset *set)
{
	procset_free(set);
}

void DbgRemoveFromSet(procset *dst, procset *src);
void DbgAddToSet(procset *dst, procset *src);

void
DbgAddProcToSet(procset *set, int pid)
{
	procset_add_proc(set, pid);
}

void DbgRemoveProcFromSet(procset *set, int pid);

/*
 * Session initialization
 */
int
DbgInit(session **s, char *proxy, char *attr, ...)
{
	va_list	ap;
	void *	data;
	int		res;
	
	*s = malloc(sizeof(session));
	if (find_proxy(proxy, &(*s)->sess_proxy) < 0) {
		free(*s);
		return -1;
	}
	va_start(ap, attr);
	res = (*s)->sess_proxy->clnt_funcs->init(&data, attr, ap);
	va_end(ap);
	
	if (res < 0) {
		free(*s);
		return -1;
	}
	(*s)->sess_proxy_data = data;
	return 0;
}

/*
 * Breakpoint operations
 */
int 
DbgSetLineBreakpoint(session *s, procset *set, char *file, int line)
{
	return s->sess_proxy->clnt_funcs->setlinebreakpoint(s->sess_proxy_data, set, file, line);
}

int 
DbgSetFuncBreakpoint(session *s, procset *set, char *file, char *func)
{
	return s->sess_proxy->clnt_funcs->setfuncbreakpoint(s->sess_proxy_data, set, file, func);
}

int 
DbgDeleteBreakpoints(session *s, procset *set, int bpid)
{
	return s->sess_proxy->clnt_funcs->deletebreakpoint(s->sess_proxy_data, set, bpid);
}

/*
 * Process control operations
 */
int 
DbgGo(session *s, procset *set)
{
	return s->sess_proxy->clnt_funcs->go(s->sess_proxy_data, set);
}

int 
DbgStep(session *s, procset *set, int count, int type)
{
	return s->sess_proxy->clnt_funcs->step(s->sess_proxy_data, set, count, type);
}

/*
 * Stack frame operations
 */
int 
DbgListStackframes(session *s, int proc)
{
	return s->sess_proxy->clnt_funcs->liststackframes(s->sess_proxy_data, proc);
}

int 
DbgSetCurrentStackframe(session *s, int proc, int level)
{
	return s->sess_proxy->clnt_funcs->setcurrentstackframe(s->sess_proxy_data, proc, level);
}

/*
 * Expression/variable operations
 */
int 
DbgEvaluateExpression(session *s, int proc, char *exp)
{
	return s->sess_proxy->clnt_funcs->evaluateexpression(s->sess_proxy_data, proc, exp);
}

int 
DbgListLocalVariables(session *s, int proc)
{
	return s->sess_proxy->clnt_funcs->listlocalvariables(s->sess_proxy_data, proc);
}

int 
DbgListArguments(session *s, int proc)
{
	return s->sess_proxy->clnt_funcs->listarguments(s->sess_proxy_data, proc);
}

int 
DbgListGlobalVariables(session *s, int proc)
{
	return s->sess_proxy->clnt_funcs->listglobalvariables(s->sess_proxy_data, proc);
}

int 
DbgQuit(session *s)
{
	return s->sess_proxy->clnt_funcs->quit(s->sess_proxy_data);
}

/*
 * Event handling
 */
int
DbgProgress(session *s, void (*event_callback)(dbg_event *))
{
	return s->sess_proxy->clnt_funcs->progress(s->sess_proxy_data, event_callback);
}