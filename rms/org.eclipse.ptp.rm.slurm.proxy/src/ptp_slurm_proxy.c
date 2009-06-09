/********************************************************************************
 * Copyright (c) 2008,2009 
 * School of Computer, National University of Defense Technology, P.R.China
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 		Jie Jiang <jiangjie@nudt.edu.cn>
 *******************************************************************************/


#ifndef SLURM_PROXY
#define SLURM_PROXY 
#endif 


#ifdef __gnu_linux__
#define _GNU_SOURCE
#endif /* __gnu_linux__ */

#include "config.h"
#include <fcntl.h>
#include <getopt.h>
#include <unistd.h>
#include <grp.h>
#include <pwd.h>
#include <stdbool.h>
#include <errno.h>
#include <signal.h>
#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <stdint.h>       
#include <assert.h>
#include <unistd.h>
#include <pthread.h>
#include <libgen.h>
#include <sys/time.h>
#include <sys/types.h>
#include <sys/wait.h>
#include <sys/select.h>
#include <sys/param.h>
#include <sys/ipc.h>
#include <sys/shm.h>
#include <sys/socket.h>

#include "proxy.h"
#include "proxy_tcp.h"
#include "handler.h"
#include "list.h"
#include "args.h"
#include "rangeset.h"
#include "slurm/slurm.h"
#include "srun_opt.h"


#define PTP_JOBID 			0
#define SLURM_JOBID 		1
#define DEBUG_JOBID 		2
#define JOBID_INIT 			0
#define JOBID_FAIL 			-1
#define MAX_THREADS 		60
#define CORE_INVALID   		-1
#define CORE_DEFAULT 		0
#define MAX_BUF_SIZE 		8192 
#define MAX_SRUN_ARG_NUM 	256
#define ALL_JOBSTATE		-1
/*
 * Need to undef these if we include
 * two config.h files
 */
#undef PACKAGE_BUGREPORT
#undef PACKAGE_NAME
#undef PACKAGE_STRING
#undef PACKAGE_TARNAME
#undef PACKAGE_VERSION

#define WIRE_PROTOCOL_VERSION	"2.0"
#define DEFAULT_HOST			"localhost"
#define DEFAULT_PROXY			"tcp"

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
#define RTEV_ERROR_SLURM_INIT			RTEV_OFFSET + 1000
#define RTEV_ERROR_SLURM_FINALIZE		RTEV_OFFSET + 1001
#define RTEV_ERROR_SLURM_SUBMIT			RTEV_OFFSET + 1002
#define RTEV_ERROR_JOB					RTEV_OFFSET + 1003
#define RTEV_ERROR_NATTR				RTEV_OFFSET + 1007
//#define RTEV_ERROR_ORTE_BPROC_SUBSCRIBE	RTEV_OFFSET + 1008
#define RTEV_ERROR_SIGNAL				RTEV_OFFSET + 1009

/*
 * Queue attributes
 */
#define DEFAULT_QUEUE_NAME			"default"
#define SLURM_JOB_NAME_FMT			"job%02d"

#define MAX_RETRIES 				100
#define JOB_UPDATE_TIMER 			1
#define NODE_UPDATE_TIMER 			2
#define JOB_UPDATE_TIMEOUT			500000 	/*usec*/
#define NODE_UPDATE_TIMEOUT			5000000 /*usec*/

/* SLURM job state and attributes */
#define	SLURM_JOB_STATE_PENDING			"PENDING"
#define	SLURM_JOB_STATE_RUNNING			"RUNNING"
#define	SLURM_JOB_STATE_SUSPENDED		"SUSPENDED"
#define SLURM_JOB_STATE_TERMINATED		"TERMINATED"
#define	SLURM_JOB_STATE_COMPLETE		SLURM_JOB_STATE_TERMINATED
#define SLURM_JOB_STATE_CANCELLED		"CANCELLED"
#define	SLURM_JOB_STATE_FAILED		  	"FAILED"
#define	SLURM_JOB_STATE_TIMEOUT		  	"TIMEOUT"
#define	SLURM_JOB_STATE_NODEFAIL		"NODEFAIL"

#define SLURM_JOB_SUB_ID_ATTR				"jobSubId"
#define SLURM_JOB_NUM_PROCS_ATTR			"jobNumProcs" 	//-n
#define SLURM_JOB_NUM_NODES_ATTR			"jobNumNodes" 	//-N
#define SLURM_JOB_TIME_LIMIT_ATTR			"jobTimeLimit"	//-t
#define SLURM_JOB_PARTITION_ATTR			"jobPartition" 	//-p
#define SLURM_JOB_ID_ATTR					"jobId"        	//--jobid
#define SLURM_JOB_NODELIST_ATTR				"jobNodeList"  	//-w
#define SLURM_JOB_TYPE_ATTR					"jobType"      	//--jobtype
#define SLURM_JOB_IOLABEL_ATTR				"jobIoLabel"   	//-l
#define SLURM_JOB_VERBOSE_ATTR				"jobVerbose"   	//-v
#define SLURM_JOB_EXEC_NAME_ATTR			"execName"
#define SLURM_JOB_EXEC_PATH_ATTR			"execPath"
#define SLURM_JOB_WORKING_DIR_ATTR			"workingDir"
#define SLURM_JOB_PROG_ARGS_ATTR			"progArgs"
#define SLURM_JOB_ENV_ATTR					"env"
#define SLURM_JOB_DEBUG_EXEC_NAME_ATTR		"debugExecName"
#define SLURM_JOB_DEBUG_EXEC_PATH_ATTR		"debugExecPath"
#define SLURM_JOB_DEBUG_ARGS_ATTR			"debugArgs"
#define SLURM_JOB_DEBUG_FLAG_ATTR			"debug"

/* SLURM node state and attributes */
#define SLURM_NODE_STATE_UNKNOWN		"UNKNOWN"
#define SLURM_NODE_STATE_DOWN			"DOWN"
#define SLURM_NODE_STATE_IDLE			"UP"
#define	SLURM_NODE_STATE_ALLOCATED		"ALLOCATED"
#define SLURM_NODE_EXTRA_STATE_ATTR		"nodeExtraState"
#define SLURM_NODE_NUMBER_ATTR			"nodeNumber"
#define SLURM_NODE_SOCKETS_ATTR			"sockNumber"
#define SLURM_NODE_CORES_ATTR			"coreNumber"
#define SLURM_NODE_THREADS_ATTR			"threadNumber"
#define SLURM_NODE_ARCH_ATTR			"cpuArch"
#define SLURM_NODE_OS_ATTR				"OS"

#define EXIT_JOB_ALLOC_FAIL		-1
#define EXIT_JOB_IOTHREAD_FAIL	-2
#define EXIT_EXEC_FAIL			-3


struct sync_msg {
	int		slurm_jobid;
	bool 	jobid_set;
	bool	io_ready;
};
typedef struct sync_msg sync_msg;

struct ptp_machine {
	int		id;
	List *	nodes;
};
typedef struct ptp_machine	ptp_machine;

struct ptp_slurm_node {
	int 		id;  	/* model element id, generated by proxy agent */
	int			number;	/* node number, assigned by SLURM */
	char *		name;
	uint16_t	state; 	/* (uint16_t)node_info_t.state, converted to (char *)  */
	uint16_t 	sockets;
	uint16_t 	cores;
	uint16_t 	threads;
	char *		arch;
	char *		os;
};
typedef struct ptp_slurm_node	ptp_node;

struct ptp_slurm_process {
	int		id;
	int		node_id;
	int		task_id; /* MPI rank */
	int		pid;
};
typedef struct ptp_slurm_process	ptp_process;

struct ptp_slurm_job {
	int 				ptp_jobid;		/* job ID as known by PTP */
	int 				slurm_jobid;	/* job ID that will be used by program when it starts */
	bool				need_alloc;		/* need to allocate new resource */
	int					debug_jobid;
	int					num_procs;		/* number of procs requested for program (debugger: num_procs+1) */
	int					state;			/* job state(slurm definition) */
	pid_t				srun_pid;		/* pid of the srun process */
	bool				debug;			/* job is debug job */
	bool 				attach;			/* attach debug */
	int					fd_err;			/* fd of pipe for srun's stderr */
	int					fd_out;			/* fd of pipe for srun's stdout */
	int					iothread_id;	/* id of thread forwarding srun's stdio */
	bool				iothread_exit_req; /* request iothread to exit */
	bool				iothread_exit;  /* flag inidication iothread has exited  */
	bool				removable;
	ptp_process **  	procs;			/* procs of this job */
	rangeset * 			set;			/* range set of proc ID */
};
typedef struct ptp_slurm_job ptp_job;

typedef struct slurmctld_comm_addr {
	char * hostname;
	uint16_t port;
}slurmctld_comm_addr_t;

typedef void SigFunc(int);
typedef int32_t slurm_fd;


static int SLURM_Initialize(int, int, char **);
static int SLURM_ModelDef(int, int, char **);
static int SLURM_StartEvents(int, int, char **);
static int SLURM_StopEvents(int, int, char **);
static int SLURM_SubmitJob(int, int, char **);
static int SLURM_TerminateJob(int, int, char **);
static int SLURM_Quit(int, int, char **);

static FILE * init_logfp();
static void debug_log(FILE * fp, char * fmt,...);
static void * srun_output_forwarding(void * arg);
static bool job_update_timeout();
static void update_job_state(int slurm_jobid);
static bool node_update_timeout();
static void update_node_state();
static int create_node_list(ptp_machine *mach);
static void init_job_timer();
static void init_node_timer();

static struct timeval 		job_update_timer;
static struct timeval		node_update_timer;
static sync_msg *			sync_msg_addr;
static FILE * 				logfp;
static int 					destroy_job = 0; /* job allocation cancelled by signal */
static bool 				enable_state_update = false;
static 	srun_opt_t			opt;
static allocation_msg_thread_t* msg_thr = NULL;
static slurmctld_comm_addr_t slurmctld_comm_addr;
static int			gTransID = 0; /* transaction id for start of event stream, is 0 when events are off */
static int			gBaseID = -1; /* base ID for event generation */
static int			gLastID = 1;  /* ID generator */
static int			gQueueID;     /* ID of default queue */
static int 			proxy_state = STATE_INIT;
static proxy_svr *	slurm_proxy;
static List *		gJobList;
static List *		gMachineList;
static int			ptp_signal_exit = 0;;


static proxy_svr_helper_funcs helper_funcs = {
	NULL,					// newconn() - can be used to reject connections
	NULL					// numservers() - if there are multiple servers, return the number
};

#define CMD_BASE	0
static proxy_cmd	cmds[] = {
	SLURM_Quit,
	SLURM_Initialize,
	SLURM_ModelDef,
	SLURM_StartEvents,
	SLURM_StopEvents,
	SLURM_SubmitJob,
	SLURM_TerminateJob
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
	{NULL,				0,					NULL,	0}
};


/* 
 * If the log file is specified, return the FILE pointer.
 * Otherwise, default to stderr.
 */
static FILE * 
init_logfp()
{
	FILE * fp = stderr;
	char * logdir;
	char * logfile;
	int    rc;
	
	if ((logdir = getenv("PTP_SLURM_PROXY_LOGDIR")) == NULL)
		return fp;
	
	rc = access(logdir, R_OK|W_OK);
	if (rc < 0) {
		fprintf(stderr, "Please ensure the DIR %s exists and you can READ/WRITE it!\n", logdir);
		return fp;
	}
	asprintf(&logfile,"%s/ptp_proxy.log",logdir);
	fp = fopen(logfile,"w+");
	if(fp == NULL) 
		fp = stderr;
	free(logfile);

	return fp;
}

/*
 * Write debug info into logfile.
 */
static void
debug_log(FILE * fp, char * fmt,...)
{
	va_list va;

	if (fp == NULL)
		return;

	va_start(va, fmt);
	vfprintf(fp, fmt, va);
	fflush(fp);
	va_end(va);

	return;
}

