/******************************************************************************
 * Copyright (c) 2009 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *
 *****************************************************************************/

package org.eclipse.ptp.launch;

import org.eclipse.ptp.core.elements.IPJob;
import org.eclipse.ptp.core.elements.attributes.JobAttributes;

/**
 * Interface for receiving notification of job state changes.
 * 
 * Note that only jobs launched by PTP are currently considered
 * for notification.
 * 
 */
public interface ILaunchNotification {
	/**
	 * Notify that a job's state has changed.
	 * 
	 * @param job job that caused state change
	 * @param state new state of the job
	 */
	public void jobStateChange(IPJob job, JobAttributes.State state);
}
