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

#include <stdlib.h>
#include <string.h>

#include "dbg_event.h"
#include "args.h"
#include "proxy_msg.h"

#define EMPTY	"*"

static void
dbg_add_location(proxy_msg *m, location *loc)
{
	proxy_msg_add_string(m, loc->file);
	proxy_msg_add_string(m, loc->func);
	proxy_msg_add_string(m, loc->addr);
	proxy_msg_add_int(m, loc->line);
}

static void
dbg_add_breakpoint(proxy_msg *m, breakpoint *bp)
{
	proxy_msg_add_int(m, bp->id);
	proxy_msg_add_int(m, bp->ignore);
	proxy_msg_add_int(m, bp->special);
	proxy_msg_add_int(m, bp->deleted);
	proxy_msg_add_string(m, bp->type);
	dbg_add_location(m, &bp->loc);
	proxy_msg_add_int(m, bp->hits);
}

static void
dbg_add_signalinfo(proxy_msg *m, signal_info *sig)
{
	if (sig == NULL) {
		proxy_msg_add_string(m, EMPTY);
		return;
	}

	proxy_msg_add_string(m, sig->name);
	proxy_msg_add_int(m, sig->stop);
	proxy_msg_add_int(m, sig->print);
	proxy_msg_add_int(m, sig->pass);
	proxy_msg_add_string(m, sig->desc);
}

static void
dbg_add_stackframe(proxy_msg *m, stackframe *sf)
{
	if (sf == NULL) {
		proxy_msg_add_string(m, EMPTY);
		return;
	}

	proxy_msg_add_int(m, sf->level);
	dbg_add_location(m, &sf->loc);
}

static void
dbg_add_strings(proxy_msg *m, List *lst)
{
	char *	s;

	proxy_msg_add_int(m, SizeOfList(lst));

	for (SetList(lst); (s = (char *)GetListElement(lst)) != NULL; ) {
		proxy_msg_add_string(m, s);
	}
}

static void
dbg_add_stackframes(proxy_msg *m, List *lst)
{
	stackframe *	s;

	proxy_msg_add_int(m, SizeOfList(lst));

	for (SetList(lst); (s = (stackframe *)GetListElement(lst)) != NULL; ) {
		dbg_add_stackframe(m, s);
	}
}

static void
dbg_add_signals(proxy_msg *m, List *lst)
{
	signal_info *	s;

	proxy_msg_add_int(m, SizeOfList(lst));

	for (SetList(lst); (s = (signal_info *)GetListElement(lst)) != NULL; ) {
		dbg_add_signalinfo(m, s);
	}
}

static void
dbg_add_memory(proxy_msg *m, memory *mem)
{
	if (mem == NULL) {
		proxy_msg_add_string(m, EMPTY);
		return;
	}

	proxy_msg_add_string(m, mem->addr);
	proxy_msg_add_string(m, mem->ascii);
	dbg_add_strings(m, mem->data);
}

static void
dbg_add_memories(proxy_msg *m, List *lst)
{
	memory *	mem;

	proxy_msg_add_int(m, SizeOfList(lst));

	for (SetList(lst); (mem = (memory *)GetListElement(lst)) != NULL; ) {
		dbg_add_memory(m, mem);
	}
}

static void
dbg_add_memoryinfo(proxy_msg *m, memoryinfo *meninfo)
{
	if (meninfo == NULL) {
		proxy_msg_add_string(m, EMPTY);
		return;
	}

	proxy_msg_add_string(m, meninfo->addr);
	proxy_msg_add_int(m, meninfo->nextRow);
	proxy_msg_add_int(m, meninfo->prevRow);
	proxy_msg_add_int(m, meninfo->nextPage);
	proxy_msg_add_int(m, meninfo->prevPage);
	proxy_msg_add_int(m, meninfo->numBytes);
	proxy_msg_add_int(m, meninfo->totalBytes);
	dbg_add_memories(m, meninfo->memories);
}

static void
dbg_add_aif(proxy_msg *m, AIF *a)
{
	proxy_msg_add_string(m, AIF_FORMAT(a));
	proxy_msg_add_data(m, AIF_DATA(a), AIF_LEN(a));
}

/*
 * Serialize a debug event for sending between debug servers.
 *
 * Note that the serialized event does not include the bitset, since it is
 * only relevant to communication with the client. The bitset is
 * added just prior to sending back to the client.
 */
