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

#ifdef __gnu_linux__
#define _GNU_SOURCE
#endif /* __gnu_linux__ */

#include "config.h"

#include <stdlib.h>
#include <string.h>
#include <ctype.h>

#include "gdb.h"
#include "dbg.h"
#include "dbg_error.h"

static int		_address_length = 0;
static double	_gdb_version = 0.0;

/*
 * Send command and wait for a response.
 */
void
SendCommandWait(MISession *session, MICommand *cmd)
{
	MISessionSendCommand(session, cmd);
	do {
		MISessionProgress(session);
		if (session->out_fd == -1) {
			DEBUG_PRINTS(DEBUG_LEVEL_BACKEND, "------------------- SendCommandWait sess->out_fd = -1\n");
			break;
		}
	} while (!MISessionCommandCompleted(session));
}

/*
 * If this is a refence type, then remove the trailing '&'. This
 * is required because 'ptype' is not able to deal with '&' in
 * an expression.
 */
static void
FixReferenceType(char *type)
{
	char *	t = &type[strlen(type)-1];

	while (t >= type) {
		if (isspace(*t)) {
			*t-- = '\0';
			continue;
		} else if (*t == '&') {
			*t = '\0';
		}

		break;
	}
}

/*
 * Create a new MI variable and corresponding MIVar object using
 * the supplied expression 'expr'. Sets the 'exp' field to the
 * expression if it is not already set.
 * Returns NULL if the variable can't be created.
 */
MIVar *
CreateMIVar(MISession *session, char *expr)
{
	MICommand *cmd;
	MIVar *mivar;

	cmd = MIVarCreate("-", "*", expr);
	SendCommandWait(session, cmd);
	if (!MICommandResultOK(cmd)) {
		//DbgSetError(DBGERR_UNKNOWN_VARIABLE, GetLastErrorStr());
		MICommandFree(cmd);
		return NULL;
	}
	mivar = MIGetVarCreateInfo(cmd);
	if (mivar->exp == NULL) {
		mivar->exp = strdup(expr);
	}
	FixReferenceType(mivar->type);
	MICommandFree(cmd);
	return mivar;
}

void
DeleteMIVar(MISession *session, char *mi_name)
{
	MICommand *cmd;
	cmd = MIVarDelete(mi_name);
	SendCommandWait(session, cmd);
	MICommandFree(cmd);
}

stackframe *
GetCurrentFrame(MISession *session)
{
	stackframe *f;
	List *	frames;

	if (GetStackframes(session, 1, 0, 0, &frames) != DBGRES_OK)
		return NULL;

	if (EmptyList(frames)) {
		DbgSetError(DBGERR_DEBUGGER, "Could not get current stack frame");
		return NULL;
	}

	SetList(frames);
	f = (stackframe *)GetListElement(frames);
	DestroyList(frames, NULL);
	return f;
}

stackframe *
ConvertMIFrame(MIFrame *f)
{
	stackframe *	s = NewStackframe(f->level);

	if ( f->addr != NULL )
		s->loc.addr = strdup(f->addr);
	if ( f->func != NULL )
		s->loc.func = strdup(f->func);
	if ( f->file != NULL )
		s->loc.file = strdup(f->file);
	s->loc.line = f->line;

	return s;
}

breakpoint *
ConvertMIBreakpoint(MIBreakpoint *bpt)
{
	breakpoint *	bp = NewBreakpoint(bpt->number);

	bp->ignore = bpt->ignore;
	bp->type = strdup(bpt->type);
	bp->hits = bpt->times;

	if ( bpt->file != NULL )
		bp->loc.file = strdup(bpt->file);
	if ( bpt->func != NULL )
		bp->loc.func = strdup(bpt->func);
	if ( bpt->address != NULL )
		bp->loc.addr = strdup(bpt->address);
	bp->loc.line = bpt->line;

	return bp;
}

List *
GetChangedVariables(MISession *session)
{
	MICommand *cmd;
	MIList *changes;
	List *changedVars;
	MIVarChange *var;

	cmd = MIVarUpdate("*");
	SendCommandWait(session, cmd);
	if (!MICommandResultOK(cmd)) {
		DEBUG_PRINTS(DEBUG_LEVEL_BACKEND, "------------------- GetChangedVariables error\n");
		SetDebugError(cmd);
		MICommandFree(cmd);
		return NULL;
	}
	MIGetVarUpdateInfo(cmd, &changes);
	MICommandFree(cmd);

	changedVars = NewList();
	for (MIListSet(changes); (var = (MIVarChange *)MIListGet(changes)) != NULL;) {
		if (var->in_scope == 1) {
			AddToList(changedVars, (void *)strdup(var->name));
		}
		else {
			DeleteMIVar(session, var->name);
		}
	}
	MIListFree(changes, MIVarChangeFree);
	return changedVars;
}

