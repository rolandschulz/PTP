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
#include "MIVar.h"

MIVar *
MIVarNew(void)
{
	MIVar *	var = (MIVar *)malloc(sizeof(MIVar));
	var->name = NULL;
	var->type = NULL;
	var->exp = NULL;
	var->numchild = 0;
	var->children = NULL;
	return var;
}

void
MIVarFree(MIVar *var)
{
	int i;
	
	if (var->name != NULL)
		free(var->name);
	if (var->type != NULL)
		free(var->type);
	if (var->exp != NULL)
		free(var->exp);
	if (var->children != NULL) {
		for (i = 0; i < var->numchild; i++)
			MIVarFree(var->children[i]);
		free(var->children);
	}
	free(var);
}

MIVar *
MIVarParse(MIValue *tuple)
{
	MIValue *	value;
	MIResult *	result;
	MIVar *		var = MIVarNew();
	char *		str;
	
	for (SetList(tuple->results); (result = (MIResult *)GetListElement(tuple->results)) != NULL; ) {
		value = result->value;
		if (value != NULL && value->type == MIValueTypeConst) {
			str = value->cstring;
		} else {
			str = "";
		}

		if (strcmp(result->variable, "numchild") == 0) {
			var->numchild = atoi(str);
		} else if (strcmp(result->variable, "name") == 0) {
			var->name = strdup(str);
		} else if (strcmp(result->variable, "type") == 0) {
			var->type = strdup(str);
		} else if (strcmp(result->variable, "exp") == 0) {
			var->exp = strdup(str);
		}
	}
	
	return var;
}

/*
 * Some gdb MacOSX do not return a MITuple so we have
 * to check for different format.
 * See PR 81019
 */
void 
parseChildren(MIValue *val, List **res)
{
	MIValue *	value;
	MIResult *	result;
	List *		children = NULL;
	List * 		results = val->results;

	if (results != NULL) {
		for (SetList(results); (result = (MIResult *)GetListElement(results)) != NULL; ) {
			if (strcmp(result->variable, "child") == 0) { //$NON-NLS-1$
				value = result->value;
				if (value->type == MIValueTypeTuple) {
					if (children == NULL)
						children = NewList();
					AddToList(children, MIVarParse(value));
				}
			}
		}
	}
	
	*res = children;
}

void
MIVarGetVarListChildrenInfo(MIVar *var, MIResultRecord *rr)
{
	int			num;
	MIVar *		child;
	MIValue *	value;
	MIResult *	result;
	List *		children = NULL;
	
	if (rr != NULL) {
		for (SetList(rr->results); (result = (MIResult *)GetListElement(rr->results)) != NULL; ) {
			value = result->value;

			if (strcmp(result->variable, "numchild") == 0) {
				if (value->type == MIValueTypeConst) {
					var->numchild = atoi(value->cstring);
				}
			} else if (strcmp(result->variable, "children") == 0) {
				parseChildren(value, &children);
			}
		}
	}
	
	if (children != NULL) {
		num = SizeOfList(children);
		if (var->numchild != num)
			var->numchild = num;
		var->children = (MIVar **)malloc(sizeof(MIVar *) * var->numchild);
		for (num = 0, SetList(children); (child = (MIVar *)GetListElement(children)) != NULL; )
			var->children[num++] = child;
	}
}


