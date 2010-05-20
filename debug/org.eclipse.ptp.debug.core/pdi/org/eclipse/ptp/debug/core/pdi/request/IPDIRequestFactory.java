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
package org.eclipse.ptp.debug.core.pdi.request;

import org.eclipse.ptp.debug.core.TaskSet;
import org.eclipse.ptp.debug.core.pdi.IPDISession;
import org.eclipse.ptp.debug.core.pdi.model.IPDIAddressBreakpoint;
import org.eclipse.ptp.debug.core.pdi.model.IPDIBreakpoint;
import org.eclipse.ptp.debug.core.pdi.model.IPDIFunctionBreakpoint;
import org.eclipse.ptp.debug.core.pdi.model.IPDILineBreakpoint;
import org.eclipse.ptp.debug.core.pdi.model.IPDIWatchpoint;

/**
 * @author clement
 * 
 */
public interface IPDIRequestFactory {
	/**
	 * @param tasks
	 * @param command
	 * @return
	 * @since 4.0
	 */
	public IPDICommandRequest getCommandRequest(TaskSet tasks, String command);

	/**
	 * @param session
	 * @param tasks
	 * @param offset
	 * @param address
	 * @param wordFormat
	 * @param wordSize
	 * @param rows
	 * @param cols
	 * @param asChar
	 * @return
	 * @since 4.0
	 */
	public IPDIDataReadMemoryRequest getDataReadMemoryRequest(IPDISession session, TaskSet tasks, long offset, String address,
			int wordFormat, int wordSize, int rows, int cols, Character asChar);

	/**
	 * @param tasks
	 * @param offset
	 * @param address
	 * @param wordFormat
	 * @param wordSize
	 * @param value
	 * @return
	 * @since 4.0
	 */
	public IPDIDataWriteMemoryRequest getDataWriteMemoryRequest(TaskSet tasks, long offset, String address, int wordFormat,
			int wordSize, String value);

	/**
	 * @param tasks
	 * @param bpt
	 * @param allowUpdate
	 * @return
	 * @since 4.0
	 */
	public IPDIDeleteBreakpointRequest getDeleteBreakpointRequest(TaskSet tasks, IPDIBreakpoint bpt, boolean allowUpdate);

	/**
	 * @param tasks
	 * @param varId
	 * @return
	 * @since 4.0
	 */
	public IPDIDeleteVariableRequest getDeletePartialExpressionRequest(TaskSet tasks, String exprId);

	/**
	 * @param tasks
	 * @param bpt
	 * @return
	 * @since 4.0
	 */
	public IPDIDisableBreakpointRequest getDisableBreakpointRequest(TaskSet tasks, IPDIBreakpoint bpt);

	/**
	 * @param tasks
	 * @param bpt
	 * @return
	 * @since 4.0
	 */
	public IPDIEnableBreakpointRequest getEnableBreakpointRequest(TaskSet tasks, IPDIBreakpoint bpt);

	/**
	 * @param tasks
	 * @param expr
	 * @return
	 * @since 4.0
	 */
	public IPDIEvaluateExpressionRequest getEvaluateExpressionRequest(TaskSet tasks, String expr);

	/**
	 * @param tasks
	 * @return
	 * @since 4.0
	 */
	public IPDIGetInfoThreadsRequest getGetInfoThreadsRequest(TaskSet tasks);

	/**
	 * @param tasks
	 * @param expr
	 * @param exprId
	 * @param listChildren
	 * @return
	 * @since 4.0
	 */
	public IPDIEvaluatePartialExpressionRequest getEvaluatePartialExpressionRequest(TaskSet tasks, String expr, String exprId,
			boolean listChildren);

	/**
	 * @param tasks
	 * @return
	 * @since 4.0
	 */
	public IPDIGetStackInfoDepthRequest getGetStackInfoDepthRequest(TaskSet tasks);

	/**
	 * @param session
	 * @param tasks
	 * @param diff
	 * @param diff2
	 * @return
	 * @since 4.0
	 */
	public IPDIListArgumentsRequest getListArgumentsRequest(TaskSet tasks, int diff, int diff2);

	/**
	 * @param tasks
	 * @return
	 * @since 4.0
	 */
	public IPDIListLocalVariablesRequest getListLocalVariablesRequest(TaskSet tasks);

