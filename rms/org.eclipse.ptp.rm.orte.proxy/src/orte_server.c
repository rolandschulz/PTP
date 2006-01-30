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

#include "orte_config.h" 

/*
 * Need to undef these if we include
 * two config.h files
 */
#undef PACKAGE_BUGREPORT
#undef PACKAGE_NAME
#undef PACKAGE_STRING
#undef PACKAGE_TARNAME
#undef PACKAGE_VERSION
#include "config.h"

#include <getopt.h>
#include <unistd.h>
#include <grp.h>
#include <pwd.h>
#include <stdbool.h>
#include <errno.h>

#include <proxy.h>
#include <proxy_tcp.h>
#include <proxy_event.h>
#include <handler.h>
#include <list.h>
#include <args.h>

#include "include/orte_constants.h"
#include "mca/errmgr/errmgr.h"
#include "runtime/runtime.h"
#include "mca/gpr/gpr.h"
#include "orte/mca/iof/iof.h"
#include "mca/rmgr/rmgr.h"
#include "mca/base/base.h"
#include "mca/errmgr/errmgr.h"
#include "mca/rml/rml.h"

#ifdef HAVE_SYS_BPROC_H
#include "orte/mca/soh/bproc/soh_bproc.h"
#endif
//#include "mca/pls/base/base.h"
 
#include "event/event.h"

#include "threads/condition.h"
#include "tools/orted/orted.h"

#define DEFAULT_PROXY			"tcp"
#define DEFAULT_ORTED_ARGS	"orted --scope public --seed --persistent"

#define RTEV_OFFSET				200
#define RTEV_OK					RTEV_OFFSET + 0
#define RTEV_ERROR				RTEV_OFFSET + 1
#define RTEV_JOBSTATE				RTEV_OFFSET + 2
#define RTEV_PROCS				RTEV_OFFSET + 4
#define RTEV_PATTR				RTEV_OFFSET + 5
#define RTEV_NODES				RTEV_OFFSET + 7
#define RTEV_NATTR				RTEV_OFFSET + 8
#define RTEV_NEWJOB				RTEV_OFFSET + 11
#define RTEV_PROCOUT				RTEV_OFFSET + 12
#define RTEV_NODECHANGE			RTEV_OFFSET + 13

#define RTEV_ERROR_ORTE_INIT		RTEV_OFFSET + 1000
#define RTEV_ERROR_ORTE_FINALIZE	RTEV_OFFSET + 1001
#define RTEV_ERROR_ORTE_RUN		RTEV_OFFSET + 1002
#define RTEV_ERROR_TERMINATE_JOB	RTEV_OFFSET + 1003
#define RTEV_ERROR_PATTR			RTEV_OFFSET + 1004
#define RTEV_ERROR_PROCS			RTEV_OFFSET + 1005
#define RTEV_ERROR_NODES			RTEV_OFFSET + 1006
#define RTEV_ERROR_NATTR			RTEV_OFFSET + 1007
#define RTEV_ERROR_ORTE_BPROC_SUBSCRIBE	RTEV_OFFSET + 1008

#define JOB_STATE_NEW				5000

#define PTP_UINT32				1
#define PTP_STRING				2

int ORTEIsShutdown(void);
int ORTEQuit(void);
int ORTEInit(void);

int ORTEStartDaemon(char **);
int ORTERun(char **);
int ORTETerminateJob(char **);
int ORTEGetProcesses(char **);
int ORTEGetProcessAttribute(char **);
int ORTEGetNodes(char **);
int ORTEGetNodeAttribute(char **);
/*
int OMPIGetJobs(char **);
int OMPIGetMachines(char **);
int OMPIGetNodemachineID(char **);
*/

struct debug_job {
	int	debugger_jobid; // job ID of debugger
	int app_jobid; // job ID that will be used by program when it starts
	int	num_procs; // number of procs requested for program (debugger uses num_procs+1)
};
typedef struct debug_job debug_job;

int get_node_attribute(int machid, int node_num, char **input_keys, int *input_types, char **input_values, int input_num_keys);
int get_proc_attribute(orte_jobid_t jobid, int proc_num, char **input_keys, int *input_types, char **input_values, int input_num_keys);
static void job_state_callback(orte_jobid_t jobid, orte_proc_state_t state);
static int orte_console_send_command(orte_daemon_cmd_flag_t usercmd);
static void iof_callback(orte_process_name_t* src_name, orte_iof_base_tag_t src_tag, void* cbdata, const unsigned char* data, size_t count);
static void debug_add_job(int app_jobid, int debug_jobid);
static void debug_remove_job(int jobid);
static int debug_find_jobid(int jobid);

#ifdef HAVE_SYS_BPROC_H
int ORTE_Subscribe_Bproc(void);
#endif

int 			orte_shutdown = 0;
proxy_svr *	orte_proxy;
int			is_orte_initialized = 0;
int			orted_pid = 0;
List *		eventList;
List *		debugJobs;

static proxy_handler_funcs handler_funcs = {
	RegisterFileHandler,		// regfile() - call to register a file handler
	UnregisterFileHandler,	// unregfile() - called to unregister file handler
	RegisterEventHandler,		// regeventhandler() - called to register the proxy event handler
	CallEventHandlers
};

static proxy_svr_helper_funcs helper_funcs = {
	NULL,					// newconn() - can be used to reject connections
	NULL,					// numservers() - if there are multiple servers, return the number
	ORTEIsShutdown,			// shutdown_completed() - proxy will not complete until this returns true
	ORTEQuit					// quit() - called when quit message received
};

static proxy_svr_commands command_tab[] = {
	{"STARTDAEMON", 	ORTEStartDaemon},
	{"RUN",			ORTERun},
	{"TERMJOB",		ORTETerminateJob},
	{"GETPROCS",		ORTEGetProcesses},
	{"GETPATTR",	   	ORTEGetProcessAttribute},
	{"GETNODES",		ORTEGetNodes},
	{"GETNATTR",		ORTEGetNodeAttribute},
	/*
	{"GETJOBS",		OMPIGetJobs},
	{"GETMACHS",		OMPIGetMachines},
	{"GETNMID",		OMPIGetNodemachineID},
	*/
	{NULL,			NULL},
};

static struct option longopts[] = {
	{"proxy",			required_argument,	NULL, 	'P'}, 
	{"port",				required_argument,	NULL, 	'p'}, 
	{"host",				required_argument,	NULL, 	'h'}, 
	{NULL,				0,					NULL,	0}
};

char *
ORTEErrorStr(int type, char *msg)
{
	char *str;
	static char *res = NULL;
	
	if (res != NULL)
		free(res);
	
	proxy_cstring_to_str(msg, &str);
	asprintf(&res, "%d %d %s", RTEV_ERROR, type, str);
	free(str);
	
	return res;	
}

int
ORTEInitialized(void)
{
	return is_orte_initialized;
}

int
ORTECheckErrorCode(int type, int rc)
{
	if(rc != ORTE_SUCCESS) {
		printf("ARgh!  An error!\n"); fflush(stdout);
		printf("ERROR %s\n", ORTE_ERROR_NAME(rc)); fflush(stdout);
		proxy_svr_event_callback(orte_proxy, ORTEErrorStr(type, (char *)ORTE_ERROR_NAME(rc)));
		return 1;
	}
	
	return 0;
}

