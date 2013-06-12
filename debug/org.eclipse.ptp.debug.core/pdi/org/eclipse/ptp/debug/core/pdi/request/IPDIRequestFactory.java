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
 * Factory for creating requests
 * 
 * @author clement
 * 
 */
public interface IPDIRequestFactory {
	/**
	 * Create command request
	 * 
	 * @param tasks
	 * @param command
	 * @return
	 * @since 4.0
	 */
	public IPDICommandRequest getCommandRequest(TaskSet tasks, String command);

	/**
	 * Create a data read memory request
	 * 
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
	 * Create a data write memory request
	 * 
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
	 * Create a delete breakpoint request
	 * 
	 * @param tasks
	 * @param bpt
	 * @param allowUpdate
	 * @return
	 * @since 4.0
	 */
	public IPDIDeleteBreakpointRequest getDeleteBreakpointRequest(TaskSet tasks, IPDIBreakpoint bpt, boolean allowUpdate);

	/**
	 * Create a delete partial expression request
	 * 
	 * @param tasks
	 * @param varId
	 * @return
	 * @since 4.0
	 */
	public IPDIDeleteVariableRequest getDeletePartialExpressionRequest(TaskSet tasks, String exprId);

	/**
	 * Create a disable breakpoint request
	 * 
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
	 * Create an evaluate expression request
	 * 
	 * @param tasks
	 * @param expr
	 * @return
	 * @since 4.0
	 */
	public IPDIEvaluateExpressionRequest getEvaluateExpressionRequest(TaskSet tasks, String expr);

	/**
	 * Create a get info threads request
	 * 
	 * @param tasks
	 * @return
	 * @since 4.0
	 */
	public IPDIGetInfoThreadsRequest getGetInfoThreadsRequest(TaskSet tasks);

	/**
	 * Create an evaluate partial expression request
	 * 
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
	 * Create a get stack info depth request
	 * 
	 * @param tasks
	 * @return
	 * @since 4.0
	 */
	public IPDIGetStackInfoDepthRequest getGetStackInfoDepthRequest(TaskSet tasks);

	/**
	 * Create a list arguments request
	 * 
	 * @param session
	 * @param tasks
	 * @param diff
	 * @param diff2
	 * @return
	 * @since 4.0
	 */
	public IPDIListArgumentsRequest getListArgumentsRequest(TaskSet tasks, int diff, int diff2);

	/**
	 * Create a list local variables request
	 * 
	 * @param tasks
	 * @return
	 * @since 4.0
	 */
	public IPDIListLocalVariablesRequest getListLocalVariablesRequest(TaskSet tasks);

	/**
	 * Create a list signals request
	 * 
	 * @param session
	 * @param tasks
	 * @param name
	 * @return
	 * @since 4.0
	 */
	public IPDIListSignalsRequest getListSignalsRequest(IPDISession session, TaskSet tasks, String name);

	/**
	 * Create a list stack frames request
	 * 
	 * @param session
	 * @param tasks
	 * @return
	 * @since 4.0
	 */
	public IPDIListStackFramesRequest getListStackFramesRequest(IPDISession session, TaskSet tasks);

	/**
	 * Create a list stack frames request
	 * 
	 * @param session
	 * @param tasks
	 * @param low
	 * @param high
	 * @return
	 * @since 4.0
	 */
	public IPDIListStackFramesRequest getListStackFramesRequest(IPDISession session, TaskSet tasks, int low, int high);

	/**
	 * Create a resume request
	 * 
	 * @param tasks
	 * @param passSignal
	 * @return
	 * @since 4.0
	 */
	public IPDIGoRequest getResumeRequest(TaskSet tasks, boolean passSignal);

	/**
	 * Create a set address breakpoint request
	 * 
	 * @param tasks
	 * @param bpt
	 * @param allowUpdate
	 * @return
	 * @since 4.0
	 */
	public IPDISetAddressBreakpointRequest getSetAddressBreakpointRequest(TaskSet tasks, IPDIAddressBreakpoint bpt,
			boolean allowUpdate);

	/**
	 * Create a request to set the current stack frame
	 * 
	 * @param tasks
	 * @param level
	 * @return
	 * @since 4.0
	 */
	public IPDISetCurrentStackFrameRequest getSetCurrentStackFrameRequest(TaskSet tasks, int level);

	/**
	 * Create a request to set a function breakpoint
	 * 
	 * @param tasks
	 * @param bpt
	 * @param allowUpdate
	 * @return
	 * @since 4.0
	 */
	public IPDISetFunctionBreakpointRequest getSetFunctionBreakpointRequest(TaskSet tasks, IPDIFunctionBreakpoint bpt,
			boolean allowUpdate);

	/**
	 * Create a request to set a line breakpoint
	 * 
	 * @param tasks
	 * @param bpt
	 * @param allowUpdate
	 * @return
	 * @since 4.0
	 */
	public IPDISetLineBreakpointRequest getSetLineBreakpointRequest(TaskSet tasks, IPDILineBreakpoint bpt, boolean allowUpdate);

	/**
	 * Create a request to set the current thread
	 * 
	 * @param session
	 * @param tasks
	 * @param id
	 * @return
	 * @since 4.0
	 */
	public IPDISetThreadSelectRequest getSetThreadSelectRequest(IPDISession session, TaskSet tasks, int id);

	/**
	 * Create a request to set a watchpoint (expression breakpoint)
	 * 
	 * @param tasks
	 * @param bpt
	 * @param allowUpdate
	 * @return
	 * @since 4.0
	 */
	public IPDISetWatchpointRequest getSetWatchpointRequest(TaskSet tasks, IPDIWatchpoint bpt, boolean allowUpdate);

	/**
	 * Create a request to start the debugger session
	 * 
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
	 * Create a request to step to the end of a function
	 * 
	 * @param tasks
	 * @param count
	 * @return
	 * @since 4.0
	 */
	public IPDIStepFinishRequest getStepFinishRequest(TaskSet tasks, int count);

	/**
	 * Create a request to step into a function
	 * 
	 * @param tasks
	 * @param count
	 * @return
	 * @since 4.0
	 */
	public IPDIStepIntoRequest getStepIntoRequest(TaskSet tasks, int count);

	/**
	 * Create a request to set over a function
	 * 
	 * @param tasks
	 * @param count
	 * @return
	 * @since 4.0
	 */
	public IPDIStepOverRequest getStepOverRequest(TaskSet tasks, int count);

	/**
	 * Create a request to stop the debug session
	 * 
	 * @param tasks
	 * @return
	 * @since 4.0
	 */
	public IPDIStopDebuggerRequest getStopDebuggerRequest(TaskSet tasks);

	/**
	 * Create a request to suspend a running target
	 * 
	 * @param tasks
	 * @return
	 * @since 4.0
	 */
	public IPDIHaltRequest getSuspendRequest(TaskSet tasks);

	/**
	 * Create a request to suspend a running target
	 * 
	 * @param tasks
	 * @param sendEvent
	 * @return
	 * @since 4.0
	 */
	public IPDIHaltRequest getSuspendRequest(TaskSet tasks, boolean sendEvent);

	/**
	 * Create a request to terminate the target
	 * 
	 * @param tasks
	 * @return
	 * @since 4.0
	 */
	public IPDITerminateRequest getTerminateRequest(TaskSet tasks);

}
