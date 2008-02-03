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

import org.eclipse.ptp.core.util.BitList;
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
	 * @param expr
	 * @return
	 */
	public IPDIGetAIFRequest getAIFRequest(BitList tasks, String expr);
	
	/**
	 * @param tasks
	 * @param command
	 * @return
	 */
	public IPDICommandRequest getCommandRequest(BitList tasks, String command);
	
	/**
	 * @param tasks
	 * @param expr
	 * @return
	 */
	public IPDIDataEvaluateExpressionRequest getDataEvaluateExpresionRequest(BitList tasks, String expr);
	
	/**
	 * @param tasks
	 * @param expr
	 * @return
	 */
	public IPDIDataEvaluateExpressionRequest getDataEvaluateExpressionRequest(BitList tasks, String expr);
	
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
	 */
	public IPDIDataReadMemoryRequest getDataReadMemoryRequest(IPDISession session, BitList tasks, long offset, String address, int wordFormat, int wordSize, int rows, int cols, Character asChar);
	
	/**
	 * @param tasks
	 * @param offset
	 * @param address
	 * @param wordFormat
	 * @param wordSize
	 * @param value
	 * @return
	 */
	public IPDIDataWriteMemoryRequest getDataWriteMemoryRequest(BitList tasks, long offset, String address, int wordFormat, int wordSize, String value);
	
	/**
	 * @param tasks
	 * @param bpt
	 * @param allowUpdate
	 * @return
	 */
	public IPDIDeleteBreakpointRequest getDeleteBreakpointRequest(BitList tasks, IPDIBreakpoint bpt, boolean allowUpdate);
	
	/**
	 * @param tasks
	 * @param varid
	 * @return
	 */
	public IPDIDeleteVariableRequest getDeleteVariableRequest(BitList tasks, String varid);
	
	/**
	 * @param tasks
	 * @param bpt
	 * @return
	 */
	public IPDIDisableBreakpointRequest getDisableBreakpointRequest(BitList tasks, IPDIBreakpoint bpt);
	
	/**
	 * @param tasks
	 * @param bpt
	 * @return
	 */
	public IPDIEnableBreakpointRequest getEnableBreakpointRequest(BitList tasks, IPDIBreakpoint bpt);
	
	/**
	 * @param tasks
	 * @return
	 */
	public IPDIGetInfoThreadsRequest getGetInfoThreadsRequest(BitList tasks);
	
	/**
	 * @param tasks
	 * @param expr
	 * @param varid
	 * @return
	 */
	public IPDIGetPartialAIFRequest getGetPartialAIFRequest(BitList tasks, String expr, String varid);
	
	/**
	 * @param tasks
	 * @param expr
	 * @param varid
	 * @param listChildren
	 * @return
	 */
	public IPDIGetPartialAIFRequest getGetPartialAIFRequest(BitList tasks, String expr, String varid, boolean listChildren);

	/**
	 * @param tasks
	 * @return
	 */
	public IPDIGetStackInfoDepthRequest getGetStackInfoDepthRequest(BitList tasks);
	
	/**
	 * @param session
	 * @param tasks
	 * @param diff
	 * @param diff2
	 * @return
	 */
	public IPDIListArgumentsRequest getListArgumentsRequest(BitList tasks, int diff, int diff2);
	
	/**
	 * @param tasks
	 * @return
	 */
	public IPDIListLocalVariablesRequest getListLocalVariablesRequest(BitList tasks);
	
	/**
	 * @param session
	 * @param tasks
	 * @param name
	 * @return
	 */
	public IPDIListSignalsRequest getListSignalsRequest(IPDISession session, BitList tasks, String name);
	
	/**
	 * @param session
	 * @param tasks
	 * @return
	 */
	public IPDIListStackFramesRequest getListStackFramesRequest(IPDISession session, BitList tasks);
		
	/**
	 * @param session
	 * @param tasks
	 * @param low
	 * @param high
	 * @return
	 */
	public IPDIListStackFramesRequest getListStackFramesRequest(IPDISession session, BitList tasks, int low, int high);
	
	/**
	 * @param tasks
	 * @param passSignal
	 * @return
	 */
	public IPDIGoRequest getResumeRequest(BitList tasks, boolean passSignal);
	
	/**
	 * @param tasks
	 * @param bpt
	 * @param allowUpdate
	 * @return
	 */
	public IPDISetAddressBreakpointRequest getSetAddressBreakpointRequest(BitList tasks, IPDIAddressBreakpoint bpt, boolean allowUpdate);
	
	/**
	 * @param tasks
	 * @param level
	 * @return
	 */
	public IPDISetCurrentStackFrameRequest getSetCurrentStackFrameRequest(BitList tasks, int level);
	
	/**
	 * @param tasks
	 * @param bpt
	 * @param allowUpdate
	 * @return
	 */
	public IPDISetFunctionBreakpointRequest getSetFunctionBreakpointRequest(BitList tasks, IPDIFunctionBreakpoint bpt, boolean allowUpdate);
	
	/**
	 * @param tasks
	 * @param bpt
	 * @param allowUpdate
	 * @return
	 */
	public IPDISetLineBreakpointRequest getSetLineBreakpointRequest(BitList tasks, IPDILineBreakpoint bpt, boolean allowUpdate);
	
	/**
	 * @param session
	 * @param tasks
	 * @param id
	 * @return
	 */
	public IPDISetThreadSelectRequest getSetThreadSelectRequest(IPDISession session, BitList tasks, int id);
	
	/**
	 * @param tasks
	 * @param bpt
	 * @param allowUpdate
	 * @return
	 */
	public IPDISetWatchpointRequest getSetWatchpointRequest(BitList tasks, IPDIWatchpoint bpt, boolean allowUpdate);
	
	/**
	 * @param tasks
	 * @param app
	 * @param path
	 * @param dir
	 * @param args
	 * @return
	 */
	public IPDIStartDebuggerRequest getStartDebuggerRequest(BitList tasks, String app, String path, String dir, String[] args);
	
	/**
	 * @param tasks
	 * @param count
	 * @return
	 */
	public IPDIStepFinishRequest getStepFinishRequest(BitList tasks, int count);
	
	/**
	 * @param tasks
	 * @param count
	 * @return
	 */
	public IPDIStepIntoRequest getStepIntoRequest(BitList tasks, int count);
	
	/**
	 * @param tasks
	 * @param count
	 * @return
	 */
	public IPDIStepOverRequest getStepOverRequest(BitList tasks, int count);
	
	/**
	 * @param tasks
	 * @return
	 */
	public IPDIStopDebuggerRequest getStopDebuggerRequest(BitList tasks);

	/**
	 * @param tasks
	 * @return
	 */
	public IPDIHaltRequest getSuspendRequest(BitList tasks);

	/**
	 * @param tasks
	 * @param sendEvent
	 * @return
	 */
	public IPDIHaltRequest getSuspendRequest(BitList tasks, boolean sendEvent);

	/**
	 * @param tasks
	 * @return
	 */
	public IPDITerminateRequest getTerminateRequest(BitList tasks);
	
}

