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
 * 
 * Copyright (c) 2005, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 ******************************************************************************/

/******************************************************************************
 *  NOTE NOTE NOTE NOTE NOTE NOTE NOTE NOTE NOTE NOTE NOTE NOTE NOTE NOTE NOTE
 *  This code best developed and formatted on Linux using indent as follows: 
 * indent -kr -bap -bli2 -br -brs -cbi2 -ci2 -cli2 -d2 -di2 -fc1 -i2 -l1255 
 *         -lc255 -nbad -nbfde -nbfda -nbc -nsob -nut -sc -nfca ptp_ibmll_proxy.c
 * Note that the formatter still screws up a few comments but they are easily    
 * fixed and the code is so much more readable. It is appreciated if you stay                                  
 * with this formatting if you change the code in the repository. Thanks.                                      
 ******************************************************************************/
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
#ifdef __linux__
#include <getopt.h>
#endif
#include <errno.h>
#include <pwd.h>
#include <grp.h>
#include <proxy_cmd.h>
#include <proxy.h>
#include <proxy_tcp.h>
#include <proxy_event.h>
#include <proxy_cmd.h>
#include <proxy_msg.h>
#include <handler.h>
#include <signal.h>
#include <list.h>
#include <hash.h>
#include <limits.h>
#include <dlfcn.h>
#include <llapi.h>
/*
 * RTEV_ERROR codes are used internally in the ibmll specific plugin
 */
#define RTEV_ERROR_LL_INIT	    	  RTEV_OFFSET + 1000
#define RTEV_ERROR_LL_FINALIZE      RTEV_OFFSET + 1001
#define RTEV_ERROR_LL_SUBMIT_JOB   RTEV_OFFSET + 1002
#define RTEV_ERROR_CANCEL_JOB			RTEV_OFFSET + 1003
#define DEFAULT_PROXY "tcp"
#define DEFAULT_QUEUE_NAME "default"
#define ATTRIB_NODE_NAME_ID 1
#define ATTRIB_QUEUE_NAME_ID 1
/*
 * The following groups of definitions belong in a global PTP header,
 * not in individual proxies
 */
/*
 * RTEV codes must EXACTLY
 * match org.eclipse.ptp.rtsystem.proxy.event.IProxyRuntimeEvent
 */
#define RTEV_OFFSET			200
static RETSIGTYPE ptp_signal_handler(int sig);
static int server(char *name, char *host, int port, char *libpath);
static int command_initialize(int gui_transmission_id, int nargs, char *args[]);
static int command_define_model(int gui_transmission_id, int nargs, char *args[]);
static int command_submit_job(int gui_transmission_id, int nargs, char *args[]);
static int command_cancel_job(int gui_transmission_id, int nargs, char *args[]);
static int command_terminate(int gui_transmission_id, int nargs, char *args[]);
static int command_start_events(int gui_transmission_id, int nargs, char *args[]);
static int command_halt_events(int gui_transmission_id, int nargs, char *args[]);
static int command_suspend_events(int gui_transmission_id, int nargs, char *args[]);
static int command_resume_events(int gui_transmission_id, int nargs, char *args[]);
static int command_set_filters(int gui_transmission_id, int nargs, char *args[]);
static int command_clear_filters(int gui_transmission_id, int nargs, char *args[]);
static int command_get_attributes(int gui_transmission_id, int nargs, char *args[]);
static int command_query_attributes(int gui_transmission_id, int nargs, char *args[]);
static void print_message_args(int nargs, char *args[]);
static void print_message(int type, const char *, ...); /* INFO, TRACE & WARNING to stdout, ERROR & FATAL to stderr */
#define INFO_MESSAGE 0
#define TRACE_MESSAGE 1
#define WARNING_MESSAGE 2
#define ERROR_MESSAGE 3
#define FATAL_MESSAGE 4
#define ARGS_MESSAGE 5
static int state_trace = 0;     /* 0=message off, 1=message on */
static int state_info = 0;      /* 0=message off, 1=message on */
static int state_warning = 0;   /* 0=message off, 1=message on */
static int state_error = 0;     /* 0=message off, 1=message on */
static int state_fatal = 0;     /* 0=message off, 1=message on */
static int state_args = 0;      /* 0=message off, 1=message on */
static int state_events_active = 0;     /* events are off initialize */
static int state_debug_loop = 0;        /* debug loop for debugger attach is off by default */
static int state_shutdown_requested = 0;        /* shutdown not in progress */
static int state_message_timedate = 1;  /* want time date stamp in messages */
static int state_message_threadid = 1;  /* want thread id in messages */
static int state_template = 1;  /* always write default template at startup */
static char *static_template_prefix = "/tmp/PTP_IBMLL_TEMPLATE_";
static char *userid = NULL;
static char static_template_name[256];  /* template path and name */
static char *static_template_override = "";
static int max_node_sleep_seconds = 300;
static int min_node_sleep_seconds = 30;
static int job_sleep_seconds = 30;
static char hostname[256];
static int debug_level = 0; /* 0 is off */

struct ClusterObject {          /* a LoadLeveler cluster (same as a ptp machine) */
  int proxy_generated_cluster_id;
  char *cluster_name;
  Hash *node_hash;
  int proxy_generated_queue_id;
  int cluster_state;
  int queue_state;
  int cluster_is_local;
  int node_cleanup_required;
  int job_cleanup_required;
};
typedef struct ClusterObject ClusterObject;

struct NodeObject {             /* a LoadLeveler or ptp node in a cluster (machine) */
  int proxy_generated_node_id;
  int node_found;               /* node found indicator */
  int node_state;
  char *node_name;              /* use the name as the key to the node hash table in the cluster object */
};
typedef struct NodeObject NodeObject;
NodeObject *get_node_in_hash(Hash * node_hash, char *node_name);
void add_node_to_hash(Hash * node_hash, NodeObject * node_object);
void delete_node_from_list(List * node_list, NodeObject * node_object);

struct JobObject {              /* a LoadLeveler or ptp job in a cluster */
  int proxy_generated_job_id;
  int task_counter;             /* Source for task ids for job */
  char *gui_assigned_job_id;
  int job_found;                /* job found indicator */
  int job_state;                /* 1=submitted, 2=in queue */
  time_t job_submit_time;       /* time when submitted */
  List *task_list;              /* processes running for this job */
  LL_STEP_ID ll_step_id;
  char *cluster_name;
};
typedef struct JobObject JobObject;
JobObject *get_job_in_list(List * job_list, LL_STEP_ID ll_step_ID);
JobObject *get_job_in_list_from_id(List * job_list, int job_id);
void add_job_to_list(List * job_list, JobObject * job_object);
void delete_job_from_list(List * job_list, JobObject * job_object);

struct TaskObject {             /* a LoadLeveler or ptp task for job */
  int task_id;
  int task_found;               /* job found indicator */
  int ll_task_id;
  int task_state;
  char *node_name;
  char *node_address;
};
typedef struct TaskObject TaskObject;
TaskObject *get_task_in_list(List * task_list, char *task_instance_machine_name, int ll_task_id);
void add_task_to_list(List * task_list, TaskObject * task_object);
void delete_task_from_list(List * task_list, TaskObject * task_object);

static void sendErrorEvent(int gui_transmission_id, int type, char *msg);
static void sendJobSubmissionErrorEvent(int gui_transmission_id, char *subid, char *msgtext);
static void sendOkEvent(int gui_transmission_id);
static void sendShutdownEvent(int gui_transmission_id);
static int sendAttrDefIntEvent(int gui_transmission_id, char *id, char *shortname, char *description, int show, int default_value, int llimit, int ulimit);
static int sendAttrDefStringEvent(int gui_transmission_id, char *id, char *shortname, char *description, int show, char *value);
static int sendMachineAddEvent(int gui_transmission_id, ClusterObject * cluster_object);
static int sendQueueAddEvent(int gui_transmission_id, ClusterObject * cluster_object);
static int sendNodeAddEvent(int gui_transmission_id, ClusterObject * cluster_object, NodeObject * node_object);
static int sendNodeChangeEvent(int gui_transmission_id, ClusterObject * cluster_object, NodeObject * node_object);
static int sendNodeRemoveEvent(int gui_transmission_id, ClusterObject * cluster_object, NodeObject * node_object);
static int sendJobAddEvent(int gui_transmission_id, ClusterObject * cluster_object, JobObject * job_object);
static int sendJobChangeEvent(int gui_transmission_id, JobObject * job_object);
static int sendJobRemoveEvent(int gui_transmission_id, JobObject * job_object);
static int sendTaskAddEvent(int gui_transmission_id, ClusterObject * cluster_object, JobObject * job_object, TaskObject * task_object);
static int sendTaskChangeEvent(int gui_transmission_id, JobObject * job_object, TaskObject * task_object);
static int sendTaskRemoveEvent(int gui_transmission_id, JobObject * job_object, TaskObject * task_object);

static int generate_id(void);
static void enqueue_event_to_proxy_server(proxy_msg * event);
int main(int argc, char *argv[]);
extern char **environ;
void refresh_cluster_list();    /* refresh static list of clusters */
static List *cluster_list = NULL;       /* list of clusters if multicluster (we'll set to single local if none) */
static List *job_list = NULL;   /* job list for all clusters (since jobs can move) */
int get_multicluster_status();  /* are we running multicluster or single cluster ? */
static int multicluster_status = -1;    /* init to not determined yet  - we want 0-local 1-multicluster */
static pthread_t thread_map_table[256] = { 0, 0, 0, 0 };        /* to simplify messages */
static void malloc_check(void *p, const char *function, int line);
int is_substitution_required(char *line);

int register_thread(pthread_t handle);
int find_thread(pthread_t handle);

#define MY_STATE_UNKNOWN 0
#define MY_STATE_UP 1
#define MY_STATE_DOWN 2
#define MY_STATE_STOPPED 3
#define MY_STATE_RUNNING 4
#define MY_STATE_IDLE 5
#define MY_STATE_TERMINATED 6

static int ptp_signal_exit;
static int ptp_signal_thread;
static List *events = NULL;
static RETSIGTYPE(*saved_signals[NSIG]) (int);
static proxy_svr *ll_proxy = NULL;
static pthread_t monitor_LoadLeveler_jobs_thread = 0;
static pthread_attr_t monitor_LoadLeveler_jobs_thread_attr;
static void *monitor_LoadLeveler_jobs(void *args);
static pthread_t monitor_LoadLeveler_nodes_thread = 0;
static pthread_attr_t monitor_LoadLeveler_nodes_thread_attr;
static void *monitor_LoadLeveler_nodes(void *args);
static pthread_mutex_t access_LoadLeveler_lock = PTHREAD_MUTEX_INITIALIZER;
static pthread_mutex_t proxy_svr_queue_msg_lock = PTHREAD_MUTEX_INITIALIZER;
static pthread_mutex_t print_message_lock = PTHREAD_MUTEX_INITIALIZER;
static pthread_mutex_t master_lock = PTHREAD_MUTEX_INITIALIZER;
#ifdef __linux__
static struct option longopts[] = {
  {"proxy", required_argument, NULL, 'P'},
  {"port", required_argument, NULL, 'p'},
  {"host", required_argument, NULL, 'h'},
  {"lib_override", optional_argument, NULL, 'l'},
  {"debug_loop", optional_argument, NULL, 'd'},
  {"debug", optional_argument, NULL, 'g'},
  {"trace_messages", optional_argument, NULL, 't'},
  {"info_messages", optional_argument, NULL, 'i'},
  {"warning_messages", optional_argument, NULL, 'w'},
  {"error_messages", optional_argument, NULL, 'e'},
  {"fatal_messages", optional_argument, NULL, 'f'},
  {"args_messages", optional_argument, NULL, 'a'},
  {"multicluster", optional_argument, NULL, 'm'},
  {"template_override", optional_argument, NULL, 'o'},
  {"template_write", optional_argument, NULL, 'r'},
  {"node_polling_min", optional_argument, NULL, 'x'},
  {"node_polling_max", optional_argument, NULL, 'y'},
  {"job_polling", optional_argument, NULL, 'z'},
  {NULL, 0, NULL, 0}
};
#endif
static proxy_svr_helper_funcs helper_funcs = {
  NULL,
  NULL
};
static proxy_cmd ibmll_cmds[] = {
  command_terminate,
  command_initialize,
  command_define_model,
  command_start_events,
  command_halt_events,
  command_submit_job,
  command_cancel_job,
  command_suspend_events,
  command_resume_events,
  command_set_filters,
  command_clear_filters,
  command_get_attributes,
  command_query_attributes
};
static proxy_commands command_tab = {
  0, sizeof(ibmll_cmds) / sizeof(proxy_cmd), ibmll_cmds
};

static char * ibmll_libpath_name = NULL;
static void * ibmll_libpath_handle = 0;
static int ibmll_proxy_base_id = 0;
static char ibmll_proxy_base_id_string[256];
static int ibmll_last_id = 0;
static int start_events_gui_transmission_id = 0;

/**********************************************************************
 * LoadLeveler symbols from dynamic load of libllapi.a or libllapi.so *
 **********************************************************************/

static struct {
  LL_element *(*ll_query) (enum QueryType);
  int (*ll_set_request) (LL_element *, enum QueryFlags, char **, enum DataFilter);
  LL_element *(*ll_get_objs) (LL_element *, enum LL_Daemon, char *, int *, int *);
  int (*ll_get_data) (LL_element *, enum LLAPI_Specification, void *);
  int (*ll_deallocate) (LL_element *);
  LL_element *(*ll_next_obj) (LL_element *);
  int (*ll_free_objs) (LL_element *);
  int (*ll_cluster) (int, LL_element **, LL_cluster_param *);
  int (*ll_submit_job) (char *job_cmd_file, char *monitor_program, char *monitor_arg, LL_job * job_info, int job_version);
  int (*ll_terminate_job) (LL_terminate_job_info * ptr);
  void (*ll_free_job_info) (LL_job * job_info, int job_version);
  char *(*ll_error) (LL_element ** errObj, int print_to);
} LL_SYMS;

int my_ll_cluster(int, LL_element **, LL_cluster_param *);
int my_ll_get_data(LL_element *, enum LLAPI_Specification, void *);
LL_element *my_ll_query(enum QueryType);
int my_ll_free_objs(LL_element *);
int my_ll_deallocate(LL_element *);
LL_element *my_ll_next_obj(LL_element *);
LL_element *my_ll_get_objs(LL_element *, enum LL_Daemon, char *, int *, int *);
int my_ll_set_request(LL_element *, enum QueryFlags, char **, enum DataFilter);
int my_ll_submit_job(int gui_transmission_id, char *job_sub_id, char *command_file, char *job_env_vars[][3]);
int my_ll_terminate_job(int gui_transmission_id, JobObject * job_object);
void my_ll_free_job_info(LL_job * job_info);

void print_proxy_message(proxy_msg * msg);

  /*-----------------------------------------------------------------------* 
   * attributes to send to front end in response to the                    * 
   * command_define_model call to initialize the tabbed panels.            * 
   * This code is set up to send up blanks as defaults and not return any  *
   * field with blank back from gui so it will be stripped from job        *
   * command file.                                                         * 
   *-----------------------------------------------------------------------*/

  /*-----------------------------------------------------------------------* 
   * Strings                                                               * 
   *-----------------------------------------------------------------------*/
typedef struct {
  char *id;                     /* Attribute identifier                 */
  char *short_name;             /* Description used as label in GUI     */
  char *long_name;              /* Text used for tooltip text in GUI   */
  char *default_value;          /* Attribute's default value            */
} string_launch_attr;

static string_launch_attr string_launch_attrs[] = {
  {"LL_PTP_JOB_COMMAND_FILE", "IBMLLLaunch.LL_PTP_JOB_COMMAND_FILE_LABEL", "IBMLLLaunch.LL_PTP_JOB_COMMAND_FILE_TOOLTIP", ""},
  {"LL_PTP_JOB_COMMAND_FILE_TEMPLATE", "IBMLLLaunch.LL_PTP_JOB_COMMAND_FILE_TEMPLATE_LABEL", "IBMLLLaunch.LL_PTP_JOB_COMMAND_FILE_TEMPLATE_TOOLTIP", "/tmp/PTP_IBMLL_TEMPLATE_userid"},
  {"LL_PTP_CLASS", "IBMLLLaunch.LL_PTP_CLASS_LABEL", "IBMLLLaunch.LL_PTP_CLASS_TOOLTIP", "No_Class"},
  {"LL_PTP_COMMENT", "IBMLLLaunch.LL_PTP_COMMENT_LABEL", "IBMLLLaunch.LL_PTP_COMMENT_TOOLTIP", ""},
  {"LL_PTP_ERROR", "IBMLLLaunch.LL_PTP_ERROR_LABEL", "IBMLLLaunch.LL_PTP_ERROR_TOOLTIP", "/dev/null"},
  {"LL_PTP_EXECUTABLE", "IBMLLLaunch.LL_PTP_EXECUTABLE_LABEL", "IBMLLLaunch.LL_PTP_EXECUTABLE_TOOLTIP", ""},
  {"LL_PTP_ENVIRONMENT", "IBMLLLaunch.LL_PTP_ENVIRONMENT_LABEL", "IBMLLLaunch.LL_PTP_ENVIRONMENT_TOOLTIP", "COPY_ALL"},
  {"LL_PTP_INPUT", "IBMLLLaunch.LL_PTP_INPUT_LABEL", "IBMLLLaunch.LL_PTP_INPUT_TOOLTIP", "/dev/null"},
  {"LL_PTP_OUTPUT", "IBMLLLaunch.LL_PTP_OUTPUT_LABEL", "IBMLLLaunch.LL_PTP_OUTPUT_TOOLTIP", "/dev/null"},
  {"LL_PTP_INITIALDIR", "IBMLLLaunch.LL_PTP_INITIALDIR_LABEL", "IBMLLLaunch.LL_PTP_INITIALDIR_TOOLTIP", ""},
  {"LL_PTP_JOB_NAME", "IBMLLLaunch.LL_PTP_JOB_NAME_LABEL", "IBMLLLaunch.LL_PTP_JOB_NAME_TOOLTIP", ""},
  {"LL_PTP_NETWORK_MPI", "IBMLLLaunch.LL_PTP_NETWORK_MPI_LABEL", "IBMLLLaunch.LL_PTP_NETWORK_MPI_TOOLTIP", ""},
  {"LL_PTP_NETWORK_LAPI", "IBMLLLaunch.LL_PTP_NETWORK_LAPI_LABEL", "IBMLLLaunch.LL_PTP_NETWORK_LAPI_TOOLTIP", ""},
  {"LL_PTP_NETWORK_MPI_LAPI", "IBMLLLaunch.LL_PTP_NETWORK_MPI_LAPI_LABEL", "IBMLLLaunch.LL_PTP_NETWORK_MPI_LAPI_TOOLTIP", ""},
  {"LL_PTP_REQUIREMENTS", "IBMLLLaunch.LL_PTP_REQUIREMENTS_LABEL", "IBMLLLaunch.LL_PTP_REQUIREMENTS_TOOLTIP", ""},
  {"LL_PTP_RESOURCES", "IBMLLLaunch.LL_PTP_RESOURCES_LABEL", "IBMLLLaunch.LL_PTP_RESOURCES_TOOLTIP", ""},
  {"LL_PTP_SHELL", "IBMLLLaunch.LL_PTP_SHELL_LABEL", "IBMLLLaunch.LL_PTP_SHELL_TOOLTIP", ""},
  {"LL_PTP_TASK_GEOMETRY", "IBMLLLaunch.LL_PTP_TASK_GEOMETRY_LABEL", "IBMLLLaunch.LL_PTP_TASK_GEOMETRY_TOOLTIP", ""},
  {"LL_PTP_NODE_MIN", "IBMLLLaunch.LL_PTP_NODE_MIN_LABEL", "IBMLLLaunch.LL_PTP_NODE_MIN_TOOLTIP", ""},
  {"LL_PTP_NODE_MAX", "IBMLLLaunch.LL_PTP_NODE_MAX_LABEL", "IBMLLLaunch.LL_PTP_NODE_MAX_TOOLTIP", ""},
  {"LL_PTP_BLOCKING", "IBMLLLaunch.LL_PTP_BLOCKING_LABEL", "IBMLLLaunch.LL_PTP_BLOCKING_TOOLTIP", ""},
  {"LL_PTP_TOTAL_TASKS", "IBMLLLaunch.LL_PTP_TOTAL_TASKS_LABEL", "IBMLLLaunch.LL_PTP_TOTAL_TASKS_TOOLTIP", ""},
  {"LL_PTP_WALLCLOCK_HARD", "IBMLLLaunch.LL_PTP_WALLCLOCK_HARD_LABEL", "IBMLLLaunch.LL_PTP_WALLCLOCK_HARD_TOOLTIP", "00:00:00"},
  {"LL_PTP_WALLCLOCK_SOFT", "IBMLLLaunch.LL_PTP_WALLCLOCK_SOFT_LABEL", "IBMLLLaunch.LL_PTP_WALLCLOCK_SOFT_TOOLTIP", "00:00:00"}

};

  /*-----------------------------------------------------------------------* 
   * Enums                                                                 * 
   *-----------------------------------------------------------------------*/
typedef struct {
  char *id;                     /* Attribute identifier                 */
  char *short_name;             /* Description used as label in GUI */
  char *long_name;              /* Text used for tooltip text in GUI */
  char *default_value;          /* Attribute's default value            */
  char *enums;                  /* Enumeration values ',' delimited     */
} enum_launch_attr;

static enum_launch_attr enum_attrs[] = {
  {"LL_PTP_JOB_TYPE", "IBMLLLaunch.LL_PTP_JOB_TYPE_LABEL", "IBMLLLaunch.LL_PTP_JOB_TYPE_TOOLTIP", "Parallel", "Serial|Parallel|MPICH"},
  {"LL_PTP_BULK_XFER", "IBMLLLaunch.LL_PTP_BULK_XFER_LABEL", "IBMLLLaunch.LL_PTP_BULK_XFER_TOOLTIP", "(LoadLeveler default)", "(LoadLeveler default)|YES|NO"},
  {"LL_PTP_LARGE_PAGE", "IBMLLLaunch.LL_PTP_LARGE_PAGE_LABEL", "IBMLLLaunch.LL_PTP_LARGE_PAGE_TOOLTIP", "(LoadLeveler default)", "(LoadLeveler default)|Y|M|N"},
  {"LL_PTP_SUBMIT_MODE", "IBMLLLaunch.LL_PTP_SUBMIT_MODE_LABEL", "IBMLLLaunch.LL_PTP_SUBMIT_MODE_TOOLTIP", "Advanced", "Advanced|Basic"}
};

  /*-----------------------------------------------------------------------* 
   * Ints                                                                  * 
   *-----------------------------------------------------------------------*/
