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
#include <unistd.h>
#include <errno.h>
#include <stdio.h>

#include "compat.h"
#include "dbg.h"
#include "dbg_proxy.h"
#include "dbg_client.h"
#include "client_srv.h"
#include "procset.h"
#include "handler.h"
#include "list.h"

#define SHUTDOWN_CANCELLED	0
#define SHUTDOWN_STARTED		1
#define SHUTDOWN_COMPLETED	2

static int			dbg_shutdown;
static procset *		dbg_procs = NULL;
static proxy *		dbg_proxy;
static void *		dbg_proxy_data;
static struct timeval	TIMEOUT = { 0, 1000 };

/**
 * A send command is completed. Process the result and 
 * call all registered event handlers.
 */
static void
dbg_clnt_cmd_completed(dbg_event *e, void *data)
{
	handler *	h;

	for (SetHandler(); (h = GetHandler()) != NULL; ) {
		if (h->htype == HANDLER_EVENT) {
			h->event_handler(e, h->data);
		}
	}
	
	/*
	 * The next event received after a quit command
	 * must be the server shutting down.
	 */
	if (dbg_shutdown == SHUTDOWN_STARTED)
		dbg_shutdown = SHUTDOWN_COMPLETED;
}

static void
fix_null(char **str)
{
	if (*str == NULL)
		*str = "";
}

int
DbgClntInit(int num_svrs, char *proxy, proxy_svr_helper_funcs *funcs, proxy_svr_commands *cmds)
{
	/*
	 * Initialize client/server interface
	 */
	ClntInit(num_svrs);
	
	/*
	 * Create a procset containing all processes
	 */
	dbg_procs = procset_new(num_svrs);
	procset_invert(dbg_procs);
	
	/*
	 * Reset shutdown flag
	 */
	dbg_shutdown = SHUTDOWN_CANCELLED;
	
	/*
	 * Initialize proxy
	 */
	if (find_proxy(proxy, &dbg_proxy) < 0) {
		return DBGRES_ERR;
	}
	
	proxy_svr_init(dbg_proxy, funcs, cmds, &dbg_proxy_data);
	
	return DBGRES_OK;
}

int
DbgClntCreateSession(char *host, int port)
{
	if (host != NULL)
		return proxy_svr_connect(dbg_proxy, host, port, dbg_proxy_data);
	
	return proxy_svr_create(dbg_proxy, port, dbg_proxy_data);
}

void
DbgClntFinish(void)
{
	proxy_svr_finish(dbg_proxy, dbg_proxy_data);
}

int
DbgClntIsShutdown(void)
{
	return dbg_shutdown == SHUTDOWN_COMPLETED;
}

int 
DbgClntStartSession(char **args)
{
	int		res;
	char *	cmd;
	
	asprintf(&cmd, "%s \"%s\" \"%s\"", DBG_STARTSESSION_CMD, args[1], args[2]);
	res = ClntSendCommand(dbg_procs, cmd, NULL);
	free(cmd);
	return res;
}

/*
 * Breakpoint operations
 */
int 
DbgClntSetLineBreakpoint(char **args)
{
	int			res;
	char *		cmd;
	procset *	set; 

	set = str_to_procset(args[1]);
	if (set == NULL) {
		DbgSetError(DBGERR_PROCSET, NULL);
		return DBGRES_ERR;
	}
	
	asprintf(&cmd, "%s %s \"%s\" %s", DBG_SETLINEBREAKPOINT_CMD, args[2], args[3], args[4]);
	res = ClntSendCommand(set, cmd, NULL);
	
	free(cmd);
	procset_free(set);
	
	return res;
}

int 
DbgClntSetFuncBreakpoint(char **args)
{
	int			res;
	char *		cmd;
	procset *	set; 

	set = str_to_procset(args[1]);
	if (set == NULL) {
		DbgSetError(DBGERR_PROCSET, NULL);
		return DBGRES_ERR;
	}

	asprintf(&cmd, "%s %s \"%s\" \"%s\"", DBG_SETFUNCBREAKPOINT_CMD, args[2], args[3], args[4]);
	res = ClntSendCommand(set, cmd, NULL);
	
	free(cmd);
	procset_free(set);
		
	return res;
}