/*
 * Generate a model element ID.
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
new_machine()
{
	ptp_machine *	m = (ptp_machine *)malloc(sizeof(ptp_machine));
	m->id = generate_id();
	m->nodes = NewList();
    AddToList(gMachineList, (void *)m);

    return m;
}

/*
 * Convert SLURM node state code (uint16_t) to STRING.
 */
static char * 
nodestate_to_string(uint16_t slurm_node_state)
{
	char * str = NULL;

	switch (slurm_node_state & NODE_STATE_BASE) {
		case NODE_STATE_UNKNOWN:
			str =  SLURM_NODE_STATE_UNKNOWN;
			break;
		case  NODE_STATE_DOWN:
			str =  SLURM_NODE_STATE_DOWN;
			break;
		case  NODE_STATE_IDLE:
			str =  SLURM_NODE_STATE_IDLE;
			break;
		case NODE_STATE_ALLOCATED:
			str =  SLURM_NODE_STATE_ALLOCATED;
			break;
		default:
			str = SLURM_NODE_STATE_UNKNOWN;
			break;
	}

	return str;
}

/*
 * Convert SLURM job state code(uint16_t) to STRING.
 */
static char *
jobstate_to_string(uint16_t slurm_job_state)
{
	char * str = NULL;

	switch (slurm_job_state & (~JOB_COMPLETING)) {
		case JOB_PENDING:
			str = SLURM_JOB_STATE_PENDING; 
			break;
		case JOB_RUNNING:
			str = SLURM_JOB_STATE_RUNNING;
			break;
		case JOB_SUSPENDED:
			str = SLURM_JOB_STATE_SUSPENDED;
			break;
		case JOB_COMPLETE:
			str = SLURM_JOB_STATE_COMPLETE; 
			break;
		case JOB_CANCELLED:
			str = SLURM_JOB_STATE_CANCELLED;
			break;
		case JOB_FAILED:
			str = SLURM_JOB_STATE_FAILED;
			break;
		case JOB_TIMEOUT:
			str = SLURM_JOB_STATE_TIMEOUT;
			break;
		case JOB_NODE_FAIL:
			str = SLURM_JOB_STATE_NODEFAIL;
			break;
		default:
			str = "Unknown job state";
			break;
	}

	return str;
}


/*
 * Create a new node and insert it into machine node list.
 */
static ptp_node *
new_node(ptp_machine *mach, node_info_t *ni)
{
	static int node_number = 0;
	ptp_node * n = (ptp_node *)malloc(sizeof(ptp_node));
	
	memset((char *)n, 0, sizeof(ptp_node));
	n->id = generate_id();
	n->number = node_number++;

	n->sockets = ni->sockets;
	n->cores = ni->cores;
	n->threads = ni->threads;

	if (ni->name != NULL)
		n->name = strdup(ni->name);
	
	n->state = ni->node_state;
		
	if (ni->arch != NULL)
		n->arch = strdup(ni->arch);
	if (ni->os != NULL)
		n->os = strdup(ni->os);
	
    AddToList(mach->nodes, (void *)n);

    return n;
}

/*
 * Get node pointer from node name. 
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
 * Create a new process and insert it into job structure.
 */
static ptp_process *
new_process(ptp_job *job, int node_id, int task_id, int pid)
{
	ptp_process *	p = (ptp_process *)malloc(sizeof(ptp_process));
	if (p == NULL)
		return NULL;

	p->id = generate_id();
	p->task_id = task_id;
	p->pid = pid;
	p->node_id = node_id;

    job->procs[task_id] = p;
    insert_in_rangeset(job->set, p->id);

    return p;
}


/*
 * Free the space allocated to ptp_process.
 */
static void
free_process(ptp_process *p)
{
	if (p)
		free(p);
	return;
}

/*
 * Get process pointer given job and task_id.
 */
static ptp_process *
find_process(ptp_job *job, int task_id)
{
	if (task_id < 0 || task_id >= job->num_procs)
		return NULL;
		
	return job->procs[task_id];
}
	
/*
 *  Get jobid of 'which' type
 */
static int
get_jobid(ptp_job *j, int which)
{
	int id = -1;

	if (j != NULL) {
		switch (which) {
		case PTP_JOBID:
			id = j->ptp_jobid;
			break;
		case SLURM_JOBID:
			id = j->slurm_jobid;
			break;
		case DEBUG_JOBID:
			id = j->debug_jobid;
			break;
		default:
			id = -1;
			break;
		}	
	}

	return id;
}

/*
 * Create a new job,set job attributes and add to gJobList.  
 */
static ptp_job *
new_job(int num_procs, bool debug, int ptp_jobid, int slurm_jobid, int debug_jobid, bool need_alloc)
{
	ptp_job *	j = (ptp_job *)malloc(sizeof(ptp_job));
	if (j == NULL) {
		debug_log(logfp, "Allcoate space for job struct fail. \n");
		return NULL;
	}	

	j->ptp_jobid = ptp_jobid;
    j->slurm_jobid = slurm_jobid;
	j->need_alloc = need_alloc; 
    j->num_procs = num_procs;
    j->debug = debug;
	j->debug_jobid = debug_jobid;
	j->attach = false;
	j->srun_pid = -1;
	j->state = -1;
	j->removable = false;
	j->fd_err = -1;
	j->fd_out = -1;
	j->iothread_id = -1;
	j->iothread_exit_req = false;
	j->iothread_exit = false;
	j->set = new_rangeset();
	j->procs = (ptp_process **)malloc(sizeof(ptp_process *) * num_procs);
	memset(j->procs, 0, sizeof(ptp_process *) * num_procs);

    AddToList(gJobList, (void *)j);

    return j;
}

/*
 * Free job space allocated by new_job().
 */
static void
free_job(ptp_job *j)
{
	int i;

	if (j) {
		if (j->procs) {
			for (i = 0; i < j->num_procs; i++) {
				if (j->procs[i] != NULL) 
					free_process(j->procs[i]);
			}
			free(j->procs);
		}
		if (j->set) 
			free_rangeset(j->set);

		free(j);
	}	
}


/*
 * Get job pointer given the jobid of 'which' type.
 * If debug is true, find the job using the debug jobid.
 */
static ptp_job *
find_job(int jobid, int which)
{
	ptp_job *	j = NULL;
	
	for (SetList(gJobList); (j = (ptp_job *)GetListElement(gJobList)) != NULL; ) {
		if (get_jobid(j, which) == jobid) 
			return j;
	}
	return NULL;
}

/*
 * Determin if the given job is ACTIVE.
 */
static bool 
slurm_job_active(ptp_job * job)
{
	uint16_t state;
	bool 	active;
	
	state = job->state;
	if (state == JOB_COMPLETE || state == JOB_CANCELLED 
		|| state == JOB_FAILED || state == JOB_TIMEOUT || state == JOB_NODE_FAIL) {
		active = false;
	} else
		active = true;  /* JOB_COMPLETING is ACTIVE */

	return active;
}

static void
sendOKEvent(int trans_id)
{
	proxy_svr_queue_msg(slurm_proxy, proxy_ok_event(trans_id));
}

static void
sendShutdownEvent(int trans_id)
{
	proxy_svr_queue_msg(slurm_proxy, proxy_shutdown_event(trans_id));
}

static void
sendMessageEvent(int trans_id, char *level, int code, char *fmt, ...)
{
	va_list		ap;

	va_start(ap, fmt);
	proxy_svr_queue_msg(slurm_proxy, proxy_message_event(trans_id, level, code, fmt, ap));
	va_end(ap);
}

static void
sendErrorEvent(int trans_id, int code, char *fmt, ...)
{
	va_list		ap;

	va_start(ap, fmt);
	debug_log(logfp, "sendErrorEvent(%d,%d),", trans_id, code);
	debug_log(logfp, fmt,ap);
	proxy_svr_queue_msg(slurm_proxy, proxy_error_event(trans_id, code, fmt, ap));
	va_end(ap);
}

static void
sendJobSubErrorEvent(int trans_id, char *jobSubId, char *msg)
{
	proxy_svr_queue_msg(slurm_proxy, proxy_submitjob_error_event(trans_id, jobSubId, RTEV_ERROR_SLURM_SUBMIT, msg));
}


static void
sendJobTerminateErrorEvent(int trans_id, int id, char *msg)
{
	char *	job_id;
	
	asprintf(&job_id, "%d", id);
	proxy_svr_queue_msg(slurm_proxy, proxy_terminatejob_error_event(trans_id, job_id, RTEV_ERROR_JOB, msg));
}

static void
sendNewMachineEvent(int trans_id, int id, char *name)
{
	char *	rm_id;
	char *	machine_id;
	
	asprintf(&rm_id, "%d", gBaseID);	
	asprintf(&machine_id, "%d", id);	
	proxy_svr_queue_msg(slurm_proxy, proxy_new_machine_event(trans_id, rm_id, machine_id, name, MACHINE_STATE_UP));
	free(machine_id);
	free(rm_id);
}


/*
 * Get the number of node attributes setup in ptp_node.
 */
static int
num_node_attrs(ptp_node *node)
{
	int	cnt = 0;

	if (node->number >= 0)
		cnt++;
	if (node->sockets > 0)
		cnt++;
	if (node->cores > 0)
		cnt++;
	if (node->threads > 0)
		cnt++;
	if (node->arch != NULL)
		cnt++;
	if (node->os != NULL)
		cnt++;

	return cnt;	
}

/*
 * Add node attributes to proxy_msg. 
 */
static void
add_node_attrs(proxy_msg *m, ptp_node *node)
{
	
    /* NODE_NUMBER_ATTR enables the node number ruler in Machine View */
	if (node->number >= 0)
		proxy_add_int_attribute(m, SLURM_NODE_NUMBER_ATTR, node->number);

	proxy_add_int_attribute(m, SLURM_NODE_SOCKETS_ATTR, (int)node->sockets);
	proxy_add_int_attribute(m, SLURM_NODE_CORES_ATTR, (int)node->cores);
	proxy_add_int_attribute(m, SLURM_NODE_THREADS_ATTR, (int)node->threads);

	if (node->arch != NULL)
		proxy_add_string_attribute(m, SLURM_NODE_ARCH_ATTR, node->arch);
	if (node->os != NULL)
		proxy_add_string_attribute(m, SLURM_NODE_OS_ATTR, node->os);
}

static void
sendNewJobEvent(int trans_id, int jobid, char *name, char *jobSubId, char *state)
{
	char *	queue_id;
	char *	job_id;
	
	asprintf(&queue_id, "%d", gQueueID);	
	asprintf(&job_id, "%d", jobid);
	proxy_svr_queue_msg(slurm_proxy, proxy_new_job_event(trans_id, queue_id, job_id, name, state, jobSubId));
	free(queue_id);
	free(job_id);
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
		proxy_add_node(m, node_id, n->name, nodestate_to_string(n->state), num_node_attrs(n));
		add_node_attrs(m, n);
		free(node_id);
	}
	proxy_svr_queue_msg(slurm_proxy, m);
	free(machine_id);
}

static void
sendNodeChangeEvent(int trans_id, char * id_range, char * state)
{
	proxy_msg * m;

	m = proxy_node_change_event(trans_id, id_range, 1);
	proxy_add_string_attribute(m, NODE_STATE_ATTR, state);
	proxy_svr_queue_msg(slurm_proxy, m);
}

/*
 * FIXME: 
 *	Add extra attributes when task topology information is available.
 *  This can be done via slurm_job_step_layout_get() API when updating job state.
 */
static void
sendNewProcessEvent(int trans_id, int jobid, ptp_process *p, char *state)
{
	proxy_msg *	m;
	char *		job_id;
	char *		proc_id;
	char *		name;
	
	if (p == NULL)
		return;
	
	asprintf(&job_id, "%d", jobid);
	asprintf(&proc_id, "%d", p->id);
	asprintf(&name, "%d",  p->task_id);
	
	m = proxy_new_process_event(trans_id, job_id, 1);
	/*
	 * By now, p->node_id, p->task_id, p->pid can't be obtained.
	 * So set the extra_attrs=0
	 */
	proxy_add_process(m, proc_id, name, state, 0);
	/*	
	proxy_add_process(m, proc_id, name, state, 3);
	proxy_add_int_attribute(m, PROC_NODEID_ATTR, p->node_id);	
	proxy_add_int_attribute(m, PROC_INDEX_ATTR, p->task_id);	
	proxy_add_int_attribute(m, PROC_PID_ATTR, p->pid);
	*/
	proxy_svr_queue_msg(slurm_proxy, m);
	
	free(job_id);
	free(proc_id);
	free(name);
}