typedef struct {
  char *id;                     /* Attribute identifier                 */
  char *short_name;             /* Description used as label in GUI */
  char *long_name;              /* Text used for tooltip text in GUI */
  int default_value;            /* Attribute's default value            */
  int llimit;                   /* Attribute's lower limit (min)        */
  int ulimit;                   /* Attribute's upper limit (max)        */
} int_launch_attr;

int_launch_attr int_attrs[] = {
  {"LL_PTP_TASKS_PER_NODE", "IBMLLLaunch.LL_PTP_TASKS_PER_NODE_LABEL", "IBMLLLaunch.LL_PTP_TASKS_PER_NODE_TOOLTIP", 0,0,INT_MAX}
};

  /*-----------------------------------------------------------------------* 
   * Longs                                                                 * 
   *-----------------------------------------------------------------------*/
typedef struct {
  char *id;                     /* Attribute identifier                 */
  char *short_name;             /* Description used as label in GUI */
  char *long_name;              /* Text used for tooltip text in GUI */
  long long default_value;      /* Attribute's default value            */
  long long llimit;             /* Attribute's lower limit (min)        */
  long long ulimit;             /* Attribute's upper limit (max)        */
} long_int_launch_attr;

long_int_launch_attr long_int_attrs[] = {
  {"PlaceHolder-long", "???", "Place holder:", 0, 0, 0x7fffffffffffffffLL}
};

/************************************************************************* 
 * Job Command File Template                                             * 
 *************************************************************************/
static char *job_command_file_template[] = {
  "#!/bin/sh",
  "# @ account_no = <<<LL_PTP_ACCOUNT_NO>>>",
  "# @ arguments = <<<progArgs>>>",
  "#(NOT SUPPORTED)# @ bg_connection",
  "#(NOT SUPPORTED)# @ bg_partition",
  "#(NOT SUPPORTED)# @ bg_requirements",
  "#(NOT SUPPORTED)# @ bg_route",
  "#(NOT SUPPORTED)# @ bg_shape",
  "#(NOT SUPPORTED)# @ bg_size",
  "# @ blocking = <<<LL_PTP_BLOCKING>>>",
  "# @ bulkxfer = <<<LL_PTP_BULKXFER>>>",
  "# @ checkpoint = <<<LL_PTP_CHECKPOINT>>>",
  "# @ ckpt_dir = <<<LL_PTP_CKPT_DIR>>>",
  "# @ ckpt_execute_dir = <<<LL_PTP_CKPT_EXECUTE_DIR>>>",
  "# @ ckpt_file = <<<LL_PTP_CKPT_FILE>>>",
  "# @ ckpt_time_limit = <<<LL_PTP_CKPT_TIME_LIMIT_HARD>>>,<<<LL_PTP_CKPT_TIME_LIMIT_SOFT>>>",
  "# @ class = <<<LL_PTP_CLASS>>>",
  "# @ cluster_input_file = <<<LL_PTP_CLUSTER_INPUT_FILE_1>>>",
  "# @ cluster_input_file = <<<LL_PTP_CLUSTER_INPUT_FILE_2>>>",
  "# @ cluster_input_file = <<<LL_PTP_CLUSTER_INPUT_FILE_3>>>",
  "# @ cluster_list = <<<LL_PTP_CLUSTER_LIST>>>",
  "# @ cluster_output_file = <<<LL_PTP_CLUSTER_OUTPUT_FILE_1>>>",
  "# @ cluster_output_file = <<<LL_PTP_CLUSTER_OUTPUT_FILE_2>>>",
  "# @ cluster_output_file = <<<LL_PTP_CLUSTER_OUTPUT_FILE_3>>>",
  "# @ comment = <<<LL_PTP_COMMENT>>>",
  "# @ core_limit = <<<LL_PTP_CORE_LIMIT_HARD>>>,<<<LL_PTP_CORE_LIMIT_SOFT>>>",
  "#(NOT SUPPORTED)# @ coschedule",
  "# @ cpu_limit = <<<LL_PTP_CPU_LIMIT_HARD>>>,<<<LL_PTP_CPU_LIMIT_SOFT>>>",
  "# @ data_limit = <<<LL_PTP_DATA_LIMIT_HARD>>>,<<<LL_PTP_DATA_LIMIT_SOFT>>>",
  "#(NOT SUPPORTED)# @ dependency",
  "# @ env_copy = <<<LL_PTP_ENV_COPY>>>",
  "# @ environment = <<<LL_PTP_ENVIRONMENT>>>",
  "# @ error = <<<LL_PTP_ERROR>>>",
  "# @ executable = <<<execPath>>>/<<<execName>>>",
  "# @ executable = <<<LL_PTP_EXECUTABLE>>>",
  "# @ file_limit = <<<LL_PTP_FILE_LIMIT_HARD>>>,<<<LL_PTP_FILE_LIMIT_SOFT>>>",
  "# @ group = <<<LL_PTP_GROUP>>>",
  "#(NOT SUPPORTED)# @ hold",
  "# @ image_size = <<<LL_PTP_IMAGE_SIZE>>>",
  "# @ initialdir = <<<workingDir>>>",
  "# @ initialdir = <<<LL_PTP_INITIALDIR>>>",
  "# @ input = <<<LL_PTP_INPUT>>>",
  "# @ job_cpu_limit = <<<LL_PTP_JOB_CPU_LIMIT_HARD>>>, <<<LL_PTP_JOB_CPU_LIMIT_SOFT>>>",
  "# @ job_name = <<<LL_PTP_JOB_NAME>>>",
  "# @ job_type = <<<LL_PTP_JOB_TYPE>>>",
  "# @ large_page = <<<LL_PTP_LARGE_PAGE>>>",
  "#(NOT SUPPORTED)# @ max_processors",
  "# @ mcm_affinity_options = <<<LL_PTP_MCM_AFFINITY_OPTIONS>>>",
  "#(NOT SUPPORTED)# @ min_processors",
  "# @ network.MPI = <<<LL_PTP_NETWORK_MPI>>>",
  "# @ network.LAPI = <<<LL_PTP_NETWORK_LAPI>>>",
  "# @ network.MPI_LAPI = <<<LL_PTP_NETWORK_MPI_LAPI>>>",
  "# @ node = <<<LL_PTP_NODE_MIN>>>,<<<LL_PTP_NODE_MAX>>>",
  "# @ node_usage = <<<LL_PTP_NODE_USAGE>>>",
  "# @ notification = <<<LL_PTP_NOTIFICATION>>>",
  "# @ notify_user = <<<LL_PTP_NOTIFY_USER>>>",
  "# @ output = <<<LL_PTP_OUTPUT>>>",
  "# @ preferences = <<<LL_PTP_PREFERENCES>>>",
  "# @ requirements = <<<LL_PTP_REQUIREMENTS>>>",
  "# @ resources = <<<LL_PTP_RESOURCES>>>",
  "# @ restart = <<<LL_PTP_RESTART>>>",
  "# @ restart_from_ckpt = <<<LL_PTP_RESTART_FROM_CKPT>>>",
  "#(NOT SUPPORTED)# @ restart_on_same_nodes",
  "# @ rset = <<<LL_PTP_RSET>>>",
  "# @ shell = <<<LL_PTP_SHELL>>>",
  "# @ smt = <<<LL_PTP_SMT>>>",
  "# @ stack_limit = <<<LL_PTP_STACK_LIMIT_HARD>>>,<<<LL_PTP_STACK_LIMIT_SOFT>>>",
  "# @ start_date = <<<LL_PTP_START_DATE>>>",
  "#(NOT SUPPORTED)# @ step_name",
  "# @ task_geometry = <<<LL_PTP_TASK_GEOMETRY>>>",
  "# @ tasks_per_node = <<<LL_PTP_TASKS_PER_NODE>>>",
  "# @ total_tasks = <<<LL_PTP_TOTAL_TASKS>>>",
  "# @ user_priority = <<<LL_PTP_USER_PRIORITY>>>",
  "# @ wall_clock_limit = <<<LL_PTP_WALLCLOCK_HARD>>>,<<<LL_PTP_WALLCLOCK_SOFT>>>",
  "# @ queue"
};

/************************************************************************* 
 * Proxy command handler - command_initialize                            * 
 *                                                                       * 
 * Dynamically load LoadLeveler lib (libllapi.a for AIX or               * 
 * libllapi.so for Linux) then resolve the functions we need to          * 
 * call to talk to LoadLeveler (submit, cancel or query jobs,            * 
 * query machines, ...)                                                  * 
 *                                                                       * 
 * See llapi.h for interfaces to LoadLeveler.                            * 
 *************************************************************************/
int command_initialize(int gui_transmission_id, int nargs, char *args[])
{
  int i;
  int dlopen_mode = 0;
  int my_errno = 0;

  print_message(TRACE_MESSAGE, ">>> %s entered. line=%d.\n", __FUNCTION__, __LINE__);
  print_message_args(nargs, args);

  for (i = 0; i < nargs; i++) {
  	if (proxy_test_attribute(PTP_BASE_ID_ATTR, args[i])) {
  		ibmll_proxy_base_id = proxy_get_attribute_value_int(args[i]);      /* the base id established by gui front end */
  	}
  }  
  sprintf(ibmll_proxy_base_id_string, "%d", ibmll_proxy_base_id);

  memset(&LL_SYMS, '\0', sizeof(LL_SYMS));      /* zero the LoadLeveler dlsym symbol table */
  print_message(INFO_MESSAGE, "dlopen LoadLeveler shared library %s.\n", ibmll_libpath_name);
  dlopen_mode = RTLD_LOCAL | RTLD_NOW;
#ifdef _AIX
  dlopen_mode = dlopen_mode | RTLD_MEMBER;
#endif
  ibmll_libpath_handle = dlopen(ibmll_libpath_name, dlopen_mode);
  my_errno = errno;
  if (ibmll_libpath_handle == NULL) {
    print_message(ERROR_MESSAGE, "dlopen of %s failed with errno=%d.\n", ibmll_libpath_name, my_errno);
    sendErrorEvent(gui_transmission_id, RTEV_ERROR_LL_INIT, "dlopen failed for LoadLeveler shared library");
    return PTP_PROXY_RES_ERR;
  } else {
    print_message(INFO_MESSAGE, "dlopen %s successful.\n", ibmll_libpath_name);
  }

  print_message(INFO_MESSAGE, "Locating LoadLeveler functions via dlsym.\n");
  *(void **) (&(LL_SYMS.ll_query)) = dlsym(ibmll_libpath_handle, "ll_query");
  *(void **) (&LL_SYMS.ll_set_request) = dlsym(ibmll_libpath_handle, "ll_set_request");
  *(void **) (&LL_SYMS.ll_get_objs) = dlsym(ibmll_libpath_handle, "ll_get_objs");
  *(void **) (&LL_SYMS.ll_get_data) = dlsym(ibmll_libpath_handle, "ll_get_data");
  *(void **) (&LL_SYMS.ll_free_objs) = dlsym(ibmll_libpath_handle, "ll_free_objs");
  *(void **) (&LL_SYMS.ll_deallocate) = dlsym(ibmll_libpath_handle, "ll_deallocate");
  *(void **) (&LL_SYMS.ll_next_obj) = dlsym(ibmll_libpath_handle, "ll_next_obj");
  *(void **) (&LL_SYMS.ll_cluster) = dlsym(ibmll_libpath_handle, "ll_cluster");
  *(void **) (&LL_SYMS.ll_submit_job) = dlsym(ibmll_libpath_handle, "llsubmit");
  *(void **) (&LL_SYMS.ll_terminate_job) = dlsym(ibmll_libpath_handle, "ll_terminate_job");
  *(void **) (&LL_SYMS.ll_free_job_info) = dlsym(ibmll_libpath_handle, "llfree_job_info");
  *(void **) (&LL_SYMS.ll_error) = dlsym(ibmll_libpath_handle, "ll_error");
  if ((LL_SYMS.ll_query == NULL) || (LL_SYMS.ll_set_request == NULL)
      || (LL_SYMS.ll_get_objs == NULL) || (LL_SYMS.ll_get_data == NULL)
      || (LL_SYMS.ll_free_objs == NULL)
      || (LL_SYMS.ll_deallocate == NULL)
      || (LL_SYMS.ll_cluster == NULL)
      || (LL_SYMS.ll_next_obj == NULL)
      || (LL_SYMS.ll_free_job_info == NULL)
      || (LL_SYMS.ll_terminate_job == NULL)
      || (LL_SYMS.ll_error == NULL)
      || (LL_SYMS.ll_submit_job == NULL)) {
    print_message(ERROR_MESSAGE, "One or more LoadLeveler symbols could not be located in %s.\n", ibmll_libpath_name);
    sendErrorEvent(gui_transmission_id, RTEV_ERROR_LL_INIT, "LoadLeveler symbols not located");
    return PTP_PROXY_RES_ERR;
  } else {
    print_message(INFO_MESSAGE, "Successfully located all of the required LoadLeveler functions via dlsym.\n");
  }

  sendOkEvent(gui_transmission_id);

  print_message(TRACE_MESSAGE, "<<< %s returning. line=%d.\n", __FUNCTION__, __LINE__);
  return PTP_PROXY_RES_OK;
}

/************************************************************************* 
 * Proxy command handler - command_define_model                          * 
 *                                                                       * 
 * Send attributes to front end describing the launch                    * 
 * configuration (labels, default values, etc).                          * 
 *************************************************************************/
int command_define_model(int gui_transmission_id, int nargs, char *args[])
{
  int i = 0;
  char proxy_generated_attribute_string[256];
  proxy_msg *msg;
  char *cp;
  char *end_cp;
  char *cp_save;
  int n;

/* 
 * Send the attribute definitions, launch attribute definitions,
 * and element definitions known by this proxy to the GUI.
 */
  print_message(TRACE_MESSAGE, ">>> %s entered. line=%d.\n", __FUNCTION__, __LINE__);
  print_message_args(nargs, args);

    /*-----------------------------------------------------------------------* 
     * send strings                                                          * 
     *-----------------------------------------------------------------------*/
  for (i = 0; i < (sizeof(string_launch_attrs) / sizeof(string_launch_attr)); i++) {
    if (strcmp("LL_PTP_JOB_COMMAND_FILE_TEMPLATE", string_launch_attrs[i].id) == 0) {
      if (strlen(static_template_override) > 0) {
        sendAttrDefStringEvent(gui_transmission_id, string_launch_attrs[i].id, string_launch_attrs[i].short_name, string_launch_attrs[i].long_name, 0, static_template_override);
      } else {
        sendAttrDefStringEvent(gui_transmission_id, string_launch_attrs[i].id, string_launch_attrs[i].short_name, string_launch_attrs[i].long_name, 0, static_template_name);
      }
    } else {
      sendAttrDefStringEvent(gui_transmission_id, string_launch_attrs[i].id, string_launch_attrs[i].short_name, string_launch_attrs[i].long_name, 0, string_launch_attrs[i].default_value);
    }
  }

    /*-----------------------------------------------------------------------* 
     * send ints                                                             * 
     *-----------------------------------------------------------------------*/
  for (i = 0; i < (sizeof(int_attrs) / sizeof(int_launch_attr)); i++) {
    sendAttrDefIntEvent(gui_transmission_id, int_attrs[i].id, int_attrs[i].short_name, int_attrs[i].long_name, 0, int_attrs[i].default_value, int_attrs[i].llimit, int_attrs[i].ulimit);
  }

    /*-----------------------------------------------------------------------* 
     * send longs                                                            * 
     *-----------------------------------------------------------------------*/
  for (i = 0; i < (sizeof(long_int_attrs) / sizeof(long_int_launch_attr)); i++) {
    msg = new_proxy_msg(PTP_PROXY_EV_RT_ATTR_DEF, gui_transmission_id);
    proxy_msg_add_int(msg, 1);
    proxy_msg_add_int(msg, 8);
    proxy_msg_add_string(msg, long_int_attrs[i].id);
    proxy_msg_add_string(msg, "BIGINTEGER");
    proxy_msg_add_string(msg, long_int_attrs[i].short_name);
    proxy_msg_add_string(msg, long_int_attrs[i].long_name);
    proxy_msg_add_int(msg, 0);
    sprintf(proxy_generated_attribute_string, "%lld", long_int_attrs[i].default_value);
    proxy_msg_add_string(msg, proxy_generated_attribute_string);
    sprintf(proxy_generated_attribute_string, "%lld", long_int_attrs[i].llimit);
    proxy_msg_add_string(msg, proxy_generated_attribute_string);
    sprintf(proxy_generated_attribute_string, "%lld", long_int_attrs[i].ulimit);
    proxy_msg_add_string(msg, proxy_generated_attribute_string);
    enqueue_event_to_proxy_server(msg);
  }

    /*-----------------------------------------------------------------------* 
     * send enums                                                            * 
     *-----------------------------------------------------------------------*/
  for (i = 0; i < (sizeof(enum_attrs) / sizeof(enum_launch_attr)); i++) {


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
    msg = new_proxy_msg(PTP_PROXY_EV_RT_ATTR_DEF, gui_transmission_id);
    proxy_msg_add_int(msg, 1);
    proxy_msg_add_int(msg, n + 6);
    proxy_msg_add_string(msg, enum_attrs[i].id);
    proxy_msg_add_string(msg, "ENUMERATED");
    proxy_msg_add_string(msg, enum_attrs[i].short_name);
    proxy_msg_add_string(msg, enum_attrs[i].long_name);
    proxy_msg_add_int(msg, 0);
    proxy_msg_add_string(msg, enum_attrs[i].default_value);

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
      } else {
        *end_cp = '\0';
        proxy_msg_add_string(msg, cp);
        cp = end_cp + 1;
      }
    }
    free(cp_save);
  /*
   * Send the attribute definition
   */
    enqueue_event_to_proxy_server(msg);
  }

    /*-----------------------------------------------------------------------* 
     * done defining the model                                               * 
     *-----------------------------------------------------------------------*/
  sendOkEvent(gui_transmission_id);

  print_message(TRACE_MESSAGE, "<<< %s returning. line=%d.\n", __FUNCTION__, __LINE__);
  return PTP_PROXY_RES_OK;
}

/************************************************************************* 
 * Proxy command handler - command_start_events                          * 
 *                                                                       * 
 * In the LoadLeveler world a PTP machine is a cluster - so here we      * 
 *************************************************************************/
int command_start_events(int gui_transmission_id, int nargs, char *args[])
{

/* 
 * Send the complete machine state to the GUI.  Query LoadLeveler 
 * to get the set of nodes that are part of the cluster (machine) 
 * and send new node events to the GUI for each node.
 */
  print_message(TRACE_MESSAGE, ">>> %s entered. line=%d.\n", __FUNCTION__, __LINE__);
  print_message_args(nargs, args);

  state_events_active = 1;      /* events are now active */
  start_events_gui_transmission_id = gui_transmission_id;

/* Create thread to monitor LoadLeveler clusters of machines (these are nodes in a machine in ptp lingo).  */
  pthread_attr_init(&monitor_LoadLeveler_nodes_thread_attr);
  pthread_attr_setdetachstate(&monitor_LoadLeveler_nodes_thread_attr, PTHREAD_CREATE_DETACHED);
  pthread_create(&monitor_LoadLeveler_nodes_thread, &monitor_LoadLeveler_nodes_thread_attr, monitor_LoadLeveler_nodes, NULL);
  register_thread(monitor_LoadLeveler_nodes_thread);

/* Create thread to monitor LoadLeveler jobs in clusters.  */
  pthread_attr_init(&monitor_LoadLeveler_jobs_thread_attr);
  pthread_attr_setdetachstate(&monitor_LoadLeveler_jobs_thread_attr, PTHREAD_CREATE_DETACHED);
  pthread_create(&monitor_LoadLeveler_jobs_thread, &monitor_LoadLeveler_jobs_thread_attr, monitor_LoadLeveler_jobs, NULL);
  register_thread(monitor_LoadLeveler_jobs_thread);
  print_message(INFO_MESSAGE, "remapped thread id for nodes is %ul->%i and for jobs is %ul->%i\n", monitor_LoadLeveler_nodes_thread, find_thread(monitor_LoadLeveler_nodes_thread), monitor_LoadLeveler_jobs_thread, find_thread(monitor_LoadLeveler_jobs_thread));

  print_message(TRACE_MESSAGE, "<<< %s returning. line=%d.\n", __FUNCTION__, __LINE__);
  return PTP_PROXY_RES_OK;
}

/************************************************************************* 
 * Proxy command handler - command_submit_job                            * 
 *                                                                       * 
 * In the proxy we are accepting environment variables to be passed on   * 
 * to submit. These come in as env=VARNAME=VALUE                         * 
 *                                                                       * 
 * We also accept a finite number of variables to control the cmd file   * 
 * submit. This is the name of the command file and optionally a hex     * 
 * string to be written to the specified commanf file.                   * 
 *  Hex string only -> write into cmd file in tmp and submit             * 
 *  Cmd file only -> submit                                              * 
 *  Hex string and cmd file -> write into specified cmd file and submit  * 
 * It is expected that all other variables are processed in the gui and  * 
 * substituted into a template job command file.                         * 
 *************************************************************************/
