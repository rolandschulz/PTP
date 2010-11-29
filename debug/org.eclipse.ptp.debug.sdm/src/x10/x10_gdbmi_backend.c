/*
 * Copyright (c) 2010 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
 
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
#include "bpmap.h"
#include "threadmap.h"

#include "MI.h"

#include "x10/x10_aif.h"
#include "x10/x10_metadebug_info_map.h"
#include "x10/x10_variable_util.h"

#define X10_DEBUG 		"_X10_DEBUG"
#define X10_DEBUG_DATA 	"_X10_DEBUG_DATA"
#define X10_SIGNAL_FILTER_COMMAND "all nostop noprint"
#define X10_SIGNAL_ADD_BACK_COMMAND_SIGSEGV "SIGSEGV stop print"
#define X10_SIGNAL_ADD_BACK_COMMAND_SIGILL "SIGILL stop print"
#define X10_SIGNAL_ADD_BACK_COMMAND_SIGFPE "SIGFPE stop print"
#define X10_SIGNAL_ADD_BACK_COMMAND_SIGSTOP "SIGSTOP stop print"
#define X10_SIGNAL_ADD_BACK_COMMAND_SIGUSR1 "SIGUSR1 stop print"
#define X10_SIGNAL_ADD_BACK_COMMAND_SIGUSR2 "SIGUSR2 stop print"
#define X10_INFO_FILE_COMMAND "info file"

#define X10_STATEMENT_HOOK_NAME "_X10_STATEMENT_HOOK"
#define STEP_REQUEST_STEPINTO 0
#define STEP_REQUEST_STEPOVER 1
#define STEP_REQUEST_STEPRETURN 2

//#define X10_SIGNAL_FILTER_COMMAND "SIG39 SIG40 nostop noprint"


static MISession *	DebugSession;
static List *		EventList;
static void			(*EventCallback)(dbg_event *);
static int			ServerExit;
static int			Started;
static List 		*localVars = NULL;

static int			(*X10AsyncFunc)(void *) = NULL;
static void *		X10AsyncFuncData;

static int			(*X10AsyncFuncBackup)(void *) = NULL;
static void *		X10AsyncFuncBackupData;

static int			(*ProcessX10MapsFunc)(char *) = NULL;
static char *       ProcessX10MapsFuncData;
static int          X10MetaInfoAddrAvailable = 0;
static int 			Current_Thread_id = -1;

static int	X10GDBMIInit(void (*)(dbg_event *));
static int	X10GDBMIProgress(void);
static int	X10GDBMIInterrupt(void);
static int	X10GDBMIStartSession(char *, char *, char *, char *, char **, char **, long);
static int	X10GDBMISetLineBreakpoint(int, int, int, char *, int, char*, int, int);
static int	X10GDBMISetFuncBreakpoint(int, int, int, char *, char *, char*, int, int);
static int	X10GDBMIDeleteBreakpoint(int);
static int	X10GDBMIEnableBreakpoint(int);
static int	X10GDBMIDisableBreakpoint(int);
static int	X10GDBMIConditionBreakpoint(int, char *expr);
static int	X10GDBMIBreakpointAfter(int, int icount);
static int	X10GDBMIWatchpoint(int, char *, int, int, char *, int);
static int	X10GDBMIGo(void);
static int	X10GDBMIStep(int, int);
static int	X10GDBMITerminate(void);
static int	X10GDBMIListStackframes(int, int);
static int	X10GDBMISetCurrentStackframe(int);
static int	X10GDBMIEvaluateExpression(char *);
static int	X10GDBMIGetNativeType(char *);
static int	X10GDBMIGetLocalVariables(void);
static int	X10GDBMIListArguments(int, int);
static int	X10GDBMIGetInfoThread(void);
static int	X10GDBMISetThreadSelect(int);
static int	X10GDBMIStackInfoDepth(void);
static int	X10GDBMIDataReadMemory(long, char*, char*, int, int, int, char*);
static int	X10GDBMIDataWriteMemory(long, char*, char*, int, char*);
static int	X10GDBMIGetGlobalVariables(void);
static int	X10GDBCLIListSignals(char*);
static int	X10GDBCLIHandle(char*);
static int	X10GDBMIQuit(void);
static int	X10GDBEvaluatePartialExpression(char *, char *, int, int);
static int	X10GDBMIVarDelete(char*);
static int	SetAndCheckBreakpoint(int, int, int, char *, char *, int, int);

static int  X10AsyncStop(void *data);

dbg_backend_funcs	X10GDBMIBackend =
{
	X10GDBMIInit,
	X10GDBMIProgress,
	X10GDBMIInterrupt,
	X10GDBMIStartSession,
	X10GDBMISetLineBreakpoint,
	X10GDBMISetFuncBreakpoint,
	X10GDBMIDeleteBreakpoint,
	X10GDBMIEnableBreakpoint,
	X10GDBMIDisableBreakpoint,
	X10GDBMIConditionBreakpoint,
	X10GDBMIBreakpointAfter,
	X10GDBMIWatchpoint,
	X10GDBMIGo,
	X10GDBMIStep,
	X10GDBMITerminate,
	X10GDBMIListStackframes,
	X10GDBMISetCurrentStackframe,
	X10GDBMIEvaluateExpression,
	X10GDBMIGetNativeType,
	X10GDBMIGetLocalVariables,
	X10GDBMIListArguments,
	X10GDBMIGetGlobalVariables,
	X10GDBMIGetInfoThread,
	X10GDBMISetThreadSelect,
	X10GDBMIStackInfoDepth,
	X10GDBMIDataReadMemory,
	X10GDBMIDataWriteMemory,
	X10GDBCLIListSignals,
	NULL,
	X10GDBCLIHandle,
	NULL,
	X10GDBEvaluatePartialExpression,
	X10GDBMIVarDelete,
	X10GDBMIQuit
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

/*
 * Convert a MI stack frame to an X10 stack frame.
 */
static stackframe *
X10ConvertMIFrameToStackframe(MIFrame *f,int* foundMatchingX10Line)
{
	stackframe *	s;
	int tmpINT=0;
	if (f == NULL) {//by default return current frame
		return GetCurrentFrame(DebugSession);
	}

	if(foundMatchingX10Line == NULL)
	{
		foundMatchingX10Line = &tmpINT;
	}

	s = NewStackframe(f->level);
	if ( f->addr != NULL )
		s->loc.addr = strdup(f->addr);
	if ( f->func != NULL )
		s->loc.func = strdup(f->func);
	if ( f->file != NULL) {
		int mappedX10Line = 0;
		char *mappedX10File;
		char *mappedX10Method;

		*foundMatchingX10Line = CPPLineToX10Line(strdup(f->file), f->line, &mappedX10File, &mappedX10Method, &mappedX10Line);
		if (*foundMatchingX10Line == 1) {
			s->loc.file = mappedX10File;
			s->loc.line = mappedX10Line;
			if (mappedX10Method != NULL) {
				s->loc.func = mappedX10Method;
			}
		}
		else {
			s->loc.file = strdup(f->file);
			s->loc.line = f->line;
		}
	}
	else {
		s->loc.line = f->line;
	}
	return s;
}

/*
 * Call utility function to read and build X10 MetaDebug table.
 */
static int
ProcessX10Maps(char *data)
{
	char *tmp = NULL;
	/* get the start address */
	if (strstr(data, "0x") != NULL) {
		/* find the string for the starting address */
		tmp = strchr(data, '0');
		X10MetaDebugInfoMapCreate64(DebugSession, data);
	}

	return 0;
}

