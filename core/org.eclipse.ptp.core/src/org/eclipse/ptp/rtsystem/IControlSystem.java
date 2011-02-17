/*******************************************************************************
 * Copyright (c) 2005 The Regents of the University of California. 
 * This material was produced under U.S. Government contract W-7405-ENG-36 
 * for Los Alamos National Laboratory, which is operated by the University 
 * of California for the U.S. Department of Energy. The U.S. Government has 
 * rights to use, reproduce, and distribute this software. NEITHER THE 
 * GOVERNMENT NOR THE UNIVERSITY MAKES ANY WARRANTY, EXPRESS OR IMPLIED, OR 
 * ASSUMES ANY LIABILITY FOR THE USE OF THIS SOFTWARE. If software is modified 
 * to produce derivative works, such modified software should be clearly marked, 
 * so as not to confuse it with the version available from LANL.
 * 
 * Additionally, this program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * LA-CC 04-115
 *******************************************************************************/

package org.eclipse.ptp.rtsystem;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.ptp.core.attributes.AttributeDefinitionManager;

/**
 * A Control System is a portion of a runtime system that handles controlling
 * jobs. This includes starting new jobs, terminating jobs, getting information
 * about running jobs and processes, etc.
 * 
 * @author Nathan DeBardeleben
 */

public interface IControlSystem {

	/**
	 * Get the attribute definition manager.
	 * 
	 * @return the attribute definition manager
	 * @since 5.0
	 */
	public AttributeDefinitionManager getAttributeDefinitionManager();

	/**
	 * Submits a job run using the submission ID and the attributes provided by
	 * the ILaunchConfiguration.
	 * 
	 * The job submission ID can be used to identify the new job that is
	 * created. The ID will be returned as an attribute on the new job.
	 * 
	 * This will call will result in either a new job event when the job is
	 * created by the resource manager or an error event if the job submission
	 * fails.
	 * 
	 * @param subId
	 *            job submission ID
	 * @param configuration
	 *            launch configuration
	 * @param mode
	 *            launch mode
	 * @throws CoreException
	 * @since 5.0
	 */
	public void submitJob(String subId, ILaunchConfiguration configuration, String mode) throws CoreException;

	/**
	 * Terminates a running job. The job identifier is used to locate the job by
	 * the control system.
	 * 
	 * @param jobId
	 *            ID of the job to terminate
	 * @throws CoreException
	 * @since 5.0
	 */
	public void terminateJob(String jobId) throws CoreException;

}