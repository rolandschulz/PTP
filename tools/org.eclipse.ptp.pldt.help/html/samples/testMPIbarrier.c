#include <stdio.h>
#include <string.h>
#include "mpi.h"
// Sample MPI program


void barrier(){
	MPI_Barrier(MPI_COMM_WORLD);
}

int main(int argc, char* argv[]){	
	int  my_rank; /* rank of process */
	int  p;       /* number of processes */
	int source;   /* rank of sender */
	int dest;     /* rank of receiver */
	int tag=0, x = 3;    /* tag for messages */
	char message[100];        /* storage for message */
	MPI_Status status ;   /* return status for receive */
	 
	/* start up MPI */
	
	MPI_Init(&argc, &argv);
	
	/* find out process rank */
	MPI_Comm_rank(MPI_COMM_WORLD, &my_rank); 
	
	/* find out number of processes */
	MPI_Comm_size(MPI_COMM_WORLD, &p); 
	
	MPI_Barrier(MPI_COMM_WORLD);
	
	
	if (my_rank !=0){
		/* create message */
		sprintf(message, "Greetings from process %d!", my_rank);
		dest = 0;
		/* use strlen+1 so that '\0' get transmitted */
		MPI_Send(message, strlen(message)+1, MPI_CHAR,
		   dest, tag, MPI_COMM_WORLD);
		MPI_Barrier(MPI_COMM_WORLD);
	}
	else{
		printf("From process 0: Num processes: %d\n",p);
		for (source = 1; source < p; source++) {
			MPI_Recv(message, 100, MPI_CHAR, source, tag,
			      MPI_COMM_WORLD, &status);
			printf("%s\n",message);
		}
		//MPI_Barrier(MPI_COMM_WORLD);
		barrier();
	}


	if(my_rank == 0){
		printf("test errors\n");
		MPI_Barrier(MPI_COMM_WORLD);
	}
	else{
		printf("this path does not contain a barrier");
	}
	
	if(x < 3){
		printf("It is not an error\n");
		MPI_Barrier(MPI_COMM_WORLD);
	}
	
	while(x < my_rank){
		MPI_Barrier(MPI_COMM_WORLD);
		x ++;
	}
	
	/* shut down MPI */
	MPI_Finalize(); 

	return 0;
}

