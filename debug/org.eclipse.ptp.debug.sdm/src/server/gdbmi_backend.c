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

#include <string.h>
#include <errno.h>
#include <fcntl.h>

#include <mi_gdb.h>
#include <aif.h>

#include "dbg.h"
#include "dbg_event.h"
#include "backend.h"
#include "list.h"

//#define DEBUG

struct bpentry {
	int local;
	int remote;
};

struct bpmap {
	int				nels; // number of elements currently in map
	int				size; // total size of map
	struct bpentry *	maps;
};

static struct timeval	SELECT_TIMEOUT = { 0, 1000 };
static mi_h *		MIHandle;
static dbg_event *	LastEvent;
static void			(*EventCallback)(dbg_event *, void *);
static void *		EventCallbackData;
static int			ServerExit;
static int			Started;
static struct bpmap	BPMap = { 0, 0, NULL };
static int			(*AsyncFunc)(void *) = NULL;
static void *		AsyncFuncData;

static int	GDBMIBuildAIFVar(char *, char *, char *, AIF **);
static int	SetAndCheckBreak(int, char *);
static int	GetStackframes(int, List **);
static int	GetTypeInfo(char *, char **, char **);

static int	GDBMIInit(void (*)(dbg_event *, void *), void *);
static int	GDBMIProgress(void);
static int	GDBMIInterrupt(void);
static int	GDBMIStartSession(char *, char *, char *, char *, char **);
static int	GDBMISetLineBreakpoint(int, char *, int);
static int	GDBMISetFuncBreakpoint(int, char *, char *);
static int	GDBMIDeleteBreakpoint(int);
static int	GDBMIGo(void);
static int	GDBMIStep(int, int);
static int	GDBMITerminate(void);
static int	GDBMIListStackframes(int);
static int	GDBMISetCurrentStackframe(int);
static int	GDBMIEvaluateExpression(char *);
static int	GDBMIGetNativeType(char *);
static int	GDBMIGetAIFType(char *);
static int	GDBMIGetLocalVariables(void);
static int	GDBMIListArguments(int);
static int	GDBMIGetGlobalVariables(void);
static int	GDBMIQuit(void);

dbg_backend_funcs	GDBMIBackend =
{
	GDBMIInit,
	GDBMIProgress,
	GDBMIInterrupt,
	GDBMIStartSession,
	GDBMISetLineBreakpoint,
	GDBMISetFuncBreakpoint,
	GDBMIDeleteBreakpoint,
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
	GDBMIQuit
};

#define CHECK_SESSION() \
	if (MIHandle == NULL) { \
		DbgSetError(DBGERR_NOSESSION, NULL); \
		return DBGRES_ERR; \
	}
	
#define ERROR_TO_EVENT(e) \
	e = NewDbgEvent(DBGEV_ERROR); \
	e->error_code = DbgGetError(); \
	e->error_msg = strdup(DbgGetErrorStr())

static char *
GetLastErrorStr(void)
{
	if (mi_error == MI_FROM_GDB && mi_error_from_gdb != NULL)
 		return mi_error_from_gdb;
	return (char *)mi_get_error_str();
}

static void
ResetError(void)
{
	mi_error=MI_OK;
}

static void
SaveEvent(dbg_event *e)
{
	if (LastEvent != NULL)
		FreeDbgEvent(LastEvent);
		
	LastEvent = e;
}

static void
AddBPMap(int local, int remote)
{
	int				i;
	struct bpentry *	map;
	
	if (BPMap.size == 0) {
		BPMap.maps = (struct bpentry *)malloc(sizeof(struct bpentry) * 100);
		BPMap.size = 100;
		
		for (i = 0; i < BPMap.size; i++) {
			map = &BPMap.maps[i];
			map->remote = map->local = -1;
		}
	}
	
	if (BPMap.nels == BPMap.size) {
		i = BPMap.size;
		BPMap.size *= 2;
		BPMap.maps = (struct bpentry *)realloc(BPMap.maps, sizeof(struct bpentry) * BPMap.size);
		
		for (; i < BPMap.size; i++) {
			map = &BPMap.maps[i];
			map->remote = map->local = -1;
		}
	}
	
	for (i = 0; i < BPMap.size; i++) {
		map = &BPMap.maps[i];
		if (map->remote == -1) {
			map->remote = remote;
			map->local = local;
			BPMap.nels++;
		}
	}
}

