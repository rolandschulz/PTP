/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <strings.h>
#include <ctype.h>

#include "compat.h"
#include "args.h"

int
main(int argc, char *argv[])
{
	char **	a1;
	char **	a2;
	char **	a3;
	char **	a4;
	char **	a5;
	char **	r1;
	char **	r2;
	char **	r3;
	char **	r4;
	char **	r5;
	char **	r6;
	
	a1 = NewArgs("1", "2", "3", "4", "5", "6", "7", NULL);
	printf("a1 = %s\n", ArgsToStr(a1));
	a2 = NewArgs("1", "2", "3", "4", "5", "6", "7", "8", NULL);
	printf("a2 = %s\n", ArgsToStr(a2));
	a3 = StrToArgs("a b c d e");
	printf("a3 = %s\n", ArgsToStr(a3));
	a4 = StrToArgs("a b c d e f g");
	printf("a4 = %s\n", ArgsToStr(a4));
	a5 = StrToArgs("a b c d e f g h");
	printf("a5 = %s\n", ArgsToStr(a5));
	
	r1 = AppendArgv(a1, a3);
	printf("r1 = %s\n", ArgsToStr(r1));
	r2 = AppendArgv(a2, a3);
	printf("r2 = %s\n", ArgsToStr(r2));
	r3 = AppendArgv(a1, a4);
	printf("r3 = %s\n", ArgsToStr(r3));
	r4 = AppendArgv(a2, a4);
	printf("r4 = %s\n", ArgsToStr(r4));
	r5 = AppendArgv(a1, a5);
	printf("r5 = %s\n", ArgsToStr(r5));
	r6 = AppendArgv(a2, a5);
	printf("r6 = %s\n", ArgsToStr(r6));
	
	FreeArgs(a1);
	FreeArgs(a2);
	FreeArgs(a3);
	FreeArgs(a4);
	FreeArgs(a5);
	FreeArgs(r1);
	FreeArgs(r2);
	FreeArgs(r3);
	FreeArgs(r4);
	FreeArgs(r5);
	FreeArgs(r6);
	
	return 0;
}
