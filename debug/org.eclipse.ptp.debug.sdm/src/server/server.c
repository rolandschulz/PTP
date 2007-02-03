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

#ifdef __gnu_linux__
#define _GNU_SOURCE
#endif /* __gnu_linux__ */

#include "config.h"

#include <mpi.h>
#include <stdlib.h>
#include <string.h>

#include "client_srv.h"
#include "hash.h"
#include "backend.h"
#include "proxy.h"
#include "proxy_tcp.h"

extern int	svr_init(dbg_backend *, void (*)(dbg_event *, void *), void *, char **);
extern int	svr_dispatch(dbg_backend *, char *);
extern int	svr_progress(dbg_backend *);
extern int	svr_interrupt(dbg_backend *);

static void
event_callback(dbg_event *e, void *data)
{
	char *		msg;
	
	if (DbgEventToStr(e, &msg) < 0)
		return;
		
	ClntSvrInsertMessage(msg);
}

static void
do_normal_command(char *cmd, void *data)
{
	dbg_backend *	dbgr = (dbg_backend *)data;
	
	DEBUG_PRINTF(DEBUG_LEVEL_SERVER, "executing local command '%s'\n", cmd);
	
	(void)svr_dispatch(dbgr, cmd);
}

static void
do_int_command(void *data)
{
	dbg_backend *	dbgr = (dbg_backend *)data;
	
	DEBUG_PRINTS(DEBUG_LEVEL_SERVER, "executing interrupt command\n");
	
	(void)svr_interrupt(dbgr);
}

void
setenviron(char *str, int val)
{
	char *	buf;
	
	asprintf(&buf, "%s=%d", str, val);
	putenv(buf);
	free(buf);
}

/*
 * Debug server implementation
 * 
 * NOTE: currently the debug server does not support multiple simulutaneous command execution. That is, it
 * will only process one command at at time, and will not accept another command until a reply
 * has been sent. The client may send multiple commands, but it is the underlying transport
 * that is providing the command buffering. 
 * 
 * This approach should be sufficient for all debugging tasks if we assume that the debugger operates
 * in one of two states: ACCEPTING_COMMANDS and PROCESSING_COMMAND and will only accept additional commands
 * when it is in the ACCEPTING_COMMANDS state. E.g.
 * 
 * Command		Debugger State		GDB State
 * 
 * --SLB-->		ACCEPTING_COMMANDS	SUSPENDED
 * 				PROCESSING_COMMAND	SUSPENDED
 * <--OK---		ACCEPTING_COMMANDS	SUSPENDED
 * 
 * --GOP-->		ACCEPTING_COMMANDS	SUSPENDED
 * 				PROCESSING_COMMAND	RUNNING
 * 				PROCESSING_COMMAND	SUSPENDED
 * <--OK---		ACCEPTING_COMMANDS	SUSPENDED
 * 
 * The exception to this is the INT command which can be sent at any time. This works by sending
 * with a special MPI tag used of OOB communication. It will be implemented later...
 * 
 */
void
server(int nprocs, int my_id, int job_id, dbg_backend *dbgr)
{
	char **		env = NULL;
	
	DEBUG_PRINTF(DEBUG_LEVEL_SERVER, "starting server on [%d,%d,%d]\n", my_id, nprocs, job_id);
	
	if (job_id >= 0) {
		env = (char **)malloc(4 * sizeof(char **));
		asprintf(&env[0], "OMPI_MCA_ns_nds_jobid=%d", job_id);
		asprintf(&env[1], "OMPI_MCA_ns_nds_vpid=%d", my_id);
		asprintf(&env[2], "OMPI_MCA_ns_nds_num_procs=%d", nprocs-1);
		env[3] = NULL;
	}
	
	ClntSvrInit(nprocs, my_id);
	ClntSvrRegisterCompletionCallback(ClntSvrSendReply);
	ClntSvrRegisterLocalCmdCallback(do_normal_command, (void *)dbgr);
	ClntSvrRegisterInterruptCmdCallback(do_int_command, (void *)dbgr);
	
	svr_init(dbgr, event_callback, NULL, env);
	
	for (;;) {
		ClntSvrProgressCmds();
		
		if (svr_progress(dbgr) < 0)
			break;
	}
	
	ClntSvrFinish();
}

