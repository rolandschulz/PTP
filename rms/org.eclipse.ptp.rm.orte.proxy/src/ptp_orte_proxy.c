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

#include <sys/types.h>
#include <sys/wait.h>

#include <proxy.h>
#include <proxy_tcp.h>
#include <proxy_event.h>
#include <handler.h>
#include <list.h>
#include <args.h>
#include <signal.h>

#include "opal/util/output.h"
#include "opal/util/path.h"
#include "opal/event/event.h"
#include "opal/threads/condition.h"

#if ORTE_MINOR_VERSION == 0
#include "include/orte_constants.h"
#else /* ORTE_MINOR_VERSION == 0 */
#include "orte/orte_constants.h"
#endif /* ORTE_MINOR_VERSION == 0 */

#include "orte/tools/orted/orted.h"
#if ORTE_MINOR_VERSION != 0
#include "orte/tools/orteconsole/orteconsole.h"
#endif /* ORTE_MINOR_VERSION != 0 */

#include "orte/mca/iof/iof.h"
#include "orte/mca/rmgr/rmgr.h"
#include "orte/mca/errmgr/errmgr.h"
#include "orte/mca/rml/rml.h"
#include "orte/mca/rmgr/base/base.h"
#include "orte/mca/gpr/gpr.h"
#if ORTE_MINOR_VERSION != 0
#include "orte/mca/pls/pls.h"
#endif /* ORTE_MINOR_VERSION != 0 */

#ifdef HAVE_SYS_BPROC_H
#include "orte/mca/soh/bproc/soh_bproc.h"
#endif

#include "orte/runtime/runtime.h"

#define DEFAULT_PROXY		"tcp"
#define DEFAULT_ORTED_ARGS	"orted --scope public --seed --persistent"

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
#define RTEV_ERROR_ORTE_INIT			RTEV_OFFSET + 1000
#define RTEV_ERROR_ORTE_FINALIZE		RTEV_OFFSET + 1001
#define RTEV_ERROR_ORTE_RUN				RTEV_OFFSET + 1002
#define RTEV_ERROR_TERMINATE_JOB		RTEV_OFFSET + 1003
#define RTEV_ERROR_PATTR				RTEV_OFFSET + 1004
#define RTEV_ERROR_PROCS				RTEV_OFFSET + 1005
#define RTEV_ERROR_NODES				RTEV_OFFSET + 1006
#define RTEV_ERROR_NATTR				RTEV_OFFSET + 1007
#define RTEV_ERROR_ORTE_BPROC_SUBSCRIBE	RTEV_OFFSET + 1008
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

#define JOB_STATE_NEW				5000

#define PTP_UINT32				1
#define PTP_STRING				2

int ORTEIsShutdown(void);
int ORTEInit(char *universe_name);

int ORTEStartDaemon(char **);
int ORTERun(char **);
int ORTETerminateJob(char **);
int ORTEGetProcesses(char **);
int ORTEGetProcessAttribute(char **);
int ORTEDiscover(char **);
int ORTEQuit(char **);

struct ptp_job {
	int	debug_jobid; // job ID of debugger or -1 if not a debug job
	int jobid; // job ID that will be used by program when it starts
	int	num_procs; // number of procs requested for program (debugger uses num_procs+1)
};
typedef struct ptp_job ptp_job;

static int get_node_attribute(int machid, int node_num, char **input_keys, int *input_types, char **input_values, int input_num_keys);
static int get_proc_attribute(orte_jobid_t jobid, int proc_num, char **input_keys, int *input_types, char **input_values, int input_num_keys);
static void job_state_callback(orte_jobid_t jobid, orte_proc_state_t state);
#if ORTE_MINOR_VERSION == 0
static int orte_console_send_command(orte_daemon_cmd_flag_t usercmd);
#endif /* ORTE_MINOR_VERSION == 0 */
static void iof_callback(orte_process_name_t* src_name, orte_iof_base_tag_t src_tag, void* cbdata, const unsigned char* data, size_t count);
static void add_job(int jobid, int debug_jobid);
static void remove_job(int jobid);
static void remove_debug_job(int jobid);
static ptp_job *find_job(int jobid);
static char *orte_get_process_attrib(char **);
static int get_num_procs(orte_jobid_t jobid);

#ifdef HAVE_SYS_BPROC_H
int ORTE_Subscribe_Bproc(void);
#else
/*
 * Provide some fake values.
 */
#define ORTE_SOH_BPROC_NODE_USER	"ORTE_SOH_BPROC_NODE_USER"
#define ORTE_SOH_BPROC_NODE_GROUP	"ORTE_SOH_BPROC_NODE_GROUP"
#define ORTE_SOH_BPROC_NODE_STATUS	"ORTE_SOH_BPROC_NODE_STATUS"
#define ORTE_SOH_BPROC_NODE_MODE	"ORTE_SOH_BPROC_NODE_MODE"
#endif

int 		orte_shutdown = 0;
proxy_svr *	orte_proxy;
int			is_orte_initialized = 0;
pid_t		orted_pid = 0;
List *		eventList;
List *		jobList;
int			ptp_signal_exit;
RETSIGTYPE	(*saved_signals[NSIG])(int);

static proxy_handler_funcs handler_funcs = {
	RegisterFileHandler,	// regfile() - call to register a file handler
	UnregisterFileHandler,	// unregfile() - called to unregister file handler
	RegisterEventHandler,	// regeventhandler() - called to register the proxy event handler
	CallEventHandlers
};

static proxy_svr_helper_funcs helper_funcs = {
	NULL,					// newconn() - can be used to reject connections
	NULL					// numservers() - if there are multiple servers, return the number
};

static proxy_svr_commands command_tab[] = {
	{"STARTDAEMON",	ORTEStartDaemon},
	{"DISCOVER",    ORTEDiscover},
	{"RUN",			ORTERun},
	{"TERMJOB",		ORTETerminateJob},
	{"GETPROCS",	ORTEGetProcesses},
	{"GETPATTR",	ORTEGetProcessAttribute},
	{"QUI",			ORTEQuit},
	{NULL,			NULL},
};

static struct option longopts[] = {
	{"proxy",			required_argument,	NULL, 	'P'}, 
	{"port",			required_argument,	NULL, 	'p'}, 
	{"host",			required_argument,	NULL, 	'h'}, 
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
	char *res, *universe_name;
	
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
				char **orted_args;
				
				proxy_svr_finish(orte_proxy);
				
				asprintf(&res, "%s --universe PTP-ORTE-%d", DEFAULT_ORTED_ARGS, getpid());
				//asprintf(&res, "%s", DEFAULT_ORTED_ARGS);				

				orted_args  = Str2Args(res);
				printf("StartDaemon(orted %s)\n", res); fflush(stdout);
				free(res);
				
				/* spawn the daemon */
				printf("CHILD: Starting execvp now!\n"); fflush(stdout);
				errno = 0;

				setsid();
				ret = execvp("orted", orted_args);

