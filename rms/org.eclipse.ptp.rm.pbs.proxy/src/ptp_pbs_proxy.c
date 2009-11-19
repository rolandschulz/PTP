/*
 * Copyright (c) 2009 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
#ifdef __gnu_linux__
#define _GNU_SOURCE
#endif /* __gnu_linux__ */

#include "config.h"

#include <getopt.h>
#include <unistd.h>
#include <stdbool.h>
#include <errno.h>
#include <signal.h>
#include <stdlib.h>
#include <string.h>
#include <stdio.h>
#include <pwd.h>
#include <regex.h>

#include <sys/types.h>
#include <sys/wait.h>
#include <sys/select.h>

#include "proxy.h"
#include "proxy_tcp.h"
#include "handler.h"
#include "list.h"
#include "hash.h"
#include "args.h"
#include "rangeset.h"
#include "pbs_ifl.h"

#define WIRE_PROTOCOL_VERSION	"2.0"

/*
 * PBS Resources
 *
 * arch			string		System architecture
 * cput			time		Maximum, aggregate CPU time required by all processes
 * file			size		Maximum disk space requirements for any single file to be created
 * host			string		Name of requested host/node
 * mem			size		Maximum amount of physical memory (RAM)
 * mpiprocs		int			Number of MPI processes for this chunk
 * ncpus		int			Number of CPUs (processors)
 * nice			int			Requested job priority
 * nodes		string		Number and/or type of nodes
 * nodect		int			Number of chunks in resource request from selection directive, or number of vnodes requested from node specification
 * ompthreads	int			Number of OpenMP threads for this chunk.
 * pcput		time		Per-process maximum CPU time
 * pmem			size		Per-process maximum amount of physical memory
 * pvmem		size		Per-process maximum amount of virtual memory
 * resc			string		Single-node variable resource specification string
 * vmem			size		Maximum, aggregate amount of virtual memory used by all concurrent processes
 * walltime		time		Maximum amount of real time (wall-clock elapsed time)
 * mppe			int			The number of processing elements used by a single process
 * mppt			time		Maximum wallclock time used on the MPP.
 * pf			size		Maximum number of file system blocks that can be used by all process
 * pmppt		time		Maximum amount of wall clock time used on the MPP by a single process
 * pncpus		int			Maximum number of processors used by any single process
 * ppf			size		Maximum number of file system blocks that can be used by a single process
 * procs		int			Maximum number of processes
 * psds			size		Maximum number of data blocks on the SDS (secondary data storage) for any process
 * sds			size		Maximum number of data blocks on the SDS (secondary data storage)
 *
 * Job Attributes
 *
 * Account_Name
 * Checkpoint
 * depend
 * Error_Path
 * Execution_Time
 * group_list
 * Hold_Types
 * Job_Name
 * Join_Path
 * Keep_Files
 * Mail_Points
 * Mail_Users
 * no_stdio_sockets
 * Output_Path
 * Priority
 * Rerunnable
 * Resource_List[.resource]
 * Shell_Path_List
 * stagein
 * stageout
 * umask
 * User_List
 * Variable_List
 * comment
 *
 * Read-only Job Attributes
 *
 * accounting_id
 * alt_id
 * array
 * array_id
 * array_index
 * array_indices_remaining
 * array_indices_submitted
 * array_state_count
 * ctime
 * etime
 * exec_host
 * egroup
 * euser
 * hashname
 * interactive
 * Job_Owner
 * job_state
 * mtime
 * qtime
 * queue
 * resources_used
 * run_count
 * schedselect
 * server
 * session_id
 *
 * Queue Attributes
 * acl_groups			string	""		The list of groups which may submit jobs to the queue
 * acl_group_enable 	boolean	false	Only allow jobs submitted from groups specified by the acl_groups parameter
 * acl_group_sloppy		boolean	false	acl_groups will be checked against all groups of which the job user is a member
 * acl_hosts			string	""		List of hosts that may submit jobs to the queue
 * acl_host_enable		boolean	false	Only allow jobs submitted from hosts specified by the acl_hosts parameter
 * acl_logic_or			boolean	false	User and group acls are logically OR'd together
 * acl_users			string	""		The list of users who may submit jobs to the queue
 * acl_user_enable		boolean	false	Only allow jobs submitted from users specified by the acl_users parameter
 * disallowed_types		string	""		List of job "types" that are not allowed in this queue
 * enabled				boolean	false 	The queue accepts new job submissions
 * keep_completed		integer	0		The number of seconds jobs should be held in the Completed state after exiting
 * kill_delay			integer	2		The number of seconds between sending a SIGTERM and a SIGKILL to a job being cancelled
 * max_queuable			integer	+INF	The maximum number of jobs allowed in the queue at any given time
 * max_running			integer	+INF	The maximum number of jobs in the queue allowed to run at any given time
 * max_user_queuable	integer	+INF	The maximum number of jobs, per user, allowed in the queue at any given time
 * max_user_run			integer	+INF	The maximum number of jobs, per user, in the queue allowed to run at any given time
 * priority				integer	+INF	The priority value associated with the queue.  DEFAULT: 0	qmgr -c "set queue batch priority=20"
 * queue_type			enum	e	 	The queue type (e=execution, r=route)
 * resources_available	string	""		The cumulative resources available to all jobs running in the queue
 * resources_default	string	""		Default resource requirements for jobs submitted to the queue
 * resources_max		string	""		The maximum resource limits for jobs submitted to the queue
 * resources_min		string	""		The minimum resource limits for jobs submitted to the queue
 * route_destinations	string	""		The potential destination queues for jobs submitted to the associated routing queue
 * started				boolean	false	Jobs in the queue are allowed to execute
 */
/*
 * Proxy server states. The SHUTTING_DOWN state is used to
 * give the proxy a chance to send any pending events once
 * a QUIT command has been received.
 */
#define STATE_INIT			0
#define STATE_RUNNING		1
#define STATE_SHUTTING_DOWN	2
#define STATE_SHUTDOWN		3

/*
 * RTEV codes must EXACTLY match org.eclipse.ptp.rtsystem.proxy.event.IProxyRuntimeEvent
 */
#define RTEV_OFFSET						200

/*
 * RTEV_ERROR codes are used internally in the PBS specific plugin
 */
#define RTEV_ERROR_INIT			RTEV_OFFSET + 1000
#define RTEV_ERROR_FINALIZE		RTEV_OFFSET + 1001
#define RTEV_ERROR_SUBMIT		RTEV_OFFSET + 1002
#define RTEV_ERROR_JOB			RTEV_OFFSET + 1003
#define RTEV_ERROR_SERVER		RTEV_OFFSET + 1004
#define RTEV_ERROR_NATTR		RTEV_OFFSET + 1007
#define RTEV_ERROR_SIGNAL		RTEV_OFFSET + 1009
#define RTEV_ERROR_FILTER		RTEV_OFFSET + 1010
#define RTEV_ERROR_START_EVENTS	RTEV_OFFSET + 1011

