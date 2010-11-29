/*
 * Copyright (c) 2010 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
 
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include "disassembly.h"

/*
 * Create a new DisassemblyInfo record.
 *
 */
DisassemblyInfo *
NewDisassemblyInfo(void) {
	DisassemblyInfo * info = (DisassemblyInfo *)malloc(sizeof(DisassemblyInfo));
	info->addr = NULL;
	info->func_name = NULL;
	info->offset = 0;
	info->inst = NULL;
	return info;
}

/*
 * Free up the memory of an DisassemblyInfo record.
 *
 */
void 
FreeDisassemblyInfo(DisassemblyInfo *info) {
	if (info->addr != NULL)
		free(info->addr);
	if (info->func_name != NULL)
		free(info->func_name);
	if (info->inst!= NULL)
		free(info->inst);
	free(info);
}

