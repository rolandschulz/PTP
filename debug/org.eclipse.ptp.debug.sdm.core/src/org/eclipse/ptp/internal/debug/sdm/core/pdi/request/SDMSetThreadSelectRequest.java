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
package org.eclipse.ptp.internal.debug.sdm.core.pdi.request;

import org.eclipse.ptp.debug.core.TaskSet;
import org.eclipse.ptp.debug.core.pdi.IPDILocator;
import org.eclipse.ptp.debug.core.pdi.IPDISession;
import org.eclipse.ptp.debug.core.pdi.PDIException;
import org.eclipse.ptp.debug.core.pdi.PDILocationFactory;
import org.eclipse.ptp.debug.core.pdi.model.IPDIStackFrameDescriptor;
import org.eclipse.ptp.internal.debug.core.pdi.request.AbstractSetThreadSelectRequest;
import org.eclipse.ptp.internal.debug.sdm.core.messages.Messages;
import org.eclipse.ptp.proxy.debug.client.ProxyDebugStackFrame;
import org.eclipse.ptp.proxy.debug.event.IProxyDebugSetThreadSelectEvent;

public class SDMSetThreadSelectRequest extends AbstractSetThreadSelectRequest {
	private IPDISession session;
	
	public SDMSetThreadSelectRequest(IPDISession session, TaskSet tasks, int id) {
		super(tasks, id);
		this.session = session;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.request.IPDISetThreadSelectRequest#getStackFrame(org.eclipse.ptp.core.util.TaskSet)
	 */
	public IPDIStackFrameDescriptor getStackFrame(TaskSet qTasks) throws PDIException {
		waitUntilCompleted(qTasks);
		Object obj = getResult(qTasks);
		if (obj instanceof Object[]) {
			Object[] returnValues = (Object[])obj;
			ProxyDebugStackFrame frame = (ProxyDebugStackFrame)returnValues[1];
			IPDILocator loc = PDILocationFactory.newLocator(frame.getLocator().getFile(), frame.getLocator().getFunction(), 
					frame.getLocator().getLineNumber(), frame.getLocator().getAddress());
			return session.getModelFactory().newStackFrameDescriptor(frame.getLevel(), loc);
		}
		throw new PDIException(qTasks, Messages.SDMSetThreadSelectRequest_0);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.internal.debug.core.pdi.request.AbstractEventResultRequest#storeResult(org.eclipse.ptp.core.util.TaskSet, org.eclipse.ptp.proxy.debug.event.IProxyDebugEvent)
	 */
	protected void storeResult(TaskSet rTasks, Object result) {
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
}
