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

/*
 * The proxy handles communication between the client debug library API and the
 * client debugger, since they may be running on different hosts, and will
 * certainly be running in different processes.
 */

#include <sys/socket.h>
#include <arpa/inet.h>
#include <netdb.h>

#include <stdio.h>
#include <errno.h>
#include <string.h>
#include <unistd.h>

#include "compat.h"
#include "dbg.h"
#include "dbg_client.h"
#include "args.h"
#include "session.h"
#include "proxy.h"
#include "proxy_tcp.h"
#include "procset.h"

static int	proxy_tcp_svr_create(proxy_svr_helper_funcs *, void **);
static int	proxy_tcp_svr_progress(proxy_svr_helper_funcs *, void *);
static void	proxy_tcp_svr_finish(proxy_svr_helper_funcs *, void *);

static int	proxy_tcp_svr_recv_msgs(int, void *);
static int	proxy_tcp_svr_accept(int, void *);
static int	proxy_tcp_svr_dispatch(proxy_tcp_conn *, char *);

proxy_svr_funcs proxy_tcp_svr_funcs =
{
	proxy_tcp_svr_create,
	proxy_tcp_svr_progress,
	proxy_tcp_svr_finish,
};

struct proxy_tcp_svr_func {
	char *cmd;
	int (*func)(proxy_svr_helper_funcs *, char **, char **);
};

typedef struct proxy_tcp_svr_func	proxy_tcp_svr_func;

static int proxy_tcp_svr_startsession(proxy_svr_helper_funcs *, char **, char **);
static int proxy_tcp_svr_setlinebreakpoint(proxy_svr_helper_funcs *, char **, char **);
static int proxy_tcp_svr_setfuncbreakpoint(proxy_svr_helper_funcs *, char **, char **);
static int proxy_tcp_svr_deletebreakpoint(proxy_svr_helper_funcs *, char **, char **);
static int proxy_tcp_svr_go(proxy_svr_helper_funcs *, char **, char **);
static int proxy_tcp_svr_step(proxy_svr_helper_funcs *, char **, char **);
static int proxy_tcp_svr_liststackframes(proxy_svr_helper_funcs *, char **, char **);
static int proxy_tcp_svr_setcurrentstackframe(proxy_svr_helper_funcs *, char **, char **);
static int proxy_tcp_svr_evaluateexpression(proxy_svr_helper_funcs *, char **, char **);
static int proxy_tcp_svr_gettype(proxy_svr_helper_funcs *, char **, char **);
static int proxy_tcp_svr_listlocalvariables(proxy_svr_helper_funcs *, char **, char **);
static int proxy_tcp_svr_listarguments(proxy_svr_helper_funcs *, char **, char **);
static int proxy_tcp_svr_listglobalvariables(proxy_svr_helper_funcs *, char **, char **);
static int proxy_tcp_svr_quit(proxy_svr_helper_funcs *, char **, char **);

static proxy_tcp_svr_func proxy_tcp_svr_func_tab[] =
{
	{"INI",	proxy_tcp_svr_startsession},
	{"SLB",	proxy_tcp_svr_setlinebreakpoint},
	{"SFB",	proxy_tcp_svr_setfuncbreakpoint},
	{"DBS",	proxy_tcp_svr_deletebreakpoint},
	{"GOP",	proxy_tcp_svr_go},
	{"STP",	proxy_tcp_svr_step},
	{"LSF",	proxy_tcp_svr_liststackframes},
	{"SCS",	proxy_tcp_svr_setcurrentstackframe},
	{"EEX",	proxy_tcp_svr_evaluateexpression},
	{"TYP",	proxy_tcp_svr_gettype},
	{"LLV",	proxy_tcp_svr_listlocalvariables},
	{"LAR",	proxy_tcp_svr_listarguments},
	{"LGV",	proxy_tcp_svr_listglobalvariables},
	{"QUI",	proxy_tcp_svr_quit},
	{NULL,	NULL},
};

static int proxy_tcp_svr_shutdown;

/*
 * Called when an event is received in response to a client debug command.
 * Sends the event to the proxy peer.
 */
