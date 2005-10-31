#include <getopt.h>
#include <proxy.h>
#include <proxy_tcp.h>
#include <handler.h>

#define DEFAULT_PROXY		"tcp"

#define RTEV_OFFSET		200
#define RTEV_OK			RTEV_OFFSET + 0
#define RTEV_ERROR		RTEV_OFFSET + 1
#define RTEV_JBSTATE		RTEV_OFFSET + 2
#define RTEV_JOBS		RTEV_OFFSET + 3;
#define RTEV_PROCS		RTEV_OFFSET + 4;
#define RTEV_PROCATTR		RTEV_OFFSET + 5;
#define RTEV_MACHINES		RTEV_OFFSET + 6;
#define RTEV_NODES		RTEV_OFFSET + 7;
#define RTEV_NODEATTR		RTEV_OFFSET + 8;
#define RTEV_MACHID		RTEV_OFFSET + 9;

int ORTEIsShutdown(void);

int ORTERun(char **);
int OMPIGetJobs(char **);
int OMPIGetProcesses(char **);
int OMPIGetProcessAttribute(char **);
int OMPIGetMachines(char **);
int OMPIGetNodes(char **);
int OMPIGetNodeAttribute(char **);
int OMPIGetNodemachineID(char **);
int OMPIQuit(void);

int 			shutdown = 0;
proxy_svr *	proxy;

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
	OMPIQuit					// quit() - called when quit message received
};

static proxy_svr_commands command_tab[] = { 
	{"RUN", 		ORTERun},
	{"GETJOBS", 	OMPIGetJobs},
	{"GETPROCS",	OMPIGetProcesses},
	{"GETPATTR",	OMPIGetProcessAttribute},
	{"GETMACHS",	OMPIGetMachines},
	{"GETNODES",	OMPIGetNodes},
	{"GETNATTR",	OMPIGetNodeAttribute},
	{"GETNMID",	OMPIGetNodemachineID},
	{NULL,		NULL}
};

static struct option longopts[] = {
	{"proxy",			required_argument,	NULL, 	'P'}, 
	{"port",				required_argument,	NULL, 	'p'}, 
	{"host",				required_argument,	NULL, 	'h'}, 
	{NULL,				0,					NULL,	0}
};

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
	
	if(ORTECheckErrorCode(ORTE_INIT, rc)) return 1;
	
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
	
	if(ORTECheckErrorCode(ORTE_FINALIZE, rc)) return 1;
	
	return 0;
}

/* this tells the daemon to exit */
int
ORTEShutdown(void)
{
	ompi_sendcmd(ORTE_DAEMON_EXIT_CMD);
	shutdown++;
	return 0;
}

int
ORTEIsShutdown(void)
{
	return shutdown != 0;
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
	struct timeval	tv;
	handler *		h;

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
	
	opal_mutex_lock(&opal_event_lock);
	opal_event_loop(0);
	opal_mutex_unlock(&opal_event_lock);
	
	return PROXY_RES_OK;
}

/* terminate a job, given a jobid
 * EVENT RETURN:
 *   type = TERMINATE_JOB
 *   data = "success" ?
int
ORTETerminateJob(int jobid)
{
	int rc;
	
	rc = orte_rmgr.terminate_job(jobid);
	if(ORTECheckErrorCode(ORTE_TERMINATE_JOB, rc)) return 1;
	
	return 0;
}

/* spawn a job with the given executable path and # of procs.
 * EVENT RETURN:
 *   type = RUN
 *   data = "jobid" (example: "848")
 */