#define JOB_NAME_FMT			"job%02d"
#define PBS_QUEUE_ATTR			"queue"
#define PBS_POLL_INTERVAL		60000000 /* 60 seconds */
#define PROXY_TIMEOUT			20000	 /* 20 ms */

#define DEFAULT_HASH_SIZE				8192
#define DEFAULT_FILTER_HASH_SIZE		32
#define DEFAULT_FILTER_ATTR_HASH_SIZE	32
#define DEFAULT_HOST					"localhost"
#define DEFAULT_PROXY					"tcp"

int PBS_Initialize(int, int, char **);
int PBS_ModelDef(int, int, char **);
int PBS_StartEvents(int, int, char **);
int PBS_StopEvents(int, int, char **);
int PBS_SubmitJob(int, int, char **);
int PBS_TerminateJob(int, int, char **);
int PBS_Quit(int, int, char **);
int PBS_FilterEvents(int, int, char **);

enum job_state {
	JOB_CREATED,
	JOB_NORMAL,
	JOB_TERMINATING,
	JOB_TERMINATED
};
typedef enum job_state	job_state;

struct ptp_machine {
	int		id;
	char *	name;
	List *	nodes;
};
typedef struct ptp_machine	ptp_machine;

struct ptp_node {
	int	 	id;
	int		number;
	char *	name;
	char *	state;
	char *	user;
	char *	group;
	char *	mode;
};
typedef struct ptp_node	ptp_node;

struct ptp_process {
	int		id;
	int		node_id;
	int		task_id;
	int		pid;
};
typedef struct ptp_process	ptp_process;

struct ptp_queue {
	int		id;
	char *	name;
};
typedef struct ptp_queue ptp_queue;

struct ptp_job {
	int 			ptp_jobid;		/* job ID as known by PTP */
	char * 			pbs_jobid;		/* PBS job ID */
	char *			jobsubid;		/* submission ID of job */
	ptp_queue *		queue;			/* queue this job is in */
	int				num_procs;		/* number of procs requested for program (debugger uses num_procs+1) */
	bool			debug;			/* job is debug job */
	job_state		state;			/* job state */
	bool			iof;			/* job has i/o forwarding */
	ptp_process **	procs;			/* procs for this job */
	rangeset *		set;			/* range set of proc IDs */
};
typedef struct ptp_job ptp_job;

struct ptp_filter {
	Hash *	hash;		/* attributes to filter */
	int		num_attrs;	/* number of attributes in hash */
	bool	children;	/* apply filter to children */
};
typedef struct ptp_filter	ptp_filter;

static int			gTransID = 0; /* transaction id for start of event stream, is 0 when events are off */
static int			gBaseID = -1; /* base ID for event generation */
static int			gLastID = 1; /* ID generator */
static int 			proxy_state = STATE_INIT;
static proxy_svr *	conn;
static List *		gJobList;
static Hash *		gJobHash;
static Hash *		gFilters;
static List *		gMachineList;
static List *		gQueueList;
static int			ptp_signal_exit;
static int			debug_level = 0; /* 0 is off */
static RETSIGTYPE	(*saved_signals[NSIG])(int);
static int			stream;
static char *		gUserName;

extern char *		pbs_server;

static proxy_svr_helper_funcs helper_funcs = {
	NULL,					// newconn() - can be used to reject connections
	NULL					// numservers() - if there are multiple servers, return the number
};

#define CMD_BASE	0

static proxy_cmd	cmds[] = {
	PBS_Quit,
	PBS_Initialize,
	PBS_ModelDef,
	PBS_StartEvents,
	PBS_StopEvents,
	PBS_SubmitJob,
	PBS_TerminateJob,
	PBS_FilterEvents
};

static proxy_commands command_tab = {
	CMD_BASE,
	sizeof(cmds)/sizeof(proxy_cmd),
	cmds
};

static struct option longopts[] = {
	{"proxy",			required_argument,	NULL, 	'P'}, 
	{"port",			required_argument,	NULL, 	'p'}, 
	{"host",			required_argument,	NULL, 	'h'}, 
	{"debug",			required_argument,	NULL, 	'd'}, 
	{NULL,				0,					NULL,	0}
};

/*
 * Generate a model element ID
 */
static int
generate_id(void)
{
	return gBaseID + gLastID++;
}

/*
 * Create a new machine.
 */
static ptp_machine *
new_machine(char *name)
{
	ptp_machine *	m = (ptp_machine *)malloc(sizeof(ptp_machine));
	m->id = generate_id();
	m->name = strdup(name);
	m->nodes = NewList();
    AddToList(gMachineList, (void *)m);
    return m;
}

static void
free_machine(ptp_machine *m)
{
	RemoveFromList(gMachineList, (void *)m);
	free(m->name);
	free(m);
}

/*
 * Create a new node.
 */
static ptp_node *
new_node(ptp_machine *mach, char *name, char *state, char *user, char *group, char *mode)
{
	static int node_number = 0;
	ptp_node *	n = (ptp_node *)malloc(sizeof(ptp_node));
	
	memset((char *)n, 0, sizeof(ptp_node));
	n->id = generate_id();
	n->number = node_number++;
	if (name != NULL)
		n->name = strdup(name);
	if (state != NULL)
		n->state = strdup(state);
	if (user != NULL)
		n->user = strdup(user);
	if (group != NULL)
		n->group = strdup(group);
	if (mode != NULL)
		n->mode = strdup(mode);
    AddToList(mach->nodes, (void *)n);
    return n;
}

/*
 * Very expensive!
 */
static ptp_node *
find_node_by_name(char *name)
{
	ptp_machine *	m;
	ptp_node *		n;
	
	for (SetList(gMachineList); (m = (ptp_machine *)GetListElement(gMachineList)) != NULL; ) {
		for (SetList(m->nodes); (n = (ptp_node *)GetListElement(m->nodes)) != NULL; ) {
			if (strcmp(name, n->name) == 0)
				return n;
		}
	}
	
	return NULL;
}

/*
 * Create a new process.
 */
static ptp_process *
new_process(ptp_job *job, int node_id, int task_id, int pid)
{
	ptp_process *	p = (ptp_process *)malloc(sizeof(ptp_process));
	p->id = generate_id();
	p->node_id = node_id;
	p->task_id = task_id;
	p->pid = pid;
    job->procs[task_id] = p;
    insert_in_rangeset(job->set, p->id);
    return p;
}

static void
free_process(ptp_process *p)
{
	free(p);
}

