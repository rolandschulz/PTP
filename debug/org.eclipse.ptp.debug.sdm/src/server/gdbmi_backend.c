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

#include "gdb.h"
#include "dbg.h"
#include "dbg_event.h"
#include "backend.h"
#include "signal_info.h"
#include "bpmap.h"

static MISession *	DebugSession;
static List *		EventList;
static void			(*EventCallback)(dbg_event *);
static int			ServerExit;
static int			Started;
static int			(*AsyncFunc)(void *) = NULL;
static void *		AsyncFuncData;

static int	GDBMIInit(void (*)(dbg_event *));
static int	GDBMIProgress(void);
static int	GDBMIInterrupt(void);
static int	GDBMIStartSession(char *, char *, char *, char *, char **, char **, long);
static int	GDBMISetLineBreakpoint(int, int, int, char *, int, char *, int, int);
static int	GDBMISetFuncBreakpoint(int, int, int, char *, char *, char *, int, int);
static int	GDBMIDeleteBreakpoint(int);
static int	GDBMIEnableBreakpoint(int);
static int	GDBMIDisableBreakpoint(int);
static int	GDBMIConditionBreakpoint(int, char *);
static int	GDBMIBreakpointAfter(int, int);
static int	GDBMIWatchpoint(int, char *, int, int, char *, int);
static int	GDBMIGo(void);
static int	GDBMIStep(int, int);
static int	GDBMITerminate(void);
static int	GDBMIListStackframes(int, int);
static int	GDBMISetCurrentStackframe(int);
static int	GDBMIEvaluateExpression(char *);
static int	GDBMIGetNativeType(char *);
static int	GDBMIGetLocalVariables(void);
static int	GDBMIListArguments(int, int);
static int	GDBMIGetInfoThread(void);
static int	GDBMISetThreadSelect(int);
static int	GDBMIStackInfoDepth(void);
static int	GDBMIDataReadMemory(long, char *, char *, int, int, int, char *);
static int	GDBMIDataWriteMemory(long, char *, char *, int, char *);
static int	GDBMIGetGlobalVariables(void);
static int	GDBCLIListSignals(char*);
static int	GDBCLIHandle(char*);
static int	GDBMIQuit(void);
static int	GDBEvaluatePartialExpression(char *, char *, int, int);
static int	GDBMIVarDelete(char*);

static int		SetAndCheckBreakpoint(int, int, int, char *, char *, int, int);

dbg_backend_funcs	GDBMIBackend =
{
	GDBMIInit,
	GDBMIProgress,
	GDBMIInterrupt,
	GDBMIStartSession,
	GDBMISetLineBreakpoint,
	GDBMISetFuncBreakpoint,
	GDBMIDeleteBreakpoint,
	GDBMIEnableBreakpoint,
	GDBMIDisableBreakpoint,
	GDBMIConditionBreakpoint,
	GDBMIBreakpointAfter,
	GDBMIWatchpoint,
	GDBMIGo,
	GDBMIStep,
	GDBMITerminate,
	GDBMIListStackframes,
	GDBMISetCurrentStackframe,
	GDBMIEvaluateExpression,
	GDBMIGetNativeType,
	GDBMIGetLocalVariables,
	GDBMIListArguments,
	GDBMIGetGlobalVariables,
	GDBMIGetInfoThread,
	GDBMISetThreadSelect,
	GDBMIStackInfoDepth,
	GDBMIDataReadMemory,
	GDBMIDataWriteMemory,
	GDBCLIListSignals,
	NULL,
	GDBCLIHandle,
	NULL,
	GDBEvaluatePartialExpression,
	GDBMIVarDelete,
	GDBMIQuit
};

#define CHECK_SESSION() \
	if (DebugSession == NULL) { \
		DbgSetError(DBGERR_NOSESSION, NULL); \
		return DBGRES_ERR; \
	}

#define ERROR_TO_EVENT(e) \
	e = DbgErrorEvent(DbgGetError(), DbgGetErrorStr())

static char *
GetLastErrorStr(void)
{
	return MIGetErrorStr();
}

static void
SaveEvent(dbg_event *e)
{
	AddToList(EventList, (void *)e);
}

