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
 
#ifndef _DBG_H_
#define _DBG_H_

#include <stdarg.h>

#include "session.h"
#include "procset.h"
#include "breakpoint.h"
#include "stackframe.h"
#include "dbg_error.h"
#include "dbg_event.h"
#include "list.h"

#define DBGRES_OK			0
#define DBGRES_ERR			-1

/*
 * Process set operations
 */
void DbgCreateProcSet(int nprocs, procset **set);
void DbgDestroyProcSet(procset *set);
void DbgRemoveFromSet(procset *dst, procset *src);
void DbgAddToSet(procset *dst, procset *src);
void DbgAddProcToSet(procset *set, int pid);
void DbgRemoveProcFromSet(procset *set, int pid);

/*
 * Session control
 */
int DbgInit(session **, char *, char *, ...);
int DbgQuit(session *);

/*
 * Breakpoint operations
 */
int DbgSetLineBreakpoint(session *s, procset *set, char *file, int line);
int DbgSetFuncBreakpoint(session *s, procset *set, char *file, char *func);
int DbgDeleteBreakpoint(session *s, procset *set, int bpid);

/*
 * Process control operations
 */
int DbgGo(session *s, procset *set);
int DbgStep(session *s, procset *set, int count, int type);

/*
 * Stack frame operations
 */
int DbgListStackFrames(session *s, int proc);
int DbgSetCurrentStackFrame(session *s, int proc, int level);

/*
 * Expression/variable operations
 */
int DbgEvaluateExpression(session *s, int proc, char *exp);
int DbgListLocalVariables(session *s, int proc);
int DbgListArguments(session *s, int proc);
int DbgListGlobalVariables(session *s, int proc);

/*
 * Event Handling
 */
int DbgProgress(session *, void (*)(dbg_event*));

/*
 * Error Handling
 */
void		DbgSetError(int, char *);
int		DbgGetError(void);
char *	DbgGetErrorStr(void);

#endif /* _DBG_H_ */