				FreeArgs(orted_args);
				
				if (ret != 0) {
					printf("CHILD: error return from execvp, ret = %d, errno = %d\n", ret, errno); fflush(stdout);
					printf("CHILD: PATH = %s\n", getenv("PATH")); fflush(stdout);
					_exit(ret);
				}
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
		printf("Start daemon returning ERROR, orted_pid = %d.\n", orted_pid); fflush(stdout);
		res = ORTEErrorStr(RTEV_ERROR_ORTE_INIT, "initialization failed");
		proxy_svr_event_callback(orte_proxy, res);
		return 0;
	}
	
	asprintf(&universe_name, "PTP-ORTE-%d", orted_pid);
	
	if (ORTEInit(universe_name) != 0) {
		free(universe_name);
		return 0;
	}
	
	free(universe_name);
	
#ifdef HAVE_SYS_BPROC_H
	ORTE_Subscribe_Bproc();
#endif

#if ORTE_MINOR_VERSION == 0
	orte_rmgr.query();
#endif /* ORTE_MINOR_VERSION == 0 */
	
	printf("Start daemon returning OK.\n");
	asprintf(&res, "%d", RTEV_OK);
	proxy_svr_event_callback(orte_proxy, res);
	free(res);
	
	return 0;
}

int
ORTEInit(char *universe_name)
{
	int rc;
	char *str;
		
	printf("ORTEInit (%s)\n", universe_name); fflush(stdout);
	asprintf(&str, "OMPI_MCA_universe=%s", universe_name);
	
	printf("str = '%s'\n", str); fflush(stdout);
	/* this makes the orte_init() fail if the orte daemon isn't
	 * running */
	putenv("OMPI_MCA_orte_univ_exist=1");
	putenv(str);
	/* we cannot free 'str' because it's hooked into the environment, putenv()
	 * man says that any changes to 'str' will automagically change the
	 * environment variable's value - which includes freeing it */
	//free(str);
	
	rc = orte_init(true);
	
	if(ORTECheckErrorCode(RTEV_ERROR_ORTE_INIT, rc)) return 1;
	
	/* this code was given to me to put in here to force the system to populate the node segment
	 * in ORTE.  It basically crashes us if we use our own universe name.  I'm leaving it here
	 * because I think one day we might be able to find a way to use this right. */
#if 0
	{
	orte_ras_base_module_t *module = NULL;
  	orte_jobid_t jobid = 1;
	printf("Calling RDS_base_query() . . .\n"); fflush(stdout);
	orte_rds_base_query();
	printf("Calling RAS_Base_allocate() . . .\n"); fflush(stdout);
	orte_ras_base_allocate(jobid, &module);
	printf("Success with BOTH!\n"); fflush(stdout);
	}
#endif
	
	is_orte_initialized = true;
	
	return 0;
}

/*
 * This callback gets invoked when any attributes of a process get changed.
 * 
 * There appears to be a bug in ORTE that causes it to get called with the 
 * same arguments multiple times. The number of times equals the number of 
 * processes in the job.
 */
static void 
job_proc_notify_callback(orte_gpr_notify_data_t *data, void *cbdata)
{
	int						len;
	size_t					i;
	size_t					j;
	size_t					k;
	orte_gpr_value_t **		values;
	orte_gpr_value_t *		value;
	orte_gpr_keyval_t **	keyvals;
	ptp_job *				job = (ptp_job *)cbdata;
	char *					str1;
	char *					str2;
	char *					res;
	char *					kv = NULL;
	char *					vpid = NULL;
	
	values = (orte_gpr_value_t**)(data->values)->addr;
	
	for(i=0, k=0; k<data->cnt && i < (data->values)->size; i++) {
		if(values[i] == NULL) continue;
		
		k++;
		value = values[i];
		keyvals = value->keyvals;
		
		len = strlen(ORTE_VPID_KEY);
		
		if (strlen(value->tokens[1]) <= len
			|| strncmp(value->tokens[1], ORTE_VPID_KEY, len) != 0)
			continue;
			
		asprintf(&vpid, "%s", value->tokens[1]+len+1);
		
		for(j=0; j<value->cnt; j++) {
			orte_gpr_keyval_t *keyval = keyvals[j];
#if ORTE_MINOR_VERSION == 0
			orte_data_type_t dt = keyval->type;
#else /* ORTE_MINOR_VERSION == 0 */
			orte_data_type_t dt = keyval->value->type;
#endif /* ORTE_MINOR_VERSION == 0 */
			char *external_key = NULL;
			char * tmp_str = NULL;
			int tmp_int;

			if (!strcmp(keyval->key, ORTE_NODE_NAME_KEY))
				asprintf(&external_key, "%s", ATTRIB_PROCESS_NODE_NAME);
			else if (!strcmp(keyval->key, ORTE_PROC_PID_KEY))
				asprintf(&external_key, "%s", ATTRIB_PROCESS_PID);
			else
				external_key = strdup(keyval->key);

			if (external_key != NULL) {					
				switch(dt) {
					case ORTE_STRING:
#if ORTE_MINOR_VERSION == 0
						tmp_str = keyval->value.strptr;
#else /* ORTE_MINOR_VERSION == 0 */
						if( orte_dss.get( (void **) &tmp_str, keyval->value, ORTE_STRING) != ORTE_SUCCESS )
							break;
#endif /* ORTE_MINOR_VERSION == 0 */
						asprintf(&kv, "%s=%s", external_key, tmp_str);
						break;
					case ORTE_UINT32:
#if ORTE_MINOR_VERSION == 0
						tmp_int = keyval->value.ui32;
#else /* ORTE_MINOR_VERSION == 0 */
						if( orte_dss.get( (void **) &tmp_int, keyval->value, ORTE_UINT32) != ORTE_SUCCESS )
							break;
#endif /* ORTE_MINOR_VERSION == 0 */
						asprintf(&kv, "%s=%d", external_key, tmp_int);
						break;
					case ORTE_PID:
#if ORTE_MINOR_VERSION == 0
						tmp_int = keyval->value.pid;
#else /* ORTE_MINOR_VERSION == 0 */
						if( orte_dss.get( (void **) &tmp_int, keyval->value, ORTE_PID) != ORTE_SUCCESS )
							break;
#endif /* ORTE_MINOR_VERSION == 0 */
						asprintf(&kv, "%s=%d", external_key, tmp_int);
						break;
					default:
						asprintf(&kv, "%s=<unknown type>%d", external_key, dt);
						break;
				}
	
				if (kv != NULL) {
					if (job != NULL) {
						proxy_cstring_to_str("", &str1);
						proxy_cstring_to_str(kv, &str2);
						asprintf(&res, "%d %d 0:0 %s 1 %s %s", RTEV_PATTR, job->jobid, str1, vpid, str2);
						
			        	proxy_svr_event_callback(orte_proxy, res);
			        	
			        	free(str1);
			        	free(str2);
			        	free(res);
					}
					free(kv);
					kv = NULL;
				}
				
				free(external_key);
	        }
		}
		
		free(vpid);
	}
}

