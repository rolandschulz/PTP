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
#include <unistd.h>
#include <errno.h>
#include <stdio.h>

#include "compat.h"
#include "dbg.h"
#include "dbg_client.h"
#include "procset.h"
#include "list.h"

#define HANDLER_FILE		1
#define HANDLER_SIGNAL	2
#define HANDLER_EVENT		3

struct dbg_event_handler {
	int		htype;
	void *	data;
		
	/*
	 * HANDLER_FILE
	 */
	int		file_type;
	int		fd;
	int		(*file_handler)(int, void *);
	
	/*
	 * HANDLER_SIGNAL
	 */
	int		signal;

	/*
	 * HANDLER_EVENT
	 */
	 void 	(*event_handler)(dbg_event *);
};
typedef struct dbg_event_handler	dbg_event_handler;

static List *		dbg_event_handlers = NULL;
static struct timeval	TIMEOUT = { 25, 0 };

/**
 * Create an event handler structure and add it to the list of handlers
 */
static dbg_event_handler *
new_dbg_event_handler(int type, void *data)
{
	dbg_event_handler *	h;
	
	if (dbg_event_handlers == NULL)
		dbg_event_handlers = NewList();
		
	h = (dbg_event_handler *)malloc(sizeof(dbg_event_handler));
	h->htype = type;
	h->data = data;
	
	AddToList(dbg_event_handlers, (void *)h);
	
	return h;
}

/**
 * Remove handler from list and dispose of memory
 */
static void
destroy_dbg_event_handler(dbg_event_handler *h)
{
	RemoveFromList(dbg_event_handlers, (void *)h);
	free(h);
}

/**
 * A send command is completed. Process the result and 
 * call all registered event handlers.
 */
static void
dbg_clnt_cmd_completed(procset *p)
{
	/*
	dbg_event *e = resp_to_event(res);
	
	SetList(dbg_event_handlers);
	
	while ((h = (dbg_event_handler *)GetListElement(dbg_event_handlers)) != NULL) {
		if (h->htype == HANDLER_EVENT) {
			h->event_handler(e);
		}
	}
	
	free_event(e);
	*/
}

/*
 * Breakpoint operations
 */
int 
DbgClntSetLineBreakpoint(procset *set, char *file, int line)
{
	char *	cmd;
	
	asprintf(&cmd, "SLB \"%s\" %d", file, line);
	return send_command(set, cmd, dbg_clnt_cmd_completed);
}

int 
DbgClntSetFuncBreakpoint(procset *set, char *file, char *func)
{
	return DBGRES_OK;
}

int 
DbgClntDeleteBreakpoints(procset *set)
{
	return DBGRES_OK;
}

/*
 * Process control operations
 */
int 
DbgClntGo(procset *set)
{
	return DBGRES_OK;
}

int 
DbgClntStep(procset *set, int count, int type)
{
	return DBGRES_OK;
}

/*
 * Stack frame operations
 */
int 
DbgClntListStackframes(int proc)
{
	return DBGRES_OK;
}

int 
DbgClntSetCurrentStackframe(int proc, int count, int dir)
{
	return DBGRES_OK;
}

/*
 * Expression/variable operations
 */
int 
DbgClntEvaluateExpression(int proc)
{
	return DBGRES_OK;
}

int 
DbgClntListLocalVariables(int proc)
{
	return DBGRES_OK;
}

int 
DbgClntListArguments(int proc)
{
	return DBGRES_OK;
}

int 
DbgClntListGlobalVariables(int proc)
{
	return DBGRES_OK;
}

int 
DbgClntQuit(void)
{
	return DBGRES_OK;
}

/*
 * Event handling
 */
int
DbgClntProgress(void)
{
	fd_set				rfds;
	fd_set				wfds;
	fd_set				efds;
	int					res;
	int					nfds = 0;
	struct timeval		tv;
	dbg_event_handler *	h;

	if (dbg_event_handlers == NULL || EmptyList(dbg_event_handlers))
		return 0;
	
	/***********************************
	 * First: Check for any file events
	 */
	 
	/*
	 * Set up fd sets
	 */
	FD_ZERO(&rfds);
	FD_ZERO(&wfds);
	FD_ZERO(&efds);
	
	SetList(dbg_event_handlers);
	
	while ((h = (dbg_event_handler *)GetListElement(dbg_event_handlers)) != NULL) {
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
		
			perror("select");
			return -1;
		
		case 0:
			return 0;
		
		default:
			break;
		}
	
		break;
	}
	
	SetList(dbg_event_handlers);
	
	while ((h = (dbg_event_handler *)GetListElement(dbg_event_handlers)) != NULL) {
		if (h->htype == HANDLER_FILE
			&& ((h->file_type & READ_FILE_HANDLER && FD_ISSET(h->fd, &rfds))
				|| (h->file_type & WRITE_FILE_HANDLER && FD_ISSET(h->fd, &wfds))
				|| (h->file_type & EXCEPT_FILE_HANDLER && FD_ISSET(h->fd, &efds))))
			h->file_handler(h->fd, h->data);
	}
	
	/*************************************
	 * Second: Check for any server events
	 */
	 
	progress_commands();
	
	return 0;
}

void
DbgClntRegisterEventHandler(void (*event_callback)(dbg_event *))
{
	dbg_event_handler *	h;
	
	h = new_dbg_event_handler(HANDLER_EVENT, NULL);
	h->event_handler = event_callback;
}

/**
 * Unregister file descriptor handler
 */
void
DbgClntUnregisterEventHandler(void (*event_callback)(dbg_event *))
{
	dbg_event_handler *	h;

	if (dbg_event_handlers == NULL || EmptyList(dbg_event_handlers))
		return;
		
	SetList(dbg_event_handlers);
	
	while ((h = (dbg_event_handler *)GetListElement(dbg_event_handlers)) != NULL) {
		if (h->htype == HANDLER_EVENT && h->event_handler == event_callback)
			destroy_dbg_event_handler(h);
	}
}

/**
 * Register a handler for file descriptor events.
 * 
 * TODO: Should the handler return a value?
 */
void
DbgClntRegisterFileHandler(int fd, int type, int (*handler)(int, void *), void *data)
{
	dbg_event_handler *	h;
	
	h = new_dbg_event_handler(HANDLER_FILE, data);
	h->file_type = type;
	h->fd = fd;
	h->file_handler = handler;
}

/**
 * Unregister file descriptor handler
 */
void
DbgClntUnregisterFileHandler(int fd)
{
	dbg_event_handler *	h;

	if (dbg_event_handlers == NULL || EmptyList(dbg_event_handlers))
		return;
		
	SetList(dbg_event_handlers);
	
	while ((h = (dbg_event_handler *)GetListElement(dbg_event_handlers)) != NULL) {
		if (h->htype == HANDLER_FILE && h->fd == fd)
		{
			RemoveFromList(dbg_event_handlers, (void *)h);
			free(h);
		}
	}
}
