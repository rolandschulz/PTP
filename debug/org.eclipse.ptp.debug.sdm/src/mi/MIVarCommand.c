/******************************************************************************
 * Copyright (c) 2005 The Regents of the University of California.
 * This material was produced under U.S. Government contract W-7405-ENG-36
 * for Los Alamos National Laboratory, which is operated by the University
 * of California for the U.S. Department of Energy. The U.S. Government has
 * rights to use, reproduce, and distribute this software. NEITHER THE
 * GOVERNMENT NOR THE UNIVERSITY MAKES ANY WARRANTY, EXPRESS OR IMPLIED, OR
 * ASSUMES ANY LIABILITY FOR THE USE OF THIS SOFTWARE. If software is modified
 * to produce derivative works, such modified software should be clearly  
 * marked, so as not to confuse it with the version available from LANL.
 *
 * Additionally, this program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * LA-CC 04-115
 ******************************************************************************/
 
 /*
  * Based on the QNX Java implementation of the MI interface
  */
 
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
MIVarUpdate(char *name)
{
	MICommand *	cmd;
	
	cmd = MICommandNew("-var-update", MIResultRecordDONE);
	MICommandAddOption(cmd, name, NULL);
	return cmd;
}

MICommand *
MIDataEvaluateExpression(char *name)
{
	MICommand *	cmd;
	
	cmd = MICommandNew("-data-evaluate-expression", MIResultRecordDONE);
	MICommandAddOption(cmd, name, NULL);
	return cmd;
}

MICommand *
MIVarInfoType(char *name)
{
	MICommand *	cmd;
	
	cmd = MICommandNew("-var-info-type", MIResultRecordDONE);
	MICommandAddOption(cmd, name, NULL);
	return cmd;
}

MICommand *
MIVarInfoNumChildren(char *name)
{
	MICommand *	cmd;
	
	cmd = MICommandNew("-var-info-num-children", MIResultRecordDONE);
	MICommandAddOption(cmd, name, NULL);
	return cmd;
}
