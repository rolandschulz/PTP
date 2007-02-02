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

#define ORTE_VERSION_1_0	(ORTE_MAJOR_VERSION == 1 && ORTE_MINOR_VERSION == 0)

#include "opal/util/output.h"
#include "opal/util/path.h"
#include "opal/event/event.h"
#include "opal/threads/condition.h"

#if ORTE_VERSION_1_0
#include "include/orte_constants.h"
#else /* ORTE_VERSION_1_0 */
#include "orte/orte_constants.h"
#endif /* ORTE_VERSION_1_0 */

#include "orte/tools/orted/orted.h"

#include "orte/mca/iof/iof.h"
#include "orte/mca/rmgr/rmgr.h"
#include "orte/mca/errmgr/errmgr.h"
#include "orte/mca/rml/rml.h"
#include "orte/mca/gpr/gpr.h"
#if ORTE_VERSION_1_0
#include "orte/mca/rmgr/base/base.h"
#else /* ORTE_VERSION_1_0 */
#include "orte/mca/pls/pls.h"
#include "orte/mca/rds/rds.h"
#include "orte/mca/ras/ras.h"
#include "orte/mca/rmaps/rmaps.h"
#include "orte/mca/smr/smr.h"
#include "orte/mca/rmgr/base/rmgr_private.h"
#include "orte/mca/odls/odls.h"
#endif /* ORTE_VERSION_1_0 */

#ifdef HAVE_SYS_BPROC_H
#include "orte/mca/soh/bproc/soh_bproc.h"
#endif

#include "orte/runtime/runtime.h"

#if ORTE_VERSION_1_0
#define ORTE_QUERY(jobid)									orte_rmgr.query()
#define ORTE_LAUNCH_JOB(jobid)								orte_rmgr.launch(jobid)
#define ORTE_TERMINATE_JOB(jobid,attr)						orte_rmgr.terminate_job(jobid)
#define ORTE_SETUP_JOB(app_context,num_context,jobid,attr)	orte_rmgr.create(app_context,num_context,jobid)
#define ORTE_SUBSCRIBE(jobid,cbfunc,cbdata,cond)			orte_rmgr_base_proc_stage_gate_subscribe(jobid,cbfunc,cbdata,cond)
#define ORTE_SPAWN(apps,num_apps,jobid,cbfunc)				orte_rmgr.spawn(apps,num_apps,jobid,cbfunc)
#define ORTE_PACK(buf,cmd,num,type)							orte_dps.pack(buf,cmd,num,type)
#define ORTE_FREE_NAME(name)								orte_ns.free_name(&name)
#define ORTE_KEYVALUE_TYPE(keyval)							(keyval)->type
#define ORTE_GET_UINT32_VALUE(keyval)						(keyval)->value.ui32
#define ORTE_GET_STRING_VALUE(keyval)						(keyval)->value.strptr
#define ORTE_GET_PID_VALUE(keyval)							(keyval)->value.pid
#define ORTE_NOTIFY_ALL										ORTE_STAGE_GATE_ALL
#define ORTE_STD_CNTR_TYPE									size_t
#else /* ORTE_VERSION_1_0 */
#define ORTE_QUERY(jobid)									orte_rds.query(jobid)
#define ORTE_TERMINATE_JOB(jobid,attr)						orte_pls.terminate_job(jobid, attr)
#define ORTE_SUBSCRIBE(jobid,cbfunc,cbdata,cond)			orte_smr.job_stage_gate_subscribe(jobid,cbfunc,cbdata,cond)
#define ORTE_PACK(buf,cmd,num,type)							orte_dss.pack(buf,cmd,num,type)
#define ORTE_GET_VPID_RANGE(jobid, start, range)			orte_rmgr.get_vpid_range(jobid, start, range)
#define ORTE_FREE_NAME(name)								free(name)
#define ORTE_KEYVALUE_TYPE(keyval)							(keyval)->value->type
#define ORTE_STD_CNTR_TYPE									orte_std_cntr_t
#endif /* ORTE_VERSION_1_0 */