	/**
	 * @param session
	 * @param tasks
	 * @param name
	 * @return
	 * @since 4.0
	 */
	public IPDIListSignalsRequest getListSignalsRequest(IPDISession session, TaskSet tasks, String name);

	/**
	 * @param session
	 * @param tasks
	 * @return
	 * @since 4.0
	 */
	public IPDIListStackFramesRequest getListStackFramesRequest(IPDISession session, TaskSet tasks);

	/**
	 * @param session
	 * @param tasks
	 * @param low
	 * @param high
	 * @return
	 * @since 4.0
	 */
	public IPDIListStackFramesRequest getListStackFramesRequest(IPDISession session, TaskSet tasks, int low, int high);

	/**
	 * @param tasks
	 * @param passSignal
	 * @return
	 * @since 4.0
	 */
	public IPDIGoRequest getResumeRequest(TaskSet tasks, boolean passSignal);

	/**
	 * @param tasks
	 * @param bpt
	 * @param allowUpdate
	 * @return
	 * @since 4.0
	 */
	public IPDISetAddressBreakpointRequest getSetAddressBreakpointRequest(TaskSet tasks, IPDIAddressBreakpoint bpt,
			boolean allowUpdate);

	/**
	 * @param tasks
	 * @param level
	 * @return
	 * @since 4.0
	 */
	public IPDISetCurrentStackFrameRequest getSetCurrentStackFrameRequest(TaskSet tasks, int level);

	/**
	 * @param tasks
	 * @param bpt
	 * @param allowUpdate
	 * @return
	 * @since 4.0
	 */
	public IPDISetFunctionBreakpointRequest getSetFunctionBreakpointRequest(TaskSet tasks, IPDIFunctionBreakpoint bpt,
			boolean allowUpdate);

	/**
	 * @param tasks
	 * @param bpt
	 * @param allowUpdate
	 * @return
	 * @since 4.0
	 */
	public IPDISetLineBreakpointRequest getSetLineBreakpointRequest(TaskSet tasks, IPDILineBreakpoint bpt, boolean allowUpdate);

	/**
	 * @param session
	 * @param tasks
	 * @param id
	 * @return
	 * @since 4.0
	 */
	public IPDISetThreadSelectRequest getSetThreadSelectRequest(IPDISession session, TaskSet tasks, int id);

	/**
	 * @param tasks
	 * @param bpt
	 * @param allowUpdate
	 * @return
	 * @since 4.0
	 */
	public IPDISetWatchpointRequest getSetWatchpointRequest(TaskSet tasks, IPDIWatchpoint bpt, boolean allowUpdate);

	/**
	 * @param tasks
	 * @param app
	 * @param path
	 * @param dir
	 * @param args
	 * @return
	 * @since 4.0
	 */
	public IPDIStartDebuggerRequest getStartDebuggerRequest(TaskSet tasks, String app, String path, String dir, String[] args);

	/**
	 * @param tasks
	 * @param count
	 * @return
	 * @since 4.0
	 */
	public IPDIStepFinishRequest getStepFinishRequest(TaskSet tasks, int count);

	/**
	 * @param tasks
	 * @param count
	 * @return
	 * @since 4.0
	 */
	public IPDIStepIntoRequest getStepIntoRequest(TaskSet tasks, int count);

	/**
	 * @param tasks
	 * @param count
	 * @return
	 * @since 4.0
	 */
	public IPDIStepOverRequest getStepOverRequest(TaskSet tasks, int count);

	/**
	 * @param tasks
	 * @return
	 * @since 4.0
	 */
	public IPDIStopDebuggerRequest getStopDebuggerRequest(TaskSet tasks);

	/**
	 * @param tasks
	 * @return
	 * @since 4.0
	 */
	public IPDIHaltRequest getSuspendRequest(TaskSet tasks);

	/**
	 * @param tasks
	 * @param sendEvent
	 * @return
	 * @since 4.0
	 */
	public IPDIHaltRequest getSuspendRequest(TaskSet tasks, boolean sendEvent);

	/**
	 * @param tasks
	 * @return
	 * @since 4.0
	 */
	public IPDITerminateRequest getTerminateRequest(TaskSet tasks);

}
