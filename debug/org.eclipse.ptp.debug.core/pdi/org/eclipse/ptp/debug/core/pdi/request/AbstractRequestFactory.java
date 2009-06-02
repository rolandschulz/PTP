/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.debug.core.pdi.request;

import org.eclipse.ptp.core.util.BitList;
import org.eclipse.ptp.debug.core.pdi.model.IPDIAddressBreakpoint;
import org.eclipse.ptp.debug.core.pdi.model.IPDIBreakpoint;
import org.eclipse.ptp.debug.core.pdi.model.IPDIFunctionBreakpoint;
import org.eclipse.ptp.debug.core.pdi.model.IPDILineBreakpoint;
import org.eclipse.ptp.debug.core.pdi.model.IPDIWatchpoint;
import org.eclipse.ptp.debug.internal.core.pdi.request.CommandRequest;
import org.eclipse.ptp.debug.internal.core.pdi.request.DataWriteMemoryRequest;
import org.eclipse.ptp.debug.internal.core.pdi.request.DeleteBreakpointRequest;
import org.eclipse.ptp.debug.internal.core.pdi.request.DeletePartialExpressionRequest;
import org.eclipse.ptp.debug.internal.core.pdi.request.DisableBreakpointRequest;
import org.eclipse.ptp.debug.internal.core.pdi.request.EnableBreakpointRequest;
import org.eclipse.ptp.debug.internal.core.pdi.request.ResumeRequest;
import org.eclipse.ptp.debug.internal.core.pdi.request.SetAddressBreakpointRequest;
import org.eclipse.ptp.debug.internal.core.pdi.request.SetFunctionBreakpointRequest;
import org.eclipse.ptp.debug.internal.core.pdi.request.SetLineBreakpointRequest;
import org.eclipse.ptp.debug.internal.core.pdi.request.SetWatchpointRequest;
import org.eclipse.ptp.debug.internal.core.pdi.request.StartDebuggerRequest;
import org.eclipse.ptp.debug.internal.core.pdi.request.StepFinishRequest;
import org.eclipse.ptp.debug.internal.core.pdi.request.StepIntoRequest;
import org.eclipse.ptp.debug.internal.core.pdi.request.StepOverRequest;
import org.eclipse.ptp.debug.internal.core.pdi.request.StopDebuggerRequest;
import org.eclipse.ptp.debug.internal.core.pdi.request.SuspendRequest;
import org.eclipse.ptp.debug.internal.core.pdi.request.TerminateRequest;

/**
 * @author clement
 *
 */
/**
 * @author greg
 *
 */