static ptp_process *
find_process(ptp_job *job, int task_id)
{
	if (task_id < 0 || task_id >= job->num_procs)
		return NULL;

	return job->procs[task_id];
}

static ptp_queue *
new_queue(char *name) {
	ptp_queue *	q = (ptp_queue *)malloc(sizeof(ptp_queue));
	q->id = generate_id();
	q->name = strdup(name);
	AddToList(gQueueList, (void *)q);
	return q;
}

static void
free_queue(ptp_queue *q)
{
	free(q->name);
	free(q);
}

static ptp_queue *
find_queue_by_id(int id)
{
	ptp_queue *	q;

	for (SetList(gQueueList); (q = (ptp_queue *)GetListElement(gQueueList)) != NULL; ) {
		if (q->id == id) {
			return q;
		}
	}
	return NULL;
}

static ptp_queue *
find_queue_by_name(char *name)
{
	ptp_queue *	q;

	for (SetList(gQueueList); (q = (ptp_queue *)GetListElement(gQueueList)) != NULL; ) {
		if (strcmp(q->name, name) == 0) {
			return q;
		}
	}
	return NULL;
}

/*
 * Find the name of the queue from the PBS job attributes
 */
static char *
find_pbs_queue_name(struct attrl *attrs)
{
	struct attrl *attr;

	for (attr = attrs; attr != NULL; attr = attr->next) {
		if (strcmp(attr->name, PBS_QUEUE_ATTR) == 0) {
			return attr->value;
		}
	}

	return NULL;
}

/*
 * Keep a list of the jobs that we have created. If they are
 * debug jobs, keep the debug jobid as well.
 */
static ptp_job *
new_job(int num_procs, bool debug, ptp_queue *queue, char *jobsubid, char *pbs_jobid)
{
	ptp_job *	j = (ptp_job *)malloc(sizeof(ptp_job));
	j->queue = queue;
	j->ptp_jobid = generate_id();
	j->pbs_jobid = strdup(pbs_jobid);
	j->jobsubid = NULL;
	if (jobsubid != NULL) {
		j->jobsubid = strdup(jobsubid);
	}
    j->num_procs = 0;
    j->procs = NULL;
    j->debug = debug;
    j->state = JOB_CREATED;
    j->iof = false;
    j->set = new_rangeset();
    //j->procs = (ptp_process **)malloc(sizeof(ptp_process *) * num_procs);
    //memset(j->procs, 0, sizeof(ptp_process *) * num_procs);
    AddToList(gJobList, (void *)j);
    HashInsert(gJobHash, HashCompute(pbs_jobid, strlen(pbs_jobid)), (void *)j);
    return j;
}

static void
free_job(ptp_job *j)
{
	int	i;
	
	RemoveFromList(gJobList, (void *)j);
	HashRemove(gJobHash, HashCompute(j->pbs_jobid, strlen(j->pbs_jobid)));
	free(j->pbs_jobid);
	free(j->jobsubid);
	for (i = 0; i < j->num_procs; i++) {
		if (j->procs[i] != NULL)
			free_process(j->procs[i]);
	}
	if (j->procs != NULL) {
		free(j->procs);
	}
	free_rangeset(j->set);
	free(j);
}

/*
 * Find a job on the list using the PTP job ID
 */
static ptp_job *
find_job_by_id(int jobid)
{
	ptp_job *	j;
	
	for (SetList(gJobList); (j = (ptp_job *)GetListElement(gJobList)) != NULL; ) {
		if (j->ptp_jobid == jobid) {
			return j;
		}
	}
	return NULL;
}

static int
get_pbs_attr_count(struct attrl *attrs)
{
	int 			count = 0;
	struct attrl *	attr;

	for (attr = attrs; attr != NULL; attr = attr->next) {
		count++;
	}

	return count;
}

static void
add_pbs_attributes(proxy_msg *m, struct attrl *attrs)
{
	struct attrl *attr;

	for (attr = attrs; attr != NULL; attr = attr->next) {
		proxy_add_string_attribute(m, attr->name, attr->value);
	}
}

static void
shutdown_pbs()
{
	pbs_disconnect(stream);
}

static void
sendOKEvent(int trans_id)
{
	proxy_svr_queue_msg(conn, proxy_ok_event(trans_id));
}

static void
sendShutdownEvent(int trans_id)
{
	proxy_svr_queue_msg(conn, proxy_shutdown_event(trans_id));
}

static void
sendMessageEvent(int trans_id, char *level, int code, char *fmt, ...)
{
	va_list		ap;

	va_start(ap, fmt);
	proxy_svr_queue_msg(conn, proxy_message_event(trans_id, level, code, fmt, ap));
	va_end(ap);
}

static void
sendErrorEvent(int trans_id, int code, char *fmt, ...)
{
	va_list		ap;

	va_start(ap, fmt);
	if (debug_level > 0) {
		fprintf(stderr, "sendErrorEvent(%d, %d, ", trans_id, code);
		vfprintf(stderr, fmt, ap);
		fprintf(stderr, ")\n");
		fflush(stderr);
	}
	proxy_svr_queue_msg(conn, proxy_error_event(trans_id, code, fmt, ap));
	va_end(ap);
}

static void
sendJobSubErrorEvent(int trans_id, char *jobSubId, char *msg)
{
	proxy_svr_queue_msg(conn, proxy_submitjob_error_event(trans_id, jobSubId, RTEV_ERROR_SUBMIT, msg));
}

static void
sendJobTerminateErrorEvent(int trans_id, int id, char *msg)
{
	char *	job_id;
	
	asprintf(&job_id, "%d", id);
	
	proxy_svr_queue_msg(conn, proxy_terminatejob_error_event(trans_id, job_id, RTEV_ERROR_JOB, msg));
}

static void
sendNewMachineEvent(int trans_id, int id, char *name)
{
	char *	rm_id;
	char *	machine_id;
	
	asprintf(&rm_id, "%d", gBaseID);	
	asprintf(&machine_id, "%d", id);	
	
	proxy_svr_queue_msg(conn, proxy_new_machine_event(trans_id, rm_id, machine_id, name, PTP_MACHINE_STATE_UP));
	
	free(machine_id);
	free(rm_id);
}

static void
sendNewQueueEvent(int trans_id, int id, char *name, struct attrl *attrs)
{
	char *		rm_id;
	char *		queue_id;
	proxy_msg *	m;

	asprintf(&rm_id, "%d", gBaseID);
	asprintf(&queue_id, "%d", id);

	m = proxy_new_queue_event(trans_id, rm_id, queue_id, name, PTP_QUEUE_STATE_NORMAL, get_pbs_attr_count(attrs));
	add_pbs_attributes(m, attrs);
	proxy_svr_queue_msg(conn, m);

	free(queue_id);
	free(rm_id);
}

