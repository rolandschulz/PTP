/********************************************************************************
 * Copyright (c) 2008,2009 School of Computer Science, 
 * National University of Defense Technology, P.R.China
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


//#ifndef USE_POSIX_THREADS
//#define USE_POSIX_THREADS /*Use thread-safe List operation*/
//#endif


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
#include <sys/mman.h>
#include <sys/ptrace.h>
#include <sys/utsname.h>
#include <linux/elf.h>
#include <pthread.h>


#include "proxy.h"
#include "proxy_tcp.h"
#include "handler.h"
#include "list.h"
#include "args.h"
#include "rangeset.h"

#ifdef SLURM_VERSION_2_2
#define List SLURM_List
#endif

#include "slurm/slurm.h"

#ifdef SLURM_VERSION_2_2
#undef List
#endif
#include "slurm/slurm_errno.h"
#include "job_opt.h"


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

#define MAX_RETRY 					100
#define JOB_UPDATE_TIMER 			1
#define NODE_UPDATE_TIMER 			2
#define JOB_UPDATE_TIMEOUT			500000 	/*usec*/
#define NODE_UPDATE_TIMEOUT			500000 	/*usec*/

/* job state/status string */
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
#define SLURM_JOB_NODELIST_ATTR				"jobReqList"  	//-w
#define SLURM_JOB_EXCNODELIST_ATTR			"jobExcList"	//-x
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

/* node state/status string */
#define SLURM_NODE_STATE_UNKNOWN		"UNKNOWN"
#define SLURM_NODE_STATE_DOWN			"DOWN"
#define SLURM_NODE_STATE_UP				"UP"
#define SLURM_NODE_STATE_ERROR			"ERROR"

#define SLURM_NODE_STATUS_IDLE			"IDLE"
#define	SLURM_NODE_STATUS_ALLOCATED		"ALLOCATED"
#define SLURM_NODE_STATUS_UNKNOWN		"UNKNOWN"
#define SLURM_NODE_STATUS_DOWN			"DOWN"
#define SLURM_NODE_STATUS_ERROR			"ERROR"
#define SLURM_NODE_STATUS_MIXED			"MIXED"
#define SLURM_NODE_STATUS_FUTURE		"FUTURE"

/* node attribute string */
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

#define BASEPORT   32768 


struct ptp_machine {
	int		id;
	List *	nodes;
	pthread_mutex_t mutex; /*lock to protect the machine's nodes list*/
};
typedef struct ptp_machine	ptp_machine;

struct ptp_slurm_node {
	int 		id;  	/* model element id, generated by proxy agent */
	int			number;	/* node number, assigned by SLURM */
	char *		name;
	uint16_t	state; 	/* (uint16_t)node_info_t.state, converted to (char *) */
	uint16_t 	sockets;
	uint16_t 	cores;
	uint16_t 	threads;
	char *		arch;
	char *		os;
};
typedef struct ptp_slurm_node	ptp_node;

struct ptp_slurm_process {
	void * job; 		/* ptr to job  the process belongs to */
	int		id;
	int		node_id;
	int		task_id; 	/* MPI rank */
	int		pid;
};
typedef struct ptp_slurm_process	ptp_process;

typedef struct {
   char * hostname;            /* something like inet_addr or node name */
   char * executable_name;     /* image name */
   int    pid;                 /* process pid*/
} MPIR_PROCDESC;

struct ptp_slurm_job {
	int		ptp_jobid;		/* job ID as known by PTP */
	int		slurm_jobid;	/* job ID that will be used by program when it starts */
	int		num_procs;		/* number of procs requested for program (debugger: num_procs+1) */
	char * 	cwd;			/* remote current working dir */
	int		state;			/* job state(slurm definition) */
	pid_t	sdmclnt_pid;
	bool	debug;			/* job is debug job */
	int		fd_err[2];			/* pipe fds for job stderr */
	int		fd_out[2];			/* pipe fds for job stdout */
	pthread_t	iothr_id;	/* id of job io thread */
	bool	iothr_exit_req; /* request iothread to exit */
	bool	iothr_exit;  /* flag inidication iothread has exited */
	pthread_t	launch_thr_id; /*id of job launch thread*/
	bool 	launch_thr_exit; /* flag indicating launch thread exited */
	bool	removable;
	ptp_process ** 	procs;		/* procs of this job */
	rangeset *	set;			/* range set of proc ID */
	bool	newprocess_event_sent;  /* true if NewJob/NewProcess has been sent */
	bool 	init_state_updated;   /* true if initial state upstate has been sent*/
	struct timeval	update_timer;   /* timestamp of the last state update */
	int	proctable_size;
	MPIR_PROCDESC * proctable;
	bool step_layout_ready;  /*set if layout information is available*/
	//job_opt_t * opt; /*pointer to job submit option structure*/
};
typedef struct ptp_slurm_job ptp_job;

typedef void SigFunc(int);

static int SLURM_Initialize(int, int, char **);
static int SLURM_ModelDef(int, int, char **);
static int SLURM_StartEvents(int, int, char **);
static int SLURM_StopEvents(int, int, char **);
static int SLURM_SubmitJob(int, int, char **);
static int SLURM_TerminateJob(int, int, char **);
static int SLURM_Quit(int, int, char **);

static FILE * init_logfp();
static void debug_log(FILE * fp, char * fmt,...);
static void job_destroy(ptp_job * job);
static void * job_launch_internal(void * arg);
static void * job_io_handler(void * arg);
static int create_node_list(ptp_machine *mach);
static void init_timer(struct timeval * timer);
static void write_routing_file(ptp_job * job);
static char * get_path(char * cmd, char * cwd, int mode);
static void  delete_routing_file(char * cwd);
static char * get_default_partition();
static bool partition_verify(char * partition);
static void opt_release(job_opt_t * opt);

static FILE *	logfp;
static int		gTransID = 0; /* transaction id for start of event stream, is 0 when events are off */
static int		gBaseID = -1; /* base ID for event generation */
static int		gLastID = 1;  /* ID generator */
static int		gQueueID;     /* ID of default queue */
static int 		proxy_state = STATE_INIT;
static proxy_svr *	slurm_proxy = NULL;
static List *	gJobList = NULL;
static List *	gMachineList = NULL;
static int		ptp_signal_exit = 0;;

static pthread_t 	ns_thr_id; /* node state update thread id*/
static pthread_t 	js_thr_id; /* job state update thread id */
static bool 		enable_state_update = false;
static pthread_cond_t state_cv = PTHREAD_COND_INITIALIZER;
static pthread_mutex_t state_mx = PTHREAD_MUTEX_INITIALIZER;
static pthread_mutex_t log_mx = PTHREAD_MUTEX_INITIALIZER;
static bool	nsu_thr_exit_req = false;
static bool	jsu_thr_exit_req = false;
static bool init_node_status_send = false;
static pthread_mutex_t joblist_mx = PTHREAD_MUTEX_INITIALIZER;

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
	{"proxy",		required_argument,	NULL, 	'P'}, 
	{"port",		required_argument,	NULL, 	'p'}, 
	{"host",		required_argument,	NULL, 	'h'}, 
	{"nodeint",		required_argument,	NULL,	't'},/* node state update interval */
	{"jobint",		required_argument,	NULL,	'T'},/* job state update interval */
	{NULL,			0,					NULL,	0}
};


/* 
 * Return the FILE pointer of the log file given by environmental variable.
 * Default to stderr if unspecified.
 */
static FILE * 
init_logfp()
{
	FILE * fp = stderr;
	char * logdir = NULL;
	char  logfile[256];
	struct passwd * pw = NULL;

#ifdef DEBUG	
	pw = getpwuid(getuid());
	setenv("PTP_SLURM_PROXY_LOGDIR", pw->pw_dir, 1);
#endif	
	if ((logdir = getenv("PTP_SLURM_PROXY_LOGDIR")) == NULL) {
		return fp;
	}	
	if (access(logdir, R_OK|W_OK) < 0) {
		return fp;
	}
	sprintf(logfile,"%s/ptp_proxy.log", logdir);
	fp = fopen(logfile,"w+");
	if (fp == NULL) { 
		fp = stderr;
	}	

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
	pthread_mutex_lock(&log_mx);
	vfprintf(fp, fmt, va);
	fflush(fp);
	pthread_mutex_unlock(&log_mx);
	va_end(va);

	return;
}

/*
 * Delete existing/old routing file of previous debug session
 */
static void 
delete_routing_file(char * cwd)
{
	char * fname = NULL;
	char * str1 = NULL;
	char * str2 = NULL;
	char dir[MAXPATHLEN];
	int rc = 0;

	fname = getenv("SDM_ROUTING_FILE");
	if (fname == NULL) {
		if (cwd) 
			str1 = cwd;	
		else {
			getcwd(dir, MAXPATHLEN);
			str1 = dir;
		}	
		str2 = "routing_file";
		rc = asprintf(&fname, "%s/%s",str1,str2);
		if (rc > 0) {
			unlink(fname);
			free(fname);
		}
	} else 	
		unlink(fname);

	return; 	
}

/*
 * Generate routing file for a debug session
 */ 
void 
write_routing_file(ptp_job * job)
{
	char * fname = NULL;
	int	rank;
	char * str1;
	char * str2;
	char cwd[MAXPATHLEN];
	FILE * fp = NULL;

	if (job->step_layout_ready) {
		/*
		 * write job topology informatin to routing file
		 * default to job->cwd/routing_file unless specified by SDM_ROUTING_FILE
		 */
		fname = getenv("SDM_ROUTING_FILE");
		if (fname == NULL) {
			if (job->cwd) 
				str1 = job->cwd;	
			else {
				getcwd(cwd, MAXPATHLEN);
				str1 = cwd;
			}	
			str2 = "routing_file";
			asprintf(&fname, "%s/%s", str1, str2);
		}

		fp = fopen(fname,"w+");
		if (fp) {
			/* write job's process number */
			fprintf(fp,"%d\n", job->num_procs);
			/* write MPIR_proctable entries */
			for (rank = 0; rank < job->num_procs; rank++) 
				fprintf(fp, "%d %s %d\n", rank, job->proctable[rank].hostname, BASEPORT+rank);
			fclose(fp);
		}
		if (fname)
			free(fname);
	}		
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
	ptp_machine * m = NULL;

	m = (ptp_machine *)malloc(sizeof(ptp_machine));
	if (m) {	
		m->id = generate_id();
		m->nodes = NewList();
		pthread_mutex_init(&(m->mutex), NULL);
    	AddToList(gMachineList, (void *)m);
	}

    return m;
}


static void
lock_joblist()
{
	pthread_mutex_lock(&joblist_mx);
}


static void
unlock_joblist()
{
	pthread_mutex_unlock(&joblist_mx);
}

static void
lock_nodelist(ptp_machine * m)
{
	if(m)
		pthread_mutex_lock(&(m->mutex));
}


static void
unlock_nodelist(ptp_machine * m)
{
	if (m)
		pthread_mutex_unlock(&(m->mutex));
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
		str =  SLURM_NODE_STATE_UP;
		break;
	case NODE_STATE_ALLOCATED:
		str =  SLURM_NODE_STATE_UP;
		break;
	case NODE_STATE_ERROR:
		str = SLURM_NODE_STATE_DOWN;
		break;
	case NODE_STATE_MIXED:
		str = SLURM_NODE_STATE_UP;
		break;
	case NODE_STATE_FUTURE:
		str = SLURM_NODE_STATE_DOWN;
		break;
	default:
		str = SLURM_NODE_STATE_UNKNOWN;
		break;
	}

	return str;
}

/*
 * Create a new node structure and insert it into machine node list.
 */
static ptp_node *
new_node(ptp_machine *mach, node_info_t *ni)
{
	static int node_number = 0;
	ptp_node * n = (ptp_node *)malloc(sizeof(ptp_node));
	if (n == NULL) {
		debug_log(logfp, "Malloc error for new_node\n");
		return NULL;
	}
	memset((char *)n, 0, sizeof(ptp_node));
	n->id = generate_id();
	n->number = node_number++;
	n->sockets = ni->sockets;
	n->cores = ni->cores;
	n->threads = ni->threads;
	if (ni->name != NULL) {
		n->name = strdup(ni->name);
	}	
	n->state = ni->node_state;
	if (ni->arch != NULL) {
		n->arch = strdup(ni->arch);
	}	
	if (ni->os != NULL) {
		n->os = strdup(ni->os);
	}

    AddToList(mach->nodes, (void *)n);

    return n;
}

