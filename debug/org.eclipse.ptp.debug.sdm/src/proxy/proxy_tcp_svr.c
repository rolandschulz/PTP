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

#include <stdio.h>
#include <string.h>

#include "compat.h"
#include "session.h"
#include "proxy.h"
#include "proxy_tcp.h"

static int proxy_tcp_svr_dispatch(int);

proxy_svr_funcs proxy_tcp_svr_funcs =
{
	proxy_tcp_svr_dispatch,
};

struct proxy_tcp_svr_func {
	char *cmd;
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
proxy_tcp_svr_dispatch(SOCKET fd)
{
	int					i;
	int					n;
	int					res;
	char *				command;
	char *				response;
	char *				cmd;
	proxy_tcp_svr_func * sf;
	
	n = proxy_tcp_recv(fd, &command);
	if (n <= 0)
		return -1;
	
	cmd = getword(&command);
	
	response = NULL;

	for (i = 0; i < sizeof(proxy_tcp_svr_func_tab) / sizeof(proxy_tcp_svr_func); i++) {
		sf = &proxy_tcp_svr_func_tab[i];
		if (sf->cmd != NULL && strcmp(cmd, sf->cmd) == 0)
			res = sf->func(command, &response);
	}
	
	free(command);
	free(cmd);
	
	if (res == 0) {
		if (proxy_tcp_send(fd, response, strlen(response)) < 0 ) {
			free(response);
			return -1;
		}
		free(response);
	}
	
	return res;
}

static int 
proxy_tcp_svr_setlinebreakpoint(char *args, char **response)
{
	char *		file;
	char *		line_str;
	int			line;
	
	file = getword(&args);
	line_str = getword(&args);
	line = atoi(line_str);
	free(line_str);
	
	asprintf(response, "0 setting line breakpoint %s %d\n", file, line);
	
	free(file);
	
	return 0;
}

static int 
proxy_tcp_svr_quit(char *args, char **response)
{       
	asprintf(response, "0 quitting\n");
	return 0;
}  