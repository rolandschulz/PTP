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
static struct timeval	SELECT_TIMEOUT = { 0, 1000 };
static mi_h *		MIHandle;
static List *		Breakpoints;
static dbg_event *	LastEvent;
static void			(*EventCallback)(dbg_event *, void *);
static void *		EventCallbackData;
static int			ServerExit;
static int			Started;

static int	GDBMIBuildAIFVar(char *, char *, char *, AIF **);
static int	SetAndCheckBreak(char *);

static int	GDBMIInit(void (*)(dbg_event *, void *), void *);
static int	GDBMIProgress(void);
static int	GDBMIStartSession(char *, char *, char*);
static int	GDBMISetLineBreakpoint(char *, int);
static int	GDBMISetFuncBreakpoint(char *, char *);
static int	GDBMIDeleteBreakpoint(int);
static int	GDBMIGo(void);
static int	GDBMIStep(int, int);
static int	GDBMITerminate(void);
static int	GDBMIListStackframes(int);
static int	GDBMISetCurrentStackframe(int);
static int	GDBMIEvaluateExpression(char *);
static int	GDBMIGetType(char *);
static int	GDBMIGetLocalVariables(void);
static int	GDBMIGetArguments(void);
static int	GDBMIGetGlobalVariables(void);
static int	GDBMIQuit(void);

dbg_backend_funcs	GDBMIBackend =
{
	GDBMIInit,
	GDBMIProgress,
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
	GDBMIGetType,
	GDBMIGetLocalVariables,
	GDBMIGetArguments,
	GDBMIGetGlobalVariables,
	GDBMIQuit
};

#define CHECK_SESSION() \
	if (MIHandle == NULL) { \
		DbgSetError(DBGERR_NOSESSION, NULL); \
		return DBGRES_ERR; \
	}
	
char *
GetLastErrorStr(void)
{
	if (mi_error == MI_FROM_GDB && mi_error_from_gdb != NULL)
 		return mi_error_from_gdb;
	return (char *)mi_get_error_str();
}

void
ResetError(void)
{
	mi_error=MI_OK;
}

void
SaveEvent(dbg_event *e)
{
	if (LastEvent != NULL)
		FreeEvent(LastEvent);
		
	LastEvent = e;
}

/*
 * Wait for terminated children
 */
void
Reap(int sig)
{
	int	status;
	printf("waiting on child...\n");
	wait(&status);
}

/*
** AsyncCallback is called by mi_get_response() when an async response is
** detected. It can't issue any gdb commands or there's a potential
** for deadlock. If commands need to be issues (e.g. to obtain
** current stack frame, they must be called from the main select
** loop using the AsyncCheck() mechanism. 
*/
static void
AsyncCallback(mi_output *mio, void *data)
{
	mi_stop *	stop;
	dbg_event *	e;
	breakpoint *	bp;

	stop = mi_get_stopped(mio->c);

	if ( !stop )
		return;

	switch ( stop->reason )
	{
	case sr_bkpt_hit:
		if ((bp = FindBreakpoint(Breakpoints, stop->bkptno)) == NULL)
		{
			DbgSetError(DBGERR_DEBUGGER, "bad breakpoint");
			return;
		}
	
		e = NewEvent(DBGEV_BPHIT);
		e->bp = CopyBreakpoint(bp);
		break;

	case sr_end_stepping_range:
		e = NewEvent(DBGEV_STEP);
		break;

	case sr_exited_signalled:
	case sr_signal_received:
		e = NewEvent(DBGEV_SIGNAL);
		e->sig_name = strdup(stop->signal_name);
		e->sig_meaning = strdup(stop->signal_meaning);
		e->thread_id = stop->thread_id;
		break;

	case sr_exited:
		e = NewEvent(DBGEV_EXIT);
		e->exit_status = stop->exit_code;
		break;

	case sr_exited_normally:
		e = NewEvent(DBGEV_EXIT);
		e->exit_status = 0;
		break;

	default:
		break;
	}

	mi_free_stop(stop);
	
	if (EventCallback != NULL)
		EventCallback(e, EventCallbackData);
		
	FreeEvent(e);
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

	Breakpoints = NewList();
	
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

/*
 * Start GDB session
 */	
static int
GDBMIStartSession(char *gdb_path, char *prog, char *args)
{
	char *		p;
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
	
	ResetError();
	
	if ((MIHandle = mi_connect_local()) == NULL) {
		DbgSetError(DBGERR_DEBUGGER, GetLastErrorStr());
		return DBGRES_ERR;
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
	SaveEvent(NewEvent(DBGEV_OK));

	return DBGRES_OK;
}

/*
 * Progress gdb commands.
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

	/*
	 * Check for existing events
	 */
	if (LastEvent != NULL) {
		if (EventCallback != NULL)
			EventCallback(LastEvent, EventCallbackData);
		
		if (ServerExit && LastEvent->event == DBGEV_OK) {
			if (MIHandle != NULL) {
				mi_disconnect(MIHandle);
				MIHandle = NULL;
			}
			res = -1;
		}
			
		FreeEvent(LastEvent);
		LastEvent = NULL;
		
		return res;
	}
	
	if (MIHandle == NULL)
		return 0;
	
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
	}
	
	ResetError();
	if ( mi_get_response(MIHandle) < 0 ) {
		DbgSetError(DBGERR_DEBUGGER, GetLastErrorStr());
		mi_disconnect(MIHandle);
		MIHandle = NULL;
	}

	return 0;
}

