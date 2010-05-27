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

//-break-condition NUMBER EXPR
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

//-break-watch [ -a | -r ]
MICommand *
MIBreakWatch(char *expr, int access, int read)
{
	MICommand *	cmd;
	
	cmd = MICommandNew("-break-watch", MIResultRecordDONE);

	if (access) {
		MICommandAddOption(cmd, "-a", NULL);
	} else if (read) {
		MICommandAddOption(cmd, "-r", NULL);
	}

	MICommandAddOption(cmd, expr, NULL);
	return cmd;
}

//-break-after NUMBER COUNT
MICommand *
MIBreakAfter(int nbps, int *bpids, int ignoreCount)
{
	int i;
	MICommand *	cmd;
	
	cmd = MICommandNew("-break-after", MIResultRecordDONE);

	for (i = 0; i < nbps; i++) {
		MICommandAddOption(cmd, MIIntToCString(bpids[i]), MIIntToCString(ignoreCount));
	}	
	return cmd;
}
