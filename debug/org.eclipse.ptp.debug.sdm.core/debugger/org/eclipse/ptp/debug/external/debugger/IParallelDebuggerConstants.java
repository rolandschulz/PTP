/*******************************************************************************
 * Copyright (c) 2005 The Regents of the University of California. 
 * This material was produced under U.S. Government contract W-7405-ENG-36 
 * for Los Alamos National Laboratory, which is operated by the University 
 * of California for the U.S. Department of Energy. The U.S. Government has 
 * rights to use, reproduce, and distribute this software. NEITHER THE 
 * GOVERNMENT NOR THE UNIVERSITY MAKES ANY WARRANTY, EXPRESS OR IMPLIED, OR 
 * ASSUMES ANY LIABILITY FOR THE USE OF THIS SOFTWARE. If software is modified 
 * to produce derivative works, such modified software should be clearly marked, 
 * so as not to confuse it with the version available from LANL.
 * 
 * Additionally, this program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * LA-CC 04-115
 *******************************************************************************/
package org.eclipse.ptp.debug.external.debugger;
/**
 * @author Clement chu
 * 
 */
public interface IParallelDebuggerConstants {
	public static final int DBGERR_NOTIMP			=	1;
	public static final int DBGERR_PROXY_TERM		=	2;
	public static final int DBGERR_PROXY_PROTO		=	3;
	public static final int DBGERR_DEBUGGER			=	4;
	public static final int DBGERR_NOBACKEND		=	5;
	public static final int DBGERR_INPROGRESS		=	6;
	public static final int DBGERR_CBCREATE			=	7;
	public static final int DBGERR_NOSESSION		=	8;
	public static final int DBGERR_SESSION			=	9;
	public static final int DBGERR_NOLINE			=	10;
	public static final int DBGERR_NOFUNC			=	11;
	public static final int DBGERR_NOFILE			=	12;
	public static final int DBGERR_NOBP				=	13;
	public static final int DBGERR_NOSYM			=	14;
	public static final int DBGERR_NOMEM			=	15;
	public static final int DBGERR_CANTRUN			=	16;
	public static final int DBGERR_INVOKE			=	17;
	public static final int DBGERR_ISRUNNING		=	18;
	public static final int DBGERR_NOTRUN			=	19;
	public static final int DBGERR_FIRSTFRAME		=	20;
	public static final int DBGERR_LASTFRAME		=	21;
	public static final int DBGERR_BADBPARG			=	22;
	public static final int DBGERR_REGEX			=	23;
	public static final int DBGERR_NOSTACK			=	24;
	public static final int DBGERR_OUTOFRANGE		=	25;
	public static final int DBGERR_NOFILEDIR		=	26;
	public static final int DBGERR_NOSYMS			=	27;
	public static final int DBGERR_TEMP				=	28;
	public static final int DBGERR_PIPE				=	29;
	public static final int DBGERR_FORK				=	30;
	public static final int DBGERR_SYSTEM			=	31;
	public static final int DBGERR_NOTEXEC			=	32;
	public static final int DBGERR_CHDIR			=	33;
	public static final int DBGERR_SOURCE			=	34;
	public static final int DBGERR_SETVAR			=	35;
	public static final int DBGERR_PROCSET			=	36;
	public static final int DBGERR_UNKNOWN			=	37;
}
