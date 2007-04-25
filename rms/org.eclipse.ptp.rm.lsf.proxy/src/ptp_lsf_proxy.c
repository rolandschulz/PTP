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
#define ATTRIB_NODE_NAME_ID			2
#define ATTRIB_QUEUE_NAME_ID		3

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
	{CMD_MODEL_DEF,		LSF_ModelDef},
	{CMD_START_EVENTS,	LSF_StartEvents},
	{CMD_STOP_EVENTS,	LSF_StopEvents},
	{CMD_SUBMIT_JOB,	LSF_SubmitJob},
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

/* TODO - turn change events back on
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
		free(str1);
		free(str2);
		free(str3);
	}
*/
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
/* TODO - turn on change notification
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
*/
	return PROXY_RES_OK;
}


/**
 * Send Attributes to client
 */
 static void
 sendAttributes(int trans_id)
 {
 	char *	res;
 	char	id[32], type[32], sname[64], lname[128], min[32], max[32], def[32];
 	char *	str_id, *str_type, *str_sname, *str_lname, *str_min, *str_max, *str_def;

//	ID	TYPE	 SNAME	LNAME	[ MIN	MAX DEF	VALS ]

 	/* node name */
 	
	sprintf(id, "id=%d", ATTRIB_NODE_NAME_ID);
	sprintf(type, "type=%d", RTEV_ATTRIB_TYPE_STRING);
	sprintf(sname, "sname=%s", "Node name");
	sprintf(lname, "lname=%s", "Name of the host machine");
	sprintf(min, "min=%s", "1");
	sprintf(max, "max=%s", "128");
	sprintf(def, "def=%s", "1");

	proxy_cstring_to_str(id, &str_id);
	proxy_cstring_to_str(type, &str_type);
	proxy_cstring_to_str(sname, &str_sname);
	proxy_cstring_to_str(lname, &str_lname);
	proxy_cstring_to_str(min, &str_min);
	proxy_cstring_to_str(max, &str_max);
	proxy_cstring_to_str(def, &str_def);
	asprintf(&res, "%d %d %s %s %s %s %s %s %s", trans_id, RTEV_ATTR_DEF,
			 str_id, str_type, str_sname, str_lname, str_min, str_max, str_def);
	AddToList(eventList, (void *)res);

	free(str_id);
	free(str_type);
	free(str_sname);
	free(str_lname);
	free(str_min);
	free(str_max);
	free(str_def);

 	/* queue name */
 	
	sprintf(id, "id=%d", ATTRIB_QUEUE_NAME_ID);
	sprintf(type, "type=%d", RTEV_ATTRIB_TYPE_STRING);
	sprintf(sname, "sname=%s", "Queue name");
	sprintf(lname, "lname=%s", "Name of a queue for job submissions");
	sprintf(min, "min=%s", "queueA");
	sprintf(max, "max=%s", "queueC");
	sprintf(def, "def=%s", "queueB");

	proxy_cstring_to_str(id, &str_id);
	proxy_cstring_to_str(type, &str_type);
	proxy_cstring_to_str(sname, &str_sname);
	proxy_cstring_to_str(lname, &str_lname);
	proxy_cstring_to_str(min, &str_min);
	proxy_cstring_to_str(max, &str_max);
	proxy_cstring_to_str(def, &str_def);
	asprintf(&res, "%d %d %s %s %s %s %s %s %s", trans_id, RTEV_ATTR_DEF,
			 str_id, str_type, str_sname, str_lname, str_min, str_max, str_def);
	AddToList(eventList, (void *)res);

	free(str_id);
	free(str_type);
	free(str_sname);
	free(str_lname);
	free(str_min);
	free(str_max);
	free(str_def);
 }

/**
 * Send new machine info to client
 */
 static void
 sendMachineInfo(int trans_id)
 {
 	char *	res;
 	char	name[64];
 	char *	str_name;

 	/* machine name */
 	
	sprintf(name, "denali.lanl.gov");
	proxy_cstring_to_str(name, &str_name);
	asprintf(&res, "%d %d %s", trans_id, RTEV_NEW_MACHINE, str_name);
	AddToList(eventList, (void *)res);
	free(str_name);
 }


/**
 * Send new node info to client
 */
 static void
 sendNodeInfo(int trans_id)
 {
 	char *	res;
 	char	name[64];
 	char *	str_name;

 	/* node name */
 	
	sprintf(name, "node0");
	proxy_cstring_to_str(name, &str_name);
	asprintf(&res, "%d %d %s", trans_id, RTEV_NEW_NODE, str_name);
	AddToList(eventList, (void *)res);
	free(str_name);
 }


