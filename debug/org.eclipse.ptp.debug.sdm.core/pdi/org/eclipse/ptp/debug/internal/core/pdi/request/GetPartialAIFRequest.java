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
package org.eclipse.ptp.debug.internal.core.pdi.request;

import org.eclipse.ptp.core.util.BitList;
import org.eclipse.ptp.debug.core.pdi.IPDIDebugger;
import org.eclipse.ptp.debug.core.pdi.PDIException;
import org.eclipse.ptp.debug.core.pdi.model.aif.IAIF;
import org.eclipse.ptp.debug.core.pdi.request.IPDIGetPartialAIFRequest;
import org.eclipse.ptp.debug.internal.core.pdi.model.AIF;
import org.eclipse.ptp.proxy.debug.client.ProxyDebugAIF;
import org.eclipse.ptp.proxy.debug.event.IProxyDebugEvent;
import org.eclipse.ptp.proxy.debug.event.IProxyDebugPartialAIFEvent;


/**
 * @author clement
 *
 */
public class GetPartialAIFRequest extends InternalEventRequest implements IPDIGetPartialAIFRequest {
	private String expr;
	private String varid;
	private boolean listChildren = false;
	private boolean express = false;
	
	public GetPartialAIFRequest(BitList tasks, String expr, String varid, boolean listChildren, boolean express) {
		super(tasks);
		this.expr = expr;
		this.varid = varid;
		this.listChildren = listChildren;
		this.express = express;
	}
	public GetPartialAIFRequest(BitList tasks, String expr, String varid, boolean listChildren) {
		this(tasks, expr, varid, listChildren, false);
	}
	public GetPartialAIFRequest(BitList tasks, String expr, String varid) {
		this(tasks, expr, varid, false, (varid != null));
	}
	public void doExecute(IPDIDebugger debugger) throws PDIException {
		debugger.retrievePartialAIF(tasks, expr, varid, listChildren, express);
	}
	protected void storeResult(BitList rTasks, IProxyDebugEvent result) {
		if (result instanceof IProxyDebugPartialAIFEvent) {
			Object[] objs = new Object[2];
			objs[0] = ((IProxyDebugPartialAIFEvent)result).getName();
			ProxyDebugAIF proxyAIF = ((IProxyDebugPartialAIFEvent)result).getData();
			objs[1] = new AIF(proxyAIF.getFDS(), proxyAIF.getData(), proxyAIF.getDescription());
			results.put(rTasks, objs);
		}
		else {
			storeUnknownResult(rTasks, result);
		}
	}
	public String getVarId(BitList qTasks) throws PDIException {
		waitUntilCompleted(qTasks);
		Object obj = getResult(qTasks);
		if (obj instanceof Object[]) {
			Object[] returnValues = (Object[])obj;
			return (String)returnValues[0];
		}
		throw new PDIException(qTasks, "Variable ID " + varid + ": No variable ID found");
	}
	public IAIF getPartialAIF(BitList qTasks) throws PDIException {
		waitUntilCompleted(qTasks);
		Object obj = getResult(qTasks);
		if (obj instanceof Object[]) {
			Object[] returnValues = (Object[])obj;
			return (IAIF)returnValues[1];
		}
		throw new PDIException(qTasks, "Variable " + expr + ": No aif found");
	}
	public String getName() {
		return "Get partial aif request";
	}
	public String toString() {
		return getName() + " for tasks " + BitList.showBitList(getTasks()) + ", exp: " + expr + ", var id: " + varid + ", is list children: " + listChildren + ", is express: " + express; 
	}
}