int command_submit_job(int gui_transmission_id, int nargs, char *args[])
{
  int rc = 0;

  char *cp = NULL;
  char *job_sub_id = NULL;
  char *ll_job_command_file = NULL;
  char *ll_job_command_file_template = NULL;
  char *job_env_vars[1024][3];  /* allow 1024 env vars keyword, new value, old value */
  char *ll_ptp_vars[1024][2];   /* allow 1024 ll vars keyword, value */
  int i = 0;
  int k = 0;
  int o = 0;
  int match = 0;
  int env_count = 0;
  int ll_count = 0;
  char incoming[2048];
  char outgoing[2048];
  char submit_temp_file[256];
  int submit_temp_file_fd = 0;
  FILE *submit_template_FILE = NULL;
  int advanced_mode = 1;        /* preset to advanced mode */
  int bytes_written = 0;
  int myerrno = 0;
  int submit_failed = 0;        /* preset to all ok on this submit */

  print_message(TRACE_MESSAGE, ">>> %s entered. line=%d.\n", __FUNCTION__, __LINE__);
  print_message_args(nargs, args);

  memset(job_env_vars, '\0', sizeof(job_env_vars));     /* zero the area */
  memset(submit_temp_file, '\0', sizeof(submit_temp_file));     /* zero the area */

    /*-----------------------------------------------------------------------* 
     * process args passed into the submit command                           * 
     *-----------------------------------------------------------------------*/
  for (i = 0; args[i] != NULL; i++) {

      /*-----------------------------------------------------------------------* 
       * LoadLeveler keywords                                                  * 
       *-----------------------------------------------------------------------*/
    if (strncmp(args[i], "LL_PTP_JOB_COMMAND_FILE=", strlen("LL_PTP_JOB_COMMAND_FILE=")) == 0) {
      cp = strchr(args[i], '=');
      ll_job_command_file = strdup(cp + 1);
    }
    if (strncmp(args[i], "LL_PTP_JOB_COMMAND_FILE_TEMPLATE=", strlen("LL_PTP_JOB_COMMAND_FILE_TEMPLATE=")) == 0) {
      cp = strchr(args[i], '=');
      ll_job_command_file_template = strdup(cp + 1);
    }

     /*-----------------------------------------------------------------------* 
      * General PTP launch configuration variables and environment vars       * 
      *-----------------------------------------------------------------------*/
    if (strncmp(args[i], PTP_JOB_SUB_ID_ATTR "=", strlen(PTP_JOB_SUB_ID_ATTR) + 1) == 0) {
      cp = strchr(args[i], '=');
      job_sub_id = strdup(cp + 1);      /* pick up the val part of the job id */
    }

    if (strncmp(args[i], PTP_JOB_ENV_ATTR "=", strlen(PTP_JOB_ENV_ATTR) + 1) == 0) {
      cp = strchr(args[i], '=');        /* pick up the val part of env= which is a keyword=value pair */
      cp++;                     /* first char of keyword=value */
      memset(incoming, '\0', sizeof(incoming));
      strcpy(incoming, cp);     /* pick up into work area for modifications */
      cp = strchr(incoming, '=');       /* pick up the = in keyword=value pair */
      *cp = '\0';               /* make key a string by itself */
      cp++;                     /* first char of value in keyword=value pair */
      job_env_vars[env_count][0] = strdup(incoming);    /* keyword */
      job_env_vars[env_count][1] = strdup(cp);  /* new value */
      env_count++;
    }

     /*-----------------------------------------------------------------------* 
      * Substitution variables (like LL_PTP_xxxxxxxxx)                        * 
      *-----------------------------------------------------------------------*/
    if ((strncmp(args[i], "LL_PTP_", strlen("LL_PTP_")) == 0) || (strncmp(args[i], "workingDir", strlen("workingDir")) == 0) || (strncmp(args[i], "progArgs", strlen("progArgs")) == 0) || (strncmp(args[i], "execName", strlen("execName")) == 0) || (strncmp(args[i], "execPath", strlen("execPath")) == 0)
      ) {
      memset(incoming, '\0', sizeof(incoming));
      strcpy(incoming, args[i]);        /* pick up into work area for modifications */
      cp = strchr(incoming, '=');       /* pick up the = in keyword=value pair */
      *cp = '\0';               /* make key a string by itself */
      ll_ptp_vars[ll_count][0] = strdup(incoming);      /* pick up the keyword */
      cp++;                     /* first char of value in keyword=value pair */
      ll_ptp_vars[ll_count][1] = strdup(cp);    /* pick up the value */
      print_message(INFO_MESSAGE, "Received keyword[%d]=value %s=%s\n", ll_count, ll_ptp_vars[ll_count][0], ll_ptp_vars[ll_count][1]);
      if (strcmp(ll_ptp_vars[ll_count][0], "LL_PTP_SUBMIT_MODE") == 0) {
        if (strcmp(ll_ptp_vars[ll_count][1], "Basic") == 0) {
          advanced_mode = 0;    /* no longer advanced mode - now we are basic */
        }
      }
      ll_count++;               /* count it */
    }

  }                             /* end loop on args passed into submit command */

    /*-----------------------------------------------------------------------* 
     * Handle overrides of LL_PTP_xxxxx variables over  PTP variables        * 
     * LL_PTP_INITIALDIR overrides workingDir                                *
     * LL_PTP_EXECUTABLE overrides execPath/execName                         *
     *-----------------------------------------------------------------------*/
  for (k = 0; k < ll_count; k++) {      /* loop on keyword value pairs */
    if (strncmp("LL_PTP_EXECUTABLE", ll_ptp_vars[k][0], strlen(ll_ptp_vars[k][0])) == 0) {      /* match so far */
      for (i = 0; i < ll_count; i++) {  /* loop on keyword value pairs */
        if (strncmp("execPath", ll_ptp_vars[i][0], strlen(ll_ptp_vars[i][0])) == 0) {
          free(ll_ptp_vars[i][0]);
          ll_ptp_vars[i][0] = strdup(" ");
        }
        if (strncmp("execName", ll_ptp_vars[i][0], strlen(ll_ptp_vars[i][0])) == 0) {
          free(ll_ptp_vars[i][0]);
          ll_ptp_vars[i][0] = strdup(" ");
        }
      }
    }
    if (strncmp("LL_PTP_INITIALDIR", ll_ptp_vars[k][0], strlen(ll_ptp_vars[k][0])) == 0) {      /* match so far */
      for (i = 0; i < ll_count; i++) {  /* loop on keyword value pairs */
        if (strncmp("workingDir", ll_ptp_vars[i][0], strlen(ll_ptp_vars[i][0])) == 0) {
          free(ll_ptp_vars[i][0]);
          ll_ptp_vars[i][0] = strdup(" ");
        }
      }
    }
  }                             /* end loop on keyword value pairs */


     /*-----------------------------------------------------------------------* 
      * Perform substitutions and do submit                                   * 
      *-----------------------------------------------------------------------*/
  if (advanced_mode == 1) {     /* if advanced mode submit - no substitutions */
    rc = my_ll_submit_job(gui_transmission_id, job_sub_id, ll_job_command_file, job_env_vars);
    if (rc == 0) {              /* if job submit successful */
      print_message(INFO_MESSAGE, "Job %s submitted.\n", ll_job_command_file);
    } /* end if job submit successful */
    else {                      /* if job submit error */
      print_message(ERROR_MESSAGE, "Job submit failed for job %s.\n", ll_job_command_file);
    }                           /* end if job submit error */
  } /* end if advanced mode submit - no substitutions */
  else {                        /* if basic mode submit -  substitutions to be performed */
    /*-----------------------------------------------------------------------* 
     * Open a temp file for submitting on /tmp                               * 
     *-----------------------------------------------------------------------*/
    strcpy(submit_temp_file, "/tmp/ll_ptp_temp_submit_file_");
    strcat(submit_temp_file, userid);
    strcat(submit_temp_file, "_XXXXXX");
    submit_temp_file_fd = mkstemp(submit_temp_file);

    /*-----------------------------------------------------------------------* 
     * Open template file                                                    * 
     *-----------------------------------------------------------------------*/
    submit_template_FILE = fopen(ll_job_command_file_template, "r");
    if (submit_template_FILE == NULL) {
      myerrno = errno;
      print_message(ERROR_MESSAGE, "Unable to open submit template file %s, errno=%d\n", ll_job_command_file_template, myerrno);
      submit_failed = 1;        /* stop procesing the submit */
    }

    /*-----------------------------------------------------------------------* 
     * Read in from template and substitute in key=value, if line contains   * 
     * any non substituted then don't write the line back out                * 
     *-----------------------------------------------------------------------*/
    if (submit_failed == 0) {   /* if still ok to process */
      memset(incoming, '\0', sizeof(incoming)); /* zero the area */
      while ((fgets(incoming, sizeof(incoming), submit_template_FILE)) != NULL) {       /* read line into incoming */
        o = 0;                  /* outgoing line position */
        memset(outgoing, '\0', sizeof(outgoing));       /* zero the outgoing area */

        for (i = 0; i < strlen(incoming);) {    /* loop on incoming line */
          match = 0;
          if (strlen(&incoming[i]) >= 6) {      /* if line left is >= 6 */
            if (strncmp(&incoming[i], "<<<", 3) == 0) { /* maybe a match */
              for (k = 0; k < ll_count; k++) {  /* loop on keyword value pairs */
                if (strncmp(&incoming[i + 3], ll_ptp_vars[k][0], strlen(ll_ptp_vars[k][0])) == 0) {     /* match so far */
                  if (strncmp(&incoming[i + 3 + strlen(ll_ptp_vars[k][0])], ">>>", 3) == 0) {   /* it is a complete match ! */
                    strncpy(&outgoing[o], ll_ptp_vars[k][1], strlen(ll_ptp_vars[k][1]));
                    i = i + 6 + strlen(ll_ptp_vars[k][0]);      /* next char in incoming line */
                    o = o + strlen(ll_ptp_vars[k][1]);  /* next char in outgoing line */
                    match = 1;  /* we match on this string in incoming */
                    break;
                  }             /* end if it is a complete match */
                }               /* end if match */
              }                 /* end loop on keyword value pairs */
            }                   /* end if maybe a match */
          }                     /* end if line left is >= 7 */
          if (match == 0) {
            outgoing[o++] = incoming[i++];      /* copy 1 byte over to new line */
          }

        }                       /* end loop on incoming line */

        if (is_substitution_required(outgoing) == 0) {  /* if there are not any <<<XXXXXXXX>>> variables */
          bytes_written = write(submit_temp_file_fd, outgoing, strlen(outgoing));
        }
      /* end loop on keyword value pairs */
        myerrno = errno;
        memset(incoming, '\0', sizeof(incoming));       /* zero the area for next template read */
      }
    }                           /* end if still ok to process */
    if (submit_temp_file_fd > 0) {
      close(submit_temp_file_fd);
    }
    if (submit_template_FILE != NULL) {
      fclose(submit_template_FILE);
    }

    if (submit_failed == 0) {   /* if still ok to process */
    /*-----------------------------------------------------------------------* 
     * Submit the file                                                       * 
     *-----------------------------------------------------------------------*/
      rc = my_ll_submit_job(gui_transmission_id, job_sub_id, submit_temp_file, job_env_vars);
      if (rc == 0) {            /* if job submit successful */
        print_message(INFO_MESSAGE, "Job %s submitted.\n", submit_temp_file);
      } /* end if job submit successful */
      else {                    /* if job submit error */
        print_message(ERROR_MESSAGE, "Job submit failed for job %s.\n", submit_temp_file);
        submit_failed = 1; /* flag as failed */
      }                         /* end if job submit error */

    /*-----------------------------------------------------------------------* 
     * Delete the temporary file                                             * 
     *-----------------------------------------------------------------------*/
      remove(submit_temp_file); /* delete the tmp file */
    }          /* end if still ok to process */
    
    /*-----------------------------------------------------------------------* 
     * Cleanup after submit                                                  * 
     *-----------------------------------------------------------------------*/
    for (i = 0; i < ll_count; i++) {
      free(ll_ptp_vars[i][0]);
      ll_ptp_vars[i][0] = NULL;
      free(ll_ptp_vars[i][1]);
      ll_ptp_vars[i][1] = NULL;
    }
    if (ll_job_command_file_template != NULL) {
      free(ll_job_command_file_template);
      ll_job_command_file_template = NULL;
    }

    if (ll_job_command_file != NULL) {
      free(ll_job_command_file);
      ll_job_command_file = NULL;
    }
  }                             /* end if basic mode */

  if (submit_failed == 0) {
    sendOkEvent(gui_transmission_id);     /* close out the halt events command */
  }
  else {
	sendJobSubmissionErrorEvent(gui_transmission_id, job_sub_id, "LoadLeveler job submit failed.");
  }
  
  print_message(TRACE_MESSAGE, "<<< %s returning. line=%d.\n", __FUNCTION__, __LINE__);
  return PTP_PROXY_RES_OK;
}

/************************************************************************* 
 * Proxy command handler - command_halt_events                           * 
 *************************************************************************/
int command_halt_events(int gui_transmission_id, int nargs, char *args[])
{
  print_message(TRACE_MESSAGE, ">>> %s entered. line=%d.\n", __FUNCTION__, __LINE__);
  print_message_args(nargs, args);

  sendOkEvent(start_events_gui_transmission_id);        /* close out the start events command (kept open for async) */
  state_events_active = 0;      /* events are now inactive */
  sendOkEvent(gui_transmission_id);     /* close out the halt events command */
  print_message(TRACE_MESSAGE, "<<< %s returning. line=%d.\n", __FUNCTION__, __LINE__);
  return PTP_PROXY_RES_OK;
}

/************************************************************************* 
 * Proxy command handler - command_cancel_job                            * 
 *************************************************************************/
int command_cancel_job(int gui_transmission_id, int nargs, char *args[])
{
  int i;
  int job_ident = -1;
  JobObject *job_object = NULL;
  print_message(TRACE_MESSAGE, ">>> %s entered. line=%d.\n", __FUNCTION__, __LINE__);
  print_message_args(nargs, args);

  for (i = 0; i < nargs; i++) {
    if (proxy_test_attribute(PTP_JOB_ID_ATTR, args[i])) {
	  job_ident = proxy_get_attribute_value_int(args[i]);
    }
  }
  job_object = get_job_in_list_from_id(job_list, job_ident);
  if (job_object != NULL) {
    my_ll_terminate_job(gui_transmission_id, job_object);
  }

  sendOkEvent(gui_transmission_id);
  print_message(TRACE_MESSAGE, "<<< %s returning. line=%d.\n", __FUNCTION__, __LINE__);
  return 0;
}

/************************************************************************* 
 * Proxy command handler - command_terminate                             * 
 *************************************************************************/
int command_terminate(int gui_transmission_id, int nargs, char *args[])
{
  int rc = 0;
  print_message(TRACE_MESSAGE, ">>> %s entered. line=%d.\n", __FUNCTION__, __LINE__);
  print_message_args(nargs, args);
  state_shutdown_requested = 1;

  print_message(INFO_MESSAGE, "Waiting for monitor threads to become inactve.\n");
  pthread_mutex_lock(&master_lock);
  print_message(INFO_MESSAGE, "Continuing termination request.\n");
  print_message(INFO_MESSAGE, "dlclose LoadLeveler shared library %s.\n", ibmll_libpath_name);
  rc = dlclose(ibmll_libpath_handle);
  if (rc != 0) {
    print_message(ERROR_MESSAGE, "dlclose of %s failed with rc=%d.\n", rc);
    sendErrorEvent(gui_transmission_id, RTEV_ERROR_LL_INIT, "dlclose failed for LoadLeveler shared library");
    sendShutdownEvent(gui_transmission_id);
    print_message(TRACE_MESSAGE, "<<< %s returning. line=%d.\n", __FUNCTION__, __LINE__);
    pthread_mutex_unlock(&master_lock);
    return PTP_PROXY_RES_ERR;
  } else {
    print_message(INFO_MESSAGE, "dlclose %s successful.\n", ibmll_libpath_name);
    sendShutdownEvent(gui_transmission_id);
    print_message(TRACE_MESSAGE, "<<< %s returning. line=%d.\n", __FUNCTION__, __LINE__);
    pthread_mutex_unlock(&master_lock);
    return PTP_PROXY_RES_OK;
  }
  pthread_mutex_unlock(&master_lock);
}


int command_suspend_events(int gui_transmission_id, int nargs, char *args[])
{
    proxy_set_flow_control(1);
    return PTP_PROXY_RES_OK;
}

int command_resume_events(int gui_transmission_id, int nargs, char *args[])
{
    proxy_set_flow_control(0);
    return PTP_PROXY_RES_OK;
}

int command_set_filters(int gui_transmission_id, int nargs, char *args[])
{
    return PTP_PROXY_RES_OK;
}

int command_clear_filters(int gui_transmission_id, int nargs, char *args[])
{
    return PTP_PROXY_RES_OK;
}

int command_get_attributes(int gui_transmission_id, int nargs, char *args[])
{
    return PTP_PROXY_RES_OK;
}

int command_query_attributes(int gui_transmission_id, int nargs, char *args[])
{
    return PTP_PROXY_RES_OK;
}

/************************************************************************* 
 * Service thread - Loop while allowed to monitor LoadLeveler for nodes  * 
 *************************************************************************/
void *monitor_LoadLeveler_nodes(void *job_ident)
{
  int rc = 0;
  int i = 0;
  char *node_name = NULL;
  LL_element *node = NULL;
  LL_element *query_elem = NULL;
  int node_count = 0;
  int sleep_seconds = 30;
  int sleep_time_reset = 0;     /* if changes this pass */
  LL_element *errObj = NULL;

  ListElement *cluster_list_element = NULL;
  ClusterObject *cluster_object = NULL;
  NodeObject *node_object = NULL;

  print_message(TRACE_MESSAGE, ">>> %s entered. line=%d.\n", __FUNCTION__, __LINE__);

  /*-----------------------------------------------------------------------* 
   * loop forever until we are told we are shutting down.                  * 
   *-----------------------------------------------------------------------*/
  while (state_shutdown_requested == 0) {       /* loop while not shutting down */
    pthread_mutex_lock(&master_lock);
    if (state_shutdown_requested == 1) {        /* if main task started shutdown and snuck in at the right time */
      pthread_mutex_unlock(&master_lock);
      break;
    }
    query_elem = NULL;
    node_name = NULL;
    node_count = 0;
    node = NULL;
    LL_cluster_param cluster_parm;
    char *remote_cluster[2];
    Hash *node_hash = NULL;
    HashEntry *hash_element = NULL;
    List *node_list = NULL;
    ListElement *node_list_element = NULL;
    sleep_time_reset = 0;       /* preset for this loop that no changes were detected */

    print_message(TRACE_MESSAGE, ">>> %s thread running. line=%d.\n", __FUNCTION__, __LINE__);
    if (cluster_list == NULL) {
      refresh_cluster_list();   /* insure we have latest cluster configuration from LoadLeveler */
    }

    if (cluster_list != NULL) { /* if we have clusters, nodes, etc. */

      /*-----------------------------------------------------------------------* 
       * loop on the cluster list we obtained earlier from LoadLeveler.        * 
       *-----------------------------------------------------------------------*/
      cluster_list_element = cluster_list->l_head;
      while (cluster_list_element != NULL) {    /* while we have a cluster to query */
        cluster_object = cluster_list_element->l_value; /* get our cluster object from this list element */
        cluster_list_element = cluster_list_element->l_next;    /* prepare for next pass */
        if (cluster_object->node_hash->count <= 0) {    /* if no nodes in this cluster */
          sleep_time_reset = 1; /* we need to keep looking for nodes, etc on short interval */
        }

        if (multicluster_status == 1) { /* if running multicluster */

         /*-----------------------------------------------------------------------* 
          * we are running multicluster - set cluster name into environment       * 
          * to influence where LoadLeveler searches for data (what cluster)       * 
          *-----------------------------------------------------------------------*/
          remote_cluster[0] = cluster_object->cluster_name;
          remote_cluster[1] = NULL;
          print_message(INFO_MESSAGE, "Setting access for LoadLeveler cluster=%s.\n", cluster_object->cluster_name);
          cluster_parm.action = CLUSTER_SET;    /* we are setting the cluster for remote access */
          cluster_parm.cluster_list = remote_cluster;   /* cluster name we want data from */
          rc = my_ll_cluster(LL_API_VERSION, &errObj, &cluster_parm);   /* set the cluster name */
        } /* end if this is not a local cluster */
        else {                  /* this is a local cluster */

         /*-----------------------------------------------------------------------* 
          * not running multicluster                                              * 
          *-----------------------------------------------------------------------*/
          print_message(INFO_MESSAGE, "Setting access for LoadLeveler local cluster (single cluster).\n");
          cluster_parm.action = CLUSTER_UNSET;  /* we are unsetting the cluster */
          cluster_parm.cluster_list = NULL;
          rc = my_ll_cluster(LL_API_VERSION, &errObj, &cluster_parm);   /* unset the cluster name - back to local */
        }                       /* end if this is a local cluster */

        /*-----------------------------------------------------------------------* 
         * build a LoadLeveler query object (for nodes)                          * 
         *-----------------------------------------------------------------------*/
        query_elem = my_ll_query(MACHINES);
        if (query_elem == NULL) {
          print_message(ERROR_MESSAGE, "Unable to obtain query element. LoadLeveler may not be active or is not responding.\n");
          continue;
        }

        /*-----------------------------------------------------------------------* 
         * set the request type for LoadLeveler (we want nodes)                  * 
         *-----------------------------------------------------------------------*/
        print_message(INFO_MESSAGE, "Call LoadLeveler (ll_set_request) for nodes in cluster=%s.\n", cluster_object->cluster_name);
        rc = my_ll_set_request(query_elem, QUERY_ALL, NULL, ALL_DATA);
        if (rc != 0) {
          rc = my_ll_deallocate(query_elem);
          query_elem = NULL;
          continue;
        }

        /*-----------------------------------------------------------------------* 
         * get nodes from LoadLeveler for current or local cluster.              * 
         *-----------------------------------------------------------------------*/
        print_message(INFO_MESSAGE, "Call LoadLeveler (ll_get_objs) for nodes in cluster=%s.\n", cluster_object->cluster_name);
        node = my_ll_get_objs(query_elem, LL_CM, NULL, &node_count, &rc);
        if (rc != 0) {
          rc = my_ll_deallocate(query_elem);
          query_elem = NULL;
          continue;
        }

        print_message(INFO_MESSAGE, "Number of LoadLeveler Nodes=%d in cluster=%s.\n", node_count, cluster_object->cluster_name);

        /*-----------------------------------------------------------------------* 
         * loop on the nodes returned by LoadLeveler                             * 
         *-----------------------------------------------------------------------*/
        i = 0;
        while (node != NULL) {  /* node points to the first or next node in the list from LoadLeveler */
          print_message(INFO_MESSAGE, "LoadLeveler Node %d:\n", i);
          rc = my_ll_get_data(node, LL_MachineName, &node_name);
          if (rc == 0) { /* do something here with the node object */
            print_message(INFO_MESSAGE, "Node name=%s\n", node_name);
            if ((node_object = get_node_in_hash(cluster_object->node_hash, node_name)) != NULL) {       /* existing node */
              /*-----------------------------------------------------------------------* 
               * node returned by LoadLeveler was found in our ptp node list.          * 
               * flag it as found.                                                     * 
               *-----------------------------------------------------------------------*/
              node_object->node_found = 1;      /* flag it as found */
              if (node_object->node_state != MY_STATE_UP) {
                node_object->node_state = MY_STATE_UP;
                sleep_time_reset = 1;   /* we have a change this pass */
                print_message(INFO_MESSAGE, "Schedule event notification: node=%s changed for LoadLeveler Cluster=%s.\n", node_name, cluster_object->cluster_name);
                sendNodeChangeEvent(start_events_gui_transmission_id, cluster_object, node_object);
              }
            } else {            /* new node (not yet in list) */
              /*-----------------------------------------------------------------------* 
               * node returned by LoadLeveler was not found in our ptp node list       * 
               * add it and generate an event to the gui. flag it as added.            * 
               *-----------------------------------------------------------------------*/
              node_object = (NodeObject *) malloc(sizeof(NodeObject));
              malloc_check(node_object, __FUNCTION__, __LINE__);
              memset(node_object, '\0', sizeof(node_object));   /* zero the malloc area */
              node_object->proxy_generated_node_id = generate_id();     /* a unique identifier for this cluster */
              node_object->node_name = strdup(node_name);
              node_object->node_found = 2;      /* flag it as added */
              node_object->node_state = MY_STATE_UP;
              sleep_time_reset = 1;     /* we have a change this pass */
              add_node_to_hash(cluster_object->node_hash, (void *) node_object);        /* add the new node object to the hash */
              sleep_time_reset = 1;     /* we have a change this pass */
              print_message(INFO_MESSAGE, "Schedule event notification: node=%s added for LoadLeveler Cluster=%s.\n", node_name, cluster_object->cluster_name);
              sendNodeAddEvent(start_events_gui_transmission_id, cluster_object, node_object);
            }                   /* end if new node */
          }                     /* we got a node name ok */

          i++;
          node = my_ll_next_obj(query_elem);
        }                       /* end while we have a node */

        /*-----------------------------------------------------------------------* 
         * loop on the ptp node list to see if any nodes were not returned       * 
         * by LoadLeveler on this pass (maybe they went down).                   * 
         * generate an event (changed/gone) to the gui.                          * 
         *-----------------------------------------------------------------------*/
        node_hash = cluster_object->node_hash;  /* pick up the node hash */
        if (node_hash != NULL) {        /* if we have a node hash table ok */
          HashSet(node_hash);   /* position to beginning of the hash table */
          hash_element = HashGet(node_hash);    /* get next hash table entry */
          while (hash_element != NULL) {
            node_list = (List *) hash_element->h_data;  /* the hash entry is a list of node objects */
            hash_element = HashGet(node_hash);  /* get next hash table entry */
            node_list_element = node_list->l_head;      /* first in list */
            while (node_list_element != NULL) { /* while we have a node to query */
              node_object = node_list_element->l_value; /* get our node object from this list element */
              node_list_element = node_list_element->l_next;    /* prepare for next pass */
              if (node_object->node_found == 0) {       /* if not found - node went away */
                if (node_object->node_state != MY_STATE_UNKNOWN) {
                  node_object->node_state = MY_STATE_UNKNOWN;
                  print_message(INFO_MESSAGE, "Schedule event notification: node=%s changed for LoadLeveler Cluster=%s.\n", node_name, cluster_object->cluster_name);
                  sendNodeChangeEvent(start_events_gui_transmission_id, cluster_object, node_object);
                  sleep_time_reset = 1; /* we have a change this pass */
                }
              } else {          /* node was found or added this pass */
                node_object->node_found = 0;    /* reset for next pass */
              }
            }                   /* end while we have node list element to look at */

          }                     /* end while we still have hash table entry */
        }                       /* end if we have a node hash table ok */
        if (query_elem != NULL) {
          rc = my_ll_free_objs(query_elem);
          rc = my_ll_deallocate(query_elem);
          query_elem = NULL;
        }

      }                         /* end while we have a cluster to query */
    } /* end if we have cluster list, nodes, etc. */
    else {
      sleep_time_reset = 1;     /* we need to keep looking for nodes, etc */
    }

    pthread_mutex_unlock(&master_lock);

    /*-----------------------------------------------------------------------* 
     * adjust sleep interval based on changes this pass.                     * 
     *-----------------------------------------------------------------------*/
    if (sleep_time_reset == 1) {        /* yes - there was activity */
      sleep_seconds = min_node_sleep_seconds;   /* set to min sleep interval */
    } /* end if activity this pass */
    else {                      /* if no activity this pass */
      sleep_seconds = sleep_seconds + min_node_sleep_seconds;
      if (sleep_seconds > max_node_sleep_seconds) {     /* if max exceeded */
        sleep_seconds = max_node_sleep_seconds; /* reset to max sleep interval */
      }                         /* end if max exceeded */
    }                           /* end if no activity this pass */

    /*-----------------------------------------------------------------------* 
     * sleep and loop again on the LoadLeveler machines.                     * 
     *-----------------------------------------------------------------------*/
    if (state_shutdown_requested == 0) {
      int sleep_interval = 0;
      int mini_sleep_interval = (sleep_seconds + 4) / 5;
      print_message(INFO_MESSAGE, "%s Sleeping for (%d seconds) %d intervals of 5 seconds\n", __FUNCTION__, mini_sleep_interval * 5, mini_sleep_interval);
      for (sleep_interval = 0; sleep_interval < mini_sleep_interval; sleep_interval++) {
        if (state_shutdown_requested == 0) {
          sleep(5);
        }
      }
    }

  }                             /* end while we are not shutting down */

  print_message(TRACE_MESSAGE, "<<< %s returning. line=%d.\n", __FUNCTION__, __LINE__);
  return NULL;
}

