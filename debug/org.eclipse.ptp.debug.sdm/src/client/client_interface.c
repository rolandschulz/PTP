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
#include <string.h>
#include <unistd.h>
#include <errno.h>

#include "dbg.h"
#include "session.h"
#include "proxy.h"
#include "procset.h"
#include "handler.h"

static struct timeval	TIMEOUT = { 0, 1000 };

static void session_event_handler(dbg_event *, void *);

static proxy_clnt_helper_funcs helper_funcs = {
	RegisterFileHandler,
	UnregisterFileHandler,
	session_event_handler,
	NULL
};

/*
 * Intercept INIT events to obtain number of procs
 */
static void
session_event_handler(dbg_event *e, void *data)
{
	session *	s = (session *)data;
	
	if (e != NULL && e->event == DBGEV_INIT) {
		s->sess_procs = e->num_servers;
	}
	
	if (s->sess_event_handler != NULL)
		s->sess_event_handler(e, s->sess_event_data);
}

/*
 * Session initialization
 */
int
DbgInit(session **s, char *proxy, char *attr, ...)
{
	va_list	ap;
	void *	data;
	int		res;
	
	*s = malloc(sizeof(session));
	
	if (find_proxy(proxy, &(*s)->sess_proxy) < 0) {
		free(*s);
		return -1;
	}
	
	helper_funcs.eventdata = (void *)(*s);
	(*s)->sess_proxy->clnt_helper_funcs = &helper_funcs;
	
	va_start(ap, attr);
	res = proxy_clnt_init((*s)->sess_proxy, &helper_funcs, &data, attr, ap);
	va_end(ap);
	
	if (res < 0) {
		free(*s);
		return -1;
	}
	
	(*s)->sess_proxy_data = data;
	(*s)->sess_event_handler = NULL;
	
	return 0;
}

int
DbgConnect(session *s)
{
	return proxy_clnt_connect(s->sess_proxy, s->sess_proxy_data);
}

int
DbgCreate(session *s)
{
	return proxy_clnt_create(s->sess_proxy, s->sess_proxy_data);
}

int
DbgStartSession(session *s, char *prog, char *args)
{
	return proxy_clnt_startsession(s->sess_proxy, s->sess_proxy_data, prog, args);
}

/*
 * Breakpoint operations
 */
int 
DbgSetLineBreakpoint(session *s, procset *set, int bpid, char *file, int line)
{
	return proxy_clnt_setlinebreakpoint(s->sess_proxy, s->sess_proxy_data, set, bpid, file, line);
}

int 
DbgSetFuncBreakpoint(session *s, procset *set, int bpid, char *file, char *func)
{
	return proxy_clnt_setfuncbreakpoint(s->sess_proxy, s->sess_proxy_data, set, bpid, file, func);
}

int 
DbgDeleteBreakpoint(session *s, procset *set, int bpid)
{
	return proxy_clnt_deletebreakpoint(s->sess_proxy, s->sess_proxy_data, set, bpid);
}

/*
 * Process control operations
 */
int 
DbgGo(session *s, procset *set)
{
	return proxy_clnt_go(s->sess_proxy, s->sess_proxy_data, set);
}

int 
DbgStep(session *s, procset *set, int count, int type)
{
	return proxy_clnt_step(s->sess_proxy, s->sess_proxy_data, set, count, type);
}

int 
DbgTerminate(session *s, procset *set)
{
	return proxy_clnt_terminate(s->sess_proxy, s->sess_proxy_data, set);
}

/*
 * Stack frame operations
 */
int 
DbgListStackframes(session *s, procset *set, int current)
{
	return proxy_clnt_liststackframes(s->sess_proxy, s->sess_proxy_data, set, current);
}

int 
DbgSetCurrentStackframe(session *s, procset *set, int level)
{
	return proxy_clnt_setcurrentstackframe(s->sess_proxy, s->sess_proxy_data, set, level);
}

/*
 * Expression/variable operations
 */
int 
DbgEvaluateExpression(session *s, procset *set, char *exp)
{
	return proxy_clnt_evaluateexpression(s->sess_proxy, s->sess_proxy_data, set, exp);
}

int 
DbgGetType(session *s, procset *set, char *exp)
{
	return proxy_clnt_gettype(s->sess_proxy, s->sess_proxy_data, set, exp);
}

int 
DbgListLocalVariables(session *s, procset *set)
{
	return proxy_clnt_listlocalvariables(s->sess_proxy, s->sess_proxy_data, set);
}

int 
DbgListArguments(session *s, procset *set)
{
	return proxy_clnt_listarguments(s->sess_proxy, s->sess_proxy_data, set);
}

int 
DbgListGlobalVariables(session *s, procset *set)
{
	return proxy_clnt_listglobalvariables(s->sess_proxy, s->sess_proxy_data, set);
}

int 
DbgQuit(session *s)
{
	return proxy_clnt_quit(s->sess_proxy, s->sess_proxy_data);
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
	struct timeval	tv;
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
	
	tv = TIMEOUT;
	
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
		
	return proxy_clnt_progress(s->sess_proxy, s->sess_proxy_data);
}

void
DbgRegisterEventHandler(session *s, void (*event_callback)(dbg_event *, void *), void *data)
{
	s->sess_event_handler = event_callback;
	s->sess_event_data = data;
	RegisterEventHandler(session_event_handler, (void *)s);
	//s->sess_proxy->clnt_funcs->regeventhandler(s->sess_proxy_data, session_event_handler, (void *)s);
}

/**
 * Unregister file descriptor handler
 */
void
DbgUnregisterEventHandler(session *s, void (*event_callback)(dbg_event *, void *))
{
	s->sess_event_handler = NULL;
	UnregisterEventHandler(session_event_handler);
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
