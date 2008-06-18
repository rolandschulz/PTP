/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/

package org.eclipse.ptp.core.elements.events;



/**
 * This event is sent when a job submission fails.
 * 
 * @see org.eclipse.ptp.core.elements.listeners.IResourceManagerListener
 */
public interface IResourceManagerSubmitJobErrorEvent extends IResourceManagerErrorEvent {
	
	/**
	 * Get the job submission id that was used in the original submission
	 * 
	 * @return id job submission id of failed submission
	 */
	public String getJobSubmissionId();
}
