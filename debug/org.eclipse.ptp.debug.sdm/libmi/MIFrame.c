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

#include "MIList.h"
#include "MIValue.h"
#include "MIResult.h"
#include "MIArg.h"
#include "MIFrame.h"
#include "MIOOBRecord.h"

static MIList *MIFrameInfoParse(MIList *results);

MIFrame *
MIFrameNew(void)
{
	MIFrame *	frame;

	frame = (MIFrame *)malloc(sizeof(MIFrame));
	frame->level = 0;
	frame->line = 0;
	frame->addr = NULL;
	frame->func = NULL;
	frame->file = NULL;
	frame->args = NULL;
	return frame;
}

void
MIFrameFree(MIFrame *frame)
{
	if (frame->addr != NULL)
		free(frame->addr);
	if (frame->func != NULL)
		free(frame->func);
	if (frame->file != NULL)
		free(frame->file);
	if (frame->args != NULL)
		MIListFree(frame->args, MIArgFree);
	free(frame);
}

MIFrame *
MIFrameParse(MIValue *tuple)
{
	char *		str = NULL;
	char *		var;
	MIValue *	value;
	MIResult *	result;
	MIList *	results = tuple->results;
	MIFrame *	frame = MIFrameNew();

	for (MIListSet(results); (result = (MIResult *)MIListGet(results)) != NULL; ) {
		var = result->variable;
		value = result->value;

		if (value != NULL || value->type == MIValueTypeConst) {
			str = value->cstring;
		}

		if (strcmp(var, "level") == 0 && str != NULL) { //$NON-NLS-1$
			frame->level = (int)strtol(str, NULL, 10);
		} else if (strcmp(var, "addr") == 0 && str != NULL) { //$NON-NLS-1$
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
		} else if (strcmp(var, "file") == 0 && str != NULL) { //$NON-NLS-1$
			frame->file = strdup(str);
		} else if (strcmp(var, "line") == 0 && str != NULL) { //$NON-NLS-1$
			frame->line = (int)strtol(str, NULL, 10);
		} else if (strcmp(var, "args") == 0) { //$NON-NLS-1$
			frame->args = MIArgsParse(value);
		}
	}

	return frame;
}

MIList *
MIGetStackListFramesInfo(MICommand *cmd)
{
	MIValue *			val;
	MIResultRecord *	rr;
	MIResult *			result;
	MIList *			frames = NULL;
	MIValue * 			frameVal;

	if (!cmd->completed || cmd->output == NULL || cmd->output->rr == NULL)
		return NULL;

	rr = cmd->output->rr;
	for (MIListSet(rr->results); (result = (MIResult *)MIListGet(rr->results)) != NULL; ) {
		if (strcmp(result->variable, "stack") == 0) {
			val = result->value;
			if (val->type == MIValueTypeList || val->type == MIValueTypeTuple) {//TODO correct???
				for (MIListSet(val->results); (result = (MIResult *)MIListGet(val->results)) != NULL; ) {
					if (strcmp(result->variable, "frame") == 0) {
						//val = result->value; /* error: overwrite prior MIValue
						frameVal = result->value;
						if (frameVal->type == MIValueTypeTuple) {
							if (frames == NULL)
								frames = MIListNew();
							MIListAdd(frames, MIFrameParse(frameVal));
						}
					}
				}
			}
		}
	}
	if (frames == NULL)
		frames = MIListNew();
	return frames;
}

static MIList *
MIFrameInfoParse(MIList *results)
{
	MIValue *	val;
	MIResult *	result;
	MIList *	frames = NULL;

	for (MIListSet(results); (result = (MIResult *)MIListGet(results)) != NULL;) {
		if (strcmp(result->variable, "frame") == 0) {
			val = result->value;
			if (val->type == MIValueTypeTuple) {
				if (frames == NULL)
					frames = MIListNew();
				MIListAdd(frames, (void *)MIFrameParse(val));
			}
		}
	}
	if (frames == NULL)
		frames = MIListNew();
	return frames;
}

MIList *
MIGetFrameInfo(MICommand *cmd)
{
	MIResultRecord *	rr;

	if (!cmd->completed || cmd->output == NULL || cmd->output->rr == NULL)
		return NULL;

	rr = cmd->output->rr;
	return MIFrameInfoParse(rr->results);
}

MIList *
MIGetStackListLocalsInfo(MICommand *cmd)
{
	MIValue *			val;
	MIResultRecord *	rr;
	MIResult *			result;
	MIList *			locals = NULL;

	if (!cmd->completed || cmd->output == NULL || cmd->output->rr == NULL)
		return NULL;

	rr = cmd->output->rr;
	for (MIListSet(rr->results); (result = (MIResult *)MIListGet(rr->results)) != NULL; ) {
		if (strcmp(result->variable, "locals") == 0) {
			val = result->value;
			if (val->type == MIValueTypeList || val->type == MIValueTypeTuple) {
				locals = MIArgsParse(val);
			}
		}
	}
	return locals;
}

MIList *
MIGetStackListArgumentsInfo(MICommand *cmd)
{
	MIValue *			val;
	MIResultRecord *	rr;
	MIResult *			result;
	MIList *			frames = NULL;

	if (!cmd->completed || cmd->output == NULL || cmd->output->rr == NULL)
		return NULL;

	rr = cmd->output->rr;
	MIListSet(rr->results);
	if ((result = (MIResult *)MIListGet(rr->results)) != NULL) {
		if (strcmp(result->variable, "stack-args") == 0) {
			val = result->value;
			if (val->type == MIValueTypeList || val->type == MIValueTypeTuple) {
				frames = MIFrameInfoParse(val->results);
			}
		}
	}
	return frames;
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

	for (MIListSet(f->args); (arg = (MIArg *)MIListGet(f->args)) != NULL; ) {
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

int
MIGetStackInfoDepth(MICommand *cmd)
{
	MIValue * val;
	MIResultRecord * rr;
	MIResult * result;

	if (!cmd->completed || cmd->output == NULL || cmd->output->rr == NULL)
		return -1;

	rr = cmd->output->rr;
	MIListSet(rr->results);
	if ((result = (MIResult *)MIListGet(rr->results)) != NULL) {
		if (strcmp(result->variable, "depth") == 0) {
			val = result->value;
			return (int)strtol(val->cstring, NULL, 10);
		}
	}
	return -1;
}
