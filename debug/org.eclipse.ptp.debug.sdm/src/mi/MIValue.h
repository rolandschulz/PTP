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
#ifndef _MIVALUE_H_
#define _MIVALUE_H_

#include "list.h"
#include "MIString.h"

#define MIValueTypeConst	1
#define MIValueTypeList	2
#define MIValueTypeTuple	3

/**
 * GDB/MI value.
 */
struct MIValue {
	int type;
	char *cstring;
	List *values;
	List *results;
};
typedef struct MIValue MIValue;

extern MIValue *NewMIConst(void);
extern MIValue *NewMITuple(void);
extern MIValue *NewMIList(void);
extern MIString *MIConstToString(MIValue *);
extern MIString *MIListToString(MIValue *);
extern MIString *MITupleToString(MIValue *);
extern MIString *MIValueToString(MIValue *);
#endif _MIVALUE_H_