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

/*
 * Client runs as task id num_procs, where num_procs are the number of processes
 * in the job being debugged, and is responsible for coordinating protocol
 * messages between the debug client interface (whatever that may be)
 * and the debug servers.
 * 
 * Note that there will be num_procs+1 [0..num_procs] processes in our 
 * communicator, where num_procs is the number of processes in the parallel 
 * job being debugged. To simplify the accounting, we use the task id of
 * num_procs as the client task id and [0..num_procs-1] for the server
 * task ids.
 */

#include <mpi.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>

#include "dbg.h"
#include "dbg_client.h"
#include "procset.h"
#include "list.h"

/*
 * A request represents an asynchronous send/receive transaction between the client
 * and all servers. completed() is called once all replys have been received.
 */
struct active_request {
	procset *		procs;
	void				(*completed)(procset *);
	//Hash *			replys;
};
typedef struct active_request	active_request;

static procset *		sending_procs;
static procset *		receiving_procs;
static List *		active_requests;
static char **		cmd_bufs;
static MPI_Request *	cmd_requests;
int *			pids;
MPI_Status *		stats;

int num_servers;
int my_task_id;

/*
 * Send a command to the servers specified in procset. 
 * 
 * It is permissible to have multiple outstanding commands, provided
 * the processes each command applies to are disjoint sets.
 */
int
send_command(procset *procs, char *str, void (*completed_callback)(procset *))
{
	int				pid;
	int				cmd_len;
	procset *		p;
	active_request *	r;
	
	/*
	 * Check if any processes already have active requests
	 */
	p = procset_and(sending_procs, procs);
	procset_andeq(p, receiving_procs);
	if (!procset_isempty(p)) {
		DbgClntSetError(DBGERR_INPROGRESS, NULL);
		return -1;
	}
	
	procset_free(p);
	/*
	 * Update sending processes
	 */	
	procset_oreq(sending_procs, procs);

	/*
	 * Create a new request and add it too the active list
	 */
	r = (active_request *)malloc(sizeof(active_request));
	r->procs = procset_copy(procs);
	r->completed = completed_callback;
	//r->replys = NewHash(procset_size(procs));
	
	AddToList(active_requests, (void *)r);

	/*
	 * Now post commands to the servers
	 */
	for (pid = 0; pid < num_servers; pid++) {
		if (procset_test(procs, pid)) {
			/*
			 * MPI spec does not allow read access to a send buffer while send is in progress
			 * so we must make a copy for each send.
			 */
			cmd_bufs[pid] = strdup(str);
			cmd_len = strlen(str);
			
			printf("[%d] sending message \"%s\" to [%d]\n", my_task_id, cmd_bufs[pid], pid);
			MPI_Isend(cmd_bufs[pid], cmd_len, MPI_CHAR, pid, 0, MPI_COMM_WORLD, &cmd_requests[pid]); // TODO: handle fatal errors
		}
	}

	return 0;
}

/*
 * Check for any replies from servers. If any are received, and these complete a send request,
 * then processes the reply.
 */
void
progress_commands(void)
{
	int				i;
	int				count;
	int				avail;
	int				recv_pid;
	int 				completed;
	char *			reply_buf;
	active_request *	r;
	MPI_Status		stat;

	/*
	 * Check for completed sends
	 */
	printf("sending procs is %s\n", procset_to_str(sending_procs));
	count = procset_size(sending_procs);
	if (count > 0) {
printf("count = %d\n", count);
		if (MPI_Testsome(count, cmd_requests, &completed, pids, stats) != MPI_SUCCESS) {
			printf("error in testsome\n");
			exit(1);
		}
		
		printf("completed = %d\n", completed);
		
		for (i = 0; i < completed; i++) {
			printf("send to %d complete\n", pids[i]);
			procset_remove_proc(sending_procs, pids[i]);
			procset_add_proc(receiving_procs, pids[i]);
			free(cmd_bufs[i]);
		}
	}
		
	/*
	 * Check for replys
	 */
	count = procset_size(receiving_procs);
	if (count > 0) {
		MPI_Iprobe(MPI_ANY_SOURCE, 0, MPI_COMM_WORLD, &avail, &stat);
	
		if (avail == 0)
			return;
		
		/*
		 * A message is available, so receive it
		 */
		MPI_Get_count(&stat, MPI_CHAR, &count);
		
		reply_buf = (char *)malloc(count + 1);
		recv_pid = stat.MPI_SOURCE;
		
		MPI_Recv(reply_buf, count, MPI_CHAR, recv_pid, 0, MPI_COMM_WORLD, &stat);
		
		reply_buf[count] = '\0';
			
		printf("[%d] got reply from [%d] \"%s\"\n", my_task_id, recv_pid, reply_buf);
		
		free(reply_buf);
		
		/*
		 * remove from active and receiving procsets
		 */
		procset_remove_proc(receiving_procs, recv_pid);
		
		/*
		 * Check if any sends/recvs are complete. Call notify function for completed sends.
		 */
		SetList(active_requests);
	
		while ((r = (active_request *)GetListElement(active_requests)) != NULL) {
			if (procset_test(r->procs, recv_pid)) {
				procset_remove_proc(r->procs, recv_pid);
				if (procset_isempty(r->procs)) {
					RemoveFromList(active_requests, (void *)r);
					r->completed(r->procs);
					procset_free(r->procs);
					free(r);
				}
			}
		}
	}
}

#define TEST

#ifdef TEST
int completed;

void 
send_complete(procset *p)
{
	printf("send completed\n");
	completed++;
}

void
wait_for_server(void)
{
	completed = 0;
	
	while (!completed) {
		progress_commands();
		usleep(10000);
	}
}
#endif

void 
client(int task_id)
{
#ifdef TEST
	int	i;
	procset *p;
#endif

	num_servers = my_task_id = task_id;
	
	printf("client starting on [%d]\n", task_id);
	
	cmd_bufs = (char **)malloc(sizeof(char *) * num_servers);
	cmd_requests = (MPI_Request *) malloc(sizeof(MPI_Request) * num_servers);
	pids = (int *)malloc(sizeof(int) * num_servers);
	stats = (MPI_Status *)malloc(sizeof(MPI_Status) * num_servers);

	for (i = 0; i < num_servers; i++)
		cmd_requests[i] = MPI_REQUEST_NULL;

	sending_procs = procset_new(num_servers);
	receiving_procs = procset_new(num_servers);
	active_requests = NewList();
	
#ifdef TEST
	p = procset_new(num_servers);
	for (i = 0; i < num_servers; i++)
		procset_add_proc(p, i);
	
	printf("client sending command...\n");
	
	send_command(p, "hello", send_complete);
	
	printf("client waiting for replies\n");
	
	wait_for_server();
#endif
}
