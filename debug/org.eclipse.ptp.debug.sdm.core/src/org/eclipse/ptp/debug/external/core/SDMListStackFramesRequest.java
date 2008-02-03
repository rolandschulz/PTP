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
import org.eclipse.ptp.debug.core.pdi.IPDILocator;
import org.eclipse.ptp.debug.core.pdi.IPDISession;
import org.eclipse.ptp.debug.core.pdi.PDIException;
import org.eclipse.ptp.debug.core.pdi.PDILocationFactory;
import org.eclipse.ptp.debug.core.pdi.model.IPDIStackFrameDescriptor;
import org.eclipse.ptp.debug.core.pdi.request.AbstractListStackFramesRequest;
import org.eclipse.ptp.proxy.debug.client.ProxyDebugStackFrame;
import org.eclipse.ptp.proxy.debug.event.IProxyDebugStackframeEvent;

public class SDMListStackFramesRequest extends AbstractListStackFramesRequest {
	private IPDISession session;

	public SDMListStackFramesRequest(IPDISession session, BitList tasks) {
		this(session, tasks, 0, 0);
	}
	
	public SDMListStackFramesRequest(IPDISession session, BitList tasks, int low, int high) {
		super(tasks, low, high);
		this.session = session;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.request.IPDIListStackFramesRequest#getStackFrames(org.eclipse.ptp.core.util.BitList)
	 */
	public IPDIStackFrameDescriptor[] getStackFrames(BitList qTasks) throws PDIException {
		waitUntilCompleted(qTasks);
		Object obj = getResult(qTasks);
		if (obj instanceof ProxyDebugStackFrame[]) {
			ProxyDebugStackFrame[] frames = (ProxyDebugStackFrame[])obj;
			IPDIStackFrameDescriptor[] desc = new IPDIStackFrameDescriptor[frames.length];
			for (int i = 0; i < desc.length; i++) {
				IPDILocator loc = PDILocationFactory.newLocator(frames[i].getLocator().getFile(), frames[i].getLocator().getFunction(),
						frames[i].getLocator().getLineNumber(), frames[i].getLocator().getAddress());
				desc[i] = session.getModelFactory().newStackFrameDescriptor(frames[i].getLevel(), loc);
			}
			return desc;
		}
		throw new PDIException(qTasks, "No stack frames found");
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.internal.core.pdi.request.AbstractEventResultRequest#storeResult(org.eclipse.ptp.core.util.BitList, org.eclipse.ptp.proxy.debug.event.IProxyDebugEvent)
	 */
	protected void storeResult(BitList rTasks, Object result) {
		if (result instanceof IProxyDebugStackframeEvent) {
			results.put(rTasks, ((IProxyDebugStackframeEvent)result).getFrames());
		}
		else {
			storeUnknownResult(rTasks, result);
		}
	}
}