int 
DbgClntDeleteBreakpoint(char **args)
{
	int			res;
	char *		cmd;
	procset *	set; 

	set = str_to_procset(args[1]);
	if (set == NULL) {
		DbgSetError(DBGERR_PROCSET, NULL);
		return DBGRES_ERR;
	}
	
	asprintf(&cmd, "%s %s", DBG_DELETEBREAKPOINT_CMD, args[2]);
	res = ClntSendCommand(set, cmd, NULL);
	
	free(cmd);
	procset_free(set);
		
	return res;
}

/*
 * Process control operations
 */
int 
DbgClntGo(char **args)
{
	int			res;
	procset *	set; 

	set = str_to_procset(args[1]);
	if (set == NULL) {
		DbgSetError(DBGERR_PROCSET, NULL);
		return DBGRES_ERR;
	}
	
	res = ClntSendCommand(set, DBG_GO_CMD, NULL);
	
	procset_free(set);
		
	return res;
}

int 
DbgClntStep(char **args)
{
	int			res;
	char *		cmd;
	procset *	set; 

	set = str_to_procset(args[1]);
	if (set == NULL) {
		DbgSetError(DBGERR_PROCSET, NULL);
		return DBGRES_ERR;
	}
	
	asprintf(&cmd, "%s %s %s", DBG_STEP_CMD, args[2], args[3]);
	res = ClntSendCommand(set, cmd, NULL);
	
	free(cmd);
	procset_free(set);
	
	return res;
}

int 
DbgClntTerminate(char **args)
{
	int			res;
	procset *	set; 

	set = str_to_procset(args[1]);
	if (set == NULL) {
		DbgSetError(DBGERR_PROCSET, NULL);
		return DBGRES_ERR;
	}
	
	res = ClntSendCommand(set, DBG_TERMINATE_CMD, NULL);
	
	procset_free(set);
	
	return res;
}

/*
 * Stack frame operations
 */
int 
DbgClntListStackframes(char **args)
{
	int			res;
	char *		cmd;
	procset *	set; 

	set = str_to_procset(args[1]);
	if (set == NULL) {
		DbgSetError(DBGERR_PROCSET, NULL);
		return DBGRES_ERR;
	}
	
	asprintf(&cmd, "%s %s", DBG_LISTSTACKFRAMES_CMD, args[2]);
	res = ClntSendCommand(set, cmd, NULL);
	
	free(cmd);
	procset_free(set);
	
	return res;
}

int 
DbgClntSetCurrentStackframe(char **args)
{
	int			res;
	char *		cmd;
	procset *	set; 

	set = str_to_procset(args[1]);
	if (set == NULL) {
		DbgSetError(DBGERR_PROCSET, NULL);
		return DBGRES_ERR;
	}
	
	asprintf(&cmd, "%s %s", DBG_SETCURRENTSTACKFRAME_CMD, args[2]);
	res = ClntSendCommand(set, cmd, NULL);
	
	free(cmd);
	procset_free(set);
	
	return res;
}

/*
 * Expression/variable operations
 */
int 
DbgClntEvaluateExpression(char **args)
{
	int			res;
	char *		cmd;
	procset *	set; 

	set = str_to_procset(args[1]);
	if (set == NULL) {
		DbgSetError(DBGERR_PROCSET, NULL);
		return DBGRES_ERR;
	}
	
	asprintf(&cmd, "%s \"%s\"", DBG_EVALUATEEXPRESSION_CMD, args[2]);
	res = ClntSendCommand(set, cmd, NULL);
	
	free(cmd);
	procset_free(set);
	
	return res;
}

