#include <stdio.h>
#include <string.h>
#include <stdlib.h>  // for omp sample
#include <math.h>    // for omp
// fake header file from OpenMPstub proj
#include "openmp.h"    

 
// Sample dummy OpenMP program 


int main(int argc, char* argv[]){
	int    i,arraySize;
	double *x, *y;     /* the arrays                 */
	printf("Hello OpenMP World.\n");	
	
// sample openMP API
	if (omp_in_parallel()){
		printf("true"); 
	}
	  /* Allocate memory for the arrays. */
  x = (double *) malloc( (size_t) (  arraySize * sizeof(double) ) );
  y = (double *) malloc( (size_t) (  arraySize * sizeof(double) ) );
 
  /* Here's the OpenMP pragma that parallelizes the for-loop. */
#pragma omp parallel for
  for ( i = 0; i < arraySize; i++ )
    {
      y[i] = sin( exp( cos( - exp( sin(x[i]) ) ) ) );
    }
	  
	 
	return 0;   
}