/*
 * Delete an internal used breakpoint from this debug session.
 */
static int 
deleteInternalBreakpoint(int breakpointNum)
{
	MICommand *		cmd;
	CHECK_SESSION();
	cmd = MIBreakDelete(1, &breakpointNum);
	SendCommandWait(DebugSession, cmd);

	if (!MICommandResultOK(cmd)) {
		SetDebugError(cmd);
		MICommandFree(cmd);
		return DBGRES_ERR;
	}

	RemoveBPMap(breakpointNum);

	return DBGRES_OK;
}

/*
 * Check that breakpoint command has succeded and
 * extract appropriate information. Returns breakpoint
 * id in bid. 
*/
static int
SetAndCheckInternalBreak(int *pBPNumber, int remoteID, int isTemp, int isHard, char *where, char *condition, int ignoreCount, int tid)
{
	MIBreakpoint *	bpt;
	MICommand *		cmd;
	MIList *			bpts;

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
		//DbgSetError(DBGERR_NOFILE, "error getting breakpoint information");
		return DBGRES_ERR;
	}

	MIListSet(bpts);
	bpt = (MIBreakpoint *)MIListGet(bpts);
	
	//remember the breakpoint for X10 hook breakpoints
	if (pBPNumber != NULL) {
		*pBPNumber = bpt->number;
	}

	if (remoteID != -1) {
		AddBPMap(bpt->number, remoteID, isTemp);
	}

	MIListFree(bpts, MIBreakpointFree);

	return DBGRES_OK;
}

/*
** Set breakpoint at start of specified function.
*/
static int
setInternalFuncBreakpoint(int *pBPNumber, int remoteID, int isTemp, int isHard, char *file, char *func, char *condition, int ignoreCount, int tid)
{
	int		res;
	char *	where;

	CHECK_SESSION();

	if (file == NULL || *file == '\0') 
		asprintf(&where, "%s", func);
	else
		asprintf(&where, "%s:%s", file, func);

	res = SetAndCheckInternalBreak(pBPNumber, remoteID, isTemp, isHard, where, condition, ignoreCount, tid);

	free(where);

	return res;
}


/*
** Set breakpoint at specified line.
*/
static int
setInternalLineBreakpoint(int *pBPNumber, int remoteID, int isTemp, int isHard, char *file, int line, char *condition, int ignoreCount, int tid, int needToMap)
{
	int		res;
	char *	where;

	CHECK_SESSION();

	if (file == NULL || *file == '\0') {
		asprintf(&where, "%d", line);
	}
	else if (0 == needToMap) {
		asprintf(&where, "%s:%d", file, line);
	}
	else {
		//Need to map the x10 location to CPP location
		int mappedCPPLine = 0;
		char *mappedCPPFile;
		X10LineToCPPLine(file, line, &mappedCPPFile, &mappedCPPLine);
		asprintf(&where, "%s:%d", mappedCPPFile, mappedCPPLine);
	}

	res = SetAndCheckInternalBreak(pBPNumber, remoteID, isTemp, isHard, where, condition, ignoreCount, tid);

	free(where);

	return res;
}



/*
 * Step out of a function.
 *
 */
static int
internalStepReturn(int count)
{
	MICommand *	cmd;

	CHECK_SESSION();

	cmd = MIExecFinish();

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
 * Step an instruction.
 *
 */
static int
internalStepInstruction()
{
	MICommand *	cmd;

	CHECK_SESSION();

	cmd = MIExecStepInstruction();

	SendCommandWait(DebugSession, cmd);

	if (!MICommandResultOK(cmd)) {
		SetDebugError(cmd);
		MICommandFree(cmd);
		return DBGRES_ERR;
	}

	MICommandFree(cmd);

	return DBGRES_OK;
}

/*
 * Set a function breakpoint at X10 STATEMENT HOOK.
 *
 */
static int setStatementHookBP(int *pBPNumber, int threadID)
{
 	return setInternalFuncBreakpoint(pBPNumber, -1, 0, 0, "", X10_STATEMENT_HOOK_NAME, "", 0, threadID);
}

/*
 * Read disassembly code from a memory location.
 *
 */
static List *
InternalReadDisassemble(char *startAddr, char *endAddr, char *format)
{
	MICommand *	cmd;
	MIDataReadDisassemblyInfo * info;
	MIDisassemblyInfo  * mi_dis;
	List *dis_list = NULL;
	DisassemblyInfo* dis_info;

	if (DebugSession == NULL) {
		return NULL;
	}

	cmd = MIDataReadDisassemble(startAddr, endAddr, format);
	SendCommandWait(DebugSession, cmd);
	if (!MICommandResultOK(cmd)) {
		SetDebugError(cmd);
		MICommandFree(cmd);
		return NULL;
	}
	info = MIGetDataReadDisassemblyInfo(cmd);
	MICommandFree(cmd);

	if (info != NULL) {
		
		if (info->asm_insns != NULL ) {
			dis_list = NewList();
			for (MIListSet(info->asm_insns); (mi_dis = (MIDisassemblyInfo *)MIListGet(info->asm_insns)) != NULL;) {
				dis_info = NewDisassemblyInfo();
				if (mi_dis->addr != NULL) {
					dis_info->addr = strdup(mi_dis->addr);
				}
				if (mi_dis->func_name!= NULL) {
					dis_info->func_name= strdup(mi_dis->func_name);
				}
				if (mi_dis->inst != NULL) {
					dis_info->inst= strdup(mi_dis->inst);
				}
				dis_info->offset = mi_dis->offset;
				
				AddToList(dis_list, (void *)dis_info);
			}
		}
	}

	return dis_list;
}



/*
** AsyncCallback is called by mi_get_response() when an async response is
** detected. It can't issue any gdb commands or there's a potential
** for deadlock. If commands need to be issues (e.g. to obtain
** current stack frame, they must be called from the main select
** loop using the AsyncFunc() mechanism.
*/
static void
X10AsyncCallback(MIEvent *evt)
{
	if (NULL == X10AsyncFunc) {
		X10AsyncFunc = X10AsyncStop;
		X10AsyncFuncData = (void *)evt;
	}
	else {
		X10AsyncFuncBackup = X10AsyncStop;
		X10AsyncFuncBackupData = (void *)evt;
	}
}

/*
** LogCallback is called by mi_get_response() when an MIOOBRecordLog response is
** detected. It can't issue any gdb commands or there's a potential
** for deadlock. If we detect the X10 address for X10 Meta data, we need to process it from the 
** main select loop.
*/
static void
LogCallback(char *str)
{
	/*  Check if the string input contains the record we want. 
	 ** If yes, we will process it to build up the X10 related maps.
	 */
	 //We are looking for _X10_DEBUG, instead of _X10_DEBUG_DATA
	if (X10MetaInfoAddrAvailable == 0 && (strstr(str, X10_DEBUG) != NULL && strstr(str, X10_DEBUG_DATA) == NULL)) {
		ProcessX10MapsFunc = ProcessX10Maps;
		ProcessX10MapsFuncData = (char *) malloc(strlen(str) * sizeof(char) + 1);
		strcpy(ProcessX10MapsFuncData, str);
		X10MetaInfoAddrAvailable = 1;
	}
}

/*
** StreamCallback is called by mi_get_response() when an MIOOBRecordTargetStream response is
** detected. 
*/
static void
X10StreamTargetCallback(char *str) {
	dbg_event *e = NewDbgEvent(DBGEV_OUTPUT);
	e->dbg_event_u.output = strdup(str);
	SaveEvent(e);
}

/*
 * Initialize GDB
 */
static int
X10GDBMIInit(void (*event_callback)(dbg_event *))
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
X10GDBMIStartSession(char *gdb_path, char *prog, char *path, char *work_dir, char **args, char **env, long timeout)
{
	char *		prog_path;
	char **		e;
	struct stat	st;
	MICommand *	cmd;
	MISession *	sess;
	float		version;

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

	MISessionRegisterEventCallback(sess, X10AsyncCallback);
	MISessionRegisterTargetCallback(sess, X10StreamTargetCallback);
	/* Xuan: Add here for the log callback */
	MISessionRegisterLogCallback(sess, LogCallback);
	MISessionRegisterConsoleCallback(sess, LogCallback);

	DebugSession = sess;

	cmd = CLIHandle(X10_SIGNAL_FILTER_COMMAND);
	SendCommandWait(sess, cmd);
	MICommandFree(cmd);

	cmd = CLIHandle(X10_SIGNAL_ADD_BACK_COMMAND_SIGSEGV);
	SendCommandWait(sess, cmd);
	MICommandFree(cmd);

	cmd = CLIHandle(X10_SIGNAL_ADD_BACK_COMMAND_SIGILL);
	SendCommandWait(sess, cmd);
	MICommandFree(cmd);

	cmd = CLIHandle(X10_SIGNAL_ADD_BACK_COMMAND_SIGFPE);
	SendCommandWait(sess, cmd);
	MICommandFree(cmd);

	cmd = CLIHandle(X10_SIGNAL_ADD_BACK_COMMAND_SIGSTOP);
	SendCommandWait(sess, cmd);
	MICommandFree(cmd);

	cmd = CLIHandle(X10_SIGNAL_ADD_BACK_COMMAND_SIGUSR1);
	SendCommandWait(sess, cmd);
	MICommandFree(cmd);

	cmd = CLIHandle(X10_SIGNAL_ADD_BACK_COMMAND_SIGUSR2);
	SendCommandWait(sess, cmd);
	MICommandFree(cmd);
	
	cmd = MICommandNew(X10_INFO_FILE_COMMAND, MIResultRecordDONE);
	SendCommandWait(sess, cmd);
	MICommandFree(cmd);

	Started = 0;
	SaveEvent(NewDbgEvent(DBGEV_OK));

	return DBGRES_OK;
}

/*
 * Progress gdb commands.
 */
static int
X10GDBMIProgress(void)
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
		 * Do any extra process X10 functions. We can call gdbmi safely here
		 */
		if (ProcessX10MapsFunc != NULL) {
			ProcessX10MapsFunc(ProcessX10MapsFuncData);
			ProcessX10MapsFunc = NULL;
			free(ProcessX10MapsFuncData);
		}
		
		/*
		 * Do any extra async functions. We can call gdbmi safely here
		 */
		if (X10AsyncFunc != NULL) {
			X10AsyncFunc(X10AsyncFuncData);
			X10AsyncFunc = NULL;
			return 0;
		}

		if (X10AsyncFuncBackup != NULL) {
			X10AsyncFuncBackup(X10AsyncFuncBackupData);
			X10AsyncFuncBackup = NULL;
			return 0;
		}
	}

	return 0;
}