//jstring jompi_bin_path, jstring jorted_path, jstring jorted_bin, jobjectArray array
int
ORTEStartDaemon(char **args)
{
	int ret;
	char *res;
	
	printf("StartDaemon(%s %s)\n", ORTED, DEFAULT_ORTED_ARGS); fflush(stdout);
	
	switch(orted_pid = fork()) {
		case -1:
			{
				res = ORTEErrorStr(RTEV_ERROR_ORTE_INIT, "fork() failed for the orted spawn in ORTESpawnDaemon");
				proxy_svr_event_callback(orte_proxy, res);
				printf("\t%s\n", res);
				return 1;
			}
			break;
		/* child */
		case 0:
			{
				char **orted_args = Str2Args(DEFAULT_ORTED_ARGS);
				
				/* spawn the daemon */
				printf("Starting execv now!\n"); fflush(stdout);
				errno = 0;

				ret = execv(ORTED, orted_args);

				FreeArgs(orted_args);
			}
			break;
	    /* parent */
	    default:
	    		printf("PARENT: orted_pid = %d\n", orted_pid); fflush(stdout);
			/* sleep - letting the daemon get started up */
			sleep(1);
			wait(&ret);
			//printf("parent ret from child = %d\n", ret); fflush(stdout);
			break;
	}

	if (ret != 0) {
		printf("Start daemon returning ERROR.\n");
		res = ORTEErrorStr(RTEV_ERROR_ORTE_INIT, "initialization failed");
		proxy_svr_event_callback(orte_proxy, res);
		return 0;
	}
	
	if (ORTEInit() != 0)
		return 0;
	
#ifdef HAVE_SYS_BPROC_H
	ORTE_Subscribe_Bproc();
#endif
		
	orte_rmgr.query();
	
	printf("Start daemon returning OK.\n");
	asprintf(&res, "%d", RTEV_OK);
	proxy_svr_event_callback(orte_proxy, res);
	free(res);
	
	return 0;
}

int
ORTEInit(void)
{
	int rc;
	
	printf("ORTEInit\n"); fflush(stdout);
	
	/* this makes the orte_init() fail if the orte daemon isn't
	 * running */
	putenv("OMPI_MCA_orte_univ_exist=1");
	
	rc = orte_init(true);
	
	if(ORTECheckErrorCode(RTEV_ERROR_ORTE_INIT, rc)) return 1;
	
	is_orte_initialized = true;
	
	return 0;
}

#ifdef HAVE_SYS_BPROC_H
void 
bproc_notify_callback(orte_gpr_notify_data_t *data, void *cbdata)
{
	size_t i, j, k;
	orte_gpr_value_t **values, *value;
	orte_gpr_keyval_t **keyvals;
	char *res, *kv, *str1, *str2, *nodename;
	int machID;
	
	printf("BPROC NOTIFY CALLBACK!\n"); fflush(stdout);
	
	values = (orte_gpr_value_t**)(data->values)->addr;
	
	machID = 0;
	
	for(i=0, k=0; k<data->cnt && i < (data->values)->size; i++) {
		if(values[i] == NULL) continue;
		
		k++;
		value = values[i];
		keyvals = value->keyvals;
		
		asprintf(&nodename, "%s", value->tokens[1]);
		printf("NODE NAME = %s\n", nodename);
		
		for(j=0; j<value->cnt; j++) {
			orte_gpr_keyval_t *keyval = keyvals[j];
			
			printf("--- BPROC CHANGE: key = %s\n", keyval->key); fflush(stdout);
			
			switch(keyval->type) {
				case ORTE_NODE_STATE:
					printf("--- BPROC CHANGE: (state) val = %d\n", keyval->value.node_state); fflush(stdout);
					asprintf(&kv, "%s=%d", keyval->key, keyval->value.node_state);
					break;
				case ORTE_STRING:
					printf("--- BPROC CHANGE: (str) val = %s\n", keyval->value.strptr); fflush(stdout);
					asprintf(&kv, "%s=%s", keyval->key, keyval->value.strptr);
					break;
				case ORTE_UINT32:
					printf("--- BPROC CHANGE: (uint32) val = %d\n", keyval->value.ui32); fflush(stdout);
					asprintf(&kv, "%s=%d", keyval->key, keyval->value.ui32);
					break;
				default:
					printf("--- BPROC CHANGE: unknown type %d\n", keyval->type); fflush(stdout);
					asprintf(&kv, "%s=%d", keyval->key, keyval->type);
			}
			
			proxy_cstring_to_str(nodename, &str1);
			proxy_cstring_to_str(kv, &str2);
			asprintf(&res, "%d %d %s %s", RTEV_NODECHANGE, machID, str1, str2);
        	proxy_svr_event_callback(orte_proxy, res);
        	free(res);
        	free(kv);
        	free(str1);
        	free(str2);
        	}
		
		free(nodename);
	}
}

int 
ORTE_Subscribe_Bproc(void)
{
	int i, rc, ret;
	orte_gpr_subscription_t sub, *subs;
	orte_gpr_value_t value, *values;
	
	OBJ_CONSTRUCT(&sub, orte_gpr_subscription_t);
	sub.action = ORTE_GPR_NOTIFY_VALUE_CHG;
	
	OBJ_CONSTRUCT(&values, orte_gpr_value_t);
	values = &value;
	sub.values = &values;
	sub.cnt = 1; /* number of values */
	value.addr_mode = ORTE_GPR_TOKENS_XAND | ORTE_GPR_KEYS_OR;
	value.segment = strdup(ORTE_NODE_SEGMENT);
	
	value.cnt = 6; /* number of keyvals */
	value.keyvals = (orte_gpr_keyval_t**)malloc(value.cnt * sizeof(orte_gpr_keyval_t*));
	
	i = 0;
	
	value.keyvals[i] = OBJ_NEW(orte_gpr_keyval_t);
	value.keyvals[i++]->key = strdup(ORTE_NODE_NAME_KEY);
	
	value.keyvals[i] = OBJ_NEW(orte_gpr_keyval_t);
	value.keyvals[i++]->key = strdup(ORTE_NODE_STATE_KEY);
	
	value.keyvals[i] = OBJ_NEW(orte_gpr_keyval_t);
	value.keyvals[i++]->key = strdup(ORTE_SOH_BPROC_NODE_STATUS);
	
	value.keyvals[i] = OBJ_NEW(orte_gpr_keyval_t);
	value.keyvals[i++]->key = strdup(ORTE_SOH_BPROC_NODE_MODE);
	
	value.keyvals[i] = OBJ_NEW(orte_gpr_keyval_t);
	value.keyvals[i++]->key = strdup(ORTE_SOH_BPROC_NODE_USER);
	
	value.keyvals[i] = OBJ_NEW(orte_gpr_keyval_t);
	value.keyvals[i++]->key = strdup(ORTE_SOH_BPROC_NODE_GROUP);
	
	/* any token */
	value.tokens = NULL;
	value.num_tokens = 0;
	
	sub.cbfunc = bproc_notify_callback;
	sub.user_tag = NULL;
	
	subs = &sub;
	rc = orte_gpr.subscribe(1, &subs, 0, NULL);
	
	if(ORTECheckErrorCode(RTEV_ERROR_ORTE_BPROC_SUBSCRIBE, rc)) return 1;
	
	return 0;
}
#endif

/* this finalizes the registry */
int
ORTEFinalize(void)
{
	int rc;
	
	//opal_mutex_lock(&opal_event_lock);
	rc = orte_finalize();
	//opal_mutex_unlock(&opal_event_lock);
	
	if(ORTECheckErrorCode(RTEV_ERROR_ORTE_FINALIZE, rc)) return 1;
	
	return 0;
}

/* this tells the daemon to exit */
int
ORTEShutdown(void)
{
	printf("ORTEShutdown() called.  Telling daemon to turn off.\n"); fflush(stdout);
	//ompi_sendcmd(ORTE_DAEMON_EXIT_CMD);
	orte_console_send_command(ORTE_DAEMON_EXIT_CMD);
	printf("ORTEShutdown() - told ORTEd to exit.\n"); fflush(stdout);
	orte_shutdown++;
	
	orte_finalize();
	
	return 0;
}

int
ORTEIsShutdown(void)
{
	return orte_shutdown != 0;
}

/* this is the event progress bit.  we'll have to figure out how to hook
 * this in correctly */