/*
 * Get node pointer from node name. 
 */
static ptp_node *
find_node_by_name(char *name)
{
	ptp_machine * m = NULL;
	ptp_node * n = NULL;
	
	for (SetList(gMachineList); (m = (ptp_machine *)GetListElement(gMachineList)) != NULL;) {
		lock_nodelist(m);
		for (SetList(m->nodes); (n = (ptp_node *)GetListElement(m->nodes)) != NULL;) {
			if (strcmp(name, n->name) == 0)
				break;
		}
		unlock_nodelist(m);
	}

	return n;
}

/*
 * Create a new process structure and insert it into job->procs array.
 */
static ptp_process *
new_process(ptp_job *job, int node_id, int task_id, int pid)
{
	ptp_process * p = (ptp_process *)malloc(sizeof(ptp_process));
	if (p == NULL) {
		return NULL;
	}	
	p->id = generate_id();
	p->task_id = task_id;
	p->pid = pid;
	p->node_id = node_id;
	p->job = (void *)job;

    job->procs[task_id] = p;
 
 	//insert_in_rangeset(job->set, p->id);
 	insert_in_rangeset(job->set, p->task_id);

    return p;
}

/*
 * Free the space allocated to ptp_process structure.
 */
static void
free_process(ptp_process *p)
{
	if (p)
		free(p);
	return;
}

/*
 * Get process pointer, given job and task_id.
 */
static ptp_process *
find_process(ptp_job *job, int task_id)
{
	if (task_id < 0 || task_id >= job->num_procs)
		return NULL;
		
	return job->procs[task_id];
}
	
/*
 * Get jobid of 'type'
 */
static int
get_jobid(ptp_job *j, int type)
{
	int id = -1;

	if (j != NULL) {
		switch (type) {
		case PTP_JOBID:
			id = j->ptp_jobid;
			break;
		case SLURM_JOBID:
			id = j->slurm_jobid;
			break;
		default:
			debug_log(logfp, "Unknown job type\n");
			break;
		}	
	}

	return id;
}

/*
 * Create a new job structure,set job attributes and add to gJobList.  
 */
static ptp_job *
new_job(job_opt_t * opt, uint32_t slurm_jobid)
{
	ptp_job * j = NULL;

	j = (ptp_job *)malloc(sizeof(ptp_job));
	if (j == NULL) {
		debug_log(logfp, "malloc error in new_job()\n");
		return j;
	}	
	memset(j, 0, sizeof(ptp_job));
	j->ptp_jobid = opt->ptpid;
    j->slurm_jobid = slurm_jobid;
    j->num_procs = opt->nprocs;
    j->debug = opt->debug;
	if (opt->cwd) 
		j->cwd = strdup(opt->cwd);
	j->state = -1;
	j->removable = false;
	j->fd_out[0] = -1;
	j->fd_out[1] = -1;
	j->fd_err[0] = -1;
	j->fd_err[1] = -1;
	j->iothr_id = -1;
	j->iothr_exit_req = false;
	j->iothr_exit = false;
	j->launch_thr_id = -1;
	j->launch_thr_exit = false;
	j->set = new_rangeset();
	j->procs = (ptp_process **)malloc(sizeof(ptp_process *) * (opt->nprocs));
	memset(j->procs, 0, sizeof(ptp_process *) * (opt->nprocs));
	j->init_state_updated = false;
	j->newprocess_event_sent = false;
	j->proctable_size = 0;
	j->proctable = NULL;
	j->step_layout_ready = false;
	//j->opt = opt;
	
    AddToList(gJobList, (void *)j);

    return j;
}

/*
 * Free space allocated by new_job().
 */
static void
job_release(ptp_job *j)
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
		if (j->proctable) {
			for (i = 0; i < j->proctable_size; i++) {
				if (j->proctable[i].hostname)
					free(j->proctable[i].hostname);
				if (j->proctable[i].executable_name)
					free(j->proctable[i].executable_name);
			}	
			free(j->proctable);	
		}
	
		if (j->cwd)
			free(j->cwd);

		//if (j->opt)
		//	opt_release(j->opt);

		free(j);
	}	
}


/*
 * Get job pointer using the jobid of 'type'.
 */
static ptp_job *
find_job(int jobid, int type)
{
	ptp_job * j = NULL;
	
	lock_joblist();	
	for (SetList(gJobList); (j = (ptp_job *)GetListElement(gJobList)) != NULL;) {
		if (get_jobid(j, type) == jobid) { 
			break;
		}	
	}
	unlock_joblist();

	return j;
}

/*
 * Send OK event to ptp ui.
 */
static void
sendOKEvent(int trans_id)
{
	proxy_svr_queue_msg(slurm_proxy, proxy_ok_event(trans_id));
}

/*
 * Send ShutDown event to ptp ui.
 */
static void
sendShutdownEvent(int trans_id)
{
	proxy_svr_queue_msg(slurm_proxy, proxy_shutdown_event(trans_id));
}

/*
 * Send Message event to ptp ui.
 */
/* NO use
static void
sendMessageEvent(int trans_id, char *level, int code, char *fmt, ...)
{
	va_list	ap;

	va_start(ap, fmt);
	proxy_svr_queue_msg(slurm_proxy, proxy_message_event(trans_id, level, code, fmt, ap));
	va_end(ap);
}
*/

/*
 * Send  Error event to ptp ui.
 */
static void
sendErrorEvent(int trans_id, int code, char *fmt, ...)
{
	va_list	ap;

	va_start(ap, fmt);
	debug_log(logfp, "sendErrorEvent(%d,%d),", trans_id, code);
	debug_log(logfp, fmt,ap);
	proxy_svr_queue_msg(slurm_proxy, proxy_error_event(trans_id, code, fmt, ap));
	va_end(ap);
}

/*
 * Send JobSubSError event to ptp ui.
 */
static void
sendJobSubErrorEvent(int trans_id, char *jobSubId, char *msg)
{
	proxy_svr_queue_msg(slurm_proxy, proxy_submitjob_error_event(trans_id, jobSubId, RTEV_ERROR_SLURM_SUBMIT, msg));
}

/*
 * Send JobTerminateError event to ptp ui.
 */
static void
sendJobTerminateErrorEvent(int trans_id, int id, char *msg)
{
	char * job_id;
	
	asprintf(&job_id, "%d", id);
	proxy_svr_queue_msg(slurm_proxy, proxy_terminatejob_error_event(trans_id, job_id, RTEV_ERROR_JOB, msg));
	free(job_id);
}

/*
 * Send NewMachine event to ptp ui.
 */
static void
sendNewMachineEvent(int trans_id, int id, char *name)
{
	char * rm_id;
	char * machine_id;
	
	asprintf(&rm_id, "%d", gBaseID);	
	asprintf(&machine_id, "%d", id);	
	proxy_svr_queue_msg(slurm_proxy, proxy_new_machine_event(trans_id, rm_id, machine_id, name, PTP_MACHINE_STATE_UP));
	free(machine_id);
	free(rm_id);
}

/*
 * Get the number of attributes of a node structure.
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
	if (node->number >= 0) {
		proxy_add_int_attribute(m, SLURM_NODE_NUMBER_ATTR, node->number);
	}
	proxy_add_int_attribute(m, SLURM_NODE_SOCKETS_ATTR, (int)node->sockets);
	proxy_add_int_attribute(m, SLURM_NODE_CORES_ATTR, (int)node->cores);
	proxy_add_int_attribute(m, SLURM_NODE_THREADS_ATTR, (int)node->threads);

	if (node->arch != NULL) {
		proxy_add_string_attribute(m, SLURM_NODE_ARCH_ATTR, node->arch);
	}	
	if (node->os != NULL) {
		proxy_add_string_attribute(m, SLURM_NODE_OS_ATTR, node->os);
	}	
}

/*
 * Send NewJob event to ptp ui.
 */
static void
sendNewJobEvent(int trans_id, int jobid, char *name, char *jobSubId, char *state)
{
	char * queue_id = NULL;
	char * job_id = NULL;
	
	asprintf(&queue_id, "%d", gQueueID);	
	asprintf(&job_id, "%d", jobid);
	proxy_svr_queue_msg(slurm_proxy, proxy_new_job_event(trans_id, queue_id, job_id, name, state, jobSubId));
	free(queue_id);
	free(job_id);
}

/*
 * Send  NewNode event to ptp ui.
 */
static void
sendNewNodeEvent(int trans_id, int machid, ptp_machine *mach)
{
	ptp_node * n = NULL;
	proxy_msg *	m = NULL;
	char * machine_id = NULL;
	char * node_id = NULL;
	
	asprintf(&machine_id, "%d", machid);
	m = proxy_new_node_event(trans_id, machine_id, SizeOfList(mach->nodes));
	
	lock_nodelist(mach);	
	for (SetList(mach->nodes); (n = (ptp_node *)GetListElement(mach->nodes)) != NULL;) {
		asprintf(&node_id, "%d", n->id);
		proxy_add_node(m, node_id, n->name, nodestate_to_string(n->state), num_node_attrs(n));
		add_node_attrs(m, n);
		free(node_id);
	}
	unlock_nodelist(mach);

	proxy_svr_queue_msg(slurm_proxy, m);
	free(machine_id);
}

/*
 * Send NodeChange event to ptp ui.
 */
static void
sendNodeChangeEvent(int trans_id, char * id_range, uint16_t slurm_node_state)
{
	proxy_msg * m = NULL;
	char * state = NULL;
	char * status = NULL;
	
	switch (slurm_node_state & NODE_STATE_BASE) {
	case NODE_STATE_UNKNOWN:
		state  = SLURM_NODE_STATE_UNKNOWN;
		status = SLURM_NODE_STATUS_UNKNOWN;
		break;
	case  NODE_STATE_DOWN:
		state =  SLURM_NODE_STATE_DOWN;
		status = SLURM_NODE_STATUS_DOWN;
		break;
	case  NODE_STATE_IDLE:
		state =  SLURM_NODE_STATE_UP;
		status = SLURM_NODE_STATUS_IDLE;
		break;
	case NODE_STATE_ALLOCATED:
		state =  SLURM_NODE_STATE_UP;
		status = SLURM_NODE_STATUS_ALLOCATED;
		break;
	case NODE_STATE_ERROR:
		state = SLURM_NODE_STATE_ERROR;
		status = SLURM_NODE_STATUS_ERROR;
		break;
	case NODE_STATE_MIXED:
		state = SLURM_NODE_STATE_UP;
		status = SLURM_NODE_STATUS_MIXED;
		break;
	case NODE_STATE_FUTURE:
		state = SLURM_NODE_STATE_DOWN;
		status = SLURM_NODE_STATUS_FUTURE;
		break;
	default:
		state = SLURM_NODE_STATE_UNKNOWN;
		status = SLURM_NODE_STATUS_UNKNOWN;
		break;
	}

	m = proxy_node_change_event(trans_id, id_range, 2);
	proxy_add_string_attribute(m, "nodeState", state);
	proxy_add_string_attribute(m, "nodeStatus", status);
	proxy_svr_queue_msg(slurm_proxy, m);

	return;
}

/*
 * Send NewProcesse event to ptp ui.
 */
static void
sendNewProcessEvent(int trans_id, int ptp_jobid, ptp_process *p, char *state)
{
	proxy_msg *	m = NULL;
	
	char jobid[64];
	char proc_id[64];
	char name[64];

	if (p == NULL)
		return;
	
	sprintf(jobid, "%d", ptp_jobid);
	/* in rm protocol 4.0, replace p->id with p->task_id */
	//sprintf(proc_id, "%d", p->id);
	sprintf(proc_id, "%d", p->task_id);
	sprintf(name, "%d",  p->task_id);
	
	m = proxy_new_process_event(trans_id, jobid, 1);
	/*
	 * In SLURM, p->node_id, p->task_id, p->pid can't be obtained during job launching.
	 * But the debug model requires at least the "PROC_INDEX_ATTR",
	 * so set extra_attrs=1.
	 * Remember that the "task_id" is not the real rank number of a process,
	 * it is only a FAKED number that can satisfy devbugger's requirement.
	 */
	proxy_add_process(m, proc_id, name, state, 1);

	/*
	 * NODEID_ATTR will be provided by sendProcessChangeEvent() when job layout is available
	 */
	//proxy_add_int_attribute(m, PTP_PROC_NODEID_ATTR, p->node_id);	
	
	proxy_add_int_attribute(m, PTP_PROC_INDEX_ATTR, p->task_id);

	/*
	 * SLURM doesn't provide PID information
	 */
   //proxy_add_int_attribute(m, PTP_PROC_PID_ATTR, p->pid);
	
	proxy_svr_queue_msg(slurm_proxy, m);
}