/************************************************************************* 
 * Service thread - Loop while allowed to monitor LoadLeveler for jobs   * 
 *************************************************************************/
void *monitor_LoadLeveler_jobs(void *job_ident)
{
  int rc = 0;
  int i = 0;
  char *job_name = NULL;
  char *job_submit_host = NULL;
  char *step_ID = NULL;
  LL_STEP_ID ll_step_id;
  int step_machine_count = 0;
  LL_element *job = NULL;
  LL_element *step = NULL;
  LL_element *query_elem = NULL;
  LL_element *node = NULL;
  LL_element *task = NULL;
  LL_element *task_instance = NULL;
  int job_count = 0;
  LL_element *errObj = NULL;
  LL_cluster_param cluster_parm;
  char *remote_cluster[2];
  ListElement *job_list_element = NULL;
  List *task_list = NULL;
  ListElement *task_list_element = NULL;
  char *task_instance_machine_name = NULL;
  char *task_instance_machine_address = NULL;
  int task_instance_task_ID = 0;
  int step_node_count = 0;
  int node_task_count = 0;
  int task_instance_count = 0;
  time_t my_clock;

  ListElement *cluster_list_element = NULL;
  ClusterObject *cluster_object = NULL;
  JobObject *job_object = NULL;
  TaskObject *task_object = NULL;

  print_message(TRACE_MESSAGE, ">>> %s entered. line=%d.\n", __FUNCTION__, __LINE__);

  /*-----------------------------------------------------------------------* 
   * loop forever until we are told we are shutting down.                  * 
   *-----------------------------------------------------------------------*/
  while (state_shutdown_requested == 0) {       /* loop while not shutting down */
    pthread_mutex_lock(&master_lock);
    if (state_shutdown_requested == 1) {        /* if main task started shutdown and snuck in at the right time */
      pthread_mutex_unlock(&master_lock);
      break;
    }
    query_elem = NULL;
    job_name = NULL;
    step_ID = NULL;
    job_count = 0;
    step_machine_count = 0;
    job = NULL;
    char *pChar = NULL;

    print_message(TRACE_MESSAGE, ">>> %s thread running. line=%d.\n", __FUNCTION__, __LINE__);

    if (cluster_list != NULL) { /* if we have clusters, nodes, etc. from monitor_LoadLeveler_nodes thread */

      /*-----------------------------------------------------------------------* 
       * loop on the cluster list we obtained earlier from LoadLeveler.        * 
       *-----------------------------------------------------------------------*/
      cluster_list_element = cluster_list->l_head;
      while (cluster_list_element != NULL) {    /* while we have a cluster to query */
        cluster_object = cluster_list_element->l_value; /* get our cluster object from this list element */
        cluster_list_element = cluster_list_element->l_next;    /* prepare for next pass */

        if (cluster_object != NULL) {   /* if we have a cluster object */
          if (cluster_object->node_hash != NULL) {      /* if we have a node has table to look at */
            if (cluster_object->node_hash->count > 0) { /* if we have nodes in this cluster */

              if (multicluster_status == 1) {   /* if running multicluster */

         /*-----------------------------------------------------------------------* 
          * we are running multicluster - set cluster name into environment       * 
          * to influence where LoadLeveler searches for data (what cluster)       * 
          *-----------------------------------------------------------------------*/
                remote_cluster[0] = cluster_object->cluster_name;
                remote_cluster[1] = NULL;
                print_message(INFO_MESSAGE, "Setting access for LoadLeveler cluster=%s.\n", cluster_object->cluster_name);
                cluster_parm.action = CLUSTER_SET;      /* we are setting the cluster for remote access */
                cluster_parm.cluster_list = remote_cluster;     /* cluster name we want data from */
                rc = my_ll_cluster(LL_API_VERSION, &errObj, &cluster_parm);     /* set the cluster name */
              } /* end if this is not a local cluster */
              else {            /* this is a local cluster */

         /*-----------------------------------------------------------------------* 
          * not running multicluster                                              * 
          *-----------------------------------------------------------------------*/
                print_message(INFO_MESSAGE, "Setting access for LoadLeveler local cluster (single cluster).\n");
                cluster_parm.action = CLUSTER_UNSET;    /* we are unsetting the cluster */
                cluster_parm.cluster_list = NULL;
                rc = my_ll_cluster(LL_API_VERSION, &errObj, &cluster_parm);     /* unset the cluster name - back to local */
              }                 /* end if this is a local cluster */

        /*-----------------------------------------------------------------------* 
         * build a LoadLeveler query object (for jobs)                           * 
         *-----------------------------------------------------------------------*/
              query_elem = my_ll_query(JOBS);
              if (query_elem == NULL) {
                print_message(ERROR_MESSAGE, "Unable to obtain query element. LoadLeveler may not be active or is not responding.\n");
                continue;
              }

        /*-----------------------------------------------------------------------* 
         * set the request type for LoadLeveler (we want nodes)                  * 
         *-----------------------------------------------------------------------*/
              print_message(INFO_MESSAGE, "Call LoadLeveler (ll_set_request) for jobs in cluster=%s.\n", cluster_object->cluster_name);
              rc = my_ll_set_request(query_elem, QUERY_ALL, NULL, ALL_DATA);
              if (rc != 0) {
                rc = my_ll_deallocate(query_elem);
                query_elem = NULL;
                continue;
              }

        /*-----------------------------------------------------------------------* 
         * get jobs from LoadLeveler for current or local cluster.               * 
         *-----------------------------------------------------------------------*/
              print_message(INFO_MESSAGE, "Call LoadLeveler (ll_get_objs) for jobs in cluster=%s.\n", cluster_object->cluster_name);
              job_count = 0;    /* preset to no jobs found in cluster */
              job = my_ll_get_objs(query_elem, LL_CM, NULL, &job_count, &rc);
              if (rc != 0) {
                rc = my_ll_deallocate(query_elem);
                query_elem = NULL;
              }

              print_message(INFO_MESSAGE, "Number of LoadLeveler Jobs=%d in cluster=%s.\n", job_count, cluster_object->cluster_name);

        /*-----------------------------------------------------------------------* 
         * loop on the jobs returned by LoadLeveler                              * 
         *-----------------------------------------------------------------------*/
              i = 0;
              while (job != NULL) {     /* job points to the first or next job in the list from LoadLeveler */
                print_message(INFO_MESSAGE, "LoadLeveler Job %d:\n", i);
                rc = my_ll_get_data(job, LL_JobSubmitHost, &job_submit_host);
                rc = my_ll_get_data(job, LL_JobName, &job_name);
                if (rc == 0) { /* do something here with the job object */
                  print_message(INFO_MESSAGE, "Job name=%s\n", job_name);
                  rc = my_ll_get_data(job, LL_JobGetFirstStep, &step);  /* get a job step */
                  while (step != NULL) {        /* while we have a step */
                    step_machine_count = 0;     /* pre assume job not running */
                    rc = my_ll_get_data(step, LL_StepID, &step_ID);
                    if (rc == 0) {    /* do something here with the step object */
               /*-----------------------------------------------------------------------* 
                * break the job step name apart into a LoadLeveler LL_STEP_ID           * 
                *-----------------------------------------------------------------------*/
                      ll_step_id.from_host = strdup(job_submit_host);
                      pChar = step_ID + strlen(job_submit_host) + 1;    /* The next segment is the cluster or job number */
                      pChar = strtok(pChar, ".");
                      ll_step_id.cluster = atoi(pChar); /* The last token is the proc or step number */
                      pChar = strtok(NULL, ".");
                      ll_step_id.proc = atoi(pChar);

                      print_message(INFO_MESSAGE, "Job step ID=%s.%d.%d\n", ll_step_id.from_host, ll_step_id.cluster, ll_step_id.proc);
                      if ((job_object = get_job_in_list(job_list, ll_step_id)) != NULL) {       /* existing step */

              /*-----------------------------------------------------------------------* 
               * step returned by LoadLeveler was found in our ptp job list.           * 
               * flag it as found.                                                     * 
               *-----------------------------------------------------------------------*/
                        job_object->job_found = 1;      /* flag it as found */
                        if (job_object->job_state == MY_STATE_UNKNOWN) {
                          job_object->job_state = MY_STATE_IDLE;
                          print_message(INFO_MESSAGE, "Schedule event notification: Job=%s.%d.%d changed for LoadLeveler Cluster=%s.\n", job_object->ll_step_id.from_host, job_object->ll_step_id.cluster, job_object->ll_step_id.proc, job_object->cluster_name);
                          sendJobChangeEvent(start_events_gui_transmission_id, job_object);
                        }
                        if (multicluster_status == 1) { /* if running multicluster */
                          if (strcmp(job_object->cluster_name, cluster_object->cluster_name) != 0) {    /* if job moved */
                            sendJobRemoveEvent(start_events_gui_transmission_id, job_object);
                            job_object->cluster_name = cluster_object->cluster_name;    /* pick up new cluster */
                            sendJobAddEvent(start_events_gui_transmission_id, cluster_object, job_object);
                          }     /* if job moved */
                        }       /* end if running multicluster */
                      } else {  /* new job (not yet in list) */

              /*-----------------------------------------------------------------------* 
               * job returned by LoadLeveler was not found in our ptp job list         * 
               * add it and generate an event to the gui. flag it as added.            * 
               *-----------------------------------------------------------------------*/
                        job_object = (JobObject *) malloc(sizeof(JobObject));
                        malloc_check(job_object, __FUNCTION__, __LINE__);
                        memset(job_object, '\0', sizeof(job_object));   /* zero the malloc area */
                        job_object->proxy_generated_job_id = generate_id();     /* a unique identifier for this cluster */
			job_object->task_counter = 0;
                        job_object->gui_assigned_job_id = "-1"; /* unsolicited job from proxy */
                        job_object->ll_step_id.from_host = strdup(ll_step_id.from_host);
                        job_object->ll_step_id.cluster = ll_step_id.cluster;
                        job_object->ll_step_id.proc = ll_step_id.proc;
                        job_object->job_found = 2;      /* flag it as newly added */
                        job_object->job_state = MY_STATE_IDLE;
                        job_object->task_list = NewList();      /* list to hold tasks for this job step */
                        job_object->cluster_name = strdup(cluster_object->cluster_name);
                        add_job_to_list(job_list, (void *) job_object); /* add the new job object to the list */
                        print_message(INFO_MESSAGE, "Schedule event notification: Job=%s.%d.%d added for LoadLeveler Cluster=%s.\n", job_object->ll_step_id.from_host, job_object->ll_step_id.cluster, job_object->ll_step_id.proc, job_object->cluster_name);
                        sendJobAddEvent(start_events_gui_transmission_id, cluster_object, job_object);
                      }         /* end if new node */
                      rc = my_ll_get_data(step, LL_StepNodeCount, &step_node_count);    /* nodes running on */
                      print_message(INFO_MESSAGE, "Step=%s.%d.%d. StepNodeCount=%d.\n", job_object->ll_step_id.from_host, job_object->ll_step_id.cluster, job_object->ll_step_id.proc, step_node_count);

                /*-----------------------------------------------------------------------* 
                 * if this job from LoadLeveler has nodes (is running-like) then loop on *
                 * the nodes to see task status for new or existing tasks.               * 
                 *-----------------------------------------------------------------------*/
                      if (step_node_count > 0) {        /* if job step is in running-like state (is using nodes) */
                        rc = my_ll_get_data(step, LL_StepGetFirstNode, &node);  /* node */

                  /*-----------------------------------------------------------------------* 
                   * loop on the nodes in the job returned by LoadLeveler                  * 
                   *-----------------------------------------------------------------------*/
                        while (node != NULL) {  /* while we have a valid LoadLeveler node */
                          rc = my_ll_get_data(node, LL_NodeTaskCount, &node_task_count);        /* tasks running on node */
                          print_message(INFO_MESSAGE, "NodeTaskCount=%d.\n", node_task_count);
                          rc = my_ll_get_data(node, LL_NodeGetFirstTask, &task);        /* task */

                  /*-----------------------------------------------------------------------* 
                   * loop on the tasks in the job returned by LoadLeveler                  * 
                   *-----------------------------------------------------------------------*/
                          while (task != NULL) {
                            rc = my_ll_get_data(task, LL_TaskTaskInstanceCount, &task_instance_count);  /* task instances */
                            print_message(INFO_MESSAGE, "TaskInstanceCount=%d.\n", task_instance_count);
                            rc = my_ll_get_data(task, LL_TaskGetFirstTaskInstance, &task_instance);     /* task instances */

                      /*-----------------------------------------------------------------------* 
                       * loop on the task_instances in the job returned by LoadLeveler         * 
                       *-----------------------------------------------------------------------*/
                            while (task_instance != NULL) {
                              rc = my_ll_get_data(task_instance, LL_TaskInstanceMachineName, &task_instance_machine_name);      /* machine name */
                              rc = my_ll_get_data(task_instance, LL_TaskInstanceMachineAddress, &task_instance_machine_address);        /* machine name */
                              rc = my_ll_get_data(task_instance, LL_TaskInstanceTaskID, &task_instance_task_ID);        /* task id */
                              print_message(INFO_MESSAGE, "TaskInstanceMachineName=%s. TaskInstanceMachineAddress=%s. TaskInstanceTaskID=%d.\n", task_instance_machine_name, task_instance_machine_address, task_instance_task_ID);
                              if ((task_object = get_task_in_list(job_object->task_list, task_instance_machine_name, task_instance_task_ID)) != NULL) { /* existing task */

                         /*-----------------------------------------------------------------------* 
                         * task returned by LoadLeveler was found in our ptp job task list.      * 
                         * flag it as found.                                                     * 
                         *-----------------------------------------------------------------------*/
                                task_object->ll_task_id = task_instance_task_ID;
                                task_object->task_found = 1;    /* flag it as found */
                                if (task_object->task_state != MY_STATE_RUNNING) {
                                  task_object->task_state = MY_STATE_RUNNING;
                                  print_message(INFO_MESSAGE, "Schedule event notification: Task_ID=%d running on node_name=%s added for LoadLeveler Job=%s.%d.%d for LoadLeveler Cluster=%s.\n", task_object->ll_task_id, task_object->node_name, job_object->ll_step_id.from_host, job_object->ll_step_id.cluster, job_object->ll_step_id.proc, job_object->cluster_name);
                                  sendTaskChangeEvent(start_events_gui_transmission_id, job_object, task_object);
                                }
                                if (job_object->job_state != MY_STATE_RUNNING) {
                                  job_object->job_state = MY_STATE_RUNNING;
                                  print_message(INFO_MESSAGE, "Schedule event notification: Job=%s.%d.%d changed for LoadLeveler Cluster=%s.\n", job_object->ll_step_id.from_host, job_object->ll_step_id.cluster, job_object->ll_step_id.proc, job_object->cluster_name);
                                  sendJobChangeEvent(start_events_gui_transmission_id, job_object);
                                }
                              } else {  /* new task (not yet in list) */

                           /*-----------------------------------------------------------------------* 
                            * task returned by LoadLeveler was not found in our ptp job task list   * 
                            * add it and generate an event to the gui. flag it as added.            * 
                            *-----------------------------------------------------------------------*/
                                task_object = (TaskObject *) malloc(sizeof(TaskObject));
                                malloc_check(task_object, __FUNCTION__, __LINE__);
                                memset(task_object, '\0', sizeof(task_object)); /* zero the malloc area */
                                task_object->task_id = job_object->task_counter;
				job_object->task_counter = job_object->task_counter + 1;
                                task_object->ll_task_id = task_instance_task_ID;
                                task_object->node_name = strdup(task_instance_machine_name);
                                task_object->node_address = strdup(task_instance_machine_address);
                                task_object->task_found = 2;    /* flag it as added */
                                task_object->task_state = MY_STATE_RUNNING;
                                print_message(INFO_MESSAGE, "Schedule event notification: Job=%s.%d.%d changed for LoadLeveler Cluster=%s.\n", job_object->ll_step_id.from_host, job_object->ll_step_id.cluster, job_object->ll_step_id.proc, job_object->cluster_name);
                                if (job_object->job_state != MY_STATE_RUNNING) {
                                  job_object->job_state = MY_STATE_RUNNING;
                                  print_message(INFO_MESSAGE, "Schedule event notification: Job=%s.%d.%d changed for LoadLeveler Cluster=%s.\n", job_object->ll_step_id.from_host, job_object->ll_step_id.cluster, job_object->ll_step_id.proc, job_object->cluster_name);
                                  sendJobChangeEvent(start_events_gui_transmission_id, job_object);
                                }
                                add_task_to_list(job_object->task_list, (void *) task_object);  /* add the new task object to the list */
                                print_message(INFO_MESSAGE, "Schedule event notification: Task_ID=%d running on node_name=%s added for LoadLeveler Job=%s.%d.%d for LoadLeveler Cluster=%s.\n", task_object->ll_task_id, task_object->node_name, job_object->ll_step_id.from_host, job_object->ll_step_id.cluster, job_object->ll_step_id.proc, job_object->cluster_name);
                                sendTaskAddEvent(start_events_gui_transmission_id, cluster_object, job_object, task_object);
                              } /* end if new node */
                              rc = my_ll_get_data(task, LL_TaskGetNextTaskInstance, &task_instance);    /* task instances */
                            }
                            rc = my_ll_get_data(node, LL_NodeGetNextTask, &task);       /* task */
                          }
                          rc = my_ll_get_data(step, LL_StepGetNextNode, &node); /* node */
                        }       /* end while we have a valid LoadLeveler node */
                      }           /* end if job step is in running-like state (is using nodes) */
                      
                /*-----------------------------------------------------------------------* 
                 * loop on the tasks in the job object - if any not found that were      * 
                 * present before then generate deleted task events.                     * 
                 *-----------------------------------------------------------------------*/
                      task_list = (List *) job_object->task_list;       /* the task list in the job */
                      task_list_element = task_list->l_head;    /* first in list */
                      while (task_list_element != NULL) {       /* while we have a task to query */
                        task_object = task_list_element->l_value;       /* get the task object (if any) */
                        task_list_element = task_list_element->l_next;  /* prepare for next pass */
                        if (task_object != 0) { /* if we have a task object */
                          if (task_object->task_found == 0) {   /* if not found or added this pass */
                            task_object->task_state = MY_STATE_TERMINATED;
                            task_object->task_found = 0;        /* reset for next pass */
                            print_message(INFO_MESSAGE, "Schedule event notification: Task_ID=%d running on node_name=%s deleted for LoadLeveler Job=%s.%d.%d for LoadLeveler Cluster=%s.\n", task_object->ll_task_id, task_object->node_name, job_object->ll_step_id.from_host, job_object->ll_step_id.cluster, job_object->ll_step_id.proc, job_object->cluster_name);
                            sendTaskRemoveEvent(start_events_gui_transmission_id, job_object, task_object);
                            delete_task_from_list(task_list, task_object);      /* clean this task object and delete from the job */
                            if (SizeOfList(task_list) == 0) {
                              if (job_object->job_state == MY_STATE_RUNNING) {
                                job_object->job_state = MY_STATE_TERMINATED;    /* job is done */
                                print_message(INFO_MESSAGE, "Schedule event notification: Job=%s.%d.%d terminated for LoadLeveler Cluster=%s.\n", job_object->ll_step_id.from_host, job_object->ll_step_id.cluster, job_object->ll_step_id.proc, job_object->cluster_name);
                                sendJobChangeEvent(start_events_gui_transmission_id, job_object);
                              /* NO print_message(INFO_MESSAGE, "Schedule event notification: Job=%s.%d.%d deleted for LoadLeveler Cluster=%s.\n", job_object->ll_step_id.from_host, job_object->ll_step_id.cluster, job_object->ll_step_id.proc, job_object->cluster_name); */
                              /* NO sendJobRemoveEvent(start_events_gui_transmission_id, job_object); */
                              } else {
                                job_object->job_state = MY_STATE_IDLE;
                                print_message(INFO_MESSAGE, "Schedule event notification: Job=%s.%d.%d changed for LoadLeveler Cluster=%s.\n", job_object->ll_step_id.from_host, job_object->ll_step_id.cluster, job_object->ll_step_id.proc, job_object->cluster_name);
                                sendJobChangeEvent(start_events_gui_transmission_id, job_object);
                              }
                            }
                          } else {      /* it was found or added this pass */
                            task_object->task_found = 0;        /* reset for next pass */
                          }
                        }       /* end if we have a task object */
                      }         /* end while we have task list element to look at */


                      rc = my_ll_get_data(job, LL_JobGetNextStep, &step);       /* get another job step */
                    }
                  }             /* end while we have a step */
                }               /* we got a job name ok */

                i++;
                job = my_ll_next_obj(query_elem);
              }                 /* end while we have a job */
            }                   /* end if we have nodes in this cluster */
          }                     /* end if we have a node table in this cluster */
        }                       /* end if we have a cluster object */
        if (query_elem != NULL) {
          rc = my_ll_free_objs(query_elem);
          rc = my_ll_deallocate(query_elem);
          query_elem = NULL;
        }
      }                         /* end while we have a cluster to query */

      if (query_elem != NULL) {
        rc = my_ll_free_objs(query_elem);
        rc = my_ll_deallocate(query_elem);
        query_elem = NULL;
      }

      /*-----------------------------------------------------------------------* 
       * get the time and see if job has been sitting in submitted state too   * 
       * long.                                                                 * 
       *-----------------------------------------------------------------------*/
      time(&my_clock);             /* what time is it ? */

      /*-----------------------------------------------------------------------* 
       * loop on the ptp job list to see if any jobs were not returned         * 
       * by LoadLeveler on this pass (maybe they went down).                   * 
       * generate an event (changed/gone) to the gui.                          * 
       *-----------------------------------------------------------------------*/
      if (job_list != NULL) {   /* if we have a job list ok */
        job_list_element = job_list->l_head;    /* first in list */
        while (job_list_element != NULL) {      /* while we have a job to query */
          job_object = job_list_element->l_value;       /* get our job object from this list element */
          job_list_element = job_list_element->l_next;  /* prepare for next pass */
          if ((job_object->job_found == 0) &&   /* if job not found */
              ((job_object->job_state != MY_STATE_UNKNOWN) ||   /* and was running */
               ((my_clock - job_object->job_submit_time) > 300))) {        /* or never queued after time limit (seconds) */
            job_object->job_found = 0;  /* reset for next pass */

            /*-----------------------------------------------------------------------* 
             * loop on the tasks in the job object - send deleted event and mark     *
             * all deleted.                                                          *
             *-----------------------------------------------------------------------*/
            task_list = (List *) job_object->task_list; /* the task list in the job */
            task_list_element = task_list->l_head;      /* first in list */
            while (task_list_element != NULL) { /* while we have a task to delete */
              task_object = task_list_element->l_value; /* get the task object (if any) */
              task_list_element = task_list_element->l_next;    /* prepare for next pass */
              if (task_object != 0) {   /* if we have a task object */
                print_message(INFO_MESSAGE, "Schedule event notification: Task_ID=%d deleted on node_name=%s deleted for LoadLeveler Job=%s.%d.%d for LoadLeveler Cluster=%s.\n", task_object->ll_task_id, task_object->node_name, job_object->ll_step_id.from_host, job_object->ll_step_id.cluster, job_object->ll_step_id.proc, job_object->cluster_name);
                sendTaskRemoveEvent(start_events_gui_transmission_id, job_object, task_object);
                delete_task_from_list(task_list, task_object);  /* clean this task object and delete from the job */
                if (SizeOfList(task_list) == 0) {
                  job_object->job_state = MY_STATE_IDLE;
                  print_message(INFO_MESSAGE, "Schedule event notification: Job=%s.%d.%d changed for LoadLeveler Cluster=%s.\n", job_object->ll_step_id.from_host, job_object->ll_step_id.cluster, job_object->ll_step_id.proc, job_object->cluster_name);
                  sendJobChangeEvent(start_events_gui_transmission_id, job_object);
                }
              }                 /* end if we have a task object */
            }                   /* end while we have task list element to look at */

            job_object->job_state = MY_STATE_TERMINATED;        /* job is done */
            print_message(INFO_MESSAGE, "Schedule event notification: Job=%s.%d.%d terminated for LoadLeveler Cluster=%s.\n", job_object->ll_step_id.from_host, job_object->ll_step_id.cluster, job_object->ll_step_id.proc, job_object->cluster_name);
            sendJobChangeEvent(start_events_gui_transmission_id, job_object);
            print_message(INFO_MESSAGE, "Schedule event notification: Job=%s.%d.%d deleted for LoadLeveler Cluster=%s.\n", job_object->ll_step_id.from_host, job_object->ll_step_id.cluster, job_object->ll_step_id.proc, job_object->cluster_name);
            sendJobRemoveEvent(start_events_gui_transmission_id, job_object);
            delete_job_from_list(job_list, job_object); /* clean this job object and delete from its cluster */
          } /* end if not found and was not previously in queue */
          else {                /* job was found or not found but hasn't made it to queue yet */
            job_object->job_found = 0;  /* reset for next pass */
          }                     /* end if job was found or not found but hasn't made it to queue yet */
        }                       /* end while we have job list element to look at */
      }                         /* end while we have a job list table */
    }
  /* end if we have a clusters, nodes, etc */
    pthread_mutex_unlock(&master_lock);

    /*-----------------------------------------------------------------------* 
     * sleep and loop again on the LoadLeveler machines.                     * 
     *-----------------------------------------------------------------------*/
    if (state_shutdown_requested == 0) {
      int sleep_interval = 0;
      int mini_sleep_interval = (job_sleep_seconds + 4) / 5;
      print_message(INFO_MESSAGE, "%s Sleeping for (%d seconds) %d intervals of 5 seconds\n", __FUNCTION__, mini_sleep_interval * 5, mini_sleep_interval);
      for (sleep_interval = 0; sleep_interval < mini_sleep_interval; sleep_interval++) {
        if (state_shutdown_requested == 0) {
          sleep(5);
        }
      }
    }
  }                             /* end while we are not shutting down */

  print_message(TRACE_MESSAGE, "<<< %s returning. line=%d.\n", __FUNCTION__, __LINE__);
  return NULL;
}

