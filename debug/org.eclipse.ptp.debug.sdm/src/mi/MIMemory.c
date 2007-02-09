/*******************************************************************************
 * Copyright (c) 2005 The Regents of the University of California. 
 * This material was produced under U.S. Government contract W-7405-ENG-36 
 * for Los Alamos National Laboratory, which is operated by the University 
 * of California for the U.S. Department of Energy. The U.S. Government has 
 * rights to use, reproduce, and distribute this software. NEITHER THE 
 * GOVERNMENT NOR THE UNIVERSITY MAKES ANY WARRANTY, EXPRESS OR IMPLIED, OR 
 * ASSUMES ANY LIABILITY FOR THE USE OF THIS SOFTWARE. If software is modified 
 * to produce derivative works, such modified software should be clearly marked, 
 * so as not to confuse it with the version available from LANL.
 * 
 * Additionally, this program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * LA-CC 04-115
 *******************************************************************************/
 
 /**
 * @author Clement chu
 * 
 */
 
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include "MIValue.h"
#include "MIResult.h"
#include "MIMemory.h"

MIMemory * 
MIMemoryNew(void)
{
	MIMemory *	memory;
	memory = (MIMemory *)malloc(sizeof(MIMemory));
	memory->addr = NULL;
	memory->ascii = NULL;
	memory->data = NULL;
	return memory;	
}

void 
MIMemoryFree(MIMemory *memory)
{
	if (memory->addr != NULL)
		free(memory->addr);
	if (memory->ascii != NULL)
		free(memory->ascii);
	if (memory->data != NULL)
		DestroyList(memory->data, free);
	free(memory);
}

MIMemory *
MIMemoryParse(MIValue *tuple)
{
	char *		str;
	char *		var;
	MIValue *	value;
	MIResult *	result;
	List *		results = tuple->results;
	MIMemory *	memory = MIMemoryNew();
	
	for (SetList(results); (result = (MIResult *)GetListElement(results)) != NULL;) {
		var = result->variable;
		value = result->value;
	
		if (value != NULL && value->type == MIValueTypeConst) {
			str = value->cstring;
		}
	
		if (strcmp(var, "addr") == 0) {
			memory->addr = strdup(str);
		} else if (strcmp(var, "ascii") == 0) {
			memory->ascii = strdup(str);
		} else if (strcmp(var, "data") == 0) {
			if (value != NULL) {
				memory->data = MIMemoryDataParse(value);
			}
		}
	}
	return memory;
}

List *
MIMemoryDataParse(MIValue* miValue)
{
	List * data = NewList();
	List * values = miValue->values;
	MIValue * value;
	
	if (values != NULL) {
		for (SetList(values); (value = (MIValue *)GetListElement(values)) != NULL; ) {
			if (value->type == MIValueTypeConst) {
				AddToList(data, (void *) strdup(value->cstring));
			}
		}
	}
	return data;
}

MIDataReadMemoryInfo *
MIDataReadMemoryInfoNew(void)
{
	MIDataReadMemoryInfo *	memoryInfo;
	memoryInfo = (MIDataReadMemoryInfo *)malloc(sizeof(MIDataReadMemoryInfo));
	memoryInfo->addr = NULL;
	memoryInfo->memories = NULL;
	return memoryInfo;	
}

void
MIDataReadMemoryInfoFree(MIDataReadMemoryInfo *memoryInfo)
{
	if (memoryInfo->addr != NULL)
		free(memoryInfo->addr);
	if (memoryInfo->memories != NULL)
		DestroyList(memoryInfo->memories, MIMemoryFree);
	free(memoryInfo);
}

MIDataReadMemoryInfo *
MIGetDataReadMemoryInfo(MICommand *cmd)
{
	char * var;
	char * str;
	MIValue * value;
	MIResultRecord * rr;
	MIResult * result;
	MIDataReadMemoryInfo * info = MIDataReadMemoryInfoNew();

	if (!cmd->completed || cmd->output == NULL || cmd->output->rr == NULL) {
		return NULL;
	}
	
	rr = cmd->output->rr;
	for (SetList(rr->results); (result = (MIResult *)GetListElement(rr->results)) != NULL; ) {
		var = result->variable;
		value = result->value;

		if (value != NULL || value->type == MIValueTypeConst) {
			str = value->cstring;
		}

		if (strcmp(var, "addr") == 0) {
			info->addr = strdup(str);
		} else if (strcmp(var, "nr-bytes") == 0) {
			info->numBytes = strtol(str, NULL, 10);
		} else if (strcmp(var, "total-bytes") == 0) {
			info->totalBytes = strtol(str, NULL, 10);
		} else if (strcmp(var, "next-row") == 0) {
			info->nextRow = strtol(str, NULL, 10);
		} else if (strcmp(var, "prev-row") == 0) {
			info->prevRow = strtol(str, NULL, 10);
		} else if (strcmp(var, "next-page") == 0) {
			info->nextPage = strtol(str, NULL, 10);
		} else if (strcmp(var, "prev-page") == 0) {
			info->prevPage = strtol(str, NULL, 10);
		} else if (strcmp(var, "memory") == 0) {
			if (value->type == MIValueTypeList) {
				info->memories = MIGetMemoryList(value);
			}
		}
	}	
	return info;
}

List *
MIGetMemoryList(MIValue *miValue)
{
	List *memories = NewList();
	List *values = miValue->values;
	MIValue *value;
	
	if (values != NULL) {
		for (SetList(values); (value = (MIValue *)GetListElement(values)) != NULL;) {
			if (value->type == MIValueTypeTuple) {
				AddToList(memories, (void *)MIMemoryParse(value));
			}
		}
	}
	return memories;
}
