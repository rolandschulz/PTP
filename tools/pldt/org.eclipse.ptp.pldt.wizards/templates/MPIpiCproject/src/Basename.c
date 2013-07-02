/*
 ============================================================================
 Name        : $(baseName).c
 Author      : $(author)
 Version     :
 Copyright   : $(copyright)
 Description : Calculate Pi in MPI
 ============================================================================
 */
#include <mpi.h>
#include <stdio.h>
#include <string.h>

void
calc_pi(int rank, int num_procs)
{
	int		i;
	int		num_intervals;
	double	h;
	double	mypi;
	double	pi;
	double	sum;
	double	x;

	/* set number of intervals to calculate */
	if (rank == 0) {
		num_intervals = 100000000;
	}

	/* tell other tasks how many intervals */
	MPI_Bcast(&num_intervals, 1, MPI_INT, 0, MPI_COMM_WORLD);

	/* now everyone does their calculation */

	h = 1.0 / (double) num_intervals; 
	sum = 0.0;

	for (i = rank + 1; i <= num_intervals; i += num_procs) { 
		x = h * ((double)i - 0.5); 
		sum += (4.0 / (1.0 + x*x)); 
	} 

	mypi = h * sum;

	/* combine everyone's calculations */
	MPI_Reduce(&mypi, &pi, 1, MPI_DOUBLE, MPI_SUM, 0, MPI_COMM_WORLD);

	if (rank == 0) {
		printf("PI is approximately %.16f\n", pi);
	}
}

int 
main(int argc, char *argv[])
{
	int			my_rank;		/* rank of process */
	int			num_procs;		/* number of processes */
	int			source;			/* rank of sender */
	int			dest = 0;		/* rank of receiver */
	int			tag = 0;		/* tag for messages */
	char		message[100];	/* storage for message */
	MPI_Status	status ;		/* return status for receive */

	/* start up MPI */

	MPI_Init(&argc, &argv);

	/* find out process rank */
	MPI_Comm_rank(MPI_COMM_WORLD, &my_rank); 

	/* find out number of processes */
	MPI_Comm_size(MPI_COMM_WORLD, &num_procs); 


	if (my_rank != 0) {
		/* create message */
		snprintf(message,26, "Greetings from process %d!", my_rank);
		/* use strlen+1 so that '\0' get transmitted */
		MPI_Send(message, strlen(message)+1, MPI_CHAR,
				dest, tag, MPI_COMM_WORLD);
	} else {
		printf("Num processes: %d\n",num_procs);
		for (source = 1; source < num_procs; source++) {
			MPI_Recv(message, 100, MPI_CHAR, source, tag,
					MPI_COMM_WORLD, &status);
			printf("Process 0 received \"%s\"\n",message);
		}

		/* now return the compliment */
		snprintf(message, 26, "Hi, how are you?           ");
	}

	MPI_Bcast(message, strlen(message)+1, MPI_CHAR, dest, MPI_COMM_WORLD);

	if (my_rank != 0) {
		printf("Process %d received \"%s\"\n", my_rank, message);
	}

	/* calculate PI */
	calc_pi(my_rank, num_procs);

	/* shut down MPI */
	MPI_Finalize(); 

	return 0;
}
