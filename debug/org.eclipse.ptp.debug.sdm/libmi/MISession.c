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

#include "MIList.h"
#include "MISession.h"
#include "MICommand.h"
#include "MIError.h"
#include "MIOOBRecord.h"
#include "MIValue.h"
#include "MIResult.h"

static MIList *			MISessionList = NULL;
static struct timeval	MISessionDefaultSelectTimeout = {0, 1000};
static int				MISessionDebug = 1;

static void DoOOBCallbacks(MISession *sess, MIList *oobs);
static void HandleChild(int sig);
static int WriteCommand(int fd, char *cmd);
static char *ReadResponse(int fd);

extern int get_master_pty(char **);
extern int get_slave_pty(char *);

MISession *
MISessionNew(void)
{
	MISession *	sess = (MISession *)malloc(sizeof(MISession));
	
	sess->in_fd = -1;
	sess->out_fd = -1;
	sess->pty_fd = -1;
	sess->pid = -1;
	sess->exited = 1;
	sess->exit_status = 0;
	sess->command = NULL;
	sess->send_queue = MIListNew();
	sess->gdb_path = strdup("gdb");
	sess->event_callback = NULL;
	sess->cmd_callback = NULL;
	sess->exec_callback = NULL;
	sess->status_callback = NULL;
	sess->notify_callback = NULL;
	sess->console_callback = NULL;
	sess->log_callback = NULL;
	sess->target_callback = NULL;
	sess->select_timeout = MISessionDefaultSelectTimeout;

	if (MISessionList == NULL)
		MISessionList = MIListNew();
	MIListAdd(MISessionList, (void *)sess);
	
	return sess;
}

void
MISessionFree(MISession *sess)
{
	MIListRemove(MISessionList, (void *)sess);
	free(sess);
}

static void
HandleChild(int sig)
{
	int			stat;
	pid_t		pid;
	MISession *	sess;
	
	pid = wait(&stat);
	
	if (MISessionList != NULL) {
		for (MIListSet(MISessionList); (sess = (MISession *)MIListGet(MISessionList)) != NULL; ) {
			if (sess->pid == pid) {
				sess->exited = 1;
				sess->exit_status = stat;
			}
		}
	}
}

void
MISessionSetTimeout(MISession *sess, long sec, long usec)
{
	sess->select_timeout.tv_sec = sec;
	sess->select_timeout.tv_usec = usec;
}

void
MISessionSetDebug(int debug)
{
	MISessionDebug = debug;
}

int
MISessionStartLocal(MISession *sess, char *prog)
{
	int			p1[2];
	int			p2[2];
	int			master;
	char *		name;
	
	if (pipe(p1) < 0 || pipe(p2) < 0) {
		MISetError(MI_ERROR_SYSTEM, strerror(errno));
		return -1;
	}
	
	sess->in_fd = p2[1];
	sess->out_fd = p1[0];
	sess->exited = 0;
	
	signal(SIGCHLD, HandleChild);
	signal(SIGPIPE, SIG_IGN);

	if ((sess->pty_fd = get_master_pty(&name)) < 0 ) {
		name = strdup("/dev/null");
	}
	
	switch (sess->pid = fork())
	{
	case 0:
		dup2(p2[0], 0);
		dup2(p1[1], 1);
		close(p1[0]);
		close(p1[1]);
		close(p2[0]);
		close(p2[1]);
		
		if (prog == NULL)
			execlp(sess->gdb_path, "gdb", "-q", "-tty", name, "-i", "mi", NULL);
		else
			execlp(sess->gdb_path, "gdb", "-q", "-tty", name, "-i", "mi", prog, NULL);
		
		exit(1);
	
	case -1:
		MISetError(MI_ERROR_SYSTEM, strerror(errno));
		return -1;
	        
	default:
	    break;
	}

	free(name);

	close(p1[1]);
	close(p2[0]);
	
	return 0;
}

void
MISessionRegisterEventCallback(MISession *sess, void (*callback)(MIEvent *))
{
	sess->event_callback = callback;
}

