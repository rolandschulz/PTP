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
#ifndef _AIX
#include <getopt.h>
#endif
#include <stdlib.h>
#include <stdarg.h>

#include "backend.h"
#include "proxy.h"
#include "proxy_tcp.h"
#include "sdm.h"

#define DEFAULT_BACKEND	"gdb-mi"
#define DEFAULT_PROXY	"tcp"

extern void master(char *proxy, char *host, int port);
extern void server(dbg_backend *dbgr);

#define OPT_TYPE_DEBUGGER		0
#define OPT_TYPE_DEBUGGER_PATH	1
#define OPT_TYPE_PROXY			2
#define OPT_TYPE_PORT			3
#define OPT_TYPE_HOST			4
#define OPT_TYPE_MASTER			5
#define OPT_TYPE_SERVER			6
#define OPT_TYPE_GENERATE		7
#define OPT_TYPE_ROUTING_FILE	8
#define OPT_TYPE_DEBUG			9

static int opt_type;

#ifndef _AIX
static struct option longopts[] = {
	{"debugger",		required_argument,	&opt_type,	OPT_TYPE_DEBUGGER},
	{"debugger_path",	required_argument,	&opt_type, 	OPT_TYPE_DEBUGGER_PATH},
	{"proxy",			required_argument,	&opt_type, 	OPT_TYPE_PROXY},
	{"port",			required_argument,	&opt_type, 	OPT_TYPE_PORT},
	{"host",			required_argument,	&opt_type, 	OPT_TYPE_HOST},
	{"master",			no_argument,	 	&opt_type,	OPT_TYPE_MASTER},
	{"server",			required_argument,	&opt_type,	OPT_TYPE_SERVER},
	{"generate_routes",	required_argument,	&opt_type,	OPT_TYPE_GENERATE},
	{"routing_file",	required_argument,	&opt_type,	OPT_TYPE_ROUTING_FILE},
#ifdef DEBUG
	{"debug",			optional_argument,	&opt_type, 	OPT_TYPE_DEBUG},
#endif /* DEBUG */
	{NULL,				0,					NULL,	0}
};
#endif

#ifdef DEBUG
static char * shortopts = "b:e:P:p:h:d:ms:g:r:";
#else /* DEBUG */
static char * shortopts = "b:e:P:p:h:ms:g:r:";
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
 * @arg	--debugger=type			select backend debugger
 * @arg	--debugger_path=path	set backend debugger path
 * @arg	--port=port				port number to listen on/connect to
 * @arg	--host=host				host to connect to
 * @arg	--proxy=proxy_type		type of proxy connection to use
 * @arg --master				master process
 * @arg --server=id				server process with rank 'id'
 * @arg --generate_routes=list	master will generate routing file using the supplied list
 * @arg --routing_file=path		path to routing file
 */
