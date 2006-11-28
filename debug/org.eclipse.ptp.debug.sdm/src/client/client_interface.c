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

static proxy_handler_funcs handler_funcs = {
	RegisterFileHandler,
	UnregisterFileHandler,
	RegisterEventHandler,
	CallEventHandlers
};

static proxy_clnt_helper_funcs clnt_helper_funcs = {
	session_event_handler,
	NULL
};

/*
 * Intercept INIT events to obtain number of procs
 */
static void
session_event_handler(void *event, void *data)
{
	dbg_event *	de;
	proxy_event *pe = (proxy_event *)event;
	session *	s = (session *)data;
	
	switch (pe->event) {
	case PROXY_EV_OK:
		if (DbgStrToEvent(pe->event_data, &de) < 0) {
			de = NewDbgEvent(DBGEV_ERROR);
			de->dbg_event_u.error_event.error_code = DBGERR_PROXY_PROTO;
			de->dbg_event_u.error_event.error_msg = strdup("");
		}
		break;

	case PROXY_EV_ERROR:
		de = NewDbgEvent(DBGEV_ERROR);
		
		switch (pe->error_code) {
		case PROXY_ERR_CLIENT:
			de->dbg_event_u.error_event.error_code = DBGERR_DEBUGGER;
			de->dbg_event_u.error_event.error_msg = strdup(pe->error_msg);
			break;
			
		case PROXY_ERR_SYSTEM:
			de->dbg_event_u.error_event.error_code = DBGERR_SYSTEM;
			de->dbg_event_u.error_event.error_msg = strdup(pe->error_msg);
			break;
		}
		break;

	case PROXY_EV_CONNECTED:
		de = NewDbgEvent(DBGEV_OK);
		break;
	}
	
	if (s->sess_event_handler != NULL)
		s->sess_event_handler(de, s->sess_event_data);
}

/*
 * Session initialization
 */
int
DbgInit(session **sess, char *name, char *attr, ...)
{
	va_list		ap;
	int			res;
	session *	s;
	
	s = (session *)malloc(sizeof(session));
	
	clnt_helper_funcs.eventdata = (void *)s;
	
	va_start(ap, attr);
	res = proxy_clnt_init(name, &handler_funcs, &clnt_helper_funcs, &s->sess_proxy, attr, ap);
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
	return proxy_clnt_sendcmd(s->sess_proxy, DBG_STARTSESSION_CMD, DBG_STARTSESSION_FMT, SERVER_TIMEOUT, dir, prog, args);
}

/*
 * Breakpoint operations
 */
int 
DbgSetLineBreakpoint(session *s, bitset *set, int bpid, char *file, int line)
{
	int		res;
	char *	set_str = bitset_to_str(set);

	res = proxy_clnt_sendcmd(s->sess_proxy, DBG_SETLINEBREAKPOINT_CMD, DBG_SETLINEBREAKPOINT_FMT, set_str, bpid, file, line);
	free(set_str);
	return res;
}

int 
DbgSetFuncBreakpoint(session *s, bitset *set, int bpid, char *file, char *func)
{
	int		res;
	char *	set_str = bitset_to_str(set);

	res = proxy_clnt_sendcmd(s->sess_proxy, DBG_SETFUNCBREAKPOINT_CMD, DBG_SETFUNCBREAKPOINT_FMT, set_str, bpid, file, func);
	free(set_str);
	return res;
}

int 
DbgDeleteBreakpoint(session *s, bitset *set, int bpid)
{
	int		res;
	char *	set_str = bitset_to_str(set);
	
	res = proxy_clnt_sendcmd(s->sess_proxy, DBG_DELETEBREAKPOINT_CMD, DBG_DELETEBREAKPOINT_FMT, set_str, bpid);
	free(set_str);
	return res;
}

int 
DbgEnableBreakpoint(session *s, bitset *set, int bpid)
{
	int		res;
	char *	set_str = bitset_to_str(set);
	
	res = proxy_clnt_sendcmd(s->sess_proxy, DBG_ENABLEBREAKPOINT_CMD, DBG_ENABLEBREAKPOINT_FMT, set_str, bpid);
	free(set_str);
	return res;
}

