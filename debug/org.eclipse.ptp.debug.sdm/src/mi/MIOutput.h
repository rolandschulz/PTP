/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/

#include "list.h"

/**
 * GDB/MI response.
 */
struct MIOutput {
	MIResultRecord *rr;
	List *oobs;
};
typedef struct MIOutput MIOutput;

extern MIOutput *NewMIOutput(void);
extern MIOutput *MIParse(char *buffer);