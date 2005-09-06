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
#include "procset.h"

int num_servers;
int my_task_id;
char *cmd_buf;
char **reply_bufs;
MPI_Request *cmd_requests;
MPI_Request *reply_requests;
MPI_Status *msg_stats;

void
send_cmd(char *str)
{
	int i;
	int res;
	strcpy(cmd_buf, str);
#if 1
	printf("[%d] sending message \"%s\"\n", my_task_id, cmd_buf);
	res = MPI_Startall(num_servers, cmd_requests);
	printf("[%d] send waitall (res = %d)\n", my_task_id, res);
	res = MPI_Waitall(num_servers, cmd_requests, msg_stats);
	printf("[%d] end waitall (res = %d)\n", my_task_id, res);
#else
	for (i = 0; i < num_servers; i++) {
		printf("[%d] sending message \"%s\" to [%d]\n", my_task_id, cmd_buf, i);
		MPI_Send(cmd_buf, CMD_BUF_SIZE, MPI_CHAR, i, 0, MPI_COMM_WORLD);
	}
#endif
}

void
wait_reply(void)
{
	int i;

#if 0	
	printf("[%d] reply startall\n", my_task_id);
	MPI_Startall(num_servers, reply_requests);
	printf("[%d] reply waitall\n", my_task_id);
	MPI_Waitall(num_servers, reply_requests, msg_stats);
	for (i = 0; i < num_servers; i++)
		printf("[%d] got reply from [%d] \"%s\"\n", my_task_id, i, reply_bufs[i]);
#else
	for (i = 0; i < num_servers; i++) {
		MPI_Status stat;
		MPI_Recv(reply_bufs[0], REPLY_BUF_SIZE, MPI_CHAR, i, 0, MPI_COMM_WORLD, &stat);		
		printf("[%d] got reply from [%d] \"%s\"\n", my_task_id, i, reply_bufs[0]);
	}
#endif
}

void 
client(int task_id)
{
	int i;
	
	num_servers = my_task_id = task_id;
	
	printf("client starting on [%d]\n", task_id);
	
	cmd_buf = malloc(CMD_BUF_SIZE);
	reply_bufs = malloc(num_servers);
	cmd_requests = (MPI_Request *) malloc(sizeof(MPI_Request) * num_servers);
	reply_requests = (MPI_Request *) malloc(sizeof(MPI_Request) * num_servers);
	msg_stats = (MPI_Status *) malloc(sizeof(MPI_Status) * num_servers);
	
	for (i = 0; i < task_id; i++) {
		MPI_Send_init(cmd_buf, CMD_BUF_SIZE, MPI_CHAR, i, 0, MPI_COMM_WORLD, &cmd_requests[i]);
		reply_bufs[i] = malloc(REPLY_BUF_SIZE);
		MPI_Recv_init(reply_bufs[i], REPLY_BUF_SIZE, MPI_CHAR, i, 0, MPI_COMM_WORLD, &reply_requests[i]);
	}
	
	send_cmd("message 1");
	wait_reply();
	send_cmd("message 2");
	wait_reply();
	send_cmd("message 3");
	wait_reply();
	send_cmd("exit");
	wait_reply();
}
