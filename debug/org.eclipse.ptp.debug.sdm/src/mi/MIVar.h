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

#ifndef _MIVAR_H_
#define _MIVAR_H_

#include "list.h"
#include "MIString.h"
#include "MIResultRecord.h"
#include "MICommand.h"

/**
 * Represents a set variable object.
 */
struct MIVar {
	char *			name;
	char *			type;
	char *			exp;
	int				numchild;
	struct MIVar **	children;
};
typedef struct MIVar	MIVar;

/**
 * Represents a set variable object.
 */
struct MIVarChange {
	char *	name;
	int		in_scope;
	int		type_changed;
};
typedef struct MIVarChange	MIVarChange;

extern MIVar *MIVarNew(void);
extern void MIVarFree(MIVar *var);

extern MIVarChange *MIVarChangeNew(void);
extern void MIVarChangeFree(MIVarChange *var);

extern MIVar *MIVarParse(List *results);
extern MIVar *MIGetVarCreateInfo(MICommand *cmd);
extern void MIGetVarListChildrenInfo(MIVar *var, MICommand *cmd);
extern char *MIGetVarEvaluateExpressionInfo(MICommand *cmd);
extern char *MIGetDataEvaluateExpressionInfo(MICommand *cmd);

extern void MIGetVarUpdateInfo(MICommand *cmd, List **varchanges);
extern MIVar *MIGetVarInfoType(MICommand *cmd);
extern void MIGetVarInfoNumChildren(MICommand *cmd, MIVar *var);
#endif /* _MIVAR_H_ */


