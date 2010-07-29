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

#ifndef _GDB_AIF_H_
#define _GDB_AIF_H_

#include <aif.h>
#include <MI.h>
#include <list.h>

#include "stackframe.h"
#include "breakpoint.h"

#define GDB_BUG_2188	__gnu_linux__ && __i386__ && __GNUC__ == 4 && __GNUC_MINOR__ == 1

/*
 * gdb_aif.c
 */
extern int		GetAIFVar(MISession *session, char *var, AIF **val, char **type);
extern AIF *	GetAIFPointer(MISession *session, char *addr, AIF *i);
extern AIF *	GetCharPointerAIF(MISession *session, char *res);
extern AIF *	GetSimpleAIF(MISession *session, MIVar *var);
extern AIF *	GetNamedAIF(AIF *a, int named);
extern AIF *	GetStructAIF(MISession *session, MIVar *var, int named);
extern AIF *	GetClassAIF(MISession *session, MIVar *var, int named);
extern AIF *	GetUnionAIF(MISession *session, MIVar *var, int named);
extern AIF *	GetArrayAIF(MISession *session, MIVar *var, int named);
extern AIF *	GetPointerAIF(MISession *session, MIVar *var, int named);
extern AIF *	GetComplexAIF(MISession *session, MIVar *var, int named);
extern AIF *	GetAIF(MISession *session, MIVar *var, int named);
extern AIF *	GetPartialArrayAIF(MISession *session, char *expr, MIVar *var);
extern AIF *	GetPartialAggregateAIF(MISession *session, char *expr, MIVar *var);
extern AIF *	GetPartialUnionAIF(MISession *session, char *expr, MIVar *var);
extern AIF *	GetPartialPointerAIF(MISession *session, char *expr, MIVar *var);
extern AIF *	GetPartialComplexAIF(MISession *session, char *expr, MIVar *var);
extern AIF *	GetPartialAIF(MISession *session, char *expr, MIVar *var);

/*
 * gdb_mi.c
 */
extern stackframe *	ConvertMIFrame(MIFrame *f);
extern breakpoint *	ConvertMIBreakpoint(MIBreakpoint *bp);
extern MIVar *		CreateMIVar(MISession *session, char *expr);
extern void			DeleteMIVar(MISession *session, char *mi_name);
extern int			GetAddressLength(MISession *session);
extern List *		GetChangedVariables(MISession *session);
extern stackframe *	GetCurrentFrame(MISession *session);
extern float		GetGDBVersion(MISession *session);
extern int			GetMIInfoDepth(MISession *session);
extern MIVar *		GetMIVarClassFields(MISession *session, char *name);
extern MIVar *		GetMIVarDetails(MISession *session, char *name, MIVar *mivar, int listChildren);
extern char *		GetPtypeValue(MISession *session, char *expr, MIVar *var);
extern int			GetStackframes(MISession *session, int current, int low, int high, List **flist);
extern char *		GetVarValue(MISession *session, char *var);
extern void			SendCommandWait(MISession *session, MICommand *cmd);
extern void			SetDebugError(MICommand * cmd);
#if GDB_BUG_2188
extern int			CurrentFrame(MISession *session, int level, char *name);
#endif /* GDB_BUG_2188 */

/*
 * gdb_bpmap.c
 */
extern void		AddBPMap(int local, int remote, int temp);
extern void		ClearBPMaps(void);
extern int		IsTempBP(int id);
extern int		GetLocalBPID(int id);
extern int		GetRemoteBPID(int id);
extern void		RemoveBPMap(int id);

#endif /* _GDB_AIF_H_ */
