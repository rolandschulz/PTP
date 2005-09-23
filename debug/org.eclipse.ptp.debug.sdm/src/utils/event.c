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

#include <stdlib.h>
#include <string.h>

#include "dbg_event.h"
#include "stackframe.h"

dbg_event *	
NewEvent(int event) {
	dbg_event *	e = (dbg_event *)malloc(sizeof(dbg_event));
	
	memset((void *)e, 0, sizeof(dbg_event));
	 
	e->event = event;
	
	return e;
}

void	
FreeEvent(dbg_event *e) {
	switch (e->event) {
	case DBGEV_OK:
	case DBGEV_EXIT:
	case DBGEV_STEP:
	case DBGEV_INIT:
		break;
		
	case DBGEV_BPSET:
	case DBGEV_BPHIT:
		if (e->bp != NULL)
			FreeBreakpoint(e->bp);
		break;
		
	case DBGEV_FRAMES:
		if (e->list != NULL)
			DestroyList(e->list, FreeStackframe);
		break;
		
	case DBGEV_DATA:
		if (e->data != NULL)
			AIFFree(e->data);
		break;
		
	case DBGEV_TYPE:
		if (e->type_desc != NULL)
			free(e->type_desc);
		break;
		
	case DBGEV_VARS:
		if (e->list != NULL)
			DestroyList(e->list, free);
		break;
		
	case DBGEV_SIGNAL:
		if (e->list != NULL) {
			free(e->sig_name);
			free(e->sig_meaning);
		}
		break;
	}
	
	if (e->procs != NULL)
		procset_free(e->procs);

	free(e);
}
