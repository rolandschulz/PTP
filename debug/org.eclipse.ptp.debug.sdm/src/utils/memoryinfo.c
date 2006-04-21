/*******************************************************************************
 * Copyright (c) 2005 The Regents of the University of California. 
 * This material was produced under U.S. Government contract W-7405-ENG-36 
 * for Los Alamos National Laboratory, which is operated by the University 
 * of California for the U.S. Department of Energy. The U.S. Government has 
 * rights to use, reproduce, and distribute this software. NEITHER THE 
 * GOVERNMENT NOR THE UNIVERSITY MAKES ANY WARRANTY, EXPRESS OR IMPLIED, OR 
 * ASSUMES ANY LIABILITY FOR THE USE OF THIS SOFTWARE. If software is modified 
 * to produce derivative works, such modified software should be clearly marked, 
 * so as not to confuse it with the version available from LANL.
 * 
 * Additionally, this program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * LA-CC 04-115
 *******************************************************************************/
 
 /**
 * @author Clement chu
 * 
 */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include "memoryinfo.h"

memoryinfo * NewMemoryInfo(void) {
	memoryinfo * meminfo = (memoryinfo *)malloc(sizeof(memoryinfo));	
	meminfo->addr = NULL;
	meminfo->memories = NULL;
	return meminfo;
}

memory * NewMemory(void) {
	memory * mem = (memory *)malloc(sizeof(memory));	
	mem->addr = NULL;
	mem->data = NULL;
	mem->ascii = NULL;
	return mem;
}

void FreeMemoryInfo(memoryinfo *meminfo)  {
	if (meminfo->addr != NULL)
		free(meminfo->addr);
	if (meminfo->memories != NULL)
		DestroyList(meminfo->memories, FreeMemory);
	free(meminfo);
}

void FreeMemory(memory *mem) {
	if (mem->addr != NULL)
		free(mem->addr);
	if (mem->ascii != NULL)
		free(mem->ascii);
	if (mem->data != NULL)
		DestroyList(mem->data, free);
	free(mem);
}