int
DbgSerializeEvent(dbg_event *e, char **result, int *len)
{
	int			res = 0;
	proxy_msg *	p = NULL;

	if (e == NULL)
		return -1;

	p = new_proxy_msg(e->event_id, e->trans_id);

	switch (e->event_id)
	{
	case DBGEV_OK:
		break;

	case DBGEV_ERROR:
		proxy_msg_add_int(p, e->dbg_event_u.error_event.error_code);
		proxy_msg_add_string(p, e->dbg_event_u.error_event.error_msg);
		break;

	case DBGEV_OUTPUT:
		proxy_msg_add_string(p, e->dbg_event_u.output);
		break;

	case DBGEV_SUSPEND:
		proxy_msg_add_int(p, e->dbg_event_u.suspend_event.reason);

		switch (e->dbg_event_u.suspend_event.reason) {
		case DBGEV_SUSPEND_BPHIT:
			proxy_msg_add_int(p, e->dbg_event_u.suspend_event.ev_u.bpid);
			proxy_msg_add_int(p, e->dbg_event_u.suspend_event.thread_id);
			proxy_msg_add_int(p, e->dbg_event_u.suspend_event.depth);
			dbg_add_strings(p, e->dbg_event_u.suspend_event.changed_vars);
			break;

		case DBGEV_SUSPEND_SIGNAL:
			dbg_add_signalinfo(p, e->dbg_event_u.suspend_event.ev_u.sig);
			dbg_add_stackframe(p, e->dbg_event_u.suspend_event.frame);
			proxy_msg_add_int(p, e->dbg_event_u.suspend_event.thread_id);
			proxy_msg_add_int(p, e->dbg_event_u.suspend_event.depth);
			dbg_add_strings(p, e->dbg_event_u.suspend_event.changed_vars);
			break;

		case DBGEV_SUSPEND_STEP:
		case DBGEV_SUSPEND_INT:
			dbg_add_stackframe(p, e->dbg_event_u.suspend_event.frame);
			proxy_msg_add_int(p, e->dbg_event_u.suspend_event.thread_id);
			proxy_msg_add_int(p, e->dbg_event_u.suspend_event.depth);
			dbg_add_strings(p, e->dbg_event_u.suspend_event.changed_vars);
			break;
		}
		break;

	case DBGEV_BPSET:
 		proxy_msg_add_int(p, e->dbg_event_u.bpset_event.bpid);
		dbg_add_breakpoint(p, e->dbg_event_u.bpset_event.bp);
		break;

	case DBGEV_SIGNALS:
 		dbg_add_signals(p, e->dbg_event_u.list);
		break;

	case DBGEV_EXIT:
		proxy_msg_add_int(p, e->dbg_event_u.exit_event.reason);

		switch (e->dbg_event_u.exit_event.reason) {
		case DBGEV_EXIT_NORMAL:
			proxy_msg_add_int(p, e->dbg_event_u.exit_event.ev_u.exit_status);
			break;

		case DBGEV_EXIT_SIGNAL:
			dbg_add_signalinfo(p, e->dbg_event_u.exit_event.ev_u.sig);
			break;
		}
		break;

	case DBGEV_FRAMES:
 		dbg_add_stackframes(p, e->dbg_event_u.list);
		break;

	case DBGEV_THREAD_SELECT:
 		proxy_msg_add_int(p, e->dbg_event_u.thread_select_event.thread_id);
		dbg_add_stackframe(p, e->dbg_event_u.thread_select_event.frame);
		break;

	case DBGEV_THREADS:
 		proxy_msg_add_int(p, e->dbg_event_u.threads_event.thread_id);
		dbg_add_strings(p, e->dbg_event_u.threads_event.list);
		break;

	case DBGEV_STACK_DEPTH:
 		proxy_msg_add_int(p, e->dbg_event_u.stack_depth);
		break;

	case DBGEV_DATAR_MEM:
 		dbg_add_memoryinfo(p, e->dbg_event_u.meminfo);
		break;

	case DBGEV_VARS:
 		dbg_add_strings(p, e->dbg_event_u.list);
		break;

	case DBGEV_ARGS:
		dbg_add_strings(p, e->dbg_event_u.list);
		break;

	case DBGEV_TYPE:
		proxy_msg_add_string(p, e->dbg_event_u.type_desc);
		break;

	case DBGEV_DATA:
 		dbg_add_aif(p, e->dbg_event_u.data_event.data);
		proxy_msg_add_string(p, e->dbg_event_u.data_event.type_desc);
		proxy_msg_add_string(p, e->dbg_event_u.data_event.name);
		break;

	default:
		res = -1;
		break;
	}

	res = proxy_serialize_msg(p, result, len);
	free_proxy_msg(p);
	return res;
}

