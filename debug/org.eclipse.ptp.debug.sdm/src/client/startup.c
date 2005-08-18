#include <mpi.h>

extern void client(int);
extern void server(int, int);

MPI_Comm mcast_comm;

int
main(int argc, char *argv[])
{
	int rank;
	int size;
	
	MPI_Init(&argc, &argv);
	
	MPI_Comm_size(MPI_COMM_WORLD, &size);
	MPI_Comm_rank(MPI_COMM_WORLD, &rank);
	
	/* Create multicast communicator */
	
	if (rank == size-1) {
		client(rank);
	} else {
		server(size - 1, rank);
	}
	
	MPI_Finalize();
	
	return 0;
}