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
 * Client runs as task id num_procs, where num_procs are the number of processes
 * in the job being debugged, and is responsible for coordinating protocol
 * messages between the debug client interface (whatever that may be)
 * and the debug servers.
 * 
 * Note that there will be num_procs+1 [0..num_procs] processes in our 
 * communicator, where num_procs is the number of processes in the parallel 
 * job being debugged. To simplify the accounting, we use the task id of
 * num_procs as the client task id and [0..num_procs-1] for the server
 * task ids.
 */

#include <stdlib.h>
#include <string.h>
#include <unistd.h>

#include "dbg.h"
#include "dbg_client.h"
#include "procset.h"
#include "list.h"
#include "proxy.h"
#include "proxy_tcp.h"

#define TEST

#ifdef TEST
static int completed;

void 
process_event(dbg_event *e, void *data)
{
	stackframe *	f;
	
	completed++;
	
	if (e == NULL) {
		printf("got null event!\n");
		return;
	}
		
	switch (e->event) {
	case DBGEV_ERROR:
		printf("error: %s\n", e->error_msg);
		break;
	case DBGEV_OK:
		printf("command ok\n");
		break;
	case DBGEV_BPHIT:
		printf("hit breakpoint at line %d\n", e->bp->loc.line);
		break;
	case DBGEV_BPSET:
		printf("breakpoint set\n");
		break;
	case DBGEV_STEP:
		printf("step completed\n");
		break;
	case DBGEV_SIGNAL:
		printf("received signal %s\n", e->sig_name);
		break;
	case DBGEV_EXIT:
		printf("exited with status %d\n", e->exit_status);
		break;
	case DBGEV_DATA:
		printf("data is "); AIFPrint(stdout, 0, e->data); printf("\n");
		break;
	case DBGEV_FRAMES:
		printf("got frames:\n");
		for (SetList(e->list); (f = (stackframe *)GetListElement(e->list)) != NULL; ) {
			printf(" #%d %s() at %s:%d\n", f->level, f->loc.func, f->loc.file, f->loc.line);
		}
		break;
	default:
		printf("got event %d\n", e->event);
		break;
	}
}

void
wait_for_server(void)
{
	completed = 0;
	
	while (!completed) {
		DbgClntProgress();
		usleep(1000);
	}
}
#endif

void
reg_read_file_handler(int fd, int (*handler)(int, void *), void *data)
{
	DbgClntRegisterFileHandler(fd, READ_FILE_HANDLER, handler, data);
}

static int shutdown = 0;

void
shutdown_server(void)
{
	shutdown++;
}

proxy_svr_helper_funcs helper_funcs = {
	shutdown_server,
	reg_read_file_handler,
	DbgClntUnregisterFileHandler,
	DbgClntRegisterEventHandler,
	DbgClntProgress,
	DbgClntSetLineBreakpoint,
	DbgClntSetFuncBreakpoint,
	DbgClntDeleteBreakpoint,
	DbgClntGo,
	DbgClntStep,
	DbgClntListStackframes,
	DbgClntSetCurrentStackframe,
	DbgClntEvaluateExpression,
	DbgClntGetType,
	DbgClntListLocalVariables,
	DbgClntListArguments,
	DbgClntListGlobalVariables
};

void 
client(int num_servers, int task_id, proxy *p)
{
	int		i;
#ifndef TEST
	int		stat;
	void *	pc;
#else
	procset *p1, *p2;
#endif

	DbgClntInit(num_servers);
	
#ifndef TEST
	
	proxy_svr_init(p, &helper_funcs);
	
	if (proxy_svr_create(p, &pc) < 0) {
		fprintf(stderr, "proxy_svr_create failed\n");
		return;
	}
	
	while (!shutdown) {
		if ((stat = proxy_svr_progress(p, pc)) < 0)
			break;
	}
		
	if (!shutdown && stat < 0)
		fprintf(stderr, "progress failed\n");
	
	proxy_svr_finish(p, pc);
	
#else

	p1 = procset_new(num_servers);
	for (i = 0; i < num_servers; i++)
		procset_add_proc(p1, i);
		
	p2 = procset_new(num_servers);
	procset_add_proc(p2, 0);

	DbgClntRegisterEventHandler(process_event, NULL);

	
	DbgClntStartSession("yyy", NULL);

	wait_for_server();	
	DbgClntStartSession("xxx", NULL);
	wait_for_server();
	DbgClntSetLineBreakpoint(p1, "yyy.c", 6);
	wait_for_server();
	DbgClntSetLineBreakpoint(p1, "xxx.c", 99);
	wait_for_server();
	DbgClntSetLineBreakpoint(p1, "xxx.c", 14);
	wait_for_server();
	DbgClntGo(p1);
	wait_for_server();
	DbgClntListStackframes(p1, 0);
	wait_for_server();
	DbgClntStep(p1, 1, 0);
	wait_for_server();
	DbgClntStep(p1, 1, 0);
	wait_for_server();
	DbgClntStep(p2, 1, 0);
	wait_for_server();
	DbgClntEvaluateExpression(p1, "a");
	wait_for_server();
	DbgClntListStackframes(p1, 1);
	wait_for_server();
	DbgClntSetFuncBreakpoint(p1, "xxx.c", "b");
	wait_for_server();
	DbgClntGo(p1);
	wait_for_server();
	DbgClntListStackframes(p1, 1);
	wait_for_server();
	DbgClntListStackframes(p1, 0);
	wait_for_server();
	DbgClntGo(p1);
	wait_for_server();
	
	DbgClntQuit();
	wait_for_server();
	
#endif
}
