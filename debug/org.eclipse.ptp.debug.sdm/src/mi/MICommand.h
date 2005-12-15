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
#ifndef _MICOMMAND_H_
#define _MICOMMAND_H_

#include "MIResultRecord.h"

struct MICommand {
	char *			command;							/* command to execute */
	char **			options;							/* command options */
	int				num_options;						/* number of options for command */
	int				opt_size;						/* allocated size of options */
	int				completed;						/* command has been completed */
	int				expected_class;
	MIResultRecord *	result;							/* result from completed command */
	void				(*callback)(MIResultRecord *);	/* command completed callback */
};
typedef struct MICommand	MICommand;

extern MICommand *MICommandNew(char *, int);
extern void MICommandFree(MICommand *cmd);
extern void MICommandAddOption(MICommand *cmd, char *opt, char *arg);
extern int MICommandCompleted(MICommand *cmd);
extern int MICommandResultOK(MICommand *cmd);
extern MIResultRecord *MICommandResult(MICommand *cmd);
extern void MICommandRegisterCallback(MICommand *cmd, void (*callback)(MIResultRecord *));
extern char *MICommandToString(MICommand *cmd);

/*
 * -gdb-* commands
 */
extern MICommand *MIGDBSet(char *, char *);
extern MICommand *MIGDBExit(void);

/*
 * -exec-* commands
 */
extern MICommand *MIExecContinue(void);
extern MICommand *MIExecRun(void);
extern MICommand *MIExecStep(int);
extern MICommand *MIExecNext(int);
extern MICommand *MIExecFinish(void);
extern MICommand *MIExecInterrupt(void);

/*
 * -break-* commands
 */
extern MICommand *MIBreakInsert(int isTemporary, int isHardware, char *condition, int ignoreCount, char *line, int tid);
extern MICommand *MIBreakDelete(int nbps, int *bpids);

/*
 * -stack-* commands
 */
extern MICommand *MIStackSelectFrame(int level);
extern MICommand *MIStackListFrames(int low, int high);
extern MICommand *MIStackListAllFrames(void);
extern MICommand *MIStackListLocals(int vals);
extern MICommand *MIStackListArguments(int vals, int low, int high);
extern MICommand *MIStackListAllArguments(int vals);

/*
 * -var-* commands
 */
extern MICommand *MIVarCreate(char *name, char *frame, char *expr);
extern MICommand *MIVarDelete(char *name);
extern MICommand *MIVarListChildren(char *name);
#endif /* _MICOMMAND_H_ */

