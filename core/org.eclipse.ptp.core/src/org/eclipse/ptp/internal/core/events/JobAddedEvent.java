/*******************************************************************************
 * Copyright (c) 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.internal.core.events;

import org.eclipse.ptp.core.jobs.IJobAddedEvent;
import org.eclipse.ptp.core.jobs.IJobStatus;

public class JobAddedEvent implements IJobAddedEvent {

	private final IJobStatus fJobStatus;

	public JobAddedEvent(IJobStatus jobStatus) {
		fJobStatus = jobStatus;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.core.events.IJobAddedEvent#getJobStatus()
	 */
	public IJobStatus getJobStatus() {
		return fJobStatus;
	}

}