/*
** Set breakpoint at specified line.
*/
static int
X10GDBMISetLineBreakpoint(int bpid, int isTemp, int isHard, char *file, int line, char *condition, int ignoreCount, int tid)
{
	int		res;
	char *	where;

	CHECK_SESSION();

	if (file == NULL || *file == '\0')
		asprintf(&where, "%d", line);
	else {
		//Need to map the x10 location to CPP location
		int mappedCPPLine = 0;
		char *mappedCPPFile = NULL;
		X10LineToCPPLine(file, line, &mappedCPPFile, &mappedCPPLine);
		if (NULL == mappedCPPFile || 0 == mappedCPPLine) {
			//mapping is not successfully.  return error.
			DbgSetError(DBGERR_NOFILE, file);
			return DBGRES_ERR; 
		}
		asprintf(&where, "%s:%d", mappedCPPFile, mappedCPPLine);
	}

	res = SetAndCheckBreakpoint(bpid, isTemp, isHard, where, condition, ignoreCount, tid);

	free(where);

	return res;
}

/*
** Set breakpoint at start of specified function.
*/
static int
X10GDBMISetFuncBreakpoint(int bpid, int isTemp, int isHard, char *file, char *func, char *condition, int ignoreCount, int tid)
{
	int		res;
	char *	where;

	CHECK_SESSION();
	
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
	MIList *			bpts;

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
X10GDBMIDeleteBreakpoint(int bpid)
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
X10GDBMIEnableBreakpoint(int bpid)
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
X10GDBMIDisableBreakpoint(int bpid)
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
X10GDBMIConditionBreakpoint(int bpid, char *expr)
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
X10GDBMIBreakpointAfter(int bpid, int icount)
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
X10GDBMIWatchpoint(int bpid, char *expr, int isAccess, int isRead, char *condition, int ignoreCount)
{
	dbg_event *		e;
	MIBreakpoint *	bpt;
	MICommand *		cmd;
	MIList *			bpts;
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
		X10GDBMIConditionBreakpoint(bpid, condition);
	}
	if (ignoreCount > 0) {
		X10GDBMIBreakpointAfter(bpid, ignoreCount);
	}

	e = NewDbgEvent(DBGEV_BPSET);
	e->dbg_event_u.bpset_event.bpid = bpid;
	e->dbg_event_u.bpset_event.bp = bp;
	SaveEvent(e);

	MIListFree(bpts, MIBreakpointFree);

	return DBGRES_OK;
}

/*
 * Start/continue executing program.
 */
static int
X10GDBMIGo(void)
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
 * Get a list of stack frame.
 */
