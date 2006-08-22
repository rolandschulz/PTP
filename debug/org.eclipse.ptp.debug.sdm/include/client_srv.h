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
 
#ifndef _CLIENT_SRV_H_
#define _CLIENT_SRV_H_

#include "bitset.h"
#include "dbg_event.h"

void	ClntSvrRegisterCompletionCallback(void (*)(bitset *, char *, void *));
void	ClntSvrRegisterLocalCmdCallback(void (*func)(char *, void *), void *data);
void	ClntSvrRegisterInterruptCmdCallback(void (*func)(void *), void *data);
void	ClntSvrInit(int, int);
int		ClntSvrSendCommand(bitset *, int, char *, char *);
int		ClntSvrSendInterrupt(bitset *);
void	ClntSvrInsertMessage(char *);
void	ClntSvrSendReply(bitset *, char *, void *);
int		ClntSvrProgressCmds(void);
void	ClntSvrFinish(void);

#endif /* _CLIENT_SRV_H_ */
