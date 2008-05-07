/*
 ============================================================================
 Name        : $(baseName).c
 Author      : $(author)
 Version     :
 Copyright   : $(copyright)
 Description : Hello OpenMP World in C
 ============================================================================
 */
#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <math.h>
#include "omp.h"


int main(int argc, char* argv[]){
	int    i,arraySize;
	double *x, *y;     /* the arrays                 */
	printf("$(openmp.hello.message)\n");

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