int
ORTEProgress(void)
{
	fd_set			rfds;
	fd_set			wfds;
	fd_set			efds;
	int				res;
	int				nfds = 0;
	char *			event;
	struct timeval	tv;
	handler *		h;
	
	struct timeval TIMEOUT;
	TIMEOUT.tv_sec = 0;
	TIMEOUT.tv_usec = 2000;

	for (SetList(eventList); (event = (char *)GetListElement(eventList)) != NULL; ) {
		proxy_svr_event_callback(orte_proxy, event);
		RemoveFromList(eventList, (void *)event);
		free(event);	
	}
	
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
	
	/* only run the progress of the ORTE code if we've initted the ORTE daemon */
	if(ORTEInitialized()) {
		opal_event_loop(OPAL_EVLOOP_ONCE);
	}
	
	return PROXY_RES_OK;
}

/* terminate a job, given a jobid */
int
ORTETerminateJob(char **args)
{
	int rc;
	int jobid = atoi(args[1]);
	
	rc = orte_rmgr.terminate_job(jobid);
	if(ORTECheckErrorCode(RTEV_ERROR_TERMINATE_JOB, rc)) return 1;
	
	if ((jobid = debug_find_jobid(jobid)) >= 0) {
		rc = orte_rmgr.terminate_job(jobid);
		if(ORTECheckErrorCode(RTEV_ERROR_TERMINATE_JOB, rc)) return 1;
	}
	
	return PROXY_RES_OK;
}

static void
debug_add_job(int app_jobid, int debug_jobid)
{
	debug_job *	djob = (debug_job *)malloc(sizeof(debug_job));
    djob->app_jobid = app_jobid;
    djob->debugger_jobid = debug_jobid;
    AddToList(debugJobs, (void *)djob);
}

static void
debug_remove_job(int jobid)
{
	debug_job *	djob;
	printf("remove debug job %d\n", jobid); fflush(stdout);
	for (SetList(debugJobs); (djob = (debug_job *)GetListElement(debugJobs)) != NULL; ) {
		if (djob->debugger_jobid == jobid) {
			RemoveFromList(debugJobs, (void *)djob);
			break;
		}
	}
}

static int
debug_find_jobid(int jobid)
{
	debug_job *	djob;
	
	for (SetList(debugJobs); (djob = (debug_job *)GetListElement(debugJobs)) != NULL; ) {
		if (djob->app_jobid == jobid) {
			return djob->debugger_jobid;
		}
	}
	return -1;
}

/*
 * If we're under debug control, let the debugger handle process state update. 
 * We still want to wire up stdio though.
 * 
 * Note: this will only be used if the debugger allows the program to
 * reach MPI_Init(), which may not ever happen. Don't rely this to do anything
 * for any type of job.
 * 
 * Note also: the debugger manages process state updates so we don't need
 * to send events back to the runtime.
 */
static void
debug_app_job_state_callback(orte_jobid_t jobid, orte_proc_state_t state)
{
	int			rc;
	orte_process_name_t* name;

	switch(state) {
		case ORTE_PROC_STATE_INIT:
			if (ORTE_SUCCESS != (rc = orte_ns.create_process_name(&name, 0, jobid, 0))) {
                ORTE_ERROR_LOG(rc);
                break;
            	}
 			if (ORTE_SUCCESS != (rc = orte_iof.iof_subscribe(name, ORTE_NS_CMP_JOBID, ORTE_IOF_STDOUT, iof_callback, NULL))) {                
				opal_output(0, "[%s:%d] orte_iof.iof_subscribed failed\n", __FILE__, __LINE__);
            	}
            	if (ORTE_SUCCESS != (rc = orte_iof.iof_subscribe(name, ORTE_NS_CMP_JOBID, ORTE_IOF_STDERR, iof_callback, NULL))) {                
                	opal_output(0, "[%s:%d] orte_iof.iof_subscribed failed\n", __FILE__, __LINE__);
           	}
			break;
		case ORTE_PROC_STATE_TERMINATED:
			if (ORTE_SUCCESS != (rc = orte_ns.create_process_name(&name, 0, jobid, 0))) {
                ORTE_ERROR_LOG(rc);
                break;
            	}
			if (ORTE_SUCCESS != (rc = orte_iof.iof_unsubscribe(name, ORTE_NS_CMP_JOBID, ORTE_IOF_STDERR))) {                
                	opal_output(0, "[%s:%d] orte_iof.iof_unsubscribed failed\n", __FILE__, __LINE__);
           	}
			break;
	}
}

/*
 * job_state_callback for the debugger. Detects debugger exit and cleans up
 * job id map.
 */
static void
debug_job_state_callback(orte_jobid_t jobid, orte_proc_state_t state)
{
	switch(state) {
		case ORTE_PROC_STATE_TERMINATED:
		case ORTE_PROC_STATE_ABORTED:
           	debug_remove_job(jobid);
    			break;
	}
}

static void debug_wireup_stdin(orte_jobid_t jobid)
{
	int rc;
	orte_process_name_t* name;
	
	if (ORTE_SUCCESS != (rc = orte_ns.create_process_name(&name, 0, jobid, 0))) {
		ORTE_ERROR_LOG(rc);
		return;
	}
	if (ORTE_SUCCESS != (rc = orte_iof.iof_push(name, ORTE_NS_CMP_JOBID, ORTE_IOF_STDIN, 0))) {
		ORTE_ERROR_LOG(rc);
	}
}

static void debug_callback(orte_gpr_notify_data_t *data, void *cbdata)
{   
	orte_rmgr_cb_fn_t cbfunc = (orte_rmgr_cb_fn_t)cbdata;
	orte_gpr_value_t **values, *value;
	orte_gpr_keyval_t** keyvals;
	orte_jobid_t jobid;
	size_t i, j, k;
	int rc;
	    
	/* we made sure in the subscriptions that at least one
	 * value is always returned
	 * get the jobid from the segment name in the first value
	 */
	values = (orte_gpr_value_t**)(data->values)->addr;
	if (ORTE_SUCCESS != (rc =
			orte_schema.extract_jobid_from_segment_name(&jobid,
			values[0]->segment))) {
			ORTE_ERROR_LOG(rc);
		return;
	}

	for(i = 0, k=0; k < data->cnt && i < (data->values)->size; i++) {
		if (NULL != values[i]) {
			k++;
			value = values[i];
			/* determine the state change */
			keyvals = value->keyvals;
			for(j=0; j<value->cnt; j++) { 
				orte_gpr_keyval_t* keyval = keyvals[j];
				if(strcmp(keyval->key, ORTE_PROC_NUM_AT_STG1) == 0) {
					(*cbfunc)(jobid,ORTE_PROC_STATE_AT_STG1);
					/* BWB - XXX - FIX ME: this needs to happen when all
					   are LAUNCHED, before STG1 */
					debug_wireup_stdin(jobid);
					continue;
				}
				if(strcmp(keyval->key, ORTE_PROC_NUM_AT_STG2) == 0) {
					(*cbfunc)(jobid,ORTE_PROC_STATE_AT_STG2);
					continue;
				}
				if(strcmp(keyval->key, ORTE_PROC_NUM_AT_STG3) == 0) {
					(*cbfunc)(jobid,ORTE_PROC_STATE_AT_STG3);
					continue;
				}
				if(strcmp(keyval->key, ORTE_PROC_NUM_FINALIZED) == 0) {
					(*cbfunc)(jobid,ORTE_PROC_STATE_FINALIZED);
					continue;
				}
				if(strcmp(keyval->key, ORTE_PROC_NUM_TERMINATED) == 0) {
					(*cbfunc)(jobid,ORTE_PROC_STATE_TERMINATED);
					continue;
				}
				if(strcmp(keyval->key, ORTE_PROC_NUM_ABORTED) == 0) {
					(*cbfunc)(jobid,ORTE_PROC_STATE_ABORTED);
					continue;
				}
			}
		}
	}
}

