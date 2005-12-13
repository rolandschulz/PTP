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

#ifndef _MIARG_H_
#define _MIARG_H_

#include "list.h"
#include "MIString.h"

/**
 * Represents a set name=value.
 */
struct MIArg {
	char * name;
	char * value;
};
typedef struct MIArg	MIArg;

extern MIArg *MIArgNew(void);
extern void MIArgFree(MIArg *arg);
extern List *MIArgsParse(MIValue *miValue);
extern MIArg *MIArgParse(MIValue *tuple);
extern MIString *MIArgToString(MIArg *arg);
#endif _MIARG_H_


