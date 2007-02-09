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

#include "list.h"
#include "MIEvent.h"
#include "MIResult.h"
#include "MIValue.h"

static void MIEventParseStopped(MIEvent *event, List *results);
static void MIEventParseValue(MIEvent *event, List *results);
static void MIEventParseWPT(MIEvent *event, List *results);

MIEvent *
MIEventNew(int class, int type)
{
	MIEvent * event = (MIEvent *)malloc(sizeof(MIEvent));
	event->class = class;
	event->type = type;
	
	event->threadId = 0;
	event->bkptno = 0;
	event->number = 0;
	event->code = 0;
	event->frame = NULL;
	event->sigName = NULL;
	event->sigMeaning = NULL;
	event->gdbResult = NULL;
	event->returnValue = NULL;
	event->returnType = NULL;
	event->exp = NULL;
	event->oldValue = NULL;
	event->newValue = NULL;
	
	return event;
}

void
MIEventFree(MIEvent *event)
{
	if (event->frame != NULL)
		MIFrameFree(event->frame);
	if (event->sigName != NULL)
		free(event->sigName);
	if (event->sigMeaning != NULL)
		free(event->sigMeaning);
	if (event->gdbResult != NULL)
		free(event->gdbResult);
	if (event->returnValue != NULL)
		free(event->returnValue);
	if (event->returnType != NULL)
		free(event->returnType);
	if (event->exp != NULL)
		free(event->exp);
	if (event->oldValue != NULL)
		free(event->oldValue);
	if (event->newValue != NULL)
		free(event->newValue);
	free(event);
}

MIEvent *
MIEventCreateStoppedEvent(char *reason, List *results)
{
	MIEvent *	event = NULL;
	
	if (strcmp(reason, "breakpoint-hit") == 0) {
		event = MIEventNew(MIEventClassStopped, MIEventTypeBreakpointHit);
	} else if (
		strcmp(reason, "watchpoint-trigger") == 0
			|| strcmp(reason, "read-watchpoint-trigger") == 0
			|| strcmp(reason, "access-watchpoint-trigger") == 0) {
		event = MIEventNew(MIEventClassStopped, MIEventTypeWatchpointTrigger);
	} else if (strcmp(reason, "watchpoint-scope") == 0) {
		event = MIEventNew(MIEventClassStopped, MIEventTypeWatchpointScope);
	} else if (strcmp(reason, "end-stepping-range") == 0) {
		event = MIEventNew(MIEventClassStopped, MIEventTypeSteppingRange);
	} else if (strcmp(reason, "signal-received") == 0) {
		event = MIEventNew(MIEventClassStopped, MIEventTypeSignal);
	} else if (strcmp(reason, "location-reached") == 0) {
		event = MIEventNew(MIEventClassStopped, MIEventTypeLocationReached);
	} else if (strcmp(reason, "function-finished") == 0) {
		event = MIEventNew(MIEventClassStopped, MIEventTypeFunctionFinished);
	} else if (strcmp(reason, "exited-normally") == 0 || strcmp(reason, "exited") == 0) {
		event = MIEventNew(MIEventClassStopped, MIEventTypeInferiorExit);
	} else if (strcmp(reason, "exited-signalled") == 0) {
		event = MIEventNew(MIEventClassStopped, MIEventTypeInferiorSignalExit);
	} else if (strcmp(reason, "temporary-breakpoint-hit") == 0) {
		/*
		 * temporary-breakpoint-hit is a fake reason we use because Linux
		 * support for temporary breakpoints is broken.
		 */ 
		event = MIEventNew(MIEventClassStopped, MIEventTypeSuspended);
	}
	
	MIEventParseStopped(event, results);
	return event;
}

static void
MIEventParseStopped(MIEvent *event, List *results)
{
	char *		str = "";
	MIResult *	res;
	MIValue *	value;
	
	if (results != NULL) {
		for (SetList(results); (res = (MIResult *)GetListElement(results)) != NULL; ) {
			value = res->value;
			if (value != NULL && value->type == MIValueTypeConst) {
				str = value->cstring;
			}
	
			if (strcmp(res->variable, "bkptno") == 0) {
				event->bkptno = (int)strtol(str, NULL, 10);
			} else if (strcmp(res->variable, "wpt") == 0 || strcmp(res->variable, "hw-awpt") == 0|| strcmp(res->variable, "hw-rwpt") == 0) {
				if (value->type == MIValueTypeTuple) {
					MIEventParseWPT(event, value->results);
				}
			} else if (strcmp(res->variable, "value") == 0) {
				if (value->type == MIValueTypeTuple) {
					MIEventParseValue(event, value->results);
				}
			} else if (strcmp(res->variable, "wpnum") == 0) {
				event->number = (int)strtol(str, NULL, 10);
			} else if (strcmp(res->variable, "signal-name") == 0) {
				event->sigName = strdup(str);
			} else if (strcmp(res->variable, "signal-meaning") == 0) {
				event->sigMeaning = strdup(str);
			} else if (strcmp(res->variable, "gdb-result-var") == 0) {
				event->gdbResult = strdup(str);
			} else if (strcmp(res->variable, "return-value") == 0) {
				event->returnValue = strdup(str);
			} else if (strcmp(res->variable, "return-type") == 0) {
				event->returnType = strdup(str);
			} else if (strcmp(res->variable, "exit-code") == 0) {
				event->code = (int)strtol(str, NULL, 10);
			} else if (strcmp(res->variable, "thread-id") == 0) {
				event->threadId = (int)strtol(str, NULL, 10);
			} else if (strcmp(res->variable, "frame") == 0) {
				if (value->type == MIValueTypeTuple) {
					event->frame = MIFrameParse(value);
				}
			}
		}
	}
}

static void
MIEventParseWPT(MIEvent *event, List *results)
{
	char *		str = "";
	MIResult *	res;
	MIValue *	value;
	
	for (SetList(results); (res = (MIResult *)GetListElement(results)) != NULL; ) {
		value = res->value;
		if (value->type == MIValueTypeConst) {
			str = value->cstring;
		}
		if (strcmp(res->variable, "number") == 0) {
			event->number = (int)strtol(str, NULL, 10);
		} else if (strcmp(res->variable, "exp") == 0) { 
			event->exp = strdup(value->cstring);
		}
	}
}

static void
MIEventParseValue(MIEvent *event, List *results)
{
	char *		str = "";
	MIResult *	res;
	MIValue *	value;
	
	for (SetList(results); (res = (MIResult *)GetListElement(results)) != NULL; ) {
		value = res->value;
		if (value->type == MIValueTypeConst) {
			str = value->cstring;
		}
		if (strcmp(res->variable, "old") == 0) {
			event->oldValue = strdup(str);
		} else if (strcmp(res->variable, "new") == 0) { 
			event->newValue = strdup(str);
		} else if (strcmp(res->variable, "value") == 0) { 
			event->oldValue = strdup(str);
			event->newValue = strdup(str);
		}
	}
}
