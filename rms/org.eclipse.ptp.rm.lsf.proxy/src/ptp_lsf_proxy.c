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
#include "ptp_lsf_proxy.h"

#define LSF 1

#ifdef LSF
#include <lsf/lsbatch.h>
#else
#define LS_LONG_INT long
#endif

/* non portable code, get rid of this and other remnants */
#ifdef _GNU_SOURCE
#define HAVE_ASPRINTF 1
#endif

#include <getopt.h>
//#include <stdbool.h>
#include <errno.h>
#include <stdio.h>
#include <stdlib.h>
#include <signal.h>
#include <string.h>

#include <proxy.h>
#include <proxy_tcp.h>
#include <proxy_event.h>
#include <proxy_cmd.h>
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

#define LSF_MAX_MSG_SIZE 256

/*
 * RTEV_ERROR codes are used internally in the ORTE specific plugin
 */
// TODO: are signals needed?
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

struct ptp_job {
	int ptp_jobid;		// job ID as known by PTP
	int debug_jobid;	// job ID of debugger or -1 if not a debug job
	int lsf_jobid;		// job ID that will be used by program when it starts
	int num_procs;		// number of procs requested for program (debugger uses num_procs+1)
};
typedef struct ptp_job ptp_job;

int 		lsf_shutdown = 0;
proxy_svr *	lsf_proxy;			/* this proxy server */
pid_t		lsfd_pid = 0;
List *		eventList;
List *		jobList;
int			ptp_signal_exit;
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
	{CMD_INIT,			LSF_Initialize},
	{CMD_SEND_EVENTS,	LSF_SendEvents},
	{CMD_HALT_EVENTS,	LSF_HaltEvents},
	{CMD_QUIT,			LSF_Quit},
	{0,					NULL},
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

/**
 * Initialize LSFLIB
 */
 static int
initLSF(char* app_name)
 {
 	int ret;
 	
    gInitialized = 1;
	gLSF_host_poll_freq  = LSF_HOST_POLL_FREQ_DEFAULT;
	gLSF_queue_poll_freq = LSF_QUEUE_POLL_FREQ_DEFAULT;
	
	/* initialize LSBLIB */
	if ( (ret = lsb_init(app_name)) < 0 ) {
		lsb_perror("%s: lsb_init() failed");
		return ret;
	}

 	return 0;
 }


static int
isShutdown(void)
{
	return lsf_shutdown != 0;
}


static useconds_t
usleepInterval()
{
	useconds_t interval = 1000000 / gLSF_queue_poll_freq;
	return interval;
}


/**
 * Shutdown the LSF service (no LSBLIB calls needed to shutdown LSF)
 */
static int
shutdownLSF(void)
{
	lsf_shutdown = 0;
	return 0;
}


static char *
errorStr(int type, char *msg)
{
	char * str;
	static char * res = NULL;
	
	if (res != NULL) {
		free(res);
	}
	
	proxy_cstring_to_str(msg, &str);
	res = malloc(strlen(str) + 1 + 2*32);
	sprintf(res, "%d %d %s", RTEV_ERROR, type, str);
	free(str);
	
	return res;	
}


#ifdef LSF
static int
checkErrorCode(int type, int rc)
{
	if (rc != LSF_SUCCESS) {
// FIXME
//		printf("ARgh!  An error!\n"); fflush(stdout);
//		printf("ERROR %s\n", LSF_ERROR_NAME(rc)); fflush(stdout);
//		AddToList(eventList, (void *)errorStr(type, (char *)LSF_ERROR_NAME(rc)));
		return 1;
	}

/***** FIXME
	if (jobId < 0)
        // if job submission fails, lsb_submit returns -1
    switch (lsberrno) {
        // and sets lsberrno to indicate the error
    case LSBE_QUEUE_USE:
    case LSBE_QUEUE_CLOSED:
    lsb_perror(reply.queue);
    exit(-1);
    default:
    lsb_perror(NULL);
    exit(-1);

*****/
    fprintf(stdout, "LSF_CheckErrorCode: type = %d\n", type);   // FIXME by removal
    
	return PROXY_RES_OK;
}
#endif


