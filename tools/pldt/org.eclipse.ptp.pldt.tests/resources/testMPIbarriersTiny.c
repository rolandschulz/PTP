#include "mpi.h"
int main(int argc, char* argv[]){
	int  my_rank; /* rank of process */
	MPI_Init(&argc, &argv);
	MPI_Comm_rank(MPI_COMM_WORLD, &my_rank); 
	if (my_rank !=0){
		MPI_Barrier(MPI_COMM_WORLD);
	}
	else{
		MPI_Barrier(MPI_COMM_WORLD);
	}
	MPI_Finalize(); 
	return 0;
}
