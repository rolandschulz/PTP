/******************************************************************************
 * Copyright (c) 2005 The Regents of the University of California. 
 * This material was produced under U.S. Government contract W-7405-ENG-36 
 * for Los Alamos National Laboratory, which is operated by the University 
 * of California for the U.S. Department of Energy. The U.S. Government has 
 * rights to use, reproduce, and distribute this software. NEITHER THE 
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
 * 
 * Copyright (c) 2005, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/

/*
 * Questions/problems
 * 22) Should probably serialize around process and node object generation to
 *	   simplify generation of node and process state change messages affecting
 *	   multiple nodes or processes with consecutive object id ranges
 */

#include <pthread.h>
#include "config.h"
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <sys/time.h>
#include <sys/resource.h>
#include <sys/wait.h>
#include <unistd.h>
#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <strings.h>
#include <errno.h>
#include <pwd.h>
#include <grp.h>
#include <dirent.h>
#include <proxy_cmd.h>
#include <proxy.h>
#include <proxy_tcp.h>
#include <proxy_event.h>
#include <proxy_cmd.h>
#include <proxy_msg.h>
#include <proxy_attr.h>
#include <handler.h>
#include <signal.h>
#include <list.h>
#include <hash.h>
#include <limits.h>
#ifdef __linux__
#include <getopt.h>
#endif
#ifdef _AIX
#include <procinfo.h>
#endif

#define DEFAULT_PROXY "tcp"
#define DEFAULT_QUEUE_NAME "default"
#define SKIPPING_SPACES			1
#define PARSING_UNQUOTED_ARG	2
#define PARSING_QUOTED_ARG		3
#define SKIPPING_CHARS			4
#define READ_BUFFER_SIZE 1024
#define STDIO_WRITE_BUFSIZE 1024
#define POE "/usr/bin/poe"
#define INFO_MESSAGE 0
#define TRACE_MESSAGE 1
#define TRACE_DETAIL_MESSAGE 2
#define WARNING_MESSAGE 3
#define ERROR_MESSAGE 4
#define FATAL_MESSAGE 5
#define ARGS_MESSAGE 6

typedef struct jobinfo *jobinfoptr;	/* Forward reference to jobinfo         */

typedef struct {
    char *read_buf;		/* Read buffer for stdout/stderr        */
    char *write_buf;		/* Write buffer for stdout/stderr       */
    char *cp;			/* Current character in write buffer    */
    int allocated;		/* Allocated size of write buffer       */
    int remaining;		/* Bytes left in write buffer           */
    void (*write_func) (jobinfoptr job, char *buf);	/* Function writing data */
} ioinfo;

typedef struct {
    int proxy_taskid;		/* Process id assigned by proxy         */
    pid_t parent_pid;		/* PID for parent (poe) process         */
    pid_t task_pid;		/* PID for this process                 */
    char *ipaddr;		/* IP address of node where task is     */
    char *hostname;		/* Hostname of node where task is       */
} taskinfo;

typedef struct {
    int proxy_jobid;		/* Job id assigned by proxy             */
    char *submit_jobid;		/* Jobid used when submitted by GUI     */
    pid_t poe_pid;		/* Process id for main poe process      */
    pid_t task0_pid;		/* Process id for app. task 0           */
    int stdin_fd;		/* STDIN pipe/file descriptor           */
    int stdout_fd;		/* STDOUT pipe/file descriptor          */
    int stderr_fd;		/* STDERR pipe/file descriptor          */
    ioinfo stdout_info;		/* Stdout file buffer info              */
    ioinfo stderr_info;		/* Stderr file buffer info              */
    int numtasks;		/* Number of tasks in application       */
    taskinfo *tasks;		/* Tasks in this application            */
    pthread_t startup_thread;	/* Startup monitor thread for app       */
    time_t submit_time;		/* Time job was submitted               */
    int label_io:1;		/* User set MP_LABELIO                  */
    int split_io:1;		/* STDOUT is split by task              */
    int stdin_redirect:1;	/* Stdin redirected to file             */
    int stdout_redirect:1;	/* Stdout redirected to file            */
    int stderr_redirect:1;	/* Stderr redirected to file            */
    int discovered_job:1;	/* Job already running at PTP startup   */
} jobinfo;

typedef struct NODE_REFCOUNT {
    struct NODE_REFCOUNT *next;	/* -> Next node in hash chain           */
    char *key;			/* Hash key for this structure          */
    int proxy_nodeid;		/* Proxy assigned node id for node      */
    int node_number;		/* This node's node number              */
    int task_count;		/* Number of tasks running on node      */
} node_refcount;

typedef struct {
    char *id;			/* Attribute identifier                 */
    char *short_name;		/* Description used as label in GUI     */
    char *long_name;		/* Text used for tooltip text in GUI    */
    char *default_value;	/* Attribute's default value            */
} string_launch_attr;

typedef struct {
    char *id;			/* Attribute identifier                 */
    char *short_name;		/* Description used as label in GUI */
    char *long_name;		/* Text used for tooltip text in GUI */
    char *default_value;	/* Attribute's default value            */
    char *enums;		/* Enumeration values ',' delimited     */
} enum_launch_attr;

typedef struct {
    char *id;			/* Attribute identifier                 */
    char *short_name;		/* Description used as label in GUI */
    char *long_name;		/* Text used for tooltip text in GUI */
    int default_value;		/* Attribute's default value            */
    int llimit;			/* Attribute's lower limit              */
    int ulimit;			/* Attribute's upper limit              */
} int_launch_attr;

typedef struct {
    char *id;			/* Attribute identifier                 */
    char *short_name;		/* Description used as label in GUI */
    char *long_name;		/* Text used for tooltip text in GUI */
    long long default_value;	/* Attribute's default value            */
    long long llimit;		/* Attribute's lower limit              */
    long long ulimit;		/* Attribute's upper limit              */
} long_int_launch_attr;

static RETSIGTYPE ptp_signal_handler(int sig);
static int server(char *name, char *host, int port);
static int start_daemon(int trans_id, int nargs, char *args[]);
static int define_model(int trans_id, int nargs, char *args[]);
static int run(int trans_id, int nargs, char *args[]);
static int terminate_job(int trans_id, int nargs, char *args[]);
static int quit(int trans_id, int nargs, char *args[]);
static int shutdown(void);
static int start_events(int trans_id, int nargs, char *args[]);
static int halt_events(int trans_id, int nargs, char *args[]);
static void post_error(int trans_id, int type, char *msg);
static char
**create_exec_parmlist(char *execname, char *targetname, char *args);
static char **create_env_array(char *args[], int split_io, char *mp_buffer_mem,
			       char *mp_rdma_count);
static int stdout_handler(int fd, void *job);
static int stderr_handler(int fd, void *job);
static void check_bufsize(ioinfo * file_info);
static void send_stdout(jobinfo * job, char *buf);
static void send_stderr(jobinfo * job, char *buf);
static int write_output(int fd, jobinfo * job, ioinfo * file_info);
static void *pmd_monitor(void *args);
static void update_node_refcounts(int numtasks, taskinfo * tasks);
static void delete_noderef(char *hostname);
static void *startup_monitor(void *pid);
static void delete_task_list(int numtasks, taskinfo * tasks);
static void *kill_process(void *pid);
static void update_nodes(int trans_id, FILE * hostlist);
static void malloc_check(void *p, const char *function, int line);
static node_refcount *add_node(char *key);
static node_refcount *find_node(char *key);
static void hash_cleanup(void *hash_list);
static void send_ok_event(int trans_id);
static void discover_jobs(void);
static void add_discovered_job(char *pid);
static void redirect_io(void);
static proxy_msg *proxy_attr_def_enum_event(int trans_id, char *id, char *name,
					    char *desc, int display, char *def,
					    int count);
static proxy_msg *proxy_attr_def_int_limits_event(int trans_id, char *id,
						  char *name, char *desc,
						  int display, int def,
						  int llimit, int ulimit);
static proxy_msg *proxy_attr_def_long_int_limits_event(int trans_id, char *id,
						       char *name, char *desc,
						       int display,
						       long long def,
						       long long llimit,
						       long long ulimit);
static void send_string_attrs(int trans_id);
static void send_int_attrs(int trans_id);
static void send_long_int_attrs(int trans_id);
static void send_enum_attrs(int trans_id);
static void send_local_default_attrs(int trans_id);
static void send_new_node_list(int trans_id, int machine_id, List * new_nodes);
static void send_job_state_change_event(int trans_id, int jobid, char *state);
static void send_process_state_change_event(int trans_id, jobinfo * job,
					    char *state);
static void send_process_state_output_event(int trans_id, int procid,
					    char *output);
static int generate_id(void);
static void enqueue_event(proxy_msg * event);
static void print_message(int type, const char *format, ...);
static void print_message_args(int argc, char *optional_args[]);
int main(int argc, char *argv[]);

extern char **environ;
static int events_enabled = 0;
static int shutdown_requested;
static int debug_level = 0; /* 0 is off */
static int ptp_signal_exit;
static List *jobs;		/* Jobs run by this proxy                   */
static Hash *nodes;		/* Nodes currently in use                   */
static int node_count;		/* Number of active nodes                   */
static int global_node_index;	/* Sequentially assigned node number        */
static RETSIGTYPE(*saved_signals[NSIG]) (int);
static proxy_svr *pe_proxy;	/* Handle for proxy message link            */
static pthread_attr_t thread_attrs;	/* Thread creation attributes       */
static pthread_t termination_thread;	/* Thread to monitor process exit   */
static int base_id;		/* Base id for proxy objects                */
static int last_id = 1;		/* Last assigned object id                  */
static int queue_id;		/* Object id for our queue                  */
static int machine_id;		/* Object id for our machine                */
static int start_events_transid;	/* start_events command id          */
static char emsg_buffer[256];	/* Buffer for building error msg            */
static pthread_mutex_t print_message_lock = PTHREAD_MUTEX_INITIALIZER;

/*
 * List functions are safe for adding or removing list elements since they
 * have appropriate locks for updating the list. However, since the list
 * current location pointer is part of the list object, any time the list
 * is traversed, starting with a SetList call, the list must be locked
 * since a SetList call for the same list on a different thread can 
 * invalidate the list positioning for the first thread.
 */
static pthread_mutex_t job_lock = PTHREAD_MUTEX_INITIALIZER;
static pthread_mutex_t node_lock = PTHREAD_MUTEX_INITIALIZER;
static int state_trace = 1;
static int state_trace_detail = 1;
static int state_info = 1;
static int state_warning = 1;
static int state_error = 1;
static int state_args = 1;
static int state_message_timedate = 1;
static int state_message_threadid = 1;
static pthread_t thread_map_table[256];

#ifdef __linux__
static struct option longopts[] = { {"proxy", required_argument, NULL, 'P'},
{"port", required_argument, NULL, 'p'}, {"host", required_argument,
					 NULL, 'h'}, {"debug", required_argument, NULL, 'd'}, {NULL, 0, NULL, 0}
};
#endif

static proxy_svr_helper_funcs helper_funcs = { NULL, NULL };

/*
 * Proxy infrastructure expects commands in exactly this sequence.
 * Be careful when adding or deleting commands
 */
static proxy_cmd cmds[] = { quit, start_daemon, define_model, start_events,
    halt_events, run, terminate_job
};
static proxy_commands
    command_tab = { 0, sizeof cmds / sizeof(proxy_cmd), cmds };

static char *mp_infolevel_labels[] = { "Error", "Warning", "Informational",
    "Diagnostic", "Diagnostic level 4", "Diagnostic level 5",
    "Diagnostic level 6"
};

#ifdef __linux__

/*
 * This table defines the launch attributes corresponding to PE environment
 * variables for the stand-alone PE case for Linux.
 */
static string_launch_attr string_launch_attrs[] = {
    /*
     * Attributes needed in both basic and advanced mode
     */
    {"PE_STDIN_PATH", "Stdin Path",
     "Specify path for stdin input file", ""},
    {"PE_STDOUT_PATH", "Stdout Path",
     "Specify path for stdout output file", ""},
    {"PE_STDERR_PATH", "Stderr Path",
     "Specify path for stderr output file", ""},
    /*
     * I/O Related attributes
     */
    {"MP_IONODEFILE", "MPI I/O Node List",
     "Specify file listing nodes performing parallel I/O (MP_IONODEFILE)",
     ""},
    /*
     * Diagnostic related attributes
     */
    /* LoadLeveler only - what about MP_RMLIB?
       {
       "MP_STATISTICS", "MPI Statistics",
       "Specify if statistics obtained for user space job (MP_STATISTICS)",
       "no"
       },
     */
    /*
     * Debug related attributes
     */
    {"MP_COREDIR", "Corefile Directory",
     "Specify directory for application's core files (MP_COREDIR)",
     ""},
    {"MP_DEBUG_INITIAL_STOP", "Initial Breakpoint",
     "Initial breakpoint when debugging an application (MP_DEBUG_INITIAL_STOP)",
     ""},
    {"MP_PROFDIR", "GMON Directory",
     "Directory containing GMON profiling data files (GMON_PROFDIR)",
     ""},
    /*
     * System resource related  attributes
     */
    /*
     * Node allocation related attributes
     */
    {"MP_CMDFILE", "Command File",
     "Specify script to load nodes in partition (MP_CMDFILE)",
     ""},
    {"MP_HOSTFILE", "Host List File",
     "Specify name of host list file for node allocation (MP_HOSTFILE)",
     ""},
    /*
     * Performance related attributes
     */
    /*
     * Miscellaneous attributes
     */
    {"MP_EUILIBPATH", "Library Path",
     "Specify path to message passing and communications libraries (MP_EUILIBPATH)",
     ""},
    /*
     * Alternate resource manager attributes
     */
    {"MP_RMLIB", "Resource Manager Library",
     "Specify alternate resource manager library", ""},
    /*
     * Other attributes
     */
    {"PE_ENV_SCRIPT", "Environment Setup Script",
     "Specify if using basic or advanced mode", ""},
    {"PE_ADVANCED_MODE", "Advanced Mode",
     "Specify name of PE environment variable setup script",
     "no"}
};

