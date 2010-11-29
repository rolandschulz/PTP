/*
 * Copyright (c) 2010 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
 
#ifdef __gnu_linux__
#define _GNU_SOURCE
#endif /* __gnu_linux__ */

#include "config.h"

#include <stdlib.h>
#include <unistd.h>
#include <string.h>

#include "dbg.h"
#include "dbg_error.h"
#include "gdb.h"
#include "x10/x10_da.h"
#include "x10/x10_aif.h"
#include "x10/x10_var.h"

extern int GetArrayElementType(x10variable_t * var);

/*
 * Get the value of an variable.
 *
 */
static char *
X10GetVarValue(MISession *session, x10variable_t *var)
{
	char *value;
	int  length;
	VariableValue(session, var, &value, &length);
	return value;
}

/*
 * Create an AIF for a simple variable.
 *
 */
static AIF *
X10CreateSimpleAIF(x10dbg_type_t id, char *res)
{
	if (res == NULL) {
		return NULL;
	}
	switch (id) {
	case X10DTChar:
		return CharToAIF((char)res[0]);
	case X10DTString:
		return StringToAIF(res);
	case X10DTShort:
		return ShortToAIF((short)strtol(res, NULL, 10));
	case X10DTUShort:
		return UnsignedShortToAIF((unsigned short)strtoul(res, NULL, 10));
	case X10DTByte:
		return ShortToAIF((short)strtol(res, NULL, 10));
	case X10DTUByte:
		return ShortToAIF((short)strtol(res, NULL, 10));
	case X10DTBoolean:
		return BoolToAIF((unsigned int)strtol(res, NULL, 10));
	case X10DTInt:
		return IntToAIF((int)strtol(res, NULL, 10));
	case X10DTUInt:
		return UnsignedIntToAIF((unsigned int)strtoul(res, NULL, 10));
#ifdef CC_HAS_LONG_LONG
	case X10DTLong:
		return LongLongToAIF((long long)strtoll(res, NULL, 10));
	case X10DTULong:
		return UnsignedLongLongToAIF((unsigned long long)strtoull(res, NULL, 10));
#endif /* CC_HAS_LONG_LONG */
	case X10DTFloat:
		return FloatToAIF((float)strtod(res, NULL));
	case X10DTDouble:
		return DoubleToAIF(strtod(res, NULL));
	default://other type
		return VoidToAIF(0, 0);
	}
}

/*
 * Create an AIF for a simple variable, and retrieve its value.
 *
 */
static AIF *
X10GetSimpleAIF(MISession *session, x10_var_t *var)
{
	AIF *	a = NULL;
	char *	v;

	DEBUG_PRINTF(DEBUG_LEVEL_BACKEND, "---------------------- GetSimpleAIF (%s)\n", var->var->type_name);

	int id = var->var->type;
	
	switch (id) {
	case X10DTInt:
	case X10DTLong:
	case X10DTShort :
	case X10DTByte:
	case X10DTChar:
	case X10DTFloat:
	case X10DTDouble:
	case X10DTBoolean:
	case X10DTUByte:
	case X10DTUInt: 
	case X10DTULong: 
	case X10DTUShort: 
	case X10DTString:
		v = X10GetVarValue(session, var->var);
		a = X10CreateSimpleAIF(id, v);
		free(v);
		return a;
	case X10DTClass:
	case X10DTRandom:
	case X10DTDist:
		a = EmptyAggregateToAIF(var->var->type_name);
		return a;
	default:
		v = X10GetVarValue(session, var->var);
		a = X10CreateSimpleAIF(id, v);
		free(v);
		return a;
	}
}

/*
 * Get the base type of an array from it's type
 * definition string. This is only used if it's
 * not possible to get the type of one of the
 * array elements.
 */
static int
X10GetArrayBaseType(MISession *session, x10_var_t *var)
{
	return GetArrayElementType(var->var);
}

/*
 * Create an AIF for an array variable.
 *
 */
static AIF *
X10GetArrayAIF(MISession *session, char *expr, x10_var_t *var)
{
	int		id;
	AIF *	a = NULL;
	AIF *	ac;

	//We will not display content in array.
	id = X10GetArrayBaseType(session, var);
	ac = X10CreateSimpleAIF(id, "");
	a = EmptyArrayToAIF(0, 0, ac);
	AIFFree(ac);
	return a;
}

/*
 * Create an AIF for the input variable.
 *
 */
static AIF *
X10GetComplexAIF(MISession *session, char *expr, x10_var_t *var)
{
	AIF *	a = NULL;

	DEBUG_PRINTF(DEBUG_LEVEL_BACKEND, "---------------------- GetPartialComplexAIF (%s, %s)\n", expr != NULL ? expr : "NULL", var->var->type_name);

	int id = var->var->type;
	
	switch (id) {
	case X10DTArray:
	case X10DTDistArrayLocalState:
		a = X10GetArrayAIF(session, expr, var);
		break;
	case X10DTClass:
	case X10DTRegion:
	case X10DTPlaceLocalHandle:
	case X10DTPoint:
	case X10DTDistArray:
		//We will just treat them as no children
		a = X10GetSimpleAIF(session, var);
		break;
	default:
		/*
		 * Maybe it was simple all along
		 */
		a = X10GetSimpleAIF(session, var);
	}

	DEBUG_PRINTF(DEBUG_LEVEL_BACKEND, "---------------------- GetPartialComplexAIF id is %d, returns %s\n", id, a != NULL ? AIF_FORMAT(a) : "NULL");
	return a;
}


/*
 * Get the AIF of the partially evaluated var of array type.
 *
 */