/************************************************************************* 
 * Retrieve a list of LoadLeveler clusters.                              * 
 * If no multicluster environment then return a list of 1 cluster.       *
 *************************************************************************/
void refresh_cluster_list()
{
  int rc = 0;
  int i = 0;
  LL_element *query_elem = NULL;
  LL_element *cluster = NULL;
  int cluster_count = 0;
  char *cluster_name = NULL;
  int cluster_local = 0;
  ClusterObject *cluster_object = NULL;
  char workarea[512];

  print_message(TRACE_MESSAGE, ">>> %s entered. line=%d.\n", __FUNCTION__, __LINE__);

  if ((state_shutdown_requested == 0) && (state_events_active == 1)) {  /* if not shutting down */

    if (multicluster_status == -1) {    /* if state not yet determined */
      multicluster_status = get_multicluster_status();  /* are we running multicluster? */
    }
  /* end if state not yet determined */
    switch (multicluster_status) {

      /*-----------------------------------------------------------------------* 
       * no contact with loadleveler yet                                       * 
       *-----------------------------------------------------------------------*/
      case -1:
        break;

      /*-----------------------------------------------------------------------* 
       * single cluster and multi cluster                                      * 
       *-----------------------------------------------------------------------*/
      case 0:                  /* single cluster */
        if (cluster_list == NULL) {     /* if no list obtained yet */
          cluster_list = NewList();     /* create a new cluster list */
          job_list = NewList(); /* create a new job list */
        }
      /* end if no list obtained yet */
        print_message(INFO_MESSAGE, "Number of LoadLeveler Clusters=0 (not running multicluster).\n");
        i = 0;
        cluster_object = (ClusterObject *) malloc(sizeof(ClusterObject));
        malloc_check(cluster_object, __FUNCTION__, __LINE__);
        memset(cluster_object, '\0', sizeof(cluster_object));   /* zero the malloc area */
        cluster_object->proxy_generated_cluster_id = generate_id();     /* a unique identifier for this cluster */
        cluster_object->proxy_generated_queue_id = generate_id();       /* a unique identifier for this cluster */
        cluster_object->cluster_state = MY_STATE_UP;    /* mark cluster object created */
        cluster_object->queue_state = MY_STATE_UP;      /* mark queue object created */
        memset(workarea, '\0', sizeof(workarea));       /* zero the area */
        strcpy(workarea,"Local@");
        strcat(workarea,hostname);
        strcat(workarea," (not multicluster)");
        cluster_object->cluster_name = strdup(workarea);      /* first cluster in the list is the local cluster (noname) */
        cluster_object->cluster_is_local = 1;   /* this is a local cluster */
        cluster_object->node_hash = HashCreate(1024);
        print_message(INFO_MESSAGE, "Cluster name=%s\n", cluster_name);
        AddToList(cluster_list, (void *) cluster_object);       /* add the new cluster object to the list */
        print_message(INFO_MESSAGE, "Send event notification: PTP Machine added for LoadLeveler Cluster=%s.\n", cluster_name);
        sendMachineAddEvent(start_events_gui_transmission_id, cluster_object);
        print_message(INFO_MESSAGE, "Send event notification: PTP Queue added for LoadLeveler Cluster=%s.\n", cluster_name);
        sendQueueAddEvent(start_events_gui_transmission_id, cluster_object);
        break;

      /*-----------------------------------------------------------------------* 
       * multicluster                                                          * 
       *-----------------------------------------------------------------------*/
      case 1:                  /* multicluster */
        if (cluster_list == NULL) {     /* if no list obtained yet */
          cluster_list = NewList();     /* create a new cluster list */
          job_list = NewList(); /* create a new job list */
        }
      /* end if no list obtained yet */
        query_elem = NULL;

        query_elem = my_ll_query(MCLUSTERS);

        if (query_elem == NULL) {
          print_message(ERROR_MESSAGE, "Unable to obtain query element. LoadLeveler may not be active.\n");
          multicluster_status = -1;     /* we need to come in here again */
          return;
        }

        print_message(INFO_MESSAGE, "Call LoadLeveler (ll_set_request) for clusters.\n");
        rc = my_ll_set_request(query_elem, QUERY_ALL, NULL, ALL_DATA);

        if (rc != 0) {
          rc = my_ll_deallocate(query_elem);
          query_elem = NULL;
          multicluster_status = -1;     /* we need to come in here again */
          return;
        }

        print_message(INFO_MESSAGE, "Call LoadLeveler (ll_get_objs) for clusters.\n");
        cluster = my_ll_get_objs(query_elem, LL_SCHEDD, NULL, &cluster_count, &rc);
        if (rc != 0) {
          rc = my_ll_deallocate(query_elem);
          query_elem = NULL;
          multicluster_status = -1;     /* we need to come in here again */
          return;
        }

        print_message(INFO_MESSAGE, "Number of LoadLeveler Clusters=%d.\n", cluster_count);
        i = 0;
        while ((cluster != NULL) && (query_elem != NULL)) {
          print_message(INFO_MESSAGE, "Cluster %d:\n", i);
          rc = my_ll_get_data(cluster, LL_MClusterName, &cluster_name);
          if (rc != 0) {
            rc = my_ll_free_objs(query_elem);
            rc = my_ll_deallocate(query_elem);
            query_elem = NULL;
            multicluster_status = -1;   /* we need to come in here again */
            return;
          } else {
            cluster_object = (ClusterObject *) malloc(sizeof(ClusterObject));
            malloc_check(cluster_object, __FUNCTION__, __LINE__);
            memset(cluster_object, '\0', sizeof(cluster_object));       /* zero the malloc area */
            cluster_object->proxy_generated_cluster_id = generate_id(); /* a unique identifier for this cluster */
            cluster_object->proxy_generated_queue_id = generate_id();   /* a unique identifier for this cluster */
            cluster_object->cluster_state = MY_STATE_UP;        /* mark cluster object created */
            cluster_object->queue_state = MY_STATE_UP;  /* mark queue object created */
            rc = my_ll_get_data(cluster, LL_MClusterLocal, &cluster_local);
            cluster_object->cluster_name = strdup(cluster_name);        /* first cluster in the list is the local cluster (noname) */
            if (cluster_local == 1) {
              cluster_object->cluster_is_local = 1;     /* this is a local cluster */
            }
            cluster_object->node_hash = HashCreate(1024);
            print_message(INFO_MESSAGE, "Cluster name=%s\n", cluster_name);
            AddToList(cluster_list, (void *) cluster_object);   /* add the new cluster object to the list */
            print_message(INFO_MESSAGE, "Send event notification: PTP Machine added for LoadLeveler Cluster=%s.\n", cluster_name);
            sendMachineAddEvent(start_events_gui_transmission_id, cluster_object);
            print_message(INFO_MESSAGE, "Send event notification: PTP Queue added for LoadLeveler Cluster=%s.\n", cluster_name);
            sendQueueAddEvent(start_events_gui_transmission_id, cluster_object);
          }

          i++;
          cluster = my_ll_next_obj(query_elem);
        }

        if (query_elem != NULL) {
          rc = my_ll_free_objs(query_elem);
          rc = my_ll_deallocate(query_elem);
        }
        query_elem = NULL;
        break;

    }                           /* end switch */

  }                             /* end if not shutdown */
  print_message(TRACE_MESSAGE, "<<< %s returning. line=%d.\n", __FUNCTION__, __LINE__);
  return;
}

/************************************************************************* 
 * Send OK Event to front end GUI                                        * 
 *************************************************************************/
void sendOkEvent(int gui_transmission_id)
{
  proxy_msg *msg;
  print_message(TRACE_MESSAGE, ">>> %s entered. line=%d.\n", __FUNCTION__, __LINE__);
  msg = proxy_ok_event(gui_transmission_id);
  enqueue_event_to_proxy_server(msg);
  print_message(TRACE_MESSAGE, "<<< %s returning. line=%d.\n", __FUNCTION__, __LINE__);
}

/************************************************************************* 
 * Send Shutdown Event to front end GUI                                  * 
 *************************************************************************/
void sendShutdownEvent(int gui_transmission_id)
{
  proxy_msg *msg;
  print_message(TRACE_MESSAGE, ">>> %s entered. line=%d.\n", __FUNCTION__, __LINE__);
  msg = proxy_shutdown_event(gui_transmission_id);
  enqueue_event_to_proxy_server(msg);
  print_message(TRACE_MESSAGE, "<<< %s returning. line=%d.\n", __FUNCTION__, __LINE__);
}

/************************************************************************* 
 * Send ERROR Event to front end GUI (error type and message)            * 
 *************************************************************************/
void sendErrorEvent(int gui_transmission_id, int type, char *msgtext)
{
  print_message(TRACE_MESSAGE, ">>> %s entered. line=%d. type=%d. message=%s.\n", __FUNCTION__, __LINE__, type, msgtext);
/* 
 * Send an error message to the front end
 */
  proxy_msg *msg;
  msg = new_proxy_msg(PTP_PROXY_EV_ERROR, gui_transmission_id);
  proxy_msg_add_int(msg, type);
  proxy_msg_add_string(msg, msgtext);
  enqueue_event_to_proxy_server(msg);
  print_message(TRACE_MESSAGE, "<<< %s returning. line=%d.\n", __FUNCTION__, __LINE__);
  return;
}

/************************************************************************* 
 * Send job submission ERROR Event to front end GUI (error type and      *
 * message)                                                              * 
 *************************************************************************/
void sendJobSubmissionErrorEvent(int gui_transmission_id, char *subid, char *msgtext)
{
  print_message(TRACE_MESSAGE, ">>> %s entered. line=%d. id=%s. message=%s.\n", __FUNCTION__, __LINE__, subid, msgtext);
/* 
 * Send an error message to the front end
 */
  proxy_msg *msg;
  msg = proxy_submitjob_error_event(gui_transmission_id, subid, 0, msgtext);
  enqueue_event_to_proxy_server(msg);
  print_message(TRACE_MESSAGE, "<<< %s returning. line=%d.\n", __FUNCTION__, __LINE__);
  return;
}

/************************************************************************* 
 * Send Configuration panel integer attribute Event to front end GUI     * 
 *************************************************************************/
static int sendAttrDefIntEvent(int gui_transmission_id, char *attribute_id, char *attribute_shortname, char *attribute_description, int attribute_show, int attribute_default_value, int attribute_llimit, int attribute_ulimit)
{
  proxy_msg *msg;

  print_message(TRACE_MESSAGE, ">>> %s entered. line=%d. id=%s. name=%s. description=%s. default=%d. value1=%d. value2=%d.\n", __FUNCTION__, __LINE__, attribute_id, attribute_shortname, attribute_description, attribute_default_value, attribute_llimit, attribute_ulimit);
  msg = new_proxy_msg(PTP_PROXY_EV_RT_ATTR_DEF, gui_transmission_id);
  proxy_msg_add_int(msg, 1);
  proxy_msg_add_int(msg, 8);
  proxy_msg_add_string(msg, attribute_id);
  proxy_msg_add_string(msg, "INTEGER");
  proxy_msg_add_string(msg, attribute_shortname);
  proxy_msg_add_string(msg, attribute_description);
  proxy_msg_add_int(msg, attribute_show);
  proxy_msg_add_int(msg, attribute_default_value);
  proxy_msg_add_int(msg, attribute_llimit);
  proxy_msg_add_int(msg, attribute_ulimit);
  enqueue_event_to_proxy_server(msg);
  print_message(TRACE_MESSAGE, "<<< %s returning. line=%d.\n", __FUNCTION__, __LINE__);
  return 0;
}

/************************************************************************* 
 * Send Configuration panel string attribute Event to front end GUI      * 
 *************************************************************************/
static int sendAttrDefStringEvent(int gui_transmission_id, char *attribute_id, char *attribute_shortname, char *attribute_description, int attribute_show, char *attribute_value)
{
  proxy_msg *msg;

  print_message(TRACE_MESSAGE, ">>> %s entered. line=%d. id=%s. name=%s. description=%s. value=%s.\n", __FUNCTION__, __LINE__, attribute_id, attribute_shortname, attribute_description, attribute_value);
  msg = proxy_attr_def_string_event(gui_transmission_id, attribute_id, attribute_shortname, attribute_description, attribute_show, attribute_value);
  enqueue_event_to_proxy_server(msg);
  print_message(TRACE_MESSAGE, "<<< %s returning. line=%d.\n", __FUNCTION__, __LINE__);
  return 0;
}

/************************************************************************* 
 * Determine if LoadLeveler is configured multicluster.                  * 
 * If any errors then default to local. This code will not be called     * 
 * if the user has forced us to run local only or multicluster mode.     * 
 *************************************************************************/
int get_multicluster_status()
{
  int rc = 0;
  int i = 0;
  LL_element *query_elem = NULL;
  LL_element *cluster = NULL;
  int cluster_count = 0;

  print_message(TRACE_MESSAGE, ">>> %s entered. line=%d.\n", __FUNCTION__, __LINE__);

  if ((state_shutdown_requested == 0) && (state_events_active == 1)) {  /* if not shutting down and allowed to process events */

    if (multicluster_status == -1) {    /* if state not yet determined */

      query_elem = NULL;
      query_elem = my_ll_query(CLUSTERS);

      if (query_elem == NULL) {
        print_message(ERROR_MESSAGE, "Unable to obtain query element. LoadLeveler may not be active.\n");
        return -1;
      }

    /* Get information relating to LoadLeveler clusters.
     * QUERY_ALL: we are querying for local cluster data  
     * NULL: no filter needed 
     * ALL_DATA: we want all the information available about the cluster */

      print_message(INFO_MESSAGE, "Call LoadLeveler (ll_set_request) for cluster.\n");
      rc = my_ll_set_request(query_elem, QUERY_ALL, NULL, ALL_DATA);

      print_message(INFO_MESSAGE, "Call LoadLeveler (ll_get_objs) for cluster.\n");
      cluster = my_ll_get_objs(query_elem, LL_CM, NULL, &cluster_count, &rc);
      if (rc < 0) {
        rc = my_ll_deallocate(query_elem);
        return -1;
      }

      print_message(INFO_MESSAGE, "Number of LoadLeveler Clusters=%d.\n", cluster_count);
      i = 0;
      if (cluster != NULL) {
        print_message(INFO_MESSAGE, "Cluster %d:\n", i);
        rc = my_ll_get_data(cluster, LL_ClusterMusterEnvironment, &multicluster_status);
        if (rc != 0) {
          print_message(ERROR_MESSAGE, "Error rc=%d trying to determine if LoadLeveler is running local or multicluster configuration. Defaulting to local cluster only (no multicluster).\n", rc);
          multicluster_status = 0;      /* default to local */
        } else {
          print_message(INFO_MESSAGE, "Multicluster returned is = %d.\n", multicluster_status);
        }

      }

    /* First we need to release the individual objects that were */
    /* obtained by the query */
      rc = my_ll_free_objs(query_elem);
      rc = my_ll_deallocate(query_elem);
    }                           /* end if state not yet determined */
  }                             /* end if not shutdown */
  print_message(TRACE_MESSAGE, "<<< %s returning. line=%d.\n", __FUNCTION__, __LINE__);
  return multicluster_status;
}

