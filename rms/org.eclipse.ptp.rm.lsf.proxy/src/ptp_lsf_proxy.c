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

#include <getopt.h>
//#include <stdbool.h>
#include <errno.h>
#include <stdio.h>

#include <proxy.h>
#include <proxy_tcp.h>
#include <proxy_event.h>
#include <handler.h>
#include <list.h>

/*
 * Need to undef these if we include
 * two config.h files
 */
#undef PACKAGE_BUGREPORT
#undef PACKAGE_NAME
#undef PACKAGE_STRING
#undef PACKAGE_TARNAME
#undef PACKAGE_VERSION

#define DEFAULT_PROXY		"tcp"

/*
 * RTEV codes must EXACTLY match org.eclipse.ptp.rtsystem.proxy.event.IProxyRuntimeEvent
 */
#define RTEV_OFFSET						200
#define RTEV_OK							RTEV_OFFSET + 0
#define RTEV_ERROR						RTEV_OFFSET + 1
#define RTEV_JOBSTATE					RTEV_OFFSET + 2
#define RTEV_PROCS						RTEV_OFFSET + 4
#define RTEV_PATTR						RTEV_OFFSET + 5
#define RTEV_NODES						RTEV_OFFSET + 7
#define RTEV_NATTR						RTEV_OFFSET + 8
#define RTEV_NEWJOB						RTEV_OFFSET + 12
#define RTEV_PROCOUT					RTEV_OFFSET + 13

/*
 * RTEV_ERROR codes are used internally in the ORTE specific plugin
 */
#define RTEV_ERROR_SIGNAL				RTEV_OFFSET + 1009

/*
 * Attribute names must EXACTLY match org.eclipse.ptp.core.AttributeConstants
 * 
 * TODO: Replace with new attribute system
 */
#define ATTRIB_MACHINEID			"Machine ID"
#define ATTRIB_NODE_NAME			"Node Name"
#define ATTRIB_NODE_NUMBER			"Node Number"
#define ATTRIB_NODE_STATE			"Status"
#define ATTRIB_NODE_GROUP			"Group Owner"
#define ATTRIB_NODE_USER			"User Owner"
#define ATTRIB_NODE_MODE			"Mode"
#define ATTRIB_PROCESS_PID			"ATTRIB_PROCESS_PID"
#define ATTRIB_PROCESS_EXIT_CODE	"ATTRIB_PROCESS_EXIT_CODE"
#define ATTRIB_PROCESS_STATUS		"ATTRIB_PROCESS_STATUS"
#define ATTRIB_PROCESS_SIGNAL		"ATTRIB_PROCESS_SIGNAL"
#define ATTRIB_PROCESS_NODE_NAME	"ATTRIB_PROCESS_NODE_NAME"

#define JOB_STATE_INIT             1
#define JOB_STATE_RUNNING          2
#define JOB_STATE_TERMINATED       3
#define JOB_STATE_ERROR            4

#define LSF_SUCCESS                1


int LSF_StartDaemon(char **);
int LSF_Run(char **);
int LSF_Discover(char **);
int LSF_TerminateJob(char **);
int LSF_Quit(char **);

struct ptp_job {
	int ptp_jobid;		// job ID as known by PTP
	int debug_jobid;	// job ID of debugger or -1 if not a debug job
	int lsf_jobid;		// job ID that will be used by program when it starts
	int num_procs;		// number of procs requested for program (debugger uses num_procs+1)
};
typedef struct ptp_job ptp_job;

int 		lsf_shutdown = 0;
proxy_svr *	lsf_proxy;			/* this proxy server */
int		is_lsf_initialized = 0;
pid_t		lsfd_pid = 0;
List *		eventList;
List *		jobList;
int		ptp_signal_exit;
RETSIGTYPE	(*saved_signals[NSIG])(int);

static proxy_handler_funcs handler_funcs = {
	RegisterFileHandler,	/* regfile() - call to register a file handler */
	UnregisterFileHandler,	/* unregfile() - called to unregister file handler */
	RegisterEventHandler,	/* regeventhandler() - called to register the proxy event handler */
	CallEventHandlers
};

static proxy_svr_helper_funcs helper_funcs = {
	NULL,			/* newconn() - can be used to reject connections */
	NULL			/* numservers() - if there are multiple servers, return the number */
};

