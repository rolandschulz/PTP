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

#include "MICommand.h"
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
extern MIList *MIBreakpointGetBreakInsertInfo(MICommand *cmd);
#endif /* _MIBREAKPOINT_H_ */

