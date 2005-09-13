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

int called_back;

void
event_callback(dbg_event *e)
{
	printf("got callback\n");
	called_back++;
}

void
wait_for_event(session *s)
{
	for (called_back = 0; !called_back; )
		DbgProgress(s, event_callback);
}

int
main(int argc, char *argv[])
{
	int		i;
	session *s;
	procset *p;
	
	if (DbgInit(&s, "tcp", "host", "localhost", "port", PROXY_TCP_PORT, NULL) < 0) {
		fprintf(stderr, "DbgInit failed\n");
		exit(1);
	}
	
	DbgCreateProcSet(100, &p);
	for (i = 0; i < 10; i++)
		DbgAddProcToSet(p, i * 3 + 1);
	
	for (i = 0; i < 10; i++) {
		DbgSetLineBreakpoint(s, p, "test.c", 23, NULL);
	
		wait_for_event(s);
	}
	
	DbgQuit(s);
	
	wait_for_event(s);
	
	exit(0);
}