#include <jni.h>
#include "ptp_ompi_jni.h"
#include <stdio.h>
#include <unistd.h>

#include "orte_config.h"
#include <stdbool.h>

#include "include/orte_constants.h"
#include "mca/errmgr/errmgr.h"
#include "runtime/runtime.h"
#include "mca/gpr/gpr.h"
#include "mca/rmgr/rmgr.h"
#include "mca/base/base.h"
#include "mca/errmgr/errmgr.h"
#include "mca/rml/rml.h"
#include "mca/pls/base/base.h"

#include "event/event.h"

#include "threads/condition.h"
#include "tools/orted/orted.h"

char error_msg[256];

static int pid;
static int pgid;

static void job_state_callback(orte_jobid_t jobid, orte_proc_state_t state);
static int ptp_ompi_spawn(char *app, int num_procs);
static void ptp_ompi_sendcmd(orte_daemon_cmd_flag_t usercmd);

void set_error(char *msg);

extern int errno;

opal_mutex_t eclipse_orte_lock; 

/***********************************************************************
 * THE JNI FUNCTIONS THAT THE RUNTIME ENVIRONMENT WILL CALL - THESE ARE
 * JUST STUBS THAT WILL CALL ADDITIONAL FUNCTIONS TO DO THE 'REAL'
 * WORK
 **********************************************************************/

JNIEXPORT jstring JNICALL
Java_org_eclipse_ptp_rtsystem_ompi_OMPIControlSystem_OMPIGetError(JNIEnv *env, jobject obj)
{
    	return (*env)->NewStringUTF(env, error_msg);
}

JNIEXPORT jint JNICALL Java_org_eclipse_ptp_rtsystem_ompi_OMPIControlSystem_OMPIStartDaemon(JNIEnv *env, jobject obj, jstring jorted_path, jstring jorted_bin, jobjectArray array)
{
    	char *orted_path, *orted_bin;
	char **orted_args;
	int ret;
	jsize len;
	int i;

	printf("JNI (C) OMPI: OMPI_StartDaemon()\n");

	pid = pgid = -1;

	switch(pid = fork()) {
	    case -1:
		set_error("Unable to fork() for the orted spawn.");
		return -1;
		break;
	    /* child */
	    case 0:
		orted_path = (char *)(*env)->GetStringUTFChars(env, 
		  jorted_path, NULL);
		orted_bin = (char *)(*env)->GetStringUTFChars(env, 
		  jorted_bin, NULL);
		len = (*env)->GetArrayLength(env, array);

		/* we need 'len + 2' args for len args, 1 for NULL
		 * termination, and 1 preface for the program name
		 * (argv[0] essentially) */
		orted_args = (char **)malloc((len+2) * sizeof(char*));
		orted_args[0] = strdup(orted_bin);
		for(i=1; i<len+1; i++) {
		    jstring astr;
		    char *bstr;

		    astr = (*env)->GetObjectArrayElement(env, array, i - 1);
		    bstr = (char *)(*env)->GetStringUTFChars(env, astr, NULL);

		    /* copy the original string from the Java array over
		     * into a C array element */
		    orted_args[i] = strdup(bstr);
		    
		    (*env)->ReleaseStringUTFChars(env, astr, bstr);
		}
		/* exec() requires the array be NULL terminated */
		orted_args[len+1] = NULL;

		printf("PATH: '%s'\n", orted_path);
		for(i=0; i<len+2; i++) {
		    if(orted_args[i] != NULL) 
		    	printf("[C] #%d = '%s'\n", i, orted_args[i]);
		    else
			printf("[C] #%d = NULL\n", i);
		    fflush(stdout);
		}
		
		/* spawn the daemon */
		ret = execv(orted_path, orted_args);
		printf("exec returned: %d\n", ret);
		fflush(stdout);
		printf("error: %s\n", strerror(errno));

		//ret = execl("/Users/ndebard/local/bin/orted", "orted", 0);
		/* release str */
		(*env)->ReleaseStringUTFChars(env, jorted_path, orted_path);
		(*env)->ReleaseStringUTFChars(env, jorted_bin, orted_bin);
		for(i=0; i<len + 1; i++) {
		    free(orted_args[i]);
		}
		free(orted_args);
		break;
	    /* parent */
	    default:
		printf("PARENT!\n");
		pgid = getpgid(pid);
		printf("[p] GROUP PID = %d\n", pgid);
		sleep(1);
		pgid = getpgid(pid);
		printf("[p] GROUP PID = %d\n", pgid);
		wait(&ret);
		printf("parent ret from child = %d\n", ret);
		printf("PID = %d\n", pid);
		printf("GROUP PID = %d\n", pgid);
		fflush(stdout);
		break;
	}
}

