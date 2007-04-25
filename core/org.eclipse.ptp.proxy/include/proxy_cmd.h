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
 
#ifndef PROXY_CMD_H_
#define PROXY_CMD_H_

/*
 * CMD codes must EXACTLY match org.eclipse.ptp.core.proxy.IProxyCommand
 */

#define CMD_QUIT			1
#define CMD_INIT			2
#define CMD_MODEL_DEF		3
#define CMD_START_EVENTS	4
#define CMD_STOP_EVENTS		5
#define CMD_SUBMIT_JOB		6
#define CMD_TERM_JOB		7

#define CMD_ID_SIZE			4
#define CMD_ID_MASK			0xffff
#define CMD_TRANS_ID_SIZE	4
#define CMD_TRANS_ID_MASK	0xffff
#define CMD_NARGS_SIZE		8
#define CMD_NARGS_MASK		0xffffffff
#define CMD_ARG_LEN_SIZE	8
#define CMD_ARG_LEN_MASK	0xffffffff

/*
 * Command dispatch structure
 */

typedef int (*proxy_cmd)(int trans_id, int nargs, char **args);

struct proxy_commands {
	int			cmd_base;
	int			cmd_size;
	proxy_cmd *	cmd_funcs;
};
typedef struct proxy_commands	proxy_commands;

#endif /*PROXY_CMD_H_*/