static int
dbg_str_to_int(char ***args, int *nargs, int *val)
{
	if (*nargs < 1)
		return -1;

	proxy_get_int(*(*args)++, val);
	(*nargs)--;

	return 0;
}

static int
dbg_copy_str(char ***args, int *nargs, char **str)
{
	if (*nargs < 1)
		return -1;

	*str = strdup(*(*args)++);
	(*nargs)--;

	return 0;
}

static int
dbg_str_to_data(char ***args, int *nargs, char **data, int *len)
{
	if (*nargs < 1)
		return -1;

	proxy_get_data(*(*args)++, data, len);
	(*nargs)--;

	return 0;
}

static int
dbg_str_to_location(char ***args, int *nargs, location *loc)
{
	if (dbg_copy_str(args, nargs, &loc->file) < 0 ||
		dbg_copy_str(args, nargs, &loc->func) < 0 ||
		dbg_copy_str(args, nargs, &loc->addr) < 0 ||
		dbg_str_to_int(args, nargs, &loc->line) < 0) {
			FreeLocation(loc);
			return -1;
	}

	return 0;
}

static int
dbg_str_to_breakpoint(char ***args, int *nargs, breakpoint **bp)
{
	int				id;
	breakpoint *	b;

	if (dbg_str_to_int(args, nargs, &id) < 0) {
		return -1;
	}

	b = NewBreakpoint(id);

	if (dbg_str_to_int(args, nargs, &b->ignore) < 0 ||
		dbg_str_to_int(args, nargs, &b->special) < 0 ||
		dbg_str_to_int(args, nargs, &b->deleted) < 0 ||
		dbg_copy_str(args, nargs, &b->type) < 0 ||
		dbg_str_to_location(args, nargs, &b->loc) < 0 ||
		dbg_str_to_int(args, nargs, &b->hits) < 0) {
			FreeBreakpoint(b);
			return -1;
	}

	*bp = b;

	return 0;
}

static int
dbg_str_to_stackframe(char ***args, int *nargs, stackframe **frame)
{
	int				level;
	stackframe *	sf;

	if (strcmp(*(*args), EMPTY) == 0) {
		(*args)++;
		(*nargs)--;
		*frame = NULL;
		return 0;
	}

	if (dbg_str_to_int(args, nargs, &level) < 0) {
		return -1;
	}

	sf = NewStackframe(level);

	if (dbg_str_to_location(args, nargs, &sf->loc) < 0) {
		FreeStackframe(sf);
		return -1;
	}

	*frame = sf;

	return 0;
}

static int
dbg_str_to_signalinfo(char ***args, int *nargs, signal_info **sig)
{
	signal_info *s;

	if (strcmp(*(*args), EMPTY) == 0) {
		(*args)++;
		(*nargs)--;
		*sig = NULL;
		return 0;
	}

	s = NewSignalInfo();

	if (dbg_copy_str(args, nargs, &s->name) < 0 ||
		dbg_str_to_int(args, nargs, &s->stop) < 0 ||
		dbg_str_to_int(args, nargs, &s->print) < 0 ||
		dbg_str_to_int(args, nargs, &s->pass) < 0 ||
		dbg_copy_str(args, nargs, &s->desc) < 0) {
			FreeSignalInfo(s);
			return -1;
	}

	*sig = s;

	return 0;
}

static int
dbg_str_to_stackframes(char ***args, int *nargs, List **lst)
{
	int				i;
	int				count;
	stackframe *	sf;

	if (dbg_str_to_int(args, nargs, &count) < 0) {
		return -1;
	}

	*lst = NewList();

	for (i = 0; i < count; i++) {
		if (dbg_str_to_stackframe(args, nargs, &sf) < 0) {
			DestroyList(*lst, FreeStackframe);
			return -1;
		}
		AddToList(*lst, (void *)sf);
	}

	return 0;
}

static int
dbg_str_to_signals(char ***args, int *nargs, List **lst)
{
	int				i;
	int				count;
	signal_info *	sig;

	if (dbg_str_to_int(args, nargs, &count) < 0) {
	}

	*lst = NewList();

	for (i = 0; i < count; i++) {
		if (dbg_str_to_signalinfo(args, nargs, &sig) < 0) {
			DestroyList(*lst, FreeMemory);
			return -1;
		}
		AddToList(*lst, (void *)sig);
	}

	return 0;
}

