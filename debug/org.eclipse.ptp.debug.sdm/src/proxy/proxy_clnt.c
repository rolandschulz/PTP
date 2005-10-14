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
 * Miscellaneous proxy functions.
 */
 
#include <stdio.h>

#include "session.h"
#include "proxy.h"
#include "proxy_tcp.h"
#include "dbg.h"
 
proxy proxies[] = {
	{"tcp", &proxy_tcp_clnt_funcs, NULL, NULL, NULL},
	{NULL, NULL, NULL, NULL, NULL}
};

int 
proxy_clnt_init(proxy *p, proxy_clnt_helper_funcs *funcs, void **data, char *attr, va_list ap)
{
	if (p != NULL)
		return p->clnt_funcs->init(funcs, data, attr, ap);
		
	return -1;
}

int 
proxy_clnt_connect(proxy *p, void *data)
{
	if (p != NULL)
		return p->clnt_funcs->connect(data);
		
	return -1;
}

int 
proxy_clnt_create(proxy *p, void *data)
{
	if (p != NULL)
		return p->clnt_funcs->create(data);
		
	return -1;
}

int
proxy_clnt_progress(proxy *p, void *data)
{
	if (p != NULL)
		return p->clnt_funcs->progress(data);
		
	return -1;
}

int
proxy_clnt_sendcmd(proxy *p, void *data, char *cmd, char *fmt, ...)
{
	int		res = -1;
	va_list	ap;
	
	if (p != NULL) {
		va_start(ap, fmt);
		res = p->clnt_funcs->sendcmd(data, cmd, fmt, ap);
		va_end(ap);
	}
	
	return res;
}

int 
proxy_clnt_quit(proxy *p, void *data)
{
	return proxy_clnt_sendcmd(p, data, PROXY_QUIT_CMD, NULL);
}

