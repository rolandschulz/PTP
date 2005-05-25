#include "orte_config.h"
#include <stdbool.h>

#include "include/orte_constants.h"
#include "mca/errmgr/errmgr.h"
#include "runtime/runtime.h"
#include "mca/gpr/gpr.h"
#include "mca/rmgr/rmgr.h"
#include "mca/base/base.h"

#include "threads/condition.h"

bool exitted;
ompi_mutex_t lock;
ompi_condition_t cond;

static void job_state_callback(orte_jobid_t jobid, orte_proc_state_t state);

int main(int argc, char **argv)
{
	int rc;
	int num_apps;
	int i;
	orte_app_context_t **apps;
	orte_jobid_t jobid = ORTE_JOBID_MAX;
	
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
	apps[0]->num_procs = 2;
	apps[0]->app = strdup("/home/ndebard/ompi-test/test-mpi");
	apps[0]->cwd = strdup("/home/ndebard/ompi-test/");
	/* no special environment variables */
	apps[0]->num_env = 0;
	apps[0]->env = NULL;
	/* no special mapping of processes to nodes */
	apps[0]->num_map = 0;
	apps[0]->map_data = NULL;
	/* setup argv */
	apps[0]->argv = (char **)malloc(1 * sizeof(char *));
	apps[0]->argv[0] = strdup("test-mpi");
	apps[0]->argc = 1;

	/* spawn the job */
	rc = orte_rmgr.spawn(apps, num_apps, &jobid, job_state_callback);
	printf("after spawn - jobid = %d\n", jobid);

	if(rc != ORTE_SUCCESS) {
	    ompi_output(0, "%s: failed with errno=%d\n", argv[0],
	      rc);
	}
	else {
	    OMPI_THREAD_LOCK(&lock);
	    while(!exitted) {
		ompi_condition_wait(&cond, &lock);
	    }
	    printf("Broke out of wait!\n");
	    OMPI_THREAD_UNLOCK(&lock);
	}
	
	for(i=0; i<num_apps; i++) OBJ_RELEASE(apps[i]);
	free(apps);

	orte_finalize();

	printf("Registry finalized.\n");

	return(0);
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
		OMPI_THREAD_LOCK(&lock);
		exitted = true;
		ompi_condition_signal(&cond);
		OMPI_THREAD_UNLOCK(&lock);
		break;
	    case ORTE_PROC_STATE_ABORTED:
	    	printf("    state = ORTE_PROC_STATE_ABORTED\n");
		break;
	}
}
