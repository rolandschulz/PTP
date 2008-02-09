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
import org.eclipse.ptp.debug.core.pdi.request.AbstractGetPartialAIFRequest;
import org.eclipse.ptp.proxy.debug.client.ProxyDebugAIF;
import org.eclipse.ptp.proxy.debug.event.IProxyDebugPartialAIFEvent;

public class SDMGetPartialAIFRequest extends AbstractGetPartialAIFRequest {
	public SDMGetPartialAIFRequest(BitList tasks, String expr, String varid) {
		this(tasks, expr, varid, false, (varid != null));
	}
	
	public SDMGetPartialAIFRequest(BitList tasks, String expr, String varid, boolean listChildren) {
		this(tasks, expr, varid, listChildren, false);
	}
	
	public SDMGetPartialAIFRequest(BitList tasks, String expr, String varid, boolean listChildren, boolean express) {
		super(tasks, expr, varid, listChildren, express);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.internal.core.pdi.request.AbstractEventResultRequest#storeResult(org.eclipse.ptp.core.util.BitList, org.eclipse.ptp.proxy.debug.event.IProxyDebugEvent)
	 */
	protected void storeResult(BitList rTasks, Object result) {
		if (result instanceof IProxyDebugPartialAIFEvent) {
			Object[] objs = new Object[2];
			objs[0] = ((IProxyDebugPartialAIFEvent)result).getName();
			ProxyDebugAIF proxyAIF = ((IProxyDebugPartialAIFEvent)result).getData();
			objs[1] = AIFFactory.newAIF(proxyAIF.getFDS(), proxyAIF.getData(), proxyAIF.getDescription());
			results.put(rTasks, objs);
		}
		else {
			storeUnknownResult(rTasks, result);
		}
	}
}
