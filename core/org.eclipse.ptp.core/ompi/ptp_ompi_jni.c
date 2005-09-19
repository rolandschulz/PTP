/*******************************************************************************
 * Copyright (c) 2005 The Regents of the University of California. 
 * This material was produced under U.S. Government contract W-7405-ENG-36 
 * for Los Alamos National Laboratory, which is operated by the University 
 * of California for the U.S. Department of Energy. The U.S. Government has 
 * rights to use, reproduce, and distribute this software. NEITHER THE 
 * GOVERNMENT NOR THE UNIVERSITY MAKES ANY WARRANTY, EXPRESS OR IMPLIED, OR 
 * ASSUMES ANY LIABILITY FOR THE USE OF THIS SOFTWARE. If software is modified 
 * to produce derivative works, such modified software should be clearly marked, 
 * so as not to confuse it with the version available from LANL.
 * 
 * Additionally, this program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * LA-CC 04-115
 *******************************************************************************/
 
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
//#include "mca/pls/base/base.h"

#include "event/event.h"

#include "threads/condition.h"
#include "tools/orted/orted.h"

char error_msg[256];

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
Java_org_eclipse_ptp_rtsystem_ompi_OMPIJNIBroker_OMPIGetError(JNIEnv *env, jobject obj)
{
    	return (*env)->NewStringUTF(env, error_msg);
}

