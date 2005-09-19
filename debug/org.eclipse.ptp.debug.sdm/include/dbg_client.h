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
 
#ifndef _DBG_CLIENT_H_
#define _DBG_CLIENT_H_

#include "procset.h"
#include "breakpoint.h"
#include "stackframe.h"
#include "dbg_event.h"

#define READ_FILE_HANDLER		1
#define WRITE_FILE_HANDLER	2
#define EXCEPT_FILE_HANDLER	4

/*
 * Session control
 */
int DbgClntQuit(void);

/*
 * Breakpoint operations
 */
int DbgClntSetLineBreakpoint(procset *set, char *file, int line);
int DbgClntSetFuncBreakpoint(procset *set, char *file, char *func);
int DbgClntDeleteBreakpoint(procset *set);

/*
 * Process control operations
 */
int DbgClntGo(procset *set);
int DbgClntStep(procset *set, int count, int type);

/*
 * Stack frame operations
 */
int DbgClntListStackFrames(int proc);
int DbgClntSetCurrentStackFrame(int proc, int count, int dir);

/*
 * Expression/variable operations
 */
int DbgClntEvaluateExpression(int proc);
int DbgClntListLocalVariables(int proc);
int DbgClntListArguments(int proc);
int DbgClntListGlobalVariables(int proc);

/*
 * Event Handling
 */
int DbgClntProgress(void);
void DbgClntRegisterFileHandler(int, int, int (*)(int, void *), void *);
void DbgClntUnregisterFileHandler(int);

/*
 * Error Handling
 */
void		DbgClntSetError(int, char *);
int		DbgClntGetError(void);
char *	DbgClntGetErrorStr(void);

#endif /* _DBG_CLIENT_H_ */