/************************************************************************* 
 * send cluster added event to gui (a cluster to LoadLeveler is a        * 
 * PTP machine to the gui)                                               * 
 *************************************************************************/
static int sendMachineAddEvent(int gui_transmission_id, ClusterObject * cluster_object)
{
  proxy_msg *msg;
  char proxy_generated_cluster_id_string[256];
  char *machine_state_to_report = PTP_MACHINE_STATE_UNKNOWN;

  print_message(TRACE_MESSAGE, ">>> %s entered. line=%d. cluster=%s. state=%d.\n", __FUNCTION__, __LINE__, cluster_object->cluster_name, cluster_object->cluster_state);
  memset(proxy_generated_cluster_id_string, '\0', sizeof(proxy_generated_cluster_id_string));   /* zero the area */
  sprintf(proxy_generated_cluster_id_string, "%d", cluster_object->proxy_generated_cluster_id);

  switch (cluster_object->cluster_state) {
    case MY_STATE_UP:
      machine_state_to_report = PTP_MACHINE_STATE_UP;
      break;
    case MY_STATE_DOWN:
      machine_state_to_report = PTP_MACHINE_STATE_DOWN;
      break;
    default:
      machine_state_to_report = PTP_MACHINE_STATE_UNKNOWN;
      break;
  }
  msg = proxy_new_machine_event(gui_transmission_id, ibmll_proxy_base_id_string, proxy_generated_cluster_id_string, cluster_object->cluster_name, machine_state_to_report);
  enqueue_event_to_proxy_server(msg);
  print_message(TRACE_MESSAGE, "<<< %s returning. line=%d.\n", __FUNCTION__, __LINE__);
  return 0;
}

/************************************************************************* 
 * send queue aqdded event to gui (one per cluster)                      * 
 *************************************************************************/
static int sendQueueAddEvent(int gui_transmission_id, ClusterObject * cluster_object)
{
  proxy_msg *msg;
  char proxy_generated_queue_id_string[256];

  print_message(TRACE_MESSAGE, ">>> %s entered. line=%d. queue=%s. state=%d.\n", __FUNCTION__, __LINE__, cluster_object->cluster_name, cluster_object->queue_state);
  memset(proxy_generated_queue_id_string, '\0', sizeof(proxy_generated_queue_id_string));       /* zero the area */
  sprintf(proxy_generated_queue_id_string, "%d", cluster_object->proxy_generated_queue_id);

  switch (cluster_object->cluster_state) {
    case MY_STATE_UP:
      break;
    default:
      break;
  }

  msg = proxy_new_queue_event(gui_transmission_id, ibmll_proxy_base_id_string, proxy_generated_queue_id_string, cluster_object->cluster_name, 0);
  enqueue_event_to_proxy_server(msg);
  print_message(TRACE_MESSAGE, "<<< %s returning. line=%d.\n", __FUNCTION__, __LINE__);
  return 0;
}

/************************************************************************* 
 * send node added event to gui                                          * 
 *************************************************************************/
static int sendNodeAddEvent(int gui_transmission_id, ClusterObject * cluster_object, NodeObject * node_object)
{
  proxy_msg *msg;
  char proxy_generated_cluster_id_string[256];
  char proxy_generated_node_id_string[256];
  char *node_state_to_report = PTP_NODE_STATE_UNKNOWN;

  print_message(TRACE_MESSAGE, ">>> %s entered. line=%d. cluster=%s. node=%s. state=%d.\n", __FUNCTION__, __LINE__, cluster_object->cluster_name, node_object->node_name, node_object->node_state);
  memset(proxy_generated_cluster_id_string, '\0', sizeof(proxy_generated_cluster_id_string));   /* zero the area */
  memset(proxy_generated_node_id_string, '\0', sizeof(proxy_generated_node_id_string)); /* zero the area */
  sprintf(proxy_generated_cluster_id_string, "%d", cluster_object->proxy_generated_cluster_id);
  sprintf(proxy_generated_node_id_string, "%d", node_object->proxy_generated_node_id);

  switch (node_object->node_state) {
    case MY_STATE_UP:
      node_state_to_report = PTP_NODE_STATE_UP;
      break;
    case MY_STATE_DOWN:
      node_state_to_report = PTP_NODE_STATE_DOWN;
      break;
    default:
      node_state_to_report = PTP_NODE_STATE_UNKNOWN;
      break;
  }

  msg = proxy_new_node_event(gui_transmission_id, proxy_generated_cluster_id_string, 1);        /* 1==number new nodes in cluster */
  proxy_add_node(msg, proxy_generated_node_id_string, node_object->node_name, node_state_to_report, 0); /* 0==extra attributes */
  enqueue_event_to_proxy_server(msg);
  print_message(TRACE_MESSAGE, "<<< %s returning. line=%d.\n", __FUNCTION__, __LINE__);
  return 0;
}

/************************************************************************* 
 * send node changed event to gui                                        * 
 *************************************************************************/
static int sendNodeChangeEvent(int gui_transmission_id, ClusterObject * cluster_object, NodeObject * node_object)
{
  proxy_msg *msg;
  char proxy_generated_node_id_string[256];
  char *node_state_to_report = PTP_NODE_STATE_UNKNOWN;

  print_message(TRACE_MESSAGE, ">>> %s entered. line=%d. cluster=%s. node=%s. state=%d.\n", __FUNCTION__, __LINE__, cluster_object->cluster_name, node_object->node_name, node_object->node_state);
  memset(proxy_generated_node_id_string, '\0', sizeof(proxy_generated_node_id_string)); /* zero the area */
  sprintf(proxy_generated_node_id_string, "%d", node_object->proxy_generated_node_id);

  switch (node_object->node_state) {
    case MY_STATE_UP:
      node_state_to_report = PTP_NODE_STATE_UP;
      break;
    case MY_STATE_DOWN:
      node_state_to_report = PTP_NODE_STATE_DOWN;
      break;
    default:
      node_state_to_report = PTP_NODE_STATE_UNKNOWN;
      break;
  }

  msg = proxy_node_change_event(gui_transmission_id, proxy_generated_node_id_string, 1);        /* 1==number new nodes in cluster */
  proxy_add_node(msg, proxy_generated_node_id_string, node_object->node_name, node_state_to_report, 0); /* 0==extra attributes */
  enqueue_event_to_proxy_server(msg);
  print_message(TRACE_MESSAGE, "<<< %s returning. line=%d.\n", __FUNCTION__, __LINE__);
  return 0;
}

/************************************************************************* 
 * send node removed event to gui                                        * 
 *************************************************************************/
static int sendNodeRemoveEvent(int gui_transmission_id, ClusterObject * cluster_object, NodeObject * node_object)
{
  proxy_msg *msg;
  char proxy_generated_node_id_string[256];

  print_message(TRACE_MESSAGE, ">>> %s entered. line=%d. cluster=%s.\n", __FUNCTION__, __LINE__, cluster_object->cluster_name, node_object->node_name);
  memset(proxy_generated_node_id_string, '\0', sizeof(proxy_generated_node_id_string)); /* zero the area */
  sprintf(proxy_generated_node_id_string, "%d", node_object->proxy_generated_node_id);

  msg = proxy_remove_node_event(gui_transmission_id, proxy_generated_node_id_string);
  enqueue_event_to_proxy_server(msg);
  print_message(TRACE_MESSAGE, "<<< %s returning. line=%d.\n", __FUNCTION__, __LINE__);
  return 0;
}

/************************************************************************* 
 * send job added event to gui                                           * 
 *************************************************************************/
static int sendJobAddEvent(int gui_transmission_id, ClusterObject * cluster_object, JobObject * job_object)
{
  proxy_msg *msg;
  char proxy_generated_job_id_string[256];
  char proxy_generated_queue_id_string[256];
  char job_name_string[256];
  char *job_state_to_report = PTP_JOB_STATE_STARTING;

  print_message(TRACE_MESSAGE, ">>> %s entered. line=%d. job=%s.%d.%d. state=%d.\n", __FUNCTION__, __LINE__, job_object->ll_step_id.from_host, job_object->ll_step_id.cluster, job_object->ll_step_id.proc, job_object->job_state);
  memset(proxy_generated_job_id_string, '\0', sizeof(proxy_generated_job_id_string));   /* zero the area */
  memset(proxy_generated_queue_id_string, '\0', sizeof(proxy_generated_queue_id_string));       /* zero the area */
  memset(job_name_string, '\0', sizeof(job_name_string));       /* zero the area */
  sprintf(proxy_generated_job_id_string, "%d", job_object->proxy_generated_job_id);
  sprintf(proxy_generated_queue_id_string, "%d", cluster_object->proxy_generated_queue_id);

  switch (job_object->job_state) {
    case MY_STATE_IDLE:
      job_state_to_report = PTP_JOB_STATE_STARTING;
      break;
    case MY_STATE_RUNNING:
      job_state_to_report = PTP_JOB_STATE_RUNNING;
      break;
    case MY_STATE_STOPPED:
      job_state_to_report = PTP_JOB_STATE_SUSPENDED;
      break;
    case MY_STATE_TERMINATED:
      job_state_to_report = PTP_JOB_STATE_COMPLETED;
      break;
    default:
      job_state_to_report = PTP_JOB_STATE_STARTING;
      break;
  }

  sprintf(job_name_string, "%s.%d.%d", job_object->ll_step_id.from_host, job_object->ll_step_id.cluster, job_object->ll_step_id.proc);
  msg = proxy_new_job_event(gui_transmission_id, proxy_generated_queue_id_string, proxy_generated_job_id_string, job_name_string, job_state_to_report, job_object->gui_assigned_job_id);
  enqueue_event_to_proxy_server(msg);
  print_message(TRACE_MESSAGE, "<<< %s returning. line=%d.\n", __FUNCTION__, __LINE__);
  return 0;
}

/************************************************************************* 
 * send job changed event to gui                                         * 
 *************************************************************************/
static int sendJobChangeEvent(int gui_transmission_id, JobObject * job_object)
{
  proxy_msg *msg;
  char proxy_generated_job_id_string[256];
  char job_state_string[256];
  char *job_state_to_report = PTP_JOB_STATE_STARTING;

  print_message(TRACE_MESSAGE, ">>> %s entered. line=%d. job=%s.%d.%d. state=%d.\n", __FUNCTION__, __LINE__, job_object->ll_step_id.from_host, job_object->ll_step_id.cluster, job_object->ll_step_id.proc, job_object->job_state);
  memset(proxy_generated_job_id_string, '\0', sizeof(proxy_generated_job_id_string));   /* zero the area */
  memset(job_state_string, '\0', sizeof(job_state_string));     /* zero the area */
  sprintf(proxy_generated_job_id_string, "%d", job_object->proxy_generated_job_id);

  switch (job_object->job_state) {
    case MY_STATE_IDLE:
      job_state_to_report = PTP_JOB_STATE_STARTING;
      break;
    case MY_STATE_RUNNING:
      job_state_to_report = PTP_JOB_STATE_RUNNING;
      break;
    case MY_STATE_STOPPED:
      job_state_to_report = PTP_JOB_STATE_SUSPENDED;
      break;
    case MY_STATE_TERMINATED:
      job_state_to_report = PTP_JOB_STATE_COMPLETED;
      break;
  }

  msg = proxy_job_change_event(gui_transmission_id, proxy_generated_job_id_string, 1);
  sprintf(job_state_string, "%d", job_object->job_state);
  proxy_add_string_attribute(msg, PTP_JOB_STATE_ATTR, job_state_to_report);
  enqueue_event_to_proxy_server(msg);
  print_message(TRACE_MESSAGE, "<<< %s returning. line=%d.\n", __FUNCTION__, __LINE__);
  return 0;
}

/************************************************************************* 
 * send job removed event to gui                                         * 
 *************************************************************************/
static int sendJobRemoveEvent(int gui_transmission_id, JobObject * job_object)
{
  proxy_msg *msg;
  char proxy_generated_job_id_string[256];

  print_message(TRACE_MESSAGE, ">>> %s entered. line=%d. job=%s.%d.%d. state=%d.\n", __FUNCTION__, __LINE__, job_object->ll_step_id.from_host, job_object->ll_step_id.cluster, job_object->ll_step_id.proc, job_object->job_state);
  memset(proxy_generated_job_id_string, '\0', sizeof(proxy_generated_job_id_string));   /* zero the area */
  sprintf(proxy_generated_job_id_string, "%d", job_object->proxy_generated_job_id);

  msg = proxy_remove_job_event(gui_transmission_id, proxy_generated_job_id_string);
  enqueue_event_to_proxy_server(msg);
  print_message(TRACE_MESSAGE, "<<< %s returning. line=%d.\n", __FUNCTION__, __LINE__);
  return 0;
}

/************************************************************************* 
 * send task added event to gui                                          * 
 *************************************************************************/
static int sendTaskAddEvent(int gui_transmission_id, ClusterObject * cluster_object, JobObject * job_object, TaskObject * task_object)
{
  proxy_msg *msg;
  char proxy_generated_job_id_string[12];
  char proxy_generated_task_id_string[12];
  char ll_task_id_string[12];
  char *task_state_to_report = PTP_PROC_STATE_SUSPENDED;
  NodeObject *node_object = NULL;

  print_message(TRACE_MESSAGE, ">>> %s entered. line=%d. job=%s.%d.%d. node=%s. task=%d.\n", __FUNCTION__, __LINE__, job_object->ll_step_id.from_host, job_object->ll_step_id.cluster, job_object->ll_step_id.proc, task_object->node_name, task_object->ll_task_id);
  sprintf(proxy_generated_job_id_string, "%d", job_object->proxy_generated_job_id);
  sprintf(proxy_generated_task_id_string, "%d", task_object->task_id);
  sprintf(ll_task_id_string, "%d", task_object->ll_task_id);
  msg = proxy_new_process_event(gui_transmission_id, proxy_generated_job_id_string, 1); /* 1 == num tasks */

  switch (task_object->task_state) {
    case MY_STATE_IDLE:
      task_state_to_report = PTP_PROC_STATE_STARTING;
      break;
    case MY_STATE_RUNNING:
      task_state_to_report = PTP_PROC_STATE_RUNNING;
      break;
    case MY_STATE_STOPPED:
      task_state_to_report = PTP_PROC_STATE_SUSPENDED;
      break;
    case MY_STATE_TERMINATED:
      task_state_to_report = PTP_PROC_STATE_COMPLETED;
      break;
    default:
      task_state_to_report = PTP_PROC_STATE_STARTING;
      break;
  }


  proxy_add_process(msg, proxy_generated_task_id_string, ll_task_id_string, task_state_to_report, 2);   /* 2=extra attrs */

  node_object = get_node_in_hash(cluster_object->node_hash, task_object->node_name);
  proxy_add_int_attribute(msg, PTP_PROC_NODEID_ATTR, node_object->proxy_generated_node_id); /* proxy node id */
  proxy_add_int_attribute(msg, PTP_PROC_INDEX_ATTR, task_object->ll_task_id);       /* loadleveler task id */

  enqueue_event_to_proxy_server(msg);
  print_message(TRACE_MESSAGE, "<<< %s returning. line=%d.\n", __FUNCTION__, __LINE__);
  return 0;
}

/************************************************************************* 
 * send task changed event to gui                                        * 
 *************************************************************************/
static int sendTaskChangeEvent(int gui_transmission_id, JobObject * job_object, TaskObject * task_object)
{
  proxy_msg *msg;
  char jobid[12];
  char proxy_generated_task_id_string[12];
  char *task_state_to_report = PTP_PROC_STATE_STARTING;

  print_message(TRACE_MESSAGE, ">>> %s entered. line=%d.\n", __FUNCTION__, __LINE__);
  sprintf(jobid, "%d", job_object->proxy_generated_job_id);
  sprintf(proxy_generated_task_id_string, "%d", task_object->task_id);
  msg = proxy_process_change_event(gui_transmission_id, jobid, proxy_generated_task_id_string, 1);

  switch (task_object->task_state) {
    case MY_STATE_IDLE:
      task_state_to_report = PTP_PROC_STATE_STARTING;
      break;
    case MY_STATE_RUNNING:
      task_state_to_report = PTP_PROC_STATE_RUNNING;
      break;
    case MY_STATE_STOPPED:
      task_state_to_report = PTP_PROC_STATE_SUSPENDED;
      break;
    case MY_STATE_TERMINATED:
      task_state_to_report = PTP_PROC_STATE_COMPLETED;
      break;
  }

  proxy_add_string_attribute(msg, PTP_PROC_STATE_ATTR, task_state_to_report);
  enqueue_event_to_proxy_server(msg);
  print_message(TRACE_MESSAGE, "<<< %s returning. line=%d.\n", __FUNCTION__, __LINE__);
  return 0;
}

/************************************************************************* 
 * send task removed event to gui                                        * 
 *************************************************************************/
static int sendTaskRemoveEvent(int gui_transmission_id, JobObject * job_object, TaskObject * task_object)
{
  proxy_msg *msg;
  char proxy_generated_task_id_string[12];
  char jobid[12];

  print_message(TRACE_MESSAGE, ">>> %s entered. line=%d.\n", __FUNCTION__, __LINE__);
  sprintf(jobid, "%d", job_object->proxy_generated_job_id);
  sprintf(proxy_generated_task_id_string, "%d", task_object->task_id);
  msg = proxy_remove_process_event(gui_transmission_id, jobid, proxy_generated_task_id_string);
  enqueue_event_to_proxy_server(msg);
  print_message(TRACE_MESSAGE, "<<< %s returning. line=%d.\n", __FUNCTION__, __LINE__);
  return 0;
}

/************************************************************************* 
 * Generate a unique id                                                  * 
 *************************************************************************/
static int generate_id()
{
  return ibmll_proxy_base_id + ibmll_last_id++;
}

/************************************************************************* 
 * Send an event message (proxy_msg) to gui                              * 
 *************************************************************************/
static void enqueue_event_to_proxy_server(proxy_msg * msg)
{
  print_message(TRACE_MESSAGE, ">>> %s entered. line=%d.\n", __FUNCTION__, __LINE__);
  print_proxy_message(msg);
  pthread_mutex_lock(&proxy_svr_queue_msg_lock);
  proxy_svr_queue_msg(ll_proxy, msg);
  pthread_mutex_unlock(&proxy_svr_queue_msg_lock);
  print_message(TRACE_MESSAGE, "<<< %s returning. line=%d.\n", __FUNCTION__, __LINE__);
}

/************************************************************************* 
 * Print command arguments from gui command calls                        * 
 *************************************************************************/
void print_message_args(int nargs, char *optional_args[])
{
  int i = 0;
  if (optional_args != NULL) {
    while ((optional_args[i] != NULL) && (i < nargs)) {
      print_message(ARGS_MESSAGE, " '%s'", optional_args[i++]);
    }
  }
  print_message(ARGS_MESSAGE, "\n");
}

/************************************************************************* 
 * Proxy startup and command loop                                        * 
 *************************************************************************/