int 
DbgClntGetType(char **args)
{
	int		res;
	char *	cmd;
	procset *	set; 

	set = str_to_procset(args[1]);
	if (set == NULL) {
		DbgSetError(DBGERR_PROCSET, NULL);
		return DBGRES_ERR;
	}
	
	asprintf(&cmd, "%s \"%s\"", DBG_GETTYPE_CMD, args[2]);
	res = ClntSendCommand(set, cmd, NULL);
	
	free(cmd);
	procset_free(set);
	
	return res;
}

int 
DbgClntListLocalVariables(char **args)
{
	int			res;
	procset *	set; 

	set = str_to_procset(args[1]);
	if (set == NULL) {
		DbgSetError(DBGERR_PROCSET, NULL);
		return DBGRES_ERR;
	}
	
	res = ClntSendCommand(set, DBG_LISTLOCALVARIABLES_CMD, NULL);
	
	procset_free(set);
	
	return res;
}

int 
DbgClntListArguments(char **args)
{
	int			res;
	procset *	set; 

	set = str_to_procset(args[1]);
	if (set == NULL) {
		DbgSetError(DBGERR_PROCSET, NULL);
		return DBGRES_ERR;
	}
	
	res = ClntSendCommand(set, DBG_LISTARGUMENTS_CMD, NULL);
	
	procset_free(set);
	
	return res;
}

int 
DbgClntListGlobalVariables(char **args)
{
	int			res;
	procset *	set; 

	set = str_to_procset(args[1]);
	if (set == NULL) {
		DbgSetError(DBGERR_PROCSET, NULL);
		return DBGRES_ERR;
	}
	
	res = ClntSendCommand(set, DBG_LISTGLOBALVARIABLES_CMD, NULL);
	
	procset_free(set);
	
	return res;
}

int 
DbgClntQuit(void)
{
	dbg_shutdown = SHUTDOWN_STARTED;
	
	return ClntSendCommand(dbg_procs, "QUI", NULL);
}

/*
 * Event handling
 */
int
DbgClntProgress(void)
{
	fd_set			rfds;
	fd_set			wfds;
	fd_set			efds;
	int				res;
	int				nfds = 0;
	struct timeval	tv;
	handler *		h;

	/***********************************
	 * First: Check for any file events
	 */
	 
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
					&& h->file_handler(h->fd, h->data) < 0)
					return DBGRES_ERR;
			}
			
		}
	
		break;
	}
	
	/*************************************
	 * Second: Check for any server events
	 */
	 
	ClntProgressCmds();
	
	/**************************************
	 * Third: Check for proxy events
	 */
	 
	return proxy_svr_progress(dbg_proxy, dbg_proxy_data);
}

void
DbgClntRegisterEventHandler(void (*event_callback)(dbg_event *, void *), void *data)
{
	static int	registered = 0;
	
	if (registered == 0) {
		ClntRegisterCallback(dbg_clnt_cmd_completed);
		registered++;
	}

	RegisterEventHandler(event_callback, data);
}

/**
 * Unregister file descriptor handler
 */
void
DbgClntUnregisterEventHandler(void (*event_callback)(dbg_event *, void *))
{
	UnregisterEventHandler(event_callback);
}

/**
 * Register a handler for file descriptor events.
 * 
 * TODO: Should the handler return a value?
 */
void
DbgClntRegisterReadFileHandler(int fd, int (*file_handler)(int, void *), void *data)
{
	RegisterFileHandler(fd, READ_FILE_HANDLER, file_handler, data);
}

void
DbgClntRegisterWriteFileHandler(int fd, int (*file_handler)(int, void *), void *data)
{
	RegisterFileHandler(fd, WRITE_FILE_HANDLER, file_handler, data);
}

void
DbgClntRegisterExceptFileHandler(int fd, int (*file_handler)(int, void *), void *data)
{
	RegisterFileHandler(fd, EXCEPT_FILE_HANDLER, file_handler, data);
}

/**
 * Unregister file descriptor handler
 */
void
DbgClntUnregisterFileHandler(int fd)
{
	UnregisterFileHandler(fd);
}
