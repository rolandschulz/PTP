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

MIBreakpoint *
MIBreakpointNew(void)
{
	MIBreakpoint *	bp;
	
	bp = (MIBreakpoint *)malloc(sizeof(MIBreakpoint));
	bp->number = 0;
	bp->enabled = 0;
	bp->line = 0;
	bp->times = 0;
	bp->ignore = 0;
	bp->isWpt = 0;
	bp->isAWpt = 0;
	bp->isRWpt = 0;
	bp->isWWpt = 0;
	bp->isHdw = 0;
	bp->type = NULL;
	bp->disp = NULL;
	bp->address = NULL;
	bp->func = NULL;
	bp->file = NULL;
	bp->cond = NULL;
	bp->what = NULL;
	bp->threadId = NULL;
	return bp;
}

void
MIBreakpointFree(MIBreakpoint *bp)
{
	if (bp->type != NULL)
		free(bp->type);
	if (bp->disp != NULL)
		free(bp->disp);
	if (bp->address != NULL)
		free(bp->address);
	if (bp->func != NULL)
		free(bp->func);
	if (bp->file != NULL)
		free(bp->file);
	if (bp->cond != NULL)
		free(bp->cond);
	if (bp->what != NULL)
		free(bp->what);
	if (bp->threadId != NULL)
		free(bp->threadId);
	free(bp);
}

MIBreakpoint *
MIBreakpointParse(MIValue *tuple) 
{
	char *			str;
	char *			var;
	MIValue *		value;
	MIResult *		result;
	List *			results = tuple->results;
	MIBreakpoint *	bp = MIBreakpointNew();
	
	for (SetList(results); (result = (MIResult *)GetListElement(results)) != NULL;) {
		var = result->variable;
		value = result->value;

		if (value == NULL || value->type != MIValueTypeConst)
			continue;

		str = value->cstring;
		
		if (strcmp(var, "number") == 0) {
			bp->number = atoi(str);
		} else if (strcmp(var, "type") == 0) {
			bp->type = strdup(str);
			//type="hw watchpoint"
			if (strncmp(str, "hw", 2) == 0) {
				bp->isHdw = 1;
				bp->isWWpt = 1;
				bp->isWpt = 1;
			}
			//type="acc watchpoint"
			if (strncmp(str, "acc", 3) == 0) {
				bp->isWWpt = 1;
				bp->isRWpt = 1;
				bp->isWpt = 1;
			}
			//type="read watchpoint"
			if (strncmp(str, "read", 4) == 0) {
				bp->isRWpt = 1;
				bp->isWpt = 1;
			}
			// ??
			if (strcmp(str, "watchpoint") == 0) {
				bp->isWpt = 1;
			}
			// type="breakpoint"
			// default ok.
		} else if (strcmp(var, "disp") == 0) {
			bp->disp = strdup(str);
		} else if (strcmp(var, "enabled") == 0) {
			bp->enabled = strcmp(str, "y") == 0;
		} else if (strcmp(var, "addr") == 0) {
			bp->address = strdup(str);
		} else if (strcmp(var, "func") == 0) {
			bp->func = strdup(str);
		} else if (strcmp(var, "file") == 0) {
			bp->file = strdup(str);
		} else if (strcmp(var, "thread") == 0) {
			bp->threadId = strdup(str);
		} else if (strcmp(var, "line") == 0) {
			bp->line = atoi(str);
		} else if (strcmp(var, "times") == 0) {
			bp->times = atoi(str);
		} else if (strcmp(var, "what") == 0 || strcmp(var, "exp") == 0) { //$NON-NLS-2$
			bp->what = strdup(str);
		} else if (strcmp(var, "ignore") == 0) {
			bp->ignore = atoi(str);
		} else if (strcmp(var, "cond") == 0) {
			bp->cond = strdup(str);
		}
	}
	
	return bp;
}

List *
MIBreakpointGetBreakInsertInfo(MICommand *cmd)
{
	char *			var;
	MIValue *		val;
	MIResult *		result;
	MIBreakpoint *	bpt;
	MIResultRecord *	rr;
	List *			breakpoints = NULL;
	
	if (!cmd->completed || cmd->result == NULL)
		return NULL;
		
	rr = cmd->result;
	
	for (SetList(rr->results); (result = (MIResult *)GetListElement(rr->results)) != NULL; ) {
		var = result->variable;
		val = result->value;
		bpt = NULL;
		if (strcmp(var, "wpt") == 0) {
			if (val->type == MIValueTypeTuple) {
				bpt = MIBreakpointParse(val);
				bpt->enabled = 1;
				bpt->isWpt = 1;
				bpt->isWWpt = 1;
			}
		} else if (strcmp(var, "bkpt") == 0) {
			if (val->type == MIValueTypeTuple) {
				bpt = MIBreakpointParse(val);
				bpt->enabled = 1;
			}
		} else if (strcmp(var, "hw-awpt") == 0) {
			if (val->type == MIValueTypeTuple) {
				bpt = MIBreakpointParse(val);
				bpt->enabled = 1;
				bpt->isWpt = 1;
				bpt->isAWpt = 1;
			}
		} else if (strcmp(var, "hw-rwpt") == 0) {
			if (val->type == MIValueTypeTuple) {
				bpt = MIBreakpointParse(val);
				bpt->enabled = 1;
				bpt->isWpt = 1;
				bpt->isRWpt = 1;
			}
		}
		if (bpt != NULL) {
			if (breakpoints == NULL)
				breakpoints = NewList();
			AddToList(breakpoints, (void *)bpt);
		}
	}
		
	return breakpoints;
}
