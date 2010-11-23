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

#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include "MIValue.h"
#include "MIResult.h"
#include "MIDisassembly.h"

MIDisassemblyInfo *
MIDisassemblyInfoNew(void)
{
	MIDisassemblyInfo *	info;
	info = (MIDisassemblyInfo *)malloc(sizeof(MIDisassemblyInfo));
	info->addr = NULL;
	info->func_name = NULL;
	info->offset = 0;
	info->inst = NULL;
	return info;
}

void 
MIDisassemblyInfoFree(MIDisassemblyInfo *info)
{
	if (info->addr != NULL)
		free(info->addr);
	if (info->func_name != NULL)
		free(info->func_name);
	if (info->inst != NULL)
		free(info->inst);
	free(info);
}

MIDisassemblyInfo *
MIDisassemblyInfoParse(MIValue *tuple)
{
	char *				var = NULL;
	MIValue *			value = NULL;
	MIResult *			result = NULL;
	MIList *			results = tuple->results;
	MIDisassemblyInfo *	info = MIDisassemblyInfoNew();
	
	for (MIListSet(results); (result = (MIResult *)MIListGet(results)) != NULL;) {
		var = result->variable;
		value = result->value;
	
		if (value != NULL && value->type == MIValueTypeConst) {
			if (strcmp(var, "address") == 0 && value->cstring != NULL) {
				info->addr = strdup(value->cstring);
			} else if (strcmp(var, "func-name") == 0 && value->cstring != NULL) {
				info->func_name = strdup(value->cstring);
			} else if (strcmp(var, "inst") == 0 && value->cstring != NULL) {
				info->inst = strdup(value->cstring);
			}
		}
	}
	return info;
}


MIDataReadDisassemblyInfo *
MIDataReadDisassemblyInfoNew(void)
{
	MIDataReadDisassemblyInfo *	info;
	info = (MIDataReadDisassemblyInfo *)malloc(sizeof(MIDataReadDisassemblyInfo));
	info->asm_insns = NULL;
	return info;
}

void
MIDataReadDisassemblyInfoFree(MIDataReadDisassemblyInfo *info)
{
	if (info->asm_insns != NULL)
		MIListFree(info->asm_insns, MIDisassemblyInfoFree);
	free(info);
}

MIDataReadDisassemblyInfo *
MIGetDataReadDisassemblyInfo(MICommand *cmd)
{
	char * 						var;
	MIValue * 					value;
	MIResultRecord * 			rr;
	MIResult * 					result;
	MIDataReadDisassemblyInfo  * info = MIDataReadDisassemblyInfoNew();

	if (!cmd->completed || cmd->output == NULL || cmd->output->rr == NULL) {
		return NULL;
	}
	
	rr = cmd->output->rr;
	for (MIListSet(rr->results); (result = (MIResult *)MIListGet(rr->results)) != NULL; ) {
		var = result->variable;
		value = result->value;

		if (strcmp(var, "asm_insns") == 0) {
			if (value->type == MIValueTypeList) {
				info->asm_insns = MIGetDisassemblyList(value);
			}
		}
	}	
	return info;
}

MIList *
MIGetDisassemblyList(MIValue *miValue)
{
	MIList *	list = MIListNew();
	MIList *	values = miValue->values;
	MIValue *	value;
	
	if (values != NULL) {
		for (MIListSet(values); (value = (MIValue *)MIListGet(values)) != NULL;) {
			if (value->type == MIValueTypeTuple) {
				MIListAdd(list, (void *)MIDisassemblyInfoParse(value));
			}
		}
	}
	return list;
}

