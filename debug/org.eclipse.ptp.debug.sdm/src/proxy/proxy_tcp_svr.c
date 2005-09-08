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

static int proxy_tcp_svr_dispatch(int);

proxy_svr_funcs proxy_tcp_svr_funcs =
{
	proxy_tcp_svr_dispatch,
};

struct proxy_tcp_svr_func {
	char *cmd, 
	int (*func)(char *, char **);
};

typedef struct proxy_tcp_svr_func	proxy_tcp_svr_func;

static int proxy_tcp_svr_setlinebreakpoint(char *, char **);
static int proxy_tcp_svr_quit(char *, char **);

proxy_tcp_svr_func proxy_tcp_svr_func_tab[] =
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

static int
proxy_tcp_svr_dispatch(int *fd)
{
	int					i;
	int					res;
	char *				response;
	proxy_tcp_svr_func * sf;
	
	n = tcp_read(fd, &result);
	if (n <= 0)
		return -1;
	
	tail = getword(result, command);
	
	response = NULL;

	for (i = 0; i < sizeof(proxy_tcp_srv_func_tab) / sizeof(proxy_tcp_svr_func); i++) {
		sf = &proxy_tcp_srv_func_tab[i];
		if (sf->cmd != NULL && strcmp(command, sf->cmd) == 0)
			response = sf->func(tail, &response);
	}
	
	free(result);
	
	if (res == 0) {
		if (tcp_send(fd, response) < 0 ) {
			fprintf(stderr, "dbgsrv dbgsrv_soc soc_sendreply failed\n");
		}
		free(response);
	}
	
	return res;
}

static int 
proxy_tcp_svr_setlinebreakpoint(char *args, char **response)
{
	int             len;
	dbgevent_t *    e;
	char            par[1024];
	char *          file;
	int             line;
	char *          st_event;
	
	args = getword(args, par);
	file = StrSocEventDup(par);
	
	args = getword(args, par);
	line = atoi(par);
	
	e = DbgClntSetLineBreak(file, line);
	
	free(file);
	
	st_event = EventToSocProto(e);
	len = strlen(st_event) + 16;
	asprintf(*response, len-1, "%s\n", st_event);
	free(st_event);
	
	return 0;
}

static int 
proxy_tcp_svr_quit(char *args, char **response)
{       
        int             len;
        char *          st_event;
        
        st_event = EventToSocProto(DbgClntQuit());
        
        len = strlen(st_event) + 16;
        asprintf(*response, len-1, "%s\n",st_event);

        free(st_event);
        
        Shutdown++;
        
        return 0;
}  