int
ORTERun(char **args)
{
	int rc; 
	
	int i;
	orte_jobid_t jobid = ORTE_JOBID_MAX;
	char pgm_name[128], cwd[128];
	char *c;
	orte_app_context_t **apps;
	int num_apps;
	char *exec_path = args[1];
	int num_procs = atoi(args[2]);

	c = rindex(app, '/');

	strncpy(pgm_name, c + 1, strlen(c));
	strncpy(cwd, app, c - app + 1);
	cwd[c-app+1] = '\0';

	/* hard coded test for spawning just 1 job (JOB not PROCESSES!) */
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

	/*
	printf("Spawning %d processes of job '%s'\n", apps[0]->num_procs, apps[0]->app);
	fflush(stdout);
	*/
	
	opal_mutex_lock(&opal_event_lock);
	/* calls the ORTE spawn function with the app to spawn.  Return the
	 * jobid assigned by the registry/ORTE.  Passes a callback function
	 * that ORTE will call with state change on this job */
	rc = orte_rmgr.spawn(apps, num_apps, &jobid, job_state_callback);
	opal_mutex_unlock(&opal_event_lock);
	
	
	
	if(ORTECheckErrorCode(ORTE_RUN, rc)) return 1;
	
	/* generate an event stating what the new/assigned job ID is.
	 * The caller must record this and use this as an identifier to get
	 * information about a job */
	GenerateEvent(ORTE_RUN, jobid);
	
	return PROXY_RES_OK;
}

static void
job_state_callback(orte_jobid_t jobid, orte_proc_state_t state)
{
	char *	res;
	
	/* not sure yet how we want to handle this callback, what events
	 * we want to generate, but here are the states that I know of
	 * that a job can go through.  I've watched ORTE call this callback
	 * with each of these states.  We'll want to come in here and
	 * generate events where appropriate */
	
	printf("job_state_callback(%d)\n", jobid);
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

	asprintf(res, "%d %d", RTEV_JOBSTATE, state);
	proxy_svr_event_callback(proxy, res);
	free(res);
}

/* this is an internal function we'll call from within this, consider
 * it 'private' */
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
int
OMPIGetJobs(char **args)
{
	char *	res;
	char *	str;
	int		n;
	int *	jobs
	
	n = get_jobs(&jobs);
	intarray_to_str(n, jobs, &str);
	
	asprintf(&res, "%d %s", RTEV_JOBS, str);
	proxy_svr_event_callback(proxy, res);
	
	free(str);
	free(res);
	
	return PROXY_RES_OK;
}

/* given a jobid (valid from OMPIGetJobs()) we request a list of the process
 * IDs associated with that job.  this will return an event with a list of
 * processIDs 
 * EVENT RETURN:
 *   type = GET_PROCESSES
 *   data = [85,86,87,88] <-- those are process IDs
 * 
 * NOTE: for processes we must return a bitset, since there may be vary many. This
 * means that we may need to map between internal/external representation of
 * process id's (if they don't start with 0).
 */
int 
OMPIGetProcesses(char **args)
{
	int				jobid;
	bitset *			procs;
	char *			pstr;
	char *			res;
	
	jobid = atoi(args[1]);
	procs = get_procs(jobid);
	
	if (procs == NULL) {
		asprintf(&res, "%d %s", RTEV_ERROR, "no such jobid");
	} else {
		pstr = bitset_to_str(procs);
		asprintf(res, "%d %s", RTEV_PROCS, pstr);
		free(pstr);
	}
	
	proxy_svr_event_callback(proxy, res);
	
	free(res);
	
	return PROXY_RES_OK;
}

/* given a processID (associated with some jobID, but that part is implied)
 * we request the value of an attribute.  the attribute keys are defined
 * elsewhere but an example might be ATTRIB_PROCESS_STATE, ATTRIB_PROCESS_NODE
 * which would be the state of the process (starting, running, terminated,
 * exited, etc) and the nodeID of the node the process is running on, 
 * respectively 
 * EVENT RETURN:
 *   type = GET_NODE_ATTRIBUTE
 *   data = "key=value" (example "state=running", "node=54" <-- 54 = node ID, not necessary node number)
 */
int
OMPIGetProcessAttribute(char **args)
{
	int		procid = atoi(args[1]);
	char *	key = args[2];
	char *	val;
	
	val = get_proc_attribute(procid, key);
	
	if (val == NULL) {
		asprintf(&res, "%d %s", RTEV_ERROR, "no such attribute");
	} else {
		asprintf(res, "%d %s=%s", RTEV_PROCATTR, key, val);
	}
	
	proxy_svr_event_callback(proxy, res);
	
	free(res);

	return PROXY_RES_OK;
}

/* MONITORING RELATED FUNCTIONS */

