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

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <ctype.h>

#include "breakpoint.h"
#include "list.h"

breakpoint *	
NewBreakpoint(int bpid) {
	breakpoint *	bp = (breakpoint *)malloc(sizeof(breakpoint));
	
	bp->id = bpid;
	bp->ignore = 0;
	bp->special = 0;
	bp->deleted = 0;
	bp->type = NULL;
	bp->loc.file = NULL;
	bp->loc.func = NULL;
	bp->loc.addr = NULL;
	bp->loc.line = 0;
	bp->hits = 0;
	
	return bp;
}

void	
FreeBreakpoint(breakpoint *bp) {
	if (bp->type != NULL)
		free(bp->type);
	if (bp->loc.file != NULL)
		free(bp->loc.file);
	if (bp->loc.func != NULL)
		free(bp->loc.func);
	if (bp->loc.addr != NULL)
		free(bp->loc.addr);
		
	free(bp);
}

breakpoint *	
CopyBreakpoint(breakpoint *bp) {
	breakpoint *	nb = NewBreakpoint(bp->id);

	nb->ignore = bp->ignore;
	nb->special = bp->special;
	nb->deleted = bp->deleted;
	nb->type = strdup(bp->type);
	nb->loc.file = strdup(bp->loc.file);
	nb->loc.func = strdup(bp->loc.func);
	nb->loc.addr = strdup(bp->loc.addr);
	nb->loc.line = bp->loc.line;
	nb->hits = bp->hits;

	return nb;
}

void	
AddBreakpoint(List *l, breakpoint *bp) {
	AddToList(l, (void *)bp);
}

void	
RemoveBreakpoint(List *l, int bpid) {
	breakpoint *	bp = FindBreakpoint(l, bpid);
	
	if (bp != NULL)
		RemoveFromList(l, (void *)bp);
}

breakpoint *
FindBreakpoint(List *l, int bpid) {
	breakpoint *	bp;
	
	SetList(l);
	
	while ((bp = (breakpoint *)GetListElement(l)) != NULL) {
		if (bp->id == bpid)
			return bp;
	}
		
	return NULL;
}
