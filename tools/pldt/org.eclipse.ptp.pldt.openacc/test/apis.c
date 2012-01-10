/*
 * For testing OpenACC user assistance features only.
 *
 * This is a list of invocations of all of the OpenACC runtime library routines
 * in the order they are listed in the OpenACC standard.  This program is syntactically
 * correct and type correct, but it is not a runnable program.  It is intended to be
 * used only to test hover help, dynamic help, and content assist in the C editor, as
 * well as OpenACC artifact detection.
 */

#include <stddef.h>
#include "openacc.h"

void for_testing_only()
{
	int i;
	void *p;
	acc_device_t dev = acc_device_none;

	i = acc_get_num_devices(dev);
	acc_set_device_type(dev);
	dev = acc_get_device_type();
	acc_set_device_num(i, dev);
	i = acc_get_device_num(dev);
	i = acc_async_test(0);
	i = acc_async_test_all();
	acc_async_wait(0);
	acc_async_wait_all();
	acc_init(dev);
	acc_shutdown(dev);
	i = acc_on_device(dev);
	p = acc_malloc((size_t)1024);
	acc_free(p);
}
