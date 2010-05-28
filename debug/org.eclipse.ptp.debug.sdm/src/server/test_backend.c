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

#ifdef __gnu_linux__
#define _GNU_SOURCE
#endif /* __gnu_linux__ */

#include "config.h"

#include <sys/types.h>
#include <sys/stat.h>
#include <sys/wait.h>

#include <stdlib.h>
#include <unistd.h>
#include <string.h>
#include <errno.h>
#include <fcntl.h>

#include "dbg.h"
#include "dbg_event.h"
#include "backend.h"
#include "list.h"

static dbg_event *	LastEvent;
static void			(*EventCallback)(dbg_event *);
static int			ServerExit;
static int			Started;

static int	TestInit(void (*)(dbg_event *));
static int	TestProgress(void);
static int	TestInterrupt(void);
static int	TestStartSession(char *, char *, char *, char *, char **, char **, long);
static int	TestSetLineBreakpoint(int, int, int, char *, int, char*, int, int);
static int	TestSetFuncBreakpoint(int, int, int, char *, char *, char*, int, int);
static int	TestDeleteBreakpoint(int);
static int	TestEnableBreakpoint(int);
static int	TestDisableBreakpoint(int);
static int	TestConditionBreakpoint(int, char *expr);
static int	TestBreakpointAfter(int, int icount);
static int	TestWatchpoint(int, char *, int, int, char *, int);
static int	TestGo(void);
static int	TestStep(int, int);
static int	TestTerminate(void);
static int	TestListStackframes(int, int);
static int	TestSetCurrentStackframe(int);
static int	TestEvaluateExpression(char *);
static int	TestGetNativeType(char *);
static int	TestGetLocalVariables(void);
static int	TestListArguments(int, int);
static int	TestGetInfoThread(void);
static int	TestSetThreadSelect(int);
static int	TestStackInfoDepth(void);
static int	TestDataReadMemory(long, char*, char*, int, int, int, char*);
static int	TestDataWriteMemory(long, char*, char*, int, char*);
static int	TestGetGlobalVariables(void);
static int	TestListSignals(char*);
static int	TestSignalInfo(char*);
static int	TestHandle(char*);
static int	TestQuit(void);
static int	TestDataEvaluateExpression(char*);
static int	TestGetPartialAIF(char *, char *, int, int);
static int	TestVarDelete(char*);

static int	SetAndCheckBreak(int, int, int, char *, char *, int, int);

dbg_backend_funcs	TestBackend =
{
	TestInit,
	TestProgress,
	TestInterrupt,
	TestStartSession,
	TestSetLineBreakpoint,
	TestSetFuncBreakpoint,
	TestDeleteBreakpoint,
	TestEnableBreakpoint,
	TestDisableBreakpoint,
	TestConditionBreakpoint,
	TestBreakpointAfter,
	TestWatchpoint,
	TestGo,
	TestStep,
	TestTerminate,
	TestListStackframes,
	TestSetCurrentStackframe,
	TestEvaluateExpression,
	TestGetNativeType,
	TestGetLocalVariables,
	TestListArguments,
	TestGetGlobalVariables,
	TestGetInfoThread,
	TestSetThreadSelect,
	TestStackInfoDepth,
	TestDataReadMemory,
	TestDataWriteMemory,
	TestListSignals,
	TestSignalInfo,
	TestHandle,
	TestDataEvaluateExpression,
	TestGetPartialAIF,
	TestVarDelete,
	TestQuit
};

static void
SaveEvent(dbg_event *e)
{
	if (LastEvent != NULL)
		FreeDbgEvent(LastEvent);
		
	LastEvent = e;
}

/*
 * Initialize Test
 */
static int
TestInit(void (*event_callback)(dbg_event *))
{
	EventCallback = event_callback;
	LastEvent = NULL;
	ServerExit = 0;
		
	signal(SIGTERM, SIG_IGN);
	signal(SIGHUP, SIG_IGN);
	signal(SIGINT, SIG_IGN);

	return DBGRES_OK;
}

