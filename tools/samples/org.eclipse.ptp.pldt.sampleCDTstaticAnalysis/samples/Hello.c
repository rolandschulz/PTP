

#include <stdio.h>
#define MYVAR 42

int graphtest(void) {
	int a,b;
	int pi=3.14;
	a=0;
	b=MYVAR; // use defined
	b = b + a;
	a=3.14;
	return b;
}

