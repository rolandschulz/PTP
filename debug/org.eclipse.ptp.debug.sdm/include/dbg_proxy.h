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

#define DBG_STARTSESSION_CMD			"INI"
#define DBG_SETLINEBREAKPOINT_CMD		"SLB"
#define DBG_SETFUNCBREAKPOINT_CMD		"SFB"
#define DBG_DELETEBREAKPOINT_CMD		"DBP"
#define DBG_ENABLEBREAKPOINT_CMD		"EAB"
#define DBG_DISABLEBREAKPOINT_CMD		"DAB"
#define DBG_CONDITIONBREAKPOINT_CMD		"CBP"
#define DBG_BREAKPOINTAFTER_CMD			"BPA"
#define DBG_SETWATCHPOINT_CMD			"SWP"
#define DBG_GO_CMD						"GOP"
#define DBG_STEP_CMD					"STP"
#define DBG_TERMINATE_CMD				"TRM"
#define DBG_SUSPEND_CMD					"HLT"
#define DBG_LISTSTACKFRAMES_CMD			"LSF"
#define DBG_SETCURRENTSTACKFRAME_CMD	"SCS"
#define DBG_EVALUATEEXPRESSION_CMD		"EEX"
#define DBG_GETTYPE_CMD					"TYP"
#define DBG_LISTLOCALVARIABLES_CMD		"LLV"
#define DBG_LISTARGUMENTS_CMD			"LAR"
#define DBG_LISTGLOBALVARIABLES_CMD		"LGV"
#define DBG_LISTINFOTHREADS_CMD			"ITH"
#define DBG_SETTHREADSELECT_CMD			"THS"
#define DBG_STACKINFODEPTH_CMD			"SID"
#define DBG_DATAREADMEMORY_CMD			"DRM"
#define DBG_DATAWRITEMEMORY_CMD			"DWM"
#define DBG_LISTSIGNALS_CMD				"LSI"
#define DBG_SIGNALINFO_CMD				"SIG"
#define DBG_CLIHANDLE_CMD				"CHL"
#define DBG_QUIT_CMD					"QUI"
#define DBG_DATAEVALUATEEXPRESSION_CMD	"DEE"
#define DBG_GETPARTIALAIF_CMD			"GPA"
#define DBG_VARIABLEDELETE_CMD			"VDE"

#define DBG_STARTSESSION_FMT			"%s %ld \"%s\" \"%s\""
#define DBG_SETLINEBREAKPOINT_FMT		"%s %d \"%s\" %d"
#define DBG_SETFUNCBREAKPOINT_FMT		"%s %d \"%s\" \"%s\""
#define DBG_DELETEBREAKPOINT_FMT		"%s %d"
#define DBG_ENABLEBREAKPOINT_FMT		"%s %d"
#define DBG_DISABLEBREAKPOINT_FMT		"%s %d"
#define DBG_CONDITIONBREAKPOINT_FMT		"%s %d \"%s\""
#define DBG_BREAKPOINTAFTER_FMT			"%s %d %d"
#define DBG_SETWATCHPOINT_FMT			"%s %d \"%s\" %d %d \"%s\" %d"
#define DBG_GO_FMT						"%s"
#define DBG_STEP_FMT					"%s %d %d"
#define DBG_TERMINATE_FMT				"%s"
#define DBG_SUSPEND_FMT					"%s"
#define DBG_LISTSTACKFRAMES_FMT			"%s %d %d"
#define DBG_SETCURRENTSTACKFRAME_FMT	"%s %d"
#define DBG_EVALUATEEXPRESSION_FMT		"%s \"%s\""
#define DBG_GETTYPE_FMT					"%s \"%s\""
#define DBG_LISTLOCALVARIABLES_FMT		"%s"
#define DBG_LISTARGUMENTS_FMT			"%s %d %d"
#define DBG_LISTGLOBALVARIABLES_FMT		"%s"
#define DBG_LISTINFOTHREADS_FMT			"%s"
#define DBG_SETTHREADSELECT_FMT			"%s %d"
#define DBG_STACKINFODEPTH_FMT			"%s"
#define DBG_DATAREADMEMORY_FMT			"%s %ld \"%s\" \"%s\" %d %d %d \"%s\""
#define DBG_DATAWRITEMEMORY_FMT			"%s %ld \"%s\" \"%s\" %d \"%s\""
#define DBG_LISTSIGNALS_FMT				"%s \"%s\""
#define DBG_SIGNALINFO_FMT				"%s \"%s\""
#define DBG_CLIHANDLE_FMT				"%s \"%s\""
#define DBG_DATAEVALUATEEXPRESSION_FMT	"%s \"%s\""
#define DBG_GETPARTIALAIF_FMT			"%s \"%s\" \"%s\" %d %d"
#define DBG_VARIABLEDELETE_FMT			"%s \"%s\""

#endif /* _DBG_PROXY_H_ */