int
debug_allocate(orte_app_context_t** app_context, size_t num_context, orte_jobid_t* jobid, orte_rmgr_cb_fn_t cbfunc)
{
	int rc;
	orte_process_name_t* name;

	/* 
	 * Initialize job segment and allocate resources
	 */ /* JJH Insert C/N mapping stuff here */
	if (ORTE_SUCCESS != (rc = orte_rmgr.create(app_context,num_context,jobid))) {
		ORTE_ERROR_LOG(rc);
		return rc;
	}
	
	if (ORTE_SUCCESS != (rc = orte_rmgr.allocate(*jobid))) {
		ORTE_ERROR_LOG(rc);
		return rc;
	}
	
	if (ORTE_SUCCESS != (rc = orte_rmgr.map(*jobid))) {
		ORTE_ERROR_LOG(rc);
		return rc;
	}
	/* 
	 * setup I/O forwarding
	 */
	if (ORTE_SUCCESS != (rc = orte_ns.create_process_name(&name, 0, *jobid, 0))) {      
		ORTE_ERROR_LOG(rc);
		return rc;
	} if (ORTE_SUCCESS != (rc = orte_iof.iof_pull(name, ORTE_NS_CMP_JOBID, ORTE_IOF_STDOUT, 1))) {
		ORTE_ERROR_LOG(rc);
		return rc;
	} if (ORTE_SUCCESS != (rc = orte_iof.iof_pull(name, ORTE_NS_CMP_JOBID, ORTE_IOF_STDERR, 2))) {
		ORTE_ERROR_LOG(rc);
		return rc;
	}
    
	/* 
	 * setup callback
	 */
	
	if(NULL != cbfunc) {
		rc = orte_rmgr_base_proc_stage_gate_subscribe(*jobid, debug_callback, (void*)cbfunc, ORTE_STAGE_GATE_ALL);
		if(ORTE_SUCCESS != rc) {
			ORTE_ERROR_LOG(rc);
			return rc;
		}
	}
	
	orte_ns.free_name(&name);
	
	return ORTE_SUCCESS;
}

int
debug_spawn(char *debug_path, int argc, char **argv, orte_app_context_t** app_context, size_t num_context, orte_jobid_t* jobid)
{
	int					i;
	int					rc;
	orte_jobid_t			debug_jobid;
	orte_jobid_t			app_jobid;
	orte_app_context_t **	debug_context;

	if ((rc = debug_allocate(app_context, num_context, &app_jobid, debug_app_job_state_callback)) != ORTE_SUCCESS)
		return rc;

	debug_context = malloc(sizeof(orte_app_context_t *));
	debug_context[0] = OBJ_NEW(orte_app_context_t);
	debug_context[0]->num_procs = app_context[0]->num_procs + 1;
	debug_context[0]->app = strdup(debug_path);
	debug_context[0]->cwd = strdup(app_context[0]->cwd);
	/* no special environment variables */
	debug_context[0]->num_env = 0;
	debug_context[0]->env = NULL;
	/* no special mapping of processes to nodes */
	debug_context[0]->num_map = 0;
	debug_context[0]->map_data = NULL;
	/* setup argv */
	debug_context[0]->argv = (char **)malloc((argc+2) * sizeof(char *));
	for (i = 0; i < argc; i++) {
		debug_context[0]->argv[i] = strdup(argv[i]);
	}
	asprintf(&debug_context[0]->argv[i++], "--jobid=%d", app_jobid);
	debug_context[0]->argv[i++] = NULL;
	debug_context[0]->argc = i;

	if ((rc = debug_allocate(debug_context, num_context, &debug_jobid, debug_job_state_callback)) != ORTE_SUCCESS) {
		// TODO free debug_context...
		return rc;
	}

	/*
	 * launch the debugger
	 */
	if (ORTE_SUCCESS != (rc = orte_rmgr.launch(debug_jobid))) {
		ORTE_ERROR_LOG(rc);
		return rc;
	}
    	
	*jobid = app_jobid;
    
    debug_add_job(app_jobid, debug_jobid);
    
	return ORTE_SUCCESS;
}

/* spawn a job with the given executable path and # of procs. */
int
ORTERun(char **args)
{
	int					rc; 
	int					i;
	int					a;
	int					num_procs;
	int					debug = 0;
	int					num_apps;
	int					debug_argc = 0;
	char					pgm_name[128];
	char					cwd[128];
	char *				c;
	char *				res;
	char *				exec_path;
	char *				debug_exec_path;
	char **				debug_args;
	orte_app_context_t **	apps;
	orte_jobid_t			jobid = ORTE_JOBID_MAX;

	for (i = 1; args[i] != NULL; i += 2) {
		if (strcmp(args[i], "pathToExecutable") == 0) {
			exec_path = args[i+1];
		} else if (strcmp(args[i], "numberOfProcesses") == 0) {
			num_procs = atoi(args[i+1]);
		} else if (strcmp(args[i], "numberOfProcessesPerNode") == 0) {
			// not yet
		} else if (strcmp(args[i], "firstNodeNumber") == 0) {
			// not yet
		} else if (strcmp(args[i], "debuggerPath") == 0) {
			debug_exec_path = args[i+1];
			debug = 1;
		} else if (strcmp(args[i], "debuggerArg") == 0) {
			debug_argc++;
		}
	}
	
	if (access(exec_path, X_OK) < 0) {
		printf("ERROR exec_path = '%s' not found\n", exec_path); fflush(stdout);
		proxy_svr_event_callback(orte_proxy, ORTEErrorStr(RTEV_ERROR_ORTE_RUN, strerror(errno)));
		return PROXY_RES_OK;
	}
	
	if (debug) {
		printf("DeBUG!\n"); fflush(stdout);
		
		if (access(debug_exec_path, X_OK) < 0) {
			printf("ERROR debug_exec_path = '%s' not found\n", debug_exec_path); fflush(stdout);
			proxy_svr_event_callback(orte_proxy, ORTEErrorStr(RTEV_ERROR_ORTE_RUN, strerror(errno)));
			return PROXY_RES_OK;
		}
		
		debug_argc++;
		debug_args = (char **)malloc((debug_argc+1) * sizeof(char *));
		debug_args[0] = debug_exec_path;
		for (i = 1, a = 1; args[i] != NULL; i += 2) {
			if (strcmp(args[i], "debuggerArg") == 0) {
				debug_args[a++] = args[i+1];
			}
		}
		debug_args[a] = NULL;
	}
	
	c = rindex(exec_path, '/');
	strncpy(pgm_name, c + 1, strlen(c));
	strncpy(cwd, exec_path, c - exec_path + 1);
	cwd[c-exec_path+1] = '\0';

	/* hard coded test for spawning just 1 job (JOB not PROCESSES!) */
	num_apps = 1;

	/* format the app_context_t struct */
	apps = malloc(sizeof(orte_app_context_t *) * num_apps);
	apps[0] = OBJ_NEW(orte_app_context_t);
	apps[0]->num_procs = num_procs;
	apps[0]->app = strdup(exec_path);
	apps[0]->cwd = strdup(cwd);
	/* no special environment variables */
	apps[0]->num_env = 0;
	apps[0]->env = NULL;
	/* no special mapping of processes to nodes */
	apps[0]->num_map = 0;
	apps[0]->map_data = NULL;
	/* setup argv */
	apps[0]->argv = (char **)malloc((2) * sizeof(char *));
	apps[0]->argv[0] = strdup(pgm_name);
	apps[0]->argv[1] = NULL;
	apps[0]->argc = 1;
	
	printf("(debug ? %d) Spawning %d processes of job '%s'\n", debug, (int)apps[0]->num_procs, apps[0]->app);
	printf("\tprogram name '%s'\n", apps[0]->argv[0]);
	fflush(stdout);
	
	/* calls the ORTE spawn function with the app to spawn.  Return the
	 * jobid assigned by the registry/ORTE.  Passes a callback function
	 * that ORTE will call with state change on this job */
	if (!debug)
		rc = orte_rmgr.spawn(apps, num_apps, &jobid, job_state_callback);
	else
		rc = debug_spawn(debug_exec_path, debug_argc, debug_args, apps, num_apps, &jobid);

	printf("SPAWNED [error code %d = '%s'], now unlocking\n", rc, ORTE_ERROR_NAME(rc)); fflush(stdout);
	
	if(ORTECheckErrorCode(RTEV_ERROR_ORTE_RUN, rc)) return 1;

	printf("NEW JOBID = %d\n", (int)jobid); fflush(stdout);
	
	asprintf(&res, "%d %d", RTEV_NEWJOB, jobid);
	printf("res = '%s'\n", res); fflush(stdout);
	proxy_svr_event_callback(orte_proxy, res);

	if(res) free(res);
	
	if(debug) free(debug_args);
	
//	/* generate an event stating what the new/assigned job ID is.
//	 * The caller must record this and use this as an identifier to get
//	 * information about a job */
//	//GenerateEvent(ORTE_RUN, jobid);
	
	printf("Returning from ORTERun\n"); fflush(stdout);
	
	return PROXY_RES_OK;
}