/**
 * Compares hostInfoEnt structures
 * 
 * @returns true if entries are the same, otherwise copies new entry into the
 * original and returns false
 * 
 */
static int
hostInfoHasChanged(struct hostInfoEnt * hInfoSaved, struct hostInfoEnt * hInfo, int * initialize)
{
	if (*initialize) {
		*initialize = 0;
		goto hasChanged;
	}
	if (hInfoSaved->host		!= hInfo->host)			goto hasChanged;
	if (hInfoSaved->hStatus		!= hInfo->hStatus)		goto hasChanged;
	
	/* no changes */
	return 0;
	
	/* something has changed */
	hasChanged:

	*hInfoSaved = *hInfo;
	return 1;
}


/**
 * Compares queueInfoEnt structures
 * 
 * @returns true if entries are the same, otherwise copies new entry into the
 * original and returns false
 * 
 */
static int
queueInfoHasChanged(struct queueInfoEnt * qInfoSaved, struct queueInfoEnt * qInfo, int * initialize)
{
	if (*initialize) {
		*initialize = 0;
		goto hasChanged;
	}
	if (strcmp(qInfoSaved->queue, qInfo->queue) != 0)	goto hasChanged;
	if (qInfoSaved->qStatus		!= qInfo->qStatus)		goto hasChanged;
	
	/* no changes */
	return 0;
	
	/* something has changed */
	hasChanged:

	*qInfoSaved = *qInfo;
	return 1;
}


/**
 * Notify proxy client of host information changes
 * 
 * 	TODO - compare with old info
 */
static int
notifyHostInfoChange(struct hostInfoEnt *hInfo)
{
	char *res, *str1, *str2, *str3;
	char id[64], host[64], state[64];

	static int initialize = 1;
	static struct hostInfoEnt hInfoSaved;

	if (hostInfoHasChanged(&hInfoSaved, hInfo, &initialize)) {
		sprintf(id, "%s=%d", ATTRIB_MACHINEID, 0);
		//sprintf(host, "%s=%s", ATTRIB_NODE_NUMBER, hInfo->host); ??????
		sprintf(host, "%s=%s", ATTRIB_NODE_NAME, hInfo->host);
		sprintf(state, "%s=%d", ATTRIB_NODE_STATE, hInfo->hStatus);
		proxy_cstring_to_str(id, &str1);
		proxy_cstring_to_str(host, &str2);
		proxy_cstring_to_str(state, &str3);
		asprintf(&res, "%d %s %s %s", RTEV_NATTR, str1, str2, str3);
		AddToList(eventList, (void *)res);
	}
	
	return PROXY_RES_OK;
}


/**
 * Notify proxy client of queue information changes
 * 
 * 	TODO - compare with old info
 */
static int
notifyQueueInfoChange(struct queueInfoEnt *qInfo)
{
	char *res, *str1, *str2, *str3;
	char id[64], host[64], state[64];

	static int initialize = 1;
	static struct queueInfoEnt qInfoSaved;

	if (queueInfoHasChanged(&qInfoSaved, qInfo, &initialize)) {
//		sprintf(id, "%s=%d", ATTRIB_MACHINEID, 0);
		//sprintf(host, "%s=%s", ATTRIB_NODE_NUMBER, qInfo->host); ??????
//		sprintf(host, "%s=%s", ATTRIB_NODE_NAME, qInfo->host);
		sprintf(state, "%s=%d", ATTRIB_NODE_STATE, qInfo->qStatus);
		proxy_cstring_to_str(id, &str1);
		proxy_cstring_to_str(host, &str2);
		proxy_cstring_to_str(state, &str3);
		asprintf(&res, "%d %s %s %s", RTEV_NATTR, str1, str2, str3);
//		AddToList(eventList, (void *)res);
	}
	
	return PROXY_RES_OK;
}


/**
 * Send EVENT_RUNTIME_OK to server
 */
 static void
 sendRuntimeEventOK()
 {
 	char* res;
 	asprintf(&res, "%d", RTEV_OK);
	AddToList(eventList, (void *)res);
	// TODO - is this a memory leak?
 }