static enum_launch_attr enum_attrs[] = {
    /*
     * I/O tab related attributes
     */
    {"MP_DEVTYPE", "Device Type Class",
     "Specify that Infiniband interconnect is to be used (MP_DEVTYPE)",
     "", "|ib"},
    {"MP_LABELIO", "Label I/O",
     "Specify if application output is labeled by task id (MP_LABELIO)",
     "no", "yes|no"},
    {"MP_IO_ERRLOG", "Create I/O Error Log",
     "Specify if I/O error logging is enabled (MP_IO_ERRLOG)",
     "no", "yes|no"},
    {"PE_SPLIT_STDOUT", "Split STDOUT by Task",
     "Specify if stdio output is split by task", "no",
     "yes|no"},
    {"MP_STDINMODE", "STDIN Mode",
     "Specify how application's stdin is managed (MP_STDINMODE)",
     "all", "all|none"},
    {"MP_STDOUTMODE", "STDOUT Mode",
     "Specify how application's stdio output is handled (MP_STDOUTMODE)",
     "unordered", "ordered|unordered"},
    /*
     * Diagnostic tab related attributes
     */
    {"MP_PMDLOG", "Create PMD Log",
     "Specify if PE diagnostic messages are logged", "no",
     "yes|no"},
    {"MP_PRINTENV", "Print Environment",
     "Specify if PE environment variables are printed (MP_PRINTENV)",
     "no", "yes|no"},
    {"MP_INFOLEVEL", "Message Level",
     "Specify level of PE message reporting (MP_INFOLEVEL)",
     "Warning",
     "Error|Warning|Informational|Diagnostic|Diagnostic level 4|Diagnostic level 5|Diagnostic level 6"},
    {"MP_LAPI_TRACE_LEVEL", "LAPI Trace Level",
     "Specify level of LAPI trace (MP_LAPI_TRACE_LEVEL)",
     "0", "0|1|2|3|4|5"},
    /*
     * Debug tab related attributes
     */
    {"MP_DEBUG_NOTIMEOUT", "Suppress Timeout",
     "Specify if debugger can attach without causing application timeout (MP_DEBUG_NOTIMEOUT)",
     "no", "yes|no"},
    {"MP_COREFILE_SIGTERM", "Corefile on SIGTERM",
     "Specify if corefile generated on SIGTERM (MP_COREFILE_SIGTERM)",
     "no", "yes|no"},
    {"MP_EUIDEVELOP", "MPI Parameter Checking",
     "Specify level of MPI parameter checking (MP_EUIDEVELOP)",
     "no", "yes|no|debug|minimum"},
    /*
     * System tab related attributes
     */
    {"MP_ADAPTER_USE", "Exclusive Adapter Use",
     "Specify how node's adapter should be used (MP_ADAPTER_USE)",
     "shared", "dedicated|shared"},
    {"MP_CPU_USE", "Exclusive CPU Use",
     "Specify how node's CPU should be used (MP_CPU_USE)",
     "multiple", "multiple|unique"},
//    { LoadLeveler only
//     "MP_EUIDEVICE", "Adapter",
//     "Specify adapter to use for message passing (MP_EUIDEVICE)",
//     "", "en0|fi0|sn_all|sn_single"
//    },
    /*
     * Node tab related attributes
     */
    {"MP_PGMMODEL", "Programming Model",
     "Specify programming model (MP_PGMMODEL)", "spmd",
     "spmd|mpmd"},
    /*
     * Performance tab related attributes
     */
    {"MP_CC_SCRATCH_BUF", "Fastest Collectives",
     "Specify if fastest collective algorithm is used (MP_CC_SCRATCH_BUF)",
     "yes", "yes|no"},
    {"MP_CSS_INTERRUPT", "Packets Generate Interrupts",
     "Specify if arriving packets generate interrupts (MP_CSS_INTERRUPT)",
     "no", "yes|no"},
    {"MP_SHARED_MEMORY", "Use Shared Memory",
     "Specify is shared memory used for communication (MP_SHARED_MEMORY)",
     "yes", "yes|no"},
    {"MP_SINGLE_THREAD", "Single MPI Thread",
     "Specify if application has single thread with MPI calls (MP_SINGLE_THREAD)",
     "no", "yes|no"},
    {"MP_WAIT_MODE", "Wait Mode",
     "Specify thread behavior when waiting for messages (MP_WAIT_MODE)",
     "poll", "nopoll|poll|sleep|yield"},
    /*
     * Miscellaneous tab related attributes
     */
    {"MP_HINTS_FILTERED", "Hints Filtered",
     "Specify if MPI info objects reject hints (MP_HINTS_FILTERED)",
     "yes", "yes|no"},
    {"MP_CLOCK_SOURCE", "Clock Source",
     "Specify if high performance switch clock is time source (MP_CLOCK_SOURCE)",
     "", "|OS"},
    {"MP_MSG_API", "Message Passing API",
     "Specify message passing API used by application (MP_MSG_API)",
     "MPI", "MPI|LAPI|MPI_LAPI|MPI,LAPI"},
};

int_launch_attr int_attrs[] = {
    /*
     * I/O tab related attributes
     */
    {"MP_IO_BUFFER_SIZE", "MPI I/O Buffer Size",
     "Specify default buffer size for MPI-IO (MPI_IO_BUFFER_SIZE)",
     8192, 1, 0x8000000},
    /*
     * Node tab related attributes
     */
    {"MP_PROCS", "Number of Tasks",
     "Specify number of program tasks (MP_PROCS)", 1, 1, INT_MAX},
    /*
     * Performance tab related attributes
     */
    {"MP_ACK_THRESH",
     "Acknowledgment Threshold",
     "Specify packet acknowledgment threshhold (MP_ACK_THRESH)",
     30, 1, 31},
    {"MP_EAGER_LIMIT", "Eager Limit",
     "Specify rondezvous protocol message size threshhold (MP_EAGER_LIMIT)",
     0x10000, 1, INT_MAX},
    {"MP_MSG_ENVELOPE_BUF", "Envelope Buffer Size",
     "Specify size of message envelope buffer (MP_MSG_ENVELOPE_BUF)",
     0x800000, 1000001, INT_MAX},
    {"MP_POLLING_INTERVAL", "MPI Polling Interval",
     "Specify PE polling interval (MP_POLLING_INTERVAL)",
     400000, 1, INT_MAX},
    {"PE_RDMA_COUNT", "Number of rCtx Blocks",
     "Specify number of rCxt blocks (MP_RDMA_COUNT)",
     0, 0, INT_MAX},
    {"PE_RDMA_COUNT_2", "Number of rCtx Blocks",
     "Specify number of rCxt blocks (MP_RDMA_COUNT)", 0, 0, INT_MAX},
    {"MP_RETRANSMIT_INTERVAL", "MPI Retransmit Interval",
     "Specify interval to check if retransmit neeeded (MP_RETRANSMIT_INTERVAL)",
     10000, 1000, INT_MAX},
    {"MP_REXMIT_BUF_CNT",
     "Retransmit Buffers",
     "Specify number of retransmit buffers per task (MP_REXMIT_BUF_CNT)",
     128, 1, INT_MAX},
    {"MP_REXMIT_BUF_SIZE", "Maximum LAPI Buffered Message",
     "Specify maximum LAPI buffer size (MP_REXMIT_BUF_SIZE)",
     65568, 1, INT_MAX},
    {"MP_UDP_PACKET_SIZE", "UDP Packet Size",
     "Specify UDP packet size (MP_UDP_PACKET_SIZE)",
     1, 1, INT_MAX},
    {"PE_BUFFER_MEM", "Buffer Memory",
     "Specify size of preallocated early arrival buffer (MP_BUFFER_MEM)",
     0x4000000, 0, INT_MAX},
    /*
     * Miscellaneous tab related attributes
     */
    {"MP_THREAD_STACKSIZE",
     "Additional MPI Thread Stack Size",
     "Specify additional stack size for MPI service thread (MP_THREAD_STACKSIZE)",
     0, 0, INT_MAX},
    {"MP_PULSE", "Pulse Interval",
     "Specify interval PE checks remote modes (MP_PULSE)",
     600, 0, INT_MAX},
    {"MP_TIMEOUT", "Connection Timeout",
     "Specify timeout limit for connecting to remote nodes (MP_TIMEOUT)",
     150, 1, INT_MAX},
    /*
     * Additional integer attributes used only for validation of attribute
     * field contents. These are used in cases where a single integer, 
     * string or enumerated attribute is insufficient for validating 
     * an attribute field, for instance where an allowable value is an 
     * enumeration or integer value, or where a field contains
     * multiple sub-fields, such as mmm,nnn. For these attributes, 
     * short name, long name and default value are irrelevant.
     */
    {"MP_INSTANCES_INT", "???", "???", 0, 0, INT_MAX},
//    {"MP_RETRY_INT", "???", "???", 0, 1, INT_MAX}, Load Leveler only
};

long_int_launch_attr long_int_attrs[] = {
    {
     "PE_BUFFER_MEM_MAX", "???",
     "Specify maximum size of early arrival buffer (MP_BUFFER_MEM)",
     0, 0, 0x7fffffffffffffffLL}
};
#endif
#ifdef _AIX

/*
 * This table defines the launch attributes corresponding to PE environment
 * variables for the stand-alone PE case for AIX.
 */
static string_launch_attr string_launch_attrs[] = {
    /*
     * Attributes needed in both basic and advanced mode
     */
    {"PE_STDIN_PATH", "Stdin Path",
     "Specify path for stdin input file", ""},
    {"PE_STDOUT_PATH", "Stdout Path",
     "Specify path for stdout output file", ""},
    {"PE_STDERR_PATH", "Stderr Path",
     "Specify path for stderr output file", ""},
    /*
     * I/O Related attributes
     */
    {"MP_IONODEFILE", "MPI I/O Node List",
     "Specify file listing nodes performing parallel I/O (MP_IONODEFILE)",
     ""},
    /*
     * Diagnostic related attributes
     */
    /* LoadLeveler only - what about MP_RMLIB?
       {
       "MP_STATISTICS", "MPI Statistics",
       "Specify if statistics obtained for user space job (MP_STATISTICS)",
       "no"
       },
     */
    /*
     * Debug related attributes
     */
    {"MP_COREDIR", "Corefile Directory",
     "Specify directory for application's core files (MP_COREDIR)",
     ""},
    {"MP_DEBUG_INITIAL_STOP", "Initial Breakpoint",
     "Initial breakpoint when debugging an application (MP_DEBUG_INITIAL_STOP)",
     ""},
    /*
     * System resource related  attributes
     */
    /*
     * Node allocation related attributes
     */
    {"MP_CMDFILE", "Command File",
     "Specify script to load nodes in partition (MP_CMDFILE)",
     ""},
    {"MP_HOSTFILE", "Host List File",
     "Specify name of host list file for node allocation (MP_HOSTFILE)",
     ""},
    {"MP_REMOTEDIR", "Remote Directory Command",
     "Specify name of script echoing current directory (MP_REMOTEDIR)",
     ""},
    /*
     * Performance related attributes
     */
    {"MP_PRIORITY", "Priority Class",
     "Specify dispatch priority class (MP_PRIORITY)", ""},
    /*
     * Miscellaneous attributes
     */
    {"MP_EUILIBPATH", "Library Path",
     "Specify path to message passing and communications libraries (MP_EUILIBPATH)",
     ""},
    {"MP_PRIORITY_LOG_DIR", "Priority Log Directory",
     "Specify directory containing co-scheduler log files (MP_PRIORITY_LOG_DIR)",
     ""},
    {"MP_PRIORITY_LOG_NAME", "Priority Log Name",
     "Specify filename for co-scheduler log files (MP_PRIORITY_LOG_NAME)",
     ""},
    /*
     * Alternate resource manager attributes
     */
    {"MP_RMLIB", "Resource Manager Library",
     "Specify alternate resource manager library", ""},
    /*
     * Other attributes
     */
    {"PE_ENV_SCRIPT", "Environment Setup Script",
     "Specify if using basic or advanced mode", ""},
    {"PE_ADVANCED_MODE", "Advanced Mode",
     "Specify name of PE environment variable setup script",
     "no"}
};

static enum_launch_attr enum_attrs[] = {
    /*
     * I/O tab related attributes
     */
    {"MP_DEVTYPE", "Device Type Class",
     "Specify that Infiniband interconnect is to be used (MP_DEVTYPE)",
     "", "|ib"},
    {"MP_LABELIO", "Label I/O",
     "Specify if application output is labeled by task id (MP_LABELIO)",
     "no", "yes|no"},
    {"MP_IO_ERRLOG", "Create I/O Error Log",
     "Specify if I/O error logging is enabled (MP_IO_ERRLOG)",
     "no", "yes|no"},
    {"PE_SPLIT_STDOUT", "Split STDOUT by Task",
     "Specify if stdio output is split by task", "no",
     "yes|no"},
    {"MP_STDINMODE", "STDIN Mode",
     "Specify how application's stdin is managed (MP_STDINMODE)",
     "all", "all|none"},
    {"MP_STDOUTMODE", "STDOUT Mode",
     "Specify how application's stdio output is handled (MP_STDOUTMODE)",
     "unordered", "ordered|unordered"},
    /*
     * Diagnostic tab related attributes
     */
    {"MP_PMDLOG", "Create PMD Log",
     "Specify if PE diagnostic messages are logged", "no",
     "yes|no"},
    {"MP_PRINTENV", "Print Environment",
     "Specify if PE environment variables are printed (MP_PRINTENV)",
     "no", "yes|no"},
    {"MP_PRIORITY_LOG", "Log Co-scheduler Messages",
     "Specify if messages are logged to co-scheduler log (MP_PRIORITY_LOG)",
     "yes", "yes|no"},
    {"MP_INFOLEVEL", "Message Level",
     "Specify level of PE message reporting (MP_INFOLEVEL)",
     "Warning",
     "Error|Warning|Informational|Diagnostic|Diagnostic level 4|Diagnostic level 5|Diagnostic level 6"},
    {"MP_LAPI_TRACE_LEVEL", "LAPI Trace Level",
     "Specify level of LAPI trace (MP_LAPI_TRACE_LEVEL)",
     "0", "0|1|2|3|4|5"},
    /*
     * Debug tab related attributes
     */
    {"MP_DEBUG_NOTIMEOUT", "Suppress Timeout",
     "Specify if debugger can attach without causing application timeout (MP_DEBUG_NOTIMEOUT)",
     "no", "yes|no"},
    {"MP_COREFILE_SIGTERM", "Corefile on SIGTERM",
     "Specify if corefile generated on SIGTERM (MP_COREFILE_SIGTERM)",
     "no", "yes|no"},
    {"MP_EUIDEVELOP", "MPI Parameter Checking",
     "Specify level of MPI parameter checking (MP_EUIDEVELOP)",
     "no", "yes|no|debug|minimum"},
    {"MP_COREFILE_FORMAT", "Corefile Format",
     "Specify core file format or name of lightweight core file(MP_COREFILE_FORMAT)",
     "standard", "standard|STDERR"},
    /*
     * System tab related attributes
     */
    {"MP_ADAPTER_USE", "Exclusive Adapter Use",
     "Specify how node's adapter should be used (MP_ADAPTER_USE)",
     "shared", "dedicated|shared"},
    {"MP_CPU_USE", "Exclusive CPU Use",
     "Specify how node's CPU should be used (MP_CPU_USE)",
     "multiple", "multiple|unique"},
//    { LoadLeveler only
//     "MP_EUIDEVICE", "Adapter",
//     "Specify adapter to use for message passing (MP_EUIDEVICE)",
//     "", "en0|fi0|sn_all|sn_single"
//    },
    /*
     * Node tab related attributes
     */
    {"MP_PGMMODEL", "Programming Model",
     "Specify programming model (MP_PGMMODEL)", "spmd",
     "spmd|mpmd"},
    /*
     * Performance tab related attributes
     */
    {"MP_CC_SCRATCH_BUF", "Fastest Collectives",
     "Specify if fastest collective algorithm is used (MP_CC_SCRATCH_BUF)",
     "yes", "yes|no"},
    {"MP_CSS_INTERRUPT", "Packets Generate Interrupts",
     "Specify if arriving packets generate interrupts (MP_CSS_INTERRUPT)",
     "no", "yes|no"},
    {"MP_PRIORITY_NTP", "Coscheduler Disables NTP",
     "Specify if PE co-scheduler turns NTP off (MP_PRIORITY_NTP)",
     "no", "yes|no"},
    {"MP_SHARED_MEMORY", "Use Shared Memory",
     "Specify is shared memory used for communication (MP_SHARED_MEMORY)",
     "yes", "yes|no"},
    {"MP_SINGLE_THREAD", "Single MPI Thread",
     "Specify if application has single thread with MPI calls (MP_SINGLE_THREAD)",
     "no", "yes|no"},
    {"MP_WAIT_MODE", "Wait Mode",
     "Specify thread behavior when waiting for messages (MP_WAIT_MODE)",
     "poll", "nopoll|poll|sleep|yield"},
    {"MP_TASK_AFFINITY", "Task Affinity",
     "Specify task affininty constraints (MP_TASK_AFFINITY)",
     "", "|SNI|-1"},
    /*
     * Miscellaneous tab related attributes
     */
    {"MP_HINTS_FILTERED", "Hints Filtered",
     "Specify if MPI info objects reject hints (MP_HINTS_FILTERED)",
     "yes", "yes|no"},
    {"MP_CLOCK_SOURCE", "Clock Source",
     "Specify if high performance switch clock is time source (MP_CLOCK_SOURCE)",
     "", "|OS"},
    {"MP_MSG_API", "Message Passing API",
     "Specify message passing API used by application (MP_MSG_API)",
     "MPI", "MPI|LAPI|MPI_LAPI|MPI,LAPI"},
    {"MP_TLP_REQUIRED", "Large Page Check",
     "Specify if PE checks if application was compiled with large pages (MP_TLP_REQUIRED)",
     "none", "none|warn|kill"}
};

