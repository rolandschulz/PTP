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

#include "dbg.h"
#include "dbg_event.h"
#include "backend.h"

extern int	Shutdown;

static int	GDBMIBuildAIFVar(char *, char *, char *, dbg_event **);
static int	SetAndCheckBreak(char *, dbg_event **);

static int	GDBMIInit(int *, int *, dbg_event **);
static int	GDBMIRead(int);
static int	GDBMISetLineBreakpoint(char *, int, dbg_event **);
static int	GDBMISetFuncBreakpoint(char *, char *, dbg_event **);
static int	GDBMIDeleteBreakpoints(int, dbg_event **);
static int	GDBMIGo(char *, int, dbg_event **);
static int	GDBMIStep(int, int, char *, int, dbg_event **);
static int	GDBMIListStackframes(dbg_event **);
static int	GDBMISetCurrentStackframe(int, int, dbg_event **);
static int	GDBMIEvaluateExpression(char *, dbg_event **);
static int	GDBMIGetLocalVariables(char *, dbg_event **);
static int	GDBMIGetArguments(char *, dbg_event **);
static int	GDBMIGetGlobalVariables(char *, dbg_event **);
static int	GDBMIQuit(dbg_event **);

dbg_backend_funcs	GDBMIBackend =
{
	GDBMIInit,
	GDBMIRead,
	GDBMISetLineBreakpoint,
	GDBMISetFuncBreakpoint,
	GDBMIDeleteBreakpoints,
	GDBMIGo,
	GDBMIStep,
	GDBMIListStackframes,
	GDBMISetCurrentStackframe,
	GDBMIEvaluateExpression,
	GDBMIGetLocalVariables,
	GDBMIGetArguments,
	GDBMIGetGlobalVariables,
	GDBMIQuit
};

/*
** Look up current frame.
*/
static int
SetCurrFrame(void)
{
	dbgframe_t *	f;
	mi_frames *	frame;

	if ( (frame = gmi_stack_info_frame(MIHandle)) == NULL )
		return -1;

	f = (dbgframe_t *)Alloc(sizeof(dbgframe_t));
	f->frame_next = (dbgframe_t *)NULL;
	f->frame_loc.loc_addr = NULL;
	f->frame_loc.loc_func = NULL;
	f->frame_loc.loc_file = NULL;

	f->frame_level = frame->level;
	if ( frame->addr != 0 )
		asprintf(&f->frame_loc.loc_addr, "0x%x", frame->addr);
	if ( frame->func != NULL )
		f->frame_loc.loc_func = strdup(frame->func);
	if ( frame->file != NULL )
		f->frame_loc.loc_file = strdup(frame->file);
	f->frame_loc.loc_line = frame->line;

	mi_free_frames(frame);

	if ( CurrFrame != NULL )
		FreeFrame(CurrFrame);

	CurrFrame = f;

	return 0;
}

static void
SetAsync(char *host, int prog)
{
	if ( AsyncHost != NULL )
		Free(AsyncHost);
	
	AsyncHost = strdup(host);
	AsyncProg = prog;
}

dbgevent_t *
AsyncBreakpointHit(void *arg)
{
	dbgevent_t *	e;
	int 		bkpt = (int)arg;

	if ( SetCurrFrame() < 0 )
	{
		EVENT_ERROR(e, DBGERR_DEBUGGER, (char *)mi_get_error_str());
		return e;
	}


	if ( (CurrBP = FindBPByID(BP, bkpt)) == NULL )
	{
		EVENT_ERROR(e, DBGERR_DEBUGGER, "bad breakpoint");
		return e;
	}

	e = NewEvent(DBGEV_BPHIT);
	e->ev_bp = DupBP(CurrBP);

	return e;
}

