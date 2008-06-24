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

/*
 * Payload operations.
 */

#include "compat.h"
#include "sdm.h"

static void	(*payload_callback)(const sdm_message msg, void *data) = NULL;
static void *payload_data;

void
sdm_payload_set_callback(void (*callback)(const sdm_message msg, void *data), void *data)
{
	payload_callback = callback;
	payload_data = data;
}

void
sdm_payload_deliver(const sdm_message msg)
{
	if (payload_callback != NULL) {
		payload_callback(msg, payload_data);
	}
}