static void
proxy_tcp_svr_event_callback(dbg_event *ev, void *data)
{
	proxy_tcp_conn *	conn = (proxy_tcp_conn *)data;
	char *			str;
	
	if (proxy_tcp_event_to_str(ev, &str) < 0) {
		/*
		 * TODO should send an error back to proxy peer
		 */
		fprintf(stderr, "proxy_tcp_svr_event_callback: event conversion failed\n");
		return;
	}

printf("SVR reply <%s>\n", str);	
	(void)proxy_tcp_send_msg(conn, str, strlen(str));
	free(str);
}

/**
 * Create server socket and bind address to it. 
 * 
 * @return conn structure containing server socket and port
 */
static int 
proxy_tcp_svr_create(proxy_svr_helper_funcs *helper, void **data)
{
	socklen_t			slen;
	SOCKET				sd;
	struct sockaddr_in	sname;
	proxy_tcp_conn		*conn;
	
	if ( (sd = socket(PF_INET, SOCK_STREAM, 0)) == INVALID_SOCKET )
	{
		fprintf(stderr, "socket error");
		return -1;
	}
	
	memset (&sname, 0, sizeof(sname));
	sname.sin_family = PF_INET;
	sname.sin_port = htons(PROXY_TCP_PORT);
	sname.sin_addr.s_addr = htonl(INADDR_ANY);
	
	if (bind(sd,(struct sockaddr *) &sname, sizeof(sname)) == SOCKET_ERROR )
	{
		fprintf(stderr, "bind error\n");
		CLOSE_SOCKET(sd);
		return -1;
	}
	
	slen = sizeof(sname);
	
	if ( getsockname(sd, (struct sockaddr *)&sname, &slen) == SOCKET_ERROR )
	{
		fprintf(stderr, "getsockname error\n");
		CLOSE_SOCKET(sd);
		return -1;
	}
	
	if ( listen(sd, 5) == SOCKET_ERROR )
	{
		fprintf(stderr, "listen error\n");
		CLOSE_SOCKET(sd);
		return -1;
	}
	
	proxy_tcp_create_conn(&conn);
	
	conn->svr_sock = sd;
	conn->port = (int) ntohs(sname.sin_port);
	conn->helper = helper;
	*data = (void *)conn;
	
	helper->regreadfile(sd, proxy_tcp_svr_accept, (void *)conn);
	helper->regeventhandler(proxy_tcp_svr_event_callback, (void *)conn);
	
	return 0;
}

/**
 * Accept a new proxy connection. Register dispatch routine.
 */
static int
proxy_tcp_svr_accept(int fd, void *data)
{
	proxy_tcp_conn *	conn = (proxy_tcp_conn *)data;
	socklen_t		fromlen;
	SOCKET			ns;
	struct sockaddr	addr;
	dbg_event *		e;
	
	fromlen = sizeof(addr);
	ns = accept(fd, &addr, &fromlen);
	if (ns < 0) {
		perror("accept");
		return 0;
	}
	
	/*
	 * Only allow one connection at a time.
	 */
	if (conn->sock != INVALID_SOCKET) {
		CLOSE_SOCKET(ns); // reject
		return 0;
	}
	
	if (conn->helper->newconn() < 0) {
		CLOSE_SOCKET(ns); // reject
		return 0;
	}
	
	conn->sock = ns;
	
	conn->helper->regreadfile(ns, proxy_tcp_svr_recv_msgs, (void *)conn);
	
	e = NewEvent(DBGEV_INIT);
	e->num_servers = conn->helper->numservers();
	proxy_tcp_svr_event_callback(e, data);
	
	return 0;
	
}

/**
 * Cleanup prior to server exit.
 */
static void 
proxy_tcp_svr_finish(proxy_svr_helper_funcs *helper, void *data)
{
	proxy_tcp_conn *	conn = (proxy_tcp_conn *)data;
	
	if (conn->sock != INVALID_SOCKET) {
		helper->unregreadfile(conn->sock);
		CLOSE_SOCKET(conn->sock);
		conn->sock = INVALID_SOCKET;
	}
	
	if (conn->svr_sock != INVALID_SOCKET) {
		helper->unregreadfile(conn->svr_sock);
		CLOSE_SOCKET(conn->svr_sock);
		conn->svr_sock = INVALID_SOCKET;
	}
	
	proxy_tcp_destroy_conn(conn);
}