int main(int argc, char *argv[])
{
  char *user_libpath = NULL;    /* final LoadLeveler lib path and
                                 * shared library name */
  char *host = "localhost";
  char *proxy_str = DEFAULT_PROXY;
  int ch;
  int port = PTP_PROXY_TCP_PORT;
  int rc;
  struct passwd *pwd;
  int i = 0;
  char *cp = NULL;
  int n = 0;

  n = 0; /* prevent warning on linux for unsed var (used only on aix) */
/* preprocess input args to set debug loop and message processing 
 * before we do any further processing */

  for (i = 0; i < argc; i++) {
    if (strstr(argv[i], "debug_loop=y") != NULL) {
      state_debug_loop = 1;     /* turn on hard loop for attaching debugger */
      continue;
    }
    if (strstr(argv[i], "trace_messages=y") != NULL) {
      state_trace = 1;          /* turn on tracing messages */
      continue;
    }
    if (strstr(argv[i], "info_messages=y") != NULL) {
      state_info = 1;           /* turn on info messages */
      continue;
    }
    if (strstr(argv[i], "warning_messages=y") != NULL) {
      state_warning = 1;        /* turn on warning messages */
      continue;
    }
    if (strstr(argv[i], "error_messages=y") != NULL) {
      state_error = 1;          /* turn on error messages */
      continue;
    }
    if (strstr(argv[i], "fatal_messages=y") != NULL) {
      state_fatal = 1;          /* turn on fatal messages */
      continue;
    }
    if (strstr(argv[i], "args_messages=y") != NULL) {
      state_args = 1;           /* turn on args messages */
      continue;
    }
  }


  register_thread(pthread_self());

/* set following directive to "if 1" and recompile to turn on debug, "if 0" and recompile to turn off debug */
  if (state_debug_loop) {
    int debug_loop = 1;
    while (debug_loop > 0) {
      if ((debug_loop % 60) == 1) {
        print_message(INFO_MESSAGE, "Debug loop - attach debugger, set breakpoints and then change debug_loop to 0 to continue.\n");
      }
      debug_loop++;
      sleep(1);
    }
  }

  gethostname(hostname,sizeof(hostname));
  cp = strchr(hostname, '.');
  if (cp != NULL) {
      *cp = '\0';
  }
  
  for (i = 0; i < argc; i++) {
    print_message(INFO_MESSAGE, "Main called with arg[%d]=%s.\n", i, argv[i]);
  }

  pwd = getpwuid(getuid());
  userid = strdup(pwd->pw_name);

#ifdef __linux__  
  while ((ch = getopt_long(argc, argv, "P:p:h:l:d:t:i:w:e:f:a:m:o:r:x:y:z:", longopts, NULL)) != -1) {
    switch (ch) {
      case 'P':
        proxy_str = strdup(optarg);
        break;
      case 'p':
        port = atoi(optarg);
        break;
      case 'g':
        debug_level = atoi(optarg);
        break;
      case 'h':
        host = strdup(optarg);
        break;
      case 'l':
        user_libpath = strdup(optarg);  /* user has specified override full path to LoadLeveler shared library */
        break;
      case 'm':
        if (strncmp(optarg, "y", 1) == 0) {     /* y - multicluster forced on */
          multicluster_status = 1;      /* force multicluster */
        } else if (strncmp(optarg, "n", 1) == 0) {      /* n - multicluster forced off */
          multicluster_status = 0;      /* force local */
        } else {                /* d - default to LoadLeveler determine multicluster state */
          multicluster_status = -1;     /* allow LoadLeveler to determine mode */
        }
        break;
      case 'o':
        static_template_override = strdup(optarg);      /* user supplied template file name */
        break;
      case 'r':
        if (strncmp(optarg, "y", 1) == 0) {     /* write template file always (at main startup) */
          state_template = 1;   /* write template at every main startup */
        } else {                /* n - do not write default template file */
          state_template = 0;   /* never write template file */
        }
        break;
      case 'd':                /* already preprocessed */
      case 't':                /* already preprocessed */
      case 'i':                /* already preprocessed */
      case 'w':                /* already preprocessed */
      case 'e':                /* already preprocessed */
      case 'f':                /* already preprocessed */
      case 'a':                /* already preprocessed */
        break;
      case 'x':                /* min node polling */
        min_node_sleep_seconds = atoi(optarg);
        break;
      case 'y':                /* max node polling */
        max_node_sleep_seconds = atoi(optarg);
        break;
      case 'z':                /* job polling */
        job_sleep_seconds = atoi(optarg);
        break;
      default:
        print_message(ERROR_MESSAGE, "%s [--Proxy=proxy] [--host=host_name] [--port=port] [--debug=level] [--lib_override=directory] [--debug_loop=y|n] [--trace_messages=y|n] [--info_messages=y|n] [--warning_messages=y|n] [--error_messages=y|n] [--fatal_messages=y|n] [--args_messages=y|n] [--multicluster=d|n|y] [--template_override=file] [--template_write=y|n] --node_polling_min=value --node_polling_max=value --job_polling=value \n", argv[0]);
        fflush(stderr);
        return 1;
    }
  }
#else
 /* AIX does not have the getopt_long function. Since the proxy is
  * invoked only by the PTP front end, the following simplified options
  * parsing is sufficient.
  * Make sure that this is maintained in sync with the above
  * getopt_long loop.
  */
n = 1;
while (n < argc) {
 cp = strchr(argv[n], '=');
 if (cp != NULL) {
     *cp = '\0';
     if (strcmp(argv[n], "--proxy") == 0) {
         proxy_str = strdup(cp + 1);
         n = n + 1;
     }
     else if (strcmp(argv[n], "--port") == 0) {
         port = atoi(cp + 1);
         n = n + 1;
     }
     else if (strcmp(argv[n], "--host") == 0) {
         host = strdup(cp + 1);
         n = n + 1;
     }
     else if (strcmp(argv[n], "--lib_override") == 0) {
    	 user_libpath = strdup(cp + 1);  /* user has specified override full path to LoadLeveler shared library */
              n = n + 1;
          }
     else if (strcmp(argv[n], "--multicluster") == 0) {
    	 if (strncmp(cp + 1, "y", 1) == 0) {     /* y - multicluster forced on */
    	           multicluster_status = 1;      /* force multicluster */
    	         } else if (strncmp(cp + 1, "n", 1) == 0) {      /* n - multicluster forced off */
    	           multicluster_status = 0;      /* force local */
    	         } else {                /* d - default to LoadLeveler determine multicluster state */
    	           multicluster_status = -1;     /* allow LoadLeveler to determine mode */
    	         }
                   n = n + 1;
               }
     else if (strcmp(argv[n], "--template_override") == 0) {
    	 static_template_override = strdup(cp + 1);      /* user supplied template file name */
                   n = n + 1;
               }
     else if (strcmp(argv[n], "--template_write") == 0) {
    	 if (strncmp(cp + 1, "y", 1) == 0) {     /* write template file always (at main startup) */
    	           state_template = 1;   /* write template at every main startup */
    	         } else {                /* n - do not write default template file */
    	           state_template = 0;   /* never write template file */
    	         }
                   n = n + 1;
               }
     else if ((strcmp(argv[n], "--debug_loop") == 0) || /* already preprocessed */
    	      (strcmp(argv[n], "--trace_messages") == 0) || /* already preprocessed */
    	      (strcmp(argv[n], "--info_messages") == 0) || /* already preprocessed */
    	      (strcmp(argv[n], "--warning_messages") == 0) || /* already preprocessed */
    	      (strcmp(argv[n], "--error_messages") == 0) || /* already preprocessed */
    	      (strcmp(argv[n], "--fatal_messages") == 0) || /* already preprocessed */
    		  (strcmp(argv[n], "--args_messages") == 0)) { /* already preprocessed */
                   n = n + 1;
               }
     else if (strcmp(argv[n], "--node_polling_min") == 0) {
    	 min_node_sleep_seconds = atoi(cp + 1);
                   n = n + 1;
               }
     else if (strcmp(argv[n], "--node_polling_max") == 0) {
    	 max_node_sleep_seconds = atoi(cp + 1);
                   n = n + 1;
               }
     else if (strcmp(argv[n], "--debug") == 0) {
     	    debug_level = atoi(cp + 1);
     		n = n + 1;
     	    }
     else if (strcmp(argv[n], "--job_polling") == 0) {
    	  job_sleep_seconds = atoi(cp + 1);
                   n = n + 1;
               }
     else {
         print_message(ERROR_MESSAGE, "Invalid argument %s (%d)\n", argv[n], n);
         print_message(ERROR_MESSAGE, "%s [--Proxy=proxy] [--host=host_name] [--port=port] [--lib_override=directory] [--debug_loop=y|n] [--trace_messages=y|n] [--info_messages=y|n] [--warning_messages=y|n] [--error_messages=y|n] [--fatal_messages=y|n] [--args_messages=y|n] [--multicluster=d|n|y] [--template_override=file] [--template_write=y|n] --node_polling_min=value --node_polling_max=value --job_polling=value \n", argv[0]);
               fflush(stderr);
         return 1;
     }
 }
}
 
#endif
  ptp_signal_exit = 0;
  ptp_signal_thread = -1;
  signal(SIGINT, ptp_signal_handler);
  signal(SIGHUP, ptp_signal_handler);
  signal(SIGILL, ptp_signal_handler);
  signal(SIGSEGV, ptp_signal_handler);
  signal(SIGTERM, ptp_signal_handler);
  signal(SIGQUIT, ptp_signal_handler);
  signal(SIGABRT, ptp_signal_handler);
  rc = server(proxy_str, host, port, user_libpath);
  state_shutdown_requested = 1;
  exit(rc);
}

/************************************************************************* 
 * Handle a signal                                                       * 
 *************************************************************************/
RETSIGTYPE ptp_signal_handler(int sig)
{
  ptp_signal_exit = sig;
  ptp_signal_thread = find_thread(pthread_self());
  if (sig >= 0 && sig < NSIG) {
    RETSIGTYPE(*saved_signal) (int) = saved_signals[sig];
    if (saved_signal != SIG_ERR && saved_signal != SIG_IGN && saved_signal != SIG_DFL) {
      saved_signal(sig);
    }
  }
}


/************************************************************************* 
 * Print message (with time and date and thread id)                      * 
 * Info, Trace, Arg and Warning messages go to stdout.                   * 
 * Error and Fatal messages go to stderr.                                * 
 *************************************************************************/
