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
package org.eclipse.ptp.debug.sdm.core.pdi.request;

import org.eclipse.ptp.core.util.BitList;
import org.eclipse.ptp.debug.core.pdi.model.aif.AIFFactory;
import org.eclipse.ptp.debug.core.pdi.request.AbstractGetAIFRequest;
import org.eclipse.ptp.proxy.debug.client.ProxyDebugAIF;
import org.eclipse.ptp.proxy.debug.event.IProxyDebugDataEvent;

public class SDMGetAIFRequest extends AbstractGetAIFRequest {
	public SDMGetAIFRequest(BitList tasks, String expr) {
		super(tasks, expr);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.internal.core.pdi.request.AbstractGetAIFRequest#storeResult(org.eclipse.ptp.core.util.BitList, org.eclipse.ptp.proxy.debug.event.IProxyDebugEvent)
	 */
	protected void storeResult(BitList rTasks, Object result) {
		if (result instanceof IProxyDebugDataEvent) {
			ProxyDebugAIF proxyAIF = ((IProxyDebugDataEvent)result).getData();
			results.put(rTasks, AIFFactory.newAIF(proxyAIF.getFDS(), proxyAIF.getData(), proxyAIF.getDescription()));
		}
		else {
			storeUnknownResult(rTasks, result);
		}
	}
}