static void
sendNewQueueEvent(int trans_id)
{
	char *		rm_id;
	char *		queue_id;
	
	gQueueID = generate_id();
	
	asprintf(&rm_id, "%d", gBaseID);
	asprintf(&queue_id, "%d", gQueueID);
	proxy_svr_queue_msg(slurm_proxy, proxy_new_queue_event(trans_id, rm_id, queue_id, DEFAULT_QUEUE_NAME, QUEUE_STATE_NORMAL));
	
	free(rm_id);
	free(queue_id);
}

static void
sendProcessStateChangeEvent(int trans_id, ptp_job *j, char *state)
{
	proxy_msg *	m;
	
	if (j == NULL || j->num_procs == 0)
		return;
		
	m = proxy_process_change_event(trans_id, rangeset_to_string(j->set), 1);
	proxy_add_string_attribute(m, PROC_STATE_ATTR, state);
	proxy_svr_queue_msg(slurm_proxy, m);
}
	
	
static void
sendJobStateChangeEvent(int trans_id, int jobid, char *state)
{
	char *		job_id;
	proxy_msg *	m;
	
	asprintf(&job_id, "%d", jobid);

	m = proxy_job_change_event(trans_id, job_id, 1);
	proxy_add_string_attribute(m, JOB_STATE_ATTR, state);
	proxy_svr_queue_msg(slurm_proxy, m);
	
	free(job_id);
}

/*
 * SLURM provides no process state, only job state.
 * Let process state equal to job state.
 */
static void
sendProcessChangeEvent(int trans_id, ptp_process *p, int node_id, int task_id, int pid)
{
	return;
}

static void
sendProcessOutputEvent(int trans_id, int procid, char *output)
{
	char *		proc_id;
	proxy_msg *	m;
	
	asprintf(&proc_id, "%d", procid);
	
	m = proxy_process_change_event(trans_id, proc_id, 1);
	proxy_add_string_attribute(m, PROC_STDOUT_ATTR, output);
	proxy_svr_queue_msg(slurm_proxy, m);
	
	free(proc_id);	
}

/*
 * Get the number of compute nodes managed by SLURM.
 */
static int
get_num_nodes(int machid)
{
	uint32_t cnt = 0;
	node_info_msg_t * ninfo;

	slurm_load_node((time_t)NULL, &ninfo, SHOW_ALL);
	cnt = ninfo->record_count;
	slurm_free_node_info_msg(ninfo);
	
	return cnt;
}

/* 
 * Currently only ONE machine supported.
 */
static int 
get_num_machines()
{
	return 1;
}

/*
 * Get hostname of the server node where slurmctld runs.
 */
static char *
get_machine_name(int num)
{
	static char	hostname[512];
	
	gethostname(hostname, 512);
	return hostname;
}

/*
 * Cteate the node list for a machine.
 */
static int
create_node_list(ptp_machine *mach)
{
	uint32_t cnt;
	node_info_msg_t *nmsg;
	ptp_node *		node;
	int i;

	if (slurm_load_node((time_t)NULL,&nmsg, SHOW_ALL))
		return -1;

	cnt = nmsg->record_count;
	for (i = 0; i < cnt; i++) 
		node = new_node(mach, nmsg->node_array + i);

	return 0;
}

/*
 * If we're under debug control, let the debugger handle process state update. 
 * 
 * Note: this will only be called if the debugger allows the program to
 * reach MPI_Init(), which may never happen (e.g. if it's not an MPI program). 
 * Don't rely this to do anything for arbitrary jobs.
 * 
 * Note also: the debugger manages process state updates so we don't need
 * to send events back to the runtime.
 */

 
//static void
//debug_app_job_state_callback(orte_jobid_t jobid, orte_proc_state_t state)
//{
	/* this is what it has before, untouched */
//	switch(state) {
//		case ORTE_PROC_STATE_TERMINATED:
//		case ORTE_PROC_STATE_ABORTED:
//			break;
//	}
//}

/*
 * job_state_callback for the debugger. Detects debugger start and exit and notifies the
 * UI. Cleans up job id map.
 */
/* 
static void
debug_job_state_callback(orte_jobid_t jobid, orte_proc_state_t state)
{
	ptp_job	*		j;
	
	if ((j = find_job(jobid, JOBID_DEBUG)) == NULL)
		return;
	
	do_state_callback(j, state);
}
*/

/* 
 * Tell the daemon to exit. 
 * Noops for SLURM.
 */
static int
do_slurm_shutdown(void)
{
	debug_log(logfp, "do_slurm_shutdown() called.\n"); 
	return 0;
}

/* 
 * gethostname_short - equivalent to gethostname, but return only the first
 * component of the fully qualified name 
 * (e.g. "linux123.foo.bar" becomes "linux123") 
 */
static int
gethostname_short (char *name, size_t len)
{
	int error_code, name_len;
	char *dot_ptr, path_name[1024];

	error_code = gethostname (path_name, sizeof(path_name));
	if (error_code)
		return error_code;

	dot_ptr = strchr (path_name, '.');
	if (dot_ptr == NULL)
		dot_ptr = path_name + strlen(path_name);
	else
		dot_ptr[0] = '\0';

	name_len = dot_ptr - path_name;
	if (name_len > len)
		return ENAMETOOLONG;

	strcpy (name, path_name);
	return 0;
}


/*
 * Set default srun options.
 */
static int 
opt_default(srun_opt_t * opt)
{
	char buf[MAXPATHLEN + 1];
	struct passwd *pw;
	int i;
	char hostname[64];
	
	if (opt == NULL)
		return -1;

	if ((pw = getpwuid(getuid())) != NULL) {
		strncpy(opt->ps_user, pw->pw_name, MAX_USERNAME);
		opt->ps_uid = pw->pw_uid;
	} else {
		debug_log(logfp, "opt_default:who are you?");
		return -1;
	}

	opt->ps_gid = getgid();

	if ((getcwd(buf, MAXPATHLEN)) == NULL) {
		debug_log(logfp,"getcwd failed");
		return -1;
	}
	opt->ps_cwd = strdup(buf); 
	opt->ps_cwd_set = false;
	opt->ps_progname = NULL;
	opt->ps_nprocs = 1;
	opt->ps_nprocs_set = false;
	opt->ps_cpus_per_task = 1; 
	opt->ps_cpus_set = false;
	opt->ps_min_nodes = 1;
	opt->ps_max_nodes = 0;
	opt->ps_min_sockets_per_node = NO_VAL; /* requested min/maxsockets */
	opt->ps_max_sockets_per_node = NO_VAL;
	opt->ps_min_cores_per_socket = NO_VAL; /* requested min/maxcores */
	opt->ps_max_cores_per_socket = NO_VAL;
	opt->ps_min_threads_per_core = NO_VAL; /* requested min/maxthreads */
	opt->ps_max_threads_per_core = NO_VAL; 
	opt->ps_ntasks_per_node      = NO_VAL; /* ntask max limits */
	opt->ps_ntasks_per_socket    = NO_VAL; 
	opt->ps_ntasks_per_core      = NO_VAL; 
	opt->ps_nodes_set = false;
	opt->ps_cpu_bind_type = 0;
	opt->ps_cpu_bind = NULL;
	opt->ps_mem_bind_type = 0;
	opt->ps_mem_bind = NULL;
	opt->ps_time_limit = NO_VAL;
	opt->ps_time_limit_str = NULL;
	opt->ps_ckpt_interval = 0;
	opt->ps_ckpt_interval_str = NULL;
	opt->ps_ckpt_path = NULL;
	opt->ps_partition = NULL;
	
	//use default value:32
	/* 
	opt->ps_max_threads = MAX_THREADS;
	pmi_server_max_threads(opt->ps_max_threads);
	*/

	opt->ps_relative = NO_VAL;
	opt->ps_relative_set = false;
	opt->ps_job_name = NULL;
	opt->ps_job_name_set = false;
	opt->ps_jobid = NO_VAL;
	opt->ps_jobid_set = false;
	opt->ps_dependency = NULL;
	opt->ps_account  = NULL;
	opt->ps_comment  = NULL;

	opt->ps_distribution = SLURM_DIST_UNKNOWN;
	opt->ps_plane_size   = NO_VAL;

	opt->ps_ofname = NULL;
	opt->ps_ifname = NULL;
	opt->ps_efname = NULL;

	opt->ps_core_type = CORE_DEFAULT;

	opt->ps_labelio = false;
	opt->ps_unbuffered = false;
	opt->ps_overcommit = false;
	opt->ps_shared = (uint16_t)NO_VAL;
	opt->ps_exclusive = false;
	opt->ps_no_kill = false;
	opt->ps_kill_bad_exit = false;

	opt->ps_immediate = false;

	opt->ps_join = false;
	slurm_ctl_conf_t * slurm_ctl_conf_ptr;
	slurm_load_ctl_conf((time_t)NULL, &slurm_ctl_conf_ptr);
	opt->ps_max_wait = slurm_ctl_conf_ptr->wait_time;

	opt->ps_quit_on_intr = false;
	opt->ps_disable_status = false;
	opt->ps_test_only   = false;
	
	opt->ps_quiet = 0;

	opt->ps_job_min_cpus    = NO_VAL;
	opt->ps_job_min_sockets = NO_VAL;
	opt->ps_job_min_cores   = NO_VAL;
	opt->ps_job_min_threads = NO_VAL;
	opt->ps_job_min_memory  = NO_VAL;
	opt->ps_task_mem        = NO_VAL;
	opt->ps_job_min_tmp_disk= NO_VAL;

	opt->ps_hold = false;
	opt->ps_constraints = NULL;
	opt->ps_contiguous = false;
	opt->ps_nodelist = NULL;
	opt->ps_exc_nodes = NULL;
	opt->ps_max_launch_time = 120;/* 120 seconds to launch job */
	opt->ps_max_exit_timeout= 60; /* Warn user 60 seconds after task exit */
	/* Default launch msg timeout */
	opt->ps_msg_timeout = slurm_ctl_conf_ptr->msg_timeout;  

	for (i=0; i<SYSTEM_DIMENSIONS; i++)
		opt->ps_geometry[i] = (uint16_t) NO_VAL;
	opt->ps_reboot = false;
	opt->ps_no_rotate = false;
	opt->ps_conn_type = (uint16_t) NO_VAL;
	opt->ps_blrtsimage = NULL;
	opt->ps_linuximage = NULL;
	opt->ps_mloaderimage = NULL;
	opt->ps_ramdiskimage = NULL;
	opt->ps_euid = (uid_t) -1;
	opt->ps_egid = (gid_t) -1;
	opt->ps_propagate = NULL;  /* propagate specific rlimits */
	opt->ps_prolog = slurm_ctl_conf_ptr->srun_prolog;
	opt->ps_epilog = slurm_ctl_conf_ptr->srun_epilog;
	opt->ps_task_prolog = NULL;
	opt->ps_task_epilog = NULL;
	gethostname_short(hostname, sizeof(hostname));
	opt->ps_ctrl_comm_ifhn = strdup(hostname);
	opt->ps_pty = false;
	opt->ps_open_mode = 0;
	opt->ps_acctg_freq = -1;
	
	if (slurm_ctl_conf_ptr)
		slurm_free_ctl_conf(slurm_ctl_conf_ptr);
	
	return 0;
}

/* 
 * Initialize option defaults. 
 */
static int
set_srun_options_defaults(srun_opt_t * opt)
{
	return opt_default(opt);
}

/*
 * Free space allocated for job "opt". 
 */
