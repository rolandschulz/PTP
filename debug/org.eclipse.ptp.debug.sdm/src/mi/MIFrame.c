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
#include "MIValue.h"
#include "MIResult.h"
#include "MIArg.h"
#include "MIFrame.h"

MIFrame *
MIFrameNew(void)
{
	MIFrame *	frame;
	
	frame = (MIFrame *)malloc(sizeof(MIFrame));
	frame->addr = NULL;
	frame->func = NULL;
	frame->file = NULL;
	frame->args = NULL;
	return frame;	
}

void
MIFrameFree(MIFrame *frame)
{
	if (	frame->addr != NULL)
		free(frame->addr);
	if (	frame->func != NULL)
		free(frame->func);
	if (	frame->file != NULL)
		free(frame->file);
	if (	frame->args != NULL)
		DestroyList(frame->args, MIArgFree);
	free(frame);
}

MIFrame *
MIFrameParse(MIValue *tuple)
{
	char *		str;
	char *		var;
	MIValue *	value;
	MIResult *	result;
	List *		results = tuple->results;
	MIFrame *	frame = MIFrameNew();
	
	for (SetList(results); (result = (MIResult *)GetListElement(results)) != NULL; ) {
		var = result->variable;
		value = result->value;
		
		if (value == NULL || value->type != MIValueTypeConst)
			continue;

		str = value->cstring;

		if (strcmp(var, "level") == 0) { //$NON-NLS-1$
			frame->level = atoi(str);
		} else if (strcmp(var, "addr") == 0) { //$NON-NLS-1$
			frame->addr = strdup(str);
		} else if (strcmp(var, "func") == 0) { //$NON-NLS-1$
			frame->func = NULL;
			if ( str != NULL ) {
				if (strcmp(str, "??") == 0) //$NON-NLS-1$
					frame->func = strdup(""); //$NON-NLS-1$
				else
				{
					// In some situations gdb returns the function names that include parameter types.
					// To make the presentation consistent truncate the parameters. PR 46592
					char * s = strchr(str, '(');
					if (s != NULL)
						*s = '\0';
					frame->func = strdup(str);
				}
			}
		} else if (strcmp(var, "file") == 0) { //$NON-NLS-1$
			frame->file = strdup(str);
		} else if (strcmp(var, "line") == 0) { //$NON-NLS-1$
			frame->line = atoi(str);
		} else if (strcmp(var, "args") == 0) { //$NON-NLS-1$
			frame->args = MIArgsParse(value);
		}
	}
	
	return frame;
}

MIString *
MIFrameToString(MIFrame *f)
{
	int			first = 1;
	MIArg *		arg;
	MIString *	str = MIStringNew("level=\"%d\"", f->level);
	
	MIStringAppend(str, MIStringNew(",addr=\"%s\"", f->addr));
	MIStringAppend(str, MIStringNew(",func=\"%s\"", f->func));
	MIStringAppend(str, MIStringNew(",file=\"%s\"", f->file));
	MIStringAppend(str, MIStringNew(",line=\"%d\"", f->line));
	MIStringAppend(str, MIStringNew(",args=["));
	
	for (SetList(f->args); (arg = (MIArg *)GetListElement(f->args)) != NULL; ) {
		if (!first) {
			MIStringAppend(str, MIStringNew(","));
		}
		first = 0;
		MIStringAppend(str, MIStringNew("{name=\"%s\"", arg->name));
		MIStringAppend(str, MIStringNew(",value=\"%s\"", arg->value));
	}
	MIStringAppend(str, MIStringNew("]"));
		
	return str;
}
