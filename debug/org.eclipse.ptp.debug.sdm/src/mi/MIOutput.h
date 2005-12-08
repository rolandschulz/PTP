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

#ifndef _MIOUTPUT_H_
#define _MIOUTPUT_H_

#include "list.h"
#include "MIResultRecord.h"

/**
 * GDB/MI response.
 */
struct MIOutput {
	MIResultRecord *	rr;
	List *			oobs;
};
typedef struct MIOutput MIOutput;

extern MIOutput *MIOutputNew(void);
extern MIOutput *MIParse(char *buffer);
extern void MIOutputFree(MIOutput *op);
#endif _MIOUTPUT_H_
