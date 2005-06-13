#include <jni.h>
#include "ptp_ompi_jni.h"
#include <stdio.h>

#include "orte_config.h"
#include <stdbool.h>

#include "include/orte_constants.h"
#include "mca/errmgr/errmgr.h"
#include "runtime/runtime.h"
#include "mca/gpr/gpr.h"
#include "mca/rmgr/rmgr.h"
#include "mca/base/base.h"

#include "threads/condition.h"

bool ptp_exitted;
ompi_mutex_t ptp_lock;
ompi_condition_t ptp_cond;

static void job_state_callback(orte_jobid_t jobid, orte_proc_state_t state);
static int ptp_ompi_spawn(char *app, int num_procs);

/***********************************************************************
 * THE JNI FUNCTIONS THAT THE RUNTIME ENVIRONMENT WILL CALL - THESE ARE
 * JUST STUBS THAT WILL CALL ADDITIONAL FUNCTIONS TO DO THE 'REAL'
 * WORK
 **********************************************************************/
JNIEXPORT void JNICALL 
Java_org_eclipse_ptp_rtmodel_ompi_OMPIRuntimeModel_testHelloWorld(JNIEnv *env, jobject obj) 
{
	printf("JNI (C) OMPI: Hello world!\n");
	fflush(stdout);
	return;
}

JNIEXPORT void JNICALL 
Java_org_eclipse_ptp_rtmodel_ompi_OMPIRuntimeModel_OMPIRun(JNIEnv *env, jobject obj) 
{
	printf("JNI (C) OMPI: OMPIRun() starting . . .\n");
	fflush(stdout);

	ptp_ompi_spawn("/Users/ndebard/ompi-test/mpi-test", 2);
	
	return;
}

/**********************************************************************
 * THE FUNCTIONS THAT DO THE ACTUAL WORK
 *********************************************************************/

/* 'app' needs to be a full pathname to the app as we'll extract the
 * current working directory from it as well
 *
 * 'num_procs' is the number of processes to spawn */
int ptp_ompi_spawn(char *app, int num_procs)
{
	int rc;
	int num_apps;
	int i;
	orte_app_context_t **apps;
	orte_jobid_t jobid = ORTE_JOBID_MAX;
	char pgm_name[128], cwd[128];
	char *c;

	c = rindex(app, '/');
	printf("str = %s\n", app);
	if(c == NULL)
	    printf("index = NULL\n");
	else
	    printf("index = %s\n", c);
	strncpy(pgm_name, c + 1, strlen(c));
	printf("program name = %s\n", pgm_name);
	strncpy(cwd, app, c - app + 1);
	cwd[c-app+1] = '\0';
	printf("cwd = %s\n", cwd);
	
	/* to copy i -> j 
	 *  strncpy(new_buffer, old_buffer + i, j-i+1);
	 *          new_buffer[j-i+1] = '\0';
	 */
	
	/* setup the runtime environment */
	if (ORTE_SUCCESS != (rc = orte_init())) {
	    ORTE_ERROR_LOG(rc);
	    return rc;
	}
	printf("Registry initted.\n");

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

	/* spawn the job */
	rc = orte_rmgr.spawn(apps, num_apps, &jobid, job_state_callback);
	printf("after spawn - jobid = %d\n", jobid);

	if(rc != ORTE_SUCCESS) {
	    ompi_output(0, "%s: failed with errno=%d\n", app,
	      rc);
	}
	else {
	    OMPI_THREAD_LOCK(&ptp_lock);
	    while(!ptp_exitted) {
		ompi_condition_wait(&ptp_cond, &ptp_lock);
	    }
	    printf("Broke out of wait!\n");
	    OMPI_THREAD_UNLOCK(&ptp_lock);
	}
	
	for(i=0; i<num_apps; i++) OBJ_RELEASE(apps[i]);
	free(apps);

	orte_finalize();

	printf("Registry finalized.\n");

	return 0;
}

static void job_state_callback(orte_jobid_t jobid, orte_proc_state_t state)
{
    	printf("CALLED: job_state_callback()\n");
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
		OMPI_THREAD_LOCK(&ptp_lock);
		ptp_exitted = true;
		ompi_condition_signal(&ptp_cond);
		OMPI_THREAD_UNLOCK(&ptp_lock);
		break;
	    case ORTE_PROC_STATE_ABORTED:
	    	printf("    state = ORTE_PROC_STATE_ABORTED\n");
		break;
	}
}
