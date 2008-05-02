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
 
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include "MICommand.h"
#include "MIString.h"

MICommand *
MIExecArguments(char **argv)
{
	int			i;
	int 		len = 0;
	char *		arg_str;
	MICommand *	cmd;
	
	for (i = 0; argv[i] != NULL; i++) {
		if (i > 0) {
			len += 1;
		}
		len += strlen(argv[i]);
	}
	
	arg_str = (char *)malloc(len * sizeof(char));
	*arg_str = '\0';
	
	for (i = 0; argv[i] != NULL; i++) {
		if (i > 0) {
			strcat(arg_str, " ");
		}
		strcat(arg_str, argv[i]);
	}
	
	cmd = MICommandNew("-exec-arguments", MIResultRecordRUNNING);
	MICommandAddOption(cmd, arg_str, NULL);
	return cmd;
}

MICommand *
MIExecContinue(void)
{
	return MICommandNew("-exec-continue", MIResultRecordRUNNING);
}

MICommand *
MIExecRun(void)
{
	return MICommandNew("-exec-run", MIResultRecordRUNNING);
}

MICommand *
MIExecStep(int count)
{
	MICommand *	cmd = MICommandNew("-exec-step", MIResultRecordRUNNING);
	MICommandAddOption(cmd, MIIntToCString(count), NULL);
	return cmd;
}

MICommand *
MIExecNext(int count)
{
	MICommand *	cmd = MICommandNew("-exec-next", MIResultRecordRUNNING);
	MICommandAddOption(cmd, MIIntToCString(count), NULL);
	return cmd;
}

MICommand *
MIExecFinish(void)
{
	return MICommandNew("-exec-finish", MIResultRecordRUNNING);
}

MICommand *
MIExecUtil(char* loc)
{
	MICommand *	cmd = MICommandNew("-exec-util", MIResultRecordRUNNING);
	MICommandAddOption(cmd, loc, NULL);
	return cmd;
}

MICommand *
MIExecInterrupt(void)
{
	return MICommandNew("-exec-interrupt", MIResultRecordDONE);
}

#ifdef __APPLE__
MICommand *
MIPidInfo(void)
{
	return MICommandNew("-pid-info", MIResultRecordDONE);
}
#endif /* __APPLE__ */