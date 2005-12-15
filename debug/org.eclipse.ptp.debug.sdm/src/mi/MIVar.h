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

#ifndef _MIVAR_H_
#define _MIVAR_H_

#include "list.h"
#include "MIString.h"
#include "MIResultRecord.h"
#include "MICommand.h"

/**
 * Represents a set variable object.
 */
struct MIVar {
	char *			name;
	char *			type;
	char *			exp;
	int				numchild;
	struct MIVar **	children;
};
typedef struct MIVar	MIVar;

extern MIVar *MIVarNew(void);
extern void MIVarFree(MIVar *var);
extern MIVar *MIVarParse(List *results);
extern MIVar *MIVarGetVarCreateInfo(MICommand *cmd);
extern void MIVarGetVarListChildrenInfo(MIVar *var, MICommand *cmd);
#endif /* _MIVAR_H_ */