/**
 * Initialize the LSF service
 */
int
LSF_Initialize(char** args)
{
	char * version;

	proxy_str_to_cstring(args[0], &version);
	fprintf(stdout, "LSF_Initialize {%s}\n", version); fflush(stdout);
	
	if (strncmp(version, "2.0", 3) != 0) {
		fprintf(stdout, "LSF_Initialize: incorrect version [%s]\n", version); fflush(stdout);
		AddToList(eventList, errorStr(1, "LSF_Initialize: incorrect version, should be 2.0"));
		free(version);
		return PROXY_RES_ERR;
	} else {
		sendRuntimeEventOK();
		free(version);
		return PROXY_RES_OK;
	}
}


/**
 * Start polling LSF change events
 * 
 *  start polling for:
 *		1. host information - lsb_gethostinfo()
 * 		2. batch specific host information - lsb_hostinfo()
 * 		3. queue information - lsb_queueinfo()
 */
 int
LSF_SendEvents(char **args)
{
	fprintf(stdout, "  LSF_SendEvents\n"); fflush(stdout);
	gSendEvents = 1;
	return PROXY_RES_OK;	
}


/**
 * Stop polling for LSF change events
 */
 int
LSF_HaltEvents(char **args)
{
	fprintf(stdout, "  LSF_HaltEvents\n"); fflush(stdout);
	gSendEvents = 0;
	return PROXY_RES_OK;	
}


/**
 * Run/submit a job with the given executable path and arguments (remote call from a client proxy)
 *
 * TODO - what about queues, should there be a LSF_Submit?
 */
int
LSF_Run(char **args)
{
#ifdef LSF_NOT_YET
	int					rtn;
	LS_LONG_INT			jobId;				
	struct submit*		jobSubReq;		/* Job specifications */
	struct submitReply*	jobSubReply;	/* Results of job submission */
	rtn = lsb_submit(jobSubReq, jobSubReply);
	rtn = lsb_modify(jobSubReq, jobSubReply, jobId);
#endif
	
	fprintf(stdout, "Returning from LSFRun\n"); fflush(stdout);
	return PROXY_RES_OK;
}


/**
 * Initiate the discovery phase
 * 
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
#ifdef LSF
	int		rtn;			/* TODO - find what return value signifies from documentation */
	int		times = 0;		/* number of runs before job is deleted */
	int		options = 0;
#endif
	LS_LONG_INT jobid = atoi(args[1]);
	
	fprintf(stdout, "  LSF_TerminateJob (%ld)\n", jobid); fflush(stdout);
#ifdef LSF
	rtn = lsb_deletejob(jobid, times, options);
#endif

	return PROXY_RES_OK;
}


/**
 * Shutdown 
 */
int
LSF_Quit(char **args)
{
	fprintf(stdout, "LSF_Quit called!\n"); fflush(stdout);
	shutdownLSF();
	sendRuntimeEventOK();

	return PROXY_RES_OK;
}


/**
 * Check for events and call appropriate progress hooks.
 */