static int
num_node_attrs(ptp_node *node)
{
	int	cnt = 0;
	if (node->number >= 0)
		cnt++;
	return cnt;	
}

/*
 * NOTE: sending a NODE_NUMBER_ATTR will enable the node number ruler in the machines view.
 */
static void
add_node_attrs(proxy_msg *m, ptp_node *node)
{
	if (node->number >= 0)
		proxy_add_int_attribute(m, PTP_NODE_NUMBER_ATTR, node->number);
}

static ptp_filter *
new_filter()
{
	ptp_filter *	f = (ptp_filter *)malloc(sizeof(ptp_filter));

	f->hash = HashCreate(DEFAULT_FILTER_ATTR_HASH_SIZE);
	f->num_attrs = 0;
	f->children = false;

	return f;
}

static void
add_filter_attribute(ptp_filter *f, char *attr)
{
	char *	name = proxy_copy_attribute_name_filter(attr);
	char *	value = proxy_get_attribute_value_str(attr);

	if (name != NULL && value != NULL) {
		regex_t *	reg = (regex_t *)malloc(sizeof(regex_t));
		int idx = HashCompute(name, strlen(name));
		if (regcomp(reg, value, REG_EXTENDED|REG_NOSUB) == 0) {
			HashInsert(f->hash, idx, reg);
			f->num_attrs++;
		} else {
			free(reg);
		}
		free(name);
	}
}

static void
free_filter(ptp_filter *f)
{
	HashDestroy(f->hash, free);
	free(f);
}

static bool
match_filter_str(int id, bool is_child, struct attrl *attrs)
{
	struct attrl *	attr;
	ptp_filter *	f = (ptp_filter *)HashSearch(gFilters, id);

	if (f != NULL && (f->children | !is_child)) {
		for (attr = attrs; attr != NULL; attr = attr->next) {
			regex_t	*	reg = (regex_t *)HashFind(f->hash, attr->name);
			if (reg != NULL && regexec(reg, attr->value, 0, NULL, 0) != 0) {
				return false;
			}
		}
	}

	return true;
}

void
update_filter(int id, ptp_filter *nf)
{
	ptp_filter *	f = (ptp_filter *)HashSearch(gFilters, id);

	if (f != NULL) {
		/*
		 * If no attributes specified, remove existing filter otherwise
		 * remove current filter and replace with new
		 */
		HashRemove(gFilters, id);
		free_filter(f);

		if (nf->num_attrs == 0) {
			free_filter(nf);
			return;
		}
	}

	HashInsert(gFilters, id, (void *)nf);
}

static void
sendNewJobEvent(int trans_id, ptp_job *j)
{
	char *		job_id;
	char *		queue_id;
	proxy_msg *	m;

	asprintf(&job_id, "%d", j->ptp_jobid);
	asprintf(&queue_id, "%d", j->queue->id);

	m = proxy_new_job_event(trans_id, queue_id, job_id, j->pbs_jobid, PTP_JOB_STATE_STARTING, j->jobsubid);
	proxy_svr_queue_msg(conn, m);

	free(job_id);
	free(queue_id);
}

static void
sendNewNodeEvent(int trans_id, int machid, ptp_machine *mach)
{
	ptp_node *	n;
	proxy_msg *	m;
	char *		machine_id;
	char *		node_id;
	
	asprintf(&machine_id, "%d", machid);
	
	m = proxy_new_node_event(trans_id, machine_id, SizeOfList(mach->nodes));
	
	for (SetList(mach->nodes); (n = (ptp_node *)GetListElement(mach->nodes)) != NULL; ) {
		asprintf(&node_id, "%d", n->id);
		proxy_add_node(m, node_id, n->name, n->state, num_node_attrs(n));
		add_node_attrs(m, n);
		free(node_id);
	}
	
	proxy_svr_queue_msg(conn, m);
	
	free(machine_id);
}

static void
sendNewProcessEvent(int trans_id, int jobid, ptp_process *p, char *state)
{
	proxy_msg *	m;
	char *		job_id;
	char *		proc_id;
	char *		name;
	
	asprintf(&job_id, "%d", jobid);
	asprintf(&proc_id, "%d", p->id);
	asprintf(&name, "%d",  p->task_id);
	
	m = proxy_new_process_event(trans_id, job_id, 1);
	proxy_add_process(m, proc_id, name, state, 3);
	proxy_add_int_attribute(m, PTP_PROC_NODEID_ATTR, p->node_id);
	proxy_add_int_attribute(m, PTP_PROC_INDEX_ATTR, p->task_id);
	proxy_add_int_attribute(m, PTP_PROC_PID_ATTR, p->pid);
	
	proxy_svr_queue_msg(conn, m);
	
	free(job_id);
	free(proc_id);
	free(name);
}

static void
sendProcessStateChangeEvent(int trans_id, ptp_job *j, char *state)
{
	proxy_msg *	m;
	
	if (j == NULL || j->num_procs == 0)
		return;
		
	m = proxy_process_change_event(trans_id, rangeset_to_string(j->set), 1);
	proxy_add_string_attribute(m, PTP_PROC_STATE_ATTR, state);
	proxy_svr_queue_msg(conn, m);
}

static void
sendRMAttributesEvent(int trans_id, struct attrl *attr)
{
	proxy_msg *	m;
	char *rm_id;

	asprintf(&rm_id, "%d", gBaseID);

	m = proxy_rm_change_event(trans_id, rm_id, get_pbs_attr_count(attr));
	add_pbs_attributes(m, attr);
	proxy_svr_queue_msg(conn, m);

	free(rm_id);
}
	
static void
sendJobChangeEvent(int trans_id, ptp_job *j, struct attrl *attrs)
{
	char *	job_id;
	proxy_msg *	m;

	asprintf(&job_id, "%d", j->ptp_jobid);
	m = proxy_job_change_event(trans_id, job_id, get_pbs_attr_count(attrs));
	add_pbs_attributes(m, attrs);
	proxy_svr_queue_msg(conn, m);
	free(job_id);
}

static void
sendJobStateChangeEvent(int trans_id, int jobid, char *state)
{
	char *		job_id;
	proxy_msg *	m;
	
	asprintf(&job_id, "%d", jobid);

	m = proxy_job_change_event(trans_id, job_id, 1);
	proxy_add_string_attribute(m, PTP_JOB_STATE_ATTR, state);
	proxy_svr_queue_msg(conn, m);
	
	free(job_id);
}