int 
DbgDisableBreakpoint(session *s, bitset *set, int bpid)
{
	int		res;
	char *	set_str = bitset_to_str(set);
	
	res = proxy_clnt_sendcmd(s->sess_proxy, DBG_DISABLEBREAKPOINT_CMD, DBG_DISABLEBREAKPOINT_FMT, set_str, bpid);
	free(set_str);
	return res;
}

int 
DbgConditionBreakpoint(session *s, bitset *set, int bpid, char *expr)
{
	int		res;
	char *	set_str = bitset_to_str(set);
	
	res = proxy_clnt_sendcmd(s->sess_proxy, DBG_CONDITIONBREAKPOINT_CMD, DBG_CONDITIONBREAKPOINT_FMT, set_str, bpid, expr);
	free(set_str);
	return res;
}

int 
DbgBreakpointAfter(session *s, bitset *set, int bpid, int icount)
{
	int		res;
	char *	set_str = bitset_to_str(set);
	
	res = proxy_clnt_sendcmd(s->sess_proxy, DBG_BREAKPOINTAFTER_CMD, DBG_BREAKPOINTAFTER_FMT, set_str, bpid, icount);
	free(set_str);
	return res;
}

int 
DbgSetWatchpoint(session *s, bitset *set, int bpid, char *expr, int access, int read, char *condition, int icount)
{
	int		res;
	char *	set_str = bitset_to_str(set);

	res = proxy_clnt_sendcmd(s->sess_proxy, DBG_SETWATCHPOINT_CMD, DBG_SETWATCHPOINT_FMT, set_str, bpid, expr, access, read, condition, icount);
	free(set_str);
	return res;
}
/*
 * Process control operations
 */
int 
DbgGo(session *s, bitset *set)
{
	int		res;
	char *	set_str = bitset_to_str(set);
	res = proxy_clnt_sendcmd(s->sess_proxy, DBG_GO_CMD, DBG_GO_FMT, set_str);
	free(set_str);
	return res;
}

int 
DbgStep(session *s, bitset *set, int count, int type)
{
	int		res;
	char *	set_str = bitset_to_str(set);
	
	res = proxy_clnt_sendcmd(s->sess_proxy, DBG_STEP_CMD, DBG_STEP_FMT, set_str, count, type);
	free(set_str);
	return res;
}

int 
DbgTerminate(session *s, bitset *set)
{
	int		res;
	char *	set_str = bitset_to_str(set);
	res = proxy_clnt_sendcmd(s->sess_proxy, DBG_TERMINATE_CMD, DBG_TERMINATE_FMT, set_str);
	free(set_str);
	return res;
}

int 
DbgSuspend(session *s, bitset *set)
{
	int		res;
	char *	set_str = bitset_to_str(set);
	res = proxy_clnt_sendcmd(s->sess_proxy, DBG_SUSPEND_CMD, DBG_SUSPEND_FMT, set_str);
	free(set_str);
	return res;
}
/*
 * Stack frame operations
 */
int 
DbgListStackframes(session *s, bitset *set, int low, int depth)
{
	int		res;
	char *	set_str = bitset_to_str(set);
	
	res = proxy_clnt_sendcmd(s->sess_proxy, DBG_LISTSTACKFRAMES_CMD, DBG_LISTSTACKFRAMES_FMT, set_str, low, depth);
	free(set_str);
	return res;
}

int 
DbgSetCurrentStackframe(session *s, bitset *set, int level)
{
	int		res;
	char *	set_str = bitset_to_str(set);
	
	res = proxy_clnt_sendcmd(s->sess_proxy, DBG_SETCURRENTSTACKFRAME_CMD, DBG_SETCURRENTSTACKFRAME_FMT, set_str, level);
	free(set_str);
	return res;
}

/*
 * Expression/variable operations
 */
