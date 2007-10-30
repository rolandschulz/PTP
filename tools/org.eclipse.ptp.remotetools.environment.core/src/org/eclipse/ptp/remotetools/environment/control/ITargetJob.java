/**
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *
 */
package org.eclipse.ptp.remotetools.environment.control;

import org.eclipse.ptp.remotetools.core.IRemoteExecutionManager;

/**
 * Describes a job that will run on a remote host.
 * <p>
 * Job is a collection of sequential operations executed using the IRemoteExecutionManager
 * on the target environment.
 * <p> 
 * The ITargetJob be run as a thread inside the Job Controller.
 *
 * @author Daniel Felix Ferber
 * @since 1.1
 */
public interface ITargetJob {
	/**
	 * Implementation of the job.
	 * <p>
	 * The IRemoteExecutionManager that is capable of doing the operations
	 * will be provided automatically.
	 */
	void run(IRemoteExecutionManager manager);	
}
