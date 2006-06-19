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

#include "MIOOBRecord.h"
#include "CLISigHandle.h"

#include "signalinfo.h"

static int 
getBoolean(char* value) 
{
	if (value != NULL && strncmp(value, "Yes", 3) == 0) {
		return 1;
	}
	return 0;
}

void 
CLIGetSigHandleList(MICommand *cmd, List** signals) 
{
	List *oobs;
	MIOOBRecord *oob;
	char *text = NULL;
	
	const char* delims[] = { " ", "\\" };
	char *token, *pch;
	int i;
	
	signalinfo *sig;
	*signals = NewList();

	if (!cmd->completed || cmd->output == NULL || cmd->output->oobs == NULL)
		return;

	oobs = cmd->output->oobs;
	for (SetList(oobs); (oob = (MIOOBRecord *)GetListElement(oobs)) != NULL; ) {
		text = oob->cstring;
		if (*text == '\0' || *text == '\\') {
			continue;
		}
		
		if (strncmp(text, "Signal", 6) != 0 && strncmp(text, "Use", 3) != 0 && strncmp(text, "info", 4) != 0) {
			token = strdup(text);
			pch = strstr(token, delims[0]);
			if (pch == NULL) {
				continue;
			}
			sig = NewSignalInfo();
			for (i=0; pch != NULL; i++,pch=strstr(token, delims[1])) {
				if (*pch == '\0') {
					break;
				}
				
				*pch = '\0';
				pch++;
				while (*pch == ' ' || *pch =='t') { //remove whitespace or t character
					pch++;
				}
				if (*pch == '\\') { //ignore '\\t' again
					pch += 2;
				}

//printf("---token(%d): %s\n", i, strdup(token));
				switch(i) {
					case 0:
						sig->name = strdup(token);
						break;
					case 1:
						sig->stop = getBoolean(token);
						break;
					case 2:
						sig->print = getBoolean(token);
						break;
					case 3:
						sig->pass = getBoolean(token);
						break;
					case 4:
						sig->desc = strdup(token);
						break;
				}
				token = strdup(pch);
			}
			free(token);
			free(pch);
			AddToList(*signals, (void *)sig);			
		}
	}
}