/*
 * Start Test session
 */	
static int
TestStartSession(char *test_path, char *prog, char *path, char *work_dir, char **args, char **env, long timeout)
{
	Started = 0;
	SaveEvent(NewDbgEvent(DBGEV_OK));
	return DBGRES_OK;
}

/*
 * Progress test commands.
 * 
 * TODO: Deal with errors
 * 
 * @return	-1	server shutdown
 * 			1	event callback
 * 			0	completed operation
 */
static int	
TestProgress(void)
{
	int				res = 0;
	
	/*
	 * Check for existing events
	 */
	if (LastEvent != NULL) {
		if (EventCallback != NULL) {
			EventCallback(LastEvent);
			res = 1;
		}
			
		if (ServerExit && LastEvent->event_id == DBGEV_OK) {
			res = -1;
		}
			
		FreeDbgEvent(LastEvent);
		LastEvent = NULL;
		return res;
	}
	
	return 0;
}

/*
** Set breakpoint at specified line.
*/
static int
TestSetLineBreakpoint(int bpid, int isTemp, int isHard, char *file, int line, char *condition, int ignoreCount, int tid)
{
	int		res;
	char*	where;
	res = SetAndCheckBreak(bpid, isTemp, isHard, where, condition, ignoreCount, tid);
	return res;
}

/*
** Set breakpoint at start of specified function.
*/
static int
TestSetFuncBreakpoint(int bpid, int isTemp, int isHard, char *file, char *func, char *condition, int ignoreCount, int tid)
{
	int		res;
	char*	where;
	res = SetAndCheckBreak(bpid, isTemp, isHard, where, condition, ignoreCount, tid);
	return res;
}

/*
** Check that breakpoint command has succeded and
** extract appropriate information. Returns breakpoint
** id in bid. Adds to breakpoint list if necessary.
*/
static int
SetAndCheckBreak(int bpid, int isTemp, int isHard, char *where, char *condition, int ignoreCount, int tid)
{
	/*
	dbg_event *		e;
	e = NewDbgEvent(DBGEV_BPSET);
	e->dbg_event_u.bpset_event.bpid = bpid;
	e->dbg_event_u.bpset_event.bp = MIBreakpointNew();
	SaveEvent(e);
	*/
	SaveEvent(NewDbgEvent(DBGEV_OK));
	return DBGRES_OK;
}

/*
** Delete a breakpoint.
*/
static int
TestDeleteBreakpoint(int bpid)
{
	SaveEvent(NewDbgEvent(DBGEV_OK));
	return DBGRES_OK;
}

/*
** Enable a breakpoint.
*/
static int
TestEnableBreakpoint(int bpid)
{
	SaveEvent(NewDbgEvent(DBGEV_OK));
	return DBGRES_OK;
}

/*
** Disable a breakpoint.
*/
static int
TestDisableBreakpoint(int bpid)
{
	SaveEvent(NewDbgEvent(DBGEV_OK));
	return DBGRES_OK;
}

/*
** Condition a breakpoint.
*/
static int
TestConditionBreakpoint(int bpid, char *expr)
{
	SaveEvent(NewDbgEvent(DBGEV_OK));
	return DBGRES_OK;
}

/*
** breakpoint after.
*/
static int
TestBreakpointAfter(int bpid, int icount)
{
	SaveEvent(NewDbgEvent(DBGEV_OK));
	return DBGRES_OK;
}

/*
 * Set watch point
 */
static int 
TestWatchpoint(int bpid, char *expr, int isAccess, int isRead, char *condition, int ignoreCount) 
{
	/*
	dbg_event *		e;
	e = NewDbgEvent(DBGEV_BPSET);
	e->dbg_event_u.bpset_event.bpid = bpid;
	e->dbg_event_u.bpset_event.bp = MIBreakpointNew();
	SaveEvent(e);
	*/
	SaveEvent(NewDbgEvent(DBGEV_OK));
	return DBGRES_OK;
}

