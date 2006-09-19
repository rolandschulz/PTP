/*
** GDB MI interface routines
**
** Copyright (c) 1996-2002 by Guardsoft Pty Ltd.
**
** This program is free software; you can redistribute it and/or modify
** it under the terms of the GNU General Public License as published by
** the Free Software Foundation; either version 2 of the License, or
** (at your option) any later version.
**
** This program is distributed in the hope that it will be useful,
** but WITHOUT ANY WARRANTY; without even the implied warranty of
** MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
** GNU General Public License for more details.
**
** You should have received a copy of the GNU General Public License
** along with this program; if not, write to the Free Software
** Foundation, Inc., 59 Temple Place - Suite 330,
** Boston, MA 02111-1307, USA.
**
*/

#ifdef __gnu_linux__
#define _GNU_SOURCE
#endif /* __gnu_linux__ */

#include <sys/types.h>
#include <sys/stat.h>
#include <sys/wait.h>

#include <stdlib.h>
#include <unistd.h>
#include <string.h>
#include <errno.h>
#include <fcntl.h>

#include <aif.h>

#include "dbg.h"
#include "dbg_event.h"
#include "backend.h"
#include "list.h"

#include "MI.h"

//#define DEBUG

#define MI_TIMEOUT_MS		10000

struct bpentry {
	int local;
	int remote;
	int temp;
};
typedef struct bpentry	bpentry;

struct bpmap {
	int				nels; // number of elements currently in map
	int				size; // total size of map
	struct bpentry *	maps;
};

struct varinfo {
	char * name; //variable name
	MIVar *mivar;
};
typedef struct varinfo	varinfo;

struct varmap {
	int nels; // number of elements currently in map
	int size; // total size of map
	struct varinfo * maps;
};

static MISession *	DebugSession;
static dbg_event *	LastEvent;
static void			(*EventCallback)(dbg_event *, void *);
static void *		EventCallbackData;
static int			ServerExit;
static int			Started;
static struct bpmap	BPMap = { 0, 0, NULL };
static struct varmap VARMap = { 0, 0, NULL };
static int			(*AsyncFunc)(void *) = NULL;
static void *		AsyncFuncData;

static int	SetAndCheckBreak(int, int, int, char *, char *, int, int);
static int	GetStackframes(int, List **);
static int	GetAIFVar(char *, AIF **, char **);
static AIF * ConvertVarToAIF(char *, MIVar *, int);
static AIF * GetPrimitiveTypeToAIF(int, char *); 

static int	GDBMIInit(void (*)(dbg_event *, void *), void *);
static int	GDBMIProgress(void);
static int	GDBMIInterrupt(void);
static int	GDBMIStartSession(char *, char *, char *, char *, char **, char **);
static int	GDBMISetLineBreakpoint(int, int, int, char *, int, char*, int, int);
static int	GDBMISetFuncBreakpoint(int, int, int, char *, char *, char*, int, int);
static int	GDBMIDeleteBreakpoint(int);
static int	GDBMIEnableBreakpoint(int);
static int	GDBMIDisableBreakpoint(int);
static int	GDBMIConditionBreakpoint(int, char *expr);
static int	GDBMIBreakpointAfter(int, int icount);
static int	GDBMIWatchpoint(int, char *, int, int, char *, int);
static int	GDBMIGo(void);
static int	GDBMIStep(int, int);
static int	GDBMITerminate(void);
static int	GDBMIListStackframes(int);
static int	GDBMISetCurrentStackframe(int);
static int	GDBMIEvaluateExpression(char *);
static int	GDBMIGetNativeType(char *);
#ifdef notdef
static int	GDBMIGetAIFType(char *);
#endif
static int	GDBMIGetLocalVariables(void);
static int	GDBMIListArguments(int);
static int	GDBMIGetInfoThread(void);
static int	GDBMISetThreadSelect(int);
static int	GDBMIStackInfoDepth(void);
static int	GDBMIDataReadMemory(long, char*, char*, int, int, int, char*);
static int	GDBMIDataWriteMemory(long, char*, char*, int, char*);
static int	GDBMIGetGlobalVariables(void);
static int	GDBCLIListSignals(char*);
static int	GDBCLISignalInfo(char*);
static int	GDBCLIHandle(char*);
static int	GDBMIQuit(void);

static char* GetModifierType(char *);
static int getSimpleTypeID(char*, char*);
static AIF* CreateNamed(AIF*, int);

static char * GetPtypeValue(char *);
static AIF * ComplexVarToAIF(char *, MIVar *, int);
static AIF * GetAIFPointer(char *res, AIF *a);
static int GetAddressLength();
static List * GetChangedVariables();
static void RemoveAllMaps();

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
	GDBCLISignalInfo,
	GDBCLIHandle,
	GDBMIQuit
};

#define CHECK_SESSION() \
	if (DebugSession == NULL) { \
		DbgSetError(DBGERR_NOSESSION, NULL); \
		return DBGRES_ERR; \
	}
	
#define ERROR_TO_EVENT(e) \
	e = NewDbgEvent(DBGEV_ERROR); \
	e->dbg_event_u.error_event.error_code = DbgGetError(); \
	e->dbg_event_u.error_event.error_msg = strdup(DbgGetErrorStr())

static char *
GetLastErrorStr(void)
{
	return MIGetErrorStr();
}

static void
SaveEvent(dbg_event *e)
{
	if (LastEvent != NULL)
		FreeDbgEvent(LastEvent);
		
	LastEvent = e;
}

static void
AddBPMap(int local, int remote, int temp)
{
	int				i;
	struct bpentry *	map;
	
	if (BPMap.size == 0) {
		BPMap.maps = (struct bpentry *)malloc(sizeof(struct bpentry) * 100);
		BPMap.size = 100;
		
		for (i = 0; i < BPMap.size; i++) {
			map = &BPMap.maps[i];
			map->remote = map->local = -1;
			map->temp = 0;
		}
	}
	
	if (BPMap.nels == BPMap.size) {
		i = BPMap.size;
		BPMap.size *= 2;
		BPMap.maps = (struct bpentry *)realloc(BPMap.maps, sizeof(struct bpentry) * BPMap.size);
		
		for (; i < BPMap.size; i++) {
			map = &BPMap.maps[i];
			map->remote = map->local = -1;
			map->temp = 0;
		}
	}
	
	for (i = 0; i < BPMap.size; i++) {
		map = &BPMap.maps[i];
		if (map->remote == -1) {
			map->remote = remote;
			map->local = local;
			map->temp = temp;
			BPMap.nels++;
		}
	}
}

static void
RemoveBPMap(bpentry *bp)
{
	int				i;
	struct bpentry *	map;
	for (i = 0; i < BPMap.nels; i++) {
		map = &BPMap.maps[i];
		if (map == bp) {
			map->remote = -1;
			map->local = -1;
			map->temp = 0;
			BPMap.nels--;
			return;
		}
	}		
}

static bpentry *
FindLocalBP(int local) 
{
	int				i;
	struct bpentry *	map;
	for (i = 0; i < BPMap.nels; i++) {
		map = &BPMap.maps[i];
		if (map->local == local) {
			return map;
		}
	}
	return NULL;
}

