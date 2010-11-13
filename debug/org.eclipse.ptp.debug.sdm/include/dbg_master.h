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
extern int 	DbgMasterInit(int, int, char *, proxy_svr_helper_funcs *, proxy_commands *);
extern int 	DbgMasterCreateSession(int, char *, int);
extern int 	DbgMasterStartSession(int, int, char **);
extern int 	DbgMasterQuit(int, int, char **);
extern int 	DbgMasterIsShutdown(void);
extern void DbgMasterFinish(void);

/*
 * Breakpoint operations
 */
extern int 	DbgMasterSetLineBreakpoint(int, int, char **);
extern int 	DbgMasterSetFuncBreakpoint(int, int, char **);
extern int 	DbgMasterDeleteBreakpoint(int, int, char **);
extern int 	DbgMasterEnableBreakpoint(int, int, char **);
extern int 	DbgMasterDisableBreakpoint(int, int, char **);
extern int 	DbgMasterConditionBreakpoint(int, int, char **);
extern int 	DbgMasterBreakpointAfter(int, int, char **);
extern int 	DbgMasterSetWatchpoint(int, int, char **);

/*
 * Process control operations
 */
extern int 	DbgMasterGo(int, int, char **);
extern int 	DbgMasterStep(int, int, char **);
extern int 	DbgMasterTerminate(int, int, char **);
extern int 	DbgMasterSuspend(int, int, char **);

/*
 * Stack frame operations
 */
extern int 	DbgMasterListStackframes(int, int, char **);
extern int 	DbgMasterSetCurrentStackframe(int, int, char **);

/*
 * Expression/variable operations
 */
extern int 	DbgMasterEvaluateExpression(int, int, char **);
extern int 	DbgMasterGetType(int, int, char **);
extern int 	DbgMasterListLocalVariables(int, int, char **);
extern int 	DbgMasterListArguments(int, int, char **);
extern int 	DbgMasterListGlobalVariables(int, int, char **);
extern int 	DbgMasterEvaluatePartialExpression(int, int, char **);
extern int 	DbgMasterDeletePartialExpression(int, int, char **);

/**
 * Thread operations
 */
extern int 	DbgMasterListInfoThreads(int, int, char **);
extern int 	DbgMasterSetThreadSelect(int, int, char **);

extern int 	DbgMasterStackInfoDepth(int, int, char **);

/**
 * Thread operations
 */
extern int 	DbgMasterDataReadMemory(int, int, char **);
extern int 	DbgMasterDataWriteMemory(int, int, char **);

/*
 * Event Handling
 */
extern int 	DbgMasterProgress(void);
extern void DbgMasterRegisterReadFileHandler(int, int (*)(int, void *), void *);
extern void	DbgMasterRegisterWriteFileHandler(int, int (*)(int, void *), void *);
extern void DbgMasterRegisterExceptFileHandler(int, int (*)(int, void *), void *);
extern void DbgMasterUnregisterFileHandler(int);
extern void DbgMasterRegisterEventHandler(void (*)(dbg_event *, void *), void *);

/*
 * Error Handling
 */
extern void		DbgMasterSetError(int, char *);
extern int		DbgMasterGetError(void);
extern char *	DbgMasterGetErrorStr(void);

/*
 * CLI Handling
 */
extern int	DbgMasterCLIHandle(int, int, char **);

/**
 * Signal operations
 */
extern int 	DbgMasterListSignals(int, int, char **);
extern int 	DbgMasterSignalInfo(int, int, char **);

#endif /* _DBG_MASTER_H_ */
