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
 
#ifndef _DBG_MASTER_H_
#define _DBG_MASTER_H_

#include "bitset.h"
#include "breakpoint.h"
#include "stackframe.h"
#include "dbg_event.h"
#include "proxy.h"

/*
 * Session control
 */
int 	DbgMasterInit(int, int, char *, proxy_svr_helper_funcs *, proxy_commands *);
int 	DbgMasterCreateSession(int, char *, int);
int 	DbgMasterStartSession(int, int, char **);
int 	DbgMasterQuit(int, int, char **);
int 	DbgMasterIsShutdown(void);
void 	DbgMasterFinish(void);

/*
 * Breakpoint operations
 */
int 	DbgMasterSetLineBreakpoint(int, int, char **);
int 	DbgMasterSetFuncBreakpoint(int, int, char **);
int 	DbgMasterDeleteBreakpoint(int, int, char **);
int 	DbgMasterEnableBreakpoint(int, int, char **);
int 	DbgMasterDisableBreakpoint(int, int, char **);
int 	DbgMasterConditionBreakpoint(int, int, char **);
int 	DbgMasterBreakpointAfter(int, int, char **);
int 	DbgMasterSetWatchpoint(int, int, char **);

/*
 * Process control operations
 */
int 	DbgMasterGo(int, int, char **);
int 	DbgMasterStep(int, int, char **);
int 	DbgMasterTerminate(int, int, char **);
int 	DbgMasterSuspend(int, int, char **);

/*
 * Stack frame operations
 */
int 	DbgMasterListStackframes(int, int, char **);
int 	DbgMasterSetCurrentStackframe(int, int, char **);

/*
 * Expression/variable operations
 */
int 	DbgMasterEvaluateExpression(int, int, char **);
int 	DbgMasterGetType(int, int, char **);
int 	DbgMasterListLocalVariables(int, int, char **);
int 	DbgMasterListArguments(int, int, char **);
int 	DbgMasterListGlobalVariables(int, int, char **);
int 	DbgMasterEvaluatePartialExpression(int, int, char **);
int 	DbgMasterDeletePartialExpression(int, int, char **);

/**
 * Thread operations
 */
int 	DbgMasterListInfoThreads(int, int, char **);
int 	DbgMasterSetThreadSelect(int, int, char **);

int 	DbgMasterStackInfoDepth(int, int, char **);

/**
 * Thread operations
 */
int 	DbgMasterDataReadMemory(int, int, char **);
int 	DbgMasterDataWriteMemory(int, int, char **);

/*
 * Event Handling
 */
int 	DbgMasterProgress(void);
void 	DbgMasterRegisterReadFileHandler(int, int (*)(int, void *), void *);
void	DbgMasterRegisterWriteFileHandler(int, int (*)(int, void *), void *);
void 	DbgMasterRegisterExceptFileHandler(int, int (*)(int, void *), void *);
void 	DbgMasterUnregisterFileHandler(int);
void 	DbgMasterRegisterEventHandler(void (*)(dbg_event *, void *), void *);

/*
 * Error Handling
 */
void	DbgMasterSetError(int, char *);
int		DbgMasterGetError(void);
char *	DbgMasterGetErrorStr(void);

/*
 * CLI Handling
 */
int		DbgMasterCLIHandle(int, int, char **);

/**
 * Signal operations
 */
int 	DbgMasterListSignals(int, int, char **);
int 	DbgMasterSignalInfo(int, int, char **);

#endif /* _DBG_MASTER_H_ */
