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

#include "MISession.h"
#include "MICommand.h"

MICommand *
MICommandNew(char *command)
{
	MICommand *	cmd;
	
	cmd = (MICommand *)malloc(sizeof(MICommand));
	cmd->command = strdup(command);
	cmd->options = (char **)malloc(MICOMMAND_OPT_SIZE * sizeof(char *));
	cmd->opt_size = MICOMMAND_OPT_SIZE;
	cmd->num_options = 0;
	return cmd;
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
		free(cmd->options);
	}
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

int
MICommandSend(MISession *sess, MICommand *cmd)
{
}

