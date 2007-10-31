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
import org.eclipse.ptp.debug.core.pdi.request.IPDIGetAIFRequest;
import org.eclipse.ptp.debug.internal.core.pdi.model.AIF;
import org.eclipse.ptp.proxy.debug.client.ProxyDebugAIF;
import org.eclipse.ptp.proxy.debug.event.IProxyDebugDataEvent;
import org.eclipse.ptp.proxy.debug.event.IProxyDebugEvent;


/**
 * @author clement
 *
 */
public class GetAIFRequest extends InternalEventRequest implements IPDIGetAIFRequest {
	private String expr;
	
	public GetAIFRequest(BitList tasks, String expr) {
		super(tasks);
		this.expr = expr;
	}
	public void doExecute(IPDIDebugger debugger) throws PDIException {
		debugger.retrieveAIF(tasks, expr);
	}
	protected void storeResult(BitList rTasks, IProxyDebugEvent result) {
		if (result instanceof IProxyDebugDataEvent) {
			ProxyDebugAIF proxyAIF = ((IProxyDebugDataEvent)result).getData();
			results.put(rTasks, new AIF(proxyAIF.getFDS(), proxyAIF.getData(), proxyAIF.getDescription()));
		}
		else {
			storeUnknownResult(rTasks, result);
		}
	}
	public IAIF getAIF(BitList qTasks) throws PDIException {
		waitUntilCompleted(qTasks);
		Object obj = getResult(qTasks);
		if (obj instanceof IAIF) {
			return (IAIF)obj;
		}
		throw new PDIException(qTasks, "Variable "+ expr +": No value found");
	}
	public String getName() {
		return "Get aif request";
	}
}
