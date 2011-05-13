/* The ubiquitous cpi program.
   Compute pi using a simple quadrature rule
   in parallel
   Usage: cpi [intervals_per_thread]

   BRT: from http://www.psc.edu/general/software/packages/upc/examples/cpi.upc
   */

#include <stdio.h>
#include <stdlib.h>
#include <math.h>
#include <upc_relaxed.h>

#define INTERVALS_PER_THREAD_DEFAULT 100
/* Add up all the inputs on all the threads.
   When the collective spec becomes finalised this
   will be replaced */

shared double reduce_data[THREADS];
shared double reduce_result;
double myreduce(double myinput)
{
  reduce_data[MYTHREAD]=myinput;
  upc_barrier;
  if(MYTHREAD == 0) {
    double result = 0;
    int i;
    for(i=0;i < THREADS;i++) {
      result += reduce_data[i];
    }
    reduce_result = result;
  }
  upc_barrier;
  return(reduce_result);
}

/* The function to be integrated */
double f(double x)
{
  double dfour=4;
  double done=1;
  return(dfour/(done + (x*x)));
}

/* Implementation of a simple quadrature rule */
double integrate(double left,double right,int intervals)
{
  int i;
  double sum = 0;
  double h = (right-left)/intervals;
  double hh = h/2;
  /* Use the midpoint rule */
  double midpt = left + hh;
  for(i=0;i < intervals;i++) {
    sum += f(midpt + i*h);
  }
  return(h*sum);
}

int main(int argc,char **argv)
{
  double mystart, myend;
  double myresult;
  double piapprox;
  int intervals_per_thread = INTERVALS_PER_THREAD_DEFAULT;
  double realpi=3.141592653589793238462643;
  /* Get the part of the range that I'm responsible for */
  mystart = (1.0*MYTHREAD)/THREADS;
  myend = (1.0*(MYTHREAD+1))/THREADS;
  if(argc > 1) {
    intervals_per_thread = atoi(argv[1]);
  }
  piapprox = myreduce(integrate(mystart,myend,intervals_per_thread));
  if(MYTHREAD == 0) {
    printf("Approx: %20.17f Error: %23.17f\n",piapprox,fabs(piapprox - realpi));
  }
  upc_addrfield();
  return(0);
}
