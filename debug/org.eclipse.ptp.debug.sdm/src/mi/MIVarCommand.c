/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
 
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include "list.h"
#include "MIString.h"
#include "MICommand.h"

MICommand *
MIVarCreate(char *name, char *frame, char *expr)
{
	MICommand *	cmd;
	
	cmd = MICommandNew("-var-create", MIResultRecordDONE);
	MICommandAddOption(cmd, name, frame);
	MICommandAddOption(cmd, expr, NULL);
	return cmd;
}

MICommand *
MIVarDelete(char *name)
{
	MICommand *	cmd;
	
	cmd = MICommandNew("-var-delete", MIResultRecordDONE);
	MICommandAddOption(cmd, name, NULL);
	return cmd;
}

MICommand *
MIVarListChildren(char *name)
{
	MICommand *	cmd;
	
	cmd = MICommandNew("-var-list-children", MIResultRecordDONE);
	MICommandAddOption(cmd, name, NULL);
	return cmd;
}

MICommand *
MIVarEvaluateExpression(char *name)
{
	MICommand *	cmd;
	
	cmd = MICommandNew("-var-evaluate-expression", MIResultRecordDONE);
	MICommandAddOption(cmd, name, NULL);
	return cmd;
}

MICommand *
MIPType(char *name) {
	MICommand * cmd;
	cmd = MICommandNew("ptype", MIResultRecordDONE);
	MICommandAddOption(cmd, name, NULL);
	return cmd;
}