/*
 * Subscribe to attribute changes for 'procid' in 'job'.
 */
static int
orte_subscribe_proc(ptp_job * job, int procid)
{
	int							i;
	int							rc;
	char *						jobid_str;
	orte_gpr_subscription_t 	sub;
	orte_gpr_subscription_t *	subs;
	orte_gpr_value_t			value;
	orte_gpr_value_t *			values;
	
	rc = orte_ns.convert_jobid_to_string(&jobid_str, job->jobid);
	if(rc != ORTE_SUCCESS) {
		printf("ERROR: '%s'\n", ORTE_ERROR_NAME(rc)); fflush(stdout);
		return -1;
	}
		
	OBJ_CONSTRUCT(&sub, orte_gpr_subscription_t);
	sub.action = ORTE_GPR_NOTIFY_VALUE_CHG;
	
	OBJ_CONSTRUCT(&value, orte_gpr_value_t);
	values = &value;
	sub.values = &values;
	sub.cnt = 1; /* number of values */
	value.addr_mode = ORTE_GPR_TOKENS_XAND | ORTE_GPR_KEYS_OR;
	asprintf(&value.segment, "%s-%s", ORTE_JOB_SEGMENT, jobid_str);
	
	value.cnt = 3; /* number of keyvals */
	value.keyvals = (orte_gpr_keyval_t**)malloc(value.cnt * sizeof(orte_gpr_keyval_t*));
	
	i = 0;
	
	value.keyvals[i] = OBJ_NEW(orte_gpr_keyval_t);
	value.keyvals[i++]->key = strdup(ORTE_NODE_NAME_KEY);

	value.keyvals[i] = OBJ_NEW(orte_gpr_keyval_t);
	value.keyvals[i++]->key = strdup(ORTE_PROC_PID_KEY);
	
	value.keyvals[i] = OBJ_NEW(orte_gpr_keyval_t);
	value.keyvals[i++]->key = strdup(ORTE_PROC_NAME_KEY);

	/* any token */
	value.tokens = NULL;
	value.num_tokens = 0;
	
	sub.cbfunc = job_proc_notify_callback;
	sub.user_tag = (void *)job;
	
	subs = &sub;
	rc = orte_gpr.subscribe(1, &subs, 0, NULL);
	
	if(ORTECheckErrorCode(RTEV_ERROR_ORTE_BPROC_SUBSCRIBE, rc)) return 1;
	
	return 0;
}

/*
 * Subscribe to all processes in a job. 
 * 
 * It would be nice if there was a more efficient way of doing this.
 */
int 
ORTE_Subscribe_Job(orte_jobid_t jobid)
{
	int			i;
	int			num_procs;
	ptp_job *	job;
	
	num_procs = get_num_procs(jobid);
	
	if (num_procs == 0) {
		printf("no processes for job\n"); fflush(stdout);
		return -1;
	}
	
	printf("subscribing %d procs\n", num_procs); fflush(stdout);
	
	for (i = 0; i < num_procs; i++) {
		job = find_job(jobid);
		if (job != NULL)
			orte_subscribe_proc(job, i);
	}
		
	return 0;
}

#ifdef HAVE_SYS_BPROC_H
/*
 * This callback gets invoked when any of the bproc specific state of health attributes
 * change. These are all node-related events, so this is really the monitoring
 * functions for bproc support.
 */
static void 
bproc_notify_callback(orte_gpr_notify_data_t *data, void *cbdata)
{
	size_t i, j, k;
	orte_gpr_value_t **values, *value;
	orte_gpr_keyval_t **keyvals;
	char *res, *kv, *str1, *str2, *str3, *foo, *bar, *nodename;
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
			char *external_key;
			
			printf("--- BPROC CHANGE: key = %s\n", keyval->key); fflush(stdout);
			
			if(!strcmp(keyval->key, ORTE_NODE_NAME_KEY))
				asprintf(&external_key, "%s", ATTRIB_NODE_NAME);
			else if(!strcmp(keyval->key, ORTE_SOH_BPROC_NODE_USER))
				asprintf(&external_key, "%s", ATTRIB_NODE_USER);
			else if(!strcmp(keyval->key, ORTE_SOH_BPROC_NODE_GROUP))
				asprintf(&external_key, "%s", ATTRIB_NODE_GROUP);
			else if(!strcmp(keyval->key, ORTE_SOH_BPROC_NODE_STATUS) || !strcmp(keyval->key, ORTE_NODE_STATE_KEY))
				asprintf(&external_key, "%s", ATTRIB_NODE_STATE);
			else if(!strcmp(keyval->key, ORTE_SOH_BPROC_NODE_MODE))
				asprintf(&external_key, "%s", ATTRIB_NODE_MODE);
			else
				printf("******************* Unknown key type on bproc event - key = '%s'\n", keyval->key); fflush(stdout);
					
			switch(keyval->type) {
				case ORTE_NODE_STATE:
					printf("--- BPROC CHANGE: (state) val = %d\n", keyval->value.node_state); fflush(stdout);
					asprintf(&kv, "%s=%d", external_key, keyval->value.node_state);
					break;
				case ORTE_STRING:
					printf("--- BPROC CHANGE: (str) val = %s\n", keyval->value.strptr); fflush(stdout);
					asprintf(&kv, "%s=%s", external_key, keyval->value.strptr);
					break;
				case ORTE_UINT32:
					printf("--- BPROC CHANGE: (uint32) val = %d\n", keyval->value.ui32); fflush(stdout);
					asprintf(&kv, "%s=%d", external_key, keyval->value.ui32);
					break;
				default:
					printf("--- BPROC CHANGE: unknown type %d\n", keyval->type); fflush(stdout);
					asprintf(&kv, "%s=%d", external_key, keyval->type);
			}
			
			
			asprintf(&foo, "%s=%d", ATTRIB_MACHIINEID, 0);
			asprintf(&bar, "%s=%s", ATTRIB_NODE_NUMBER, nodename); /* for bproc the node number is the same as the name */
			asprintf(&bar, "%s=%s", ATTRIB_NODE_NAME, nodename);
			proxy_cstring_to_str(foo, &str1);
			proxy_cstring_to_str(bar, &str2);
			proxy_cstring_to_str(kv, &str3);
			asprintf(&res, "%d %s %s %s", RTEV_NATTR, str1, str2, str3);
			
        	proxy_svr_event_callback(orte_proxy, res);
        	free(res);
        	free(kv);
			free(external_key);
        	free(foo);
        	free(bar);
        	free(str1);
        	free(str2);
        	free(str3);
        }
		
		free(nodename);
	}
}

/*
 * Subscribe to the bproc specific attribute changes.
 */
