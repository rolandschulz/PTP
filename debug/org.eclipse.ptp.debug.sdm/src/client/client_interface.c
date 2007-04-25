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

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <errno.h>

#include "dbg.h"
#include "dbg_proxy.h"
#include "session.h"
#include "proxy.h"
#include "proxy_event.h"
#include "bitset.h"
#include "handler.h"

/*
 * The TIMEOUT determines how often the debugger polls progress routines.
 * Too often and it uses too much CPU, too few and things will slow down...
 */
#define CLIENT_TIMEOUT	50000
#define SERVER_TIMEOUT	50000

static void session_event_handler(void *, void *);

static proxy_clnt_helper_funcs clnt_helper_funcs = {
	session_event_handler,
	NULL
};

static void
session_event_handler(void *event, void *data)
{
	dbg_event *	ev;
	proxy_msg *	msg = (proxy_msg *)event;
	session *	s = (session *)data;
	
	if (DbgDeserializeEvent(msg->msg_id, msg->num_args, msg->args, &ev) < 0)
		return;
	
	if (s->sess_event_handler != NULL)
		s->sess_event_handler(ev, s->sess_event_data);
}

/*
 * Session initialization
 */
int
DbgInit(session **sess, char *name, char *attr, ...)
{
	va_list			ap;
	int				res;
	session *		s;
	struct timeval	tv = {0, CLIENT_TIMEOUT};
	
	s = (session *)malloc(sizeof(session));
	
	clnt_helper_funcs.eventdata = (void *)s;
	
	va_start(ap, attr);
	res = proxy_clnt_init(name, &tv, &clnt_helper_funcs, &s->sess_proxy, attr, ap);
	va_end(ap);
	
	if (res < 0) {
		free(s);
		return -1;
	}
	
	s->sess_event_handler = NULL;
	*sess = s;
	
	return 0;
}

int
DbgConnect(session *s)
{
	return proxy_clnt_connect(s->sess_proxy);
}

int
DbgCreate(session *s)
{
	return proxy_clnt_create(s->sess_proxy);
}

int
DbgStartSession(session *s, char *dir, char *prog, char *args)
{
	proxy_msg *	msg = new_proxy_msg(DBG_STARTSESSION_CMD, 0);
	
	proxy_msg_add_string(msg, dir);
	proxy_msg_add_string(msg, prog);
	proxy_msg_add_string(msg, args);
	
	proxy_clnt_queue_msg(s->sess_proxy, msg);
	
	free_proxy_msg(msg);
	
	return 0;
}

/*
 * Breakpoint operations
 */
int 
DbgSetLineBreakpoint(session *s, bitset *set, int bpid, char *file, int line)
{
	proxy_msg *	msg = new_proxy_msg(DBG_SETLINEBREAKPOINT_CMD, 0);

	proxy_msg_add_bitset(msg, set);
	proxy_msg_add_int(msg, bpid);
	proxy_msg_add_string(msg, file);
	proxy_msg_add_int(msg, line);

	proxy_clnt_queue_msg(s->sess_proxy, msg);
	
	free_proxy_msg(msg);
	
	return 0;
}

int 
DbgSetFuncBreakpoint(session *s, bitset *set, int bpid, char *file, char *func)
{
	proxy_msg *	msg = new_proxy_msg(DBG_SETFUNCBREAKPOINT_CMD, 0);

	proxy_msg_add_bitset(msg, set);
	proxy_msg_add_int(msg, bpid);
	proxy_msg_add_string(msg, file);
	proxy_msg_add_string(msg, func);

	proxy_clnt_queue_msg(s->sess_proxy, msg);
	
	free_proxy_msg(msg);
	
	return 0;
}

int 
DbgDeleteBreakpoint(session *s, bitset *set, int bpid)
{
	proxy_msg *	msg = new_proxy_msg(DBG_DELETEBREAKPOINT_CMD, 0);
	
	proxy_msg_add_bitset(msg, set);
	proxy_msg_add_int(msg, bpid);
	
	proxy_clnt_queue_msg(s->sess_proxy, msg);
	
	free_proxy_msg(msg);
	
	return 0;
}

int 
DbgEnableBreakpoint(session *s, bitset *set, int bpid)
{
	proxy_msg *	msg = new_proxy_msg(DBG_ENABLEBREAKPOINT_CMD, 0);
	
	proxy_msg_add_bitset(msg, set);
	proxy_msg_add_int(msg, bpid);

	proxy_clnt_queue_msg(s->sess_proxy, msg);
	
	free_proxy_msg(msg);
	
	return 0;
}