int_launch_attr int_attrs[] = {
    /*
     * I/O tab related attributes
     */
    {"MP_IO_BUFFER_SIZE", "MPI I/O Buffer Size",
     "Specify default buffer size for MPI-IO (MPI_IO_BUFFER_SIZE)",
     8192, 1, 0x8000000},
    /*
     * Node tab related attributes
     */
    {"MP_PROCS", "Number of Tasks",
     "Specify number of program tasks (MP_PROCS)", 1, 1, INT_MAX},
    /*
     * Performance tab related attributes
     */
    {"MP_ACK_THRESH",
     "Acknowledgment Threshold",
     "Specify packet acknowledgment threshhold (MP_ACK_THRESH)",
     30, 1, 31},
    {"MP_EAGER_LIMIT", "Message Size Threshhold",
     "Specify rondezvous protocol message size threshhold (MP_EAGER_LIMIT)",
     0x10000, 1, INT_MAX},
    {"MP_MSG_ENVELOPE_BUF", "Envelope Buffer Size",
     "Specify size of message envelope buffer (MP_MSG_ENVELOPE_BUF)",
     0x800000, 1000001, INT_MAX},
    {"MP_POLLING_INTERVAL", "MPI Polling Interval",
     "Specify PE polling interval (MP_POLLING_INTERVAL)",
     400000, 1, INT_MAX},
    {"PE_RDMA_COUNT", "Number of rCtx Blocks",
     "Specify number of rCxt blocks (MP_RDMA_COUNT)",
     0, 0, INT_MAX},
    {"PE_RDMA_COUNT_2", "Number of rCtx Blocks",
     "Specify number of rCxt blocks (MP_RDMA_COUNT)", 0, 0, INT_MAX},
    {"MP_RETRANSMIT_INTERVAL", "MPI Retransmit Interval",
     "Specify interval to check if retransmit neeeded (MP_RETRANSMIT_INTERVAL)",
     10000, 1000, INT_MAX},
    {"MP_REXMIT_BUF_CNT",
     "Retransmit Buffers",
     "Specify number of retransmit buffers per task (MP_REXMIT_BUF_CNT)",
     128, 1, INT_MAX},
    {"MP_REXMIT_BUF_SIZE", "Maximum LAPI Buffered Message",
     "Specify maximum LAPI buffer size (MP_REXMIT_BUF_SIZE)",
     65568, 1, INT_MAX},
    {"MP_UDP_PACKET_SIZE", "UDP Packet Size",
     "Specify UDP packet size (MP_UDP_PACKET_SIZE)",
     1, 1, INT_MAX},
    {"PE_BUFFER_MEM", "Buffer Memory",
     "Specify size of preallocated early arrival buffer (MP_BUFFER_MEM)",
     0x4000000, 0, INT_MAX},
    /*
     * Miscellaneous tab related attributes
     */
    {"MP_THREAD_STACKSIZE",
     "Additional MPI Thread Stack Size",
     "Specify additional stack size for MPI service thread (MP_THREAD_STACKSIZE)",
     0, 0, INT_MAX},
    {"MP_PULSE", "Pulse Interval",
     "Specify interval PE checks remote modes (MP_PULSE)",
     600, 0, INT_MAX},
    {"MP_TIMEOUT", "Connection Timeout",
     "Specify timeout limit for connecting to remote nodes (MP_TIMEOUT)",
     150, 1, INT_MAX},
    /*
     * Additional integer attributes used only for validation of attribute
     * field contents. These are used in cases where a single integer, 
     * string or enumerated attribute is insufficient for validating 
     * an attribute field, for instance where an allowable value is an 
     * enumeration or integer value, or where a field contains
     * multiple sub-fields, such as mmm,nnn. For these attributes, 
     * short name, long name and default value are irrelevant.
     */
    {"MP_INSTANCES_INT", "???", "???", 0, 0, INT_MAX},
//    {"MP_RETRY_INT", "???", "???", 0, 1, INT_MAX}, Load Leveler only
};

long_int_launch_attr long_int_attrs[] = {
    {
     "PE_BUFFER_MEM_MAX", "???",
     "Specify maximum size of early arrival buffer (MP_BUFFER_MEM)",
     0, 0, 0x7fffffffffffffffLL}
};
#endif


/*************************************************************************/

/* Proxy command handlers                                                */

/*************************************************************************/
int
start_daemon(int trans_id, int nargs, char *args[])
{
    /*
     * Proxy startup. Allocate a list to contain machine definitions, where
     * each unique hostfile defines a new machine. Create a thread that
     * will monitor started poe processes for termination and will
     * notify the front end that the poe process has terminatred.
     */
	int i;
    pthread_attr_t term_thread_attrs;

    print_message(TRACE_MESSAGE, ">>> %s entered. line = %d.\n", __FUNCTION__,
		  __LINE__);
    print_message_args(nargs, args);
    for (i = 0; i < nargs; i++) {
    	if (proxy_test_attribute(BASE_ID_ATTR, args[i])) {
    		base_id = proxy_get_attribute_value_int(args[i]);
    	}
    }
    nodes = HashCreate(1024);
    pthread_attr_init(&thread_attrs);
    pthread_attr_setdetachstate(&thread_attrs, PTHREAD_CREATE_DETACHED);
    pthread_attr_init(&term_thread_attrs);
    pthread_attr_setdetachstate(&term_thread_attrs, PTHREAD_CREATE_JOINABLE);
    pthread_create(&termination_thread, &thread_attrs, pmd_monitor, NULL);
    send_ok_event(trans_id);
    return PROXY_RES_OK;
}

int
define_model(int trans_id, int nargs, char *args[])
{
    /*
     * Send the attribute definitions, launch attribute definitions,
     * and element definitions known by this proxy to the GUI.
     */
    print_message(TRACE_MESSAGE, ">>> %s entered. line = %d.\n", __FUNCTION__,
		  __LINE__);
    print_message_args(nargs, args);
    send_string_attrs(trans_id);
    send_int_attrs(trans_id);
    send_long_int_attrs(trans_id);
    send_enum_attrs(trans_id);
    send_local_default_attrs(trans_id);
    send_ok_event(trans_id);
    return PROXY_RES_OK;
}

int
start_events(int trans_id, int nargs, char *args[])
{
    /*
     * Send the complete machine state to the GUI. In PE standalone case,
     * there is only a machine and a queue, since nodes are not known
     * until an appliation is invoked and a hostlist provided. In the
     * PE/LoadLeveler case, query LoadLeveler to get the set of nodes
     * that are part of the cluster (machine) and send new node events
     * to the GUI for each node.
     */
    char id_str[12];
    char base_id_str[12];

    print_message(TRACE_MESSAGE, ">>> %s entered. line = %d.\n", __FUNCTION__,
		  __LINE__);
    print_message_args(nargs, args);
    start_events_transid = trans_id;
    machine_id = generate_id();
    queue_id - generate_id();
    sprintf(base_id_str, "%d", base_id);
    sprintf(id_str, "%d", machine_id);
    enqueue_event(proxy_new_machine_event(trans_id, base_id_str, id_str,
					  "default", MACHINE_STATE_UP));
    sprintf(id_str, "%d", queue_id);
    enqueue_event(proxy_new_queue_event(trans_id, base_id_str, id_str,
					"default", QUEUE_STATE_NORMAL));
    /*
     * Look for poe jobs already running on system
     */
    discover_jobs();
    /*
     * Do not send an acknowledgment for the start_events command here
     * since asynchronous event notifications for new machine, node,
     * process, job, as well as state changes in those objects use the
     * start_events transaction id. If an ack is sent here, that 
     * transaction id is invalid and any events using that id will cause
     * an exception in the front end state machine loop, terminating the
     * state machine.
     */
    return PROXY_RES_OK;
}

