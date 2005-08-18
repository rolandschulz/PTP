/*
 * server() has two roles. The first is to launch the process being debugged 
 * (debuggee) under the control of a debugger (us). The second is to manage 
 * communication with the client process.
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

int pset_num = 1;
MPI_Comm pset_comm[MAX_PROC_SET] = {MPI_COMM_WORLD};
MPI_Request pset_cmd_reqs[MAX_PROC_SET];
MPI_Request pset_reply_reqs[MAX_PROC_SET];
char *cmd_bufs[MAX_PROC_SET];
char cmd_buf[CMD_BUF_SIZE];
char reply_buf[REPLY_BUF_SIZE];

int
do_commands(int client_task_id, int my_task_id)
{
	int i;
	int ret = 0;
	int num_ready;
	int indices[MAX_PROC_SET];
	MPI_Status status[MAX_PROC_SET];
	MPI_Status stat;

#if 0	
	printf("[%d] about to startall\n", my_task_id);
	MPI_Startall(pset_num, pset_cmd_reqs);
	
	printf("[%d] entering waitsome...\n", my_task_id);
	MPI_Waitsome(pset_num, pset_cmd_reqs, &num_ready, indices, status);
	printf("[%d] waitsome returns with %d ready\n", my_task_id, num_ready);
	
	for (i = 0; i < num_ready; i++) {
		printf("[%d] received message \"%s\"\n", my_task_id, cmd_bufs[indices[i]]);
		if (strcmp(cmd_bufs[indices[i]], "exit") == 0)
			ret = 1;
		strcpy(reply_buf, "ok");
		MPI_Send(reply_buf, REPLY_BUF_SIZE, MPI_CHAR, client_task_id, 0, MPI_COMM_WORLD);
		printf("[%d] sent reply\n", my_task_id);
	}
#endif
	printf("[%d] waiting for client\n", my_task_id);

	MPI_Recv(cmd_buf, CMD_BUF_SIZE, MPI_CHAR, client_task_id, 0, MPI_COMM_WORLD, &stat);
	printf("[%d] received message \"%s\"\n", my_task_id, cmd_buf);
	if (strcmp(cmd_buf, "exit") == 0)
		ret = 1;
	strcpy(reply_buf, "ok");
	printf("[%d] sendind reply\n", my_task_id);
	MPI_Send(reply_buf, REPLY_BUF_SIZE, MPI_CHAR, client_task_id, 0, MPI_COMM_WORLD);
	return ret;
}

void
server(int client_task_id, int my_task_id)
{
	int exit = 0;
	int signal;
	char status;
	char **args;
	
	printf("starting server on [%d]\n", my_task_id);
	
	//unpack_executable(&args);
	
	//initalize_low();
	
	//signal = start_inferior(&args, &status);
	cmd_bufs[0] = malloc(CMD_BUF_SIZE);
	MPI_Recv_init(cmd_bufs[0], CMD_BUF_SIZE, MPI_CHAR, client_task_id, 0, MPI_COMM_WORLD, &pset_cmd_reqs[0]);
	
	while (!exit) {
		exit = do_commands(client_task_id, my_task_id);
	}
}