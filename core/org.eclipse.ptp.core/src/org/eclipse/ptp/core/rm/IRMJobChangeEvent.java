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
package org.eclipse.ptp.core.rm;

import java.util.Set;

/**
 * @since 5.0
 */
public interface IRMJobChangeEvent {
	/**
	 * Get resource manager on which the event occurred
	 * 
	 * @return
	 */
	public IResourceManager getResourceManager();

	/**
	 * Get new status of each job that changed
	 */
	public Set<IJobStatus> getJobStatus();
}