int
run(int trans_id, int nargs, char *args[])
{
    /*
     * Submit a Parallel Environmnent application for execution.
     * This function:
     * 1) parses the passed argument list
     * 2) sets the current working directory
     * 3) sets all environment variables passed in the argument list
     * 4) creates pipes to handle stdio to/from the application
     * 5) sets up I/O handlers for stdio
     * 6) sets up the argument list for the application
     * 7) starts the application by fork/exec
     * 8) starts a monitoring thread whose purpose is to wait for the
     *    attach.cfg file to be created, then update job state to running.
     * 9) notify the front end a new job has been submitted
     */
    char *execname;
    char *execdir;
    char *argp;
    char *jobid;
    char *cp;
    jobinfo *job;
    int i;
    int label_io;
    int split_io;
    int status;
    int stdout_pipe[2];
    int stderr_pipe[2];
    char *stdin_path;
    char *stdout_path;
    char *stderr_path;
    pid_t pid;
    char *mp_buffer_mem;
    char *mp_buffer_mem_max;
    int mp_buffer_mem_set;
    char mp_buffer_mem_value[50];
    char *mp_rdma_count;
    char *mp_rdma_count_2;
    int mp_rdma_count_set;
    char mp_rdma_count_value[50];

    print_message(TRACE_MESSAGE, ">>> %s entered. line = %d.\n", __FUNCTION__,
		  __LINE__);
    print_message_args(nargs, args);
    execdir = NULL;
    execname = NULL;
    argp = NULL;
    label_io = 0;
    split_io = 0;
    stdin_path = NULL;
    stdout_path = NULL;
    stderr_path = NULL;
    mp_buffer_mem = "";
    mp_buffer_mem_max = "";
    mp_buffer_mem_set = 0;
    mp_buffer_mem_value[0] = '\0';
    mp_rdma_count = "";
    mp_rdma_count_2 = "";
    mp_rdma_count_set = 0;
    mp_rdma_count_value[0] = '\0';
    /*
     * Process arguments passed to this function
     */
    for (i = 0; args[i] != NULL; i++) {
	/*
	 * Check if this is a PE environment variable, for instance 
	 * MP_PROCS=2
	 */
	if (strncmp(args[i], "MP_", 3) == 0) {
	    cp = strchr(args[i], '=');
	    if (cp != NULL) {
		/*
		 * The MP_LABELIO and MP_HOSTFILE environment variables are 
		 * used by the proxy so process them here.
		 */
		*cp = '\0';
		if (strcmp(args[i], "MP_LABELIO") == 0) {
		    if (strcmp((cp + 1), "yes") == 0) {
			label_io = 1;
		    }
		}
		else if (strcmp(args[i], "MP_HOSTFILE") == 0) {
		    FILE *hostlist;
		    /*
		     * Process host file, building new machine 
		     * configuration if this is a unique hostfile
		     */
		    hostlist = fopen((cp + 1), "r");
		    if (hostlist != NULL) {
			update_nodes(trans_id, hostlist);
			fclose(hostlist);
		    }
		}
		/*
		 * If MP_INFOLELEL is > 1 char, then convert from label 
		 * name to real setting value. Note that the original 
		 * setting is overwritten, which is ok since the
		 * new value is guaranteed to be shorter than the
		 * original value.
		 */
		else if ((strcmp(args[i], "MP_INFOLEVEL") == 0) &&
			 (strlen(cp + 1) != 1)) {
		    int n;

		    for (n = 0;
			 n < sizeof mp_infolevel_labels / sizeof(char *); n++) {
			if (strcmp(cp + 1, mp_infolevel_labels[n]) == 0) {
			    break;
			}
		    }
		    if (n < (sizeof mp_infolevel_labels / sizeof(char *))) {
			sprintf(cp + 1, "%d", n);
		    }
		}
		/*
		 * Restore the '=' in the environment variable setting
		 */
		*cp = '=';
	    }
	}
	/*
	 * Check if this is a variable set by the PE front end and handle it 
	 * if so.
	 */
	else if (strncmp(args[i], "PE_", 3) == 0) {
	    cp = strchr(args[i], '=');
	    if (cp != NULL) {
		*cp = '\0';
		if (strcmp(args[i], "PE_STDIN_PATH") == 0) {
		    stdin_path = cp + 1;
		}
		else if (strcmp(args[i], "PE_STDOUT_PATH") == 0) {
		    stdout_path = cp + 1;
		}
		else if (strcmp(args[i], "PE_STDERR_PATH") == 0) {
		    stderr_path = cp + 1;
		}
		else if ((strcmp(args[i], "PE_SPLIT_STDOUT") == 0) &&
			 (strcmp(cp + 1, "yes") == 0)) {
		    split_io = 1;
		}
		/*
		 * The PE environment variable MP_BUFFER_MEM gets
		 * special handling since it is the only environment
		 * variable which has up to 2 parameters, These parameters
		 * are treated separately in the GUI to simplify
		 * validation. The GUI sends the attributes PE_BUFFER_MEM
		 * and PE_BUFFER_MEM_MAX representing these two values.
		 * This code must re-assemble the parameters into a proper
		 * MP_BUFFER_MEM setting and ensure that the environment
		 * variable gets set. Since the values passed by the
		 * GUI must not be passed directly to the application, the
		 * front end identifies them with a PE_ prefix instead of
		 * the usual MP_ prefix.
		 * MP_RDMA_COUNT gets similar treatment, using GUI attributes
		 * PE_RDMA_COUNT and PE_RDMA_COUNT_2
		 */
		else if (strcmp(args[i], "PE_BUFFER_MEM") == 0) {
		    mp_buffer_mem = cp + 1;
		    mp_buffer_mem_set = 1;
		}
		else if (strcmp(args[i], "PE_BUFFER_MEM_MAX") == 0) {
		    mp_buffer_mem_max = cp + 1;
		    mp_buffer_mem_set = 1;
		}
		else if (strcmp(args[i], "PE_RDMA_COUNT") == 0) {
		    mp_rdma_count = cp + 1;
		    mp_rdma_count_set = 1;
		}
		else if (strcmp(args[i], "PE_RDMA_COUNT_2") == 0) {
		    mp_rdma_count_2 = cp + 1;
		    mp_rdma_count_set = 1;
		}
	    }
	}
	else {
	    /*
	     * Look for general launch configuration variables and handle 
	     * appropriately
	     */
	    cp = strchr(args[i], '=');
	    if (cp != NULL) {
		*cp = '\0';
		cp = cp + 1;
		if (strcmp(args[i], JOB_SUB_ID_ATTR) == 0) {
		    jobid = strdup(cp);
		}
		else if (strcmp(args[i], JOB_EXEC_NAME_ATTR) == 0) {
		    execname = cp;
		}
		else if (strcmp(args[i], JOB_WORKING_DIR_ATTR) == 0) {
		    status = chdir(cp);
		    if (status == -1) {
			post_error(trans_id, PROXY_EV_RT_SUBMITJOB_ERROR,
				   "Invalid working directory");
			return PROXY_RES_OK;
		    }
		}
		else if (strcmp(args[i], JOB_PROG_ARGS_ATTR) == 0) {
		    argp = cp;
		}
		else if (strcmp(args[i], JOB_EXEC_PATH_ATTR) == 0) {
		    execdir = cp;
		}
		else if (strcmp(args[i], JOB_ENV_ATTR) == 0) {
		}
	    }
	}
    }
    if (mp_buffer_mem_set) {
	snprintf(mp_buffer_mem_value, sizeof mp_buffer_mem_value,
		 "MP_BUFFER_MEM=%s%s%s", mp_buffer_mem,
		 (mp_buffer_mem_max[0] == '\0') ? "" : ",", mp_buffer_mem_max);
    }
    if (mp_rdma_count_set) {
	snprintf(mp_rdma_count_value, sizeof mp_rdma_count_value,
		 "MP_RDMA_COUNT=%s%s%s", mp_rdma_count,
		 (mp_rdma_count_2[0] == '\0') ? "" : ",", mp_rdma_count_2);
    }
    if (execdir == NULL) {
	post_error(trans_id, PROXY_EV_RT_SUBMITJOB_ERROR,
		   "No executable directory specified");
	return PROXY_RES_OK;
    }
    if (execname == NULL) {
	post_error(trans_id, PROXY_EV_RT_SUBMITJOB_ERROR,
		   "No executable specified");
	return PROXY_RES_OK;
    }
    job = (jobinfo *) malloc(sizeof(jobinfo));
    malloc_check(job, __FUNCTION__, __LINE__);
    /*
     * Set up pipes or files to handle stdio for application. If the path 
     * for a file is null, then that file descriptor will be redirected to
     * a pipe.
     * Handle file descriptor setup for stdout first
     */
    if (stdout_path == NULL) {
	status = pipe(stdout_pipe);
	if (status == -1) {
	    snprintf(emsg_buffer, sizeof emsg_buffer,
		     "Error creating stdout pipe: %s", strerror(errno));
	    emsg_buffer[sizeof emsg_buffer - 1] = '\0';
	    post_error(trans_id, PROXY_EV_RT_SUBMITJOB_ERROR, emsg_buffer);
	    return PROXY_RES_OK;
	}
	status = fcntl(stdout_pipe[0], F_SETFL, O_NONBLOCK);
	if (status == -1) {
	    snprintf(emsg_buffer, sizeof emsg_buffer,
		     "Error initializing stdout pipe: %s", strerror(errno));
	    emsg_buffer[sizeof emsg_buffer - 1] = '\0';
	    post_error(trans_id, PROXY_EV_RT_SUBMITJOB_ERROR, emsg_buffer);
	    return PROXY_RES_OK;
	}
	job->stdout_fd = stdout_pipe[0];
	job->stdout_redirect = 0;
    }
    else {
	job->stdout_fd = open(stdout_path, O_RDWR | O_CREAT | O_TRUNC, 0644);
	if (job->stdout_fd == -1) {
	    snprintf(emsg_buffer, sizeof emsg_buffer,
		     "Error redirecting stdout to %s: %s", stdout_path,
		     strerror(errno));
	    emsg_buffer[sizeof emsg_buffer - 1] = '\0';
	    post_error(trans_id, PROXY_EV_RT_SUBMITJOB_ERROR, emsg_buffer);
	    return PROXY_RES_OK;
	}
	job->stdout_redirect = 1;
    }
    /*
     * Handle file descriptor setup for stderr
     */
    if (stderr_path == NULL) {
	status = pipe(stderr_pipe);
	if (status == -1) {
	    snprintf(emsg_buffer, sizeof emsg_buffer,
		     "Error creating stderr pipe: %s", strerror(errno));
	    emsg_buffer[sizeof emsg_buffer - 1] = '\0';
	    post_error(trans_id, PROXY_EV_RT_SUBMITJOB_ERROR, emsg_buffer);
	    return PROXY_RES_OK;
	}
	status = fcntl(stderr_pipe[0], F_SETFL, O_NONBLOCK);
	if (status == -1) {
	    snprintf(emsg_buffer, sizeof emsg_buffer,
		     "Error initializing stderr pipe: %s", strerror(errno));
	    emsg_buffer[sizeof emsg_buffer - 1] = '\0';
	    post_error(trans_id, PROXY_EV_RT_SUBMITJOB_ERROR, emsg_buffer);
	    return PROXY_RES_OK;
	}
	/*
	 * The handler for stderr must be registered here in order to
	 * handle error messages issued at application startup. 
	 */
	RegisterFileHandler(stderr_pipe[0], READ_FILE_HANDLER,
			    stderr_handler, job);
	job->stderr_fd = stderr_pipe[0];
	job->stderr_redirect = 0;
    }
    else {
	job->stderr_fd = open(stderr_path, O_RDWR | O_CREAT | O_TRUNC, 0644);
	if (job->stderr_fd == -1) {
	    snprintf(emsg_buffer, sizeof emsg_buffer,
		     "Error redirecting stderr to %s: %s", stdout_path,
		     strerror(errno));
	    emsg_buffer[sizeof emsg_buffer - 1] = '\0';
	    post_error(trans_id, PROXY_EV_RT_SUBMITJOB_ERROR, emsg_buffer);
	    return PROXY_RES_OK;
	}
	job->stderr_redirect = 1;
    }
    job->submit_jobid = jobid;
    job->label_io = label_io;
    job->split_io = split_io;
    job->stdout_info.read_buf = (char *) malloc(READ_BUFFER_SIZE);
    malloc_check(job->stdout_info.read_buf, __FUNCTION__, __LINE__);
    job->stdout_info.write_buf = (char *) malloc(STDIO_WRITE_BUFSIZE);
    malloc_check(job->stdout_info.write_buf, __FUNCTION__, __LINE__);
    job->stdout_info.allocated = STDIO_WRITE_BUFSIZE;
    job->stdout_info.remaining = STDIO_WRITE_BUFSIZE - 1;
    job->stdout_info.cp = job->stdout_info.write_buf;
    job->stdout_info.write_func = send_stdout;
    job->stderr_info.read_buf = (char *) malloc(READ_BUFFER_SIZE);
    malloc_check(job->stderr_info.read_buf, __FUNCTION__, __LINE__);
    job->stderr_info.write_buf = (char *) malloc(STDIO_WRITE_BUFSIZE);
    malloc_check(job->stderr_info.write_buf, __FUNCTION__, __LINE__);
    job->stderr_info.allocated = STDIO_WRITE_BUFSIZE;
    job->stderr_info.remaining = STDIO_WRITE_BUFSIZE - 1;
    job->stderr_info.cp = job->stderr_info.write_buf;
    job->stderr_info.write_func = send_stderr;
    job->discovered_job = 0;
    pid = fork();
    if (pid == 0) {
	char **argv;
	char **envp;
	char poe_target[_POSIX_PATH_MAX * 2 + 2];
	int max_fd;

	/*
	 * Set up executable argument list and environment variables first 
	 * since there is a small timing window where a second run command 
	 * could be processed while this process is still setting up parameters
	 * for the first run, resulting in modification of the first program's
	 * parameters and environment variables.
	 */
	argv = create_exec_parmlist(POE, poe_target, argp);
	envp = create_env_array(args, split_io, mp_buffer_mem_value,
				mp_rdma_count_value);
	/*
	 * Connect stdio to pipes or files owned by parent process (the 
	 * proxy)
	 */
	if (job->stdout_redirect) {
	    status = dup2(job->stdout_fd, STDOUT_FILENO);
	}
	else {
	    close(stdout_pipe[0]);
	    status = dup2(stdout_pipe[1], STDOUT_FILENO);
	}
	if (status == -1) {
	    snprintf(emsg_buffer, sizeof emsg_buffer,
		     "Error setting stdout file descriptor for application: %s",
		     strerror(errno));
	    emsg_buffer[sizeof emsg_buffer - 1] = '\0';
	    post_error(trans_id, PROXY_EV_RT_SUBMITJOB_ERROR, emsg_buffer);
	    exit(1);
	}
	if (job->stderr_redirect) {
	    status = dup2(job->stderr_fd, STDERR_FILENO);
	}
	else {
	    close(stderr_pipe[0]);
	    status = dup2(stderr_pipe[1], STDERR_FILENO);
	}
	if (status == -1) {
	    snprintf(emsg_buffer, sizeof emsg_buffer,
		     "Error setting stderr file descriptor for application: %s",
		     strerror(errno));
	    emsg_buffer[sizeof emsg_buffer - 1] = '\0';
	    post_error(trans_id, PROXY_EV_RT_SUBMITJOB_ERROR, emsg_buffer);
	    exit(1);
	}
	/*
	 * Close all open file descriptors above stderr.
	 */
	max_fd = sysconf(_SC_OPEN_MAX);
	for (i = STDERR_FILENO + 1; i < max_fd; i++) {
	    close(i);
	}
	/*
	 * Invoke the application as a target of 'poe'
	 */
	snprintf(poe_target, sizeof poe_target, "%s/%s", execdir, execname);
	poe_target[sizeof poe_target - 1] = '\0';
	print_message(INFO_MESSAGE, "Ready to invoke %s\n", poe_target);
	status = execve("/usr/bin/poe", argv, envp);
	print_message(ERROR_MESSAGE, "%s failed to execute, status %s\n",
		      argv[0], strerror(errno));
	post_error(trans_id, PROXY_EV_RT_SUBMITJOB_ERROR, "Exec failed");
	exit(1);
    }
    else {
	if (pid == -1) {
	    post_error(trans_id, PROXY_EV_RT_SUBMITJOB_ERROR, "Fork failed");
	    return PROXY_RES_OK;
	}
	else {
	    char jobname[40];
	    char queue_id_str[12];
	    char jobid_str[12];
	    struct passwd *userinfo;

	    if (!job->stdout_redirect) {
		close(stdout_pipe[1]);
	    }
	    if (!job->stderr_redirect) {
		close(stderr_pipe[1]);
	    }
	    /*
	     * Update job information for application and notify front end 
	     * that job is started.
	     */
	    job->tasks = NULL;
	    job->poe_pid = pid;
	    job->task0_pid = -1;
	    job->submit_time = time(NULL);
	    job->proxy_jobid = generate_id();
	    /*
	     * Create thread to watch for application's attach.cfg file to
	     * be created
	     */
	    pthread_create(&job->startup_thread, &thread_attrs,
			   startup_monitor, job);
	    AddToList(jobs, job);
	    userinfo = getpwuid(getuid());
	    snprintf(jobname, sizeof jobname, "%s.%s",
		     userinfo->pw_name, job->submit_jobid);
	    sprintf(queue_id_str, "%d", queue_id);
	    sprintf(jobid_str, "%d", job->proxy_jobid);
	    jobname[sizeof jobname - 1] = '\0';
	    enqueue_event(proxy_new_job_event(start_events_transid,
					      queue_id_str, jobid_str,
					      jobname, JOB_STATE_INIT,
					      job->submit_jobid));
	}
    }
    send_ok_event(trans_id);
    return PROXY_RES_OK;
}

int
halt_events(int trans_id, int nargs, char *args[])
{
    /*
     * Set flag indicating events are shut down, and send OK acks for
     * both the start_events and stop_events commands now. Once the
     * ack for the start_events command is sent, the start_events 
     * transaction id is no longer valid.
     */
    print_message(TRACE_MESSAGE, ">>> %s entered. line = %d.\n", __FUNCTION__,
		  __LINE__);
    print_message_args(nargs, args);
    events_enabled = 0;
    send_ok_event(start_events_transid);
    send_ok_event(trans_id);
    return PROXY_RES_OK;
}

int
terminate_job(int trans_id, int nargs, char *args[])
{
    /*
     * Terminate the application. The initial kill is SIGTERM. If that
     * doesn't work, then a separate thread issues a kill -9 one minute
     * later.
     */
    pthread_t kill_tid;
    jobinfo *job;
    int job_ident;

    print_message(TRACE_MESSAGE, ">>> %s entered. line = %d.\n", __FUNCTION__,
		  __LINE__);
    print_message_args(nargs, args);
    pthread_mutex_lock(&job_lock);
    SetList(jobs);
    job = GetListElement(jobs);
    job_ident = atoi(args[0]);
    while (job != NULL) {
	if (job_ident == job->proxy_jobid) {
	    break;
	}
	job = GetListElement(jobs);
    }
    if (job != NULL) {
	kill(job->poe_pid, SIGTERM);
	/*
	 * Create a thread to kill the process with kill(9) if the
	 * target process is still around after 1 minute.
	 */
	pthread_create(&kill_tid, &thread_attrs, kill_process,
		       (void *) job->poe_pid);
    }
    pthread_mutex_unlock(&job_lock);
    return PROXY_RES_OK;
}

int
quit(int trans_id, int nargs, char *args[])
{
    void *thread_status;

    print_message(TRACE_MESSAGE, ">>> %s entered. line = %d.\n", __FUNCTION__,
		  __LINE__);
    print_message_args(nargs, args);
    pthread_cancel(termination_thread);
    pthread_join(termination_thread, &thread_status);
    shutdown_requested = 1;
    redirect_io();
    return PROXY_RES_OK;
}

/*************************************************************************/

/* Service threads                                                       */