/**
 * Send new queue info to client
 */
 static void
 sendQueueInfo(int trans_id)
 {
 	char *	res;
 	char	name[64];
 	char *	str_name;

 	/* queue name */
 	
	sprintf(name, "default");
	proxy_cstring_to_str(name, &str_name);
	asprintf(&res, "%d %d %s", trans_id, RTEV_NEW_QUEUE, str_name);
	AddToList(eventList, (void *)res);
	free(str_name);
 }


/**
 * Send EVENT_RUNTIME_OK to client
 */
 static void
 sendRuntimeEventOK(int trans_id)
 {
 	char* res;
 	asprintf(&res, "%d %d", trans_id, RTEV_OK);
	AddToList(eventList, (void *)res);
 }


/**
 * Initialize the LSF service
 */
int
LSF_Initialize(int trans_id, char** args)
{
	char * version = args[0];
	fprintf(stdout, "LSF_Initialize (%d): version {%s}\n", trans_id, version); fflush(stdout);
	
	if (strncmp(version, "2.0", 3) != 0) {
		fprintf(stdout, "LSF_Initialize: incorrect version {%s}\n", version); fflush(stdout);
		AddToList(eventList, errorStr(1, "LSF_Initialize: incorrect version, should be 2.0"));
		return PROXY_RES_ERR;
	}

	sendRuntimeEventOK(trans_id);
	return PROXY_RES_OK;
}


/**
 * Initiate the model definition phase
 */
int
LSF_ModelDef(int trans_id, char **args)
{
	fprintf(stdout, "LSF_ModelDef (%d):\n", trans_id); fflush(stdout);
	
//	sendAttributes(trans_id);
	
	sendRuntimeEventOK(trans_id);
	return PROXY_RES_OK;
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
LSF_StartEvents(int trans_id, char **args)
{
	fprintf(stdout, "  LSF_StartEvents (%d):\n", trans_id); fflush(stdout);
	gStartEventsID = trans_id;

	sendMachineInfo(trans_id);
	sendNodeInfo(trans_id);
	sendQueueInfo(trans_id);
	
	return PROXY_RES_OK;	
}


/**
 * Stop polling for LSF change events
 */
 int
LSF_StopEvents(int trans_id, char **args)
{
	fprintf(stdout, "  LSF_StopEvents (%d):\n", trans_id); fflush(stdout);
	/* notification that start events have completed */
	sendRuntimeEventOK(gStartEventsID);
	gStartEventsID = 0;
	sendRuntimeEventOK(trans_id);
	return PROXY_RES_OK;	
}


/**
 * Submit a job with the given executable path and arguments (remote call from a client proxy)
 *
 * TODO - what about queues, should there be a LSF_Submit?
 */
int
LSF_SubmitJob(int trans_id, char **args)
{
#ifdef LSF_NOT_YET
	int					rtn;
	LS_LONG_INT			jobId;				
	struct submit*		jobSubReq;		/* Job specifications */
	struct submitReply*	jobSubReply;	/* Results of job submission */
	rtn = lsb_submit(jobSubReq, jobSubReply);
	rtn = lsb_modify(jobSubReq, jobSubReply, jobId);
#endif

	fprintf(stdout, "  LSF_SubmitJob (%d): %s %s %s %s %s %s\n", trans_id,
		args[0], args[1], args[2], args[3], args[4], args[5]); fflush(stdout);

	sendRuntimeEventOK(trans_id);

	return PROXY_RES_OK;
}


/**
 * Terminate a job given a jobid (remote call from a client proxy)
 */
int
LSF_TerminateJob(int trans_id, char **args)
{
#ifdef LSF
	int		rtn;			/* TODO - find what return value signifies from documentation */
	int		times = 0;		/* number of runs before job is deleted */
	int		options = 0;
#endif
	LS_LONG_INT jobid = atoi(args[1]);
	
	fprintf(stdout, "  LSF_TerminateJob (%d):\n", trans_id); fflush(stdout);
#ifdef LSF
	rtn = lsb_deletejob(jobid, times, options);
#endif

	return PROXY_RES_OK;
}


/**
 * Shutdown 
 */
int
LSF_Quit(int trans_id, char **args)
{
	fprintf(stdout, "LSF_Quit (%d):\n", trans_id); fflush(stdout);
	shutdownLSF();

	/* if events are running, ok the start events command  to complete it */
	if (gStartEventsID > 0) {
		sendRuntimeEventOK(gStartEventsID);
		gStartEventsID = 0;
	}
	sendRuntimeEventOK(trans_id);

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
    if (gStartEventsID && --nextHostPoll < 1) {
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
    if (gStartEventsID) {
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
	int rc = 0;

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
	
	if (ptp_signal_exit != 0) {
		char msg[LSF_MAX_MSG_SIZE], msg1[LSF_MAX_MSG_SIZE], msg2[LSF_MAX_MSG_SIZE];
		
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
