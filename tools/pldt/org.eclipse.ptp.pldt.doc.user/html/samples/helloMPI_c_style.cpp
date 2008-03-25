#include "mpi.h"
#include <iostream>
using namespace std;

// from ews/gridmpi/mpiExample2/src/helloMPI.c

class HelloWorld {
	public:
		int sayHello(int argc, char* argv[]);
};

int HelloWorld::sayHello(int argc, char* argv[]) {
	int  my_rank; /* rank of process */
	int  p;       /* number of processes */
	int source;   /* rank of sender */
	int dest;     /* rank of receiver */
	int tag=0;    /* tag for messages */
	char message[100];        /* storage for message */
	MPI_Status status ;   /* return status for receive */
	
	cout << "Hello MPI World the original." << endl;
	
	/* start up MPI */
	MPI_Init(&argc, (char***)(&argv));
	
	/* find out process rank */
	MPI_Comm_rank(MPI_COMM_WORLD, &my_rank); 
	
	/* find out number of processes */
	MPI_Comm_size(MPI_COMM_WORLD, &p); 
	
	
	if (my_rank !=0){
		/* create message */
		sprintf(message, "Greetings from process %d!", my_rank);
		dest = 0;
		/* use strlen+1 so that '\0' get transmitted */
		MPI_Send(message, strlen(message)+1, MPI_CHAR,
		   dest, tag, MPI_COMM_WORLD);
	}
	else{
		printf("From process 0: Num processes: %d\n",p);
		for (source = 1; source < p; source++) {
			MPI_Recv(message, 100, MPI_INTEGER, source, tag,
			      MPI_COMM_WORLD, &status);
			cout << message << endl;
		}
	}
	/* shut down MPI */
	MPI_Finalize(); 
	
	return 0;
}

int main (int argc, char* argv[]) {
  HelloWorld hello;
  hello.sayHello(argc, argv);
  return 0;
}