char *
LookupLine(char *file, int line)
{
	size_t	len;
	char *	lp;
	char *	ret;
	FILE *	fp;

	if ( file == NULL )
		return NULL;

	if ( (fp = fopen(file, "r")) == NULL )
		return NULL;

	while ( line-- > 0 )
		lp = fgetln(fp, &len);

	if ( lp == NULL )
	{
		fclose(fp);
		return NULL;
	}
		
	/*
	** Remove newline if it exists.
	*/
	if ( lp[len-1] == '\n' )
		len--;

	ret = (char *)Alloc(sizeof(char *) * len + 1);	

	memcpy(ret, lp, len);
	ret[len] = '\0';

	fclose(fp);

	return ret;
}

dbgevent_t *
AsyncStep(void *arg)
{
	dbgevent_t *	e;

	if ( SetCurrFrame() < 0 )
	{
		EVENT_ERROR(e, DBGERR_DEBUGGER, (char *)mi_get_error_str());
		return e;
	}

	e = NewEvent(DBGEV_STEP);
	e->ev_step_lno = CurrFrame->frame_loc.loc_line;
	e->ev_step_frame = DupFrame(CurrFrame);
	e->ev_step_line = LookupLine(CurrFrame->frame_loc.loc_file, CurrFrame->frame_loc.loc_line);

	return e;
}

dbgevent_t *
AsyncSignal(void *arg)
{
	dbgevent_t *	e;
	char *		sig = (char *)arg;

	if ( SetCurrFrame() < 0 )
	{
		Free(sig);
		EVENT_ERROR(e, DBGERR_DEBUGGER, (char *)mi_get_error_str());
		return e;
	}

	e = NewEvent(DBGEV_SIGNAL);
	e->ev_sig_type = sig;
	e->ev_sig_frame = DupFrame(CurrFrame);
	e->ev_sig_line = LookupLine(CurrFrame->frame_loc.loc_file, CurrFrame->frame_loc.loc_line);

	return e;
}

dbgevent_t *
AsyncExit(void *arg)
{
	dbgevent_t *	e;
	int		exit_code = (int)arg;

	e = NewEvent(DBGEV_EXIT);
	e->ev_exit = exit_code;

	if ( CurrFrame != NULL )
		FreeFrame(CurrFrame);
	CurrFrame = NULL;

	return e;
}

/*
** AsyncCB is called by mi_get_response() when an async response is
** detected. It can't issue any gdb commands or there's a potential
** for deadlock. If commands need to be issues (e.g. to obtain
** current stack frame, they must be called from the main select
** loop using the AsyncCheck() mechanism. 
*/
static void
AsyncCB(mi_output *mio, void *data)
{
	mi_stop *	stop;

	if ( Status == DBGSTAT_INITIALISING)
		return;
	
	stop = mi_get_stopped(mio->c);

	if ( !stop )
		return;

	switch ( stop->reason )
	{
	case sr_bkpt_hit:
		AsyncCheck(AsyncBreakpointHit, (void *)stop->bkptno, AsyncHost, AsyncProg);
		break;

	case sr_end_stepping_range:
		AsyncCheck(AsyncStep, NULL, AsyncHost, AsyncProg);
		break;

	case sr_exited_signalled:
	case sr_signal_received:
		AsyncCheck(AsyncSignal, (void *)strdup(stop->signal_name), AsyncHost, AsyncProg);
		break;

	case sr_exited:
		AsyncCheck(AsyncExit, (void *)stop->exit_code, AsyncHost, AsyncProg);
		break;

	case sr_exited_normally:
		AsyncCheck(AsyncExit, 0, AsyncHost, AsyncProg);
		break;

	default:
		break;
	}

	mi_free_stop(stop);	

	Status = DBGSTAT_STOPPED;
}

/*
 * Initialize GDB
 */
