int
ORTECheckErrorCode(int type, int rc)
{
	if(rc != ORTE_SUCCESS) {
		GenerateEvent(type, ORTE_ERROR_NAME(rc));
		return 1;
	}
	
	return 0;
}

int
ORTEInit(void)
{
	int rc;
	
	/* this makes the orte_init() fail if the orte daemon isn't
	 * running */
	putenv("OMPI_MCA_orte_univ_exist=1");
	
	rc = orte_init(true);
	
	if(ORTECheckErrorCode(ORTE_TERMINATE_JOB, rc)) return 1;
	
	return 0;
}

/* this finalizes the registry */
int
ORTEFinalize(void)
{
	int rc;
	
	opal_mutex_lock(&opal_event_lock);
	rc = orte_finalize();
	opal_mutex_unlock(&opal_event_lock);
	
	if(ORTECheckErrorCode(ORTE_TERMINATE_JOB, rc)) return 1;
	
	return 0;
}

/* this tells the daemon to exit */
int
ORTEShutdown(void)
{
	ompi_sendcmd(ORTE_DAEMON_EXIT_CMD);
	return 0;
}

int
ORTEProgress(void)
{
	opal_mutex_lock(&opal_event_lock);
	opal_event_loop(0);
	opal_mutex_unlock(&opal_event_lock);
	
	return 0;
}

int
ORTETerminateJob(int jobid)
{
	int rc;
	
	rc = orte_rmgr.terminate_job(jobid);
	if(ORTECheckErrorCode(ORTE_TERMINATE_JOB, rc)) return 1;
	
	return 0;
}

int
ORTERun(char *exec_path, int num_procs)
{
	int rc; 
	
	opal_mutex_lock(&eclipse_orte_lock);
	rc = ptp_ompi_spawn(exec_path, num_procs);
	opal_mutex_unlock(&eclipse_orte_lock);
	
	if(ORTECheckErrorCode(ORTE_TERMINATE_JOB, rc)) return 1;
}

static int 
ptp_ompi_sendcmd(orte_daemon_cmd_flag_t usercmd)
{
	orte_buffer_t *cmd;
	orte_daemon_cmd_flag_t command;
	int rc;
	orte_process_name_t seed={0,0,0};

	cmd = OBJ_NEW(orte_buffer_t);
	if (NULL == cmd) {
		fprintf(stderr, "console: comm failure\n");
		return 1;
	}
	command = usercmd;

	if (ORTE_SUCCESS != (rc = orte_dps.pack(cmd, &command, 1, ORTE_DAEMON_CMD))) {
		ORTE_ERROR_LOG(rc);
		return 1;
	}
	if (0 > orte_rml.send_buffer(&seed, cmd, ORTE_RML_TAG_DAEMON, 0)) {
		ORTE_ERROR_LOG(ORTE_ERR_COMM_FAILURE);
		return 1;
	}
	OBJ_RELEASE(cmd);
	
	return 0;
}