static int
X10GetStackframes(int current, int low, int high, List **flist)
{
	MIList *		frames;
	MIFrame *	f;
	MICommand *	cmd;
	int		nframes;
	int		i;
	stackframe *	s;
	char *		file;
	int mappingFound = 0;
	int mappingFlag = 0;
	int displayAllFrames = 0; //do not filter any frame if "DER_DBG_SOURCE_FILTER == ALL"
	List *tmpList;
	float version;

	//checking gdb version
	if (current) {
		GetGDBVersion(DebugSession, &version);
		if (version > 6.3) {
			cmd = MIStackInfoFrame();
		}
		else {
			cmd = CLIFrame();
		}
	}
	else {
		if (low == 0 && high == 0) {
			cmd = MIStackListAllFrames();
		}
		else {
			cmd = MIStackListFrames(low, high);
		}
	}
	SendCommandWait(DebugSession, cmd);

	if (!MICommandResultOK(cmd)) {
		DEBUG_PRINTS(DEBUG_LEVEL_BACKEND, "------------------- GetStackframes error\n");
		SetDebugError(cmd);
		MICommandFree(cmd);
		return DBGRES_ERR;
	}

	if (current)
		frames = MIGetFrameInfo(cmd);
	else
		frames = MIGetStackListFramesInfo(cmd);

	MICommandFree(cmd);

	if ( frames == NULL ) {
		DbgSetError(DBGERR_DEBUGGER, "Failed to get stack frames from backend");
		return DBGRES_ERR;
	}

	char *envText = getenv("DER_DBG_SOURCE_FILTER");
	if (envText != NULL && strcmp(envText, "ALL") == 0) {
		displayAllFrames = 1;
	}
	if (envText != NULL) {
		free(envText);
		envText = NULL;
	}

	//Filter non-X10 frames if DER_DBG_SOURCE_FILTER is set to values other than "ALL".
	*flist = NewList();
	tmpList = NewList();
	i = 0;
	nframes = 0;
	for (MIListSet(frames); (f = (MIFrame *)MIListGet(frames)) != NULL; i++) {
		s = X10ConvertMIFrameToStackframe(f,&mappingFlag);
		if(mappingFlag > 0)
			mappingFound++;

		file = s->loc.file;
		// If top stack frame or some line mapping found or all frames required being displayed
		AddToList(tmpList,(void *)s);//Keep the tmpList until we found mappingFound > 0 after this for-loop
		if (i == 0 || mappingFlag > 0 || displayAllFrames == 1) {
			stackframe *dup = NewStackframe(s->level);
			dup->gdb_level = s->gdb_level;
			if (s->loc.addr != NULL ) {
				dup->loc.addr = strdup(s->loc.addr);
			}
			if (s->loc.file != NULL) {
				dup->loc.file = strdup(s->loc.file);
			}
			if (s->loc.func != NULL) {
				dup->loc.func = strdup(s->loc.func);
			}
			dup->loc.line = s->loc.line;
			AddToList(*flist, (void *)dup);
			nframes++;
		} 
	}

	//If no mapping is found within all frames, then add them all.
	if(mappingFound == 0) {
		DestroyList(*flist, FreeStackframe);
		*flist = tmpList;
	}
	else {
		DestroyList(tmpList, FreeStackframe);
	}
	
		
	MIListFree(frames, MIFrameFree);

	//We need to update the level properly since some of the stack frames are filtered out.
	i = 0;
	for (SetList(*flist); (s = (stackframe *)GetListElement(*flist)) != NULL; i++) {
			s->level = i;
	}

	return DBGRES_OK;
}

/*
 * Get the source of the current executable line.
 */
static char *
getCurrentSourceLine()
{
	MICommand *	cmd;
	
	cmd = CLIListSourceLines();
	SendCommandWait(DebugSession, cmd);
	if (!MICommandResultOK(cmd)) {
		DEBUG_PRINTS(DEBUG_LEVEL_BACKEND, "------------------- CLIListSourceLines error\n");
		SetDebugError(cmd);
		MICommandFree(cmd);
		return NULL;
	}
	char *result = CLIGetCurrentSourceLine(cmd);
	
	MICommandFree(cmd);
	return result;
}

/*
 * Decidie if we need to skip a statement hook or not.
 */