/*
** Start/continue executing program. 
*/
static int
TestGo(void)
{
	//SendCommandWait(DebugSession, cmd);
	return DBGRES_OK;
}

/*
 * Execute count statements. 
 * 
 * type	step kind
 * 0		enter function calls
 * 1		do not enter function calls
 * 2		step out of function (count ignored)
 */
static int
TestStep(int count, int type)
{
	//SendCommandWait(DebugSession, cmd);
	return DBGRES_OK;
}

/*
** Terminate program execution.
*/
static int
TestTerminate(void)
{
	SaveEvent(NewDbgEvent(DBGEV_OK));
	return DBGRES_OK;
}

/*
** Interrupt an executing program.
*/
static int
TestInterrupt(void)
{
	//SendCommandWait(DebugSession, cmd);
	return DBGRES_OK;
}

/*
** Move up or down count stack frames.
*/
static int
TestSetCurrentStackframe(int level)
{
	SaveEvent(NewDbgEvent(DBGEV_OK));
	return DBGRES_OK;
}

/*
** List current or all stack frames.
*/
static int
TestListStackframes(int low, int high)
{
	/*
	dbg_event *	e;
	e = NewDbgEvent(DBGEV_FRAMES);
	e->dbg_event_u.list = frames;	
	SaveEvent(e);
	*/
	SaveEvent(NewDbgEvent(DBGEV_OK));
	return DBGRES_OK;
}

/*
** List local variables.
*/
static int
TestGetLocalVariables(void)
{
	/*
	dbg_event *	e;
	e = NewDbgEvent(DBGEV_VARS);
	e->dbg_event_u.list = NewList();
	SaveEvent(e);
	*/
	SaveEvent(NewDbgEvent(DBGEV_OK));
	return DBGRES_OK;
}

/*
** List arguments.
*/
static int
TestListArguments(int low, int high)
{
	/*
	dbg_event *	e;
	e = NewDbgEvent(DBGEV_ARGS);
	e->dbg_event_u.list = NewList();
	SaveEvent(e);
	*/
	SaveEvent(NewDbgEvent(DBGEV_OK));
	return DBGRES_OK;
}

/*
** List global variables.
*/
static int
TestGetGlobalVariables(void)
{
	DbgSetError(DBGERR_NOTIMP, NULL);
	return DBGRES_ERR;
}

/*
** Quit debugger.
*/
static int
TestQuit(void)
{
	SaveEvent(NewDbgEvent(DBGEV_OK));
	ServerExit++;
	return DBGRES_OK;
}

static int
TestGetInfoThread(void) 
{
	/*
	dbg_event *	e;
	e = NewDbgEvent(DBGEV_THREADS);
	e->dbg_event_u.threads_event.thread_id = info->current_thread_id;
	e->dbg_event_u.threads_event.list = NewList();
	SaveEvent(e);
	*/
	SaveEvent(NewDbgEvent(DBGEV_OK));
	return DBGRES_OK;
}

static int 
TestSetThreadSelect(int threadNum) 
{
	/*
	dbg_event *	e;
	e = NewDbgEvent(DBGEV_THREAD_SELECT);
	e->dbg_event_u.thread_select_event.thread_id = info->current_thread_id;
	e->dbg_event_u.thread_select_event.frame = s;
	SaveEvent(e);
	*/
	SaveEvent(NewDbgEvent(DBGEV_OK));
	return DBGRES_OK;
}

static int 
TestStackInfoDepth() 
{
	/*
	dbg_event *	e;
	e = NewDbgEvent(DBGEV_STACK_DEPTH);
	e->dbg_event_u.stack_depth = depth;
	SaveEvent(e);
	*/
	SaveEvent(NewDbgEvent(DBGEV_OK));
	return DBGRES_OK;
}

