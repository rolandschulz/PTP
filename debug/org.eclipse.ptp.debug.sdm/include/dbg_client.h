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
#include "proxy.h"

/*
 * Session control
 */
int DbgClntInit(int, char *, proxy_svr_helper_funcs *);
int DbgClntCreateSession(char *, int);
int DbgClntStartSession(char *, char *);
int DbgClntQuit(void);
int DbgClntIsShutdown(void);
void DbgClntFinish(void);

/*
 * Breakpoint operations
 */
int DbgClntSetLineBreakpoint(procset *set, int bpid, char *file, int line);
int DbgClntSetFuncBreakpoint(procset *set, int bpid, char *file, char *func);
int DbgClntDeleteBreakpoint(procset *set, int);

/*
 * Process control operations
 */
int DbgClntGo(procset *set);
int DbgClntStep(procset *set, int count, int type);
int DbgClntTerminate(procset *set);

/*
 * Stack frame operations
 */
int DbgClntListStackframes(procset *set, int);
int DbgClntSetCurrentStackframe(procset *set, int level);

/*
 * Expression/variable operations
 */
int DbgClntEvaluateExpression(procset *set, char *);
int DbgClntGetType(procset *set, char *);
int DbgClntListLocalVariables(procset *set);
int DbgClntListArguments(procset *set);
int DbgClntListGlobalVariables(procset *set);

/*
 * Event Handling
 */
int DbgClntProgress(void);
void DbgClntRegisterReadFileHandler(int, int (*)(int, void *), void *);
void DbgClntRegisterWriteFileHandler(int, int (*)(int, void *), void *);
void DbgClntRegisterExceptFileHandler(int, int (*)(int, void *), void *);
void DbgClntUnregisterFileHandler(int);
void DbgClntRegisterEventHandler(void (*)(dbg_event *, void *), void *);

/*
 * Error Handling
 */
void		DbgClntSetError(int, char *);
int		DbgClntGetError(void);
char *	DbgClntGetErrorStr(void);

#endif /* _DBG_CLIENT_H_ */
