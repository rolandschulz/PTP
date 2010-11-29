/*
 * Copyright (c) 2010 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

#ifndef _DISASSEMBLY_H_
#define _DISASSEMBLY_H_

#include "list.h"

struct DisassemblyInfo {
	char *addr;
	char *func_name;
	long offset;
	char *inst;
};
typedef struct DisassemblyInfo DisassemblyInfo;

extern DisassemblyInfo *	NewDisassemblyInfo(void);
extern void					FreeDisassemblyInfo(DisassemblyInfo *);

#endif /*_DISASSEMBLY_H_*/

