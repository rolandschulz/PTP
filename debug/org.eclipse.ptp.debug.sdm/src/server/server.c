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

int
do_commands(int client_task_id, int my_task_id)
{
	int			count;
	int			ret = 0;
	char *		cmd_buf;
	char *		reply_buf;
	MPI_Status	stat;

	printf("[%d] waiting for client\n", my_task_id);

	MPI_Probe(client_task_id, 0, MPI_COMM_WORLD, &stat);
	MPI_Get_count(&stat, MPI_CHAR, &count);
	
	printf("[%d] message size %d available\n", my_task_id, count);
	
	cmd_buf = (char *)malloc(count);
	
	MPI_Recv(cmd_buf, count, MPI_CHAR, client_task_id, 0, MPI_COMM_WORLD, &stat);
	
	printf("[%d] received message \"%s\"\n", my_task_id, cmd_buf);
	
	if (strcmp(cmd_buf, "exit") == 0)
		ret = 1;
	
	free(cmd_buf);
	
	printf("[%d] sendind reply\n", my_task_id);
	
	asprintf(&reply_buf, "ok");
	
	MPI_Send(reply_buf, strlen(reply_buf), MPI_CHAR, client_task_id, 0, MPI_COMM_WORLD);
	
	free(reply_buf);
	
	return ret;
}

void
server(int client_task_id, int my_task_id)
{
	int exit = 0;
	//int signal;
	//char status;
	//char **args;
	
	printf("starting server on [%d]\n", my_task_id);
	
	//unpack_executable(&args);
	
	//initalize_low();
	
	//signal = start_inferior(&args, &status);
	
	while (!exit) {
		exit = do_commands(client_task_id, my_task_id);
	}
}