static int
LSF_Progress(void)
{
#ifdef LSF
	char*	hosts[1];			/* list of host names */
	int		num_hosts = 1;		/* number of hosts (0 = all; 1 = localhost) */
	struct	hostInfoEnt* host_info;
	
	char*  queues[1];			/* Array containing names of queues of interest */
	int   *numQueues;			/* Number of queues */
	char  *queuehost = NULL;	/* Specified queues using hostname (NULL for all) */
	char  *username = NULL;		/* Specified queues enabled for user (NULL for all) */
	int   options = 0;			/* Reserved for future use; supply 0 */
	struct queueInfoEnt* queue_info;
#endif

	// TODO - create function for this
	static int nextHostPoll = 0;	/* poll for hosts when reaches 0 */

	/* timeout interval to wait for arrival of events (usecs) */ 
	struct timeval timeout;
	timeout.tv_sec = 2;
	timeout.tv_usec = 2000;
//	timeout.tv_usec = 2000000;  // TODO - this causes error, make sure tests completes and fails

    if (proxy_svr_handle_events(lsf_proxy, eventList, timeout) == PROXY_RES_ERR) {
    	return PROXY_RES_ERR;
    }

#ifdef LSF
	/* poll for hosts */
    if (gSendEvents && --nextHostPoll < 1) {
    	nextHostPoll = 0;	// TODO
		host_info = lsb_hostinfo(hosts, &num_hosts);
		if (host_info == NULL) {
			lsb_perror("ptp_lsf_proxy: lsb_hostinfo() failed");
			return PROXY_RES_ERR;
		}
		notifyHostInfoChange(host_info);
    }
#endif

#ifdef LSF
    /* poll for queues */
    if (gSendEvents) {
		queue_info = lsb_queueinfo(queues, numQueues, queuehost, username, options);
		if (queue_info == NULL) {
			lsb_perror("ptp_lsf_proxy: lsb_queueinfo() failed");
			return PROXY_RES_ERR;
		}
		notifyQueueInfoChange(queue_info);
	}
#endif
	// not needed sleep in fd reads usleep(usleepInterval());

	return PROXY_RES_OK;
}


static int
server(char* app_name, char* name, char* host, int port)
{
	char* msg, * msg1, * msg2;
	int rc;

	initLSF(app_name);
	
	eventList = NewList();
	jobList = NewList();
	
	if (proxy_svr_init(name, &handler_funcs, &helper_funcs, command_tab, &lsf_proxy) != PROXY_RES_OK) {
		return 0;
	}
	
	/* no proxy_svr_create(), create comes after init (CER)? */

	proxy_svr_connect(lsf_proxy, host, port);
	printf("proxy_svr_connect returned.\n");
	
	/* make progress until shutdown */
	while (ptp_signal_exit == 0 && !isShutdown()) {
		if  ( LSF_Progress() != PROXY_RES_OK ) {
			break;
		}
		if  ( proxy_svr_progress(lsf_proxy) != PROXY_RES_OK ) {
			break;
		}
	}
	
	msg  = malloc(LSF_MAX_MSG_SIZE);
	msg1 = malloc(LSF_MAX_MSG_SIZE);
	msg2 = malloc(LSF_MAX_MSG_SIZE);
	
	if (ptp_signal_exit != 0) {
		switch(ptp_signal_exit) {
			case SIGINT:
				sprintf(msg1, "INT");
				sprintf(msg2, "Interrupt");
				break;
			case SIGHUP:
				sprintf(msg1, "HUP");
				sprintf(msg2, "Hangup");
				break;
			case SIGILL:
				sprintf(msg1, "ILL");
				sprintf(msg2, "Illegal Instruction");
				break;
			case SIGSEGV:
				sprintf(msg1, "SEGV");
				sprintf(msg2, "Segmentation Violation");
				break;
			case SIGTERM:
				sprintf(msg1, "TERM");
				sprintf(msg2, "Termination");
				break;
			case SIGQUIT:
				sprintf(msg1, "QUIT");
				sprintf(msg2, "Quit");
				break;
			case SIGABRT:
				sprintf(msg1, "ABRT");
				sprintf(msg2, "Process Aborted");
				break;
			default:
				sprintf(msg1, "***UNKNOWN SIGNAL***");
				sprintf(msg2, "ERROR - UNKNOWN SIGNAL, REPORT THIS!");
				break;
		}
		printf("###### SIGNAL: %s\n", msg1);
		printf("###### Shutting down LSFd\n");
		shutdownLSF();
		sprintf(msg, "ptp_lsf_proxy received signal %s (%s).  Exit was required and performed cleanly.", msg1, msg2);
		AddToList(eventList, (void *) errorStr(RTEV_ERROR_SIGNAL, msg));
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
	char*		host = "localhost";
	char*		app_name = "ptp_lsf_proxy";
	char*		proxy_str = DEFAULT_PROXY;
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
	rc = server(app_name, proxy_str, host, port);
	
	return rc;
}
