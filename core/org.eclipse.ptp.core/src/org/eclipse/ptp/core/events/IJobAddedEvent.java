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
package org.eclipse.ptp.core.events;

import org.eclipse.ptp.rmsystem.IJobStatus;

/**
 * This event is sent when a job is added
 * 
 * @see org.eclipse.ptp.core.elements.listeners.IJobListener
 * @since 6.0
 */
public interface IJobAddedEvent {

	/**
	 * Get the status of the added job.
	 * 
	 * @return job status
	 */
	public IJobStatus getJobStatus();
}