static void iof_callback(
    orte_process_name_t* src_name,
    orte_iof_base_tag_t src_tag,
    void* cbdata,
    const unsigned char* data,
    size_t count)
{
	char *res, *str;
	char line[1024];
	
	printf("IO callback!  count = %d\n", (int)count); fflush(stdout);
    if(count > 0) {
        fprintf(stdout, "[%lu,%lu,%lu] ", ORTE_NAME_ARGS(src_name));
        strncpy((char*)line, (char*)data, count);
        if(line[count-1] == '\n') line[count-1] = '\0';
        line[count] = '\0';
        //printf("STR = '%s'\n", str);
        /* src_name->jobid = jobid
         * src_name->vpid = processID */
        //write(STDOUT_FILENO, data, count);
        proxy_cstring_to_str(line, &str);
        asprintf(&res, "%d %d %d %s", RTEV_PROCOUT, src_name->jobid, src_name->vpid, str);
        proxy_svr_event_callback(orte_proxy, res);
        free(res);
        free(str);
    }
}

static void
job_state_callback(orte_jobid_t jobid, orte_proc_state_t state)
{
	char *				res;
	int					rc;
	orte_process_name_t *	name;
	
	printf("JOB STATE CALLBACK!\n"); fflush(stdout);
		
	/* not sure yet how we want to handle this callback, what events
	 * we want to generate, but here are the states that I know of
	 * that a job can go through.  I've watched ORTE call this callback
	 * with each of these states.  We'll want to come in here and
	 * generate events where appropriate */
	
	//printf("job_state_callback(%d)\n", jobid); fflush(stdout);
	switch(state) {
		case ORTE_PROC_STATE_INIT:
			printf("    state = ORTE_PROC_STATE_INIT\n");
			if (ORTE_SUCCESS != (rc = orte_ns.create_process_name(&name, 0, jobid, 0))) {
                ORTE_ERROR_LOG(rc);
                break;
            	}
            	printf("name = '%s'\n", (char *)name); fflush(stdout);
            	
			if (ORTE_SUCCESS != (rc = orte_iof.iof_subscribe(name, ORTE_NS_CMP_JOBID, ORTE_IOF_STDOUT, iof_callback, NULL))) {                
				opal_output(0, "[%s:%d] orte_iof.iof_subscribed failed\n", __FILE__, __LINE__);
            	}
            	if (ORTE_SUCCESS != (rc = orte_iof.iof_subscribe(name, ORTE_NS_CMP_JOBID, ORTE_IOF_STDERR, iof_callback, NULL))) {                
                	opal_output(0, "[%s:%d] orte_iof.iof_subscribed failed\n", __FILE__, __LINE__);
           	}
			break;
		case ORTE_PROC_STATE_LAUNCHED:
			printf("    state = ORTE_PROC_STATE_LAUNCHED\n");
			break;
		case ORTE_PROC_STATE_AT_STG1:
			printf("    state = ORTE_PROC_STATE_AT_STG1\n");
			break;
		case ORTE_PROC_STATE_AT_STG2:
			printf("    state = ORTE_PROC_STATE_AT_STG2\n");
			break;
		case ORTE_PROC_STATE_RUNNING:
			printf("    state = ORTE_PROC_STATE_RUNNING\n");
			break;
		case ORTE_PROC_STATE_AT_STG3:
			printf("    state = ORTE_PROC_STATE_AT_STG3\n");
			break;
		case ORTE_PROC_STATE_FINALIZED:
			printf("    state = ORTE_PROC_STATE_FINALIZED\n");
			break;
		case ORTE_PROC_STATE_TERMINATED:
			printf("    state = ORTE_PROC_STATE_TERMINATED\n");
			if (ORTE_SUCCESS != (rc = orte_ns.create_process_name(&name, 0, jobid, 0))) {
                ORTE_ERROR_LOG(rc);
                break;
            	}
            	printf("name = %s\n", (char *)name); fflush(stdout);
			if (ORTE_SUCCESS != (rc = orte_iof.iof_unsubscribe(name, ORTE_NS_CMP_JOBID, ORTE_IOF_STDERR))) {                
				opal_output(0, "[%s:%d] orte_iof.iof_unsubscribed failed\n", __FILE__, __LINE__);
			}
			break;
		case ORTE_PROC_STATE_ABORTED:
			printf("    state = ORTE_PROC_STATE_ABORTED\n");
			break;
	}
	
//	printf("A!\n"); fflush(stdout);
	asprintf(&res, "%d %d %d", RTEV_JOBSTATE, jobid, state);
//	printf("B!\n"); fflush(stdout);
	AddToList(eventList, (void *)res);
	printf("state callback retrning!\n"); fflush(stdout);
}

#if 0
/* this is an internal function we'll call from within this, consider
 * it 'private' */
static int 
ompi_sendcmd(orte_daemon_cmd_flag_t usercmd)
{
	orte_buffer_t *cmd;
	orte_daemon_cmd_flag_t command;
	int rc;
	orte_process_name_t seed={0,0,0};

	printf("ompi_sendcmd 1\n"); fflush(stdout);
	cmd = OBJ_NEW(orte_buffer_t);
	printf("ompi_sendcmd 2\n"); fflush(stdout);
	
	if (NULL == cmd) {
		fprintf(stderr, "console: comm failure\n");
		return 1;
	}
	command = usercmd;
	
	printf("ompi_sendcmd 3\n"); fflush(stdout);
	if (ORTE_SUCCESS != (rc = orte_dps.pack(cmd, &command, 1, ORTE_DAEMON_CMD))) {
		ORTE_ERROR_LOG(rc);
		return 1;
	}
	
	printf("ompi_sendcmd 4\n"); fflush(stdout);
	if (0 > orte_rml.send_buffer(&seed, cmd, ORTE_RML_TAG_DAEMON, 0)) {
		ORTE_ERROR_LOG(ORTE_ERR_COMM_FAILURE);
		return 1;
	}
	
	printf("ompi_sendcmd 5\n"); fflush(stdout);
	OBJ_RELEASE(cmd);
	printf("ompi_sendcmd 6\n"); fflush(stdout);
	
	return 0;
}
#endif

static int orte_console_send_command(orte_daemon_cmd_flag_t usercmd)
{
    orte_buffer_t *cmd;
    orte_daemon_cmd_flag_t command;
    orte_process_name_t    seed = {0,0,0};
    int rc;

	printf("ompi_sendcmd 1\n"); fflush(stdout);
	/*
    if(!daemon_is_active) {
        opal_show_help("help-orteconsole.txt", "orteconsole:no-daemon-started", false);
        return ORTE_SUCCESS;
    }*/
    printf("ompi_sendcmd 2\n"); fflush(stdout);

    cmd = OBJ_NEW(orte_buffer_t);
    printf("ompi_sendcmd 3\n"); fflush(stdout);
    if (NULL == cmd) {
        ORTE_ERROR_LOG(ORTE_ERROR);
        return ORTE_ERROR;
    }

    command = usercmd;
	
	printf("ompi_sendcmd 4\n"); fflush(stdout);
    rc = orte_dps.pack(cmd, &command, 1, ORTE_DAEMON_CMD);
    printf("ompi_sendcmd 5\n"); fflush(stdout);
    if ( ORTE_SUCCESS != rc ) {
        ORTE_ERROR_LOG(rc);
        OBJ_RELEASE(cmd);
        return rc;
    } 

	printf("ompi_sendcmd 6\n"); fflush(stdout);
    rc = orte_rml.send_buffer(&seed, cmd, ORTE_RML_TAG_DAEMON, 0);
    printf("ompi_sendcmd 7\n"); fflush(stdout);
    if ( 0 > rc ) {
        ORTE_ERROR_LOG(ORTE_ERR_COMM_FAILURE);
        OBJ_RELEASE(cmd);
        return ORTE_ERR_COMM_FAILURE;
    }

    OBJ_RELEASE(cmd);
    printf("ompi_sendcmd 8\n"); fflush(stdout);

    return ORTE_SUCCESS;
}


