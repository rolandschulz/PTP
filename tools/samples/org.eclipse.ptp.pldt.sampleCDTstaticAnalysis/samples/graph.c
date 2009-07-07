#include "mpi.h"
#include "stdio.h"
 
void foo(int x);
void gee(int x);
void kei(int x);

void foo(int x){
  x ++;
  gee(x);
}

void gee(int x){
  x *= 3;
  kei(x);
}

void kei(int x){
  x = x % 10;
  foo(x);
}

void a(int x){
  x --;
}

int main3(int argc, char* argv[]){
  int x = 0;
  foo(x);
  a(x);
}