static int 
determineToSkipStatementHook()
{
	//We will check the current source line to determine if we need to skip statement hook or not.
	//Will not use X10->C++ mapping any more.

	int result = 0;
	
	//We need to determine if the source line has X10_STATEMENT_HOOK_NAME
	char *currentSourceLine = getCurrentSourceLine();
	if (currentSourceLine != NULL)
	{
		if (NULL != strstr(currentSourceLine, X10_STATEMENT_HOOK_NAME))
		{
			result = 1;
		}
		free(currentSourceLine);
		currentSourceLine = NULL;	
	}
	
	return result;
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
X10GDBMIStep(int count, int type)
{
	MICommand *	cmd;
	int stmtHookBpNumber = -1;
	int returnCode = DBGRES_OK;
	thread_entry *threadEntry;
	int stepOverCount = 1;
	
	CHECK_SESSION();
	
	switch ( type ) {
	case STEP_REQUEST_STEPINTO:
		//this is for step into.  Set breakpoint on statement hook for this thread and run.
		returnCode = setStatementHookBP(&stmtHookBpNumber, Current_Thread_id);
		if (DBGRES_OK == returnCode) {
			threadEntry = FindThreadEntry(Current_Thread_id);
			if (NULL != threadEntry) {
				//Need to determine if we need to skip one bp hit on statement hook
			   	//. If the thread stop status is THREAD_STATUS_STOP_BP_HIT
			   	//. Get the top stack frame, and check if the C++ line is in the 
			   	//  X10->C++ mapping table.  
			   	//. If yes, it means there will be a statement hook added to this C++ line
			   	//  Then we need to skip once for BP in the statement hook.
			   	if (threadEntry->stop_status == THREAD_STATUS_STOP_BP_HIT || threadEntry->stop_status == THREAD_STATUS_STOP_STEP_OVER) {
			   		int result = determineToSkipStatementHook();
			   		threadEntry->skip_statement_hook = result;
			   	}
				threadEntry->statement_hook_bp_num = stmtHookBpNumber;
			}
			else {
				AddThreadMap(Current_Thread_id, THREAD_STATUS_STOP_UNKNOWN, stmtHookBpNumber, 0);
			}
			X10GDBMIGo();
			return DBGRES_OK;
		}
		else
		{
			//We cannot set breakpoint on statement hook.  Just simply do a step into.
			cmd = MIExecStep(count);
			break;
		}

	case STEP_REQUEST_STEPOVER:
		//this is for step over.  We will step until we land on a C++ line which is in X10 to C++ map
		//first, remove statement hook breakpoint if there is one
		threadEntry = FindThreadEntry(Current_Thread_id);
		if (NULL != threadEntry) {
			if (threadEntry->statement_hook_bp_num > 0) {
				deleteInternalBreakpoint(threadEntry->statement_hook_bp_num);
				threadEntry->statement_hook_bp_num = -1;
			}
		   	if (threadEntry->stop_status == THREAD_STATUS_STOP_BP_HIT || threadEntry->stop_status == THREAD_STATUS_STOP_STEP_OVER) {
		   		int result = determineToSkipStatementHook();
		   		if (result) {
		   			stepOverCount++;
		   		}
		   	}
		}
		else {
			AddThreadMap(Current_Thread_id, THREAD_STATUS_STOP_UNKNOWN, stmtHookBpNumber, 0);
		}
		cmd = MIExecNext(stepOverCount);
		break;
		
	case STEP_REQUEST_STEPRETURN:
		threadEntry = FindThreadEntry(Current_Thread_id);
		if (NULL != threadEntry) {
			threadEntry->stop_status = THREAD_STATUS_STEP_RETURN_REQUESTED;
		}
		else {
			AddThreadMap(Current_Thread_id, THREAD_STATUS_STEP_RETURN_REQUESTED, -1, 0);
		}
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
X10GDBMITerminate(void)
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
X10GDBMIInterrupt(void)
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
X10GDBMISetCurrentStackframe(int level)
{
	MICommand *	cmd;
	int gdb_level = -1;

	CHECK_SESSION();

	//Since for the X10 program, its stack level may not be the same level as returned by gdb 
	//We need to find out its corresponding gdb level, and make the "-stack-select-frame" call
	thread_entry *currentThreadEntry = FindThreadEntry(Current_Thread_id);
	if (NULL != currentThreadEntry)
	{
		if (NULL != currentThreadEntry->stackframes) {
			//Go through the stackframes
			stackframe *s;
			for (SetList(currentThreadEntry->stackframes); ((s = (stackframe *)GetListElement(currentThreadEntry->stackframes)) != NULL);)  {
				if (s->level == level) {
					gdb_level = s->gdb_level;
				}
			}
		}
	}
	if (-1 == gdb_level) {
		//use level instead
		gdb_level = level;
	}
	
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
X10GDBMIListStackframes(int low, int high)
{
	dbg_event *	e;
	List *		returnedFrames = NULL;
	List *		stackFrames = NULL;

	CHECK_SESSION();

	//Find out the stack frames in the current thread.
	thread_entry *currentThreadEntry = FindThreadEntry(Current_Thread_id);
	if (NULL == currentThreadEntry) {
		currentThreadEntry = AddThreadMap(Current_Thread_id, THREAD_STATUS_STOP_UNKNOWN, -1, 0);
	}
	
	
	if (NULL == currentThreadEntry->stackframes) {
		//We haven't get the stack yet, or stack is not valid any more.
		int returnCode = X10GetStackframes(0, 0, 0, &stackFrames);
		if (DBGRES_OK != returnCode)
		{
			return returnCode;
		}
		currentThreadEntry->stackframes = stackFrames;
	}

	if (NULL != currentThreadEntry->stackframes) {
		//We need to make a copy of the stackframes
		int frameIndex = 0;
		stackframe *s;
		returnedFrames = NewList();
		for (SetList(currentThreadEntry->stackframes); ((s = (stackframe *)GetListElement(currentThreadEntry->stackframes)) != NULL) && frameIndex <= high;) {
			if (frameIndex >= low && frameIndex <= high) {
				stackframe *dup = NewStackframe(s->level);
				dup->gdb_level = s->gdb_level;
				if (s->loc.addr != NULL ) {
					dup->loc.addr = strdup(s->loc.addr);
				}
				if (s->loc.file != NULL) {
					dup->loc.file = strdup(s->loc.file);
				}
				if (s->loc.func != NULL) {
					dup->loc.func = strdup(s->loc.func);
				}
				dup->loc.line = s->loc.line;
				AddToList(returnedFrames, (void *)dup);
			}
		}
	}

	if (NULL == returnedFrames) {
		return DBGRES_ERR;
	}

	e = NewDbgEvent(DBGEV_FRAMES);
	
	e->dbg_event_u.list = returnedFrames;
	SaveEvent(e);

	return DBGRES_OK;
}


/*
** List local variables.
*/
static int
X10GDBMIGetLocalVariables(void)
{
	
	dbg_event *	e;
	//get the current frame
	stackframe * s = GetCurrentFrame(DebugSession);
	//Get the current C++ top stack frame (only for now, this need to be changed)
	if (NULL == s) {
		return DBGRES_ERR;
	}

	if (NULL != localVars) {
		DestroyList(localVars, free);
		localVars = NULL;
	}
	
	localVars = GetX10LocalVarNames(s->loc.file, s->loc.line);
	
	e = NewDbgEvent(DBGEV_VARS);
	e->dbg_event_u.list = NewList();

	char* name;
	for (SetList(localVars); (name = (char *)GetListElement(localVars)) != NULL; ) {
		AddToList(e->dbg_event_u.list, (void *)strdup(name));
	}
	//We don't destory it.
	//DestroyList(localVars, free);
	SaveEvent(e);
	return DBGRES_OK;
}


/*
 * List arguments, which basically don't do anything for X10 case, since arguement will be 
 * included in the local variable mapping, which will be retrieved by X10GDBMIGetLocalVariables()
 */
static int
X10GDBMIListArguments(int low, int high)
{
	dbg_event *	e;

	e = NewDbgEvent(DBGEV_ARGS);
	e->dbg_event_u.list = NewList();

	SaveEvent(e);

	return DBGRES_OK;
}

/*
 * List global variables.
 */
static int
X10GDBMIGetGlobalVariables(void)
{
	CHECK_SESSION();

	DbgSetError(DBGERR_NOTIMP, NULL);
	return DBGRES_ERR;
}

/*
** Quit debugger.
*/
static int
X10GDBMIQuit(void)
{
	MICommand *	cmd;

	if (DebugSession != NULL) {
		cmd = MIGDBExit();
		SendCommandWait(DebugSession, cmd);
		MICommandFree(cmd);
	}
	ClearBPMaps();
	ClearThreadMaps();
	ClearMetaDebugInfoMaps();
	SaveEvent(NewDbgEvent(DBGEV_OK));
	ServerExit++;

	return DBGRES_OK;
}

/*
 * Retrieve thread information.
 */
static int
X10GDBMIGetInfoThread(void)
{
	MICommand *	cmd;
	dbg_event *	e;
	char *		tid;
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
		int threadID = strtol(tid, NULL, 10);
		thread_entry *thisThreadEntry = FindThreadEntry(threadID);
		if (NULL != thisThreadEntry) {
			if (threadID != Current_Thread_id && NULL != thisThreadEntry->stackframes) {
				//the stack information is not valid any more.
				DestroyList(thisThreadEntry->stackframes, FreeStackframe);
				thisThreadEntry->stackframes = NULL;
			}
		}
	}

	MIListFree(info->thread_ids, free);
	free(info);
	SaveEvent(e);

	return DBGRES_OK;
}

/*
 * Set the current thread to input threadNum.
 */
static int
X10GDBMISetThreadSelect(int threadNum)
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
		s = X10ConvertMIFrameToStackframe(info->frame, NULL);
	}
	e = NewDbgEvent(DBGEV_THREAD_SELECT);
	e->dbg_event_u.thread_select_event.thread_id = info->current_thread_id;
	e->dbg_event_u.thread_select_event.frame = s;

	Current_Thread_id = threadNum;
	thread_entry *thisThreadEntry = FindThreadEntry(threadNum);
	if (NULL == thisThreadEntry) {
		AddThreadMap(threadNum, THREAD_STATUS_STOP_UNKNOWN, -1, 0);
	}
	
	MIFrameFree(info->frame);
	free(info);
	SaveEvent(e);

	return DBGRES_OK;
}

/*
 * Retrieve the stackframes of the current thread (if not retrieved yet), and return its depth.
 */
static int
X10GDBMIStackInfoDepth()
{
	int depth = 0;
	dbg_event *	e;
	List *	stackframes = NULL;

	CHECK_SESSION();

	thread_entry *currentThreadEntry = FindThreadEntry(Current_Thread_id);
	if (NULL == currentThreadEntry) {
		currentThreadEntry = AddThreadMap(Current_Thread_id, THREAD_STATUS_STOP_UNKNOWN, -1, 0);
	}
	
	if (NULL == currentThreadEntry->stackframes) {
		//We haven't get the stack yet, or stack is not valid any more.
		int returnCode = X10GetStackframes(0, 0, 0, &stackframes);
		if (DBGRES_OK != returnCode)
		{
			return returnCode;
		}
		currentThreadEntry->stackframes = stackframes;
	}
	if (NULL != currentThreadEntry->stackframes) {
		depth = SizeOfList(currentThreadEntry->stackframes);
	}

	e = NewDbgEvent(DBGEV_STACK_DEPTH);
	e->dbg_event_u.stack_depth = depth;
	SaveEvent(e);
	return DBGRES_OK;
}

/*
 * Read memory contents of a memory location.
 */
static int
X10GDBMIDataReadMemory(long offset, char* address, char* format, int wordSize, int rows, int cols, char* asChar)
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


/*
 * Write input content to a memory location.
 */
static int
X10GDBMIDataWriteMemory(long offset, char* address, char* format, int wordSize, char* value)
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

/*
 * List a signal.
 */
static int
X10GDBCLIListSignals(char* name)
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
		if (sig->desc != NULL) {
			s->desc = strdup(sig->desc);
		}
		else {
			s->desc = strdup(" ");
		}
		AddToList(e->dbg_event_u.list, s);
	}
	SaveEvent(e);
	MIListFree(signals, MISignalInfoFree);

	return DBGRES_OK;
}

