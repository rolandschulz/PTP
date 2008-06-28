/******************************************************************************
 * Copyright (c) 2005 The Regents of the University of California.
 * This material was produced under U.S. Government contract W-7405-ENG-36
 * for Los Alamos National Laboratory, which is operated by the University
 * of California for the U.S. Department of Energy. The U.S. Government has
 * rights to use, reproduce, and distribute this software. NEITHER THE
 * GOVERNMENT NOR THE UNIVERSITY MAKES ANY WARRANTY, EXPRESS OR IMPLIED, OR
 * ASSUMES ANY LIABILITY FOR THE USE OF THIS SOFTWARE. If software is modified
 * to produce derivative works, such modified software should be clearly
 * marked, so as not to confuse it with the version available from LANL.
 *
 * Additionally, this program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * LA-CC 04-115
 ******************************************************************************/

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

#include "hash.h"
#include "backend.h"
#include "proxy.h"
#include "proxy_tcp.h"
#include "sdm.h"

extern int	svr_init(dbg_backend *, void (*)(dbg_event *, void *), void *);
extern int	svr_dispatch(dbg_backend *, char *, int);
extern int	svr_progress(dbg_backend *);
extern int	svr_isshutdown(void);

static dbg_backend *	backend;

static void
event_callback(dbg_event *e, void *data)
{
	int			len;
	char *		buf;
	sdm_message msg;

	if (DbgSerializeEvent(e, &buf) < 0)
		return;

	len = strlen(buf);

	DEBUG_PRINTF(DEBUG_LEVEL_SERVER, "[%d] server event_callback '%s'\n", sdm_route_get_id(), buf);

	msg = sdm_message_new(buf, len);
	sdm_aggregate_set_value(sdm_message_get_aggregate(msg), SDM_AGGREGATE_HASH, buf, len);
	sdm_set_add_element(sdm_message_get_destination(msg), SDM_MASTER);

	sdm_aggregate_message(msg, SDM_AGGREGATE_UPSTREAM);
}

static void
payload_callback(char *cmd, int len)
{
	DEBUG_PRINTF(DEBUG_LEVEL_SERVER, "server payload_callback '%s'\n", cmd);

	(void)svr_dispatch(backend, cmd, len);
}

static int
aggregate_callback(sdm_message msg) {
	sdm_message_set_send_callback(msg, sdm_message_free);
	sdm_message_send(msg);
	return 0;
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
server(dbg_backend *dbgr)
{
	DEBUG_PRINTF(DEBUG_LEVEL_SERVER, "starting server on [%d,%d]\n", sdm_route_get_id(), sdm_route_get_size());

	backend = dbgr;

	sdm_message_set_payload_callback(payload_callback);
	sdm_aggregate_set_completion_callback(aggregate_callback);

	svr_init(dbgr, event_callback, NULL);

	while (!svr_isshutdown()) {
		sdm_progress();
		svr_progress(dbgr);
	}

	sdm_finalize();
}