/*************************************************************************/
void *
startup_monitor(void *job_ident)
{
    /*
     * Wait for the attach.cfg file for this task to be completely filled
     * in, then read and parse it to build the map of tasks to nodes for
     * this application
     */
    char tasklist_path[_POSIX_PATH_MAX + 1];
    char *cfginfo;
    FILE *cfgfile;
    int numtasks;
    taskinfo *tasks;
    taskinfo *taskp;
    jobinfo *job;
    proxy_msg *msg;
    int status;
    int done;
    time_t last_mtime;
    struct stat fileinfo;
    int i;
    List *new_nodes;
    char jobid_str[30];

    job = (jobinfo *) job_ident;
    if (job->discovered_job) {
	new_nodes = NewList();
    }
    snprintf(tasklist_path, sizeof tasklist_path, "/tmp/.ppe.%d.attach.cfg",
	     job->poe_pid);
    tasklist_path[sizeof tasklist_path - 1] = '\0';
    print_message(TRACE_DETAIL_MESSAGE, "Waiting for task config file %s\n",
		  tasklist_path);
    last_mtime = -1;
    /*
     * Wait for the attach.cfg file to be created and to not be modified
     * within the last second before trying to read and parse it
     */
    for (;;) {
	sleep(1);
	status = stat(tasklist_path, &fileinfo);
	if (status == 0) {
	    if ((last_mtime >= job->submit_time) &&
		(last_mtime == fileinfo.st_mtime)) {
		break;
	    }
	    else {
		last_mtime = fileinfo.st_mtime;
	    }
	}
    }
    print_message(TRACE_DETAIL_MESSAGE, "Have task config file\n");
    done = 0;
    while (!done) {
	char *lineptr;
	char *tokenptr;
	char *linep;
	char *p;
	int tasknum;
	int numlines;

	stat(tasklist_path, &fileinfo);
	cfgfile = fopen(tasklist_path, "r");
	if (cfgfile != NULL) {
	    cfginfo = (char *) malloc(fileinfo.st_size);
	    malloc_check(cfginfo, __FUNCTION__, __LINE__);
	    status = fread(cfginfo, fileinfo.st_size, 1, cfgfile);
	    fclose(cfgfile);
	    if (status != fileinfo.st_size) {
		/*
		 * First line contains version info which we don't care
		 * about, and so it is ignored
		 */
		p = strtok_r(cfginfo, ";", &lineptr);
		if (p == NULL) {
		    break;
		}
		/*
		 * Second line contains number of tasks in job
		 */
		p = strtok_r(NULL, ";", &lineptr);
		if (p == NULL) {
		    break;
		}
		linep = p;
		p = strtok_r(linep, " ", &tokenptr);
		numtasks = atoi(p);
		numlines = 0;
		print_message(TRACE_DETAIL_MESSAGE,
			      "Application has %d tasks\n", numtasks);
		tasks = (taskinfo *) calloc(numtasks, sizeof(taskinfo));
		malloc_check(tasks, __FUNCTION__, __LINE__);
		/*
		 * Now read one line per task and build task list
		 */
		p = strtok_r(NULL, ";", &lineptr);
		if (p == NULL) {
		    break;
		}
		while (!done) {
		    char *cp;
		    linep = p;
		    print_message(TRACE_DETAIL_MESSAGE, "Processing %s\n",
				  linep);
		    /*
		     * get task index
		     */
		    p = strtok_r(linep, " ", &tokenptr);
		    if (p == NULL) {
			break;
		    }
		    tasknum = atoi(p);
		    taskp = &tasks[tasknum];
		    taskp->proxy_taskid = generate_id();
#ifdef __linux__
		    /*
		     * skip ignored token
		     */
		    p = strtok_r(NULL, " ", &tokenptr);
		    if (p == NULL) {
			break;
		    }
#endif
		    /*
		     * get node IP address
		     */
		    p = strtok_r(NULL, " ", &tokenptr);
		    if (p == NULL) {
			break;
		    }
		    taskp->ipaddr = strdup(p);
		    /*
		     * get task hostname. hostname will be truncated to
		     * short form.
		     */
		    p = strtok_r(NULL, " ", &tokenptr);
		    if (p == NULL) {
			break;
		    }
		    cp = strchr(p, '.');
		    if (cp != NULL) {
			*cp = '\0';
		    }
		    taskp->hostname = strdup(p);
		    /*
		     * get task pid
		     */
		    p = strtok_r(NULL, " ", &tokenptr);
		    if (p == NULL) {
			break;
		    }
		    taskp->task_pid = atoi(p);
		    /*
		     * get task parent pid
		     */
		    p = strtok_r(NULL, " ", &tokenptr);
		    if (p == NULL) {
			break;
		    }
		    taskp->parent_pid = atoi(p);
		    numlines = numlines + 1;
		    if (numlines >= numtasks) {
			done = 1;
			break;
		    }
		    /*
		     * Read next line of task info
		     */
		    p = strtok_r(NULL, ";", &lineptr);
		    if (p == NULL) {
			break;
		    }
		}
		/*
		 * If numline == numtasks then the file was complete and
		 * we are done with parsing the file. Otherwise clean up
		 * the list and try again
		 */
		if (!done) {
		    free(cfginfo);
		    delete_task_list(tasknum + 1, tasks);
		}
	    }
	    else {
		/*
		 * File was not completely read. Close the file then try 
		 * again. 
		 */
		free(cfginfo);
		fclose(cfgfile);
		delete_task_list(tasknum + 1, tasks);
		sleep(1);
	    }
	}
    }
    /*
     * attach.cfg file is complete and has been parsed. Notify the
     * front end that the job is running
     */
    job->tasks = tasks;
    job->numtasks = numtasks;
    send_job_state_change_event(start_events_transid, job->proxy_jobid,
				JOB_STATE_RUNNING);
    /*
     * For each task in the application, send a new process event to the
     * GUI.
     */
    taskp = tasks;
    sprintf(jobid_str, "%d", ((jobinfo *) job_ident)->proxy_jobid);
    msg = proxy_new_process_event(start_events_transid, jobid_str, numtasks);
    if (job->discovered_job) {
	/*
	 * If this is a job running before the proxy started, then
	 * there will be no hostfile. In this case, just add new
	 * nodes for each unique nodename found in the attach.cfg file.
	 */
	for (i = 0; i < numtasks; i++) {
	    if (find_node(taskp[i].hostname) == NULL) {
		node_refcount *node;

		node = add_node(taskp[i].hostname);
		AddToList(new_nodes, node);
		node_count = node_count + 1;
	    }
	}
	if (SizeOfList(new_nodes) > 0) {
	    send_new_node_list(start_events_transid, machine_id, new_nodes);
	}
	DestroyList(new_nodes, NULL);
    }
    for (i = 0; i < numtasks; i++) {
	node_refcount *node;

	if (i == 0) {
	    job->task0_pid = taskp->task_pid;
	}
	/*
	 * Increment the number of tasks running on the node and send the
	 * new process event
	 */
	node = find_node(taskp[i].hostname);
	if (node == NULL) {
	    print_message(ERROR_MESSAGE, "Node %s not found in node list\n",
			  taskp->hostname);
	}
	else {
	    char procid_str[12];
	    char procname[20];

	    node->task_count = node->task_count + 1;
	    sprintf(procid_str, "%d", taskp[i].proxy_taskid);
	    sprintf(procname, "task_%d", i);
	    proxy_add_process(msg, procid_str, procname, PROC_STATE_RUNNING, 3);
	    proxy_add_int_attribute(msg, PROC_NODEID_ATTR, node->proxy_nodeid);
	    proxy_add_int_attribute(msg, PROC_INDEX_ATTR, i);
	    proxy_add_int_attribute(msg, PROC_PID_ATTR, taskp[i].task_pid);
	}
    }
    enqueue_event(msg);
    /*
     * Now that all task pids are known, I/O from the application can be
     * enabled since we can now map I/O to a specific pid. This is done
     * here only for stdout since task mapping information is required to
     * handle splitting stdout by task. Output to stderr is not split by
     * task, so the stderr file handler is registered at application startup.
     * If the job was a job discovered at proxy startup, there is no connection
     * to stdio file descriptors, so don't register file handlers. 
     */
    if (!job->stdout_redirect && !job->discovered_job) {
	RegisterFileHandler(job->stdout_fd, READ_FILE_HANDLER,
			    stdout_handler, job);
    }
    /*
     * The startup thread exits at this point, so clear the reference in 
     * the job info
     */
    job->startup_thread = 0;
    return NULL;
}

void *
pmd_monitor(void *arg)
{
    /*
     * Watch for poe tasks started by this proxy to terminate. When a task
     * terminates, clean up resources and notify the front end that a job
     * has completed. Post completion status to front end. This function is
     * invoked by a thread started at proxy startup, and runs until the
     * proxy is shut down
     */
    int status;
    pid_t terminated_pmd;
    struct rusage rusage_info;
    jobinfo *job;

    print_message(TRACE_MESSAGE, ">>> %s entered. line = %d.\n", __FUNCTION__,
		  __LINE__);
    pthread_setcancelstate(PTHREAD_CANCEL_ENABLE, NULL);
    pthread_setcanceltype(PTHREAD_CANCEL_ASYNCHRONOUS, NULL);

    for (;;) {
	pthread_mutex_lock(&job_lock);
	/*
	 * Scan the job list looking for jobs running before the proxy
	 * started. For each of those jobs, check if the poe process
	 * still exists. If the process has disappeared, then mark the
	 * job terminated.
	 */
	SetList(jobs);
	job = GetListElement(jobs);
	while (job != NULL) {
	    if ((job->discovered_job) && (kill(job->poe_pid, 0) != 0)) {
		print_message(TRACE_DETAIL_MESSAGE,
			      "%d Exited with unknown status\n", job->poe_pid);
		send_job_state_change_event(start_events_transid,
					    job->proxy_jobid,
					    JOB_STATE_TERMINATED);
		send_process_state_change_event(start_events_transid, job,
						PROC_STATE_EXITED);
		if (job->tasks != NULL) {
		    update_node_refcounts(job->numtasks, job->tasks);
		    delete_task_list(job->numtasks, job->tasks);
		}
		RemoveFromList(jobs, job);
	    }
	    job = GetListElement(jobs);
	}
	/*
	 * wait for any poe task to terminate
	 */
	terminated_pmd = wait3(&status, WNOHANG, &rusage_info);
	if (terminated_pmd > 0) {

	    print_message(TRACE_DETAIL_MESSAGE, "Pid %d exited\n",
			  terminated_pmd);
	    /*
	     * Look for job with matching poe process pid. If found, then
	     * post process status to front end and clean up resources
	     */
	    SetList(jobs);
	    job = GetListElement(jobs);
	    while (job != NULL) {
		if (terminated_pmd == job->poe_pid) {
		    break;
		}
		job = GetListElement(jobs);
	    }
	    if (job != NULL) {
		/*
		 * POE process status used to be reported to the GUI. 
		 * Apparently, now, only job status is posted.
		 */
		if (status <= 128) {
		    print_message(TRACE_DETAIL_MESSAGE,
				  "%d Exited with status %08x\n",
				  terminated_pmd, status);
		    send_job_state_change_event(start_events_transid,
						job->proxy_jobid,
						JOB_STATE_TERMINATED);
		    send_process_state_change_event(start_events_transid, job,
						    PROC_STATE_EXITED);
		}
		else {
		    print_message(TRACE_DETAIL_MESSAGE,
				  "%d signalled with status %08x\n",
				  terminated_pmd, status - 128);
		    send_job_state_change_event(start_events_transid,
						job->proxy_jobid,
						JOB_STATE_ERROR);
		    send_process_state_change_event(start_events_transid, job,
						    PROC_STATE_EXITED_SIGNALLED);
		}
		/*
		 * Since the job is terminated, the stdio file descriptor
		 * pipes should be unregistered from the main polling loop
		 * and the file descriptors closed. However, if there
		 * is still data in the buffers, this data would be lost. 
		 * So, leave the pipes registered. The polling loop
		 * will eventually process the data left in the pipe. If the
		 * last line of output does not end with a newline, that
		 * line may be lost
		 */
		if (job->tasks != NULL) {
		    update_node_refcounts(job->numtasks, job->tasks);
		    delete_task_list(job->numtasks, job->tasks);
		}
		free(job->submit_jobid);
		free(job->stdout_info.read_buf);
		free(job->stderr_info.read_buf);
		free(job->stdout_info.write_buf);
		free(job->stderr_info.write_buf);
		RemoveFromList(jobs, job);
		free(job);
	    }
	}
	pthread_mutex_unlock(&job_lock);
	/*
	 * Poll for status once per second
	 */
	sleep(1);
    }
}

static
    void
redirect_io(void)
{
    /*
     * STDIO for running applications needs to be redirected in order to
     * prevent the application from terminating with SIGPIPE for stdin
     * or from blocking on stdout or stderr pipes when they fill.
     * To do this, build a command line invocation for a miniproxy
     * process consisting of the path prefixes for stdout and stderr
     * file descriptors followed by lists of file descriptors for stdin,
     * stdout and stderr, then invoke the miniproxy.
     */
    jobinfo *job;
    int stdin_count;
    int stdout_count;
    int stderr_count;
    int redirected_fds;
    char *miniproxy_args[3];
    char *miniproxy_parmlist;
    char *stdin_fds;
    char *stdout_fds;
    char *stderr_fds;
    char num[10];
    static char *miniproxy_env[] = { NULL };

    /*
     * Determine how many non-redirected file descriptors there are for
     * stdin, stdout and stderr.
     */
    stdin_count = 0;
    stdout_count = 0;
    stderr_count = 0;
    SetList(jobs);
    job = GetListElement(jobs);
    redirected_fds = 0;
    while (job != NULL) {
	if (!job->stdin_redirect) {
	    stdin_count = stdin_count + 1;
	    redirected_fds = 1;
	}
	if (!job->stdout_redirect) {
	    stdout_count = stdout_count + 1;
	    redirected_fds = 1;
	}
	if (!job->stderr_redirect) {
	    stderr_count = stderr_count + 1;
	    redirected_fds = 1;
	}
	job = GetListElement(jobs);
    }
    if (redirected_fds) {
	/*
	 * Allocate a string long enough to hold three pathnames, and lists of file 
	 * descriptors for stdin, stdout and stderr, each prefixed with a file
	 * descriptor count. Then allocate work strings for each file descriptor list
	 */
	miniproxy_parmlist = (char *) malloc(PATH_MAX * 3 + stdin_count * 11 +
					     stdout_count * 11 +
					     stderr_count * 11 + 30);
	if (miniproxy_parmlist == NULL) {
	    exit(1);
	}
	stdin_fds = (char *) malloc(stdin_count * 11);
	if (stdin_fds == NULL) {
	    exit(1);
	}
	stdout_fds = (char *) malloc(stdout_count * 11);
	if (stdout_fds == NULL) {
	    exit(1);
	}
	stderr_fds = (char *) malloc(stderr_count * 11);
	if (stderr_fds == NULL) {
	    exit(1);
	}
	/*
	 * Initialize the file descriptor lists, then concatenate each
	 * fd number for a non-redirected file to its list.
	 */
	stdin_fds[0] = '\0';
	stdout_fds[0] = '\0';
	stderr_fds[0] = '\0';
	SetList(jobs);
	job = GetListElement(jobs);
	while (job != NULL) {
	    if (!job->stdin_redirect) {
		sprintf(num, "%d ", job->stdin_fd);
		strcat(stdin_fds, num);
	    }
	    if (!job->stdout_redirect) {
		sprintf(num, "%d ", job->stdout_fd);
		strcat(stdout_fds, num);
	    }
	    if (!job->stderr_redirect) {
		sprintf(num, "%d ", job->stderr_fd);
		strcat(stderr_fds, num);
	    }
	    job = GetListElement(jobs);
	}
	/*
	 * Build the miniproxy parameter list. All data is contained in a 
	 * into individual parameters.
	 */
	strcpy(miniproxy_parmlist, miniproxy_args[0]);
	strcat(miniproxy_parmlist, "y /tmp/mp_stdout /tmp/mp_stderr ");
	sprintf(num, "%d ", stdin_count);
	strcat(miniproxy_parmlist, num);
	strcat(miniproxy_parmlist, stdin_fds);
	sprintf(num, "%d ", stdout_count);
	strcat(miniproxy_parmlist, num);
	strcat(miniproxy_parmlist, stdout_fds);
	sprintf(num, "%d ", stderr_count);
	strcat(miniproxy_parmlist, num);
	strcat(miniproxy_parmlist, stderr_fds);
	miniproxy_args[0] =
	    "/home/deepview/PTP_Head_June_2007/org.eclipse.ptp.ibmpe.proxy/ptp_ibmpe_miniproxy";
	miniproxy_args[1] = miniproxy_parmlist;
	miniproxy_args[2] = NULL;
	print_message(TRACE_DETAIL_MESSAGE, "Invoking miniproxy with args %s\n",
		      miniproxy_parmlist);
	if (fork() == 0) {
	    execve(miniproxy_args[0], miniproxy_args, miniproxy_env);
	    print_message(ERROR_MESSAGE, "Failed to invke miniproxy %s: %s\n",
			  miniproxy_args[0], strerror(errno));
	    exit(1);
	}
    }
}

