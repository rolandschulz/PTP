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
#include "args.h"
#include "session.h"
#include "proxy.h"
#include "proxy_tcp.h"

static int proxy_tcp_svr_create(void **, void (*)(void));
static int proxy_tcp_svr_progress(void *);
static void proxy_tcp_svr_dispatch(void *);
static void proxy_tcp_svr_finish(void *);

proxy_svr_funcs proxy_tcp_svr_funcs =
{
	proxy_tcp_svr_create,
	proxy_tcp_svr_progress,
	proxy_tcp_svr_dispatch,
	proxy_tcp_svr_finish,
};

struct proxy_tcp_svr_func {
	char *cmd;
	int (*func)(char **, char **);
};

typedef struct proxy_tcp_svr_func	proxy_tcp_svr_func;

static int proxy_tcp_svr_setlinebreakpoint(char **, char **);
static int proxy_tcp_svr_quit(char **, char **);

static proxy_tcp_svr_func proxy_tcp_svr_func_tab[] =
{
	{"SETLINEBREAK",	proxy_tcp_svr_setlinebreakpoint},
	{NULL,			NULL},
	{NULL,			NULL},
	{NULL,			NULL},
	{NULL,			NULL},
	{NULL,			NULL},
	{NULL,			NULL},
	{NULL,			NULL},
	{NULL,			NULL},
	{NULL,			NULL},
	{NULL,			NULL},
	{"QUIT",			proxy_tcp_svr_quit},
	{NULL,			NULL},
};

static int proxy_tcp_svr_shutdown;
static void (*proxy_tcp_svr_shutdown_callback)(void);

/**
 * Create server socket and bind address to it. 
 * 
 * @return conn structure containing server socket and port
 */
static int 
proxy_tcp_svr_create(void **data, void (*shutdown)(void))
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
	*data = (void *)conn;
	
	proxy_tcp_svr_shutdown = 0;
	proxy_tcp_svr_shutdown_callback = shutdown;
	
	return 0;
}

static void 
proxy_tcp_svr_finish(void *data)
{
	proxy_tcp_conn *	conn = (proxy_tcp_conn *)data;
	proxy_tcp_destroy_conn(conn);
}

/**
 * Check for incoming messages or connection attempts.
 * 
 * @return
 * 	-1:	error
 * 	 0: no action
 * 	 1: message ready for dispatch
 */
static int
proxy_tcp_svr_progress(void *data)
{
	proxy_tcp_conn *	conn = (proxy_tcp_conn *)data;
	fd_set			fds;
	int				res;
	int				nfds = 0;
	socklen_t		fromlen;
	SOCKET			ns;
	struct sockaddr	addr;
	struct timeval	tv;

	if (proxy_tcp_svr_shutdown) {
		if (conn->sock != INVALID_SOCKET) {
			CLOSE_SOCKET(conn->sock);
			conn->sock = INVALID_SOCKET;
		}
		if (conn->svr_sock != INVALID_SOCKET) {
			CLOSE_SOCKET(conn->svr_sock);
			conn->svr_sock = INVALID_SOCKET;
		}
		proxy_tcp_svr_shutdown_callback();
		return 0;
	}
		
	FD_ZERO(&fds);
	if (conn->sock != INVALID_SOCKET) {
		FD_SET(conn->sock, &fds);
		nfds = conn->sock;
	}
	
	FD_SET(conn->svr_sock, &fds);
	tv = TCPTIMEOUT;
	
	for ( ;; ) {
		res = select(MAX(nfds, conn->svr_sock)+1, &fds, NULL, NULL, &tv);
	
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
	
	if (conn->sock != INVALID_SOCKET && FD_ISSET(conn->sock, &fds))
		return 1;
		
	if (FD_ISSET(conn->svr_sock, &fds)) {
		fromlen = sizeof(addr);
		ns = accept(conn->svr_sock, &addr, &fromlen);
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
		
		conn->sock = ns;
		return 0;
	}
		
	fprintf(stderr, "select on bad socket\n");
	return -1;
}

/*
 * Dispatch a command to the server
 *
 * proxy_tcp_svr_dispatch() should never fail. If we get a read error from the client then we just
 * assume the client has gone away. Errors from server commands are just reported back to the
 * client.
 */
static void
proxy_tcp_svr_dispatch(void *data)
{
	int					i;
	int					n;
	int					res;
	char *				msg;
	char **				args;
	char *				response;
	proxy_tcp_svr_func * sf;
	proxy_tcp_conn *		conn = (proxy_tcp_conn *)data;
	
	n = proxy_tcp_recv_msg(conn, &msg);
	if (n <= 0)
		return;
	
	args = Str2Args(msg);
	
	response = NULL;

	for (i = 0; i < sizeof(proxy_tcp_svr_func_tab) / sizeof(proxy_tcp_svr_func); i++) {
		sf = &proxy_tcp_svr_func_tab[i];
		if (sf->cmd != NULL && strcmp(args[0], sf->cmd) == 0) {
			res = sf->func(args, &response);
			break;
		}
	}
	
	FreeArgs(args);
	free(msg);
	
	if (res != 0) {
		asprintf(&response, "-1 some nasty error ocurred");
	}
	
	(void)proxy_tcp_send_msg(conn, response, strlen(response));
	free(response);
}

static int 
proxy_tcp_svr_setlinebreakpoint(char **args, char **response)
{
	char *		file;
	int			line;
	
	file = args[1];
	line = atoi(args[2]);
	
	asprintf(response, "0 setting line breakpoint %s %d", file, line);
	
	return 0;
}

static int 
proxy_tcp_svr_quit(char **args, char **response)
{
	asprintf(response, "0 quitting");
	proxy_tcp_svr_shutdown++;
	return 0;
}