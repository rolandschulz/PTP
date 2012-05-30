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
public abstract class AbstractRuntimeProcessesEvent implements IRuntimeProcessesEvent {

	private final String jobId;
	private final RangeSet processJobRanks;

	/**
	 * @param jobId
	 * @param processJobRanks
	 */
	public AbstractRuntimeProcessesEvent(String jobId, RangeSet processJobRanks) {
		this.jobId = jobId;
		this.processJobRanks = processJobRanks;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rtsystem.events.IRuntimeProcessesEvent#getJobId()
	 */
	public String getJobId() {
		return jobId;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rtsystem.events.IRuntimeProcessesEvent#getProcessJobRanks ()
	 */
	public RangeSet getProcessJobRanks() {
		return processJobRanks;
	}

}
