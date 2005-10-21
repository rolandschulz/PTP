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

#include <stdlib.h>
#include <string.h>

#include "dbg_event.h"
#include "stackframe.h"
#include "proxy_event.h"
#include "args.h"

static int
dbg_location_to_str(location *loc, char **result)
{
	char *	file;
	char *	func;
	char *	addr;

	proxy_cstring_to_str(loc->file, &file);
	proxy_cstring_to_str(loc->func, &func);
	proxy_cstring_to_str(loc->addr, &addr);

	asprintf(result, "%s %s %s %d", file, func, addr, loc->line);

	free(file);
	free(func);
	free(addr);

	return 0;
}

static int
dbg_breakpoint_to_str(breakpoint *bp, char **result)
{
	char *	type;
	char *	loc;
	
	proxy_cstring_to_str(bp->type, &type);
	dbg_location_to_str(&bp->loc, &loc);
	
	asprintf(result, "%d %d %d %d %s %s %d", bp->id, bp->ignore, bp->special, bp->deleted, type, loc, bp->hits);

	free(type);
	free(loc);

	return 0;
	
}

static int
dbg_stackframe_to_str(stackframe *sf, char **result)
{
	char *	loc;
	
	dbg_location_to_str(&sf->loc, &loc);
	
	asprintf(result, "%d %s", sf->level, loc);
	
	free(loc);
	
	return 0;
}

static int
dbg_vars_to_str(List *lst, char **result)
{
	return proxy_list_to_str(lst, (int (*)(void *, char **))proxy_cstring_to_str, result);
}

static int
dbg_stackframes_to_str(List *lst, char **result)
{
	return proxy_list_to_str(lst, (int (*)(void *, char **))dbg_stackframe_to_str, result);
}

static int
dbg_aif_to_str(AIF *a, char **result)
{
	char *	fmt;
	char *	data;
		
	proxy_data_to_str(AIF_FORMAT(a), strlen(AIF_FORMAT(a)), &fmt);
	proxy_data_to_str(AIF_DATA(a), AIF_LEN(a), &data);
	
	asprintf(result, "%s %s", fmt, data);

	free(fmt);
	free(data);

	return 0;
}

int
DbgEventToStr(dbg_event *e, char **result)
{
	int		res = 0;
	char *	str;
	char *	str2;
	char *	pstr;

	if (e == NULL)
		return -1;
	
	pstr = bitset_to_str(e->procs);
	
	switch (e->event)
	{
	case DBGEV_OK:
		asprintf(result, "%d %s", e->event, pstr);
		break;
		
	case DBGEV_ERROR:
 		proxy_cstring_to_str(e->error_msg, &str);
 		asprintf(result, "%d %s %d %s", e->event, pstr, e->error_code, str);
		break;
	
	case DBGEV_INIT:
 		asprintf(result, "%d %s %d", e->event, pstr, e->num_servers);
		break;
	
	case DBGEV_BPHIT:
		asprintf(result, "%d %s %d", e->event, pstr, e->bpid);
		break;

	case DBGEV_BPSET:
		dbg_breakpoint_to_str(e->bp, &str);
		asprintf(result, "%d %s %d %s", e->event, pstr, e->bpid, str);
		free(str);
		break;

	case DBGEV_SIGNAL:
		proxy_cstring_to_str(e->sig_name, &str);
		proxy_cstring_to_str(e->sig_meaning, &str2);
		asprintf(result, "%d %s %s %s %d", e->event, pstr, str, str2, e->thread_id);
		free(str);
		free(str2);
		break;

	case DBGEV_EXIT:
		asprintf(result, "%d %s %d", e->event, pstr, e->exit_status);
		break;

	case DBGEV_STEP:
		dbg_stackframe_to_str(e->frame, &str);
		asprintf(result, "%d %s %s", e->event, pstr, str);
		break;

	case DBGEV_FRAMES:
		dbg_stackframes_to_str(e->list, &str);
		asprintf(result, "%d %s %s", e->event, pstr, str);
		free(str);
		break;

	case DBGEV_VARS:
		dbg_vars_to_str(e->list, &str);
		asprintf(result, "%d %s %s", e->event, pstr, str);
		free(str);
		break;

	case DBGEV_TYPE:
		proxy_cstring_to_str(e->type_desc, &str);
		asprintf(result, "%d %s %s", e->event, pstr, str);
		free(str);
		break;

	case DBGEV_DATA:
		dbg_aif_to_str(e->data, &str);
		asprintf(result, "%d %s %s", e->event, pstr, str);
		free(str);
		break;
		
	default:
		res = -1;
		break;
	}

	free(pstr);
	
	return res;
}

