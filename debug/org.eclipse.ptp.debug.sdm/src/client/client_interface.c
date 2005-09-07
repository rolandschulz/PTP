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

/*
 * Process set operations
 */
void DbgCreateProcSet(procset *set);
void DbgDestroyProcSet(procset *set);
void DbgRemoveFromSet(procset *dst, procset *src);
void DbgAddToSet(procset *dst, procset *src);
void DbgAddProcToSet(procset *set, int pid);
void DbgRemoveProcFromSet(procset *set, int pid);

/*
 * Session initialization
 */
int
DbgInit(char *proxy, void *data, session **s)
{
	*s = malloc(sizeof(session));
	if (find_proxy(proxy, &(*s)->sess_proxy) < 0) {
		free(*s);
		return -1;
	}
	(*s)->sess_proxy_data = data;
	if ((*s)->sess_proxy->funcs->init(data) < 0) {
		free(*s);
		return -1;
	}
	return 0;
}

/*
 * Breakpoint operations
 */
int 
DbgSetLineBreakpoint(session *s, procset *set, char *file, int line, breakpoint *bp)
{
	return s->sess_proxy->funcs->setlinebreakpoint(s, set, file, line, bp);
}

int 
DbgSetFuncBreakpoint(session *s, procset *set, char *file, char *func, breakpoint *bp)
{
	return s->sess_proxy->funcs->setfuncbreakpoint(s, set, file, func, bp);
}

int 
DbgDeleteBreakpoints(session *s, procset *set, breakpoint *bp)
{
	return s->sess_proxy->funcs->deletebreakpoints(s, set, bp);
}

/*
 * Process control operations
 */
int 
DbgGo(session *s, procset *set)
{
	return s->sess_proxy->funcs->go(s, set);
}

int 
DbgStep(session *s, procset *set, int count, int type)
{
	return s->sess_proxy->funcs->step(s, set, count, type);
}

/*
 * Stack frame operations
 */
int 
DbgListStackframes(session *s, int proc, stackframelist *frame)
{
	return s->sess_proxy->funcs->liststackframes(s, proc, frame);
}

int 
DbgSetCurrentStackframe(session *s, int proc, int count, int dir, stackframe *frame)
{
	return s->sess_proxy->funcs->setcurrentstackframe(s, proc, count, dir, frame);
}

/*
 * Expression/variable operations
 */
int 
DbgEvaluateExpression(session *s, int proc, char *exp)
{
	return s->sess_proxy->funcs->evaluateexpression(s, proc, exp);
}

int 
DbgListLocalVariables(session *s, int proc, stackframe *frame)
{
	return s->sess_proxy->funcs->listlocalvariables(s, proc, frame);
}

int 
DbgListArguments(session *s, int proc, stackframe *frame)
{
	return s->sess_proxy->funcs->listarguments(s, proc, frame);
}

int 
DbgListGlobalVariables(session *s, int proc)
{
	return s->sess_proxy->funcs->listglobalvariables(s, proc);
}

/*
 * Event handling
 */
int
DbgProgress(session *s)
{
	return s->sess_proxy->funcs->progress(s);
}