static proxy_svr_commands command_tab[] = {
	{"STARTDAEMON",		LSF_StartDaemon},
	{"DISCOVER",		LSF_Discover},
	{"RUN",			LSF_Run},
	{"TERMJOB",		LSF_TerminateJob},
	{"QUI",			LSF_Quit},
	{NULL,			NULL},
};

static struct option longopts[] = {
	{"proxy",			required_argument,	NULL, 	'P'}, 
	{"port",			required_argument,	NULL, 	'p'}, 
	{"host",			required_argument,	NULL, 	'h'}, 
	{NULL,				0,					NULL,	0}
};

#define JOBID_PTP	0
#define JOBID_ORTE	1
#define JOBID_DEBUG	2
#define JOBID_LSF	3


static int
LSF_IsShutdown(void)
{
	return lsf_shutdown != 0;
}


static int
LSF_Shutdown(void)
{
	lsf_shutdown = 0;
	return 0;
}


static char *
LSF_ErrorStr(int type, char *msg)
{
	char * str;
	static char * res = NULL;
	
	if (res != NULL) {
		free(res);
	}
	
	proxy_cstring_to_str(msg, &str);
	asprintf(&res, "%d %d %s", RTEV_ERROR, type, str);
	free(str);
	
	return res;	
}


static int
LSF_Initialized(void)
{
	return is_lsf_initialized;
}


static int
LSF_CheckErrorCode(int type, int rc)
{
	if(rc != LSF_SUCCESS) {
		printf("ARgh!  An error!\n"); fflush(stdout);
		printf("ERROR %s\n", LSF_ERROR_NAME(rc)); fflush(stdout);
		AddToList(eventList, (void *)LSF_ErrorStr(type, (char *)LSF_ERROR_NAME(rc)));
		return 1;
	}
	
	return 0;
}


/**
 * Start the LSF daemon (remote call from a client proxy)
 *
 * TODO - may not need to start a daemon for LSF (perhaps need to start a copy of myself)
 */
int
LSF_StartDaemon(char ** args)
{
	fprintf(stdout, "  LSF_StartDaemon\n"); fflush(stdout);
	return 0;
}


/**
 * Run/submit a job with the given executable path and arguments (remote call from a client proxy)
 *
 * TODO - what about queues, should there be a LSF_Submit?
 */
int
LSF_Run(char **args)
{
	fprintf(stdout, "Returning from LSFRun\n"); fflush(stdout);
	return PROXY_RES_OK;
}


/**
 * Initiate the discovery phase 
 */
int
LSF_Discover(char **args)
{	
	fprintf(stdout, "DISCOVERY PHASE: end\n"); fflush(stdout);
	return PROXY_RES_OK;
}


/**
 * Terminate a job given a jobid (remote call from a client proxy)
 */
int
LSF_TerminateJob(char **args)
{
	int		jobid = atoi(args[1]);
	fprintf(stdout, "  LSF_TerminateJob (%d)\n", jobid); fflush(stdout);
	return PROXY_RES_OK;
}


/**
 * Shutdown 
 */
int
LSF_Quit(char **args)
{
	LSF_Shutdown();
	fprintf(stdout, "LSF_Quit called!\n"); fflush(stdout);
	return PROXY_RES_OK;
}


/**
 * Initialize the LSF service
 *
 * TODO - who calls this, it may not be needed, called from proxy_srv_init
 */
static int
LSF_Init(char *universe_name)
{
	fprintf(stdout, "LSF_Init (%s)\n", universe_name); fflush(stdout);
	is_lsf_initialized = 1;
	return 0;
}


/**
 * Check for events and call appropriate progress hooks.
 */
