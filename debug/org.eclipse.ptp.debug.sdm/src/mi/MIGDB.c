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

#include "MIValue.h"
#include "MIResult.h"
#include "MIMemory.h"
#include "MIOOBRecord.h"

char * 
MIGetGDBVersion(MICommand *cmd)
{
	List *oobs;
	MIOOBRecord *oob;
	char *text;
	char * pch;
	
	if (!cmd->completed || cmd->output == NULL || cmd->output->oobs == NULL) {
		return NULL;
	}

	if (cmd->output->rr != NULL && cmd->output->rr->resultClass == MIResultRecordERROR) {
		return NULL;
	}

	oobs = cmd->output->oobs;
	for (SetList(oobs); (oob = (MIOOBRecord *)GetListElement(oobs)) != NULL; ) {
		text = oob->cstring;
		if (*text == '\0') {
			continue;
		}
		while (*text == ' ') {
			*text++;
		}

		if (strncmp(text, "GNU gdb", 7) == 0) {
			text += 8; //bypass "GUN gdb "
			pch = strchr(text, '\\');
			if (pch != NULL) {
				*pch = '\0';
				return strdup(text);
			}
		}
	}
	return NULL;
}
