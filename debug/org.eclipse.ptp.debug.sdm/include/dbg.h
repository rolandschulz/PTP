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
#include "bitset.h"
#include "breakpoint.h"
#include "stackframe.h"
#include "memoryinfo.h"
#include "dbg_error.h"
#include "dbg_event.h"
#include "list.h"
#include "proxy.h"

#define DBGRES_OK			PROXY_RES_OK
#define DBGRES_ERR			PROXY_RES_ERR

/*
 * Session control
 */
int 	DbgInit(session **, char *, char *, ...);
int		DbgConnect(session *);
int		DbgCreate(session *);
void 	DbgRegisterEventHandler(session *, void (*)(dbg_event *, void *), void *);
int 	DbgStartSession(session *, char *, char *, char *);
int 	DbgQuit(session *);

/*
 * Breakpoint operations
 */
int		DbgSetLineBreakpoint(session *s, bitset *set, int bpid, char *file, int line);
int 	DbgSetFuncBreakpoint(session *s, bitset *set, int bpid, char *file, char *func);
int 	DbgDeleteBreakpoint(session *s, bitset *set, int bpid);

/*
 * Process control operations
 */
int 	DbgGo(session *s, bitset *set);
int 	DbgStep(session *s, bitset *set, int count, int type);
int 	DbgTerminate(session *s, bitset *set);
int 	DbgSuspend(session *s, bitset *set);

/*
 * Stack frame operations
 */
int 	DbgListStackframes(session *s, bitset *set, int low, int high);
int 	DbgSetCurrentStackframe(session *s, bitset *set, int level);

/*
 * Expression/variable operations
 */
int 	DbgEvaluateExpression(session *s, bitset *set, char *exp);
int 	DbgGetType(session *s, bitset *set, char *exp);
int 	DbgListLocalVariables(session *s, bitset *set);
int 	DbgListArguments(session *s, bitset *set, int, int);
int 	DbgListGlobalVariables(session *s, bitset *set);

/**
 * Thread operations
 */
int 	DbgListInfoThreads(session *s, bitset *set);
int 	DbgSetThreadSelect(session *s, bitset *set, int);
int		DbgStackInfoDepth(session *s, bitset *set);

/**
 * Memory operations
 */
int 	DbgDataReadMemory(session *s, bitset *set, long, char*, char*, int, int, int, char*);
int 	DbgDataWriteMemory(session *s, bitset *set, long, char*, char*, int, char*);

/*
 * Event Handling
 */
int 	DbgProgress(session *);
void 	DbgRegisterReadFileHandler(session *s, int, int (*)(int, void *), void *);
void 	DbgRegisterWriteFileHandler(session *s, int, int (*)(int, void *), void *);
void 	DbgRegisterExceptFileHandler(session *s, int, int (*)(int, void *), void *);
void	DbgUnregisterFileHandler(session *s, int);
void 	DbgRegisterEventHandler(session *s, void (*)(dbg_event *, void *), void *);

/*
 * Error Handling
 */
void	DbgSetError(int, char *);
int		DbgGetError(void);
char *	DbgGetErrorStr(void);

/*
 * CLI Handling
 */
int 	DbgCLIHandle(session *s, bitset *set, char*);

/*
 * Signal operations
 */
int 	DbgListSignals(session *s, bitset *set, char*);
int 	DbgSignalInfo(session *s, bitset *set, char*);

/*
 * New functions
 */
int 	DbgDataEvaluteExpression(session *s, bitset *set, char*);
int 	DbgVariableCreate(session *s, bitset *set, char*);
int 	DbgVariableDelete(session *s, bitset *set, char*);
int 	DbgVariableUpdate(session *s, bitset *set, char*);
int 	DbgGetPrtialAIF(session *s, bitset *set, char*, int);
int 	DbgGetAIFValue(session *s, bitset *set, char*);

#endif /* _DBG_H_ */
