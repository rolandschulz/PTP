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
#ifndef _MIRESULT_H_
#define _MIRESULT_H_

#include "MIString.h"

/**
 * GDB/MI result sematic (Variable=Value)
 */
struct MIResult {
	char *variable;
	MIValue *value;
};
typedef struct MIResult MIResult;

extern MIResult *MIResultNew(void);
extern MIString *MIResultToString(MIResult *r);
extern void MIResultFree(MIResult *r);
#endif /* _MIRESULT_H_ */
