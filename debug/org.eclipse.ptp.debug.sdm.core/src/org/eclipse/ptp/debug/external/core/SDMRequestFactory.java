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
package org.eclipse.ptp.debug.external.core;

import org.eclipse.ptp.core.util.BitList;
import org.eclipse.ptp.debug.core.pdi.IPDISession;
import org.eclipse.ptp.debug.core.pdi.request.AbstractRequestFactory;
import org.eclipse.ptp.debug.core.pdi.request.IPDIDataEvaluateExpressionRequest;
import org.eclipse.ptp.debug.core.pdi.request.IPDIDataReadMemoryRequest;
import org.eclipse.ptp.debug.core.pdi.request.IPDIGetAIFRequest;
import org.eclipse.ptp.debug.core.pdi.request.IPDIGetInfoThreadsRequest;
import org.eclipse.ptp.debug.core.pdi.request.IPDIGetPartialAIFRequest;
import org.eclipse.ptp.debug.core.pdi.request.IPDIGetStackInfoDepthRequest;
import org.eclipse.ptp.debug.core.pdi.request.IPDIListArgumentsRequest;
import org.eclipse.ptp.debug.core.pdi.request.IPDIListLocalVariablesRequest;
import org.eclipse.ptp.debug.core.pdi.request.IPDIListSignalsRequest;
import org.eclipse.ptp.debug.core.pdi.request.IPDIListStackFramesRequest;
import org.eclipse.ptp.debug.core.pdi.request.IPDISetCurrentStackFrameRequest;
import org.eclipse.ptp.debug.core.pdi.request.IPDISetThreadSelectRequest;


public class SDMRequestFactory extends AbstractRequestFactory {
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.request.IPDIRequestFactory#getAIFRequest(org.eclipse.ptp.core.util.BitList, java.lang.String)
	 */
	public IPDIGetAIFRequest getAIFRequest(BitList tasks, String expr) {
		return new SDMGetAIFRequest(tasks, expr);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.request.IPDIRequestFactory#getDataEvaluateExpresionRequest(org.eclipse.ptp.core.util.BitList, java.lang.String)
	 */
	public IPDIDataEvaluateExpressionRequest getDataEvaluateExpresionRequest(BitList tasks, String expr) {
		return new SDMDataEvaluateExpressionRequest(tasks, expr);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.request.IPDIRequestFactory#getDataEvaluateExpressionRequest(org.eclipse.ptp.core.util.BitList, java.lang.String)
	 */
	public IPDIDataEvaluateExpressionRequest getDataEvaluateExpressionRequest(
			BitList tasks, String expr) {
		return new SDMDataEvaluateExpressionRequest(tasks, expr);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.request.IPDIRequestFactory#getDataReadMemoryRequest(org.eclipse.ptp.debug.core.pdi.IPDISession, org.eclipse.ptp.core.util.BitList, long, java.lang.String, int, int, int, int, java.lang.Character)
	 */
	public IPDIDataReadMemoryRequest getDataReadMemoryRequest(IPDISession session, BitList tasks,
			long offset, String address, int wordFormat, int wordSize,
			int rows, int cols, Character asChar) {
		return new SDMDataReadMemoryRequest(session, tasks, offset, address, wordFormat, wordSize, rows, cols, asChar);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.request.IPDIRequestFactory#getGetInfoThreadsRequest(org.eclipse.ptp.core.util.BitList)
	 */
	public IPDIGetInfoThreadsRequest getGetInfoThreadsRequest(BitList tasks) {
		return new SDMGetInfoThreadsRequest(tasks);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.request.IPDIRequestFactory#getGetPartialAIFRequest(org.eclipse.ptp.core.util.BitList, java.lang.String, java.lang.String)
	 */
	public IPDIGetPartialAIFRequest getGetPartialAIFRequest(BitList tasks,
			String expr, String varid) {
		return new SDMGetPartialAIFRequest(tasks, expr, varid);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.request.IPDIRequestFactory#getGetPartialAIFRequest(org.eclipse.ptp.core.util.BitList, java.lang.String, java.lang.String, boolean)
	 */
	public IPDIGetPartialAIFRequest getGetPartialAIFRequest(BitList tasks,
			String expr, String varid, boolean listChildren) {
		return new SDMGetPartialAIFRequest(tasks, expr, varid, listChildren);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.request.IPDIRequestFactory#getGetStackInfoDepthRequest(org.eclipse.ptp.core.util.BitList)
	 */
	public IPDIGetStackInfoDepthRequest getGetStackInfoDepthRequest(
			BitList tasks) {
		return new SDMGetStackInfoDepthRequest(tasks);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.request.IPDIRequestFactory#getListArgumentsRequest(org.eclipse.ptp.core.util.BitList, int, int)
	 */
	public IPDIListArgumentsRequest getListArgumentsRequest(BitList tasks, int diff, int diff2) {
		return new SDMListArgumentsRequest(tasks, diff, diff2);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.request.IPDIRequestFactory#getListLocalVariablesRequest(org.eclipse.ptp.core.util.BitList)
	 */
	public IPDIListLocalVariablesRequest getListLocalVariablesRequest(
			BitList tasks) {
		return new SDMListLocalVariablesRequest(tasks);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.request.IPDIRequestFactory#getListSignalsRequest(org.eclipse.ptp.debug.core.pdi.IPDISession, org.eclipse.ptp.core.util.BitList, java.lang.String)
	 */
	public IPDIListSignalsRequest getListSignalsRequest(IPDISession session, BitList tasks, String name) {
		return new SDMListSignalsRequest(session, tasks, name);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.request.IPDIRequestFactory#getListStackFramesRequest(org.eclipse.ptp.debug.core.pdi.IPDISession, org.eclipse.ptp.core.util.BitList)
	 */
	public IPDIListStackFramesRequest getListStackFramesRequest(IPDISession session, BitList tasks) {
		return new SDMListStackFramesRequest(session, tasks);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.request.IPDIRequestFactory#getListStackFramesRequest(org.eclipse.ptp.debug.core.pdi.IPDISession, org.eclipse.ptp.core.util.BitList, int, int)
	 */
	public IPDIListStackFramesRequest getListStackFramesRequest(IPDISession session, BitList tasks, int low, int high) {
		return new SDMListStackFramesRequest(session, tasks, low, high);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.request.IPDIRequestFactory#getSetCurrentStackFrameRequest(org.eclipse.ptp.core.util.BitList, int)
	 */
	public IPDISetCurrentStackFrameRequest getSetCurrentStackFrameRequest(
			BitList tasks, int level) {
		return new SDMSetCurrentStackFrameRequest(tasks, level);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.request.IPDIRequestFactory#getSetThreadSelectRequest(org.eclipse.ptp.debug.core.pdi.IPDISession, org.eclipse.ptp.core.util.BitList, int)
	 */
	public IPDISetThreadSelectRequest getSetThreadSelectRequest(
			IPDISession session, BitList tasks, int id) {
		return new SDMSetThreadSelectRequest(session, tasks, id);
	}

}
