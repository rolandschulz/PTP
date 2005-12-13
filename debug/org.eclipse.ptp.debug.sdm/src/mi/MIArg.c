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

#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include "list.h"
#include "MIValue.h"
#include "MIResult.h"
#include "MIArg.h"

MIArg *
MIArgNew(void)
{
	MIArg *	arg = (MIArg *)malloc(sizeof(MIArg));
	arg->name = NULL;
	arg->value = NULL;
	return arg;
}

void
MIArgFree(MIArg *arg)
{
	if (arg->name != NULL)
		free(arg->name);
	if (arg->value != NULL)
		free(arg->value);
	free(arg);
}

/**
 * Parsing a MIList of the form:
 * [{name="xxx",value="yyy"},{name="xxx",value="yyy"},..]
 * [name="xxx",name="xxx",..]
 * [{name="xxx"},{name="xxx"}]
 * 
 * Parsing a MITuple of the form:
 * {{name="xxx",value="yyy"},{name="xxx",value="yyy"},..}
 * {name="xxx",name="xxx",..}
 * {{name="xxx"},{name="xxx"}}
 */
List *
MIArgsParse(MIValue *miValue) 
{
	List *		aList = NewList();
	List *		values = miValue->values;
	List *		results = miValue->results;
	MIValue *	value;
	MIResult *	result;
	
	if (values != NULL) {
		printf("values != NULL\n");
		for (SetList(values); (value = (MIValue *)GetListElement(values)) != NULL; ) {
			if (value->type == MIValueTypeTuple) {
				MIArg *arg = MIArgParse(value);
				if (arg != NULL) {
					AddToList(aList, (void *)arg);
				}
			}
		}
	}
	
	if (results != NULL) {
		for (SetList(results); (result = (MIResult *)GetListElement(results)) != NULL; ) {
			value = result->value;
			if (value->type == MIValueTypeConst) {
				MIArg *arg = MIArgNew();
				arg->name = strdup(value->cstring);
				AddToList(aList, (void *)arg);
			}
		}
	}
	
	return aList;
}

/**
 * Parsing a MITuple of the form:
 * {name="xxx",value="yyy"}
 * {name="xxx"}
 */
MIArg *
MIArgParse(MIValue *tuple)
{
	MIValue *	value;
	MIResult *	result;
	char *		str;
	MIArg *		arg = MIArgNew();
	
	for (SetList(tuple->results); (result = (MIResult *)GetListElement(tuple->results)) != NULL; ) {
		value = result->value;
		if (value != NULL && value->type == MIValueTypeConst) {
			str = value->cstring;
		} else {
			str = "";
		}

		if (strcmp(result->variable, "name") == 0) {
			arg->name = strdup(str);
		} else if (strcmp(result->variable, "value") == 0) {
			arg->value = strdup(str);
		}
	}
	
	return arg;
}

MIString *
MIArgToString(MIArg *arg)
{
	return MIStringNew("%s=%s", arg->name, arg->value);
}