/*
 * Invoke gdb handle command on the input arguement.
 */
static int
X10GDBCLIHandle(char *arg)
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
** Find native type of variable.
*/
static int
X10GDBMIGetNativeType(char *var)
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
 * Retrieve the stackframes of the current thread (if not retrieved yet), and return its depth.
 */
static int
X10GetStackInfoDepth()
{
	int depth;
	List *	stackframes;

	CHECK_SESSION();
	int returnCode = X10GetStackframes(0, 0, 0, &stackframes);
	if (DBGRES_OK != returnCode) {
		return -1;
	}
	//make sure update the stack of the current thread to this value.
	//Find out the stack frames in the current thread.
	thread_entry *currentThreadEntry = FindThreadEntry(Current_Thread_id);
	if (NULL == currentThreadEntry) {
		currentThreadEntry = AddThreadMap(Current_Thread_id, THREAD_STATUS_STOP_UNKNOWN, -1, 0);
	}

	if (NULL != currentThreadEntry) {
		//Update the stack of this thread entry with the new value.
		if (NULL != currentThreadEntry->stackframes) {
			DestroyList(currentThreadEntry->stackframes, FreeStackframe);
		}
		currentThreadEntry->stackframes = stackframes;
	}
	depth = SizeOfList(stackframes);
	return depth;
}

/*
 * This function will be called when there an async response is detected, and AsyncCallback is called.  It will be called
 * after the processing of the current gdb command, from the main select loop.
 * You can issue additional gdb command in this function.
 */
