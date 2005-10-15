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
 
#ifdef __gnu_linux__
#define _GNU_SOURCE
#endif

#include <sys/types.h>
#include <sys/socket.h>
#include <sys/select.h>
#include <arpa/inet.h>
#include <netdb.h>

#include <string.h>
#include <errno.h>
#include <stdio.h>
#include <stdarg.h>
#include <unistd.h>

#include "compat.h"
#include "session.h"
#include "proxy.h"
#include "proxy_tcp.h"
#include "handler.h"

struct timeval	SELECT_TIMEOUT = {0, 1000};

static int proxy_tcp_clnt_init(proxy_clnt_helper_funcs *, void **, char *, va_list);
static int proxy_tcp_clnt_connect(void *);
static int proxy_tcp_clnt_create(void *);
static int proxy_tcp_clnt_accept(int, void *);
static int proxy_tcp_clnt_progress(void *);
static int proxy_tcp_clnt_sendcmd(void *, char *, char *, va_list);

proxy_clnt_funcs proxy_tcp_clnt_funcs =
{
	proxy_tcp_clnt_init,
	proxy_tcp_clnt_connect,
	proxy_tcp_clnt_create,
	proxy_tcp_clnt_progress,
	proxy_tcp_clnt_sendcmd,
};

static int
proxy_tcp_clnt_recv_msgs(int fd, void *data)
{
	proxy_tcp_conn *			conn = (proxy_tcp_conn *)data;
	proxy_clnt_helper_funcs *	helper = (proxy_clnt_helper_funcs *)conn->helper;
	
	if (proxy_tcp_recv_msgs(conn) < 0) {
		helper->unregfile(conn->sess_sock);
		return PROXY_RES_ERR;
	}
	
	return PROXY_RES_OK;
}

/*
 * CLIENT FUNCTIONS
 */
static int
proxy_tcp_clnt_init(proxy_clnt_helper_funcs *helper, void **data, char *attr, va_list ap)
{
	int					port;
	char *				host;
	proxy_tcp_conn *		conn;
	
	while (attr != NULL) {
		if (strcmp(attr, "host") == 0)
			host = strdup(va_arg(ap, char *));
		else if (strcmp(attr, "port") == 0)
			port = va_arg(ap, int);
			
		attr = va_arg(ap, char *);
	}

	proxy_tcp_create_conn(&conn);
	
	conn->host = strdup(host);
	conn->port = port;
	conn->helper = (void *)helper;

	*data = (void *)conn;
	
	return PROXY_RES_OK;
}
		
/**
 * Connect to a remote proxy.
 */
static int
proxy_tcp_clnt_connect(void *data)
{
	SOCKET					sd;
	struct hostent *			hp;
	long int					haddr;
	struct sockaddr_in		scket;
	proxy_tcp_conn *			conn = (proxy_tcp_conn *)data;
	proxy_clnt_helper_funcs *	helper = (proxy_clnt_helper_funcs *)conn->helper;
		        
	if (conn->host == NULL) {
		proxy_set_error(PROXY_ERR_CLIENT, "no host specified");
		return PROXY_RES_ERR;
	}
	
	hp = gethostbyname(conn->host);
	        
	if (hp == (struct hostent *)NULL) {
		proxy_set_error(PROXY_ERR_CLIENT, "could not find host");
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
	scket.sin_port = htons((u_short) conn->port);
	scket.sin_addr.s_addr = htonl(haddr);
	
	if ( connect(sd, (struct sockaddr *) &scket, sizeof(scket)) == SOCKET_ERROR )
	{
		proxy_set_error(PROXY_ERR_SYSTEM, strerror(errno));
		CLOSE_SOCKET(sd);
		return PROXY_RES_ERR;
	}

	conn->sess_sock = sd;
	conn->connected++;
	
	helper->regfile(sd, READ_FILE_HANDLER, proxy_tcp_clnt_recv_msgs, (void *)conn);
	
	return PROXY_RES_OK;
}

static int 
proxy_tcp_clnt_create(void *data)
{
	socklen_t				slen;
	SOCKET					sd;
	struct sockaddr_in		sname;
	proxy_tcp_conn *			conn = (proxy_tcp_conn *)data;
	proxy_clnt_helper_funcs *	helper = (proxy_clnt_helper_funcs *)conn->helper;
	
	if ( (sd = socket(PF_INET, SOCK_STREAM, 0)) == INVALID_SOCKET )
	{
		proxy_set_error(PROXY_ERR_SYSTEM, strerror(errno));
		return PROXY_RES_ERR;
	}
	
	memset (&sname, 0, sizeof(sname));
	sname.sin_family = PF_INET;
	sname.sin_port = htons(conn->port);
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
	
	helper->regfile(sd, READ_FILE_HANDLER, proxy_tcp_clnt_accept, (void *)conn);
	
	return PROXY_RES_OK;
}

static int
proxy_tcp_clnt_accept(int fd, void *data)
{
	SOCKET					ns;
	socklen_t				fromlen;
	struct sockaddr			addr;
	proxy_tcp_conn *			conn = (proxy_tcp_conn *)data;
	proxy_clnt_helper_funcs *	helper = (proxy_clnt_helper_funcs *)conn->helper;
	
	fromlen = sizeof(addr);
	ns = accept(fd, &addr, &fromlen);
	if (ns < 0) {
		proxy_set_error(PROXY_ERR_SYSTEM, strerror(errno));
		return PROXY_RES_ERR;
	}
	
	/*
	 * Only allow one connection at a time.
	 */
	if (conn->sess_sock != INVALID_SOCKET) {
		CLOSE_SOCKET(ns); // reject
		return PROXY_RES_OK;
	}
	
	conn->sess_sock = ns;
	conn->connected++;

	helper->regfile(ns, READ_FILE_HANDLER, proxy_tcp_clnt_recv_msgs, (void *)conn);
	
	return PROXY_RES_OK;
	
}

static int 
proxy_tcp_clnt_progress(void *data)
{
	int						res;
	char *					result;
	proxy_event *			ev;
	proxy_tcp_conn *			conn = (proxy_tcp_conn *)data;
	proxy_clnt_helper_funcs *	helper = (proxy_clnt_helper_funcs *)conn->helper;

	if ((res = proxy_tcp_get_msg(conn, &result)) <= 0)
		return res;
	
	if (proxy_str_to_event(result, &ev) < 0) {
		ev = new_proxy_event(PROXY_EV_ERROR);
		ev->error_code = PROXY_ERR_PROTO;
	}
	
	free(result);
	
	helper->eventhandler(ev, helper->eventdata);
	
	return PROXY_RES_OK;
}

static int
proxy_tcp_clnt_sendcmd(void *data, char *cmd, char *fmt, va_list ap)
{
	char *					request;
	char *					args;
	proxy_tcp_conn *	conn = (proxy_tcp_conn *)data;
	proxy_clnt_helper_funcs *	helper = (proxy_clnt_helper_funcs *)conn->helper;
	
	if (!conn->connected)
		return PROXY_RES_ERR;
	
	if (fmt != NULL) {
		vasprintf(&args, fmt, ap);
		asprintf(&request, "%s %s", cmd, args);
		free(args);
	} else 
		request = strdup(cmd);
		
printf("tcp_clnt_send_cmd: <%s>\n", request);

	if ( proxy_tcp_send_msg(conn, request, strlen(request)) < 0 )
	{
			proxy_set_error(PROXY_ERR_CLIENT, "connection unexpectedly terminated");
			helper->unregfile(conn->sess_sock);
			free(request);
			return PROXY_RES_ERR;
	}
	
	free(request);
	
	return PROXY_RES_OK;
}
