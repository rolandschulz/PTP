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

extern int do_test(session *, char *);
extern void	event_callback(dbg_event *, void *);
extern int	wait_for_event(session *, bitset *);

int
main(int argc, char *argv[])
{
	session *	s;
	char *		exe;
	char *		host = "localhost";
	
	if (argc < 2) {
		fprintf(stderr, "usage: test_proxy_clnt exe [host]\n");
		return 1;
	}
	
	exe = argv[1];
	
	if (argc > 2)
		host = argv[2];
		
	if (DbgInit(&s, "tcp", "host", host, "port", PROXY_TCP_PORT, NULL) < 0) {
		fprintf(stderr, "DbgInit failed\n");
		exit(1);
	}
	
	DbgRegisterEventHandler(s, event_callback, NULL);

	if (DbgConnect(s) < 0) {
		fprintf(stderr, "error: %s\n", DbgGetErrorStr());
		return 1;
	}

	wait_for_event(s, NULL);

	return do_test(s, exe);
}