static void
sendProcessChangeEvent(int trans_id, ptp_process *p, int node_id, int task_id, int pid)
{
	int			cnt = 0;
	char *		proc_id;
	proxy_msg *	m;
	
	if (p->node_id != node_id || p->task_id != task_id || p->pid != pid) {
		if (p->node_id != node_id) {
			cnt++;	
		}
		if (p->task_id != task_id) {
			cnt++;	
		}
		if (p->pid != pid) {
			cnt++;	
		}
		
		asprintf(&proc_id, "%d", p->id);

		m = proxy_process_change_event(trans_id, proc_id, cnt);
		
		if (p->node_id != node_id) {
			p->node_id = node_id;
			proxy_add_int_attribute(m, PTP_ELEMENT_ID_ATTR, node_id);
		}
		if (p->task_id != task_id) {
			p->task_id = task_id;
			proxy_add_int_attribute(m, PTP_PROC_INDEX_ATTR, task_id);
		}
		if (p->pid != pid) {
			p->pid = pid;
			proxy_add_int_attribute(m, PTP_PROC_PID_ATTR, pid);
		}
		
		proxy_svr_queue_msg(conn, m);
		
		free(proc_id);
	}
}

/*
 * TODO: optimize this so that we don't send one event for
 * every process, even if the output is identical.
 */
static void
sendProcessOutputEvent(int trans_id, int procid, char *output)
{
	char *		proc_id;
	proxy_msg *	m;
	
	asprintf(&proc_id, "%d", procid);
	
	m = proxy_process_change_event(trans_id, proc_id, 1);
	proxy_add_string_attribute(m, PTP_PROC_STDOUT_ATTR, output);
	proxy_svr_queue_msg(conn, m);
	
	free(proc_id);	
}

/*
 * Set initial filter on queues
 */
static void
initialize_queue_filter(ptp_queue *q)
{
	char *			attr;
	ptp_filter *	f = new_filter();

	f->children = true;
	asprintf(&attr, "Job_OwnerFilter=%s@.*", gUserName);
	add_filter_attribute(f, attr);
	update_filter(q->id, f);
	free(attr);
}

/******************************
 * START OF DISPATCH ROUTINES *
 ******************************/
int
PBS_Initialize(int trans_id, int nargs, char **args)
{
	int						i;
	ptp_machine *			mach;
	struct batch_status *	s;
	struct batch_status *	status;
	
	if (debug_level > 0) {
		fprintf(stderr, "PBS_Initialize (%d):\n", trans_id); fflush(stderr);
	}
	
	if (proxy_state != STATE_INIT) {
		sendErrorEvent(trans_id, RTEV_ERROR_INIT, "already initialized");
		return PTP_PROXY_RES_OK;
	}
	
	/*
	 * Process arguments for the init command
	 */
	for (i = 0; i < nargs; i++) {
		if (proxy_test_attribute(PROTOCOL_VERSION_ATTR, args[i])) {
			if (strcmp(proxy_get_attribute_value_str(args[i]), WIRE_PROTOCOL_VERSION) != 0) {
				sendErrorEvent(trans_id, RTEV_ERROR_INIT, "wire protocol version \"%s\" not supported", args[0]);
				return PTP_PROXY_RES_OK;
			}
		} else if (proxy_test_attribute(BASE_ID_ATTR, args[i])) {
			gBaseID = proxy_get_attribute_value_int(args[i]);
		}
	}

	/*
	 * It's an error if no base ID was supplied
	 */
	if (gBaseID < 0) {
		sendErrorEvent(trans_id, RTEV_ERROR_INIT, "no base ID supplied");
		return PTP_PROXY_RES_OK;
	}
	
	stream = pbs_connect(NULL);
	if (stream < 0) {
		sendErrorEvent(trans_id, RTEV_ERROR_INIT, "could not connect to PBS daemon");
		return PTP_PROXY_RES_OK;
	}

	status = pbs_statserver(stream, NULL, NULL);
	if (status == NULL) {
		sendErrorEvent(trans_id, RTEV_ERROR_INIT, pbs_geterrmsg(stream));
		return PTP_PROXY_RES_OK;
	}

	/*
	 * Create the server machine
	 */
	mach = new_machine(status->name);

	pbs_statfree(status);

	/*
	 * Get queues and queue attributes
	 */
	status = pbs_statque(stream, NULL, NULL, NULL);
	if (status == NULL) {
		sendErrorEvent(trans_id, RTEV_ERROR_INIT, pbs_geterrmsg(stream));
		return PTP_PROXY_RES_OK;
	}

	for (s=status; s != NULL; s = s->next) {
		ptp_queue * q = new_queue(s->name);
		initialize_queue_filter(q);
	}

	pbs_statfree(status);

	proxy_state = STATE_RUNNING;
		
	sendOKEvent(trans_id);
		
	return PTP_PROXY_RES_OK;
}

/**
 * Initiate the model definition phase
 */
int
PBS_ModelDef(int trans_id, int nargs, char **args)
{
	if (debug_level > 0) {
		fprintf(stderr, "PBS_ModelDef (%d):\n", trans_id); fflush(stderr);
	}
	
	/*
	 * Send attribute definitions
	 */
	/*
	 * Send default filters
	 */
	sendOKEvent(trans_id);
	return PTP_PROXY_RES_OK;
}

/**
 * Stop polling for LSF change events
 */
 int
PBS_StopEvents(int trans_id, int nargs, char **args)
{
	if (debug_level > 0) {
		fprintf(stderr, "  PBS_StopEvents (%d):\n", trans_id); fflush(stderr);
	}
	/* notification that start events have completed */
	sendOKEvent(gTransID);
	gTransID = 0;
	sendOKEvent(trans_id);
	return PTP_PROXY_RES_OK;
}

/**
 * Submit a job with the given executable path and arguments (remote call from a client proxy)
 *
 * TODO - what about queues, should there be a LSF_Submit?
 */
