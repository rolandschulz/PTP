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

#include	"list.h"
#include "MISession.h"
#include "MIError.h"

static char *	MISessionGDBPath = NULL;
static List *	MISessionList = NULL;

MISession *
MISessionNew(void)
{
	MISession *	sess = (MISession *)malloc(sizeof(MISession));
	
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
			if (sess->gdb_pid == pid) {
				// TODO notify that gdb has died
				sess->gdb_pid = -1;
			}
		}
	}
}

MISession *
MISessionStartLocal(void)
{
	int			p1[2];
	int			p2[2];
	MISession *	sess = MISessionNew();
	
	if (pipe(p1) < 0 || pipe(p2) < 0) {
		MISetError(MI_ERROR_SYSTEM, strerror(errno));
		MISessionFree(sess);
		return NULL;
	}
	
	signal(SIGCHLD, MISessionHandleChild);
	
	switch (sess->gdb_pid = fork())
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

	sess->gdb_fd[0] = p1[0];
	sess->gdb_fd[1] = p2[1];
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