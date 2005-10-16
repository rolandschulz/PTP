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
	{"tcp", NULL, &proxy_tcp_clnt_funcs, NULL},
	{NULL, NULL, NULL, NULL}
};

int 
proxy_clnt_init(char *name, proxy_handler_funcs *pf, proxy_clnt_helper_funcs *cf, proxy_clnt **clnt, char *attr, va_list ap)
{
	proxy *		p;
	proxy_clnt *	pc;
	void *		data;
	
	if (find_proxy(name, &p) < 0)
		return PROXY_RES_ERR;
		
	p->handler_funcs = pf;

	pc = (proxy_clnt *)malloc(sizeof(proxy_clnt));
	pc->proxy = p;
	pc->clnt_helper_funcs = cf;
	
	if (p->clnt_funcs->init(pc, &data, attr, ap) < 0) {
		free(pc);
		return PROXY_RES_ERR;
	}

	pc->clnt_data = data;
	
	*clnt = pc;
	
	return PROXY_RES_OK;	
}

int 
proxy_clnt_connect(proxy_clnt *pc)
{
	if (pc != NULL)
		return pc->proxy->clnt_funcs->connect(pc);
		
	return PROXY_RES_ERR;
}

int 
proxy_clnt_create(proxy_clnt *pc)
{
	if (pc != NULL)
		return pc->proxy->clnt_funcs->create(pc);
		
	return PROXY_RES_ERR;
}

int
proxy_clnt_progress(proxy_clnt *pc)
{
	if (pc != NULL)
		return pc->proxy->clnt_funcs->progress(pc);
		
	return PROXY_RES_ERR;
}

int
proxy_clnt_sendcmd(proxy_clnt *pc, char *cmd, char *fmt, ...)
{
	int		res = PROXY_RES_ERR;
	va_list	ap;
	
	if (pc != NULL) {
		va_start(ap, fmt);
		res = pc->proxy->clnt_funcs->sendcmd(pc, cmd, fmt, ap);
		va_end(ap);
	}
	
	return res;
}

int 
proxy_clnt_quit(proxy_clnt *pc)
{
	return proxy_clnt_sendcmd(pc, PROXY_QUIT_CMD, NULL);
}

void
proxy_clnt_event_callback(proxy_clnt *pc, char *data)
{
	if (pc != NULL)
		proxy_event_callback(pc->proxy, data);
}
