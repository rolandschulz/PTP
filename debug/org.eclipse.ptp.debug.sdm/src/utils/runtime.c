/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/

/*
 * Runtime abstraction. These routines provide an abstraction between the debugger
 * and the underlying runtime system.
 * 
 * Currently only MPI/OMPI is supported.
 */

#include <mpi.h>
#include <stdlib.h>

#include "compat.h"
#include "runtime.h"

static MPI_Status *last_status;
static int argc = 1;
static char *argv[] = {"fake_argv", NULL};

/**
 * Initialize the runtime abstraction.
 * 
 * @param[out] size number of processes launched
 * @param[out] rank index of this process
 * @return 0 on success, -1 on failure
 */
int
runtime_init(int *size, int *rank)
{
	MPI_Init(&argc, (char ***)&argv);
	MPI_Comm_size(MPI_COMM_WORLD, size);
	MPI_Comm_rank(MPI_COMM_WORLD, rank);
	
	return 0;
}

/**
 * Finalize the runtime abstraction.
 * 
 * @return 0 on success, -1 on failure
 */
int
runtime_finalize()
{
	MPI_Finalize();
	
	return 0;
}

/**
 * Send a tagged message to the destination.
 * 
 * @param[in] buf buffer to send
 * @param[in] len length of buffer (in bytes)
 * @param[in] dest destaination ID
 * @param[in] tag message tag
 * @return 0 on success, -1 on failure
 */
int
runtime_send(char *buf, int len, int dest, int tag)
{
	int err = MPI_Send(buf, len, MPI_CHAR, dest, tag, MPI_COMM_WORLD);
	
	if (err != MPI_SUCCESS) {
		return -1;
	}
	
	return 0;
}

/**
 * Receive a tagged message from the source.
 * 
 * @param[in] buf buffer to receive message
 * @param[in] len length of buffer (in bytes)
 * @param[in] source source ID
 * @param[in,out] tag message tag
 * @return 0 on success, -1 on failure
 */
int
runtime_recv(char *buf, int len, int source, int *tag)
{
	int						err;
	static MPI_Status		stat;

	err = MPI_Recv(buf, len, MPI_CHAR, source, *tag, MPI_COMM_WORLD, &stat);
	
	if (err != MPI_SUCCESS) {
		return -1;
	}

	*tag = stat.MPI_TAG;
	
	last_status = &stat;
	
	return 0;
}

/**
 * Test for available message
 * 
 * @param[out] avail a non-zero value indicates a message is available
 * @param[out] source source ID of message
 * @param[out] tag message tag
 * @param[out] count length of message (in bytes)
 * @return 0 on success, -1 on failure
 */
int
runtime_probe(int *avail, int *source, int *tag, int *count)
{
	int				err;
	MPI_Status		stat;

	err = MPI_Iprobe(MPI_ANY_SOURCE, MPI_ANY_TAG, MPI_COMM_WORLD, avail, &stat);
	
	if (err != MPI_SUCCESS) {
		return -1;
	}

	if (avail) {
		MPI_Get_count(&stat, MPI_CHAR, count);
		*source = stat.MPI_SOURCE;
		*tag = stat.MPI_TAG;
	}
	
	return 0;
}

/**
 * Setup environment prior to starting server
 * 
 * @param[in] nprocs number of processes being debugged
 * @param[in] id process index of this process
 * @param[in] job_id job ID of this job
 * @param[out] envp pointer to array of environment variables
 */
int
runtime_setup_environment(int nprocs, int id, int job_id, char ***envp)
{
	char **	env = NULL;
	
	if (job_id >= 0) {
		env = (char **)malloc(4 * sizeof(char **));
		asprintf(&env[0], "OMPI_MCA_ns_nds_jobid=%d", job_id);
		asprintf(&env[1], "OMPI_MCA_ns_nds_vpid=%d", id);
		asprintf(&env[2], "OMPI_MCA_ns_nds_num_procs=%d", nprocs);
		env[3] = NULL;
	}
	
	*envp = env;
	
	return 0;
}
