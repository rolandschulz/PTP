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
#define ORTE_LAUNCH_JOB(jobid)								orte_pls.launch_job(jobid)
#define ORTE_TERMINATE_JOB(jobid,attr)						orte_pls.terminate_job(jobid, attr)
#define ORTE_SETUP_JOB(app_context,num_context,jobid,attr)	orte_rmgr.setup_job(app_context,num_context,jobid, attr)
#define ORTE_SUBSCRIBE(jobid,cbfunc,cbdata,cond)			orte_smr.job_stage_gate_subscribe(jobid,cbfunc,cbdata,cond)
#define ORTE_PACK(buf,cmd,num,type)							orte_dss.pack(buf,cmd,num,type)
#define ORTE_GET_VPID_RANGE(jobid, start, range)			orte_rmgr.get_vpid_range(jobid, start, range)
#define ORTE_FREE_NAME(name)								free(name)
#define ORTE_KEYVALUE_TYPE(keyval)							(keyval)->value->type
#define ORTE_NOTIFY_ALL										ORTE_PROC_STATE_ALL
#define ORTE_STD_CNTR_TYPE									orte_std_cntr_t
#ifndef ORTE_RML_NAME_SEED
#define ORTE_RML_NAME_SEED									ORTE_PROC_MY_HNP
#endif /* ORTE_RML_NAME_SEED */
#endif /* ORTE_VERSION_1_0 */

#if ORTE_VERSION_1_0
static int
ORTE_ALLOCATE_AND_MAP(orte_jobid_t jobid)
{
	int rc;
	
	if (ORTE_SUCCESS != (rc = orte_rmgr.allocate(jobid))) {
		ORTE_ERROR_LOG(rc);
		return rc;
	}
	
	if (ORTE_SUCCESS != (rc = orte_rmgr.map(jobid))) {
		ORTE_ERROR_LOG(rc);
		return rc;
	}
	
	return rc;
}

static int
ORTE_STAGE_GATE_INIT(orte_jobid_t jobid)
{
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
ORTE_ALLOCATE_AND_MAP(orte_jobid_t jobid)
{
	int			rc;
	opal_list_t attributes;
	
	OBJ_CONSTRUCT(&attributes, opal_list_t);
	
	if (ORTE_SUCCESS != (rc = orte_ras.allocate_job(jobid, &attributes))) {
		ORTE_ERROR_LOG(rc);
		goto cleanup;
	}
	
	if (ORTE_SUCCESS != (rc = orte_rmaps.map_job(jobid, &attributes))) {
		ORTE_ERROR_LOG(rc);
		goto cleanup;
	}
	
cleanup:
	OBJ_DESTRUCT(&attributes);
	
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

static int 
ORTE_STAGE_GATE_INIT(orte_jobid_t jobid)
{
    orte_buffer_t cmd;
    orte_buffer_t rsp;
    orte_std_cntr_t count;
    orte_rmgr_cmd_t command=ORTE_RMGR_SETUP_GATES_CMD;
    int rc;

    /* construct command */
    OBJ_CONSTRUCT(&cmd, orte_buffer_t);

    /* pack the command */
    if (ORTE_SUCCESS != (rc = orte_dss.pack(&cmd, &command, 1, ORTE_RMGR_CMD))) {
        ORTE_ERROR_LOG(rc);
        OBJ_DESTRUCT(&cmd);
        return rc;
    }

    /* pack the jobid */
    if(ORTE_SUCCESS != (rc = orte_dss.pack(&cmd, &jobid, 1, ORTE_JOBID))) {
        ORTE_ERROR_LOG(rc);
        OBJ_DESTRUCT(&cmd);
        return rc;
    }

    /* send the command */
    if(0 > (rc = orte_rml.send_buffer(ORTE_RML_NAME_SEED, &cmd, ORTE_RML_TAG_RMGR, 0))) {
        ORTE_ERROR_LOG(rc);
        OBJ_DESTRUCT(&cmd);
        return rc;
    }
    
    OBJ_DESTRUCT(&cmd);
    /* wait for response */
    
    OBJ_CONSTRUCT(&rsp, orte_buffer_t);
    if(0 > (rc = orte_rml.recv_buffer(ORTE_RML_NAME_SEED, &rsp, ORTE_RML_TAG_RMGR))) {
        ORTE_ERROR_LOG(rc);
        OBJ_DESTRUCT(&rsp);
        return rc;
    }

    /* get the returned command */
    count = 1;
    if (ORTE_SUCCESS != (rc = orte_dss.unpack(&rsp, &command, &count, ORTE_RMGR_CMD))) {
        ORTE_ERROR_LOG(rc);
        OBJ_DESTRUCT(&rsp);
        return rc;
    }
    /* and check it to ensure valid comm */
    if (ORTE_RMGR_SETUP_GATES_CMD != command) {
        OBJ_DESTRUCT(&rsp);
        ORTE_ERROR_LOG(ORTE_ERR_COMM_FAILURE);
        return ORTE_ERR_COMM_FAILURE;    
    }
        
    OBJ_DESTRUCT(&rsp);
    return rc; 
} 
#endif /* !ORTE_VERSION_1_0 */