int 
DbgEvaluateExpression(session *s, bitset *set, char *exp)
{
	int		res;
	char *	set_str = bitset_to_str(set);
	res = proxy_clnt_sendcmd(s->sess_proxy, DBG_EVALUATEEXPRESSION_CMD, DBG_EVALUATEEXPRESSION_FMT, set_str, exp);
	free(set_str);
	return res;
}

int 
DbgGetType(session *s, bitset *set, char *exp)
{
	int		res;
	char *	set_str = bitset_to_str(set);
	res = proxy_clnt_sendcmd(s->sess_proxy, DBG_GETTYPE_CMD, DBG_GETTYPE_FMT, set_str, exp);
	free(set_str);
	return res;
}

int 
DbgListLocalVariables(session *s, bitset *set)
{
	int		res;
	char *	set_str = bitset_to_str(set);
	res = proxy_clnt_sendcmd(s->sess_proxy, DBG_LISTLOCALVARIABLES_CMD, DBG_LISTLOCALVARIABLES_FMT, set_str);
	free(set_str);
	return res;
}

int 
DbgListArguments(session *s, bitset *set, int low, int high)
{
	int		res;
	char *	set_str = bitset_to_str(set);
	res = proxy_clnt_sendcmd(s->sess_proxy, DBG_LISTARGUMENTS_CMD, DBG_LISTARGUMENTS_FMT, set_str, low, high);
	free(set_str);
	return res;
}

int 
DbgListGlobalVariables(session *s, bitset *set)
{
	int		res;
	char *	set_str = bitset_to_str(set);
	res = proxy_clnt_sendcmd(s->sess_proxy, DBG_LISTGLOBALVARIABLES_CMD, DBG_LISTGLOBALVARIABLES_FMT, set_str);
	free(set_str);
	return res;
}

int
DbgListInfoThreads(session *s, bitset *set) 
{
	int		res;
	char *	set_str = bitset_to_str(set);
	res = proxy_clnt_sendcmd(s->sess_proxy, DBG_LISTINFOTHREADS_CMD, DBG_LISTINFOTHREADS_FMT, set_str);
	free(set_str);
	return res;
}

int 
DbgSetThreadSelect(session *s, bitset *set, int threadNum) 
{
	int		res;
	char *	set_str = bitset_to_str(set);
	res = proxy_clnt_sendcmd(s->sess_proxy, DBG_SETTHREADSELECT_CMD, DBG_SETTHREADSELECT_FMT, set_str, threadNum);
	free(set_str);
	return res;
}

int 
DbgStackInfoDepth(session *s, bitset *set) 
{
	int		res;
	char *	set_str = bitset_to_str(set);
	res = proxy_clnt_sendcmd(s->sess_proxy, DBG_STACKINFODEPTH_CMD, DBG_STACKINFODEPTH_FMT, set_str);
	free(set_str);
	return res;
}

int 
DbgDataReadMemory(session *s, bitset *set, long offset, char* address, char* format, int wordSize, int rows, int cols, char* asChar) 
{
	int		res;
	char *	set_str = bitset_to_str(set);
	res = proxy_clnt_sendcmd(s->sess_proxy, DBG_DATAREADMEMORY_CMD, DBG_DATAREADMEMORY_FMT, set_str, offset, address, format, wordSize, rows, cols, asChar);
	free(set_str);
	return res;
}

int 
DbgDataWriteMemory(session *s, bitset *set, long offset, char* address, char* format, int wordSize, char* value) 
{
	int		res;
	char *	set_str = bitset_to_str(set);
	res = proxy_clnt_sendcmd(s->sess_proxy, DBG_DATAWRITEMEMORY_CMD, DBG_DATAWRITEMEMORY_FMT, set_str, offset, address, format, wordSize, value);
	free(set_str);
	return res;
}

int
DbgListSignals(session *s, bitset *set, char* name)
{
	int		res;
	char *	set_str = bitset_to_str(set);
	res = proxy_clnt_sendcmd(s->sess_proxy, DBG_LISTSIGNALS_CMD, DBG_LISTSIGNALS_FMT, set_str, name);
	free(set_str);
	return res;
}
int
DbgSignalInfo(session *s, bitset *set, char* arg)
{
	int		res;
	char *	set_str = bitset_to_str(set);
	res = proxy_clnt_sendcmd(s->sess_proxy, DBG_SIGNALINFO_CMD, DBG_SIGNALINFO_FMT, set_str, arg);
	free(set_str);
	return res;
}