/* returns  0 if orted was already started and we connected to it
 * returns  1 if orted was NOT started so the caller needs to start it
 * returns -1 if there was some error of any kind - call OMPIGetError()
 *            to get the error string
 */
JNIEXPORT jint JNICALL Java_org_eclipse_ptp_rtsystem_ompi_OMPIControlSystem_OMPIInit(JNIEnv *env, jobject obj)
{
    	int rc, id;

    	printf("JNI (C) OMPI: OMPIInit()\n");
	set_error("no error");
	fflush(stdout);

	/* this makes the orte_init() below fail if the orte daemon
	 * isn't running */
	putenv("OMPI_MCA_orte_univ_exist=1");
	/*
	id = mca_base_param_register_int("orte", "univ", "exist",NULL,0);
	mca_base_param_set_int(id, 1);
	*/
	
	/* setup the runtime environment */
	rc = orte_init();
	if(rc == ORTE_ERR_UNREACH) {
	    /* unreachable orted */
	    set_error("ORTED unreachable!  Sorry!");
	    printf("ORTED unreachable!  Sorry!\n");
	    fflush(stdout);
	    return 1;
	}

	/*
	    printf("ERROR: orte_init() failed - return code = %d!\n", rc);
	    fflush(stdout);
	    ORTE_ERROR_LOG(rc);
	    return;
	}
	*/
	printf("Registry initted.\n");
	fflush(stdout);

	OBJ_CONSTRUCT(&eclipse_orte_lock, opal_mutex_t); 
	return 0;
}

JNIEXPORT void JNICALL 
Java_org_eclipse_ptp_rtsystem_ompi_OMPIControlSystem_OMPIFinalize(JNIEnv *env, jobject obj) 
{
    	printf("JNI (C) OMPI: OMPIFinalize()\n");
	fflush(stdout);
	opal_mutex_lock(&opal_event_lock);
	orte_finalize();
	opal_mutex_unlock(&opal_event_lock);

	printf("Registry finalized.\n");
	fflush(stdout);
}

JNIEXPORT void JNICALL 
Java_org_eclipse_ptp_rtsystem_ompi_OMPIControlSystem_OMPIProgress(JNIEnv *env, jobject obj) 
{
    	printf("JNI (C) OMPI: OMPIProgress() starting . . .\n");
	fflush(stdout);

	opal_mutex_lock(&opal_event_lock);
	opal_event_loop(0); 
	opal_mutex_unlock(&opal_event_lock);
#if 0
	while(1) {
	    ompi_mutex_lock(&eclipse_orte_lock);
	    ompi_event_loop(OMPI_EVLOOP_NONBLOCK);
	    ompi_mutex_unlock(&eclipse_orte_lock);
	    usleep(1000);
	} 
#endif


    	printf("JNI (C) OMPI: OMPIProgress() exiting . . .\n");
	fflush(stdout);
}

JNIEXPORT void JNICALL 
Java_org_eclipse_ptp_rtsystem_ompi_OMPIControlSystem_OMPIRun(JNIEnv *env, jobject obj, jobjectArray array)
{
	printf("JNI (C) OMPI: OMPIRun() starting . . .\n");
	fflush(stdout);

	jstring jstr;
	char *str;
	int i;

	jsize len = (*env)->GetArrayLength(env, array);
	
	for(i=0; i<len; i++) {
	    jstr = (*env)->GetObjectArrayElement(env, array, i);
	    str = (char *)(*env)->GetStringUTFChars(env, jstr, NULL);

	    printf("string[%d] = %s\n", i, str);

	    /* release str */
	    (*env)->ReleaseStringUTFChars(env, jstr, str);
	}

	opal_mutex_lock(&eclipse_orte_lock);
	ptp_ompi_spawn("/Users/ndebard/ompi-test/test-mpi", 2);
	opal_mutex_unlock(&eclipse_orte_lock); 
	
	return;
}

JNIEXPORT void JNICALL 
Java_org_eclipse_ptp_rtsystem_ompi_OMPIControlSystem_OMPIShutdown(JNIEnv *env, jobject obj)
{
    	int ret;

    	printf("JNI (C) OMPI: OMPIShutdown()\n");
	fflush(stdout);
    	ptp_ompi_sendcmd(ORTE_DAEMON_EXIT_CMD);
}

