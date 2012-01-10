/*
 * For testing OpenACC user assistance features only.
 *
 * This is a list of invocations of all of the OpenACC pragmas in the order they are
 * listed in the OpenACC standard.  This is not a runnable program.  It is intended to be
 * used only to test the content assist and artifact detection features in the C editor.
 */

#include <stdio.h>
#include <stddef.h>
#include "openacc.h"

void for_testing_only() {
	float data[100];
	void *ptr;

	printf("OpenACC version is %d\n", _OPENACC);

	#pragma acc parallel
	{
	}

	#pragma acc kernels
	{
	}

	#pragma acc data
	{
	}

	#pragma acc host_data
	{
	}

	#pragma acc loop
	{
	}

	#pragma acc cache(data[0:50])
	{
	}

	#pragma acc parallel loop
	for (;;) {
	}

	#pragma acc parallel kernels
	for (;;) {
	}

	#pragma acc declare deviceptr(ptr);

	#pragma acc update device(data)

	#pragma acc wait
}