/* generates an event which returns a list of all the machineIDs know to
 * the universe.
 * EVENT RETURN:
 *   type = GET_MACHINES
 *   data = [2,3,4,5] <--- those are machine IDs
 * 
 * NOTE: we assume there are unlikely to be many machines,
 * so we can return this as a list of id's.
 */
int
OMPIGetMachines(char **args)
{
	char *	res;
	char *	str;
	int		n;
	int *	machines
	
	n = get_machines(&machines);
	intarray_to_str(n, machines, &res);
	
	asprintf(&res, "%d %s", RTEV_MACHINES, str);
	proxy_svr_event_callback(proxy, res);
	
	free(str);
	free(res);

	return PROXY_RES_OK;
}

/* given a machine ID, this generates an event which contains a list of
 * all the nodeIDs associated with the given machineID
 * EVENT RETURN:
 *   type = GET_NODES
 *   data = [10,11,12,13,14,15] <-- those are node IDs
 * 
 * NOTE: for nodes we must return a bitset, since there may be vary many. This
 * means that we may need to map between internal/external representation of
 * nodes id's (if they don't start with 0).
 */
int
OMPIGetNodes(char **args)
{	
	int				mid;
	bitset *			nodes;
	char *			pstr;
	char *			res;
	
	mid = atoi(args[1]);
	nodes = get_nodes(mid);
	
	if (nodes == NULL) {
		asprintf(&res, "%d %s", RTEV_ERROR, "no such jobid");
	} else {
		pstr = bitset_to_str(nodes);
		asprintf(res, "%d %s", RTEV_NODES, pstr);
		free(pstr);
	}
	
	proxy_svr_event_callback(proxy, res);
	
	free(res);
	return PROXY_RES_OK;
}

/* given a nodeid and an attribute key this generates an event with
 * the attribute's value.  sample attributes might be ATTRIB_NODE_STATE
 * or ATTRIB_NODE_OS which might return up, down, booting, or error and
 * linux, solaris, windows, osx, etc respectively
 * EVENT RETURN:
 *   type = GET_NODE_ATTRIBUTE
 *   data = "key=value" (example "state=down", "user=ndebard")
 */
int
OMPIGetNodeAttribute(char **args)
{
	int		nodeid = atoi(args[1]);
	char *	key = args[2];
	char *	val;
	
	val = get_node_attribute(nodeid, key);
	
	if (val == NULL) {
		asprintf(&res, "%d %s", RTEV_ERROR, "no such attribute");
	} else {
		asprintf(res, "%d %s=%s", RTEV_NODEATTR, key, val);
	}
	
	proxy_svr_event_callback(proxy, res);
	
	free(res);	
	return PROXY_RES_OK;
}

/* perhaps we don't need this function.
 * this takes a given nodeid and determines the machineID that it is
 * contained within.  in theory this can be inferred by calling getMachines
 * and getNodes(some_machine_id).  once the model is populated, we should
 * have a relationship between the two but this MAY be helpful ... really
 * it depends on whether we want helper functions at this level or up at
 * the Java level - probably in my opinion we want them at the Java level 
 * EVENT RETURN:
 *   type = GET_NODE_MACHINE_ID
 *   data = "nodeid=machineid" (example node 1000 owned by machine 4 -> "1000=4")
 */ 
int
OMPIGetNodeMachineID(char **args)
{
	int nodeid = atoi(args[1]);
	
	return PROXY_RES_OK;
}

int
OMPIQuit(void)
{
	return PROXY_RES_OK;
}

void
server(char *name)
{
	if (proxy_svr_init(name, handler_funcs, helper_funcs, command_tab, &proxy) != PROXY_RES_OK)
		return;
	
	proxy_svr_connect(proxy, host, port);
	
	for (;;) {
		if (ORTEProgress() != PROXY_RES_OK ||
			proxy_svr_progress(proxy) != PROXY_RES_OK)
			break;
	}
	
	proxy_svr_finish(proxy);
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
	
	if (!ORTEInit()) {
		fprintf(stderr, "Faild to initialize ORTE\n");
		return 1;
	}
	
	server(proxy_str);
	
	ORTEFinalize();
	
	return 0;
}