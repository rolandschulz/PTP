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
 
#ifndef _DBG_ERROR_H_
#define _DBG_ERROR_H_

#define DBGERR_NOTIMP			1
#define DBGERR_PROXY_TERM		2
#define DBGERR_PROXY_PROTO		3
#define DBGERR_DEBUGGER			4
#define DBGERR_NOBACKEND		5
#define DBGERR_INPROGRESS		6
#define DBGERR_CBCREATE			7
#define DBGERR_NOSESSION		8
#define DBGERR_SESSION			9
#define DBGERR_NOLINE			10
#define DBGERR_NOFUNC			11
#define DBGERR_NOFILE			12
#define DBGERR_NOBP				13
#define DBGERR_NOSYM			14
#define DBGERR_NOMEM			15
#define DBGERR_CANTRUN			16
#define DBGERR_INVOKE			17
#define DBGERR_ISRUNNING		18
#define DBGERR_NOTRUN			19
#define DBGERR_FIRSTFRAME		20
#define DBGERR_LASTFRAME		21
#define DBGERR_BADBPARG			22
#define DBGERR_REGEX			23
#define DBGERR_NOSTACK			24
#define DBGERR_OUTOFRANGE		25
#define DBGERR_NOFILEDIR		26
#define DBGERR_NOSYMS			27
#define DBGERR_TEMP				28
#define DBGERR_PIPE				29
#define DBGERR_FORK				30
#define DBGERR_SYSTEM			31
#define DBGERR_NOTEXEC			32
#define DBGERR_CHDIR			33
#define DBGERR_SOURCE			34
#define DBGERR_SETVAR			35
#define DBGERR_PROCSET			36
#define DBGERR_UNKNOWN			37
#define DBGERR_UNKNOWN_TYPE		38
#define DBGERR_UNKNOWN_VARIABLE	39
/* REMEBER TO ADD NEW ERRORS TO utils/error.c! */
/*
 * Error Handling
 */
void	DbgSetError(int, char *);
int		DbgGetError(void);
char *	DbgGetErrorStr(void);

#endif /* _DBG_ERROR_H_ */
