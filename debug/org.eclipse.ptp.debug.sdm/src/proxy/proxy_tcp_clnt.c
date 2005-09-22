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
 
#include <sys/types.h>
#include <sys/socket.h>
#include <sys/select.h>
#include <arpa/inet.h>
#include <netdb.h>

#include <string.h>
#include <errno.h>
#include <stdio.h>
#include <unistd.h>

#include "compat.h"
#include "session.h"
#include "proxy.h"
#include "proxy_tcp.h"

static int proxy_tcp_clnt_init(void **, char *, va_list);
static int proxy_tcp_clnt_setlinebreakpoint(void *, procset *, char *, int);
static int proxy_tcp_clnt_quit(void *);
static int proxy_tcp_clnt_progress(void *, void (*)(dbg_event *));

proxy_clnt_funcs proxy_tcp_clnt_funcs =
{
	proxy_tcp_clnt_init,
	proxy_tcp_clnt_setlinebreakpoint,
	proxy_clnt_setfuncbreakpoint_not_imp,
	proxy_clnt_deletebreakpoint_not_imp,
	proxy_clnt_go_not_imp,
	proxy_clnt_step_not_imp,
	proxy_clnt_liststackframes_not_imp,
	proxy_clnt_setcurrentstackframe_not_imp,
	proxy_clnt_evaluateexpression_not_imp,
	proxy_clnt_gettype_not_imp,
	proxy_clnt_listlocalvariables_not_imp,
	proxy_clnt_listarguments_not_imp,
	proxy_clnt_listglobalvariables_not_imp,
	proxy_tcp_clnt_quit,
	proxy_tcp_clnt_progress,
};


/**
 * Connect to a remote proxy.
 * 
 * @return conn structure that can be used for subsequent proxy requests.
 */
int
proxy_tcp_client_connect(char *host, int port, proxy_tcp_conn **cp)
{
	SOCKET				sd;
	struct hostent *		hp;
	long int				haddr;
	struct sockaddr_in	scket;
	proxy_tcp_conn *		conn;
	        
	hp = gethostbyname(host);
	        
	if (hp == (struct hostent *)NULL) {
		fprintf(stderr, "could not find host \"%s\"\n", host);
		return -1;
	}
	
	haddr = ((hp->h_addr[0] & 0xff) << 24) |
			((hp->h_addr[1] & 0xff) << 16) |
			((hp->h_addr[2] & 0xff) <<  8) |
			((hp->h_addr[3] & 0xff) <<  0);
	
	if ( (sd = socket(PF_INET, SOCK_STREAM, 0)) == INVALID_SOCKET )
	{
		perror("socket");
		return -1;
	}
	
	memset (&scket,0,sizeof(scket));
	scket.sin_family = PF_INET;
	scket.sin_port = htons((u_short) port);
	scket.sin_addr.s_addr = htonl(haddr);
	
	if ( connect(sd, (struct sockaddr *) &scket, sizeof(scket)) == SOCKET_ERROR )
	{
		perror("connect");
		CLOSE_SOCKET(sd);
		return -1;
	}
	
	proxy_tcp_create_conn(&conn);
	
	conn->sock = sd;
	conn->host = strdup(host);
	conn->port = port;

	*cp = conn;
	
	return 0;
}

/*
 * CLIENT FUNCTIONS
 */
static int
proxy_tcp_clnt_init(void **data, char *attr, va_list ap)
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
	
	if (proxy_tcp_client_connect(host, port, &conn) < 0)
		return -1;
	
	*data = (void *)conn;
	
	return 0;
}

static int
proxy_tcp_clnt_setlinebreakpoint(void *data, procset *set, char *file, int line)
{
	proxy_tcp_conn *	conn = (proxy_tcp_conn *)data;
	char *			request;
	char *			esc_file;
	char *			procs;

	procs = procset_to_str(set);
	
	if ( file == NULL )
		asprintf(&esc_file, "<null>");
	else
		asprintf(&esc_file, "\"%s\"", file);
	        
	asprintf(&request, "SLB %s %s %d", procs, esc_file, line);
	
	free(procs);
	free(esc_file);
	
	if ( proxy_tcp_send_msg(conn, request, strlen(request)) < 0 )
	{
	        free(request);
	        return -1;
	}
	
	free(request);
	
	return 0;
}


static int
proxy_tcp_clnt_quit(void *data)
{
	proxy_tcp_conn *	conn = (proxy_tcp_conn *)data;
	char *			request;
	
	asprintf(&request, "QUI");
	
	if ( proxy_tcp_send_msg(conn, request, strlen(request)) < 0 )
	{
	        free(request);
	        return -1;
	}
	
	free(request);
	
	return 0;
}

static int 
proxy_tcp_clnt_progress(void *data, void (*event_callback)(dbg_event *))
{
	proxy_tcp_conn *	conn = (proxy_tcp_conn *)data;
	fd_set			fds;
	int				res;
	char *			result;
	dbg_event *		ev;
	struct timeval	tv;

	FD_ZERO(&fds);
	FD_SET(conn->sock, &fds);
	tv = TCPTIMEOUT;
	
	for ( ;; ) {
		res = select(conn->sock+1, &fds, NULL, NULL, &tv);
	
		switch (res) {
		case INVALID_SOCKET:
			if ( errno == EINTR )
				continue;
		
			perror("select");
			return -1;
		
		case 0:
			return 0;
		
		default:
			if ( !FD_ISSET(conn->sock, &fds) )
			{
				fprintf(stderr, "select on bad socket\n");
				return -1;
			}
			break;
		}
	
		break;
	}
	
	res = proxy_tcp_recv_msg(conn, &result);
	if (res <= 0) {
		return res;
	}
	
	if (proxy_tcp_str_to_event(result, &ev) < 0) {
		fprintf(stderr, "bad response");
		free(result);
		return -1;
	}
	
	free(result);
	
	event_callback(ev);
	
	return 0;
}