static int
LSF_Progress(void)
{
	fd_set			rfds;
	fd_set			wfds;
	fd_set			efds;
	int			res;
	int			nfds = 0;
	char *			event;
	struct timeval	tv;
	handler *		h;
	
	struct timeval TIMEOUT;
	TIMEOUT.tv_sec = 0;
	TIMEOUT.tv_usec = 2000;

	for (SetList(eventList); (event = (char *)GetListElement(eventList)) != NULL; ) {
		proxy_svr_event_callback(lsf_proxy, event);
		RemoveFromList(eventList, (void *)event);
		free(event);	
	}

	// TODO - are file events needed?
	
	/***********************************
	 * First: Check for any file events
	 */
	 
	/*
	 * Set up fd sets
	 */
	FD_ZERO(&rfds);
	FD_ZERO(&wfds);
	FD_ZERO(&efds);
	
	for (SetHandler(); (h = GetHandler()) != NULL; ) {
		if (h->htype == HANDLER_FILE) {
			if (h->file_type & READ_FILE_HANDLER)
				FD_SET(h->fd, &rfds);
			if (h->file_type & WRITE_FILE_HANDLER)
				FD_SET(h->fd, &wfds);
			if (h->file_type & EXCEPT_FILE_HANDLER)
				FD_SET(h->fd, &efds);
			if (h->fd > nfds)
				nfds = h->fd;
		}
	}
	
	tv = TIMEOUT;
	
	for ( ;; ) {
		res = select(nfds+1, &rfds, &wfds, &efds, &tv);
	
		switch (res) {
		case INVALID_SOCKET:
			if ( errno == EINTR )
				continue;
		
			perror("socket");
			return PROXY_RES_ERR;
		
		case 0:
			/*
			 * Timeout.
			 */
			 break;
			 		
		default:
			for (SetHandler(); (h = GetHandler()) != NULL; ) {
				if (h->htype == HANDLER_FILE
					&& ((h->file_type & READ_FILE_HANDLER && FD_ISSET(h->fd, &rfds))
						|| (h->file_type & WRITE_FILE_HANDLER && FD_ISSET(h->fd, &wfds))
						|| (h->file_type & EXCEPT_FILE_HANDLER && FD_ISSET(h->fd, &efds)))
					&& h->file_handler(h->fd, h->data) < 0)
					return PROXY_RES_ERR;
			}
			
		}
	
		break;
	}
	
	/* only run the progress of the LSF code if we've initialized the LSF daemon */
	if (LSF_Initialized()) {
		// TODO: opal_event_loop(OPAL_EVLOOP_ONCE);
	}
	
	return PROXY_RES_OK;
}


int
server(char* name, char* host, int port)
{
	char* msg, * msg1, * msg2;
	int rc;
	
	eventList = NewList();
	jobList = NewList();
	
	if (proxy_svr_init(name, &handler_funcs, &helper_funcs, command_tab, &lsf_proxy) != PROXY_RES_OK) {
		return 0;
	}
	
	/* no proxy_svr_create(), create comes after init (CER)? */

	proxy_svr_connect(lsf_proxy, host, port);
	printf("proxy_svr_connect returned.\n");
	
        /* make progress until shutdown */
	while (ptp_signal_exit == 0 && !LSF_IsShutdown()) {
		if  ((LSF_Progress() != PROXY_RES_OK) || (proxy_svr_progress(lsf_proxy) != PROXY_RES_OK)) {
			break;
		}
	}
	
	if (ptp_signal_exit != 0) {
		switch(ptp_signal_exit) {
			case SIGINT:
				asprintf(&msg1, "INT");
				asprintf(&msg2, "Interrupt");
				break;
			case SIGHUP:
				asprintf(&msg1, "HUP");
				asprintf(&msg2, "Hangup");
				break;
			case SIGILL:
				asprintf(&msg1, "ILL");
				asprintf(&msg2, "Illegal Instruction");
				break;
			case SIGSEGV:
				asprintf(&msg1, "SEGV");
				asprintf(&msg2, "Segmentation Violation");
				break;
			case SIGTERM:
				asprintf(&msg1, "TERM");
				asprintf(&msg2, "Termination");
				break;
			case SIGQUIT:
				asprintf(&msg1, "QUIT");
				asprintf(&msg2, "Quit");
				break;
			case SIGABRT:
				asprintf(&msg1, "ABRT");
				asprintf(&msg2, "Process Aborted");
				break;
			default:
				asprintf(&msg1, "***UNKNOWN SIGNAL***");
				asprintf(&msg2, "ERROR - UNKNOWN SIGNAL, REPORT THIS!");
				break;
		}
		printf("###### SIGNAL: %s\n", msg1);
		printf("###### Shutting down LSFd\n");
		LSF_Shutdown();
		asprintf(&msg, "ptp_lsf_proxy received signal %s (%s).  Exit was required and performed cleanly.", msg1, msg2);
		AddToList(eventList, (void *) LSF_ErrorStr(RTEV_ERROR_SIGNAL, msg));
		free(msg);
		free(msg1);
		free(msg2);
		/* our return code = the signal that fired */
		rc = ptp_signal_exit;
	}
	
	proxy_svr_finish(lsf_proxy);
	printf("proxy_svr_finish returned.\n");
	
	return rc;
}