/**** aysnc stop ****/
static int
AsyncStop(void *data)
{
	dbg_event *		e;
	stackframe *	frame;
	MIEvent *		evt = (MIEvent *)data;

	switch (evt->type)
	{
	case MIEventTypeBreakpointHit:
		if (!IsTempBP(evt->bkptno)) {
			e = NewDbgEvent(DBGEV_SUSPEND);
			e->dbg_event_u.suspend_event.reason = DBGEV_SUSPEND_BPHIT;
			e->dbg_event_u.suspend_event.ev_u.bpid = GetRemoteBPID(evt->bkptno);
			e->dbg_event_u.suspend_event.thread_id = evt->threadId;
			e->dbg_event_u.suspend_event.frame = NULL;
			e->dbg_event_u.suspend_event.depth = GetMIInfoDepth(DebugSession);
			e->dbg_event_u.suspend_event.changed_vars = GetChangedVariables(DebugSession);
			break;
		}
		/* else must be a temporary breakpoint drop through... */
		RemoveBPMap(evt->bkptno);

	case MIEventTypeSuspended:
		if (evt->frame != NULL) {
			frame = ConvertMIFrame(evt->frame);
		} else {
			frame = GetCurrentFrame(DebugSession);
		}
		if (frame == NULL) {
			ERROR_TO_EVENT(e);
		} else {
			e = NewDbgEvent(DBGEV_SUSPEND);
			e->dbg_event_u.suspend_event.reason = DBGEV_SUSPEND_INT;
			e->dbg_event_u.suspend_event.thread_id = evt->threadId;
			e->dbg_event_u.suspend_event.frame = frame;
			e->dbg_event_u.suspend_event.depth = GetMIInfoDepth(DebugSession);
			e->dbg_event_u.suspend_event.changed_vars = GetChangedVariables(DebugSession);
		}
		break;

	case MIEventTypeFunctionFinished:
	case MIEventTypeSteppingRange:
		if (evt->frame != NULL) {
			frame = ConvertMIFrame(evt->frame);
		} else {
			frame = GetCurrentFrame(DebugSession);
		}
		if (frame == NULL) {
			ERROR_TO_EVENT(e);
		} else {
			e = NewDbgEvent(DBGEV_SUSPEND);
			e->dbg_event_u.suspend_event.reason = DBGEV_SUSPEND_STEP;
			e->dbg_event_u.suspend_event.thread_id = evt->threadId;
			e->dbg_event_u.suspend_event.frame = frame;
			e->dbg_event_u.suspend_event.depth = GetMIInfoDepth(DebugSession);
			e->dbg_event_u.suspend_event.changed_vars = GetChangedVariables(DebugSession);
		}
		break;

	case MIEventTypeSignal:
		if (evt->frame != NULL) {
			frame = ConvertMIFrame(evt->frame);
		} else {
			frame = GetCurrentFrame(DebugSession);
		}
		if (frame == NULL) {
			ERROR_TO_EVENT(e);
		} else {
			e = NewDbgEvent(DBGEV_SUSPEND);
			e->dbg_event_u.suspend_event.reason = DBGEV_SUSPEND_SIGNAL;
			e->dbg_event_u.suspend_event.ev_u.sig = NewSignalInfo();
			e->dbg_event_u.suspend_event.ev_u.sig->name = strdup(evt->sigName);
			e->dbg_event_u.suspend_event.ev_u.sig->desc = strdup(evt->sigMeaning);
			e->dbg_event_u.suspend_event.thread_id = evt->threadId;
			e->dbg_event_u.suspend_event.frame = frame;
			e->dbg_event_u.suspend_event.depth = GetMIInfoDepth(DebugSession);
			e->dbg_event_u.suspend_event.changed_vars = GetChangedVariables(DebugSession);
		}
		break;

	case MIEventTypeInferiorSignalExit:
		e = NewDbgEvent(DBGEV_EXIT);
		e->dbg_event_u.exit_event.reason = DBGEV_EXIT_SIGNAL;
		e->dbg_event_u.exit_event.ev_u.sig = NewSignalInfo();
		e->dbg_event_u.exit_event.ev_u.sig->name = strdup(evt->sigName);
		e->dbg_event_u.exit_event.ev_u.sig->desc = strdup(evt->sigMeaning);
		//RemoveAllMaps();
		break;

	case MIEventTypeInferiorExit:
		e = NewDbgEvent(DBGEV_EXIT);
		e->dbg_event_u.exit_event.reason = DBGEV_EXIT_NORMAL;
		e->dbg_event_u.exit_event.ev_u.exit_status = evt->code;
		//RemoveAllMaps();
		break;

	default:
		DbgSetError(DBGERR_DEBUGGER, "Unknown reason for stopping");
		return DBGRES_ERR;
	}
	MIEventFree(evt);

	if (EventCallback != NULL)
		EventCallback(e);

	FreeDbgEvent(e);
	return DBGRES_OK;
}

/*
** AsyncCallback is called by mi_get_response() when an async response is
** detected. It can't issue any gdb commands or there's a potential
** for deadlock. If commands need to be issues (e.g. to obtain
** current stack frame, they must be called from the main select
** loop using the AsyncFunc() mechanism.
*/
static void
AsyncCallback(MIEvent *evt)
{
	AsyncFunc = AsyncStop;
	AsyncFuncData = (void *)evt;
}

static void
StreamTargetCallback(char *str) {
	dbg_event *e = NewDbgEvent(DBGEV_OUTPUT);
	e->dbg_event_u.output = strdup(str);
	SaveEvent(e);
}

/*
 * Initialize GDB
 */
static int
GDBMIInit(void (*event_callback)(dbg_event *))
{
	EventCallback = event_callback;
	DebugSession = NULL;
	EventList = NewList();
	ServerExit = 0;

	signal(SIGTERM, SIG_IGN);
	signal(SIGHUP, SIG_IGN);
	signal(SIGINT, SIG_IGN);

	return DBGRES_OK;
}

/*
 * Start GDB session
 */
