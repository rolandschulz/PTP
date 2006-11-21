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
#include <stdlib.h>

#include "dbg.h"
#include "proxy.h"
#include "proxy_tcp.h"
#include "bitset.h"
#include "itimer.h"

int 			completed;
int			fatal;
bitset *		procs_outstanding = NULL;
bitset *		sess_procs = NULL;

void
event_callback(dbg_event *e, void *data)
{
	stackframe *	f;
	char *		s;
	
	printf("in event callback\n");
	
	if (e == NULL) {
		printf("got null event!\n");
		return;
	}
	
	if (e->procs != NULL) {
		if (sess_procs == NULL)
			sess_procs = bitset_dup(e->procs);
		s = bitset_to_set(e->procs);
		printf("%s ", s);
		free(s);
	}
		
	printf("-> ");
	
	switch (e->event) {
	case DBGEV_ERROR:
		printf("error: %s\n", e->dbg_event_u.error_event.error_msg);
		switch (e->dbg_event_u.error_event.error_code) {
		case DBGERR_NOBACKEND:
			fatal++;
			break;
		default:
			break;
		}
		break;
	case DBGEV_OK:
		printf("command ok\n");
		break;
	case DBGEV_INIT:
		printf("debugger initilized\n");
		break;
	case DBGEV_SUSPEND:
		switch (e->dbg_event_u.suspend_event.reason) {
		case DBGEV_SUSPEND_BPHIT:
			printf("hit breakpoint %d\n", e->dbg_event_u.suspend_event.ev_u.bpid);
			break;
		case DBGEV_SUSPEND_INT:
			printf("suspend completed\n");
			break;
		case DBGEV_SUSPEND_STEP:
			printf("step completed\n");
			break;
		case DBGEV_SUSPEND_SIGNAL:
			printf("received signal %s\n", e->dbg_event_u.suspend_event.ev_u.sig->name);
			break;
		}
		break;
	case DBGEV_BPSET:
		printf("breakpoint set\n");
		break;
	case DBGEV_EXIT:
		switch (e->dbg_event_u.suspend_event.reason) {
		case DBGEV_EXIT_NORMAL:
			printf("exited with status %d\n", e->dbg_event_u.exit_event.ev_u.exit_status);
			break;
		case DBGEV_EXIT_SIGNAL:
			printf("exited with signal %s\n", e->dbg_event_u.exit_event.ev_u.sig->name);
			break;
		}
	case DBGEV_DATA:
		printf("data is "); AIFPrint(stdout, 0, e->dbg_event_u.data_event.data); printf("\n");
		break;
	case DBGEV_FRAMES:
		printf("got frames:\n");
		for (SetList(e->dbg_event_u.list); (f = (stackframe *)GetListElement(e->dbg_event_u.list)) != NULL; ) {
			printf(" #%d %s() at %s:%d\n", f->level, f->loc.func, f->loc.file, f->loc.line);
		}
		break;
	default:
		printf("got event %d\n", e->event);
		break;
	}

	if (e->procs != NULL && procs_outstanding != NULL) {
		bitset_invert(e->procs);
		bitset_andeq(procs_outstanding, e->procs);
		if (!bitset_isempty(procs_outstanding))
			return;
	}
	
	completed++;
}

int
wait_for_event(session *s, bitset *p)
{
	completed = 0;
	fatal = 0;

	printf("entering wait_for_event...\n");
	
	if (p != NULL)
		procs_outstanding = bitset_dup(p);
		
	while (!completed) {
		if (DbgProgress(s) < 0) {
			fprintf(stderr, "error: %s\n", DbgGetErrorStr());
			exit(1);
		}
	}
	
	if (p != NULL) {
		bitset_free(procs_outstanding);
		procs_outstanding = NULL;
	}
		
	/*
	 * Check a fatal error hasn't ocurred
	 */
	return fatal ? -1 : 0;
}

/*
 * Tell server to quit
 */
void
cleanup_and_exit(session *s, bitset *p)
{
	DbgQuit(s);
	wait_for_event(s, p);
	exit(0);
}

int
do_test(session *s, char *dir, char *exe)
{
	bitset *		p1;
	int			bpid = 54;
	itimer *		t;
	
	t = itimer_new("debug");
	itimer_start(t);
	
	if (DbgStartSession(s, dir, exe, NULL) < 0) {
		fprintf(stderr, "error: %s\n", DbgGetErrorStr());
		return 1;
	}
	if (wait_for_event(s, NULL) < 0) {
		cleanup_and_exit(s, NULL);
	}
	
	itimer_mark(t, "launch");
	
	p1 = sess_procs;
		
	if (DbgSetLineBreakpoint(s, p1, bpid++, "xxx.c", 17) < 0) {
		fprintf(stderr, "error: %s\n", DbgGetErrorStr());
		return 1;
	}
	wait_for_event(s, p1);
	
	itimer_mark(t, "breakpoint");
	
	if (DbgGo(s, p1) < 0) {
		fprintf(stderr, "error: %s\n", DbgGetErrorStr());
		return 1;
	}
	wait_for_event(s, p1);
	
	itimer_mark(t, "suspend");
		
	if (DbgListStackframes(s, p1, 0, 1) < 0) {
		fprintf(stderr, "error: %s\n", DbgGetErrorStr());
		return 1;
	}
	wait_for_event(s, p1);

	itimer_mark(t, "where");
	
	if (DbgStep(s, p1, 1, 0) < 0) {
		fprintf(stderr, "error: %s\n", DbgGetErrorStr());
		return 1;
	}
	wait_for_event(s, p1);
	
	itimer_mark(t, "step");

	if (DbgStep(s, p1, 1, 0) < 0) {
		fprintf(stderr, "error: %s\n", DbgGetErrorStr());
		return 1;
	}
	wait_for_event(s, p1);
	
	itimer_mark(t, "step");

	if (DbgEvaluateExpression(s, p1, "a") < 0) {
		fprintf(stderr, "error: %s\n", DbgGetErrorStr());
		return 1;
	}
	wait_for_event(s, p1);
	
	itimer_mark(t, "evaluate");

	if (DbgListStackframes(s, p1, 1, 1) < 0) {
		fprintf(stderr, "error: %s\n", DbgGetErrorStr());
		return 1;
	}
	wait_for_event(s, p1);
	
	itimer_mark(t, "where");

	if (DbgQuit(s) < 0) {
		fprintf(stderr, "error: %s\n", DbgGetErrorStr());
		return 1;
	}
	wait_for_event(s, p1);

	itimer_stop(t);
	itimer_print(t);
	itimer_free(t);
	
	exit(0);
}

