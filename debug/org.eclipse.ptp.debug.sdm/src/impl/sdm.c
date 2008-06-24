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

#include <mpi.h>
#include <stdlib.h>

#include "compat.h"
#include "sdm.h"

static int	shutting_down = 0;

static void	recv_callback(sdm_message msg);

int
sdm_init(int argc, char *argv[])
{
	if (sdm_message_init(argc, argv) < 0) {
		return -1;
	}

	if (sdm_route_init(argc, argv) < 0) {
		return -1;
	}

	if (sdm_aggregate_init(argc, argv) < 0) {
		return -1;
	}

	sdm_message_set_recv_callback(recv_callback);

	return 0;
}

/**
 * Finalize the runtime abstraction.
 */
void
sdm_finalize(void)
{
	shutting_down = 1;

	sdm_aggregate_finalize();
	sdm_route_finalize();
	sdm_message_finalize();
}

void
sdm_progress(void)
{
	sdm_message_progress();
	sdm_aggregate_progress();
}

/*
 * Process a received message. This implements the main communication engine
 * message processes algorithm.
 */
static void
recv_callback(sdm_message msg)
{
	if (sdm_set_contains(sdm_message_get_source(msg), SDM_MASTER)) {
		/*
		 * Downstream message.
		 */

		if (shutting_down) {
			/*
			 * Stop processing downstream messages
			 */
			return;
		}

		DEBUG_PRINTF(DEBUG_LEVEL_CLIENT, "[%d] got downstream message\n", sdm_route_get_id());

		sdm_aggregate_start(msg);

		if (sdm_set_contains(sdm_message_get_destination(msg), sdm_route_get_id())) {
			sdm_message_deliver_payload(msg);
		}

		sdm_message_set_send_callback(msg, sdm_message_free);
		sdm_message_send(msg);
	} else {
		/*
		 * Upstream message
		 */

		DEBUG_PRINTF(DEBUG_LEVEL_CLIENT, "[%d] got upstream message #%x\n", sdm_route_get_id());
		sdm_aggregate_message(msg);
	}
}