static int
dbg_str_to_list(char ***args, int *nargs, List **lst)
{
	int		i;
	int		count;
	char *	str;

	if (dbg_str_to_int(args, nargs, &count) < 0) {
		return -1;
	}

	*lst = NewList();

	for (i = 0; i < count; i++) {
		if (dbg_copy_str(args, nargs, &str) < 0) {
			DestroyList(*lst, free);
			return -1;
		}
		AddToList(*lst, (void *)str);
	}

	return 0;
}

static int
dbg_str_to_aif(char ***args, int *nargs, AIF **res)
{
	int		data_len;
	AIF *	a;
	char *	fmt;
	char *	data;

	if (dbg_copy_str(args, nargs, &fmt) < 0 ||
		dbg_str_to_data(args, nargs, &data, &data_len) < 0) {
			return -1;
	}

	a = NewAIF(0, 0);
	AIF_FORMAT(a) = fmt;
	AIF_DATA(a) = data;
	AIF_LEN(a) = data_len;

	*res = a;

	return 0;
}

static int
dbg_str_to_memory_data(char ***args, int *nargs, List **lst)
{
	int		i;
	int		count;
	char *	str;

	if (dbg_str_to_int(args, nargs, &count) < 0) {
		return -1;
	}

	*lst = NewList();

	for (i = 0; i < count; i++) {
		if (dbg_copy_str(args, nargs, &str) < 0) {
			DestroyList(*lst, free);
			return -1;
		}
		AddToList(*lst, (void *)str);
	}

	return 0;
}


static int
dbg_str_to_memory(char ***args, int *nargs, List **lst)
{
	int			i;
	int			count;
	memory *	m;

	if (dbg_str_to_int(args, nargs, &count) < 0) {
	}

	*lst = NewList();

	for (i = 0; i < count; i++) {
		m = NewMemory();
		if (dbg_copy_str(args, nargs, &m->addr) < 0 ||
			dbg_copy_str(args, nargs, &m->ascii) < 0 ||
			dbg_str_to_memory_data(args, nargs, &m->data) < 0) {
				DestroyList(*lst, FreeMemory);
				return -1;
		}
		AddToList(*lst, (void *)m);
	}

	return 0;
}

static int
dbg_str_to_memoryinfo(char ***args, int *nargs, memoryinfo **info)
{
	memoryinfo * meminfo;

	if (*nargs < 1)
		return -1;

	if (strcmp(*(*args), EMPTY) == 0) {
		(*args)++;
		(*nargs)--;
		*info = NULL;
	}

	meminfo = NewMemoryInfo();

	if (dbg_copy_str(args, nargs, &meminfo->addr) < 0 ||
		dbg_str_to_int(args, nargs, (int *)&meminfo->nextRow) < 0 ||
		dbg_str_to_int(args, nargs, (int *)&meminfo->prevRow) < 0 ||
		dbg_str_to_int(args, nargs, (int *)&meminfo->nextPage) < 0 ||
		dbg_str_to_int(args, nargs, (int *)&meminfo->prevPage) < 0 ||
		dbg_str_to_int(args, nargs, (int *)&meminfo->numBytes) < 0 ||
		dbg_str_to_int(args, nargs, (int *)&meminfo->totalBytes) < 0 ||
		dbg_str_to_memory(args, nargs, &meminfo->memories) < 0) {
			FreeMemoryInfo(meminfo);
			return -1;
	}

	*info = meminfo;

	return 0;
}

/*
 * Convert an array of strings (as a result of deserializing a proxy message)
 * into a debug event. This is used on the client end, so the event will include
 * a bitset.
 */
