
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

/*
 * This structure contains information for both job and jobstep.
 */

typedef struct job_options {
	char 	user[MAX_USERNAME];			/* local username */
	uid_t 	uid;						/* local uid */
	gid_t 	gid;						/* local gid */
	int  	nprocs;						/* --nprocs=n,  -n n */
	bool 	nprocs_set;					/* true if nprocs explicitly set */
	int32_t min_nodes;					/* --nodes=n,  -N n	*/ 
	int32_t max_nodes;					/* --nodes=x-n,  -N x-n	*/ 
	bool 	nodes_set;					/* true if nodes explicitly set */
	int  	tlimit;						/* --time,   -t	(int minutes) */
	bool	tlimit_set;					/* true if tlimit set */
	char *	partition;	 				/* --partition=n, -p n 	*/
	unsigned int jobid;     			/* --jobid=jobid */
	bool jobid_set;						/* true if jobid explicitly set */
	char * nodelist;					/* --nodelist=node1,node2,...	*/
	char * exc_nodes;					/* --exclude=node1,node2,... -x	*/

	bool labelio;						/* --label-output, -l	*/
	char *	cwd;						/* current working directory */
	int		envc;
	char ** env;

	char * 	exec_name;					/* argv[0] of this program */  
	char *	exec_path;					/* path of progname */
	char *  exec_fullname;				/* full path of progname*/
	int		prog_argc;
	char ** prog_argv;

	bool 	debug;
	char * debug_exec_name;
	char * debug_exec_path;
	char * debug_exec_fullname;
	int 	debug_argc;
	char ** debug_argv;

	int 	trans_id;
	int		ptpid;
	char *  jobsubid;
}job_opt_t;
