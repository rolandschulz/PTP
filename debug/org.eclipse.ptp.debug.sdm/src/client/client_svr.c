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
 * The model assumes that each process being debugged will have an associated
 * controller. The controllers are partitioned in a tree such that each controller
 * has a parent (apart from the SDM_MASTER) and multiple children (apart from the leaves).
 * The SDM_MASTER does not control a process; it instead manages communication with the client.
 *
 * A controller receives a message from its parent and is responsible for forwarding
 * the message to a set of destinations. It is also responsible for receiving reply messages from
 * the children, coalescing these into one or more reply messages and forwarding them
 * back to the parent.
 */

#ifdef __gnu_linux__
#define _GNU_SOURCE
#endif /* __gnu_linux__ */

#include "config.h"

#include <stdlib.h>
#include <string.h>
#include <unistd.h>

#include "compat.h"
#include "dbg.h"
#include "dbg_client.h"
#include "sdm.h"

#define CMD_NORMAL	'N'
#define CMD_URGENT	'U'

static void *			local_cmd_data;
static void				(*local_cmd_callback)(char *, void *);
static void *			int_cmd_data;
static void				(*int_cmd_callback)(void *);

/*
 * Check if command is normal or interrupt.
 * FIXME: move to debug specific code.
 */
int
is_normal_command(char *buf)
{
	return *buf == CMD_NORMAL;
}

/*
 * Payload specific callback.
 * FIXME: move to debug specific code.
 */
static void
payload_callback(sdm_message msg, void *data)
{
	int 	len;
	int 	normal;
	char *	buf;

	sdm_message_get_payload(msg, &buf, &len);

	normal = is_normal_command(buf);

	if (normal && local_cmd_callback != NULL) {
		DEBUG_PRINTF(DEBUG_LEVEL_CLIENT, "[%d] running command locally\n", sdm_route_get_id());
		local_cmd_callback(buf + 1, local_cmd_data);
		DEBUG_PRINTF(DEBUG_LEVEL_CLIENT, "[%d] finished local command\n", sdm_route_get_id());
	} else if (!normal && int_cmd_callback != NULL) {
		DEBUG_PRINTF(DEBUG_LEVEL_CLIENT, "[%d] running interrupt locally\n", sdm_route_get_id());
		sdm_aggregate_finish(msg);
		int_cmd_callback(int_cmd_data);
		DEBUG_PRINTF(DEBUG_LEVEL_CLIENT, "[%d] finished interrupt command\n", sdm_route_get_id());
	}
}

/*
 * Register completion callback. This callback is made
 * when a controller has received replies from all controllers
 * in the destination set, or the appropriate timeout has expired.
 */
void
ClntSvrRegisterCompletionCallback(void (*callback)(sdm_message msg, void *data))
{
	sdm_aggregate_set_completion_callback(callback, NULL);
}

/*
 * Register local command callback. This callback is made when
 * a command must be executed by this controller.
 */
void
ClntSvrRegisterLocalCmdCallback(void (*func)(char *, void *), void *data)
{
	local_cmd_callback = func;
	local_cmd_data = data;
}

/*
 * Register an interrupt command callback. This callback is made when
 * an interrupt command must be executed by this controller.
 */
void
ClntSvrRegisterInterruptCmdCallback(void (*func)(void *), void *data)
{
	int_cmd_callback = func;
	int_cmd_data = data;
}

/*
 * Initialize the request/response handler.
 */
void
ClntSvrInit(int size, int my_id)
{
	local_cmd_callback = NULL;
	int_cmd_callback = NULL;

	sdm_payload_set_callback(payload_callback, NULL);
}

/*
 * Send a command to the controllers specified in the dest set.
 *
 * Commands can only be sent to controllers that do not have an active request pending. The
 * exception is the interrupt command which can be sent at any time. The response to
 * an interrupt command is to cause the pending request to complete.
 */
int
ClntSvrSendCommand(sdm_idset dest, int timeout, char *cmd, void *cbdata)
{
	int				len;
	char *			buf;
	sdm_message 	msg;

	DEBUG_PRINTF(DEBUG_LEVEL_CLIENT, "[%d] ClntSvrSendCommand %s\n", sdm_route_get_id(), _set_to_str(dest));

	// FIXME: get rid of this crap
	len = strlen(cmd) + 1;
	buf = (char *)malloc(len + 1);
	*buf = CMD_NORMAL;
	memcpy(buf + 1, cmd, len);
	len++;

	msg = sdm_message_new(buf, len);
	sdm_aggregate_set_value(sdm_message_get_aggregate(msg), SDM_AGGREGATE_TIMEOUT, timeout);
	sdm_set_union(sdm_message_get_destination(msg), dest);

	sdm_aggregate_start(msg);

	sdm_message_set_send_callback(msg, sdm_message_free);
	sdm_message_send(msg);

	return 0;
}

/*
 * Interrupt dest controllers, which is assumed to only contain non-SDM_MASTER controllers. Only interrupt
 * active requests that have outstanding replies.
 */
int
ClntSvrSendInterrupt(sdm_idset dest)
{
	char *			buf;
	sdm_message 	msg;

	DEBUG_PRINTF(DEBUG_LEVEL_CLIENT, "[%d] ClntSvrSendInterrupt %s\n", sdm_route_get_id(), _set_to_str(dest));

	// FIXME: get rid of this crap
	buf = (char *)malloc(2);
	*buf++ = CMD_URGENT;
	*buf = '\0';

	msg = sdm_message_new(buf, 2);
	sdm_aggregate_set_value(sdm_message_get_aggregate(msg), SDM_AGGREGATE_TIMEOUT, 0);
	sdm_set_union(sdm_message_get_destination(msg), dest);

	sdm_aggregate_start(msg);

	sdm_message_set_send_callback(msg, sdm_message_free);
	sdm_message_send(msg);

	return 0;
}

/*
 * Process local debugger message and update the appropriate reply. Cannot be called by SDM_MASTER.
 */
void
ClntSvrInsertMessage(char *str)
{
	int				len = strlen(str) + 1;
	sdm_message 	msg;

	DEBUG_PRINTF(DEBUG_LEVEL_CLIENT, "[%d] ClntSvrInsertMessage '%s'\n", sdm_route_get_id(), str);

	msg = sdm_message_new(str, len);
	sdm_aggregate_set_value(sdm_message_get_aggregate(msg), SDM_AGGREGATE_HASH, str, len);
	sdm_set_add_element(sdm_message_get_destination(msg), SDM_MASTER);

	sdm_aggregate_message(msg);
}



/*
 * Send a debugger event that originated from src to dest. Cannot be called by SDM_MASTER.
 */
void
ClntSvrSendReply(sdm_message msg, void *data)
{
	DEBUG_PRINTF(DEBUG_LEVEL_CLIENT, "[%d] ClntSvrSendReply from %s to %s\n", sdm_route_get_id(),
			_set_to_str(sdm_message_get_source(msg)),
			_set_to_str(sdm_message_get_destination(msg)));

	sdm_message_send(msg);
}

/*
 * Check and process commands/responses.
 */
int
ClntSvrProgressCmds(void)
{
	sdm_progress();

	return 0;
}

/*
 * Wait for any pending replies and flush requests before exiting
 */
void
ClntSvrFinish(void)
{
	sdm_finalize();
}