static void print_message(int type, const char *format, ...)
{
  va_list ap;
  char timebuf[20];
  time_t my_clock;
  struct tm a_tm;
  int thread_id = 0;

  memset(timebuf, '\0', sizeof(timebuf));       /* zero the area */
  pthread_mutex_lock(&print_message_lock);
  pthread_t tid = pthread_self();       /* what thread am I ? */
  for (thread_id = 0; thread_id < (sizeof(thread_map_table) / sizeof(pthread_t)); thread_id++) {
    if (tid == thread_map_table[thread_id]) {
      break;
    }
  }
  time(&my_clock);                 /* what time is it ? */
  localtime_r(&my_clock, &a_tm);
  strftime(&timebuf[0], 15, "%m/%d %02H:%02M:%02S", &a_tm);

  va_start(ap, format);
  switch (type) {
    case INFO_MESSAGE:
      if (state_info == 1) {    /* if info messages allowed */
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
      if (state_trace == 1) {   /* if trace messages allowed */
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
      if (state_warning == 1) { /* if warning messages allowed */
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
      if (state_error == 1) {   /* if error messages allowed */
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
    case FATAL_MESSAGE:        /* fatal messages are never suppressed */
      if (state_fatal == 1) {   /* if fatal messages allowed */
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
      if (state_args == 1) {    /* if formatted arg messages are allowed */
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
    default:                   /* unknown message type - allow it */
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

/************************************************************************* 
 * main proxy processing                                                 * 
 *************************************************************************/
int server(char *name, char *host, int port, char *user_libpath)
{
/* 
 * Initialize the proxy, connect to front end, then run the main loop
 * until the proxy is requested to shut down.
 */
  char shared_buffer[1024];
  char *msg1 = NULL;
  char *msg2 = NULL;
  int rc = 0;
  struct timeval timeout = {
    0, 20000
  };
  int lib_found = 0;
  int status = 0;
  struct stat statinfo;
  int i = 0;
  int save_errno = 0;
  int connect_rc=0;
#ifdef  __linux__
  char *libpath[] = {
    NULL, "/opt/ibmll/LoadL/full/lib/", "/opt/ibmll/LoadL/so/lib/", "/opt/ibmll/LoadL/scheduler/full/lib", (char *) -1
  };
  char *libname = "libllapi.so";
#else
  char *libpath[] = {
    NULL, "/usr/lpp/LoadL/full/lib", "/usr/lpp/LoadL/so/lib", "/opt/ibmll/LoadL/full/lib/", "/opt/ibmll/LoadL/so/lib/", (char *) -1
  };
  char *libname = "libllapi.a";
#endif
  int template_write = 1;       /* preset to write */
  FILE *template_FILE = NULL;
  int template_count = 0;

  memset(shared_buffer, '\0', sizeof(shared_buffer));   /* zero the area */
  memset(static_template_name, '\0', sizeof(static_template_name));     /* zero the area */

  /*-----------------------------------------------------------------------* 
   *                                                                       * 
   * Find the LoadLeveler shared library we are using for AIX or Linux.    *
   * If we cannot find it then LoadLeveler is not installed or we have     * 
   *  been directed to use the wrong path.  If we find it OK then we will  *
   * try to dynamic open the library and compare versions later in the     *
   * command_initialize.                                                   * 
   *-----------------------------------------------------------------------*/
  if (user_libpath != NULL) {   /* if user specified lib */
    if (strlen(user_libpath) > 0) {     /* if not a null string */
      libpath[0] = user_libpath;        /* pick up user specified libpath
                                         * as only one to check */
      libpath[1] = (char *) -1; /* new end of list */
    }
  }

  lib_found = 0;                /* preset to not found */
  print_message(INFO_MESSAGE, "Searching for LoadLeveler shared library.\n");
  while ((libpath[i] != (char *) -1) && (lib_found == 0)) {     /* if not end of list and not yet found */
    if (libpath[i] != NULL) {   /* if valid entry */
      strcpy(shared_buffer, libpath[i]);
      strcat(shared_buffer, "/");
      strcat(shared_buffer, libname);
    /* see if this is a valid LoadLeveler shared library */
      print_message(INFO_MESSAGE, "Trying: %s\n", shared_buffer);
      status = stat(shared_buffer, &statinfo);
      save_errno = errno;
      if (status == 0) {
#ifdef _AIX
      strcat(shared_buffer,"(shr.o)");
#endif
        ibmll_libpath_name = strdup(shared_buffer);
        print_message(INFO_MESSAGE, "Successful search: Found LoadLeveler shared library %s\n", ibmll_libpath_name);
        lib_found = 1;          /* we found it */
        break;
      } else {
        print_message(ERROR_MESSAGE, "Search failure: \"stat\" of LoadLeveler shared library %s returned errno=%d.\n", shared_buffer, save_errno);
      }
    }
    i++;
  }

  if (lib_found == 0) {
    print_message(FATAL_MESSAGE, "No LoadLeveler shared library found - quitting...\n");
    state_shutdown_requested = 1;
    exit(99);
  }

  /*-----------------------------------------------------------------------* 
   * If allowed, write the template file to /tmp                           *
   * Template file will be saved with userid as                            *
   * /tmp/PTP_IBMLL_TEMPLATE_userid                                        *
   *-----------------------------------------------------------------------*/
  switch (state_template) {
    case 0:                    /* do not write template file */
      template_write = 0;
      break;
    case 1:                    /* always write template file */
    default:
      template_write = 1;
      break;
  }

  if (template_write == 1) {
    strcpy(&static_template_name[0], static_template_prefix);
    strcpy(&static_template_name[strlen(static_template_prefix)], userid);
    print_message(INFO_MESSAGE, "Writing job command template file %s.\n", static_template_name);

    template_FILE = fopen(static_template_name, "w");   /* reset file length to 0 or create file */
    template_count = sizeof(job_command_file_template) / sizeof(job_command_file_template[0]);

    for (i = 0; i < template_count; i++) {
      fputs(job_command_file_template[i], template_FILE);
      fputs("\n", template_FILE);
    }
    fclose(template_FILE);

  }

  /*-----------------------------------------------------------------------* 
   * continue proxy initialization                                         *
   *-----------------------------------------------------------------------*/
  events = NewList();
  if (proxy_svr_init(name, &timeout, &helper_funcs, &command_tab, &ll_proxy) != PTP_PROXY_RES_OK) {
    return 0;
  }

  if ((connect_rc = proxy_svr_connect(ll_proxy, host, port)) == PTP_PROXY_RES_OK) {
  print_message(INFO_MESSAGE, "Running proxy on port %d.\n", port);
  while (ptp_signal_exit == 0 && state_shutdown_requested == 0) {
    if ((proxy_svr_progress(ll_proxy) != PTP_PROXY_RES_OK)) {
      print_message(INFO_MESSAGE, "Ending node monitor loop\n");
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
    print_message(FATAL_MESSAGE, "###### SIGNAL: %i (%s) detected in T(%i)\n", ptp_signal_exit, msg1, ptp_signal_thread);
    print_message(FATAL_MESSAGE, "###### Shutting down proxy\n");
    print_message(FATAL_MESSAGE, "ptp_ibmll_proxy received signal %s (%s) and is exiting.\n", msg1, msg2);
  /* our return code = the signal that fired */
    rc = ptp_signal_exit;
  }
  }
  else {
   print_message(ERROR_MESSAGE,"proxy connection failed. rc=%d\n",connect_rc);
  }
  proxy_svr_finish(ll_proxy);
  print_message(INFO_MESSAGE, "proxy_svr_finish returned.\n");
  state_shutdown_requested = 1;
  return rc;
}

/************************************************************************* 
 * Call LoadLeveler to retrieve the cluster element                      * 
 *************************************************************************/
int my_ll_cluster(int version, LL_element ** errObj, LL_cluster_param * cp)
{
  int rc = 0;
  print_message(TRACE_MESSAGE, ">>> %s entered. line=%d. version=%d.\n", __FUNCTION__, __LINE__, version);
  pthread_mutex_lock(&access_LoadLeveler_lock);
  rc = (*LL_SYMS.ll_cluster) (version, errObj, cp);     /* set the cluster name */
  pthread_mutex_unlock(&access_LoadLeveler_lock);
  if (rc != 0) {
    print_message(INFO_MESSAGE, "LoadLeveler ll_cluster rc=%d.\n", rc);
  }
  print_message(TRACE_MESSAGE, "<<< %s returning. line=%d. rc=%d.\n", __FUNCTION__, __LINE__, rc);
  return rc;
}

/************************************************************************* 
 * Call LoadLeveler to get data                                          * 
 *************************************************************************/
int my_ll_get_data(LL_element * request, enum LLAPI_Specification spec, void *result)
{
  int rc = 0;
  print_message(TRACE_MESSAGE, ">>> %s entered. line=%d. LLAPI_Specification=%d.\n", __FUNCTION__, __LINE__, spec);
  pthread_mutex_lock(&access_LoadLeveler_lock);
  rc = (*LL_SYMS.ll_get_data) (request, spec, result);
  pthread_mutex_unlock(&access_LoadLeveler_lock);
  if (rc != 0) {
    print_message(INFO_MESSAGE, "LoadLeveler ll_get_data rc=%d.\n", rc);
  }
  print_message(TRACE_MESSAGE, "<<< %s returning. line=%d. rc=%d.\n", __FUNCTION__, __LINE__, rc);
  return rc;
}

/************************************************************************* 
 * Call LoadLeveler to perform a query                                   * 
 *************************************************************************/
LL_element *my_ll_query(enum QueryType type)
{
  LL_element *query_elem = NULL;
  print_message(TRACE_MESSAGE, ">>> %s entered. line=%d. QueryType=%d.\n", __FUNCTION__, __LINE__, type);
  pthread_mutex_lock(&access_LoadLeveler_lock);
  query_elem = (*LL_SYMS.ll_query) (type);
  pthread_mutex_unlock(&access_LoadLeveler_lock);
  if (query_elem == NULL) {
    print_message(INFO_MESSAGE, "LoadLeveler ll_query element=NULL. End of list was probably reached.\n");
  }
  print_message(TRACE_MESSAGE, "<<< %s returning. line=%d.\n", __FUNCTION__, __LINE__);
  return query_elem;
}

/************************************************************************* 
 * Call LoadLeveler to free the objects in the query element             * 
 *************************************************************************/
int my_ll_free_objs(LL_element * query_elem)
{
  int rc = 0;
  print_message(TRACE_MESSAGE, ">>> %s entered. line=%d.\n", __FUNCTION__, __LINE__);
  pthread_mutex_lock(&access_LoadLeveler_lock);
  rc = (*LL_SYMS.ll_free_objs) (query_elem);
  pthread_mutex_unlock(&access_LoadLeveler_lock);
  if (rc != 0) {
    print_message(ERROR_MESSAGE, "LoadLeveler ll_free_objs rc=%d.\n", rc);
  }
  print_message(TRACE_MESSAGE, "<<< %s returning. line=%d. rc=%d.\n", __FUNCTION__, __LINE__, rc);
  return rc;
}

/************************************************************************* 
 * Call LoadLeveler to deallocate the query element                      * 
 *************************************************************************/
int my_ll_deallocate(LL_element * query_elem)
{
  int rc = 0;
  print_message(TRACE_MESSAGE, ">>> %s entered. line=%d.\n", __FUNCTION__, __LINE__);
  pthread_mutex_lock(&access_LoadLeveler_lock);
  rc = (*LL_SYMS.ll_deallocate) (query_elem);
  pthread_mutex_unlock(&access_LoadLeveler_lock);
  if (rc != 0) {
    print_message(ERROR_MESSAGE, "LoadLeveler ll_deallocate rc=%d.\n", rc);
  }
  print_message(TRACE_MESSAGE, "<<< %s returning. line=%d. rc=%d.\n", __FUNCTION__, __LINE__, rc);
  return rc;
}

/************************************************************************* 
 * Call LoadLeveler to get the next object from the returned list        * 
 *************************************************************************/
LL_element *my_ll_next_obj(LL_element * query_elem)
{
  LL_element *next_elem = NULL;;
  print_message(TRACE_MESSAGE, ">>> %s entered. line=%d.\n", __FUNCTION__, __LINE__);
  pthread_mutex_lock(&access_LoadLeveler_lock);
  next_elem = (*LL_SYMS.ll_next_obj) (query_elem);
  pthread_mutex_unlock(&access_LoadLeveler_lock);
  if (next_elem == NULL) {
    print_message(INFO_MESSAGE, "LoadLeveler ll_next_obj element=NULL. End of list was probably reached.\n");
  }
  print_message(TRACE_MESSAGE, "<<< %s returning. line=%d.\n", __FUNCTION__, __LINE__);
  return next_elem;
}

/************************************************************************* 
 * Call LoadLeveler to get objects from the previously returned element  * 
 *************************************************************************/
LL_element *my_ll_get_objs(LL_element * query_elem, enum LL_Daemon daemon, char *ignore, int *value, int *rc)
{
  *rc = 0;                      /* preset rc to 0 */
  LL_element *ret_object = NULL;
  print_message(TRACE_MESSAGE, ">>> %s entered. line=%d. LL_Daemon=%d.\n", __FUNCTION__, __LINE__, daemon);
  pthread_mutex_lock(&access_LoadLeveler_lock);
  ret_object = LL_SYMS.ll_get_objs(query_elem, daemon, ignore, value, rc);
  pthread_mutex_unlock(&access_LoadLeveler_lock);
  if (ret_object == NULL) {
    print_message(INFO_MESSAGE, "LoadLeveler ll_get_objs element=NULL. End of list was probably reached.\n");
  }
  print_message(TRACE_MESSAGE, "<<< %s returning. line=%d. rc=%d.\n", __FUNCTION__, __LINE__, *rc);
  return ret_object;
}

/************************************************************************* 
 *  Call LoadLeveler to build a request object for a subsequent call     * 
 *************************************************************************/
int my_ll_set_request(LL_element * query_elem, enum QueryFlags qflags, char **ignore, enum DataFilter dfilter)
{
  int rc = 0;
  print_message(TRACE_MESSAGE, ">>> %s entered. line=%d. QueryFlags=%d.\n", __FUNCTION__, __LINE__, qflags);
  pthread_mutex_lock(&access_LoadLeveler_lock);
  rc = LL_SYMS.ll_set_request(query_elem, qflags, ignore, dfilter);
  pthread_mutex_unlock(&access_LoadLeveler_lock);
  if (rc != 0) {
    print_message(ERROR_MESSAGE, "LoadLeveler ll_set_request rc=%i.\n", rc);
  }
  print_message(TRACE_MESSAGE, "<<< %s returning. line=%d. rc=%d.\n", __FUNCTION__, __LINE__, rc);
  return rc;
}

/************************************************************************* 
 * Call LoadLeveler to submit a job                                      * 
 *                                                                       * 
 * In the proxy we are accepting environment variables to be passed on   * 
 * to submit by forking the submit and making sure we se the env vars    * 
 * into the environment.                                                 * 
 *************************************************************************/
int my_ll_submit_job(int gui_transmission_id, char *job_sub_id, char *command_file, char *job_env_vars[][3])
{
  LL_job job_info;
  LL_job_step *job_step_info;
  JobObject *job_object = NULL;
  int submit_rc = 0;
  LL_cluster_param cluster_parm;
  LL_element *errObj = NULL;
  int i = 0;
  ClusterObject *cluster_object = NULL;
  ListElement *cluster_list_element = NULL;
  time_t my_clock;
  char *tempstring = NULL;

  print_message(TRACE_MESSAGE, ">>> %s entered. line=%d.\n", __FUNCTION__, __LINE__);
  memset(&job_info, '\0', sizeof(job_info));    /* zero the job info area */

  pthread_mutex_lock(&master_lock);
  cluster_list_element = cluster_list->l_head;  /* always submit to the first cluster */
  cluster_object = cluster_list_element->l_value;       /* get our cluster object from this list element */

  /*-----------------------------------------------------------------------* 
   * always submit local regardless if running multicluster or not         * 
   *-----------------------------------------------------------------------*/
  print_message(INFO_MESSAGE, "Setting access for LoadLeveler local cluster (single cluster).\n");
  cluster_parm.action = CLUSTER_UNSET;  /* we are unsetting the cluster */
  cluster_parm.cluster_list = NULL;
  submit_rc = my_ll_cluster(LL_API_VERSION, &errObj, &cluster_parm);    /* unset the cluster name - back to local */

  pthread_mutex_lock(&access_LoadLeveler_lock);

  /*-----------------------------------------------------------------------* 
   * retrieve environment variables                                        * 
   *-----------------------------------------------------------------------*/
  i = 0;
  while (job_env_vars[i][0] != NULL) {  /* if keyword */
    tempstring = getenv(job_env_vars[i][0]);
    if (tempstring != NULL) {
      print_message(INFO_MESSAGE, "Retrieving env var %s=%s.\n", job_env_vars[i][0], tempstring);
      job_env_vars[i][2] = strdup(tempstring);  /* if old value */
    }
    i++;
  }

  /*-----------------------------------------------------------------------* 
   * write new environment variables                                       * 
   *-----------------------------------------------------------------------*/
  i = 0;
  while (job_env_vars[i][0] != NULL) {  /* if keyword */
    if (job_env_vars[i][1] != NULL) {   /* if new value */
      if ((job_env_vars[i][2] == NULL) ||       /* if no old val */
          ((job_env_vars[i][2] != NULL) &&      /* or if we had old value and different than new */
           (strcmp(job_env_vars[i][1], job_env_vars[i][2]) != 0))) {    /* and if old and new not identical */
        print_message(INFO_MESSAGE, "Setting env var %s=%s.\n", job_env_vars[i][0], job_env_vars[i][1]);
        setenv(job_env_vars[i][0], job_env_vars[i][1], 1);      /* set the new value */
      }
    }
    i++;
  }

  /*-----------------------------------------------------------------------* 
   * submit job                                                            * 
   *-----------------------------------------------------------------------*/
  submit_rc = LL_SYMS.ll_submit_job(command_file, NULL, NULL, &job_info, LL_JOB_VERSION);
  if (submit_rc == -1) {
    char *theError = LL_SYMS.ll_error(NULL, 3); /* print any internal errors */
    print_message(ERROR_MESSAGE, "Internal errors (if any) returned by LoadLeveler submit API:\n%s\n\n", theError);
  }

  /*-----------------------------------------------------------------------* 
   * restore environment variables                                         * 
   *-----------------------------------------------------------------------*/
  i = 0;

  while (job_env_vars[i][0] != NULL) {  /* if keyword */
    if (job_env_vars[i][1] != NULL) {   /* if new value */

      if (job_env_vars[i][2] != NULL) { /* if we had old value */
        if (strcmp(job_env_vars[i][1], job_env_vars[i][2]) != 0) {      /* and if old and new not identical */
          print_message(INFO_MESSAGE, "Restoring env var %s=%s.\n", job_env_vars[i][0], job_env_vars[i][2]);
          setenv(job_env_vars[i][0], job_env_vars[i][2], 1);    /* restore old value back into environment */
        }
      } /* end if we had old value */
      else {                    /* if new only - no old */
        print_message(INFO_MESSAGE, "Unsetting env var %s.\n", job_env_vars[i][0]);
        unsetenv(job_env_vars[i][0]);   /* unset new value we put in environment */
      }                         /* end if new value only */

    }                           /* end if we had a new value */
    i++;
  }                             /* end while loop on keywords */

  pthread_mutex_unlock(&access_LoadLeveler_lock);
  if (submit_rc != 0) {
    print_message(ERROR_MESSAGE, "LoadLeveler ll_submit_job rc=%i.\n", submit_rc);
  } else {
    print_message(INFO_MESSAGE, "Job Submitted: job_name=%s submithost=%s owner=%s steps=%d.\n", job_info.job_name, job_info.submit_host, job_info.owner, job_info.steps);

    time(&my_clock);               /* what time is it ? */

    for (i = 0; i < job_info.steps; i++) {
      job_step_info = job_info.step_list[i];
      print_message(INFO_MESSAGE, "Job step:  step_name=%s. step_class=%s. step_id=%s.%d.%d\n", job_step_info->step_name, job_step_info->stepclass, job_step_info->id.from_host, job_step_info->id.cluster, job_step_info->id.proc);

    /*-----------------------------------------------------------------------* 
     * create new job object for submitted job                               * 
     *-----------------------------------------------------------------------*/
      job_object = (JobObject *) malloc(sizeof(JobObject));
      malloc_check(job_object, __FUNCTION__, __LINE__);
      memset(job_object, '\0', sizeof(job_object));     /* zero the malloc area */
      job_object->proxy_generated_job_id = generate_id();       /* a unique identifier for this cluster */
      job_object->task_counter = 0;
      job_object->gui_assigned_job_id = "-1";   /* preset to async id (2 - n) */
      if (i == 0) {             /* if first */
        job_object->gui_assigned_job_id = job_sub_id;   /* pick up the parsed jobid for the first jobstep in job */
      }                         /* end if first */
      job_object->ll_step_id.from_host = strdup(job_step_info->id.from_host);
      job_object->ll_step_id.cluster = job_step_info->id.cluster;
      job_object->ll_step_id.proc = job_step_info->id.proc;
      job_object->task_list = NewList();        /* list to hold tasks for this job step */
      job_object->cluster_name = strdup(cluster_object->cluster_name);
      job_object->job_state = MY_STATE_IDLE;
      job_object->job_submit_time = my_clock;      /* time since epoch when job submitted */
      add_job_to_list(job_list, (void *) job_object);   /* add the new job object to the list */
      print_message(INFO_MESSAGE, "Schedule event notification: Job=%s.%d.%d added for LoadLeveler Cluster=%s.\n", job_object->ll_step_id.from_host, job_object->ll_step_id.cluster, job_object->ll_step_id.proc, job_object->cluster_name);
      sendJobAddEvent(start_events_gui_transmission_id, cluster_object, job_object);
    }

    my_ll_free_job_info(&job_info);
  }

  pthread_mutex_unlock(&master_lock);
  print_message(TRACE_MESSAGE, "<<< %s returning. line=%d. rc=%d.\n", __FUNCTION__, __LINE__, submit_rc);
  return submit_rc;
}

/************************************************************************* 
 * Call LoadLeveler to cancel a job                                      * 
 *************************************************************************/
int my_ll_terminate_job(int gui_transmission_id, JobObject * job_object)
{
  LL_terminate_job_info terminate_job_info;
  int rc = 0;
  LL_cluster_param cluster_parm;
  LL_element *errObj = NULL;
  char *remote_cluster[2];

  print_message(TRACE_MESSAGE, ">>> %s entered. line=%d.\n", __FUNCTION__, __LINE__);
  memset(&terminate_job_info, '\0', sizeof(terminate_job_info));        /* zero the terminate job info area */

  pthread_mutex_lock(&master_lock);
  if (multicluster_status == 1) {       /* if running multicluster */

   /*-----------------------------------------------------------------------* 
    * we are running multicluster - set cluster name into environment       * 
    * to influence where LoadLeveler searches for data (what cluster)       * 
    *-----------------------------------------------------------------------*/
    remote_cluster[0] = job_object->cluster_name;
    remote_cluster[1] = NULL;
    print_message(INFO_MESSAGE, "Setting access for LoadLeveler cluster %s.\n", job_object->cluster_name);
    cluster_parm.action = CLUSTER_SET;  /* we are setting the cluster for remote access */
    cluster_parm.cluster_list = remote_cluster; /* cluster name we want to cancel job in */
    rc = my_ll_cluster(LL_API_VERSION, &errObj, &cluster_parm); /* set the cluster name */
  } /* end if this is not a local cluster */
  else {                        /* this is a local cluster */

   /*-----------------------------------------------------------------------* 
    * not running multicluster                                              * 
    *-----------------------------------------------------------------------*/
    print_message(INFO_MESSAGE, "Setting access for LoadLeveler local cluster (single cluster).\n");
    cluster_parm.action = CLUSTER_UNSET;        /* we are unsetting the cluster */
    cluster_parm.cluster_list = NULL;
    rc = my_ll_cluster(LL_API_VERSION, &errObj, &cluster_parm); /* unset the cluster name - back to local */
  }                             /* end if this is a local cluster */

  /*-----------------------------------------------------------------------* 
   * send the ll cancel command and hope that it eventually works          * 
   *-----------------------------------------------------------------------*/
  terminate_job_info.version_num = LL_PROC_VERSION;
  terminate_job_info.StepId.cluster = job_object->ll_step_id.cluster;   /* old notation for job number */
  terminate_job_info.StepId.proc = job_object->ll_step_id.proc; /* old notation for step number */
  terminate_job_info.StepId.from_host = job_object->ll_step_id.from_host;       /* old notation for originating schedd node name */
  terminate_job_info.msg = NULL;        /* message to use */
  pthread_mutex_lock(&access_LoadLeveler_lock);
  rc = LL_SYMS.ll_terminate_job(&terminate_job_info);
  pthread_mutex_unlock(&access_LoadLeveler_lock);
  if (rc != 0) {
    print_message(ERROR_MESSAGE, "LoadLeveler ll_terminate_job rc=%i.\n", rc);
  }

  pthread_mutex_unlock(&master_lock);
  print_message(TRACE_MESSAGE, "<<< %s returning. line=%d. rc=%d.\n", __FUNCTION__, __LINE__, rc);
  return rc;
}

/************************************************************************* 
 * call LoadLeveler to free job info                                     * 
 *************************************************************************/
void my_ll_free_job_info(LL_job * job_info)
{
  print_message(TRACE_MESSAGE, ">>> %s entered. line=%d.\n", __FUNCTION__, __LINE__);
  LL_SYMS.ll_free_job_info(job_info, LL_JOB_VERSION);
  print_message(TRACE_MESSAGE, "<<< %s returning. line=%d.\n", __FUNCTION__, __LINE__);
}

/************************************************************************* 
 * find node in my hash table                                            * 
 *************************************************************************/
NodeObject *get_node_in_hash(Hash * node_hash, char *node_name)
{
  int hash_key = 0;
  List *node_list = NULL;
  ListElement *node_list_element = NULL;
  NodeObject *node_object = NULL;

  print_message(TRACE_MESSAGE, ">>> %s entered. line=%d. name=%s.\n", __FUNCTION__, __LINE__, node_name);
  hash_key = HashCompute(node_name, strlen(node_name));
  node_list = HashSearch(node_hash, hash_key);
  if (node_list != NULL) {
    node_list_element = node_list->l_head;      /* beginning of the list */
    while (node_list_element != NULL) {
      node_object = node_list_element->l_value;
      node_list_element = node_list_element->l_next;    /* prepare for next pass */
      if (strcmp(node_name, node_object->node_name) == 0) {
        print_message(TRACE_MESSAGE, "<<< %s returning. line=%d. node_object=x\'%08x\'.\n", __FUNCTION__, __LINE__, node_object);
        return node_object;
      }
    }
  }
  node_object = NULL;           /* not found in list */
  print_message(TRACE_MESSAGE, "<<< %s returning. line=%d. node_object=x\'%08x\'.\n", __FUNCTION__, __LINE__, node_object);
  return node_object;
}

/************************************************************************* 
 * add node to my hash table                                             * 
 *************************************************************************/
void add_node_to_hash(Hash * node_hash, NodeObject * node_object)
{
  int hash_key;
  List *node_list;

  print_message(TRACE_MESSAGE, ">>> %s entered. line=%d. object=x\'%08x\'.\n", __FUNCTION__, __LINE__, node_object);
  hash_key = HashCompute(node_object->node_name, strlen(node_object->node_name));
  node_list = HashSearch(node_hash, hash_key);
  if (node_list == NULL) {
    node_list = NewList();
    AddToList(node_list, node_object);
    HashInsert(node_hash, hash_key, node_list);
  } else {
    AddToList(node_list, node_object);
  }
  print_message(TRACE_MESSAGE, "<<< %s returning. line=%d.\n", __FUNCTION__, __LINE__);
}

/************************************************************************* 
 * find job in my list                                                   * 
 *************************************************************************/
JobObject *get_job_in_list(List * job_list, LL_STEP_ID ll_step_id)
{
  ListElement *job_list_element = NULL;
  JobObject *job_object = NULL;

  print_message(TRACE_MESSAGE, ">>> %s entered. line=%d. name=%s.%d.%d.\n", __FUNCTION__, __LINE__, ll_step_id.from_host, ll_step_id.cluster, ll_step_id.proc);
  job_list_element = job_list->l_head;  /* beginning of the list */
  while (job_list_element != NULL) {
    job_object = job_list_element->l_value;
    job_list_element = job_list_element->l_next;        /* prepare for next pass */
    if ((strcmp(ll_step_id.from_host, job_object->ll_step_id.from_host) == 0) && (ll_step_id.cluster == job_object->ll_step_id.cluster) && (ll_step_id.proc == job_object->ll_step_id.proc)) {
      print_message(TRACE_MESSAGE, "<<< %s returning. line=%d. job_object=x\'%08x\'.\n", __FUNCTION__, __LINE__, job_object);
      return job_object;
    }
  }
  job_object = NULL;            /* not found in list */
  print_message(TRACE_MESSAGE, "<<< %s returning. line=%d. job_object=x\'%08x\'.\n", __FUNCTION__, __LINE__, job_object);
  return job_object;
}

JobObject *get_job_in_list_from_id(List * job_list, int job_id)
{
  ListElement *job_list_element = NULL;
  JobObject *job_object = NULL;
  print_message(TRACE_MESSAGE, ">>> %s entered. line=%d. id=%d.\n", __FUNCTION__, __LINE__, job_id);
  job_list_element = job_list->l_head;  /* beginning of the list */
  while (job_list_element != NULL) {
    job_object = job_list_element->l_value;
    job_list_element = job_list_element->l_next;        /* prepare for next pass */
    if (job_id == job_object->proxy_generated_job_id) {
      print_message(TRACE_MESSAGE, "<<< %s returning. line=%d. job_object=x\'%08x\'.\n", __FUNCTION__, __LINE__, job_object);
      return job_object;
    }
  }
  job_object = NULL;            /* not found in list */
  print_message(TRACE_MESSAGE, "<<< %s returning. line=%d. job_object=x\'%08x\'.\n", __FUNCTION__, __LINE__, job_object);
  return job_object;
}

/************************************************************************* 
 * add job to my list                                                    * 
 *************************************************************************/
void add_job_to_list(List * job_list, JobObject * job_object)
{
  print_message(TRACE_MESSAGE, ">>> %s entered. line=%d. job_object=x\'%08x\'.\n", __FUNCTION__, __LINE__, job_object);
  AddToList(job_list, job_object);
  print_message(TRACE_MESSAGE, "<<< %s returning. line=%d.\n", __FUNCTION__, __LINE__);
}

/************************************************************************* 
 * save real thread number for mapping to useable number (0, 1, 2, ...)  * 
 *************************************************************************/
int register_thread(pthread_t handle)
{
  int thread_id = -1;

  for (thread_id = 0; thread_id < (sizeof(thread_map_table) / sizeof(pthread_t)); thread_id++) {
    if (thread_map_table[thread_id] == 0) {
      thread_map_table[thread_id] = handle;
      break;
    }
  }
  return thread_id;
}

/************************************************************************* 
 * map my threads to useable number (0, 1, 2, ...)                       * 
 *************************************************************************/
int find_thread(pthread_t handle)
{
  int thread_id = -1;

  for (thread_id = 0; thread_id < (sizeof(thread_map_table) / sizeof(pthread_t)); thread_id++) {
    if (thread_map_table[thread_id] == handle) {
      break;
    }
  }
  return thread_id;
}

/************************************************************************* 
 * verify that malloc didn't fail                                        * 
 *************************************************************************/
void malloc_check(void *p, const char *function, int line)
{
  if (p == NULL) {
    print_message(ERROR_MESSAGE, "Memory allocation error in function %s (line %d)\n", function, line);
    state_shutdown_requested = 1;
    exit(1);
  }

}

/************************************************************************* 
 * find task in my list                                                  * 
 *************************************************************************/
TaskObject *get_task_in_list(List * task_list, char *task_instance_machine_name, int ll_task_id)
{
  TaskObject *task_object = NULL;
  ListElement *task_list_element = NULL;

  print_message(TRACE_MESSAGE, ">>> %s entered. line=%d. id=%d.\n", __FUNCTION__, __LINE__, ll_task_id);
  task_list_element = task_list->l_head;        /* beginning of the list */
  while (task_list_element != NULL) {   /* while we have a valid task element */
    task_object = task_list_element->l_value;
    task_list_element = task_list_element->l_next;      /* prepare for next pass */
    if ((ll_task_id == task_object->ll_task_id) && (strcmp(task_object->node_name, task_instance_machine_name) == 0)) {
      print_message(TRACE_MESSAGE, "<<< %s returning. line=%d. task_object=x\'%08x\'.\n", __FUNCTION__, __LINE__, task_object);
      return task_object;
    }
  }                             /* end while we have a valid task element */
  task_object = NULL;           /* not found */
  print_message(TRACE_MESSAGE, "<<< %s returning. line=%d. task_object=x\'%08x\'.\n", __FUNCTION__, __LINE__, task_object);
  return task_object;
}

/************************************************************************* 
 * add task to my list                                                   * 
 *************************************************************************/
void add_task_to_list(List * task_list, TaskObject * task_object)
{
  print_message(TRACE_MESSAGE, ">>> %s entered. line=%d. task_object=x\'%08x\'.\n", __FUNCTION__, __LINE__, task_object);
  AddToList(task_list, task_object);
  print_message(TRACE_MESSAGE, "<<< %s returning. line=%d.\n", __FUNCTION__, __LINE__);
}

/************************************************************************* 
 * delete node from my list                                              * 
 *************************************************************************/
void delete_node_from_list(List * node_list, NodeObject * node_object)
{
  print_message(TRACE_MESSAGE, ">>> %s entered. line=%d.\n", __FUNCTION__, __LINE__);
  if (node_object->node_name != NULL) {
    free(node_object->node_name);
    node_object->node_name = NULL;
  }
  RemoveFromList(node_list, node_object);       /* remove from list and free memory */
  print_message(TRACE_MESSAGE, "<<< %s returning. line=%d.\n", __FUNCTION__, __LINE__);
}

/************************************************************************* 
 * delete job from my list                                               * 
 *************************************************************************/
void delete_job_from_list(List * job_list, JobObject * job_object)
{
  print_message(TRACE_MESSAGE, ">>> %s entered. line=%d.\n", __FUNCTION__, __LINE__);
  if (job_object->ll_step_id.from_host != NULL) {
    free(job_object->ll_step_id.from_host);
    job_object->ll_step_id.from_host = NULL;
  }
  if (job_object->cluster_name != NULL) {
    free(job_object->cluster_name);
    job_object->cluster_name = NULL;
  }
  if (job_object->task_list != NULL) {
    free(job_object->task_list);
    job_object->task_list = NULL;
  }
  RemoveFromList(job_list, job_object); /* remove from list and free memory */
  print_message(TRACE_MESSAGE, "<<< %s returning. line=%d.\n", __FUNCTION__, __LINE__);
}

/************************************************************************* 
 * delete task from my list                                              * 
 *************************************************************************/
void delete_task_from_list(List * task_list, TaskObject * task_object)
{
  print_message(TRACE_MESSAGE, ">>> %s entered. line=%d.\n", __FUNCTION__, __LINE__);
  if (task_object->node_name != NULL) {
    free(task_object->node_name);
    task_object->node_name = NULL;
  }
  if (task_object->node_address != NULL) {
    free(task_object->node_address);
    task_object->node_address = NULL;
  }
  RemoveFromList(task_list, task_object);       /* remove from list and free memory */
  print_message(TRACE_MESSAGE, "<<< %s returning. line=%d.\n", __FUNCTION__, __LINE__);
}

/************************************************************************* 
 * dump out all the parms being sent back to the proxy front end         * 
 *************************************************************************/
void print_proxy_message(proxy_msg * msg)
{
  int i = 0;
  print_message(TRACE_MESSAGE, ">>> %s entered. line=%d.\n", __FUNCTION__, __LINE__);
  print_message(INFO_MESSAGE, "msg_id=%d. trans_id=%d. num_args=%d. arg_size=%d.\n", msg->msg_id, msg->trans_id, msg->num_args, msg->arg_size);
  if (msg->num_args > 0) {      /* if we have arguments */
    print_message(INFO_MESSAGE, "proxy msg (event) arguments:\n");
    for (i = 0; i < msg->num_args; i++) {
      print_message(INFO_MESSAGE, "arg[%d]=%s.\n", i, msg->args[i]);
    }
  }
  print_message(TRACE_MESSAGE, "<<< %s returning. line=%d.\n", __FUNCTION__, __LINE__);
}

/************************************************************************* 
 * see if <<<XXXXXXXX>>> variable in the line                            * 
 *************************************************************************/
int is_substitution_required(char *line)
{
  char *position1 = NULL;
  char *position2 = NULL;
  for (position1 = line; strlen(position1) >= 6; position1++) {
    if (strncmp(position1, "<<<", 3) == 0) {    /* if <<< found */
      for (position2 = position1 + 3; strlen(position2) >= 3; position2++) {
        if (((position2[0] >= 'a') && (position2[0] <= 'z')) || ((position2[0] >= 'A') && (position2[0] <= 'Z')) || ((position2[0] >= '0') && (position2[0] <= '9')) || (position2[0] == '_')) {
          continue;
        } else {
          if (strncmp(position2, ">>>", 3) == 0) {      /* if >>> found */
            return 1;           /* found a <<<XXXXXXXX>>> match */
          } /* end if <<< found */
          else {
            break;
          }
        }
      }
    }                           /* end if <<< found */
  }
  return 0;
}