int 
DbgDisableBreakpoint(session *s, bitset *set, int bpid)
{
	proxy_msg *	msg = new_proxy_msg(DBG_DISABLEBREAKPOINT_CMD, 0);
	
	proxy_msg_add_bitset(msg, set);
	proxy_msg_add_int(msg, bpid);

	proxy_clnt_queue_msg(s->sess_proxy, msg);
	
	free_proxy_msg(msg);
	
	return 0;
}

int 
DbgConditionBreakpoint(session *s, bitset *set, int bpid, char *expr)
{
	proxy_msg *	msg = new_proxy_msg(DBG_CONDITIONBREAKPOINT_CMD, 0);
	
	proxy_msg_add_bitset(msg, set);
	proxy_msg_add_int(msg, bpid);
	proxy_msg_add_string(msg, expr);

	proxy_clnt_queue_msg(s->sess_proxy, msg);
	
	free_proxy_msg(msg);
	
	return 0;
}

int 
DbgBreakpointAfter(session *s, bitset *set, int bpid, int icount)
{
	proxy_msg *	msg = new_proxy_msg(DBG_BREAKPOINTAFTER_CMD, 0);
	
	proxy_msg_add_bitset(msg, set);
	proxy_msg_add_int(msg, bpid);
	proxy_msg_add_int(msg, icount);

	proxy_clnt_queue_msg(s->sess_proxy, msg);
	
	free_proxy_msg(msg);
	
	return 0;
}

int 
DbgSetWatchpoint(session *s, bitset *set, int bpid, char *expr, int access, int read, char *condition, int icount)
{
	proxy_msg *	msg = new_proxy_msg(DBG_SETWATCHPOINT_CMD, 0);

	proxy_msg_add_bitset(msg, set);
	proxy_msg_add_int(msg, bpid);
	proxy_msg_add_string(msg, expr);
	proxy_msg_add_int(msg, access);
	proxy_msg_add_int(msg, read);
	proxy_msg_add_string(msg, condition);
	proxy_msg_add_int(msg, icount);
	
	proxy_clnt_queue_msg(s->sess_proxy, msg);
	
	free_proxy_msg(msg);
	
	return 0;
}
/*
 * Process control operations
 */
int 
DbgGo(session *s, bitset *set)
{
	proxy_msg *	msg = new_proxy_msg(DBG_GO_CMD, 0);

	proxy_msg_add_bitset(msg, set);

	proxy_clnt_queue_msg(s->sess_proxy, msg);
	
	free_proxy_msg(msg);
	
	return 0;
}

int 
DbgStep(session *s, bitset *set, int count, int type)
{
	proxy_msg *	msg = new_proxy_msg(DBG_STEP_CMD, 0);
	
	proxy_msg_add_bitset(msg, set);
	proxy_msg_add_int(msg, count);
	proxy_msg_add_int(msg, type);

	proxy_clnt_queue_msg(s->sess_proxy, msg);
	
	free_proxy_msg(msg);
	
	return 0;
}

int 
DbgTerminate(session *s, bitset *set)
{
	proxy_msg *	msg = new_proxy_msg(DBG_TERMINATE_CMD, 0);

	proxy_msg_add_bitset(msg, set);

	proxy_clnt_queue_msg(s->sess_proxy, msg);
	
	free_proxy_msg(msg);
	
	return 0;
}

int 
DbgSuspend(session *s, bitset *set)
{
	proxy_msg *	msg = new_proxy_msg(DBG_SUSPEND_CMD, 0);

	proxy_msg_add_bitset(msg, set);

	proxy_clnt_queue_msg(s->sess_proxy, msg);
	
	free_proxy_msg(msg);
	
	return 0;
}
/*
 * Stack frame operations
 */
int 
DbgListStackframes(session *s, bitset *set, int low, int depth)
{
	proxy_msg *	msg = new_proxy_msg(DBG_LISTSTACKFRAMES_CMD, 0);
	
	proxy_msg_add_bitset(msg, set);
	proxy_msg_add_int(msg, low);
	proxy_msg_add_int(msg, depth);

	proxy_clnt_queue_msg(s->sess_proxy, msg);
	
	free_proxy_msg(msg);
	
	return 0;
}

int 
DbgSetCurrentStackframe(session *s, bitset *set, int level)
{
	proxy_msg *	msg = new_proxy_msg(DBG_SETCURRENTSTACKFRAME_CMD, 0);
	
	proxy_msg_add_bitset(msg, set);
	proxy_msg_add_int(msg, level);

	proxy_clnt_queue_msg(s->sess_proxy, msg);
	
	free_proxy_msg(msg);
	
	return 0;
}

/*
 * Expression/variable operations
 */
