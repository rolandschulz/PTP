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

#include "stackframe.h"

stackframe *	
NewStackframe(int level)
{
	stackframe *	sf = (stackframe *)malloc(sizeof(stackframe));
	
	sf->level = level;
	sf->loc.file = NULL;
	sf->loc.func = NULL;
	sf->loc.addr = NULL;
	sf->loc.line = 0;
	
	return sf;
}

void
FreeLocation(location *loc) 
{
	if (loc->file != NULL) {
		free(loc->file);
		loc->file = NULL;
	}
	if (loc->func != NULL) {
		free(loc->func);
		loc->func = NULL;
	}
	if (loc->addr != NULL) {
		free(loc->addr);
		loc->addr = NULL;
	}
}

void	
FreeStackframe(stackframe *sf)
{
	FreeLocation(&sf->loc);	
	free(sf);
}
