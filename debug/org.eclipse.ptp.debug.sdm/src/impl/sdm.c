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

extern int sdm_message_init(int argc, char *argv[]);
extern int sdm_route_init(int argc, char *argv[]);
extern void sdm_message_finalize(void);
extern void sdm_route_finalize(void);

int
sdm_init(int argc, char *argv[])
{
	if (sdm_message_init(argc, argv) < 0) {
		return -1;
	}

	if (sdm_route_init(argc, argv) < 0) {
		return -1;
	}

	return 0;
}

/**
 * Finalize the runtime abstraction.
 */
void
sdm_finalize(void)
{
	sdm_route_finalize();
	sdm_message_finalize();
}