int 
DbgEvaluateExpression(session *s, bitset *set, char *exp)
{
	proxy_msg *	msg = new_proxy_msg(DBG_EVALUATEEXPRESSION_CMD, 0);

	proxy_msg_add_bitset(msg, set);
	proxy_msg_add_string(msg, exp);

	proxy_clnt_queue_msg(s->sess_proxy, msg);
	
	free_proxy_msg(msg);
	
	return 0;
}

int 
DbgGetType(session *s, bitset *set, char *exp)
{
	proxy_msg *	msg = new_proxy_msg(DBG_GETTYPE_CMD, 0);

	proxy_msg_add_bitset(msg, set);
	proxy_msg_add_string(msg, exp);

	proxy_clnt_queue_msg(s->sess_proxy, msg);
	
	free_proxy_msg(msg);
	
	return 0;
}

int 
DbgListLocalVariables(session *s, bitset *set)
{
	proxy_msg *	msg = new_proxy_msg(DBG_LISTLOCALVARIABLES_CMD, 0);

	proxy_msg_add_bitset(msg, set);

	proxy_clnt_queue_msg(s->sess_proxy, msg);
	
	free_proxy_msg(msg);
	
	return 0;
}

int 
DbgListArguments(session *s, bitset *set, int low, int high)
{
	proxy_msg *	msg = new_proxy_msg(DBG_LISTARGUMENTS_CMD, 0);

	proxy_msg_add_bitset(msg, set);
	proxy_msg_add_int(msg, low);
	proxy_msg_add_int(msg, high);

	proxy_clnt_queue_msg(s->sess_proxy, msg);
	
	free_proxy_msg(msg);
	
	return 0;
}

int 
DbgListGlobalVariables(session *s, bitset *set)
{
	proxy_msg *	msg = new_proxy_msg(DBG_LISTGLOBALVARIABLES_CMD, 0);

	proxy_msg_add_bitset(msg, set);

	proxy_clnt_queue_msg(s->sess_proxy, msg);
	
	free_proxy_msg(msg);
	
	return 0;
}

int
DbgListInfoThreads(session *s, bitset *set) 
{
	proxy_msg *	msg = new_proxy_msg(DBG_LISTINFOTHREADS_CMD, 0);

	proxy_msg_add_bitset(msg, set);

	proxy_clnt_queue_msg(s->sess_proxy, msg);
	
	free_proxy_msg(msg);
	
	return 0;
}

int 
DbgSetThreadSelect(session *s, bitset *set, int threadNum) 
{
	proxy_msg *	msg = new_proxy_msg(DBG_SETTHREADSELECT_CMD, 0);

	proxy_msg_add_bitset(msg, set);
	proxy_msg_add_int(msg, threadNum);

	proxy_clnt_queue_msg(s->sess_proxy, msg);
	
	free_proxy_msg(msg);
	
	return 0;
}

int 
DbgStackInfoDepth(session *s, bitset *set) 
{
	proxy_msg *	msg = new_proxy_msg(DBG_STACKINFODEPTH_CMD, 0);

	proxy_msg_add_bitset(msg, set);

	proxy_clnt_queue_msg(s->sess_proxy, msg);
	
	free_proxy_msg(msg);
	
	return 0;
}

int 
DbgDataReadMemory(session *s, bitset *set, long offset, char *address, char *format, int wordSize, int rows, int cols, char *asChar) 
{
	proxy_msg *	msg = new_proxy_msg(DBG_DATAREADMEMORY_CMD, 0);

	proxy_msg_add_bitset(msg, set);
	proxy_msg_add_int(msg, offset);
	proxy_msg_add_string(msg, address);
	proxy_msg_add_string(msg, format);
	proxy_msg_add_int(msg, wordSize);
	proxy_msg_add_int(msg, rows);
	proxy_msg_add_int(msg, cols);
	proxy_msg_add_string(msg, asChar);

	proxy_clnt_queue_msg(s->sess_proxy, msg);
	
	free_proxy_msg(msg);
	
	return 0;
}

int 
DbgDataWriteMemory(session *s, bitset *set, long offset, char *address, char *format, int wordSize, char *value) 
{
	proxy_msg *	msg = new_proxy_msg(DBG_DATAWRITEMEMORY_CMD, 0);

	proxy_msg_add_bitset(msg, set);
	proxy_msg_add_int(msg, offset);
	proxy_msg_add_string(msg, address);
	proxy_msg_add_string(msg, format);
	proxy_msg_add_int(msg, wordSize);
	proxy_msg_add_string(msg, value);

	proxy_clnt_queue_msg(s->sess_proxy, msg);
	
	free_proxy_msg(msg);
	
	return 0;
}

