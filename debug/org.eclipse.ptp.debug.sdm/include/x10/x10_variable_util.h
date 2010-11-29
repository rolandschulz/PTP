/*
 * Copyright (c) 2010 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

#ifndef _X10_VARIABLE_UTIL_H_
#define _X10_VARIABLE_UTIL_H_

#include "x10_var.h"
#include "MI.h"

extern x10_var_t *	GetX10VariableDetails(MISession *session, char *cppFileName, int cppLineNum, char *rootName, List *offsprings, long arraySize, long arrayStartIndex, int listChildren);

#endif /* _X10_VARIABLE_UTIL_H_ */
