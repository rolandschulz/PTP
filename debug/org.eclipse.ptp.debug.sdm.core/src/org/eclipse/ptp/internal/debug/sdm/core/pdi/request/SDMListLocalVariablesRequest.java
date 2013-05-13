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
import org.eclipse.ptp.internal.debug.core.pdi.request.AbstractListLocalVariablesRequest;
import org.eclipse.ptp.proxy.debug.event.IProxyDebugVarsEvent;

public class SDMListLocalVariablesRequest extends AbstractListLocalVariablesRequest {
	public SDMListLocalVariablesRequest(TaskSet tasks) {
		super(tasks);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.internal.debug.core.pdi.request.AbstractEventResultRequest#storeResult(org.eclipse.ptp.core.util.TaskSet, org.eclipse.ptp.proxy.debug.event.IProxyDebugEvent)
	 */
	protected void storeResult(TaskSet rTasks, Object result) {
		if (result instanceof IProxyDebugVarsEvent) {
			results.put(rTasks, ((IProxyDebugVarsEvent)result).getVariables());
		}
		else {
			storeUnknownResult(rTasks, result);
		}
	}
}