static void 
free_opt(srun_opt_t * opt)
{
	if (opt == NULL)
		return;

	if (opt->ps_cwd_set == true && opt->ps_cwd != NULL)
		free(opt->ps_cwd);
	if (opt->ps_nodes_set == true && opt->ps_nodelist != NULL)
		free(opt->ps_nodelist);
	if (opt->ps_ctrl_comm_ifhn) /* allocated in opt_default() */
		free(opt->ps_ctrl_comm_ifhn);
}

/*
 * Free space allocated to save srun args in SLURM_SubmitJob().
 */
static void 
free_srun_argv(int srun_argc,char ** srun_argv)
{
	int i;

	for (i = 0; i< srun_argc; i++) {
		if (srun_argv[i] != NULL)
			free(srun_argv[i]);
	}

	free(srun_argv);
}


/*
 * Create job desc. msg from opts for job allocation.
 * By now, -w/-x options are not supported.
 */
static job_desc_msg_t * 
create_job_desc_msg_from_opts(srun_opt_t *opt)
{

	job_desc_msg_t * j = NULL;
	
	assert(opt != NULL);
	if ((j = (job_desc_msg_t *)malloc(sizeof(job_desc_msg_t))) == NULL) {
		debug_log(logfp, "Allocate job_desg_msg fail");
		return NULL;
	}

	slurm_init_job_desc_msg(j);
	
	if (opt->ps_account)
		j->account = strdup(opt->ps_account);

	j->contiguous     = opt->ps_contiguous;
	j->features       = opt->ps_constraints;
	j->immediate      = (uint16_t)opt->ps_immediate;
	j->name           = opt->ps_job_name;

	if (opt->ps_nodelist) 
		j->req_nodes = strdup(opt->ps_nodelist);
	/*
	 * FIXME: handle -w nodelist request
	 */
	/*
	if(j->req_nodes) {
		hl = hostlist_create(j->req_nodes);
		hostlist_ranged_string(hl, sizeof(buf), buf);
		xfree(opt.nodelist);
		opt.nodelist = xstrdup(buf);
		hostlist_uniq(hl);
		hostlist_ranged_string(hl, sizeof(buf), buf);
		hostlist_destroy(hl);
		xfree(j->req_nodes);
		j->req_nodes = xstrdup(buf);
	}
	*/
	if(opt->ps_distribution == SLURM_DIST_ARBITRARY
	   && !j->req_nodes) {
		debug_log(logfp,"With Arbitrary distribution you need to \
				specify a nodelist or hostfile with the -w option");
		return NULL;
	}
	j->exc_nodes = opt->ps_exc_nodes;
	j->partition = opt->ps_partition;
	j->min_nodes = opt->ps_min_nodes;
	if (opt->ps_min_sockets_per_node != NO_VAL)
		j->min_sockets = (uint16_t)opt->ps_min_sockets_per_node;
	if (opt->ps_min_cores_per_socket != NO_VAL)
		j->min_cores = (uint16_t)opt->ps_min_cores_per_socket;
	if (opt->ps_min_threads_per_core != NO_VAL)
		j->min_threads = (uint16_t)opt->ps_min_threads_per_core;
	j->user_id = opt->ps_uid;
	j->dependency = opt->ps_dependency;
	if (opt->ps_nice)
		j->nice = (uint16_t)(NICE_OFFSET + opt->ps_nice);
	j->task_dist = (uint16_t)opt->ps_distribution;
	if (opt->ps_plane_size != NO_VAL)
		j->plane_size = (uint16_t)opt->ps_plane_size;
	j->group_id = opt->ps_gid;
	j->mail_type = opt->ps_mail_type;

	if (opt->ps_ntasks_per_node != NO_VAL)
		j->ntasks_per_node = (uint16_t)opt->ps_ntasks_per_node;
	if (opt->ps_ntasks_per_socket != NO_VAL)
		j->ntasks_per_socket = (uint16_t)opt->ps_ntasks_per_socket;
	if (opt->ps_ntasks_per_core != NO_VAL)
		j->ntasks_per_core =(uint16_t)opt->ps_ntasks_per_core;

	if (opt->ps_mail_user)
		j->mail_user = strdup(opt->ps_mail_user);
	if (opt->ps_begin)
		j->begin_time = opt->ps_begin;
	if (opt->ps_licenses)
		j->licenses = strdup(opt->ps_licenses);
	if (opt->ps_network)
		j->network = strdup(opt->ps_network);
	if (opt->ps_comment)
		j->comment = strdup(opt->ps_comment);

	if (opt->ps_hold)
		j->priority = 0;
	if (opt->ps_jobid != NO_VAL)
		j->job_id = opt->ps_jobid;

	#if SYSTEM_DIMENSIONS
	if (opt->ps_geometry[0] > 0) {
		int i;
		for (i=0; i<SYSTEM_DIMENSIONS; i++)
			j->geometry[i] = opt->ps_geometry[i];
	}
	#endif

	if (opt->ps_conn_type != (uint16_t) NO_VAL)
		j->conn_type = opt->ps_conn_type;
			
	if (opt->ps_reboot)
		j->reboot = 1;
	if (opt->ps_no_rotate)
		j->rotate = 0;

	if (opt->ps_blrtsimage)
		j->blrtsimage = strdup(opt->ps_blrtsimage);
	if (opt->ps_linuximage)
		j->linuximage = strdup(opt->ps_linuximage);
	if (opt->ps_mloaderimage)
		j->mloaderimage = strdup(opt->ps_mloaderimage);
	if (opt->ps_ramdiskimage)
		j->ramdiskimage = strdup(opt->ps_ramdiskimage);

	if (opt->ps_max_nodes)
		j->max_nodes = opt->ps_max_nodes;
	if (opt->ps_max_sockets_per_node)
		j->max_sockets = opt->ps_max_sockets_per_node;
	if (opt->ps_max_cores_per_socket)
		j->max_cores = opt->ps_max_cores_per_socket;
	if (opt->ps_max_threads_per_core)
		j->max_threads = opt->ps_max_threads_per_core;

	if (opt->ps_job_min_cpus != NO_VAL)
		j->job_min_procs = opt->ps_job_min_cpus;
	if (opt->ps_job_min_sockets != NO_VAL)
		j->job_min_sockets = opt->ps_job_min_sockets;
	if (opt->ps_job_min_cores != NO_VAL)
		j->job_min_cores = opt->ps_job_min_cores;
	if (opt->ps_job_min_threads != NO_VAL)
		j->job_min_threads = opt->ps_job_min_threads;
	if (opt->ps_job_min_memory != NO_VAL)
		j->job_min_memory = opt->ps_job_min_memory;
	if (opt->ps_job_min_tmp_disk != NO_VAL)
		j->job_min_tmp_disk = opt->ps_job_min_tmp_disk;
	if (opt->ps_overcommit) {
		j->num_procs  = opt->ps_min_nodes;
		j->overcommit = opt->ps_overcommit;
	} else
		j->num_procs  = opt->ps_nprocs * opt->ps_cpus_per_task;
	if (opt->ps_nprocs_set)
		j->num_tasks  = opt->ps_nprocs;

	if (opt->ps_cpus_set)
		j->cpus_per_task = opt->ps_cpus_per_task;

	if (opt->ps_no_kill)
		j->kill_on_node_fail = 0;
	if (opt->ps_time_limit != NO_VAL)
		j->time_limit  = opt->ps_time_limit;
	j->shared = opt->ps_shared;

	/*
	 * srun uses the same listening port for the allocation response
	 * message as for all other message.
	 * slurmctld_comm_addr structure initialized by slurmctld_msg_init()
	 */
	j->alloc_resp_port = slurmctld_comm_addr.port;
	j->other_port = slurmctld_comm_addr.port;

	return j;
}

/*
 * Callback handlers for job allocation.
 */
static void 
timeout_handler(srun_timeout_msg_t *msg)
{
	static time_t last_timeout = 0;

	if (msg->timeout != last_timeout) {
		last_timeout = msg->timeout;
		debug_log(logfp,"callback--->timeout_handler:");
		debug_log(logfp,"job time limit to be reached at %s", 
			ctime(&msg->timeout));
	}
}

static void 
user_msg_handler(srun_user_msg_t *msg)
{
	debug_log(logfp,"callback--->usr_msg_handler: %s", msg->msg);
}

static void
ping_handler(srun_ping_msg_t *msg) 
{
	debug_log(logfp,"callback--->pingt_handler:");
}


static void 
node_fail_handler(srun_node_fail_msg_t *msg)
{
	debug_log(logfp,"callback--->node_fail_handler: Node failure on %s", msg->nodelist);
}

static void 
job_complete_handler(srun_job_complete_msg_t * msg)
{
	debug_log(logfp,"callback--->job_complete_handler: Force Terminate job\n");
}

static bool 
wait_retry()
{
	static int count = 0;

	if (count < MAX_RETRIES) {
		sleep(++count);
		return true;
	}else
		return false;
}

/*
 * Install signo handler.
 */
static SigFunc *
xsignal(int signo, SigFunc *f)
{
	struct sigaction sa, old_sa;

	sa.sa_handler = f;
	sigemptyset(&sa.sa_mask);
	sigaddset(&sa.sa_mask, signo);
	sa.sa_flags = 0;
	if (sigaction(signo, &sa, &old_sa) < 0)
		debug_log(logfp,"xsignal(%d) failed: %m", signo);

	return old_sa.sa_handler;
}

/*
 * Signal handler during allocating jobs.
 */
static void
signal_while_allocating(int signo)
{
	destroy_job = 1;
	if (sync_msg_addr && (sync_msg_addr->slurm_jobid > 0)) 
		slurm_complete_job(sync_msg_addr->slurm_jobid, 0);
}


/*
 * Pending callback during block job allocation.
 */
static void
set_pending_jobid(uint32_t id)
{
	/* 
	 * This callback can set the jobid even in the case of blocking allocation.
	 * So proxy agent can get the slurm jobid and will not block .
	 */
	 if (sync_msg_addr != NULL) {
		sync_msg_addr->slurm_jobid = (int) id;
		sync_msg_addr->jobid_set = true;
	}
}

static void 
ignore_signal(int signo)
{
	/*do nothing*/
}

/*
 * Release allocated memory in job_desc_msg.
 */
static void 
destroy_job_desc_msg(job_desc_msg_t * j)
{
	if (j != NULL) {
		if (j->account != NULL)
			free(j->account);
		if (j->req_nodes != NULL) 
			free(j->req_nodes);
		if (j->mail_user != NULL)
			free(j->mail_user);
		if (j->licenses != NULL)
			free(j->licenses);
		if (j->network != NULL)
			free(j->network);
		if (j->comment != NULL)
			free(j->comment);
		if (j->blrtsimage != NULL)
			free(j->blrtsimage);
		if (j->linuximage != NULL)
			free(j->linuximage);
		if (j->mloaderimage != NULL)
			free(j->mloaderimage);
		if (j->ramdiskimage != NULL)
			free(j->ramdiskimage);

		free(j);
	}
}

/*
 * Create a socket to communicate with slurmctld 
 * during job allocation. This fd will be closed
 * on executing srun after job allocation.
 */
slurm_fd 
slurm_init_msg_engine_port(uint16_t port)
{
	slurm_addr addr;

	int fd;
	int rc;
	const int one = 1;
	const size_t sz1 = sizeof(one);

	addr.sin_family = AF_INET;
	addr.sin_port = htons(port);
	addr.sin_addr.s_addr = htonl(INADDR_ANY);

	if ((fd = socket(AF_INET, SOCK_STREAM, IPPROTO_TCP)) < 0) {
		debug_log(logfp,"create slurmctld socket error\n");
		return fd;
	}
	rc = setsockopt(fd, SOL_SOCKET, SO_REUSEADDR,&one,sz1);
	if (rc < 0) {
		goto error;
	}
	rc = bind(fd, &addr, sizeof(addr));
	if (rc < 0) {
		goto error;
	}
	rc = listen(fd, 128);
	if(rc < 0) {
		goto error;
	}
	
	return fd;

error:
	if ((close(fd) < 0) && (errno == EINTR))
		close(fd); /*try again*/
	return rc;
}


