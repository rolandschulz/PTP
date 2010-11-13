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
#include "MIDisassemble.h"

MIAssemble * 
MIAssembleNew(void)
{
	MIAssemble *	assemble;
	assemble = (MIAssemble *)malloc(sizeof(MIAssemble));
	assemble->addr = NULL;
	assemble->funcName = NULL;
	assemble->offset = 0;
	assemble->inst = NULL;
	return assemble;	
}

void 
MIAssembleFree(MIAssemble *assemble)
{
	if (assemble->addr != NULL)
		free(assemble->addr);
	if (assemble->funcName != NULL)
		free(assemble->funcName);
	if (assemble->inst != NULL)
		free(assemble->inst);
	free(assemble);
}

MIAssemble *
MIAssembleParse(MIValue *tuple)
{
	char *		str = NULL;
	char *		var = NULL;
	MIValue *	value = NULL;
	MIResult *	result = NULL;
	MIList *		results = tuple->results;
	MIAssemble *	assemble = MIAssembleNew();
	
	for (MIListSet(results); (result = (MIResult *)MIListGet(results)) != NULL;) {
		var = result->variable;
		value = result->value;
	
		if (value != NULL && value->type == MIValueTypeConst) {
			str = value->cstring;
		}
	
		if (strcmp(var, "address") == 0 && NULL != str) {
			assemble->addr = strdup(str);
		} else if (strcmp(var, "func-name") == 0 && NULL != str) {
			assemble->funcName = strdup(str);
		} else if (strcmp(var, "inst") == 0 && NULL != str) {
			assemble->inst = strdup(str);
		}
	}
	return assemble;
}


MIDataReadDisassembleInfo  *
MIDataReadDisassembleInfoNew(void)
{
	MIDataReadDisassembleInfo *	disassembleInfo;
	disassembleInfo = (MIDataReadDisassembleInfo  *)malloc(sizeof(MIDataReadDisassembleInfo));
	disassembleInfo->asm_insns = NULL;
	return disassembleInfo;	
}

void
MIDataReadDisassembleInfoFree(MIDataReadDisassembleInfo  *disassembleInfo)
{
	if (disassembleInfo->asm_insns != NULL)
		MIListFree(disassembleInfo->asm_insns, MIAssembleFree);
	free(disassembleInfo);
}

MIDataReadDisassembleInfo *
MIGetDataReadDisassembleInfo(MICommand *cmd)
{
	char * var;
	char * str;
	MIValue * value;
	MIResultRecord * rr;
	MIResult * result;
	MIDataReadDisassembleInfo  * info = MIDataReadDisassembleInfoNew();

	if (!cmd->completed || cmd->output == NULL || cmd->output->rr == NULL) {
		return NULL;
	}
	
	rr = cmd->output->rr;
	for (MIListSet(rr->results); (result = (MIResult *)MIListGet(rr->results)) != NULL; ) {
		var = result->variable;
		value = result->value;

		if (value != NULL || value->type == MIValueTypeConst) {
			str = value->cstring;
		}

		if (strcmp(var, "asm_insns") == 0) {
			if (value->type == MIValueTypeList) {
				info->asm_insns = MIGetAssembleList(value);
			}
		}
	}	
	return info;
}

MIList *
MIGetAssembleList(MIValue *miValue)
{
	MIList *assembles = MIListNew();
	MIList *values = miValue->values;
	MIValue *value;
	
	if (values != NULL) {
		for (MIListSet(values); (value = (MIValue *)MIListGet(values)) != NULL;) {
			if (value->type == MIValueTypeTuple) {
				MIListAdd(assembles, (void *)MIAssembleParse(value));
			}
		}
	}
	return assembles;
}

