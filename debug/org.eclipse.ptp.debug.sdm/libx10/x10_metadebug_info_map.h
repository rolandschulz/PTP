/*
 * Copyright (c) 2010 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

#ifndef _X10_METADEBUG_INFO_MAP_H_
#define _X10_METADEBUG_INFO_MAP_H_

#include <stdlib.h>
#include <unistd.h>
#include <string.h>
#include <errno.h>

#include "dbg.h"
#include "gdb.h"
#include "x10_debug_line_info.h"
#include "x10_da.h"
#include "x10_var.h"

extern void			X10MetaDebugInfoMapCreate64(MISession *session, char *addressInfo);
extern int			X10LineToCPPLine(char* x10SourceFile, int x10Line, char **pCPPSourceFile, int *pCPPLine);
extern int			CPPLineToX10Line(char* cppSourceFile, int cppLine, char **pX10SourceFile, char **pX10Method, int *pX10Line);
extern int			MapX10MainToCPPLine(char **pCppFileName, int *pLineNumber);
extern int			CppLineInX10ToCPPMap(char* inputCPPSourceFileName, int inputCPPLine);
extern List *		GetX10LocalVarNames(char *cppFileName, int cppLineNum );
extern x10_var_t *	GetSymbolVariable (MISession *session, char *cppFileName, int cppLineNum, char *rootName, List *offsprings, long arraySize, long arrayStartIndex, int listChildren);
extern int			GetArrayElementType(x10variable_t * var);
extern void			ClearMetaDebugInfoMaps();

#endif /* _X10_METADEBUG_INFO_MAP_H_ */
