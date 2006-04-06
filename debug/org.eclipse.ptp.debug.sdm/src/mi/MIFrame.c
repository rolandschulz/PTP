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
#include <ctype.h>

#include "list.h"
#include "MIValue.h"
#include "MIResult.h"
#include "MIArg.h"
#include "MIFrame.h"
#include "MIOOBRecord.h" //clement added

static List *MIFrameInfoParse(List *results);

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
	
		if (value != NULL || value->type == MIValueTypeConst) {
			str = value->cstring;
		}
	
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

List *
MIGetStackListFramesInfo(MICommand *cmd)
{
	MIValue *		val;
	MIResultRecord *	rr;
	MIResult *		result;
	List *			frames = NULL;
	MIValue * frameVal;// clement added - forgot
	
	if (!cmd->completed || cmd->output == NULL || cmd->output->rr == NULL)
		return NULL;
		
	rr = cmd->output->rr;
	for (SetList(rr->results); (result = (MIResult *)GetListElement(rr->results)) != NULL; ) {
		if (strcmp(result->variable, "stack") == 0) {
			val = result->value;
			if (val->type == MIValueTypeList || val->type == MIValueTypeTuple) {//TODO correct???
				for (SetList(val->results); (result = (MIResult *)GetListElement(val->results)) != NULL; ) {
					if (strcmp(result->variable, "frame") == 0) {
						//val = result->value; /* error: overwrite prior MIValue
						frameVal = result->value;
						if (frameVal->type == MIValueTypeTuple) {
							if (frames == NULL)
								frames = NewList();
							AddToList(frames, MIFrameParse(frameVal));
						}
					}
				}
			}
		}
	}
	return frames;
}

static List *
MIFrameInfoParse(List *results)
{
	MIValue *	val;
	MIResult *	result;
	List *		frames = NULL;

	//SetList(results); 
	//if ((result = (MIResult *)GetListElement(results)) != NULL) {
	for (SetList(results); (result = (MIResult *)GetListElement(results)) != NULL;) {
		if (strcmp(result->variable, "frame") == 0) {
			val = result->value;
			if (val->type == MIValueTypeTuple) {
				if (frames == NULL)
					frames = NewList();
				AddToList(frames, (void *)MIFrameParse(val));
			}
		}
	}
	return frames;
}

List *
MIGetFrameInfo(MICommand *cmd)
{
	MIResultRecord *	rr;
	
	if (!cmd->completed || cmd->output == NULL || cmd->output->rr == NULL)
		return NULL;
		
	rr = cmd->output->rr;
	return MIFrameInfoParse(rr->results);
}

List *
MIGetStackListLocalsInfo(MICommand *cmd)
{
	MIValue *		val;
	MIResultRecord *	rr;
	MIResult *		result;
	List *			locals = NULL;

	if (!cmd->completed || cmd->output == NULL || cmd->output->rr == NULL)
		return NULL;
	
	rr = cmd->output->rr;
	for (SetList(rr->results); (result = (MIResult *)GetListElement(rr->results)) != NULL; ) {
		if (strcmp(result->variable, "locals") == 0) {
			val = result->value;
			if (val->type == MIValueTypeList || val->type == MIValueTypeTuple) {
				locals = MIArgsParse(val);
			}
		}
	}
	return locals;
}

List *
MIGetStackListArgumentsInfo(MICommand *cmd)
{
	MIValue *		val;
	MIResultRecord *	rr;
	MIResult *		result;
	List *			frames = NULL;

	if (!cmd->completed || cmd->output == NULL || cmd->output->rr == NULL)
		return NULL;
	
	rr = cmd->output->rr;
	SetList(rr->results); 
	if ((result = (MIResult *)GetListElement(rr->results)) != NULL) {
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


//clement added
MIThreadInfo *MIThreadInfoNew(void) {
	MIThreadInfo * info;
	info = (MIThreadInfo *)malloc(sizeof(MIThreadInfo));
	info->current_thread_id = 0;
	info->thread_ids = NULL;
	return info;
}

//clement added
MIThreadSelectInfo *MIThreadSelectInfoNew(void) {
	MIThreadSelectInfo * info;
	info = (MIThreadSelectInfo *)malloc(sizeof(MIThreadSelectInfo));
	info->current_thread_id = 0;
	info->frame = NULL;
	return info;
}

//clement added
MIThreadInfo *MIGetInfoThreads(MICommand *cmd) {
	List *oobs;
	MIOOBRecord *oob;
	MIThreadInfo * info = MIThreadInfoNew();
	char * text = NULL;
	char * id = NULL;

	if (!cmd->completed || cmd->output == NULL || cmd->output->oobs == NULL) {
		return info;
	}

	if (cmd->output->rr != NULL && cmd->output->rr->resultClass == MIResultRecordERROR) {
		return info;
	}

	oobs = cmd->output->oobs;
	info->thread_ids = NewList();
	for (SetList(oobs); (oob = (MIOOBRecord *)GetListElement(oobs)) != NULL; ) {
		text = oob->cstring;
		if (*text == '\0') {
			continue;
		}
		while (*text == ' ') {
			*text++;
		}
		if (strncmp(text, "*", 1) == 0) {
			text += 2;//escape "* ";
			if (isdigit(*text)) {
				info->current_thread_id = strtol(text, &text, 10);
			}
			continue;
		}
		if (isdigit(*text)) {
			if (info->thread_ids == NULL)
				info->thread_ids = NewList();

			id = strchr(text, ' ');
			if (id != NULL) {
				*id = '\0';
				AddToList(info->thread_ids, (void *)strdup(text));
			}
		}
	}
	return info;
}
//clement added
MIThreadSelectInfo *MISetThreadSelectInfo(MICommand *cmd) {
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
			info->current_thread_id = atoi(val->cstring);
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
//clement added
int MIGetStackInfoDepth(MICommand *cmd) {
	MIValue * val;
	MIResultRecord * rr;
	MIResult * result;

	if (!cmd->completed || cmd->output == NULL || cmd->output->rr == NULL)
		return -1;
		
	rr = cmd->output->rr;
	SetList(rr->results); 
	if ((result = (MIResult *)GetListElement(rr->results)) != NULL) {
		if (strcmp(result->variable, "depth") == 0) {
			val = result->value;
			return atoi(val->cstring);
		}
	}
	return -1;
}
