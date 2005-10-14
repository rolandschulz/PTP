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
#include <stdlib.h>

#include "compat.h"
#include "dbg.h"
#include "dbg_client.h"
#include "args.h"
#include "session.h"
#include "proxy.h"
#include "proxy_tcp.h"
#include "procset.h"
#include "handler.h"

static void	proxy_tcp_svr_init(proxy_svr_helper_funcs *, proxy_svr_commands *cmds, void **);
static int	proxy_tcp_svr_create(int, void *);
static int	proxy_tcp_svr_connect(char *, int, void *);
static int	proxy_tcp_svr_progress(void *);
static void	proxy_tcp_svr_finish(void *);

static int	proxy_tcp_svr_recv_msgs(int, void *);
static int	proxy_tcp_svr_accept(int, void *);
static int	proxy_tcp_svr_dispatch(proxy_tcp_conn *, char *);

proxy_svr_funcs proxy_tcp_svr_funcs =
{
	proxy_tcp_svr_init,
	proxy_tcp_svr_create,
	proxy_tcp_svr_connect,
	proxy_tcp_svr_progress,
	proxy_tcp_svr_finish,
};

static int proxy_tcp_svr_quit(proxy_svr_helper_funcs *, char **);

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

static void
proxy_tcp_svr_init(proxy_svr_helper_funcs *helper, proxy_svr_commands *cmds, void **data)
{
	proxy_tcp_conn		*conn;

	proxy_tcp_create_conn(&conn);
	conn->helper = helper;
	conn->commands = cmds;
	*data = (void *)conn;
}

/**
 * Create server socket and bind address to it. 
 * 
 * @return conn structure containing server socket and port
 */
static int 
proxy_tcp_svr_create(int port, void *data)
{
	socklen_t				slen;
	SOCKET					sd;
	struct sockaddr_in		sname;
	proxy_tcp_conn *			conn = (proxy_tcp_conn *)data;
	proxy_svr_helper_funcs *	helper = (proxy_svr_helper_funcs *)conn->helper;
	
	if ( (sd = socket(PF_INET, SOCK_STREAM, 0)) == INVALID_SOCKET )
	{
		DbgSetError(DBGERR_SYSTEM, strerror(errno));
		return DBGRES_ERR;
	}
	
	memset (&sname, 0, sizeof(sname));
	sname.sin_family = PF_INET;
	sname.sin_port = htons(port);
	sname.sin_addr.s_addr = htonl(INADDR_ANY);
	
	if (bind(sd,(struct sockaddr *) &sname, sizeof(sname)) == SOCKET_ERROR )
	{
		DbgSetError(DBGERR_SYSTEM, strerror(errno));
		CLOSE_SOCKET(sd);
		return DBGRES_ERR;
	}
	
	slen = sizeof(sname);
	
	if ( getsockname(sd, (struct sockaddr *)&sname, &slen) == SOCKET_ERROR )
	{
		DbgSetError(DBGERR_SYSTEM, strerror(errno));
		CLOSE_SOCKET(sd);
		return DBGRES_ERR;
	}
	
	if ( listen(sd, 5) == SOCKET_ERROR )
	{
		DbgSetError(DBGERR_SYSTEM, strerror(errno));
		CLOSE_SOCKET(sd);
		return DBGRES_ERR;
	}
	
	conn->svr_sock = sd;
	conn->port = (int) ntohs(sname.sin_port);
	
	helper->regfile(sd, READ_FILE_HANDLER, proxy_tcp_svr_accept, (void *)conn);
	helper->regeventhandler(proxy_tcp_svr_event_callback, (void *)conn);
	
	return DBGRES_OK;
}

/**
 * Connect to a remote proxy.
 */
static int
proxy_tcp_svr_connect(char *host, int port, void *data)
{
	SOCKET					sd;
	struct hostent *			hp;
	long int					haddr;
	struct sockaddr_in		scket;
	dbg_event *				e;
	proxy_tcp_conn *			conn = (proxy_tcp_conn *)data;
	proxy_svr_helper_funcs *	helper = (proxy_svr_helper_funcs *)conn->helper;
		        
	if (host == NULL) {
		DbgSetError(DBGERR_DEBUGGER, "no host specified");
		return DBGRES_ERR;
	}
	
	hp = gethostbyname(host);
	        
	if (hp == (struct hostent *)NULL) {
		DbgSetError(DBGERR_DEBUGGER, "could not find host");
		return DBGRES_ERR;
	}
	
	haddr = ((hp->h_addr[0] & 0xff) << 24) |
			((hp->h_addr[1] & 0xff) << 16) |
			((hp->h_addr[2] & 0xff) <<  8) |
			((hp->h_addr[3] & 0xff) <<  0);
	
	if ( (sd = socket(PF_INET, SOCK_STREAM, 0)) == INVALID_SOCKET )
	{
		DbgSetError(DBGERR_SYSTEM, strerror(errno));
		return DBGRES_ERR;
	}
	
	memset (&scket,0,sizeof(scket));
	scket.sin_family = PF_INET;
	scket.sin_port = htons((u_short) port);
	scket.sin_addr.s_addr = htonl(haddr);
	
	if ( connect(sd, (struct sockaddr *) &scket, sizeof(scket)) == SOCKET_ERROR )
	{
		DbgSetError(DBGERR_SYSTEM, strerror(errno));
		CLOSE_SOCKET(sd);
		return DBGRES_ERR;
	}

	conn->sess_sock = sd;
	conn->host = strdup(host);
	conn->port = port;
	
	helper->regeventhandler(proxy_tcp_svr_event_callback, (void *)conn);
	helper->regfile(sd, READ_FILE_HANDLER, proxy_tcp_svr_recv_msgs, (void *)conn);
	
	e = NewEvent(DBGEV_INIT);
	e->num_servers = helper->numservers();
	proxy_tcp_svr_event_callback(e, data);
	
	return DBGRES_OK;
}

