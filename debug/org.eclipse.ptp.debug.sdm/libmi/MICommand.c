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

#define MICOMMAND_OPT_SIZE	10

#include "MICommand.h"
#include "MIResult.h"

MICommand *
MICommandNew(char *command, int class)
{
	MICommand *	cmd;

	cmd = (MICommand *)malloc(sizeof(MICommand));
	cmd->command = strdup(command);
	cmd->options = (char **)malloc(MICOMMAND_OPT_SIZE * sizeof(char *));
	cmd->opt_size = MICOMMAND_OPT_SIZE;
	cmd->num_options = 0;
	cmd->completed = 0;
	cmd->expected_class = class;
	cmd->output = MIOutputNew();
	cmd->callback = NULL;
	cmd->timeout = 0;
	return cmd;
}

void
MICommandSetTimeout(MICommand *cmd, int timeout)
{
	cmd->timeout = timeout;
}

void
MICommandFree(MICommand *cmd)
{
	int i;

	if (cmd->command != NULL)
		free(cmd->command);
	if (cmd->num_options > 0) {
		for (i = 0; i < cmd->num_options; i++)
			free(cmd->options[i]);
	}
	free(cmd->options);
	MIOutputFree(cmd->output);
	free(cmd);
}

void
MICommandAddOption(MICommand *cmd, char *opt, char *arg)
{
	int add = 1;

	if (arg != NULL)
		add++;

	if (cmd->num_options + add > cmd->opt_size) {
		cmd->opt_size += MICOMMAND_OPT_SIZE;
		cmd->options = (char **)realloc(cmd->options, cmd->opt_size * sizeof(char *));
	}

	cmd->options[cmd->num_options++] = strdup(opt);

	if (arg != NULL)
		cmd->options[cmd->num_options++] = strdup(arg);
}

void
MICommandRegisterCallback(MICommand *cmd, void (*callback)(MIResultRecord *, void *), void *data)
{
	cmd->callback = callback;
	cmd->cb_data = data;
}

int
MICommandCompleted(MICommand *cmd)
{
	return cmd->completed;
}

MIResultRecord *
MICommandResult(MICommand *cmd)
{
	if (cmd->output == NULL)
		return NULL;
	return cmd->output->rr;
}

int
MICommandResultOK(MICommand *cmd)
{
	if (!cmd->completed || cmd->output == NULL || cmd->output->rr == NULL)
		return 0;

	return cmd->output->rr->resultClass == cmd->expected_class;
}

int
MICommandResultClass(MICommand *cmd)
{
	if (!cmd->completed || cmd->output == NULL || cmd->output->rr == NULL)
		return MIResultRecordINVALID;

	return cmd->output->rr->resultClass;
}

char *
MICommandResultErrorMessage(MICommand *cmd)
{
	MIResult *	r;
	MIString *	s;
	char *		res = NULL;

	if (!cmd->completed || cmd->output == NULL || cmd->output->rr == NULL)
		return NULL;

	if (MICommandResultClass(cmd) != MIResultRecordERROR)
		return NULL;

	for (MIListSet(cmd->output->rr->results); (r = (MIResult *)MIListGet(cmd->output->rr->results)) != NULL;) {
		if (r->value != NULL) {
			s = MIValueToString(r->value);
			res = strdup(MIStringToCString(s));
			break;
		}
	}

	return res;
}

char *
MICommandToString(MICommand *cmd)
{
	int				i;
	int				cmd_len = strlen(cmd->command);
	int				size;
	char *			s;
	static int		str_size = 0;
	static char *	str_res = NULL;

	size = cmd_len + 3;

	for (i = 0; i < cmd->num_options; i++)
		size += strlen(cmd->options[i]) + 1;

	if (size > str_size) {
		if (str_res != NULL)
			free(str_res);
		str_res = (char *)malloc(size);
		str_size = size;
	}

	s = str_res;
	memcpy(s, cmd->command, cmd_len);
	s += cmd_len;

	for (i = 0; i < cmd->num_options; i++) {
		int len = strlen(cmd->options[i]);
		*s++ = ' ';
		memcpy(s, cmd->options[i], len);
		s += len;
	}

	*s++ = '\n';
	*s++ = '\0';

	return str_res;
}

