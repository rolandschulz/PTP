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
 
 #include "session.h"
 #include "proxy.h"

extern proxy_svr_funcs 	proxy_tcp_svr_funcs;

proxy proxies[] = {
	{"tcp", NULL, NULL,  &proxy_tcp_svr_funcs, NULL},
	{NULL, NULL, NULL, NULL, NULL}
};

void
proxy_svr_init(proxy *p, proxy_svr_helper_funcs *funcs, void **data)
{
	if (p != NULL)
		p->svr_funcs->init(funcs, data);

	p->svr_helper_funcs = funcs;
}

int
proxy_svr_create(proxy *p, int port, void *data)
{
	if (p != NULL)
		return p->svr_funcs->create(port, data);
		
	return -1;
}

int
proxy_svr_connect(proxy *p, char *host, int port, void *data)
{
	if (p != NULL)
		return p->svr_funcs->connect(host, port, data);
		
	return -1;
}

int
proxy_svr_progress(proxy *p, void *data)
{
	if (p != NULL)
		return p->svr_funcs->progress(data);
		
	return -1;
}

void
proxy_svr_finish(proxy *p, void *data)
{
	if (p != NULL)
		p->svr_funcs->finish(data);
}
