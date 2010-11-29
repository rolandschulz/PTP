/*
 * Copyright (c) 2010 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

#ifndef _X10_VARIABLE_H_
#define _X10_VARIABLE_H_

#include <stdint.h>

#include "x10_dbg_types.h"

typedef struct x10variable_t {
	char* 				name;
	char*				cpp_name;
	char* 				cpp_full_name;
	x10dbg_type_t		type;
	int32_t 			type_index;
	int32_t				map_index;
	char* 				type_name;
	int					size;
	int					offset;		/* 0 for non-members */;
	char*				location;
	long				num_children;
	int					is_reference;
	int					is_array;
} x10variable_t;

typedef struct x10member_t {
	x10dbg_type_t		type;
	char                *name;
} x10member_t;

typedef struct x10_builtin_type_t {
	char*	type_name;
	int	member_count;
	x10member_t *member;
} x10_builtin_type_t;

extern x10variable_t *	X10VariableNew();
extern void				X10VariableFree(x10variable_t *x10Variable);

#endif /* _X10_VARIABLE_H_ */

