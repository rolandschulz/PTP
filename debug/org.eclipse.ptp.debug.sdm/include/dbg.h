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
#include "disassembly.h"
#include "dbg_error.h"
#include "dbg_event.h"
#include "list.h"
#include "proxy.h"

#define DBGRES_OK			PTP_PROXY_RES_OK
#define DBGRES_ERR			PTP_PROXY_RES_ERR

/*
 * Session control
 */
extern int 	DbgInit(session **, int, char *[]);
extern int	DbgConnect(session *);
extern int	DbgCreate(session *);
extern void DbgRegisterEventHandler(session *, void (*)(dbg_event *, void *), void *);
extern int 	DbgStartSession(session *, char *, char *, char *);
extern int 	DbgQuit(session *);

/*
 * Breakpoint operations
 */
extern int	DbgSetLineBreakpoint(session *s, bitset *set, int bpid, char *file, int line);
extern int 	DbgSetFuncBreakpoint(session *s, bitset *set, int bpid, char *file, char *func);
extern int 	DbgDeleteBreakpoint(session *s, bitset *set, int bpid);

/*
 * Process control operations
 */
extern int 	DbgGo(session *s, bitset *set);
extern int 	DbgStep(session *s, bitset *set, int count, int type);
extern int 	DbgTerminate(session *s, bitset *set);
extern int 	DbgSuspend(session *s, bitset *set);

/*
 * Stack frame operations
 */
extern int 	DbgListStackframes(session *s, bitset *set, int low, int high);
extern int 	DbgSetCurrentStackframe(session *s, bitset *set, int level);

/*
 * Expression/variable operations
 */
extern int 	DbgEvaluateExpression(session *s, bitset *set, char *exp);
extern int 	DbgGetType(session *s, bitset *set, char *exp);
extern int 	DbgListLocalVariables(session *s, bitset *set);
extern int 	DbgListArguments(session *s, bitset *set, int, int);
extern int 	DbgListGlobalVariables(session *s, bitset *set);

/**
 * Thread operations
 */
extern int 	DbgListInfoThreads(session *s, bitset *set);
extern int 	DbgSetThreadSelect(session *s, bitset *set, int);
extern int	DbgStackInfoDepth(session *s, bitset *set);

/**
 * Memory operations
 */
extern int 	DbgDataReadMemory(session *s, bitset *set, long, char*, char*, int, int, int, char*);
extern int 	DbgDataWriteMemory(session *s, bitset *set, long, char*, char*, int, char*);

/*
 * Event Handling
 */
extern int 	DbgProgress(session *);
extern void DbgRegisterReadFileHandler(session *s, int, int (*)(int, void *), void *);
extern void DbgRegisterWriteFileHandler(session *s, int, int (*)(int, void *), void *);
extern void DbgRegisterExceptFileHandler(session *s, int, int (*)(int, void *), void *);
extern void	DbgUnregisterFileHandler(session *s, int);
extern void DbgRegisterEventHandler(session *s, void (*)(dbg_event *, void *), void *);

/*
 * Error Handling
 */
extern void		DbgSetError(int, char *);
extern int		DbgGetError(void);
extern char *	DbgGetErrorStr(void);

/*
 * CLI Handling
 */
extern int 	DbgCLIHandle(session *s, bitset *set, char*);

/*
 * Signal operations
 */
extern int 	DbgListSignals(session *s, bitset *set, char*);
extern int 	DbgSignalInfo(session *s, bitset *set, char*);

/*
 * New functions
 */
extern int 	DbgDeletePartialExpression(session *s, bitset *set, char*);
extern int 	DbgEvaluatePartialExpression(session *s, bitset *set, char*, char *, int, int);

#endif /* _DBG_H_ */