/**
 * Accept a new proxy connection. Register dispatch routine.
 */
static int
proxy_tcp_svr_accept(int fd, void *data)
{
	socklen_t				fromlen;
	SOCKET					ns;
	struct sockaddr			addr;
	dbg_event *				e;
	proxy_tcp_conn *			conn = (proxy_tcp_conn *)data;
	proxy_svr_helper_funcs *	helper = (proxy_svr_helper_funcs *)conn->helper;
	
	fromlen = sizeof(addr);
	ns = accept(fd, &addr, &fromlen);
	if (ns < 0) {
		DbgSetError(DBGERR_SYSTEM, strerror(errno));
		return DBGRES_ERR;
	}
	
	/*
	 * Only allow one connection at a time.
	 */
	if (conn->connected) {
		CLOSE_SOCKET(ns); // reject
		return DBGRES_OK;
	}
	
	if (helper->newconn() < 0) {
		CLOSE_SOCKET(ns); // reject
		return DBGRES_OK;
	}
	
	conn->sess_sock = ns;
	conn->connected++;
	
	helper->regfile(ns, READ_FILE_HANDLER, proxy_tcp_svr_recv_msgs, (void *)conn);
	
	e = NewEvent(DBGEV_INIT);
	e->num_servers = helper->numservers();
	proxy_tcp_svr_event_callback(e, data);
	
	return DBGRES_OK;
}

/**
 * Cleanup prior to server exit.
 */
static void 
proxy_tcp_svr_finish(void *data)
{
	proxy_tcp_conn *			conn = (proxy_tcp_conn *)data;
	proxy_svr_helper_funcs *	helper = (proxy_svr_helper_funcs *)conn->helper;
	
	if (conn->sess_sock != INVALID_SOCKET) {
		if (helper != NULL)
			helper->unregfile(conn->sess_sock);
		CLOSE_SOCKET(conn->sess_sock);
		conn->sess_sock = INVALID_SOCKET;
	}
	
	if (conn->svr_sock != INVALID_SOCKET) {
		if (helper != NULL)
			helper->unregfile(conn->svr_sock);
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
proxy_tcp_svr_progress(void *data)
{
	char *					msg;
	proxy_tcp_conn *			conn = (proxy_tcp_conn *)data;
	proxy_svr_helper_funcs *	helper = (proxy_svr_helper_funcs *)conn->helper;

	if (proxy_tcp_get_msg(conn, &msg) > 0) {
		proxy_tcp_svr_dispatch(conn, msg);
		free(msg);
	}
	
	if (proxy_tcp_svr_shutdown) {
		if (helper != NULL)
			 return helper->shutdown_completed() ? DBGRES_ERR : DBGRES_OK;
		else
			return DBGRES_ERR;
	}
		
	return DBGRES_OK;
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
	int					res;
	char **				args;
	dbg_event *			e;
	proxy_svr_commands * cmd;
	
printf("SVR received <%s>\n", msg);

	if (proxy_tcp_svr_shutdown) {
		e = NewEvent(DBGEV_ERROR);
		e->error_code = DBGERR_DEBUGGER;
		e->error_msg = strdup("server is shutting down");
		proxy_tcp_svr_event_callback(e, (void *)conn);
	}
	
	args = Str2Args(msg);

	if (strcmp(args[0], PROXY_QUIT_CMD) == 0)
		res = proxy_tcp_svr_quit(conn->helper, args);
	else {
		for (cmd = conn->commands; cmd->cmd_name != NULL; cmd++) {
			if (strcmp(args[0], cmd->cmd_name) == 0) {
				res = cmd->cmd_func(args);
				break;
			}
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

static int
proxy_tcp_svr_quit(proxy_svr_helper_funcs *helper, char **args)
{
	int	res;
	
	res = helper->quit();
	
	proxy_tcp_svr_shutdown++;
	
	return res;
}