int
PBS_SubmitJob(int trans_id, int nargs, char **args)
{
	int						i;
	int						a;
	int						debug = false;
	int						num_args = 0;
	int						num_env = 0;
	int						debug_argc = 0;
	char *					jobsubid = NULL;
	char *					pbs_jobid = NULL;
	char *					queue_name = NULL;
	char *					full_path;
	char *					pgm_name = NULL;
	char *					cwd = NULL;
	char *					exec_path = NULL;
	char *					debug_exec_name = NULL;
	char *					debug_exec_path = NULL;
	char *					debug_full_path;
	char **					debug_args = NULL;
	char **					env = NULL;
	ptp_queue *				queue;

	if (debug_level > 0) {
		fprintf(stderr, "  PBS_SubmitJob (%d):\n", trans_id);
	}

	for (i = 0; i < nargs; i++) {
		if (debug_level > 0) {
			fprintf(stderr, "\t%s\n", args[i]);
		}
		if (proxy_test_attribute(PTP_JOB_SUB_ID_ATTR, args[i])) {
			jobsubid = proxy_get_attribute_value_str(args[i]);
		} else if (proxy_test_attribute(PTP_QUEUE_ID_ATTR, args[i])) {
			queue_name = proxy_get_attribute_value_str(args[i]);
		} else if (proxy_test_attribute(PTP_JOB_EXEC_NAME_ATTR, args[i])) {
			pgm_name = proxy_get_attribute_value_str(args[i]);
		} else if (proxy_test_attribute(PTP_JOB_EXEC_PATH_ATTR, args[i])) {
			exec_path = proxy_get_attribute_value_str(args[i]);
		} else if (proxy_test_attribute(PTP_JOB_WORKING_DIR_ATTR, args[i])) {
			cwd = proxy_get_attribute_value_str(args[i]);
		} else if (proxy_test_attribute(PTP_JOB_PROG_ARGS_ATTR, args[i])) {
			num_args++;
		} else if (proxy_test_attribute(PTP_JOB_ENV_ATTR, args[i])) {
			num_env++;
		} else if (proxy_test_attribute(PTP_JOB_DEBUG_ARGS_ATTR, args[i])) {
			debug_argc++;
		} else if (proxy_test_attribute(PTP_JOB_DEBUG_FLAG_ATTR, args[i])) {
			debug = proxy_get_attribute_value_bool(args[i]);
		}
	}

	if (debug_level > 0) {
		fflush(stderr);
	}
	
	if (jobsubid == NULL) {
		sendErrorEvent(trans_id, RTEV_ERROR_SUBMIT, "missing ID on job submission");
		return PTP_PROXY_RES_OK;
	}
	
	if (proxy_state != STATE_RUNNING) {
		sendJobSubErrorEvent(trans_id, jobsubid, "must call INIT first");
		return PTP_PROXY_RES_OK;
	}
	
	if (queue_name == NULL) {
		sendJobSubErrorEvent(trans_id, jobsubid, "no queue specified");
		return PTP_PROXY_RES_OK;
	}

	if ((queue = find_queue_by_name(queue_name)) == NULL) {
		sendJobSubErrorEvent(trans_id, jobsubid, "unknown queue specified");
		return PTP_PROXY_RES_OK;
	}

	if (nargs < 1) {
		sendJobSubErrorEvent(trans_id, jobsubid, "incorrect arg count");
		return PTP_PROXY_RES_OK;
	}
	
	/*
	 * Do some checking first
	 */
	 
	if (pgm_name == NULL) {
		sendJobSubErrorEvent(trans_id, jobsubid, "Must specify a program name");
		return PTP_PROXY_RES_OK;
	}
	
	/*
	 * Get supplied environment. It is used to locate executable if necessary.
	 */
	
	if (num_env > 0) {
		env = (char **)malloc((num_env + 1) * sizeof(char *));
		for (i = 0, a = 0; i < nargs; i++) {
			if (proxy_test_attribute(PTP_JOB_ENV_ATTR, args[i]))
				env[a++] = strdup(proxy_get_attribute_value_str(args[i]));
		}
		env[a] = NULL;
	}
		
	/*
	 * If no path is specified, then try to locate executable.
	 */		
	if (exec_path == NULL) {
		full_path = pgm_name;
	} else {
		asprintf(&full_path, "%s/%s", exec_path, pgm_name);
	}
	
	if (access(full_path, X_OK) < 0) {
		sendJobSubErrorEvent(trans_id, jobsubid, strerror(errno));
		return PTP_PROXY_RES_OK;
	}
	
	if (debug) {		
		debug_argc++;
		debug_args = (char **)malloc((debug_argc+1) * sizeof(char *));
		for (i = 0, a = 1; i < nargs; i++) {
			if (proxy_test_attribute(PTP_JOB_DEBUG_ARGS_ATTR, args[i])) {
				debug_args[a++] = proxy_get_attribute_value_str(args[i]);
			} else if (proxy_test_attribute(PTP_JOB_DEBUG_EXEC_NAME_ATTR, args[i])) {
				debug_exec_name = proxy_get_attribute_value_str(args[i]);
			} else if (proxy_test_attribute(PTP_JOB_DEBUG_EXEC_PATH_ATTR, args[i])) {
				debug_exec_path = proxy_get_attribute_value_str(args[i]);
			}
		}
		debug_args[a] = NULL;
		
		/*
		 * If no path is specified, then try to locate executable.
		 */		
		if (debug_exec_path == NULL) {
			debug_full_path = debug_exec_name;
		} else {
			asprintf(&debug_full_path, "%s/%s", debug_exec_path, debug_exec_name);
		}
		
		if (access(debug_full_path, X_OK) < 0) {
			sendJobSubErrorEvent(trans_id, jobsubid, strerror(errno));
			return PTP_PROXY_RES_OK;
		}

		debug_args[0] = strdup(debug_full_path);
	}

	/* app_jobid = pbs_submit(stream, ...); */
	
	if (pbs_jobid == NULL) {
		sendJobSubErrorEvent(trans_id, jobsubid, pbs_geterrmsg(stream));
		return PTP_PROXY_RES_OK;
	}
	
	new_job(0, false, queue, jobsubid, pbs_jobid);

	/*
	 * Send ok for job submission.
	 */	
	sendOKEvent(trans_id);
	
	return PTP_PROXY_RES_OK;
}

/* 
 * terminate a job, given a jobid 
 */
int
PBS_TerminateJob(int trans_id, int nargs, char **args)
{
	int			i;
	int			jobid = -1;
	ptp_job *	j;
	
	if (proxy_state != STATE_RUNNING) {
		sendErrorEvent(trans_id, RTEV_ERROR_JOB, "Must call INIT first");
		return PTP_PROXY_RES_OK;
	}
	
	for (i = 0; i < nargs; i++) {
		if (proxy_test_attribute(PTP_JOB_ID_ATTR, args[i])) {
			jobid = proxy_get_attribute_value_int(args[i]);
		}
	}
	
	if (jobid < 0) {
		sendJobTerminateErrorEvent(trans_id, jobid, "Invalid job ID");
		return PTP_PROXY_RES_OK;
	}
	
	if ((j = find_job_by_id(jobid)) != NULL) {
		if (j->state == JOB_TERMINATING) {
			sendJobTerminateErrorEvent(trans_id, jobid, "Job termination already requested");
			return PTP_PROXY_RES_OK;
		}
		
		j->state = JOB_TERMINATING;

		/* pbs_terminate(j->pbs_jobid); */
		
		sendOKEvent(trans_id);
	}
	
	return PTP_PROXY_RES_OK;
}

/*
 * Enables sending of events. The first thing that must be sent is a
 * description of the model. This comprises new model element events
 * for each element in the model. Once the model description has been
 * sent, model change events will be sent as detected.
 * 
 */
 int