static int
GDBMIInit(int *rd, int *tty, dbg_event **ev)
{
	dbgevent_t *	e;
	mi_pty *	pty;

	MIHandle = mi_connect_local();

	if ( !MIHandle )
	{
		DbgSetError(DBGERR_DEBUGGER, (char *)mi_get_error_str());
		return DBGRES_ERR;
	}

	mi_set_async_cb(MIHandle, AsyncCB, NULL);

#ifdef DEBUG
	mi_set_to_gdb_cb(MIHandle, ToGDBCB, NULL);
	mi_set_from_gdb_cb(MIHandle, FromGDBCB, NULL);
#endif /* DEBUG */

	pty = gmi_look_for_free_pty();

	if ( !pty || !gmi_target_terminal(MIHandle, pty->slave) )
	{
		fprintf(stderr, "Could not select target terminal\n");
		*tty = -1;
	}
	else
		*tty = pty->master;

	*rd = MIHandle->from_gdb[0];

	signal(SIGCHLD, Reap);
	signal(SIGTERM, GDBMIFinish);
	signal(SIGHUP, GDBMIFinish);
	signal(SIGINT, GDBMIFinish);
	signal(SIGPIPE, GDBMIFinish);

	BP = NewList();

	Status = DBGSTAT_INITIALISING;

	return DBGRES_OK;
}

/*
 * Read response from gdb. Keep reading until a prompt is
 * received (mi_get_response() returns 1).
 */
static int
GDBMIRead(int rd)
{
	int res;

	while ( (res = mi_get_response(MIHandle)) <= 0 )
	{
		if (res < 0)
			return 0;
	}

	return 1;
}

/*
** Set breakpoint at specified line.
*/
static int
GDBMISetLineBreakpoint(char *file, int line, dbg_event **ev)
{
	char *where;

	if ( file == NULL || *file == '\0' )
		asprintf(&where, "%d", line);
	else
		asprintf(&where, "%s:%d", file, line);

	return SetAndCheckBreak(where, ev);
}

/*
** Set breakpoint at start of specified function.
*/
static int
GDBMISetFuncBreakpoint(char *file, char *func, dbg_event **ev)
{
	char *where;

	if ( file == NULL || *file == '\0' )
		asprintf(&where, "%s", func);
	else
		asprintf(&where, "%s:%s", file, func);

	return SetAndCheckBreak(where, ev);
}

/*
** Check that breakpoint command has succeded and
** extract appropriate information. Returns breakpoint
** id in bid. Adds to breakpoint list if necessary.
*/
static int
SetAndCheckBreak(char *where, dbg_event **ev)
{
	breakpoint *	bp;
	dbg_event *	e;
	mi_bkpt *	bpt;

	bpt = gmi_break_insert_full(MIHandle, 0, 0, NULL, -1, -1, where);

	(void)free(where);

	if ( bpt == NULL ) {
		DbgSetError(DBGERR_DEBUGGER, (char *)mi_get_error_str());
		return DBGRES_ERR;
	}

	bp = NewBreakpoint(bpt->number);

	bp->bp_ignore = bpt->ignore;

	switch ( bpt->type ) {
	case t_unknown:
		bp->bp_type = strdup("unknown");
		break;

	case t_breakpoint:
		bp->bp_type = strdup("breakpoint");
		break;

	case t_hw:
		bp->bp_type = strdup("hw");
		break;
	}

	bp->bp_hits = bpt->times;

	if ( bpt->file != NULL )
		bp->bp_loc.loc_file = strdup(bpt->file);
	if ( bpt->func != NULL )
		bp->bp_loc.loc_func = strdup(bpt->func);
	if ( bpt->addr != 0 )
		asprintf(&bp->bp_loc.loc_addr, "0x%x", bpt->addr);
	bp->bp_loc.loc_line = bpt->line;

	bp->bp_stmt = LookupLine(bpt->file, bpt->line);

	/*
	** Link a copy of the breakpoint onto BP.
	*/
	AddToList(Breakpoints, (void *)bp);

	/*
	** Now create a fake event and make it
	** look like a BPSET.
	*/

	e = NewEvent(DBGEV_BPSET);
	e->ev_bp = DupBP(bp);
	*ev = e;
	
	mi_free_bkpt(bpt);

	return DBGRES_OK;
}

