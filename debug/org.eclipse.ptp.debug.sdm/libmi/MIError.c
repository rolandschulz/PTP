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
#ifdef __gnu_linux__
#define _GNU_SOURCE
#endif /* __gnu_linux__ */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include "MIError.h"

static int 		MIErrorNum = 0;
static char *	MIErrorStr = NULL;

static char * MIErrorTab[] =
{
	"NO_ERROR",
	"System error: %s",
	"GDB session does not exist or was terminated",
};

/*
 * Error handling
 */
void
MISetError(int errnum, char *msg)
{
	MIErrorNum = errnum;
	
	if (MIErrorStr != NULL) {
		free(MIErrorStr);
		MIErrorStr = NULL;
	}
	
	if (MIErrorNum >= sizeof(MIErrorTab)/sizeof(char *)) {
		if (msg != NULL) {
			MIErrorStr = strdup(msg);
		} else {
			asprintf(&MIErrorStr, "Error %d occurred.", MIErrorNum);
		}
	} else {
		if (msg == NULL)
			msg = "<null>";
			
		asprintf(&MIErrorStr, MIErrorTab[MIErrorNum], msg);
	}
}

int
MIGetError(void)
{
	return MIErrorNum;
}

char *
MIGetErrorStr(void)
{
	if (MIErrorStr == NULL)
		return "";
		
	return MIErrorStr;
}
