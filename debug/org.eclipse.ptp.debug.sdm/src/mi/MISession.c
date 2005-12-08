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

#include <sys/types.h>
#include <sys/wait.h>
#include <sys/errno.h>

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <signal.h>
#include <unistd.h>
#include <fcntl.h>

#include	"list.h"
#include "MISession.h"
#include "MICommand.h"
#include "MIError.h"
#include "MIOOBRecord.h"

static char *			MISessionGDBPath = NULL;
static List *			MISessionList = NULL;
static struct timeval		MISessionSelectTimeout = {0, 1000};

MISession *
MISessionNew(void)
{
	MISession *	sess = (MISession *)malloc(sizeof(MISession));
	
	sess->in_fd = -1;
	sess->out_fd = -1;
	sess->pid = -1;
	sess->command_completed = 1;
	sess->send_queue = NewList();
	sess->output = NULL;
	sess->cmd_callback = NULL;
	sess->exec_callback = NULL;
	sess->status_callback = NULL;
	sess->notify_callback = NULL;
	sess->console_callback = NULL;
	sess->log_callback = NULL;
	sess->target_callback = NULL;

	if (MISessionList == NULL)
		MISessionList = NewList();
	AddToList(MISessionList, (void *)sess);
	
	return sess;
}

void
MISessionFree(MISession *sess)
{
	RemoveFromList(MISessionList, (void *)sess);
	free(sess);
}

static void
MISessionHandleChild(int sig)
{
	int			stat;
	pid_t		pid;
	MISession *	sess;
	
	pid = wait(&stat);
	
	if (MISessionList != NULL) {
		for (SetList(MISessionList); (sess = (MISession *)GetListElement(MISessionList)) != NULL; ) {
			if (sess->pid == pid) {
				// TODO notify that gdb has died
				sess->pid = -1;
				close(sess->in_fd);
				close(sess->out_fd);
				sess->in_fd = -1;
				sess->out_fd = -1;
			}
		}
	}
}

MISession *
MISessionLocal(void)
{
	int			p1[2];
	int			p2[2];
	MISession *	sess = MISessionNew();
	
	if (pipe(p1) < 0 || pipe(p2) < 0) {
		MISetError(MI_ERROR_SYSTEM, strerror(errno));
		MISessionFree(sess);
		return NULL;
	}
	
	if (fcntl(p1[0], F_SETFL, O_NONBLOCK) < 0) {
		MISetError(MI_ERROR_SYSTEM, strerror(errno));
		MISessionFree(sess);
		return NULL;
	}
	
	signal(SIGCHLD, MISessionHandleChild);
	
	switch (sess->pid = fork())
	{
	case 0:
		dup2(p2[0], 0);
		dup2(p1[1], 1);
		close(p1[0]);
		close(p1[1]);
		close(p2[0]);
		close(p2[1]);
		
		execlp(MISessionGDBPath, "gdb", "-q", "-i", "mi", NULL);
		
		exit(1);
	
	case -1:
		MISetError(MI_ERROR_SYSTEM, strerror(errno));
		MISessionFree(sess);
		return NULL;
	        
	default:
	    break;
	}

	sess->in_fd = p2[1];
	sess->out_fd = p1[0];
	
	close(p1[1]);
	close(p2[0]);
	
	return sess;
}

void
MISessionSetGDBPath(char *path)
{
	if (MISessionGDBPath != NULL)
		free(MISessionGDBPath);
	MISessionGDBPath = strdup(path);
}

int
MISessionSendCommand(MISession *sess, MICommand *cmd)
{
	if (sess->pid == -1) {
		MISetError(MI_ERROR_SESSION, "");
		return -1;
	}
	
	AddToList(sess->send_queue, (void *)cmd);
	
	return 0;
}

/*
 * Send command to GDB. We keep writing
 * until the whole command has been sent.
 * 
 * There is a chance this could block.
 */
static int
WriteCommand(int fd, char *cmd)
{
	int	n;
	int	len = strlen(cmd);
	
	while (len > 0) {
		n = write(fd, cmd, len);
		if (n <= 0) {
			if (n < 0) {
				if (errno == EINTR)
					continue;
				MISetError(MI_ERROR_SYSTEM, strerror(errno));
			}
			return -1;
		}
		
		cmd += n;
		len -= n;
	}
	
	return 0;
}

/*
 * Read BUFSIZ chunks of data until we have read
 * everything available.
 * 
 * Everything has been read if:
 * 1) the read returns less than BUFSIZ
 * 2) the read would block (errno == EAGAIN)
 * 
 * The assumption is that O_NONBLOCK has been set 
 * of the file descriptor.
 * 
 * The buffer is static, so we just reuse it for each
 * call and increase the size if necessary.
 */
static char *
ReadResponse(int fd)
{
	int				n;
	int				len = 0;
	char *			p;
	static int		res_buf_len = BUFSIZ;
	static char *	res_buf = NULL;
	
	if (res_buf == NULL)
		res_buf = (char *)malloc(BUFSIZ);
	
	p = res_buf;
	
	for (;;) {
		n = read(fd, p, BUFSIZ);
		if (n <= 0) {
			if (n < 0) {
				if (errno == EAGAIN)
					break;
				if (errno == EINTR)
					continue;
				MISetError(MI_ERROR_SYSTEM, strerror(errno));
			}
			return NULL;
		}

		if (n < BUFSIZ)
			break;
			
		len += BUFSIZ;

		if (len == res_buf_len) {
			res_buf_len += BUFSIZ;
			res_buf = (char *)realloc(res_buf, res_buf_len);
		}
		
		p = &res_buf[len];
	}
	
	return res_buf;
}