static int
dbg_str_to_location(char **args, location *loc)
{
	if (proxy_str_to_cstring(args[0], &loc->file) < 0 ||
		proxy_str_to_cstring(args[1], &loc->func) < 0 ||
		proxy_str_to_cstring(args[2], &loc->addr) < 0 ||
		proxy_str_to_int(args[3], &loc->line) < 0) {
		FreeLocation(loc);
		return -1;
	}
	
	return 0;
}

static int
dbg_str_to_breakpoint(char **args, breakpoint **bp)
{
	int			id;
	breakpoint *	b;
	
	if (proxy_str_to_int(args[0], &id) < 0)
		return -1;
		
	b = NewBreakpoint(id);
	
	if (proxy_str_to_int(args[1], &b->ignore) < 0 ||
		proxy_str_to_int(args[2], &b->special) < 0 ||
		proxy_str_to_int(args[3], &b->deleted) < 0 ||
		proxy_str_to_cstring(args[4], &b->type) < 0 ||
		dbg_str_to_location(&args[5], &b->loc) < 0 ||
		proxy_str_to_int(args[9], &b->hits) < 0) {
		FreeBreakpoint(b);
		return -1;
	}
	
	*bp = b;
	
	return 0;
}

static int
dbg_str_to_stackframe(char **args, stackframe **frame)
{
	int			level;
	stackframe *	sf;
	
	if (proxy_str_to_int(args[0], &level) < 0) {
		return -1;
	}
	sf = NewStackframe(level);
	if (dbg_str_to_location(&args[1], &sf->loc) < 0) {
		FreeStackframe(sf);
		return -1;			
	}
	
	*frame = sf;
	
	return 0;
}

static int
dbg_str_to_stackframes(char **args, List **lst)
{
	int			i;
	int			pos;
	int			count = atoi(args[0]);
	stackframe *	sf;

	*lst = NewList();
	
	for (i = 0, pos = 1; i < count; i++, pos += 5) {
		if (dbg_str_to_stackframe(&args[pos], &sf) < 0) {
			DestroyList(*lst, FreeStackframe);
			return -1;
		}
		AddToList(*lst, (void *)sf);
	}
	
	return 0;
}

static int
dbg_str_to_vars(char **args, List **lst)
{
	int		i;
	int		count = atoi(args[0]);
	char *	str;
	
	*lst = NewList();
	
	for (i = 0; i < count; i++) {
		if (proxy_str_to_cstring(args[i+1], &str) < 0) {
			DestroyList(*lst, free);
			return -1;
		}
		AddToList(*lst, (void *)str);
	}
	
	return 0;
}

static int
dbg_str_to_aif(char **args, AIF **res)
{
	int		fmt_len;
	int		data_len;
	AIF *	a;
	char *	fmt;
	char *	data;
	
	if (proxy_str_to_data(args[0], &fmt, &fmt_len) < 0 ||
		proxy_str_to_data(args[1], &data, &data_len))
		return -1;
	
	a = NewAIF(0, 0);
	AIF_FORMAT(a) = fmt;
	AIF_DATA(a) = data;
	AIF_LEN(a) = data_len;
	
	*res = a;
	
	return 0;
}