/*
** Delete a breakpoint.
*/
static int
GDBMIDeleteBreakpoints(int bid, dbg_event **ev)
{
	if ( !gmi_break_delete(MIHandle, bid) ) {
		DbgSetError(DBGERR_DEBUGGER, (char *)mi_get_error_str());
		return DBGRES_ERR;
	}

	RemoveBP(BP, bid);

	*ev = NewEvent(DBGEV_OK);

	return DBGRES_OK;
}

/*
** Start/continue executing program. 
*/
static int
GDBMIGo(char *host, int prog, dbg_event **ev)
{
	if ( !gmi_exec_continue(MIHandle) )
	{
		EVENT_ERROR(e, DBGERR_DEBUGGER, (char *)mi_get_error_str());
		return e;
	}

	/*
	 * Wait for event...
	 */
	 
	//*ev = NewEvent(DBGEV_WHATEVER);
	
	return DBGRES_OK;
}

/*
** Execute count statements. If in == 0, do not enter
** function calls.
*/
static int
GDBMIStep(int count, int in, char *host, int prog, dbg_event **ev)
{
	int		res;
	dbgevent_t *	e;

	if ( !in )
		res = gmi_exec_next_cnt(MIHandle, count);
	else
		res = gmi_exec_step_cnt(MIHandle, count);

	if ( !res )
	{
		DbgSetError(DBGERR_DEBUGGER, (char *)mi_get_error_str());
		return DBGRES_ERR;
	}

	/*
	 * Wait for event...
	 */
	 
	//*ev = NewEvent(DBGEV_WHATEVER);
	
	return DBGRES_OK;
}

/*
** Move up or down count stack frames.
*/
static int
GDBMISetCurrentStackframe(int count, int up, dbg_event **ev)
{
	int		level;
	dbgevent_t *	e;

	if ( up )
	{
		level = CurrFrameLevel - count;

		if ( level < 0 )
			level = 0;
	}
	else
	{
		level = CurrFrameLevel + count;

		if ( level > LastFrameLevel )
			level = LastFrameLevel;
	}

	if 
	( 
		!gmi_stack_select_frame(MIHandle, level) 
		||
		SetCurrFrame() < 0
	)
	{
		DbgSetError(DBGERR_DEBUGGER, (char *)mi_get_error_str());
		return DBGRES_ERR;
	}

	//*ev = NewEvent(STACKFRAME);
	return DBGRES_OK;
}

