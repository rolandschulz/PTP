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

import org.eclipse.osgi.util.NLS;
import org.eclipse.ptp.debug.core.TaskSet;
import org.eclipse.ptp.debug.core.pdi.model.aif.AIFFormatException;
import org.eclipse.ptp.internal.debug.core.pdi.model.aif.AIFFactory;
import org.eclipse.ptp.internal.debug.core.pdi.request.AbstractEvaluateExpressionRequest;
import org.eclipse.ptp.internal.debug.sdm.core.messages.Messages;
import org.eclipse.ptp.proxy.debug.client.ProxyDebugAIF;
import org.eclipse.ptp.proxy.debug.event.IProxyDebugDataEvent;

public class SDMEvaluateExpressionRequest extends AbstractEvaluateExpressionRequest {
	public SDMEvaluateExpressionRequest(TaskSet tasks, String expr) {
		super(tasks, expr);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.internal.debug.core.pdi.request.AbstractEventResultRequest
	 * #storeResult(org.eclipse.ptp.core.util.TaskSet,
	 * org.eclipse.ptp.proxy.debug.event.IProxyDebugEvent)
	 */
	@Override
	protected void storeResult(TaskSet rTasks, Object result) {
		if (result instanceof IProxyDebugDataEvent) {
			ProxyDebugAIF proxyAIF = ((IProxyDebugDataEvent) result).getData();
			try {
				results.put(rTasks, AIFFactory.newAIF(proxyAIF.getFDS(), proxyAIF.getData(), proxyAIF.getDescription()));
			} catch (AIFFormatException e) {
				throw new RuntimeException(NLS.bind(Messages.SDMEvaluateExpressionRequest_0, e.getLocalizedMessage()));
			}
		} else {
			storeUnknownResult(rTasks, result);
		}
	}
}