/* 
 * Init socket fd to handle message from slurmctld.
 */
slurm_fd 
slurmctld_msg_init(srun_opt_t * opt)
{
	slurm_addr slurm_address;
	uint16_t port;
	static slurm_fd slurmctld_fd = 0;
	socklen_t name_len;
	int fval;
	int rc;
	
	if (slurmctld_fd)
		return slurmctld_fd;

	slurmctld_fd = -1;
	slurmctld_comm_addr.hostname = NULL;
	slurmctld_comm_addr.port = 0;
	
	/* open socket */
	if ((slurmctld_fd = slurm_init_msg_engine_port(0)) < 0) {
		debug_log(logfp, "slurm_init_msg_engine_port error\n");
		return -1;	
	}

	/* get socket port number */	
	name_len = sizeof(slurm_address);
	if ((rc = getsockname(slurmctld_fd, &slurm_address, &name_len)) < 0) {
		debug_log(logfp, "getsockname error\n");
		return -1;
	}

	/* set non-blocking */
	fval = fcntl(slurmctld_fd, F_GETFL, 0);
	fcntl(slurmctld_fd, F_SETFL, fval|O_NONBLOCK);

	/* set FD_CLOEXEC: close slurmctld_fd when exec(srun) after allocation */
	fval = fcntl(slurmctld_fd, F_GETFD, 0);
	fcntl(slurmctld_fd, F_SETFD, fval|FD_CLOEXEC);
	
	/* set global var.: slurmctld_comm_addr */
	slurmctld_comm_addr.hostname = strdup(opt->ps_ctrl_comm_ifhn);
	port = ntohs(slurm_address.sin_port);
	slurmctld_comm_addr.port = port;

	return slurmctld_fd;
}

/*
 * Allocate job resource in response to SubmitJob cmd.
 * Called in child process 
 */
static resource_allocation_response_msg_t *
allocate_nodes(srun_opt_t *opt)
{
	resource_allocation_response_msg_t *resp = NULL;
	slurm_allocation_callbacks_t callbacks;

	slurmctld_msg_init(opt);
	job_desc_msg_t *j = create_job_desc_msg_from_opts(opt);
	if (!j) 
		return NULL;
	
	/* 
	 * Do not re-use existing job id when submitting new job
	 * from within a running job 
	 */
	if ((j->job_id != NO_VAL) && !opt->ps_jobid_set) {
		if (!opt->ps_jobid_set)	/* Let slurmctld set jobid */
			j->job_id = NO_VAL;
	}
	callbacks.ping = ping_handler;
	callbacks.timeout = timeout_handler;
	callbacks.job_complete = job_complete_handler;
	callbacks.user_msg = user_msg_handler;
	callbacks.node_fail = node_fail_handler;

	/* create message thread to handle pings and such from slurmctld */
	msg_thr = slurm_allocation_msg_thr_create(&j->other_port, &callbacks);

	xsignal(SIGHUP, signal_while_allocating);
	xsignal(SIGINT, signal_while_allocating);
	xsignal(SIGQUIT, signal_while_allocating);
	xsignal(SIGPIPE, signal_while_allocating);
	xsignal(SIGTERM, signal_while_allocating);
	xsignal(SIGUSR1, signal_while_allocating);
	xsignal(SIGUSR2, signal_while_allocating);

//	while (!resp) 
	{
		/* 
		 * BLOCK unitl allocation granted or interrupt by signal
		 * if allocation blocked/pending, 
		 * the jobid can be returned to parent by 'set_pending_jobid' callback
		 */
		resp = slurm_allocate_resources_blocking(j, 0, set_pending_jobid);
		if (resp == NULL)
			debug_log(logfp, "blocking job allocate error!\n");
//		if (destroy_job) /* interrupt by signal */
//			break;
//	 	else if(!resp && !wait_retry()) /* time out */ 
//			break;		
	}
	

	xsignal(SIGHUP, ignore_signal);
	xsignal(SIGINT, ignore_signal);
	xsignal(SIGQUIT, ignore_signal);
	xsignal(SIGPIPE, ignore_signal);
	xsignal(SIGTERM, ignore_signal);
	xsignal(SIGUSR1, ignore_signal);
	xsignal(SIGUSR2, ignore_signal);

	destroy_job_desc_msg(j);
	return resp;
}

static int 
handle_attach_debug()
{
	return 0;
}

static int 
handle_debug()
{
	return 0;
}

/*
 * Create a thread to retrieve srun's stdout/stderr,
 * and send to ptp ui.
 */
static int 
create_iothread(ptp_job * job)
{
	pthread_attr_t attr;
	pthread_t iothread_id;
	int rc;
				
	pthread_attr_init(&attr);
	pthread_attr_setdetachstate(&attr, PTHREAD_CREATE_DETACHED);
	if (pthread_create(&iothread_id, &attr, srun_output_forwarding, (void *)job) == 0 ) {
		job->iothread_id = iothread_id;
		debug_log(logfp, "iothread created for job[%d].\n", job->slurm_jobid);
		rc = 0;
	} else {
		debug_log(logfp,"error on creating iothread for job[%d].\n", job->slurm_jobid);
		rc =  -1;
	}	

	return rc;
}

/*
 * Allocate job resource if necessary, launch job
 * and forward job stdout/stderr to ptp ui. 
 */
static int 
allocate_and_launch_job(int trans_id, char * jobsubid, ptp_job * in, srun_opt_t * opt, int srun_argc, char * srun_argv[])
{
	int i;
	int shmid;
	int fd_out[2];
	int fd_err[2];
	bool need_alloc;
	pid_t pid;
	int rc;
	char * ptr;
	sync_msg * pp, *pc;
	char * msg;
	ptp_job * job;
	char * name;
	ptp_process  * p;
	int node_id = -1;
	int task_id = -1;
	int proc_pid = -1;
	ptp_machine * m;
	ptp_node * n;
	int kill_jobid = -1;
	int allocated_jobid = -1;
	resource_allocation_response_msg_t * resp = NULL;

	/* create a shared memory region to communiate/synchronize with child process */
	if ((shmid = shmget(IPC_PRIVATE, sizeof(sync_msg), SHM_R|SHM_W)) < 0) {
		debug_log(logfp,"error on creating shared memory\n");
		return -1;
	}
	/* init shm */
	pp = (sync_msg *)shmat(shmid, 0, 0);
	pp->slurm_jobid = JOBID_INIT;
	pp->jobid_set = false; 
	pp->io_ready = false;

	pipe(fd_out);
	pipe(fd_err); 
	
	if (opt->ps_jobid == NO_VAL && opt->ps_jobid_set == false)
		need_alloc = true;
	else {
		need_alloc = false;
		allocated_jobid = opt->ps_jobid;
	}	

	switch (pid = fork()) {
	case 0:	/* child: allocate job if necessary and launch job via srun */
		/* attach shm */
		pc = (sync_msg *)shmat(shmid, 0, 0);
		/*
		 * save shm addr in a global variable.
		 * if job allocation blocked, SLURM callback can store the pending job id at this addr, 
		 * so parent can get the slurm jobid without blocking.
		 */
		sync_msg_addr = pc;

		if (need_alloc) { /* require new job allocation */
			resp = allocate_nodes(opt);			
			if (resp == NULL || resp->node_list == NULL) { /* allocation fail, rarely happen */
				pc->slurm_jobid = JOBID_FAIL;
				pc->jobid_set = true;
				shmdt(pc);
				exit(EXIT_JOB_ALLOC_FAIL); 
			}

			/* allocation granted */
			if (pc->jobid_set  == false) {
				pc->slurm_jobid = resp->job_id;
				pc->jobid_set = true;
			}	
			
			kill_jobid = resp->job_id;

			asprintf(&ptr,"%d",(int)resp->job_id);
			/* srun will no more allocate resource if SLURM_JOBID set. */
			setenv("SLURM_JOBID", ptr, 1);
			free(ptr);
			if (resp)
				slurm_free_resource_allocation_response_msg(resp);
		}
		else { 
			/* 
			 * run in allocated job (required by ATTACH debug)
			 */
			pc->slurm_jobid = allocated_jobid;
			pc->jobid_set = true;
			asprintf(&ptr,"%d",allocated_jobid);
			setenv("SLURM_JOBID", ptr, 1);
			free(ptr);
		}
		/*
		 * BLOCK until parent sets io_ready to true,
		 * which means the io thread is ready.
		 * Parent sets this flag after getting jobid and create iothread.
		 */
		while (!pc->io_ready && wait_retry()) {
			continue;
		}

		if (!pc->io_ready) {
			if (need_alloc){
				//slurm_complete_job(kill_jobid, 0);
				slurm_kill_job(kill_jobid, SIGKILL, 0);
			}	
			shmdt(pc);
			debug_log(logfp, "child exits due to iothread fail\n");
			exit(EXIT_JOB_IOTHREAD_FAIL); 
		} else { /* io thread ready */
			shmdt(pc);
			/* redirect srun's stdout and stderr */
			close(fd_out[0]);
			close(fd_err[0]);
			/* job stdout+stderr ==> srun's stdout */
			dup2(fd_out[1],1);
			/* srun outputs ==> srun's stderr */
			dup2(fd_err[1],2);
 			/* spawn job with srun cmd */
			rc = execvp(srun_argv[0], srun_argv); 
			if (rc < 0) {/* rarely happens */
				if (need_alloc) {
					//slurm_complete_job(kill_jobid, 0);
 					slurm_kill_job(kill_jobid, SIGKILL, 0);
				}	
				debug_log(logfp,"srun exec fail\n");
				exit(EXIT_EXEC_FAIL); 
			}	
		}
		break;
	case -1:/* error */
		debug_log(logfp,"child fork error\n");
		close(fd_out[0]);
		close(fd_out[1]);
		close(fd_err[0]);
		close(fd_err[1]);
		if (pp)
			shmdt(pp);
		shmctl(shmid,IPC_RMID, 0);
		return -1;
	default:/* parent */
		/* 
		 * BLOCK until child set the slurm jobid  
		 */
		while (!pp->jobid_set && wait_retry()) {
			continue;
		}
		if (!pp->jobid_set || pp->slurm_jobid == JOBID_FAIL) { 
			msg = "Job allocation fail!\nPlease check RMS and job config parameters!";
			debug_log(logfp, msg);
			sendJobSubErrorEvent(trans_id, jobsubid, msg);
			close(fd_out[0]);
			close(fd_out[1]);
			close(fd_err[0]);
			close(fd_err[1]);
			shmdt(pp);
			kill(pid, SIGKILL); 
			shmctl(shmid, IPC_RMID, 0);
			return -1;
		}

		/* ceate job structure after getting slurm jobid */
		in->slurm_jobid = pp->slurm_jobid;
		job = new_job(in->num_procs, in->debug, in->ptp_jobid, in->slurm_jobid, in->debug_jobid, need_alloc);
		job->srun_pid = pid;
		
		
		/* send OK event for SubmitJob cmd */	
		sendOKEvent(trans_id);
	
		/* send NewJob event */
		asprintf(&name, SLURM_JOB_NAME_FMT, job->slurm_jobid);
		sendNewJobEvent(gTransID, job->ptp_jobid, name, jobsubid, JOB_STATE_INIT);
		free(name);

		/*
		 * As required by ptp ui,
		 * one NewProcess Event MUST be sent for each process of this new job.
		 */
		task_id = 0;
		node_id = 0;
		proc_pid = 0;
		SetList(gMachineList);
		/* By now, only 1 machine is supported */
		m = (ptp_machine *)GetFirstElement(gMachineList);
		SetList(m->nodes);
		n = (ptp_node *)GetListElement(m->nodes);
		node_id = n->id; /* node_id calculated by generateid() */

		for (i = 0; i < job->num_procs; i++) {
			/*
			 * SLURM provide no API to get pid ,node_id and task_id.
			 * And these information can obtained ONLY after job launching.
			 * So FAKE them by now. 
			 * sendNewProcessEvent() shouldn't make use of these fileds.
			 * FIXME:
			 *	call slurm_job_step_layout_get() to obtain such information
			 *	and send to ui via sendProcessChangeEvent	
			 */
			p = new_process(job, node_id, task_id, proc_pid);
			sendNewProcessEvent(gTransID, job->ptp_jobid, p, PROC_STATE_STARTING);
			task_id += 1;
			proc_pid += 1;
		 }

		close(fd_out[1]);
		close(fd_err[1]);
		job->fd_out = fd_out[0];
		job->fd_err = fd_err[0];

		/* 
		 * Create io thread to manage srun's stderr and stdout 
	     * Don't start it before sending the NewProcess events!
	     */
		if (create_iothread(job) == 0) {
			pp->io_ready = true;
		} else {
			pp->io_ready = false;
			job->iothread_exit = true;
		}
		shmdt(pp);
		shmctl(shmid, IPC_RMID, 0);
		return 0;
	}

	return 0;
}

