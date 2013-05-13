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
import org.eclipse.ptp.internal.debug.core.pdi.request.AbstractGetInfoThreadsRequest;
import org.eclipse.ptp.proxy.debug.event.IProxyDebugInfoThreadsEvent;

public class SDMGetInfoThreadsRequest extends AbstractGetInfoThreadsRequest {
	public SDMGetInfoThreadsRequest(TaskSet tasks) {
		super(tasks);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.internal.debug.core.pdi.request.AbstractEventResultRequest#storeResult(org.eclipse.ptp.core.util.TaskSet, org.eclipse.ptp.proxy.debug.event.IProxyDebugEvent)
	 */
	protected void storeResult(TaskSet rTasks, Object result) {
		if (result instanceof IProxyDebugInfoThreadsEvent) {
			results.put(rTasks, ((IProxyDebugInfoThreadsEvent)result).getThreadIds());
		}
		else {
			storeUnknownResult(rTasks, result);
		}
	}
}
