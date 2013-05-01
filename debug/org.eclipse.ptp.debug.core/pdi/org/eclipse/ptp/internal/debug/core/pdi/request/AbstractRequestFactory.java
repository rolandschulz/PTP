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
package org.eclipse.ptp.internal.debug.core.pdi.request;

import org.eclipse.ptp.debug.core.TaskSet;
import org.eclipse.ptp.debug.core.pdi.model.IPDIAddressBreakpoint;
import org.eclipse.ptp.debug.core.pdi.model.IPDIBreakpoint;
import org.eclipse.ptp.debug.core.pdi.model.IPDIFunctionBreakpoint;
import org.eclipse.ptp.debug.core.pdi.model.IPDILineBreakpoint;
import org.eclipse.ptp.debug.core.pdi.model.IPDIWatchpoint;
import org.eclipse.ptp.debug.core.pdi.request.IPDICommandRequest;
import org.eclipse.ptp.debug.core.pdi.request.IPDIDataWriteMemoryRequest;
import org.eclipse.ptp.debug.core.pdi.request.IPDIDeleteBreakpointRequest;
import org.eclipse.ptp.debug.core.pdi.request.IPDIDeleteVariableRequest;
import org.eclipse.ptp.debug.core.pdi.request.IPDIDisableBreakpointRequest;
import org.eclipse.ptp.debug.core.pdi.request.IPDIEnableBreakpointRequest;
import org.eclipse.ptp.debug.core.pdi.request.IPDIGoRequest;
import org.eclipse.ptp.debug.core.pdi.request.IPDIHaltRequest;
import org.eclipse.ptp.debug.core.pdi.request.IPDIRequestFactory;
import org.eclipse.ptp.debug.core.pdi.request.IPDISetAddressBreakpointRequest;
import org.eclipse.ptp.debug.core.pdi.request.IPDISetFunctionBreakpointRequest;
import org.eclipse.ptp.debug.core.pdi.request.IPDISetLineBreakpointRequest;
import org.eclipse.ptp.debug.core.pdi.request.IPDISetWatchpointRequest;
import org.eclipse.ptp.debug.core.pdi.request.IPDIStartDebuggerRequest;
import org.eclipse.ptp.debug.core.pdi.request.IPDIStepFinishRequest;
import org.eclipse.ptp.debug.core.pdi.request.IPDIStepIntoRequest;
import org.eclipse.ptp.debug.core.pdi.request.IPDIStepOverRequest;
import org.eclipse.ptp.debug.core.pdi.request.IPDIStopDebuggerRequest;
import org.eclipse.ptp.debug.core.pdi.request.IPDITerminateRequest;

/**
 * @author clement
 *
 */
/**
 * @author greg
 * 
 */
