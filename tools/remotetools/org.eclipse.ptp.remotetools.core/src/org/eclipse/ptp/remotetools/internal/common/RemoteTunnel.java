/******************************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *
 *****************************************************************************/
package org.eclipse.ptp.remotetools.internal.common;

import org.eclipse.ptp.remotetools.core.IRemoteTunnel;

/**
 * Default implementation of <code>IRemoteTunnel</code>
 * @author Daniel Felix Ferber
 *
 */
public class RemoteTunnel implements IRemoteTunnel {
	int localPort;
	int portOnRemoteHost;
	String addressOnRemoteHost;
	
	public RemoteTunnel(int localPort, int portOnRemoteHost, String addressOnRemoteHost) {
		super();
		this.localPort = localPort;
		this.portOnRemoteHost = portOnRemoteHost;
		this.addressOnRemoteHost = addressOnRemoteHost;
	}
	
	public String getAddressOnRemoteHost() {
		return addressOnRemoteHost;
	}
	public int getLocalPort() {
		return localPort;
	}
	public int getPortOnRemoteHost() {
		return portOnRemoteHost;
	}
	public boolean equals(Object obj) {
		RemoteTunnel other = (RemoteTunnel) obj;
		return (other.localPort == this.localPort)
		&& (other.addressOnRemoteHost == this.addressOnRemoteHost) 
		&& (other.portOnRemoteHost == this.portOnRemoteHost);
	}
	
}
