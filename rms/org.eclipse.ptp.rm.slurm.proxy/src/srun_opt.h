
/********************************************************************************
 * Copyright (c) 2008,2009  School of Computer Science, 
 * National University of Defense Technology, P.R.China
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	Jie Jiang <jiangjie@nudt.edu.cn>
 *******************************************************************************/

#define MAX_USERNAME 128

typedef struct srun_options {

	char * ps_progname;					/* argv[0] of this program */  
	bool ps_multi_prog;					/* multiple programs to execute */
	char ps_user[MAX_USERNAME];			/* local username */
	uid_t ps_uid;						/* local uid */
	gid_t ps_gid;						/* local gid */
	uid_t ps_euid;						/* effective user --uid=user */
	gid_t ps_egid;						/* effective group --gid=group */
	char *ps_cwd;						/* current working directory */
	bool ps_cwd_set;					/* true if cwd is explicitly set */
	int  ps_nprocs;						/* --nprocs=n,  -n n */
	bool ps_nprocs_set;					/* true if nprocs explicitly set */
	int  ps_cpus_per_task;				/* --cpus-per-task=n, -c n	*/
	bool ps_cpus_set;					/* true if cpus_per_task explicitly set */
	int32_t ps_max_threads;				/* --threads, -T (threads in srun) */
	int32_t ps_min_nodes;				/* --nodes=n,  -N n	*/ 
	int32_t ps_max_nodes;				/* --nodes=x-n,  -N x-n	*/ 
	int32_t ps_min_sockets_per_node; 	/* --sockets-per-node=n */
	int32_t ps_max_sockets_per_node; 	/* --sockets-per-node=x-n */
	int32_t ps_min_cores_per_socket; 	/* --cores-per-socket=n  */
	int32_t ps_max_cores_per_socket; 	/* --cores-per-socket=x-n */
	int32_t ps_min_threads_per_core; 	/* --threads-per-core=n */
	int32_t ps_max_threads_per_core; 	/* --threads-per-core=x-n */
	int32_t ps_ntasks_per_node;   		/* --ntasks-per-node=n */
	int32_t ps_ntasks_per_socket; 		/* --ntasks-per-socket=n */
	int32_t ps_ntasks_per_core;   		/* --ntasks-per-core=n */
	cpu_bind_type_t ps_cpu_bind_type; 	/* --cpu_bind=  */
	char * ps_cpu_bind;					/* binding map for map/mask_cpu */
	mem_bind_type_t ps_mem_bind_type; 	/* --mem_bind= */
	char *ps_mem_bind;					/* binding map for map/mask_mem	*/
	bool ps_nodes_set;					/* true if nodes explicitly set */
	bool ps_extra_set;					/* true if extra node info explicitly set */
	int  ps_time_limit;					/* --time,   -t	(int minutes) */
	char *ps_time_limit_str;			/* --time,   -t (string) */
	int  ps_ckpt_interval;				/* --checkpoint (int minutes) */
	char * ps_ckpt_interval_str;		/* --checkpoint (string) */
	char * ps_ckpt_path;				/* --checkpoint-path (string)  */
	bool ps_exclusive;					/* --exclusive */
	char *ps_partition;	 				/* --partition=n, -p n 	*/
	enum task_dist_states ps_distribution;		/* --distribution=, -m dist	*/
    uint32_t ps_plane_size;    	
	char * ps_job_name;					/* --job-name=,  -J name */
	bool ps_job_name_set;				/* true if job_name explicitly set */
	unsigned int ps_jobid;     			/* --jobid=jobid */
	bool ps_jobid_set;					/* true if jobid explicitly set */
	char * ps_mpi_type;					/* --mpi=type */
	char * ps_dependency;				/* --dependency, -P type:jobid	*/
	int ps_nice;						/* --nice */
	char * ps_account;					/* --account, -U acct_name	*/
	char * ps_comment;					/* --comment */
	char * ps_ofname;					/* --output -o filename */
	char * ps_ifname;					/* --input  -i filename */
	char * ps_efname;					/* --error, -e filename */
	int  ps_core_type;					/* --core= 	*/
	bool ps_join;						/* --join,  -j */
	int ps_immediate;					/* -i, --immediate     	*/
	bool ps_hold;						/* --hold, -H			*/
	bool ps_labelio;					/* --label-output, -l	*/
	bool ps_unbuffered;       			/* --unbuffered,   -u   */
	bool ps_allocate;					/* --allocate, 	   -A	*/
	bool ps_noshell;					/* --no-shell           */
	bool ps_overcommit;					/* --overcommit,   -O	*/
	bool ps_no_kill;					/* --no-kill, -k		*/
	bool ps_kill_bad_exit;				/* --kill-on-bad-exit, -K */
	uint16_t ps_shared;					/* --share,   -s		*/
	int  ps_max_wait;					/* --wait,    -W		*/
	bool ps_quit_on_intr;   	   		/* --quit-on-interrupt, -q */
	bool ps_disable_status;    			/* --disable-status, -X */
	int  ps_quiet;
	bool ps_parallel_debug;				/* srun controlled by debugger	*/
	bool ps_debugger_test;				/* --debugger-test */
	bool ps_test_only;					/* --test-only */
	char * ps_propagate;				/* --propagate[=RLIMIT_CORE,...]*/
	char * ps_task_epilog;				/* --task-epilog=		*/
	char * ps_task_prolog;				/* --task-prolog=		*/
	char * ps_licenses;					/* --licenses, -L		*/

	/* constraint options */
	int32_t ps_job_min_cpus;			/* --mincpus=n			*/
	int32_t ps_job_min_sockets;			/* --minsockets=n		*/
	int32_t ps_job_min_cores;			/* --mincores=n			*/
	int32_t ps_job_min_threads;			/* --minthreads=n		*/
	int32_t ps_job_min_memory;			/* --mem=n			*/
	int32_t ps_task_mem;				/* --task-mem=n			*/
	long ps_job_min_tmp_disk;			/* --tmp=n			*/
	char * ps_constraints;				/* --constraints=, -C constraint*/
	bool ps_contiguous;					/* --contiguous			*/
	char * ps_nodelist;					/* --nodelist=node1,node2,...	*/
	char * ps_alloc_nodelist;   		/* grabbed from the environment */
	char * ps_exc_nodes;				/* --exclude=node1,node2,... -x	*/
	int  ps_relative;					/* --relative -r N              */
	bool ps_relative_set;
	bool ps_no_alloc;					/* --no-allocate, -Z */
	int  ps_max_launch_time;   			
	int  ps_max_exit_timeout;  		
	int  ps_msg_timeout;      
	char * ps_network;					/* --network= */

	/***************** BLUEGENE ********************/
	uint16_t ps_geometry[SYSTEM_DIMENSIONS]; /* --geometry, -g	*/
	bool ps_reboot;						/* --reboot			*/
	bool ps_no_rotate;					/* --no_rotate, -R	*/
	uint16_t ps_conn_type;				/* --conn-type 		*/
	char * ps_blrtsimage;       		/* --blrtsimage BlrtsImage for block */
	char * ps_linuximage;       		/* --linuximage LinuxImage for block */
	char * ps_mloaderimage;     		/* --mloaderimage mloaderImage for block */
	char * ps_ramdiskimage;    			/* --ramdiskimage RamDiskImage for block */
	/***************** END of BLUEGENE **************/

	char * ps_prolog;           		/* --prolog     */
	char * ps_epilog;           		/* --epilog     */
	time_t ps_begin;					/* --begin		*/
	uint16_t ps_mail_type;				/* --mail-type	*/
	char * ps_mail_user;				/* --mail-user	*/
	char * ps_ctrl_comm_ifhn;			/* --ctrl-comm-ifhn */
	uint8_t ps_open_mode;				/* --open-mode=append|truncate */
	int ps_acctg_freq;					/* --acctg-freq=secs */
	bool ps_pty;						/* --pty		*/
	int ps_argc;						/* length of argv array	*/
	char ** ps_argv;					/* left over on command line */
} srun_opt_t;