/**************************************************************************/

/* Support functions                                                      */

/**************************************************************************/
char **
create_exec_parmlist(char *execname, char *targetname, char *args)
{
    char *tokenized_args;
    char *cp;
    char **argv;
    int i;
    int arg_count;
    int state;
    char quote;
    /*
     * Process argument list to 'poe' command. This is done in two passes
     * The first pass determines how many arguments there are so that argv
     * can be allocated, and terminates each arg with a '\0'.  The first
     * pass ignores whitespace outside of quoted strings when counting
     * and tokenizing args. It must preserve spaces within quoted strings
     * and also deal with quoted strings within an arg, for instance
     * 'This is an arg with a "quoted string" inside'
     */
    arg_count = 0;
    if (args == NULL) {
	argv = malloc(3 * sizeof(char *));
	malloc_check(argv, __FUNCTION__, __LINE__);
	argv[0] = execname;
	argv[1] = targetname;
	argv[2] = NULL;
    }
    else {
	quote = '\0';
	tokenized_args = strdup(args);
	cp = tokenized_args;
	state = SKIPPING_SPACES;
	while (*cp != '\0') {
	    switch (*cp) {
		case ' ':
		    if (state == PARSING_UNQUOTED_ARG) {
			arg_count = arg_count + 1;
			*cp = '\0';
			state = SKIPPING_SPACES;
		    }
		    break;
		case '"':
		case '\'':
		    if (state == PARSING_QUOTED_ARG) {
			if (*cp == quote) {
			    arg_count = arg_count + 1;
			    quote = '\0';
			    *cp = '\0';
			    state = SKIPPING_SPACES;
			}
			else {
			    quote = *cp;
			    state = PARSING_QUOTED_ARG;
			}
		    }
		    break;
		default:
		    if (state == SKIPPING_SPACES) {
			state = PARSING_UNQUOTED_ARG;
		    }
	    }
	    cp = cp + 1;
	}
	if (state != SKIPPING_SPACES) {
	    /*
	     * Last arg is terminated by ending '\0' so needs to be counted
	     * here
	     */
	    arg_count = arg_count + 1;
	}
	/*
	 * The second pass allocates argv, with one extra slot for the 
	 * poe executable, one for the name of the executable invoked by
	 * poe and one for the terminating null pointer. It then builds the
	 * argv array by scanning for the start of each arg, skipping 
	 * over an initial quote, if present. The trailing quote in a
	 * quoted arg was removed by the first pass tokenizing step.
	 */
	argv = malloc(sizeof(char *) * (arg_count + 3));
	malloc_check(argv, __FUNCTION__, __LINE__);
	argv[0] = execname;
	argv[1] = targetname;
	i = 2;
	cp = tokenized_args;
	state = SKIPPING_SPACES;
	while (i < (arg_count + 2)) {
	    if (state == SKIPPING_SPACES) {
		if (*cp != ' ') {
		    state = SKIPPING_CHARS;
		    if ((*cp == '"') || (*cp == '\'')) {
			argv[i] = cp + 1;
		    }
		    else {
			argv[i] = cp;
		    }
		    i = i + 1;
		}
	    }
	    else {
		if (*cp == '\0') {
		    state = SKIPPING_SPACES;
		}
	    }
	    cp = cp + 1;
	}
	argv[i] = NULL;
    }
    return argv;
}

char **
create_env_array(char *args[], int split_io, char *mp_buffer_mem,
		 char *mp_rdma_count)
{
    /*
     * Set up the environment variable array for the target application. 
     * Environment variables have two forms. Environment variables set on 
     * the environment tab of the launch configuration have the form
     * env=HOME=/home/ptpuser. PE environment variables set on the
     * parallel tab of the launch configuration have the form MP_PROCS=2
     * Both types of environment variables are added to the application 
     * environment array in the form HOME=/home/ptpuser. 
     * The user may have requested stdout output to be split by task. If so, 
     * then MP_LABELIO must be set to 'yes' as the last environment variable 
     * setting in the array. 
     * No other environment variables are passed to the application.
     */
    int i;
    int next_entry;
    int array_size;
    char **env_array;

    array_size = 100;
    next_entry = 0;
    env_array = (char **) malloc(sizeof(char *) * array_size);
    for (i = 0; args[i] != NULL; i++) {
	if (next_entry >= array_size) {
	    array_size = array_size + 100;
	    env_array = (char **) realloc(env_array,
					  sizeof(char *) * array_size);
	    malloc_check(env_array, __FUNCTION__, __LINE__);
	}
	if (strncmp(args[i], "MP_", 3) == 0) {
	    env_array[next_entry++] = strdup(args[i]);
	}
	else {
	    if (strncmp(args[i], "env=", 4) == 0) {
		env_array[next_entry++] = strdup(args[i] + 4);
	    }
	}
    }
    if (split_io == 1) {
	if (next_entry >= array_size) {
	    env_array = (char **) realloc(env_array,
					  sizeof(char *) * (array_size + 1));
	    malloc_check(env_array, __FUNCTION__, __LINE__);
	}
	env_array[next_entry++] = "MP_LABELIO=yes";
    }
    if (mp_buffer_mem[0] != '\0') {
	if (next_entry >= array_size) {
	    env_array = (char **) realloc(env_array,
					  sizeof(char *) * (array_size + 1));
	    malloc_check(env_array, __FUNCTION__, __LINE__);
	}
	env_array[next_entry++] = mp_buffer_mem;
    }
    if (mp_rdma_count[0] != '\0') {
	if (next_entry >= array_size) {
	    env_array = (char **) realloc(env_array,
					  sizeof(char *) * (array_size + 1));
	    malloc_check(env_array, __FUNCTION__, __LINE__);
	}
	env_array[next_entry++] = mp_rdma_count;
    }
    if (next_entry >= array_size) {
	env_array = (char **) realloc(env_array,
				      sizeof(char *) * (array_size + 1));
	malloc_check(env_array, __FUNCTION__, __LINE__);
    }
    env_array[next_entry++] = NULL;
    return env_array;
}

void
update_nodes(int trans_id, FILE * hostlist)
{
    /*
     * Create a node list, containing unique nodes, from the hostlist file.
     * Each time this function is called, new nodes may be referenced in the
     * hostlist since there is no restriction on modifying the hostlist. Any
     * new nodes will be added to the machine configuration.
     *
     * Message format for nodes message is event-id (RTEV_NATTR) followed by
     * encoded key-value pairs where each token is preceded with one space.
     * Multiple consecutive spaces in the message will cause parsing errors
     * in the Java code handling the response.
     */
    char *res;
    char *valstr;
    struct group *grp;
    struct passwd *pwd;
    char hostname[256];
    List *new_nodes;

    print_message(TRACE_MESSAGE, ">>> %s entered. line = %d.\n", __FUNCTION__,
		  __LINE__);
    valstr = NULL;
    pwd = getpwuid(geteuid());
    grp = getgrgid(getgid());
    res = fgets(hostname, sizeof(hostname), hostlist);
    new_nodes = NewList();
    while (res != NULL) {
	char *cp;

	/*
	 * Truncate node name to short form name
	 */
	cp = strpbrk(hostname, ".\n\r");
	if (cp != NULL) {
	    *cp = '\0';
	}
	if (find_node(hostname) == NULL) {
	    node_refcount *node;

	    node = add_node(hostname);
	    AddToList(new_nodes, node);
	    node_count = node_count + 1;
	}
	res = fgets(hostname, sizeof(hostname), hostlist);
    }
    send_new_node_list(start_events_transid, machine_id, new_nodes);
    DestroyList(new_nodes, NULL);
}

node_refcount *
find_node(char *key)
{
    /*
     * Look for node with matching key in the node list. The HashSearch
     * function only looks for a matching hash value. Since multiple keys
     * may hash to the same hash value, an additional check needs to be
     * made of keys that hash to the same value before determining a true
     * match.
     */
    int hash;
    List *node_list;

    hash = HashCompute(key, strlen(key));
    node_list = HashSearch(nodes, hash);
    if (node_list == NULL) {
	return NULL;
    }
    else {
	node_refcount *node;

	/*
	 * Optimally, this code would obtain a lock on the hash node rather 
	 * than locking all accesses to nodes, but currently the hash entry 
	 * only contains a pointer to the list. If this becomes a problem
	 * then the hash object could be replaced with a structure 
	 * containing the node lock and the list pointer.
	 */
	pthread_mutex_lock(&node_lock);
	SetList(node_list);
	node = GetListElement(node_list);
	while (node != NULL) {
	    if (strcmp(key, node->key) == 0) {
		break;
	    }
	    node = GetListElement(node_list);
	}
	pthread_mutex_unlock(&node_lock);
	return node;
    }
}

node_refcount *
add_node(char *key)
{
    /*
     * Add a node to the node list. Since the hash functions in hash.c only
     * detect duplicate hash values, and not keys that map to the same hash
     * value, the data at each node in the hash table is a simple linked
     * list of keys that map to the same hash and the associated data.
     */
    int hash;
    node_refcount *node;
    List *node_list;

    /*
     * Create the node for the hostname. The node key is the node's hostname
     */
    node = malloc(sizeof(node_refcount));
    malloc_check(node, __FUNCTION__, __LINE__);
    node->key = strdup(key);
    node->node_number = global_node_index;
    global_node_index = global_node_index + 1;
    node->proxy_nodeid = generate_id();
    node->task_count = 0;
    /*
     * Look for the hash key. If the hash key already exists, then
     * just add the hostname to the list for the hash key. Otherwise,
     * create a new list, add the hostname to the list and then add the
     * node list as the 'data' for the hash node.
     */
    hash = HashCompute(key, strlen(key));
    node_list = HashSearch(nodes, hash);
    if (node_list == NULL) {
	node_list = NewList();
	AddToList(node_list, node);
	HashInsert(nodes, hash, node_list);
    }
    else {
	AddToList(node_list, node);
    }
    return node;
}

void
update_node_refcounts(int numtasks, taskinfo * tasks)
{
    /*
     * Update node reference counts (number of tasks running on node),
     * subtracting 1 for each appearance of a node name in the task
     * list. If the count for a node is zero, then that node needs to
     * be removed from the node list and the front end notified.
     */
    node_refcount *noderef;
    int i;

    for (i = 0; i < numtasks; i++) {
	noderef = find_node(tasks[i].hostname);
	if (noderef == NULL) {
	    print_message(TRACE_DETAIL_MESSAGE,
			  "Failed to find expected node %s\n",
			  tasks[i].hostname);
	}
	else {
	    noderef->task_count = noderef->task_count - 1;
	    if (noderef->task_count <= 0) {
		print_message(TRACE_DETAIL_MESSAGE,
			      "No tasks left on %s, deleting\n",
			      tasks[i].hostname);
		delete_noderef(tasks[i].hostname);
	    }
	    else {
		print_message(TRACE_DETAIL_MESSAGE,
			      "Refcount for %s is %d\n", tasks[i].hostname,
			      noderef->task_count);
	    }
	}
    }
}

void
delete_noderef(char *hostname)
{
    /*
     * delete node from the node list and notify the front end
     */
    int hash;
    List *node_list;
    node_refcount *node;

    hash = HashCompute(hostname, strlen(hostname));
    node_list = HashSearch(nodes, hash);
    if (node_list == NULL) {
	return;
    }
    pthread_mutex_lock(&node_lock);
    SetList(node_list);
    node = GetListElement(node_list);
    while (node != NULL) {
	if (strcmp(node->key, hostname) == 0) {
	    char node_number[11];

	    free(node->key);
	    RemoveFromList(node_list, node);
	    sprintf(node_number, "%d", node->proxy_nodeid);
	    print_message(TRACE_DETAIL_MESSAGE,
			  "Sending delete event for node %s\n", node_number);
	    enqueue_event(proxy_remove_node_event(start_events_transid,
						  node_number));
	    free(node);
	    break;
	}
	node = GetListElement(node_list);
    }
    pthread_mutex_unlock(&node_lock);
}

void
delete_task_list(int numtasks, taskinfo * tasks)
{
    int i;

    for (i = 0; i < numtasks; i++) {
	if (tasks[i].hostname != NULL) {
	    free(tasks[i].hostname);
	}
	if (tasks[i].ipaddr != NULL) {
	    free(tasks[i].ipaddr);
	}
    }
    free(tasks);
}

void
hash_cleanup(void *hash_list)
{
    DestroyList(hash_list, NULL);
}

void *
kill_process(void *pid)
{
    sleep(60);
    kill((pid_t) pid, 9);
    return NULL;
}

void
malloc_check(void *p, const char *function, int line)
{
    if (p == NULL) {
	print_message(FATAL_MESSAGE,
		      "Memory allocation error in resource manager in %s (line %d)\n",
		      function, line);
	exit(1);
    }

}

void
send_string_attrs(int trans_id)
{
    /*
     * Send string attributes, including default values, to front end
     */
    int i;

    for (i = 0; i < (sizeof string_launch_attrs / sizeof(string_launch_attr));
	 i++) {
	enqueue_event(proxy_attr_def_string_event
		      (trans_id, string_launch_attrs[i].id,
		       string_launch_attrs[i].short_name,
		       string_launch_attrs[i].long_name, 0,
		       string_launch_attrs[i].default_value));
    }
}

void
send_int_attrs(int trans_id)
{
    /*
     * Send integer attributes, including default value, lower and upper
     * bounds, to front end
     */
    int i;

    for (i = 0; i < (sizeof int_attrs / sizeof(int_launch_attr)); i++) {
	enqueue_event(proxy_attr_def_int_limits_event
		      (trans_id, int_attrs[i].id,
		       int_attrs[i].short_name,
		       int_attrs[i].long_name, 0,
		       int_attrs[i].default_value,
		       int_attrs[i].llimit, int_attrs[i].ulimit));
    }
}

