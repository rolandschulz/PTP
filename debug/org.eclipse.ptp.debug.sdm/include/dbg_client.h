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

#include "bitset.h"
#include "breakpoint.h"
#include "stackframe.h"
#include "dbg_event.h"
#include "proxy.h"

/*
 * Session control
 */
int 	DbgClntInit(int, int, char *, proxy_svr_helper_funcs *, proxy_commands *);
int 	DbgClntCreateSession(int, char *, int);
int 	DbgClntStartSession(int, int, char **);
int 	DbgClntQuit(int, int, char **);
int 	DbgClntIsShutdown(void);
void 	DbgClntFinish(void);

/*
 * Breakpoint operations
 */
int 	DbgClntSetLineBreakpoint(int, int, char **);
int 	DbgClntSetFuncBreakpoint(int, int, char **);
int 	DbgClntDeleteBreakpoint(int, int, char **);
int 	DbgClntEnableBreakpoint(int, int, char **);
int 	DbgClntDisableBreakpoint(int, int, char **);
int 	DbgClntConditionBreakpoint(int, int, char **);
int 	DbgClntBreakpointAfter(int, int, char **);
int 	DbgClntSetWatchpoint(int, int, char **);

/*
 * Process control operations
 */
int 	DbgClntGo(int, int, char **);
int 	DbgClntStep(int, int, char **);
int 	DbgClntTerminate(int, int, char **);
int 	DbgClntSuspend(int, int, char **);

/*
 * Stack frame operations
 */
int 	DbgClntListStackframes(int, int, char **);
int 	DbgClntSetCurrentStackframe(int, int, char **);

/*
 * Expression/variable operations
 */
int 	DbgClntEvaluateExpression(int, int, char **);
int 	DbgClntGetType(int, int, char **);
int 	DbgClntListLocalVariables(int, int, char **);
int 	DbgClntListArguments(int, int, char **);
int 	DbgClntListGlobalVariables(int, int, char **);

/**
 * Thread operations
 */
int 	DbgClntListInfoThreads(int, int, char **);
int 	DbgClntSetThreadSelect(int, int, char **);

int 	DbgClntStackInfoDepth(int, int, char **);

/**
 * Thread operations
 */
int 	DbgClntDataReadMemory(int, int, char **);
int 	DbgClntDataWriteMemory(int, int, char **);

/*
 * Event Handling
 */
int 	DbgClntProgress(void);
void 	DbgClntRegisterReadFileHandler(int, int (*)(int, void *), void *);
void	DbgClntRegisterWriteFileHandler(int, int (*)(int, void *), void *);
void 	DbgClntRegisterExceptFileHandler(int, int (*)(int, void *), void *);
void 	DbgClntUnregisterFileHandler(int);
void 	DbgClntRegisterEventHandler(void (*)(dbg_event *, void *), void *);

/*
 * Error Handling
 */
void	DbgClntSetError(int, char *);
int		DbgClntGetError(void);
char *	DbgClntGetErrorStr(void);

/*
 * CLI Handling
 */
int		DbgClntCLIHandle(int, int, char **);

/**
 * Signal operations
 */
int 	DbgClntListSignals(int, int, char **);
int 	DbgClntSignalInfo(int, int, char **);

int 	DbgClntDataEvaluateExpression(int, int, char **);
int 	DbgClntGetPartialAIF(int, int, char **);
int 	DbgClntVariableDelete(int, int, char **);

#endif /* _DBG_CLIENT_H_ */
