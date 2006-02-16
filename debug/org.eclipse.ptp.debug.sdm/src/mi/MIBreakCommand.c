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
#include "MIBreakpoint.h"
#include "MIValue.h"
#include "MIResult.h"
#include "MIString.h"

MICommand *
MIBreakInsert(int isTemporary, int isHardware, char *condition, int ignoreCount, char *where, int tid)
{
	MICommand *	cmd;
	
	cmd = MICommandNew("-break-insert", MIResultRecordDONE);

	if (isTemporary) {
		MICommandAddOption(cmd, "-t", NULL);
	} else if (isHardware) {
		MICommandAddOption(cmd, "-h", NULL);
	}
	if (condition != NULL) {
		MICommandAddOption(cmd, "-c", condition);
	}
	if (ignoreCount > 0) {
		MICommandAddOption(cmd, "-i", MIIntToCString(ignoreCount));
	}
	if (tid > 0) {
		MICommandAddOption(cmd, "-p", MIIntToCString(tid));
	}
	
	MICommandAddOption(cmd, where, NULL);
	
	return cmd;
}

MICommand *
MIBreakDelete(int nbps, int *bpids)
{
	int 			i;
	MICommand *	cmd;
	
	cmd = MICommandNew("-break-delete", MIResultRecordDONE);
	
	for (i = 0; i < nbps; i++) {
		MICommandAddOption(cmd, MIIntToCString(bpids[i]), NULL);
	}
	
	return cmd;
}

MICommand *
MIBreakDisable(int nbps, int *bpids)
{
	int 		i;
	MICommand *	cmd;
	
	cmd = MICommandNew("-break-disable", MIResultRecordDONE);
	
	for (i = 0; i < nbps; i++) {
		MICommandAddOption(cmd, MIIntToCString(bpids[i]), NULL);
	}
	
	return cmd;
}

MICommand *
MIBreakEnable(int nbps, int *bpids)
{
	int 		i;
	MICommand *	cmd;
	
	cmd = MICommandNew("-break-enable", MIResultRecordDONE);
	
	for (i = 0; i < nbps; i++) {
		MICommandAddOption(cmd, MIIntToCString(bpids[i]), NULL);
	}
	
	return cmd;
}

MICommand *
MIBreakCondition(int nbps, int *bpids, char *expr)
{
	int 		i;
	MICommand *	cmd;
	
	cmd = MICommandNew("-break-condition", MIResultRecordDONE);
	
	for (i = 0; i < nbps; i++) {
		MICommandAddOption(cmd, MIIntToCString(bpids[i]), expr);
	}	
	return cmd;
}
