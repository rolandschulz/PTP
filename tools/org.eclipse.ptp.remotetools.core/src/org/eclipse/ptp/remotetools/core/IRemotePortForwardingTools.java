/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.remotetools.core;

import org.eclipse.ptp.remotetools.exception.CancelException;
import org.eclipse.ptp.remotetools.exception.PortForwardingException;
import org.eclipse.ptp.remotetools.exception.RemoteConnectionException;


public interface IRemotePortForwardingTools {
	public IRemotePortForwarding forwardRemotePort(int remotePort, int localPort) throws RemoteConnectionException, PortForwardingException, CancelException;
	public IRemotePortForwarding forwardRemotePort(int remotePort, String localAddress, int localPort) throws RemoteConnectionException, PortForwardingException, CancelException;

//	public LocalPortForwarding forwardLocalPort(int localPort, int remotePort) throws RemoteConnectionException;
//	public LocalPortForwarding forwardLocalPort(int localPort, String remoteAddress, int remotePort) throws RemoteConnectionException;

	public void releaseForwarding(IPortForwarding forwading) throws RemoteConnectionException, PortForwardingException;
	
	public IRemotePortForwarding getRemotePortForwarding(int remotePort) throws RemoteConnectionException, PortForwardingException;
//	public LocalPortForwarding getLocalPortForwarding(int localPort);
	
}
