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

#ifndef _MIFRAME_H_
#define _MIFRAME_H_

#include "MICommand.h"
#include "MIValue.h"

struct MIFrame {
	int		level;
	char *	addr;
	char *	func;
	char *	file;
	int		line;
	List *	args;
};
typedef struct MIFrame	MIFrame;

extern MIFrame *MIFrameNew(void);
extern void MIFrameFree(MIFrame *f);
extern MIFrame *MIFrameParse(MIValue *tuple);
extern List *MIFrameGetStackListFramesInfo(MICommand *cmd);
extern MIString *MIFrameToString(MIFrame *f);
#endif _MIFRAME_H_