/**
 * Check for incoming messages or connection attempts.
 * 
 * @return	0	success
 * 			-1	server shutdown
 */
static int
proxy_tcp_svr_progress(proxy_svr_helper_funcs *helper, void *data)
{
	char *			msg;
	proxy_tcp_conn *	conn = (proxy_tcp_conn *)data;

	if (proxy_tcp_get_msg(conn, &msg) > 0) {
		proxy_tcp_svr_dispatch(conn, msg);
		free(msg);
	}
			
	helper->progress();
	
	if (proxy_tcp_svr_shutdown && helper->shutdown_completed())
		return -1;
		
	return 0;
}

/*
 * Dispatch a command to the server
 *
 * proxy_tcp_svr_dispatch() should never fail. If we get a read error from the client then we just
 * assume the client has gone away. Errors from server commands are just reported back to the
 * client.
 */
static int
proxy_tcp_svr_dispatch(proxy_tcp_conn *conn, char *msg)
{
	int					i;
	int					res;
	char **				args;
	dbg_event *			e;
	proxy_tcp_svr_func * sf;
	
printf("SVR received <%s>\n", msg);

	if (proxy_tcp_svr_shutdown) {
		e = NewEvent(DBGEV_ERROR);
		e->error_code = DBGERR_DEBUGGER;
		e->error_msg = strdup("server is shutting down");
		proxy_tcp_svr_event_callback(e, (void *)conn);
	}
	
	args = Str2Args(msg);

	for (i = 0; i < sizeof(proxy_tcp_svr_func_tab) / sizeof(proxy_tcp_svr_func); i++) {
		sf = &proxy_tcp_svr_func_tab[i];
		if (sf->cmd != NULL && strcmp(args[0], sf->cmd) == 0) {
			res = sf->func(conn->helper, args, NULL);
			break;
		}
	}
	
	FreeArgs(args);
	
	if (res != DBGRES_OK) {
		e = NewEvent(DBGEV_ERROR);
		e->error_code = DbgGetError();
		e->error_msg = strdup(DbgGetErrorStr());
		proxy_tcp_svr_event_callback(e, (void *)conn);
	}
	
	return 0;
}

static int
proxy_tcp_svr_recv_msgs(int fd, void *data)
{
	proxy_tcp_conn *		conn = (proxy_tcp_conn *)data;
	
	return proxy_tcp_recv_msgs(conn);
}

/*
 * SERVER FUNCTIONS
 */
static int 
proxy_tcp_svr_startsession(proxy_svr_helper_funcs *helper, char **args, char **response)
{
	return helper->startsession(args[1], args[2]);
}

static int 
proxy_tcp_svr_setlinebreakpoint(proxy_svr_helper_funcs *helper, char **args, char **response)
{
	int			res;
	procset *	procs;
	
	procs = str_to_procset(args[1]);
	if (procs == NULL) {
		DbgSetError(DBGERR_PROCSET, NULL);
		return DBGRES_ERR;
	}
	
	res = helper->setlinebreakpoint(procs, args[2], atoi(args[3]));
	
	procset_free(procs);
	
	return res;
}

static int 
proxy_tcp_svr_setfuncbreakpoint(proxy_svr_helper_funcs *helper, char **args, char **response)
{
	int			res;
	procset *	procs;
	
	procs = str_to_procset(args[1]);
	if (procs == NULL) {
		DbgSetError(DBGERR_PROCSET, NULL);
		return DBGRES_ERR;
	}
	
	res = helper->setfuncbreakpoint(procs, args[2], args[3]);
	
	procset_free(procs);
	
	return res;
}

static int 
proxy_tcp_svr_deletebreakpoint(proxy_svr_helper_funcs *helper, char **args, char **response)
{
	int			res;
	procset *	procs;
	
	procs = str_to_procset(args[1]);
	if (procs == NULL) {
		DbgSetError(DBGERR_PROCSET, NULL);
		return DBGRES_ERR;
	}
	
	res = helper->deletebreakpoint(procs, atoi(args[2]));
	
	procset_free(procs);
	
	return res;
}