void
send_long_int_attrs(trans_id)
{
    /*
     * Send long (64 bit) intgeger attributes, including default value and 
     * lower and upper bounds to front end
     */
    int i;
    proxy_msg *msg;

    for (i = 0; i < (sizeof long_int_attrs / sizeof(long_int_launch_attr)); i++) {
	enqueue_event(proxy_attr_def_long_int_limits_event
		      (trans_id, long_int_attrs[i].id,
		       long_int_attrs[i].short_name,
		       long_int_attrs[i].long_name, 0,
		       long_int_attrs[i].default_value,
		       long_int_attrs[i].llimit, long_int_attrs[i].ulimit));
    }
}

void
send_enum_attrs(int trans_id)
{
    /*
     * Send enumerated attributes, including default and allowable values, to
     * front end.
     */
    int i;

    for (i = 0; i < (sizeof enum_attrs / sizeof(enum_launch_attr)); i++) {
	char *cp;
	char *end_cp;
	char *cp_save;
	int n;

	proxy_msg *msg;

	/*
	 * Count the number of enumerations in the enum list. Enumerations are
	 * delimited by '|' so number of enumerations is number of '|' + 1
	 */
	cp = enum_attrs[i].enums;
	n = 1;
	while (*cp != '\0') {
	    if ((*cp) == '|') {
		n = n + 1;
	    }
	    cp = cp + 1;
	}
	/*
	 * Create the enumeration attribute definition header
	 */
	msg = proxy_attr_def_enum_event(trans_id, enum_attrs[i].id,
					enum_attrs[i].short_name,
					enum_attrs[i].long_name, 0,
					enum_attrs[i].default_value, n);
	/*
	 * Append enumerations to message. Since enumerations string is
	 * a string literal that is illegal to modify, create a working copy
	 */
	cp = strdup(enum_attrs[i].enums);
	cp_save = cp;
	for (;;) {
	    end_cp = strchr(cp, '|');
	    if (end_cp == NULL) {
		proxy_msg_add_string(msg, cp);
		break;
	    }
	    else {
		*end_cp = '\0';
		proxy_msg_add_string(msg, cp);
		cp = end_cp + 1;
	    }
	}
	free(cp_save);
	/*
	 * Send the attribute definition
	 */
	enqueue_event(msg);
    }
}

void
send_local_default_attrs(int trans_id)
{
    /*
     * Send values of any PE environment variables (MP_*) to front end. These 
     * are sent as string attributes with the MP_ prefix replaced with 
     * EN_. These are used by the front end to set local (user) default values
     * for PE environment values, overriding IBM defaults.
     */
    char **env;
    char *cp;
    char *value;

    env = environ;
    while (*env != NULL) {
	/*
	 * Check if the environment variable is a PE environment variable.
	 */
	if (memcmp(*env, "MP_", 3) == 0) {
	    /*
	     * Make a duplicate of the environment then split into name/value
	     * at '=', change the first two characters of the name to 'EN' and
	     * send it to the front end as a string attribute. These attributes
	     * do not have attribute name or description since they do not 
	     * display in the GUI.
	     */
	    cp = strdup(*env);
	    value = strchr(cp, '=');
	    if (value != NULL) {
		*value = '\0';
		value = value + 1;
		memcpy(cp, "EN", 2);
		enqueue_event(proxy_attr_def_string_event(trans_id, cp, "",
							  "", 0, value));
	    }
	    free(cp);
	}
	env = env + 1;
    }

}

#ifdef __linux__
void
discover_jobs()
{
    /*
     * Look for already running poe jobs started by this user, and inform
     * the front end of each new job. For Linux, new jobs are found by 
     * reading /proc looking for directories corresponding to processes.
     * For each process which is owned by the user running the proxy, and
     * where the executable named in the command line is /usr/bin/poe,
     * generate the new job.
     */
    DIR *procdir;
    struct dirent *proc_entry;
    uid_t my_uid;
    struct stat stat_info;
    int status;
    char cmd_path[PATH_MAX];

    my_uid = getuid();
    procdir = opendir("/proc");
    if (procdir == NULL) {
	return;
    }
    status = chdir("/proc");
    if (status != 0) {
	return;
    }
    proc_entry = readdir(procdir);
    /*
     * For each filename in /proc, determine if it is a directory owned by
     * the user running the proxy. If it is, then attempt to read 
     * /proc/<pid>/cmdline and verify the executable's pathname. If it is
     * /usr/bin/poe, then this is a new job to be added to the job list.
     * Note that since a process may exit at any time, this function must
     * anticipate errors attempting to open or read files in /proc.
     */
    while (proc_entry != NULL) {
	status = stat(proc_entry->d_name, &stat_info);
	if (status == 0) {
	    if ((stat_info.st_uid == my_uid) && (S_ISDIR(stat_info.st_mode))) {
		FILE *cmdline_file;

		snprintf(cmd_path, sizeof cmd_path, "/proc/%s/cmdline",
			 proc_entry->d_name);
		cmdline_file = fopen(cmd_path, "r");
		if (cmdline_file != NULL) {
		    char cmdline[PATH_MAX];
		    char *cp;

		    cp = fgets(cmdline, sizeof cmdline, cmdline_file);
		    if (cp != NULL) {
			int len;

			/*
			 * The command line is an array of '\0' terminated
			 * tokens representing arg[0] thru arg[n]. Look
			 * at the last 3 characters of arg[0] to see if
			 * it is a poe process.
			 */
			len = strlen(cmdline);
			if (len >= 3) {
			    cp = cp + len - 3;
			}
			if (strcmp(cp, "poe") == 0) {
			    add_discovered_job(proc_entry->d_name);
			}
		    }
		    fclose(cmdline_file);
		}
	    }
	}
	proc_entry = readdir(procdir);
    }
    closedir(procdir);
}
#endif

#ifdef _AIX
void
discover_jobs()
{
    /*
     * Look for already running poe jobs started by this user, and inform
     * the front end of each new job. For AIX, do this by querying the 
     * process table and notifying the front end for each job detected.
     */
    struct procsinfo *procinfo;
    int num_procs;
    uid_t uid;
    pid_t start_pid;

    /*
     * Just get the number of entries in the process table so that we know
     * how big of an array to allocate. Note that processes may be added or 
     * deleted from the process table before the next call to getprocs, so it
     * is remotely possible that a newly started poe process is missed or
     * a poe process just terminating is found. The chances of either happening
     * are small and not worth the trouble of iterating on getprocs() to ensure
     * all processes are detected
     */
    start_pid = 0;
    num_procs = getprocs(NULL, sizeof(struct procsinfo), NULL,
			 sizeof(struct fdsinfo), &start_pid, INT_MAX);
    if (num_procs > 0) {
	procinfo =
	    (struct procsinfo *) malloc(num_procs * sizeof(struct procsinfo));
	malloc_check(procinfo, __FUNCTION__, __LINE__);
	/*
	 * Now get the actual process table.
	 * start_pid must be reset in order for getprocs to get process
	 * table information.
	 */
	start_pid = 0;
	num_procs = getprocs(procinfo, sizeof(struct procsinfo), NULL,
			     sizeof(struct fdsinfo), &start_pid, num_procs);
	if (num_procs >= 0) {
	    int i;

	    uid = getuid();
	    /*
	     * Notify the front end for each poe process owned by the user
	     */
	    for (i = 0; i < num_procs; i++) {
		if (procinfo[i].pi_uid == uid) {
		    char *cp;

		    cp = strrchr(procinfo[i].pi_comm, '/');
		    if (cp == NULL) {
			cp = procinfo[i].pi_comm;
		    }
		    else {
			cp = cp + 1;
		    }
		    if (strcmp(cp, "poe") == 0) {
			char poe_pid[20];

			snprintf(poe_pid, sizeof poe_pid, "%d",
				 procinfo[i].pi_pid);
			add_discovered_job(poe_pid);
		    }
		}
	    }
	}
	free(procinfo);
    }
}
#endif

void
add_discovered_job(char *pid)
{
    /*
     * Create a new job object for the discovered job, add it to the job
     * list, and notify the proxy of the new job.
     */
    jobinfo *job;
    struct passwd *userinfo;
    char jobid_str[12];
    char queueid_str[12];
    char jobname[40];

    job = calloc(1, sizeof(jobinfo));
    malloc_check(job, __FUNCTION__, __LINE__);
    job->proxy_jobid = generate_id();
    job->submit_jobid = "";
    job->poe_pid = atoi(pid);
    job->discovered_job = 1;
    job->submit_time = 0;
    sprintf(jobid_str, "%d", job->proxy_jobid);
    sprintf(queueid_str, "%d", queue_id);
    userinfo = getpwuid(getuid());
    snprintf(jobname, sizeof jobname, "%s.run_%s", userinfo->pw_name, pid);
    jobname[sizeof jobname - 1] = '\0';
    AddToList(jobs, job);
    enqueue_event(proxy_new_job_event(start_events_transid, queueid_str,
				      jobid_str, jobname, JOB_STATE_INIT,
				      job->submit_jobid));
    /*
     * Start a thread to watch for the attach.cfg file for this job
     * as if it was invoked by the front end.
     */
    pthread_create(&job->startup_thread, &thread_attrs, startup_monitor, job);
}

/*************************************************************************/

/* STDOUT/STDERR handling                                                */

/*************************************************************************/
int
stdout_handler(int fd, void *job)
{
    return write_output(fd, (jobinfo *) job, &((jobinfo *) job)->stdout_info);
}

int
stderr_handler(int fd, void *job)
{
    return write_output(fd, (jobinfo *) job, &((jobinfo *) job)->stderr_info);
}

int
write_output(int fd, jobinfo * job, ioinfo * file_info)
{
    /*
     * Read available data from the file descriptor and send it to
     * front end. Data is read from a non-blocking pipe to avoid hanging
     * on a read of an incomplete line of data. As a result, the data read
     * may be an incomplete line. This requires that as data is read, it is
     * appended to a second buffer until a newline character is read.
     * Once the newline character is read, the second buffer is sent to the
     * front end and the buffer reset so the next line of output can be
     * built.
     * FIX: May need to flush an incomplete line of output if the
     * application process exits (and closes the pipe) before the ending
     * newline is seen. 
     */
    int byte_count;
    char *cp;

    byte_count = read(fd, file_info->read_buf, READ_BUFFER_SIZE - 1);
    while (byte_count > 0) {
	file_info->read_buf[byte_count] = '\0';
	cp = file_info->read_buf;
	while (*cp != '\0') {
	    check_bufsize(file_info);
	    *file_info->cp = *cp;
	    if (*cp == '\n') {
		check_bufsize(file_info);
		file_info->cp = file_info->cp + 1;
		*file_info->cp = '\0';
		file_info->write_func(job, file_info->write_buf);
		file_info->cp = file_info->write_buf;
		file_info->remaining = file_info->allocated;
	    }
	    else {
		file_info->cp = file_info->cp + 1;
		file_info->remaining = file_info->remaining - 1;
	    }
	    cp = cp + 1;
	}
	byte_count = read(fd, file_info->read_buf, READ_BUFFER_SIZE - 1);
    }
    if (byte_count == -1) {
	if ((errno == EINTR) || (errno == EAGAIN)) {
	    return 0;
	}
	else {
	    return -1;
	}
    }
    else {
	print_message(TRACE_DETAIL_MESSAGE,
		      "EOF for fd %d. Unregistering handler\n", fd);
	UnregisterFileHandler(fd);
	close(fd);
	return 0;
    }
}

void
check_bufsize(ioinfo * file_info)
{
    if (file_info->remaining == 0) {
	file_info->allocated = file_info->allocated * 2;
	file_info->write_buf = (char *) realloc(file_info->write_buf,
						file_info->allocated);
	malloc_check(file_info->write_buf, __FILE__, __LINE__);
    }
}

void
send_stdout(jobinfo * job, char *buf)
{
    /*
     * Send the data written to stdio file descriptors to the front end.
     * If the user requested I/O to be split by task, then tag the message
     * with the task id of the originating task. Otherwise, tag it for task
     * 0.  In order to properly tag stdio, the proxy internally sets
     * MP_LABELIO=yes If the user had also set MP_LABELIO=yes, then send
     * the output with the PE-generated task id to the front end. Otherwise
     * strip off the task id before sending the message.
     */
    char *cp;
    char *outp;
    int task;

    cp = strchr(buf, ':');
    if (cp != NULL) {
	if (job->split_io) {
	    *cp = '\0';
	    task = atoi(buf);
	    *cp = ':';
	}
	else {
	    task = 0;
	}
	if (job->label_io) {
	    outp = buf;
	}
	else {
	    outp = cp + 1;
	}
    }
    else {
	task = 0;
	outp = buf;
    }
    send_process_state_output_event(start_events_transid,
				    job->tasks[task].proxy_taskid, outp);
}

void
send_stderr(jobinfo * job, char *buf)
{
    /*
     * Send the data written to stderr file descriptors to the front end. 
     */
    fprintf(stderr, "%s", buf);
}

/*************************************************************************/

/* Functions to send events to front end GUI                             */

/*************************************************************************/

static void
send_ok_event(int trans_id)
{
    proxy_msg *msg;

    msg = proxy_ok_event(trans_id);
    enqueue_event(msg);
}

void
post_error(int trans_id, int type, char *msgtext)
{
    /*
     * Send an error message to the front end
     */
    proxy_msg *msg;

    fprintf(stderr, "post_error: %s\n", msgtext);
    fflush(stderr);
    msg = new_proxy_msg(PROXY_EV_ERROR, trans_id);
    proxy_msg_add_int(msg, type);
    proxy_msg_add_string(msg, msgtext);
    enqueue_event(msg);
    return;
}

/*
 * Functions to send event notifications to front end. The attributes
 * and attribute values allowed for each event are defined in proxy_event.h
 */

static proxy_msg *
proxy_attr_def_int_limits_event(int trans_id, char *id,
				char *name, char *desc, int display, int def,
				int llimit, int ulimit)
{
    proxy_msg *msg;

    msg = new_proxy_msg(PROXY_EV_RT_ATTR_DEF, trans_id);
    proxy_msg_add_int(msg, 1);
    proxy_msg_add_int(msg, 8);
    proxy_msg_add_string(msg, id);
    proxy_msg_add_string(msg, "INTEGER");
    proxy_msg_add_string(msg, name);
    proxy_msg_add_string(msg, desc);
    proxy_msg_add_int(msg, display);
    proxy_msg_add_int(msg, def);
    proxy_msg_add_int(msg, llimit);
    proxy_msg_add_int(msg, ulimit);
    return msg;
}

static proxy_msg *
proxy_attr_def_long_int_limits_event(int trans_id, char *id,
				     char *name, char *desc, int display,
				     long long def, long long llimit,
				     long long ulimit)
{
    proxy_msg *msg;
    char num_str[22];

    msg = new_proxy_msg(PROXY_EV_RT_ATTR_DEF, trans_id);
    proxy_msg_add_int(msg, 1);
    proxy_msg_add_int(msg, 8);
    proxy_msg_add_string(msg, id);
    proxy_msg_add_string(msg, "BIGINTEGER");
    proxy_msg_add_string(msg, name);
    proxy_msg_add_string(msg, desc);
    proxy_msg_add_int(msg, display);
    sprintf(num_str, "%lld", def);
    proxy_msg_add_string(msg, num_str);
    sprintf(num_str, "%lld", llimit);
    proxy_msg_add_string(msg, num_str);
    sprintf(num_str, "%lld", ulimit);
    proxy_msg_add_string(msg, num_str);
    return msg;
}