/* JOB RELATED FUNCTIONS */

/* get a dump of all the jobs known in the universe, this will return
 * an event with a list of jobIDs
 * EVENT RETURN:
 *   type = GET_JOBS
 *   data = [99,195,4555] <-- job IDs 
 * 
 * NOTE: we assume there are unlikely to be many jobs running simultaneously,
 * we can return this as a list of id's.
 */
//int
//OMPIGetJobs(char **args)
//{
//	char *	res;
//	char *	str;
//	int		n;
//	int *	jobs
//	
//	n = get_jobs(&jobs);
//	intarray_to_str(n, jobs, &str);
//	
//	asprintf(&res, "%d %s", RTEV_JOBS, str);
//	proxy_svr_event_callback(orte_proxy, res);
//	
//	free(str);
//	free(res);
//	
//	return PROXY_RES_OK;
//}
//

int
get_num_procs(orte_jobid_t jobid)
{
	char *keys[2];
	int rc, ret;
	size_t cnt;
	char *segment = NULL;
	char *jobid_str = NULL;
	orte_gpr_value_t **values;
	
	keys[0] = ORTE_PROC_PID_KEY;
	keys[1] = NULL;
	
	rc = orte_ns.convert_jobid_to_string(&jobid_str, jobid);
	if(rc != ORTE_SUCCESS) {
		ret = 0;
		goto cleanup;
	}
	
	asprintf(&segment, "%s-%s", ORTE_JOB_SEGMENT, jobid_str);
	
	rc = orte_gpr.get(ORTE_GPR_KEYS_OR | ORTE_GPR_TOKENS_OR,
			segment, NULL, keys, &cnt, &values);
	if(rc != ORTE_SUCCESS) {
		ret = 0;
		goto cleanup;
	}
	
	ret = cnt;
	
cleanup:
	if(jobid_str != NULL)
		free(jobid_str);
	if(segment != NULL)
		free(segment);
		
	return ret;
}

/* given a jobid (valid from OMPIGetJobs()) we request the number of processes
 * associated with this job.  A simple int response is sent back.
 */
int 
ORTEGetProcesses(char **args)
{
	int				jobid;
	int				procs;
	char *			res;
	
	jobid = atoi(args[1]);
	
	procs = get_num_procs((orte_jobid_t)jobid);
	
	if (procs == 0) {
		res = ORTEErrorStr(RTEV_ERROR_PROCS, "no such jobid or error retrieving processes on job");
	} else {
		asprintf(&res, "%d %d", RTEV_PROCS, procs);
	}
	
	proxy_svr_event_callback(orte_proxy, res);
	
	free(res);
	
	return PROXY_RES_OK;
}

/* given a jobID and a processID inside that job we attempt to find the values associated
 * with the given keys.  the caller can pass any number of possible keys and the response
 * message will be in the same order.
 * 
 * the user can pass in a process ID they want info on or -1 if they want info on EVERY
 * process contained in this job
 */
int
ORTEGetProcessAttribute(char **args)
{
	int				jobid;
	int				procid;
	char *			res;
	char *			str1;
	char *			str2;
	int				last_arg;
	int				i;
	char **			keys = NULL;
	char **			values = NULL;
	int *			types = NULL;
	int				tot_len;
	char *			valstr = NULL;
	int				values_len;
	
	printf("ORTEGetProcessAttribute!\n"); fflush(stdout);
	//return;
	
	jobid = atoi(args[1]);
	procid = atoi(args[2]);
	
	printf("\tjobid = %d, procid = %d\n", jobid, procid);
	
	/* run through the rest of the args, counting the keys */
	i = 3;
	while(args[i] != NULL) i++;
	
	last_arg = i;

	keys = (char**)malloc((last_arg - 3)*sizeof(char*));
	values_len = last_arg - 3;
	/* if they want to see ALL the processes then the requested values
	 * have to be multipled by how many processes are in this job */
	if(procid == -1) {
		int numprocs = get_num_procs((orte_jobid_t)jobid);
		values_len = values_len * numprocs;
	}
	values = (char**)malloc(values_len * sizeof(char*));
	types = (int*)malloc((last_arg - 3)*sizeof(int));
	
	/* go through the args now, set up the key array */
	for(i=3; i<last_arg; i++) {
		if(!strcmp(args[i], "ATTRIB_PROCESS_PID")) {
			asprintf(&(keys[i-3]), "%s", ORTE_PROC_PID_KEY);
			types[i-3] = PTP_UINT32;
		} else if(!strcmp(args[i], "ATTRIB_PROCESS_NODE_NAME")) {
			asprintf(&(keys[i-3]), "%s", ORTE_NODE_NAME_KEY);
			types[i-3] = PTP_STRING;
		} else {
			asprintf(&(keys[i-3]), "UNDEFINED");
			types[i-3] = PTP_STRING;
		}
	}
	
	for(i=3; i<last_arg; i++) {
		printf("BEFORE CALL KEYS[%d] = '%s'\n", i-3, keys[i-3]); fflush(stdout);
	}
		
	if(get_proc_attribute(jobid, procid, keys, types, values, last_arg-3)) {
		/* error - so bail out */
		res = ORTEErrorStr(RTEV_ERROR_PATTR, "error finding key on process or error getting keys");
		proxy_svr_event_callback(orte_proxy, res);
		
		return PROXY_RES_OK;
	}
	/* else we're good, use the values */
	
	tot_len = 0;
	for(i=0; i<values_len; i++) {
		printf("AFTER CALL! VALS[%d] = '%s'\n", i, values[i]); fflush(stdout);
		tot_len += strlen(values[i]);
	}
	
	tot_len += values_len * 2; /* add on some for spaces and null, etc - little bit of extra here */
	printf("totlen = %d\n", tot_len);
	
	for(i=0; i<values_len; i++) {
		proxy_cstring_to_str(values[i], &str1);
		if (i > 0) {
			str2 = valstr;
			asprintf(&valstr, "%s %s", str2, str1);
			free(str1);
			free(str2);
		} else
			valstr = str1;
	}
	
	printf("valSTR = '%s'\n", valstr);
	
	asprintf(&res, "%d %s", RTEV_PATTR, valstr);
	
	proxy_svr_event_callback(orte_proxy, res);
	
	free(res);
	
	for(i=3; i<last_arg; i++) {
		if(keys[i-3] != NULL) free(keys[i-3]);
	}
	for(i=0; i<values_len; i++) {
		if(values[i] != NULL) free(values[i]);
	}
		
	free(keys);
	free(values);
	free(types);
	free(valstr);
	
	return PROXY_RES_OK;
}

/*
** Very inefficient! Probably better to build a data structure
** from keyvals, then work with that.
*/
int get_ui32_value(orte_gpr_value_t *value, char *key)
{
        int k;

        for(k=0; k<value->cnt; k++) {
                orte_gpr_keyval_t* keyval = value->keyvals[k];
                if (strcmp(key, keyval->key) == 0)
                        return keyval->value.ui32;
        }

        return -1;
}

/*
** Ditto.
*/
char *get_str_value(orte_gpr_value_t *value, char *key)
{
        int k;

        for(k=0; k<value->cnt; k++) {
                orte_gpr_keyval_t* keyval = value->keyvals[k];
                if (strcmp(key, keyval->key) == 0)
                        return keyval->value.strptr;
        }

        return "";
}