static int 
proxy_tcp_svr_go(proxy_svr_helper_funcs *helper, char **args, char **response)
{
	int			res;
	procset *	procs;
	
	procs = str_to_procset(args[1]);
	if (procs == NULL) {
		DbgSetError(DBGERR_PROCSET, NULL);
		return DBGRES_ERR;
	}
	
	res = helper->go(procs);
	
	procset_free(procs);
	
	return res;
}

static int 
proxy_tcp_svr_step(proxy_svr_helper_funcs *helper, char **args, char **response)
{
	int			res;
	procset *	procs;
	
	procs = str_to_procset(args[1]);
	if (procs == NULL) {
		DbgSetError(DBGERR_PROCSET, NULL);
		return DBGRES_ERR;
	}
	
	res = helper->step(procs, atoi(args[2]), atoi(args[3]));
	
	procset_free(procs);
	
	return res;}

static int 
proxy_tcp_svr_liststackframes(proxy_svr_helper_funcs *helper, char **args, char **response)
{
	int			res;
	procset *	procs;
	
	procs = str_to_procset(args[1]);
	if (procs == NULL) {
		DbgSetError(DBGERR_PROCSET, NULL);
		return DBGRES_ERR;
	}
	
	res = helper->liststackframes(procs, atoi(args[2]));
	
	procset_free(procs);
	
	return res;
}

static int 
proxy_tcp_svr_setcurrentstackframe(proxy_svr_helper_funcs *helper, char **args, char **response)
{
	int			res;
	procset *	procs;
	
	procs = str_to_procset(args[1]);
	if (procs == NULL) {
		DbgSetError(DBGERR_PROCSET, NULL);
		return DBGRES_ERR;
	}
	
	res = helper->setcurrentstackframe(procs, atoi(args[2]));
	
	procset_free(procs);
	
	return res;
}

static int 
proxy_tcp_svr_evaluateexpression(proxy_svr_helper_funcs *helper, char **args, char **response)
{
	int			res;
	procset *	procs;
	
	procs = str_to_procset(args[1]);
	if (procs == NULL) {
		DbgSetError(DBGERR_PROCSET, NULL);
		return DBGRES_ERR;
	}
	
	res = helper->evaluateexpression(procs, args[2]);
	
	procset_free(procs);
	
	return res;
}

static int 
proxy_tcp_svr_gettype(proxy_svr_helper_funcs *helper, char **args, char **response)
{
	int			res;
	procset *	procs;
	
	procs = str_to_procset(args[1]);
	if (procs == NULL) {
		DbgSetError(DBGERR_PROCSET, NULL);
		return DBGRES_ERR;
	}
	
	res = helper->gettype(procs, args[2]);
	
	procset_free(procs);
	
	return res;
}

static int 
proxy_tcp_svr_listlocalvariables(proxy_svr_helper_funcs *helper, char **args, char **response)
{
	int			res;
	procset *	procs;
	
	procs = str_to_procset(args[1]);
	if (procs == NULL) {
		DbgSetError(DBGERR_PROCSET, NULL);
		return DBGRES_ERR;
	}
	
	res = helper->listlocalvariables(procs);
	
	procset_free(procs);
	
	return res;
}

static int 
proxy_tcp_svr_listarguments(proxy_svr_helper_funcs *helper, char **args, char **response)
{
	int			res;
	procset *	procs;
	
	procs = str_to_procset(args[1]);
	if (procs == NULL) {
		DbgSetError(DBGERR_PROCSET, NULL);
		return DBGRES_ERR;
	}
	
	res = helper->listarguments(procs);
	
	procset_free(procs);
	
	return res;
}

static int 
proxy_tcp_svr_listglobalvariables(proxy_svr_helper_funcs *helper, char **args, char **response)
{
	int			res;
	procset *	procs;
	
	procs = str_to_procset(args[1]);
	if (procs == NULL) {
		DbgSetError(DBGERR_PROCSET, NULL);
		return DBGRES_ERR;
	}
	
	res = helper->listglobalvariables(procs);
	
	procset_free(procs);
	
	return res;
}

static int
proxy_tcp_svr_quit(proxy_svr_helper_funcs *helper, char **args, char **response)
{
	int	res;
	
	res = helper->quit();
	
	proxy_tcp_svr_shutdown++;
	
	return res;
}