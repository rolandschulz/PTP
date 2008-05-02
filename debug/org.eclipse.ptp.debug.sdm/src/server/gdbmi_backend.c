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

#include <aif.h>

#include "dbg.h"
#include "dbg_event.h"
#include "backend.h"
#include "list.h"

#include "MI.h"

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

static double		GDB_Version;
static int			ADDRESS_LENGTH = 0;
static MISession *	DebugSession;
static dbg_event *	LastEvent;
static void			(*EventCallback)(dbg_event *);
static int			ServerExit;
static int			Started;
static struct bpmap	BPMap = { 0, 0, NULL };
#if 0
static struct varmap VARMap = { 0, 0, NULL };
#endif
static int			(*AsyncFunc)(void *) = NULL;
static void *		AsyncFuncData;

static int	GDBMIInit(void (*)(dbg_event *));
static int	GDBMIProgress(void);
static int	GDBMIInterrupt(void);
static int	GDBMIStartSession(char *, char *, char *, char *, char **, char **, long);
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
static int	GDBMIListStackframes(int, int);
static int	GDBMISetCurrentStackframe(int);
static int	GDBMIEvaluateExpression(char *);
static int	GDBMIGetNativeType(char *);
static int	GDBMIGetLocalVariables(void);
static int	GDBMIListArguments(int, int);
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
static int	GDBMIDataEvaluateExpression(char*);
static int	GDBGetPartialAIF(char *, char *, int, int);
static int	GDBMIVarDelete(char*);

static void SendCommandWait(MISession *, MICommand *);
static int	SetAndCheckBreak(int, int, int, char *, char *, int, int);
static int	GetStackframes(int, int, int, List **);
static int	GetAIFVar(char *, AIF **, char **);

static AIF * GetAIF(MIVar *, char *, int);
static AIF * GetPartialAIF(MIVar *, char *);
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
	GDBMIDataEvaluateExpression,
	GDBGetPartialAIF,
	GDBMIVarDelete,
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

/**** Variable ****/
static MIVar*
CreateMIVar(char *name)
{
	MICommand *cmd;
	MIVar *mivar;

	cmd = MIVarCreate("-", "*", name);
	SendCommandWait(DebugSession, cmd);
	if (!MICommandResultOK(cmd)) {
		//DbgSetError(DBGERR_UNKNOWN_VARIABLE, GetLastErrorStr());
		MICommandFree(cmd);
		return NULL;
	}
	mivar = MIGetVarCreateInfo(cmd);
	MICommandFree(cmd);
	return mivar;
}

static void
DeleteMIVar(char *mi_name)
{
	MICommand *cmd;
	cmd = MIVarDelete(mi_name);
	SendCommandWait(DebugSession, cmd);
	MICommandFree(cmd);
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
			break;
		}
	}
}

static void
RemoveBPMap(bpentry *bp)
{
	int				i;
	struct bpentry *	map;
	for (i = 0; i < BPMap.size; i++) {
		map = &BPMap.maps[i];
		if (map == bp) {
			map->remote = -1;
			map->local = -1;
			map->temp = 0;
			BPMap.nels--;
			break;
		}
	}		
}

