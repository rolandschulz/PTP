/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.rmsystem;

import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.IStreamsProxy;
import org.eclipse.ptp.core.attributes.AttributeManager;
import org.eclipse.ptp.core.elements.attributes.JobAttributes;

/**
 * @since 5.0
 */
public interface IJobStatus {

	/**
	 * Get the job ID of the job this status is for
	 * 
	 * @return job ID
	 */
	public String getJobId();

	/**
	 * Get job attributes
	 * 
	 * @return attributes about this job
	 */
	public AttributeManager getAttributes();

	/**
	 * Get the launch configuration used to launch this job
	 * 
	 * @return
	 */
	public ILaunchConfiguration getLaunchConfiguration();

	/**
	 * Get job status
	 * 
	 * @return state of the job
	 */
	public JobAttributes.State getState();

	/**
	 * @return
	 */
	public IStreamsProxy getStreamsProxy();
}
