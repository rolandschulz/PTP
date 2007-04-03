/*
 * server() has two roles. The first is to launch the process being debugged 
 * (debuggee) under the control of a debugger (us). The second is to manage 
 * communication with the client process.
 *
 * Note that there will be num_procs+1 [0..num_procs] processes in our 
 * communicator, where num_procs is the number of processes in the parallel 
 * job being debugged. To simplify the accounting, we use the task id of
 * num_procs as the client task id and [0..num_procs-1] for the server
 * task ids.
 */

#include <string.h>

#include "backend.h"

extern dbg_backend_funcs	GDBMIBackend;
extern dbg_backend_funcs	TestBackend;

dbg_backend	dbg_backends[] = {
	{"gdb-mi", &GDBMIBackend, "gdb"},
	{"test", &TestBackend, NULL}
};

int
find_dbg_backend(char *name, dbg_backend **dp)
{
	int				i;
	dbg_backend *	d;
	
	for (i = 0; i < sizeof(dbg_backends) / sizeof(dbg_backend); i++) {
		d = &dbg_backends[i];
		if (strcmp(name, d->db_name) == 0) {
			*dp = d;
			return 0;
		}
	}

	return -1;
}

void
backend_set_path(dbg_backend *d, char *path)
{
	d->db_exe_path = path;
}