/*
** Set breakpoint at specified line.
*/
static int
GDBMISetLineBreakpoint(char *file, int line)
{
	char *where;

	CHECK_SESSION()

	if ( file == NULL || *file == '\0' )
		asprintf(&where, "%d", line);
	else
		asprintf(&where, "%s:%d", file, line);

	return SetAndCheckBreak(where);
}

/*
** Set breakpoint at start of specified function.
*/
static int
GDBMISetFuncBreakpoint(char *file, char *func)
{
	char *where;

	CHECK_SESSION()

	if ( file == NULL || *file == '\0' )
		asprintf(&where, "%s", func);
	else
		asprintf(&where, "%s:%s", file, func);

	return SetAndCheckBreak(where);
}

/*
** Check that breakpoint command has succeded and
** extract appropriate information. Returns breakpoint
** id in bid. Adds to breakpoint list if necessary.
*/
static int
SetAndCheckBreak(char *where)
{
	breakpoint *	bp;
	dbg_event *	e;
	mi_bkpt *	bpt;

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
		asprintf(&bp->loc.addr, "0x%p", bpt->addr);
	bp->loc.line = bpt->line;

	/*
	** Link a copy of the breakpoint onto BP.
	*/
	AddBreakpoint(Breakpoints, bp);

	/*
	** Now create a fake event and make it
	** look like a BPSET.
	*/

	e = NewEvent(DBGEV_BPSET);
	e->bp = CopyBreakpoint(bp);
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
	CHECK_SESSION()

	ResetError();

	if ( !gmi_break_delete(MIHandle, bpid) ) {
		DbgSetError(DBGERR_DEBUGGER, GetLastErrorStr());
		return DBGRES_ERR;
	}

	RemoveBreakpoint(Breakpoints, bpid);

	SaveEvent(NewEvent(DBGEV_OK));

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
** Execute count statements. If type == 0, do not enter
** function calls.
*/
static int
GDBMIStep(int count, int type)
{
	int		res;

	CHECK_SESSION()

	ResetError();

	if ( type == 0 )
		res = gmi_exec_next_cnt(MIHandle, count);
	else
		res = gmi_exec_step_cnt(MIHandle, count);

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

	SaveEvent(NewEvent(DBGEV_OK));
	
	return DBGRES_OK;
}

/*
** List current or all stack frames.
*/
static int
GDBMIListStackframes(int current)
{
	dbg_event *	e;
	List *		flist;
	stackframe *	s;
	mi_frames *	f;
	mi_frames *	frames;
	
	CHECK_SESSION()

	ResetError();

	if (current)
		frames = gmi_stack_info_frame(MIHandle);
	else
		frames = gmi_stack_list_frames(MIHandle);
		
	if ( frames == NULL )
	{
		DbgSetError(DBGERR_DEBUGGER, GetLastErrorStr());
		return DBGRES_ERR;
	}
	
	flist = NewList();
	
	for (f = frames; f != NULL; f = f->next) {
		s = NewStackframe(f->level);

		if ( f->addr != 0 )
			asprintf(&s->loc.addr, "0x%p", f->addr);
		if ( f->func != NULL )
			s->loc.func = strdup(f->func);
		if ( f->file != NULL )
			s->loc.file = strdup(f->file);
		s->loc.line = f->line;
		
		AddToList(flist, (void *)s);
	}

	mi_free_frames(frames);
	
	e = NewEvent(DBGEV_FRAMES);
	e->list = flist;	
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
	int			res;
	char *		type;
	char			tmp[18];
	AIF *		a;
	dbg_event *	e;

	if (GDBMIGetType(exp) != DBGRES_OK)
		return DBGRES_ERR;
			
	type = strdup(LastEvent->type_desc);

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

	res = GDBMIBuildAIFVar(exp, type, tmp, &a);
	
	if (res == DBGRES_OK) {
		e = NewEvent(DBGEV_DATA);
		e->data = a;
		SaveEvent(e);
	}

	(void)free(type);
	(void)unlink(tmp);
	
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
	*(s->buf) = '\0';

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
#if 0
		case '}': /* struct */
			str_add(fds, "{|");
			num = gvar->numchild;
			gvar = gvar->child;
			for ( i = 0 ; i < num ; i++ )
			{
				if ( ConvertType(gvar, fds) != DBGRES_OK ) {
					return DBGRES_ERR;
				}
				gvar = gvar->next;
			}
			add_to_str(fds, ";;;}");
			break;
#endif
		default:
			DbgSetError(DBGERR_DEBUGGER, "type not supported (yet)");
			return DBGRES_ERR;
		}
		
	}

	return DBGRES_OK;
}

