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

#include <stdlib.h>
#include <string.h>

#include "hash.h"
#include "backend.h"
#include "proxy.h"
#include "proxy_tcp.h"
#include "sdm.h"

extern int	svr_init(dbg_backend *, void (*)(dbg_event *, int));
extern int	svr_dispatch(dbg_backend *, char *, int, int);
extern int	svr_progress(dbg_backend *);
extern int	svr_isshutdown(void);
extern int	svr_shutdown(dbg_backend *);

static dbg_backend *	backend;

char *
stringify(char *buf, int len)
{
	static char *	str_buf = NULL;

	if (str_buf != NULL) {
		free(str_buf);
	}

	str_buf = (char *)malloc(len + 1);

	memcpy(str_buf, buf, len);
	str_buf[len] = '\0';

	return str_buf;
}

/*
 * Aggregate debugger response.
 */
static void
event_callback(dbg_event *e, int data)
{
	int			len;
	char *		buf;
	sdm_message	msg;

	if (DbgSerializeEvent(e, &buf, &len) < 0) {
		return;
	}

	DEBUG_PRINTF(DEBUG_LEVEL_SERVER, "[%d] server event_callback '%s'\n", sdm_route_get_id(), stringify(buf, len));

	msg = sdm_message_new(buf, len);
	sdm_message_set_id(msg, data);
	sdm_set_add_element(sdm_message_get_destination(msg), SDM_MASTER);
	sdm_aggregate_message(msg, SDM_AGGREGATE_UPSTREAM | SDM_AGGREGATE_INIT);
}

/*
 * Dispatch payload to debugger.
 */
static void
deliver_callback(const sdm_message msg)
{
	int		id;
	int		len;
	char *	buf;

	DEBUG_PRINTF(DEBUG_LEVEL_SERVER, "[%d] Enter deliver_callback\n", sdm_route_get_id());

	sdm_message_get_payload(msg, &buf, &len);
	id = sdm_message_get_id(msg);
	(void)svr_dispatch(backend, buf, len, id);

	DEBUG_PRINTF(DEBUG_LEVEL_SERVER, "[%d] Leaving deliver_callback\n", sdm_route_get_id());
}

/*
 * Just forward aggregated message to parent.
 */
static int
aggregate_callback(sdm_message msg, void *data)
{
	DEBUG_PRINTF(DEBUG_LEVEL_SERVER, "[%d] Enter aggregate_callback\n", sdm_route_get_id());

	sdm_message_set_send_callback(msg, sdm_message_free);
	sdm_message_send(msg);

	DEBUG_PRINTF(DEBUG_LEVEL_SERVER, "[%d] Leaving aggregate_callback\n", sdm_route_get_id());
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
	DEBUG_PRINTF(DEBUG_LEVEL_SERVER, "starting server on %d, size=%d\n", sdm_route_get_id(), sdm_route_get_size());

	backend = dbgr;

	sdm_message_set_deliver_callback(deliver_callback);
	sdm_aggregate_set_completion_callback(aggregate_callback, NULL);

	svr_init(dbgr, event_callback);

	while (!svr_isshutdown()) {
		if (sdm_progress() < 0) {
			svr_shutdown(dbgr);
		}
		svr_progress(dbgr);
	}
}