static int 
TestDataReadMemory(long offset, char* address, char* format, int wordSize, int rows, int cols, char* asChar) 
{
	/*
	dbg_event *	e;
	e = NewDbgEvent(DBGEV_DATAR_MEM);
	e->dbg_event_u.meminfo = meminfo;
	SaveEvent(e);
	*/
	SaveEvent(NewDbgEvent(DBGEV_OK));	
	return DBGRES_OK;
}

static int 
TestDataWriteMemory(long offset, char* address, char* format, int wordSize, char* value) 
{
//TODO
	SaveEvent(NewDbgEvent(DBGEV_OK));
	return DBGRES_OK;
}

static int 
TestListSignals(char* name) 
{
	/*
	dbg_event *	e;
	e = NewDbgEvent(DBGEV_SIGNALS);
	e->dbg_event_u.list = signals;
	SaveEvent(e);
	*/
	SaveEvent(NewDbgEvent(DBGEV_OK));
	return DBGRES_OK;
}

static int 
TestSignalInfo(char* arg) 
{
	//SendCommandWait(DebugSession, cmd);
	return DBGRES_OK;
}

static int
TestHandle(char *arg)
{
	SaveEvent(NewDbgEvent(DBGEV_OK));
	return DBGRES_OK;
} 

static int
TestDataEvaluateExpression(char *arg)
{
	/*
	dbg_event *	e;
	e = NewDbgEvent(DBGEV_DATA_EVA_EX);
	e->dbg_event_u.data_expression = res;
	SaveEvent(e);
	*/
	SaveEvent(NewDbgEvent(DBGEV_OK));
	return DBGRES_OK;
}

/*
** Evaluate the expression exp.
*/
static int
TestEvaluateExpression(char *exp)
{
	AIF *a;
	if (strcmp(exp, "long_string") == 0) {
		a = StringToAIF("testing start from 01234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789 end 200");
		dbg_event *	e;
		e = NewDbgEvent(DBGEV_DATA);
		e->dbg_event_u.data_event.data = a;
		e->dbg_event_u.data_event.type_desc = "string";
		SaveEvent(e);
	}
	else if (strcmp(exp, "long_long_string") == 0) {
		a = StringToAIF("testing start from \
		01234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789\
		01234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789\
		01234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789\
		01234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789\
		01234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789\
		01234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789\
		01234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789\
		01234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789\
		01234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789\
		01234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789\
		01234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789\
		01234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789\
		01234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789\
		01234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789\
		01234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789\
		01234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789\
		01234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789\
		01234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789\
		01234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789\
		01234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789\
		01234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789\
		01234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789\
		01234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789\
		01234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789\
		01234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789\
		end 5000");
		dbg_event *	e;
		e = NewDbgEvent(DBGEV_DATA);
		e->dbg_event_u.data_event.data = a;
		e->dbg_event_u.data_event.type_desc = "string";
		SaveEvent(e);
	}
	else { 
		SaveEvent(NewDbgEvent(DBGEV_OK));
	}
	return DBGRES_OK;
}
/*
** Find native type of variable.
*/
static int
TestGetNativeType(char *var)
{
	/*
	dbg_event *	e;
	e = NewDbgEvent(DBGEV_TYPE);
	e->dbg_event_u.type_desc = type;
	SaveEvent(e);
	*/
	SaveEvent(NewDbgEvent(DBGEV_OK));
	return DBGRES_OK;
}

/*************************** PARTIAL AIF ***************************/
static int
TestGetPartialAIF(char* name, char* key, int listChildren, int express)
{
	/*
	dbg_event *	e;
	e = NewDbgEvent(DBGEV_PARTIAL_AIF);
	e->dbg_event_u.partial_aif_event.data = a;
	e->dbg_event_u.partial_aif_event.type_desc = strdup(mivar->type);
	e->dbg_event_u.partial_aif_event.name = strdup(mivar->name);
	SaveEvent(e);
	*/
	SaveEvent(NewDbgEvent(DBGEV_OK));
	return DBGRES_OK;
}

static int
TestVarDelete(char *name)
{
	SaveEvent(NewDbgEvent(DBGEV_OK));
	return DBGRES_OK;
} 
