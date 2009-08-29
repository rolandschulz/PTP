/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/

#include "config.h"

#include <stdlib.h>
#include <stdio.h>
#include <string.h>

#include "sdm.h"

static int	shutting_down = 0;

static void	recv_callback(sdm_message msg);

/*
 * Initialize the abstraction layers. ORDER IS IMPORTANT!
 */
int
sdm_init(int argc, char *argv[])
{
	if (sdm_routing_table_init(argc, argv) < 0) {
		DEBUG_PRINTF(DEBUG_LEVEL_STARTUP, "[%d] sdm_routing_table_init failed\n", sdm_route_get_id());
		return -1;
	}

	if (sdm_message_init(argc, argv) < 0) {
		DEBUG_PRINTF(DEBUG_LEVEL_STARTUP, "[%d] sdm_message_init failed\n", sdm_route_get_id());
		return -1;
	}

	if (sdm_aggregate_init(argc, argv) < 0) {
		DEBUG_PRINTF(DEBUG_LEVEL_STARTUP, "[%d] sdm_aggregate_init failed\n", sdm_route_get_id());
		return -1;
	}

	DEBUG_PRINTF(DEBUG_LEVEL_STARTUP, "[%d] Initialization successful\n", sdm_route_get_id());

	sdm_message_set_recv_callback(recv_callback);

	return 0;
}

/**
 * Finalize the abstraction layers
 */
void
sdm_finalize(void)
{
	shutting_down = 1;

	sdm_aggregate_finalize();
	sdm_route_finalize();
	sdm_message_finalize();
}

/*
 * Progress messages and the aggregation layer
 */
int
sdm_progress(void)
{
	if (sdm_message_progress() < 0) {
		return -1;
	}
	sdm_aggregate_progress();
	return 0;
}

/*
 * Process a received message. This implements the main communication engine
 * message processing algorithm.
 */
static void
recv_callback(sdm_message msg)
{
	DEBUG_PRINTF(DEBUG_LEVEL_MESSAGES, "[%d] Enter recv_callback\n", sdm_route_get_id());

	if (sdm_set_contains(sdm_message_get_source(msg), SDM_MASTER)) {
		DEBUG_PRINTF(DEBUG_LEVEL_MESSAGES, "[%d] got downstream message src=%s, dest=%s\n",
				sdm_route_get_id(),
				_set_to_str(sdm_message_get_source(msg)),
				_set_to_str(sdm_message_get_destination(msg)));

		if (shutting_down) {
			/*
			 * Stop processing downstream messages
			 */
			return;
		}

		/*
		 * Start aggregating messages
		 */
		sdm_aggregate_message(msg, SDM_AGGREGATE_DOWNSTREAM);

		/*
		 * If we are the destination, then deliver the payload
		 */
		if (sdm_set_contains(sdm_message_get_destination(msg), sdm_route_get_id())) {
			sdm_message_deliver(msg);
		}

		/*
		 * Free the message once it's been forwarded
		 */
		sdm_message_set_send_callback(msg, sdm_message_free);

		/*
		 * Now forward the message
		 */
		sdm_message_send(msg);
	} else {
		DEBUG_PRINTF(DEBUG_LEVEL_MESSAGES, "[%d] got upstream message src=%s, dest=%s\n",
				sdm_route_get_id(),
				_set_to_str(sdm_message_get_source(msg)),
				_set_to_str(sdm_message_get_destination(msg)));

		/*
		 * Upstream messages are always aggregated
		 */
		sdm_aggregate_message(msg, SDM_AGGREGATE_UPSTREAM);
	}

	DEBUG_PRINTF(DEBUG_LEVEL_MESSAGES, "[%d] Leaving recv_callback\n", sdm_route_get_id());
}
