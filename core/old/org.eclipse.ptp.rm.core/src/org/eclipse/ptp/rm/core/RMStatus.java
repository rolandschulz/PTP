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
 * Provide consistent labeling of element status<br>
 * OK element is up and able to accept jobs, etc.<br>
 * DOWN element is down, reason will have to be provided in other attributes<br>
 * UNAVAILABLE element is unable to accept jobs, etc., reason will have to be
 * provied in other attributes<br>
 * ALLOCATED_OTHER element is up but unable to accept jobs due to allocations by
 * other users<br>
 * UNKNOWN the status is unknown
 * 
 * @author rsqrd
 * 
 */
public final class RMStatus {
	/**
	 * OK element is up and able to accept jobs, etc.
	 */
	public static final RMStatus OK = new RMStatus("OK");

	/**
	 * DOWN element is down, reason will have to be provided in other attributes
	 */
	public static final RMStatus DOWN = new RMStatus("DOWN");

	/**
	 * UNAVAILABLE element is unable to accept jobs, etc., reason will have to
	 * be provied in other attributes
	 */
	public static final RMStatus UNAVAILABLE = new RMStatus("UNAVAILABLE");

	/**
	 * element is up but unable to accept jobs due to allocations by other users
	 */
	public static final RMStatus ALLOCATED_OTHER = new RMStatus(
			"ALLOCATED_OTHER");

	/**
	 * the status is unknown
	 */
	public static final RMStatus UNKNOWN = new RMStatus("UNKNOWN");

	private final String status;

	private RMStatus(String status) {
		this.status = status;
	}

	public String toString() {
		return status;
	}
}