RETSIGTYPE
ptp_signal_handler(int sig)
{
		ptp_signal_exit = sig;
		if(sig >= 0 && sig < NSIG) {
			RETSIGTYPE (*saved_signal)(int) = saved_signals[sig];
			if(saved_signal != SIG_ERR && saved_signal != SIG_IGN && saved_signal != SIG_DFL) {
				saved_signal(sig);
			}
		}
}

int
main(int argc, char* argv[])
{
	int			ch;
	int			port = PROXY_TCP_PORT;
	char *			host = "localhost";
	char *			proxy_str = DEFAULT_PROXY;
	int			rc;
	
	while ((ch = getopt_long(argc, argv, "P:p:h:", longopts, NULL)) != -1)
	switch (ch) {
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
		fprintf(stderr, "%s [--proxy=proxy] [--host=host_name] [--port=port]\n", argv[0]);
		return 1;
	}
	
	/* 
	 * signal can happen any time after handlers are installed, so
	 * make sure we catch it
	 */
	ptp_signal_exit = 0;
	
	/* setup our signal handlers */
	saved_signals[SIGINT] = signal(SIGINT, ptp_signal_handler);
	saved_signals[SIGHUP] = signal(SIGHUP, ptp_signal_handler);
	saved_signals[SIGILL] = signal(SIGILL, ptp_signal_handler);
	saved_signals[SIGSEGV] = signal(SIGSEGV, ptp_signal_handler);
	saved_signals[SIGTERM] = signal(SIGTERM, ptp_signal_handler);
	saved_signals[SIGQUIT] = signal(SIGQUIT, ptp_signal_handler);
	saved_signals[SIGABRT] = signal(SIGABRT, ptp_signal_handler);
	
	if(saved_signals[SIGINT] != SIG_ERR && saved_signals[SIGINT] != SIG_IGN && saved_signals[SIGINT] != SIG_DFL) {
		printf("  ---> SIGNAL SIGINT was previously already defined.  Shadowing.\n"); fflush(stdout);
	}
	if(saved_signals[SIGHUP] != SIG_ERR && saved_signals[SIGHUP] != SIG_IGN && saved_signals[SIGHUP] != SIG_DFL) {
		printf("  ---> SIGNAL SIGHUP was previously already defined.  Shadowing.\n"); fflush(stdout);
	}
	if(saved_signals[SIGILL] != SIG_ERR && saved_signals[SIGILL] != SIG_IGN && saved_signals[SIGILL] != SIG_DFL) {
		printf("  ---> SIGNAL SIGILL was previously already defined.  Shadowing.\n"); fflush(stdout);
	}
	if(saved_signals[SIGSEGV] != SIG_ERR && saved_signals[SIGSEGV] != SIG_IGN && saved_signals[SIGSEGV] != SIG_DFL) {
		printf("  ---> SIGNAL SIGSEGV was previously already defined.  Shadowing.\n"); fflush(stdout);
	}	
	if(saved_signals[SIGTERM] != SIG_ERR && saved_signals[SIGTERM] != SIG_IGN && saved_signals[SIGTERM] != SIG_DFL) {
		printf("  ---> SIGNAL SIGTERM was previously already defined.  Shadowing.\n"); fflush(stdout);
	}
	if(saved_signals[SIGQUIT] != SIG_ERR && saved_signals[SIGQUIT] != SIG_IGN && saved_signals[SIGQUIT] != SIG_DFL) {
		printf("  ---> SIGNAL SIGQUIT was previously already defined.  Shadowing.\n"); fflush(stdout);
	}
	if(saved_signals[SIGABRT] != SIG_ERR && saved_signals[SIGABRT] != SIG_IGN && saved_signals[SIGABRT] != SIG_DFL) {
		printf("  ---> SIGNAL SIGABRT was previously already defined.  Shadowing.\n"); fflush(stdout);
	}	
	rc = server(proxy_str, host, port);
	
	return rc;
}