PBS_StartEvents(int trans_id, int nargs, char **args)
{
	ptp_machine *			mach;
	struct batch_status *	s;
	struct batch_status *	status;
	
	if (debug_level > 0) {
		fprintf(stderr, "  PBS_StartEvents (%d):\n", trans_id); fflush(stderr);
	}

	if (proxy_state != STATE_RUNNING) {
		sendErrorEvent(trans_id, RTEV_ERROR_START_EVENTS, "must call INIT first");
		return PTP_PROXY_RES_OK;
	}

	gTransID = trans_id;
	
	/*
	 * Send the RM attributes
	 */
	status = pbs_statserver(stream, NULL, NULL);
	if (status == NULL) {
		sendErrorEvent(trans_id, RTEV_ERROR_START_EVENTS, pbs_geterrmsg(stream));
		return PTP_PROXY_RES_OK;
	}

	sendRMAttributesEvent(trans_id, status->attribs);

	/*
	 * Send the machines
	 */
	for (SetList(gMachineList); (mach = (ptp_machine *)GetListElement(gMachineList)) != NULL; ) {
		sendNewMachineEvent(trans_id, mach->id, mach->name);
	}

	pbs_statfree(status);

	/*
	 * Send queues and queue attributes
	 */
	status = pbs_statque(stream, NULL, NULL, NULL);
	if (status == NULL) {
		sendErrorEvent(trans_id, RTEV_ERROR_START_EVENTS, pbs_geterrmsg(stream));
		return PTP_PROXY_RES_OK;
	}

	for (s=status; s != NULL; s = s->next) {
		ptp_queue * q = find_queue_by_name(s->name);
		if (q != NULL) {
			sendNewQueueEvent(trans_id, q->id, q->name, s->attribs);
		}
	}

	pbs_statfree(status);

	if (debug_level > 0) {
		fprintf(stderr, "  end PBS_StartEvents (%d):\n", trans_id); fflush(stderr);
	}

	return PTP_PROXY_RES_OK;
}

int
PBS_Quit(int trans_id, int nargs, char **args)
{
	int old_state = proxy_state;
	
	if (debug_level > 0) {
		fprintf(stderr, "PBS_Quit called!\n"); fflush(stderr);
	}
	
	proxy_state = STATE_SHUTTING_DOWN;

	if (old_state == STATE_RUNNING) {
		shutdown_pbs();
	}
	
	sendShutdownEvent(trans_id);
	
	return PTP_PROXY_RES_OK;
}

int
PBS_FilterEvents(int trans_id, int nargs, char **args)
{
	int				i;
	int				id = 0;
	bool			filter_children = false;
	ptp_filter *	f;

	if (debug_level > 0) {
		fprintf(stderr, "  PBS_FilterEvents (%d):\n", trans_id); fflush(stderr);
	}

	f = new_filter();

	for (i = 0; i < nargs; i++) {
		if (proxy_test_attribute(PTP_ELEMENT_ID_ATTR, args[i])) {
			id = atoi(proxy_get_attribute_value_str(args[i]));
		} else if (proxy_test_attribute(PTP_FILTER_CHILDREN_ATTR, args[i])) {
			filter_children = proxy_get_attribute_value_bool(args[i]);
		} else {
			add_filter_attribute(f, args[i]);
		}
	}

	if (id == 0) {
		sendErrorEvent(trans_id, RTEV_ERROR_FILTER, "no element ID specified");
		return PTP_PROXY_RES_OK;
	}

	f->children = filter_children;

	update_filter(id, f);

	return PTP_PROXY_RES_OK;
}

static int
poll_pbs()
{
	int						new_jobs = 0;
	int						changed_jobs = 0;
	int						removed_jobs = 0;
	HashEntry *				h;
	Hash *					tmpJobHash;
	ptp_job *				j;
	struct batch_status *	status;
	struct batch_status *	s;

	status = pbs_statjob(stream, NULL, NULL, NULL);
	if (status < 0) {
		if (debug_level > 0) {
			fprintf(stderr, "pbs_statjob: %s\n", pbs_geterrmsg(stream));
		}
		return -1;
	}

	/*
	 * Create tmp job hash
	 * Create tmp job list
	 * foreach (job in status) {
	 * 	add job and attributes to tmp job hash
	 *  if (job not in job hash) {
	 *  	add to tmp job list
	 *  }
	 * }
	 * foreach (job in job hash) {
	 * 	if job does not exist in tmp hash, remove job
	 * }
	 * foreach (job in tmp job list) {
	 * 	add to job hash
	 * }
	 */
	tmpJobHash = HashCreate(DEFAULT_HASH_SIZE);

	for (s = status; s != NULL; s = s->next) {
		int idx = HashCompute(s->name, strlen(s->name));
		HashInsert(tmpJobHash, idx, (void *)s);
	}

	for (HashSet(gJobHash); (h = HashGet(gJobHash)) != NULL; ) {
		j = (ptp_job *)h->h_data;
		if (HashFind(tmpJobHash, j->pbs_jobid) == NULL) {
			sendJobStateChangeEvent(gTransID, j->ptp_jobid, PTP_JOB_STATE_COMPLETED);
			//sendRemoveJobEvent(gTransID, j);
			removed_jobs++;
		}
	}

	for (HashSet(tmpJobHash); (h = HashGet(tmpJobHash)) != NULL; ) {
		j = (ptp_job *)HashSearch(gJobHash, h->h_hval);
		s = (struct batch_status *)h->h_data;
		if (j == NULL) {
			char * queue_name = NULL;
			ptp_queue * queue = NULL;

			queue_name = find_pbs_queue_name(s->attribs);

			if (queue_name == NULL || ((queue = find_queue_by_name(queue_name)) == NULL)) {
				break;
			}

			/*
			 * Check for queue filters
			 */
			if (match_filter_str(queue->id, true, s->attribs)) {
				j = new_job(0, false, queue, NULL, s->name);

				if (debug_level > 0) {
					fprintf(stderr, "creating new job for %s\n", s->name); fflush(stderr);
				}
			} else if (debug_level > 0) {
				fprintf(stderr, "filtered job %s\n", s->name); fflush(stderr);
			}
		}
		if (j->state == JOB_CREATED) {
			sendNewJobEvent(gTransID, j);
			j->state = JOB_NORMAL;
			new_jobs++;
		}
		changed_jobs++;
	}

	if (debug_level > 0) {
		fprintf(stderr, "poll_pbs: new=%d, changed=%d, removed=%d\n", new_jobs, changed_jobs, removed_jobs); fflush(stderr);
	}

	HashDestroy(tmpJobHash, NULL);

	return 0;
}

