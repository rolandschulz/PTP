/*******************************************************************************
 * Copyright (c) 2006 The Regents of the University of California. 
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
package org.eclipse.ptp.rm.core;

/**
 * Provide consistent labeling of job status<br>
 * PENDING job is pending in queue<br>
 * RUNNING job is running normally<br>
 * SUSPENDED job is suspended, reason will have to be provided in other
 * attributes<br>
 * DONE job has completed normally<br>
 * EXIT job has completed abnormally, reason will have to be provide in other
 * attributes<br>
 * UNKNOWN job status is unknown
 * 
 * @author rsqrd
 * 
 */
public final class RMJobStatus {
	/**
	 * job is pending in queue
	 */
	public static final RMJobStatus PENDING = new RMJobStatus("PENDING");

	/**
	 * job is running normally
	 */
	public static final RMJobStatus RUNNING = new RMJobStatus("RUNNING");

	/**
	 * job is suspended, reason will have to be provided in other attributes
	 */
	public static final RMJobStatus SUSPENDED = new RMJobStatus("SUSPENDED");

	/**
	 * job has completed normally
	 */
	public static final RMJobStatus DONE = new RMJobStatus("DONE");

	/**
	 * job has completed abnormally, reason will have to be provide in other
	 * attributes
	 */
	public static final RMJobStatus EXIT = new RMJobStatus("EXIT");

	/**
	 * job status is unknown
	 */
	public static final RMJobStatus UNKNOWN = new RMJobStatus("UNKNOWN");

	private final String status;

	private RMJobStatus(String status) {
		this.status = status;
	}

	public String toString() {
		return status;
	}

}
