/*******************************************************************************
 * Copyright (c) 2010 Los Alamos National Laboratory and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	LANL - Initial API and implementation
 *******************************************************************************/

package org.eclipse.ptp.rtsystem.events;

import org.eclipse.ptp.utils.core.RangeSet;

/**
 * @author Randy M. Roberts
 * @since 4.0
 * 
 */
@Deprecated
public interface IRuntimeProcessesEvent extends IRuntimeEvent {
	/**
	 * @return the id of the job for which this process event pertains
	 */
	public String getJobId();

	/**
	 * @return the set of job ranks that specify the affected ranges
	 */
	public RangeSet getProcessJobRanks();
}