int
DbgDeserializeEvent(int id, int nargs, char **args, dbg_event **ev)
{
	dbg_event *	e = NULL;
	bitset *	procs = NULL;

	proxy_get_bitset(*args++, &procs);
	nargs--;

	e = NewDbgEvent(id);

	switch (id)
	{
	case DBGEV_OK:
		break;

	case DBGEV_ERROR:
		dbg_str_to_int(&args, &nargs, &e->dbg_event_u.error_event.error_code);
		dbg_copy_str(&args, &nargs, &e->dbg_event_u.error_event.error_msg);
		break;

	case DBGEV_OUTPUT:
		dbg_copy_str(&args, &nargs, &e->dbg_event_u.output);
		break;

	case DBGEV_SUSPEND:
		dbg_str_to_int(&args, &nargs, &e->dbg_event_u.suspend_event.reason);

		switch (e->dbg_event_u.suspend_event.reason) {
		case DBGEV_SUSPEND_BPHIT:
			dbg_str_to_int(&args, &nargs, &e->dbg_event_u.suspend_event.ev_u.bpid);
			dbg_str_to_int(&args, &nargs, &e->dbg_event_u.suspend_event.thread_id);
			dbg_str_to_int(&args, &nargs, &e->dbg_event_u.suspend_event.depth);
			dbg_str_to_list(&args, &nargs, &e->dbg_event_u.suspend_event.changed_vars);
			break;

		case DBGEV_SUSPEND_SIGNAL:
			dbg_str_to_signalinfo(&args, &nargs, &e->dbg_event_u.suspend_event.ev_u.sig);
			dbg_str_to_stackframe(&args, &nargs, &e->dbg_event_u.suspend_event.frame);
			dbg_str_to_int(&args, &nargs, &e->dbg_event_u.suspend_event.thread_id);
			dbg_str_to_int(&args, &nargs, &e->dbg_event_u.suspend_event.depth);
			dbg_str_to_list(&args, &nargs, &e->dbg_event_u.suspend_event.changed_vars);
			break;

		case DBGEV_SUSPEND_STEP:
		case DBGEV_SUSPEND_INT:
			dbg_str_to_stackframe(&args, &nargs, &e->dbg_event_u.suspend_event.frame);
			dbg_str_to_int(&args, &nargs, &e->dbg_event_u.suspend_event.thread_id);
			dbg_str_to_int(&args, &nargs, &e->dbg_event_u.suspend_event.depth);
			dbg_str_to_list(&args, &nargs, &e->dbg_event_u.suspend_event.changed_vars);
			break;

		default:
			goto error_out;
		}

		break;

	case DBGEV_BPSET:
		dbg_str_to_int(&args, &nargs, &e->dbg_event_u.bpset_event.bpid);
		dbg_str_to_breakpoint(&args, &nargs, &e->dbg_event_u.bpset_event.bp);
		break;

	case DBGEV_SIGNALS:
		dbg_str_to_signals(&args, &nargs, &e->dbg_event_u.list);
		break;

	case DBGEV_EXIT:
		dbg_str_to_int(&args, &nargs, &e->dbg_event_u.exit_event.reason);

		switch (e->dbg_event_u.exit_event.reason) {
		case DBGEV_EXIT_NORMAL:
			dbg_str_to_int(&args, &nargs, &e->dbg_event_u.exit_event.ev_u.exit_status);
			break;

		case DBGEV_EXIT_SIGNAL:
			dbg_str_to_signalinfo(&args, &nargs, &e->dbg_event_u.exit_event.ev_u.sig);
			break;

		default:
			goto error_out;
		}

		break;

	case DBGEV_FRAMES:
		dbg_str_to_stackframes(&args, &nargs, &e->dbg_event_u.list);
		break;

	case DBGEV_THREAD_SELECT:
		dbg_str_to_int(&args, &nargs, &e->dbg_event_u.thread_select_event.thread_id);
		dbg_str_to_stackframe(&args, &nargs, &e->dbg_event_u.thread_select_event.frame);
		break;

	case DBGEV_THREADS:
		dbg_str_to_int(&args, &nargs, &e->dbg_event_u.threads_event.thread_id);
		dbg_str_to_list(&args, &nargs, &e->dbg_event_u.threads_event.list);
		break;

	case DBGEV_STACK_DEPTH:
		dbg_str_to_int(&args, &nargs, &e->dbg_event_u.stack_depth);
		break;

	case DBGEV_DATAR_MEM:
		dbg_str_to_memoryinfo(&args, &nargs, &e->dbg_event_u.meminfo);
		break;

	case DBGEV_VARS:
		dbg_str_to_list(&args, &nargs, &e->dbg_event_u.list);
		break;

	case DBGEV_ARGS:
		dbg_str_to_list(&args,&nargs,  &e->dbg_event_u.list);
		break;

	case DBGEV_TYPE:
		dbg_copy_str(&args, &nargs, &e->dbg_event_u.type_desc);
		break;

	case DBGEV_DATA:
		dbg_str_to_aif(&args, &nargs, &e->dbg_event_u.data_event.data);
		dbg_copy_str(&args, &nargs, &e->dbg_event_u.data_event.type_desc);
		dbg_copy_str(&args, &nargs, &e->dbg_event_u.data_event.name);
		break;

	default:
		goto error_out;
	}

	e->procs = procs;
	*ev = e;

	return 0;

error_out:

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

	e->event_id = event;

	return e;
}

