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
#include <stdarg.h>
#include <unistd.h>

#include "compat.h"
#include "session.h"
#include "proxy.h"
#include "proxy_tcp.h"

struct timeval	SELECT_TIMEOUT = {0, 1000};

static int proxy_tcp_clnt_init(void **, char *, va_list);
static void proxy_tcp_clnt_regeventhandler(void *, void (*)(dbg_event *, void *), void *);
static int proxy_tcp_clnt_startsession(void *, char *, char *);
static int proxy_tcp_clnt_setlinebreakpoint(void *, procset *, char *, int);
static int proxy_tcp_clnt_setfuncbreakpoint(void *, procset *, char *, char *);
static int proxy_tcp_clnt_deletebreakpoint(void *, procset *, int);
static int proxy_tcp_clnt_go(void *, procset *);
static int proxy_tcp_clnt_step(void *, procset *, int, int);
static int proxy_tcp_clnt_liststackframes(void *, procset *, int);
static int proxy_tcp_clnt_setcurrentstackframe(void *, procset *, int);
static int proxy_tcp_clnt_evaluateexpression(void *, procset *, char *);
static int proxy_tcp_clnt_gettype(void *, procset *, char *);
static int proxy_tcp_clnt_listlocalvariables(void *, procset *);
static int proxy_tcp_clnt_listarguments(void *, procset *);
static int proxy_tcp_clnt_listglobalvariables(void *, procset *);
static int proxy_tcp_clnt_quit(void *);
static int proxy_tcp_clnt_progress(void *);

proxy_clnt_funcs proxy_tcp_clnt_funcs =
{
	proxy_tcp_clnt_init,
	proxy_tcp_clnt_regeventhandler,
	proxy_tcp_clnt_startsession,
	proxy_tcp_clnt_setlinebreakpoint,
	proxy_tcp_clnt_setfuncbreakpoint,
	proxy_tcp_clnt_deletebreakpoint,
	proxy_tcp_clnt_go,
	proxy_tcp_clnt_step,
	proxy_tcp_clnt_liststackframes,
	proxy_tcp_clnt_setcurrentstackframe,
	proxy_tcp_clnt_evaluateexpression,
	proxy_tcp_clnt_gettype,
	proxy_tcp_clnt_listlocalvariables,
	proxy_tcp_clnt_listarguments,
	proxy_tcp_clnt_listglobalvariables,
	proxy_tcp_clnt_quit,
	proxy_tcp_clnt_progress,
};


/**
 * Connect to a remote proxy.
 * 
 * @return conn structure that can be used for subsequent proxy requests.
 */
static int
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

static int
proxy_tcp_clnt_send_cmd(proxy_tcp_conn *conn, char *fmt, ...)
{
	va_list	ap;
	char *	request;
	
	va_start(ap, fmt);
	vasprintf(&request, fmt, ap);
	va_end(ap);
printf("tcp_clnt_send_cmd: <%s>\n", request);

	if ( proxy_tcp_send_msg(conn, request, strlen(request)) < 0 )
	{
	        free(request);
	        return -1;
	}
	
	free(request);
	
	return 0;
}

