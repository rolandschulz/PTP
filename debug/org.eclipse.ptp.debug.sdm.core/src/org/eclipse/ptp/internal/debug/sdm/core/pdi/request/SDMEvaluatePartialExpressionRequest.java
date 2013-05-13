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
import org.eclipse.ptp.internal.debug.core.pdi.request.AbstractEvaluatePartialExpressionRequest;
import org.eclipse.ptp.internal.debug.sdm.core.messages.Messages;
import org.eclipse.ptp.proxy.debug.client.ProxyDebugAIF;
import org.eclipse.ptp.proxy.debug.event.IProxyDebugDataEvent;

public class SDMEvaluatePartialExpressionRequest extends AbstractEvaluatePartialExpressionRequest {
	public SDMEvaluatePartialExpressionRequest(TaskSet tasks, String expr, String exprId, boolean listChildren) {
		super(tasks, expr, exprId, listChildren);
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
			Object[] objs = new Object[2];
			objs[0] = ((IProxyDebugDataEvent) result).getName();
			ProxyDebugAIF proxyAIF = ((IProxyDebugDataEvent) result).getData();
			try {
				objs[1] = AIFFactory.newAIF(proxyAIF.getFDS(), proxyAIF.getData(), proxyAIF.getDescription());
			} catch (AIFFormatException e) {
				throw new RuntimeException(NLS.bind(Messages.SDMEvaluatePartialExpressionRequest_0, e.getLocalizedMessage()));
			}
			results.put(rTasks, objs);
		} else {
			storeUnknownResult(rTasks, result);
		}
	}
}