/*
 * Search $CWD and $PATH dir and return the absolute path of cmd. 
 */
static char *
get_path(char * cmd, char * cwd, int mode)
{
	char * path = NULL;
	char * fullpath = NULL;
	char * c;
	char * ptr;

	if ( (cmd[0] == '.' || cmd[0] == '/')
		&& access(cmd, mode) == 0) {
		if (cmd[0] == '.') 
			asprintf(&fullpath,"%s/%s",cwd,cmd);
		else 
			asprintf(&fullpath,cmd);
	} else {
		/* search $PATH */
		path = getenv("PATH");
		if (path != NULL) {
			c = ptr = path;
			while((c=strchr(ptr,':')) != NULL) {
				*c = '\0';
				asprintf(&fullpath, "%s/%s",ptr,cmd);
				if (access(fullpath, mode) == 0)
					break;
				else {
					free(fullpath);
					fullpath = NULL;
					ptr = c + 1;
				}	
			}
			/* handle the last element */
			if (*ptr != '\0') {
				asprintf(&fullpath, "%s/%s",ptr,cmd);
				if (access(fullpath, mode) != 0) {
					free(fullpath);
					fullpath = NULL;
				}	
			}
		}
	}
	return fullpath;
}

ssize_t fd_read_line(int fd, void *buf, size_t maxlen)
{
    ssize_t n, rc;
    unsigned char c, *p;

    n = 0;
    p = buf;
    while (n < maxlen - 1) {   /* reserve space for NUL-termination */
		if ((rc = read(fd, &c, 1)) == 1) {
			n++;
			*p++ = c;
			if (c == '\n')
				break;         /* store newline, like fgets() */
		}else if (rc == 0) {
			if (n == 0)        /* EOF, no data read */
				return(0);
			else               /* EOF, some data read */
				break;
		}else {
			if (errno == EINTR)
				continue;
			return(-1);
		}
	}

	*p = '\0';  /* NULL-terminate, like fgets() */
	return(n);
}

/*
 * Forwarding srun's stdout to ptp ui
 * FIXME:
 * 		How about srun's stderr? By now, it's ignored.
 */
void *
srun_output_forwarding(void * arg)
{
	int fd = -1;
	char * ptr;
	ptp_job * job;
	int task_id;
	fd_set rfds;
	struct timeval tv;
	char buf[MAX_BUF_SIZE];
	FILE * fp = NULL;
	int cancel_state;
	int cancel_type;
	int	ret;
	char * p;
	pid_t cpid;
	int status;
	
	pthread_setcancelstate(PTHREAD_CANCEL_ENABLE,&cancel_state);
	pthread_setcanceltype(PTHREAD_CANCEL_DEFERRED, &cancel_type);
	
	job = (ptp_job *)arg;
	job->iothread_exit = false;
	/* By now only focus on job stdout */
	fd = job->fd_out;
	
	while (1)
	{
		if (job->iothread_exit_req) {
			debug_log(logfp, "job[%d] iothread exit on exit_request.\n", job->slurm_jobid);
			job->iothread_exit = true;
			if (fp)
				fclose(fp);
			pthread_exit(NULL);
		}
		
		tv.tv_sec = 0;
		tv.tv_usec = 5000;
		
		/* 
		 * if timeout, the rfds will be cleared 
		 * rfds must be set in each iteration
		 */
		FD_ZERO(&rfds);
		FD_SET(fd, &rfds);
		ret = select(fd+1,&rfds,NULL,NULL,&tv);

		switch(ret)
		{
		case -1: /* error */
			debug_log(logfp,"job[%d] iothread exit on select error\n",job->slurm_jobid);
			if (fp)
				fclose(fp);
			job->iothread_exit = true;
			pthread_exit(NULL);
			break;

		case 0: /* timeout */
			//debug_log(logfp,"select timeout\n");
			break;
				
		default: /* fd ready */
			if (fp == NULL)
				fp = fdopen(fd,"r");
			if (fgets(buf, sizeof(buf), fp) != NULL) {
			//if ((rc = fd_read_line(fd, buf, sizeof(buf)))> 0 ) {
				p = buf;
				/* get task id from srun label */
				task_id = atoi(p);
				ptr = strchr(p, ':');
				ptr ++;
				if (ptr != NULL) {
					//debug_log(logfp,"send task[%d] output to ptp ui\n", task_id);
					/* 
					 * no synchronization needed 
					 * since the event list is internally protected by pthread_mutex
					 */
					ptp_process * proc;
					proc = find_process(job, task_id);
					sendProcessOutputEvent(gTransID, proc->id, ptr);
				} else 
					debug_log(logfp,"process label not found\n");
			} else { /* error or EOF of pipe(write end closed) */
				debug_log(logfp,"EOF of pipe\n");
				if (fp)
					fclose(fp);
				job->iothread_exit = true;
				cpid = waitpid(job->srun_pid, &status, 0);
				if (cpid == job->srun_pid) {
					if (WIFEXITED(status)) {
						if (job->need_alloc)
							slurm_complete_job(job->slurm_jobid, 0);
						debug_log(logfp, "srun exit code:%d\n", WEXITSTATUS(status));
					}
					else if (WIFSIGNALED(status)) {
						if (job->need_alloc)
							slurm_kill_job(job->slurm_jobid, SIGKILL, 0);
						debug_log(logfp, "srun terminated by signal[%d]\n", WTERMSIG(status));
					}
					pthread_exit(NULL);
				}
			}	
			break; 
		}	
	}		
	
	return NULL;
}

/*
 * Delete removable job fro gJobList.
 */
static void 
purge_global_joblist()
{
	ptp_job * j;
	
	for (SetList(gJobList); (j = (ptp_job *)GetListElement(gJobList)) != NULL; ) {
		if (j->iothread_exit && !slurm_job_active(j))
			j->removable = true;
		if (j->removable) {
			RemoveFromList(gJobList, j);
			free_job(j);
		}	
	}
	return;
}


/******************************
 * START OF DISPATCH ROUTINES *
 ******************************/

int
SLURM_Initialize(int trans_id, int nargs, char **args)
{
	int		i;
	int 	primary = 1;
	int 	secondary = 2;
	long	version;
	
	debug_log(logfp, "SLURM_Initialize (%d):\n", trans_id);
	
	if (proxy_state != STATE_INIT) {
		sendErrorEvent(trans_id, RTEV_ERROR_SLURM_INIT, "Already initialized");
		return PROXY_RES_OK;
	}
	
	for (i = 0; i < nargs; i++) {
		if (proxy_test_attribute(PROTOCOL_VERSION_ATTR, args[i])) {
			if (strcmp(proxy_get_attribute_value_str(args[i]), WIRE_PROTOCOL_VERSION) != 0) {
				sendErrorEvent(trans_id, RTEV_ERROR_SLURM_INIT, "Wire protocol version \"%s\" not supported", args[0]);
				return PROXY_RES_OK;
			}
		} else if (proxy_test_attribute(BASE_ID_ATTR, args[i])) 
			gBaseID = proxy_get_attribute_value_int(args[i]);
	}

	if (gBaseID < 0) {
		sendErrorEvent(trans_id, RTEV_ERROR_SLURM_INIT, "No base ID supplied");
		return PROXY_RES_OK;
	}
	
 	/* confirm slurmctld works well via slurm_ping */ 
	if (slurm_ping(primary) && slurm_ping(secondary)) {
		sendErrorEvent(trans_id, RTEV_ERROR_SLURM_INIT, "No response from slurmctld. Check SLURM RMS!");
		return PROXY_RES_OK;
	}	
  	/* 
	 * SLURM version verfication,
	 * Should work on more versions supporting used API. 
	 */
	 version = slurm_api_version();
	 if (version < SLURM_VERSION_NUM(1,3,4)) {
		sendErrorEvent(trans_id, RTEV_ERROR_SLURM_INIT, "SLURM version number too low");
		return PROXY_RES_OK;
	 }

	proxy_state = STATE_RUNNING;
	sendOKEvent(trans_id);
	
	return PROXY_RES_OK;
}

/*
 * Init the model definition phase.
 */
int
SLURM_ModelDef(int trans_id, int nargs, char **args)
{
	debug_log(logfp, "SLURM_ModelDef (%d):\n", trans_id); 
	
	sendOKEvent(trans_id);
	return PROXY_RES_OK;
}

int
SLURM_StopEvents(int trans_id, int nargs, char **args)
{
	debug_log(logfp, "SLURM_StopEvents (%d):\n", trans_id); 

	/* Notify that tartEvents complete */
	sendOKEvent(gTransID);
	gTransID = 0;
	sendOKEvent(trans_id);
	return PROXY_RES_OK;	
}


/*
 * Submit a job with the given executable path and arguments. 
 * (1)Process cmd arguments;
 * (2)Distinguish between debug job and non-debug job;
 * (3)Allocate resource and spawn job step.
 */