int
DbgCLIHandle(session *s, bitset *set, char* arg)
{
	int		res;
	char *	set_str = bitset_to_str(set);
	res = proxy_clnt_sendcmd(s->sess_proxy, DBG_CLIHANDLE_CMD, DBG_CLIHANDLE_FMT, set_str, arg);
	free(set_str);
	return res;
}

int
DbgQuit(session *s)
{
	int		res;
	res = proxy_clnt_sendcmd(s->sess_proxy, DBG_QUIT_CMD, NULL);
	return res;

}

int
DbgDataEvaluateExpression(session *s, bitset *set, char* arg)
{
	int		res;
	char *	set_str = bitset_to_str(set);
	res = proxy_clnt_sendcmd(s->sess_proxy, DBG_DATAEVALUATEEXPRESSION_CMD, DBG_DATAEVALUATEEXPRESSION_FMT, set_str, arg);
	free(set_str);
	return res;
}
int
DbgGetPartialAIF(session *s, bitset *set, char* arg, int listChildren, int express)
{
	int		res;
	char *	set_str = bitset_to_str(set);
	res = proxy_clnt_sendcmd(s->sess_proxy, DBG_GETPARTIALAIF_CMD, DBG_GETPARTIALAIF_FMT, set_str, arg, listChildren, express);
	free(set_str);
	return res;
}
int
DbgVariableDelete(session *s, bitset *set, char* arg)
{
	int		res;
	char *	set_str = bitset_to_str(set);
	res = proxy_clnt_sendcmd(s->sess_proxy, DBG_VARIABLEDELETE_CMD, DBG_VARIABLEDELETE_FMT, set_str, arg);
	free(set_str);
	return res;
}

/*
 * Event handling
 */
int
DbgProgress(session *s)
{
	fd_set			rfds;
	fd_set			wfds;
	fd_set			efds;
	int				res;
	int				nfds = 0;
	struct timeval	tv = { 0, CLIENT_TIMEOUT };
	handler *		h;

	/*
	 * Set up fd sets
	 */
	FD_ZERO(&rfds);
	FD_ZERO(&wfds);
	FD_ZERO(&efds);
	
	for (SetHandler(); (h = GetHandler()) != NULL; ) {
		if (h->htype == HANDLER_FILE) {
			if (h->file_type & READ_FILE_HANDLER)
				FD_SET(h->fd, &rfds);
			if (h->file_type & WRITE_FILE_HANDLER)
				FD_SET(h->fd, &wfds);
			if (h->file_type & EXCEPT_FILE_HANDLER)
				FD_SET(h->fd, &efds);
			if (h->fd > nfds)
				nfds = h->fd;
		}
	}
	
	for ( ;; ) {
		res = select(nfds+1, &rfds, &wfds, &efds, &tv);
	
		switch (res) {
		case INVALID_SOCKET:
			if ( errno == EINTR )
				continue;
		
			DbgSetError(DBGERR_SYSTEM, strerror(errno));
			return DBGRES_ERR;
		
		case 0:
			/*
			 * Timeout.
			 */
			 break;
			 		
		default:
			for (SetHandler(); (h = GetHandler()) != NULL; ) {
				if (h->htype == HANDLER_FILE
					&& ((h->file_type & READ_FILE_HANDLER && FD_ISSET(h->fd, &rfds))
						|| (h->file_type & WRITE_FILE_HANDLER && FD_ISSET(h->fd, &wfds))
						|| (h->file_type & EXCEPT_FILE_HANDLER && FD_ISSET(h->fd, &efds)))
					&& h->file_handler(h->fd, h->data) < 0) {
						printf("warning: file handler for %d returns bad\n", h->fd);
						res = -1;
					}
			}
			
		}
	
		break;
	}

	if (res < 0)
		return DBGRES_ERR;
		
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