static AIF *
X10GetPartialArrayAIF(MISession *session, char *expr, x10_var_t *var)
{
	AIF *	a = NULL;
	AIF *	ac;
	int 	i= 0;
	int 	id;
	x10_var_t *		child = NULL;

	DEBUG_PRINTF(DEBUG_LEVEL_BACKEND, "---------------------- GetPartialArrayAIF (%s, %s)\n", expr != NULL ? expr : "NULL", var->var->type_name);

	if (var->children == NULL || var->var->num_children <= 0) {
		id = X10GetArrayBaseType(session, var);
		ac = X10CreateSimpleAIF(id, "");
		int numChildren = var->var->num_children;
		a = EmptyArrayToAIF(0, numChildren, ac);
		AIFFree(ac);
	} 
	else {
		for (SetList(var->children); (child = (x10_var_t *)GetListElement(var->children)) != NULL;) {
			ac = X10GetPartialAIF(session, expr, child);
			int numChildren = var->var->num_children;
			if (ac != NULL) {
				if (a == NULL) {
					a = EmptyArrayToAIF(0, numChildren, ac);
				}
				AIFAddArrayElement(a, i, ac);
				AIFFree(ac);
			}
			i++;
		}	
	}
	return a;
}

/*
 * Add a partially evaluated field member to an Aggregate AIF.
 *
 */
static void
X10AddPartialFieldToAggregate(MISession *session, char *field, AIF *a, x10_var_t *var, AIFAccess access)
{
	AIF *	ac = X10GetPartialAIF(session, field, var);
	if (ac != NULL) {
		DEBUG_PRINTF(DEBUG_LEVEL_BACKEND, "---------------------- X10AddPartialFieldToAggregate adding field %s = (%s, %d)\n", var->var->name, AIF_FORMAT(ac), AIF_LEN(ac));
		AIFAddFieldToAggregate(a, access, var->var->name, ac);
		AIFFree(ac);
	} else {
		DEBUG_PRINTF(DEBUG_LEVEL_BACKEND, "---------------------- X10AddPartialFieldToAggregate field %s was null!\n", field);
	}
}

/*
 * Create an AIF for the input aggregate variable, and evaluate its immediate children.
 */
static AIF *
X10GetPartialAggregateAIF(MISession *session, char *expr, x10_var_t *var)
{
	AIF *		a;
	char *		field;
	x10_var_t *		child;

	DEBUG_PRINTF(DEBUG_LEVEL_BACKEND, "---------------------- GetPartialAggregateAIF (%s, %s)\n", expr != NULL ? expr : "NULL", var->var->type_name);

	a = EmptyAggregateToAIF(var->var->type_name);
	
	if (var->children != NULL) {
		for (SetList(var->children); (child = (x10_var_t *)GetListElement(var->children)) != NULL;) {
			asprintf(&field, "(%s).%s", expr, child->var->name);
			X10AddPartialFieldToAggregate(session, field, a, child, AIF_ACCESS_PUBLIC);
			free(field);
		}
	}
	return a;
}

/*
 * Create a complex AIF object corresponding to 'var'.
 */
static AIF *
X10GetPartialComplexAIF(MISession *session, char *expr, x10_var_t *var)
{
	AIF *	a = NULL;

	DEBUG_PRINTF(DEBUG_LEVEL_BACKEND, "---------------------- GetPartialComplexAIF (%s, %s)\n", expr != NULL ? expr : "NULL", var->var->type_name);

	int id = var->var->type;
	
	switch (id) {
	case X10DTArray:
	case X10DTDistArrayLocalState:
		a = X10GetPartialArrayAIF(session, expr, var);
		break;
	
	case X10DTClass:
	case X10DTRegion:
	case X10DTPlaceLocalHandle:
	case X10DTPoint:
	case X10DTDistArray:
		a = X10GetPartialAggregateAIF(session, expr, var);
		break;
	default:
		/*
		 * Maybe it was simple all along
		 */
		a = X10GetSimpleAIF(session, var);
	}

	DEBUG_PRINTF(DEBUG_LEVEL_BACKEND, "---------------------- GetPartialComplexAIF id is %d, returns %s\n", id, a != NULL ? AIF_FORMAT(a) : "NULL");
	return a;
}

/*
 * Create a partial AIF object corresponding to 'var'.
 *
 * A partial AIF object contains only only enough type
 * information to determine if the variable is
 * structured or not. This speeds up displaying the variable
 * in the UI as the variable contents do not need to be
 * read from the program.
 *
 * Detailed type information and the variable contents will
 * be requested by the UI as the user drills into the variable.
 */
AIF *
X10GetPartialAIF(MISession *session, char *expr, x10_var_t *var)
{
	DEBUG_PRINTF(DEBUG_LEVEL_BACKEND, "---------------------- GetPartialAIF (%s, %s)\n", expr != NULL ? expr : "NULL", var->var->type_name);

	if (var->var->num_children == 0 && var->var->is_array == 0) {
		return X10GetSimpleAIF(session, var);
	}
	return X10GetPartialComplexAIF(session, expr, var);
}

/*
 * Get the AIF of the the input variable.
 *
 */
AIF *
X10GetAIF(MISession *session, char *expr, x10_var_t *var)
{
	DEBUG_PRINTF(DEBUG_LEVEL_BACKEND, "---------------------- X10GetPartialAIF (%s, %s)\n", expr != NULL ? expr : "NULL", var->var->type_name);

	if (var->var->num_children == 0 && var->var->is_array == 0) {
		return X10GetSimpleAIF(session, var);
	}
	return X10GetComplexAIF(session, expr, var);
}

