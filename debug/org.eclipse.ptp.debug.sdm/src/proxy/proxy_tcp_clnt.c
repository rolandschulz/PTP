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
#include "dbg_error.h"

struct timeval	SELECT_TIMEOUT = {0, 1000};

static int proxy_tcp_clnt_init(proxy_clnt_helper_funcs *, void **, char *, va_list);
static int proxy_tcp_clnt_connect(void *);
static int proxy_tcp_clnt_create(void *);
static int proxy_tcp_clnt_accept(int, void *);
static int proxy_tcp_clnt_progress(void *);
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

proxy_clnt_funcs proxy_tcp_clnt_funcs =
{
	proxy_tcp_clnt_init,
	proxy_tcp_clnt_connect,
	proxy_tcp_clnt_create,
	proxy_tcp_clnt_progress,
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
};

static int
proxy_tcp_clnt_send_cmd(proxy_tcp_conn *conn, char *fmt, ...)
{
	va_list					ap;
	char *					request;
	proxy_clnt_helper_funcs *	helper = (proxy_clnt_helper_funcs *)conn->helper;
	
	if (!conn->connected)
		return -1;
		
	va_start(ap, fmt);
	vasprintf(&request, fmt, ap);
	va_end(ap);
printf("tcp_clnt_send_cmd: <%s>\n", request);

	if ( proxy_tcp_send_msg(conn, request, strlen(request)) < 0 )
	{
			DbgSetError(DBGERR_PROXY_TERM, NULL);
			helper->unregfile(conn->sess_sock);
			free(request);
			return -1;
	}
	
	free(request);
	
	return 0;
}

static int
proxy_tcp_clnt_recv_msgs(int fd, void *data)
{
	proxy_tcp_conn *			conn = (proxy_tcp_conn *)data;
	proxy_clnt_helper_funcs *	helper = (proxy_clnt_helper_funcs *)conn->helper;
	
	if (proxy_tcp_recv_msgs(conn) < 0) {
		helper->unregfile(conn->sess_sock);
		return -1;
	}
	
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
	
	return 0;
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
		DbgSetError(DBGERR_DEBUGGER, "no host specified");
		return -1;
	}
	
	hp = gethostbyname(conn->host);
	        
	if (hp == (struct hostent *)NULL) {
		DbgSetError(DBGERR_DEBUGGER, "could not find host");
		return -1;
	}
	
	haddr = ((hp->h_addr[0] & 0xff) << 24) |
			((hp->h_addr[1] & 0xff) << 16) |
			((hp->h_addr[2] & 0xff) <<  8) |
			((hp->h_addr[3] & 0xff) <<  0);
	
	if ( (sd = socket(PF_INET, SOCK_STREAM, 0)) == INVALID_SOCKET )
	{
		DbgSetError(DBGERR_SYSTEM, strerror(errno));
		return -1;
	}
	
	memset (&scket,0,sizeof(scket));
	scket.sin_family = PF_INET;
	scket.sin_port = htons((u_short) conn->port);
	scket.sin_addr.s_addr = htonl(haddr);
	
	if ( connect(sd, (struct sockaddr *) &scket, sizeof(scket)) == SOCKET_ERROR )
	{
		DbgSetError(DBGERR_SYSTEM, strerror(errno));
		CLOSE_SOCKET(sd);
		return -1;
	}

	conn->sess_sock = sd;
	conn->connected++;
	
	helper->regfile(sd, READ_FILE_HANDLER, proxy_tcp_clnt_recv_msgs, (void *)conn);
	//helper->regeventhandler(proxy_tcp_clnt_event_callback, (void *)conn);
	
	return 0;
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
		DbgSetError(DBGERR_SYSTEM, strerror(errno));
		return -1;
	}
	
	memset (&sname, 0, sizeof(sname));
	sname.sin_family = PF_INET;
	sname.sin_port = htons(conn->port);
	sname.sin_addr.s_addr = htonl(INADDR_ANY);
	
	if (bind(sd,(struct sockaddr *) &sname, sizeof(sname)) == SOCKET_ERROR )
	{
		DbgSetError(DBGERR_SYSTEM, strerror(errno));
		CLOSE_SOCKET(sd);
		return -1;
	}
	
	slen = sizeof(sname);
	
	if ( getsockname(sd, (struct sockaddr *)&sname, &slen) == SOCKET_ERROR )
	{
		DbgSetError(DBGERR_SYSTEM, strerror(errno));
		CLOSE_SOCKET(sd);
		return -1;
	}
	
	if ( listen(sd, 5) == SOCKET_ERROR )
	{
		DbgSetError(DBGERR_SYSTEM, strerror(errno));
		CLOSE_SOCKET(sd);
		return -1;
	}
	
	conn->svr_sock = sd;
	conn->port = (int) ntohs(sname.sin_port);
	
	if (helper != NULL)
		helper->regfile(sd, READ_FILE_HANDLER, proxy_tcp_clnt_accept, (void *)conn);
	
	return 0;
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
		perror("accept");
		return 0;
	}
	
	conn->sess_sock = ns;
	
	/*
	 * Only allow one connection at a time.
	 */
	if (conn->sess_sock != INVALID_SOCKET) {
		CLOSE_SOCKET(ns); // reject
		return 0;
	}
	
	conn->sess_sock = ns;
	
	if (helper != NULL)
		helper->regfile(ns, READ_FILE_HANDLER, proxy_tcp_clnt_recv_msgs, (void *)conn);
	
	return 0;
	
}

static int 
proxy_tcp_clnt_progress(void *data)
{
	int						res;
	char *					result;
	dbg_event *				ev;
	proxy_tcp_conn *			conn = (proxy_tcp_conn *)data;
	proxy_clnt_helper_funcs *	helper = (proxy_clnt_helper_funcs *)conn->helper;

	if ((res = proxy_tcp_get_msg(conn, &result)) == 0)
		return 0;
		
	if (res < 0) {
		DbgSetError(DBGERR_PROXY_TERM, NULL);
		return -1;
	}
	
	if (proxy_tcp_str_to_event(result, &ev) < 0) {
		ev = NewEvent(DBGEV_ERROR);
		ev->error_code = DBGERR_PROXY_PROTO;
		ev->error_msg = strdup("");
	}
	
	free(result);
	
	helper->eventhandler(ev, helper->eventdata);
	
	return 0;
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