JNIEXPORT jint JNICALL Java_org_eclipse_ptp_rtsystem_ompi_OMPIJNIBroker_OMPIStartDaemon(JNIEnv *env, jobject obj, jstring jompi_bin_path, jstring jorted_path, jstring jorted_bin, jobjectArray array)
{
    	char *orted_path, *orted_bin, *ompi_bin_path;
	char **orted_args;
	int ret;
	jsize len;
	int i;

	char args[256];
	char spawn_cmd[512];
	char *user_path, *user_path_new, *user_path_new_tmp;

	printf("JNI (C) OMPI: OMPI_StartDaemon()\n");

	/* the below, if 0ed out code uses fork() and exec() to spawn
	 * the orte daemon.  it works, but you don't seem to be able to
	 * run MPI jobs through that daemon once it's spawned.  we
	 * theorize that this has something to do with the environment
	 * not being copied over and we might need to switch to execle()
	 * in the future and specify the environment.  for now, let's go
	 * with a much simpler (albiet less portable) solution of
	 * system() */
	switch(fork()) {
	    case -1:
		set_error("Unable to fork() for the orted spawn.");
		return -1;
		break;
	    /* child */
	    case 0:
		ompi_bin_path = (char *)(*env)->GetStringUTFChars(env,
		  jompi_bin_path, NULL);
		orted_path = (char *)(*env)->GetStringUTFChars(env, 
		  jorted_path, NULL);
		orted_bin = (char *)(*env)->GetStringUTFChars(env, 
		  jorted_bin, NULL);
		len = (*env)->GetArrayLength(env, array);

		//user_path = (char*)malloc(2048 * sizeof(char));
		user_path_new_tmp = (char*)malloc((2048+strlen(ompi_bin_path+2))*sizeof(char));
		user_path_new = (char*)malloc((2048+strlen(ompi_bin_path+2))*sizeof(char));
		user_path = getenv("PATH");
		printf("Original user's PATH: %s\n", user_path);
		user_path_new_tmp = strcat(ompi_bin_path, ":");
		user_path_new = strcat(user_path_new_tmp, user_path);
		setenv("PATH", user_path_new, 1);
		user_path = getenv("PATH");
		printf("New user's PATH (temporarily) after prepending "
		  "OMPI bin: %s\n", user_path);
		printf("sup dog!?\n"); fflush(stdout);
		//free(user_path);
		//printf("after free1.\n"); fflush(stdout);
		free(user_path_new_tmp);
		printf("after free1.\n"); fflush(stdout);
		free(user_path_new);
		printf("after frees.\n"); fflush(stdout);

		/* we need 'len + 2' args for len args, 1 for NULL
		 * termination, and 1 preface for the program name
		 * (argv[0] essentially) */
		orted_args = (char **)malloc((len+2) * sizeof(char*));
		orted_args[0] = strdup(orted_bin);
		printf("woot args time!\n"); fflush(stdout);
		for(i=1; i<len+1; i++) {
		    jstring astr;
		    char *bstr;

		    astr = (*env)->GetObjectArrayElement(env, array, i - 1);
		    bstr = (char *)(*env)->GetStringUTFChars(env, astr, NULL);
		    printf("bstr[%d] = %s\n", i, bstr); fflush(stdout);

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

		(*env)->ReleaseStringUTFChars(env, jorted_path, orted_path);
		(*env)->ReleaseStringUTFChars(env, jorted_bin, orted_bin);
		(*env)->ReleaseStringUTFChars(env, jompi_bin_path, ompi_bin_path);
		for(i=0; i<len + 1; i++) {
		    free(orted_args[i]);
		}
		free(orted_args);
		break;
	    /* parent */
	    default:
		/* sleep - letting the daemon get started up */
		sleep(1);
		wait(&ret);
		printf("parent ret from child = %d\n", ret);
		break;
	}
}

/* returns  0 if orted was already started and we connected to it
 * returns  1 if orted was NOT started so the caller needs to start it
 * returns -1 if there was some error of any kind - call OMPIGetError()
 *            to get the error string
 */
JNIEXPORT jint JNICALL Java_org_eclipse_ptp_rtsystem_ompi_OMPIJNIBroker_OMPIInit(JNIEnv *env, jobject obj)
{
    	int rc, id;

    	printf("JNI (C) OMPI: OMPIInit()\n");
	set_error("no error");
	fflush(stdout);

	/* this makes the orte_init() below fail if the orte daemon
	 * isn't running */
	putenv("OMPI_MCA_orte_univ_exist=1");
	
	/* setup the runtime environment */
	rc = orte_init(true);
	if(rc == ORTE_ERR_UNREACH) {
	    /* unreachable orted */
	    char foo[256];
	    snprintf(foo, sizeof(foo), "ORTEd unreachable.  Check the "
	      "preferences and be sure the path and arguments to the ORTEd "
	      "are valid: %s", 
	      ORTE_ERROR_NAME(rc));
	    set_error(foo);
	    printf(foo);
	    printf("\n");
	    fflush(stdout);
	    return 1;
	}
	else if(rc != ORTE_SUCCESS) {
	    char foo[256];
	    snprintf(foo, sizeof(foo), "ORTEd initilization failure: %s", 
	      ORTE_ERROR_NAME(rc));
	    set_error(foo);
	    printf(foo);
	    printf("\n");
	    fflush(stdout);
	}

	printf("Registry initted successfully.\n");
	fflush(stdout);

	OBJ_CONSTRUCT(&eclipse_orte_lock, opal_mutex_t); 
	return 0;
}

JNIEXPORT void JNICALL 
Java_org_eclipse_ptp_rtsystem_ompi_OMPIJNIBroker_OMPIFinalize(JNIEnv *env, jobject obj) 
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
Java_org_eclipse_ptp_rtsystem_ompi_OMPIJNIBroker_OMPIProgress(JNIEnv *env, jobject obj) 
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
Java_org_eclipse_ptp_rtsystem_ompi_OMPIJNIBroker_OMPITerminateJob(JNIEnv *env, jobject obj, jint jjobid)
{
    	int jobid;

	jobid = jjobid;
	printf("JNI (C) OMPI: OMPITerminateJob(%d)\n", jobid);
	fflush(stdout);
	orte_rmgr.terminate_job(jobid);
}


/* Invokes an OMPI parallel job run.  The string array passed in is
 * composed of key-value pairs (keys are even 0, 2, 4, etc - values are
 * 1, 3, 5, etc).  The KVPs specify what program to run, the number of
 * processes, etc.
 *
 * RETURN:
 *   Upon error a -1 will be returned and the error string will be set
 *   through set_error();
 *   Any other integer return signifies a successful spawn and the
 *   integer represents the OMPI JobID of the created parallel job
 */
JNIEXPORT jint JNICALL 
Java_org_eclipse_ptp_rtsystem_ompi_OMPIJNIBroker_OMPIRun(JNIEnv *env, jobject obj, jobjectArray array)
{
	printf("JNI (C) OMPI: OMPIRun() starting . . .\n");
	fflush(stdout);

	jstring jstr, jstr2;
	char *str, *str2;
	char *exec_path;
	int num_procs;
	int i, rc;

	jsize len = (*env)->GetArrayLength(env, array);
	
	/* hop in twos because we're sending Key-Value pairs */
	for(i=0; i<len; i+=2) {
	    jstr = (*env)->GetObjectArrayElement(env, array, i);
	    str = (char *)(*env)->GetStringUTFChars(env, jstr, NULL);
	    jstr2 = (*env)->GetObjectArrayElement(env, array, i+1);
	    str2 = (char *)(*env)->GetStringUTFChars(env, jstr2, NULL);

	    printf("string[%d] = %s\n", i, str);
	    if(strcmp(str, "pathToExecutable") == 0) {
		exec_path = strdup(str2);
	    }
	    else if(strcmp(str, "numberOfProcesses") == 0) {
		num_procs = atoi(str2);
	    }

	    /* release str */
	    (*env)->ReleaseStringUTFChars(env, jstr, str);
	    (*env)->ReleaseStringUTFChars(env, jstr2, str2);
	}

	printf("path = '%s', #procs = %d\n", exec_path, num_procs);

	opal_mutex_lock(&eclipse_orte_lock);
	rc = ptp_ompi_spawn(exec_path, num_procs);
	opal_mutex_unlock(&eclipse_orte_lock); 
	
	free(exec_path);
	return rc;
}

JNIEXPORT void JNICALL 
Java_org_eclipse_ptp_rtsystem_ompi_OMPIJNIBroker_OMPIShutdown(JNIEnv *env, jobject obj)
{
    	int ret;

    	printf("JNI (C) OMPI: OMPIShutdown()\n");
	fflush(stdout);
    	ptp_ompi_sendcmd(ORTE_DAEMON_EXIT_CMD);
}

/* PROCESS/JOB MONITORING */
JNIEXPORT jobjectArray JNICALL Java_org_eclipse_ptp_rtsystem_ompi_OMPIJNIBroker_OMPIGetJobs (JNIEnv *env, jobject obj)
{
}

JNIEXPORT jobjectArray JNICALL Java_org_eclipse_ptp_rtsystem_ompi_OMPIJNIBroker_OMPIGetProcesses (JNIEnv *env, jobject obj, jstring jjobName)
{
	char *jobName;

	jobName = (char *)(*env)->GetStringUTFChars(env, jjobName, NULL);

	(*env)->ReleaseStringUTFChars(env, jjobName, jobName);
}

JNIEXPORT jstring JNICALL Java_org_eclipse_ptp_rtsystem_ompi_OMPIJNIBroker_OMPIGetProcessesAttribute (JNIEnv *env, jobject obj, jstring jprocName, jstring jattrib)
{
	char *procName, *attrib;
	char value[256];

	procName = (char *)(*env)->GetStringUTFChars(env, jprocName, NULL);
	attrib = (char *)(*env)->GetStringUTFChars(env, jattrib, NULL);

	(*env)->ReleaseStringUTFChars(env, jprocName, procName);
	(*env)->ReleaseStringUTFChars(env, jattrib, attrib);

	if(!strcmp(attrib, "ATTRIB_NODE_STATE")) {
	    snprintf(value, 256, "down");
	} else if(!strcmp(attrib, "ATTRIB_NODE_MODE")) {
	    snprintf(value, 256, "0100");
	} else if(!strcmp(attrib, "ATTRIB_NODE_USER")) {
	    snprintf(value, 256, "root");
	} else if(!strcmp(attrib, "ATTRIB_NODE_GROUP")) {
	    snprintf(value, 256, "root");
	} else {
	    return NULL;
	}

	return ((*env)->NewStringUTF(env, value));
}

/* NODE/MACHINE MONITORING */
JNIEXPORT jstring JNICALL Java_org_eclipse_ptp_rtsystem_ompi_OMPIJNIBroker_OMPIGetMachines (JNIEnv *env, jobject obj)
{
}

JNIEXPORT jobjectArray JNICALL Java_org_eclipse_ptp_rtsystem_ompi_OMPIJNIBroker_OMPIGetNodes (JNIEnv *env, jobject obj, jstring jmachineName)
{
	char *machineName;

	machineName = (char *)(*env)->GetStringUTFChars(env, jmachineName, NULL);

	(*env)->ReleaseStringUTFChars(env, jmachineName, machineName);
}

JNIEXPORT jstring JNICALL Java_org_eclipse_ptp_rtsystem_ompi_OMPIJNIBroker_OMPIGetNodeMachineName (JNIEnv *env, jobject obj, jstring jnodeName)
{
	char *nodeName;

	nodeName = (char *)(*env)->GetStringUTFChars(env, jnodeName, NULL);

	(*env)->ReleaseStringUTFChars(env, jnodeName, nodeName);
}

JNIEXPORT jstring JNICALL Java_org_eclipse_ptp_rtsystem_ompi_OMPIJNIBroker_OMPIGetNodeAttribute (JNIEnv *env, jobject obj, jstring jnodeName, jstring jattrib)
{ 
	char *nodeName, *attrib;

	nodeName = (char *)(*env)->GetStringUTFChars(env, jnodeName, NULL);
	attrib = (char *)(*env)->GetStringUTFChars(env, jattrib, NULL);

	(*env)->ReleaseStringUTFChars(env, jnodeName, nodeName);
	(*env)->ReleaseStringUTFChars(env, jattrib, attrib);
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
 * 'num_procs' is the number of processes to spawn
 *
 * RETURN:
 *   returns -1 if there was an error and the error string will be set
 *   with set_error()
 *   returns any other integer indicating the OMPI JobID number of this
 *   new job
 */
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
	    char foo[256];
	    opal_output(0, "%s: failed with errno=%d\n", app,
	      rc);
	    snprintf(foo, sizeof(foo), "OMPI spawn() failure: %s", 
	      ORTE_ERROR_NAME(rc));
	    set_error(foo);
	    printf(foo);
	    printf("\n");
	    fflush(stdout);
	    return -1;
	}
	printf("after spawn - jobid = %d\n", jobid);
	fflush(stdout);
	for(i=0; i<num_apps; i++) OBJ_RELEASE(apps[i]);
	free(apps);

	return jobid;
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
