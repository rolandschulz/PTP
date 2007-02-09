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
#include <ctype.h>

#include "list.h"
#include "MIValue.h"
#include "MIResult.h"
#include "MIArg.h"
#include "MIThread.h"
#include "MIOOBRecord.h"

MIThreadSelectInfo *
MIThreadSelectInfoNew(void) 
{
	MIThreadSelectInfo * info;
	info = (MIThreadSelectInfo *)malloc(sizeof(MIThreadSelectInfo));
	info->current_thread_id = 0;
	info->frame = NULL;
	return info;
}

MIThreadSelectInfo *
MISetThreadSelectInfo(MICommand *cmd) 
{
	MIThreadSelectInfo * info = MIThreadSelectInfoNew();
	MIValue * val;
	MIResultRecord * rr;
	MIResult * result;

	if (!cmd->completed || cmd->output == NULL || cmd->output->rr == NULL)
		return NULL;

	rr = cmd->output->rr;
	for (SetList(rr->results); (result = (MIResult *)GetListElement(rr->results)) != NULL; ) {
		if (strcmp(result->variable, "new-thread-id") == 0) {
			val = result->value;
			info->current_thread_id = (int)strtol(val->cstring, NULL, 10);
		}
		else if (strcmp(result->variable, "frame") == 0) {
			val = result->value;
			if (val->type == MIValueTypeTuple) {
				info->frame = MIFrameParse(val);
			}
		}
	}	
	return info;	
}
