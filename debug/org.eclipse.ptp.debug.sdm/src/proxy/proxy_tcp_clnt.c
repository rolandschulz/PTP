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

#include <string.h>
#include <errno.h>
#include <stdio.h>

#include "compat.h"
#include "session.h"
#include "proxy.h"
#include "proxy_tcp.h"

static int proxy_tcp_clnt_init(void **, char *, ...);
static int proxy_tcp_clnt_setlinebreakpoint(void *, procset *, char *, int , breakpoint *);
static int proxy_tcp_clnt_quit(void *);
static int proxy_tcp_clnt_progress(void *, void (*)(dbg_event *));

proxy_clnt_funcs proxy_tcp_clnt_funcs =
{
	proxy_tcp_clnt_init,
	proxy_tcp_clnt_setlinebreakpoint,
	proxy_clnt_setfuncbreakpoint_not_imp,
	proxy_clnt_deletebreakpoints_not_imp,
	proxy_clnt_go_not_imp,
	proxy_clnt_step_not_imp,
	proxy_clnt_liststackframes_not_imp,
	proxy_clnt_setcurrentstackframe_not_imp,
	proxy_clnt_evaluateexpression_not_imp,
	proxy_clnt_listlocalvariables_not_imp,
	proxy_clnt_listarguments_not_imp,
	proxy_clnt_listglobalvariables_not_imp,
	proxy_tcp_clnt_quit,
	proxy_tcp_clnt_progress,
};
	
/*
 * CLIENT FUNCTIONS
 */
static int
proxy_tcp_clnt_init(void **data, char *attr, ...)
{
	va_list	ap;
	proxy_tcp_conn *conn = malloc(sizeof(proxy_tcp_conn));
	
	va_start(ap, attr);
	
	while (attr != NULL) {
		if (strcmp(attr, "host") == 0)
			conn->host = strdup(va_arg(ap, char *));
		else if (strcmp(attr, "port") == 0)
			conn->port = va_arg(ap, int);
			
		attr = va_arg(ap, char *);
	}
	
	va_end(ap);
	
	conn->sock = INVALID_SOCKET;
	
	*data = (void *)conn;
	
	return 0;
}

static int
proxy_tcp_clnt_setlinebreakpoint(void *data, procset *set, char *file, int line, breakpoint *bp)
{
	proxy_tcp_conn *	conn = (proxy_tcp_conn *)data;
	int				status;
	char *			request;
	char *			result;

	if ( file == NULL )
		file = "<null>";
	        
	asprintf(&request, "SETLINEBREAK %s %s %d\n", procset_to_str(set), file, line);
	
	if ( proxy_tcp_send_request(conn->sock, request, &result, &status, NULL) < 0 )
	{
	        free(request);
	        return DBGRES_ERR;
	}
	
	free(request);
	
	if ( status != DBGRES_OK ) {
	        free(result);
	        return status;
	}
	
	s = result;
	
	s = getword(s,par);
	status = atoi(par);
	
	if (status != DBGEV_BPSET) {
		if (status == DBGEV_ERROR) {
			s = getword(s,par);
			s = skipspace(s);
			DbgSetErr(atoi(par), s);
		}
		free(result);
		return status;
	}
}


static int
proxy_tcp_clnt_quit(void *data)
{
	proxy_tcp_conn *	conn = (proxy_tcp_conn *)data;
	int				status;
	char *			request;
	char *			result;
	char *			s;
	
	asprintf(&request, "QUIT\n");
	
	if ( proxy_tcp_send_request(conn->sock, request, &result, &status, NULL) < 0 )
	{
	        free(request);
	        return DBGRES_ERR;
	}
	
	free(request);
	
	if ( status != DBGRES_OK ) {
	        free(result);
	        return status;
	}
	
	s = result;
	
	s = getword(s,par);
	status = atoi(par);
	
	if (status != DBGEV_BPSET) {
		if (status == DBGEV_ERROR) {
			s = getword(s,par);
			s = skipspace(s);
			DbgSetErr(atoi(par), s);
		}
		free(result);
		return status;
	}
}

static int 
proxy_tcp_clnt_progress(void * data, void (*event_callback)(dbg_event *))
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
	
	res = proxy_tcp_recv(conn->sock, &result);
	if (res <= 0) {
		return -1;
	}
	
	if (proxy_tcp_result_to_event(result, &ev) < 0) {
		fprintf(stderr, "bad response");
		return -1;
	}
	
	event_callback(ev);
	
	return 0;
}