public abstract class AbstractRequestFactory implements IPDIRequestFactory {
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.request.IPDIRequestFactory#getCommandRequest(org.eclipse.ptp.core.util.BitList, java.lang.String)
	 */
	public IPDICommandRequest getCommandRequest(BitList tasks, String command) {
		return new CommandRequest(tasks, command);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.request.IPDIRequestFactory#getDataWriteMemoryRequest(org.eclipse.ptp.core.util.BitList, long, java.lang.String, int, int, java.lang.String)
	 */
	public IPDIDataWriteMemoryRequest getDataWriteMemoryRequest(BitList tasks,
			long offset, String address, int wordFormat, int wordSize,
			String value) {
		return new DataWriteMemoryRequest(tasks, offset, address, wordFormat, wordSize, value);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.request.IPDIRequestFactory#getDeleteBreakpointRequest(org.eclipse.ptp.core.util.BitList, org.eclipse.ptp.debug.core.pdi.model.IPDIBreakpoint, boolean)
	 */
	public IPDIDeleteBreakpointRequest getDeleteBreakpointRequest(
			BitList tasks, IPDIBreakpoint bpt, boolean allowUpdate) {
		return new DeleteBreakpointRequest(tasks, bpt, allowUpdate);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.request.IPDIRequestFactory#getDeletePartialExpressionRequest(org.eclipse.ptp.core.util.BitList, java.lang.String)
	 */
	public IPDIDeleteVariableRequest getDeletePartialExpressionRequest(BitList tasks,
			String exprId) {
		return new DeletePartialExpressionRequest(tasks, exprId);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.request.IPDIRequestFactory#getDisableBreakpointRequest(org.eclipse.ptp.core.util.BitList, org.eclipse.ptp.debug.core.pdi.model.IPDIBreakpoint)
	 */
	public IPDIDisableBreakpointRequest getDisableBreakpointRequest(
			BitList tasks, IPDIBreakpoint bpt) {
		return new DisableBreakpointRequest(tasks, bpt);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.request.IPDIRequestFactory#getEnableBreakpointRequest(org.eclipse.ptp.core.util.BitList, org.eclipse.ptp.debug.core.pdi.model.IPDIBreakpoint)
	 */
	public IPDIEnableBreakpointRequest getEnableBreakpointRequest(
			BitList tasks, IPDIBreakpoint bpt) {
		return new EnableBreakpointRequest(tasks, bpt);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.request.IPDIRequestFactory#getResumeRequest(org.eclipse.ptp.core.util.BitList, boolean)
	 */
	public IPDIGoRequest getResumeRequest(BitList tasks, boolean passSignal) {
		return new ResumeRequest(tasks, passSignal);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.request.IPDIRequestFactory#getSetAddressBreakpointRequest(org.eclipse.ptp.core.util.BitList, org.eclipse.ptp.debug.core.pdi.model.IPDIAddressBreakpoint, boolean)
	 */
	public IPDISetAddressBreakpointRequest getSetAddressBreakpointRequest(
			BitList tasks, IPDIAddressBreakpoint bpt, boolean allowUpdate) {
		return new SetAddressBreakpointRequest(tasks, bpt, allowUpdate);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.request.IPDIRequestFactory#getSetFunctionBreakpointRequest(org.eclipse.ptp.core.util.BitList, org.eclipse.ptp.debug.core.pdi.model.IPDIFunctionBreakpoint, boolean)
	 */
	public IPDISetFunctionBreakpointRequest getSetFunctionBreakpointRequest(
			BitList tasks, IPDIFunctionBreakpoint bpt, boolean allowUpdate) {
		return new SetFunctionBreakpointRequest(tasks, bpt, allowUpdate);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.request.IPDIRequestFactory#getSetLineBreakpointRequest(org.eclipse.ptp.core.util.BitList, org.eclipse.ptp.debug.core.pdi.model.IPDILineBreakpoint, boolean)
	 */
	public IPDISetLineBreakpointRequest getSetLineBreakpointRequest(
			BitList tasks, IPDILineBreakpoint bpt, boolean allowUpdate) {
		return new SetLineBreakpointRequest(tasks, bpt, allowUpdate);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.request.IPDIRequestFactory#getSetWatchpointRequest(org.eclipse.ptp.core.util.BitList, org.eclipse.ptp.debug.core.pdi.model.IPDIWatchpoint, boolean)
	 */
	public IPDISetWatchpointRequest getSetWatchpointRequest(BitList tasks,
			IPDIWatchpoint bpt, boolean allowUpdate) {
		return new SetWatchpointRequest(tasks, bpt, allowUpdate);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.request.IPDIRequestFactory#getStartDebuggerRequest(org.eclipse.ptp.core.util.BitList, java.lang.String, java.lang.String, java.lang.String, java.lang.String[])
	 */
	public IPDIStartDebuggerRequest getStartDebuggerRequest(BitList tasks,
			String app, String path, String dir, String[] args) {
		return new StartDebuggerRequest(tasks, app, path, dir, args);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.request.IPDIRequestFactory#getStepFinishRequest(org.eclipse.ptp.core.util.BitList, int)
	 */
	public IPDIStepFinishRequest getStepFinishRequest(BitList tasks, int count) {
		return new StepFinishRequest(tasks, count);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.request.IPDIRequestFactory#getStepIntoRequest(org.eclipse.ptp.core.util.BitList, int)
	 */
	public IPDIStepIntoRequest getStepIntoRequest(BitList tasks, int count) {
		return new StepIntoRequest(tasks, count);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.request.IPDIRequestFactory#getStepOverRequest(org.eclipse.ptp.core.util.BitList, int)
	 */
	public IPDIStepOverRequest getStepOverRequest(BitList tasks, int count) {
		return new StepOverRequest(tasks, count);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.request.IPDIRequestFactory#getStopDebuggerRequest(org.eclipse.ptp.core.util.BitList)
	 */
	public IPDIStopDebuggerRequest getStopDebuggerRequest(BitList tasks) {
		return new StopDebuggerRequest(tasks);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.request.IPDIRequestFactory#getSuspendRequest(org.eclipse.ptp.core.util.BitList)
	 */
	public IPDIHaltRequest getSuspendRequest(BitList tasks) {
		return new SuspendRequest(tasks);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.request.IPDIRequestFactory#getSuspendRequest(org.eclipse.ptp.core.util.BitList, boolean)
	 */
	public IPDIHaltRequest getSuspendRequest(BitList tasks, boolean sendEvent) {
		return new SuspendRequest(tasks, sendEvent);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.request.IPDIRequestFactory#getTerminateRequest(org.eclipse.ptp.core.util.BitList)
	 */
	public IPDITerminateRequest getTerminateRequest(BitList tasks) {
		return new TerminateRequest(tasks);
	}
}