static int
X10AsyncStop(void *data)
{
	dbg_event *		e;
	stackframe *	frame;
	MIEvent *		evt = (MIEvent *)data;
	List *assembles = NULL;
	Current_Thread_id = evt->threadId;
	int needToSkipStatementHookBP = 0;
	int cppLineForX10Main = -1;
	int returnCode = DBGRES_OK;
	int flag = 0;
	int lastRequestStepNop = 0;
	MICommand *	cmd;
	int stmtHookBpNumber = -1;
	char *cppFileNameForX10Main = NULL;
	int foundCppLineForX10Main = 0;

	thread_entry *threadEntry = FindThreadEntry(Current_Thread_id);
	if (evt->frame != NULL && evt->frame->file!= NULL) {
		flag = CppLineInX10ToCPPMap(evt->frame->file, evt->frame->line);
	}

	switch (evt->type) {
	case MIEventTypeBreakpointHit:
		//If we hit main() method in C++, we need to set C++ line for X10 line.
		if (0 == strcmp(evt->frame->func, "main") ) {
			foundCppLineForX10Main = MapX10MainToCPPLine(&cppFileNameForX10Main, &cppLineForX10Main);
			if (0 != foundCppLineForX10Main) {
				RemoveBPMap(evt->bkptno);  //remove the temp breakpoint from breakpoint map.
				returnCode = setInternalLineBreakpoint(NULL, 0, 1, 0, cppFileNameForX10Main, cppLineForX10Main, "", 0, 0, 0); //replace it with this breakpoint.
				free(cppFileNameForX10Main);
				if (returnCode == DBGRES_OK) {
					X10GDBMIGo();
					return DBGRES_OK;
				}
			}
		}
		//this is the case where statement hook breakpoint is hit
		//. if the step request is step into, 
		//    when statement hook is hit, it means it encounters the 
		//    next X10 statement, and does not matter if it is inside an X10 function call, 
		//    or in the same level.
		//    just step return to get out of the statement hook.
		//. if the step request is step over
		//    when statement hook is hit, it means it does not encounter
		//    any entry hook in between, and the statement hook is from the
		//    x10 statement of the same level.
		//    just step return to get out of the statement hook.
		else if (0 == strcmp(evt->frame->func, X10_STATEMENT_HOOK_NAME)) {
			if (NULL != threadEntry) {
				needToSkipStatementHookBP = threadEntry->skip_statement_hook;
				if (needToSkipStatementHookBP) {
					//We need to skip this breakpoint
					threadEntry->skip_statement_hook = 0;  //reset this flag
					X10GDBMIGo();
					return DBGRES_OK;
				}
				if (threadEntry->statement_hook_bp_num > 0) {
					deleteInternalBreakpoint(threadEntry->statement_hook_bp_num);
					threadEntry->statement_hook_bp_num = -1;
				}
				threadEntry->stop_status = THREAD_STATUS_STOP_STEP_STMT_HOOK_BP_HIT;
			}
			else {
				AddThreadMap(Current_Thread_id, THREAD_STATUS_STOP_STEP_STMT_HOOK_BP_HIT, -1, 0);
			}
			internalStepReturn(1);
			return DBGRES_OK;
		}
		else {
			//regular breakpoint hit, Set the status of this thread to THREAD_STATUS_STOP_BP_HIT
			if (NULL != threadEntry) {
				threadEntry->stop_status = THREAD_STATUS_STOP_BP_HIT;
				if (threadEntry->statement_hook_bp_num > 0) {
					deleteInternalBreakpoint(threadEntry->statement_hook_bp_num);
					threadEntry->statement_hook_bp_num = -1;
				}
			}
			else {
				AddThreadMap(Current_Thread_id, THREAD_STATUS_STOP_BP_HIT, -1, 0);
			}
		}
		
		if (!IsTempBP(evt->bkptno)) {
			e = NewDbgEvent(DBGEV_SUSPEND);
			e->dbg_event_u.suspend_event.reason = DBGEV_SUSPEND_BPHIT;
			e->dbg_event_u.suspend_event.ev_u.bpid = GetRemoteBPID(evt->bkptno);
			e->dbg_event_u.suspend_event.thread_id = evt->threadId;
			e->dbg_event_u.suspend_event.frame = NULL;
			e->dbg_event_u.suspend_event.depth = X10GetStackInfoDepth();
			//e->dbg_event_u.suspend_event.changed_vars = GetChangedVariables(DebugSession);
			if (localVars != NULL) {
				e->dbg_event_u.suspend_event.changed_vars = localVars;
				localVars = NULL;
			}
			else {
				//give it an empty list
				e->dbg_event_u.suspend_event.changed_vars = NewList();
			}
			break;
		}
		
		RemoveBPMap(evt->bkptno);
		if (evt->frame != NULL) {
			frame = X10ConvertMIFrameToStackframe(evt->frame, NULL);
		}
		else {
			frame = GetCurrentFrame(DebugSession);
		}
		if (frame == NULL) {
			ERROR_TO_EVENT(e);
		}
		else {
			e = NewDbgEvent(DBGEV_SUSPEND);
			e->dbg_event_u.suspend_event.reason = DBGEV_SUSPEND_INT;
			e->dbg_event_u.suspend_event.thread_id = evt->threadId;
			e->dbg_event_u.suspend_event.frame = frame;
			e->dbg_event_u.suspend_event.depth = X10GetStackInfoDepth();
			//e->dbg_event_u.suspend_event.changed_vars = GetChangedVariables(DebugSession);
			if (localVars != NULL) {
				e->dbg_event_u.suspend_event.changed_vars = localVars;
				localVars = NULL;
			}
			else
			{
				//give it an empty list
				e->dbg_event_u.suspend_event.changed_vars = NewList();
			}
			break;
		}
		break;

	case MIEventTypeSuspended:
		
		//If we hit main() method in C++, we need to set C++ line for X10 line.
		if (0 == strcmp(evt->frame->func, "main") ) {
			foundCppLineForX10Main = MapX10MainToCPPLine(&cppFileNameForX10Main, &cppLineForX10Main);
			if (0 != foundCppLineForX10Main) {
				RemoveBPMap(evt->bkptno);  //remove the temp breakpoint from breakpoint map.
				returnCode = setInternalLineBreakpoint(NULL, 0, 1, 0, cppFileNameForX10Main, cppLineForX10Main, "", 0, 0, 0);
				free(cppFileNameForX10Main);
				if (returnCode == DBGRES_OK) {
					X10GDBMIGo();
					return returnCode;
				}
				else {
					frame = GetCurrentFrame(DebugSession);
				}
			}
		}
		if (evt->frame != NULL) {
			frame = X10ConvertMIFrameToStackframe(evt->frame, NULL);
		}
		else {
			frame = GetCurrentFrame(DebugSession);
		}
		if (frame == NULL) {
			ERROR_TO_EVENT(e);
		}
		else {
			e = NewDbgEvent(DBGEV_SUSPEND);
			e->dbg_event_u.suspend_event.reason = DBGEV_SUSPEND_INT;
			e->dbg_event_u.suspend_event.thread_id = evt->threadId;
			e->dbg_event_u.suspend_event.frame = frame;
			e->dbg_event_u.suspend_event.depth = X10GetStackInfoDepth();
			//e->dbg_event_u.suspend_event.changed_vars = GetChangedVariables(DebugSession);
			if (localVars != NULL) {
				e->dbg_event_u.suspend_event.changed_vars = localVars;
				localVars = NULL;
			}
			else {
				//give it an empty list
				e->dbg_event_u.suspend_event.changed_vars = NewList();
			}
			break;
		}
		break;

	case MIEventTypeFunctionFinished:
		if (THREAD_STATUS_STEP_RETURN_REQUESTED == threadEntry->stop_status)
		{
			//We will set a breakpoint on Statement hook, and run.
			returnCode = setStatementHookBP(&stmtHookBpNumber, Current_Thread_id);
			if (DBGRES_OK == returnCode) {
				threadEntry->statement_hook_bp_num = stmtHookBpNumber;
				X10GDBMIGo();
				return returnCode;
			}
		}
		
		assembles = InternalReadDisassemble("$pc", "\"$pc + 4\"", "0");
		SetList(assembles); 
		DisassemblyInfo *assemble = (DisassemblyInfo *)GetListElement(assembles);
		if (0 == strcmp(assemble->inst, "nop")) {
			//this is the nop instruction we are stopping.  Need to issue a -exec-step-instruction
			DestroyList(assembles, FreeDisassemblyInfo);
			if (NULL != threadEntry) {
				threadEntry->step_nop = 1;
			}
			else {
				AddThreadMap(Current_Thread_id, THREAD_STATUS_STOP_STEP_OVER, -1, 1);
			}
			return internalStepInstruction();
		}
		DestroyList(assembles, FreeDisassemblyInfo);
		
		if (evt->frame != NULL) {
			frame = X10ConvertMIFrameToStackframe(evt->frame, NULL);
		}
		else {
			frame = GetCurrentFrame(DebugSession);
		}
		
		if (frame == NULL) {
			ERROR_TO_EVENT(e);
		}
		else {
			e = NewDbgEvent(DBGEV_SUSPEND);
			e->dbg_event_u.suspend_event.reason = DBGEV_SUSPEND_STEP;
			e->dbg_event_u.suspend_event.thread_id = evt->threadId;
			e->dbg_event_u.suspend_event.frame = frame;
			e->dbg_event_u.suspend_event.depth = X10GetStackInfoDepth();
			//e->dbg_event_u.suspend_event.changed_vars = GetChangedVariables(DebugSession);
			if (localVars != NULL) {
				e->dbg_event_u.suspend_event.changed_vars = localVars;
				localVars = NULL;
			}
			else
			{
				//give it an empty list
				e->dbg_event_u.suspend_event.changed_vars = NewList();
			}
			break;
		}
		break;
		
	case MIEventTypeSteppingRange:
		//Step over only considered to be completed when the stopped C++ line can be found in the 
		//X10 to C++ mapping
		if (NULL != threadEntry)
		{
			lastRequestStepNop = threadEntry->step_nop;
			if (1 == threadEntry->step_nop) {
				threadEntry->step_nop = 0;
			}
			else {
				threadEntry->stop_status = THREAD_STATUS_STOP_STEP_OVER;
			}
		}
		else {
			AddThreadMap(Current_Thread_id, THREAD_STATUS_STOP_STEP_OVER, -1, 0);
		}
		if (lastRequestStepNop == 0) {
			if (NULL == evt->frame || NULL == evt->frame->file) {
				//probably step to the place we cannot step any more.  Simply do a run
				X10GDBMIGo();
				return DBGRES_OK;
			}
			if (0 == flag) {
				//We need to continue stepping over ("next" command)
				cmd = MIExecNext(1);
				SendCommandWait(DebugSession, cmd);
				return DBGRES_OK;
			}
		}
		if (evt->frame != NULL) {
			frame = X10ConvertMIFrameToStackframe(evt->frame, NULL);
		}
		else {
			frame = GetCurrentFrame(DebugSession);
		}
		if (frame == NULL) {
			ERROR_TO_EVENT(e);
		}
		else {
			e = NewDbgEvent(DBGEV_SUSPEND);
			e->dbg_event_u.suspend_event.reason = DBGEV_SUSPEND_STEP;
			e->dbg_event_u.suspend_event.thread_id = evt->threadId;
			e->dbg_event_u.suspend_event.frame = frame;
			e->dbg_event_u.suspend_event.depth = X10GetStackInfoDepth();
			//e->dbg_event_u.suspend_event.changed_vars = GetChangedVariables(DebugSession);
			if (localVars != NULL) {
				e->dbg_event_u.suspend_event.changed_vars = localVars;
				localVars = NULL;
			}
			else {
				//give it an empty list
				e->dbg_event_u.suspend_event.changed_vars = NewList();
			}
			break;
		}
		break;

	case MIEventTypeSignal:
		if (evt->frame != NULL) {
			frame = X10ConvertMIFrameToStackframe(evt->frame, NULL);
		}
		else {
			frame = GetCurrentFrame(DebugSession);
		}
		if (frame == NULL) {
			ERROR_TO_EVENT(e);
		}
		else {
			e = NewDbgEvent(DBGEV_SUSPEND);
			e->dbg_event_u.suspend_event.reason = DBGEV_SUSPEND_SIGNAL;
			e->dbg_event_u.suspend_event.ev_u.sig = NewSignalInfo();
			e->dbg_event_u.suspend_event.ev_u.sig->name = strdup(evt->sigName);
			e->dbg_event_u.suspend_event.ev_u.sig->desc = strdup(evt->sigMeaning);
			e->dbg_event_u.suspend_event.thread_id = evt->threadId;
			e->dbg_event_u.suspend_event.frame = frame;
			e->dbg_event_u.suspend_event.depth = X10GetStackInfoDepth();
			//e->dbg_event_u.suspend_event.changed_vars = GetChangedVariables(DebugSession);
			if (localVars != NULL) {
				e->dbg_event_u.suspend_event.changed_vars = localVars;
				localVars = NULL;
			}
			else {
				//give it an empty list
				e->dbg_event_u.suspend_event.changed_vars = NewList();
			}
			break;
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
 * Parse an X10 expression name to find out the parent/children relationship of this expression.
 */
int 
parseX10ExpressionName(char *expr, char **pRootName, List **pOffsprings, long *pArraySize, long *pArrayStartIndex)
{
	long arraySize = -1;
	long arrayStartIndex = -1;
	List *offsprings = NULL;
	
	//An X10 variable could be something like the following:
	//"a":  a regular variable name
	//"(*a + 10)@10", a is an array, and it want 10 elements from index 0
	//"((a).b)": field b of class variable a
	//"(((((a).b).c).e)+0)@10

	//if expr does not contain ")", the rootName is the expr
	if (NULL == strchr(expr, ')')) {
		*pRootName = strdup(expr);
		*pOffsprings = NULL;
		return 0;
	}

	char *tmpExpr = strdup(expr);

	//Check if expr contains '@'.  If yes, it is an array
	char *tmp = strchr(expr, '@');
	if (tmp != NULL) {
		//it is an array.  Find out the array size and starting index
		char *arraySizeString = tmp + 1;
		arraySize = strtol(arraySizeString, NULL, 10);
		//find the index for '+'
		char *plusIndex = strchr(expr, '+');
		//find the last index of ')'
		char *lastRightBracket = strrchr(expr, ')');
		long len = lastRightBracket - plusIndex - 1;
		char *startIndexString = (char*)malloc(len + 1);
		memcpy(startIndexString, plusIndex+1, len);
		startIndexString[len] = '\0';
		arrayStartIndex = strtol(startIndexString, NULL, 10);
		//Get the string before '+'
		tmpExpr = strtok(tmpExpr, "+");
	}

	//First, find the rootName
	char *firstRightBracket = strchr(tmpExpr, ')');
	//Find the last '(' in the expression
	char *lastLeftBracket = strrchr(tmpExpr, '(');
	long len = firstRightBracket - lastLeftBracket - 1;
	char* rootName = (char*)malloc(len+1);
	memcpy(rootName, lastLeftBracket + 1, len);
	rootName[len] = '\0';

	tmpExpr = firstRightBracket + 1;
	//Now, get the offsprings name;
	tmp = strtok(tmpExpr, ")");
	while(tmp != NULL) {
		//skip '.'
		if (offsprings == NULL) {
			offsprings = NewList();
		}
		AddToList(offsprings, (void *)strdup(tmp+1));
		tmp = strtok(NULL, ")");
	}

	*pRootName = rootName;
	*pOffsprings = offsprings;
	*pArraySize = arraySize;
	*pArrayStartIndex = arrayStartIndex;

	//free(tmpExpr);
	
	return 0;
	
}


/*
 * Evaluate a partial expression.
 *
 * - Parse the expression name to find out the parent/chid relation of this expression.
 * - Call GetX10VariableDetails() to build a tree relationship of this expression.
 * - Call X10GetPartialAIF() to build the AIF records of this expression.
 * - If we cannot find this expression by calling GetX10VariableDetails(), will fall back to just evaluate it
 *   it as a C++ expression.
 */
static int
X10EvaluatePartialExpression(char* expr, char* var_id, int list_child, int express)
{
	
	AIF *		a;
	dbg_event *	e;
	//get the sp of the top frame;
	stackframe * s = NULL;
	char *rootName = NULL;
	List *offsprings = NULL;
	long arraySize = -1;
	long arrayStartIndex = -1;
	
	//Get the current C++ top stack frame (only for now, this need to be changed)
	s = GetCurrentFrame(DebugSession);
	if (NULL == s) {
		return 0;
	}
	
	//First, parse the expression name.  
	parseX10ExpressionName(expr, &rootName, &offsprings, &arraySize, &arrayStartIndex);
	
	x10_var_t *x10Var = GetX10VariableDetails(DebugSession, s->loc.file, s->loc.line, rootName, offsprings, arraySize, arrayStartIndex, list_child);
 
	if (x10Var == NULL) {
		//We cannot find this variable in the Meta debug mapping for variable.
		//Will return as ERROR so that it will be handled by X10GDBMIEvaluatePartialExpression.
		return DBGRES_ERR;
	}
	
	a = X10GetPartialAIF(DebugSession, expr, x10Var);
	if (a == NULL) {
		DbgSetError(DBGERR_UNKNOWN_TYPE, "Unknown type");
		X10VarFree(x10Var);
		return DBGRES_ERR;
	}
	e = NewDbgEvent(DBGEV_DATA);
	e->dbg_event_u.data_event.data = a;
	e->dbg_event_u.data_event.type_desc = strdup(x10Var->var->type_name);
	e->dbg_event_u.data_event.name = strdup(x10Var->var->name);

	SaveEvent(e);
	X10VarFree(x10Var);
	return DBGRES_OK;
}


/*
** Evaluate the expression exp.
*/
static int
X10GDBMIEvaluateExpression(char *exp)
{
	AIF *		a;
	dbg_event *	e;
	stackframe * s;
	
	//Get the current C++ top stack frame (only for now, this need to be changed)
	s = GetCurrentFrame(DebugSession);
	if (NULL == s) {
		return DBGRES_ERR;
	}
	
	x10_var_t *x10Var = GetX10VariableDetails(DebugSession, s->loc.file, s->loc.line, exp, NULL, 0, 0, 0);
	FreeStackframe(s);

	if (x10Var == NULL) {
		DbgSetError(DBGERR_UNKNOWN_VARIABLE, exp); 
		return DBGRES_ERR;
	}
	
	a = X10GetAIF(DebugSession, exp, x10Var);
	if (a == NULL) {
		DbgSetError(DBGERR_UNKNOWN_TYPE, "Unknown type");
		X10VarFree(x10Var);
		return DBGRES_ERR;
	}
	e = NewDbgEvent(DBGEV_DATA);
	e->dbg_event_u.data_event.data = a;
	e->dbg_event_u.data_event.type_desc = strdup(x10Var->var->type_name);
	e->dbg_event_u.data_event.name = strdup(x10Var->var->name);

	SaveEvent(e);
	X10VarFree(x10Var);
	return DBGRES_OK;
}

/*
 * Evaluate a partial expression as C++ variable.
 *
 */
static int
X10GDBEvaluatePartialExpression(char* expr, char* var_id, int list_child, int express)
{
	MIVar *		mivar = NULL;
	AIF *		a;
	dbg_event *	e;

	int returnCode = X10EvaluatePartialExpression(expr, var_id, list_child, express);
	if (DBGRES_OK == returnCode)
	{
		return returnCode;
	}
	//We cannot evaluate this expression as X10 variable.  Fallback to C/C++ variable.
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
		//Only call it when list_child is >0
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

/*
 * Delete a variable from gdb.
 *
 */
static int
X10GDBMIVarDelete(char *name)
{
	CHECK_SESSION();
	SaveEvent(NewDbgEvent(DBGEV_OK));
	return DBGRES_OK;
}

