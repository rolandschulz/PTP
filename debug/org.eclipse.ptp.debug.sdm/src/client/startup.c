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

#include <mpi.h>
#include	<getopt.h>
#include <stdlib.h>
#include <stdarg.h>

#include "backend.h"
#include "proxy.h"
#include "proxy_tcp.h"

#define DEFAULT_BACKEND	"gdb-mi"
#define DEFAULT_PROXY		"tcp"

extern void client(int, int, char *, char *, int);
extern void server(int, int, dbg_backend *);

static struct option longopts[] = {
	{"debugger",			required_argument,	NULL,	'b'},
	{"debugger_path",	required_argument,	NULL, 	'e'}, 
	{"proxy",			required_argument,	NULL, 	'P'}, 
	{"port",				required_argument,	NULL, 	'p'}, 
	{"host",				required_argument,	NULL, 	'h'}, 
	{NULL,				0,					NULL,	0}
};

void
error_msg(int rank, char *fmt, ...)
{
	va_list	ap;
	
	if (rank != 0)
		return;
		
	va_start(ap, fmt);
	vfprintf(stderr, fmt, ap);
	va_end(ap);
}

/*
 * Main entry point for MPI parallel debugger.
 * 
 * @arg	-b debugger	select backend debugger
 * @arg	-e path		set backend debugger path
 * @arg	-p port		port number to listen on/connect to
 * @arg	-h host		host to connect to
 * @arg	-P proxy		type of proxy connection to use
 */
int
main(int argc, char *argv[])
{
	int 				rank;
	int 				size;
	int				ch;
	int				port = PROXY_TCP_PORT;
	char *			host = NULL;
	char *			debugger_str = DEFAULT_BACKEND;
	char *			proxy_str = DEFAULT_PROXY;
	char *			path = NULL;
	proxy *			p;
	dbg_backend *	d;

	MPI_Init(&argc, &argv);
	
	MPI_Comm_size(MPI_COMM_WORLD, &size);
	MPI_Comm_rank(MPI_COMM_WORLD, &rank);
	
	while ((ch = getopt_long(argc, argv, "b:e:P:p:h:", longopts, NULL)) != -1)
	switch (ch) {
	case 'b':
		debugger_str = optarg;
		break;
	case 'e':
		path = optarg;
		break;
	case 'P':
		proxy_str = optarg;
		break;
	case 'p':
		port = atoi(optarg);
		break;
	case 'h':
		host = optarg;
		break;
	default:
		error_msg(rank, "sdm [--debugger=value] [--debugger_path=path]\n");
		error_msg(rank, "    [--proxy=proxy]\n");
		error_msg(rank, "    [--host=host_name] [--port=port]\n");
		exit(1);
	}
	
	argc -= optind;
	argv += optind;
	
	if (size < 2) {
		error_msg(rank, "Debugger requires at least 2 processes\n");
		MPI_Finalize();
		return 1;
	}
	
	// MPI_Comm_create_errhandler(handle_fatal_errors, &err_handler);
	// MPI_Comm_set_errhandler(MPI_COMM_WORLD, err_handler);
	
	if (find_dbg_backend(debugger_str, &d) < 0) {
		error_msg(rank, "No such backend: \"%s\"\n", debugger_str);
		MPI_Finalize();
		return 1;
	}
	
	if (path != NULL)
		backend_set_path(d, path);
	
	if (find_proxy(proxy_str, &p) < 0) {
		error_msg(rank, "No such proxy: \"%s\"\n", proxy_str);
		MPI_Finalize();
		return 1;
	}
	
	if (rank == size-1) {
		client(size - 1, rank, proxy_str, host, port);
	} else {
		server(size - 1, rank, d);
	}
	
	MPI_Finalize();
	
	return 0;
}
