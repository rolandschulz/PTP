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
import org.eclipse.ptp.debug.core.pdi.request.IPDISetThreadSelectRequest;
import org.eclipse.ptp.debug.internal.core.pdi.Session;
import org.eclipse.ptp.proxy.debug.client.ProxyDebugStackFrame;
import org.eclipse.ptp.proxy.debug.event.IProxyDebugEvent;
import org.eclipse.ptp.proxy.debug.event.IProxyDebugSetThreadSelectEvent;

/**
 * @author clement
 *
 */
public class SetThreadSelectRequest extends InternalEventRequest implements IPDISetThreadSelectRequest {
	private int id = 0;
	public SetThreadSelectRequest(Session session, BitList tasks, int id) {
		super(tasks);
		this.id = id;
	}
	public void doExecute(IPDIDebugger debugger) throws PDIException {
		debugger.selectThread(tasks, id);
	}
	protected void storeResult(BitList rTasks, IProxyDebugEvent result) {
		if (result instanceof IProxyDebugSetThreadSelectEvent) {
			Object[] objs = new Object[2];
			objs[0] = new Integer(((IProxyDebugSetThreadSelectEvent)result).getThreadId());
			objs[1] = ((IProxyDebugSetThreadSelectEvent)result).getFrame();
			results.put(rTasks, objs);
		}
		else {
			storeUnknownResult(rTasks, result);
		}
	}
	public int getThreadId(BitList qTasks) throws PDIException {
		waitUntilCompleted(qTasks);
		Object obj = getResult(qTasks);
		if (obj instanceof Object[]) {
			Object[] returnValues = (Object[])obj;
			return ((Integer)returnValues[0]).intValue();
		}
		throw new PDIException(qTasks, "No Thread ID found");
	}
	public ProxyDebugStackFrame getStackFrame(BitList qTasks) throws PDIException {
		waitUntilCompleted(qTasks);
		Object obj = getResult(qTasks);
		if (obj instanceof Object[]) {
			Object[] returnValues = (Object[])obj;
			return (ProxyDebugStackFrame)returnValues[1];
		}
		throw new PDIException(qTasks, "No Stack frame found");
	}
	public String getName() {
		return "Set thread select request";
	}
}
