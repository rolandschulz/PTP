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

#include "compat.h"
#include "proxy.h"
#include "breakpoint.h"

extern int proxy_tcp_setlinebreak(sessions *, procset *, char *, int , breakpoint *);

proxy_funcs proxy_tcp_funcs =
{
	proxy_tcp_init,
	proxy_tcp_setlinebreakpoint,
	proxy_setfuncbreakpoint_not_imp,
	proxy_deletebreakpoints_not_imp,
	proxy_go_not_imp,
	proxy_step_not_imp,
	proxy_liststackframes_not_imp,
	proxy_setcurrentstackframe_not_imp,
	proxy_evaluateexpression_not_imp,
	proxy_listlocalvariables_not_imp,
	proxy_listarguments_not_imp,
	proxy_listglobalvariables_not_imp,
	proxy_progress_not_imp,
};

static int
proxy_client_connect(char *host, int port, proxy_conn **cp)
{
	SOCKET                  sd;
	struct hostent *        hp;
	long int                haddr;
	struct sockaddr_in      scket;
	        
	*cp = (proxy_conn *) malloc(sizeof(proxy_conn));
	
	hp = gethostbyname(host);
	        
	if (hp == (struct hostent *)NULL) {
		fprintf(stderr, "could not find host \"%s\"\n", chost);
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
	
	cp->cl_fd = sd;
	
	return cp;
}

static int
proxy_send_request(char *request, char *reply)
{
}

static int
proxy_tcp_init(void *data)
{
	proxy_tcp_conn *conn = (proxy_tcp_conn *)data;
	
}

static int
proxy_tcp_setlinebreakpoint(sessions *s, procset *set, char *file, int line, breakpoint *bp)
{
	int			status;
	char *		request;
	char *		result;
	char *		s;
	char			par[1024];
	
	if ( file == NULL )
		file = "<null>";
	        
	asprintf(&request, "SETLINEBREAK %s %s %d\n", procset_to_str(set), file, line);
	
	if ( proxy_send_request(request, &result, &status, NULL) < 0 )
	{
	        fprintf(stderr,"DbgSetLineBP failed\n");
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
