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

proxy proxies[] = {
	{"tcp", proxy_tcp_clnt_funcs, NULL},
	{NULL, NULL}
};

int 
proxy_clnt_init_not_imp(void *data)
{
	DbgSetError(DBGERR_NOTIMP, NULL);
	return -1;
}

int 
proxy_clnt_setlinebreakpoint_not_imp(session *s, procset *p, char *file, int l, breakpoint *bp)
{
	DbgSetError(DBGERR_NOTIMP, NULL);
	return -1;
}

int 
proxy_clnt_setfuncbreakpoint_not_imp(session *s, procset *p, char *file, char *func, breakpoint *bp)
{
	DbgSetError(DBGERR_NOTIMP, NULL);
	return -1;
}

int 
proxy_clnt_deletebreakpoints_not_imp(session *s, procset *p, breakpoint *bp)
{
	DbgSetError(DBGERR_NOTIMP, NULL);
	return -1;
}

int
proxy_clnt_go_not_imp(session *s, procset *p)
{
	DbgSetError(DBGERR_NOTIMP, NULL);
	return -1;
}

int 
proxy_clnt_step_not_imp(session *s, procset *p, int count, int type)
{
	DbgSetError(DBGERR_NOTIMP, NULL);
	return -1;
}

int 
proxy_clnt_liststackframes_not_imp(session *s, int p, stackframelist *list)
{
	DbgSetError(DBGERR_NOTIMP, NULL);
	return -1;
}

int 
proxy_clnt_setcurrentstackframe_not_imp(session *s, int p, int count, int type, stackframe *frame)
{
	DbgSetError(DBGERR_NOTIMP, NULL);
	return -1;
}

int 
proxy_clnt_evaluateexpression_not_imp(session *s, int p, char *)
{
	DbgSetError(DBGERR_NOTIMP, NULL);
	return -1;
}

int 
proxy_clnt_listlocalvariables_not_imp(session *s, int p, stackframe *frame)
{
	DbgSetError(DBGERR_NOTIMP, NULL);
	return -1;
}

int 
proxy_clnt_listarguments_not_imp(session *s, int p, stackframe *frame)
{
	DbgSetError(DBGERR_NOTIMP, NULL);
	return -1;
}

int 
proxy_clnt_listglobalvariables_not_imp(session *s, int p)
{
	DbgSetError(DBGERR_NOTIMP, NULL);
	return -1;
}

int 
proxy_clnt_progress(session *s)
{
	DbgSetError(DBGERR_NOTIMP, NULL);
	return -1;
}