int
SLURM_SubmitJob(int trans_id, int nargs, char **args)
{
	int		i,k;
	int		num_args = 0;
	int		num_env = 0;
	char *	jobsubid = NULL; /* jobid assigned by RMS(ui) */
	
	/* srun options: -n, -N, -t, -p, -l, -v, --jobid, -w, -x, --job_type */
	int		num_procs = 0; /* -n: number of tasks to run*/
	int		num_nodes = 0; /* -N: number of nodes on which to run (N=min[-max]) */
	int		tlimit = -1;   /* -t: time limit */		
	bool 	tlimit_set = false;
	char * 	partition = NULL; /* -p: partition requested */
	bool	partition_set = false;
	bool	io_label = false; /* -l: prepend task number to lines of stdout/err */
	bool 	io_label_set = false;
	bool 	verbose = false;	/* -v:verbose mode */
	bool	verbose_set = false;
	char * 	node_list=NULL;	/* -w: request a specific list of hosts */
	bool	nodelist_set = false;
	int		allocated_jobid; /* --jobid: run under already allocated job */
	bool	jobid_set = false;
	char * 	jobtype = NULL;	/* --job_type:mpi,omp,serial */
	bool	jobtype_set = false;

	char *	full_path = NULL;
	char *	cwd = NULL;
	char *	exec_path = NULL; /* PATH of executable */
	char *	pgm_name = NULL; 
	
	/* debug job support */ 
	bool	debug = false;
	int		debug_argc = 0;
	int		attach_mode;
	char *	debug_exec_name = NULL;
	char *	debug_exec_path = NULL;
	char *	debug_full_path;
	char **	debug_args = NULL;

	int		srun_argc = 0;
	char ** srun_argv = NULL;
	int 	ret;
	int		ptpid = generate_id();

	debug_log(logfp, "SLURM_SubmitJob (%d):\n", trans_id);
	/* Process job submit args  */
	debug_log(logfp, "job submit commands:\n");
	for (i = 0; i < nargs; i++) {
		debug_log(logfp, "\t%s\n", args[i]);
		if (proxy_test_attribute(SLURM_JOB_SUB_ID_ATTR, args[i])) {
			jobsubid = proxy_get_attribute_value_str(args[i]);
		} else if (proxy_test_attribute(SLURM_JOB_NUM_PROCS_ATTR, args[i])) {
			num_procs = proxy_get_attribute_value_int(args[i]);
		} else if (proxy_test_attribute(SLURM_JOB_NUM_NODES_ATTR, args[i])) {
			num_nodes = proxy_get_attribute_value_int(args[i]);
		} else if (proxy_test_attribute(SLURM_JOB_TIME_LIMIT_ATTR, args[i])) {
			tlimit_set = true;
			tlimit = proxy_get_attribute_value_int(args[i]);
		} else if (proxy_test_attribute(SLURM_JOB_PARTITION_ATTR, args[i])) {
			partition_set = true;
			partition = proxy_get_attribute_value_str(args[i]);
		} else if (proxy_test_attribute(SLURM_JOB_IOLABEL_ATTR, args[i])) {
			io_label_set = true;
			io_label = proxy_get_attribute_value_bool(args[i]);
		} else if (proxy_test_attribute(SLURM_JOB_VERBOSE_ATTR, args[i])) {
			verbose_set = true;
			verbose = proxy_get_attribute_value_bool(args[i]);
		} else if (proxy_test_attribute(SLURM_JOB_ID_ATTR, args[i])) {
			jobid_set = true;
			allocated_jobid = proxy_get_attribute_value_int(args[i]);
		} else if (proxy_test_attribute(SLURM_JOB_TYPE_ATTR, args[i])) {
			jobtype_set = true;
			jobtype = proxy_get_attribute_value_str(args[i]);
		} else if (proxy_test_attribute(SLURM_JOB_NODELIST_ATTR, args[i])) {
			nodelist_set = true;
			node_list = proxy_get_attribute_value_str(args[i]);
		} else if (proxy_test_attribute(SLURM_JOB_EXEC_NAME_ATTR, args[i])) {
			pgm_name = proxy_get_attribute_value_str(args[i]);
		} else if (proxy_test_attribute(SLURM_JOB_EXEC_PATH_ATTR, args[i])) {
			exec_path = proxy_get_attribute_value_str(args[i]);
		} else if (proxy_test_attribute(SLURM_JOB_WORKING_DIR_ATTR, args[i])) {
			cwd = proxy_get_attribute_value_str(args[i]);
		} else if (proxy_test_attribute(SLURM_JOB_PROG_ARGS_ATTR, args[i])) {
			num_args++;
		} else if (proxy_test_attribute(SLURM_JOB_ENV_ATTR, args[i])) {
			num_env++;
		} else if (proxy_test_attribute(SLURM_JOB_DEBUG_ARGS_ATTR, args[i])) {
			debug_argc++;
		} else if (proxy_test_attribute(SLURM_JOB_DEBUG_FLAG_ATTR, args[i])) {
			debug = proxy_get_attribute_value_bool(args[i]);
		}
	}

	/* Do some checking first */
	if (jobsubid == NULL) {
		sendErrorEvent(trans_id, RTEV_ERROR_SLURM_SUBMIT, "missing ID on job submission");
		return PROXY_RES_OK;
	}
	if (proxy_state != STATE_RUNNING) {
		sendJobSubErrorEvent(trans_id, jobsubid, "must call INIT first");
		return PROXY_RES_OK;
	}
	if (nargs < 1) {
		sendJobSubErrorEvent(trans_id, jobsubid, "incorrect arg count");
		return PROXY_RES_OK;
	}
	if (pgm_name == NULL) {
		sendJobSubErrorEvent(trans_id, jobsubid, "must specify a program name");
		return PROXY_RES_OK;
	}
	if (num_procs <= 0) {
		sendJobSubErrorEvent(trans_id, jobsubid, "must specify number of task to launch");
		return PROXY_RES_OK;
	}

	/*
	 * Process environment variables. 
	 * Environment variables will be brought to compute node 
	 * by SLURM before launching job. 
	 */
	if (num_env > 0) {
		for (i = 0; i < nargs; i++) {
			if (proxy_test_attribute(SLURM_JOB_ENV_ATTR, args[i]))
				putenv(proxy_get_attribute_value_str(args[i]));
		}
	}
	/* locate execuable */
	if (exec_path == NULL) {
		full_path = get_path(pgm_name, cwd, X_OK);
		if (full_path == NULL) {
			sendJobSubErrorEvent(trans_id, jobsubid, "executable not found");
			return PROXY_RES_OK;
		}
	} else 
		asprintf(&full_path, "%s/%s", exec_path, pgm_name);
	/* check access right */		
	if (access(full_path, X_OK) < 0) {
		sendJobSubErrorEvent(trans_id, jobsubid, strerror(errno));
		if (full_path != NULL)
			free(full_path);
		return PROXY_RES_OK;
	}
	
	/* allocate space for srun args */
	srun_argv = (char **)malloc(sizeof(char *)*MAX_SRUN_ARG_NUM);
	if (srun_argv == NULL) {
		debug_log(logfp, "memory allocation for srun_argv fail");
		sendJobSubErrorEvent(trans_id, jobsubid, "memory allocation for srun_args fail");
		return PROXY_RES_OK;
	}

	/*
	 *FIXME: more debug job support will be added soon.
	 */
	/****************handle debug job*****************/
	if (debug) {		
		debug_argc++;
		debug_args = (char **)malloc((debug_argc+1) * sizeof(char *));
		for (i = 0, k = 1; i < nargs; i++) {
			if (proxy_test_attribute(SLURM_JOB_DEBUG_ARGS_ATTR, args[i])) {
				debug_args[k++] = proxy_get_attribute_value_str(args[i]);
			} else if (proxy_test_attribute(SLURM_JOB_DEBUG_EXEC_NAME_ATTR, args[i])) {
				debug_exec_name = proxy_get_attribute_value_str(args[i]);
			} else if (proxy_test_attribute(SLURM_JOB_DEBUG_EXEC_PATH_ATTR, args[i])) {
				debug_exec_path = proxy_get_attribute_value_str(args[i]);
			}
		}
		debug_args[k] = NULL;
		
		/*
		 * If no path specified, try to locate execuable.
		 */		
		if (debug_exec_path == NULL) {
			debug_full_path = get_path(debug_exec_name, cwd, X_OK);
			if (debug_full_path == NULL) {
				sendJobSubErrorEvent(trans_id, jobsubid, "Debugger executuable not found");
				return PROXY_RES_OK;
			}
		} else {
			asprintf(&debug_full_path, "%s/%s", debug_exec_path, debug_exec_name);
		}
		
		if (access(debug_full_path, X_OK) < 0) {
			sendJobSubErrorEvent(trans_id, jobsubid, strerror(errno));
			if (debug_full_path != NULL)
				free(debug_full_path);
			return PROXY_RES_OK;
		}

		debug_args[0] = strdup(debug_full_path);
		if (debug_full_path != NULL)
			free(debug_full_path);
	}
	/*******************************************************/

	/* set default srun options */
	set_srun_options_defaults(&opt);

	/*
	 * change srun options based on SubmitJob cmd args
	 * and prepare srun_argc,srun_argv
	 */
	int index = 0;
	srun_argv[index++] = strdup("srun");

	if (num_procs > 0) {
		opt.ps_nprocs = num_procs;
		opt.ps_nprocs_set = true;
		asprintf(&(srun_argv[index]), "--ntasks=%d", opt.ps_nprocs);
		index += 1;
	}	
	if (num_nodes > 0) {
		opt.ps_min_nodes = num_nodes;
		opt.ps_nodes_set = true;
		asprintf(&(srun_argv[index]), "--nodes=%d", opt.ps_min_nodes);
		index += 1;
	}
	if (tlimit_set) {
		opt.ps_time_limit = tlimit;
		asprintf(&(srun_argv[index]), "--time=%d", opt.ps_time_limit);
		index += 1;
	}
	if (partition_set) {
		opt.ps_partition =  partition;
		asprintf(&(srun_argv[index]), "--partition=%s", opt.ps_partition);
		index += 1;
	}
	/* To distinguish task stdout, this option MUST be set */
	io_label_set = true;
	if (io_label_set) { 
		opt.ps_labelio = io_label;
		asprintf(&(srun_argv[index]), "--label");
		index += 1;
	}
	if (verbose_set) {
		asprintf(&(srun_argv[index]), "--verbose");
		index += 1;
	}
	if (jobid_set) {
		opt.ps_jobid = allocated_jobid;
		opt.ps_jobid_set = true;
		asprintf(&(srun_argv[index]), "--jobid=%d", opt.ps_jobid);
		index += 1;
	}
	
	/*if (jobtype_set) {
		opt.ps_jobtype = jobtype;
    	asprintf(&(srun_argv[index]), "--jobtype=%s", opt.ps_jobtype);
		index += 1;
	}
	*/
	if (nodelist_set) {
		opt.ps_nodelist = strdup(node_list);
		opt.ps_nodes_set = true;
		asprintf(&(srun_argv[index]), "--nodelist=%s", opt.ps_nodelist);
		index += 1;
	}

	if (cwd) {
		if (opt.ps_cwd != NULL)
			free(opt.ps_cwd);
		opt.ps_cwd = strdup(cwd);
		opt.ps_cwd_set = true;
	}

	opt.ps_progname = full_path;
	/* set job name,otherwise be NULL */
	opt.ps_job_name = basename(full_path);
	asprintf(&(srun_argv[index]), "%s", opt.ps_progname);
	index += 1;
	
	/* program name followd by args */
	if (num_args > 0) {
		for (i = 0; i < nargs; i++) {
			if (proxy_test_attribute(SLURM_JOB_PROG_ARGS_ATTR, args[i])) {
				asprintf(&(srun_argv[index]), "%s", proxy_get_attribute_value_str(args[i]));
				index += 1;
			}	
		}
	}
	srun_argv[index] = NULL; /* mark the end of srun_argv[] */

	srun_argc = index;	
	debug_log(logfp, "srun cmd:");
	for (i = 0; i < srun_argc; i++) 
		debug_log(logfp,"%s ",srun_argv[i]);
	debug_log(logfp, "\n");


	/*
	 * FIXME: By now, debug job is not supported.
	 */
	if (debug) {
		if (attach_mode) {
			ret =  handle_attach_debug();
		} else {
			ret = handle_debug();
		}	
	} else {
		ptp_job j;
		j.debug = debug;
		j.ptp_jobid = ptpid; /* model element id generated by proxy agent */
		j.num_procs = num_procs;
		allocate_and_launch_job(trans_id, jobsubid, &j, &opt, srun_argc, srun_argv);
		free_opt(&opt);
		free_srun_argv(srun_argc,srun_argv);
	}

	return PROXY_RES_OK;
}		

/* 
 * Cancel job, given ptp jobid (not slurm jobid).
 */
int
SLURM_TerminateJob(int trans_id, int nargs, char **args)
{
	int			i;
	int 		ptp_jobid = -1;
	ptp_job * 	j;

	
	if (proxy_state != STATE_RUNNING) {
		sendErrorEvent(trans_id, RTEV_ERROR_JOB, "must call INIT first");
		return PROXY_RES_OK;
	}
	
	for (i = 0; i < nargs; i++) {
		if (proxy_test_attribute(JOB_ID_ATTR, args[i])) {
			ptp_jobid = proxy_get_attribute_value_int(args[i]);
			break;
		}
	}

	if (ptp_jobid < 0) {
		sendJobTerminateErrorEvent(trans_id, ptp_jobid, "invalid jobid ");
		return PROXY_RES_OK;
	}

	/* convert ptp jobid to slurm jobid */
	if ((j = find_job(ptp_jobid, PTP_JOBID)) != NULL) {
		/*
		 * Kill all job steps and request iothread to exit.
		 * Removing job structure from the global job list 
		 * is left to purge_global_joblist().
		 */
		kill(j->srun_pid, SIGKILL);
		j->iothread_exit_req = true;
		if (j->need_alloc) {
			slurm_kill_job(j->slurm_jobid, SIGKILL, 0); 
		}	
	} 
	sendOKEvent(trans_id);

	return PROXY_RES_OK;
}