int
get_proc_attribute(orte_jobid_t jobid, int proc_num, char **input_keys, int *input_types, char **input_values, int input_num_keys)
{
	char **keys;;
	int rc;
	size_t cnt;
	char *segment = NULL;
	char *jobid_str = NULL;
	orte_gpr_value_t **values;
	orte_gpr_value_t *value;
	int i, ret, j, min, max;
	
	keys = (char**)malloc((input_num_keys + 1)* sizeof(char*));
	for(i=0; i<input_num_keys; i++) {
		keys[i] = strdup(input_keys[i]);
	}
	/* null terminated */
	keys[input_num_keys] = NULL;
	
	rc = orte_ns.convert_jobid_to_string(&jobid_str, jobid);
	if(rc != ORTE_SUCCESS) {
		printf("ERROR: '%s'\n", ORTE_ERROR_NAME(rc));
		ret = 1;
		goto cleanup;
	}
	
	asprintf(&segment, "%s-%s", ORTE_JOB_SEGMENT, jobid_str);
	
	rc = orte_gpr.get(ORTE_GPR_KEYS_OR | ORTE_GPR_TOKENS_OR,
			segment, NULL, keys, &cnt, &values);
	if(rc != ORTE_SUCCESS) {
		printf("ERROR2: '%s'\n", ORTE_ERROR_NAME(rc));
		ret = 1;
		goto cleanup;
	}
	
	/* specified a proc bigger than any we know of in this job, bail out */
	if(proc_num != -1 && proc_num >= cnt) {
		ret = 1;
		//printf("BIGGER!  QUIT!\n"); fflush(stdout);
		goto cleanup;
	}
	
	/* they want a full dump */
	if(proc_num == -1) {
		min = 0;
		max = cnt;
	}
	/* they were looking for a specific process - quick and dirty */
	else {
		min = proc_num;
		max = proc_num + 1;
	}
	
	//printf("MAX = %d, MIN = %d\n", max, min); fflush(stdout);
		
	for(i=min; i<max; i++) {
		value = values[i];
			
		for(j=0; j<input_num_keys; j++) {
			switch(input_types[j]) {
				case PTP_STRING:
					asprintf(&(input_values[((i-min) * input_num_keys) + j]), "%s", get_str_value(value, input_keys[j]));
					break;
				case PTP_UINT32:
					asprintf(&(input_values[((i-min) * input_num_keys) + j]), "%d", get_ui32_value(value, input_keys[j]));
					break;
			}
			//printf("VALUES IN GET FUNC[%d][%d] = '%s'\n", i, j, input_values[((i-min) * input_num_keys) + j]); fflush(stdout);
		}
	}
	
	/* success */
	ret = 0;
	
cleanup:
	if(keys != NULL) {
		for(i=0; i<input_num_keys; i++) {
			if(keys[i] != NULL) free(keys[i]);
		}
		free(keys);
	}
	if(jobid_str != NULL)
		free(jobid_str);
	if(segment != NULL)
		free(segment);
		
	return ret;
}

//
///* MONITORING RELATED FUNCTIONS */
//
///* generates an event which returns a list of all the machineIDs know to
// * the universe.
// * EVENT RETURN:
// *   type = GET_MACHINES
// *   data = [2,3,4,5] <--- those are machine IDs
// * 
// * NOTE: we assume there are unlikely to be many machines,
// * so we can return this as a list of id's.
// */
//int
//OMPIGetMachines(char **args)
//{
//	char *	res;
//	char *	str;
//	int		n;
//	int *	machines
//	
//	n = get_machines(&machines);
//	intarray_to_str(n, machines, &res);
//	
//	asprintf(&res, "%d %s", RTEV_MACHINES, str);
//	proxy_svr_event_callback(orte_proxy, res);
//	
//	free(str);
//	free(res);
//
//	return PROXY_RES_OK;
//}
//

int
get_num_nodes(int machid)
{
	int rc;
	size_t cnt;
	orte_gpr_value_t **values;
	
	/* we're going to ignore machine ID until ORTE implements that */
	
	rc = orte_gpr.get(ORTE_GPR_KEYS_OR|ORTE_GPR_TOKENS_OR,
                        ORTE_NODE_SEGMENT, NULL, NULL, &cnt, &values);
                        
	if(rc != ORTE_SUCCESS) {
		return 0;
	}
	
	return cnt;
}

/* given a machine ID this generates an event which has a single int
 * return - the number of nodes associated with this machine
 */
int
ORTEGetNodes(char **args)
{	
	int				mid;
	char *			res;
	int				nodes;
	
	mid = atoi(args[1]);
	nodes = get_num_nodes(mid);

	asprintf(&res, "%d %d", RTEV_NODES, nodes);
	
	proxy_svr_event_callback(orte_proxy, res);
	
	free(res);
	return PROXY_RES_OK;
}

/* given a machineID and a nodeID inside that machine we attempt to find the values associated
 * with the given keys.  the caller can pass any number of possible keys and the response
 * message will be in the same order.
 * 
 * the user can pass in a node ID they want info on or -1 if they want info on EVERY
 * machine contained in this machine
 */
int
ORTEGetNodeAttribute(char **args)
{
	int				machid;
	int				nodeid;
	char *			res;
	char *			str1;
	char *			str2;
	char *			str3;
	int				last_arg;
	int				i;
	char **			keys = NULL;
	char **			values = NULL;
	int *			types = NULL;
	int				tot_len;
	char *			valstr = NULL;
	int				values_len;
	int				default_needed = 0;
	
	machid = atoi(args[1]);
	nodeid = atoi(args[2]);
	
	/* run through the rest of the args, counting the keys */
	i = 3;
	while(args[i] != NULL) i++;
	
	last_arg = i;
	
	keys = (char**)malloc((last_arg - 3)*sizeof(char*));
	values_len = last_arg - 3;
	/* if they want to see ALL the processes then the requested values
	 * have to be multipled by how many processes are in this job */
	if(nodeid == -1) {
		int numnodes = get_num_nodes(machid);
		values_len = values_len * numnodes;
		//printf("values_len = %d\n", values_len); fflush(stdout);
	}
	values = (char**)malloc(values_len * sizeof(char*));
	types = (int*)malloc((last_arg - 3)*sizeof(int));
	
	default_needed = 0;
	
	/* go through the args now, set up the key array */
	for(i=3; i<last_arg; i++) {
		if(!strcmp(args[i], "ATTRIB_NODE_NAME")) {
			asprintf(&(keys[i-3]), "%s", "orte-node-name");
			types[i-3] = PTP_STRING;
			default_needed++;
		} else if(!strcmp(args[i], "ATTRIB_NODE_USER")) {
			asprintf(&(keys[i-3]), "%s", "orte-node-bproc-user");	
			types[i-3] = PTP_STRING;
			default_needed++;
		} else if(!strcmp(args[i], "ATTRIB_NODE_GROUP")) {
			asprintf(&(keys[i-3]), "%s", "orte-node-bproc-group");
			types[i-3] = PTP_STRING;
			default_needed++;
		} else if(!strcmp(args[i], "ATTRIB_NODE_STATE")) {
			asprintf(&(keys[i-3]), "%s", "orte-node-bproc-status");
			types[i-3] = PTP_STRING;
		} else if(!strcmp(args[i], "ATTRIB_NODE_MODE")) {		
			asprintf(&(keys[i-3]), "%s", "orte-node-bproc-mode");
			types[i-3] = PTP_UINT32;
		} else {
			asprintf(&(keys[i-3]), "UNDEFINED");
			types[i-3] = PTP_STRING;
		}
	}
	
	for(i=3; i<last_arg; i++) {
		printf("BEFORE CALL KEYS[%d] = '%s'\n", i-3, keys[i-3]); fflush(stdout);
	}
		
	if(get_node_attribute(machid, nodeid, keys, types, values, last_arg-3)) {
		/* error - so bail out */
		res = ORTEErrorStr(RTEV_ERROR_NATTR, "error finding key on node or error getting keys");
		proxy_svr_event_callback(orte_proxy, res);
		
		return PROXY_RES_OK;
	}
	/* else we're good, use the values */
	
	tot_len = 0;
	for(i=0; i<values_len; i++) {
		printf("AFTER CALL! VALS[%d] = '%s'\n", i, values[i]); fflush(stdout);
		tot_len += strlen(values[i]);
	}
	
	printf("totlen = %d\n", tot_len); fflush(stdout);
	tot_len += values_len * 2; /* add on some for spaces and null, etc - little bit of extra here */
	printf("totlen = %d\n", tot_len); fflush(stdout);
	
	if(tot_len == 0 && default_needed == 3) {
		char hostname[256];
		gid_t gid;
		struct group *grp;
		struct passwd *pwd;
		
		pwd = getpwuid(geteuid());
		gid = getgid();
		grp = getgrgid(gid);
		
		gethostname(hostname, 256);
		printf("Hostname = '%s'\n", hostname); fflush(stdout);
		printf("Username = '%s'\n", pwd->pw_name); fflush(stdout);
		printf("Groupname = '%s'\n", grp->gr_name); fflush(stdout);
		
		proxy_cstring_to_str(hostname, &str1);
		proxy_cstring_to_str(pwd->pw_name, &str2);
		proxy_cstring_to_str(grp->gr_name, &str3);
		asprintf(&valstr, "%s %s %s", str1, str2, str3);
		free(str1);
		free(str2);
		free(str3);
	}
	else {
		for(i=0; i<values_len; i++) {
			proxy_cstring_to_str(values[i], &str2);
			if (i > 0) {
				str1 = valstr;
				asprintf(&valstr, "%s %s", str1, str2);
				free(str1);
				free(str2);
			} else {
				valstr = str2;
			}

		}
	}
	
	printf("valSTR = '%s'\n", valstr); fflush(stdout);
	
	asprintf(&res, "%d %s", RTEV_NATTR, valstr);
	proxy_svr_event_callback(orte_proxy, res);
	free(res);

	for(i=3; i<last_arg; i++) {
		if(keys[i-3] != NULL) free(keys[i-3]);
	}
	
	for(i=0; i<values_len; i++) {
		if(values[i] != NULL) free(values[i]);
	}
	
	free(keys);
	free(values);
	free(types);
	free(valstr);
	
	return PROXY_RES_OK;
}