/*
** Find type of variable.
*/
static int
GDBMIGetType(char *var)
{
	dbg_event *	e;
	mi_gvar *	gvar;
	str_ptr		fds;

	CHECK_SESSION()

	ResetError();

	gvar = gmi_var_create(MIHandle, -1, var);

	if ( gvar == NULL )
	{
		DbgSetError(DBGERR_DEBUGGER, GetLastErrorStr());
		return DBGRES_ERR;
	}

	fds = str_init();

	if ( ConvertType(gvar, fds) != DBGRES_OK ) {
		return DBGRES_ERR;
	}

	e = NewEvent(DBGEV_TYPE);
	e->type_desc = strdup(fds->buf);
	SaveEvent(e);
	
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
	mi_results *	res;

	CHECK_SESSION()

	ResetError();

	res = gmi_stack_list_locals(MIHandle, 0);

	if ( res == NULL )
	{
		DbgSetError(DBGERR_DEBUGGER, GetLastErrorStr());
		return DBGRES_ERR;
	}

	e = NewEvent(DBGEV_VARS);
	e->list = NewList();

	c = res;

	while ( c != NULL )
	{
		if ( c->type == t_const && strcmp(c->var, "name") == 0 ) 
		{
			AddToList(e->list, strdup(c->var));
		}
		c = c->next;
	}

	mi_free_results(res);

	SaveEvent(e);
	
	return DBGRES_OK;
}

/*
** List arguments.
*/
static int
GDBMIGetArguments(void)
{
	CHECK_SESSION()

	DbgSetError(DBGERR_NOTIMP, NULL);
	return DBGRES_ERR;
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
		
	SaveEvent(NewEvent(DBGEV_OK));
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
