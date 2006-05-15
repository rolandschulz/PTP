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
 * Determine which remote (or local) host's resource manager to proxy.  Determine
 * which resource manager on the host to proxy.  Provide hosts's status
 * 
 * @author rsqrd
 * 
 */
public class RMResourceManagerHost {
	private final String ipAddress;

	private final String name;

	private final String resourceMangerType;

	private RMStatus status = RMStatus.UNKNOWN;

	public RMResourceManagerHost(String ipAddress, String name,
			String resourceManagerType) {
		this.ipAddress = ipAddress;
		this.name = name;
		this.resourceMangerType = resourceManagerType;
	}

	/**
	 * @return the hosts ip address in ddd.ddd.ddd.ddd format
	 */
	public String getIpAddress() {
		return ipAddress;
	}

	/**
	 * @return the name of the host
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return an arbitrary string that uniquely identifies
	 * the resource manager on this host, e.g. "LSF"
	 */
	public String getResourceMangerType() {
		return resourceMangerType;
	}

	/**
	 * @return the status of this host
	 */
	public RMStatus getStatus() {
		return status;
	}

	/**
	 * @param status the new status for this host
	 */
	public void setStatus(RMStatus status) {
		this.status = status;
	}
}