static int
GDBMIStartSession(char *gdb_path, char *prog, char *path, char *work_dir, char **args, char **env, long timeout)
{
	char *		prog_path;
	char **		e;
	float		version;
	struct stat	st;
	MICommand *	cmd;
	MISession *	sess;

	if (DebugSession != NULL) {
		DbgSetError(DBGERR_SESSION, NULL);
		return DBGRES_ERR;
	}

	/*
	 * see if we can find the gdb executable
	 */
	if (*gdb_path == '/') {
	 	if (stat(gdb_path, &st) < 0 || !S_ISREG(st.st_mode)) {
	 		DbgSetError(DBGERR_NOBACKEND, gdb_path);
	 		return DBGRES_ERR;
	 	}
	}

	if (*work_dir != '\0' && chdir(work_dir) < 0) {
		DbgSetError(DBGERR_CHDIR, work_dir);
		return DBGRES_ERR;
	}

	if (path != NULL)
		asprintf(&prog_path, "%s/%s", path, prog);
	else
		prog_path = strdup(prog);

	if (access(prog_path, R_OK) < 0) {
		DbgSetError(DBGERR_NOFILEDIR, prog_path);
		free(prog_path);
		return DBGRES_ERR;
	}

	sess = MISessionNew();

#ifdef DEBUG
	MISessionSetDebug(TEST_DEBUG_LEVEL(DEBUG_LEVEL_BACKEND));
#endif /* DEBUG */

	MISessionSetTimeout(sess, 0, timeout);

	MISessionSetGDBPath(sess, gdb_path);

	if (MISessionStartLocal(sess, prog_path) < 0) {
		DbgSetError(DBGERR_DEBUGGER, GetLastErrorStr());
		MISessionFree(sess);
		free(prog_path);
		return DBGRES_ERR;
	}

	free(prog_path);

	if (GetGDBVersion(sess, &version) < 0) {
		return DBGRES_ERR;
	}
	DEBUG_PRINTF(DEBUG_LEVEL_BACKEND, "------------------- gdb version: %f\n", version);

	if (*args != NULL) {
		cmd = MIExecArguments(args);
		SendCommandWait(sess, cmd);
		MICommandFree(cmd);
	}

	for (e = env; e != NULL && *e != NULL; e++) {
		cmd = MIGDBSet("environment", *e);
		SendCommandWait(sess, cmd);
		MICommandFree(cmd);
	}

	cmd = MIGDBSet("confirm", "off");
	SendCommandWait(sess, cmd);
	MICommandFree(cmd);

	MISessionRegisterEventCallback(sess, AsyncCallback);
	MISessionRegisterTargetCallback(sess, StreamTargetCallback);

	DebugSession = sess;
	Started = 0;
	SaveEvent(NewDbgEvent(DBGEV_OK));

	return DBGRES_OK;
}

/*
 * Progress gdb commands.
 */
static int
GDBMIProgress(void)
{
	dbg_event *	e;

	/*
	 * Check for existing events
	 */
	if (!EmptyList(EventList)) {
		e = (dbg_event *)RemoveFirst(EventList);

		if (EventCallback != NULL) {
			EventCallback(e);
		}

		if (ServerExit && e->event_id == DBGEV_OK) {
			if (DebugSession != NULL) {
				MISessionFree(DebugSession);
				DebugSession = NULL;
			}
		}

		FreeDbgEvent(e);
		return 0;
	}

	if (DebugSession != NULL) {
		if (MISessionProgress(DebugSession) < 0) {
			MISessionFree(DebugSession);
			DebugSession = NULL;
			DbgSetError(DBGERR_DEBUGGER, GetLastErrorStr());
			ERROR_TO_EVENT(e);
			SaveEvent(e);
			return 0;
		}

		/*
		 * Do any extra async functions. We can call gdbmi safely here
		 */
		if (AsyncFunc != NULL) {
			AsyncFunc(AsyncFuncData);
			AsyncFunc = NULL;
			return 0;
		}
	}

	return 0;
}

/*
** Set breakpoint at specified line.
*/
static int
GDBMISetLineBreakpoint(int bpid, int isTemp, int isHard, char *file, int line, char *condition, int ignoreCount, int tid)
{
	int		res;
	char *	where;

	CHECK_SESSION();

	if (file == NULL || *file == '\0')
		asprintf(&where, "%d", line);
	else
		asprintf(&where, "%s:%d", file, line);

	res = SetAndCheckBreakpoint(bpid, isTemp, isHard, where, condition, ignoreCount, tid);

	free(where);

	return res;
}

/*
 * Set breakpoint at start of specified function.
 *
 * If the function is "main" and this is a temporary breakpoint, assume that
 * this is a result of the "Stop in main() on startup" setting. To deal with
 * Fortran in this situation, we first try to set a breakpoint at "MAIN__"
 * which is the main Fortran entry point. If this fails, we assume that we
 * are debugging C/C++ and set the breakpoint on "main".
 */