public abstract class AbstractRequestFactory implements IPDIRequestFactory {
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.pdi.request.IPDIRequestFactory#getCommandRequest
	 * (org.eclipse.ptp.core.util.TaskSet, java.lang.String)
	 */
	/**
	 * @since 4.0
	 */
	public IPDICommandRequest getCommandRequest(TaskSet tasks, String command) {
		return new CommandRequest(tasks, command);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.pdi.request.IPDIRequestFactory#
	 * getDataWriteMemoryRequest(org.eclipse.ptp.core.util.TaskSet, long,
	 * java.lang.String, int, int, java.lang.String)
	 */
	/**
	 * @since 4.0
	 */
	public IPDIDataWriteMemoryRequest getDataWriteMemoryRequest(TaskSet tasks, long offset, String address, int wordFormat,
			int wordSize, String value) {
		return new DataWriteMemoryRequest(tasks, offset, address, wordFormat, wordSize, value);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.pdi.request.IPDIRequestFactory#
	 * getDeleteBreakpointRequest(org.eclipse.ptp.core.util.TaskSet,
	 * org.eclipse.ptp.debug.core.pdi.model.IPDIBreakpoint, boolean)
	 */
	/**
	 * @since 4.0
	 */
	public IPDIDeleteBreakpointRequest getDeleteBreakpointRequest(TaskSet tasks, IPDIBreakpoint bpt, boolean allowUpdate) {
		return new DeleteBreakpointRequest(tasks, bpt, allowUpdate);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.pdi.request.IPDIRequestFactory#
	 * getDeletePartialExpressionRequest(org.eclipse.ptp.core.util.TaskSet,
	 * java.lang.String)
	 */
	/**
	 * @since 4.0
	 */
	public IPDIDeleteVariableRequest getDeletePartialExpressionRequest(TaskSet tasks, String exprId) {
		return new DeletePartialExpressionRequest(tasks, exprId);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.pdi.request.IPDIRequestFactory#
	 * getDisableBreakpointRequest(org.eclipse.ptp.core.util.TaskSet,
	 * org.eclipse.ptp.debug.core.pdi.model.IPDIBreakpoint)
	 */
	/**
	 * @since 4.0
	 */
	public IPDIDisableBreakpointRequest getDisableBreakpointRequest(TaskSet tasks, IPDIBreakpoint bpt) {
		return new DisableBreakpointRequest(tasks, bpt);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.pdi.request.IPDIRequestFactory#
	 * getEnableBreakpointRequest(org.eclipse.ptp.core.util.TaskSet,
	 * org.eclipse.ptp.debug.core.pdi.model.IPDIBreakpoint)
	 */
	/**
	 * @since 4.0
	 */
	public IPDIEnableBreakpointRequest getEnableBreakpointRequest(TaskSet tasks, IPDIBreakpoint bpt) {
		return new EnableBreakpointRequest(tasks, bpt);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.pdi.request.IPDIRequestFactory#getResumeRequest
	 * (org.eclipse.ptp.core.util.TaskSet, boolean)
	 */
	/**
	 * @since 4.0
	 */
	public IPDIGoRequest getResumeRequest(TaskSet tasks, boolean passSignal) {
		return new ResumeRequest(tasks, passSignal);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.pdi.request.IPDIRequestFactory#
	 * getSetAddressBreakpointRequest(org.eclipse.ptp.core.util.TaskSet,
	 * org.eclipse.ptp.debug.core.pdi.model.IPDIAddressBreakpoint, boolean)
	 */
	/**
	 * @since 4.0
	 */
	public IPDISetAddressBreakpointRequest getSetAddressBreakpointRequest(TaskSet tasks, IPDIAddressBreakpoint bpt,
			boolean allowUpdate) {
		return new SetAddressBreakpointRequest(tasks, bpt, allowUpdate);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.pdi.request.IPDIRequestFactory#
	 * getSetFunctionBreakpointRequest(org.eclipse.ptp.core.util.TaskSet,
	 * org.eclipse.ptp.debug.core.pdi.model.IPDIFunctionBreakpoint, boolean)
	 */
	/**
	 * @since 4.0
	 */
	public IPDISetFunctionBreakpointRequest getSetFunctionBreakpointRequest(TaskSet tasks, IPDIFunctionBreakpoint bpt,
			boolean allowUpdate) {
		return new SetFunctionBreakpointRequest(tasks, bpt, allowUpdate);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.pdi.request.IPDIRequestFactory#
	 * getSetLineBreakpointRequest(org.eclipse.ptp.core.util.TaskSet,
	 * org.eclipse.ptp.debug.core.pdi.model.IPDILineBreakpoint, boolean)
	 */
	/**
	 * @since 4.0
	 */
	public IPDISetLineBreakpointRequest getSetLineBreakpointRequest(TaskSet tasks, IPDILineBreakpoint bpt, boolean allowUpdate) {
		return new SetLineBreakpointRequest(tasks, bpt, allowUpdate);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.pdi.request.IPDIRequestFactory#
	 * getSetWatchpointRequest(org.eclipse.ptp.core.util.TaskSet,
	 * org.eclipse.ptp.debug.core.pdi.model.IPDIWatchpoint, boolean)
	 */
	/**
	 * @since 4.0
	 */
	public IPDISetWatchpointRequest getSetWatchpointRequest(TaskSet tasks, IPDIWatchpoint bpt, boolean allowUpdate) {
		return new SetWatchpointRequest(tasks, bpt, allowUpdate);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.pdi.request.IPDIRequestFactory#
	 * getStartDebuggerRequest(org.eclipse.ptp.core.util.TaskSet,
	 * java.lang.String, java.lang.String, java.lang.String, java.lang.String[])
	 */
	/**
	 * @since 4.0
	 */
	public IPDIStartDebuggerRequest getStartDebuggerRequest(TaskSet tasks, String app, String path, String dir, String[] args) {
		return new StartDebuggerRequest(tasks, app, path, dir, args);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.pdi.request.IPDIRequestFactory#
	 * getStepFinishRequest(org.eclipse.ptp.core.util.TaskSet, int)
	 */
	/**
	 * @since 4.0
	 */
	public IPDIStepFinishRequest getStepFinishRequest(TaskSet tasks, int count) {
		return new StepFinishRequest(tasks, count);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.pdi.request.IPDIRequestFactory#getStepIntoRequest
	 * (org.eclipse.ptp.core.util.TaskSet, int)
	 */
	/**
	 * @since 4.0
	 */
	public IPDIStepIntoRequest getStepIntoRequest(TaskSet tasks, int count) {
		return new StepIntoRequest(tasks, count);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.pdi.request.IPDIRequestFactory#getStepOverRequest
	 * (org.eclipse.ptp.core.util.TaskSet, int)
	 */
	/**
	 * @since 4.0
	 */
	public IPDIStepOverRequest getStepOverRequest(TaskSet tasks, int count) {
		return new StepOverRequest(tasks, count);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.pdi.request.IPDIRequestFactory#
	 * getStopDebuggerRequest(org.eclipse.ptp.core.util.TaskSet)
	 */
	/**
	 * @since 4.0
	 */
	public IPDIStopDebuggerRequest getStopDebuggerRequest(TaskSet tasks) {
		return new StopDebuggerRequest(tasks);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.pdi.request.IPDIRequestFactory#getSuspendRequest
	 * (org.eclipse.ptp.core.util.TaskSet)
	 */
	/**
	 * @since 4.0
	 */
	public IPDIHaltRequest getSuspendRequest(TaskSet tasks) {
		return new SuspendRequest(tasks);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.pdi.request.IPDIRequestFactory#getSuspendRequest
	 * (org.eclipse.ptp.core.util.TaskSet, boolean)
	 */
	/**
	 * @since 4.0
	 */
	public IPDIHaltRequest getSuspendRequest(TaskSet tasks, boolean sendEvent) {
		return new SuspendRequest(tasks, sendEvent);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.pdi.request.IPDIRequestFactory#getTerminateRequest
	 * (org.eclipse.ptp.core.util.TaskSet)
	 */
	/**
	 * @since 4.0
	 */
	public IPDITerminateRequest getTerminateRequest(TaskSet tasks) {
		return new TerminateRequest(tasks);
	}
}
