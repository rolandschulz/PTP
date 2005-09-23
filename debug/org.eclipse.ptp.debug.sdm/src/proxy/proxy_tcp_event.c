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

#include <string.h>
#include <stdlib.h>
#include <ctype.h>

#include "dbg_event.h"
#include "stackframe.h"
#include "args.h"

static char tohex[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

static int
proxy_tcp_data_to_str(char *data, int len, char **result)
{
	int		i;
	int		n;
	char		ch;
	char *	res;
	
	if (data == NULL) {
		*result = strdup("0:0");
		return 0;
	}
	
	*result = res = (char *)malloc((len * 2) + 8 + 2);
	
	/*
	 * Convert length (silently truncate to 32 bits)
	 */
	n = sprintf(res, "%X:", len & 0xffffffff);
	
	res += n;
	
	/*
	 * Add rest of data 
	 */
	for (i = 0; i < len; i++) {
		ch = *data++;
		*res++ = tohex[(ch >> 4) & 0xf];
		*res++ = tohex[ch & 0xf];
	}
	
	*res = '\0';
	
	return 0;
}

static int
proxy_tcp_cstring_to_str(char *str, char **result)
{
	int	len;
	
	if (str == NULL)
		len = 0;
	else
		len = strlen(str) + 1;

	return proxy_tcp_data_to_str(str, len, result);
}

static int
proxy_tcp_location_to_str(location *loc, char **result)
{
	char *	file;
	char *	func;
	char *	addr;

	proxy_tcp_cstring_to_str(loc->file, &file);
	proxy_tcp_cstring_to_str(loc->func, &func);
	proxy_tcp_cstring_to_str(loc->addr, &addr);

	asprintf(result, "%s %s %s %d", file, func, addr, loc->line);

	free(file);
	free(func);
	free(addr);

	return 0;
}

static int
proxy_tcp_breakpoint_to_str(breakpoint *bp, char **result)
{
	char *	type;
	char *	loc;
	
	proxy_tcp_cstring_to_str(bp->type, &type);
	proxy_tcp_location_to_str(&bp->loc, &loc);
	
	asprintf(result, "%d %d %d %d %s %s %d", bp->id, bp->ignore, bp->special, bp->deleted, type, loc, bp->hits);

	free(type);
	free(loc);

	return 0;
	
}

static int
proxy_tcp_stackframe_to_str(stackframe *sf, char **result)
{
	char *	loc;
	
	proxy_tcp_location_to_str(&sf->loc, &loc);
	
	asprintf(result, "%d %s", sf->level, loc);
	
	free(loc);
	
	return 0;
}
	
static int
proxy_tcp_list_to_str(List *lst, int (*el_to_str)(void *, char **), char **result)
{
	int		i;
	int		len;
	int		count;
	void *	el;
	char *	res;
	char **	strs;
	/*
	 * Calculate number of strings.
	 */
	for (count = 0, SetList(lst); GetListElement(lst) != NULL; )
		count++;
	
	strs = (char **)malloc(sizeof(char *) * count);
	
	/*
	 * Convert strings.
	 */
	for (len = 0, i = 0, SetList(lst); (el = GetListElement(lst)) != NULL; i++) {
		el_to_str(el, &strs[i]);
		len += strlen(strs[i]) + 1;
	}

	*result = res = (char *)malloc(9 + len + 1);
	
	sprintf(res, "%X ", count & 0xffff);
	
	for (i = 0; i < count; i++) {
		if (i > 0)
			strcat(res, " ");
		strcat(res, strs[i]);
		free(strs[i]);
	}
	
	free(strs);

	return 0;
}


static int
proxy_tcp_vars_to_str(List *lst, char **result)
{
	return proxy_tcp_list_to_str(lst, (int (*)(void *, char **))proxy_tcp_cstring_to_str, result);
}

static int
proxy_tcp_stackframes_to_str(List *lst, char **result)
{
	return proxy_tcp_list_to_str(lst, (int (*)(void *, char **))proxy_tcp_stackframe_to_str, result);
}

static int
proxy_tcp_aif_to_str(AIF *a, char **result)
{
	char *	fmt;
	char *	data;
		
	proxy_tcp_data_to_str(AIF_FORMAT(a), strlen(AIF_FORMAT(a)), &fmt);
	proxy_tcp_data_to_str(AIF_DATA(a), AIF_LEN(a), &data);
	
	asprintf(result, "%s %s", fmt, data);

	free(fmt);
	free(data);

	return 0;
}

int
proxy_tcp_event_to_str(dbg_event *e, char **result)
{
	int		res = 0;
	char *	str;
	char *	str2;
	char *	pstr;

	if (e == NULL)
		return -1;
	
	pstr = procset_to_str(e->procs);
	
	switch (e->event)
	{
	case DBGEV_OK:
 		asprintf(result, "%d %s", e->event, pstr);
		break;

	case DBGEV_ERROR:
		proxy_tcp_cstring_to_str(e->error_msg, &str);
 		asprintf(result, "%d %s %d %s", e->event, pstr, e->error_code, str);
		break;
	
	case DBGEV_BPHIT:
	case DBGEV_BPSET:
		proxy_tcp_breakpoint_to_str(e->bp, &str);
		asprintf(result, "%d %s %s", e->event, pstr, str);
		free(str);
		break;

	case DBGEV_SIGNAL:
		proxy_tcp_cstring_to_str(e->sig_name, &str);
		proxy_tcp_cstring_to_str(e->sig_meaning, &str2);
		asprintf(result, "%d %s %s %s %d", e->event, pstr, str, str2, e->thread_id);
		free(str);
		free(str2);
		break;

	case DBGEV_EXIT:
		asprintf(result, "%d %s %d", e->event, pstr, e->exit_status);
		break;

	case DBGEV_STEP:
		asprintf(result, "%d %s %d", e->event, pstr, e->thread_id);
		break;

	case DBGEV_FRAMES:
		proxy_tcp_stackframes_to_str(e->list, &str);
		asprintf(result, "%d %s %s", e->event, pstr, str);
		free(str);
		break;

	case DBGEV_VARS:
		proxy_tcp_vars_to_str(e->list, &str);
		asprintf(result, "%d %s %s", e->event, pstr, str);
		free(str);
		break;

	case DBGEV_TYPE:
		proxy_tcp_cstring_to_str(e->type_desc, &str);
		asprintf(result, "%d %s %s", e->event, pstr, str);
		free(str);
		break;

	case DBGEV_DATA:
		proxy_tcp_aif_to_str(e->data, &str);
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
proxy_tcp_str_to_data(char *str, char **data, int *len)
{
	int		data_len;
	char		ch;
	char *	p;
	
	if (str == NULL)
		return -1;
		
	for (data_len = 0; *str != ':' && *str != '\0' && isxdigit(*str); str++) {
		data_len <<= 4;
		data_len += digittoint(*str);
	}
	
	if (*str++ != ':')
		return -1;

	*len = data_len;
	*data = p = (char *)malloc(sizeof(char) * data_len);
	
	for (; data_len > 0; data_len--) {
		ch = digittoint(*str++);
		ch <<= 4;
		ch |= digittoint(*str++);

		*p++ = ch;
	}
		
	return 0;
}

static int
proxy_tcp_str_to_cstring(char *str, char **cstring)
{
	int	len;
	
	return proxy_tcp_str_to_data(str, cstring, &len);
}


static int
proxy_tcp_str_to_int(char *str, int *val)
{
	if (str == NULL)
		return -1;
	
	*val = atoi(str);
	
	return 0;
}

static int
proxy_tcp_str_to_location(char **args, location *loc)
{
	if (proxy_tcp_str_to_cstring(args[0], &loc->file) < 0 ||
		proxy_tcp_str_to_cstring(args[1], &loc->func) < 0 ||
		proxy_tcp_str_to_cstring(args[2], &loc->addr) < 0 ||
		proxy_tcp_str_to_int(args[3], &loc->line) < 0) {
		FreeLocation(loc);
		return -1;
	}
	
	return 0;
}

static int
proxy_tcp_str_to_breakpoint(char **args, breakpoint **bp)
{
	int			id;
	breakpoint *	b;
	
	if (proxy_tcp_str_to_int(args[0], &id) < 0)
		return -1;
		
	b = NewBreakpoint(id);
	
	if (proxy_tcp_str_to_int(args[1], &b->ignore) < 0 ||
		proxy_tcp_str_to_int(args[2], &b->special) < 0 ||
		proxy_tcp_str_to_int(args[3], &b->deleted) < 0 ||
		proxy_tcp_str_to_cstring(args[4], &b->type) < 0 ||
		proxy_tcp_str_to_location(&args[5], &b->loc) < 0 ||
		proxy_tcp_str_to_int(args[9], &b->hits) < 0) {
		FreeBreakpoint(b);
		return -1;
	}
	
	*bp = b;
	
	return 0;
}

static int
proxy_tcp_str_to_stackframes(char **args, List **lst)
{
	int			i;
	int			pos;
	int			level;
	int			count = atoi(args[0]);
	stackframe *	sf;

	*lst = NewList();
	
	for (i = 0, pos = 1; i < count; i++, pos += 5) {
		if (proxy_tcp_str_to_int(args[pos], &level) < 0) {
			DestroyList(*lst, FreeStackframe);
			return -1;
		}
		sf = NewStackframe(level);
		if (proxy_tcp_str_to_location(&args[pos+1], &sf->loc) < 0) {
			FreeStackframe(sf);
			DestroyList(*lst, FreeStackframe);
			return -1;			
		}
		AddToList(*lst, (void *)sf);
	}
	
	return 0;
}

static int
proxy_tcp_str_to_vars(char **args, List **lst)
{
	int		i;
	int		count = atoi(args[0]);
	char *	str;
	
	*lst = NewList();
	
	for (i = 0; i < count; i++) {
		if (proxy_tcp_str_to_cstring(args[i+1], &str) < 0) {
			DestroyList(*lst, free);
			return -1;
		}
		AddToList(*lst, (void *)str);
	}
	
	return 0;
}

static int
proxy_tcp_str_to_aif(char **args, AIF **res)
{
	int		fmt_len;
	int		data_len;
	AIF *	a;
	char *	fmt;
	char *	data;
	
	if (proxy_tcp_str_to_data(args[0], &fmt, &fmt_len) < 0 ||
		proxy_tcp_str_to_data(args[1], &data, &data_len))
		return -1;
	
	a = NewAIF(0, 0);
	AIF_FORMAT(a) = fmt;
	AIF_DATA(a) = data;
	AIF_LEN(a) = data_len;
	
	*res = a;
	
	return 0;
}

int
proxy_tcp_str_to_event(char *str, dbg_event **ev)
{
	int			event;
	dbg_event *	e = NULL;
	char **		args;
	procset *	procs = NULL;
		
	if (str == NULL || (args = Str2Args(str)) == NULL)
		return -1;

	if (proxy_tcp_str_to_int(args[0], &event) < 0 || args[1] == NULL) {
		FreeArgs(args);
		return -1;
	}
		
	procs = str_to_procset(args[1]);

	switch (event)
	{
	case DBGEV_OK:
		e = NewEvent(DBGEV_OK);
		break;
	
	case DBGEV_ERROR:
		e = NewEvent(DBGEV_ERROR);
		if (proxy_tcp_str_to_int(args[2], &e->error_code) < 0 ||
			proxy_tcp_str_to_cstring(args[3], &e->error_msg) < 0)
			goto error_out;
		break;
		
	case DBGEV_BPHIT:
		e = NewEvent(DBGEV_BPHIT);
		if (proxy_tcp_str_to_breakpoint(&args[2], &e->bp) < 0)
			goto error_out;
		break;
	
	case DBGEV_BPSET:
		e = NewEvent(DBGEV_BPSET);
		if (proxy_tcp_str_to_breakpoint(&args[2], &e->bp) < 0)
			goto error_out;
		break;
	
	case DBGEV_SIGNAL:
		e = NewEvent(DBGEV_SIGNAL);
		if (proxy_tcp_str_to_cstring(args[2], &e->sig_name) < 0 ||
			proxy_tcp_str_to_cstring(args[3], &e->sig_meaning) < 0 ||
			proxy_tcp_str_to_int(args[4], &e->thread_id) < 0)
			goto error_out;
		break;
	
	case DBGEV_EXIT:
		e = NewEvent(DBGEV_EXIT);
		if (proxy_tcp_str_to_int(args[2], &e->exit_status) < 0)
			goto error_out;
		break;
	
	case DBGEV_STEP:
		e = NewEvent(DBGEV_STEP);
		if (proxy_tcp_str_to_int(args[2], &e->thread_id) < 0)
			goto error_out;
		break;
	
	case DBGEV_FRAMES:
		e = NewEvent(DBGEV_FRAMES);
		if (proxy_tcp_str_to_stackframes(&args[2], &e->list) < 0)
			goto error_out;
		break;

	case DBGEV_VARS:
		e = NewEvent(DBGEV_VARS);
		if (proxy_tcp_str_to_vars(&args[2], &e->list) < 0)
			goto error_out;
		break;

	case DBGEV_TYPE:
		e = NewEvent(DBGEV_TYPE);
		if (proxy_tcp_str_to_cstring(args[2], &e->type_desc) < 0)
			goto error_out;
		break;

	case DBGEV_DATA:
		e = NewEvent(DBGEV_DATA);
		if (proxy_tcp_str_to_aif(&args[2], &e->data) < 0)
			goto error_out;
		break;

	default:
		goto error_out;
	}
	
	e->procs = procs;
	*ev = e;
	
	FreeArgs(args);
	
	return 0;
	
error_out:
	FreeArgs(args);
	
	if (procs != NULL)
		procset_free(procs);
	
	if (e != NULL)
		FreeEvent(e);
		
	return -1;
}