int 
ORTE_Subscribe_Bproc(void)
{
	int i, rc;
	orte_gpr_subscription_t sub, *subs;
	orte_gpr_value_t value, *values;
	
	OBJ_CONSTRUCT(&sub, orte_gpr_subscription_t);
	sub.action = ORTE_GPR_NOTIFY_VALUE_CHG;
	
	OBJ_CONSTRUCT(&value, orte_gpr_value_t);
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

/* 
 *  finalize the registry 
 */
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

/* 
 * tell the daemon to exit 
 */
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

/* 
 * Check for events and call appropriate progress hooks.
 */
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

/* 
 * terminate a job, given a jobid 
 */
int
ORTETerminateJob(char **args)
{
	int			rc;
	int			jobid = atoi(args[1]);
	ptp_job *	j;
	
#if ORTE_MINOR_VERSION == 0
	rc = orte_rmgr.terminate_job(jobid);
#else /* ORTE_MINOR_VERSION == 0 */
	rc = orte_pls.terminate_job(jobid);
#endif /* ORTE_MINOR_VERSION == 0 */
	if(ORTECheckErrorCode(RTEV_ERROR_TERMINATE_JOB, rc)) return 1;
	
	if ((j = find_job(jobid)) != NULL) {
		if (j->debug_jobid < 0)
#if ORTE_MINOR_VERSION == 0
			rc = orte_rmgr.terminate_job(j->jobid);
#else /* ORTE_MINOR_VERSION == 0 */
			rc = orte_pls.terminate_job(jobid);
#endif /* ORTE_MINOR_VERSION == 0 */
		else
#if ORTE_MINOR_VERSION == 0
			rc = orte_rmgr.terminate_job(j->debug_jobid);
#else /* ORTE_MINOR_VERSION == 0 */
			rc = orte_pls.terminate_job(jobid);
#endif /* ORTE_MINOR_VERSION == 0 */
		
		if(ORTECheckErrorCode(RTEV_ERROR_TERMINATE_JOB, rc)) return 1;
	}
	
	return PROXY_RES_OK;
}

static void
add_job(int jobid, int debug_jobid)
{
	ptp_job *	j = (ptp_job *)malloc(sizeof(ptp_job));
    j->jobid = jobid;
    j->debug_jobid = debug_jobid;
    AddToList(jobList, (void *)j);
}

static void
remove_job(int jobid)
{
	ptp_job *	j;

	for (SetList(jobList); (j = (ptp_job *)GetListElement(jobList)) != NULL; ) {
		if (j->jobid == jobid) {
			RemoveFromList(jobList, (void *)j);
			break;
		}
	}
}

static void
remove_debug_job(int jobid)
{
	ptp_job *	j;
 
 	for (SetList(jobList); (j = (ptp_job *)GetListElement(jobList)) != NULL; ) {
		if (j->debug_jobid == jobid) {
			RemoveFromList(jobList, (void *)j);
			break;
		}
	}
}

static ptp_job *
find_job(int jobid)
{
	ptp_job *	j;
	
	for (SetList(jobList); (j = (ptp_job *)GetListElement(jobList)) != NULL; ) {
		if (j->jobid == jobid) {
			return j;
		}
	}
	return NULL;
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
           	remove_debug_job(jobid);
    			break;
	}
}

static void 
debug_wireup_stdin(orte_jobid_t jobid)
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

static void 
debug_callback(orte_gpr_notify_data_t *data, void *cbdata)
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

static int
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
/*
 * Debug spawner. To spawn a debug job, two process allocations must be made. 
 * This first is for the application and the second for the debugger (which is
 * an MPI program). We then launch the debugger, and let it deal with starting
 * the application processes.
 * 
 * This will need to be modified to support attaching.
 */
static int
debug_spawn(char *debug_path, int argc, char **argv, orte_app_context_t** app_context, size_t num_context, orte_jobid_t* app_jobid, orte_jobid_t* debug_jobid)
{
	int						i;
	int						rc;
	orte_jobid_t			jid1;
	orte_jobid_t			jid2;
	orte_app_context_t **	debug_context;

	if ((rc = debug_allocate(app_context, num_context, &jid1, debug_app_job_state_callback)) != ORTE_SUCCESS)
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
	asprintf(&debug_context[0]->argv[i++], "--jobid=%d", jid1);
	debug_context[0]->argv[i++] = NULL;
	debug_context[0]->argc = i;

	if ((rc = debug_allocate(debug_context, num_context, &jid2, debug_job_state_callback)) != ORTE_SUCCESS) {
		// TODO free debug_context...
		return rc;
	}

	/*
	 * launch the debugger
	 */
	if (ORTE_SUCCESS != (rc = orte_rmgr.launch(jid2))) {
		ORTE_ERROR_LOG(rc);
		return rc;
	}
    	
	*app_jobid = jid1;
	*debug_jobid = jid2;
    
	return ORTE_SUCCESS;
}