static int
GDBMISetFuncBreakpoint(int bpid, int isTemp, int isHard, char *file, char *func, char *condition, int ignoreCount, int tid)
{
	int		res;
	char *	where;

	CHECK_SESSION();

	if ((file == NULL || *file == '\0') && isTemp && strcmp(func, "main") == 0) {
		res = SetAndCheckBreakpoint(bpid, isTemp, isHard, "MAIN__", condition, ignoreCount, tid);
		if (res == DBGRES_OK) {
			return res;
		}
	}

	if (file == NULL || *file == '\0')
		asprintf(&where, "%s", func);
	else
		asprintf(&where, "%s:%s", file, func);

	res = SetAndCheckBreakpoint(bpid, isTemp, isHard, where, condition, ignoreCount, tid);

	free(where);

	return res;
}

/*
** Check that breakpoint command has succeeded and
** extract appropriate information. Returns breakpoint id.
** Adds to breakpoint list if necessary.
*/
static int
SetAndCheckBreakpoint(int bpid, int isTemp, int isHard, char *where, char *condition, int ignoreCount, int tid)
{
	dbg_event *		e;
	MIBreakpoint *	bpt;
	MICommand *		cmd;
	MIList *		bpts;

	if (condition != NULL && strlen(condition) == 0)
		condition = NULL;

	cmd = MIBreakInsert(isTemp, isHard, condition, ignoreCount, where, tid);
	SendCommandWait(DebugSession, cmd);

	if (!MICommandResultOK(cmd)) {
		DbgSetError(DBGERR_NOFILE, GetLastErrorStr());
		MICommandFree(cmd);
		return DBGRES_ERR;
	}
	bpts = MIBreakpointGetBreakInsertInfo(cmd);
	MICommandFree(cmd);

	if (bpts == NULL) {
		DbgSetError(DBGERR_NOFILE, where);
		return DBGRES_ERR;
	}

	MIListSet(bpts);
	bpt = (MIBreakpoint *)MIListGet(bpts);

	AddBPMap(bpt->number, bpid, isTemp);

	/*
	 * if the type is temporary, no need to send DBGEV_BPSET event
	 */
	if (isTemp) {
		SaveEvent(NewDbgEvent(DBGEV_OK));
		MIListFree(bpts, MIBreakpointFree);
		return DBGRES_OK;
	}

	e = NewDbgEvent(DBGEV_BPSET);
	e->dbg_event_u.bpset_event.bpid = bpid;
	e->dbg_event_u.bpset_event.bp = ConvertMIBreakpoint(bpt);
	SaveEvent(e);

	MIListFree(bpts, MIBreakpointFree);
	return DBGRES_OK;
}

/*
** Delete a breakpoint.
*/
static int
GDBMIDeleteBreakpoint(int bpid)
{
	int			bpid_local;
	char *		bpstr;
	MICommand *	cmd;

	CHECK_SESSION();

	if ((bpid_local = GetLocalBPID(bpid)) < 0) {
		asprintf(&bpstr, "%d", bpid);
		DbgSetError(DBGERR_NOBP, bpstr);
		free(bpstr);
		return DBGRES_ERR;
	}

	cmd = MIBreakDelete(1, &bpid_local);
	SendCommandWait(DebugSession, cmd);

	if (!MICommandResultOK(cmd)) {
		SetDebugError(cmd);
		MICommandFree(cmd);
		return DBGRES_ERR;
	}

	RemoveBPMap(bpid_local);
	SaveEvent(NewDbgEvent(DBGEV_OK));

	return DBGRES_OK;
}

/*
** Enable a breakpoint.
*/
static int
GDBMIEnableBreakpoint(int bpid)
{
	int			bpid_local;
	char *		bpstr;
	MICommand *	cmd;

	CHECK_SESSION();

	if ((bpid_local = GetLocalBPID(bpid)) < 0) {
		asprintf(&bpstr, "%d", bpid);
		DbgSetError(DBGERR_NOBP, bpstr);
		free(bpstr);
		return DBGRES_ERR;
	}

	cmd = MIBreakEnable(1, &bpid_local);
	SendCommandWait(DebugSession, cmd);

	if (!MICommandResultOK(cmd)) {
		SetDebugError(cmd);
		MICommandFree(cmd);
		return DBGRES_ERR;
	}
	SaveEvent(NewDbgEvent(DBGEV_OK));
	return DBGRES_OK;
}

/*
** Disable a breakpoint.
*/
static int
GDBMIDisableBreakpoint(int bpid)
{
	int			bpid_local;
	char *		bpstr;
	MICommand *	cmd;

	CHECK_SESSION();

	if ((bpid_local = GetLocalBPID(bpid)) < 0) {
		asprintf(&bpstr, "%d", bpid);
		DbgSetError(DBGERR_NOBP, bpstr);
		free(bpstr);
		return DBGRES_ERR;
	}

	cmd = MIBreakDisable(1, &bpid_local);
	SendCommandWait(DebugSession, cmd);

	if (!MICommandResultOK(cmd)) {
		SetDebugError(cmd);
		MICommandFree(cmd);
		return DBGRES_ERR;
	}
	SaveEvent(NewDbgEvent(DBGEV_OK));
	return DBGRES_OK;
}

