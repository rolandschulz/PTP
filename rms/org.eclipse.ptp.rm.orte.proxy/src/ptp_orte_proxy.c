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

#ifdef __gnu_linux__
#define _GNU_SOURCE
#endif /* __gnu_linux__ */

#include "config.h"

#include <getopt.h>
#include <unistd.h>
#include <grp.h>
#include <pwd.h>
#include <stdbool.h>
#include <errno.h>
#include <signal.h>

#include <sys/types.h>
#include <sys/wait.h>
#include <sys/select.h>

#include "proxy.h"
#include "proxy_tcp.h"
#include "handler.h"
#include "list.h"
#include "args.h"

/*
 * Need to undef these if we include
 * two config.h files
 */
#undef PACKAGE_BUGREPORT
#undef PACKAGE_NAME
#undef PACKAGE_STRING
#undef PACKAGE_TARNAME
#undef PACKAGE_VERSION
#include "orte_fixup.h"

#define WIRE_PROTOCOL_VERSION	"2.0"

#define DEFAULT_PROXY		"tcp"
#define DEFAULT_ORTED_ARGS	"orted --scope public --seed --persistent --no-daemonize"

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
 * RTEV_ERROR codes are used internally in the ORTE specific plugin
 */
#define RTEV_ERROR_ORTE_INIT			RTEV_OFFSET + 1000
#define RTEV_ERROR_ORTE_FINALIZE		RTEV_OFFSET + 1001
#define RTEV_ERROR_ORTE_RUN				RTEV_OFFSET + 1002
#define RTEV_ERROR_TERMINATE_JOB		RTEV_OFFSET + 1003
#define RTEV_ERROR_PATTR				RTEV_OFFSET + 1004
#define RTEV_ERROR_PROCS				RTEV_OFFSET + 1005
#define RTEV_ERROR_NODES				RTEV_OFFSET + 1006
#define RTEV_ERROR_NATTR				RTEV_OFFSET + 1007
#define RTEV_ERROR_ORTE_BPROC_SUBSCRIBE	RTEV_OFFSET + 1008
#define RTEV_ERROR_SIGNAL				RTEV_OFFSET + 1009

/*
 * Queue attributes
 */
#define DEFAULT_QUEUE_NAME			"default"

/*
 * ORTE attributes
 */
#define ORTE_JOB_NAME_FMT			"job%d"

int ORTE_Initialize(int, int, char **);
int ORTE_ModelDef(int, int, char **);
int ORTE_StartEvents(int, int, char **);
int ORTE_StopEvents(int, int, char **);
int ORTE_SubmitJob(int, int, char **);
int ORTE_TerminateJob(int, int, char **);
int ORTE_Quit(int, int, char **);

struct ptp_machine {
	int		id;
	List *	nodes;
};
typedef struct ptp_machine	ptp_machine;

