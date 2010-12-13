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

#include "x10/x10_variable.h"

/*
 * Create a new x10variable_t
 */
x10variable_t *
X10VariableNew()
{
	x10variable_t *	x10Variable = (x10variable_t *)malloc(sizeof(x10variable_t));
	x10Variable->name = NULL;
	x10Variable->cpp_name = NULL;
	x10Variable->cpp_full_name = NULL;
	x10Variable->type = X10DTNull;
	x10Variable->type_index = -1;
	x10Variable->map_index = -1;
	x10Variable->type_name = NULL;
	x10Variable->size = -1;
	x10Variable->offset = -1;
	x10Variable->location = NULL;
	x10Variable->num_children = 0;
	x10Variable->is_reference = 0;
	x10Variable->is_array = 0;
	return x10Variable;
}

/*
 * Free up the memory of an x10variable_t.
 */
void
X10VariableFree(x10variable_t *x10Variable)
{
	if (x10Variable != NULL) {
		if (NULL != x10Variable->name) {
			free(x10Variable->name);
		}
		if (NULL != x10Variable->cpp_name) {
			free(x10Variable->cpp_name);
		}
		if (NULL != x10Variable->cpp_full_name) {
			free(x10Variable->cpp_full_name);
		}
		if (NULL != x10Variable->type_name) {
			free(x10Variable->type_name);
		}
		if (NULL != x10Variable->location) {
			free(x10Variable->location);
		}
		free(x10Variable);
	}
}

