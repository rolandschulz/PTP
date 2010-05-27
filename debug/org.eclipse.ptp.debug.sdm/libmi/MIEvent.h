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

#ifndef _MIEVENT_H_
#define _MIEVENT_H_

#include "MIList.h"
#include "MIFrame.h"

#define MIEventClassStopped			1
#define MIEventClassRunning			2

#define MIEventTypeBreakpointHit		1
#define MIEventTypeWatchpointTrigger	2
#define MIEventTypeWatchpointScope		3
#define MIEventTypeSteppingRange		4
#define MIEventTypeSignal				5
#define MIEventTypeLocationReached		6
#define MIEventTypeFunctionFinished		7
#define MIEventTypeInferiorExit			8
#define MIEventTypeInferiorSignalExit	9
#define MIEventTypeSuspended			10

/**
 * Represents an asynchronous event.
 */
struct MIEvent {
	int			class;
	int			type;
	int 			threadId;
	MIFrame *	frame;
	int			bkptno;
	int			number;
	char *		sigName;
	char *		sigMeaning;
	char *		gdbResult;
	char *		returnValue;
	char *		returnType;
	int			code;
	char *		exp;
	char *		oldValue;
	char *		newValue;
};
typedef struct MIEvent	MIEvent;

extern MIEvent *MIEventNew(int, int);
extern void MIEventFree(MIEvent *var);
extern MIEvent *MIEventCreateStoppedEvent(char *reason, MIList *results);
#endif /* _MIEVENT_H_ */


