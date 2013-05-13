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
package org.eclipse.ptp.internal.debug.sdm.core;

import org.eclipse.ptp.debug.core.TaskSet;
import org.eclipse.ptp.debug.core.pdi.IPDISession;
import org.eclipse.ptp.debug.core.pdi.request.IPDIDataReadMemoryRequest;
import org.eclipse.ptp.debug.core.pdi.request.IPDIEvaluateExpressionRequest;
import org.eclipse.ptp.debug.core.pdi.request.IPDIEvaluatePartialExpressionRequest;
import org.eclipse.ptp.debug.core.pdi.request.IPDIGetInfoThreadsRequest;
import org.eclipse.ptp.debug.core.pdi.request.IPDIGetStackInfoDepthRequest;
import org.eclipse.ptp.debug.core.pdi.request.IPDIListArgumentsRequest;
import org.eclipse.ptp.debug.core.pdi.request.IPDIListLocalVariablesRequest;
import org.eclipse.ptp.debug.core.pdi.request.IPDIListSignalsRequest;
import org.eclipse.ptp.debug.core.pdi.request.IPDIListStackFramesRequest;
import org.eclipse.ptp.debug.core.pdi.request.IPDISetCurrentStackFrameRequest;
import org.eclipse.ptp.debug.core.pdi.request.IPDISetThreadSelectRequest;
import org.eclipse.ptp.internal.debug.core.pdi.request.AbstractRequestFactory;
import org.eclipse.ptp.internal.debug.sdm.core.pdi.request.SDMDataReadMemoryRequest;
import org.eclipse.ptp.internal.debug.sdm.core.pdi.request.SDMEvaluateExpressionRequest;
import org.eclipse.ptp.internal.debug.sdm.core.pdi.request.SDMEvaluatePartialExpressionRequest;
import org.eclipse.ptp.internal.debug.sdm.core.pdi.request.SDMGetInfoThreadsRequest;
import org.eclipse.ptp.internal.debug.sdm.core.pdi.request.SDMGetStackInfoDepthRequest;
import org.eclipse.ptp.internal.debug.sdm.core.pdi.request.SDMListArgumentsRequest;
import org.eclipse.ptp.internal.debug.sdm.core.pdi.request.SDMListLocalVariablesRequest;
import org.eclipse.ptp.internal.debug.sdm.core.pdi.request.SDMListSignalsRequest;
import org.eclipse.ptp.internal.debug.sdm.core.pdi.request.SDMListStackFramesRequest;
import org.eclipse.ptp.internal.debug.sdm.core.pdi.request.SDMSetCurrentStackFrameRequest;
import org.eclipse.ptp.internal.debug.sdm.core.pdi.request.SDMSetThreadSelectRequest;

