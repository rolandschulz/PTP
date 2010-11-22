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

struct MIAssemble {
	char *	addr;
	char *	func_name;
	long	offset;
	char *	inst;
};
typedef struct MIAssemble MIAssemble;

struct MIDataReadDisassembleInfo {
	MIList *asm_insns;
};
typedef struct MIDataReadDisassembleInfo MIDataReadDisassembleInfo;

extern MIAssemble *					MIAssembleNew(void);
extern MIDataReadDisassembleInfo *	MIDataReadDisassembleInfoNew(void);
extern void							MIAssembleFree(MIAssemble *assemble);
extern void							MIDataReadDisassembleInfoFree(MIDataReadDisassembleInfo *assembleInfo);
extern MIAssemble *					MIAssembleParse(MIValue *tuple);
extern MIDataReadDisassembleInfo *	MIGetDataReadDisassembleInfo(MICommand *cmd);
extern MIList *						MIGetAssembleList(MIValue *miValue);
#endif /* _MIDISASSEMBLE_H_ */

