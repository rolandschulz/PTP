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
 
#ifndef _MIDISASSEMBLY_H_
#define _MIDISASSEMBLY_H_

#include "MIList.h"
#include "MICommand.h"
#include "MIValue.h"

struct MIDisassemblyInfo {
	char *	addr;
	char *	func_name;
	long	offset;
	char *	inst;
};
typedef struct MIDisassemblyInfo MIDisassemblyInfo;

struct MIDataReadDisassemblyInfo {
	MIList *asm_insns;
};
typedef struct MIDataReadDisassemblyInfo MIDataReadDisassemblyInfo;

extern MIDisassemblyInfo *			MIDisassemblyInfoNew(void);
extern MIDataReadDisassemblyInfo *	MIDataReadDisassembleInfoNew(void);
extern void							MIDisassemblyInfoFree(MIDisassemblyInfo *info);
extern void							MIDataReadDisassemblyInfoFree(MIDataReadDisassemblyInfo *info);
extern MIDisassemblyInfo *			MIDisassemblyInfoParse(MIValue *tuple);
extern MIDataReadDisassemblyInfo *	MIGetDataReadDisassemblyInfo(MICommand *cmd);
extern MIList *						MIGetDisassemblyList(MIValue *miValue);
#endif /* _MIDISASSEMBLY_H_ */