int
GetMIInfoDepth(MISession *session)
{
	MICommand *cmd;
	int depth;

	cmd = MIStackInfoDepth();
	SendCommandWait(session, cmd);
	if (!MICommandResultOK(cmd)) {
		DEBUG_PRINTS(DEBUG_LEVEL_BACKEND, "------------------- GDBMIStackInfoDepth error\n");
		SetDebugError(cmd);
		MICommandFree(cmd);
		return -1;
	}
	depth = MIGetStackInfoDepth(cmd);
	MICommandFree(cmd);
	return depth;
}

int
GetStackframes(MISession *session, int current, int low, int high, List **flist)
{
	MIList *		frames;
	MIFrame *		f;
	MICommand *		cmd;

	if (current) {
		if (_gdb_version > 6.3) {
			cmd = MIStackInfoFrame();
		} else {
			cmd = CLIFrame();
		}
	} else {
		if (low == 0 && high == 0) {
			cmd = MIStackListAllFrames();
		} else {
			cmd = MIStackListFrames(low, high);
		}
	}
	SendCommandWait(session, cmd);

	if (!MICommandResultOK(cmd)) {
		DEBUG_PRINTS(DEBUG_LEVEL_BACKEND, "------------------- GetStackframes error\n");
		SetDebugError(cmd);
		MICommandFree(cmd);
		return DBGRES_ERR;
	}

	if (current)
		frames = MIGetFrameInfo(cmd);
	else
		frames = MIGetStackListFramesInfo(cmd);

	MICommandFree(cmd);

	if ( frames == NULL )
	{
		DbgSetError(DBGERR_DEBUGGER, "Failed to get stack frames from backend");
		return DBGRES_ERR;
	}

	*flist = NewList();
	for (MIListSet(frames); (f = (MIFrame *)MIListGet(frames)) != NULL; ) {
		AddToList(*flist, (void *)ConvertMIFrame(f));
	}
	MIListFree(frames, MIFrameFree);
	return DBGRES_OK;
}

/*
 * This is needed to check for a bug in the Linux x86 GCC 4.1 compiler
 * that causes gdb 6.4 and 6.5 to crash under certain conditions.
 */
#define GDB_BUG_2188	__gnu_linux__ && __i386__ && __GNUC__ == 4 && __GNUC_MINOR__ == 1

#if GDB_BUG_2188
int
CurrentFrame(MISession *session, int level, char *name)
{
	MICommand *	cmd;
	MIFrame *	frame;
	List *		frames;
	int val = 0;

	if (_gdb_version > 6.3 && _gdb_version < 6.7) {
		cmd = MIStackListFrames(level, level);
		SendCommandWait(session, cmd);
		if (!MICommandResultOK(cmd)) {
			MICommandFree(cmd);
			return val;
		}
		frames = MIGetStackListFramesInfo(cmd);

		//only one frame
		SetList(frames);
		if ((frame = (MIFrame *)GetListElement(frames)) != NULL) {
			if (frame->func != NULL && strncmp(frame->func, name, 4) == 0) {
				val = 1;
			}
		}
		DestroyList(frames, MIFrameFree);
	}

	return val;
}
#endif /* GDB_BUG_2188 */

char *
GetVarValue(MISession *session, char *var)
{
	char *		res;
	MICommand *	cmd = MIVarEvaluateExpression(var);

	SendCommandWait(session, cmd);
	if (!MICommandResultOK(cmd)) {
		SetDebugError(cmd);
		MICommandFree(cmd);
		return strdup("");
	}
	res = MIGetVarEvaluateExpressionInfo(cmd);
	MICommandFree(cmd);
	return res;
}

int
GetAddressLength(MISession *session)
{
	char * 		res;
	MICommand * cmd;

	if (_address_length != 0) {
		return _address_length;
	}

	cmd = MIDataEvaluateExpression("\"sizeof(char *)\"");
	SendCommandWait(session, cmd);
	if (!MICommandResultOK(cmd)) {
		SetDebugError(cmd);
		MICommandFree(cmd);
		return 0;
	}
	res = MIGetDataEvaluateExpressionInfo(cmd);
	MICommandFree(cmd);

	_address_length = (int)strtol(res, NULL, 10);

	free(res);

	return _address_length;
}

/*
 * Try to find the type of a variable using the 'ptype'
 * command. First try using the type field, or if this
 * fails, try using the expression 'expr'.
 */