static void
RemoveBPMap(int remote)
{
	int				i;
	struct bpentry *	map;
	
	for (i = 0; i < BPMap.nels; i++) {
		map = &BPMap.maps[i];
		if (map->remote == remote) {
			map->remote = -1;
			map->local = -1;
			BPMap.nels--;
			return;
		}
	}		
}

static int
LocalToRemoteBP(int local) 
{
	int				i;
	struct bpentry *	map;
	
	for (i = 0; i < BPMap.nels; i++) {
		map = &BPMap.maps[i];
		if (map->local == local) {
			return map->remote;
		}
	}

	return -1;
}

static int
RemoteToLocalBP(int remote) 
{
	int				i;
	struct bpentry *	map;
	
	for (i = 0; i < BPMap.nels; i++) {
		map = &BPMap.maps[i];
		if (map->remote == remote) {
			return map->local;
		}
	}

	return -1;
}

/*
 * Wait for terminated children
 */
static void
Reap(int sig)
{
	int	status;
	printf("waiting on child...\n");
	wait(&status);
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
	mi_stop *	stop = (	mi_stop *)data;

	switch ( stop->reason )
	{
	case sr_bkpt_hit:
		e = NewDbgEvent(DBGEV_BPHIT);
		e->bpid = LocalToRemoteBP(stop->bkptno);
		break;

	case sr_end_stepping_range:
		if (get_current_frame(&frame) < 0) {
			ERROR_TO_EVENT(e);
		} else {
			e = NewDbgEvent(DBGEV_STEP);
			e->frame = frame;
		}
		break;

	case sr_exited_signalled:
		e = NewDbgEvent(DBGEV_SIGNAL);
		e->sig_name = strdup(stop->signal_name);
		e->sig_meaning = strdup(stop->signal_meaning);
		e->thread_id = stop->thread_id;
		e->frame = NULL;
		break;
		
	case sr_signal_received:
		if (get_current_frame(&frame) < 0) {
			ERROR_TO_EVENT(e);
		} else {
			e = NewDbgEvent(DBGEV_SIGNAL);
			e->sig_name = strdup(stop->signal_name);
			e->sig_meaning = strdup(stop->signal_meaning);
			e->thread_id = stop->thread_id;
			e->frame = frame;
		}
		break;

	case sr_exited:
		e = NewDbgEvent(DBGEV_EXIT);
		e->exit_status = stop->exit_code;
		break;

	case sr_exited_normally:
		e = NewDbgEvent(DBGEV_EXIT);
		e->exit_status = 0;
		break;

	default:
		DbgSetError(DBGERR_DEBUGGER, "Unknown reason for stopping");
		return DBGRES_ERR;
	}

	mi_free_stop(stop);

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
AsyncCallback(mi_output *mio, void *data)
{
	mi_stop *	stop;

	stop = mi_get_stopped(mio->c);

	if ( stop == NULL )
		return;

	AsyncFunc = AsyncStop;
	AsyncFuncData = (void *)stop;
}


/*
 * Initialize GDB
 */
static int
GDBMIInit(void (*event_callback)(dbg_event *, void *), void *data)
{
	EventCallback = event_callback;
	EventCallbackData = data;
	MIHandle = NULL;
	LastEvent = NULL;
	ServerExit = 0;
		
	signal(SIGCHLD, Reap);
	signal(SIGTERM, SIG_IGN);
	signal(SIGHUP, SIG_IGN);
	signal(SIGINT, SIG_IGN);
	signal(SIGPIPE, SIG_IGN);

	return DBGRES_OK;
}

#ifdef DEBUG
void
to_gdb_cb(const char *str, void *data)
{
	printf(">>> %s\n", str);
}
void
from_gdb_cb(const char *str, void *data)
{
	printf("<<< %s\n", str);
}
#endif /* DEBUG */

int timeout_cb(void *data)
{
	return 0;
}

/*
 * Start GDB session
 */	
static int
GDBMIStartSession(char *gdb_path, char *dir, char *prog, char *args, char **env)
{
	char *		p;
	char **		e;
	struct stat	st;
	
	if (MIHandle != NULL) {
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
	} else {
		p = mi_search_in_path(gdb_path);
		if (p == NULL) {
	 		DbgSetError(DBGERR_NOBACKEND, gdb_path);
	 		return DBGRES_ERR;
	 	}
	 	gdb_path = p;
	}
	
	mi_set_gdb_exe(gdb_path);
	
	if (*dir != '\0' && chdir(dir) < 0) {
		DbgSetError(DBGERR_SYSTEM, strerror(errno));
		return DBGRES_ERR;
	}
	
	ResetError();
	
	if ((MIHandle = mi_connect_local()) == NULL) {
		DbgSetError(DBGERR_DEBUGGER, GetLastErrorStr());
		return DBGRES_ERR;
	}

	for (e = env; e != NULL && *e != NULL; e++) {
		gmi_gdb_set(MIHandle, "environment", *e);
	}
	
	mi_set_async_cb(MIHandle, AsyncCallback, NULL);
	
#ifdef DEBUG
	mi_set_to_gdb_cb(MIHandle, to_gdb_cb, NULL);
	mi_set_from_gdb_cb(MIHandle, from_gdb_cb, NULL);
#endif /* DEBUG */

	ResetError();
	if ( !gmi_set_exec(MIHandle, prog, args) ||
		!gmi_gdb_set(MIHandle, "confirm", "off") )
	{
		DbgSetError(DBGERR_DEBUGGER, GetLastErrorStr());
		gmi_gdb_exit(MIHandle);
		MIHandle = NULL;
		return DBGRES_ERR;
	}
	
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
 * 			0	completed operation
 */
static int	
GDBMIProgress(void)
{
	fd_set			fds;
	int				res = 0;
	struct timeval	tv;
	mi_output		*o;

	/*
	 * Check for existing events
	 */
	if (LastEvent != NULL) {
		if (EventCallback != NULL) {
			EventCallback(LastEvent, EventCallbackData);
		}
			
		if (ServerExit && LastEvent->event == DBGEV_OK) {
			if (MIHandle != NULL) {
				mi_disconnect(MIHandle);
				MIHandle = NULL;
			}
			res = -1;
		}
			
		FreeDbgEvent(LastEvent);
		LastEvent = NULL;
		
		return res;
	}
	
	if (MIHandle == NULL)
		return 0;
	/*
	FD_ZERO(&fds);
	FD_SET(MIHandle->from_gdb[0], &fds);
	tv = SELECT_TIMEOUT;

	for ( ;; ) {
		res = select(MIHandle->from_gdb[0]+1, &fds, NULL, NULL, &tv);
	
		switch (res) {
		case INVALID_SOCKET:
			if ( errno == EINTR )
				continue;
		
			mi_disconnect(MIHandle);
			MIHandle = NULL;
		
			DbgSetError(DBGERR_SYSTEM, strerror(errno));
			return 0;
		
		case 0:
			return 0;
		
		default:
			break;
		}
	
		break;
	}*/
	
	
	mi_set_time_out(MIHandle, 0, 100000);
	mi_set_time_out_cb(MIHandle, timeout_cb, NULL);

	o = mi_get_response_blk(MIHandle);

	mi_set_time_out(MIHandle, MI_DEFAULT_TIME_OUT, 0);
	mi_set_time_out_cb(MIHandle, NULL, NULL);

	if (o == NULL) {
		if (mi_error == MI_GDB_TIME_OUT)
			mi_error = MI_OK;
		return 0;
	}

/*
	ResetError();
	while ( (res = mi_get_response(MIHandle)) <= 0 ) {
		if ( res < 0 ) {
			DbgSetError(DBGERR_DEBUGGER, GetLastErrorStr());
			mi_disconnect(MIHandle);
			MIHandle = NULL;
		}
	}
	*/
	
	/*
	 * Do any extra async functions. We can call gdbmi safely here
	 */
	if (AsyncFunc != NULL) {
		AsyncFunc(AsyncFuncData);
		AsyncFunc = NULL;
	}

	return 0;
}

/*
** Set breakpoint at specified line.
*/
static int
GDBMISetLineBreakpoint(int bpid, char *file, int line)
{
	char *where;

	CHECK_SESSION()

	if ( file == NULL || *file == '\0' )
		asprintf(&where, "%d", line);
	else
		asprintf(&where, "%s:%d", file, line);

	return SetAndCheckBreak(bpid, where);
}

/*
** Set breakpoint at start of specified function.
*/
static int
GDBMISetFuncBreakpoint(int bpid, char *file, char *func)
{
	char *where;

	CHECK_SESSION()

	if ( file == NULL || *file == '\0' )
		asprintf(&where, "%s", func);
	else
		asprintf(&where, "%s:%s", file, func);

	return SetAndCheckBreak(bpid, where);
}

/*
** Check that breakpoint command has succeded and
** extract appropriate information. Returns breakpoint
** id in bid. Adds to breakpoint list if necessary.
*/
static int
SetAndCheckBreak(int bpid, char *where)
{
	dbg_event *	e;
	mi_bkpt *	bpt;
	breakpoint *	bp;

	ResetError();

	bpt = gmi_break_insert_full(MIHandle, 0, 0, NULL, -1, -1, where);

	free(where);
	
	if ( bpt == NULL ) {
		if (mi_error == MI_OK)
			DbgSetError(DBGERR_DEBUGGER, "Attempt to set breakpoint failed");
		else
			DbgSetError(DBGERR_DEBUGGER, GetLastErrorStr());
		return DBGRES_ERR;
	}

	AddBPMap(bpt->number, bpid);
	
	bp = NewBreakpoint(bpt->number);

	bp->ignore = bpt->ignore;

	switch ( bpt->type ) {
	case t_unknown:
		bp->type = strdup("unknown");
		break;

	case t_breakpoint:
		bp->type = strdup("breakpoint");
		break;

	case t_hw:
		bp->type = strdup("hw");
		break;
	}

	bp->hits = bpt->times;

	if ( bpt->file != NULL )
		bp->loc.file = strdup(bpt->file);
	if ( bpt->func != NULL )
		bp->loc.func = strdup(bpt->func);
	if ( bpt->addr != 0 )
		asprintf(&bp->loc.addr, "%p", bpt->addr);
	bp->loc.line = bpt->line;
	
	e = NewDbgEvent(DBGEV_BPSET);
	e->bpid = bpid;
	e->bp = bp;
	SaveEvent(e);
	
	mi_free_bkpt(bpt);

	return DBGRES_OK;
}

/*
** Delete a breakpoint.
*/
static int
GDBMIDeleteBreakpoint(int bpid)
{
	int		lbpid;
	char *	bpstr;
	
	CHECK_SESSION()

	ResetError();

	if ((lbpid = RemoteToLocalBP(bpid)) < 0) {
		asprintf(&bpstr, "%d", bpid);
		DbgSetError(DBGERR_NOBP, bpstr);
		free(bpstr);
		return DBGRES_ERR;
	}
	
	if ( !gmi_break_delete(MIHandle, lbpid) ) {
		DbgSetError(DBGERR_DEBUGGER, GetLastErrorStr());
		return DBGRES_ERR;
	}

	RemoveBPMap(bpid);

	SaveEvent(NewDbgEvent(DBGEV_OK));

	return DBGRES_OK;
}

/*
** Start/continue executing program. 
*/
static int
GDBMIGo(void)
{
	int res;
	
	CHECK_SESSION()

	ResetError();

	if (Started)
		res = gmi_exec_continue(MIHandle);
	else {
		res = gmi_exec_run(MIHandle);
		Started = 1;
	}
		
	if ( !res )
	{
		DbgSetError(DBGERR_DEBUGGER, GetLastErrorStr());
		return DBGRES_ERR;
	}

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
	int		res = -1;

	CHECK_SESSION()

	ResetError();

	switch ( type ) {
	case 0:
		res = gmi_exec_step_cnt(MIHandle, count);
		break;
		
	case 1:
		res = gmi_exec_next_cnt(MIHandle, count);
		break;
		
	case 2:
		res = gmi_exec_finish(MIHandle);
		break;
		
	default:
		DbgSetError(DBGERR_DEBUGGER, "Unknown step type");
		return DBGRES_ERR;
	}

	if ( !res )
	{
		DbgSetError(DBGERR_DEBUGGER, GetLastErrorStr());
		return DBGRES_ERR;
	}

	return DBGRES_OK;
}

/*
** Terminate program execution.
*/
static int
GDBMITerminate(void)
{
	CHECK_SESSION()

	ResetError();

	if (!gmi_exec_kill(MIHandle))
	{
		DbgSetError(DBGERR_DEBUGGER, GetLastErrorStr());
		return DBGRES_ERR;
	}

	SaveEvent(NewDbgEvent(DBGEV_OK));
	
	return DBGRES_OK;
}

/*
** Interrupt an executing program.
*/
static int
GDBMIInterrupt(void)
{
	CHECK_SESSION()

	ResetError();

	/*
	 * Don't do anything if there's an event pending or the
	 * target is not running.
	 */
	if (LastEvent != NULL || !gmi_exec_interrupt(MIHandle))
		return DBGRES_OK;
		
	/*
	 * Must check async here due to broken MI implementation. AsyncCallback will
	 * be called inside gmi_exec_interrupt().
	 */
	if (AsyncFunc != NULL) {
		AsyncFunc(AsyncFuncData);
		AsyncFunc = NULL;
	}
	
	return DBGRES_OK;
}

/*
** Move up or down count stack frames.
*/
static int
GDBMISetCurrentStackframe(int level)
{
	CHECK_SESSION()

	ResetError();

	if (!gmi_stack_select_frame(MIHandle, level))
	{
		DbgSetError(DBGERR_DEBUGGER, GetLastErrorStr());
		return DBGRES_ERR;
	}

	SaveEvent(NewDbgEvent(DBGEV_OK));
	
	return DBGRES_OK;
}

static int
GetStackframes(int current, List **flist)
{
	mi_frames *	frames;
	mi_frames *	f;
	stackframe *	s;
	
	if (current)
		frames = gmi_stack_info_frame(MIHandle);
	else
		frames = gmi_stack_list_frames(MIHandle);
		
	if ( frames == NULL )
	{
		DbgSetError(DBGERR_DEBUGGER, "Failed to get stack frames from backend");
		return DBGRES_ERR;
	}
	
	*flist = NewList();

	for (f = frames; f != NULL; f = f->next) {
		s = NewStackframe(f->level);

		if ( f->addr != 0 )
			asprintf(&s->loc.addr, "%p", f->addr);
		if ( f->func != NULL )
			s->loc.func = strdup(f->func);
		if ( f->file != NULL )
			s->loc.file = strdup(f->file);
		s->loc.line = f->line;
		
		AddToList(*flist, (void *)s);
	}

	mi_free_frames(frames);
	
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

	ResetError();

	if (GetStackframes(current, &frames) != DBGRES_OK)
		return DBGRES_ERR;
	
	e = NewDbgEvent(DBGEV_FRAMES);
	e->list = frames;	
	SaveEvent(e);
	
	return DBGRES_OK;
}

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
gmi_dump_binary_value(mi_h *h, char *exp, char *file)
{
	mi_send(h, "dump binary value %s %s\n", file, exp);
	return mi_res_simple_done(h);
}

/*
** Evaluate the expression exp.
*/
static int
GDBMIEvaluateExpression(char *exp)
{
	int			res  = DBGRES_OK;
	char *		type;
	char *		fds = NULL;
	char			tmp[18];
	AIF *		a;
	dbg_event *	e;

	if (GetTypeInfo(exp, &type, &fds) == DBGRES_OK) {
		strcpy(tmp, "/tmp/guard.XXXXXX");
	
		if ( mktemp(tmp) == NULL )
		{
			DbgSetError(DBGERR_DEBUGGER, (char *)strerror(errno));
			return DBGRES_ERR;
		}
	
		ResetError();
	
		if ( !gmi_dump_binary_value(MIHandle, exp, tmp) )
		{
			DbgSetError(DBGERR_DEBUGGER, GetLastErrorStr());
			return DBGRES_ERR;
		}
	
		res = GDBMIBuildAIFVar(exp, fds, tmp, &a);
	
		if (res == DBGRES_OK) {
			e = NewDbgEvent(DBGEV_DATA);
			e->data = a;
			e->type_desc = strdup(type);
			SaveEvent(e);
		}
	
		free(fds);
		(void)unlink(tmp);
	} else {
		mi_gvar * gvar = gmi_var_create(MIHandle, -1, exp);
	
		if ( gvar == NULL || !gmi_var_evaluate_expression(MIHandle, gvar) )
		{
			DbgSetError(DBGERR_DEBUGGER, GetLastErrorStr());
			return DBGRES_ERR;
		}
	
		e = NewDbgEvent(DBGEV_DATA);
		e->data = StringToAIF(gvar->value);
		e->type_desc = strdup(type);
		SaveEvent(e);
	}
	
	free(type);
	
	return res;
}

struct str_type
{
	int     blen;
	int     slen;
	int     end;
	char *  buf;
};
typedef struct str_type *	str_ptr;

#define STRSIZE	100

str_ptr	str_init(void);
void	str_add(str_ptr, char *, ...);
void	str_free(str_ptr);
char *	str_val(str_ptr);
str_ptr	str_dup(char *);

struct simple_type {
	char *	type_c;
	char *	type_fds;
	int	type_len;
};

struct simple_type simple_types[] = {
	{ "char", "c", 0 },
	{ "unsigned char", "c", 0 },
	{ "short", "is%d", sizeof(short) },
	{ "unsigned short", "iu%d", sizeof(unsigned short) },
	{ "int", "is%d", sizeof(int) },
	{ "unsigned int", "iu%d", sizeof(unsigned int) },
	{ "long", "is%d", sizeof(long) },
	{ "unsigned long", "iu%d", sizeof(unsigned long) },
	{ "long long", "is%d", sizeof(long long) },
	{ "unsigned long long", "iu%d", sizeof(unsigned long long) },
	{ "float", "f%d", sizeof(float) },
	{ "double", "f%d", sizeof(double) },
	{ NULL, NULL }
};

str_ptr
str_init(void)
{
	str_ptr s;

	s = (str_ptr)malloc(sizeof(struct str_type));
	s->buf = (char *)malloc(STRSIZE);
	s->blen = STRSIZE;
	s->slen = 0;
	s->buf[0] = '\0';

	return s;
}

void
str_add(str_ptr s1, char *s2, ...)
{
	va_list	ap;
	int     l2;
	char *	buf;

	va_start(ap, s2);
	vasprintf(&buf, s2, ap);
	va_end(ap);

	l2 = strlen(buf);

	if (s1->slen + l2 >= s1->blen)
	{
		s1->blen += MAX(STRSIZE, l2);
		s1->buf = (char *) realloc (s1->buf, s1->blen);
	}

	memcpy(&(s1->buf[s1->slen]), buf, l2);
	s1->slen += l2;
	s1->buf[s1->slen] = '\0';

	free(buf);
}

void
str_free(str_ptr s)
{
	free(s->buf);
	free(s);
}

char *
str_val(str_ptr s)
{
	return s->buf;
}

str_ptr
str_dup(char *s1)
{
	str_ptr s = str_init();
	str_add(s, s1);
	return s;
}

int
SimpleTypeToFDS(char *type, str_ptr fds)
{
	char *				p;
	char *				last = &type[strlen(type) - 1];
	struct simple_type *	s;

	if ( strcmp(type, "<text variable, no debug info>") == 0 )
		return -1;

	switch ( *last )
	{
	case '*': /* pointer */
		str_add(fds, "^");

		/*
		** get rid of '*'
		*/
		for ( p = last ; p != type && *(p-1) == ' ' ; p-- )
			;

		*p = '\0';
		break;

	case ')': /* function */
		str_add(fds, "&");

		/*
		** get rid of '(..)' for now
		*/
		if ( (p = strrchr(type, '(')) != NULL )
		{
			for ( ; p != type && *(p-1) == ' ' ; p-- )
				;

			*p = '\0';
		}

		str_add(fds, "/");
		break;

	}

	for (s = simple_types ; s->type_c != NULL ; s++ )
	{
		if ( strcmp(type, s->type_c) == 0 )
		{
			str_add(fds, s->type_fds, s->type_len);
			break;
		}
	}

	return 0;
}

static int
ConvertType(mi_gvar *gvar, str_ptr fds)
{
	int		i;
	int		num;
	char *	s;
	
	ResetError();

	if ( gvar->numchild == 0 )
	{
		if ( !gmi_var_info_type(MIHandle, gvar) )
		{
			DbgSetError(DBGERR_DEBUGGER, GetLastErrorStr());
			return DBGRES_ERR;
		}

		if ( SimpleTypeToFDS(gvar->type, fds) < 0 )
		{
			DbgSetError(DBGERR_NOSYMS, "");
			return DBGRES_ERR;
		}
	}
	else
	{
		if ( !gmi_var_list_children(MIHandle, gvar) )
		{
			DbgSetError(DBGERR_DEBUGGER, GetLastErrorStr());
			return DBGRES_ERR;
		}

		switch ( gvar->type[strlen(gvar->type) - 1] )
		{
		case ']': /* array */
			str_add(fds, "[r0..%dis4]", gvar->numchild-1);

			/*
			** Just look at first child to determine type
			*/
			if ( ConvertType(gvar->child, fds) != DBGRES_OK ) {
				return DBGRES_ERR;
			}

			break;

		case '}': /* struct */
			str_add(fds, "{|");
			num = gvar->numchild;
			gvar = gvar->child;
			for ( i = 0 ; i < num ; i++ )
			{
				if (i > 0)
					str_add(fds, ",");
				if ((s = strrchr(gvar->name, '.')) != NULL)
					str_add(fds, "%s=", ++s);
				if ( ConvertType(gvar, fds) != DBGRES_OK ) {
					return DBGRES_ERR;
				}
				gvar = gvar->next;
			}
			str_add(fds, ";;;}");
			break;

		case '*': /* pointer */
			str_add(fds, "^");
			if (strncmp(gvar->type, "struct", 6) == 0) {
				str_add(fds, "{|");
				num = gvar->numchild;
				gvar = gvar->child;
				for ( i = 0 ; i < num ; i++ )
				{
					if (i > 0)
						str_add(fds, ",");
					if ((s = strrchr(gvar->name, '.')) != NULL)
						str_add(fds, "%s=", ++s);
					if ( ConvertType(gvar, fds) != DBGRES_OK ) {
						return DBGRES_ERR;
					}
					gvar = gvar->next;
				}
				str_add(fds, ";;;}");
			} else {
				gvar = gvar->child;
				if ( ConvertType(gvar, fds) != DBGRES_OK ) {
					return DBGRES_ERR;
				}
			}
			break;
						
		default:
			DbgSetError(DBGERR_DEBUGGER, "type not supported (yet)");
			return DBGRES_ERR;
		}
		
	}

	return DBGRES_OK;
}

/*
** Find native type of variable.
*/
static int
GDBMIGetNativeType(char *var)
{
	dbg_event *	e;
	char *		type;
	char *		fds;

	CHECK_SESSION()

	if (GetTypeInfo(var, &type, &fds) != DBGRES_OK)
		return DBGRES_ERR;
		
	e = NewDbgEvent(DBGEV_TYPE);
	e->type_desc = type;

	SaveEvent(e);

	free(fds);

	return DBGRES_OK;
}

/*
** Find AIF type of variable.
*/
static int
GDBMIGetAIFType(char *var)
{
	dbg_event *	e;
	char *		type;
	char *		fds;

	CHECK_SESSION()

	if (GetTypeInfo(var, &type, &fds) != DBGRES_OK)
		return DBGRES_ERR;
		
	e = NewDbgEvent(DBGEV_TYPE);
	e->type_desc = fds;

	SaveEvent(e);

	free(type);

	return DBGRES_OK;
}

static int
GetTypeInfo(char *var, char **type, char **fds_type)
{
	mi_gvar *	gvar;
	str_ptr		fds;

	ResetError();

	gvar = gmi_var_create(MIHandle, -1, var);

	if ( gvar == NULL )
	{
		DbgSetError(DBGERR_DEBUGGER, GetLastErrorStr());
		return DBGRES_ERR;
	}
	
	if ( !gmi_var_info_type(MIHandle, gvar) )
	{
		DbgSetError(DBGERR_DEBUGGER, GetLastErrorStr());
		return DBGRES_ERR;
	}
	
	*type = strdup(gvar->type);

	fds = str_init();

	if ( ConvertType(gvar, fds) != DBGRES_OK ) {
		str_free(fds);
		return DBGRES_ERR;
	}

	*fds_type = strdup(str_val(fds));
	str_free(fds);

	mi_free_gvar(gvar);

	return DBGRES_OK;
}

/*
** List local variables.
*/
static int
GDBMIGetLocalVariables(void)
{
	dbg_event *	e;
	mi_results *	c;
	mi_results *	r;
	mi_results *	res;

	CHECK_SESSION()

	ResetError();

	res = gmi_stack_list_locals(MIHandle, 0);

	if ( res == NULL )
	{
		DbgSetError(DBGERR_DEBUGGER, GetLastErrorStr());
		return DBGRES_ERR;
	}

	e = NewDbgEvent(DBGEV_VARS);
	e->list = NewList();

	/*
	 * __APPLE__ returns a tuple of tuples: locals={{name="a"},{name="b"}}
	 * Linux returns a list: locals=[name="a",name="b"]
	 * 
	 */
	
	/*
	 * Get value of list or tuple
	 */
	r = res->v.rs;

	while ( r != NULL )
	{
		if (r->type == t_tuple)
			c = r->v.rs;
		else
			c = r;

		if ( c->type == t_const && strcmp(c->var, "name") == 0 ) 
		{
			AddToList(e->list, strdup(c->v.cstr));
		}
		
		r = r->next;
	}

	mi_free_results(res);

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
	mi_results *	c;
	mi_results *	r;
	mi_frames *	frames;

	CHECK_SESSION()

	ResetError();

	frames = gmi_stack_list_arguments_r(MIHandle, 0, level, level);

	if ( frames == NULL )
	{
		DbgSetError(DBGERR_DEBUGGER, GetLastErrorStr());
		return DBGRES_ERR;
	}

	e = NewDbgEvent(DBGEV_ARGS);
	e->list = NewList();

	/*
	 * __APPLE__ returns a tuple of tuples: args={{name="argc"},{name="argv"}}
	 * Linux returns a list: args=[name="argc",name="argv"]
	 * 
	 */
	 
	r = frames->args;

	while ( r != NULL )
	{
		if (r->type == t_tuple)
			c = r->v.rs;
		else
			c = r;
		
		if ( c->type == t_const && strcmp(c->var, "name") == 0 ) 
		{
			AddToList(e->list, strdup(c->v.cstr));
		}
		
		r = r->next;
	}

	mi_free_frames(frames);

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
	if (MIHandle != NULL)
		gmi_gdb_exit(MIHandle);
		
	SaveEvent(NewDbgEvent(DBGEV_OK));
	ServerExit++;
	
	return DBGRES_OK;
}

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

	if ( (*aif = AsciiToAIF(type, data)) == NULL )
	{
		DbgSetError(DBGERR_DEBUGGER, AIFErrorStr());
		return DBGRES_ERR;
	}
	
	return DBGRES_OK;
}