/*
** List current stack frames.
*/
static int
GDBMIListStackframes(dbg_event **ev)
{
	dbg_event *	e;

	if ( CurrFrame == NULL )
	{
		DbgSetError(DBGERR_NOSTACK, "");
		return DBGRES_ERR;
	}

	e = NewEvent(DBGEV_FRAME);
	e->ev_frame = DupFrame(CurrFrame);	
	*ev = e;
	
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
GDBMIEvaluateExpression(char *exp, dbg_event **ev)
{
#ifndef GDB_WITH_AIF
	char *		type;
	char		tmp[18];
#else /* GDB_WITH_AIF */
	mi_aif *	aif;
#endif /* GDB_WITH_AIF */
	dbg_event *	e;

#ifdef GDB_WITH_AIF
	aif = gmi_aif_evaluate_expression(MIHandle, exp);

	if ( aif == NULL )
	{
		DbgSetError(DBGERR_DEBUGGER, (char *)mi_get_error_str());
		return DBGRES_ERR;
	}

	e = NewEvent(DBGEV_DATA);
	e->ev_data = AsciiToAIF(aif->fds, aif->data);

	mi_free_aif(aif);
#else /* GDB_WITH_AIF */
	e = DbgGDBMIGetType(exp);

	if ( e->ev_event != DBGEV_TYPE )
		return e;
			
	type = strdup(e->ev_type);

	FreeEvent(e);

	strcpy(tmp, "/tmp/guard.XXXXXX");

	if ( mktemp(tmp) == NULL )
	{
		DbgSetError(DBGERR_DEBUGGER, (char *)strerror(errno));
		return DBGRES_ERR;
	}

	if ( !gmi_dump_binary_value(MIHandle, exp, tmp) )
	{
		DbgSetError(DBGERR_DEBUGGER, (char *)mi_get_error_str());
		return DBGRES_ERR;
	}

	e = GDBMIBuildAIFVar(exp, type, tmp);

	(void)free(type);
	(void)unlink(tmp);
#endif /* GDB_WITH_AIF */

	*ev = e;
	
	return DBGRES_OK;
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

	s = (str_ptr)Alloc(sizeof(struct str_type));
	s->buf = (char *)Alloc(STRSIZE);
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
		s1->buf = (char *) Resize (s1->buf, s1->blen);
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
	char *			p;
	char *			last = &type[strlen(type) - 1];
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

dbgevent_t *
ConvertType(mi_gvar *gvar, str_ptr fds)
{
	dbgevent_t *	e;

	if ( gvar->numchild == 0 )
	{
		if ( !gmi_var_info_type(MIHandle, gvar) )
		{
			EVENT_ERROR(e, DBGERR_DEBUGGER, (char *)mi_get_error_str());
			return e;
		}

		if ( SimpleTypeToFDS(gvar->type, fds) < 0 )
		{
			EVENT_ERROR(e, DBGERR_NOSYMS, "");
			return e;
		}

	}
	else
	{

		if ( !gmi_var_list_children(MIHandle, gvar) )
		{
			EVENT_ERROR(e, DBGERR_DEBUGGER, (char *)mi_get_error_str());
			return e;
		}

		switch ( gvar->type[strlen(gvar->type) - 1] )
		{
		case ']': /* array */
			str_add(fds, "[r0..%dis4]", gvar->numchild-1);

			/*
			** Just look at first child to determine type
			*/
			if ( (e = ConvertType(gvar->child, fds)) != NULL )
				return e;

			break;
#if 0
		case '}': /* struct */
			str_add(fds, "{|");
			num = gvar->numchild;
			gvar = gvar->child;
			for ( i = 0 ; i < num ; i++ )
			{
				if ( (e = ConvertType(gvar, fds)) != NULL )
					return e;
				gvar = gvar->next;
			}
			add_to_str(fds, ";;;}");
			break;
#endif
		default:
			EVENT_ERROR(e, DBGERR_DEBUGGER, "type not supported (yet)");
			return e;
			break;
		}
		
	}

	return NULL;
}

#ifdef notyet
/*
** Find type of variable.
*/
dbgevent_t *
DbgGDBMIGetType(char *var)
{
	dbgevent_t *	e;
#ifdef GDB_WITH_AIF
	char *		type;
#else /* GDB_WITH_AIF */
	mi_gvar *	gvar;
	str_ptr		fds;
#endif /* GDB_WITH_AIF */

	DPRINT(fprintf(stderr, "*** DbgGetType(%s)\n", var));

#ifdef GDB_WITH_AIF
	type = gmi_aif_type(MIHandle);

	if ( type == NULL )
	{
		EVENT_ERROR(e, DBGERR_DEBUGGER, (char *)mi_get_error_str());
		return e;
	}

	e = NewEvent(DBGEV_TYPE);
	e->ev_type = type;
#else /* GDB_WITH_AIF */
	gvar = gmi_var_create(MIHandle, -1, var);

	if ( gvar == NULL )
	{
		EVENT_ERROR(e, DBGERR_NOSYM, var);
		return e;
	}

	fds = str_init();

	if ( (e = ConvertType(gvar, fds)) != NULL )
		return e;

	e = NewEvent(DBGEV_TYPE);
	e->ev_type = strdup(fds->buf);

	str_free(fds);

	mi_free_gvar(gvar);
#endif /* GDB_WITH_AIF */

	return e;
}
#endif

/*
** List local variables.
*/
static int
GDBMIGetLocalVariables(char *file, dbg_event **ev)
{
	dbgevent_t *	e;
	dbgvars_t *	v;
	mi_results *	c;
	mi_results *	res;

	res = gmi_stack_list_locals(MIHandle, 0);

	if ( res == NULL )
	{
		DbgSetError(DBGERR_DEBUGGER, (char *)mi_get_error_str());
		return DBGRES_ERR;
	}

	e = NewEvent(DBGEV_VARS);
	e->ev_vars = (dbgvars_t *)NULL;

	c = res;

	while ( c != NULL )
	{
		if ( c->type == t_const && strcmp(c->var, "name") == 0 ) 
		{
			v = (dbgvars_t *)Alloc(sizeof(dbgvars_t));
			v->var_name = strdup(c->v.cstr);
			v->var_next = e->ev_vars;
			e->ev_vars = v;
		}
		c = c->next;
	}

	mi_free_results(res);

	*ev = e;
	
	return DBGRES_OK;
}

/*
** List arguments.
*/
static int
GDBMIGetArguments(char *file, dbg_event **ev)
{
	DbgSetError(DBGERR_NOTIMP, NULL);
	return DBGRES_ERR;
}

/*
** List global variables.
*/
static int
GDBMIGetGlobalVariables(char *file, dbg_event **ev)
{
	DbgSetError(DBGERR_NOTIMP, NULL);
	return DBGRES_ERR;
}

/*
** Quit debugger.
*/
static int
GDBMIQuit(dbg_event **ev)
{
	gmi_gdb_exit(MIHandle);

	return DBGRES_OK;
}

#ifdef notyet
dbgevent_t *
DbgGDBMIInvoke(char *host, int cb, char *proto, char *prog, char *args, char **env, int arch)
{
	dbgevent_t *	e;
	mi_bkpt *	bpt;
	mi_stop *	sr;

	DPRINT(fprintf(stderr, "*** DbgInvoke\n"));

	if ( !gmi_set_exec(MIHandle, prog, args) )
	{
		EVENT_ERROR(e, DBGERR_DEBUGGER, (char *)mi_get_error_str());
		return e;
	}

	bpt = gmi_break_insert_full(MIHandle, 0, 0, NULL, -1, -1, "main");

	if ( bpt == NULL ) {
		EVENT_ERROR(e, DBGERR_DEBUGGER, (char *)mi_get_error_str());
		return e;
	}

	if ( !gmi_exec_run(MIHandle) )
	{
		EVENT_ERROR(e, DBGERR_DEBUGGER, (char *)mi_get_error_str());
		return e;
	}

	/*
	** Wait until we hit the breakpoint at main().
	*/

	while ( !mi_get_response(MIHandle) )
		usleep(1000);

	sr = mi_res_stop(MIHandle);
	
	if ( !sr || SetCurrFrame() < 0 )
	{
		EVENT_ERROR(e, DBGERR_DEBUGGER, (char *)mi_get_error_str());
		return e;
	}

	if ( DbgProg != NULL ) 
		Free(DbgProg);

	DbgProg = strdup(prog);

	Status = DBGSTAT_STOPPED;

	return NewEvent(DBGEV_OK);
}
#endif

char tohex[] =	{'0', '1', '2', '3', '4', '5', '6', '7', 
		 '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

static int
GDBMIBuildAIFVar(char *var, char *type, char *file, dbg_event **ev)
{
	int		n;
	int		fd;
	AIF *		aif;
	char *		data;
	char *		ap;
	char *		bp;
	char		buf[BUFSIZ];
	struct stat	sb;
	dbg_event *	e;

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
		ap = data = Alloc(strlen(var) * 2 + 1);

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

		ap = data = Alloc(sb.st_size * 2 + 1);

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

	if ( (aif = AsciiToAIF(type, data)) == NULL )
	{
		DbgSetError(DBGERR_DEBUGGER, AIFErrorStr());
		return DBGRES_ERR;
	}

	e = NewEvent(DBGEV_DATA);
	e->ev_data = aif;
	*ev = e;
	
	return DBGRES_OK;
}