char *
GetPtypeValue(MISession *session, char *expr, MIVar *var)
{
	char *		type = NULL;
	MICommand * cmd;

	DEBUG_PRINTF(DEBUG_LEVEL_BACKEND, "---------------------- GetPtypeValue(%s, %s)\n", expr != NULL ? expr : "NULL", var->type);
	cmd = CLIPType(var->type);
	MICommandSetTimeout(cmd, 1000);
	SendCommandWait(session, cmd);
	if (!MICommandResultOK(cmd)) {
		if (expr == NULL) {
			DEBUG_PRINTS(DEBUG_LEVEL_BACKEND, "---------------------- GetPtypeValue failed and expr was NULL\n");
			return NULL;
		}
		cmd = CLIPType(expr);
		MICommandSetTimeout(cmd, 100);
		SendCommandWait(session, cmd);
		if (!MICommandResultOK(cmd)) {
			DEBUG_PRINTS(DEBUG_LEVEL_BACKEND, "---------------------- GetPtypeValue failed\n");
			return NULL;
		}
	}
	type = CLIGetPTypeInfo(cmd);
	MICommandFree(cmd);
	DEBUG_PRINTF(DEBUG_LEVEL_BACKEND, "---------------------- GetPtypeValue returns \"%s\"\n", type);
	return type;
}

/*
 * Create a variable containing the fields of a class
 */
MIVar *
GetMIVarClassFields(MISession *session, char *name)
{
	MICommand *	cmd;
	MIVar *		v = NULL;

	v = MIVarNew();
	cmd = MIVarListChildren(name);

	SendCommandWait(session, cmd);
	if (!MICommandResultOK(cmd)) {
		MICommandFree(cmd);
		MIVarFree(v);
		return NULL;
	}

	MIGetVarListChildrenInfo(cmd, v);
	MICommandFree(cmd);

	v->name = strdup(name);
	return v;
}

/*
 * Get MI variable details. If 'mivar' is NULL then an MIVar object is created
 * with the name 'name', otherwise the details are added to the existing variable.
 *
 * If 'listChildren' is true, then the variable's children will be
 * queried also. If 'listChildren' is false, only 'numchildren' will be supplied.
 *
 * Returns a new MIVar object or NULL if any commands fail.
 */
MIVar *
GetMIVarDetails(MISession *session, char *name, MIVar *mivar, int listChildren)
{
	MICommand *	cmd;
	MIVar *		v = NULL;

	if (mivar == NULL || mivar->type == NULL) {
		cmd = MIVarInfoType(name);
		SendCommandWait(session, cmd);
		if (!MICommandResultOK(cmd)) {
			MICommandFree(cmd);
			return NULL;
		}

		v = MIGetVarInfoType(cmd);
		MICommandFree(cmd);

		if (mivar == NULL) {
			mivar = v;
			if (mivar->name == NULL) {
				mivar->name = strdup(name);
			}
		} else {
			mivar->type = strdup(v->type);
			MIVarFree(v);
		}
		FixReferenceType(mivar->type);
	}

	/*
	 * Only fetch child info if we haven't already done it.
	 */
	if (mivar->children == NULL || mivar->numchild == 0) {
		if (listChildren) {
			cmd = MIVarListChildren(name);
		} else {
			cmd = MIVarInfoNumChildren(name);
		}

		SendCommandWait(session, cmd);
		if (!MICommandResultOK(cmd)) {
			MICommandFree(cmd);
			if (mivar == v) {
				MIVarFree(v);
			}
			return NULL;
		}

		if (listChildren) {
			MIGetVarListChildrenInfo(cmd, mivar);
		} else {
			MIGetVarInfoNumChildren(cmd, mivar);
		}

		MICommandFree(cmd);
	}
	return mivar;
}

float
GetGDBVersion(MISession *session)
{
	MICommand *	cmd = MIGDBVersion();
	SendCommandWait(session, cmd);
	if (MICommandResultOK(cmd)) {
		_gdb_version = CLIGetGDBVersion(cmd);
		DEBUG_PRINTF(DEBUG_LEVEL_BACKEND, "------------------- gdb version: %f\n", _gdb_version);
	}
	MICommandFree(cmd);
	return _gdb_version;
}

void
SetDebugError(MICommand * cmd)
{
	if (MICommandResultClass(cmd) == MIResultRecordERROR) {
		char *err = MICommandResultErrorMessage(cmd);
		if (err != NULL) {
			DbgSetError(DBGERR_DEBUGGER, err);
			free(err);
		} else {
			DbgSetError(DBGERR_DEBUGGER, "got error from gdb, but no message");
		}
	} else {
		DbgSetError(DBGERR_DEBUGGER, "bad response from gdb");
	}
}