int
get_node_attribute(int machid, int node_num, char **input_keys, int *input_types, char **input_values, int input_num_keys)
{
	int rc;
	size_t cnt;
	orte_gpr_value_t **values;
	orte_gpr_value_t *value;
	int i, ret, j, min, max;
	
	/* ignore the machine ID until ORTE implements this part */
	
	#if 0
	keys = (char**)malloc((input_num_keys + 1)* sizeof(char*));
	for(i=0; i<input_num_keys; i++) {
		keys[i] = strdup(input_keys[i]);
	}
	/* null terminated */
	keys[input_num_keys] = NULL;
	#endif
	
	rc = orte_gpr.get(ORTE_GPR_KEYS_OR | ORTE_GPR_TOKENS_OR,
			ORTE_NODE_SEGMENT, NULL, NULL, &cnt, &values);
	if(rc != ORTE_SUCCESS) {
		printf("ERROR2: '%s'\n", ORTE_ERROR_NAME(rc));
		ret = 1;
		goto cleanup;
	}
	
	/* specified a proc bigger than any we know of in this job, bail out */
	if(node_num != -1 && node_num >= cnt) {
		ret = 1;
		//printf("BIGGER!  QUIT!\n"); fflush(stdout);
		goto cleanup;
	}
	
	/* they want a full dump */
	if(node_num == -1) {
		min = 0;
		max = cnt;
	}
	/* they were looking for a specific process - quick and dirty */
	else {
		min = node_num;
		max = node_num + 1;
	}
	
	printf("MAX = %d, MIN = %d\n", max, min); fflush(stdout);
		
	for(i=min; i<max; i++) {
		value = values[i];
			
		for(j=0; j<input_num_keys; j++) {
			switch(input_types[j]) {
				case PTP_STRING:
					asprintf(&(input_values[((i-min) * input_num_keys) + j]), "%s", get_str_value(value, input_keys[j]));
					break;
				case PTP_UINT32:
					asprintf(&(input_values[((i-min) * input_num_keys) + j]), "%d", get_ui32_value(value, input_keys[j]));
					break;
			}
			printf("VALUES IN GET FUNC[%d][%d] = '%s'\n", i, j, input_values[((i-min) * input_num_keys) + j]); fflush(stdout);
		}
	}
	
	/* success */
	ret = 0;
	
cleanup:
#if 0
	if(keys != NULL) {
		for(i=0; i<input_num_keys; i++) {
			if(keys[i] != NULL) free(keys[i]);
		}
		free(keys);
	}
	#endif
		
	return ret;
}

///* given a nodeid and an attribute key this generates an event with
// * the attribute's value.  sample attributes might be ATTRIB_NODE_STATE
// * or ATTRIB_NODE_OS which might return up, down, booting, or error and
// * linux, solaris, windows, osx, etc respectively
// * EVENT RETURN:
// *   type = GET_NODE_ATTRIBUTE
// *   data = "key=value" (example "state=down", "user=ndebard")
// */
//int
//OMPIGetNodeAttribute(char **args)
//{
//	int		nodeid = atoi(args[1]);
//	char *	key = args[2];
//	char *	val;
//	
//	val = get_node_attribute(nodeid, key);
//	
//	if (val == NULL) {
//		asprintf(&res, "%d %s", RTEV_ERROR, "no such attribute");
//	} else {
//		asprintf(res, "%d %s=%s", RTEV_NODEATTR, key, val);
//	}
//	
//	proxy_svr_event_callback(orte_proxy, res);
//	
//	free(res);	
//	return PROXY_RES_OK;
//}
//
///* perhaps we don't need this function.
// * this takes a given nodeid and determines the machineID that it is
// * contained within.  in theory this can be inferred by calling getMachines
// * and getNodes(some_machine_id).  once the model is populated, we should
// * have a relationship between the two but this MAY be helpful ... really
// * it depends on whether we want helper functions at this level or up at
// * the Java level - probably in my opinion we want them at the Java level 
// * EVENT RETURN:
// *   type = GET_NODE_MACHINE_ID
// *   data = "nodeid=machineid" (example node 1000 owned by machine 4 -> "1000=4")
// */ 
//int
//OMPIGetNodeMachineID(char **args)
//{
//	int nodeid = atoi(args[1]);
//	
//	return PROXY_RES_OK;
//}

int
ORTEQuit(void)
{
	char *res;
	printf("ORTEQuit called!\n"); fflush(stdout);
	ORTEShutdown();
	asprintf(&res, "%d", RTEV_OK);
	proxy_svr_event_callback(orte_proxy, res);
	free(res);	
	return PROXY_RES_OK;
}

void
server(char *name, char *host, int port)
{
	eventList = NewList();
	debugJobs = NewList();
	
	if (proxy_svr_init(name, &handler_funcs, &helper_funcs, command_tab, &orte_proxy) != PROXY_RES_OK)
		return;
	
	proxy_svr_connect(orte_proxy, host, port);
	printf("proxy_svr_connect returned.\n");
	
	for (;;) {
		if  ((ORTEProgress() != PROXY_RES_OK) ||
			(proxy_svr_progress(orte_proxy) != PROXY_RES_OK))
			break;
		//printf("progress returned.\n");
	}
	
	proxy_svr_finish(orte_proxy);
	printf("proxy_svr_finish returned.\n");
}

int
main(int argc, char *argv[])
{
	int				ch;
	int				port = PROXY_TCP_PORT;
	char *			host = "localhost";
	char *			proxy_str = DEFAULT_PROXY;
	
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
		fprintf(stderr, "orte_server [--proxy=proxy] [--host=host_name] [--port=port]\n");
		return 1;
	}
	
	//if (!ORTEInit()) {
//		fprintf(stderr, "Faild to initialize ORTE\n");
	//	return 1;
	//}
	
	server(proxy_str, host, port);
	
	//ORTEFinalize();
	
	return 0;
}
