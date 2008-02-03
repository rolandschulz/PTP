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
import org.eclipse.ptp.debug.core.pdi.PDIException;
import org.eclipse.ptp.debug.core.pdi.model.IPDISignalDescriptor;
import org.eclipse.ptp.debug.core.pdi.request.AbstractListSignalsRequest;
import org.eclipse.ptp.proxy.debug.client.ProxyDebugSignal;
import org.eclipse.ptp.proxy.debug.event.IProxyDebugSignalsEvent;

public class SDMListSignalsRequest extends AbstractListSignalsRequest {
	private IPDISession session;
	
	public SDMListSignalsRequest(IPDISession session, BitList tasks, String name) {
		super(tasks, name);
		this.session = session;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.request.IPDIListSignalsRequest#getSignals(org.eclipse.ptp.core.util.BitList)
	 */
	public IPDISignalDescriptor[] getSignals(BitList qTasks) throws PDIException {
		waitUntilCompleted(qTasks);
		Object obj = getResult(qTasks);
		if (obj instanceof ProxyDebugSignal[]) {
			ProxyDebugSignal[] proxySigs = (ProxyDebugSignal[])obj;
			IPDISignalDescriptor[] signals = new IPDISignalDescriptor[proxySigs.length];
			for (int i = 0; i < signals.length; i++) {
				signals[i] = session.getModelFactory().newSignalDescriptor(proxySigs[i].getName(), proxySigs[i].isStop(), proxySigs[i].isPass(), 
						proxySigs[i].isPrint(), proxySigs[i].getDescription());
			}
			return signals;
		}
		throw new PDIException(qTasks, "No signals found");
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.internal.core.pdi.request.AbstractEventResultRequest#storeResult(org.eclipse.ptp.core.util.BitList, org.eclipse.ptp.proxy.debug.event.IProxyDebugEvent)
	 */
	protected void storeResult(BitList rTasks, Object result) {
		if (result instanceof IProxyDebugSignalsEvent) {
			results.put(rTasks, ((IProxyDebugSignalsEvent)result).getSignals());
		}
		else {
			storeUnknownResult(rTasks, result);
		}
	}
}
