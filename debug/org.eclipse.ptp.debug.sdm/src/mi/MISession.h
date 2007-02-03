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
#ifndef _MISESSION_H_
#define _MISESSION_H_

#include <sys/select.h>

#include "list.h"
#include "MIOutput.h"
#include "MICommand.h"
#include "MIEvent.h"

struct MISession {
	int				in_fd; /* GDB input */
	int				out_fd; /* GDB output */
	int				pid;
	int				exited;
	int				exit_status;
	MICommand *		command;
	List *			send_queue;
	char *			gdb_path;
	struct timeval	select_timeout;
	void			(*event_callback)(MIEvent *);
	void			(*cmd_callback)(MIResultRecord *);
	void			(*exec_callback)(char *, List *);
	void			(*status_callback)(char *, List *);
	void			(*notify_callback)(char *, List *);
	void			(*console_callback)(char *);
	void			(*log_callback)(char *);
	void			(*target_callback)(char *);	
};
typedef struct MISession	MISession;

extern MISession *MISessionNew(void);
extern void MISessionFree(MISession *sess);
extern void MISessionSetTimeout(MISession *sess, long sec, long usec);
extern void MISessionSetDebug(int debug);
extern int MISessionStartLocal(MISession *sess, char *);
extern void MISessionRegisterEventCallback(MISession *sess, void (*callback)(MIEvent *));
extern void MISessionRegisterCommandCallback(MISession *sess, void (*callback)(MIResultRecord *));
extern void MISessionRegisterExecCallback(MISession *sess, void (*callback)(char *, List *));
extern void MISessionRegisterStatusCallback(MISession *sess, void (*callback)(char *, List *));
extern void MISessionRegisterNotifyCallback(MISession *sess, void (*callback)(char *, List *));
extern void MISessionRegisterConsoleCallback(MISession *sess, void (*callback)(char *));
extern void MISessionRegisterLogCallback(MISession *sess, void (*callback)(char *));
extern void MISessionRegisterTargetCallback(MISession *sess, void (*callback)(char *));
extern void MISessionSetGDBPath(MISession *sess, char *path);
extern int MISessionSendCommand(MISession *sess, MICommand *cmd);
extern int MISessionCommandCompleted(MISession *sess);
//extern void MISessionProcessCommandsAndResponses(MISession *sess, fd_set *rfds, fd_set *wfds);
extern void MISessionProcessCommandsAndResponses(MISession *sess, fd_set *rfds, fd_set *wfds, MIOutput *output);
extern void MISessionGetFds(MISession *sess, int *nfds, fd_set *rfds, fd_set *wfds, fd_set *efds);
//extern int MISessionProgress(MISession *sess);
extern int MISessionProgress(MISession *sess, MIOutput *output);
extern void ProcessCLIResultRecord(MIResultRecord *, void *);
#endif /* _MISESSION_H_ */