static void
initialize()
{
	struct passwd *	pw;

	gJobList = NewList();
	gJobHash = HashCreate(DEFAULT_HASH_SIZE);
	gMachineList = NewList();
	gQueueList = NewList();
	gFilters = HashCreate(DEFAULT_FILTER_HASH_SIZE);

	pw = getpwuid(getuid());
	if (pw != NULL) {
		gUserName = strdup(pw->pw_name);
	}
}

static int
server(char *name, char *host, int port)
{
	int				rc = 0;
	int				poll_timeout = 0;
	struct timeval	timeout = {0, PROXY_TIMEOUT};
	
	initialize();
	
	if (proxy_svr_init(name, &timeout, &helper_funcs, &command_tab, &conn) != PTP_PROXY_RES_OK) {
		if (debug_level > 0) {
			fprintf(stderr, "proxy failed to initialized\n"); fflush(stderr);
		}
		return 0;
	}
	
	if (proxy_svr_connect(conn, host, port) == PTP_PROXY_RES_OK) {
		if (debug_level > 0) {
			fprintf(stderr, "proxy connected\n"); fflush(stderr);
		}
		
		while (ptp_signal_exit == 0 && proxy_state != STATE_SHUTDOWN) {
			if (proxy_state == STATE_SHUTTING_DOWN) {
				proxy_state = STATE_SHUTDOWN;
			}
			if (gTransID > 0) {
				if ((poll_timeout -= PTP_PROXY_TIMEOUT) <= 0) {
					if (poll_pbs() < 0) {
						break;
					}
					poll_timeout = PBS_POLL_INTERVAL;
				}
			}
			if (proxy_svr_progress(conn) != PTP_PROXY_RES_OK) {
				break;
			}
		}
		
		if (ptp_signal_exit != 0) {
			if (ptp_signal_exit != SIGCHLD && proxy_state != STATE_SHUTTING_DOWN
					&& proxy_state != STATE_SHUTDOWN) {
				shutdown_pbs();
			}
			/* our return code = the signal that fired */
			rc = ptp_signal_exit;
		}
	} else if (debug_level > 0) {
		fprintf(stderr, "proxy connection failed\n"); fflush(stderr);
	}
	
	proxy_svr_finish(conn);
	
	return rc;
}

RETSIGTYPE
ptp_signal_handler(int sig)
{
		int	ret;
		if (sig == SIGCHLD)
			wait(&ret);
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
	int				port = PTP_PROXY_TCP_PORT;
	char *			host = DEFAULT_HOST;
	char *			proxy_str = DEFAULT_PROXY;
	int				rc;
	
	while ((ch = getopt_long(argc, argv, "P:p:h:d:", longopts, NULL)) != -1)
	switch (ch) {
	case 'P':
		proxy_str = optarg;
		break;
	case 'p':
		port = (int)strtol(optarg, NULL, 10);
		break;
	case 'h':
		host = optarg;
		break;
	case 'd':
		debug_level = (int)strtol(optarg, NULL, 10);
		break;
	default:
		fprintf(stderr, "%s [--proxy=proxy] [--host=host_name] [--port=port] [--debug=level]\n", argv[0]);
		return 1;
	}
	
	/* 
	 * signal can happen any time after handlers are installed, so
	 * make sure we catch it
	 */
	ptp_signal_exit = 0;
	
#if 0
	/* setup our signal handlers */
	saved_signals[SIGINT] = signal(SIGINT, ptp_signal_handler);
	saved_signals[SIGHUP] = signal(SIGHUP, ptp_signal_handler);
	saved_signals[SIGILL] = signal(SIGILL, ptp_signal_handler);
	saved_signals[SIGSEGV] = signal(SIGSEGV, ptp_signal_handler);
	saved_signals[SIGTERM] = signal(SIGTERM, ptp_signal_handler);
	saved_signals[SIGQUIT] = signal(SIGQUIT, ptp_signal_handler);
	saved_signals[SIGABRT] = signal(SIGABRT, ptp_signal_handler);
	saved_signals[SIGCHLD] = signal(SIGCHLD, ptp_signal_handler);
	
	if(saved_signals[SIGINT] != SIG_ERR && saved_signals[SIGINT] != SIG_IGN && saved_signals[SIGINT] != SIG_DFL) {
		fprintf(stderr, "  ---> SIGNAL SIGINT was previously already defined.  Shadowing.\n"); fflush(stderr);
	}
	if(saved_signals[SIGHUP] != SIG_ERR && saved_signals[SIGHUP] != SIG_IGN && saved_signals[SIGHUP] != SIG_DFL) {
		fprintf(stderr, "  ---> SIGNAL SIGHUP was previously already defined.  Shadowing.\n"); fflush(stderr);
	}
	if(saved_signals[SIGILL] != SIG_ERR && saved_signals[SIGILL] != SIG_IGN && saved_signals[SIGILL] != SIG_DFL) {
		fprintf(stderr, "  ---> SIGNAL SIGILL was previously already defined.  Shadowing.\n"); fflush(stderr);
	}
	if(saved_signals[SIGSEGV] != SIG_ERR && saved_signals[SIGSEGV] != SIG_IGN && saved_signals[SIGSEGV] != SIG_DFL) {
		fprintf(stderr, "  ---> SIGNAL SIGSEGV was previously already defined.  Shadowing.\n"); fflush(stderr);
	}	
	if(saved_signals[SIGTERM] != SIG_ERR && saved_signals[SIGTERM] != SIG_IGN && saved_signals[SIGTERM] != SIG_DFL) {
		fprintf(stderr, "  ---> SIGNAL SIGTERM was previously already defined.  Shadowing.\n"); fflush(stderr);
	}
	if(saved_signals[SIGQUIT] != SIG_ERR && saved_signals[SIGQUIT] != SIG_IGN && saved_signals[SIGQUIT] != SIG_DFL) {
		fprintf(stderr, "  ---> SIGNAL SIGQUIT was previously already defined.  Shadowing.\n"); fflush(stderr);
	}
	if(saved_signals[SIGABRT] != SIG_ERR && saved_signals[SIGABRT] != SIG_IGN && saved_signals[SIGABRT] != SIG_DFL) {
		fprintf(stderr, "  ---> SIGNAL SIGABRT was previously already defined.  Shadowing.\n"); fflush(stderr);
	}	
	if(saved_signals[SIGCHLD] != SIG_ERR && saved_signals[SIGABRT] != SIG_IGN && saved_signals[SIGCHLD] != SIG_DFL) {
		fprintf(stderr, "  ---> SIGNAL SIGABRT was previously already defined.  Shadowing.\n"); fflush(stderr);
	}
#endif
	rc = server(proxy_str, host, port);
	
	return rc;
}