/*
 * Send NewQueue event to ptp ui.
 */
static void
sendNewQueueEvent(int trans_id)
{
	char * rm_id = NULL;
	char * queue_id = NULL;
	proxy_msg * m = NULL;
	
	gQueueID = generate_id();
	asprintf(&rm_id, "%d", gBaseID);
	asprintf(&queue_id, "%d", gQueueID);
	m = proxy_new_queue_event(trans_id, rm_id, queue_id, DEFAULT_QUEUE_NAME, 1);
	proxy_add_string_attribute(m, "queueState", "NORMAL");
	proxy_svr_queue_msg(slurm_proxy,m); 
	
	free(rm_id);
	free(queue_id);
}

static void
get_job_state_status(uint16_t slurm_state, char ** state, char ** status)
{
	if (!state || !status)
		return;

	switch (slurm_state & JOB_STATE_BASE) {
		case JOB_PENDING:
			*state = "STARTING";
			*status = "PENDING";
			break;
		case JOB_RUNNING:
			*status = *state = "RUNNING";
			break;
		case JOB_SUSPENDED:
			*status = *state = "SUSPENDED";
			break;
		case JOB_COMPLETE:
			*status = *state = "COMPLETED";
			break;	
		case JOB_CANCELLED:
			*state = "COMPLETED";
			*status = "CANCELLED";
			break;
		case JOB_FAILED:
			*state = "COMPLETED";
			*status = "FAILED";
			break;
		case JOB_TIMEOUT:
			*state = "COMPLETED";
			*status = "TIMEOUT";
		case JOB_NODE_FAIL:
			*state = "COMPLETED";
			*status = "NODEFAIL";
			break;
		default:
			*status = *state = "UNKNOWN";
			break;
	}

	return;
}

static void
get_proc_state_status(uint16_t slurm_state, char ** state, char ** status)
{
	/* 
	 * Since SLURM does not provide process state,
	 * fake process state using job state 
     */
	get_job_state_status(slurm_state, state, status);		
}

/*
 * Send ProcessStateChange event to ptp ui.
 */
static void
sendProcessStateChangeEvent(int trans_id, ptp_job *j, uint16_t slurm_state)
{
	char * state;
	char * status;
	char jobid[64];
	proxy_msg *	m = NULL;
	
	if (j == NULL || j->num_procs == 0)
		return;
		
	get_proc_state_status(slurm_state, &state, &status);

	sprintf(jobid, "%d", j->ptp_jobid);
	//m = proxy_process_change_event(trans_id, jobid, rangeset_to_string(j->set), 2);
	m = proxy_process_change_event(trans_id, jobid, rangeset_to_string(j->set), 1);

	proxy_add_string_attribute(m, PTP_PROC_STATE_ATTR, state);
	//proxy_add_string_attribute(m, PTP_PROC_STATUS_ATTR, status);

	proxy_svr_queue_msg(slurm_proxy, m);
}
	
/*
 * Send JobStateChange event to ptp ui.
 */
static void
sendJobStateChangeEvent(int trans_id, int jobid, uint16_t slurm_state)
{
	char * job_id = NULL;
	proxy_msg *	m = NULL;
	char * state;
	char * status;
	
	
	asprintf(&job_id, "%d", jobid);
	m = proxy_job_change_event(trans_id, job_id, 2);
	get_job_state_status(slurm_state, &state, &status);
	proxy_add_string_attribute(m, PTP_JOB_STATE_ATTR, state);
	proxy_add_string_attribute(m, PTP_JOB_STATUS_ATTR,status);
	proxy_svr_queue_msg(slurm_proxy, m);
	
	free(job_id);
}

/*
 * Process Change: node_id, task_id or pid change!
 */
static void
sendProcessChangeEvent(int trans_id, ptp_process *p, int node_id, int task_id, int pid)
{
	int cnt = 0;
	proxy_msg * msg;
	char jobid[64];
	char proc_id[64];

	if (p->node_id != node_id || p->task_id != task_id || p->pid != pid) {
		if (p->node_id != node_id)
			cnt++;
		if (p->task_id != task_id)
			cnt++;
		if (p->pid != pid)
			cnt++;
	
		//asprintf(&proc_id, "%d", p->id);
		sprintf(proc_id, "%d", p->task_id);
		sprintf(jobid, "%d", ((ptp_job *)(p->job))->ptp_jobid);
		msg = proxy_process_change_event(trans_id, jobid, proc_id, cnt);
		if (p->node_id != node_id) {
			p->node_id = node_id;
			proxy_add_int_attribute(msg, PTP_PROC_NODEID_ATTR, node_id);
		}
		if (p->task_id != task_id) {
			p->task_id = task_id;
			proxy_add_int_attribute(msg, PTP_PROC_INDEX_ATTR, task_id);
		}
		if (p->pid != pid) {
			p->pid = pid;
			proxy_add_int_attribute(msg, PTP_PROC_PID_ATTR, pid);
		}
		
		proxy_svr_queue_msg(slurm_proxy, msg);
	}
	return;
}

/*
 * Send ProcessOutput event to ptp ui.
 */
static void
sendProcessOutputEvent(int trans_id, ptp_process *p, char *output)
{
	proxy_msg *	m = NULL;
	char jobid[64];
	char proc_id[64];

	sprintf(jobid, "%d", ((ptp_job *)(p->job))->ptp_jobid);
	sprintf(proc_id, "%d", p->task_id);
	m = proxy_process_change_event(trans_id, jobid, proc_id, 1);
	proxy_add_string_attribute(m, PTP_PROC_STDOUT_ATTR, output);
	proxy_svr_queue_msg(slurm_proxy, m);
}

/*
 * Get the number of compute nodes in the machine managed by SLURM.
 */
static int
get_num_nodes(int machid)
{
	uint32_t cnt = 0;
	node_info_msg_t * ninfo = NULL;

	if (slurm_load_node((time_t)NULL, &ninfo, SHOW_ALL) == 0) {
		cnt = ninfo->record_count;
		slurm_free_node_info_msg(ninfo);
	}

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
	node_info_msg_t * nmsg = NULL;
	ptp_node * node = NULL;
	int i;

	if (slurm_load_node((time_t)NULL,&nmsg, SHOW_ALL)) {
		return -1;
	}
	for (i = 0; i < nmsg->record_count; i++) { 
		node = new_node(mach, nmsg->node_array + i);
	}

	return 0;
}

/* 
 * Notify the proxy daemon to exit. 
 */
static int
do_slurmproxy_shutdown(void)
{
	/* do nothing in SLURM since no daemons are required */
	debug_log(logfp, "do_slurmproxy_shutdown() called.\n");

	return 0;
}

/*
 * Create job description msg from opts before job allocation.
 */
static job_desc_msg_t * 
create_job_desc_msg_from_opts(job_opt_t *opt)
{
	job_desc_msg_t * msg = NULL;
	
	if ((msg = (job_desc_msg_t *)malloc(sizeof(job_desc_msg_t))) == NULL) {
		debug_log(logfp, "Allocate job_desg_msg fail");
		return NULL;
	}

	slurm_init_job_desc_msg(msg);
	if (opt->debug)	
		msg->name = strdup(opt->debug_exec_name);
	else 
		msg->name = strdup(opt->exec_name);
	msg->user_id = opt->uid;
	msg->group_id = opt->gid;
	if (opt->nprocs_set)
		msg->num_tasks = opt->nprocs;
	if (opt->nodes_set)
		msg->min_nodes = opt->min_nodes;
	if (opt->max_nodes)
		msg->max_nodes = opt->max_nodes;
	if (opt->tlimit_set)
		msg->time_limit = opt->tlimit;
	if (opt->partition)
		msg->partition = strdup(opt->partition);
	if (opt->nodelist) 
		msg->req_nodes = strdup(opt->nodelist);
	if (opt->exc_nodes)
		msg->exc_nodes = strdup(opt->exc_nodes);
	if (opt->jobid_set)
		msg->job_id = opt->jobid;

	return msg;
}

/*
 * Install signal handler.
 */
static SigFunc *
xsignal(int signo, SigFunc *f)
{
	struct sigaction sa, old_sa;

	sa.sa_handler = f;
	sigemptyset(&sa.sa_mask);
	sigaddset(&sa.sa_mask, signo);
	if (signo == SIGCHLD)
		sa.sa_flags = SA_NOCLDWAIT;/*no zombie process when child terminate*/
	else 
		sa.sa_flags = 0;
	if (sigaction(signo, &sa, &old_sa) < 0) {
		debug_log(logfp,"xsignal(%d) failed: %m", signo);
	}	

	return old_sa.sa_handler;
}

/*
 * Release allocated memory in job_desc_msg.
 */
static void 
destroy_job_desc_msg(job_desc_msg_t * j)
{
	if (j != NULL) {
		if (j->name)
			free(j->name);
		if (j->partition)
			free(j->partition);
		if (j->req_nodes)
			free(j->req_nodes);
		if (j->exc_nodes)
			free(j->exc_nodes);

		free(j);
	}
}

/*
 * Create a thread to retrieve job's stdout/stderr,
 * and send outputs to ptp ui.
 */
static int 
create_iothread(ptp_job * job)
{
	pthread_t io_tid;
	pthread_attr_t io_attr;
	int rc = 0;

	pthread_attr_init(&io_attr);
	pthread_attr_setdetachstate(&io_attr, PTHREAD_CREATE_DETACHED);
	pthread_attr_setscope(&io_attr, PTHREAD_SCOPE_SYSTEM);
	if (pthread_create(&io_tid, &io_attr, job_io_handler, (void *)job)) {
		/*create iothread fail*/
		debug_log(logfp, "Job[%d] io thread create fail.\n", job->slurm_jobid);
		rc = -1;
	} else {
		debug_log(logfp, "Job[%d] io thread create done.\n", job->slurm_jobid);
		job->iothr_id = io_tid;
		rc = 0;
	}

	pthread_attr_destroy(&io_attr);

	return rc;
}

/*
 * Create a new thread to allocate job resource (if necessary), launch job
 * and forward job stdout to ptp ui. 
 */
static int 
allocate_and_launch_job(job_opt_t * opt)
{
	pthread_t id;
	pthread_attr_t attr;
	int rc = 0;

	pthread_attr_init(&attr);
	pthread_attr_setdetachstate(&attr, PTHREAD_CREATE_DETACHED);
	pthread_attr_setscope(&attr, PTHREAD_SCOPE_SYSTEM);

	if (pthread_create(&id, &attr, job_launch_internal, (void *)opt )) {
		debug_log(logfp, "create job_launch_internal thread fail.\n");
		rc = -1;
	}

	pthread_attr_destroy(&attr);
	
	return rc;
}	

/*
 * Retrun node->number by matching nodename of elements in mach->nodelist
 */ 
static int 
get_node_id(char * nodename, int * id) {
	ptp_machine * m = NULL;
	ptp_node * n;
	int rc = -1;

	SetList(gMachineList);
	/* Now only 1 machine is supported */
	m = (ptp_machine *)GetFirstElement(gMachineList);
	
	lock_nodelist(m);
	for (SetList(m->nodes); (n = (ptp_node *)GetListElement(m->nodes)) != NULL;) {
		if (strcmp(nodename, n->name) == 0) {
			*id = n->id;
			rc = 0;
			break;
		}	
	}
	unlock_nodelist(m);

	return rc;
}

/*
 *malloc space for job proctable and initialize it
 */ 
int
job_proctable_init(ptp_job * job)
{
	int rc = -1;

	job->proctable_size = job->num_procs;
	job->proctable = (MPIR_PROCDESC *)malloc(job->proctable_size * sizeof(MPIR_PROCDESC));
	if (job->proctable) {
		memset(job->proctable, 0, job->proctable_size * sizeof(MPIR_PROCDESC));
		rc = 0;
	}

	return rc;
}

