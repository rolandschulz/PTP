/*
 * Copyright (c) 2010 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
 
#include "config.h"

#include <stdlib.h>

#include "x10/x10_variable_util.h"
#include "x10/x10_metadebug_info_map.h"

/*
 * Built up the variable information of the input variable name, depending on the input context (C++ file name + line number).
 */
x10_var_t *
GetX10VariableDetails(MISession *session, char *cppFileName, int cppLineNum, char *rootName, List *offsprings, long arraySize, long arrayStartIndex, int listChildren)
{
	return GetSymbolVariable(session, cppFileName, cppLineNum, rootName, offsprings, arraySize, arrayStartIndex, listChildren);
}

