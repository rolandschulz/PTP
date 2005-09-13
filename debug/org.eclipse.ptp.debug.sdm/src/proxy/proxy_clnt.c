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
	{"tcp", &proxy_tcp_clnt_funcs, NULL},
	{NULL, NULL, NULL}
};

int 
proxy_clnt_init_not_imp(void **data, char *attr, va_list ap)
{
	DbgSetError(DBGERR_NOTIMP, NULL);
	return -1;
}

int 
proxy_clnt_setlinebreakpoint_not_imp(void *data, procset *p, char *file, int l, breakpoint *bp)
{
	DbgSetError(DBGERR_NOTIMP, NULL);
	return -1;
}

int 
proxy_clnt_setfuncbreakpoint_not_imp(void *data, procset *p, char *file, char *func, breakpoint *bp)
{
	DbgSetError(DBGERR_NOTIMP, NULL);
	return -1;
}

int 
proxy_clnt_deletebreakpoints_not_imp(void *data, procset *p, breakpoint *bp)
{
	DbgSetError(DBGERR_NOTIMP, NULL);
	return -1;
}

int
proxy_clnt_go_not_imp(void *data, procset *p)
{
	DbgSetError(DBGERR_NOTIMP, NULL);
	return -1;
}

int 
proxy_clnt_step_not_imp(void *data, procset *p, int count, int type)
{
	DbgSetError(DBGERR_NOTIMP, NULL);
	return -1;
}

int 
proxy_clnt_liststackframes_not_imp(void *data, int p, stackframelist *list)
{
	DbgSetError(DBGERR_NOTIMP, NULL);
	return -1;
}

int 
proxy_clnt_setcurrentstackframe_not_imp(void *data, int p, int count, int type, stackframe *frame)
{
	DbgSetError(DBGERR_NOTIMP, NULL);
	return -1;
}

int 
proxy_clnt_evaluateexpression_not_imp(void *data, int p, char *expr)
{
	DbgSetError(DBGERR_NOTIMP, NULL);
	return -1;
}

int 
proxy_clnt_listlocalvariables_not_imp(void *data, int p, stackframe *frame)
{
	DbgSetError(DBGERR_NOTIMP, NULL);
	return -1;
}

int 
proxy_clnt_listarguments_not_imp(void *data, int p, stackframe *frame)
{
	DbgSetError(DBGERR_NOTIMP, NULL);
	return -1;
}

int 
proxy_clnt_listglobalvariables_not_imp(void *data, int p)
{
	DbgSetError(DBGERR_NOTIMP, NULL);
	return -1;
}

int 
proxy_clnt_quit_not_imp(void *data)
{
	DbgSetError(DBGERR_NOTIMP, NULL);
	return -1;
}

int 
proxy_clnt_progress_not_imp(void *data, void (*cb)(dbg_event *))
{
	DbgSetError(DBGERR_NOTIMP, NULL);
	return -1;
}
