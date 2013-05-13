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
import org.eclipse.ptp.debug.core.pdi.IPDISession;
import org.eclipse.ptp.debug.core.pdi.model.IPDIMemory;
import org.eclipse.ptp.internal.debug.core.pdi.request.AbstractDataReadMemoryRequest;
import org.eclipse.ptp.proxy.debug.client.ProxyDebugMemory;
import org.eclipse.ptp.proxy.debug.client.ProxyDebugMemoryInfo;
import org.eclipse.ptp.proxy.debug.event.IProxyDebugMemoryInfoEvent;

public class SDMDataReadMemoryRequest extends AbstractDataReadMemoryRequest {
	private IPDISession session;
	
	public SDMDataReadMemoryRequest(IPDISession session, TaskSet tasks, long offset, String address, int wordFormat, int wordSize, int rows, int cols, Character asChar) {
		super(tasks, offset, address, wordFormat, wordSize, rows, cols, asChar);
		this.session = session;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.internal.debug.core.pdi.request.AbstractEventResultRequest#storeResult(org.eclipse.ptp.core.util.TaskSet, org.eclipse.ptp.proxy.debug.event.IProxyDebugEvent)
	 */
	protected void storeResult(TaskSet rTasks, Object result) {
		if (result instanceof IProxyDebugMemoryInfoEvent) {
			ProxyDebugMemoryInfo info = ((IProxyDebugMemoryInfoEvent)result).getMemoryInfo();
			ProxyDebugMemory[] proxyMems = info.getMemories();
			IPDIMemory[] memories = new IPDIMemory[proxyMems.length];
			for (int i=0; i<proxyMems.length; i++) {
				memories[i] = session.getModelFactory().newMemory(proxyMems[i].getAddress(), proxyMems[i].getAscii(), proxyMems[i].getData());
			}
			results.put(rTasks, session.getEventFactory().newDataReadMemoryInfo(info.getAddress(), info.getNextRow(), info.getPrevRow(), info.getNextPage(), info.getPrevPage(), info.getNumBytes(), info.getTotalBytes(), memories));
		}
		else {
			storeUnknownResult(rTasks, result);
		}
	}
}
