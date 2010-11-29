/*
 * Copyright (c) 2010 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
 
#ifndef _X10_DA_H_
#define _X10_DA_H_

#include "x10_variable.h"
#include "MI.h"
#include "dbg.h"
#include "gdb.h"

extern int 				IsArray(x10variable_t var);
extern int				IsClass(x10variable_t var);
extern x10variable_t *	MemberVariable(MISession *session, x10variable_t *parent, const char* name, int type);
extern int				place();
extern int				QueryBuiltinType(MISession *session, int x10Type, x10_builtin_type_t** buildInType);
extern x10variable_t *	SymbolVariable(void *session, const char* name);
extern char *			TypeName(x10dbg_type_t type);
extern int				VariableType(x10variable_t var);
extern int				VariableSize(x10variable_t var);
extern const char *		VariableName(x10variable_t var);
extern int				VariableOffsetOf(x10variable_t var);
extern char *			VariableLocation(MISession *session, x10variable_t *var);
extern char *			MemberLocation(MISession *session, x10variable_t *parent, x10variable_t *member);
extern int				VariableValue(MISession *session, x10variable_t *var, char** data, int* length);
extern long				ElementCount(MISession *session, x10variable_t *parent, x10variable_t *var);
extern char *			ElementRawDataLocation(MISession *session, x10variable_t *var);
extern char *			ElementDataLocation(MISession *session, x10variable_t *var, char *rawDataLocation, long elementIndex);
extern int				ElementType(x10variable_t *var);
extern int				element(x10variable_t *var, void** data, int* length);

#endif /* _X10_DA_H_ */