static void
fd_set_exec_close(fd)
{
	int val;

	val = fcntl(fd, F_GETFD, 0);
	val |= FD_CLOEXEC;
	fcntl(fd, F_SETFD, val);
}

/*
 * kernel thread functions:
 * allocate job resources, create job step and launch it
 */ 
static void * 
job_launch_internal(void * arg)
{
	int i;
	ptp_job * job = NULL;
	ptp_process  * p = NULL;
	int node_id = -1;
	int task_id = -1;
	int proc_pid = -1;
	ptp_machine * m = NULL;
	ptp_node * n = NULL;
	int node_cnt = 0;
	job_opt_t * opt = NULL;
	job_desc_msg_t * job_req = NULL;
	resource_allocation_response_msg_t * job_resp = NULL;
	resource_allocation_response_msg_t * lookup_resp = NULL;
	resource_allocation_response_msg_t * tmp_resp = NULL;
	char * msg = NULL;
	char * name = NULL;
	slurm_step_ctx_params_t  step_params;
	slurm_step_ctx_t * step_ctx = NULL;
	slurm_step_launch_params_t launch_params;
	slurm_step_layout_t * layout = NULL;
	int fd_out[2];
	int fd_err[2];
	bool fd_set = false;

	opt = (job_opt_t *)arg;

	if (opt->debug)
		delete_routing_file(opt->cwd); /* delete existing routing file */

	job_req = create_job_desc_msg_from_opts(opt);
	if (job_req == NULL) {
		msg = "create_job_desc_msg fail.";
		debug_log(logfp, msg);
		sendJobSubErrorEvent(opt->trans_id, opt->jobsubid, msg);
		goto done;
	}
	
	if (slurm_allocate_resources(job_req, &job_resp)) { /* job allocation error */
		msg = slurm_strerror(slurm_get_errno());
		debug_log(logfp, msg);
		sendJobSubErrorEvent(opt->trans_id, opt->jobsubid, msg);
		goto done;
	}

	/* send OK event for SubmitJob cmd */	
	sendOKEvent(opt->trans_id);
	
	/* send NewJob event */
	asprintf(&name, SLURM_JOB_NAME_FMT, job_resp->job_id);
	//sendNewJobEvent(gTransID, job->ptp_jobid, name, jobsubid, JOB_STATE_INIT);
	sendNewJobEvent(gTransID, opt->ptpid, name, opt->jobsubid, "STARTING");
	free(name);
	/*create a new job and inster to gJobList*/
	job = new_job(opt, job_resp->job_id);
	job->launch_thr_id = pthread_self();
	/*
	 * As required by ptp ui,
	 * one NewProcess event MUST be sent for each process in this new job.
	 */
	task_id = 0;
	node_id = 0;
	proc_pid = 0;
	SetList(gMachineList);
	/* Now only 1 machine is supported */
	m = (ptp_machine *)GetFirstElement(gMachineList);
	
	lock_nodelist(m);
	node_cnt = SizeOfList(m->nodes);
	n = (ptp_node *)GetFirstElement(m->nodes);
	unlock_nodelist(m);

	node_id = n->id; /* node_id calculated by generateid() */

	for (i = 0; i < job->num_procs; i++) {
		/*
		 * Let's FAKE proc/node layout map by now.
		 * The REAL layout information will be sent to ui
		 * via ProcessChange event after job start.
		 *
		 * +node_cnt creates an invalid node_id, so ui will not create the map
		 * between node and process.
		 * sendNewProcessEvent() shouldn't make use of these fileds.
		 *
		 */
		task_id = i; /* task_id == task_rank */

		p = new_process(job, node_id + node_cnt, task_id, proc_pid); 
		sendNewProcessEvent(gTransID, job->ptp_jobid, p, PTP_PROC_STATE_STARTING);
		proc_pid += 1;
	 }
	job->newprocess_event_sent = true;	/* enable job/process state update */

	/*
	 * job allocation done, but may be pending (no nodes allocated).
	 * check if nodes granted.
	 */
	char * node_list = job_resp->node_list;
	tmp_resp = job_resp;

	while ((node_list == NULL) || (strlen(node_list) == 0)) {
		if (lookup_resp != NULL) {
			slurm_free_resource_allocation_response_msg(lookup_resp);
			lookup_resp = NULL;
		}
		if(slurm_allocation_lookup_lite(job_resp->job_id, &lookup_resp)){ /*lookup error*/
			if (slurm_get_errno() != ESLURM_JOB_PENDING) {
				/*
				 * job allocation fail OR user cancelled
				 * job state will be updated in handle_jobstate_update
				 * job structure will be deleted in handle_jobstate_update
				 */
				debug_log(logfp,"Job[%d] allocation fail or cancelled\n", job_resp->job_id);
				slurm_complete_job(job_resp->job_id, NO_VAL);/*Mark job CANCELLED*/
				goto done;
			}
		} else {/*lookup success*/
			node_list = lookup_resp->node_list;	
			tmp_resp = lookup_resp;
		}
		sleep(2); /*re-check after 2 seconds*/
	}

	/***********************OK. nodes granted***********************/

	/*Create job step context*/
	slurm_step_ctx_params_t_init(&step_params);
	if (opt->debug)
		step_params.name = opt->debug_exec_name;
	else 	
		step_params.name = opt->exec_name;
	step_params.job_id = tmp_resp->job_id;
#ifdef SLURM_VERSION_2_2
	step_params.min_nodes = tmp_resp->node_cnt;
	if (opt->min_nodes && (opt->min_nodes < tmp_resp->node_cnt))
		step_params.min_nodes = opt->min_nodes;
	step_params.max_nodes = tmp_resp->node_cnt;
	if (opt->max_nodes && (opt->max_nodes < tmp_resp->node_cnt))
		step_params.max_nodes = opt->max_nodes;
#else /* 2.1 */
	step_params.node_count = tmp_resp->node_cnt;
#endif
	step_params.node_list = tmp_resp->node_list;
	step_params.task_count = opt->nprocs;
	step_params.time_limit = opt->tlimit;
	step_params.uid = opt->uid;
	
	step_ctx = slurm_step_ctx_create(&step_params);
	if (step_ctx == NULL) {
		debug_log(logfp, "Job[%d] step_ctx create fail:%s\n", tmp_resp->job_id, slurm_strerror(slurm_get_errno()));
		slurm_complete_job(tmp_resp->job_id, NO_VAL);/*Mark job CANCELLED*/
		goto done;
	}
	
	/*
	 * send job layout information to ptp ui.
	 * layout is available right after step_ctx created.
	 * Note: process pid still not available since SLURM jobstep layout not provide it.
	 */
	uint32_t step_id;
	slurm_step_ctx_get(step_ctx, SLURM_STEP_CTX_STEPID, &step_id);
	layout = slurm_job_step_layout_get(job->slurm_jobid, step_id);
	if (layout != NULL) {
		/*layout->node_list:node range, e.g., node[1-5]*/
		char * node = NULL;
		int i, j, cnt, node_id = -1, rank;

		job_proctable_init(job);
		hostlist_t hl;
		hl = slurm_hostlist_create(layout->node_list);
		int hl_cnt = slurm_hostlist_count(hl);
		for (i = 0; i < hl_cnt; i++) {
			node = slurm_hostlist_shift(hl);
			if (node != NULL) {
				get_node_id(node, &node_id);
				cnt = layout->tasks[i];
				for (j = 0; j < cnt; j++) {
					rank = layout->tids[i][j];
					job->proctable[rank].hostname = strdup(node); 
					//job->proctable[rank].exec_name skipped 
					//job->proctable[rank].pid skipped
					p = job->procs[rank];
					sendProcessChangeEvent(gTransID, p, node_id, p->task_id, p->pid);
				}
				free(node);	
			}	
		}
		slurm_hostlist_destroy(hl);
		slurm_job_step_layout_free(layout);
		job->step_layout_ready = true;
	}
	else {
		debug_log(logfp, "job step layout unavailable\n" );
	}

	/*launch job step*/
	slurm_step_launch_params_t_init(&launch_params);
	if (opt->debug) {
		launch_params.argc = opt->debug_argc;
		launch_params.argv = opt->debug_argv;
	} else {
		launch_params.argc = opt->prog_argc;
		launch_params.argv = opt->prog_argv;
	}
	launch_params.envc = opt->envc;
	launch_params.env = opt->env;
	launch_params.cwd = opt->cwd;
	launch_params.user_managed_io = false;
	launch_params.labelio = true; /*true to distinguish outputs from different process*/

	/*create two pipes/fifos to handle job's stdout/stderr*/
	pipe(fd_out);
	pipe(fd_err);
	fd_set = true;

	/*set FD_CLOEXEC flag to pipe fds*/
	fd_set_exec_close(fd_out[0]);
	fd_set_exec_close(fd_out[1]);
	fd_set_exec_close(fd_err[0]);
	fd_set_exec_close(fd_err[1]);

	launch_params.local_fds.out.fd = fd_out[1];
	launch_params.local_fds.err.fd = fd_err[1];
	
	/*create iothread before launching job step*/
	job->fd_out[0] = fd_out[0];
	job->fd_out[1] = fd_out[1];
	job->fd_err[0] = fd_err[0];
	job->fd_err[1] = fd_err[1];

	if (create_iothread(job)) {
		debug_log(logfp, "Cancel Jod[%d] coz creating io thread failed.\n", job_resp->job_id);
		slurm_complete_job(job_resp->job_id, NO_VAL);/*Mark job CANCELLED*/
		/*close read end of pipes*/
		close(fd_out[0]);
		close(fd_err[0]);
		goto done;
	}	
	sleep(2); /*wait for iothread ready*/

	/*launch job step*/
	if (slurm_step_launch(step_ctx, &launch_params, NULL) != SLURM_SUCCESS) {
		slurm_complete_job(job_resp->job_id, NO_VAL);/*Mark job CANCELLED*/
		debug_log(logfp, "Job[%d] step launch fail.\n", job_resp->job_id);
		goto done;
	}

	/*wait for job step start*/
	if (slurm_step_launch_wait_start(step_ctx) != SLURM_SUCCESS) {
		slurm_kill_job(job_resp->job_id, SIGKILL, 0); /*kill all job step and complete job*/
		debug_log(logfp, "Job[%d] step wait start fail.\n", job_resp->job_id);
		goto done;
	}

	if (job->debug) { /*do extra work for debug launch*/
		/* handle debug job launch 
		 * The ORDER is very important.
		 * (1) launch sdm server as a parallel job. 
		 *     sdm server must run and in accept state before sdm client starts.
		 * (2) write routing file for sdm client and servers
		 * (3) exec sdm client on server node
		 *		NOTE:This step is done by front end (ptp ui) for ORTE,MPICH2, PE, LL rms.
		 *		In SLURM, we do it by our proxy.
		 */
		if (job->step_layout_ready) {
			/*write new routing file*/
			write_routing_file(job);
			
			/*
		 	 * launch sdm client
	 		 */
			char * clnt_host = NULL;
			char * clnt_port = NULL;
			char * clnt_master = "--master";
			for (i = 0; i < opt->debug_argc; i++) {
				if (strstr(opt->debug_argv[i],"--port")) 
					clnt_port = opt->debug_argv[i];
				if (strstr(opt->debug_argv[i], "--host")) 
					clnt_host = opt->debug_argv[i];
			}
			pid_t clnt_pid;

			switch (clnt_pid = fork()) 
			{
			case 0:/*child*/
				/*exec sdm client on server node*/
				/*to locate routing file correctly*/
				chdir(opt->cwd); 
				execl(opt->debug_argv[0], opt->debug_argv[0], clnt_host, clnt_port, clnt_master, NULL);
				break;
			case -1:
				debug_log(logfp, "fork sdmclnt error\n");
				break;
			default:
				job->sdmclnt_pid = clnt_pid;
				break;
			}
		}	
	}

done:
	/*block until job step finish*/
	slurm_step_launch_wait_finish(step_ctx);

	uint32_t job_ret_code = 0;/*Mark job COMPLETE*/
	slurm_complete_job(job->slurm_jobid, job_ret_code);

//done:
	if (job_req)
		destroy_job_desc_msg(job_req);
	if (job_resp)
		slurm_free_resource_allocation_response_msg(job_resp);
	if (lookup_resp) 
		slurm_free_resource_allocation_response_msg(lookup_resp);
	if (step_ctx)
		slurm_step_ctx_destroy(step_ctx);
	if (opt)
		opt_release(opt); /*release opt structure*/		
	
	if (fd_set) {
		/*close write end of pipe, ensure io thread exit*/	
		close(fd_out[1]);
		close(fd_err[1]);
	}
	
	if (job)
		job->launch_thr_exit = true;

	pthread_exit(NULL);
}