/*
 * Enable sending of events.
 * The first msg that must be sent is a description of the model. 
 * This comprises NEW model element events
 * (NewMachine, NewNode, NewQueue) for each element in the model. 
 * Once the model description has been sent, model change events will be sent as detected.
 */
int
SLURM_StartEvents(int trans_id, int nargs, char **args)
{
	int 			num_machines;
	int				m;
	ptp_machine *	mach;
	int				num_nodes;
	
	debug_log(logfp, "SLURM_StartEvents (%d):\n", trans_id); 

	if (proxy_state != STATE_RUNNING) {
		sendErrorEvent(trans_id, RTEV_ERROR_SLURM_INIT, "Must call INIT first");
		return PROXY_RES_OK;
	}

	/* NodeChange, JobChange event use gTransID as TID to match START_EVENTS cmd */
	gTransID = trans_id;
	
	/*
	 * FIXME: how to handle partition information in SLURM?
	 */
	num_machines = get_num_machines();
	for(m = 0; m < num_machines; m++) {
		mach = new_machine();
		/* NewMachine element */
		sendNewMachineEvent(trans_id, mach->id, get_machine_name(m));
		num_nodes = get_num_nodes(mach->id);
		if(create_node_list(mach)) {
			sendErrorEvent(trans_id, RTEV_ERROR_NATTR, "Fail to create nodelist");
			return PROXY_RES_OK;
		}
		/* NewNode element */
		sendNewNodeEvent(trans_id, mach->id, mach);
	}
	/* NewQueue element */
	sendNewQueueEvent(trans_id);

	/* From now on, job state and node state update msg can be sent */
	enable_state_update = true;
	
	return PROXY_RES_OK;
}


/*
 * Compitable interface.
 * Proxy not allowed to stop SLURM rms.
 */
int
SLURM_Quit(int trans_id, int nargs, char **args)
{
	int old_state = proxy_state;
	
	debug_log(logfp, "SLURM_Quit called\n");
	
	proxy_state = STATE_SHUTTING_DOWN;
	if (old_state == STATE_RUNNING) 
		do_slurm_shutdown();
	
	sendShutdownEvent(trans_id);
	
	return PROXY_RES_OK;
}

/******************************
 * END OF DISPATCH ROUTINES *
 ******************************/

/*
 * Init jobstate_update_timer.
 */
static void 
init_job_timer()
{
	gettimeofday(&job_update_timer, NULL);
	return;
}

/*
 * Init nodestate_update_timer.
 */
static void 
init_node_timer()
{
	gettimeofday(&node_update_timer, NULL);
	return;
}

/*
 * Check if timer expires given timeout value.
 */
static bool
update_timeout(int timer_id, const int timeout)
{
	struct timeval * timer;
	struct timeval now;
	int val;
	bool rc = false;

	switch (timer_id) {
	case JOB_UPDATE_TIMER:
		timer = &job_update_timer;
		break;
	case NODE_UPDATE_TIMER:
		timer = &node_update_timer;
		break;
	default:
		return false; 
	}
	gettimeofday(&now, NULL);
	val = (now.tv_sec - timer->tv_sec) * 1000000 + (now.tv_usec - timer->tv_usec) - timeout;
	if (val >= 0) {
		/* update timer */
		gettimeofday(timer, NULL);
		rc = true;
	}

	return rc;
}

/*
 * Wrapper routine to check job_update_timer.
 */
static bool
job_update_timeout()
{
	return update_timeout(JOB_UPDATE_TIMER, JOB_UPDATE_TIMEOUT);
}

/*
 * Wrapper routine to check node_update_timer.
 */
static bool
node_update_timeout()
{
	return update_timeout(NODE_UPDATE_TIMER, NODE_UPDATE_TIMEOUT);
}

/*
 * Update job/process state and send state CHANGE to ui.
 */
static void 
update_job_state(int slurm_jobid)
{
	int i;
	int errcode;
	bool job_find;
	ptp_job * j;
	job_info_msg_t * msg = NULL;

	errcode = slurm_load_jobs((time_t)NULL, &msg, SHOW_ALL);
	if (errcode) {
		debug_log(logfp,"slurm_load_jobs error");
		return;
	}

	for (SetList(gJobList); (j = (ptp_job *)GetListElement(gJobList)) != NULL;) {
		if (slurm_jobid > -1) { 
			if (j->slurm_jobid != slurm_jobid)
				continue;
		}
		job_find = false;
		for (i = 0; i < msg->record_count; i++) {
			if (j->slurm_jobid == (msg->job_array[i]).job_id) {
				job_find = true;
				if (j->state != (msg->job_array[i]).job_state) { /*state change*/
					j->state = (msg->job_array[i]).job_state;
					/* 
					 * SLURM doesn't provide process state.
					 * Force process state changs with job state. 
					 */
					sendProcessStateChangeEvent(gTransID, j, jobstate_to_string(j->state));
					sendJobStateChangeEvent(gTransID, j->ptp_jobid, jobstate_to_string(j->state));
				}
				break;
			}	
		}
		if (!job_find) { 
			/*
			 * job not found(rarely happens).
			 * In this case, simply mark this job removable.
			 * SLURM keep the informatin of complete/fail jobs for MinJobAge (default to 300) seconds
			 * MinJobAge can be set in slurm/etc/slurm.conf.
			 */
			j->removable = true;
		}
		if (slurm_jobid > -1)
			break;
	}
	slurm_free_job_info_msg(msg);

	return;
}

/*
 * Update ALL nodes state and send state CHANGE to ui.
 */
void
update_node_state()
{
	int i;
	ptp_node * node;
	int errcode;
	node_info_msg_t * msg;
	rangeset * unknown_set = new_rangeset();
	rangeset * idle_set = new_rangeset();
	rangeset * down_set = new_rangeset();
	rangeset * allocated_set = new_rangeset();

	if (unknown_set == NULL || idle_set == NULL || down_set == NULL || allocated_set == NULL)
		goto cleanup;

	errcode = slurm_load_node((time_t)NULL, &msg, SHOW_ALL);
	if (errcode) {
		debug_log(logfp,"slurm_load_node error.\n");
		return;
	} else {
		for (i = 0; i < msg->record_count; i++) {
			node = find_node_by_name(msg->node_array[i].name);
			if (node->state == msg->node_array[i].node_state)
				continue;
			else { /* node state change */
				node->state = msg->node_array[i].node_state;
				switch (msg->node_array[i].node_state & NODE_STATE_BASE) {
				case NODE_STATE_UNKNOWN:
					insert_in_rangeset(unknown_set,node->id);
					break;
				case NODE_STATE_DOWN:
					insert_in_rangeset(down_set,node->id);
					break;
				case NODE_STATE_IDLE:
					insert_in_rangeset(idle_set,node->id);
					break;
				case NODE_STATE_ALLOCATED:
					insert_in_rangeset(allocated_set,node->id);
					break;
				default:
					debug_log(logfp, "unrecognized node state\n");
					break;
				}	
			}
		}	
			
		if (!EmptyList(unknown_set->elements)) {
			sendNodeChangeEvent(gTransID,rangeset_to_string(unknown_set),nodestate_to_string(NODE_STATE_UNKNOWN));
		}
		if (!EmptyList(down_set->elements)) {
			sendNodeChangeEvent(gTransID,rangeset_to_string(down_set),nodestate_to_string(NODE_STATE_DOWN));
		}
		if (!EmptyList(idle_set->elements)) {
			sendNodeChangeEvent(gTransID,rangeset_to_string(idle_set),nodestate_to_string(NODE_STATE_IDLE));
		}
		if (!EmptyList(allocated_set->elements)) {
			sendNodeChangeEvent(gTransID,rangeset_to_string(allocated_set),nodestate_to_string(NODE_STATE_ALLOCATED));
		}

		slurm_free_node_info_msg(msg);
	}

cleanup:
	if(unknown_set)
		free_rangeset(unknown_set);
	if(down_set)	
		free_rangeset(down_set);
	if(idle_set)	
		free_rangeset(idle_set);
	if (allocated_set)	
		free_rangeset(allocated_set);

	return;
}


/*
 * signal handler of slurm proxy. 
 */
RETSIGTYPE
ptp_signal_handler(int sig)
{
	if (sig != SIGCHLD)  /* proxy doesn't exit on SIGCHLD */ 
		ptp_signal_exit = sig;
}

/*
 * Cleanup work on proxy exiting:
 *  kill srun process, release job resource, 
 *	terminate io_thread,and free space.
 */
static void
destroy_global_joblist()
{
	ptp_job * j;
	
	for (SetList(gJobList); (j = (ptp_job *)GetListElement(gJobList)) != NULL; ) {
		if (slurm_job_active(j)) {
			kill(j->srun_pid, SIGKILL);
			slurm_kill_job(j->slurm_jobid, SIGKILL, 0);
		}	
		if (j->iothread_exit == false)
			j->iothread_exit_req = true;
		RemoveFromList(gJobList, j);
		free_job(j);
	}
	return;
}

static int
server(char *name, char *host, int port)
{
	int				rc = 0;
	struct timeval	timeout = {0, 20000};

	gJobList = NewList();
	gMachineList = NewList();
	
	init_job_timer();
	init_node_timer();

	if (proxy_svr_init(name, &timeout, &helper_funcs, &command_tab, &slurm_proxy) != PROXY_RES_OK) {
		debug_log(logfp, "proxy failed to initialized\n"); 
		return 0;
	}
	
	if (proxy_svr_connect(slurm_proxy, host, port) == PROXY_RES_OK) {
		debug_log(logfp, "proxy connected\n"); 
		
		while (ptp_signal_exit == 0 && proxy_state != STATE_SHUTDOWN) {
			if (proxy_state == STATE_SHUTTING_DOWN) {
				proxy_state = STATE_SHUTDOWN;
			}
			if (proxy_svr_progress(slurm_proxy) != PROXY_RES_OK)
				break;
			/* update job and node state */	
			if (enable_state_update) {
				if (job_update_timeout())  
					update_job_state(ALL_JOBSTATE); 
				if (node_update_timeout()) 
					update_node_state();
			}
			/* delete removable job */
			purge_global_joblist();
		}
		if (ptp_signal_exit != 0) {
			if (proxy_state != STATE_SHUTTING_DOWN
				&& proxy_state != STATE_SHUTDOWN) {
				do_slurm_shutdown();
			}
			
			destroy_global_joblist();

			/* our return code = the signal that fired */
			rc = ptp_signal_exit;
			debug_log(logfp, "ptp_slurm_proxy terminated by signal [%d]\n", ptp_signal_exit);
		}
	} else 
		debug_log(logfp, "proxy connection failed\n"); 
	
	proxy_svr_finish(slurm_proxy);
	
	return rc;
}


/*
 * Entry routine
 */
int
main(int argc, char *argv[])
{
	int 	ch;
	int		port = PROXY_TCP_PORT;
	char *	host = DEFAULT_HOST;
	char *	proxy_str = DEFAULT_PROXY;
	int		rc;
	
	while ((ch = getopt_long(argc, argv, "P:p:h:", longopts, NULL)) != -1){ 
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
		default:
			fprintf(stderr, "%s [--proxy=proxy] [--host=host_name] [--port=port] \n", argv[0]);
			return 1;
		}
	}	

	//putenv("PTP_SLURM_PROXY_LOGDIR=$HOME/log");
	logfp = init_logfp();

	/* 
	 * signal can happen any time after handlers are installed, so
	 * make sure we catch it.
	 */
	ptp_signal_exit = 0;
	
	/* setup signal handlers */
	xsignal(SIGINT, ptp_signal_handler);
	xsignal(SIGHUP, ptp_signal_handler);
	xsignal(SIGILL, ptp_signal_handler);
	xsignal(SIGSEGV, ptp_signal_handler);
	xsignal(SIGTERM, ptp_signal_handler);
	xsignal(SIGQUIT, ptp_signal_handler);
	xsignal(SIGABRT, ptp_signal_handler);
	xsignal(SIGCHLD, ptp_signal_handler);
	
	rc = server(proxy_str, host, port);
	
	return rc;
}