static bpentry *
FindRemoteBP(int remote) 
{
	int				i;
	struct bpentry *	map;
	for (i = 0; i < BPMap.nels; i++) {
		map = &BPMap.maps[i];
		if (map->remote == remote) {
			return map;
		}
	}
	return NULL;
}

static void
RemoveAllBPMap()
{
	int				i;
	struct bpentry *	map;
	int length = BPMap.nels;
	for (i = 0; i < length; i++) {
		map = &BPMap.maps[i];
		if (map == NULL)
			return;

		map->remote = -1;
		map->local = -1;
		map->temp = 0;
		BPMap.nels--;
	}
}

static int
get_current_frame(stackframe **frame)
{
	List *	frames;
	
	if (GetStackframes(1, &frames) != DBGRES_OK)
		return -1;

	if (EmptyList(frames)) {
		DbgSetError(DBGERR_DEBUGGER, "Could not get current stack frame");
		return -1;
	} 
	
	SetList(frames);
	*frame = (stackframe *)GetListElement(frames);
	DestroyList(frames, NULL);
	return 0;
}

static int
AsyncStop(void *data)
{
	dbg_event *	e;
	stackframe *	frame;
	bpentry * bpmap;
	MIEvent *	evt = (MIEvent *)data;

	switch ( evt->type )
	{
	case MIEventTypeBreakpointHit:
	printf("$$$$$$$$$$$$$$$$$$$$ MIEventTypeBreakpointHit \n");
		bpmap = FindLocalBP(evt->bkptno);
		
		if (!bpmap->temp) {
			e = NewDbgEvent(DBGEV_SUSPEND);
			e->dbg_event_u.suspend_event.reason = DBGEV_SUSPEND_BPHIT;
			e->dbg_event_u.suspend_event.ev_u.bpid = bpmap->remote;
			e->dbg_event_u.suspend_event.thread_id = evt->threadId;
			e->dbg_event_u.suspend_event.frame = NULL;
			e->dbg_event_u.suspend_event.changed_vars = GetChangedVariables();
			break;
		}
		/* else must be a temporary breakpoint drop through... */
		RemoveBPMap(bpmap);

	case MIEventTypeSuspended:
	printf("$$$$$$$$$$$$$$$$$$$$ MIEventTypeSuspended \n");
		if (get_current_frame(&frame) < 0) {
			ERROR_TO_EVENT(e);
		} else {
			e = NewDbgEvent(DBGEV_SUSPEND);
			e->dbg_event_u.suspend_event.reason = DBGEV_SUSPEND_INT;
			e->dbg_event_u.suspend_event.thread_id = evt->threadId;
			e->dbg_event_u.suspend_event.frame = frame;
			e->dbg_event_u.suspend_event.changed_vars = GetChangedVariables();
		}
		break;

	case MIEventTypeSteppingRange:
	printf("$$$$$$$$$$$$$$$$$$$$ MIEventTypeSteppingRange \n");
		if (get_current_frame(&frame) < 0) {
			ERROR_TO_EVENT(e);
		} else {
			e = NewDbgEvent(DBGEV_SUSPEND);
			e->dbg_event_u.suspend_event.reason = DBGEV_SUSPEND_STEP;
			e->dbg_event_u.suspend_event.thread_id = evt->threadId;
			e->dbg_event_u.suspend_event.frame = frame;
			e->dbg_event_u.suspend_event.changed_vars = GetChangedVariables();
		}
		break;

	case MIEventTypeSignal:
	printf("$$$$$$$$$$$$$$$$$$$$ MIEventTypeSignal \n");
		if (get_current_frame(&frame) < 0) {
			ERROR_TO_EVENT(e);
		} else {
			e = NewDbgEvent(DBGEV_SUSPEND);
			e->dbg_event_u.suspend_event.reason = DBGEV_SUSPEND_SIGNAL;
			e->dbg_event_u.suspend_event.ev_u.sig = NewSignalInfo();
			e->dbg_event_u.suspend_event.ev_u.sig->name = strdup(evt->sigName);
			e->dbg_event_u.suspend_event.ev_u.sig->desc = strdup(evt->sigMeaning);
			e->dbg_event_u.suspend_event.thread_id = evt->threadId;
			e->dbg_event_u.suspend_event.frame = frame;
			e->dbg_event_u.suspend_event.changed_vars = GetChangedVariables();
		}
		break;
		
	case MIEventTypeInferiorSignalExit:
	printf("$$$$$$$$$$$$$$$$$$$$ MIEventTypeInferiorSignalExit \n");
		e = NewDbgEvent(DBGEV_EXIT);
		e->dbg_event_u.exit_event.reason = DBGEV_EXIT_SIGNAL;
		e->dbg_event_u.exit_event.ev_u.sig = NewSignalInfo();
		e->dbg_event_u.exit_event.ev_u.sig->name = strdup(evt->sigName);
		e->dbg_event_u.exit_event.ev_u.sig->desc = strdup(evt->sigMeaning);
		RemoveAllMaps();
		break;
		
	case MIEventTypeInferiorExit:
	printf("$$$$$$$$$$$$$$$$$$$$ MIEventTypeInferiorExit \n");
		e = NewDbgEvent(DBGEV_EXIT);
		e->dbg_event_u.exit_event.reason = DBGEV_EXIT_NORMAL;
		e->dbg_event_u.exit_event.ev_u.exit_status = evt->code;
		RemoveAllMaps();
		break;

	default:
		DbgSetError(DBGERR_DEBUGGER, "Unknown reason for stopping");
		return DBGRES_ERR;
	}

	MIEventFree(evt);
	
	if (EventCallback != NULL)
		EventCallback(e, EventCallbackData);

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


/*
 * Initialize GDB
 */
static int
GDBMIInit(void (*event_callback)(dbg_event *, void *), void *data)
{
	EventCallback = event_callback;
	EventCallbackData = data;
	DebugSession = NULL;
	LastEvent = NULL;
	ServerExit = 0;
		
	signal(SIGTERM, SIG_IGN);
	signal(SIGHUP, SIG_IGN);
	signal(SIGINT, SIG_IGN);

	return DBGRES_OK;
}

int timeout_cb(void *data)
{
	return 0;
}

/*
 * Send command and wait for immediate response.
 */
static void
SendCommandWait(MISession *sess, MICommand *cmd)
{
	MISessionSendCommand(sess, cmd);
	
	do {
		MISessionProgress(sess);
	} while (!MISessionCommandCompleted(sess));
}

/**** Variable ****/
static varinfo *
AddVARMap(char *name)
{
	int				i;
	struct varinfo *map;
	MICommand *cmd;
	MIVar *mivar;
	
	if (VARMap.size == 0) {
		VARMap.maps = (struct varinfo *)malloc(sizeof(struct varinfo) * 100);
		VARMap.size = 100;
		
		for (i = 0; i < VARMap.size; i++) {
			map = &VARMap.maps[i];
			map->name = NULL; 
			map->mivar = NULL;
		}
	}
	
	if (VARMap.nels == VARMap.size) {
		i = VARMap.size;
		VARMap.size *= 2;
		VARMap.maps = (struct varinfo *)realloc(VARMap.maps, sizeof(struct varinfo) * VARMap.size);
		
		for (; i < VARMap.size; i++) {
			map = &VARMap.maps[i];
			map->name = NULL; 
			map->mivar = NULL;
		}
	}
	
	for (i = 0; i < VARMap.size; i++) {
		map = &VARMap.maps[i];
		if (map->name == NULL) {
			cmd = MIVarCreate("-", "*", name);
			SendCommandWait(DebugSession, cmd);
			if (!MICommandResultOK(cmd)) {
				MICommandFree(cmd);
				return NULL;
			}
			mivar = MIGetVarCreateInfo(cmd);
			MICommandFree(cmd);
			
			map->name = strdup(name);
			map->mivar = mivar;
			VARMap.nels++;
			return map;
		}
	}
	return NULL;
}

static void
GetParentMiVar(char *mi_name)
{
	char* p;
	p = strchr(mi_name, '.');
	if (p != NULL) {
		*p = '\0';
	}
}

static varinfo *
FindVARByName(char *name) 
{
	int				i;
	struct varinfo *map;
	for (i = 0; i < VARMap.nels; i++) {
		map = &VARMap.maps[i];
		if (map != NULL && strcmp(map->name, name) == 0) {
			return map;
		}
	}
	return NULL;
}

static varinfo *
FindVARByMIName(char *mi_name) 
{
	int				i;
	GetParentMiVar(mi_name);
	struct varinfo *map;
	for (i = 0; i < VARMap.nels; i++) {
		map = &VARMap.maps[i];
		if (map != NULL && strcmp(map->mivar->name, mi_name) == 0) {
			return map;
		}
	}
	return NULL;
}

static void
RemoveVARMap(varinfo *map)
{
	MICommand *cmd;
	if (map != NULL) {
		if (map->name != NULL) {
			free(map->name);
			map->name = NULL;
		}
		if (map->mivar != NULL) {
			cmd = MIVarDelete(map->mivar->name);
			SendCommandWait(DebugSession, cmd);
			MICommandFree(cmd);
			MIVarFree(map->mivar);
			map->mivar = NULL;
		}
		VARMap.nels--;
	}
}

static void
RemoveVARMapByName(char *name)
{
	struct varinfo *map;	
	map = FindVARByName(name);
	RemoveVARMap(map);
}

static void
RemoveVARMapByMIName(char *mi_name)
{
	struct varinfo *map;
	map = FindVARByMIName(mi_name);
	RemoveVARMap(map);
}

static void
RemoveAllVARMap()
{
	int				i;
	struct varinfo *map;
	int length = VARMap.nels;
	for (i = 0; i < length; i++) {
		map = &VARMap.maps[i];
		if (map == NULL)
			return;
			
		RemoveVARMap(map);
	}	
}

static void
RemoveAllMaps()
{
	RemoveAllBPMap();
	RemoveAllVARMap();
}

static List * 
GetChangedVariables()
{
	MICommand *cmd;	
	List *changes;
	List *changedVars;
	MIVarChange *var;
	struct varinfo *map;
	
	cmd = MIVarUpdate("*");
	SendCommandWait(DebugSession, cmd);
	if (!MICommandResultOK(cmd)) {
		DbgSetError(DBGERR_DEBUGGER, GetLastErrorStr());
		MICommandFree(cmd);
		return NewList();
	}
	MIGetVarUpdateInfo(cmd, &changes);
	MICommandFree(cmd);
	
	changedVars = NewList();
	for (SetList(changes); (var = (MIVarChange *)GetListElement(changes)) != NULL; ) {
		map = FindVARByMIName(var->name);
		if (map != NULL) {
			if (var->in_scope == 1) {
				AddToList(changedVars, (void *)strdup(map->name));
			}
			else {
				RemoveVARMap(map);
			}				
		}
	}
	DestroyList(changes, MIVarChangeFree);
	return changedVars;
}

/*
 * Start GDB session
 */	
static int
GDBMIStartSession(char *gdb_path, char *prog, char *path, char *work_dir, char **args, char **env)
{
	char *		prog_path;
	char **		e;
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
	
	MISessionSetTimeout(sess, 0, MI_TIMEOUT_MS);
	
	MISessionSetGDBPath(sess, gdb_path);
	
	if (MISessionStartLocal(sess, prog_path) < 0) {
		DbgSetError(DBGERR_DEBUGGER, GetLastErrorStr());
		MISessionFree(sess);
		free(prog_path);
		return DBGRES_ERR;
	}
	
	free(prog_path);

	for (e = env; e != NULL && *e != NULL; e++) {
		cmd = MIGDBSet("environment", *e);
		SendCommandWait(sess, cmd);
		MICommandFree(cmd);
	}
	
	cmd = MIGDBSet("confirm", "off");
	SendCommandWait(sess, cmd);
	MICommandFree(cmd);
	
	MISessionRegisterEventCallback(sess, AsyncCallback);
	
	DebugSession = sess;

	Started = 0;
	SaveEvent(NewDbgEvent(DBGEV_OK));

	return DBGRES_OK;
}

/*
 * Progress gdb commands.
 * 
 * TODO: Deal with errors
 * 
 * @return	-1	server shutdown
 * 			1	event callback
 * 			0	completed operation
 */
static int	
GDBMIProgress(void)
{
	int				res = 0;

	/*
	 * Check for existing events
	 */
	if (LastEvent != NULL) {
		if (EventCallback != NULL) {
			EventCallback(LastEvent, EventCallbackData);
			res = 1;
		}
			
		if (ServerExit && LastEvent->event == DBGEV_OK) {
			if (DebugSession != NULL) {
				DebugSession = NULL;
			}
			res = -1;
		}
			
		FreeDbgEvent(LastEvent);
		LastEvent = NULL;
		return res;
	}
	
	if (DebugSession == NULL)
		return 0;
	
	MISessionProgress(DebugSession);
	
	/*
	 * Do any extra async functions. We can call gdbmi safely here
	 */
	if (AsyncFunc != NULL) {
		AsyncFunc(AsyncFuncData);
		AsyncFunc = NULL;
		return 1;
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

	CHECK_SESSION()

	if (file == NULL || *file == '\0')
		asprintf(&where, "%d", line);
	else
		asprintf(&where, "%s:%d", file, line);

	res = SetAndCheckBreak(bpid, isTemp, isHard, where, condition, ignoreCount, tid);
	
	free(where);
	
	return res;
}

/*
** Set breakpoint at start of specified function.
*/
static int
GDBMISetFuncBreakpoint(int bpid, int isTemp, int isHard, char *file, char *func, char *condition, int ignoreCount, int tid)
{
	int		res;
	char *	where;

	CHECK_SESSION()

	if (file == NULL || *file == '\0')
		asprintf(&where, "%s", func);
	else
		asprintf(&where, "%s:%s", file, func);
		
	res = SetAndCheckBreak(bpid, isTemp, isHard, where, condition, ignoreCount, tid);
		
	free(where);
	
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
	dbg_event *		e;
	MIBreakpoint *	bpt;
	MICommand *		cmd;
	List *			bpts;
	breakpoint *	bp;

	if (condition != NULL && strlen(condition) == 0)
		condition = NULL;
		
	cmd = MIBreakInsert(isTemp, isHard, condition, ignoreCount, where, tid);
	SendCommandWait(DebugSession, cmd);
	
	if (!MICommandResultOK(cmd)) {
		DbgSetError(DBGERR_DEBUGGER, GetLastErrorStr());
		MICommandFree(cmd);
		return DBGRES_ERR;
	}
	bpts = MIBreakpointGetBreakInsertInfo(cmd);
	MICommandFree(cmd);
			
	if (bpts == NULL) {
		DbgSetError(DBGERR_DEBUGGER, "error getting breakpoint information");
		return DBGRES_ERR;
	}
		
	SetList(bpts);
	bpt = (MIBreakpoint *)GetListElement(bpts);
	
	AddBPMap(bpt->number, bpid, isTemp);
	
	bp = NewBreakpoint(bpt->number);

	bp->ignore = bpt->ignore;
	bp->type = strdup(bpt->type);
	bp->hits = bpt->times;

	if ( bpt->file != NULL )
		bp->loc.file = strdup(bpt->file);
	if ( bpt->func != NULL )
		bp->loc.func = strdup(bpt->func);
	if ( bpt->address != NULL )
		bp->loc.addr = strdup(bpt->address);
	bp->loc.line = bpt->line;
	
	e = NewDbgEvent(DBGEV_BPSET);
	e->dbg_event_u.bpset_event.bpid = bpid;
	e->dbg_event_u.bpset_event.bp = bp;
	SaveEvent(e);
	
	DestroyList(bpts, MIBreakpointFree);

	return DBGRES_OK;
}

/*
** Delete a breakpoint.
*/
static int
GDBMIDeleteBreakpoint(int bpid)
{
	bpentry *	bp;
	char *		bpstr;
	MICommand *	cmd;
	
	CHECK_SESSION()

	if ((bp = FindRemoteBP(bpid)) == NULL) {
		asprintf(&bpstr, "%d", bpid);
		DbgSetError(DBGERR_NOBP, bpstr);
		free(bpstr);
		return DBGRES_ERR;
	}
	
	cmd = MIBreakDelete(1, &bp->local);
	SendCommandWait(DebugSession, cmd);
	
	if (!MICommandResultOK(cmd)) {
		DbgSetError(DBGERR_DEBUGGER, GetLastErrorStr());
		MICommandFree(cmd);
		return DBGRES_ERR;
	}

	RemoveBPMap(bp);

	SaveEvent(NewDbgEvent(DBGEV_OK));

	return DBGRES_OK;
}

/*
** Enable a breakpoint.
*/
static int
GDBMIEnableBreakpoint(int bpid)
{
	bpentry *	bp;
	char *		bpstr;
	MICommand *	cmd;
	
	CHECK_SESSION()

	if ((bp = FindRemoteBP(bpid)) == NULL) {
		asprintf(&bpstr, "%d", bpid);
		DbgSetError(DBGERR_NOBP, bpstr);
		free(bpstr);
		return DBGRES_ERR;
	}
	
	cmd = MIBreakEnable(1, &bp->local);
	SendCommandWait(DebugSession, cmd);
	
	if (!MICommandResultOK(cmd)) {
		DbgSetError(DBGERR_DEBUGGER, GetLastErrorStr());
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
	bpentry *	bp;
	char *		bpstr;
	MICommand *	cmd;
	
	CHECK_SESSION()

	if ((bp = FindRemoteBP(bpid)) == NULL) {
		asprintf(&bpstr, "%d", bpid);
		DbgSetError(DBGERR_NOBP, bpstr);
		free(bpstr);
		return DBGRES_ERR;
	}
	
	cmd = MIBreakDisable(1, &bp->local);
	SendCommandWait(DebugSession, cmd);
	
	if (!MICommandResultOK(cmd)) {
		DbgSetError(DBGERR_DEBUGGER, GetLastErrorStr());
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
	bpentry *	bp;
	char *		bpstr;
	MICommand *	cmd;
	
	CHECK_SESSION()

	if ((bp = FindRemoteBP(bpid)) == NULL) {
		asprintf(&bpstr, "%d", bpid);
		DbgSetError(DBGERR_NOBP, bpstr);
		free(bpstr);
		return DBGRES_ERR;
	}
	
	cmd = MIBreakCondition(1, &bp->local, expr);
	SendCommandWait(DebugSession, cmd);
	
	if (!MICommandResultOK(cmd)) {
		DbgSetError(DBGERR_DEBUGGER, GetLastErrorStr());
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
	bpentry *	bp;
	char *		bpstr;
	MICommand *	cmd;
	
	CHECK_SESSION()

	if ((bp = FindRemoteBP(bpid)) == NULL) {
		asprintf(&bpstr, "%d", bpid);
		DbgSetError(DBGERR_NOBP, bpstr);
		free(bpstr);
		return DBGRES_ERR;
	}
	
	cmd = MIBreakAfter(1, &bp->local, icount);
	SendCommandWait(DebugSession, cmd);
	
	if (!MICommandResultOK(cmd)) {
		DbgSetError(DBGERR_DEBUGGER, GetLastErrorStr());
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
	List *			bpts;
	breakpoint *	bp;

	if (condition != NULL && strlen(condition) == 0)
		condition = NULL;

	cmd = MIBreakWatch(expr, isAccess, isRead);
	SendCommandWait(DebugSession, cmd);

	if (!MICommandResultOK(cmd)) {
		DbgSetError(DBGERR_DEBUGGER, GetLastErrorStr());
		MICommandFree(cmd);
		return DBGRES_ERR;
	}
		
	bpts = MIBreakpointGetBreakInsertInfo(cmd);
	MICommandFree(cmd);
			
	if (bpts == NULL) {
		DbgSetError(DBGERR_DEBUGGER, "error getting breakpoint information");
		return DBGRES_ERR;
	}
	
	SetList(bpts);
	bpt = (MIBreakpoint *)GetListElement(bpts);
	
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
	
	DestroyList(bpts, MIBreakpointFree);

	return DBGRES_OK;
}

/*
** Start/continue executing program. 
*/
static int
GDBMIGo(void)
{
	MICommand *	cmd;
		
	CHECK_SESSION()

	if (Started)
		cmd = MIExecContinue();
	else {
		cmd = MIExecRun();
		Started = 1;
	}

	SendCommandWait(DebugSession, cmd);
	
	if (!MICommandResultOK(cmd)) {
		DbgSetError(DBGERR_DEBUGGER, GetLastErrorStr());
		MICommandFree(cmd);
		return DBGRES_ERR;
	}
		
	MICommandFree(cmd);
	
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

	CHECK_SESSION()

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
		DbgSetError(DBGERR_DEBUGGER, GetLastErrorStr());
		MICommandFree(cmd);
		return DBGRES_ERR;
	}
		
	MICommandFree(cmd);

	return DBGRES_OK;
}

/*
** Terminate program execution.
*/
static int
GDBMITerminate(void)
{
	MICommand *	cmd;
	
	CHECK_SESSION()

	cmd = MICommandNew("kill", MIResultRecordDONE);
	
	SendCommandWait(DebugSession, cmd);
	
	if (!MICommandResultOK(cmd)) {
		DbgSetError(DBGERR_DEBUGGER, GetLastErrorStr());
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
	
	CHECK_SESSION()

	/*
	 * Don't do anything if there's an event pending or the
	 * target is not running.
	 *
	if (LastEvent != NULL || !gmi_exec_interrupt(MIHandle))
		return DBGRES_OK;*/
		
	/*
	 * Must check async here due to broken MI implementation. AsyncCallback will
	 * be called inside gmi_exec_interrupt().
	 *
	if (AsyncFunc != NULL) {
		AsyncFunc(AsyncFuncData);
		AsyncFunc = NULL;
	}*/
	
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
	
	CHECK_SESSION()

	cmd = MIStackSelectFrame(level);
	
	SendCommandWait(DebugSession, cmd);
	
	if (!MICommandResultOK(cmd)) {
		DbgSetError(DBGERR_DEBUGGER, GetLastErrorStr());
		MICommandFree(cmd);
		return DBGRES_ERR;
	}
		
	MICommandFree(cmd);

	SaveEvent(NewDbgEvent(DBGEV_OK));
	
	return DBGRES_OK;
}

static int
GetStackframes(int current, List **flist)
{
	List *		frames;
	MIFrame *	f;
	MICommand *	cmd;
	stackframe *	s;
	
	if (current)
		cmd = MIStackInfoFrame();
	else
		cmd = MIStackListAllFrames();
		
	SendCommandWait(DebugSession, cmd);
	
	if (!MICommandResultOK(cmd)) {
		DbgSetError(DBGERR_DEBUGGER, GetLastErrorStr());
		MICommandFree(cmd);
		return DBGRES_ERR;
	}
	
	if (current)
		frames = MIGetFrameInfo(cmd);
	else
		frames = MIGetStackListFramesInfo(cmd);
			
	MICommandFree(cmd);
	
	if ( frames == NULL )
	{
		DbgSetError(DBGERR_DEBUGGER, "Failed to get stack frames from backend");
		return DBGRES_ERR;
	}
	
	*flist = NewList();
	for (SetList(frames); (f = (MIFrame *)GetListElement(frames)) != NULL; ) {
		s = NewStackframe(f->level);

		if ( f->addr != NULL )
			s->loc.addr = strdup(f->addr);
		if ( f->func != NULL )
			s->loc.func = strdup(f->func);
		if ( f->file != NULL )
			s->loc.file = strdup(f->file);
		s->loc.line = f->line;
		
		AddToList(*flist, (void *)s);
	}
	DestroyList(frames, MIFrameFree);
	return DBGRES_OK;
}

/*
** List current or all stack frames.
*/
static int
GDBMIListStackframes(int current)
{
	dbg_event *	e;
	List *		frames;
	
	CHECK_SESSION()

	if (GetStackframes(current, &frames) != DBGRES_OK)
		return DBGRES_ERR;
	
	e = NewDbgEvent(DBGEV_FRAMES);
	e->dbg_event_u.list = frames;	
	SaveEvent(e);
	
	return DBGRES_OK;
}

#ifdef notdef
struct mi_aif_struct
{
	char *fds;
	char *data;
};
typedef struct mi_aif_struct mi_aif;

mi_aif *
mi_alloc_aif(void)
{
	return (mi_aif *)mi_calloc1(sizeof(mi_aif));
}

mi_aif *
mi_parse_aif(mi_results *c)
{
	mi_aif *res = mi_alloc_aif();

	if ( res ) 
	{
		while ( c ) 
		{
			if ( c->type == t_const )
			{
				if ( strcmp(c->var, "fds") == 0 )
					res->fds = c->v.cstr;
				else if ( strcmp(c->var, "data") == 0 )
					res->data = c->v.cstr;
			}
			c = c->next;
		}
	}

	return res;
}

mi_aif *
mi_res_aif(mi_h *h)
{
	mi_results *r = mi_res_done_var(MIHandle, "aif");
	mi_aif *a = NULL;

	if (r && r->type == t_tuple)
		a = mi_parse_aif(r->v.rs);
	mi_free_results(r);
	return a;
}

mi_aif *
gmi_aif_evaluate_expression(mi_h *h, char *exp)
{
	mi_send(h, "-aif-evaluate-expression \"%s\"\n", exp);
	return mi_res_aif(h);
}
#endif

int
DumpBinaryValue(MISession *sess, char *exp, char *file)
{
	MICommand *	cmd;
	
	cmd = MICommandNew("dump binary value", MIResultRecordDONE);
	MICommandAddOption(cmd, file, exp);
	
	SendCommandWait(DebugSession, cmd);
	
	if (!MICommandResultOK(cmd)) {
		DbgSetError(DBGERR_DEBUGGER, GetLastErrorStr());
		MICommandFree(cmd);
		return -1;
	}
		
	MICommandFree(cmd);
	
	return 0;
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

	if (GetAIFVar(exp, &a, &type) != DBGRES_OK)
		return DBGRES_ERR;
		
	e = NewDbgEvent(DBGEV_DATA);
	e->dbg_event_u.data_event.data = a;
	e->dbg_event_u.data_event.type_desc = type;
	SaveEvent(e);

	return DBGRES_OK;
}

struct simple_type {
	char *	type_c;
	int		type;
};

#define CHAR			0
#define SHORT		1
#define USHORT		2
#define INT			3
#define UINT			4
#define LONG			5
#define ULONG		6
#define LONGLONG		7
#define ULONGLONG	8
#define FLOAT		9
#define DOUBLE		10
#define STRING		11

char* MODIFIERS[] = {
	"const volatile",
	"volatile",
	"const",
	NULL
};

struct simple_type simple_types[] = {
	{ "char", CHAR },
	{ "unsigned char", CHAR },
	{ "short int", SHORT },
	{ "short unsigned int", USHORT },
	{ "int", INT },
	{ "unsigned int", UINT },
	{ "long int", LONG },
	{ "long unsigned int", ULONG },
#ifdef CC_HAS_LONG_LONG
	{ "long long int", LONGLONG },
	{ "long long unsigned int", ULONGLONG },
#endif /* CC_HAS_LONG_LONG */
	{ "long", LONG },
	{ "float", FLOAT },
	{ "double", DOUBLE },
	{ "string", STRING },
	{ NULL, 0 }
};

static char *
GetVarValue(char *var)
{
	char *		res;
	MICommand *	cmd = MIVarEvaluateExpression(var);
	
	SendCommandWait(DebugSession, cmd);
	if (!MICommandResultOK(cmd)) {
		DbgSetError(DBGERR_DEBUGGER, GetLastErrorStr());
		MICommandFree(cmd);
		return NULL;
	}
	res = MIGetVarEvaluateExpressionInfo(cmd);	
	MICommandFree(cmd);
	
	return res;
}

static int
GetAddressLength()
{
	char * res;
	
	MICommand * cmd = MIDataEvaluateExpression("\"sizeof(char *)\"");
	SendCommandWait(DebugSession, cmd);
	if (!MICommandResultOK(cmd)) {
		DbgSetError(DBGERR_DEBUGGER, GetLastErrorStr());
		MICommandFree(cmd);
		return 0;
	}
	res = MIGetDataEvaluateExpressionInfo(cmd);	
	MICommandFree(cmd);
	
	return atoi(res);
}

static char * 
GetPtypeValue(char *exp) 
{
	MICommand* cmd;
	char * type = NULL;
	
	cmd = CLIPType(exp);
	SendCommandWait(DebugSession, cmd);
	type = MIGetDetailsType(cmd);
	MICommandFree(cmd);
	return type;
}

static AIF *
SimpleVarToAIF(char *exp, MIVar *var)
{
	int		len;
	char	*p;
	AIF		*a;
	AIF		*av;
	int		type_id;
	char	*res;
	
	len = strlen(var->type);
	if (var->type[len - 1] == ')') { /* function */
		return MakeAIF("&/is4", exp);
	} 

	if (strcmp(var->type, "char *") == 0) { /* void pointer */
		if ((res = GetVarValue(var->name)) != NULL) {
			return GetAIFPointer(res, CharPointerToAIF(res));			
		}
	}
	
	if (strcmp(var->type, "void *") == 0) { /* void pointer */
		av = VoidToAIF(0, 0);
		a = GetAIFPointer(GetVarValue(var->name), av);
		AIFFree(av);
		return a;
	} 
	
	if (strncmp(var->type, "enum", 4) == 0) { /* enum */
		if ((p = strchr(var->type, ' ')) != NULL) {
			*p++ = '\0';
			a = EmptyEnumToAIF(p);
		} else
			a = EmptyEnumToAIF(NULL);
		return a;
	}
	
	if ((type_id = getSimpleTypeID(var->type, exp)) > -1) {
		if ((res = GetVarValue(var->name)) != NULL) {
			a = GetPrimitiveTypeToAIF(type_id, res);
		}
	}
	
	if (a == NULL) {
		DbgSetError(DBGERR_UNKNOWN_TYPE, "could not convert simple type");
	}
	return a;
}	

static AIF * 
GetPrimitiveTypeToAIF(int type_id, char* res)
{
	AIF *a;
	char *p;

	switch (type_id) {
	case STRING:
	case CHAR:
		if ((p = strchr(res, ' ')) != NULL) {
			p++;
			if (*p == '\'') { //character
				p--;
				*p = '\0';
				a = CharToAIF((char)atoi(res));
			}
			else { //string
				a = StringToAIF(p);			
			}
		}
		else {
			a = CharToAIF((char)atoi(res));
		}
		break;
				
	case SHORT:
		a = ShortToAIF((short)atoi(res));
		break;
				
	case USHORT:
		a = UnsignedShortToAIF((unsigned short)atoi(res));
		break;
		
	case INT:
		a = IntToAIF(atoi(res));
		break;
		
	case UINT:
		a = UnsignedIntToAIF((unsigned int)atoi(res));
		break;
				
	case LONG:
		a = LongToAIF(atol(res));
		break;
		
	case ULONG:
		a = UnsignedLongToAIF((unsigned long)atol(res));
		break;
				
#ifdef CC_HAS_LONG_LONG					
	case LONGLONG:
		a = LongLongToAIF(atoll(res));
		break;
		
	case ULONGLONG:
		a = UnsignedLongLongToAIF((unsigned long long)atoll(res));
		break;
#endif /* CC_HAS_LONG_LONG */
	
	case FLOAT:
		a = FloatToAIF((float)atof(res));
		break;
		
	case DOUBLE:
		a = DoubleToAIF(atof(res));
		break;				
	}
	return a;
}

static int 
getSimpleTypeID(char* type, char* exp) 
{
	struct simple_type *	s;
	
	type = GetModifierType(type);
	for (s = simple_types; s->type_c != NULL; s++) {
		if (strcmp(type, s->type_c) == 0) {
			return s->type;
		}
	}
	if (exp != NULL) {
		return getSimpleTypeID(GetPtypeValue(exp), NULL);
	}
	return -1;	
}

static char * 
GetModifierType(char* type) 
{
	char** m;
	int len;
	for (m = MODIFIERS; *m != NULL; m++) {
		len = strlen(*m);
		if (strncmp(type, *m, len) == 0) {
			return type + len + 1; //+1 remove whitespace
		}
	}
	return type;
}

static AIF *
CreateStruct(MIVar *var, int named)
{
	int		i;
	MIVar *	v;
	AIF *	a;
	AIF *	ac;
	named++;
	
	if (var->type[6] == ' ' && var->type[7] != '{')
		a = EmptyStructToAIF(&var->type[7]);
	else
		a = EmptyStructToAIF(NULL);


	for ( i = 0 ; i < var->numchild ; i++ )
	{
		v = var->children[i];
		//check whether child contains parent
		if (strcmp(var->type, v->type) == 0 && strcmp(var->name, v->name)) {
			a = CreateNamed(a, named);
			ac = AIFNull(a);
		}
		else {
			if ( (ac = ConvertVarToAIF(v->exp, v, named)) == NULL ) {
				AIFFree(a);
				return NULL;
			}
		}
		AIFAddFieldToStruct(a, v->exp, ac);
	}
	return a;
}

static AIF *
CreateUnion(MIVar *var, int named)
{
	int		i;
	MIVar *	v;
	AIF *	a;
	AIF *	ac;
	named++;

	if (var->type[6] == ' ' && var->type[7] != '{')
		a = EmptyUnionToAIF(&var->type[7]);
	else
		a = EmptyUnionToAIF(NULL);
	
	for ( i = 0 ; i < var->numchild ; i++ )
	{
		v = var->children[i];
		//check whether child contains parent
		if (strcmp(var->type, v->type) == 0 && strcmp(var->name, v->name)) {
			a = CreateNamed(a, named);
			ac = AIFNull(a);
		}
		else {
			if ( (ac = ConvertVarToAIF(v->exp, v, named)) == NULL ) {
				AIFFree(a);
				return NULL;
			}
		}
		AIFAddFieldToUnion(a, v->exp, AIF_FORMAT(ac));
		
		/*
		 * Set the union value
		 */
		if (i == var->numchild - 1)
			AIFSetUnion(a, v->exp, ac);
	}
	
	return a;
}

static AIF* 
CreateNamed(AIF *a, int named) 
{
	if (FDSType(AIF_FORMAT(a)) != AIF_NAME) {
		return NameAIF(a, named);
	}
	return a;
}

static AIF *
CreateArray(MIVar *var, int named)
{
	int		i;
	MIVar *	v;
	AIF *	a = NULL;
	AIF *	ac;

	for (i = 0; i < var->numchild; i++) {
		v = var->children[i];
		if ((ac = ConvertVarToAIF(v->exp, v, named)) == NULL) {
			return NULL;
		}
		if (a == NULL)
			a = EmptyArrayToAIF(0, var->numchild-1, ac);
		AIFAddArrayElement(a, i, ac);
		AIFFree(ac);
	}

	return a;
}

static AIF *
GetAIFPointer(char *res, AIF *i)
{
	AIF *address;
	char *p;
	
	if (res == NULL) {
		address = AIFNull(NULL);
	}
	else {
		if ((p = strchr(res, ' ')) != NULL) {
			*p = '\0';
		}
		res += 2; //skip 0x
		address = AddressToAIF(res, GetAddressLength());
	}
	return PointerToAIF(address, i);
}

static AIF *
ConvertVarToAIF(char *exp, MIVar *var, int named)
{
	AIF 		*a;
	MICommand	*cmd;

	if (strcmp(var->type, "<text variable, no debug info>") == 0) {
		DbgSetError(DBGERR_NOSYMS, "");
		return NULL;
	}
	
	if (var->numchild == 0) { //simple type
		return SimpleVarToAIF(exp, var);
	}
	
	//complex type	
	cmd = MIVarListChildren(var->name);
	SendCommandWait(DebugSession, cmd);
	if (!MICommandResultOK(cmd)) {
		DbgSetError(DBGERR_DEBUGGER, GetLastErrorStr());
		MICommandFree(cmd);
		return NULL;
	}
	MIGetVarListChildrenInfo(var, cmd);
	MICommandFree(cmd);

	a = ComplexVarToAIF(exp, var, named);
	if (a == NULL) {
		DbgSetError(DBGERR_UNKNOWN_TYPE, "type not supported (yet)");
	}
	return a;
}

static AIF * 
ComplexVarToAIF(char *exp, MIVar *var, int named) 
{
	AIF		*a;
	char	*p;
	char	*type;
	int		type_id;
	char	*res;

	switch (var->type[strlen(var->type) - 1]) {
	case ']': /* array */
		a = CreateArray(var, named);
		break;

	case '*': /* pointer */
		res = GetVarValue(var->name); //get address
		type = strdup(var->type);		
		if (strncmp(type, "struct", 6) == 0) {
			a = CreateStruct(var, named);
		} else if (strncmp(type, "union", 5) == 0) {
			a = CreateUnion(var, named);
		} else if (strncmp(type, "char *", 6) == 0) {//char pointer
			a = CharPointerToAIF(res);
		} else { //other types
			p = strchr(type, '*');
			p--;
			*p = '\0';
			if ((type_id = getSimpleTypeID(type, exp)) > -1) { //simple type
				a = GetPrimitiveTypeToAIF(type_id, res);
			}
			else {
				a = ConvertVarToAIF(var->children[0]->exp, var->children[0], named);
			}			
		}
		free(type);
		if (a != NULL) {
			a = GetAIFPointer(res, a);
		}
		break;
					
	default:
		if (strncmp(var->type, "union", 5) == 0) {
			a = CreateUnion(var, named);
		}
		else {
			//if (strncmp(var->type, "struct", 6) == 0) { /* struct */
			a = CreateStruct(var, named);
		}
	}
	
	if (a == NULL) {//try again with ptype
		var->type = GetPtypeValue(exp);
		a = ComplexVarToAIF(NULL, var, named);
	}
	return a;
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

	CHECK_SESSION()

	if (GetAIFVar(var, &a, &type) != DBGRES_OK)
		return DBGRES_ERR;
		
	e = NewDbgEvent(DBGEV_TYPE);
	e->dbg_event_u.type_desc = type;

	SaveEvent(e);

	AIFFree(a);

	return DBGRES_OK;
}

#ifdef notdef
/*
** Find AIF type of variable.
*/
static int
GDBMIGetAIFType(char *var)
{
	dbg_event *	e;
	AIF *		a;
	char *		type;

	CHECK_SESSION()

	if (GetAIFVar(var, &a, &type) != DBGRES_OK)
		return DBGRES_ERR;
		
	e = NewDbgEvent(DBGEV_TYPE);
	e->type_desc = AIF_FORMAT(a);

	SaveEvent(e);

	AIFFree(a);

	return DBGRES_OK;
}
#endif

static int
GetAIFVar(char *var, AIF **val, char **type)
{
	AIF *		res;
	struct varinfo *map;

	map = FindVARByName(var);
	if (map == NULL) {
		map = AddVARMap(var);		
	}
	if (map == NULL) {
		DbgSetError(DBGERR_UNKNOWN_VARIABLE, GetLastErrorStr());
		return DBGRES_ERR;
	}
	
	/*
	cmd = MIVarCreate("-", "*", var);
	SendCommandWait(DebugSession, cmd);
	if (!MICommandResultOK(cmd)) {
		DbgSetError(DBGERR_UNKNOWN_VARIABLE, GetLastErrorStr());
		MICommandFree(cmd);
		return DBGRES_ERR;
	}
	mivar = MIGetVarCreateInfo(cmd);
	MICommandFree(cmd);
	*/
	
	if ( (res = ConvertVarToAIF(var, map->mivar, 0)) == NULL ) {
		return DBGRES_ERR;
	}
	*type = strdup(map->mivar->type);
	*val = res;

	/*
	cmd = MIVarDelete(mivar->name);
	SendCommandWait(DebugSession, cmd);
	MICommandFree(cmd);
	MIVarFree(mivar);
	*/
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
	List *		args;

	CHECK_SESSION()

	cmd = MIStackListLocals(0);

	SendCommandWait(DebugSession, cmd);
	
	if (!MICommandResultOK(cmd)) {
		DbgSetError(DBGERR_DEBUGGER, GetLastErrorStr());
		MICommandFree(cmd);
		return DBGRES_ERR;
	}

	args = MIGetStackListLocalsInfo(cmd);
	
	MICommandFree(cmd);
	
	e = NewDbgEvent(DBGEV_VARS);
	e->dbg_event_u.list = NewList();

	for (SetList(args); (arg = (MIArg *)GetListElement(args)) != NULL; ) {
		AddToList(e->dbg_event_u.list, (void *)strdup(arg->name));
	}
	
	DestroyList(args, MIArgFree);

	SaveEvent(e);
	
	return DBGRES_OK;
}

/*
** List arguments.
*/
static int
GDBMIListArguments(int level)
{
	dbg_event *	e;
	MICommand *	cmd;
	MIArg *		arg;
	MIFrame *	frame;
	List *		frames;

	CHECK_SESSION()

	cmd = MIStackListArguments(0, level, level);

	SendCommandWait(DebugSession, cmd);
	
	if (!MICommandResultOK(cmd)) {
		DbgSetError(DBGERR_DEBUGGER, GetLastErrorStr());
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
 	 */ 
	SetList(frames);
	if ((frame = (MIFrame *)GetListElement(frames)) != NULL) {
		for (SetList(frame->args); (arg = (MIArg *)GetListElement(frame->args)) != NULL; )
			AddToList(e->dbg_event_u.list, (void *)strdup(arg->name));
	}
	
	DestroyList(frames, MIFrameFree);
	
	SaveEvent(e);
	
	return DBGRES_OK;
}

/*
** List global variables.
*/
static int
GDBMIGetGlobalVariables(void)
{
	CHECK_SESSION()

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
		
	SaveEvent(NewDbgEvent(DBGEV_OK));
	ServerExit++;
	
	return DBGRES_OK;
}

static int
GDBMIGetInfoThread(void) 
{
	MICommand *	cmd;
	dbg_event *	e;
	char *		tid;
	MIThreadInfo *	info;
	
	CHECK_SESSION();
	
	cmd = MIInfoThreads();
	SendCommandWait(DebugSession, cmd);
//	if (!MICommandResultOK(cmd)) {
//		DbgSetError(DBGERR_DEBUGGER, GetLastErrorStr());
//		MICommandFree(cmd);
//		return DBGRES_ERR;
//	}
	info = MIGetInfoThreads(cmd);
	MICommandFree(cmd);
	
	e = NewDbgEvent(DBGEV_THREADS);
	e->dbg_event_u.threads_event.thread_id = info->current_thread_id;
	e->dbg_event_u.threads_event.list = NewList();
	for (SetList(info->thread_ids); (tid = (char *)GetListElement(info->thread_ids)) != NULL;) {
		AddToList(e->dbg_event_u.threads_event.list, (void *)strdup(tid));
	}

	DestroyList(info->thread_ids, free);
	free(info);
	SaveEvent(e);
	
	return DBGRES_OK;
}

static int 
GDBMISetThreadSelect(int threadNum) 
{
	MICommand *	cmd;
	dbg_event *	e;
	MIThreadSelectInfo * info;
	MIFrame *f;
	stackframe *	s;
		
	CHECK_SESSION();
	
	cmd = MIThreadSelect(threadNum);
	SendCommandWait(DebugSession, cmd);
	if (!MICommandResultOK(cmd)) {
		DbgSetError(DBGERR_DEBUGGER, GetLastErrorStr());
		MICommandFree(cmd);
		return DBGRES_ERR;
	}
	info = MISetThreadSelectInfo(cmd);
	MICommandFree(cmd);

	f = info->frame;
	if (f != NULL) {
		s = NewStackframe(f->level);
		if ( f->addr != NULL )
			s->loc.addr = strdup(f->addr);
		if ( f->func != NULL )
			s->loc.func = strdup(f->func);
		if ( f->file != NULL )
			s->loc.file = strdup(f->file);
		s->loc.line = f->line;
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
	MICommand *	cmd;
	dbg_event *	e;
	int depth;
	
	CHECK_SESSION();
	
	cmd = MIStackInfoDepth();
	SendCommandWait(DebugSession, cmd);
	if (!MICommandResultOK(cmd)) {
		DbgSetError(DBGERR_DEBUGGER, GetLastErrorStr());
		MICommandFree(cmd);
		return DBGRES_ERR;
	}
	depth = MIGetStackInfoDepth(cmd);
	MICommandFree(cmd);

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
	memoryinfo *meminfo;
	memory * m;
	
	CHECK_SESSION();
	
	cmd = MIDataReadMemory(offset, address, format, wordSize, rows, cols, asChar);
	SendCommandWait(DebugSession, cmd);
	if (!MICommandResultOK(cmd)) {
		DbgSetError(DBGERR_DEBUGGER, GetLastErrorStr());
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
			for (SetList(info->memories); (mem = (MIMemory *)GetListElement(info->memories)) != NULL;) {
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
					for (SetList(mem->data); (d = (char *)GetListElement(mem->data)) != NULL;) {
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
	
	//printf("----- gdbmi_sevrer: GDBMIDataWriteMemory called ---------\n");	
	cmd = MIDataWriteMemory(offset, address, format, wordSize, value);
	SendCommandWait(DebugSession, cmd);
	if (!MICommandResultOK(cmd)) {
		DbgSetError(DBGERR_DEBUGGER, GetLastErrorStr());
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
	MICommand *	cmd;
	List *signals;
	dbg_event *	e;
	
	CHECK_SESSION();
	
	cmd = CLIListSignals(name);
	SendCommandWait(DebugSession, cmd);
	if (!MICommandResultOK(cmd)) {
		DbgSetError(DBGERR_DEBUGGER, GetLastErrorStr());
		MICommandFree(cmd);
		return DBGRES_ERR;
	}
	CLIGetSigHandleList(cmd, &signals);
	MICommandFree(cmd);

	e = NewDbgEvent(DBGEV_SIGNALS);
	e->dbg_event_u.list = signals;
	
	SaveEvent(e);
	
	return DBGRES_OK;
}

static int 
GDBCLISignalInfo(char* arg) 
{
	MICommand *	cmd;

	CHECK_SESSION();
	
	cmd = CLISignalInfo(arg);
	MICommandRegisterCallback(cmd, ProcessCLIResultRecord, DebugSession);	
	SendCommandWait(DebugSession, cmd);
	if (!MICommandResultOK(cmd)) {
		DbgSetError(DBGERR_DEBUGGER, GetLastErrorStr());
		MICommandFree(cmd);
		return DBGRES_ERR;
	}
	MICommandFree(cmd);
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
		DbgSetError(DBGERR_DEBUGGER, GetLastErrorStr());
		MICommandFree(cmd);
		return DBGRES_ERR;
	}
	MICommandFree(cmd);
	SaveEvent(NewDbgEvent(DBGEV_OK));
	return DBGRES_OK;
} 

#if 0
char tohex[] =	{'0', '1', '2', '3', '4', '5', '6', '7', 
				 '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

static int
GDBMIBuildAIFVar(char *var, char *type, char *file, AIF **aif)
{
	int			n;
	int			fd;
	char *		data;
	char *		ap;
	char *		bp;
	char			buf[BUFSIZ];
	struct stat	sb;

	if ( stat(file, &sb) < 0 )
	{
		DbgSetError(DBGERR_DEBUGGER, (char *)strerror(errno));
		return DBGRES_ERR;
	}

	if ( FDSType(type) == AIF_FUNCTION )
	{
		/*
		** Data is function name
		*/
		ap = data = malloc(strlen(var) * 2 + 1);

		for ( bp = var ; *bp != '\0' ; bp++ )
		{
			*ap++ = tohex[(*bp >> 4) & 0xf];
			*ap++ = tohex[*bp & 0xf];
		}

		*ap++ = '\0';
	}
	else
	{
		if ( (fd = open(file, O_RDONLY)) < 0 )
		{
			DbgSetError(DBGERR_DEBUGGER, (char *)strerror(errno));
			return DBGRES_ERR;
		}

		ap = data = malloc(sb.st_size * 2 + 1);

		while ((n = read(fd, buf, BUFSIZ)) > 0)
		{
			bp = buf;

			while ( n-- > 0 )
			{
				*ap++ = tohex[(*bp >> 4) & 0xf];
				*ap++ = tohex[*bp++ & 0xf];
			}
		}

		*ap++ = '\0';

		(void)close(fd);
	}
	
	if ( FDSType(type) == AIF_POINTER )
	{
		/*
		 * Need to add marker to data
		 */
		ap = malloc(sb.st_size * 2 + 3);
		*ap++ = '0';
		*ap++ = '1'; // normal pointer
		memcpy(ap, data, sb.st_size * 2 + 1);
		data = ap;		
	}
	
	if ( (*aif = AsciiToAIF(type, data)) == NULL )
	{
		DbgSetError(DBGERR_DEBUGGER, AIFErrorStr());
		return DBGRES_ERR;
	}
	
	return DBGRES_OK;
}
#endif