/*
 * Return the absolute path of cmd using $PATH and cwd. 
 * The returned value must be freed by user.
 */
static char *
get_path(char * cmd, char * cwd, int mode)
{
	char * path = NULL;
	char * fullpath = NULL;
	char * pos = NULL;
	char * ptr = NULL;
	char * tmp = NULL;

	if ( (cmd[0] == '.' || cmd[0] == '/')
		&& access(cmd, mode) == 0) {
		if (cmd[0] == '.') 
			asprintf(&fullpath,"%s/%s", cwd, cmd);
		else 
			asprintf(&fullpath,"%s", cmd);
	} else {
		/* search $PATH */
		path = getenv("PATH");
		if (path != NULL) {
			if ((ptr = (char *) malloc(strlen(path) + 1)) != NULL ) {
				tmp = pos = ptr;
				memcpy(ptr, path, strlen(path));
				ptr[strlen(path) + 1] = '\0';
				while((pos = strchr(ptr,':')) != NULL) {
					*pos = '\0';
					asprintf(&fullpath, "%s/%s", ptr, cmd);
					if (access(fullpath, mode) == 0) {
						free(tmp);
						goto done;
					} else {
						free(fullpath);
						fullpath = NULL;
						ptr = pos + 1;
					}	
				}
				/* handle the last element */
				if (*ptr != '\0') {
					asprintf(&fullpath, "%s/%s", ptr, cmd);
					if (access(fullpath, mode) != 0) {
						free(fullpath);
						fullpath = NULL;
					}	
				}
				free(tmp);	
			}
		}
	}

done:
	return fullpath;
}

/*
 * Redirect job's stdout/stderr to ptp ui.
 */
void *
job_io_handler(void * arg)
{
	int fd_out = -1;
	int fd_err = -1;
	int fd;
	char * ptr = NULL;
	ptp_job * job = NULL;
	int task_id;
	fd_set rfds;
	struct timeval tv;
	char buf[MAX_BUF_SIZE];
	FILE * fp_out = NULL;
	FILE * fp_err = NULL;
	int cancel_state;
	int cancel_type;
	int	ret;
	char * p = NULL;
	int stdout_exit = 0;
	int stderr_exit = 0;
	
	pthread_setcancelstate(PTHREAD_CANCEL_ENABLE,&cancel_state);
	pthread_setcanceltype(PTHREAD_CANCEL_DEFERRED, &cancel_type);
	
	job = (ptp_job *)arg;
	fd_out = job->fd_out[0];
	fd_err = job->fd_err[0];
	
	while (job->iothr_exit_req == false)
	{
		tv.tv_sec = 0;
		tv.tv_usec = 5000;/* 5ms */
		/* 
		 * if timeout, the rfds will be cleared 
		 * rfds must be set in each iteration
		 */
		FD_ZERO(&rfds);
		FD_SET(fd_out, &rfds);
		FD_SET(fd_err, &rfds);
		fd = fd_out > fd_err?fd_out:fd_err;

		ret = select(fd+1, &rfds, NULL, NULL, &tv);

		switch(ret)
		{
		case -1: /* error */
			debug_log(logfp,"job[%d] iothread exit on select error:%s\n", job->slurm_jobid, strerror(errno));
			if (errno == EINTR) {
				debug_log(logfp, "select got signal. Continue.\n");
				continue;
			} else {	
				debug_log(logfp, "select error. Exit.\n");
				goto done;
			}
			break;
		case 0: /* timeout */
			break;
		default: /* fd ready */
			if (FD_ISSET(fd_out, &rfds)) { /* handle job stdout */
				if (fp_out == NULL)
					fp_out = fdopen(fd_out,"r");
				if (fgets(buf, sizeof(buf), fp_out) != NULL) { 
				//if ((rc = fd_read_line(fd, buf, sizeof(buf)))> 0 ) { 
					if (job->debug) {
						fprintf(stderr, "%s\n", buf);
						continue;
					}
					fprintf(stderr,"job output: %s\n", buf);
					p = buf;
					/* get task id from output label */
					task_id = atoi(p);
					ptr = strchr(p, ':');
					if (ptr != NULL) {
						ptr ++;
						/* 
						 * No synchronization needed, 
						 * since the event list is internally protected by pthread_mutex
						 */
						ptp_process * proc = NULL;
						proc = find_process(job, task_id);
						if (proc != NULL)
							sendProcessOutputEvent(gTransID, proc, ptr);
					}  
				} else { /* error or EOF of pipe(write end closed) */
					debug_log(logfp,"job[%d] iothread exit on EOF/ERROR of stdout fd\n",job->slurm_jobid);
					stdout_exit = 1;
				}	
			}

			if (FD_ISSET(fd_err, &rfds)) { /* handle job stderr */
				if (fp_err == NULL)
					fp_err = fdopen(fd_err,"r");
				if (fgets(buf, sizeof(buf), fp_err) != NULL) { 
				//if ((rc = fd_read_line(fd, buf, sizeof(buf)))> 0 ) {
					if (job->debug) {
						fprintf(stderr, "%s\n", buf);
						continue;
					}
					p = buf;
					/* get task id from srun label */
					task_id = atoi(p);
					ptr = strchr(p, ':');
					if (ptr != NULL) {
						ptr ++;
						/* 
						 * No synchronization needed, 
						 * since the event list is internally protected by pthread_mutex
						 */
						ptp_process * proc = NULL;
						proc = find_process(job, task_id);
						if (proc)
							sendProcessOutputEvent(gTransID, proc, ptr);
					}  
				} else { /* error or EOF of pipe (write end closed) */
					debug_log(logfp,"job[%d] iothread exit on Error/EOF of stderr fd.\n",job->slurm_jobid);
					stderr_exit = 1;
				}	
			}

			break; 
		}
		if (stdout_exit && stderr_exit)
			goto done;	
	}	

	debug_log(logfp, "job[%d] iothread exit on exit_request.\n", job->slurm_jobid);

done:
	if (fp_out)
		fclose(fp_out);
	if (fp_err)
		fclose(fp_err);

	close(job->fd_out[0]);
	close(job->fd_err[0]);

	job->iothr_exit = true;

	pthread_exit(NULL);
	
	return NULL;
}

/*
 * Initialize "opt" with default values
 */
int 
opt_default(job_opt_t * opt)
{
	int rc = 0;
	char buf[MAXPATHLEN + 1];
	struct passwd * pw;


	if ((pw = getpwuid(getuid())) != NULL) {
		strncpy(opt->user, pw->pw_name, MAX_USERNAME);
		opt->uid = pw->pw_uid;
	} else {
		debug_log(logfp, "getpwuid error.\n");
		rc = -1;
		goto done;
	}

	opt->gid = getgid();
	opt->nprocs = 1;
	opt->nprocs_set = false;
	opt->min_nodes = 1;
	opt->max_nodes = 0;
	opt->nodes_set = false;
	opt->tlimit = NO_VAL;
	opt->tlimit_set = false;
	opt->partition = NULL; 
	opt->jobid = NO_VAL;
	opt->jobid_set = false;
	opt->labelio = true; /*TRUE to distinguish process stdout*/
	opt->nodelist = NULL;
	opt->exc_nodes = NULL;

	if (getcwd(buf, MAXPATHLEN) == NULL) {
		rc = -1;
		goto done;
	}
	opt->cwd = strdup(buf); 
	opt->envc = 0;
	opt->env = NULL;
	opt->exec_name = NULL;
	opt->exec_path = NULL;
	opt->exec_fullname = NULL;
	opt->prog_argc = 0;
	opt->prog_argv = NULL;
	
	opt->debug = false;
	opt->debug_exec_name = NULL;
	opt->debug_exec_path = NULL;
	opt->debug_exec_fullname = NULL;
	opt->debug_argc = 0;
	opt->debug_argv = NULL;
	
	opt->trans_id = -1;
	opt->ptpid = -1;
	opt->jobsubid = NULL;

done:
	return rc;
}

/*
 *Set opt according to cmd args
 */ 
int 
opt_args(job_opt_t * opt, int nargs, char ** args)
{
	int i,k;
	char * str = NULL;
	int num_args = 0;
	int num_env = 0;
	int debug_argc = 0;

	for (i = 0; i < nargs; i++) {
		if (proxy_test_attribute(SLURM_JOB_NUM_PROCS_ATTR, args[i])) {
			opt->nprocs = proxy_get_attribute_value_int(args[i]);
			opt->nprocs_set = true;
		} else if (proxy_test_attribute(SLURM_JOB_NUM_NODES_ATTR, args[i])) {
			opt->min_nodes = proxy_get_attribute_value_int(args[i]);
			opt->nodes_set = true;
		} else if (proxy_test_attribute(SLURM_JOB_TIME_LIMIT_ATTR, args[i])) {
			opt->tlimit = proxy_get_attribute_value_int(args[i]);
			opt->tlimit_set = true;
		} else if (proxy_test_attribute(SLURM_JOB_PARTITION_ATTR, args[i])) {
			str = proxy_get_attribute_value_str(args[i]);
			if (strlen(str) > 0) 
				opt->partition = strdup(str);
		} else if (proxy_test_attribute(SLURM_JOB_IOLABEL_ATTR, args[i])) {
			opt->labelio = proxy_get_attribute_value_bool(args[i]);
		} else if (proxy_test_attribute(SLURM_JOB_ID_ATTR, args[i])) {
			opt->jobid = proxy_get_attribute_value_int(args[i]);
			opt->jobid_set = true;
		} else if (proxy_test_attribute(SLURM_JOB_NODELIST_ATTR, args[i])) {
			str = proxy_get_attribute_value_str(args[i]);
			if (strlen(str) > 0) 
				opt->nodelist = strdup(str); 
		} else if (proxy_test_attribute(SLURM_JOB_EXCNODELIST_ATTR, args[i])) {
			str = proxy_get_attribute_value_str(args[i]);
			if (strlen(str) > 0)
				opt->exc_nodes = strdup(str);
		} else if (proxy_test_attribute(SLURM_JOB_EXEC_NAME_ATTR, args[i])) {
			str = proxy_get_attribute_value_str(args[i]);
			if (strlen(str) > 0)
				opt->exec_name = strdup(str);
		} else if (proxy_test_attribute(SLURM_JOB_EXEC_PATH_ATTR, args[i])) {
			str = proxy_get_attribute_value_str(args[i]);
			if (strlen(str) > 0)
				opt->exec_path = strdup(str); 
		} else if (proxy_test_attribute(SLURM_JOB_WORKING_DIR_ATTR, args[i])) {
			str = proxy_get_attribute_value_str(args[i]);
			if (strlen(str) > 0) {
				if (opt->cwd)
					free(opt->cwd); /*free opt->cwd set by opt_default*/
				opt->cwd = strdup(str); 
			}
		} else if (proxy_test_attribute(SLURM_JOB_PROG_ARGS_ATTR, args[i])) {
			num_args++;
		} else if (proxy_test_attribute(SLURM_JOB_ENV_ATTR, args[i])) {
			num_env++;
		} else if (proxy_test_attribute(SLURM_JOB_DEBUG_ARGS_ATTR, args[i])) {
			debug_argc++;
		} else if (proxy_test_attribute(SLURM_JOB_DEBUG_FLAG_ATTR, args[i])) {
			opt->debug = proxy_get_attribute_value_bool(args[i]);
		} else if (proxy_test_attribute(SLURM_JOB_DEBUG_EXEC_NAME_ATTR, args[i])) {
			str = proxy_get_attribute_value_str(args[i]);
			if (strlen(str) > 0)
				opt->debug_exec_name = strdup(str);
		} else if (proxy_test_attribute(SLURM_JOB_DEBUG_EXEC_PATH_ATTR, args[i])) {
			str = proxy_get_attribute_value_str(args[i]);
			if (strlen(str) > 0)
				opt->debug_exec_path = strdup(str);
		}
	}
	
	/*handle enviorment variables*/
	opt->envc = num_env;
	if (opt->envc) {
		opt->env = (char **)malloc(opt->envc * sizeof(char *));
		if (opt->env == NULL) 
			goto done;
		k = 0;	
		for (i = 0; i < nargs; i++) {
			if (proxy_test_attribute(SLURM_JOB_ENV_ATTR, args[i])) {
				str = proxy_get_attribute_value_str(args[i]);
				if (strlen(str) > 0)
					opt->env[k++] = strdup(str);
			}
		}	
	}
	

	/*handle app argv[] parameter*/	
	if (opt->debug) { /*debug job*/		
		opt->debug_argc = debug_argc + 1; //+1 for argv[0]
		opt->debug_argv = (char **)malloc(opt->debug_argc * sizeof(char *));
		if (opt->debug_argv == NULL)
			goto done;
		memset(opt->debug_argv, 0, opt->debug_argc * sizeof(char *));
		k = 1;
		//opt->debug_argv[0] reserved and set in opt_verify()
		for (i = 0; i < nargs; i++) {
			if (proxy_test_attribute(SLURM_JOB_DEBUG_ARGS_ATTR, args[i])) {
			str = proxy_get_attribute_value_str(args[i]);
			if (strlen(str) > 0)
				opt->debug_argv[k++] = strdup(str);
			}
		}	
	} else { /*non-debug job*/	
		opt->prog_argc = num_args + 1;//+1 for argv[0]
		opt->prog_argv = (char **)malloc(opt->prog_argc*sizeof(char *));
		if (opt->prog_argv == NULL)
			goto done;
		memset(opt->prog_argv, 0, opt->prog_argc * sizeof(char *));
		k = 1;
		//opt->prog_argv[0] reserved and set in opt_verify()
		for (i = 0; i < nargs; i++) {
			if (proxy_test_attribute(SLURM_JOB_PROG_ARGS_ATTR, args[i])) {
				str = proxy_get_attribute_value_str(args[i]);
				if (strlen(str) > 0)
					opt->prog_argv[k++] = strdup(str);
			}
		}
	}	

	return  0;

done:
	debug_log(logfp, "opt_args() malloc error.\n");
	//opt_release(opt);
	return  -1;
}