/*
** Condition a breakpoint.
*/
static int
GDBMIConditionBreakpoint(int bpid, char *expr)
{
	int			bpid_local;
	char *		bpstr;
	MICommand *	cmd;

	CHECK_SESSION();

	if ((bpid_local = GetLocalBPID(bpid)) < 0) {
		asprintf(&bpstr, "%d", bpid);
		DbgSetError(DBGERR_NOBP, bpstr);
		free(bpstr);
		return DBGRES_ERR;
	}

	cmd = MIBreakCondition(1, &bpid_local, expr);
	SendCommandWait(DebugSession, cmd);

	if (!MICommandResultOK(cmd)) {
		SetDebugError(cmd);
		MICommandFree(cmd);
		return DBGRES_ERR;
	}
	SaveEvent(NewDbgEvent(DBGEV_OK));
	return DBGRES_OK;
}

/*
** breakpoint after.
*/
static int
GDBMIBreakpointAfter(int bpid, int icount)
{
	int			bpid_local;
	char *		bpstr;
	MICommand *	cmd;

	CHECK_SESSION();

	if ((bpid_local = GetLocalBPID(bpid)) < 0) {
		asprintf(&bpstr, "%d", bpid);
		DbgSetError(DBGERR_NOBP, bpstr);
		free(bpstr);
		return DBGRES_ERR;
	}

	cmd = MIBreakAfter(1, &bpid_local, icount);
	SendCommandWait(DebugSession, cmd);

	if (!MICommandResultOK(cmd)) {
		SetDebugError(cmd);
		MICommandFree(cmd);
		return DBGRES_ERR;
	}
	SaveEvent(NewDbgEvent(DBGEV_OK));
	return DBGRES_OK;
}

/*
 * Set watch point
 */
static int
GDBMIWatchpoint(int bpid, char *expr, int isAccess, int isRead, char *condition, int ignoreCount)
{
	dbg_event *		e;
	MIBreakpoint *	bpt;
	MICommand *		cmd;
	MIList *		bpts;
	breakpoint *	bp;

	if (condition != NULL && strlen(condition) == 0)
		condition = NULL;

	cmd = MIBreakWatch(expr, isAccess, isRead);
	SendCommandWait(DebugSession, cmd);

	if (!MICommandResultOK(cmd)) {
		SetDebugError(cmd);
		MICommandFree(cmd);
		return DBGRES_ERR;
	}

	bpts = MIBreakpointGetBreakInsertInfo(cmd);
	MICommandFree(cmd);

	if (bpts == NULL) {
		DbgSetError(DBGERR_DEBUGGER, "error getting breakpoint information");
		return DBGRES_ERR;
	}

	MIListSet(bpts);
	bpt = (MIBreakpoint *)MIListGet(bpts);

	AddBPMap(bpt->number, bpid, 0); //0 is not temp??

	bp = NewBreakpoint(bpt->number);

	bp->ignore = bpt->ignore;
	bp->type = strdup(bpt->type);
	bp->hits = bpt->times;

	if (condition != NULL) {
		GDBMIConditionBreakpoint(bpid, condition);
	}
	if (ignoreCount > 0) {
		GDBMIBreakpointAfter(bpid, ignoreCount);
	}

	e = NewDbgEvent(DBGEV_BPSET);
	e->dbg_event_u.bpset_event.bpid = bpid;
	e->dbg_event_u.bpset_event.bp = bp;
	SaveEvent(e);

	MIListFree(bpts, MIBreakpointFree);

	return DBGRES_OK;
}

