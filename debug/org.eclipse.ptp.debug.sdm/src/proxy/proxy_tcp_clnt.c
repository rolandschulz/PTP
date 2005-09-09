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
#include "session.h"
#include "proxy.h"

static int proxy_tcp_clnt_init(void *);
static int proxy_tcp_clnt_setlinebreak(sessions *, procset *, char *, int , breakpoint *);
static int proxy_tcp_clnt_quit(void);

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
	proxy_clnt_progress_not_imp,
};
	
/*
 * CLIENT FUNCTIONS
 */
static int
proxy_tcp_clnt_init(void *data)
{
	proxy_tcp_conn *conn = (proxy_tcp_conn *)data;
	
}

static int
proxy_tcp_clnt_setlinebreakpoint(sessions *s, procset *set, char *file, int line, breakpoint *bp)
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


static int
proxy_tcp_clnt_quit(void)
{
	int			status;
	char *		request;
	char *		result;
	char *		s;
	char			par[1024];
	
	asprintf(&request, "QUIT\n");
	
	if ( proxy_send_request(request, &result, &status, NULL) < 0 )
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