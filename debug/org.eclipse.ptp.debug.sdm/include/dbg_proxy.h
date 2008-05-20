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
 
#ifndef _DBG_PROXY_H_
#define _DBG_PROXY_H_

#define DBG_CMD_BASE					0

#define DBG_QUIT_CMD					DBG_CMD_BASE+0
#define DBG_STARTSESSION_CMD			DBG_CMD_BASE+1
#define DBG_SETLINEBREAKPOINT_CMD		DBG_CMD_BASE+2
#define DBG_SETFUNCBREAKPOINT_CMD		DBG_CMD_BASE+3
#define DBG_DELETEBREAKPOINT_CMD		DBG_CMD_BASE+4
#define DBG_ENABLEBREAKPOINT_CMD		DBG_CMD_BASE+5
#define DBG_DISABLEBREAKPOINT_CMD		DBG_CMD_BASE+6
#define DBG_CONDITIONBREAKPOINT_CMD		DBG_CMD_BASE+7
#define DBG_BREAKPOINTAFTER_CMD			DBG_CMD_BASE+8
#define DBG_SETWATCHPOINT_CMD			DBG_CMD_BASE+9
#define DBG_GO_CMD						DBG_CMD_BASE+10
#define DBG_STEP_CMD					DBG_CMD_BASE+11
#define DBG_TERMINATE_CMD				DBG_CMD_BASE+12
#define DBG_SUSPEND_CMD					DBG_CMD_BASE+13
#define DBG_LISTSTACKFRAMES_CMD			DBG_CMD_BASE+14
#define DBG_SETCURRENTSTACKFRAME_CMD	DBG_CMD_BASE+15
#define DBG_EVALUATEEXPRESSION_CMD		DBG_CMD_BASE+16
#define DBG_GETTYPE_CMD					DBG_CMD_BASE+17
#define DBG_LISTLOCALVARIABLES_CMD		DBG_CMD_BASE+18
#define DBG_LISTARGUMENTS_CMD			DBG_CMD_BASE+19
#define DBG_LISTGLOBALVARIABLES_CMD		DBG_CMD_BASE+20
#define DBG_LISTINFOTHREADS_CMD			DBG_CMD_BASE+21
#define DBG_SETTHREADSELECT_CMD			DBG_CMD_BASE+22
#define DBG_STACKINFODEPTH_CMD			DBG_CMD_BASE+23
#define DBG_DATAREADMEMORY_CMD			DBG_CMD_BASE+24
#define DBG_DATAWRITEMEMORY_CMD			DBG_CMD_BASE+25
#define DBG_LISTSIGNALS_CMD				DBG_CMD_BASE+26
#define DBG_SIGNALINFO_CMD				DBG_CMD_BASE+27 /* deprecated */
#define DBG_CLIHANDLE_CMD				DBG_CMD_BASE+28
#define DBG_DATAEVALUATEEXPRESSION_CMD	DBG_CMD_BASE+29 /* deprecated */
#define DBG_GETPARTIALAIF_CMD			DBG_CMD_BASE+30
#define DBG_VARIABLEDELETE_CMD			DBG_CMD_BASE+31

#endif /* _DBG_PROXY_H_ */
