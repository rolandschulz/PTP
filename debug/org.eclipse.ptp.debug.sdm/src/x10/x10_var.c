/*
 * Copyright (c) 2010 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

#include "config.h"

#include <stdio.h>
#include <stdlib.h>

#include "x10/x10_var.h"

/*
 * Create a new x10_var.
 *
 */
x10_var_t *
X10VarNew()
{
	x10_var_t *	x10Var = (x10_var_t *)malloc(sizeof(x10_var_t));
	x10Var->var = NULL;
	x10Var->children = NULL;
	return x10Var;
}

/*
 * Free up the memory of an x10_var.
 *
 */
void
X10VarFree(x10_var_t *var)
{
	if (NULL == var) {
		return;
	}
	
	if (var->var != NULL) {
		//free _var
		X10VariableFree(var->var);
	}

	if (NULL != var->children) {
		DestroyList(var->children, X10VarFree);
	}
	
	free(var);
}
