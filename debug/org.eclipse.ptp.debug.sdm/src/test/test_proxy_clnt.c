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

#include <stdio.h>

#include "dbg.h"
#include "proxy.h"
#include "proxy_tcp.h"
#include "procset.h"

int 			completed;
procset *	procs_outstanding;

void
event_callback(dbg_event *e, void *data)
{
	stackframe *	f;
	char *		s;
	
	if (e == NULL) {
		printf("got null event!\n");
		return;
	}
	
	s = procset_to_set(e->procs);
	printf("%s -> ", s);
	free(s);
	
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

	procset_invert(e->procs);
	procset_andeq(procs_outstanding, e->procs);
	if (procset_isempty(procs_outstanding))
		completed++;
}

void
wait_for_event(session *s, procset *p)
{
	completed = 0;
	
	if (p != NULL)
		procs_outstanding = procset_copy(p);
	else
		procs_outstanding = procset_copy(s->sess_procs); 
		
	while (!completed) {
		DbgProgress(s);
	}
		
	procset_free(procs_outstanding);
}

int
main(int argc, char *argv[])
{
	int		i;
	session *s;
	procset *p1;
	procset *p2;
	
	if (DbgInit(&s, "tcp", "host", "localhost", "port", PROXY_TCP_PORT, NULL) < 0) {
		fprintf(stderr, "DbgInit failed\n");
		exit(1);
	}
	
	DbgRegisterEventHandler(s, event_callback, NULL);
	
	s->sess_procs = procset_new(4); //TODO this should happen automatically!!!!
	
	DbgCreateProcSet(4, &p1);
	for (i = 0; i < 4; i++)
		DbgAddProcToSet(p1, i);
	DbgCreateProcSet(4, &p2);
	DbgAddProcToSet(p2, 0);
	
	DbgStartSession(s, "yyy", NULL);
	wait_for_event(s, NULL);
		
	DbgStartSession(s, "xxx", NULL);
	wait_for_event(s, NULL);

	DbgSetLineBreakpoint(s, p1, "yyy.c", 6);
	wait_for_event(s, p1);
	
	DbgSetLineBreakpoint(s, p1, "xxx.c", 99);
	wait_for_event(s, p1);
	
	DbgSetLineBreakpoint(s, p1, "xxx.c", 14);
	wait_for_event(s, p1);
	
	DbgGo(s, p1);
	wait_for_event(s, p1);
	
	DbgListStackframes(s, p1, 0);
	wait_for_event(s, p1);
	
	DbgStep(s, p1, 1, 0);
	wait_for_event(s, p1);
	
	DbgStep(s, p1, 1, 0);
	wait_for_event(s, p1);
	
	DbgStep(s, p2, 1, 0);
	wait_for_event(s, p2);
	
	DbgEvaluateExpression(s, p1, "a");
	wait_for_event(s, p1);
	
	DbgListStackframes(s, p1, 1);
	wait_for_event(s, p1);
	
	DbgSetFuncBreakpoint(s, p1, "xxx.c", "b");
	wait_for_event(s, p1);
	
	DbgGo(s, p1);
	wait_for_event(s, p1);
	
	DbgListStackframes(s, p1, 1);
	wait_for_event(s, p1);
	
	DbgListStackframes(s, p1, 0);
	wait_for_event(s, p1);
	
	DbgGo(s, p1);
	wait_for_event(s, p1);

	DbgQuit(s);
	wait_for_event(s, NULL);

	exit(0);
}