/*
 * Send first pending command to GDB for each session.
 * 
 * For each session, read response from GDB and 
 * parse the result.
 * 
 * Assumes that fds contains file descriptors ready
 * for writing.
 */
void
MISessionProcessCommandsAndResponses(fd_set *rfds, fd_set *wfds)
{
	char *		str;
	MISession *	sess;
	MICommand *	cmd;
	
	if (MISessionList == NULL)
		return;
		
	for (SetList(MISessionList); (sess = (MISession *)GetListElement(MISessionList)) != NULL; ) {
		if (sess->pid == -1)
			continue;
			
		if (sess->in_fd != -1
			&& !EmptyList(sess->send_queue)
			&& sess->command_completed
			&& FD_ISSET(sess->in_fd, wfds))
		{
			cmd = (MICommand *)RemoveFirst(sess->send_queue);
			if (WriteCommand(sess->in_fd, MICommandToString(cmd)) < 0) {
				sess->in_fd = -1;
				continue;
			}
			sess->command_completed = 0;
		}

		if (sess->out_fd != -1 && FD_ISSET(sess->out_fd, rfds)) {
			if ((str = ReadResponse(sess->out_fd)) == NULL) {
				sess->out_fd = -1;
				continue;
			}
			
			if (sess->output != NULL)
				MIOutputFree(sess->output);
			sess->output = MIParse(str);	
					
			sess->command_completed = 1;
		}
	}
}

/*
 * Checks output for each session and calls any
 * callbacks that have been registered.
 */
void
MISessionDoCallbacks(void)
{
	MISession *		sess;
	MIOOBRecord *	oob;
	
	if (MISessionList == NULL)
		return;
		
	for (SetList(MISessionList); (sess = (MISession *)GetListElement(MISessionList)) != NULL; ) {
		if (sess->pid != -1 && sess->output != NULL) {
			if (sess->output->rr != NULL && sess->cmd_callback != NULL)
				sess->cmd_callback(sess->output->rr);
			if (sess->output->oobs != NULL) {
				for (SetList(sess->output->oobs); (oob = (MIOOBRecord *)GetListElement(sess->output->oobs)) != NULL; ) {
					switch (oob->type) {
					case MIOOBRecordTypeAsync:
						switch (oob->sub_type) {
						case MIOOBRecordExecAsync:
							if (sess->exec_callback != NULL)
								sess->exec_callback(oob->class, oob->results);
							break;
							
						case MIOOBRecordStatusAsync:
							if (sess->status_callback != NULL)
								sess->status_callback(oob->class, oob->results);
							break;
							
						case MIOOBRecordNotifyAsync:
							if (sess->notify_callback != NULL)
								sess->notify_callback(oob->class, oob->results);
							break;
						}
						break;
						
					case MIOOBRecordTypeStream:
						switch (oob->sub_type) {
						case MIOOBRecordConsoleStream:
							if (sess->console_callback != NULL)
								sess->console_callback(oob->cstring);
							break;
							
						case MIOOBRecordLogStream:
							if (sess->log_callback != NULL)
								sess->log_callback(oob->cstring);
							break;
							
						case MIOOBRecordTargetStream:
							if (sess->target_callback != NULL)
								sess->target_callback(oob->cstring);
							break;
						}
						break;
					}
				}
			}
		}
	}
}

void
MISessionGetFds(int *nfds, fd_set *rfds, fd_set *wfds, fd_set *efds)
{
	int			n = 0;
	MISession *	sess;
	
	if (MISessionList == NULL)
		return;
	
	if (rfds != NULL)	
		FD_ZERO(rfds);
		
	if (wfds != NULL)	
		FD_ZERO(wfds);
		
	if (efds != NULL)	
		FD_ZERO(efds);
	
	for (SetList(MISessionList); (sess = (MISession *)GetListElement(MISessionList)) != NULL; ) {
		if (sess->pid != -1) {
			if (wfds != NULL && sess->in_fd != -1) {
				FD_SET(sess->in_fd, wfds);
				if (sess->in_fd > n)
					n = sess->in_fd;
			}
			if (rfds != NULL && sess->out_fd != -1) {
				FD_SET(sess->out_fd, rfds);
				if (sess->out_fd > n)
					n = sess->out_fd;
			}
		}
	}
	
	if (nfds != NULL)
		*nfds = n;
}

/*
 * Default progress command if none supplied.
 */
int
MISessionProgress(void)
{
	int		n;
	int		nfds;
	fd_set	rfds;
	fd_set	wfds;
	
	MISessionGetFds(&nfds, &rfds, &wfds, NULL);
	
	n = select(nfds, &rfds, &wfds, NULL, &MISessionSelectTimeout);
	
	if (n == 0)
		return 0;
		
	if (n < 0) {
		MISetError(MI_ERROR_SYSTEM, strerror(errno));
		return -1;
	}
	
	MISessionProcessCommandsAndResponses(&rfds, &wfds);
	MISessionDoCallbacks();
	
	return n;
}