static proxy_msg *
proxy_attr_def_enum_event(int trans_id, char *id, char *name,
			  char *desc, int display, char *def, int count)
{
    proxy_msg *msg;

    msg = new_proxy_msg(PROXY_EV_RT_ATTR_DEF, trans_id);
    proxy_msg_add_int(msg, 1);
    proxy_msg_add_int(msg, count + 6);
    proxy_msg_add_string(msg, id);
    proxy_msg_add_string(msg, "ENUMERATED");
    proxy_msg_add_string(msg, name);
    proxy_msg_add_string(msg, desc);
    proxy_msg_add_int(msg, display);
    proxy_msg_add_string(msg, def);
    return msg;
}

static void
send_new_node_list(int trans_id, int machine_id, List * new_nodes)
{
    proxy_msg *msg;
    node_refcount *noderef;
    char id_string[12];

    pthread_mutex_lock(&node_lock);
    SetList(new_nodes);
    sprintf(id_string, "%d", machine_id);
    msg = proxy_new_node_event(trans_id, id_string, SizeOfList(new_nodes));
    noderef = GetListElement(new_nodes);
    while (noderef != NULL) {
	sprintf(id_string, "%d", noderef->proxy_nodeid);
	proxy_add_node(msg, id_string, noderef->key, NODE_STATE_UP, 0);
	noderef = GetListElement(new_nodes);
    }
    pthread_mutex_unlock(&node_lock);
    enqueue_event(msg);
}

static void
send_job_state_change_event(int trans_id, int proxy_jobid, char *state)
{
    proxy_msg *msg;
    char jobid_str[12];

    sprintf(jobid_str, "%d", proxy_jobid);
    msg = proxy_job_change_event(trans_id, jobid_str, 1);
    proxy_msg_add_keyval_string(msg, JOB_STATE_ATTR, state);
    enqueue_event(msg);
}

static void
send_process_state_change_event(int trans_id, jobinfo * job, char *state)
{
    /*
     * Send state change event for all processes associated with the job
     *
     * Find each range of consecutive process object ids and send a
     * notification to the front end for that range of processes. This
     * usually reduces the number of process state change messages sent
     * to the front end. In the most common case, where a single
     * application was started before the next application was started,
     * there will only be a single message. If two application's
     * startup overlaps, then there is no guarantee of consecutive
     * ids for processes, then more than one message may be generated.
     */
    taskinfo *tasks;
    taskinfo *info;
    int task_index;
    int start_task;
    int next_task;
    proxy_msg *msg;
    char range[25];
    int i;

    tasks = job->tasks;
    if (tasks == NULL) {
	return;
    }
    task_index = -1;
    info = tasks;
    i = 0;
    while (i < job->numtasks) {
	task_index = info->proxy_taskid;
	start_task = task_index;
	next_task = task_index;
	/*
	 * Loop as long as the process object ids are consecutive and
	 * not end of list.
	 */
	while (task_index == next_task) {
	    i = i + 1;
	    info = &tasks[i];
	    next_task = next_task + 1;
	    if (i >= job->numtasks) {
		break;
	    }
	    task_index = info->proxy_taskid;
	}
	if (task_index != -1) {
	    /*
	     * Generate a process state change event for a consecutive
	     * range of process id objects. next_task will have a value
	     * 1 more than the last consecutive object id.
	     */
	    snprintf(range, sizeof range, "%d-%d", start_task, next_task - 1);
	    range[sizeof range - 1] = '\0';
	    msg = proxy_process_change_event(trans_id, range, 1);
	    proxy_msg_add_keyval_string(msg, PROC_STATE_ATTR, state);
	    enqueue_event(msg);
	    task_index = -1;
	}
    }
}

static void
send_process_state_output_event(int trans_id, int procid, char *output)
{
    proxy_msg *msg;
    char procid_str[12];

    sprintf(procid_str, "%d", procid);
    msg = proxy_process_change_event(trans_id, procid_str, 1);
    proxy_msg_add_keyval_string(msg, PROC_STDOUT_ATTR, output);
    print_message(TRACE_DETAIL_MESSAGE, "Sent stdout: %s\n", output);
    enqueue_event(msg);
}

static int
generate_id()
{
    return base_id + last_id++;
}

static void
enqueue_event(proxy_msg * msg)
{
    proxy_svr_queue_msg(pe_proxy, msg);
}

/*************************************************************************/

/* Proxy startup and command loop                                        */

/*************************************************************************/
int
main(int argc, char *argv[])
{
    char *host = "localhost";
    char *proxy_str = DEFAULT_PROXY;
    int ch;
    int port = PROXY_TCP_PORT;
    int rc;
    int n;
    char *cp;
    /*
     * Change the following '#if 0' to '#if 1' to enable code which suspends the
     * proxy at startup to allow a debugger to attach to the proxy process.
     * Once the debugger attaches to the process, the user can set the debug 
     * variable to 0 using the debugger, set appropriate breakpoints, then
     * resume proxy execution
     */
#if 0
    int debug = 1;
    while (debug) {
	sleep(1);
    }
#endif
#ifdef __linux__
    while ((ch = getopt_long(argc, argv, "P:p:h:d:", longopts, NULL)) != -1) {
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
	    case 'd':
	    debug_level = atoi(optarg);
	    break;
	    default:
		print_message(FATAL_MESSAGE,
			      "%s [--proxy=proxy] [--host=host_name] [--port=port] [--debug=level]\n",
			      argv[0]);
		return 1;
	}
    }
#else
    /*
     * AIX does not have the getopt_long function. Since the proxy is
     * invoked only by the PTP front end, the following simplified options
     * parsing is sufficient.
     * Make sure that this is maintained in sync with the above
     * getopt_long loop.
     */
    n = 1;
    while (n < argc) {
	cp = strchr(argv[n], '=');
	if (cp == NULL) {
	    break;
	}
	else {
	    *cp = '\0';
	    if (strcmp(argv[n], "--proxy") == 0) {
		proxy_str = cp + 1;
		n = n + 1;
	    }
	    else if (strcmp(argv[n], "--port") == 0) {
		port = atoi(cp + 1);
		n = n + 1;
	    }
	    else if (strcmp(argv[n], "--host") == 0) {
		host = cp + 1;
		n = n + 1;
	    }
	    else if (strcmp(argv[n], "--debug") == 0) {
	    debug_level = atoi(cp + 1);
		n = n + 1;
	    }
	    else {
		print_message(FATAL_MESSAGE, "Invalid argument %s (%d)\n",
			      argv[n], n);
		print_message(FATAL_MESSAGE,
			      "%s [--proxy=proxy] [--host=host_name] [--port=port] [--debug]\n",
			      argv[0]);
		return 1;
	    }
	}
    }
#endif
    ptp_signal_exit = 0;
    signal(SIGINT, ptp_signal_handler);
    signal(SIGHUP, ptp_signal_handler);
    signal(SIGILL, ptp_signal_handler);
    signal(SIGSEGV, ptp_signal_handler);
    signal(SIGTERM, ptp_signal_handler);
    signal(SIGQUIT, ptp_signal_handler);
    signal(SIGABRT, ptp_signal_handler);
    rc = server(proxy_str, host, port);
    HashDestroy(nodes, hash_cleanup);
    exit(rc);
}

int
shutdown(void)
{
    return 0;
}

RETSIGTYPE
ptp_signal_handler(int sig)
{
    ptp_signal_exit = sig;
    if (sig >= 0 && sig < NSIG) {
	RETSIGTYPE(*saved_signal) (int) = saved_signals[sig];
	if (saved_signal != SIG_ERR && saved_signal != SIG_IGN &&
	    saved_signal != SIG_DFL) {
	    saved_signal(sig);
	}
    }
}

int
server(char *name, char *host, int port)
{
    /*
     * Initialize the proxy, connect to front end, then run the main loop
     * until the proxy is requested to shut down.
     */
    char *msg1;
    char *msg2;
    int rc;
    struct timeval timeout = { 0, 20000 };

    jobs = NewList();
    if (proxy_svr_init(name, &timeout, &helper_funcs, &command_tab,
		       &pe_proxy) != PROXY_RES_OK)
	return 0;

    proxy_svr_connect(pe_proxy, host, port);
    print_message(INFO_MESSAGE, "Running IBMPE proxy on port %d\n", port);

    while (ptp_signal_exit == 0 && !shutdown_requested) {
	if ((proxy_svr_progress(pe_proxy) != PROXY_RES_OK)) {
	    print_message(TRACE_DETAIL_MESSAGE, "Loop ending\n");
	    break;
	}
    }

    if (ptp_signal_exit != 0) {
	switch (ptp_signal_exit) {
	    case SIGINT:
		msg1 = "INT";
		msg2 = "Interrupt";
		break;
	    case SIGHUP:
		msg1 = "HUP";
		msg2 = "Hangup";
		break;
	    case SIGILL:
		msg1 = "ILL";
		msg2 = "Illegal Instruction";
		break;
	    case SIGSEGV:
		msg1 = "SEGV";
		msg2 = "Segmentation Violation";
		break;
	    case SIGTERM:
		msg1 = "TERM";
		msg2 = "Termination";
		break;
	    case SIGQUIT:
		msg1 = "QUIT";
		msg2 = "Quit";
		break;
	    case SIGABRT:
		msg1 = "ABRT";
		msg2 = "Process Aborted";
		break;
	    default:
		msg1 = "***UNKNOWN SIGNAL***";
		msg2 = "ERROR - UNKNOWN SIGNAL";
		break;
	}
	print_message(FATAL_MESSAGE,
		      "ptp_pe_proxy received signal %s (%s) and is exiting.",
		      msg1, msg2);
	shutdown();
	/* our return code = the signal that fired */
	rc = ptp_signal_exit;
    }
    DestroyList(jobs, NULL);
    proxy_svr_finish(pe_proxy);
    print_message(TRACE_DETAIL_MESSAGE, "proxy_svr_finish returned.\n");

    return rc;
}

/************************************************************************* 
 * Print message (with time and date and thread id)                      * 
 * Info, Trace, Arg and Warning messages go to stdout.                   * 
 * Error and Fatal messages go to stderr.                                * 
 *************************************************************************/
void
print_message(int type, const char *format, ...)
{
    va_list ap;
    char timebuf[20];
    time_t clock;
    struct tm a_tm;
    int thread_id = 0;

    pthread_mutex_lock(&print_message_lock);
    pthread_t tid = pthread_self();	/* what thread am I ? */
    for (thread_id = 0;
	 thread_id < (sizeof(thread_map_table) / sizeof(pthread_t));
	 thread_id++) {
	if (tid == thread_map_table[thread_id]) {
	    break;
	}
    }
    time(&clock);		/* what time is it ? */
    localtime_r(&clock, &a_tm);
    strftime(&timebuf[0], 15, "%m/%d %02H:%02M:%02S", &a_tm);

    va_start(ap, format);
    switch (type) {
	case INFO_MESSAGE:
	    if (state_info == 1) {	/* if info messages allowed */
		if (state_message_timedate != 0) {
		    fprintf(stdout, "%s ", timebuf);
		}
		if (state_message_threadid != 0) {
		    fprintf(stdout, "T(%d) ", thread_id);
		}
		fprintf(stdout, "Info: ");
		vfprintf(stdout, format, ap);
		fflush(stdout);
	    }
	    break;
	case TRACE_MESSAGE:
	    if (state_trace == 1) {	/* if trace messages allowed */
		if (state_message_timedate != 0) {
		    fprintf(stdout, "%s ", timebuf);
		}
		if (state_message_threadid != 0) {
		    fprintf(stdout, "T(%d) ", thread_id);
		}
		fprintf(stdout, "Trace: ");
		vfprintf(stdout, format, ap);
		fflush(stdout);
	    }
	    break;
	case TRACE_DETAIL_MESSAGE:
	    if (state_trace_detail == 1) {	/* if trace messages allowed */
		if (state_message_timedate != 0) {
		    fprintf(stdout, "%s ", timebuf);
		}
		if (state_message_threadid != 0) {
		    fprintf(stdout, "T(%d) ", thread_id);
		}
		fprintf(stdout, "Trace: ");
		vfprintf(stdout, format, ap);
		fflush(stdout);
	    }
	    break;
	case WARNING_MESSAGE:
	    if (state_warning == 1) {	/* if warning messages allowed */
		if (state_message_timedate != 0) {
		    fprintf(stdout, "%s ", timebuf);
		}
		if (state_message_threadid != 0) {
		    fprintf(stdout, "T(%d) ", thread_id);
		}
		fprintf(stdout, "Warning: ");
		vfprintf(stdout, format, ap);
		fflush(stdout);
	    }
	    break;
	case ERROR_MESSAGE:
	    if (state_error == 1) {	/* if error messages allowed */
		if (state_message_timedate != 0) {
		    fprintf(stderr, "%s ", timebuf);
		}
		if (state_message_threadid != 0) {
		    fprintf(stderr, "T(%d) ", thread_id);
		}
		fprintf(stderr, "Error: ");
		vfprintf(stderr, format, ap);
		fflush(stderr);
	    }
	    break;
	case FATAL_MESSAGE:	/* fatal messages are never suppressed */
	    if (state_error == 1) {	/* if error messages allowed */
		if (state_message_timedate != 0) {
		    fprintf(stderr, "%s ", timebuf);
		}
		if (state_message_threadid != 0) {
		    fprintf(stderr, "T(%d) ", thread_id);
		}
		fprintf(stderr, "Fatal: ");
		vfprintf(stderr, format, ap);
		fflush(stderr);
	    }
	    break;
	case ARGS_MESSAGE:
	    if (state_args == 1) {	/* if formatted arg messages are allowed */
		if (state_message_timedate != 0) {
		    fprintf(stdout, "%s ", timebuf);
		}
		if (state_message_threadid != 0) {
		    fprintf(stdout, "T(%d) ", thread_id);
		}
		fprintf(stdout, "Args: ");
		vfprintf(stdout, format, ap);
		fprintf(stdout, "\n");
		fflush(stdout);
	    }
	    break;
	default:		/* unknown message type - allow it */
	    if (state_message_timedate != 0) {
		fprintf(stdout, "%s ", timebuf);
	    }
	    if (state_message_threadid != 0) {
		fprintf(stdout, "T(%d) ", thread_id);
	    }
	    fprintf(stdout, ": ");
	    vfprintf(stdout, format, ap);
	    fflush(stdout);
	    break;
    }

    va_end(ap);

    pthread_mutex_unlock(&print_message_lock);
}

void
print_message_args(int argc, char *optional_args[])
{
    int i;
    if (optional_args != NULL) {
	for (i = 0; i < argc; i++) {
	    print_message(ARGS_MESSAGE, " '%s'", optional_args[i]);
	}
    }
}

/*
 * This source file formatted using following indent options
 * -bap
 * -bbb
 * -br
 * -brs
 * -cli4
 * -i4
 * -l80
 * -lp
 * -ts8
 * -bli4
 * -npcs
 */