static void
fix_null(char **str)
{
	if (*str == NULL)
		*str = "";
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

static void
proxy_tcp_clnt_regeventhandler(void *data, void (*event_handler)(dbg_event *, void *), void *event_data)
{
	proxy_tcp_conn *	conn = (proxy_tcp_conn *)data;
	
	conn->event_handler = event_handler;
	conn->event_data = event_data;
}

static int
proxy_tcp_clnt_startsession(void *data, char *prog, char *args)
{
	proxy_tcp_conn *	conn = (proxy_tcp_conn *)data;
	
	fix_null(&prog);
	fix_null(&args);
	
	return proxy_tcp_clnt_send_cmd(conn, "INI \"%s\" \"%s\"", prog, args);
}

static int
proxy_tcp_clnt_setlinebreakpoint(void *data, procset *set, char *file, int line)
{
	int				res;
	char *			procs;
	proxy_tcp_conn *	conn = (proxy_tcp_conn *)data;

	procs = procset_to_str(set);
	
	fix_null(&file);
	        
	res = proxy_tcp_clnt_send_cmd(conn, "SLB %s \"%s\" %d", procs, file, line);
	
	free(procs);
		
	return res;
}

static int 
proxy_tcp_clnt_setfuncbreakpoint(void *data, procset *set, char *file, char *func)
{
	int				res;
	char *			procs;
	proxy_tcp_conn *	conn = (proxy_tcp_conn *)data;

	procs = procset_to_str(set);
	
	fix_null(&file);
	fix_null(&func);
		        
	res = proxy_tcp_clnt_send_cmd(conn, "SFB %s \"%s\" \"%s\"", procs, file, func);
	
	free(procs);
		
	return res;
}

static int 
proxy_tcp_clnt_deletebreakpoint(void *data, procset *set, int bpid)
{
	int				res;
	char *			procs;
	proxy_tcp_conn *	conn = (proxy_tcp_conn *)data;

	procs = procset_to_str(set);
	        
	res = proxy_tcp_clnt_send_cmd(conn, "DBP %s %d", procs, bpid);
	
	free(procs);
		
	return res;
}

static int
proxy_tcp_clnt_go(void *data, procset *set)
{
	int				res;
	char *			procs;
	proxy_tcp_conn *	conn = (proxy_tcp_conn *)data;

	procs = procset_to_str(set);
	        
	res = proxy_tcp_clnt_send_cmd(conn, "GOP %s", procs);
	
	free(procs);
		
	return res;
}

static int 
proxy_tcp_clnt_step(void *data, procset *set, int count, int type)
{
	int				res;
	char *			procs;
	proxy_tcp_conn *	conn = (proxy_tcp_conn *)data;

	procs = procset_to_str(set);
	        
	res = proxy_tcp_clnt_send_cmd(conn, "STP %s %d %d", procs, count, type);
	
	free(procs);
		
	return res;
}

static int 
proxy_tcp_clnt_liststackframes(void *data, procset *set, int current)
{
	int				res;
	char *			procs;
	proxy_tcp_conn *	conn = (proxy_tcp_conn *)data;

	procs = procset_to_str(set);
	        
	res = proxy_tcp_clnt_send_cmd(conn, "LSF %s %d", procs, current);
	
	free(procs);
		
	return res;
}

static int 
proxy_tcp_clnt_setcurrentstackframe(void *data, procset *set, int level)
{
	int				res;
	char *			procs;
	proxy_tcp_conn *	conn = (proxy_tcp_conn *)data;

	procs = procset_to_str(set);
	        
	res = proxy_tcp_clnt_send_cmd(conn, "SCS %s %d", procs, level);
	
	free(procs);
		
	return res;
}

static int 
proxy_tcp_clnt_evaluateexpression(void *data, procset *set, char *expr)
{
	int				res;
	char *			procs;
	proxy_tcp_conn *	conn = (proxy_tcp_conn *)data;

	procs = procset_to_str(set);
	
	fix_null(&expr);
	
	res = proxy_tcp_clnt_send_cmd(conn, "EEX %s \"%s\"", procs, expr);
	
	free(procs);
		
	return res;
}

static int 
proxy_tcp_clnt_gettype(void *data, procset *set, char *expr)
{
	int				res;
	char *			procs;
	proxy_tcp_conn *	conn = (proxy_tcp_conn *)data;

	procs = procset_to_str(set);
	
	fix_null(&expr);
	
	res = proxy_tcp_clnt_send_cmd(conn, "TYP %s \"%s\"", procs, expr);
	
	free(procs);
		
	return res;
}

static int 
proxy_tcp_clnt_listlocalvariables(void *data, procset *set)
{
	int				res;
	char *			procs;
	proxy_tcp_conn *	conn = (proxy_tcp_conn *)data;

	procs = procset_to_str(set);
	        
	res = proxy_tcp_clnt_send_cmd(conn, "LLV %s", procs);
	
	free(procs);
		
	return res;
}

static int 
proxy_tcp_clnt_listarguments(void *data, procset *set)
{
	int				res;
	char *			procs;
	proxy_tcp_conn *	conn = (proxy_tcp_conn *)data;

	procs = procset_to_str(set);
	        
	res = proxy_tcp_clnt_send_cmd(conn, "LAR %s", procs);
	
	free(procs);
		
	return res;
}

static int 
proxy_tcp_clnt_listglobalvariables(void *data, procset *set)
{
	int				res;
	char *			procs;
	proxy_tcp_conn *	conn = (proxy_tcp_conn *)data;

	procs = procset_to_str(set);
	        
	res = proxy_tcp_clnt_send_cmd(conn, "LGV %s", procs);
	
	free(procs);
		
	return res;
}

static int
proxy_tcp_clnt_quit(void *data)
{
	proxy_tcp_conn *	conn = (proxy_tcp_conn *)data;
	
	return proxy_tcp_clnt_send_cmd(conn, "QUI");
}

static int 
proxy_tcp_clnt_progress(void *data)
{
	proxy_tcp_conn *	conn = (proxy_tcp_conn *)data;
	fd_set			fds;
	int				res;
	char *			result;
	dbg_event *		ev;
	struct timeval	tv;

	FD_ZERO(&fds);
	FD_SET(conn->sock, &fds);
	tv = SELECT_TIMEOUT;
	
	for ( ;; ) {
		res = select(conn->sock+1, &fds, NULL, NULL, &tv);
	
		switch (res) {
		case INVALID_SOCKET:
			if ( errno == EINTR )
				continue;
		
			perror("select");
			return -1;
		
		case 0:
			break;
		
		default:
			if ( !FD_ISSET(conn->sock, &fds) )
			{
				fprintf(stderr, "select on bad socket\n");
				return -1;
			}
			
			if (proxy_tcp_recv_msgs(conn) < 0)
				return -1;
				
			break;
		}
	
		break;
	}
	
	if ((res = proxy_tcp_get_msg(conn, &result)) <= 0)
		return res;
	
	if (proxy_tcp_str_to_event(result, &ev) < 0) {
		fprintf(stderr, "bad response");
		free(result);
		return -1;
	}
	
	free(result);
	
	conn->event_handler(ev, conn->event_data);
	
	return 0;
}