/******************************************************************************
 * Copyright (c) 2005 The Regents of the University of California.
 * This material was produced under U.S. Government contract W-7405-ENG-36
 * for Los Alamos National Laboratory, which is operated by the University
 * of California for the U.S. Department of Energy. The U.S. Government has
 * rights to use, reproduce, and distribute this software. NEITHER THE
 * GOVERNMENT NOR THE UNIVERSITY MAKES ANY WARRANTY, EXPRESS OR IMPLIED, OR
 * ASSUMES ANY LIABILITY FOR THE USE OF THIS SOFTWARE. If software is modified
 * to produce derivative works, such modified software should be clearly  
 * marked, so as not to confuse it with the version available from LANL.
 *
 * Additionally, this program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * LA-CC 04-115
 ******************************************************************************/
 
 /*
  * Based on the QNX Java implementation of the MI interface
  */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include "list.h"
#include "MIValue.h"
#include "MIResult.h"
#include "MIVar.h"
#include "MIOOBRecord.h"

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

MIVarChange * MIVarChangeNew(void) {
	MIVarChange *varchange = (MIVarChange *)malloc(sizeof(MIVarChange));
	
	varchange->name = NULL;
	varchange->in_scope = 0;
	varchange->type_changed = 0;
	return varchange;
}

void MIVarChangeFree(MIVarChange *varchange) {
	if (varchange->name != NULL)
		free(varchange->name);
	free(varchange);
}

MIVar *
MIVarParse(List *results)
{
	MIValue *	value;
	MIResult *	result;
	MIVar *		var = MIVarNew();
	char *		str;
	
	for (SetList(results); (result = (MIResult *)GetListElement(results)) != NULL; ) {
		value = result->value;
		if (value != NULL && value->type == MIValueTypeConst) {
			str = value->cstring;
		} else {
			str = "";
		}

		if (strcmp(result->variable, "numchild") == 0) {
			var->numchild = (int)strtol(str, NULL, 10);
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

MIVar *
MIGetVarCreateInfo(MICommand *cmd) 
{
	if (!cmd->completed || cmd->output == NULL || cmd->output->rr == NULL)
		return NULL;
		
	return MIVarParse(cmd->output->rr->results);
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
					AddToList(children, MIVarParse(value->results));
				}
			}
		}
	}
	*res = children;
}