/* spawn a job with the given executable path and # of procs. */
int
ORTERun(char **args)
{
	int						rc;
	int						i;
	int						a;
	int						num_procs = 0;
	int						debug = 0;
	int						num_apps;
	int						num_args = 0;
	int						num_env = 0;
	int						debug_argc = 0;
	char *					res;
	char *					full_path;
	char	 *				pgm_name = NULL;
	char	 *				cwd = NULL;
	char *					exec_path = NULL;
	char *					debug_exec_path;
	char **					debug_args;
	char **					env = NULL;
	orte_app_context_t **	apps;
	orte_jobid_t			jobid = ORTE_JOBID_MAX;
	orte_jobid_t			debug_jobid = -1;

	for (i = 1; args[i] != NULL; i += 2) {
		if (strcmp(args[i], "execName") == 0) {
			pgm_name = args[i+1];
		} else if (strcmp(args[i], "pathToExec") == 0) {
			exec_path = args[i+1];
		} else if (strcmp(args[i], "numOfProcs") == 0) {
			num_procs = atoi(args[i+1]);
		} else if (strcmp(args[i], "procsPerNode") == 0) {
			// not yet
		} else if (strcmp(args[i], "firstNodeNum") == 0) {
			// not yet
		} else if (strcmp(args[i], "workingDir") == 0) {
			cwd = args[i+1];
		} else if (strcmp(args[i], "progArg") == 0) {
			num_args++;
		} else if (strcmp(args[i], "progEnv") == 0) {
			num_env++;
		} else if (strcmp(args[i], "debuggerPath") == 0) {
			debug_exec_path = args[i+1];
			debug = 1;
		} else if (strcmp(args[i], "debuggerArg") == 0) {
			debug_argc++;
		}
	}
	
	/*
	 * Do some checking first
	 */
	 
	if (pgm_name == NULL) {
		proxy_svr_event_callback(orte_proxy, ORTEErrorStr(RTEV_ERROR_ORTE_RUN, "Must specify a program name"));
		return PROXY_RES_OK;
	}
	
	if (num_procs <= 0) {
		proxy_svr_event_callback(orte_proxy, ORTEErrorStr(RTEV_ERROR_ORTE_RUN, "Invalid number of processes"));
		return PROXY_RES_OK;
	}
	
	/*
	 * Must specify a working directory. For local launches, this is normally the project directory. For
	 * remote launches, it will probably be the user's home directory.
	 */
	if (cwd == NULL) {
		proxy_svr_event_callback(orte_proxy, ORTEErrorStr(RTEV_ERROR_ORTE_RUN, "Must specify a working directory"));
		return PROXY_RES_OK;
	}
		
	/*
	 * Get supplied environment. It is used to locate executable if necessary.
	 */
	
	if (num_env > 0) {
		env = (char **)malloc((num_env + 1) * sizeof(char *));
		for (a = 0, i = 1; args[i] != NULL; i += 2) {
			if (strcmp(args[i], "progEnv") == 0)
				env[a++] = strdup(args[i+1]);
		}
		env[a] = NULL;
	}
		
	/*
	 * If no path is specified, then try to locate execuable.
	 */		
	if (exec_path == NULL) {
		full_path = opal_path_findv(pgm_name, 0, env, cwd);
		if (full_path == NULL) {
			proxy_svr_event_callback(orte_proxy, ORTEErrorStr(RTEV_ERROR_ORTE_RUN, "Executuable not found"));
			return PROXY_RES_OK;
		}
	} else {
		asprintf(&full_path, "%s/%s", exec_path, pgm_name);
	}
	
	if (access(full_path, X_OK) < 0) {
		proxy_svr_event_callback(orte_proxy, ORTEErrorStr(RTEV_ERROR_ORTE_RUN, strerror(errno)));
		return PROXY_RES_OK;
	}
	
	if (debug) {		
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

	/* hard coded test for spawning just 1 job (JOB not PROCESSES!) */
	num_apps = 1;

	/* format the app_context_t struct */
	apps = malloc(sizeof(orte_app_context_t *) * num_apps);
	apps[0] = OBJ_NEW(orte_app_context_t);
	apps[0]->num_procs = num_procs;
	apps[0]->app = full_path;
	apps[0]->cwd = strdup(cwd);
	/* no special environment variables */
	apps[0]->num_env = num_env;
	apps[0]->env = env;
	/* no special mapping of processes to nodes */
	apps[0]->num_map = 0;
	apps[0]->map_data = NULL;
	/* setup argv */
	apps[0]->argv = (char **)malloc((num_args + 2) * sizeof(char *));
	apps[0]->argv[0] = strdup(pgm_name);
	if (num_args > 0) {
		for (a = 1, i = 1; args[i] != NULL; i += 2) {
			if (strcmp(args[i], "progArg") == 0)
				apps[0]->argv[a++] = strdup(args[i+1]);
		}
	}
	apps[0]->argv[num_args+1] = NULL;
	apps[0]->argc = num_args + 1;
	
	printf("(debug ? %d) Spawning %d processes of job '%s'\n", debug, (int)apps[0]->num_procs, apps[0]->app);
	printf("\tprogram name '%s'\n", apps[0]->argv[0]);
	fflush(stdout);
	
	/* calls the ORTE spawn function with the app to spawn.  Return the
	 * jobid assigned by the registry/ORTE.  Passes a callback function
	 * that ORTE will call with state change on this job */
	if (!debug)
		rc = orte_rmgr.spawn(apps, num_apps, &jobid, job_state_callback);
	else
		rc = debug_spawn(debug_exec_path, debug_argc, debug_args, apps, num_apps, &jobid, &debug_jobid);

	printf("SPAWNED [error code %d = '%s'], now unlocking\n", rc, ORTE_ERROR_NAME(rc)); fflush(stdout);
	
	if(ORTECheckErrorCode(RTEV_ERROR_ORTE_RUN, rc)) return 1;

	printf("NEW JOBID = %d\n", (int)jobid); fflush(stdout);
	
    add_job(jobid, debug_jobid);
	
	asprintf(&res, "%d %d", RTEV_NEWJOB, (int)jobid);
	printf("res = '%s'\n", res); fflush(stdout);
	proxy_svr_event_callback(orte_proxy, res);

	if(res) free(res);
	
	if(debug) free(debug_args);
	
//	/* generate an event stating what the new/assigned job ID is.
//	 * The caller must record this and use this as an identifier to get
//	 * information about a job */
//	//GenerateEvent(ORTE_RUN, jobid);
	
	printf("Returning from ORTERun\n"); fflush(stdout);
	
	ORTE_Subscribe_Job(jobid);
	
	return PROXY_RES_OK;
}

/*
 * This callback is invoked when there is I/O available from a
 * process. It gets forwarded to Eclipse.
 * 
 * This needs to be modified to avoid flooding Eclipse with
 * events.
 */
static void 
iof_callback(
    orte_process_name_t* src_name,
    orte_iof_base_tag_t src_tag,
    void* cbdata,
    const unsigned char* data,
    size_t count)
{
	char *res, *str;
	char *line;
	
    if(count > 0) {
        line = (char *)malloc(count+1);
        strncpy((char*)line, (char*)data, count);
        if(line[count-1] == '\n') line[count-1] = '\0';
        line[count] = '\0';
        proxy_cstring_to_str(line, &str);
        asprintf(&res, "%d %d %d %s", RTEV_PROCOUT, (int)src_name->jobid, (int)src_name->vpid, str);
        proxy_svr_event_callback(orte_proxy, res);
        free(res);
        free(str);
        free(line);
    }
}

struct orte_job_record {
	struct orte_job_record *next;
	orte_jobid_t jobid;
};

struct orte_job_record *head = NULL;

static int 
orte_job_record_started(orte_jobid_t jobid)
{
	struct orte_job_record *cur = NULL;
	if(head == NULL) return 0;
	
	cur = head;
	
	while(cur != NULL) {
		if(cur->jobid == jobid) return 1;
		cur = cur->next;
	}
	
	return 0; /* guess we didn't find it and we're at the end */
}

static void
orte_job_record_start(orte_jobid_t jobid)
{
	struct orte_job_record *cur = NULL;
	struct orte_job_record *tmp = NULL;
	int	rc;
	orte_process_name_t *	name;
	
	/* if this is the first, we need to set it up */
	if(head == NULL) {
		head = (struct orte_job_record *)malloc(sizeof(struct orte_job_record));
		head->next = NULL;
	}
	cur = head;
	/* run to the end */
	while(cur->next != NULL) cur = cur->next;
	/* now we know 'cur' is the tail */
	tmp = (struct orte_job_record*)malloc(sizeof(struct orte_job_record));
	cur->next = tmp;
	tmp->next = NULL;
	tmp->jobid = jobid;
	
	/* register the IO forwarding callback */
	if (ORTE_SUCCESS != (rc = orte_ns.create_process_name(&name, 0, jobid, 0))) {
		ORTE_ERROR_LOG(rc);
        	return;
    	}
    	
    	printf("registering IO forwarding - name = '%s'\n", (char *)name); fflush(stdout);
            	
	if (ORTE_SUCCESS != (rc = orte_iof.iof_subscribe(name, ORTE_NS_CMP_JOBID, ORTE_IOF_STDOUT, iof_callback, NULL))) {                
		opal_output(0, "[%s:%d] orte_iof.iof_subscribed failed\n", __FILE__, __LINE__);
    	}
    	if (ORTE_SUCCESS != (rc = orte_iof.iof_subscribe(name, ORTE_NS_CMP_JOBID, ORTE_IOF_STDERR, iof_callback, NULL))) {                
        	opal_output(0, "[%s:%d] orte_iof.iof_subscribed failed\n", __FILE__, __LINE__);
    	}
}

static void
orte_job_record_end(orte_jobid_t jobid)
{
	struct orte_job_record *cur = NULL;
	struct orte_job_record *prev = NULL;
	int	rc;
	orte_process_name_t *	name;
	if(head == NULL) return; /* umm, we have nothing */
	
	cur = head;
	while(cur != NULL) {
		if(cur->jobid == jobid) {
			/* ok we found it, we need to move the pointer behind us */
			prev->next = cur->next; /* parent's next is our next */
			/* now free cur */
			free(cur);
			
			if (ORTE_SUCCESS != (rc = orte_ns.create_process_name(&name, 0, jobid, 0))) {
             	ORTE_ERROR_LOG(rc);
                	return;
            	}
            	printf("unregistering IO forwarding - name = %s\n", (char *)name); fflush(stdout);
            	if (ORTE_SUCCESS != (rc = orte_iof.iof_unsubscribe(name, ORTE_NS_CMP_JOBID, ORTE_IOF_STDOUT))) {                
				opal_output(0, "[%s:%d] orte_iof.iof_unsubscribed failed\n", __FILE__, __LINE__);
			}
			if (ORTE_SUCCESS != (rc = orte_iof.iof_unsubscribe(name, ORTE_NS_CMP_JOBID, ORTE_IOF_STDERR))) {                
				opal_output(0, "[%s:%d] orte_iof.iof_unsubscribed failed\n", __FILE__, __LINE__);
			}
			
			return;
		}
		prev = cur;
		cur = cur->next;
	}
}

/*
 * This callback is invoked when there is a job state change. We are mainly
 * interested in when a job is running and when it terminates so the process
 * icons can be updated appropriately.
 */
static void
job_state_callback(orte_jobid_t jobid, orte_proc_state_t state)
{
	char *				res;
	
	printf("JOB STATE CALLBACK!\n"); fflush(stdout);
		
	/* not sure yet how we want to handle this callback, what events
	 * we want to generate, but here are the states that I know of
	 * that a job can go through.  I've watched ORTE call this callback
	 * with each of these states.  We'll want to come in here and
	 * generate events where appropriate */
	
	//printf("job_state_callback(%d)\n", jobid); fflush(stdout);
	switch(state) {
		case ORTE_PROC_STATE_INIT:
			printf("    state = ORTE_PROC_STATE_INIT\n"); fflush(stdout);
			if(!orte_job_record_started(jobid)) {
				orte_job_record_start(jobid);
			}
			break;
		case ORTE_PROC_STATE_LAUNCHED:
			printf("    state = ORTE_PROC_STATE_LAUNCHED\n"); fflush(stdout);
			if(!orte_job_record_started(jobid)) {
				orte_job_record_start(jobid);
			}
			break;
		case ORTE_PROC_STATE_AT_STG1:
			printf("    state = ORTE_PROC_STATE_AT_STG1\n"); fflush(stdout);
			if(!orte_job_record_started(jobid)) {
				orte_job_record_start(jobid);
			}
			break;
		case ORTE_PROC_STATE_AT_STG2:
			printf("    state = ORTE_PROC_STATE_AT_STG2\n"); fflush(stdout);
			if(!orte_job_record_started(jobid)) {
				orte_job_record_start(jobid);
			}
			break;
		case ORTE_PROC_STATE_RUNNING:
			printf("    state = ORTE_PROC_STATE_RUNNING\n"); fflush(stdout);
			if(!orte_job_record_started(jobid)) {
				orte_job_record_start(jobid);
			}
			break;
		case ORTE_PROC_STATE_AT_STG3:
			printf("    state = ORTE_PROC_STATE_AT_STG3\n"); fflush(stdout);
			break;
		case ORTE_PROC_STATE_FINALIZED:
			printf("    state = ORTE_PROC_STATE_FINALIZED\n"); fflush(stdout);
			break;
		case ORTE_PROC_STATE_TERMINATED:
			printf("    state = ORTE_PROC_STATE_TERMINATED\n"); fflush(stdout);
			if(orte_job_record_started(jobid)) {
				orte_job_record_end(jobid);
			}
			remove_job(jobid);
			break;
		case ORTE_PROC_STATE_ABORTED:
			printf("    state = ORTE_PROC_STATE_ABORTED\n"); fflush(stdout);
			remove_job(jobid);
			break;
	}
	
	asprintf(&res, "%d %d %d", RTEV_JOBSTATE, jobid, state);
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

#if ORTE_MINOR_VERSION == 0
/*
 * Send a command to the ORTE daemon.
 * 
 * Currently only used to request the daemon to exit.
 */
static int 
orte_console_send_command(orte_daemon_cmd_flag_t usercmd)
{
    orte_buffer_t *cmd;
    orte_daemon_cmd_flag_t command;
    orte_process_name_t    seed = {0,0,0};
    int rc;

    cmd = OBJ_NEW(orte_buffer_t);
    if (NULL == cmd) {
        ORTE_ERROR_LOG(ORTE_ERROR);
        return ORTE_ERROR;
    }

    command = usercmd;
	
    rc = orte_dps.pack(cmd, &command, 1, ORTE_DAEMON_CMD);
    if ( ORTE_SUCCESS != rc ) {
        ORTE_ERROR_LOG(rc);
        OBJ_RELEASE(cmd);
        return rc;
    } 

    rc = orte_rml.send_buffer(&seed, cmd, ORTE_RML_TAG_DAEMON, 0);
    if ( 0 > rc ) {
        ORTE_ERROR_LOG(ORTE_ERR_COMM_FAILURE);
        OBJ_RELEASE(cmd);
        return ORTE_ERR_COMM_FAILURE;
    }

    OBJ_RELEASE(cmd);

    return ORTE_SUCCESS;
}
#endif /* ORTE_MINOR_VERSION == 0 */

/*
 * Find the number of processes started for a particular job.
 */
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

/* 
 * given a jobid (valid from OMPIGetJobs()) we request the number of processes
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

/* 
 * given a jobID and a processID inside that job we attempt to find the values associated
 * with the given keys.  the caller can pass any number of possible keys and the response
 * message will be in the same order.
 * 
 * the user can pass in a process ID they want info on or -1 if they want info on EVERY
 * process contained in this job
 */
static char *
orte_get_process_attrib(char **args)
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
		if(!strcmp(args[i], ATTRIB_PROCESS_PID)) {
			asprintf(&(keys[i-3]), "%s", ORTE_PROC_PID_KEY);
			types[i-3] = PTP_UINT32;
		} else if(!strcmp(args[i], ATTRIB_PROCESS_NODE_NAME)) {
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
		
		return NULL;
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
	
	return res;
}

int
ORTEGetProcessAttribute(char **args)
{
	char *	res;
	
	res = orte_get_process_attrib(args);
	
	proxy_svr_event_callback(orte_proxy, res);
	
	free(res);

	return PROXY_RES_OK;
}

/*
** Very inefficient! Probably better to build a data structure
** from keyvals, then work with that.
*/
static int 
get_ui32_value(orte_gpr_value_t *value, char *key)
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
static char *
get_str_value(orte_gpr_value_t *value, char *key)
{
        int k;

        for(k=0; k<value->cnt; k++) {
                orte_gpr_keyval_t* keyval = value->keyvals[k];
                if (strcmp(key, keyval->key) == 0)
                        return keyval->value.strptr;
        }

        return "";
}

static int
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

static int
get_num_nodes(int machid)
{
	int rc;
	size_t cnt;
	orte_gpr_value_t **values;
	
	/* we're going to ignore machine ID until ORTE implements that */
	
	rc = orte_gpr.get(ORTE_GPR_KEYS_OR|ORTE_GPR_TOKENS_OR,
                        ORTE_NODE_SEGMENT, NULL, NULL, &cnt, &values);
        
    //printf("RC = %d\n", rc); fflush(stdout);
                   
	if(rc != ORTE_SUCCESS) {
		return 0;
	}
	
	return cnt;
}

/* 
 * one day ORTE will have the notion of number of machines.
 * until then, we have only 1 machine
 */
static int 
get_num_machines()
{
	return 1;
}

/*
 * Initiates the discovery phase.
 * This fires up some talks with ORTE to ask it about the machines
 * it knows about, the nodes, and the attributes associated with
 * those nodes.
 * 
 * even though there CAN be args, we don't use them at this time
 */
int
ORTEDiscover(char **args)
{	
	int 			num_machines;
	int				machid;
	int				nodeid;
	char *			res;
	int				num_nodes;
	int				num_keys;
	int				i;
	char **			internal_keys = NULL;
	char **			external_keys = NULL;
	char **			values = NULL;
	int *			types = NULL;
	int				tot_len;
	char *			valstr = NULL;
	int				values_len;
	char *			str1;
	char *			str2;
	char *			str3;
	char *			str4;
	char *			str5;
	char *			str6;
	char *			str7;
	
	/* first set up the keys we're interested in about the nodes we'll
	 * discover */
	 
	/* how many keys do we send back? */
	num_keys = 7;
	internal_keys = (char**)malloc(num_keys*sizeof(char*));
	external_keys = (char**)malloc(num_keys*sizeof(char*));
	types = (int*)malloc((num_keys)*sizeof(int));
	 
	asprintf(&(internal_keys[0]), "%s", "machine_id");
	asprintf(&(external_keys[0]), "%s", ATTRIB_MACHINEID);
	types[0] = PTP_UINT32;
	asprintf(&(internal_keys[1]), "%s", "node_number");
	asprintf(&(external_keys[1]), "%s", ATTRIB_NODE_NUMBER);
	types[1] = PTP_UINT32;
	/* set up the keys we know about in ORTE and BPROC */
	asprintf(&(internal_keys[2]), "%s", ORTE_NODE_NAME_KEY);
	asprintf(&(external_keys[2]), "%s", ATTRIB_NODE_NAME);
	types[2] = PTP_STRING;
	asprintf(&(internal_keys[3]), "%s", ORTE_SOH_BPROC_NODE_USER);
	asprintf(&(external_keys[3]), "%s", ATTRIB_NODE_USER);
	types[3] = PTP_STRING;
	asprintf(&(internal_keys[4]), "%s", ORTE_SOH_BPROC_NODE_GROUP);
	asprintf(&(external_keys[4]), "%s", ATTRIB_NODE_GROUP);
	types[4] = PTP_STRING;
	asprintf(&(internal_keys[5]), "%s", ORTE_SOH_BPROC_NODE_STATUS);
	asprintf(&(external_keys[5]), "%s", ATTRIB_NODE_STATE);
	types[5] = PTP_STRING;
	asprintf(&(internal_keys[6]), "%s", ORTE_SOH_BPROC_NODE_MODE);
	asprintf(&(external_keys[6]), "%s", ATTRIB_NODE_MODE);
	types[6] = PTP_UINT32;

	num_machines = get_num_machines();
	
	for(machid = 0; machid < num_machines; machid++) {
		num_nodes = get_num_nodes(machid);
		
		values_len = num_keys * num_nodes;
		values = (char**)malloc(values_len * sizeof(char*));

		/* 
		 * if we know of no nodes, then we better just try something out I guess for this
		 * node.  this is a huge hack :( 
		 */
		if(num_nodes == 0) {
			char hostname[256];
			gid_t gid;
			struct group *grp;
			struct passwd *pwd;
        	char * status = "up";
        	char * mode = "73";
        	char * tmpstr;
		
			pwd = getpwuid(geteuid());
			gid = getgid();
			grp = getgrgid(gid);
		
			gethostname(hostname, 256);
			printf("Hostname = '%s'\n", hostname); fflush(stdout);
			printf("Username = '%s'\n", pwd->pw_name); fflush(stdout);
			printf("Groupname = '%s'\n", grp->gr_name); fflush(stdout);
		
			/* 0 = machine 0 */
			asprintf(&tmpstr, "%s=%d", external_keys[0], 0);
			proxy_cstring_to_str(tmpstr, &str1);
        	free(tmpstr);
        	/* 0 = node 0 */
			asprintf(&tmpstr, "%s=%d", external_keys[1], 0);
			proxy_cstring_to_str(tmpstr, &str2);
        	free(tmpstr);
			asprintf(&tmpstr, "%s=%s", external_keys[2], hostname);
        	proxy_cstring_to_str(tmpstr, &str3);
        	free(tmpstr);
        	asprintf(&tmpstr, "%s=%s", external_keys[3], pwd->pw_name);
        	proxy_cstring_to_str(tmpstr, &str4);
        	free(tmpstr);
        	asprintf(&tmpstr, "%s=%s", external_keys[4], grp->gr_name);
        	proxy_cstring_to_str(tmpstr, &str5);
        	free(tmpstr);
        	asprintf(&tmpstr, "%s=%s", external_keys[5], status);
        	proxy_cstring_to_str(tmpstr, &str6);
        	free(tmpstr);
        	asprintf(&tmpstr, "%s=%s", external_keys[6], mode);
        	proxy_cstring_to_str(tmpstr, &str7);
        	free(tmpstr);
       
        	asprintf(&valstr, "%s %s %s %s %s %s %s", str1, str2, str3, str4, str5, str6, str7);
        	
        	free(str1);
        	free(str2);
        	free(str3);
        	free(str4);
        	free(str5); 
        	free(str6);
        	free(str7);
		} else if (values_len != 0) {
			char *tmpstr;
			int key_num = 0;
		
			/* nodeid = -1 means we want the attributes for ALL nodes */
			nodeid = -1;
	
			if(get_node_attribute(machid, nodeid, internal_keys, types, values, num_keys)) {
				/* error - so bail out */
				res = ORTEErrorStr(RTEV_ERROR_NATTR, "error finding key on node or error getting keys");
				proxy_svr_event_callback(orte_proxy, res);
		
				return PROXY_RES_OK;
			}
			/* else we're good, use the values */
	
			tot_len = 0;
			for(i=0; i<values_len; i++) {
				tot_len += strlen(values[i]);
			}
	
			tot_len += values_len * 2; /* add on some for spaces and null, etc - little bit of extra here */
		
			asprintf(&valstr, "%s", "");
			for(i=0; i<values_len; i++) {
				if(i == 0) {
					asprintf(&tmpstr, "%s=%s", external_keys[key_num], values[i]);
					proxy_cstring_to_str(tmpstr, &valstr);
				}
				else {
					str1 = valstr;
					asprintf(&tmpstr, "%s=%s", external_keys[key_num], values[i]);
					proxy_cstring_to_str(tmpstr, &str2);
					asprintf(&valstr, "%s %s", str1, str2);
					free(str1);
					free(str2);
					free(tmpstr);
				}
				key_num++;
				if(key_num >= num_keys) key_num = 0;
			}	
		} else {
			/* error - so bail out */
			res = ORTEErrorStr(RTEV_ERROR_NATTR, "error finding key on node or error getting keys");
			proxy_svr_event_callback(orte_proxy, res);
		
			return PROXY_RES_OK;
		}
	
		asprintf(&res, "%d %s", RTEV_NATTR, valstr);
		proxy_svr_event_callback(orte_proxy, res);
		free(res);
		
		for(i=0; i<num_keys; i++) {
			if(internal_keys[i] != NULL) free(internal_keys[i]);
			if(external_keys[i] != NULL) free(external_keys[i]);
		}
	
		for(i=0; i<values_len; i++) {
			if(values[i] != NULL) free(values[i]);
		}
		
		free(values);
		free(valstr);
	}
	
	printf("DISCOVERY PHASE: end\n"); fflush(stdout);

	free(internal_keys);
	free(external_keys);
	free(types);
	
	return PROXY_RES_OK;
}

static int
get_node_attribute(int machid, int node_num, char **input_keys, int *input_types, char **input_values, int input_num_keys)
{
	int rc;
	size_t cnt;
	orte_gpr_value_t **values;
	orte_gpr_value_t *value;
	int i, ret, j, min, max;
#ifndef HAVE_SYS_BPROC_H
	gid_t gid;
	struct group *grp;
	struct passwd *pwd;
	char * status = "up";
	char * mode = "73";

	pwd = getpwuid(geteuid());
	gid = getgid();
	grp = getgrgid(gid);
#endif

	
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
	cnt = get_num_nodes(machid);
	
	
	if(cnt <= 0) {
		ret = 1;
		goto cleanup;
	}
	
	
	/* specified a proc bigger than any we know of in this job, bail out */
	if(node_num != -1 && node_num >= cnt) {
		ret = 1;
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
	
	for(i=min; i<max; i++) {
		value = values[i];
			
		for(j=0; j<input_num_keys; j++) {
			/* Open MPI does not have a notion of a machine ID or node number (though it does have node name), at least
			 * at this time, at least as far as I know.  Therefore, we push these in here by hand.
			 * It sucks, so figure out the right keys to search for if you know how to do it right */
			if (!strcmp(input_keys[j], "machine_id")) {
				asprintf(&(input_values[((i-min) * input_num_keys) + j]), "0");
			} else if (!strcmp(input_keys[j], "node_number")) {
				asprintf(&(input_values[((i-min) * input_num_keys) + j]), "%d", i);
#ifndef HAVE_SYS_BPROC_H
			} else if (!strcmp(input_keys[j], ORTE_SOH_BPROC_NODE_USER)) {
				asprintf(&(input_values[((i-min) * input_num_keys) + j]), "%s", pwd->pw_name);
			} else if (!strcmp(input_keys[j], ORTE_SOH_BPROC_NODE_GROUP)) {
				asprintf(&(input_values[((i-min) * input_num_keys) + j]), "%s", grp->gr_name);
			} else if (!strcmp(input_keys[j], ORTE_SOH_BPROC_NODE_STATUS)) {
				asprintf(&(input_values[((i-min) * input_num_keys) + j]), "%s", status);
			} else if (!strcmp(input_keys[j], ORTE_SOH_BPROC_NODE_MODE)) {
				asprintf(&(input_values[((i-min) * input_num_keys) + j]), "%s", mode);
#endif
			} else {
				switch(input_types[j]) {
					case PTP_STRING:
						asprintf(&(input_values[((i-min) * input_num_keys) + j]), "%s", get_str_value(value, input_keys[j]));
						break;
					case PTP_UINT32:
						asprintf(&(input_values[((i-min) * input_num_keys) + j]), "%d", get_ui32_value(value, input_keys[j]));
						break;
				}
			}
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

int
ORTEQuit(char **args)
{
	char *res;
	printf("ORTEQuit called!\n"); fflush(stdout);
	ORTEShutdown();
	asprintf(&res, "%d", RTEV_OK);
	proxy_svr_event_callback(orte_proxy, res);
	free(res);	
	return PROXY_RES_OK;
}

int
server(char *name, char *host, int port)
{
	char *msg, *msg1, *msg2;
	int rc;
	
	eventList = NewList();
	jobList = NewList();
	
	if (proxy_svr_init(name, &handler_funcs, &helper_funcs, command_tab, &orte_proxy) != PROXY_RES_OK)
		return 0;
	
	proxy_svr_connect(orte_proxy, host, port);
	printf("proxy_svr_connect returned.\n");
	
	while (ptp_signal_exit == 0 && !ORTEIsShutdown()) {
		if  ((ORTEProgress() != PROXY_RES_OK) ||
			(proxy_svr_progress(orte_proxy) != PROXY_RES_OK))
			break;
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
		printf("###### Shutting down ORTEd\n");
		ORTEShutdown();
		asprintf(&msg, "ptp_orte_proxy received signal %s (%s).  Exit was required and performed cleanly.", msg1, msg2);
		proxy_svr_event_callback(orte_proxy, ORTEErrorStr(RTEV_ERROR_SIGNAL, msg));
		free(msg);
		free(msg1);
		free(msg2);
		/* our return code = the signal that fired */
		rc = ptp_signal_exit;
	}
	
	proxy_svr_finish(orte_proxy);
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
main(int argc, char *argv[])
{
	int				ch;
	int				port = PROXY_TCP_PORT;
	char *			host = "localhost";
	char *			proxy_str = DEFAULT_PROXY;
	int				rc;
	
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