void
FreeDbgEvent(dbg_event *e) {
	switch (e->event_id) {
	case DBGEV_OK:
		break;

	case DBGEV_OUTPUT:
		free(e->dbg_event_u.output);
		break;

	case DBGEV_SUSPEND:
		switch (e->dbg_event_u.suspend_event.reason) {
		case DBGEV_SUSPEND_SIGNAL:
			if (e->dbg_event_u.suspend_event.ev_u.sig != NULL) {
				FreeSignalInfo(e->dbg_event_u.suspend_event.ev_u.sig);
			}
			break;
		case DBGEV_SUSPEND_INT:
		case DBGEV_SUSPEND_STEP:
			break;

		case DBGEV_SUSPEND_BPHIT:
			break;
		}

		if (e->dbg_event_u.suspend_event.frame != NULL) {
			FreeStackframe(e->dbg_event_u.suspend_event.frame);
		}
		if (e->dbg_event_u.suspend_event.changed_vars != NULL) {
			DestroyList(e->dbg_event_u.suspend_event.changed_vars, free);
		}
		break;

	case DBGEV_EXIT:
		switch (e->dbg_event_u.suspend_event.reason) {
			case DBGEV_EXIT_SIGNAL:
				if (e->dbg_event_u.suspend_event.ev_u.sig != NULL) {
					FreeSignalInfo(e->dbg_event_u.suspend_event.ev_u.sig);
				}
				break;
			case DBGEV_EXIT_NORMAL:
				break;
		}
		break;

	case DBGEV_FRAMES:
		if (e->dbg_event_u.list != NULL) {
			DestroyList(e->dbg_event_u.list, FreeStackframe);
		}
		break;

	case DBGEV_DATA:
		if (e->dbg_event_u.data_event.data != NULL) {
			AIFFree(e->dbg_event_u.data_event.data);
		}
		if (e->dbg_event_u.data_event.type_desc != NULL) {
			free(e->dbg_event_u.data_event.type_desc);
		}
		if (e->dbg_event_u.data_event.name != NULL) {
			free(e->dbg_event_u.data_event.name);
		}
		break;

	case DBGEV_TYPE:
		if (e->dbg_event_u.type_desc != NULL) {
			free(e->dbg_event_u.type_desc);
		}
		break;

	case DBGEV_THREAD_SELECT:
		if (e->dbg_event_u.thread_select_event.frame != NULL) {
			FreeStackframe(e->dbg_event_u.thread_select_event.frame);
		}
		break;

	case DBGEV_THREADS:
		if (e->dbg_event_u.threads_event.list != NULL) {
			DestroyList(e->dbg_event_u.threads_event.list, free);
		}
		break;

	case DBGEV_DATAR_MEM:
		if (e->dbg_event_u.meminfo != NULL) {
			FreeMemoryInfo(e->dbg_event_u.meminfo);
		}
		break;

	case DBGEV_ARGS:
	case DBGEV_VARS:
		if (e->dbg_event_u.list != NULL) {
			DestroyList(e->dbg_event_u.list, free);
		}
		break;

	case DBGEV_SIGNALS:
		if (e->dbg_event_u.list != NULL) {
			DestroyList(e->dbg_event_u.list, FreeSignalInfo);
		}
		break;

	case DBGEV_BPSET:
		if (e->dbg_event_u.bpset_event.bp != NULL) {
			FreeBreakpoint(e->dbg_event_u.bpset_event.bp);
		}
		break;

	case DBGEV_ERROR:
		if (e->dbg_event_u.error_event.error_msg != NULL) {
			free(e->dbg_event_u.error_event.error_msg);
		}
	}

	if (e->procs != NULL) {
		bitset_free(e->procs);
	}

	free(e);
}

dbg_event *
DbgErrorEvent(int err, char *msg)
{
	dbg_event *	e = NewDbgEvent(DBGEV_ERROR);

	e->dbg_event_u.error_event.error_code = err;
	if (msg != NULL) {
		e->dbg_event_u.error_event.error_msg = strdup(msg);
	}

	return e;
}