void
MIGetVarListChildrenInfo(MIVar *var, MICommand *cmd)
{
	int				num;
	MIVar *			child;
	MIValue *		value;
	MIResult *		result;
	MIResultRecord *	rr;
	List *			children = NULL;
	
	if (!cmd->completed || cmd->output == NULL || cmd->output->rr == NULL)
		return;

	rr = cmd->output->rr;
	for (SetList(rr->results); (result = (MIResult *)GetListElement(rr->results)) != NULL; ) {
		value = result->value;

		if (strcmp(result->variable, "numchild") == 0) {
			if (value->type == MIValueTypeConst) {
				var->numchild = (int)strtol(value->cstring, NULL, 10);
			}
		} else if (strcmp(result->variable, "children") == 0) {
			parseChildren(value, &children);
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

char *
MIGetVarEvaluateExpressionInfo(MICommand *cmd)
{
	MIValue *		value;
	MIResult *		result;
	MIResultRecord *	rr;
	char *			expr = NULL;

	if (!cmd->completed || cmd->output == NULL || cmd->output->rr == NULL)
		return NULL;

	rr = cmd->output->rr;
	for (SetList(rr->results); (result = (MIResult *)GetListElement(rr->results)) != NULL; ) {
		value = result->value;
		if (strcmp(result->variable, "value") == 0) {
			if (value->type == MIValueTypeConst) {
				expr = strdup(value->cstring);
			}
		}
	}
	return expr;
}

char *
MIGetDataEvaluateExpressionInfo(MICommand *cmd)
{
	MIValue *		value;
	MIResult *		result;
	MIResultRecord *	rr;
	char *			expr = NULL;

	if (!cmd->completed || cmd->output == NULL || cmd->output->rr == NULL)
		return NULL;

	rr = cmd->output->rr;
	for (SetList(rr->results); (result = (MIResult *)GetListElement(rr->results)) != NULL; ) {
		value = result->value;
		if (strcmp(result->variable, "value") == 0) {
			if (value->type == MIValueTypeConst) {
				expr = strdup(value->cstring);
			}
		}
	}
	return expr;
}

void
MIGetVarUpdateParseValue(MIValue* tuple, List* varchanges)
{
	MIResult * result;
	char * str;
	char * var;
	MIValue * value;
	MIVarChange * varchange;
	List * results = tuple->results;
	
	if (results != NULL) {
		for (SetList(results); (result = (MIResult *)GetListElement(results)) != NULL;) {
			var = result->variable;
			value = result->value;

			//this code is for mac
			if (value->type == MIValueTypeTuple) {
				MIGetVarUpdateParseValue(value, varchanges);
				continue;
			}
			
			if (value->type == MIValueTypeConst) {
				str = value->cstring;
			}
			
			if (strcmp(var, "name") == 0) {
				varchange = MIVarChangeNew();
				varchange->name = strdup(str);
				AddToList(varchanges, (void *)varchange);
			} else if (strcmp(var, "in_scope") == 0) {
				if (varchange != NULL) {
					varchange->in_scope = (strcmp(str, "true")==0)?1:0;
				}
			} else if (strcmp(var, "type_changed") == 0) {
				if (varchange != NULL) {
					varchange->type_changed = (strcmp(str, "true")==0)?1:0;
				}
			}
		}
	}
}

void
MIGetVarUpdateParser(MIValue* miVal, List *varchanges)
{
	MIValue* value;
	MIResult* result;

	if (miVal->type == MIValueTypeTuple) {
		MIGetVarUpdateParseValue(miVal, varchanges);
	}
	else if (miVal->type == MIValueTypeList) {
		if (EmptyList(miVal->values)) {
			for (SetList(miVal->results); (result = (MIResult *)GetListElement(miVal->results)) != NULL;) {
				MIGetVarUpdateParser(result->value, varchanges);
			}
		}
		else {
			for (SetList(miVal->values); (value = (MIValue *)GetListElement(miVal->values)) != NULL;) {
				MIGetVarUpdateParser(value, varchanges);
			}
		}
	}	
}

void
MIGetVarUpdateInfo(MICommand *cmd, List** varchanges)
{
	MIResult *		result;
	MIResultRecord *	rr;
	*varchanges = NewList();

	if (!cmd->completed || cmd->output == NULL || cmd->output->rr == NULL)
		return;

	rr = cmd->output->rr;
	for (SetList(rr->results); (result = (MIResult *)GetListElement(rr->results)) != NULL;) {
		if (strcmp(result->variable, "changelist") == 0) {
			MIGetVarUpdateParser(result->value, *varchanges);
		}
	}
}

MIVar *
MIGetVarInfoType(MICommand *cmd)
{
	if (!cmd->completed || cmd->output == NULL || cmd->output->rr == NULL)
		return NULL;

	return MIVarParse(cmd->output->rr->results);
}

void
MIGetVarInfoNumChildren(MICommand *cmd, MIVar *var)
{
	if (!cmd->completed || cmd->output == NULL || cmd->output->rr == NULL)
		return;

	List *results;
	MIValue *	value;
	MIResult *	result;
	char *		str;

	if (var == NULL)
		var = MIVarNew();

	results = cmd->output->rr->results;	
	for (SetList(results); (result = (MIResult *)GetListElement(results)) != NULL; ) {
		value = result->value;
		if (value != NULL && value->type == MIValueTypeConst) {
			str = value->cstring;
		} else {
			str = "";
		}

		if (strcmp(result->variable, "numchild") == 0) {
			var->numchild = (int)strtol(str, NULL, 10);
		}
	}
}