void
MISessionRegisterCommandCallback(MISession *sess, void (*callback)(MIResultRecord *))
{
	sess->cmd_callback = callback;
}

void
MISessionRegisterExecCallback(MISession *sess, void (*callback)(char *, MIList *))
{
	sess->exec_callback = callback;
}

void
MISessionRegisterStatusCallback(MISession *sess, void (*callback)(char *, MIList *))
{
	sess->status_callback = callback;
}

void
MISessionRegisterNotifyCallback(MISession *sess, void (*callback)(char *, MIList *))
{
	sess->notify_callback = callback;
}

void
MISessionRegisterConsoleCallback(MISession *sess, void (*callback)(char *))
{
	sess->console_callback = callback;
}

void
MISessionRegisterLogCallback(MISession *sess, void (*callback)(char *))
{
	sess->log_callback = callback;
}

void
MISessionRegisterTargetCallback(MISession *sess, void (*callback)(char *))
{
	sess->target_callback = callback;
}

void
MISessionSetGDBPath(MISession *sess, char *path)
{
	if (sess->gdb_path != NULL)
		free(sess->gdb_path);
	sess->gdb_path = strdup(path);
}

/*
 * Send command to debugger.
 */
int
MISessionSendCommand(MISession *sess, MICommand *cmd)
{
	if (sess->pid == -1) {
		MISetError(MI_ERROR_SESSION, "");
		return -1;
	}

	MIListAdd(sess->send_queue, (void *)cmd);
	
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

	if (MISessionDebug) {
		printf("MI: SEND %s", cmd);
		if (cmd[len-1] != '\n')
			printf("\n");
		fflush(stdout);
	}
		
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
#define MI_BUFSIZ 1024 //BUFSIZ
static char *
ReadResponse(int fd)
{
	int				n;
	int				len = 0;
	char *			p;
	static int		res_buf_len = MI_BUFSIZ;
	static char *	res_buf = NULL;
	
	if (res_buf == NULL)
		res_buf = (char *)malloc(MI_BUFSIZ);

	p = res_buf;
	for (;;) {
		n = read(fd, p, MI_BUFSIZ);
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

		if (n < MI_BUFSIZ)
			break;
			
		len += MI_BUFSIZ;

		if (len == res_buf_len) {
			res_buf_len += MI_BUFSIZ;
			res_buf = (char *)realloc(res_buf, res_buf_len);
		}
		
		p = &res_buf[len];
	}
	
	if (n > 0)
		p[n] = '\0';

	if (MISessionDebug) {
		printf("MI: RECV %s", res_buf); 
		if (res_buf[strlen(res_buf)-1] != '\n')
			printf("\n");
		fflush(stdout);
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
MISessionProcessCommandsAndResponses(MISession *sess, fd_set *rfds, fd_set *wfds)
{
	char *		str;
	MIOutput *	output;
	
	if (sess->pid == -1) {
		return;
	}
		
	if (sess->in_fd != -1
		&& !MIListIsEmpty(sess->send_queue)
		&& sess->command == NULL
		&& (wfds == NULL || FD_ISSET(sess->in_fd, wfds))
	)
	{	
		sess->command = (MICommand *)MIListRemoveFirst(sess->send_queue);

#ifdef __gnu_linux__
		/*
		 * NOTE: this hack only works if gdb is started with the '-tty' argument (or
		 * presumably if the 'tty' command is issued.) Without this, the only way to
		 * interrupt a running process seems to be from the command line.
		 */
		if (strcmp(sess->command->command, "-exec-interrupt") == 0) {
			if (MISessionDebug) {
				printf("MI: sending SIGINT to %d\n", sess->pid); 
				fflush(stdout);
			}		
			kill(sess->pid, SIGINT);
		} else if (WriteCommand(sess->in_fd, MICommandToString(sess->command)) < 0) {
			sess->in_fd = -1;
		}
#else /* __gnu_linux__ */
		if (WriteCommand(sess->in_fd, MICommandToString(sess->command)) < 0) {
			sess->in_fd = -1;
		}
#endif /* __gnu_linux */
	}

	if (sess->out_fd != -1 && FD_ISSET(sess->out_fd, rfds)) {
		if ((str = ReadResponse(sess->out_fd)) == NULL) {
			sess->out_fd = -1;
			return;
		}
		
		/*
		 * If there's a command in progress, use the MIOutput saved with the command
		 * to process any output. Otherwise create a new one.
		 */
		if (sess->command != NULL) {
			output = sess->command->output;
		} else {
			output = MIOutputNew();
		}

		MIParse(str, output);
			
		/*
		 * The output can consist of:
		 * 	async oob records that are not necessarily the result of a command
		 * 	stream oob records that always result from a command
		 *	result records from a command
		 * 
		 * Async and stream oob records are processed immediately and removed.
		 * 
		 * If there are result records, then the output *should* have resulted
		 * from the execution of a command. Mark the command as completed an
		 * invoke its callback.
		 * 
		 * The stream oob and result records are freed when the command is freed.
		 */
		 
		if (output->oobs != NULL) {
#ifdef __gnu_linux__
			if (sess->command != NULL && strcmp(sess->command->command, "-exec-interrupt") == 0) {
				sess->command->completed = 1;
			}
#endif /* __gnu_linux__ */	
			DoOOBCallbacks(sess, output->oobs);
		}

		/*
		 * If there's a command in progress, process it.
		 */
		if (sess->command != NULL) {
			if (output->rr != NULL) {
				if (MISessionDebug) {
					printf("MI: PROCESS COMMAND CALLBACK\n");
					fflush(stdout);
				}
				if (sess->command->callback != NULL) {
					sess->command->callback(output->rr, sess->command->cb_data);
				}
				sess->command->completed = 1;
				sess->command = NULL;
			}
		} else {
			MIOutputFree(output);
		}
	}
	
    /* process application output */
    if (sess->pty_fd != -1 && FD_ISSET(sess->pty_fd, rfds))
    {    	
        if ((str = ReadResponse(sess->pty_fd)) != NULL) {
        	if (sess->target_callback != NULL) {
        		sess->target_callback(str);
        	}
        } else {
        	sess->pty_fd = -1;
        }
    }
}

/*
 * Check if the current command has completed.
 */
int
MISessionCommandCompleted(MISession *sess)
{
	return (sess->command == NULL);
}

/*
 * Used to process a result record after a CLI command has been
 * issued if an MIEvent needs to be generated. 
 * 
 * In MI mode, GDB CLI commands append result information (after
 * the "done" result class) to the result record. This function can be 
 * passed to MICommandRegisterCallback() in order to automatically generate 
 * an MIEvent when the CLI command completes.
 * 
 * NOTE: CLI commands DO NOT operate asychronously, so some commands will 
 * block until the command is complete (e.g. "run"). This may cause a user
 * interface to block.
 */
void 
ProcessCLIResultRecord(MIResultRecord *rr, void *data)
{
	MISession *sess = (MISession *)data;
	MIResult *res;
	MIValue *val;

	if (rr->resultClass == MIResultRecordDONE) {
		for (MIListSet(rr->results); (res = (MIResult *)MIListGet(rr->results)); ) {
			if (strcmp(res->variable, "reason") == 0) {
				val = res->value;
				if (val->type == MIValueTypeConst) {
					if (sess->event_callback) {
						sess->event_callback(MIEventCreateStoppedEvent(val->cstring, rr->results));
					}
				}
			}
		}
	} 
}

/*
 * Process OOB callbacks.
 *
 * Async OOB's are removed from oobs list.
 * Stream OOB's are left on the oobs list so they are available
 * as part of the command result.
 */
static void
DoOOBCallbacks(MISession *sess, MIList *oobs)
{
	MIOOBRecord *	oob;
	MIResult *		res;
	MIValue *		val;
	
	for (MIListSet(oobs); (oob = (MIOOBRecord *)MIListGet(oobs)) != NULL; ) {
		switch (oob->type) {
		case MIOOBRecordTypeAsync:
			switch (oob->sub_type) {
			case MIOOBRecordExecAsync:
				if (sess->exec_callback != NULL) {
					sess->exec_callback(oob->class, oob->results);
				}
					
				if (strcmp(oob->class, "stopped") == 0) {
					int seen_reason = 0;
					for (MIListSet(oob->results); (res = (MIResult *)MIListGet(oob->results)); ) {
						if (strcmp(res->variable, "reason") == 0) {
							seen_reason = 1;
							val = res->value;
							if (val->type == MIValueTypeConst) {
								if (sess->event_callback) {
									sess->event_callback(MIEventCreateStoppedEvent(val->cstring, oob->results));
								}
							}
						}
					}
					
					/*
					 * Temporary breakpoints under Linux don't have a "reason". If we receive
					 * a stopped event with no reason, then we created a StoppedEvent anyway
					 */
					if (seen_reason == 0) {
						if (sess->event_callback) {
							sess->event_callback(MIEventCreateStoppedEvent("temporary-breakpoint-hit", oob->results));
						}
					}
				}
				break;
				
			case MIOOBRecordStatusAsync:
				if (sess->status_callback != NULL) {
					sess->status_callback(oob->class, oob->results);
				}
				break;
				
			case MIOOBRecordNotifyAsync:
				if (sess->notify_callback != NULL) {
					sess->notify_callback(oob->class, oob->results);
				}
				break;
			}

			MIListRemove(oobs, (void *)oob);
			MIOOBRecordFree(oob);
			break;

		case MIOOBRecordTypeStream:
			switch (oob->sub_type) {
			case MIOOBRecordConsoleStream:
				if (sess->console_callback != NULL) {
					sess->console_callback(oob->cstring);
				}
				break;
				
			case MIOOBRecordLogStream:
				if (sess->log_callback != NULL) {
					sess->log_callback(oob->cstring);
				}
				break;
				
			case MIOOBRecordTargetStream:
				if (sess->target_callback != NULL) {
					sess->target_callback(oob->cstring);
				}
				break;
			}
			break;
		}
	}
}

void
MISessionGetFds(MISession *sess, int *nfds, fd_set *rfds, fd_set *wfds, fd_set *efds)
{
	int			n = 0;
	
	if (rfds != NULL)
		FD_ZERO(rfds);
		
	if (wfds != NULL)
		FD_ZERO(wfds);
	
	if (efds != NULL)
		FD_ZERO(efds);
	
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
		
        /* check if data on pty */
        if (rfds != NULL && sess->pty_fd != -1) {
        	FD_SET(sess->pty_fd, rfds);
        	if (sess->pty_fd > n)
        		n = sess->pty_fd;
        }

	}
	
	if (nfds != NULL)
		*nfds = n + 1;
}

/*
 * Default progress command if none supplied.
 * 
 * Don't select on write fds. This will almost always return immediately,
 * so the timeout is never used. The end result is a busy wait that
 * consumes gobs of CPU.
 * 
 * Also, we make a copy of sess->select_timeout as some select()
 * functions will modify it.
 */
int
MISessionProgress(MISession *sess)
{
	int				n;
	int				nfds;
	fd_set			rfds;
	struct timeval	tv = sess->select_timeout;
	
	MISessionGetFds(sess, &nfds, &rfds, NULL, NULL);

	for ( ;; ) {
		n = select(nfds, &rfds, NULL, NULL, &tv);
		
		if (n < 0) {
			if (errno == EINTR)
				continue;
				
			MISetError(MI_ERROR_SYSTEM, strerror(errno));
			return -1;
		}		
		break;
	}

	/*
	 * Return if there is nothing to do
	 */
	if (n == 0) {
		if (sess->command != NULL && !sess->command->completed) {
			if (sess->command->timeout != 0) {
				sess->command->timeout -= (sess->select_timeout.tv_sec * 1000 + sess->select_timeout.tv_usec / 1000);
				if (sess->command->timeout <= 0) {
					sess->command->timeout = -1;
					sess->command->completed = 1;
					sess->command = NULL;
					return 0;
				}
			}
		}
		if (MIListIsEmpty(sess->send_queue)) {
			return 0;
		}
	}
	

	MISessionProcessCommandsAndResponses(sess, &rfds, NULL);
	
	return n;
}
