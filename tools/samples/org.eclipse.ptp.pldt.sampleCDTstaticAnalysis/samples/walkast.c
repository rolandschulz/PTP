// p16
#include <stdio.h>
#define MYVAR 42

int main(void) {
	int a,b;
	a=0;
	b=MYVAR; // use defined
	b = b + a;
	return b;
}
int foo(int bar){
  int z = bar;
  return z;
}