static bpentry *
FindLocalBP(int local) 
{
	int				i;
	struct bpentry *	map;
	for (i = 0; i < BPMap.size; i++) {
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
	for (i = 0; i < BPMap.size; i++) {
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
	int length = BPMap.size;
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

static stackframe *
get_current_frame()
{
	stackframe *f;
	List *	frames;
	
	if (GetStackframes(1, 0, 0, &frames) != DBGRES_OK)
		return NULL;

	if (EmptyList(frames)) {
		DbgSetError(DBGERR_DEBUGGER, "Could not get current stack frame");
		return NULL;
	} 
	
	SetList(frames);
	f = (stackframe *)GetListElement(frames);
	DestroyList(frames, NULL);
	return f;
}

static stackframe *
ConvertMIFrameToStackframe(MIFrame *f)
{
	stackframe *	s;
	if (f == NULL) {//by default return current frame
		return get_current_frame();
	}
	s = NewStackframe(f->level);
	if ( f->addr != NULL )
		s->loc.addr = strdup(f->addr);
	if ( f->func != NULL )
		s->loc.func = strdup(f->func);
	if ( f->file != NULL )
		s->loc.file = strdup(f->file);
	s->loc.line = f->line;
	return s;	
}


#if 0
static void
AddVARMap(char *name, MIVar *mivar)
{
	int i;
	struct varinfo *map;

	if (VARMap.size == 0) {
		VARMap.maps = (struct varinfo *)malloc(sizeof(struct varinfo) * 100);
		VARMap.size = 100;
		
		for (i=0; i<VARMap.size; i++) {
			map = &VARMap.maps[i];
			map->name = NULL; 
			map->mivar = NULL;
		}
	}
	if (VARMap.nels == VARMap.size) {
		i = VARMap.size;
		VARMap.size *= 2;
		VARMap.maps = (struct varinfo *)realloc(VARMap.maps, sizeof(struct varinfo) * VARMap.size);
		
		for (; i<VARMap.size; i++) {
			map = &VARMap.maps[i];
			map->name = NULL; 
			map->mivar = NULL;
		}
	}
	for (i=0; i<VARMap.size; i++) {
		map = &VARMap.maps[i];
		if (map->name == NULL) {
			map->name = strdup(name);
			map->mivar = mivar;
			VARMap.nels++;
			break;
		}
	}
}

static varinfo *
FindVARByName(char *name) 
{
	int	i;
	struct varinfo *map;
	for (i=0; i<VARMap.size; i++) {
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
	int i;
	struct varinfo *map;
	for (i=0; i<VARMap.size; i++) {
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
	if (map != NULL) {
		if (map->name != NULL) {
			free(map->name);
			map->name = NULL;
		}
		if (map->mivar != NULL) {
			DeleteMIVar(map->mivar->name);
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
	int length = VARMap.size;
	for (i = 0; i < length; i++) {
		map = &VARMap.maps[i];
		if (map == NULL)
			return;
			
		RemoveVARMap(map);
	}	
}
#endif

static void
RemoveAllMaps()
{
	RemoveAllBPMap();
#if 0
	RemoveAllVARMap();
#endif
}

static void 
SetDebugError(MICommand * cmd)
{
	if (MICommandResultClass(cmd) == MIResultRecordERROR) {
		char *err = MICommandResultErrorMessage(cmd);
		if (err != NULL) {
			DbgSetError(DBGERR_DEBUGGER, err);
			free(err);
		} else {
			DbgSetError(DBGERR_DEBUGGER, "got error from gdb, but no message");				
		}
	} else {
		DbgSetError(DBGERR_DEBUGGER, "bad response from gdb");
	}
}
static List * 
GetChangedVariables()
{
	MICommand *cmd;	
	List *changes;
	List *changedVars;
	MIVarChange *var;
	
	cmd = MIVarUpdate("*");
	SendCommandWait(DebugSession, cmd);
	if (!MICommandResultOK(cmd)) {
		DEBUG_PRINTS(DEBUG_LEVEL_BACKEND, "------------------- GetChangedVariables error\n");
		SetDebugError(cmd);
		MICommandFree(cmd);
		return NULL;
	}
	MIGetVarUpdateInfo(cmd, &changes);
	MICommandFree(cmd);
	
	changedVars = NewList();
	for (SetList(changes); (var = (MIVarChange *)GetListElement(changes)) != NULL;) {
		if (var->in_scope == 1) {
			AddToList(changedVars, (void *)strdup(var->name));
		}
		else {
			DeleteMIVar(var->name);
		}
	}
	DestroyList(changes, MIVarChangeFree);
	return changedVars;
}

static int 
get_info_depth()
{
	MICommand *cmd;
	int depth;

	cmd = MIStackInfoDepth();
	SendCommandWait(DebugSession, cmd);
	if (!MICommandResultOK(cmd)) {
		DEBUG_PRINTS(DEBUG_LEVEL_BACKEND, "------------------- GDBMIStackInfoDepth error\n");
		SetDebugError(cmd);
		MICommandFree(cmd);
		return -1;
	}
	depth = MIGetStackInfoDepth(cmd);
	MICommandFree(cmd);
	return depth;
}
/**
 * not used
 * 
static void
set_frame_with_line(stackframe *current_f, int depth)
{
	stackframe *f;
	List * frames;
	
	if (GetStackframes(0, 1, depth, &frames) != DBGRES_OK) {
		return NULL;
	}
	
	for (SetList(frames); (f = (stackframe *)GetListElement(frames)) != NULL;) {
		if (f->loc.line > 0) {
			current_f->level = f->level;
			current_f->loc.file = strdup(f->loc.file);
			current_f->loc.func = strdup(f->loc.func);
			current_f->loc.addr = strdup(f->loc.addr);
			current_f->loc.line = f->loc.line;
			break;
		}
	}
	DestroyList(frames, FreeStackframe);
}
*/

/**** aysn stop ****/
static int
AsyncStop(void *data)
{
	dbg_event *	e;
	stackframe *	frame;
	bpentry * bpmap;
	MIEvent *	evt = (MIEvent *)data;

	switch (evt->type)
	{
	case MIEventTypeBreakpointHit:
		bpmap = FindLocalBP(evt->bkptno);
		if (!bpmap->temp) {
			e = NewDbgEvent(DBGEV_SUSPEND);
			e->dbg_event_u.suspend_event.reason = DBGEV_SUSPEND_BPHIT;
			e->dbg_event_u.suspend_event.ev_u.bpid = bpmap->remote;
			e->dbg_event_u.suspend_event.thread_id = evt->threadId;
			e->dbg_event_u.suspend_event.frame = NULL;
			e->dbg_event_u.suspend_event.depth = get_info_depth();
			e->dbg_event_u.suspend_event.changed_vars = GetChangedVariables();
			break;
		}
		/* else must be a temporary breakpoint drop through... */
		RemoveBPMap(bpmap);

	case MIEventTypeSuspended:
		frame = ConvertMIFrameToStackframe(evt->frame);
		if (frame == NULL) {
			ERROR_TO_EVENT(e);
		}
		else {
			e = NewDbgEvent(DBGEV_SUSPEND);
			e->dbg_event_u.suspend_event.reason = DBGEV_SUSPEND_INT;
			e->dbg_event_u.suspend_event.thread_id = evt->threadId;
			e->dbg_event_u.suspend_event.frame = frame;
			e->dbg_event_u.suspend_event.depth = get_info_depth();
			e->dbg_event_u.suspend_event.changed_vars = GetChangedVariables();
		}
		break;

	case MIEventTypeFunctionFinished:
	case MIEventTypeSteppingRange:
		frame = ConvertMIFrameToStackframe(evt->frame);
		if (frame == NULL) {
			ERROR_TO_EVENT(e);
		}
		else {
			e = NewDbgEvent(DBGEV_SUSPEND);
			e->dbg_event_u.suspend_event.reason = DBGEV_SUSPEND_STEP;
			e->dbg_event_u.suspend_event.thread_id = evt->threadId;
			e->dbg_event_u.suspend_event.frame = frame;
			e->dbg_event_u.suspend_event.depth = get_info_depth();
			e->dbg_event_u.suspend_event.changed_vars = GetChangedVariables();
		}
		break;

	case MIEventTypeSignal:
		frame = ConvertMIFrameToStackframe(evt->frame);
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
			e->dbg_event_u.suspend_event.depth = get_info_depth();
			e->dbg_event_u.suspend_event.changed_vars = GetChangedVariables();
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

/*
 * Initialize GDB
 */
static int
GDBMIInit(void (*event_callback)(dbg_event *))
{
	EventCallback = event_callback;
	DebugSession = NULL;
	LastEvent = NULL;
	GDB_Version = -1.0;
	ServerExit = 0;
		
	signal(SIGTERM, SIG_IGN);
	signal(SIGHUP, SIG_IGN);
	signal(SIGINT, SIG_IGN);

	return DBGRES_OK;
}

#if 0
static int 
timeout_cb(void *data)
{
	return 0;
}
#endif

/*
 * Send command and wait for immediate response.
 */
static void
SendCommandWait(MISession *sess, MICommand *cmd)
{
	MISessionSendCommand(sess, cmd);
	MIOutput *output = MIOutputNew();
	do {
		MISessionProgress(sess, output);
		if (sess->out_fd == -1) {
			DEBUG_PRINTS(DEBUG_LEVEL_BACKEND, "------------------- SendCommandWait sess->out_fd = -1\n");
			break;
		}
	} while (!MISessionCommandCompleted(sess));
}

/*
 * Start GDB session
 */	
static int
GDBMIStartSession(char *gdb_path, char *prog, char *path, char *work_dir, char **args, char **env, long timeout)
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
	
	cmd = MIExecArguments(args);
	SendCommandWait(sess, cmd);
	MICommandFree(cmd);

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
	
	cmd = MIGDBVersion();
	SendCommandWait(sess, cmd);
	if (MICommandResultOK(cmd)) {
		GDB_Version = CLIGetGDBVersion(cmd);	
		DEBUG_PRINTF(DEBUG_LEVEL_BACKEND, "------------------- gdb version: %f\n", GDB_Version);
	}
	MICommandFree(cmd);
	
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
	if (LastEvent != NULL) {
		if (EventCallback != NULL) {
			EventCallback(LastEvent);
		}
			
		if (ServerExit && LastEvent->event_id == DBGEV_OK) {
			if (DebugSession != NULL) {
				MISessionFree(DebugSession);
				DebugSession = NULL;
			}
		}
			
		FreeDbgEvent(LastEvent);
		LastEvent = NULL;
		return 0;
	}
	
	if (DebugSession != NULL) {
		if (MISessionProgress(DebugSession, MIOutputNew()) < 0) {
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

	CHECK_SESSION();

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
	
	SetList(bpts);
	bpt = (MIBreakpoint *)GetListElement(bpts);
	
	AddBPMap(bpt->number, bpid, isTemp);
	
	//if the type is temporary, no need to send bpt set event
	if (isTemp) {
		SaveEvent(NewDbgEvent(DBGEV_OK));
		DestroyList(bpts, MIBreakpointFree);
		return DBGRES_OK;
	}
	
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
	
	CHECK_SESSION();
	
	if ((bp = FindRemoteBP(bpid)) == NULL) {
		asprintf(&bpstr, "%d", bpid);
		DbgSetError(DBGERR_NOBP, bpstr);
		free(bpstr);
		return DBGRES_ERR;
	}
	
	cmd = MIBreakDelete(1, &bp->local);
	SendCommandWait(DebugSession, cmd);
	
	if (!MICommandResultOK(cmd)) {
		SetDebugError(cmd);
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
	
	CHECK_SESSION();

	if ((bp = FindRemoteBP(bpid)) == NULL) {
		asprintf(&bpstr, "%d", bpid);
		DbgSetError(DBGERR_NOBP, bpstr);
		free(bpstr);
		return DBGRES_ERR;
	}
	
	cmd = MIBreakEnable(1, &bp->local);
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
	bpentry *	bp;
	char *		bpstr;
	MICommand *	cmd;
	
	CHECK_SESSION();

	if ((bp = FindRemoteBP(bpid)) == NULL) {
		asprintf(&bpstr, "%d", bpid);
		DbgSetError(DBGERR_NOBP, bpstr);
		free(bpstr);
		return DBGRES_ERR;
	}
	
	cmd = MIBreakDisable(1, &bp->local);
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
	bpentry *	bp;
	char *		bpstr;
	MICommand *	cmd;
	
	CHECK_SESSION();

	if ((bp = FindRemoteBP(bpid)) == NULL) {
		asprintf(&bpstr, "%d", bpid);
		DbgSetError(DBGERR_NOBP, bpstr);
		free(bpstr);
		return DBGRES_ERR;
	}
	
	cmd = MIBreakCondition(1, &bp->local, expr);
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
	bpentry *	bp;
	char *		bpstr;
	MICommand *	cmd;
	
	CHECK_SESSION();

	if ((bp = FindRemoteBP(bpid)) == NULL) {
		asprintf(&bpstr, "%d", bpid);
		DbgSetError(DBGERR_NOBP, bpstr);
		free(bpstr);
		return DBGRES_ERR;
	}
	
	cmd = MIBreakAfter(1, &bp->local, icount);
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
	List *			bpts;
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

static int
GetStackframes(int current, int low, int high, List **flist)
{
	List *		frames;
	MIFrame *	f;
	MICommand *	cmd;
	stackframe *	s;
	
	//checking gdb version
	if (current) {
		if (GDB_Version > 6.3) {
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
	
	if ( frames == NULL )
	{
		DbgSetError(DBGERR_DEBUGGER, "Failed to get stack frames from backend");
		return DBGRES_ERR;
	}
	
	*flist = NewList();
	for (SetList(frames); (f = (MIFrame *)GetListElement(frames)) != NULL; ) {
		s = ConvertMIFrameToStackframe(f);
		AddToList(*flist, (void *)s);
	}
	DestroyList(frames, MIFrameFree);
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

	if (GetStackframes(0, low, high, &frames) != DBGRES_OK)
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

int
DumpBinaryValue(MISession *sess, char *exp, char *file)
{
	MICommand *	cmd;
	
	cmd = MICommandNew("dump binary value", MIResultRecordDONE);
	MICommandAddOption(cmd, file, exp);
	
	SendCommandWait(DebugSession, cmd);
	
	if (!MICommandResultOK(cmd)) {
		SetDebugError(cmd);
		MICommandFree(cmd);
		return -1;
	}		
	MICommandFree(cmd);
	return 0;
}
#endif

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

	for (SetList(args); (arg = (MIArg *)GetListElement(args)) != NULL; ) {
		AddToList(e->dbg_event_u.list, (void *)strdup(arg->name));
	}
	
	DestroyList(args, MIArgFree);

	SaveEvent(e);
	
	return DBGRES_OK;
}

/*
 * This is needed to check for a bug in the Linux x86 GCC 4.1 compiler
 * that causes gdb 6.4 and 6.5 to crash under certain conditions.
 */
#define GDB_BUG_2188	__gnu_linux__ && __i386__ && __GNUC__ == 4 && __GNUC_MINOR__ == 1

#if GDB_BUG_2188
static int
CurrentFrame(int level, char *name)
{
	MICommand *	cmd;
	MIFrame *	frame;
	List *		frames;
	int val = 0;
	
	if (GDB_Version > 6.3 && GDB_Version < 6.7) {
		cmd = MIStackListFrames(level, level);
		SendCommandWait(DebugSession, cmd);
		if (!MICommandResultOK(cmd)) {
			MICommandFree(cmd);
			return val;
		}
		frames = MIGetStackListFramesInfo(cmd);
	
		//only one frame
		SetList(frames);
		if ((frame = (MIFrame *)GetListElement(frames)) != NULL) {
			if (frame->func != NULL && strncmp(frame->func, name, 4) == 0) {
				val = 1;
			}
		}
		DestroyList(frames, MIFrameFree);
	}
		
	return val;
}
#endif /* GDB_BUG_2188 */

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
	List *		frames;

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
	SetList(frames);
	if ((frame = (MIFrame *)GetListElement(frames)) != NULL) {
#if GDB_BUG_2188
		if (!CurrentFrame(frame->level, "main")) {
#endif /* GDB_BUG_2188 */
			for (SetList(frame->args); (arg = (MIArg *)GetListElement(frame->args)) != NULL; )
				AddToList(e->dbg_event_u.list, (void *)strdup(arg->name));
#if GDB_BUG_2188
		}
#endif /* GDB_BUG_2188 */
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
	RemoveAllMaps();
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
	//MIFrame *f;
	stackframe *s = NULL;
		
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
		s = ConvertMIFrameToStackframe(info->frame);
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

	if ((depth = get_info_depth()) == -1) {
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
	MICommand *	cmd;
	List *signals;
	dbg_event *	e;
	
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
		SetDebugError(cmd);
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
		SetDebugError(cmd);
		MICommandFree(cmd);
		return DBGRES_ERR;
	}
	MICommandFree(cmd);
	SaveEvent(NewDbgEvent(DBGEV_OK));
	return DBGRES_OK;
} 

static int
GDBMIDataEvaluateExpression(char *arg)
{
	MICommand *cmd;
	dbg_event *	e;
	char *res = NULL;
		
	CHECK_SESSION();
	
	cmd = MIDataEvaluateExpression(arg);
	SendCommandWait(DebugSession, cmd);
		
	if (MICommandResultOK(cmd)) {
		res = MIGetDataEvaluateExpressionInfo(cmd);
	}
	if (res == NULL) {
		res = strdup("No value found.");
	}
	MICommandFree(cmd);

	e = NewDbgEvent(DBGEV_DATA_EVA_EX);
	e->dbg_event_u.data_expression = res;
	SaveEvent(e);

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

	if (GetAIFVar(exp, &a, &type) != DBGRES_OK)
		return DBGRES_ERR;
		
	e = NewDbgEvent(DBGEV_DATA);
	e->dbg_event_u.data_event.data = a;
	e->dbg_event_u.data_event.type_desc = type;
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

	if (GetAIFVar(var, &a, &type) != DBGRES_OK)
		return DBGRES_ERR;
		
	e = NewDbgEvent(DBGEV_TYPE);
	e->dbg_event_u.type_desc = type;

	SaveEvent(e);
	AIFFree(a);
	return DBGRES_OK;
}

static int
GetAIFVar(char *var, AIF **val, char **type)
{
	AIF * res;
	MIVar *mivar;
	
	mivar = CreateMIVar(var);
	if (mivar == NULL) {
		DbgSetError(DBGERR_UNKNOWN_VARIABLE, var);
		return DBGRES_ERR;
	}

	if ((res = GetAIF(mivar, var, 0)) == NULL) {
		DbgSetError(DBGERR_UNKNOWN_TYPE, mivar->type);
		DeleteMIVar(mivar->name);
		MIVarFree(mivar);
		return DBGRES_ERR;
	}
	*type = strdup(mivar->type);
	*val = res;

	DeleteMIVar(mivar->name);
	MIVarFree(mivar);

	return DBGRES_OK;
}

static char *
GetVarValue(char *var)
{
	char *		res;
	MICommand *	cmd = MIVarEvaluateExpression(var);

	SendCommandWait(DebugSession, cmd);
	if (!MICommandResultOK(cmd)) {
		SetDebugError(cmd);
		MICommandFree(cmd);
		return "";
	}
	res = MIGetVarEvaluateExpressionInfo(cmd);
	MICommandFree(cmd);
	return res;
}

static int
GetAddressLength()
{
	if (ADDRESS_LENGTH != 0) {
		return ADDRESS_LENGTH;
	}
	char * res;	
	MICommand * cmd = MIDataEvaluateExpression("\"sizeof(char *)\"");
	SendCommandWait(DebugSession, cmd);
	if (!MICommandResultOK(cmd)) {
		SetDebugError(cmd);
		MICommandFree(cmd);
		return 0;
	}
	res = MIGetDataEvaluateExpressionInfo(cmd);	
	MICommandFree(cmd);
	
	ADDRESS_LENGTH = (int)strtol(res, NULL, 10);
	return ADDRESS_LENGTH;
}

static char * 
GetPtypeValue(char *exp) 
{
	char * type = NULL;
	MICommand* cmd = CLIPType(exp);
	SendCommandWait(DebugSession, cmd);
	type = CLIGetPTypeInfo(cmd);
	MICommandFree(cmd);
	return type;
}

/********************************************************
 * TYPE CONVERSION 
 ********************************************************/
#define T_OTHER			0
#define T_CHAR			1
#define T_SHORT			2
#define T_USHORT		3
#define T_INT			4
#define T_UINT			5
#define T_LONG			6
#define T_ULONG			7
#define T_LONGLONG		8
#define T_ULONGLONG		9
#define T_FLOAT			10
#define T_DOUBLE		11
#define T_STRING		12
#define T_BOOLEAN		13

#define T_CHAR_PTR		14
#define T_FUNCTION		15
#define T_VOID_PTR		16
#define T_UNION			17
#define T_ENUM			18
#define T_ARRAY			19
#define T_STRUCT		20
#define T_POINTER		21

static int 
get_simple_type(char *type)
{
	char *t = NULL;
	int id;
	int len = strlen(type);

	if (type[len - 1] == ')') { // function
		return T_FUNCTION;
	}
	if (strncmp(type, "void *", 6) == 0) { // void pointer
		return T_VOID_PTR;
	}
	if (strncmp(type, "enum", 4) == 0) { // enum
		return T_ENUM;
	}
	
	//check modifiers
	if (strncmp(type, "const volatile", 14) == 0)
		t = strdup(&type[15]); //+ 1 remove whitespeace
	else if (strncmp(type, "volatile", 8) == 0)
		t = strdup(&type[9]); //+ 1 remove whitespeace
	else if (strncmp(type, "const", 5) == 0)
		t = strdup(&type[6]); //+ 1 remove whitespeace
	else
		t = strdup(type);
		
	if (strncmp(t, "char *", 6) == 0)
		id = T_CHAR_PTR;
	else if (strncmp(t, "char", 4) == 0)
		id = T_CHAR;
	else if (strncmp(t, "unsigned char", 13) == 0)
		id = T_CHAR;
	else if (strncmp(t, "short int", 9) == 0 || strncmp(t, "int2", 4) == 0)
		id = T_SHORT;
	else if (strncmp(t, "short unsigned int", 18) == 0)
		id = T_USHORT;
	else if (strncmp(t, "int", 3) == 0 || strncmp(t, "int4", 4) == 0)
		id = T_INT;
	else if (strncmp(t, "unsigned int", 12) == 0)
		id = T_UINT;
	else if (strncmp(t, "long int", 8) == 0 || strncmp(t, "int8", 4) == 0)
		id = T_LONG;
	else if (strncmp(t, "long unsigned int", 17) == 0)
		id = T_ULONG;
#ifdef CC_HAS_LONG_LONG
	else if (strncmp(t, "long long int", 13) == 0 || strncmp(t, "real*16", 7) == 0)
		id = T_LONGLONG;
	else if (strncmp(t, "long long unsigned int", 22) == 0)
		id = T_ULONGLONG;
#endif /* CC_HAS_LONG_LONG */
	else if (strncmp(t, "long", 4) == 0 || strncmp(t, "real*4", 6) == 0)
		id = T_LONG;
	else if (strncmp(t, "float", 5) == 0 || strncmp(t, "real*8", 6) == 0)
		id = T_FLOAT;
	else if (strncmp(t, "double", 6) == 0)
		id = T_DOUBLE;
	else if (strncmp(t, "string", 6) == 0)
		id = T_STRING;
 	else if (strncmp(t, "logical4", 8) == 0)
 		id = T_BOOLEAN;	
	else
		id =  T_OTHER;

	free(t);
	return id;
}

static int 
get_complex_type(char *type) 
{
	int len = strlen(type);
	switch (type[len - 1]) {
	case ']':
		return T_ARRAY;
	case '*':
		if (type[len - 2] == '*') //pointer pointer
			return T_POINTER;
		if (strncmp(type, "char", 4) == 0) //char pointer
			return T_CHAR_PTR;
		return T_POINTER; //normal pointer
	default:
		if (strncmp(type, "union", 5) == 0)
			return T_UNION;

		if (strncmp(type, "struct", 6) == 0)
			return T_STRUCT;

		return T_OTHER;
	}
}

static AIF* 
GetPrimitiveAIF(int id, char *res)
{
	char *pch;
	
	if (res == NULL) {
		return NULL;
	}
	switch (id) {
		case T_STRING:
		case T_CHAR:
			if ((pch = strchr(res, ' ')) != NULL) {
				pch++;
				if (*pch == '\'') { //character
					pch--;
					*pch = '\0';
					return CharToAIF((char)strtol(res, NULL, 10));
				}
				else { //string
					return StringToAIF(pch);			
				}
			}
			else {
				return CharToAIF((char)strtol(res, NULL, 10));
			}
			break;
		case T_SHORT:
			return ShortToAIF((short)strtol(res, NULL, 10));
		case T_USHORT:
			return UnsignedShortToAIF((unsigned short)strtoul(res, NULL, 10));
		case T_INT:
			return IntToAIF((int)strtol(res, NULL, 10));
		case T_UINT:
			return UnsignedIntToAIF((unsigned int)strtoul(res, NULL, 10));
		case T_LONG:
			return LongToAIF(strtol(res, NULL, 10));
		case T_ULONG:
			return UnsignedLongToAIF((unsigned long)strtoul(res, NULL, 10));
		#ifdef CC_HAS_LONG_LONG				
			case T_LONGLONG:
				return LongLongToAIF(strtoll(res, NULL));
			case T_ULONGLONG:
				return UnsignedLongLongToAIF((unsigned long long)strtoull(res, NULL));
		#endif /* CC_HAS_LONG_LONG */
		case T_FLOAT:
			return FloatToAIF((float)strtod(res, NULL));
		case T_DOUBLE:
			return DoubleToAIF(strtod(res, NULL));
		default://other type
			return VoidToAIF(0, 0);
	}
}

static AIF *
GetAIFPointer(char *addr, AIF *i)
{
	AIF *ac;
	AIF *a;
	char *pch;
	
	if (addr == NULL) {
		ac = VoidToAIF(0, 0);
	}
	else {
		if ((pch = strchr(addr, ' ')) != NULL) {
			*pch = '\0';
		}
		addr += 2; //skip 0x
		ac = AddressToAIF(addr, GetAddressLength());
	}
	a = PointerToAIF(ac, i);
	AIFFree(ac);
	return a;
}

static AIF *
GetCharPointerAIF(char *res)
{
	char *pch;
	char *val;
	AIF *a;
	AIF *ac;
	
	if ((pch = strchr(res, ' ')) != NULL) {
		val = strdup(pch+1);
		*pch = '\0';

		res += 2;  //skip 0x
		ac = AddressToAIF(res, GetAddressLength());
		a = CharPointerToAIF(ac, val);
		free(val);
		AIFFree(ac);
		return a;
	}
	return VoidToAIF(0, 0);
}

static AIF *
GetSimpleAIF(MIVar *var, char *exp)
{
	AIF		*a = NULL;
	AIF		*ac;
	char	*pt;

	int id = get_simple_type(var->type);
	if (id == T_OTHER) {
		pt = GetPtypeValue(var->type);
		if (pt != NULL) {
			var->type = strdup(pt);
			id = get_simple_type(var->type);
			free(pt);
		}
	}
	switch (id) {
	case T_FUNCTION:
		return MakeAIF("&/is4", exp);
	case T_VOID_PTR:
		ac = VoidToAIF(0, 0);
		a = GetAIFPointer(GetVarValue(var->name), ac);
		AIFFree(ac);
		return a;
	case T_ENUM:
		return (var->type[4] == ' ') ? EmptyEnumToAIF(&var->type[5]) : EmptyEnumToAIF(NULL);
	case T_OTHER:
		if (exp == NULL)
			return NULL;
		
		return GetSimpleAIF(var, NULL);
	default:
		return GetPrimitiveAIF(id, GetVarValue(var->name));
	}
}

static AIF* 
GetNamedAIF(AIF *a, int named) 
{
	if (FDSType(AIF_FORMAT(a)) != AIF_NAME) {
		return NameAIF(a, named);
	}
	return a;
}

static AIF *
GetStructAIF(MIVar *var, int named)
{
	int		i;
	MIVar *	v;
	AIF *	a;
	AIF *	ac;
	char *pch;	
	char *struct_name = strdup(var->type);
	named++;

	if ((pch = strchr(struct_name, ' ')) != NULL) {
		pch++;
		a = EmptyStructToAIF(pch);
	}
	else {
		a = EmptyStructToAIF(struct_name);
	}
	free(struct_name);

	for (i=0; i<var->numchild; i++) {
		v = var->children[i];
		//check whether child contains parent
		if (strcmp(var->type, v->type) == 0 && strcmp(var->name, v->name)) {
			a = GetNamedAIF(a, named);
			ac = AIFNull(a);
		}
		else {
			if ((ac = GetAIF(v, v->exp, named)) == NULL) {
				AIFFree(a);
				return NULL;
			}
		}
		AIFAddFieldToStruct(a, v->exp, ac);
	}
	return a;
}

static AIF *
GetUnionAIF(MIVar *var, int named)
{
	int		i;
	MIVar *	v;
	AIF *	a;
	AIF *	ac;
	char *union_name = NULL;
	named++;

	if (var->type[5] == ' ') {
		union_name = strdup(&var->type[6]);
	}
	a = EmptyUnionToAIF(union_name);
	if (union_name != NULL)
		free(union_name);
	
	for (i=0; i<var->numchild; i++) {
		v = var->children[i];
		//check whether child contains parent
		if (strcmp(var->type, v->type) == 0 && strcmp(var->name, v->name)) {
			a = GetNamedAIF(a, named);
			ac = AIFNull(a);
		}
		else {
			if ((ac = GetAIF(v, v->exp, named)) == NULL) {
				AIFFree(a);
				return NULL;
			}
		}
		AIFAddFieldToUnion(a, v->exp, AIF_FORMAT(ac));

		//Set the union value
		if (i == var->numchild - 1)
			AIFSetUnion(a, v->exp, ac);

		AIFFree(ac);
	}
	return a;
}

static AIF *
GetArrayAIF(MIVar *var, int named)
{
	int		i;
	MIVar *	v;
	AIF *	a = NULL;
	AIF *	ac;

	for (i = 0; i < var->numchild; i++) {
		v = var->children[i];
		if ((ac = GetAIF(v, v->exp, named)) == NULL) {
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
GetPointerAIF(MIVar *var, int named)
{
	AIF *ac;
	AIF *a;
	int id;
	
	var->type[strlen(var->type) - 1] = '\0'; //remove pointer
	while (var->type[strlen(var->type) - 1] == ' ') {//remove whilespace
		var->type[strlen(var->type) - 1] = '\0';
	}

	id = get_complex_type(var->type);
	switch (id) {
		case T_POINTER:
			ac = GetAIF(var->children[0], var->children[0]->exp, named);
			break;
		case T_CHAR_PTR:
			return GetCharPointerAIF(GetVarValue(var->children[0]->name));
		case T_UNION:
			ac = GetUnionAIF(var, named);
			break;
		default:
			if (var->numchild == 1) {
				ac = GetAIF(var->children[0], var->children[0]->exp, named);
			}
			else {
				ac = GetStructAIF(var, named);
			}
			break;
	}
	
	if (ac == NULL) {
		ac = VoidToAIF(0, 0);
	}
	a = GetAIFPointer(GetVarValue(var->name), ac);
	AIFFree(ac);
	return a;
}

static AIF * 
GetComplexAIF(MIVar *var, char *exp, int named) 
{
	int id = get_complex_type(var->type);
	switch (id) {
	case T_ARRAY:
		return GetArrayAIF(var, named);
	case T_CHAR_PTR:
		return GetCharPointerAIF(GetVarValue(var->name));
	case T_POINTER:
		return GetPointerAIF(var, named); 
	case T_UNION:
		return GetUnionAIF(var, named);
	default://struct
		return GetStructAIF(var, named);
	}
}

static AIF *
GetAIF(MIVar *var, char *exp, int named)
{
	MICommand	*cmd;

	if (strcmp(var->type, "<text variable, no debug info>") == 0) {
		DbgSetError(DBGERR_NOSYMS, "");
		return NULL;
	}
	if (var->numchild == 0) { //simple type
		return GetSimpleAIF(var, exp);
	}
	//complex type
	cmd = MIVarListChildren(var->name);
	SendCommandWait(DebugSession, cmd);
	if (!MICommandResultOK(cmd)) {
		if (MICommandResultClass(cmd) == MIResultRecordERROR) {
			char *err = MICommandResultErrorMessage(cmd);
			if (err != NULL) {
				DbgSetError(DBGERR_DEBUGGER, err);
				free(err);
			} else {
				DbgSetError(DBGERR_DEBUGGER, "got error from gdb, but no message");				
			}
		} else {
			DbgSetError(DBGERR_DEBUGGER, "bad response from gdb");
		}
		MICommandFree(cmd);
		return NULL;
	}
	MIGetVarListChildrenInfo(var, cmd);
	MICommandFree(cmd);

	return GetComplexAIF(var, exp, named);
}

/*************************** PARTIAL AIF ***************************/
static AIF * 
GetPartialArrayAIF(MIVar *var)
{
	AIF *a = NULL;
	AIF *ac;
	int i;
	char *pch;
	char *pt;
	int id;

	if (var->children == NULL) {
		pch = strchr(var->type, '[');
		*pch = '\0';
		while (var->type[strlen(var->type) - 1] == ' ') {//remove whilespace
			var->type[strlen(var->type) - 1] = '\0';
		}
		id = get_simple_type(var->type);
		if (id == T_OTHER) {
			pt = GetPtypeValue(var->type);
			if (pt != NULL) {
				var->type = strdup(pt);
				id = get_simple_type(var->type);
				free(pt);
			}
		}
		ac = GetPrimitiveAIF(id, "");
		a = EmptyArrayToAIF(0, var->numchild-1, ac);
		AIFFree(ac);
	}
	else {
		for (i=0; i<var->numchild; i++) {
			ac = GetPartialAIF(var->children[i], NULL);
			if (a == NULL) {
				a = EmptyArrayToAIF(0, var->numchild-1, ac);
			}
			AIFAddArrayElement(a, i, ac);
			AIFFree(ac);				
		}
	}
	return a;
}

static AIF *
GetPartialStructAIF(MIVar *var)
{
	AIF *ac;
	AIF *a;
	int i;
	char *pch;
	char *struct_name = strdup(var->type);

	if ((pch = strchr(struct_name, ' ')) != NULL) {
		pch++;
		a = EmptyStructToAIF(pch);
	}
	else {
		a = EmptyStructToAIF(struct_name);
	}
	free(struct_name);

	if (var->children != NULL) {
		for (i=0; i<var->numchild; i++) {
			ac = GetPartialAIF(var->children[i], var->children[i]->exp);
			AIFAddFieldToStruct(a, var->children[i]->exp, ac);
			AIFFree(ac);
		}
	}
	return a;
}

static AIF *
GetPartialUnionAIF(MIVar *var)
{
	AIF *ac;
	AIF *a;
	int i;
	char *union_name = NULL;

	if (var->type[5] == ' ') {
		union_name = strdup(&var->type[6]);
	}
	a = EmptyUnionToAIF(union_name);
	if (union_name != NULL)
		free(union_name);

	if (var->children != NULL) {
		for (i=0; i<var->numchild; i++) {
			ac = GetPartialAIF(var->children[i], var->children[i]->exp);
			AIFAddFieldToUnion(a, var->children[i]->exp, AIF_FORMAT(ac));
			if (i == var->numchild - 1) {
				AIFSetUnion(a, var->children[i]->exp, ac);
			}
			AIFFree(ac);
		}
	}
	return a;
}

static AIF *
GetPartialPointerAIF(MIVar *var)
{
	AIF *ac;
	AIF *a;
	int id;
	
	if (var->children != NULL) {
		var->type[strlen(var->type) - 1] = '\0'; //remove pointer
		while (var->type[strlen(var->type) - 1] == ' ') {//remove whilespace
			var->type[strlen(var->type) - 1] = '\0';
		}

		id = get_complex_type(var->type);
		switch (id) {
			case T_CHAR_PTR:
				//replace miname
				var->name = strdup(var->children[0]->exp);
				a = GetCharPointerAIF(GetVarValue(var->children[0]->name));
				break;			
			case T_POINTER:
				//replace miname
				var->name = strdup(var->children[0]->exp);
				ac = VoidToAIF(0, 0);
				a = GetAIFPointer(GetVarValue(var->children[0]->name), ac);
				AIFFree(ac);
				break;
			case T_UNION:
				a = GetPartialUnionAIF(var);
				break;
			default:
				if (var->numchild == 1) {
					//replace miname
					var->name = strdup(var->children[0]->exp);
					a = GetPartialAIF(var->children[0], var->children[0]->exp);
				}
				else {
					a = GetPartialStructAIF(var);
				}
				break;
		}
	}
	else {
		id = get_complex_type(var->type);
		switch (id) {
			case T_CHAR_PTR:
				a = GetCharPointerAIF(GetVarValue(var->name));
				break;
			default:			
				ac = VoidToAIF(0, 0);
				a = GetAIFPointer(GetVarValue(var->name), ac);
				AIFFree(ac);
				break;
		}
	}
	return a;
}

static AIF * 
GetPartialComplexAIF(MIVar *var, char *exp)
{
	char *pt;
	int id = get_complex_type(var->type);
	if (id == T_OTHER) {
		pt = GetPtypeValue(var->type);
		if (pt != NULL) {
			var->type = pt;
			id = get_complex_type(var->type);
		}
	}
	switch (id) {
	case T_ARRAY:
		return GetPartialArrayAIF(var);
	case T_CHAR_PTR:
		return GetCharPointerAIF(GetVarValue(var->name));
	case T_POINTER:
		return GetPartialPointerAIF(var); 
	case T_UNION:
		return GetPartialUnionAIF(var);
	default://struct
		return GetPartialStructAIF(var);
	}
}

static AIF *
GetPartialAIF(MIVar *var, char *exp)
{
	if (strcmp(var->type, "<text variable, no debug info>") == 0) {
		DbgSetError(DBGERR_NOSYMS, "");
		return NULL;
	}
	if (var->numchild == 0) {
		return GetSimpleAIF(var, exp);
	}
	return GetPartialComplexAIF(var, exp);
}

static MIVar *
GetChildrenMIVar(char *mivar_name, MIVar *mivar, int showParentType)
{
	MICommand *cmd;

	if (showParentType || mivar == NULL) {
		cmd = MIVarInfoType(mivar_name);
		SendCommandWait(DebugSession, cmd);
		if (!MICommandResultOK(cmd)) {
			MICommandFree(cmd);
			return NULL;
		}
		mivar = MIGetVarInfoType(cmd);
		mivar->name = strdup(mivar_name);
		MICommandFree(cmd);
	}
	cmd = MIVarListChildren(mivar_name);
	SendCommandWait(DebugSession, cmd);
	if (!MICommandResultOK(cmd)) {
		MICommandFree(cmd);
		return NULL;
	}
	MIGetVarListChildrenInfo(mivar, cmd);
	MICommandFree(cmd);
	return mivar;	
}

static MIVar *
GetMIVarDetails(char *name)
{
	MICommand *cmd;
	MIVar *mivar;

	cmd = MIVarInfoType(name);
	SendCommandWait(DebugSession, cmd);
	if (!MICommandResultOK(cmd)) {
		MICommandFree(cmd);
		return NULL;
	}
	mivar = MIGetVarInfoType(cmd);
	mivar->name = strdup(name);
	MICommandFree(cmd);

	cmd = MIVarInfoNumChildren(name);
	SendCommandWait(DebugSession, cmd);
	MIGetVarInfoNumChildren(cmd, mivar);
	MICommandFree(cmd);
	return mivar;
}

static int
GDBGetPartialAIF(char* name, char* key, int listChildren, int express)
{
	MIVar *mivar;
	AIF *a;
	dbg_event *	e;
	char* var_name;

	CHECK_SESSION();

	//key is first perference if it exists 
	var_name = (key == NULL || strlen(key) == 0)?name:key;

	if (express) {
		mivar = GetMIVarDetails(var_name);
	}
	else {	
		//@ - for partial array
		if (!listChildren || strchr(var_name, '@') != NULL) {
			mivar = CreateMIVar(var_name);
			if (listChildren) {
				mivar = GetChildrenMIVar(mivar->name, mivar, 0);
			}
		}
		else {
			mivar = GetChildrenMIVar(var_name, NULL, 1);
		}
	}
	
	if (mivar == NULL) {
		//try again with original variable name
		var_name = name;
		if ((mivar = CreateMIVar(var_name)) == NULL) {
			DbgSetError(DBGERR_UNKNOWN_VARIABLE, var_name);
			return DBGRES_ERR; 
		}
	}
	if ((a = GetPartialAIF(mivar, var_name)) == NULL) {
		DbgSetError(DBGERR_UNKNOWN_TYPE, mivar->type);
		MIVarFree(mivar);
		return DBGRES_ERR; 
	}
	
	DEBUG_PRINTF(DEBUG_LEVEL_BACKEND, "---------------------- GDBGetPartialAIF found key: %s, format: %s\n", mivar->name, AIF_FORMAT(a));

	e = NewDbgEvent(DBGEV_PARTIAL_AIF);
	e->dbg_event_u.partial_aif_event.data = a;
	e->dbg_event_u.partial_aif_event.type_desc = strdup(mivar->type);
	e->dbg_event_u.partial_aif_event.name = strdup(mivar->name);

	MIVarFree(mivar);
	SaveEvent(e);

	return DBGRES_OK;
}

static int
GDBMIVarDelete(char *name)
{
	CHECK_SESSION();
	DeleteMIVar(name);
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
