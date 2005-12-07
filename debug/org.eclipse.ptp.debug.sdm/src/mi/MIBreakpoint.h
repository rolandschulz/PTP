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
 
/**
 * Contain info about the GDB/MI breakpoint info.
 *<ul>
 * <li>
 * -break-insert main
 * ^done,bkpt={number="1",type="breakpoint",disp="keep",enabled="y",addr="0x0804846b",func="main",file="hello.c",line="4",times="0"}
 * (gdb)
 * </li>
 * <li>
 * -break-insert -t main
 * ^done,bkpt={number="2",type="breakpoint",disp="del",enabled="y",addr="0x0804846b",func="main",file="hello.c",line="4",times="0"}
 * </li>
 * <li>
 * -break-insert -c 1 main
^done,bkpt={number="3",type="breakpoint",disp="keep",enabled="y",addr="0x0804846b",func="main",file="hello.c",line="4",cond="1",times="0"}
 * </li>
 * <li>
 * -break-insert -h main
 * ^done,bkpt={number="4",type="hw breakpoint",disp="keep",enabled="y",addr="0x0804846b",func="main",file="hello.c",line="4",times="0"}
 * <li>
 * -break-insert -p 0 main
 * ^done,bkpt={number="5",type="breakpoint",disp="keep",enabled="y",addr="0x0804846b",func="main",file="hello.c",line="4",thread="0",thread="0",times="0"}
 * </li>
 * <li>
 * -break-insert -a p
 * ^done,hw-awpt={number="2",exp="p"}
 * </li>
 * <li>
 * -break-watch -r p
 * ^done,hw-rwpt={number="4",exp="p"}
 * </li>
 * <li>
 * -break-watch p
 * ^done,wpt={number="6",exp="p"}
 * </li>
 *</ul>
 */
#ifndef _MIBREAKPOINT_H_
#define _MIBREAKPOINT_H_

#include "MISession.h"
#include "MIValue.h"

struct MIBreakpoint {
	int		number;
	char *	type;
	char *	disp;
	int		enabled;
	char *	address;
	char *	func;
	char *	file;
	int		line;
	char *	cond;
	int		times;
	char *	what;
	char *	threadId;
	int		ignore;

	int		isWpt;
	int		isAWpt;
	int		isRWpt;
	int		isWWpt;
	int		isHdw;
};
typedef struct MIBreakpoint	MIBreakpoint;

extern MIBreakpoint *MIBreakpointNew(void);
extern void MIBreakpointFree(MIBreakpoint *bp);
extern MIBreakpoint *MIBreakpointParse(MIValue *tuple);
extern int MIBreakInsert(MISession *sess, int isTemporary, int isHardware, char *condition, int ignoreCount, char *line, int tid);
#endif _MIBREAKPOINT_H_

