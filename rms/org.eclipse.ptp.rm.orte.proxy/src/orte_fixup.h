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

#include "opal/util/output.h"
#include "opal/util/path.h"
#include "opal/event/event.h"
#include "opal/threads/condition.h"
#include "orte/orte_constants.h"
#include "orte/tools/orted/orted.h"

#include "orte/mca/iof/iof.h"
#include "orte/mca/rmgr/rmgr.h"
#include "orte/mca/errmgr/errmgr.h"
#include "orte/mca/rml/rml.h"
#include "orte/mca/gpr/gpr.h"
#include "orte/mca/pls/pls.h"
#include "orte/mca/rds/rds.h"
#include "orte/mca/ras/ras.h"
#include "orte/mca/rmaps/rmaps.h"
#include "orte/mca/smr/smr.h"
#include "orte/mca/rmgr/base/rmgr_private.h"
#include "orte/mca/odls/odls.h"

#ifdef HAVE_SYS_BPROC_H
#include "orte/mca/soh/bproc/soh_bproc.h"
#endif

#include "orte/runtime/runtime.h"
#include "orte/runtime/params.h"

#define ORTE_QUERY(jobid)									orte_rds.query(jobid)
#define ORTE_SUBSCRIBE(jobid,cbfunc,cbdata,cond)			orte_smr.job_stage_gate_subscribe(jobid,cbfunc,cbdata,cond)
#define ORTE_PACK(buf,cmd,num,type)							orte_dss.pack(buf,cmd,num,type)
#define ORTE_GET_VPID_RANGE(jobid, start, range)			orte_rmgr.get_vpid_range(jobid, start, range)
#define ORTE_FREE_NAME(name)								free(name)
#define ORTE_KEYVALUE_TYPE(keyval)							(keyval)->value->type
#define ORTE_STD_CNTR_TYPE									orte_std_cntr_t

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
ORTE_TERMINATE_JOB(orte_jobid_t jobid)
{
	int					rc;
	opal_list_t			attrs;
	opal_list_item_t *	item;
	
	OBJ_CONSTRUCT(&attrs, opal_list_t);
	
	orte_rmgr.add_attribute(&attrs, ORTE_NS_INCLUDE_DESCENDANTS, ORTE_UNDEF, NULL, ORTE_RMGR_ATTR_OVERRIDE);
	
	rc = orte_pls.terminate_job(jobid, &orte_abort_timeout, &attrs);
	
    while ((item = opal_list_remove_first(&attrs)) != NULL) {
          OBJ_RELEASE(item);
    }
	OBJ_DESTRUCT(&attrs);
	
	if(rc != ORTE_SUCCESS) {
		ORTE_ERROR_LOG(rc);
	}
	
	return rc;
}

static int
ORTE_TERMINATE_ORTEDS(orte_jobid_t jobid)
{
	int					rc;
	opal_list_t			attrs;
	opal_list_item_t *	item;
   
	OBJ_CONSTRUCT(&attrs, opal_list_t);
	
	orte_rmgr.add_attribute(&attrs, ORTE_NS_INCLUDE_DESCENDANTS, ORTE_UNDEF, NULL, ORTE_RMGR_ATTR_OVERRIDE);
	
	rc = orte_pls.terminate_orteds(jobid, &orte_abort_timeout, &attrs);

    while ((item = opal_list_remove_first(&attrs)) != NULL) {
          OBJ_RELEASE(item);
    }
	OBJ_DESTRUCT(&attrs);
	
	if(rc != ORTE_SUCCESS) {
		ORTE_ERROR_LOG(rc);
	}
	
	return rc;
}

static char *
ORTE_GET_STRING_VALUE(orte_gpr_keyval_t *keyval)
{
	char *	tmp_str = NULL;
	
    orte_dss.get((void **) &tmp_str, keyval->value, ORTE_STRING);
		
	return tmp_str;
}

/*
 * Send an exit command to the ORTE daemon.
 * 
 */
static int 
ORTE_SHUTDOWN(void)
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

    command = ORTE_DAEMON_EXIT_CMD;

    rc = ORTE_PACK(cmd, &command, 1, ORTE_DAEMON_CMD);
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
