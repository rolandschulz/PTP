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
#include <ctype.h>

#include "MIList.h"
#include "MIResult.h"
#include "MIValue.h"
#include "MIOOBRecord.h"
#include "MISignalInfo.h"
#include "CLIOutput.h"

static int
getBoolean(char* value)
{
	if (value != NULL && strncmp(value, "Yes", 3) == 0) {
		return 1;
	}
	return 0;
}

void
CLIGetSigHandleList(MICommand *cmd, MIList** signals)
{
	MIList *oobs;
	MIOOBRecord *oob;
	MISignalInfo *sig;
	char *text = NULL;
	char *token;
	char *pch;
	int i;
	const char* delims[] = { " ", "\\" };

	*signals = MIListNew();

	if (!cmd->completed || cmd->output == NULL || cmd->output->oobs == NULL)
		return;

	oobs = cmd->output->oobs;
	for (MIListSet(oobs); (oob = (MIOOBRecord *)MIListGet(oobs)) != NULL; ) {
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
			sig = MISignalInfoNew();
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
			MIListAdd(*signals, (void *)sig);
		}
	}
}

double
CLIGetGDBVersion(MICommand *cmd)
{
	MIList *		oobs;
	MIOOBRecord *	oob;
	char *			text;

	if (!cmd->completed || cmd->output == NULL || cmd->output->oobs == NULL) {
		return -1.0;
	}

	if (cmd->output->rr != NULL && cmd->output->rr->resultClass == MIResultRecordERROR) {
		return -1.0;
	}

	oobs = cmd->output->oobs;
	for (MIListSet(oobs); (oob = (MIOOBRecord *)MIListGet(oobs)) != NULL; ) {
		text = oob->cstring;
		if (*text == '\0') {
			continue;
		}
		while (*text == ' ') {
			text++;
		}

		/*
		 * linux self: GUN gdb 6.5.0
		 * fedore: GNU gdb Red Hat Linux (6.5-8.fc6rh)
		 * Mac OS X: GNU gdb 6.1-20040303 (Apple version gdb-384) (Mon Mar 21 00:05:26 GMT 2005)
		 */
		if (strncmp(text, "GNU gdb", 7) == 0) {
			/*
			 * bypass "GNU gdb"
			 */
			text += 8;

			/*
			 * find first digit
			 */
			while (*text != '\0' && !isdigit(*text))
				text++;

			/*
			 * Convert whatever is here to a double
			 */
			if (*text != '\0')
				return strtod(text, NULL);
		}
	}
	return -1.0;
}

/*
 * Attempt to fix up the type information returned by the ptype command.
 *
 * - Replace newlines and tabs with spaces and replace multiple spaces with a single space.
 * - Remove "\\n" sequences.
 * - Discard '{' and any following characters
 *
 * Space allocated for the result must be freed by the caller.
 */
static char *
fix_type(char *str)
{
	int		finished = 0;
	int		seen_space = 0;
	int		seen_backslash = 0;
	char *	s = str;
	char *	r;
	char * 	result = (char *)malloc(strlen(str));

	/*
	 * Remove leading whitespace
	 */
	while (isspace(*s)) {
		s++;
	}

	for (r = result; *s != '\0' && !finished; s++) {
		switch (*s) {
		case ' ':
		case '\n':
		case '\t':
			if (!seen_space) {
				*r++ = ' ';
				seen_space = 1;
			}
			break;

		case '\\':
			seen_backslash = 1;
			break;

		case '{':
			finished = 1;
			break;

		default:
			if (!seen_backslash) {
				*r++ = *s;
			}
			seen_backslash = 0;
			seen_space = 0;
		}
	}

	*r = '\0';

	/*
	 * Remove trailing whitespace
	 */
	r--;
	while (isspace(*r) && r >= result) {
		*r-- = '\0';
	}

	return result;
}

char *
CLIGetPTypeInfo(MICommand *cmd)
{
	MIList *		oobs;
	MIOOBRecord *	oob;
	char *			text = NULL;

	if (!cmd->completed || cmd->output == NULL || cmd->output->oobs == NULL)
		return NULL;

	oobs = cmd->output->oobs;
	for (MIListSet(oobs); (oob = (MIOOBRecord *)MIListGet(oobs)) != NULL; ) {
		text = oob->cstring;
		if (*text == '\0') {
			continue;
		}
		while (*text == ' ') {
			text++;
		}

		if (strncmp(text, "type =", 6) == 0) {
			text += 6;
			text = fix_type(text);

			if (strlen(text) == 0) {
				/*
				 * Look at next line for type
				 */
				oob = (MIOOBRecord *)MIListGet(oobs);
				if (oob != NULL) {
					free(text);
					text = fix_type(oob->cstring);
				}
			}
			return text;
		}
	}
	return NULL;
}

CLIInfoThreadsInfo *
CLIInfoThreadsInfoNew(void)
{
	CLIInfoThreadsInfo * info;
	info = (CLIInfoThreadsInfo *)malloc(sizeof(CLIInfoThreadsInfo));
	info->current_thread_id = 1;
	info->thread_ids = NULL;
	return info;
}