/*
 * Verify arg settings
 */ 
bool 
opt_verify(job_opt_t * opt)
{
	bool verified = true;
	hostlist_t hl = NULL;
	int hl_cnt = 0;
	int rc = 0;
	int dec_cnt, i;
	char * host = NULL;

	if (opt == NULL)
		return false;

	if (opt->debug) { /* check debug-job parameters */
		/* get the fullpath of sdm executable */		
		if (opt->debug_exec_name == NULL) {
			verified = false;
			goto done;
		}

		if (opt->debug_exec_path == NULL) {
			opt->debug_exec_fullname = get_path(opt->debug_exec_name, opt->cwd, X_OK);
			if (opt->debug_exec_fullname == NULL) {
				verified = false;
				goto done;
			}
		} else {
			rc = asprintf(&(opt->debug_exec_fullname), "%s/%s", opt->debug_exec_path, opt->debug_exec_name);
			if (rc == -1) {
				verified = false;
				goto done;
			}
		}	
	
		/* check access right */		
		if (access(opt->debug_exec_fullname, X_OK) < 0) {
			verified = false;
			goto done;
		} else { 
			if ((opt->debug_argv[0] = strdup(opt->debug_exec_fullname))== NULL) {
				verified = false;
				goto done;
			}
		}
	} else { /* check normal-job  parameters */	
		if (opt->exec_name == NULL){
			debug_log(logfp, "program name not specified.\n");
			verified = false;
			goto done;
		}

		/* locate the execuable file to be launched */
		if (opt->exec_path == NULL) {
			opt->exec_fullname = get_path(opt->exec_name, opt->cwd, X_OK);
			if (opt->exec_fullname == NULL) { 
				verified = false;	
				goto done;
			}
		} else {
			asprintf(&(opt->exec_fullname), "%s/%s", opt->exec_path, opt->exec_name);
			/* check access right */		
			if (access(opt->exec_fullname, X_OK) < 0) {
				verified = false;
				goto done;
			} else {
				if ((opt->prog_argv[0] = strdup(opt->exec_fullname)) == NULL) {
					verified = false;
					goto done;
				}
			}
		}	
	}		
	
	/* verify partition request */
	if (opt->partition == NULL){
		opt->partition = get_default_partition();
	} else {
		 if (partition_verify(opt->partition) == false) {
			verified = false;
			goto done;
		}
	}
	
	/* verify proc number */
	if (opt->nprocs <= 0) {
		debug_log(logfp, "invalid number of processes (-n %d)\n", opt->nprocs);
		verified = false;
		goto done;
	}
		
	/* verify -N parameter */
	if (opt->min_nodes <= 0 || opt->max_nodes < 0 || 
		(opt->max_nodes && (opt->min_nodes > opt->max_nodes))) {
		debug_log(logfp, "invalid number of nodes (-N %d-%d)\n", opt->min_nodes, opt->max_nodes);
		verified = false;
		goto done;
	}

	if (opt->nodelist) {
		hl = slurm_hostlist_create(opt->nodelist);
		if (!hl) {
			debug_log(logfp, "hostlist create error.\n");
			verified = false;
			goto done;
		}
		slurm_hostlist_uniq(hl);
		hl_cnt = slurm_hostlist_count(hl);
		if (opt->nodes_set) {
			if (hl_cnt > opt->min_nodes)
				opt->min_nodes = hl_cnt;		
		} else {
			opt->min_nodes = hl_cnt;
			opt->nodes_set = true;
		}
	}

	if (opt->nodes_set && opt->nprocs_set) {
		 /* make sure max_nodex <= nprocs */
		if (opt->nprocs < opt->max_nodes) 
			opt->max_nodes = opt->nprocs;
		/* make sure nprocs >= min_nodes */
		if (opt->nprocs < opt->min_nodes) {
			debug_log(logfp, "Can't run %d processes on %d nodes, setting nnodes=%d\n",
						opt->nprocs, opt->min_nodes, opt->nprocs);
			opt->min_nodes = opt->nprocs;
			if (opt->max_nodes && opt->min_nodes > opt->max_nodes)
				opt->max_nodes = opt->min_nodes;
			if (hl_cnt > opt->min_nodes) {
				/*
				 * shrink the number of requested nodelist 
				 */ 
				dec_cnt = hl_cnt - opt->min_nodes;
				debug_log(logfp, "Shrink requested nodes by %d\n",dec_cnt);
				for (i = 0; i < dec_cnt; i++) {
					host = slurm_hostlist_shift(hl);
					free(host);
				}
				/*update opt->nodelist */	
				slurm_hostlist_ranged_string(hl, strlen(opt->nodelist)+1, opt->nodelist);
			}
		}
	}
	if (hl)
		slurm_hostlist_destroy(hl);

done:
	return verified;	
}


static char *
get_default_partition()
{
	int i = 0;
	char * ptr = NULL;
	partition_info_msg_t * part_info_msg = NULL;
	
	if (slurm_load_partitions((time_t)NULL, &part_info_msg, SHOW_ALL ) == SLURM_SUCCESS) {
		for (i = 0; i < part_info_msg->record_count; i++) {
		#ifdef SLURM_VERSION_2_2 /*2.2 or later*/
			if (part_info_msg->partition_array[i].flags & PART_FLAG_DEFAULT) {
				ptr = strdup(part_info_msg->partition_array[i].name);
				break;
			}		
		#else /*2.1*/
			if (part_info_msg->partition_array[i].default_part) {
				ptr = strdup(part_info_msg->partition_array[i].name);
				break;
			}
		#endif
			
		}
		slurm_free_partition_info_msg(part_info_msg);
	}

	return ptr; /*should be released by caller*/
}

static bool 
partition_verify(char * partition)
{
	bool rc = false;
	int i = 0;
	partition_info_msg_t * part_info_msg = NULL;
	
	if (slurm_load_partitions((time_t)NULL, &part_info_msg, SHOW_ALL ) == SLURM_SUCCESS) {
		for (i = 0; i < part_info_msg->record_count; i++) {
			if (strcmp(part_info_msg->partition_array[i].name, partition) == 0) {
				rc = true;
				break;
			}
		}
		slurm_free_partition_info_msg(part_info_msg);
	}
	
	return rc;
}


/*
 *create a new opt structure
 */ 
job_opt_t * opt_new()
{
	job_opt_t * ptr = NULL;

	ptr = (job_opt_t *)malloc(sizeof(job_opt_t));
	if (ptr) 
		memset(ptr, 0, sizeof(job_opt_t));
	
	return ptr;
}

/*
 *Release space malloced for opt elements
 */ 
static void 
opt_release(job_opt_t * opt)
{
	int i = 0;

	if (opt){
		if (opt->partition)
			free(opt->partition);
		if (opt->nodelist)
			free(opt->nodelist);
		if (opt->exc_nodes)
			free(opt->exc_nodes);
		if (opt->cwd)
			free(opt->cwd);
		if(opt->env) {	
			for (i = 0; i < opt->envc; i++) {
				if (opt->env[i])
					free(opt->env[i]);
			}
			free(opt->env);
		}	
		if (opt->exec_name)
			free(opt->exec_name);
		if (opt->exec_path)
			free(opt->exec_path);
		if(opt->exec_fullname)
			free(opt->exec_fullname);
		if (opt->prog_argv) {	
			for (i = 0; i < opt->prog_argc; i++) {
				if (opt->prog_argv[i])
					free(opt->prog_argv[i]);
			}
			free(opt->prog_argv);
		}
		if (opt->debug_exec_name)
			free(opt->debug_exec_name);
		if (opt->debug_exec_path)
			free(opt->debug_exec_path);
		if(opt->debug_exec_fullname)
			free(opt->debug_exec_fullname);
		if (opt->debug_argv) {	
			for (i = 0; i < opt->debug_argc; i++) {
				if (opt->debug_argv[i])
					free(opt->debug_argv[i]);
			}
			free(opt->debug_argv);
		}
		if (opt->jobsubid)
			free(opt->jobsubid);

		free(opt);	
	}
}

/*
 * Check if timer expires with the given timeout value.
 * timeout: usec
 */
static bool
update_timeout(struct timeval * timer, const int timeout)
{
	struct timeval now;
	int val;
	bool rc = false;
	
	if (timer == NULL)
		return rc;

	gettimeofday(&now, NULL);
	val = (now.tv_sec - timer->tv_sec) * 1000000 + (now.tv_usec - timer->tv_usec) - timeout;
	if (val >= 0)  /* time out */
		rc = true;

	return rc;
}

void 
init_timer(struct timeval * timer)
{
	gettimeofday(timer, NULL);
	return;	
}