/*
** Start/continue executing program.
*/
static int
GDBMIGo(void)
{
	MICommand *	cmd;

	CHECK_SESSION();

	if (Started)
		cmd = MIExecContinue();
	else {
		cmd = MIExecRun();
		Started = 1;
	}

	SendCommandWait(DebugSession, cmd);
	if (!MICommandResultOK(cmd)) {
		SetDebugError(cmd);
		MICommandFree(cmd);
		return DBGRES_ERR;
	}

	MICommandFree(cmd);

	SaveEvent(NewDbgEvent(DBGEV_OK));

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
GDBMIStep(int count, int type)
{
	MICommand *	cmd;

	CHECK_SESSION();

	switch ( type ) {
	case 0:
		cmd = MIExecStep(count);
		break;

	case 1:
		cmd = MIExecNext(count);
		break;

	case 2:
		cmd = MIExecFinish();
		break;

	default:
		DbgSetError(DBGERR_DEBUGGER, "Unknown step type");
		return DBGRES_ERR;
	}

	SendCommandWait(DebugSession, cmd);

	if (!MICommandResultOK(cmd)) {
		SetDebugError(cmd);
		MICommandFree(cmd);
		return DBGRES_ERR;
	}

	MICommandFree(cmd);

	SaveEvent(NewDbgEvent(DBGEV_OK));

	return DBGRES_OK;
}

/*
** Terminate program execution.
*/
static int
GDBMITerminate(void)
{
	MICommand *	cmd;

	CHECK_SESSION();

	cmd = MICommandNew("kill", MIResultRecordDONE);

	SendCommandWait(DebugSession, cmd);

	if (!MICommandResultOK(cmd)) {
		SetDebugError(cmd);
		MICommandFree(cmd);
		return DBGRES_ERR;
	}

	MICommandFree(cmd);

	SaveEvent(NewDbgEvent(DBGEV_OK));

	return DBGRES_OK;
}

/*
** Interrupt an executing program.
*/
static int
GDBMIInterrupt(void)
{
	MICommand *	cmd;

	CHECK_SESSION();

	/*
	 * Ignore error if target is not running
	 */

	cmd = MIExecInterrupt();

	SendCommandWait(DebugSession, cmd);

	MICommandFree(cmd);

	return DBGRES_OK;
}

/*
** Move up or down count stack frames.
*/
static int
GDBMISetCurrentStackframe(int level)
{
	MICommand *	cmd;

	CHECK_SESSION();

	cmd = MIStackSelectFrame(level);

	SendCommandWait(DebugSession, cmd);

	if (!MICommandResultOK(cmd)) {
		SetDebugError(cmd);
		MICommandFree(cmd);
		return DBGRES_ERR;
	}

	MICommandFree(cmd);

	SaveEvent(NewDbgEvent(DBGEV_OK));

	return DBGRES_OK;
}

/*
** List current or all stack frames.
*/
static int
GDBMIListStackframes(int low, int high)
{
	dbg_event *	e;
	List *		frames;

	CHECK_SESSION();

	if (GetStackframes(DebugSession, 0, low, high, &frames) != DBGRES_OK)
		return DBGRES_ERR;

	e = NewDbgEvent(DBGEV_FRAMES);
	e->dbg_event_u.list = frames;
	SaveEvent(e);

	return DBGRES_OK;
}

/*
** List local variables.
*/
static int
GDBMIGetLocalVariables(void)
{
	dbg_event *	e;
	MICommand *	cmd;
	MIArg *		arg;
	MIList *	args;

	CHECK_SESSION();

	cmd = MIStackListLocals(0);

	SendCommandWait(DebugSession, cmd);

	if (!MICommandResultOK(cmd)) {
		SetDebugError(cmd);
		MICommandFree(cmd);
		return DBGRES_ERR;
	}

	args = MIGetStackListLocalsInfo(cmd);

	MICommandFree(cmd);

	e = NewDbgEvent(DBGEV_VARS);
	e->dbg_event_u.list = NewList();

	for (MIListSet(args); (arg = (MIArg *)MIListGet(args)) != NULL; ) {
		AddToList(e->dbg_event_u.list, (void *)strdup(arg->name));
	}

	MIListFree(args, MIArgFree);

	SaveEvent(e);

	return DBGRES_OK;
}

/*
** List arguments.
*/
static int
GDBMIListArguments(int low, int high)
{
	dbg_event *	e;
	MICommand *	cmd;
	MIArg *		arg;
	MIFrame *	frame;
	MIList *	frames;

	CHECK_SESSION();

	cmd = MIStackListArguments(0, low, high);

	SendCommandWait(DebugSession, cmd);

	if (!MICommandResultOK(cmd)) {
		SetDebugError(cmd);
		MICommandFree(cmd);
		return DBGRES_ERR;
	}

	frames = MIGetStackListArgumentsInfo(cmd);

	MICommandFree(cmd);

	e = NewDbgEvent(DBGEV_ARGS);
	e->dbg_event_u.list = NewList();

	/*
 	 * Just look at first frame - we should only get
 	 * one anyway...
 	 * ****** If frame is more than one, no arg needs *****
 	 */
	MIListSet(frames);
	if ((frame = (MIFrame *)MIListGet(frames)) != NULL) {
#if GDB_BUG_2188
		if (!CurrentFrame(DebugSession, frame->level, "main")) {
#endif /* GDB_BUG_2188 */
			for (MIListSet(frame->args); (arg = (MIArg *)MIListGet(frame->args)) != NULL; ) {
				AddToList(e->dbg_event_u.list, (void *)strdup(arg->name));
			}
#if GDB_BUG_2188
		}
#endif /* GDB_BUG_2188 */
	}
	MIListFree(frames, MIFrameFree);
	SaveEvent(e);

	return DBGRES_OK;
}

/*
** List global variables.
*/
static int
GDBMIGetGlobalVariables(void)
{
	CHECK_SESSION();

	DbgSetError(DBGERR_NOTIMP, NULL);
	return DBGRES_ERR;
}

/*
** Quit debugger.
*/
static int
GDBMIQuit(void)
{
	MICommand *	cmd;

	if (DebugSession != NULL) {
		cmd = MIGDBExit();
		SendCommandWait(DebugSession, cmd);
		MICommandFree(cmd);
	}
	ClearBPMaps();
	SaveEvent(NewDbgEvent(DBGEV_OK));
	ServerExit++;

	return DBGRES_OK;
}

static int
GDBMIGetInfoThread(void)
{
	MICommand *				cmd;
	dbg_event *				e;
	char *					tid;
	CLIInfoThreadsInfo *	info;

	CHECK_SESSION();

	cmd = CLIInfoThreads();
	SendCommandWait(DebugSession, cmd);
	if (!MICommandResultOK(cmd)) {
		DEBUG_PRINTS(DEBUG_LEVEL_BACKEND, "------------------- GDBMIGetInfoThread error\n");
		SetDebugError(cmd);
		MICommandFree(cmd);
		return DBGRES_ERR;
	}
	info = CLIGetInfoThreadsInfo(cmd);
	MICommandFree(cmd);

	e = NewDbgEvent(DBGEV_THREADS);
	e->dbg_event_u.threads_event.thread_id = info->current_thread_id;
	e->dbg_event_u.threads_event.list = NewList();
	for (MIListSet(info->thread_ids); (tid = (char *)MIListGet(info->thread_ids)) != NULL;) {
		AddToList(e->dbg_event_u.threads_event.list, (void *)strdup(tid));
	}

	MIListFree(info->thread_ids, free);
	free(info);
	SaveEvent(e);

	return DBGRES_OK;
}

static int
GDBMISetThreadSelect(int threadNum)
{
	MICommand *				cmd;
	dbg_event *				e;
	MIThreadSelectInfo *	info;
	stackframe *			s = NULL;

	CHECK_SESSION();

	cmd = MIThreadSelect(threadNum);
	SendCommandWait(DebugSession, cmd);
	if (!MICommandResultOK(cmd)) {
		SetDebugError(cmd);
		MICommandFree(cmd);
		return DBGRES_ERR;
	}
	info = MISetThreadSelectInfo(cmd);
	MICommandFree(cmd);

	if (info->frame != NULL) {
		s = ConvertMIFrame(info->frame);
	}
	e = NewDbgEvent(DBGEV_THREAD_SELECT);
	e->dbg_event_u.thread_select_event.thread_id = info->current_thread_id;
	e->dbg_event_u.thread_select_event.frame = s;

	MIFrameFree(info->frame);
	free(info);
	SaveEvent(e);

	return DBGRES_OK;
}

static int
GDBMIStackInfoDepth()
{
	int depth;
	dbg_event *	e;

	CHECK_SESSION();

	if ((depth = GetMIInfoDepth(DebugSession)) == -1) {
		return DBGRES_ERR;
	}
	e = NewDbgEvent(DBGEV_STACK_DEPTH);
	e->dbg_event_u.stack_depth = depth;
	SaveEvent(e);
	return DBGRES_OK;
}

static int
GDBMIDataReadMemory(long offset, char* address, char* format, int wordSize, int rows, int cols, char* asChar)
{
	MICommand *	cmd;
	dbg_event *	e;
	MIDataReadMemoryInfo * info;
	MIMemory * mem;
	memoryinfo *meminfo = NULL;
	memory * m;

	CHECK_SESSION();

	cmd = MIDataReadMemory(offset, address, format, wordSize, rows, cols, asChar);
	SendCommandWait(DebugSession, cmd);
	if (!MICommandResultOK(cmd)) {
		SetDebugError(cmd);
		MICommandFree(cmd);
		return DBGRES_ERR;
	}
	info = MIGetDataReadMemoryInfo(cmd);
	MICommandFree(cmd);

	e = NewDbgEvent(DBGEV_DATAR_MEM);
	if (info != NULL) {
		meminfo = NewMemoryInfo();
		if (info->addr != NULL) {
			meminfo->addr = strdup(info->addr);
		}
		meminfo->nextRow = info->nextRow;
		meminfo->prevRow = info->prevRow;
		meminfo->nextPage = info->nextPage;
		meminfo->prevPage = info->prevPage;
		meminfo->numBytes = info->numBytes;
		meminfo->totalBytes = info->totalBytes;
		if (info->memories != NULL ) {
			meminfo->memories = NewList();
			for (MIListSet(info->memories); (mem = (MIMemory *)MIListGet(info->memories)) != NULL;) {
				m = NewMemory();
				if (mem->addr != NULL) {
					m->addr = strdup(mem->addr);
				}
				if (mem->ascii != NULL) {
					m->ascii = strdup(mem->ascii);
				}
				if (mem->data != NULL) {
					char* d;
					m->data = NewList();
					for (MIListSet(mem->data); (d = (char *)MIListGet(mem->data)) != NULL;) {
						AddToList(m->data, (void *) strdup(d));
					}
				}
				AddToList(meminfo->memories, (void *)m);
			}
		}
	}
	e->dbg_event_u.meminfo = meminfo;
	MIDataReadMemoryInfoFree(info);

	SaveEvent(e);
	return DBGRES_OK;
}

static int
GDBMIDataWriteMemory(long offset, char* address, char* format, int wordSize, char* value)
{
	MICommand *	cmd;

	CHECK_SESSION();

	//DEBUG_PRINTS(DEBUG_LEVEL_BACKEND, "----- gdbmi_sevrer: GDBMIDataWriteMemory called ---------\n");
	cmd = MIDataWriteMemory(offset, address, format, wordSize, value);
	SendCommandWait(DebugSession, cmd);
	if (!MICommandResultOK(cmd)) {
		SetDebugError(cmd);
		MICommandFree(cmd);
		return DBGRES_ERR;
	}
	MICommandFree(cmd);
//TODO
	SaveEvent(NewDbgEvent(DBGEV_OK));

	return DBGRES_OK;
}

static int
GDBCLIListSignals(char* name)
{
	MICommand *		cmd;
	MIList *		signals;
	MISignalInfo *	sig;
	dbg_event *		e;
	signal_info *	s;

	CHECK_SESSION();

	cmd = CLIListSignals(name);
	SendCommandWait(DebugSession, cmd);
	if (!MICommandResultOK(cmd)) {
		SetDebugError(cmd);
		MICommandFree(cmd);
		return DBGRES_ERR;
	}
	CLIGetSigHandleList(cmd, &signals);
	MICommandFree(cmd);

	e = NewDbgEvent(DBGEV_SIGNALS);
	e->dbg_event_u.list = NewList();
	for (MIListSet(signals); ((sig = (MISignalInfo *)MIListGet(signals)) != NULL); ) {
		s = NewSignalInfo();
		s->name = strdup(sig->name);
		s->pass = sig->pass;
		s->print = sig->print;
		s->desc = strdup(sig->desc != NULL ? sig->desc : "");
		AddToList(e->dbg_event_u.list, s);
	}
	SaveEvent(e);
	MIListFree(signals, MISignalInfoFree);

	return DBGRES_OK;
}

static int
GDBCLIHandle(char *arg)
{
	MICommand *cmd;

	CHECK_SESSION();

	cmd = CLIHandle(arg);
	SendCommandWait(DebugSession, cmd);
	if (!MICommandResultOK(cmd)) {
		SetDebugError(cmd);
		MICommandFree(cmd);
		return DBGRES_ERR;
	}
	MICommandFree(cmd);
	SaveEvent(NewDbgEvent(DBGEV_OK));
	return DBGRES_OK;
}

/*
** Evaluate the expression exp.
*/
static int
GDBMIEvaluateExpression(char *exp)
{
	char *		type;
	AIF *		a;
	dbg_event *	e;

	if (GetAIFVar(DebugSession, exp, &a, &type) != DBGRES_OK)
		return DBGRES_ERR;

	e = NewDbgEvent(DBGEV_DATA);
	e->dbg_event_u.data_event.data = a;
	e->dbg_event_u.data_event.type_desc = type;
	e->dbg_event_u.data_event.name = strdup("");
	SaveEvent(e);

	return DBGRES_OK;
}

/*
** Find native type of variable.
*/
static int
GDBMIGetNativeType(char *var)
{
	dbg_event *	e;
	AIF *		a;
	char *		type;

	CHECK_SESSION();

	if (GetAIFVar(DebugSession, var, &a, &type) != DBGRES_OK)
		return DBGRES_ERR;

	e = NewDbgEvent(DBGEV_TYPE);
	e->dbg_event_u.type_desc = type;

	SaveEvent(e);
	AIFFree(a);
	return DBGRES_OK;
}

/*
 * Evaluate a partial expression.
 *
 * - if 'var_id' is not zero length, use this to look up an existing MI variable
 * - if 'var_id' can't be found, or is zero length, create a new variable using 'expr'
 * - if 'list_child' is true, get the first level of children for the variable, otherwise
 *   get minimal information about the variable. Note that the child information is
 *   always fetched for pointers
 */
static int
GDBEvaluatePartialExpression(char* expr, char* var_id, int list_child, int express)
{
	MIVar *		mivar = NULL;
	AIF *		a;
	dbg_event *	e;

	CHECK_SESSION();

	DEBUG_PRINTF(DEBUG_LEVEL_BACKEND, "---------------------- GDBGetPartialAIF expr: %s, var_id: %s, list_child: %d, exp: %d\n",
			expr, var_id, list_child, express);

	if (strlen(var_id) != 0) {
		mivar = GetMIVarDetails(DebugSession, var_id, NULL, list_child);
	}

	if (mivar == NULL) {
		mivar = CreateMIVar(DebugSession, expr);
	}

	if (mivar != NULL) {
		if (mivar->numchild == 1 && mivar->children == NULL) {
			list_child = 1;
		}
		mivar = GetMIVarDetails(DebugSession, mivar->name, mivar, list_child);
	}

	if (mivar == NULL) {
		DbgSetError(DBGERR_UNKNOWN_VARIABLE, strlen(var_id) != 0 ? var_id : expr);
		return DBGRES_ERR;
	}

	if ((a = GetPartialAIF(DebugSession, expr, mivar)) == NULL) {
		DbgSetError(DBGERR_UNKNOWN_TYPE, mivar->type);
		MIVarFree(mivar);
		return DBGRES_ERR;
	}

	DEBUG_PRINTF(DEBUG_LEVEL_BACKEND, "---------------------- GDBGetPartialAIF found key: %s, format: %s\n", mivar->name, AIF_FORMAT(a));

	e = NewDbgEvent(DBGEV_DATA);
	e->dbg_event_u.data_event.data = a;
	e->dbg_event_u.data_event.type_desc = strdup(mivar->type);
	e->dbg_event_u.data_event.name = strdup(mivar->name);

	MIVarFree(mivar);
	SaveEvent(e);

	return DBGRES_OK;
}

static int
GDBMIVarDelete(char *name)
{
	CHECK_SESSION();
	DeleteMIVar(DebugSession, name);
	SaveEvent(NewDbgEvent(DBGEV_OK));
	return DBGRES_OK;
}