void
CLIInfoThreadsInfoFree(CLIInfoThreadsInfo *info)
{
	if (info->thread_ids != NULL)
		MIListFree(info->thread_ids, free);
	free(info);
}

CLIInfoThreadsInfo *
CLIGetInfoThreadsInfo(MICommand *cmd)
{
	MIList *				oobs;
	MIOOBRecord *			oob;
	CLIInfoThreadsInfo *	info = CLIInfoThreadsInfoNew();
	char * 					text = NULL;
	char *					id = NULL;

	if (!cmd->completed || cmd->output == NULL || cmd->output->oobs == NULL) {
		return info;
	}

	if (cmd->output->rr != NULL && cmd->output->rr->resultClass == MIResultRecordERROR) {
		return info;
	}

	oobs = cmd->output->oobs;
	info->thread_ids = MIListNew();
	for (MIListSet(oobs); (oob = (MIOOBRecord *)MIListGet(oobs)) != NULL; ) {
		text = oob->cstring;

		if (*text == '\0') {
			continue;
		}
		while (*text == ' ') {
			text++;
		}
		if (strncmp(text, "*", 1) == 0) {
			text += 2;//escape "* ";
			if (isdigit(*text)) {
				info->current_thread_id = strtol(text, &text, 10);
			}
			continue;
		}
		if (isdigit(*text)) {
			if (info->thread_ids == NULL)
				info->thread_ids = MIListNew();

			id = strchr(text, ' ');
			if (id != NULL) {
				*id = '\0';
				MIListAdd(info->thread_ids, (void *)strdup(text));
			}
		}
	}
	return info;
}

CLIInfoProcInfo *
CLIInfoProcInfoNew()
{
	CLIInfoProcInfo *	info;
	info = (CLIInfoProcInfo *)malloc(sizeof(CLIInfoProcInfo));
	info->pid = -1;
	info->cmdline = NULL;
	info->cwd = NULL;
	info->exe = NULL;
	return info;
}

void
CLIInfoProcInfoFree(CLIInfoProcInfo *info)
{
	if (info->cmdline != NULL) {
		free(info->cmdline);
	}
	if (info->cwd != NULL) {
		free(info->cwd);
	}
	if (info->exe != NULL) {
		free(info->exe);
	}
	free(info);
}

#ifdef __APPLE__
CLIInfoProcInfo *
CLIGetInfoProcInfo(MICommand *cmd)
{
	char *				str = "";
	MIResult *			result;
	MIValue *			value;
	MIResultRecord *	rr;
	CLIInfoProcInfo *	info = CLIInfoProcInfoNew();

	if (!cmd->completed || cmd->output == NULL || cmd->output->rr == NULL)
		return info;

	rr = cmd->output->rr;
	for (MIListSet(rr->results); (result = (MIResult *)MIListGet(rr->results)) != NULL; ) {
		value = result->value;
		if (value->type == MIValueTypeConst) {
			str = value->cstring;
		}
		if (strcmp(result->variable, "process-id") == 0) {
			info->pid = (int)strtol(str, NULL, 10);
		}
	}

	return info;
}
#else /* __APPLE__ */
CLIInfoProcInfo *
CLIGetInfoProcInfo(MICommand *cmd)
{
	int					len;
	MIList *			oobs;
	MIOOBRecord *		oob;
	char *				text = NULL;
	CLIInfoProcInfo *	info = CLIInfoProcInfoNew();

	if (!cmd->completed || cmd->output == NULL || cmd->output->oobs == NULL)
		return info;

	oobs = cmd->output->oobs;
	for (MIListSet(oobs); (oob = (MIOOBRecord *)MIListGet(oobs)) != NULL; ) {
		text = oob->cstring;
		if (*text == '\0') {
			continue;
		}
		while (*text == ' ') {
			text++;
		}

		if (strncmp(text, "process", 7) == 0) {
			text += 8; /* bypass " " */
			if (text != NULL) {
				info->pid = (int)strtol(text, NULL, 10);
			}
		} else if (strncmp(text, "cmdline", 7) == 0) {
			text += 11; /* bypass " = '" */
			len = strlen(text) - 1; /* exclude "'" */
			info->cmdline = (char *)malloc(len + 1);
			memcpy(info->cmdline, text, len);
			info->cmdline[len] = '\0';
		} else if (strncmp(text, "cwd", 3) == 0) {
			text += 7; /* bypass " = '" */
			len = strlen(text) - 1; /* exclude "'" */
			info->cwd = (char *)malloc(len + 1);
			memcpy(info->cwd, text, len);
			info->cwd[len] = '\0';
		} else if (strncmp(text, "exe", 3) == 0) {
			text += 7; /* bypass " = '" */
			len = strlen(text) - 1; /* exclude "'" */
			info->exe = (char *)malloc(len + 1);
			memcpy(info->exe, text, len);
			info->exe[len] = '\0';
		}
	}
	return info;
}
#endif /* __APPLE__ */