int
DbgStrToEvent(char *str, dbg_event **ev)
{
	int			event;
	char **		args;
	dbg_event *	e = NULL;
	bitset *		procs = NULL;
	
	if (str == NULL || (args = Str2Args(str)) == NULL)
		return -1;
		
	event = atoi(args[0]);
	
	procs = str_to_bitset(args[1]);

	switch (event)
	{
	case DBGEV_OK:
		e = NewDbgEvent(DBGEV_OK);
		break;
		
	case DBGEV_ERROR:
		e = NewDbgEvent(DBGEV_ERROR);
		if (proxy_str_to_int(args[2], &e->error_code) < 0 ||
			proxy_str_to_cstring(args[3], &e->error_msg) < 0)
			goto error_out;
		break;
		
	case DBGEV_INIT:
		e = NewDbgEvent(DBGEV_INIT);
		if (proxy_str_to_int(args[2], &e->num_servers) < 0)
			goto error_out;
		break;
		
	case DBGEV_BPHIT:
		e = NewDbgEvent(DBGEV_BPHIT);
		if (proxy_str_to_int(args[2], &e->bpid) < 0)
			goto error_out;
		break;
	
	case DBGEV_BPSET:
		e = NewDbgEvent(DBGEV_BPSET);
		if (proxy_str_to_int(args[2], &e->bpid) < 0 ||
			dbg_str_to_breakpoint(&args[3], &e->bp)	< 0)
			goto error_out;
		break;
	
	case DBGEV_SIGNAL:
		e = NewDbgEvent(DBGEV_SIGNAL);
		if (proxy_str_to_cstring(args[2], &e->sig_name) < 0 ||
			proxy_str_to_cstring(args[3], &e->sig_meaning) < 0 ||
			proxy_str_to_int(args[4], &e->thread_id) < 0)
			goto error_out;
		break;
	
	case DBGEV_EXIT:
		e = NewDbgEvent(DBGEV_EXIT);
		if (proxy_str_to_int(args[2], &e->exit_status) < 0)
			goto error_out;
		break;
	
	case DBGEV_STEP:
		e = NewDbgEvent(DBGEV_STEP);
		if (dbg_str_to_stackframe(&args[2], &e->frame) < 0)
			goto error_out;
		break;
	
	case DBGEV_FRAMES:
		e = NewDbgEvent(DBGEV_FRAMES);
		if (dbg_str_to_stackframes(&args[2], &e->list) < 0)
			goto error_out;
		break;

	case DBGEV_VARS:
		e = NewDbgEvent(DBGEV_VARS);
		if (dbg_str_to_vars(&args[2], &e->list) < 0)
			goto error_out;
		break;

	case DBGEV_TYPE:
		e = NewDbgEvent(DBGEV_TYPE);
		if (proxy_str_to_cstring(args[2], &e->type_desc) < 0)
			goto error_out;
		break;

	case DBGEV_DATA:
		e = NewDbgEvent(DBGEV_DATA);
		if (dbg_str_to_aif(&args[2], &e->data) < 0)
			goto error_out;
		break;

	default:
		goto error_out;
	}
	
	FreeArgs(args);
	
	e->procs = procs;
	*ev = e;
	
	return 0;
	
error_out:
	FreeArgs(args);
	
	if (procs != NULL)
		bitset_free(procs);
	
	if (e != NULL)
		FreeDbgEvent(e);
		
	return -1;
}

dbg_event *	
NewDbgEvent(int event) {
	dbg_event *	e = (dbg_event *)malloc(sizeof(dbg_event));
	
	memset((void *)e, 0, sizeof(dbg_event));
	 
	e->event = event;
	
	return e;
}

void	
FreeDbgEvent(dbg_event *e) {
	switch (e->event) {
	case DBGEV_OK:
	case DBGEV_EXIT:
	case DBGEV_INIT:
	case DBGEV_BPHIT:
		break;
		
	case DBGEV_STEP:
		if (e->frame != NULL)
			FreeStackframe(e->frame);
		break;
			
	case DBGEV_FRAMES:
		if (e->list != NULL)
			DestroyList(e->list, FreeStackframe);
		break;
		
	case DBGEV_DATA:
		if (e->data != NULL)
			AIFFree(e->data);
		break;
		
	case DBGEV_TYPE:
		if (e->type_desc != NULL)
			free(e->type_desc);
		break;
		
	case DBGEV_VARS:
		if (e->list != NULL)
			DestroyList(e->list, free);
		break;
		
	case DBGEV_SIGNAL:
		if (e->list != NULL) {
			free(e->sig_name);
			free(e->sig_meaning);
		}
		break;
		
	case DBGEV_BPSET:
		if (e->bp != NULL)
			FreeBreakpoint(e->bp);
		break;
	}
	
	if (e->procs != NULL)
		bitset_free(e->procs);

	free(e);
}
