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
 
#ifndef _DBG_EVENT_H_
#define _DBG_EVENT_H_

#include <aif.h>

#include "breakpoint.h"
#include "list.h"

#define DBGEV_OK		0
#define DBGEV_BPHIT	1
#define DBGEV_SIGNAL	2
#define DBGEV_EXIT	3
#define DBGEV_STEP	4
#define DBGEV_BPSET	5
#define DBGEV_FRAMES	7
#define DBGEV_DATA	8
#define DBGEV_TYPE	9
#define DBGEV_VARS	9
#define DBGEV_INIT	10

struct dbg_event {
	int			event;
	breakpoint *	bp;
	List *		list;
	char *		type_desc;
	AIF *		data;
};
typedef struct dbg_event dbg_event;

extern dbg_event *	NewEvent(int);
extern void			FreeEvent(dbg_event *);
#endif /* _DBG_EVENT_H_ */