int
DbgListSignals(session *s, bitset *set, char *name)
{
	proxy_msg *	msg = new_proxy_msg(DBG_LISTSIGNALS_CMD, 0);

	proxy_msg_add_bitset(msg, set);
	proxy_msg_add_string(msg, name);

	proxy_clnt_queue_msg(s->sess_proxy, msg);
	
	free_proxy_msg(msg);
	
	return 0;
}

int
DbgSignalInfo(session *s, bitset *set, char *arg)
{
	proxy_msg *	msg = new_proxy_msg(DBG_SIGNALINFO_CMD, 0);

	proxy_msg_add_bitset(msg, set);
	proxy_msg_add_string(msg, arg);

	proxy_clnt_queue_msg(s->sess_proxy, msg);
	
	free_proxy_msg(msg);
	
	return 0;
}

int
DbgCLIHandle(session *s, bitset *set, char *arg)
{
	proxy_msg *	msg = new_proxy_msg(DBG_CLIHANDLE_CMD, 0);

	proxy_msg_add_bitset(msg, set);
	proxy_msg_add_string(msg, arg);

	proxy_clnt_queue_msg(s->sess_proxy, msg);
	
	free_proxy_msg(msg);
	
	return 0;
}

int
DbgQuit(session *s)
{
	proxy_msg *	msg = new_proxy_msg(DBG_QUIT_CMD, 0);

	proxy_clnt_queue_msg(s->sess_proxy, msg);
	
	free_proxy_msg(msg);
	
	return 0;

}

int
DbgDataEvaluateExpression(session *s, bitset *set, char *arg)
{
	proxy_msg *	msg = new_proxy_msg(DBG_DATAEVALUATEEXPRESSION_CMD, 0);

	proxy_msg_add_bitset(msg, set);
	proxy_msg_add_string(msg, arg);

	proxy_clnt_queue_msg(s->sess_proxy, msg);
	
	free_proxy_msg(msg);
	
	return 0;
}
int
DbgGetPartialAIF(session *s, bitset *set, char *name, char *key, int listChildren, int express)
{
	proxy_msg *	msg = new_proxy_msg(DBG_GETPARTIALAIF_CMD, 0);

	proxy_msg_add_bitset(msg, set);
	proxy_msg_add_string(msg, name);
	proxy_msg_add_string(msg, key);
	proxy_msg_add_int(msg, listChildren);
	proxy_msg_add_int(msg, express);

	proxy_clnt_queue_msg(s->sess_proxy, msg);
	
	free_proxy_msg(msg);
	
	return 0;
}
int
DbgVariableDelete(session *s, bitset *set, char *arg)
{
	proxy_msg *	msg = new_proxy_msg(DBG_VARIABLEDELETE_CMD, 0);

	proxy_msg_add_bitset(msg, set);
	proxy_msg_add_string(msg, arg);

	proxy_clnt_queue_msg(s->sess_proxy, msg);
	
	free_proxy_msg(msg);
	
	return 0;
}

/*
 * Event handling
 */
int
DbgProgress(session *s)
{
	return proxy_clnt_progress(s->sess_proxy);
}

void
DbgRegisterEventHandler(session *s, void (*event_callback)(dbg_event *, void *), void *data)
{
	s->sess_event_handler = event_callback;
	s->sess_event_data = data;
	RegisterEventHandler(PROXY_EVENT_HANDLER, session_event_handler, (void *)s);
	//s->sess_proxy->clnt_funcs->regeventhandler(s->sess_proxy_data, session_event_handler, (void *)s);
}

/**
 * Unregister file descriptor handler
 */
void
DbgUnregisterEventHandler(session *s, void (*event_callback)(dbg_event *, void *))
{
	s->sess_event_handler = NULL;
	UnregisterEventHandler(PROXY_EVENT_HANDLER, session_event_handler);
}

/**
 * Register a handler for file descriptor events.
 */
void
DbgRegisterReadFileHandler(session *s, int fd, int (*file_handler)(int, void *), void *data)
{
	RegisterFileHandler(fd, READ_FILE_HANDLER, file_handler, data);
}

void
DbgRegisterWriteFileHandler(session *s, int fd, int (*file_handler)(int, void *), void *data)
{
	RegisterFileHandler(fd, WRITE_FILE_HANDLER, file_handler, data);
}

void
DbgRegisterExceptFileHandler(session *s, int fd, int (*file_handler)(int, void *), void *data)
{
	RegisterFileHandler(fd, EXCEPT_FILE_HANDLER, file_handler, data);
}

/**
 * Unregister file descriptor handler
 */
void
DbgUnregisterFileHandler(session *s, int fd)
{
	UnregisterFileHandler(fd);
}