int
main(int argc, char *argv[])
{
	int 			ch;
	int				port = PTP_PROXY_TCP_PORT;
	char *			host = NULL;
	char *			debugger_str = DEFAULT_BACKEND;
	char *			proxy_str = DEFAULT_PROXY;
	char *			path = NULL;
	proxy *			p;
	dbg_backend *	d;
#ifdef _AIX
	int				n;
	char *			cp;
#endif

#ifndef _AIX
	while ((ch = getopt_long(argc, argv, shortopts, longopts, NULL)) != -1) {
		if (ch == 0) {
			switch (opt_type) {
			case OPT_TYPE_DEBUGGER:
				debugger_str = optarg;
				break;
			case OPT_TYPE_DEBUGGER_PATH:
				path = optarg;
				break;
			case OPT_TYPE_PROXY:
				proxy_str = optarg;
				break;
			case OPT_TYPE_PORT:
				port = (int)strtol(optarg, NULL, 10);
				break;
			case OPT_TYPE_HOST:
				host = optarg;
				break;
			case OPT_TYPE_MASTER:
			case OPT_TYPE_SERVER:
			case OPT_TYPE_GENERATE:
			case OPT_TYPE_ROUTING_FILE:
				break;
#ifdef DEBUG
			case OPT_TYPE_DEBUG:
				if (optarg == NULL)
					debug_level = DEBUG_LEVEL_ALL;
				else
					debug_level = (int)strtol(optarg, NULL, 10);
				break;
#endif /* DEBUG */
			}
		} else {
			fprintf(stderr,
				"sdm [--debugger=value] [--debugger_path=path]\n"
				"    [--proxy=proxy]\n"
				"    [--host=host_name] [--port=port]\n"
				"	 [--master]\n"
				"    [--server=rank]\n"
				"    [--generate_routes=list]\n"
				"    [--routing_file=path]\n"
#ifdef DEBUG
				"    [--debug[=level]]\n"
#endif /* DEBUG */
			);
			return 1;
		}
	}
#else
		/*
		 * AIX does not support GNU style getopt with long options.
		 * Parse options by string comparison instead.
		 */
	n = 1;
	while (n < argc) {
		cp = strchr(argv[n], '=');
		if (cp == NULL) {
			if (strcmp(argv[n], "--master") == 0 ||
				strncmp(argv[n], "--server", 8) == 0 ||
				strncmp(argv[n], "--generate_routes", 17) == 0 ||
				strncmp(argv[n], "--routing_file", 14) == 0) {
				/* No action required */
#ifdef DEBUG
			} else if (strcmp(argv[n], "--debug") == 0) {
				debug_level = DEBUG_LEVEL_ALL;
#endif
			}
			else {
				fprintf(stderr,
					"sdm [--debugger=value] [--debugger_path=path]\n"
					"    [--proxy=proxy]\n"
					"    [--host=host_name] [--port=port]\n"
					"	 [--master]\n"
					"    [--server=rank]\n"
					"    [--generate_routes=list]\n"
#ifdef DEBUG
					"    [--debug[=level]]\n"
#endif /* DEBUG */
				);
				return 1;
			}
		}
		else {
			*cp = '\0';
			if (strncmp(argv[n], "--debugger", 10) == 0) {
				debugger_str = strdup(cp + 1);
			}
			else if (strncmp(argv[n], "--debugger_path", 15) == 0) {
				path = strdup(cp + 1);
			}
			else if (strncmp(argv[n], "--proxy", 7) == 0) {
				proxy_str = strdup(cp + 1);
			}
			else if (strncmp(argv[n], "--port", 6) == 0) {
				port = (int) strtol(cp + 1, NULL, 10);
			}
			else if (strncmp(argv[n], "--host", 6) == 0) {
				host = strdup(cp + 1);
			}
#ifdef DEBUG
			else if (strncmp(argv[n], "--debug", 7) == 0) {
				debug_level = (int) strtol(cp + 1, NULL, 10);
			}
#endif
			else {
				fprintf(stderr,
					"sdm [--debugger=value] [--debugger_path=path]\n"
					"    [--proxy=proxy]\n"
					"    [--host=host_name] [--port=port]\n"
					"	 [--master]\n"
					"    [--server=rank]\n"
					"    [--generate_routes=list]\n"
					"    [--routing_file=path]\n"
#ifdef DEBUG
					"    [--debug[=level]]\n"
#endif /* DEBUG */
				);
				return 1;
			}
		}
		n = n + 1;
	}
#endif

	if (find_dbg_backend(debugger_str, &d) < 0) {
		fprintf(stderr, "No such backend: \"%s\"\n", debugger_str);
		return 1;
	}

	if (path != NULL) {
		backend_set_path(d, path);
	}

	if (find_proxy(proxy_str, &p) < 0) {
		fprintf(stderr,"No such proxy: \"%s\"\n", proxy_str);
		return 1;
	}

	if (sdm_init(argc, argv) < 0) {
		DEBUG_PRINTS(DEBUG_LEVEL_STARTUP, "sdm_init failed\n");
		return 1;
	}

	if (sdm_route_get_id() == SDM_MASTER) {
		DEBUG_PRINTS(DEBUG_LEVEL_STARTUP, "starting client\n");
		master(proxy_str, host, port);
	} else {
		DEBUG_PRINTF(DEBUG_LEVEL_STARTUP, "starting task %d\n", sdm_route_get_id());
		server(d);
	}

	DEBUG_PRINTS(DEBUG_LEVEL_STARTUP, "all finished\n");

	sdm_finalize();

	return 0;
}
