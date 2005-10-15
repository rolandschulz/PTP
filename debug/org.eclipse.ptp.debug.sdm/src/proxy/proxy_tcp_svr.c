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
proxy_tcp_svr_event_callback(proxy_event *ev, void *data)
{
	proxy_tcp_conn *	conn = (proxy_tcp_conn *)data;
	char *			str;
	
	if (proxy_event_to_str(ev, &str) < 0) {
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
		proxy_set_error(PROXY_ERR_SYSTEM, strerror(errno));
		return PROXY_RES_ERR;
	}
	
	memset (&sname, 0, sizeof(sname));
	sname.sin_family = PF_INET;
	sname.sin_port = htons(port);
	sname.sin_addr.s_addr = htonl(INADDR_ANY);
	
	if (bind(sd,(struct sockaddr *) &sname, sizeof(sname)) == SOCKET_ERROR )
	{
		proxy_set_error(PROXY_ERR_SYSTEM, strerror(errno));
		CLOSE_SOCKET(sd);
		return PROXY_RES_ERR;
	}
	
	slen = sizeof(sname);
	
	if ( getsockname(sd, (struct sockaddr *)&sname, &slen) == SOCKET_ERROR )
	{
		proxy_set_error(PROXY_ERR_SYSTEM, strerror(errno));
		CLOSE_SOCKET(sd);
		return PROXY_RES_ERR;
	}
	
	if ( listen(sd, 5) == SOCKET_ERROR )
	{
		proxy_set_error(PROXY_ERR_SYSTEM, strerror(errno));
		CLOSE_SOCKET(sd);
		return PROXY_RES_ERR;
	}
	
	conn->svr_sock = sd;
	conn->port = (int) ntohs(sname.sin_port);
	
	if (helper->regfile != NULL)
		helper->regfile(sd, READ_FILE_HANDLER, proxy_tcp_svr_accept, (void *)conn);
	if (helper->regeventhandler != NULL)
		helper->regeventhandler(proxy_tcp_svr_event_callback, (void *)conn);
	
	return PROXY_RES_OK;
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
	proxy_tcp_conn *			conn = (proxy_tcp_conn *)data;
	proxy_svr_helper_funcs *	helper = (proxy_svr_helper_funcs *)conn->helper;
		        
	if (host == NULL) {
		proxy_set_error(PROXY_ERR_SERVER, "no host specified");
		return PROXY_RES_ERR;
	}
	
	hp = gethostbyname(host);
	        
	if (hp == (struct hostent *)NULL) {
		proxy_set_error(PROXY_ERR_SERVER, "could not find host");
		return PROXY_RES_ERR;
	}
	
	haddr = ((hp->h_addr[0] & 0xff) << 24) |
			((hp->h_addr[1] & 0xff) << 16) |
			((hp->h_addr[2] & 0xff) <<  8) |
			((hp->h_addr[3] & 0xff) <<  0);
	
	if ( (sd = socket(PF_INET, SOCK_STREAM, 0)) == INVALID_SOCKET )
	{
		proxy_set_error(PROXY_ERR_SYSTEM, strerror(errno));
		return PROXY_RES_ERR;
	}
	
	memset (&scket,0,sizeof(scket));
	scket.sin_family = PF_INET;
	scket.sin_port = htons((u_short) port);
	scket.sin_addr.s_addr = htonl(haddr);
	
	if ( connect(sd, (struct sockaddr *) &scket, sizeof(scket)) == SOCKET_ERROR )
	{
		proxy_set_error(PROXY_ERR_SYSTEM, strerror(errno));
		CLOSE_SOCKET(sd);
		return PROXY_RES_ERR;
	}

	conn->sess_sock = sd;
	conn->host = strdup(host);
	conn->port = port;
	
	if (helper->regeventhandler != NULL)
		helper->regeventhandler(proxy_tcp_svr_event_callback, (void *)conn);
	if (helper->regfile != NULL)
		helper->regfile(sd, READ_FILE_HANDLER, proxy_tcp_svr_recv_msgs, (void *)conn);
	
	return PROXY_RES_OK;
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
	proxy_tcp_conn *			conn = (proxy_tcp_conn *)data;
	proxy_svr_helper_funcs *	helper = (proxy_svr_helper_funcs *)conn->helper;
	
	fromlen = sizeof(addr);
	ns = accept(fd, &addr, &fromlen);
	if (ns < 0) {
		proxy_set_error(PROXY_ERR_SYSTEM, strerror(errno));
		return PROXY_RES_ERR;
	}
	
	/*
	 * Only allow one connection at a time.
	 */
	if (conn->connected) {
		CLOSE_SOCKET(ns); // reject
		return PROXY_RES_OK;
	}
	
	if (helper->newconn != NULL && helper->newconn() < 0) {
		CLOSE_SOCKET(ns); // reject
		return PROXY_RES_OK;
	}
	
	conn->sess_sock = ns;
	conn->connected++;
	
	if (helper->regfile != NULL)
		helper->regfile(ns, READ_FILE_HANDLER, proxy_tcp_svr_recv_msgs, (void *)conn);
	
	return PROXY_RES_OK;
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
		if (helper->unregfile != NULL)
			helper->unregfile(conn->sess_sock);
		CLOSE_SOCKET(conn->sess_sock);
		conn->sess_sock = INVALID_SOCKET;
	}
	
	if (conn->svr_sock != INVALID_SOCKET) {
		if (helper->unregfile != NULL)
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
		if (helper->shutdown_completed != NULL)
			 return helper->shutdown_completed() ? PROXY_RES_ERR : PROXY_RES_OK;
		else
			return PROXY_RES_ERR;
	}
		
	return PROXY_RES_OK;
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
	proxy_event *		e;
	proxy_svr_commands * cmd;
	
printf("SVR received <%s>\n", msg);

	if (proxy_tcp_svr_shutdown) {
		e = new_proxy_event(PROXY_EV_ERROR);
		e->error_code = PROXY_ERR_SERVER;
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
	
	if (res != PROXY_RES_OK) {
		e = new_proxy_event(PROXY_EV_ERROR);
		e->error_code = proxy_get_error();
		e->error_msg = strdup(proxy_get_error_str());
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
	int	res = PROXY_RES_OK;
	
	if (helper->quit != NULL)
		res = helper->quit();
	
	proxy_tcp_svr_shutdown++;
	
	return res;
}