/**********************************************************************
 * THE FUNCTIONS THAT DO THE ACTUAL WORK
 *********************************************************************/

void set_error(char *msg)
{
    	strncpy(error_msg, msg, 256);
}

/* 'app' needs to be a full pathname to the app as we'll extract the
 * current working directory from it as well
 *
 * 'num_procs' is the number of processes to spawn */
int ptp_ompi_spawn(char *app, int num_procs)
{
	int rc;
	int i;
	orte_jobid_t jobid = ORTE_JOBID_MAX;
	char pgm_name[128], cwd[128];
	char *c;
	orte_app_context_t **apps;
	int num_apps;

	c = rindex(app, '/');
	printf("str = %s\n", app);
	if(c == NULL)
	    printf("index = NULL\n");
	else
	    printf("index = %s\n", c);
	fflush(stdout);

	strncpy(pgm_name, c + 1, strlen(c));
	printf("program name = %s\n", pgm_name);
	fflush(stdout);
	strncpy(cwd, app, c - app + 1);
	cwd[c-app+1] = '\0';
	printf("cwd = %s\n", cwd);
	fflush(stdout);
	
	/* to copy i -> j 
	 *  strncpy(new_buffer, old_buffer + i, j-i+1);
	 *          new_buffer[j-i+1] = '\0';
	 */
	
	/* hard coded test for spawning just 1 job */
	num_apps = 1;
	
	/* format the app_context_t struct */
	apps = malloc(sizeof(orte_app_context_t *) * num_apps);
	apps[0] = OBJ_NEW(orte_app_context_t);
	apps[0]->num_procs = num_procs;
	apps[0]->app = strdup(app);
	apps[0]->cwd = strdup(cwd);
	/* no special environment variables */
	apps[0]->num_env = 0;
	apps[0]->env = NULL;
	/* no special mapping of processes to nodes */
	apps[0]->num_map = 0;
	apps[0]->map_data = NULL;
	/* setup argv */
	apps[0]->argv = (char **)malloc(2 * sizeof(char *));
	apps[0]->argv[0] = strdup(pgm_name);
	apps[0]->argv[1] = NULL;

	apps[0]->argc = 1;

	printf("Spawning %d processes of job '%s'\n", 
	  apps[0]->num_procs, apps[0]->app);
	fflush(stdout);

	/* spawn the job */
	opal_mutex_lock(&opal_event_lock);
	rc = orte_rmgr.spawn(apps, num_apps, &jobid, job_state_callback);
	opal_mutex_unlock(&opal_event_lock);
	if(rc != ORTE_SUCCESS) {
	    opal_output(0, "%s: failed with errno=%d\n", app,
	      rc);
	    return rc;
	}
	printf("after spawn - jobid = %d\n", jobid);
	fflush(stdout);
	for(i=0; i<num_apps; i++) OBJ_RELEASE(apps[i]);
	free(apps);

	return 0;
}

static void job_state_callback(orte_jobid_t jobid, orte_proc_state_t state)
{
    	printf("CALLED: job_state_callback() - jobid = %d\n", jobid);
	fflush(stdout);
    	switch(state) {
	    case ORTE_PROC_STATE_INIT:
	    	printf("    state = ORTE_PROC_STATE_INIT\n");
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
		break;
	    case ORTE_PROC_STATE_ABORTED:
	    	printf("    state = ORTE_PROC_STATE_ABORTED\n");
		break;
	}
	fflush(stdout);
}

static void ptp_ompi_sendcmd(orte_daemon_cmd_flag_t usercmd)
{
	orte_buffer_t *cmd;
	orte_daemon_cmd_flag_t command;
	int rc;
	orte_process_name_t seed={0,0,0};

	cmd = OBJ_NEW(orte_buffer_t);
	if (NULL == cmd) {
	    fprintf(stderr, "console: comm failure\n");
	    return;
	}
	command = usercmd;

	if (ORTE_SUCCESS != (rc = orte_dps.pack(cmd, &command, 1, 
	    ORTE_DAEMON_CMD))) 
	{
	    ORTE_ERROR_LOG(rc);
	    return;
	}
	if (0 > orte_rml.send_buffer(&seed, cmd, ORTE_RML_TAG_DAEMON, 0)) {
	    ORTE_ERROR_LOG(ORTE_ERR_COMM_FAILURE);
	    return;
	}
	OBJ_RELEASE(cmd);
}
