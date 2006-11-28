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
int 	DbgClntInit(int, int, char *, proxy_handler_funcs *, proxy_svr_helper_funcs *, proxy_svr_commands *);
int 	DbgClntCreateSession(int, char *, int);
int 	DbgClntStartSession(char **);
int 	DbgClntQuit(char **);
int 	DbgClntIsShutdown(void);
void 	DbgClntFinish(void);

/*
 * Breakpoint operations
 */
int 	DbgClntSetLineBreakpoint(char **);
int 	DbgClntSetFuncBreakpoint(char **);
int 	DbgClntDeleteBreakpoint(char **);
int 	DbgClntEnableBreakpoint(char **);
int 	DbgClntDisableBreakpoint(char **);
int 	DbgClntConditionBreakpoint(char **);
int 	DbgClntBreakpointAfter(char **);
int 	DbgClntSetWatchpoint(char **);

/*
 * Process control operations
 */
int 	DbgClntGo(char **);
int 	DbgClntStep(char **);
int 	DbgClntTerminate(char **);
int 	DbgClntSuspend(char **);

/*
 * Stack frame operations
 */
int 	DbgClntListStackframes(char **);
int 	DbgClntSetCurrentStackframe(char **);

/*
 * Expression/variable operations
 */
int 	DbgClntEvaluateExpression(char **);
int 	DbgClntGetType(char **);
int 	DbgClntListLocalVariables(char **);
int 	DbgClntListArguments(char **);
int 	DbgClntListGlobalVariables(char **);

/**
 * Thread operations
 */
int 	DbgClntListInfoThreads(char **);
int 	DbgClntSetThreadSelect(char **);

int 	DbgClntStackInfoDepth(char **);

/**
 * Thread operations
 */
int 	DbgClntDataReadMemory(char **);
int 	DbgClntDataWriteMemory(char **);

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
int		DbgClntCLIHandle(char **);

/**
 * Signal operations
 */
int 	DbgClntListSignals(char **);
int 	DbgClntSignalInfo(char **);

int 	DbgClntDataEvaluateExpression(char **);
int 	DbgClntGetPartialAIF(char **);
int 	DbgClntVariableDelete(char **);

#endif /* _DBG_CLIENT_H_ */