#if ORTE_VERSION_1_0
static void 
ptp_ompi_wireup_stdin(orte_jobid_t jobid)
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
ptp_ompi_callback(orte_gpr_notify_data_t *data, void *cbdata)
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
					ptp_ompi_wireup_stdin(jobid);
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
ORTE_ALLOCATE_JOB(orte_app_context_t **apps, int num_apps, orte_jobid_t *jobid, void (*cbfunc)(orte_jobid_t, orte_proc_state_t))
{
	int						rc;
	orte_process_name_t *	name;

	/* 
	 * Initialize job segment and allocate resources
	 */

	if (ORTE_SUCCESS != (rc = ORTE_SETUP_JOB(apps,num_apps,jobid,NULL))) {
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
		rc = ORTE_SUBSCRIBE(*jobid, ptp_ompi_callback, (void*)cbfunc, ORTE_NOTIFY_ALL);
		if(ORTE_SUCCESS != rc) {
			ORTE_ERROR_LOG(rc);
			return rc;
		}
	}

	ORTE_FREE_NAME(name);
	
	return ORTE_SUCCESS;
}

static int get_num_procs(orte_jobid_t jobid);

static int
ORTE_GET_VPID_RANGE(orte_jobid_t jobid, int *start, int *range)
{
	*start = 0;
	*range = get_num_procs(jobid);
	return 0;
}
#else /* ORTE_VERSION_1_0 */
static int
ORTE_ALLOCATE_JOB(orte_app_context_t **apps, int num_apps, orte_jobid_t *jobid, void (*cbfunc)(orte_jobid_t, orte_proc_state_t))
{
	int			rc;
	opal_list_t	attr;
	uint8_t		flow;
	
	OBJ_CONSTRUCT(&attr, opal_list_t);
	
	flow = ORTE_RMGR_SETUP | ORTE_RMGR_RES_DISC | ORTE_RMGR_ALLOC | ORTE_RMGR_MAP | ORTE_RMGR_SETUP_TRIGS;
    orte_rmgr.add_attribute(&attr, ORTE_RMGR_SPAWN_FLOW, ORTE_UINT8, &flow, ORTE_RMGR_ATTR_OVERRIDE);
	
	rc = orte_rmgr.spawn_job(apps, num_apps, jobid, 0, NULL, cbfunc, ORTE_PROC_STATE_ALL, &attr);

	OBJ_DESTRUCT(&attr);
	
	if(rc != ORTE_SUCCESS) {
		ORTE_ERROR_LOG(rc);
	}
	
	return rc;
}

static int
ORTE_LAUNCH_JOB(orte_jobid_t jobid)
{
	int			rc;
	opal_list_t	attr;
	uint8_t		flow;
	
	OBJ_CONSTRUCT(&attr, opal_list_t);
	
	flow = ORTE_RMGR_LAUNCH;
    orte_rmgr.add_attribute(&attr, ORTE_RMGR_SPAWN_FLOW, ORTE_UINT8, &flow, ORTE_RMGR_ATTR_OVERRIDE);
	
	rc = orte_rmgr.spawn_job(NULL, 0, &jobid, 0, NULL, NULL, ORTE_PROC_STATE_ALL, &attr);

	OBJ_DESTRUCT(&attr);
	
	if(rc != ORTE_SUCCESS) {
		ORTE_ERROR_LOG(rc);
	}
	
	return rc;
}

static int 
ORTE_SPAWN(orte_app_context_t **apps, int num_apps, orte_jobid_t *jobid, void (*cbfunc)(orte_jobid_t, orte_proc_state_t))
{
	int			rc;
	opal_list_t	attr;
	
	OBJ_CONSTRUCT(&attr, opal_list_t);
	
	rc = orte_rmgr.spawn_job(apps, num_apps, jobid, 0, NULL, cbfunc, ORTE_PROC_STATE_ALL, &attr);
	
	OBJ_DESTRUCT(&attr);
	
	if(rc != ORTE_SUCCESS) {
		ORTE_ERROR_LOG(rc);
	}
	
	return rc;
}

static int
ORTE_GET_UINT32_VALUE(orte_gpr_keyval_t *keyval)
{
	int	tmp_int = 0;
	
    orte_dss.get((void **) &tmp_int, keyval->value, ORTE_UINT32);
		
	return tmp_int;
}

static char *
ORTE_GET_STRING_VALUE(orte_gpr_keyval_t *keyval)
{
	char *	tmp_str = NULL;
	
    orte_dss.get((void **) &tmp_str, keyval->value, ORTE_STRING);
		
	return tmp_str;
}

static int
ORTE_GET_PID_VALUE(orte_gpr_keyval_t *keyval)
{
	int	tmp_int = 0;
	
    orte_dss.get((void **) &tmp_int, keyval->value, ORTE_UINT32);
		
	return tmp_int;
}
#endif /* !ORTE_VERSION_1_0 */
