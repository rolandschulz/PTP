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

#include "config.h"

#include <mpi.h>
#include <getopt.h>
#include <stdlib.h>
#include <stdarg.h>

#include "backend.h"
#include "proxy.h"
#include "proxy_tcp.h"

#define DEFAULT_BACKEND	"gdb-mi"
#define DEFAULT_PROXY	"tcp"

extern void client(int, int, char *, char *, int);
extern void server(int, int, int, dbg_backend *);

static struct option longopts[] = {
	{"debugger",		required_argument,	NULL,	'b'},
	{"debugger_path",	required_argument,	NULL, 	'e'}, 
	{"proxy",			required_argument,	NULL, 	'P'}, 
	{"port",			required_argument,	NULL, 	'p'}, 
	{"host",			required_argument,	NULL, 	'h'}, 
	{"jobid",			required_argument,	NULL, 	'j'},
#ifdef DEBUG
	{"debug",			optional_argument,	NULL, 	'd'},
#endif /* DEBUG */
	{NULL,				0,					NULL,	0}
};

#ifdef DEBUG
static char * shortopts = "b:e:P:p:h:d::";
#else /* DEBUG */
static char * shortopts = "b:e:P:p:h:";
#endif /* DEBUG */

static int	fatal_error = 0;
static char *error_str = NULL;

void
error_msg(char *fmt, ...)
{
	va_list	ap;
	
	va_start(ap, fmt);
	vasprintf(&error_str, fmt, ap);
	va_end(ap);
	
	fatal_error = 1;
}

void
print_error_msg()
{
	if (error_str != NULL) {
		fprintf(stderr, "%s", error_str);
		fflush(stderr);
	}
}

/*
 * Main entry point for MPI parallel debugger.
 * 
 * @arg	-b debugger	select backend debugger
 * @arg	-e path		set backend debugger path
 * @arg	-p port		port number to listen on/connect to
 * @arg	-h host		host to connect to
 * @arg	-P proxy		type of proxy connection to use
 * @arg	-j jobid		jobid of application being debugged
 */
int
main(int argc, char *argv[])
{
	int 			rank;
	int 			size;
	int				ch;
	int				jobid = 0;
	int				port = PROXY_TCP_PORT;
	char *			host = NULL;
	char *			debugger_str = DEFAULT_BACKEND;
	char *			proxy_str = DEFAULT_PROXY;
	char *			path = NULL;
	proxy *			p;
	dbg_backend *	d;

	while ((ch = getopt_long(argc, argv, shortopts, longopts, NULL)) != -1)
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
	case 'j':
		jobid = atoi(optarg);
		break;
#ifdef DEBUG
	case 'd':
		if (optarg == NULL)
			debug_level = DEBUG_LEVEL_ALL;
		else
			debug_level = atoi(optarg);
		break;
#endif /* DEBUG */
	default:
		error_msg(
			"sdm [--debugger=value] [--debugger_path=path]\n"
			"    [--proxy=proxy]\n"
			"    [--host=host_name] [--port=port]\n"
			"    [--jobid=jobid]\n"
#ifdef DEBUG
			"    [--debug[=level]]\n"
#endif /* DEBUG */
		);
		goto error_out;
	}
	
	argc -= optind;
	argv += optind;
	
	if (find_dbg_backend(debugger_str, &d) < 0) {
		error_msg("No such backend: \"%s\"\n", debugger_str);
		goto error_out;
	}
	
	if (path != NULL)
		backend_set_path(d, path);
	
	if (find_proxy(proxy_str, &p) < 0) {
		error_msg("No such proxy: \"%s\"\n", proxy_str);
		goto error_out;
	}
	
	/*
	 * Become an MPI program
	 */
error_out:
	DEBUG_PRINTS(DEBUG_LEVEL_STARTUP, "starting MPI\n");
	
	MPI_Init(&argc, &argv);
	
	MPI_Comm_size(MPI_COMM_WORLD, &size);
	MPI_Comm_rank(MPI_COMM_WORLD, &rank);
	
	DEBUG_SET_RANK(rank);
	
	DEBUG_PRINTF(DEBUG_LEVEL_STARTUP, "size=%d rank=%d\n", size, rank);

	if (size < 2) {
		error_msg("Debugger requires at least 2 processes\n");
	}
	
	if (fatal_error) {
		if (rank == 0)
			print_error_msg();
		MPI_Finalize();
		return 1;
	}
		
	// MPI_Comm_create_errhandler(handle_fatal_errors, &err_handler);
	// MPI_Comm_set_errhandler(MPI_COMM_WORLD, err_handler);
	
	/*
	 * The client process *must* always have task id 'size' - 1 since it
	 * does not control a debugged process.
	 */
	if (rank == size-1) {
		DEBUG_PRINTS(DEBUG_LEVEL_STARTUP, "starting client\n");
		client(size, rank, proxy_str, host, port);
	} else {
		DEBUG_PRINTF(DEBUG_LEVEL_STARTUP, "starting task %d\n", rank);
		server(size, rank, jobid, d);
	}

	DEBUG_PRINTS(DEBUG_LEVEL_STARTUP, "all finished\n");
	
	MPI_Finalize();
	
	return 0;
}
