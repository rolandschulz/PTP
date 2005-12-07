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
MIArgNew(char *name, char *value)
{
	MIArg *	arg = (MIArg *)malloc(sizeof(MIArg));
	arg->name = strdup(name);
	arg->value = strdup(value);
	return arg;
}

void
MIArgFree(MIArg *arg)
{
	free(arg->name);
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
	
	for (SetList(values); (value = (MIValue *)GetListElement(values)) != NULL; ) {
		if (value->type == MIValueTypeTuple) {
			MIArg *arg = MIArgParse(value);
			if (arg != NULL) {
				AddToList(aList, (void *)arg);
			}
		}
	}
	
	for (SetList(results); (result = (MIResult *)GetListElement(results)) != NULL; ) {
		value = result->value;
		if (value->type == MIValueTypeConst) {
			MIArg *arg = MIArgNew(value->cstring, "");
			AddToList(aList, (void *)arg);
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
	List *		args = tuple->results;
	MIArg *		arg = NULL;
	MIValue *	value;
	MIResult *	result;
	char *		aName;
	char *		aValue;
	
	SetList(args);
	
	if (!EmptyList(args)) {
		// Name
		result = (MIResult *)GetListElement(args);
		value = result->value;
		if (value != NULL && value->type == MIValueTypeConst) {
			aName = value->cstring;
		} else {
			aName = "";
		}

		// Value
		if ((result = (MIResult *)GetListElement(args)) != NULL) {
			value = result->value;
			if (value != NULL && value->type == MIValueTypeConst) {
				aValue = value->cstring;
			} else {
				aValue = "";
			}
		}

		arg = MIArgNew(aName, aValue);
	}
	
	return arg;
}

MIString *
MIArgToString(MIArg *arg)
{
	return MIStringNew("%s=%s", arg->name, arg->value);
}
