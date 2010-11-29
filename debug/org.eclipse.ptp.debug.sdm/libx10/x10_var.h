/*
 * Copyright (c) 2010 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

#ifndef _X10_VAR_H_
#define _X10_VAR_H_

#include "x10_variable.h"
#include "list.h"

typedef struct x10_var_t {
	x10variable_t *var;
	List *children;
} x10_var_t;

extern x10_var_t *	X10VarNew();
extern void			X10VarFree(x10_var_t *var);

#endif /* _X10_VAR_H_ */