int 
handle_nodestate_update(node_info_msg_t * ptr, bool init_flag)
{
	int i = 0;
	int rc = -1;
	ptp_node * node = NULL;
	rangeset * unk_set = NULL;
	rangeset * down_set = NULL;
	rangeset * idle_set = NULL;
	rangeset * alloc_set = NULL;
	rangeset * err_set = NULL;
	rangeset * mix_set = NULL;
	rangeset * future_set = NULL;

	if (ptr == NULL)
		return rc;

	unk_set = new_rangeset();
	idle_set = new_rangeset();
	down_set = new_rangeset();
	alloc_set = new_rangeset();
	err_set = new_rangeset();
	mix_set = new_rangeset();
	future_set = new_rangeset();
	if (!unk_set || !idle_set || !down_set || !alloc_set || !err_set || !mix_set || !future_set)
		goto cleanup;

	for (i = 0; i < ptr->record_count; i++) {
		node = find_node_by_name(ptr->node_array[i].name);
		if (!node) /* node not found in global node list */
			continue;

		if (!init_flag) {	
			if (node->state == ptr->node_array[i].node_state)
				continue; /* handle next node */
		}

		node->state = ptr->node_array[i].node_state;
		 /* send node state change */
		switch (node->state & NODE_STATE_BASE) {
		case NODE_STATE_UNKNOWN:
			insert_in_rangeset(unk_set,node->id);
			break;
		case NODE_STATE_DOWN:
			insert_in_rangeset(down_set,node->id);
			break;
		case NODE_STATE_IDLE:
			insert_in_rangeset(idle_set,node->id);
			break;
		case NODE_STATE_ALLOCATED:
			insert_in_rangeset(alloc_set,node->id);
			break;
		case NODE_STATE_ERROR:
			insert_in_rangeset(err_set,node->id);
			break;
		case NODE_STATE_MIXED:
			insert_in_rangeset(mix_set,node->id);
			break;
		case NODE_STATE_FUTURE:
			insert_in_rangeset(future_set,node->id);
			break;
		default:
			debug_log(logfp, "node[%d] unrecognized node state.\n", i);
			break;
		}	
	}	
				
	if (!EmptyList(unk_set->elements)) 
		sendNodeChangeEvent(gTransID,rangeset_to_string(unk_set),NODE_STATE_UNKNOWN);
	if (!EmptyList(down_set->elements)) 
		sendNodeChangeEvent(gTransID,rangeset_to_string(down_set),NODE_STATE_DOWN);
	if (!EmptyList(idle_set->elements)) 
		sendNodeChangeEvent(gTransID,rangeset_to_string(idle_set),NODE_STATE_IDLE);
	if (!EmptyList(alloc_set->elements)) 
		sendNodeChangeEvent(gTransID,rangeset_to_string(alloc_set),NODE_STATE_ALLOCATED);
	if (!EmptyList(err_set->elements)) 
		sendNodeChangeEvent(gTransID,rangeset_to_string(err_set),NODE_STATE_ERROR);
	if (!EmptyList(mix_set->elements)) 
		sendNodeChangeEvent(gTransID,rangeset_to_string(mix_set),NODE_STATE_MIXED);
	if (!EmptyList(future_set->elements)) 
		sendNodeChangeEvent(gTransID,rangeset_to_string(future_set),NODE_STATE_FUTURE);

	rc = 0;

cleanup:
	if (unk_set) {
		free_rangeset(unk_set);
		unk_set = NULL;
	}
	if (down_set) {	
		free_rangeset(down_set);
		down_set = NULL;
	}
	if (idle_set){	
		free_rangeset(idle_set);
		idle_set = NULL;
	}
	if (alloc_set){	
		free_rangeset(alloc_set);
		alloc_set = NULL;
	}
	if (err_set) {
		free_rangeset(err_set);
		err_set = NULL;
	}	
	if (mix_set) {
		free_rangeset(mix_set);
		mix_set = NULL;
	}	
	if (future_set) {
		free_rangeset(future_set);
		future_set = NULL;
	}	

	return rc;
}

/*
 * Update ALL nodes state and send state CHANGE to ui.
 */
void *
ns_update_internal(void * arg)
{
	int rc;
	int ret;
	int interval = *(int *)arg;
	static struct timeval ns_timer;	
	static node_info_msg_t * old_node_ptr = NULL, *new_node_ptr = NULL;
	node_info_msg_t * nmsg_ptr = NULL;
	uint16_t show_flags = SHOW_ALL;
	bool init_flag = false;

	init_timer(&ns_timer);
	/*
	 * wait for main thread to enable state update
	 * (after joblist and nodelist are ready,
	 * and NewNode event has been sent)
	 */	
	pthread_mutex_lock(&state_mx);	
	while (enable_state_update == false) {
		pthread_cond_wait(&state_cv, &state_mx);
	}
	pthread_mutex_unlock(&state_mx);

	while (!nsu_thr_exit_req) {
		usleep(100000); /*retry after 100ms*/
		if (init_node_status_send == false) {/*initial node status not sent*/
			init_flag = true;
			/*send initial nodestate update*/
			rc = slurm_load_node((time_t)NULL, &nmsg_ptr, show_flags);
			if (rc != SLURM_SUCCESS) { 
				continue;
			}
			else {
				ret = handle_nodestate_update(nmsg_ptr, init_flag);	
				if (ret == 0) {
					gettimeofday(&ns_timer, NULL); /*update timer*/
					init_node_status_send = true;
				}	
				slurm_free_node_info_msg(nmsg_ptr);
				debug_log(logfp, "Initial node status SENT.\n");
			}
		} else if (update_timeout(&ns_timer, interval)) {/*time out*/	
			if (old_node_ptr) {
				rc = slurm_load_node(old_node_ptr->last_update, &new_node_ptr, show_flags);
				if (rc == SLURM_SUCCESS) {
					slurm_free_node_info_msg(old_node_ptr);
				} else if (slurm_get_errno() == SLURM_NO_CHANGE_IN_DATA) {
					rc = SLURM_SUCCESS;
					new_node_ptr = old_node_ptr;
					gettimeofday(&ns_timer, NULL); /*update ns_timer*/
					continue; /*If NO change, do nothing*/
				}
			} else 
				rc = slurm_load_node((time_t)NULL, &new_node_ptr, show_flags);

			if (rc != SLURM_SUCCESS) {
				debug_log(logfp,"slurm_load_node error in update:%s\n", slurm_strerror(rc));
				continue;
			}

			old_node_ptr = new_node_ptr;
			init_flag = false;	
			ret = handle_nodestate_update(old_node_ptr, init_flag);
			if (ret == 0) 
				gettimeofday(&ns_timer, NULL); /*update ns_timer*/
		} 
	}

	return NULL;
}

/*
 * create a thread to update node state
 */ 
int 
ns_update_thr_create(int nodeint)
{
	pthread_attr_t attr;
	int arg = nodeint;
	int rc;

	pthread_attr_init(&attr);
	pthread_attr_setscope(&attr, PTHREAD_SCOPE_SYSTEM);
	pthread_attr_setstacksize(&attr, 1024*1024);
	pthread_attr_setdetachstate(&attr, PTHREAD_CREATE_DETACHED);

	rc = pthread_create(&ns_thr_id, &attr, &ns_update_internal, (void *)&arg);
	
	if (rc)
		debug_log(logfp, "nodestate_update thread create fail.\n");

	pthread_attr_destroy(&attr);	
			
	return rc;		
}

/*
 * Return true if job finished
 */ 
bool 
is_job_finished(ptp_job * job)
{
	return ((job->state & JOB_STATE_BASE) >  JOB_SUSPENDED);
}

/*
 *Return true if job completed
 */ 
bool 
is_job_completed(ptp_job * job)
{
	return  (is_job_finished(job) && ((job->state & JOB_COMPLETING) == 0));
}

/*
 * handle jobstate update related issues
 */ 
int 
handle_jobstate_update(ptp_job * j)
{	
	int rc = 0;
	job_info_msg_t * msg = NULL;
	uint16_t show_flags = SHOW_ALL;

	rc = slurm_load_job(&msg, j->slurm_jobid, show_flags);
	if (rc == SLURM_SUCCESS) {
		if (j->state != (msg->job_array[0]).job_state) { /*state change*/
			j->state = (msg->job_array[0]).job_state; /*update jobstate*/
			/* 
			 * SLURM doesn't provide process state.
			 * Force process state changs with job state. 
			 */
			sendProcessStateChangeEvent(gTransID, j, j->state);
			sendJobStateChangeEvent(gTransID, j->ptp_jobid, j->state);

			debug_log(logfp, "Send Job/Process StateChange Event: state=%d\n",j->state);
		}
		slurm_free_job_info_msg(msg);
		/*if job completed, ensure iothr exit*/
		if (is_job_completed(j)) {
			if (j->iothr_exit == false)
				j->iothr_exit_req = true;
		}
	} else if (errno == ESLURM_INVALID_JOB_ID) {
		/*
		 * SLURM keep informatin of complete/fail jobs for MinJobAge (default to 300s) 
		 * MinJobAge can be set in slurm/etc/slurm.conf.
		 * 
		 * job no longer exist in SLURM, do cleanup.
		 * It's safe to remove and destroy job structure and release job-related memory resources
		 * 
		 */
		debug_log(logfp, "Job[%d] no longer exist in SLURM. Romove it!\n", j->slurm_jobid);
		/* Note: joblist is locked! */
		RemoveFromList(gJobList, j);
		job_destroy(j);
	} else 
		debug_log(logfp, "Job[%d]: slurm_load_job error:%s\n", j->slurm_jobid, slurm_strerror(rc));		

	return rc;
}

/*
 * Update job/process state and send state CHANGE to ui.
 */
void *
js_update_internal(void * arg)
{
	int rc = 0;
	ptp_job * j = NULL;
	int interval = *(int *)arg;

	/*wait for main thread to enable state update*/	
	pthread_mutex_lock(&state_mx);	
	while (enable_state_update == false) {
		pthread_cond_wait(&state_cv, &state_mx);
	}
	pthread_mutex_unlock(&state_mx);

	while (!jsu_thr_exit_req) {
		usleep(100000);/*retry after 100ms*/
		lock_joblist();
		for (SetList(gJobList); (j = (ptp_job *)GetListElement(gJobList)) != NULL;) {
			if (j->newprocess_event_sent == false) /*Don't send update until NewJob/NewProcess event sent*/
				continue;
			if (j->init_state_updated == false) {
				/*send initial state update*/
				rc = handle_jobstate_update(j);	
				if (rc == 0) {
					j->init_state_updated = true;
					gettimeofday(&j->update_timer, NULL);
				}	
			} else {	
				if (update_timeout(&(j->update_timer), interval)) {
					rc = handle_jobstate_update(j);	
					if (rc == 0)
						gettimeofday(&j->update_timer, NULL);
				}		
			}	
		}
		unlock_joblist();
	}

	return NULL;
}

/*
 * create a thread to update job state
 */ 
int 
js_update_thr_create(int jobint)
{
	pthread_attr_t attr;
	int arg = jobint;
	int rc = 0;

	pthread_attr_init(&attr);
	pthread_attr_setscope(&attr, PTHREAD_SCOPE_SYSTEM);
	pthread_attr_setstacksize(&attr, 1024*1024);
	pthread_attr_setdetachstate(&attr, PTHREAD_CREATE_DETACHED);

	rc = pthread_create(&js_thr_id, &attr, &js_update_internal, (void *)&arg);
	if (rc)
		debug_log(logfp, "jobstate_update thread create fail.\n");
				
	pthread_attr_destroy(&attr);

	return rc;		
}


/*
 * signal handler of slurm proxy. 
 */
void
ptp_signal_handler(int sig)
{
	if (sig != SIGCHLD)  /* proxy doesn't exit on SIGCHLD */ 
		ptp_signal_exit = sig;

	fprintf(stderr, "Got signal:%d\n", sig);
}


static void 
job_destroy(ptp_job * job)
{
	/*close job pipe*/
	/*This is done when io_thr and launch_thr exit*/	
	//close(job->fd_out[0]);	
	//close(job->fd_out[1]);	
	//close(job->fd_err[0]);	
	//close(job->fd_err[1]);	
	
	//if (job->debug)
	//	kill(job->sdmclnt_pid, SIGKILl)
	
	/*free space allocateed for job*/	
	job_release(job);
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
	
	debug_log(logfp, "SLURM_Initialize (%d):\n", trans_id);
	
	if (proxy_state != STATE_INIT) {
		sendErrorEvent(trans_id, RTEV_ERROR_SLURM_INIT, "Proxy already initialized");
		return PTP_PROXY_RES_OK;
	}
	
	for (i = 0; i < nargs; i++) {
		if (proxy_test_attribute(PTP_PROTOCOL_VERSION_ATTR, args[i])) {
			if (strcmp(proxy_get_attribute_value_str(args[i]), WIRE_PROTOCOL_VERSION) != 0) {
				sendErrorEvent(trans_id, RTEV_ERROR_SLURM_INIT, "Wire protocol version \"%s\" not supported", args[0]);
				return PTP_PROXY_RES_OK;
			}
		} else if (proxy_test_attribute(PTP_BASE_ID_ATTR, args[i])) { 
			gBaseID = proxy_get_attribute_value_int(args[i]);
		}	
	}

	if (gBaseID < 0) {
		sendErrorEvent(trans_id, RTEV_ERROR_SLURM_INIT, "No base ID supplied");
		return PTP_PROXY_RES_OK;
	}
	
 	/* confirm slurmctld works well via slurm_ping */ 
	if (slurm_ping(primary) && slurm_ping(secondary)) {
		sendErrorEvent(trans_id, RTEV_ERROR_SLURM_INIT, "No response from slurmctld. Check SLURM RMS!");
		return PTP_PROXY_RES_OK;
	}	
  	/* 
	 * SLURM version verfication,
	 * Should work on more versions supporting used API. 
	 */
	  if (SLURM_VERSION_MAJOR(slurm_api_version()) < 2) { /*This version only works with SLURM-2.x*/
		sendErrorEvent(trans_id, RTEV_ERROR_SLURM_INIT, "SLURM version number too low");
		return PTP_PROXY_RES_OK;
	 }

	proxy_state = STATE_RUNNING;
	sendOKEvent(trans_id);
	
	return PTP_PROXY_RES_OK;
}

