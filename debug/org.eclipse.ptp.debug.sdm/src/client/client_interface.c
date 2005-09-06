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
session * 
DbgInit(void)
{
	return 0;
}

/*
 * Breakpoint operations
 */
int 
DbgSetLineBreakpoint(session *s, procset *set, char *file, int line, breakpoint *bp)
{
	return s->proxy.setlinebreakpoint(s, set, file, line, bp);
}

int 
DbgSetFuncBreakpoint(session *s, procset *set, char *file, char *func, breakpoint *bp)
{
	return s->proxy.setfuncbreakpoint(s, set, file, func, bp);
}

int 
DbgDeleteBreakpoints(session *s, procset *set, breakpoint *bp)
{
	return s->proxy.deletebreakpoints(s, set, bp);
}

/*
 * Process control operations
 */
int 
DbgGo(session *s, procset *set)
{
	return s->proxy.go(s, set);
}

int 
DbgStep(session *s, procset *set, int count, int type)
{
	return s->proxy.step(s, set, count, type);
}

/*
 * Stack frame operations
 */
int 
DbgListStackframes(session *s, int proc, stackframelist *frame)
{
	return s->proxy.liststackframes(s, proc, frame);
}

int 
DbgSetCurrentStackframe(session *s, int proc, int count, int dir, stackframe *frame)
{
	return s->proxy.setcurrentstackframe(s, proc, count, dir, frame);
}

/*
 * Expression/variable operations
 */
int 
DbgEvaluateExpression(session *s, int proc, char *exp)
{
	return s->proxy.evaluateexpression(s, proc, exp);
}

int 
DbgListLocalVariables(session *s, int proc, stackframe *frame)
{
	return s->proxy.listlocalvariables(s, proc, frame);
}

int 
DbgListArguments(session *s, int proc, stackframe *frame)
{
	return s->proxy.listarguments(s, proc, frame);
}

int 
DbgListGlobalVariables(session *s, int proc)
{
	return s->proxy.listglobalvariables(s, proc);
}

/*
 * Event handling
 */
int
DbgProgress(session *s)
{
	return s->proxy.progress(s);
}