public class SDMRequestFactory extends AbstractRequestFactory {
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.pdi.request.IPDIRequestFactory#
	 * getDataReadMemoryRequest(org.eclipse.ptp.debug.core.pdi.IPDISession,
	 * org.eclipse.ptp.core.util.TaskSet, long, java.lang.String, int, int, int,
	 * int, java.lang.Character)
	 */
	/**
	 * @since 4.0
	 */
	public IPDIDataReadMemoryRequest getDataReadMemoryRequest(IPDISession session, TaskSet tasks, long offset, String address,
			int wordFormat, int wordSize, int rows, int cols, Character asChar) {
		return new SDMDataReadMemoryRequest(session, tasks, offset, address, wordFormat, wordSize, rows, cols, asChar);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.pdi.request.IPDIRequestFactory#
	 * getEvaluateExpressionRequest(org.eclipse.ptp.core.util.TaskSet,
	 * java.lang.String)
	 */
	/**
	 * @since 4.0
	 */
	public IPDIEvaluateExpressionRequest getEvaluateExpressionRequest(TaskSet tasks, String expr) {
		return new SDMEvaluateExpressionRequest(tasks, expr);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.pdi.request.IPDIRequestFactory#
	 * getEvaluatePartialExpressionRequest(org.eclipse.ptp.core.util.TaskSet,
	 * java.lang.String, java.lang.String, boolean)
	 */
	/**
	 * @since 4.0
	 */
	public IPDIEvaluatePartialExpressionRequest getEvaluatePartialExpressionRequest(TaskSet tasks, String expr, String exprId,
			boolean listChildren) {
		return new SDMEvaluatePartialExpressionRequest(tasks, expr, exprId, listChildren);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.pdi.request.IPDIRequestFactory#
	 * getGetInfoThreadsRequest(org.eclipse.ptp.core.util.TaskSet)
	 */
	/**
	 * @since 4.0
	 */
	public IPDIGetInfoThreadsRequest getGetInfoThreadsRequest(TaskSet tasks) {
		return new SDMGetInfoThreadsRequest(tasks);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.pdi.request.IPDIRequestFactory#
	 * getGetStackInfoDepthRequest(org.eclipse.ptp.core.util.TaskSet)
	 */
	/**
	 * @since 4.0
	 */
	public IPDIGetStackInfoDepthRequest getGetStackInfoDepthRequest(TaskSet tasks) {
		return new SDMGetStackInfoDepthRequest(tasks);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.pdi.request.IPDIRequestFactory#
	 * getListArgumentsRequest(org.eclipse.ptp.core.util.TaskSet, int, int)
	 */
	/**
	 * @since 4.0
	 */
	public IPDIListArgumentsRequest getListArgumentsRequest(TaskSet tasks, int diff, int diff2) {
		return new SDMListArgumentsRequest(tasks, diff, diff2);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.pdi.request.IPDIRequestFactory#
	 * getListLocalVariablesRequest(org.eclipse.ptp.core.util.TaskSet)
	 */
	/**
	 * @since 4.0
	 */
	public IPDIListLocalVariablesRequest getListLocalVariablesRequest(TaskSet tasks) {
		return new SDMListLocalVariablesRequest(tasks);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.pdi.request.IPDIRequestFactory#
	 * getListSignalsRequest(org.eclipse.ptp.debug.core.pdi.IPDISession,
	 * org.eclipse.ptp.core.util.TaskSet, java.lang.String)
	 */
	/**
	 * @since 4.0
	 */
	public IPDIListSignalsRequest getListSignalsRequest(IPDISession session, TaskSet tasks, String name) {
		return new SDMListSignalsRequest(session, tasks, name);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.pdi.request.IPDIRequestFactory#
	 * getListStackFramesRequest(org.eclipse.ptp.debug.core.pdi.IPDISession,
	 * org.eclipse.ptp.core.util.TaskSet)
	 */
	/**
	 * @since 4.0
	 */
	public IPDIListStackFramesRequest getListStackFramesRequest(IPDISession session, TaskSet tasks) {
		return new SDMListStackFramesRequest(session, tasks);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.pdi.request.IPDIRequestFactory#
	 * getListStackFramesRequest(org.eclipse.ptp.debug.core.pdi.IPDISession,
	 * org.eclipse.ptp.core.util.TaskSet, int, int)
	 */
	/**
	 * @since 4.0
	 */
	public IPDIListStackFramesRequest getListStackFramesRequest(IPDISession session, TaskSet tasks, int low, int high) {
		return new SDMListStackFramesRequest(session, tasks, low, high);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.pdi.request.IPDIRequestFactory#
	 * getSetCurrentStackFrameRequest(org.eclipse.ptp.core.util.TaskSet, int)
	 */
	/**
	 * @since 4.0
	 */
	public IPDISetCurrentStackFrameRequest getSetCurrentStackFrameRequest(TaskSet tasks, int level) {
		return new SDMSetCurrentStackFrameRequest(tasks, level);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.pdi.request.IPDIRequestFactory#
	 * getSetThreadSelectRequest(org.eclipse.ptp.debug.core.pdi.IPDISession,
	 * org.eclipse.ptp.core.util.TaskSet, int)
	 */
	/**
	 * @since 4.0
	 */
	public IPDISetThreadSelectRequest getSetThreadSelectRequest(IPDISession session, TaskSet tasks, int id) {
		return new SDMSetThreadSelectRequest(session, tasks, id);
	}

}