/*
 * Init the model definition phase.
 */
int
SLURM_ModelDef(int trans_id, int nargs, char **args)
{
	debug_log(logfp, "SLURM_ModelDef (%d):\n", trans_id); 
	
	sendOKEvent(trans_id);

	return PTP_PROXY_RES_OK;
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
	int	num_machines = 0;;
	int	m = 0;;
	ptp_machine * mach = NULL;
	int num_nodes = 0;
	
	debug_log(logfp, "SLURM_StartEvents (%d):\n", trans_id); 

	if (proxy_state != STATE_RUNNING) {
		sendErrorEvent(trans_id, RTEV_ERROR_SLURM_INIT, "Must call INIT first");
		return PTP_PROXY_RES_OK;
	}

	/* NodeChange, JobChange event use gTransID as TID to match START_EVENTS cmd */
	gTransID = trans_id;
	
	/*
	 * FIXME: 
	 * how to handle partition information in SLURM?
	 */
	num_machines = get_num_machines();
	for(m = 0; m < num_machines; m++) {
		mach = new_machine();
		/* NewMachine element */
		sendNewMachineEvent(trans_id, mach->id, get_machine_name(m));
		num_nodes = get_num_nodes(mach->id);
		if(create_node_list(mach)) {
			sendErrorEvent(trans_id, RTEV_ERROR_NATTR, "Fail to create nodelist");
			return PTP_PROXY_RES_OK;
		}
		/* NewNode element */
		sendNewNodeEvent(trans_id, mach->id, mach);
	}
	/* NewQueue element */
	sendNewQueueEvent(trans_id);

	/* From now on, job state and node state update msg can be sent */
	pthread_mutex_lock(&state_mx);
	enable_state_update = true;
	pthread_cond_broadcast(&state_cv);
	pthread_mutex_unlock(&state_mx);
	
	return PTP_PROXY_RES_OK;
}

/*
 * Submit a job with the given executable path and arguments.
 * Main steps:
 * (1)process cmd arguments;
 * (2)distinguish between debug job and non-debug job;
 * (3)allocate resource and spawn job step.
	 supported job allocate/task launch options:
	 -n,-N,-t,-p,-w,-x (-l:default) 
 */
int
SLURM_SubmitJob(int trans_id, int nargs, char **args)
{
	int rc = 0;
	int i = 0;
	char *	jobsubid = NULL; /* jobid assigned by RMS (ptp ui) */
	job_opt_t * opt = NULL;
	int ptpid = generate_id();

	debug_log(logfp, "SLURM_SubmitJob (%d):\n", trans_id);
	/* Process job submit args  */
	debug_log(logfp, "job submit commands:\n");

	/* get jobsubid first */
	for (i = 0; i < nargs; i++) {
		debug_log(logfp, "\t%s\n", args[i]);
		if (proxy_test_attribute(SLURM_JOB_SUB_ID_ATTR, args[i])) 
			jobsubid = proxy_get_attribute_value_str(args[i]);
	}
	if (jobsubid == NULL) {
		sendErrorEvent(trans_id, RTEV_ERROR_SLURM_SUBMIT, "Missing ID on job submission");
		return PTP_PROXY_RES_OK;
	}

	/* Do some check first */
	if (proxy_state != STATE_RUNNING) {
		sendJobSubErrorEvent(trans_id, jobsubid, "Must call INIT first");
		return PTP_PROXY_RES_OK;
	}

	if (nargs < 1) {
		sendJobSubErrorEvent(trans_id, jobsubid, "Incorrect arg count");
		return PTP_PROXY_RES_OK;
	}

	opt = opt_new();
	if (opt == NULL) {
		sendJobSubErrorEvent(trans_id, jobsubid, "job_opt_t malloc error");
		return PTP_PROXY_RES_OK;
	}

	opt_default(opt);
	rc = opt_args(opt, nargs, args);
	if (rc) {
		sendJobSubErrorEvent(trans_id, jobsubid, "opt_args() error");
		opt_release(opt); /*release opt structure on failure*/
		return PTP_PROXY_RES_OK;
	}

	if (opt_verify(opt) == false) {
		sendJobSubErrorEvent(trans_id, jobsubid, "Invalid job configuration");
		opt_release(opt); /*release opt structure on failure*/
		return PTP_PROXY_RES_OK;
	}
	
	opt->ptpid = ptpid;	/*model element id generated by proxy agent*/
	opt->trans_id = trans_id;
	opt->jobsubid = strdup(jobsubid);

	allocate_and_launch_job(opt);
	

	return PTP_PROXY_RES_OK;
}		

/* 
 * Cancel/Terminate a running/pending job, given the ptp jobid (not slurm jobid).
 */
int
SLURM_TerminateJob(int trans_id, int nargs, char **args)
{
	int			i;
	int 		ptp_jobid = -1;
	ptp_job * 	j = NULL;
	job_info_msg_t * job_info = NULL;
	uint16_t show_flags = 0;
	int rc;

	if (proxy_state != STATE_RUNNING) {
		sendErrorEvent(trans_id, RTEV_ERROR_JOB, "Must call INIT first");
		return PTP_PROXY_RES_OK;
	}
	
	for (i = 0; i < nargs; i++) {
		if (proxy_test_attribute(PTP_JOB_ID_ATTR, args[i])) {
			ptp_jobid = proxy_get_attribute_value_int(args[i]);
			break;
		}
	}

	if (ptp_jobid < 0) {
		sendJobTerminateErrorEvent(trans_id, ptp_jobid, "Invalid jobid ");
		return PTP_PROXY_RES_OK;
	}

	if ((j = find_job(ptp_jobid, PTP_JOBID)) != NULL) {
		rc = slurm_load_job(&job_info, j->slurm_jobid, show_flags);			
		if (rc == 0) {
			if (job_info->job_array[0].job_state <= JOB_SUSPENDED)	{ /*job PENDING, RUNNING, SUSPENDED*/
				slurm_kill_job(j->slurm_jobid, SIGKILL, 0); 
			}
			slurm_free_job_info_msg(job_info);
		}
	}

	sendOKEvent(trans_id);

	return PTP_PROXY_RES_OK;
}

/*
 * Enable suspended phase
 */ 
int
SLURM_StopEvents(int trans_id, int nargs, char **args)
{
	debug_log(logfp, "SLURM_StopEvents (%d):\n", trans_id); 

	/* Notify that StartEvents complete */
	sendOKEvent(gTransID);
	gTransID = 0;
	sendOKEvent(trans_id);

	return PTP_PROXY_RES_OK;	
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
	if (old_state == STATE_RUNNING) { 
		do_slurmproxy_shutdown();
	}
	sendShutdownEvent(trans_id);
	
	return PTP_PROXY_RES_OK;
}

/******************************
 * END OF DISPATCH ROUTINES *
 ******************************/

/*
 * Cleanup work on proxy exit:
 * 
 * (1) kill ACTIVE jobs to release nodes (REQUIRED).
 * (2) After job killed, its launch thread and io thread will automatically exit.
 * (3) All memory resources will be automatically released when sever exits.
 * 
 */
void
destroy_global_joblist()
{
	ptp_job * j = NULL;

	debug_log(logfp, "Release all resources on slurm proxy exit\n");
	
	lock_joblist();
	for (SetList(gJobList); (j = (ptp_job *)GetListElement(gJobList)) != NULL;) {
		if (!is_job_completed(j)) 
			slurm_kill_job(j->slurm_jobid, SIGKILL, 0);
		//if (j->debug)
		//	kill(j->sdmclnt_pid, SIGKILL);
	}
	unlock_joblist();

	return;
}

/*
 * termiante job/node state update thread
 */ 
void 
terminate_helper_thread()
{
	nsu_thr_exit_req = true;
	jsu_thr_exit_req = true;

	sleep(2);

	return;
}

/*
 * kernel routine of proxy server
 */
static int
server(char *name, char *host, int port, int nodeint, int jobint)
{
	int rc = 0;
	struct timeval	timeout = {0, 20000};
	
	gJobList = NewList();
	gMachineList = NewList();
	
	if (proxy_svr_init(name, &timeout, &helper_funcs, &command_tab, &slurm_proxy) != PTP_PROXY_RES_OK) {
		debug_log(logfp, "proxy failed to initialized\n"); 
		return -1;
	}
	
	if (proxy_svr_connect(slurm_proxy, host, port) == PTP_PROXY_RES_OK) {
		debug_log(logfp, "proxy connected\n"); 
		
		/* create threads to update job and node state */	
		ns_update_thr_create(nodeint);
		js_update_thr_create(jobint);

		while (ptp_signal_exit == 0 && proxy_state != STATE_SHUTDOWN) {
			if (proxy_state == STATE_SHUTTING_DOWN) 
				proxy_state = STATE_SHUTDOWN;

			if (proxy_svr_progress(slurm_proxy) != PTP_PROXY_RES_OK) 
				break;
		}

		if (ptp_signal_exit != 0) {
			if (proxy_state != STATE_SHUTTING_DOWN
				&& proxy_state != STATE_SHUTDOWN) {
				do_slurmproxy_shutdown();
			}
			/* return code = the signal that fired */
			rc = ptp_signal_exit;
			debug_log(logfp, "ptp_slurm_proxy terminated by signal [%d]\n", ptp_signal_exit);
		}
	} else  
		debug_log(logfp, "proxy connection failed\n"); 

	/*
	 * do cleanup before exiting
	 */
	debug_log(logfp, "Proxy server cleanup...\n");
	/* require node/job state update thread exit */
	terminate_helper_thread();
	/* kill job step, release job, kill job launch/io thread */
	destroy_global_joblist();

	proxy_svr_finish(slurm_proxy);
	
	return rc;
}


#define DEFAULT_NODESTATE_UPDATE_INTERVAL  500000 /*usec*/
#define DEFAULT_JOBSTATE_UPDATE_INTERVAL   500000 /*usec*/
/*
 * entry routine of proxy server
 */
int
main(int argc, char *argv[])
{
	int 	ch;
	int		port = PTP_PROXY_TCP_PORT;
	char *	host = DEFAULT_HOST;
	char *	proxy_str = DEFAULT_PROXY;
	int		node_intv = DEFAULT_NODESTATE_UPDATE_INTERVAL;
	int 	job_intv = DEFAULT_JOBSTATE_UPDATE_INTERVAL;
	int		rc;
	
	while ((ch = getopt_long(argc, argv, "P:p:h:t:T:", longopts, NULL)) != -1){ 
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
		case 't':
			node_intv = (int)strtol(optarg, NULL, 10);
			break;
		case 'T':
			job_intv = (int)strtol(optarg, NULL, 10);
			break;
		default:
			fprintf(stderr, "Usage:	%s [--proxy=proxy] [--host=host_name] [--port=port] [--node_intv=interval] [--job_intv=interval]\n", argv[0]);
			return 1;
		}
	}	

	logfp = init_logfp();

	/* 
	 * signal can happen at any time after handlers are installed, 
	 * so make sure we catch it.
	 */
	ptp_signal_exit = 0;
	
	/* setup signal handlers */
	xsignal(SIGINT, ptp_signal_handler);
	xsignal(SIGHUP, ptp_signal_handler);
	xsignal(SIGILL, ptp_signal_handler);
	//xsignal(SIGSEGV, ptp_signal_handler);
	xsignal(SIGTERM, ptp_signal_handler);
	xsignal(SIGQUIT, ptp_signal_handler);
	xsignal(SIGABRT, ptp_signal_handler);
	xsignal(SIGCHLD, ptp_signal_handler);
	/*
	 * SIGPIPE is directed to a specific thread,
	 * and default action:Term
	 */

	//sleep(30);
	
	rc = server(proxy_str, host, port, node_intv, job_intv);
	
	exit(rc);
}
