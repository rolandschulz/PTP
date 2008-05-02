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
#include "MIOutput.h"

struct MISession;

struct MICommand {
	char *			command;								/* command to execute */
	char **			options;								/* command options */
	int				num_options;							/* number of options for command */
	int				opt_size;								/* allocated size of options */
	int				completed;								/* command has been completed */
	int				expected_class;
	MIOutput *		output;									/* result from completed command */
	void			(*callback)(MIResultRecord *, void *);	/* command completed callback */
	void *			cb_data;								/* callback data */
};
typedef struct MICommand	MICommand;

extern MICommand *MICommandNew(char *, int);
extern void MICommandFree(MICommand *cmd);
extern void MICommandAddOption(MICommand *cmd, char *opt, char *arg);
extern int MICommandCompleted(MICommand *cmd);
extern int MICommandResultOK(MICommand *cmd);
extern MIResultRecord *MICommandResult(MICommand *cmd);
extern int MICommandResultClass(MICommand *cmd);
extern char *MICommandResultErrorMessage(MICommand *cmd);
extern void MICommandRegisterCallback(MICommand *cmd, void (*callback)(MIResultRecord *, void *), void *data);
extern char *MICommandToString(MICommand *cmd);

/*
 * -gdb-* commands
 */
extern MICommand *MIGDBSet(char *, char *);
extern MICommand *MIGDBExit(void);
extern MICommand *MIGDBShowEndian(void);
extern MICommand *MIGDBVersion(void);

/*
 * -exec-* commands
 */
extern MICommand *MIExecArguments(char **);
extern MICommand *MIExecContinue(void);
extern MICommand *MIExecRun(void);
extern MICommand *MIExecStep(int);
extern MICommand *MIExecNext(int);
extern MICommand *MIExecFinish(void);
extern MICommand *MIExecInterrupt(void);
extern MICommand *MIExecUtil(char*);

/*
 * -break-* commands
 */
extern MICommand *MIBreakInsert(int isTemporary, int isHardware, char *condition, int ignoreCount, char *line, int tid);
extern MICommand *MIBreakDelete(int nbps, int *bpids);
extern MICommand *MIBreakDisable(int nbps, int *bpids);
extern MICommand *MIBreakEnable(int nbps, int *bpids);
extern MICommand *MIBreakCondition(int nbps, int *bpids, char *expr);
extern MICommand *MIBreakWatch(char *expr, int access, int read);
extern MICommand *MIBreakAfter(int nbps, int *bpids, int ignoreCount);

/*
 * -stack-* commands
 */
extern MICommand *MIStackSelectFrame(int level);
extern MICommand *MIStackListFrames(int low, int high);
extern MICommand *MIStackListAllFrames(void);
extern MICommand *MIStackListLocals(int vals);
extern MICommand *MIStackListArguments(int vals, int low, int high);
extern MICommand *MIStackListAllArguments(int vals);
extern MICommand *MIStackInfoFrame(void);
extern MICommand *MIStackInfoDepth(void);
extern MICommand *CLIFrame(void);

/*
 * -var-* commands
 */
extern MICommand *MIVarCreate(char *name, char *frame, char *expr);
extern MICommand *MIVarDelete(char *name);
extern MICommand *MIVarListChildren(char *name);
extern MICommand *MIVarEvaluateExpression(char *name);
extern MICommand *MIDataEvaluateExpression(char *name);
extern MICommand *MIVarUpdate(char *name);
extern MICommand *MIVarInfoType(char *name);
extern MICommand *MIVarInfoNumChildren(char *name);

/*
 * -thread-* commands
 */
extern MICommand *CLIInfoThreads(void);
extern MICommand *MIThreadSelect(int threadNum);

/*
 * -data-* commands
 */
extern MICommand *MIDataReadMemory(long, char *, char *, int, int, int, char *);
extern MICommand *MIDataWriteMemory(long offset, char * address, char * format, int wordSize, char * value);

#ifdef __APPLE__
extern MICommand *MIPidInfo(void);
#endif /* __APPLE__ */

/*
 * Non-MI commands
 */
extern MICommand *CLIPType(char *name);

/*
 * signal commands
 */
extern MICommand *CLIListSignals(char *);
extern MICommand *CLISignalInfo(char *);
extern MICommand *CLIHandle(char *);

#endif /* _MICOMMAND_H_ */