struct ptp_node {
	int 	id;
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

struct ptp_job {
	int 			ptp_jobid;		// job ID as known by PTP */
	int				debug_jobid;	// job ID of debugger or -1 if not a debug job
	int 			orte_jobid;		// job ID that will be used by program when it starts
	int				num_procs;		// number of procs requested for program (debugger uses num_procs+1)
	int				terminating;	// job termination has been requested
	ptp_process **	procs;
};
typedef struct ptp_job ptp_job;

int do_orte_init(int, char *universe_name);
static void	get_proc_info(ptp_job *);
static int get_node_attributes(ptp_machine *mach, ptp_node **first_node, ptp_node **last_node);
static void job_state_callback(orte_jobid_t jobid, orte_proc_state_t state);
static void iof_callback(orte_process_name_t* src_name, orte_iof_base_tag_t src_tag, void* cbdata, const unsigned char* data, size_t count);
static void orte_job_record_start(orte_jobid_t jobid);
static void orte_job_record_end(orte_jobid_t jobid);
static int orte_job_record_started(orte_jobid_t jobid);

#ifdef HAVE_SYS_BPROC_H
int subscribe_bproc(void);
#else /* HAVE_SYS_BPROC_H */
/*
 * Provide some fake values.
 */
#define ORTE_SOH_BPROC_NODE_USER	"ORTE_SOH_BPROC_NODE_USER"
#define ORTE_SOH_BPROC_NODE_GROUP	"ORTE_SOH_BPROC_NODE_GROUP"
#define ORTE_SOH_BPROC_NODE_STATUS	"ORTE_SOH_BPROC_NODE_STATUS"
#define ORTE_SOH_BPROC_NODE_MODE	"ORTE_SOH_BPROC_NODE_MODE"
#endif /* HAVE_SYS_BPROC_H */

static int			gTransID = 0; /* transaction id for start of event stream, is 0 when events are off */
static int			gBaseID = 0; /* base ID for event generation */
static int			gLastID = 1; /* ID generator */
static int			gQueueID; /* ID of default queue */
static int 			proxy_state = STATE_INIT;
static proxy_svr *	orte_proxy;
static pid_t		orted_pid = 0;
static List *		gJobList;
static List *		gMachineList;
static int			ptp_signal_exit;
static RETSIGTYPE	(*saved_signals[NSIG])(int);

static proxy_svr_helper_funcs helper_funcs = {
	NULL,					// newconn() - can be used to reject connections
	NULL					// numservers() - if there are multiple servers, return the number
};

#define CMD_BASE	0

static proxy_cmd	cmds[] = {
	ORTE_Quit,
	ORTE_Initialize,
	ORTE_ModelDef,
	ORTE_StartEvents,
	ORTE_StopEvents,
	ORTE_SubmitJob,
	ORTE_TerminateJob
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

#define JOBID_PTP	0
#define JOBID_ORTE	1
#define JOBID_DEBUG	2

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
new_machine()
{
	ptp_machine *	m = (ptp_machine *)malloc(sizeof(ptp_machine));
	m->id = generate_id();
	m->nodes = NewList();
    AddToList(gMachineList, (void *)m);
    return m;
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

/*
 * Select correct jobid
 */
static int
get_jobid(ptp_job *j, int which)
{
	if (j == NULL)
		return -1;
	switch (which) {
	case JOBID_PTP:
		return j->ptp_jobid;
	case JOBID_ORTE:
		return j->orte_jobid;
	case JOBID_DEBUG:
		return j->debug_jobid;
	}
	return -1;
}

/*
 * Keep a list of the jobs that we have created. If they are
 * debug jobs, keep the debug jobid as well.
 */
static ptp_job *
new_job(int num_procs, int ptp_jobid, int orte_jobid, int debug_jobid)
{
	ptp_job *	j = (ptp_job *)malloc(sizeof(ptp_job));
	j->ptp_jobid = ptp_jobid;
    j->orte_jobid = orte_jobid;
    j->debug_jobid = debug_jobid;
    j->num_procs = num_procs;
    j->terminating = false;
    j->procs = (ptp_process **)malloc(sizeof(ptp_process *) * num_procs);
    memset(j->procs, 0, sizeof(ptp_process *) * num_procs);
    AddToList(gJobList, (void *)j);
    return j;
}

static void
free_job(ptp_job *j)
{
	int	i;
	
	for (i = 0; i < j->num_procs; i++) {
		if (j->procs[i] != NULL)
			free_process(j->procs[i]);
	}
	free(j->procs);
	free(j);
}

/*
 * Remove job from the list when it terminates. If debug is true, 
 * lookup the job using the debug jobid.
 */
static void
remove_job(int jobid, int which)
{
	ptp_job *	j;
	
	for (SetList(gJobList); (j = (ptp_job *)GetListElement(gJobList)) != NULL; ) {
		if (get_jobid(j, which) == jobid) {
			RemoveFromList(gJobList, (void *)j);
			free_job(j);
			break;
		}
	}
}

/*
 * Find a job on the list. If debug is true, find the
 * job using the debug jobid.
 */
static ptp_job *
find_job(int jobid, int which)
{
	ptp_job *	j;
	
	for (SetList(gJobList); (j = (ptp_job *)GetListElement(gJobList)) != NULL; ) {
		if (get_jobid(j, which) == jobid) {
			return j;
		}
	}
	return NULL;
}

 
/**
 * See org.eclipse.ptp.rtsysmte.AbstractProxyRuntimeSystem for a description
 * of the protocol format.
 */
static void
sendOKEvent(int trans_id)
{
 	proxy_msg *	m = new_proxy_msg(PROXY_EV_OK, trans_id);
	proxy_svr_queue_msg(orte_proxy, m);
}

static int
sendErrorEvent(int trans_id, int code, char *fmt, ...)
{
	va_list		ap;
	char *		msg;
	proxy_msg *	m = new_proxy_msg(PROXY_EV_ERROR, trans_id);
	
	va_start(ap, fmt);
	vasprintf(&msg, fmt, ap);
	va_end(ap);
	
	proxy_msg_add_int(m, code);
	proxy_msg_add_string(m, msg);
	proxy_svr_queue_msg(orte_proxy, m);
	
	return 0;	
}

static int
sendAttrDefIntEvent(int trans_id, char *id, char *name, char *desc, int def)
{
	proxy_msg *	m = new_proxy_msg(PROXY_EV_RT_ATTR_DEF, trans_id);
	
	proxy_msg_add_int(m, 1); /* 1 attribute def */
	proxy_msg_add_int(m, 5); /* 5 args in this attribute def */
	proxy_msg_add_string(m, id);
	proxy_msg_add_string(m, "INTEGER");
	proxy_msg_add_string(m, name);
	proxy_msg_add_string(m, desc);
	proxy_msg_add_int(m, def);
	proxy_svr_queue_msg(orte_proxy, m);
	
	return 0;	
}


static int
sendAttrDefStringEvent(int trans_id, char *id, char *name, char *desc, char *def)
{
	proxy_msg *	m = new_proxy_msg(PROXY_EV_RT_ATTR_DEF, trans_id);
	
	proxy_msg_add_int(m, 1); /* 1 attribute def */
	proxy_msg_add_int(m, 5); /* 5 args in this attribute def */
	proxy_msg_add_string(m, id);
	proxy_msg_add_string(m, "STRING");
	proxy_msg_add_string(m, name);
	proxy_msg_add_string(m, desc);
	proxy_msg_add_string(m, def);
	proxy_svr_queue_msg(orte_proxy, m);
	
	return 0;	
}

static int
sendNewMachineEvent(int trans_id, int id, char *name)
{
	proxy_msg *	m = new_proxy_msg(PROXY_EV_RT_NEW_MACHINE, trans_id);
	
	proxy_msg_add_int(m, gBaseID);
	proxy_msg_add_int(m, 1);	
	proxy_msg_add_int(m, id);
	proxy_msg_add_int(m, 1);
	proxy_msg_add_keyval_string(m, ELEMENT_NAME_ATTR, name);
	proxy_svr_queue_msg(orte_proxy, m);
	
	return 0;	
}

static int
num_node_attrs(ptp_node *node)
{
	int	cnt = 0;
	if (node->name != NULL)
		cnt++;
	if (node->state != NULL)
		cnt++;
	if (node->number >= 0)
		cnt++;
#ifdef HAVE_SYS_BPROC_H
	if (node->user != NULL)
		cnt++;
	if (node->group != NULL)
		cnt++;
	if (node->mode != NULL)
		cnt++;
#endif /* HAVE_SYS_BPROC_H */
	return cnt;	
}

/*
 * NOTE: sending a NODE_NUMBER_ATTR will enable the node number ruler in the machines view.
 */
static void
add_node_attrs(proxy_msg *m, ptp_node *node)
{
	if (node->name != NULL)
		proxy_msg_add_keyval_string(m, ELEMENT_NAME_ATTR, node->name);
	if (node->state != NULL)
		proxy_msg_add_keyval_string(m, NODE_STATE_ATTR, node->state);
	if (node->number >= 0)
		proxy_msg_add_keyval_int(m, NODE_NUMBER_ATTR, node->number);
#ifdef HAVE_SYS_BPROC_H
	if (node->user != NULL)
		proxy_msg_add_keyval_string(m, NODE_USER_ATTR, node->user);
	if (node->group != NULL)
		proxy_msg_add_keyval_string(m, NODE_GROUP_ATTR, node->group);
	if (node->mode != NULL)
		proxy_msg_add_keyval_string(m, NODE_MODE_ATTR, node->mode);
#endif /* HAVE_SYS_BPROC_H */
}

static int
sendNewJobEvent(int trans_id, int jobid, char *name, int jobSubId, char *state)
{
	proxy_msg *	m = new_proxy_msg(PROXY_EV_RT_NEW_JOB, trans_id);
	
	proxy_msg_add_int(m, gQueueID);	
	proxy_msg_add_int(m, 1);	
	proxy_msg_add_int(m, jobid);
	proxy_msg_add_int(m, 3);	
	proxy_msg_add_keyval_string(m, ELEMENT_NAME_ATTR, name);
	proxy_msg_add_keyval_int(m, JOB_SUB_ID_ATTR, jobSubId);
	proxy_msg_add_keyval_string(m, JOB_STATE_ATTR, state);
	proxy_svr_queue_msg(orte_proxy, m);
	
	return 0;	
}

static int
sendNewNodeEvent(int trans_id, int mach_id, ptp_machine *mach)
{
	ptp_node *	n;
	proxy_msg *	m = new_proxy_msg(PROXY_EV_RT_NEW_NODE, trans_id);
	
	proxy_msg_add_int(m, mach_id);
	proxy_msg_add_int(m, SizeOfList(mach->nodes));
	for (SetList(mach->nodes); (n = (ptp_node *)GetListElement(mach->nodes)) != NULL; ) {
		proxy_msg_add_int(m, n->id);
		proxy_msg_add_int(m, num_node_attrs(n));
		add_node_attrs(m, n);
	}
	proxy_svr_queue_msg(orte_proxy, m);
	
	return 0;	
}

static int
sendNewProcessEvent(int trans_id, int job_id, ptp_process *p, char *state)
{
	proxy_msg *	m = new_proxy_msg(PROXY_EV_RT_NEW_PROCESS, trans_id);
	
	proxy_msg_add_int(m, job_id);	
	proxy_msg_add_int(m, 1);	
	proxy_msg_add_int(m, p->id);	
	proxy_msg_add_int(m, 4);	
	proxy_msg_add_keyval_int(m, PROC_NODEID_ATTR, p->node_id);	
	proxy_msg_add_keyval_string(m, PROC_STATE_ATTR, state);	
	proxy_msg_add_keyval_int(m, PROC_NUMBER_ATTR, p->task_id);	
	proxy_msg_add_keyval_int(m, PROC_PID_ATTR, p->pid);	
	proxy_svr_queue_msg(orte_proxy, m);
	
	return 0;	
}

static int
sendNewQueueEvent(int trans_id)
{
	proxy_msg *	m = new_proxy_msg(PROXY_EV_RT_NEW_QUEUE, trans_id);
	
	gQueueID = generate_id();
	
	proxy_msg_add_int(m, gBaseID);	
	proxy_msg_add_int(m, 1);	
	proxy_msg_add_int(m, gQueueID);
	proxy_msg_add_int(m, 1);	
	proxy_msg_add_keyval_string(m, ELEMENT_NAME_ATTR, DEFAULT_QUEUE_NAME);
	proxy_svr_queue_msg(orte_proxy, m);
	
	return 0;	
}

static int
sendProcessStateChangeEvent(int trans_id, ptp_job *j, char *state)
{
	char *		range;
	proxy_msg *	m;
	
	if (j == NULL || j->num_procs == 0)
		return 0;
		
	m = new_proxy_msg(PROXY_EV_RT_PROCESS_CHANGE, trans_id);
	
	asprintf(&range, "%d-%d", j->procs[0]->id, j->procs[j->num_procs-1]->id);
	proxy_msg_add_int(m, 1);	
	proxy_msg_add_string(m, range);
	proxy_msg_add_int(m, 1);	
	proxy_msg_add_keyval_string(m, PROC_STATE_ATTR, state);
	proxy_svr_queue_msg(orte_proxy, m);
	free(range);
	
	return 0;
}
	
static int
sendJobStateChangeEvent(int trans_id, int jobid, char *state)
{
	proxy_msg *	m = new_proxy_msg(PROXY_EV_RT_JOB_CHANGE, trans_id);
	
	proxy_msg_add_int(m, 1);	
	proxy_msg_add_int(m, jobid);
	proxy_msg_add_int(m, 1);	
	proxy_msg_add_keyval_string(m, JOB_STATE_ATTR, state);
	proxy_svr_queue_msg(orte_proxy, m);
	
	return 0;	
}

static int
sendNodeChangeEvent(int trans_id, ptp_node *node)
{
	proxy_msg *	m = new_proxy_msg(PROXY_EV_RT_NODE_CHANGE, trans_id);
	
	proxy_msg_add_int(m, 1);	
	proxy_msg_add_int(m, node->id);	
	proxy_msg_add_int(m, num_node_attrs(node));	
	add_node_attrs(m, node);
	proxy_svr_queue_msg(orte_proxy, m);
	
	return 0;	
}

static int
sendProcessChangeEvent(int trans_id, ptp_process *p, int node_id, int task_id, int pid)
{
	int			cnt = 0;
	proxy_msg *	m;
	
	if (p->node_id != node_id || p->task_id != task_id || p->pid != pid) {
		m = new_proxy_msg(PROXY_EV_RT_PROCESS_CHANGE, trans_id);
		
		proxy_msg_add_int(m, 1);	
		proxy_msg_add_int(m, p->id);
		
		if (p->node_id != node_id) {
			cnt++;	
		}
		if (p->task_id != task_id) {
			cnt++;	
		}
		if (p->pid != pid) {
			cnt++;	
		}
		
		proxy_msg_add_int(m, cnt);
			
		if (p->node_id != node_id) {
			p->node_id = node_id;
			proxy_msg_add_keyval_int(m, ELEMENT_ID_ATTR, node_id);	
		}
		if (p->task_id != task_id) {
			p->task_id = task_id;
			proxy_msg_add_keyval_int(m, PROC_NUMBER_ATTR, task_id);	
		}
		if (p->pid != pid) {
			p->pid = pid;
			proxy_msg_add_keyval_int(m, PROC_PID_ATTR, pid);	
		}
		
		proxy_svr_queue_msg(orte_proxy, m);
	}
	
	return 0;	
}

/*
 * TODO: optimize this so that we don't send one event for
 * every process, even if the output is identical.
 */
static int
sendProcessOutputEvent(int trans_id, int procid, char *output)
{
	proxy_msg *	m = new_proxy_msg(PROXY_EV_RT_PROCESS_CHANGE, trans_id);
	
	proxy_msg_add_int(m, 1);
	proxy_msg_add_int(m, procid);
	proxy_msg_add_int(m, 1);
	proxy_msg_add_keyval_string(m, PROC_STDOUT_ATTR, output);
	proxy_svr_queue_msg(orte_proxy, m);
	
	return 0;	
}

int
ORTECheckErrorCode(int trans_id, int type, int rc)
{
	if(rc != ORTE_SUCCESS) {
		printf("ARgh!  An error!\n"); fflush(stdout);
		printf("ERROR %s\n", ORTE_ERROR_NAME(rc)); fflush(stdout);
		sendErrorEvent(trans_id, type, (char *)ORTE_ERROR_NAME(rc));
		return 1;
	}
	
	return 0;
}

/*
 * This callback is invoked when there is I/O available from a
 * process. It gets forwarded to Eclipse.
 * 
 * This needs to be modified to avoid flooding Eclipse with
 * events.
 */
static void 
iof_callback(
    orte_process_name_t* src_name,
    orte_iof_base_tag_t src_tag,
    void* cbdata,
    const unsigned char* data,
    size_t count)
{
	char *			line;
	ptp_process *	p;
	
    if(count > 0) {
    	ptp_job *	j = find_job((int)src_name->jobid, JOBID_ORTE);
    	if (j != NULL) {
	        line = (char *)malloc(count+1);
	        strncpy((char*)line, (char*)data, count);
	        if (line[count-1] == '\n') line[count-1] = '\0';
	        line[count] = '\0';
	        p = find_process(j, (int)src_name->vpid);
	        if (p != NULL)
	        	sendProcessOutputEvent(gTransID, p->id, line);
	        free(line);
    	}
    }
}

struct orte_job_record {
	struct orte_job_record *next;
	orte_jobid_t jobid;
};

struct orte_job_record *head = NULL;

static int 
orte_job_record_started(orte_jobid_t jobid)
{
	struct orte_job_record *cur = NULL;
	if(head == NULL) return 0;
	
	cur = head;
	
	while(cur != NULL) {
		if(cur->jobid == jobid) return 1;
		cur = cur->next;
	}
	
	return 0; /* guess we didn't find it and we're at the end */
}

static void
orte_job_record_start(orte_jobid_t jobid)
{
	struct orte_job_record *cur = NULL;
	struct orte_job_record *tmp = NULL;
	int	rc;
	orte_process_name_t *	name;
	
	/* if this is the first, we need to set it up */
	if(head == NULL) {
		head = (struct orte_job_record *)malloc(sizeof(struct orte_job_record));
		head->next = NULL;
	}
	cur = head;
	/* run to the end */
	while(cur->next != NULL) cur = cur->next;
	/* now we know 'cur' is the tail */
	tmp = (struct orte_job_record*)malloc(sizeof(struct orte_job_record));
	cur->next = tmp;
	tmp->next = NULL;
	tmp->jobid = jobid;
	
	/* register the IO forwarding callback */
	if (ORTE_SUCCESS != (rc = orte_ns.create_process_name(&name, 0, jobid, 0))) {
		ORTE_ERROR_LOG(rc);
        	return;
    	}
    	
    	printf("registering IO forwarding - name = '%s'\n", (char *)name); fflush(stdout);
            	
	if (ORTE_SUCCESS != (rc = orte_iof.iof_subscribe(name, ORTE_NS_CMP_JOBID, ORTE_IOF_STDOUT, iof_callback, NULL))) {                
		opal_output(0, "[%s:%d] orte_iof.iof_subscribed failed\n", __FILE__, __LINE__);
    	}
    	if (ORTE_SUCCESS != (rc = orte_iof.iof_subscribe(name, ORTE_NS_CMP_JOBID, ORTE_IOF_STDERR, iof_callback, NULL))) {                
        	opal_output(0, "[%s:%d] orte_iof.iof_subscribed failed\n", __FILE__, __LINE__);
    	}
}

static void
orte_job_record_end(orte_jobid_t jobid)
{
	struct orte_job_record *cur = NULL;
	struct orte_job_record *prev = NULL;
	int	rc;
	orte_process_name_t *	name;
	if(head == NULL) return; /* umm, we have nothing */
	
	cur = head;
	while(cur != NULL) {
		if(cur->jobid == jobid) {
			/* ok we found it, we need to move the pointer behind us */
			prev->next = cur->next; /* parent's next is our next */
			/* now free cur */
			free(cur);
			
			if (ORTE_SUCCESS != (rc = orte_ns.create_process_name(&name, 0, jobid, 0))) {
             	ORTE_ERROR_LOG(rc);
                	return;
            	}
            	printf("unregistering IO forwarding - name = %s\n", (char *)name); fflush(stdout);
            	if (ORTE_SUCCESS != (rc = orte_iof.iof_unsubscribe(name, ORTE_NS_CMP_JOBID, ORTE_IOF_STDOUT))) {                
				opal_output(0, "[%s:%d] orte_iof.iof_unsubscribed failed\n", __FILE__, __LINE__);
			}
			if (ORTE_SUCCESS != (rc = orte_iof.iof_unsubscribe(name, ORTE_NS_CMP_JOBID, ORTE_IOF_STDERR))) {                
				opal_output(0, "[%s:%d] orte_iof.iof_unsubscribed failed\n", __FILE__, __LINE__);
			}
			
			return;
		}
		prev = cur;
		cur = cur->next;
	}
}

/*
 * This callback is invoked when there is a job state change. We are mainly
 * interested in when a job is running and when it terminates so the process
 * icons can be updated appropriately.
 */
static void
job_state_callback(orte_jobid_t jobid, orte_proc_state_t proc_state)
{
	char *		state;
	ptp_job *	j = find_job(jobid, JOBID_ORTE);
	
	if (j == NULL) {
		printf("JOB STATE CALLBACK: could not find jobid %d\n", jobid); fflush(stdout);
		return;
	}
	
	printf("JOB STATE CALLBACK: %d\n", proc_state); fflush(stdout);
		
	/* not sure yet how we want to handle this callback, what events
	 * we want to generate, but here are the states that I know of
	 * that a job can go through.  I've watched ORTE call this callback
	 * with each of these states.  We'll want to come in here and
	 * generate events where appropriate */
	
	switch (proc_state) {
#if !ORTE_VERSION_1_0
		case ORTE_JOB_STATE_LAUNCHED:
			get_proc_info(j);
			break;
#endif /* !ORTE_VERSION_1_0 */
			
#if ORTE_VERSION_1_0
		case ORTE_JOB_STATE_AT_STG1:
#else /* ORTE_VERSION_1_0 */
		case ORTE_JOB_STATE_AT_STG2:
#endif /* ORTE_VERSION_1_0 */
		case ORTE_JOB_STATE_RUNNING:
			state = JOB_STATE_RUNNING;
			sendProcessStateChangeEvent(gTransID, j, PROC_STATE_RUNNING);
			sendJobStateChangeEvent(gTransID, j->ptp_jobid, state);
			break;
			
		case ORTE_JOB_STATE_TERMINATED:
			if (orte_job_record_started(jobid)) {
				orte_job_record_end(jobid);
			}
			ORTE_TERMINATE_ORTEDS(jobid);
			sendProcessStateChangeEvent(gTransID, j, PROC_STATE_EXITED);
			sendJobStateChangeEvent(gTransID, j->ptp_jobid, JOB_STATE_TERMINATED);
			remove_job(jobid, JOBID_ORTE);
			break;
			
		case ORTE_JOB_STATE_ABORTED:
			if (orte_job_record_started(jobid)) {
				orte_job_record_end(jobid);
			}
			ORTE_TERMINATE_ORTEDS(jobid);
			sendProcessStateChangeEvent(gTransID, j, PROC_STATE_ERROR);
			sendJobStateChangeEvent(gTransID, j->ptp_jobid, JOB_STATE_TERMINATED);
			remove_job(jobid, JOBID_ORTE);
			break;

#if !ORTE_VERSION_1_0
		case ORTE_JOB_STATE_FAILED_TO_START:
			state = JOB_STATE_ERROR;
			sendJobStateChangeEvent(gTransID, j->ptp_jobid, state);
			remove_job(jobid, JOBID_ORTE);
			break;
#endif /* !ORTE_VERSION_1_0 */
			
		default: /* ignore others */
			return;
	}
}
    

#if !ORTE_VERSION_1_0
/*
 * Called to get process info when a new job is created. Subsequent calls will
 * result in process change events if anything has changed.
 */
static void
get_proc_info(ptp_job *j)
{
	int					i;
	int					rc;
	ptp_process *		p;
	char *				segment = NULL;
	ORTE_STD_CNTR_TYPE	cnt;
	orte_gpr_value_t **	values;
	char *				keys[] = {
		ORTE_NODE_NAME_KEY,
		ORTE_PROC_LOCAL_PID_KEY,
		ORTE_PROC_RANK_KEY,
		NULL
	};
	
   if((rc = orte_schema.get_job_segment_name(&segment, j->orte_jobid)) != ORTE_SUCCESS) {
        ORTE_ERROR_LOG(rc);
        return;
    }
	
	rc = orte_gpr.get(ORTE_GPR_KEYS_OR | ORTE_GPR_TOKENS_OR, segment, NULL, keys, &cnt, &values);
	if(rc != ORTE_SUCCESS) {
		free(segment);
		return;
	}
	
    for (i = 0; i < cnt; i++) {
    	int					k;
    	int					pid = 0;
    	int					task_id = 0;
    	int					node_id = 0;
    	ptp_node *			node;
		pid_t	*			pidptr;
		orte_std_cntr_t		*rankptr;
		orte_gpr_value_t *	value = values[i];
		
		/*
		 * Find process rank. Make sure it gets added to event before
		 * the other attributes.
		 */
		for(k = 0; k < value->cnt; k++) {
			orte_gpr_keyval_t *	keyval = value->keyvals[k];
			if(strcmp(keyval->key, ORTE_PROC_RANK_KEY) == 0) {
				if (ORTE_SUCCESS != (rc = orte_dss.get((void**)&rankptr, keyval->value, ORTE_STD_CNTR))) {
					ORTE_ERROR_LOG(rc);
					continue;          
				}
				task_id = *rankptr;       
			} else if(strcmp(keyval->key, ORTE_NODE_NAME_KEY) == 0) {
				node = find_node_by_name((char*)(keyval->value->data));
				if (node != NULL)
					node_id = node->id;             
			} else if(strcmp(keyval->key, ORTE_PROC_LOCAL_PID_KEY) == 0) {
				if (ORTE_SUCCESS != (rc = orte_dss.get((void**)&pidptr, keyval->value, ORTE_PID))) {
					ORTE_ERROR_LOG(rc);
					continue;          
				}       
				pid = *pidptr;          
			}
		}
		
		p = find_process(j, task_id);
		if (p == NULL) {
			p = new_process(j, node_id, task_id, pid);
	    	sendNewProcessEvent(gTransID, j->ptp_jobid, p, PROC_STATE_STARTING);
		} else {
			sendProcessChangeEvent(gTransID, p, node_id, task_id, pid);
		}
    }
}
#endif /* !ORTE_VERSION_1_0 */

#if ORTE_VERSION_1_0
/*
 * Find the number of processes started for a particular job.
 */
int
get_num_procs(orte_jobid_t jobid)
{
	char *keys[2];
	int rc, ret;
	ORTE_STD_CNTR_TYPE cnt;
	char *segment = NULL;
	char *jobid_str = NULL;
	orte_gpr_value_t **values;
	
	keys[0] = ORTE_PROC_PID_KEY;
	keys[1] = NULL;
	
	rc = orte_ns.convert_jobid_to_string(&jobid_str, jobid);
	if(rc != ORTE_SUCCESS) {
		ret = 0;
		goto cleanup;
	}
	
	asprintf(&segment, "%s-%s", ORTE_JOB_SEGMENT, jobid_str);
	
	rc = orte_gpr.get(ORTE_GPR_KEYS_OR | ORTE_GPR_TOKENS_OR, segment, NULL, keys, &cnt, &values);
	if(rc != ORTE_SUCCESS) {
		ret = 0;
		goto cleanup;
	}
	
	ret = cnt;
	
cleanup:
	if(jobid_str != NULL)
		free(jobid_str);
	if(segment != NULL)
		free(segment);
		
	return ret;
}
#endif /* ORTE_VERSION_1_0 */

static int
get_num_nodes(int machid)
{
	int rc;
	ORTE_STD_CNTR_TYPE cnt;
	orte_gpr_value_t **values;
	
	/* we're going to ignore machine ID until ORTE implements that */
	
	rc = orte_gpr.get(ORTE_GPR_KEYS_OR|ORTE_GPR_TOKENS_OR, ORTE_NODE_SEGMENT, NULL, NULL, &cnt, &values);
                   
	if(rc != ORTE_SUCCESS) {
		return 0;
	}
	
	return cnt;
}

/* 
 * one day ORTE will have the notion of number of machines.
 * until then, we have only 1 machine
 */
static int 
get_num_machines()
{
	return 1;
}

static char *
get_machine_name(int num)
{
	static char	hostname[BUFSIZ+1];
	
	gethostname(hostname, BUFSIZ);
	
	return hostname;
}

static int
get_node_attributes(ptp_machine *mach, ptp_node **first_node, ptp_node **last_node)
{
	int					rc;
	int					i;
	char *				name;
	char *				user = NULL;
	char *				group = NULL;
	char *				status = NODE_STATE_UP;
	char *				mode = NULL;
	ptp_node *			node;
	ORTE_STD_CNTR_TYPE	cnt;
	orte_gpr_value_t **	values;

	rc = orte_gpr.get(ORTE_GPR_KEYS_OR | ORTE_GPR_TOKENS_OR, ORTE_NODE_SEGMENT, NULL, NULL, &cnt, &values);
	if (rc != ORTE_SUCCESS)
		return 1;
		
	for (i = 0; i < cnt; i++) {
    	int					k;
		orte_gpr_value_t *	value = values[i];
		
		for(k = 0; k < value->cnt; k++) {
			orte_gpr_keyval_t *	keyval = value->keyvals[k];
			
			if (strcmp(keyval->key, ORTE_NODE_NAME_KEY) == 0) {
				name = ORTE_GET_STRING_VALUE(keyval);
			}              
#ifdef HAVE_SYS_BPROC_H
			else if (strcmp(input_keys[j], ORTE_SOH_BPROC_NODE_USER) == 0) {
				user = ORTE_GET_STRING_VALUE(keyval);
			} else if (!strcmp(input_keys[j], ORTE_SOH_BPROC_NODE_GROUP) == 0) {
				group = ORTE_GET_STRING_VALUE(keyval);
			} else if (!strcmp(input_keys[j], ORTE_SOH_BPROC_NODE_STATUS) == 0) {
				status = ORTE_GET_STRING_VALUE(keyval);
			} else if (!strcmp(input_keys[j], ORTE_SOH_BPROC_NODE_MODE) == 0) {
				asprintf(&mode, "%s", ORTE_GET_UINT32_VALUE(keyval));
			}
#endif  /* HAVE_SYS_BPROC_H */			
		}
		
		node = new_node(mach, name, status, user, group, mode);
		if (i == 0)
			*first_node = node;
			
#ifdef HAVE_SYS_BPROC_H
		free(mode);
#endif /* HAVE_SYS_BPROC_H */
    }
    
    *last_node = node;

	return 0;
}

/*
 * If we're under debug control, let the debugger handle process state update. 
 * We still want to wire up stdio though.
 * 
 * Note: this will only be used if the debugger allows the program to
 * reach MPI_Init(), which may not ever happen. Don't rely this to do anything
 * for any type of job.
 * 
 * Note also: the debugger manages process state updates so we don't need
 * to send events back to the runtime.
 */
static void
debug_app_job_state_callback(orte_jobid_t jobid, orte_proc_state_t state)
{
	switch(state) {
		case ORTE_PROC_STATE_TERMINATED:
		case ORTE_PROC_STATE_ABORTED:
			if (orte_job_record_started(jobid)) {
				orte_job_record_end(jobid);
			}
			break;
	}
}

/*
 * job_state_callback for the debugger. Detects debugger start and exit and notifies the
 * UI. Cleans up job id map.
 */
static void
debug_job_state_callback(orte_jobid_t jobid, orte_proc_state_t state)
{
	int				app_jobid;
	char *			job_state;
	ptp_job	*		j;
	
	if ((j = find_job(jobid, JOBID_DEBUG)) == NULL)
		return;
	
	app_jobid = j->ptp_jobid;
	
	switch(state) {
#if !ORTE_VERSION_1_0
		case ORTE_JOB_STATE_AT_STG2:
#endif /* !ORTE_VERSION_1_0 */
		case ORTE_JOB_STATE_RUNNING:
			job_state = JOB_STATE_RUNNING;
			break;
		case ORTE_PROC_STATE_TERMINATED:
		case ORTE_PROC_STATE_ABORTED:
			job_state = JOB_STATE_TERMINATED;
           	remove_job(jobid, JOBID_DEBUG);
    		break;
    	default:
    		return;
	}
	
	sendJobStateChangeEvent(gTransID, app_jobid, job_state);
}

/*
 * Debug spawner. To spawn a debug job, two process allocations must be made. 
 * This first is for the application and the second for the debugger (which is
 * an MPI program). We then launch the debugger, and let it deal with starting
 * the application processes.
 * 
 * This will need to be modified to support attaching.
 */
static int
debug_spawn(char *debug_path, int argc, char **argv, orte_app_context_t** app_context, size_t num_context, orte_jobid_t* app_jobid, orte_jobid_t* debug_jobid)
{
	int						i;
	int						rc;
	orte_jobid_t			jid1;
	orte_jobid_t			jid2;
	orte_app_context_t *	debug_context;

	rc = ORTE_ALLOCATE_JOB(app_context, num_context, &jid1, debug_app_job_state_callback);
	if (rc != ORTE_SUCCESS) {
		ORTE_ERROR_LOG(rc);
		return rc;
	}

	debug_context = OBJ_NEW(orte_app_context_t);
	debug_context->num_procs = app_context[0]->num_procs + 1;
	debug_context->app = debug_path;
	debug_context->cwd = strdup(app_context[0]->cwd);
	/* no special environment variables */
#if ORTE_VERSION_1_0
	debug_context->num_env = 0;
#endif /* ORTE_VERSION_1_0 */
	debug_context->env = NULL;
	/* no special mapping of processes to nodes */
	debug_context->num_map = 0;
	debug_context->map_data = NULL;
	/* setup argv */
	debug_context->argv = (char **)malloc((argc+2) * sizeof(char *));
	for (i = 0; i < argc; i++) {
		debug_context->argv[i] = strdup(argv[i]);
	}
	asprintf(&debug_context->argv[i++], "--jobid=%d", jid1);
	debug_context->argv[i++] = NULL;
#if ORTE_VERSION_1_0
	debug_context->argc = i;
#endif /* ORTE_VERSION_1_0 */

	rc = ORTE_ALLOCATE_JOB(&debug_context, 1, &jid2, debug_job_state_callback);
	if (rc != ORTE_SUCCESS) {
		OBJ_RELEASE(debug_context);
		ORTE_ERROR_LOG(rc);
		return rc;
	}

	printf("About to launch debugger: %s on %d procs\n", debug_path, 	debug_context->num_procs);
	
	/*
	 * launch the debugger
	 */
	if (ORTE_SUCCESS != (rc = ORTE_LAUNCH_JOB(jid2))) {
		OBJ_RELEASE(debug_context);
		ORTE_ERROR_LOG(rc);
		return rc;
	}

	*app_jobid = jid1;
	*debug_jobid = jid2;
    
    OBJ_RELEASE(debug_context);
    
	return ORTE_SUCCESS;
}

int
do_orte_init(int trans_id, char *universe_name)
{
	int rc;
	char *str;
		
	printf("ORTEInit (%s)\n", universe_name); fflush(stdout);
	asprintf(&str, "OMPI_MCA_universe=%s", universe_name);
	putenv(str);
	
	/* 
	 * make the orte_init() fail if the orte daemon isn't running
	 */
	putenv("OMPI_MCA_orte_univ_exist=1");

	rc = orte_init(true);
	
	if(ORTECheckErrorCode(trans_id, RTEV_ERROR_ORTE_INIT, rc)) return 1;
	
	/* this code was given to me to put in here to force the system to populate the node segment
	 * in ORTE.  It basically crashes us if we use our own universe name.  I'm leaving it here
	 * because I think one day we might be able to find a way to use this right. */
#if 0
	{
	orte_ras_base_module_t *module = NULL;
  	orte_jobid_t jobid = 1;
	printf("Calling RDS_base_query() . . .\n"); fflush(stdout);
	orte_rds_base_query();
	printf("Calling RAS_Base_allocate() . . .\n"); fflush(stdout);
	orte_ras_base_allocate(jobid, &module);
	printf("Success with BOTH!\n"); fflush(stdout);
	}
#endif
	
	return 0;
}

#if ORTE_VERSION_1_0
/*
 * This callback gets invoked when any attributes of a process get changed.
 */
static void 
job_proc_notify_callback(orte_gpr_notify_data_t *data, void *cbdata)
{
	int						len;
	size_t					i;
	size_t					j;
	size_t					k;
	orte_gpr_value_t **		values;
	orte_gpr_value_t *		value;
	orte_gpr_keyval_t **	keyvals;
	ptp_job *				job = (ptp_job *)cbdata;
	char *					str1;
	char *					str2;
	char *					res;
	char *					kv = NULL;
	char *					vpid = NULL;
	
	values = (orte_gpr_value_t**)(data->values)->addr;
	
	for(i=0, k=0; k<data->cnt && i < (data->values)->size; i++) {
		if(values[i] == NULL) continue;
		
		k++;
		value = values[i];
		keyvals = value->keyvals;
		
		len = strlen(ORTE_VPID_KEY);
		
		if (strlen(value->tokens[1]) <= len
			|| strncmp(value->tokens[1], ORTE_VPID_KEY, len) != 0)
			continue;
			
		asprintf(&vpid, "%s", value->tokens[1]+len+1);
		
		for(j=0; j<value->cnt; j++) {
			orte_gpr_keyval_t *keyval = keyvals[j];
			char *external_key = NULL;
			char * tmp_str = NULL;

			if (!strcmp(keyval->key, ORTE_NODE_NAME_KEY))
				asprintf(&external_key, "%s", ATTRIB_PROCESS_NODE_NAME);
			else if (!strcmp(keyval->key, ORTE_PROC_PID_KEY))
				asprintf(&external_key, "%s", ATTRIB_PROCESS_PID);
			else
				external_key = strdup(keyval->key);

			if (external_key != NULL) {					
				switch(ORTE_KEYVALUE_TYPE(keyval)) {
					case ORTE_STRING:
						if ((tmp_str = ORTE_GET_STRING_VALUE(keyval)) != NULL);
							asprintf(&kv, "%s=%s", external_key, tmp_str);
						break;
					case ORTE_UINT32:
						asprintf(&kv, "%s=%d", external_key, ORTE_GET_UINT32_VALUE(keyval));
						break;
					case ORTE_PID:
						asprintf(&kv, "%s=%d", external_key, ORTE_GET_PID_VALUE(keyval));
						break;
					default:
						asprintf(&kv, "%s=<unknown type>%d", external_key, ORTE_KEYVALUE_TYPE(keyval));
						break;
				}
	
				if (kv != NULL) {
					if (job != NULL) {
						proxy_cstring_to_str("", &str1);
						proxy_cstring_to_str(kv, &str2);
						asprintf(&res, "%d %d 0:0 %s 1 %s %s", RTEV_PATTR, job->ptp_jobid, str1, vpid, str2);
			        	AddToList(eventList, (void *)res);
			        	free(str1);
			        	free(str2);
					}
					free(kv);
					kv = NULL;
				}
				
				free(external_key);
	        }
		}
		
		free(vpid);
	}
}

/*
 * Subscribe to attribute changes for 'procid' in 'job'.
 */
static int
subscribe_proc(ptp_job * job, int procid)
{
	int							i;
	int							rc;
	char *						jobid_str;
	orte_gpr_subscription_t 	sub;
	orte_gpr_subscription_t *	subs;
	orte_gpr_value_t			value;
	orte_gpr_value_t *			values;
	orte_process_name_t			proc;

	printf("subscribing proc %d\n", procid); fflush(stdout);
	
	rc = orte_ns.convert_jobid_to_string(&jobid_str, job->orte_jobid);
	if(rc != ORTE_SUCCESS) {
		printf("ERROR: '%s'\n", ORTE_ERROR_NAME(rc)); fflush(stdout);
		return -1;
	}
		
	OBJ_CONSTRUCT(&sub, orte_gpr_subscription_t);
	sub.action = ORTE_GPR_NOTIFY_VALUE_CHG;
	
	OBJ_CONSTRUCT(&value, orte_gpr_value_t);
	values = &value;
	sub.values = &values;
	sub.cnt = 1; /* number of values */
	value.addr_mode = ORTE_GPR_TOKENS_XAND | ORTE_GPR_KEYS_OR;
	asprintf(&value.segment, "%s-%s", ORTE_JOB_SEGMENT, jobid_str);
	
	value.cnt = 3; /* number of keyvals */
	value.keyvals = (orte_gpr_keyval_t**)malloc(value.cnt * sizeof(orte_gpr_keyval_t*));
	
	i = 0;
	
	value.keyvals[i] = OBJ_NEW(orte_gpr_keyval_t);
	value.keyvals[i++]->key = strdup(ORTE_NODE_NAME_KEY);

	value.keyvals[i] = OBJ_NEW(orte_gpr_keyval_t);
	value.keyvals[i++]->key = strdup(ORTE_PROC_PID_KEY);
	
	value.keyvals[i] = OBJ_NEW(orte_gpr_keyval_t);
	value.keyvals[i++]->key = strdup(ORTE_PROC_NAME_KEY);

	proc.cellid = 0;
	proc.jobid = job->orte_jobid;
	proc.vpid = procid;
	
	orte_schema.get_proc_tokens(&value.tokens, &value.num_tokens, &proc); /* TODO: what frees tokens? */
	
	sub.cbfunc = job_proc_notify_callback;
	sub.user_tag = (void *)job;
	
	subs = &sub;
	rc = orte_gpr.subscribe(1, &subs, 0, NULL);
	
	if(ORTECheckErrorCode(RTEV_ERROR_ORTE_BPROC_SUBSCRIBE, rc)) return 1;
	
	return 0;
}

/*
 * Subscribe to all processes in a job. 
 * 
 * It would be nice if there was a more efficient way of doing this.
 */
int 
subscribe_job(orte_jobid_t jobid)
{
	int			rc;
	int			vpid;
	int			vpid_start;
	int			vpid_range;
	ptp_job *	job;
	
	if (ORTE_SUCCESS != (rc = ORTE_GET_VPID_RANGE(jobid, &vpid_start, &vpid_range))) {
		printf("no processes for job\n"); fflush(stdout);
		return -1;
	}
	
	job = find_job(jobid, JOBID_ORTE);
	if (job != NULL) {
		printf("subscribing %d procs\n", vpid_range); fflush(stdout);
		
		for (vpid = vpid_start; vpid < vpid_start + vpid_range; vpid++)
			subscribe_proc(job, vpid);
	}
		
	return 0;
}
#endif /* ORTE_VERSION_1_0 */

#ifdef HAVE_SYS_BPROC_H
/*
 * This callback gets invoked when any of the bproc specific state of health attributes
 * change. These are all node-related events, so this is really the monitoring
 * functions for bproc support.
 */
static void 
bproc_notify_callback(orte_gpr_notify_data_t *data, void *cbdata)
{
	size_t i, j, k;
	orte_gpr_value_t **values, *value;
	orte_gpr_keyval_t **keyvals;
	char *res, *kv, *str1, *str2, *str3, *foo, *bar, *nodename;
	int machID;
	
	printf("BPROC NOTIFY CALLBACK!\n"); fflush(stdout);
	
	values = (orte_gpr_value_t**)(data->values)->addr;
	
	machID = 0;
	
	for(i=0, k=0; k<data->cnt && i < (data->values)->size; i++) {
		if(values[i] == NULL) continue;
		
		k++;
		value = values[i];
		keyvals = value->keyvals;
		
		asprintf(&nodename, "%s", value->tokens[1]);
		printf("NODE NAME = %s\n", nodename);
		
		for(j=0; j<value->cnt; j++) {
			orte_gpr_keyval_t *keyval = keyvals[j];
			char *external_key;
			
			printf("--- BPROC CHANGE: key = %s\n", keyval->key); fflush(stdout);
			
			if(!strcmp(keyval->key, ORTE_NODE_NAME_KEY))
				asprintf(&external_key, "%s", ATTRIB_NODE_NAME);
			else if(!strcmp(keyval->key, ORTE_SOH_BPROC_NODE_USER))
				asprintf(&external_key, "%s", ATTRIB_NODE_USER);
			else if(!strcmp(keyval->key, ORTE_SOH_BPROC_NODE_GROUP))
				asprintf(&external_key, "%s", ATTRIB_NODE_GROUP);
			else if(!strcmp(keyval->key, ORTE_SOH_BPROC_NODE_STATUS) || !strcmp(keyval->key, ORTE_NODE_STATE_KEY))
				asprintf(&external_key, "%s", ATTRIB_NODE_STATE);
			else if(!strcmp(keyval->key, ORTE_SOH_BPROC_NODE_MODE))
				asprintf(&external_key, "%s", ATTRIB_NODE_MODE);
			else
				printf("******************* Unknown key type on bproc event - key = '%s'\n", keyval->key); fflush(stdout);
					
			switch(keyval->type) {
				case ORTE_NODE_STATE:
					printf("--- BPROC CHANGE: (state) val = %d\n", keyval->value.node_state); fflush(stdout);
					asprintf(&kv, "%s=%d", external_key, keyval->value.node_state);
					break;
				case ORTE_STRING:
					printf("--- BPROC CHANGE: (str) val = %s\n", keyval->value.strptr); fflush(stdout);
					asprintf(&kv, "%s=%s", external_key, keyval->value.strptr);
					break;
				case ORTE_UINT32:
					printf("--- BPROC CHANGE: (uint32) val = %d\n", keyval->value.ui32); fflush(stdout);
					asprintf(&kv, "%s=%d", external_key, keyval->value.ui32);
					break;
				default:
					printf("--- BPROC CHANGE: unknown type %d\n", keyval->type); fflush(stdout);
					asprintf(&kv, "%s=%d", external_key, keyval->type);
			}
			
			
			asprintf(&foo, "%s=%d", ATTRIB_MACHIINEID, 0);
			asprintf(&bar, "%s=%s", ATTRIB_NODE_NUMBER, nodename); /* for bproc the node number is the same as the name */
			asprintf(&bar, "%s=%s", ATTRIB_NODE_NAME, nodename);
			proxy_cstring_to_str(foo, &str1);
			proxy_cstring_to_str(bar, &str2);
			proxy_cstring_to_str(kv, &str3);
			asprintf(&res, "%d %s %s %s", RTEV_NATTR, str1, str2, str3);
			AddToList(eventList, (void *)res);
			
        	free(kv);
			free(external_key);
        	free(foo);
        	free(bar);
        	free(str1);
        	free(str2);
        	free(str3);
        }
		
		free(nodename);
	}
}

/*
 * Subscribe to the bproc specific attribute changes.
 */
int 
subscribe_bproc(void)
{
	int i, rc;
	orte_gpr_subscription_t sub, *subs;
	orte_gpr_value_t value, *values;
	
	OBJ_CONSTRUCT(&sub, orte_gpr_subscription_t);
	sub.action = ORTE_GPR_NOTIFY_VALUE_CHG;
	
	OBJ_CONSTRUCT(&value, orte_gpr_value_t);
	values = &value;
	sub.values = &values;
	sub.cnt = 1; /* number of values */
	value.addr_mode = ORTE_GPR_TOKENS_XAND | ORTE_GPR_KEYS_OR;
	value.segment = strdup(ORTE_NODE_SEGMENT);
	
	value.cnt = 6; /* number of keyvals */
	value.keyvals = (orte_gpr_keyval_t**)malloc(value.cnt * sizeof(orte_gpr_keyval_t*));
	
	i = 0;
	
	value.keyvals[i] = OBJ_NEW(orte_gpr_keyval_t);
	value.keyvals[i++]->key = strdup(ORTE_NODE_NAME_KEY);
	
	value.keyvals[i] = OBJ_NEW(orte_gpr_keyval_t);
	value.keyvals[i++]->key = strdup(ORTE_NODE_STATE_KEY);
	
	value.keyvals[i] = OBJ_NEW(orte_gpr_keyval_t);
	value.keyvals[i++]->key = strdup(ORTE_SOH_BPROC_NODE_STATUS);
	
	value.keyvals[i] = OBJ_NEW(orte_gpr_keyval_t);
	value.keyvals[i++]->key = strdup(ORTE_SOH_BPROC_NODE_MODE);
	
	value.keyvals[i] = OBJ_NEW(orte_gpr_keyval_t);
	value.keyvals[i++]->key = strdup(ORTE_SOH_BPROC_NODE_USER);
	
	value.keyvals[i] = OBJ_NEW(orte_gpr_keyval_t);
	value.keyvals[i++]->key = strdup(ORTE_SOH_BPROC_NODE_GROUP);
	
	/* any token */
	value.tokens = NULL;
	value.num_tokens = 0;
	
	sub.cbfunc = bproc_notify_callback;
	sub.user_tag = NULL;
	
	subs = &sub;
	rc = orte_gpr.subscribe(1, &subs, 0, NULL);
	
	if(ORTECheckErrorCode(RTEV_ERROR_ORTE_BPROC_SUBSCRIBE, rc)) return 1;
	
	return 0;
}
#endif

/* 
 * tell the daemon to exit 
 */
int
do_orte_shutdown(void)
{
	printf("ORTEShutdown() called.  Telling daemon to turn off.\n"); fflush(stdout);
	ORTE_SHUTDOWN();
	printf("ORTEShutdown() - told ORTEd to exit.\n"); fflush(stdout);
	
	orte_finalize();
	
	return 0;
}

/* 
 * Check for events and call appropriate progress hooks.
 */
int
do_orte_progress(void)
{	
	/* only run the progress of the ORTE code if we've initted the ORTE daemon */
	if(proxy_state == STATE_RUNNING) {
		opal_event_loop(OPAL_EVLOOP_ONCE);
	}
	
	return PROXY_RES_OK;
}

/******************************
 * START OF DISPATCH ROUTINES *
 ******************************/
int
ORTE_Initialize(int trans_id, int nargs, char **args)
{
	int				n;
	int				ret;
	int				pfd[2];
	char *			res;
	char *			universe_name;
	char			buf[BUFSIZ];
	fd_set			fds;
	struct timeval	timeout;
	
	fprintf(stdout, "ORTE_Initialize (%d):\n", trans_id); fflush(stdout);
	
	if (proxy_state != STATE_INIT) {
		sendErrorEvent(trans_id, RTEV_ERROR_ORTE_INIT, "already initialized");
		return 0;
	}
	
	if (nargs < 2) {
		sendErrorEvent(trans_id, RTEV_ERROR_ORTE_INIT, "incorrect arg count");
		return 0;
	}
	
	if (strcmp(args[0], WIRE_PROTOCOL_VERSION) != 0) {
		sendErrorEvent(trans_id, RTEV_ERROR_ORTE_INIT, "wire protocol version \"%s\" not supported", args[0]);
		return 0;
	}
	
	gBaseID = strtol(args[1], NULL, 10);
	
	if (pipe(pfd) < 0)
	{
		sendErrorEvent(trans_id, RTEV_ERROR_ORTE_INIT, "pipe() failed for the orted spawn in ORTESpawnDaemon");
		return 0;
	}
	
	switch(orted_pid = fork()) {
	case -1:
		{
			sendErrorEvent(trans_id, RTEV_ERROR_ORTE_INIT, "fork() failed for the orted spawn in ORTESpawnDaemon");
			return 1;
		}
		break;
	/* child */
	case 0:
		{
			char **orted_args;
			
			proxy_svr_finish(orte_proxy);
			
			asprintf(&res, "%s --universe PTP-ORTE-%d --report-uri %d", DEFAULT_ORTED_ARGS, getpid(), pfd[1]);

			orted_args  = Str2Args(res);
			printf("StartDaemon(orted %s)\n", res); fflush(stdout);
			free(res);
			
			/* spawn the daemon */
			printf("CHILD: Starting execvp now!\n"); fflush(stdout);
			errno = 0;
			close(pfd[0]);

			setsid();
			ret = execvp("orted", orted_args);

			FreeArgs(orted_args);
			
			if (ret != 0) {
				printf("CHILD: error return from execvp, ret = %d, errno = %d\n", ret, errno); fflush(stdout);
				printf("CHILD: PATH = %s\n", getenv("PATH")); fflush(stdout);
				exit(ret);
			}
		}
		break;
    /* parent */
    default:
    	printf("PARENT: orted_pid = %d\n", orted_pid); fflush(stdout);
    	
		/* 
		 * the daemon will report it's URI on the pipe when it's started up 
		 */
		timeout.tv_sec = 15;
		timeout.tv_usec = 0;
		FD_ZERO(&fds);
		FD_SET(pfd[0], &fds);
		
		switch (select(pfd[0]+1, &fds, NULL, NULL, &timeout)) {
		case -1:
			/*
			 * Something serious has gone wrong. Kill the orted and shut down.
			 */
			(void)kill(orted_pid, SIGKILL);
			sendErrorEvent(trans_id, RTEV_ERROR_ORTE_INIT, "select() returned error");
			return 0;
		case 0:
			/*
			 * Timeout. Kill off orted (if it's running) and shut down.
			 */
			(void)kill(orted_pid, SIGKILL);
			sendErrorEvent(trans_id, RTEV_ERROR_ORTE_INIT, "Timeout waiting for orted to start");
			return 0;
		default:
			if ((n = read(pfd[0], buf, BUFSIZ-1)) > 0) {
				buf[n] = '\0';
				printf("PARENT: URI = %s\n", buf); fflush(stdout);
			}
		}

		close(pfd[0]);
		close(pfd[1]);
		break;
	}
	
	asprintf(&universe_name, "PTP-ORTE-%d", orted_pid);
	
	if (do_orte_init(trans_id, universe_name) != 0) {
		free(universe_name);
		return 0;
	}
	
	free(universe_name);
	
#ifdef HAVE_SYS_BPROC_H
	subscribe_bproc();
#endif /* HAVE_SYS_BPROC_H */

	ORTE_QUERY(ORTE_JOBID_INVALID);
	
	printf("Start daemon returning OK.\n");
	
	proxy_state = STATE_RUNNING;
	
	sendOKEvent(trans_id);
	
	return 0;
}

/**
 * Initiate the model definition phase
 */
int
ORTE_ModelDef(int trans_id, int nargs, char **args)
{
	fprintf(stdout, "ORTE_ModelDef (%d):\n", trans_id); fflush(stdout);
	
#ifdef HAVE_SYS_BPROC_H
	sendAttrDefStringEvent(trans_id, NODE_USER_ATTR, "User Name", "User owner of node (BPROC)", "");
	sendAttrDefStringEvent(trans_id, NODE_GROUP_ATTR, "Group Name", "Group owner of node (BPROC)", "");
#endif /* HAVE_SYS_BPROC_H */

	sendOKEvent(trans_id);
	return PROXY_RES_OK;
}

/**
 * Stop polling for LSF change events
 */
 int
ORTE_StopEvents(int trans_id, int nargs, char **args)
{
	fprintf(stdout, "  ORTE_StopEvents (%d):\n", trans_id); fflush(stdout);
	/* notification that start events have completed */
	sendOKEvent(gTransID);
	gTransID = 0;
	sendOKEvent(trans_id);
	return PROXY_RES_OK;	
}

/**
 * Submit a job with the given executable path and arguments (remote call from a client proxy)
 *
 * TODO - what about queues, should there be a LSF_Submit?
 */
int
ORTE_SubmitJob(int trans_id, int nargs, char **args)
{
	int						rc;
	int						i;
	int						a;
	int						num_procs = 1;
	int						debug = false;
	int						num_args = 0;
	int						num_env = 0;
	int						debug_argc = 0;
	int						ptpid = generate_id();
	int						jobsubid = 0;
	char *					full_path;
	char *					name;
	char *					pgm_name = NULL;
	char *					cwd = NULL;
	char *					exec_path = NULL;
	char *					debug_exec_name = NULL;
	char *					debug_exec_path = NULL;
	char *					debug_full_path;
	char **					debug_args;
	char **					env = NULL;
	orte_app_context_t *	apps;
	orte_jobid_t			ortejobid = ORTE_JOBID_MAX;
	orte_jobid_t			debug_jobid = -1;
	ptp_job *				j;

	if (proxy_state != STATE_RUNNING) {
		sendErrorEvent(trans_id, RTEV_ERROR_ORTE_INIT, "must call INIT first");
		return PROXY_RES_OK;
	}
	
	if (nargs < 1) {
		sendErrorEvent(trans_id, RTEV_ERROR_ORTE_INIT, "incorrect arg count");
		return PROXY_RES_OK;
	}
	
	fprintf(stdout, "  ORTE_SubmitJob (%d): %s\n", trans_id, args[0]); fflush(stdout);

	for (i = 0; i < nargs; i++) {
		if (strncmp(args[i], JOB_SUB_ID_ATTR, strlen(JOB_SUB_ID_ATTR)) == 0) {
			jobsubid = (int)strtol(args[i]+strlen(JOB_SUB_ID_ATTR)+1, NULL, 10);
		} else if (strncmp(args[i], JOB_EXEC_NAME_ATTR, strlen(JOB_EXEC_NAME_ATTR)) == 0) {
			pgm_name = args[i]+strlen(JOB_EXEC_NAME_ATTR)+1;
		} else if (strncmp(args[i], JOB_EXEC_PATH_ATTR, strlen(JOB_EXEC_PATH_ATTR)) == 0) {
			exec_path = args[i]+strlen(JOB_EXEC_PATH_ATTR)+1;
		} else if (strncmp(args[i], JOB_NUM_PROCS_ATTR, strlen(JOB_NUM_PROCS_ATTR)) == 0) {
			num_procs = (int)strtol(args[i]+strlen(JOB_NUM_PROCS_ATTR)+1, NULL, 10);
		} else if (strncmp(args[i], JOB_WORKING_DIR_ATTR, strlen(JOB_WORKING_DIR_ATTR)) == 0) {
			cwd = args[i]+strlen(JOB_WORKING_DIR_ATTR)+1;
		} else if (strncmp(args[i], JOB_PROG_ARGS_ATTR, strlen(JOB_PROG_ARGS_ATTR)) == 0) {
			num_args++;
		} else if (strncmp(args[i], JOB_ENV_ATTR, strlen(JOB_ENV_ATTR)) == 0) {
			num_env++;
		} else if (strncmp(args[i], JOB_DEBUG_ARGS_ATTR, strlen(JOB_DEBUG_ARGS_ATTR)) == 0) {
			debug_argc++;
		} else if (strncmp(args[i], JOB_DEBUG_FLAG_ATTR, strlen(JOB_DEBUG_FLAG_ATTR)) == 0) {
			if (strcmp(args[i]+strlen(JOB_DEBUG_FLAG_ATTR)+1, "true") == 0) {
				debug = true;
			}
		}
	}
	
	/*
	 * Do some checking first
	 */
	 
	if (pgm_name == NULL) {
		sendErrorEvent(trans_id, RTEV_ERROR_ORTE_RUN, "Must specify a program name");
		return PROXY_RES_OK;
	}
	
	if (num_procs <= 0) {
		sendErrorEvent(trans_id, RTEV_ERROR_ORTE_RUN, "Invalid number of processes");
		return PROXY_RES_OK;
	}
	
	/*
	 * Must specify a working directory. For local launches, this is normally the project directory. For
	 * remote launches, it will probably be the user's home directory.
	 */
	if (cwd == NULL) {
		sendErrorEvent(trans_id, RTEV_ERROR_ORTE_RUN, "Must specify a working directory");
		return PROXY_RES_OK;
	}
		
	/*
	 * Get supplied environment. It is used to locate executable if necessary.
	 */
	
	if (num_env > 0) {
		env = (char **)malloc((num_env + 1) * sizeof(char *));
		for (i = 0, a = 0; i < nargs; i++) {
			if (strncmp(args[i], JOB_ENV_ATTR, strlen(JOB_ENV_ATTR)) == 0)
				env[a++] = strdup(args[i] + strlen(JOB_ENV_ATTR) + 1);
		}
		env[a] = NULL;
	}
		
	/*
	 * If no path is specified, then try to locate execuable.
	 */		
	if (exec_path == NULL) {
		full_path = opal_path_findv(pgm_name, 0, env, cwd);
		if (full_path == NULL) {
			sendErrorEvent(trans_id, RTEV_ERROR_ORTE_RUN, "Executuable not found");
			return PROXY_RES_OK;
		}
	} else {
		asprintf(&full_path, "%s/%s", exec_path, pgm_name);
	}
	
	if (access(full_path, X_OK) < 0) {
		sendErrorEvent(trans_id, RTEV_ERROR_ORTE_RUN, strerror(errno));
		return PROXY_RES_OK;
	}
	
	if (debug) {		
		debug_argc++;
		debug_args = (char **)malloc((debug_argc+1) * sizeof(char *));
		for (i = 0, a = 1; i < nargs; i++) {
			if (strncmp(args[i], JOB_DEBUG_ARGS_ATTR, strlen(JOB_DEBUG_ARGS_ATTR)) == 0) {
				debug_args[a++] = args[i] + strlen(JOB_DEBUG_ARGS_ATTR) + 1;
			} else if (strncmp(args[i], JOB_DEBUG_EXEC_NAME_ATTR, strlen(JOB_DEBUG_EXEC_NAME_ATTR)) == 0) {
				debug_exec_name = args[i]+strlen(JOB_DEBUG_EXEC_NAME_ATTR)+1;
			} else if (strncmp(args[i], JOB_DEBUG_EXEC_PATH_ATTR, strlen(JOB_DEBUG_EXEC_PATH_ATTR)) == 0) {
				debug_exec_path = args[i]+strlen(JOB_DEBUG_EXEC_PATH_ATTR)+1;
			}
		}
		debug_args[a] = NULL;
		
		/*
		 * If no path is specified, then try to locate execuable.
		 */		
		if (debug_exec_path == NULL) {
			debug_full_path = opal_path_findv(debug_exec_name, 0, env, cwd);
			if (debug_full_path == NULL) {
				sendErrorEvent(trans_id, RTEV_ERROR_ORTE_RUN, "Debugger executuable not found");
				return PROXY_RES_OK;
			}
		} else {
			asprintf(&debug_full_path, "%s/%s", debug_exec_path, debug_exec_name);
		}
		
		if (access(debug_full_path, X_OK) < 0) {
			sendErrorEvent(trans_id, RTEV_ERROR_ORTE_RUN, strerror(errno));
			return PROXY_RES_OK;
		}

		debug_args[0] = strdup(debug_full_path);
	}

	/* format the app_context_t struct */
	apps = OBJ_NEW(orte_app_context_t);
	apps->num_procs = num_procs;
	apps->app = full_path;
	apps->cwd = strdup(cwd);
	/* no special environment variables */
#if ORTE_VERSION_1_0
	apps->num_env = num_env;
#endif /* ORTE_VERSION_1_0 */
	apps->env = env;
	/* no special mapping of processes to nodes */
	apps->num_map = 0;
	apps->map_data = NULL;
	/* setup argv */
	apps->argv = (char **)malloc((num_args + 2) * sizeof(char *));
	apps->argv[0] = strdup(full_path);
	if (num_args > 0) {
		for (i = 0, a = 1; i < nargs; i++) {
			if (strncmp(args[i], JOB_PROG_ARGS_ATTR, strlen(JOB_PROG_ARGS_ATTR)) == 0)
				apps->argv[a++] = strdup(args[i] + strlen(JOB_PROG_ARGS_ATTR) + 1);
		}
	}
	apps->argv[num_args+1] = NULL;
#if ORTE_VERSION_1_0
	apps->argc = num_args + 1;
#endif /* ORTE_VERSION_1_0 */

	printf("%s %d processes of job '%s'\n", debug ? "Debugging" : "Spawning" , (int)apps->num_procs, apps->app);
	printf("\tprogram name '%s'\n", apps->argv[0]);
	fflush(stdout);
	
	/* calls the ORTE spawn function with the app to spawn.  Return the
	 * jobid assigned by the registry/ORTE.  Passes a callback function
	 * that ORTE will call with state change on this job */
	if (debug) {
		rc = debug_spawn(debug_full_path, debug_argc, debug_args, &apps, 1, &ortejobid, &debug_jobid);
		free(debug_args);
	} else {
		rc = ORTE_SPAWN(&apps, 1, &ortejobid, job_state_callback);
	}
	
	printf("SPAWNED [error code %d = '%s'], now unlocking\n", rc, ORTE_ERROR_NAME(rc)); fflush(stdout);
	
	OBJ_RELEASE(apps);
	
	if(ORTECheckErrorCode(trans_id, RTEV_ERROR_ORTE_RUN, rc)) return 1;

	printf("NEW JOB (%d,%d,%d)\n", jobsubid, ptpid, (int)ortejobid); fflush(stdout);

	/*
	 * Send ok for job submission.
	 */	
	sendOKEvent(trans_id);
	
    j = new_job(num_procs, ptpid, ortejobid, debug_jobid);
	
	/*
	 * Send new job event
	 */
	asprintf(&name, ORTE_JOB_NAME_FMT, ortejobid);
	sendNewJobEvent(gTransID, ptpid, name, jobsubid, JOB_STATE_INIT);
	free(name);
	
	/*
	 * Start I/O forwarding. Don't start it before we have
	 * sent the new process events!
	 */
	if (!orte_job_record_started(ortejobid)) {
		orte_job_record_start(ortejobid);
	}

#if ORTE_VERSION_1_0
	subscribe_job(ortejobid);
#else /* ORTE_VERSION_1_0 */
	/*
	 * If this is a debug job then the debugger will manage
	 * process state. However we need to set up the process/node
	 * mapping first. 
	 * 
	 * Also, we must create the process model elements before any
	 * process change events are generated as a result of processes
	 * writing to stdout.
	 */
	get_proc_info(j);
#endif /* ORTE_VERSION_1_0 */
	
	return PROXY_RES_OK;
}

/* 
 * terminate a job, given a jobid 
 */
int
ORTE_TerminateJob(int trans_id, int nargs, char **args)
{
	int			rc;
	int			jobid;
	ptp_job *	j;
	
	if (proxy_state != STATE_RUNNING) {
		sendErrorEvent(trans_id, RTEV_ERROR_TERMINATE_JOB, "must call INIT first");
		return PROXY_RES_OK;
	}
	
	if (nargs < 1) {
		sendErrorEvent(trans_id, RTEV_ERROR_TERMINATE_JOB, "incorrect arg count");
		return PROXY_RES_OK;
	}
	
	jobid = (int)strtol(args[0], NULL, 10);

	if ((j = find_job(jobid, JOBID_PTP)) != NULL) {
		if (j->terminating) {
			sendErrorEvent(trans_id, RTEV_ERROR_TERMINATE_JOB, "Job termination already requested");
			return PROXY_RES_OK;
		}
		
		j->terminating = true;
		
		if (j->debug_jobid < 0)
			rc = ORTE_TERMINATE_JOB(j->orte_jobid);
		else
			rc = ORTE_TERMINATE_JOB(j->debug_jobid);
		
		if(ORTECheckErrorCode(trans_id, RTEV_ERROR_TERMINATE_JOB, rc)) return 1;
	}
	
	return PROXY_RES_OK;
}

/*
 * Enables sending of events. The first thing that must be sent is a
 * description of the model. This comprises new model element events
 * for each element in the model. Once the model description has been
 * sent, model change events will be sent as detected.
 * 
 */
 int
ORTE_StartEvents(int trans_id, int nargs, char **args)
{
	int 			num_machines;
	int				m;
	ptp_machine *	mach;
	ptp_node *		node;
	int				nodeid;
	int				num_nodes;
	
	fprintf(stdout, "  ORTE_StartEvents (%d):\n", trans_id); fflush(stdout);

	if (proxy_state != STATE_RUNNING) {
		sendErrorEvent(trans_id, RTEV_ERROR_ORTE_INIT, "must call INIT first");
		return PROXY_RES_OK;
	}

	gTransID = trans_id;
	
	num_machines = get_num_machines();
	
	for(m = 0; m < num_machines; m++) {
		mach = new_machine();
		sendNewMachineEvent(trans_id, mach->id, get_machine_name(m));
		
		num_nodes = get_num_nodes(mach->id);
		
		/* 
		 * if we know of no nodes, then just use the local machine
		 */
		if(num_nodes == 0) {
			char hostname[256];
			gid_t gid;
			struct group *grp;
			struct passwd *pwd;
			char *state;
		
			pwd = getpwuid(geteuid());
			gid = getgid();
			grp = getgrgid(gid);
			gethostname(hostname, 256);
       		asprintf(&state, "%s", NODE_STATE_UP);
       		
        	node = new_node(mach, hostname, state, pwd->pw_name, grp->gr_name, NULL);
			
			free(state);

			sendNewNodeEvent(trans_id, mach->id, mach);
		} else {
			ptp_node *	first_node;
			ptp_node *	last_node;
		
			/* nodeid = -1 means we want the attributes for ALL nodes */
			nodeid = -1;
	
			if( get_node_attributes(mach, &first_node, &last_node) ) {
				/* error - so bail out */
				sendErrorEvent(trans_id, RTEV_ERROR_NATTR, "error finding key on node or error getting keys");
				return PROXY_RES_OK;
			}
	
			sendNewNodeEvent(trans_id, mach->id, mach);
		}
	}
	
	/*
	 * Send default queue
	 */
	sendNewQueueEvent(trans_id);
	
	return PROXY_RES_OK;
}

int
ORTE_Quit(int trans_id, int nargs, char **args)
{
	printf("ORTE_Quit called!\n"); fflush(stdout);
	
	if (proxy_state == STATE_RUNNING) {
		do_orte_shutdown();
	}
	
	proxy_state = STATE_SHUTTING_DOWN;
	
	sendOKEvent(trans_id);
	
	return PROXY_RES_OK;
}

int
server(char *name, char *host, int port)
{
	char *			msg;
	char *			msg1;
	char *			msg2;
	int				rc;
	int				shutdown_orted = 1;
	struct timeval	timeout = {0, 20000};
	
	gJobList = NewList();
	gMachineList = NewList();
	
	if (proxy_svr_init(name, &timeout, &helper_funcs, &command_tab, &orte_proxy) != PROXY_RES_OK)
		return 0;
	
	proxy_svr_connect(orte_proxy, host, port);
	printf("proxy_svr_connect returned.\n");
	
	while (ptp_signal_exit == 0 && proxy_state != STATE_SHUTDOWN) {
		if (proxy_state == STATE_SHUTTING_DOWN) {
			proxy_state = STATE_SHUTDOWN;
		}
		if  ((do_orte_progress() != PROXY_RES_OK) ||
			(proxy_svr_progress(orte_proxy) != PROXY_RES_OK))
			break;
	}
	
	if (ptp_signal_exit != 0) {
		switch(ptp_signal_exit) {
			case SIGINT:
				asprintf(&msg1, "INT");
				asprintf(&msg2, "Interrupt");
				break;
			case SIGHUP:
				asprintf(&msg1, "HUP");
				asprintf(&msg2, "Hangup");
				break;
			case SIGILL:
				asprintf(&msg1, "ILL");
				asprintf(&msg2, "Illegal Instruction");
				break;
			case SIGSEGV:
				asprintf(&msg1, "SEGV");
				asprintf(&msg2, "Segmentation Violation");
				break;
			case SIGTERM:
				asprintf(&msg1, "TERM");
				asprintf(&msg2, "Termination");
				break;
			case SIGQUIT:
				asprintf(&msg1, "QUIT");
				asprintf(&msg2, "Quit");
				break;
			case SIGABRT:
				asprintf(&msg1, "ABRT");
				asprintf(&msg2, "Process Aborted");
				break;
			case SIGCHLD:
				asprintf(&msg1, "CHLD");
				asprintf(&msg2, "ORTED Process Exited");
				shutdown_orted = 0;
				break;
			default:
				asprintf(&msg1, "***UNKNOWN SIGNAL***");
				asprintf(&msg2, "ERROR - UNKNOWN SIGNAL, REPORT THIS!");
				break;
		}
		printf("###### SIGNAL: %s\n", msg1);
		if (shutdown_orted) {
			printf("###### Shutting down ORTEd\n");
			do_orte_shutdown();
		}
		asprintf(&msg, "ptp_orte_proxy received signal %s (%s).  Exit was required and performed cleanly.", msg1, msg2);
		//proxy_svr_event_callback(orte_proxy, ORTEErrorStr(RTEV_ERROR_SIGNAL, msg));
		sendErrorEvent(gTransID, RTEV_ERROR_SIGNAL, msg);
		free(msg);
		free(msg1);
		free(msg2);
		/* our return code = the signal that fired */
		rc = ptp_signal_exit;
	}
	
	proxy_svr_finish(orte_proxy);
	printf("proxy_svr_finish returned.\n");
	
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
	int				port = PROXY_TCP_PORT;
	char *			host = "localhost";
	char *			proxy_str = DEFAULT_PROXY;
	int				rc;
	
	while ((ch = getopt_long(argc, argv, "P:p:h:", longopts, NULL)) != -1)
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
		fprintf(stderr, "%s [--proxy=proxy] [--host=host_name] [--port=port]\n", argv[0]);
		return 1;
	}
	
	/* 
	 * signal can happen any time after handlers are installed, so
	 * make sure we catch it
	 */
	ptp_signal_exit = 0;
	
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
		printf("  ---> SIGNAL SIGINT was previously already defined.  Shadowing.\n"); fflush(stdout);
	}
	if(saved_signals[SIGHUP] != SIG_ERR && saved_signals[SIGHUP] != SIG_IGN && saved_signals[SIGHUP] != SIG_DFL) {
		printf("  ---> SIGNAL SIGHUP was previously already defined.  Shadowing.\n"); fflush(stdout);
	}
	if(saved_signals[SIGILL] != SIG_ERR && saved_signals[SIGILL] != SIG_IGN && saved_signals[SIGILL] != SIG_DFL) {
		printf("  ---> SIGNAL SIGILL was previously already defined.  Shadowing.\n"); fflush(stdout);
	}
	if(saved_signals[SIGSEGV] != SIG_ERR && saved_signals[SIGSEGV] != SIG_IGN && saved_signals[SIGSEGV] != SIG_DFL) {
		printf("  ---> SIGNAL SIGSEGV was previously already defined.  Shadowing.\n"); fflush(stdout);
	}	
	if(saved_signals[SIGTERM] != SIG_ERR && saved_signals[SIGTERM] != SIG_IGN && saved_signals[SIGTERM] != SIG_DFL) {
		printf("  ---> SIGNAL SIGTERM was previously already defined.  Shadowing.\n"); fflush(stdout);
	}
	if(saved_signals[SIGQUIT] != SIG_ERR && saved_signals[SIGQUIT] != SIG_IGN && saved_signals[SIGQUIT] != SIG_DFL) {
		printf("  ---> SIGNAL SIGQUIT was previously already defined.  Shadowing.\n"); fflush(stdout);
	}
	if(saved_signals[SIGABRT] != SIG_ERR && saved_signals[SIGABRT] != SIG_IGN && saved_signals[SIGABRT] != SIG_DFL) {
		printf("  ---> SIGNAL SIGABRT was previously already defined.  Shadowing.\n"); fflush(stdout);
	}	
	if(saved_signals[SIGCHLD] != SIG_ERR && saved_signals[SIGABRT] != SIG_IGN && saved_signals[SIGCHLD] != SIG_DFL) {
		printf("  ---> SIGNAL SIGABRT was previously already defined.  Shadowing.\n"); fflush(stdout);
	}	
	rc = server(proxy_str, host, port);
	
	return rc;
}
