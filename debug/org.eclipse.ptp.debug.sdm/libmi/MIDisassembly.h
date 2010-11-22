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
 
#ifndef _MIDISASSEMBLE_H_
#define _MIDISASSEMBLE_H_

#include "MIList.h"
#include "MICommand.h"
#include "MIValue.h"

struct MIDissasemblyInfo {
	char *	addr;
	char *	func_name;
	long	offset;
	char *	inst;
};
typedef struct MIDissasemblyInfo MIDissasemblyInfo;

struct MIDataReadDisassemblyInfo {
	MIList *asm_insns;
};
typedef struct MIDataReadDisassemblyInfo MIDataReadDisassemblyInfo;

extern MIDissasemblyInfo *			MIDissasemblyInfoNew(void);
extern MIDataReadDisassemblyInfo *	MIDataReadDisassembleInfoNew(void);
extern void							MIDissasemblyInfoFree(MIDissasemblyInfo *info);
extern void							MIDataReadDisassemblyInfoFree(MIDataReadDisassemblyInfo *info);
extern MIDissasemblyInfo *			MIDissasemblyInfoParse(MIValue *tuple);
extern MIDataReadDisassemblyInfo *	MIGetDataReadDisassemblyInfo(MICommand *cmd);
extern MIList *						MIGetDisassemblyList(MIValue *miValue);
#endif /* _MIDISASSEMBLE_H_ */

