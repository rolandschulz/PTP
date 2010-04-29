/*
 ============================================================================
 Name        : helloOpenMP2.c
 Author      : Polly Parallel
 Version     :
 Copyright   : Your copyright notice
 Description : Hello OpenMP World in C
 ============================================================================
 */
#include <omp.h>
#include <stdio.h>
#include <stdlib.h>
/**
 * Hello OpenMP World prints the number of threads and the current thread id
 */
int main (int argc, char *argv[]) {

  int numThreads, tid;

  /* This creates a team of threads; each thread has own copy of variables  */
#pragma omp parallel private(numThreads, tid)
 {
   tid = omp_get_thread_num();
   printf("Hello World from thread number %d\n", tid);

   /* The following is executed by the master thread only (tid=0) */
   if (tid == 0)
     {
       numThreads = omp_get_num_threads();
       printf("Number of threads is %d\n", numThreads);
     }
 }
 // more pragmas, testing their region/scope
#pragma omp parallel shared(n,a,b)
 {
   #pragma omp for
   for (int i=0; i<n; i++)
   {
       a[i] = i + 1;
       #pragma omp parallel for /*-- Okay - This is a parallel region --*/
       for (int j=0; j<n; j++)
           b[i][j] = a[i];
   }
 } /*-- End of parallel region --*/
 return 0;
}


