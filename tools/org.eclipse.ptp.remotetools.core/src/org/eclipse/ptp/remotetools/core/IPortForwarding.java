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

/**
 * Represents a port forwarding created by {@link IRemotePortForwardingTools}.
 * <p>
 * In order to turn off port forwarding, call {@link #release()} or
 * {@link IRemotePortForwardingTools#releaseForwarding(IPortForwarding)}.
 * <p>
 * The information that describes the port forwarding is valid until the
 * forwarding is turned off. The check if the forwarding is active, call
 * {@link #isActive()}.
 * 
 * @author Daniel Felix Ferber
 */
public interface IPortForwarding {
	/** If the port forwarding is turned on. */
	public abstract boolean isActive();

	/**
	 * Turn off the port forwarding. It is facility method that calls
	 * {@link IRemotePortForwardingTools#releaseForwarding(IPortForwarding)}.
	 * 
	 * @throws RemoteConnectionException
	 *             The connection is not in a valid state anymore.
	 * @throws PortForwardingException
	 *             Failed to turn off the remote port forwarding. The forwarding
	 *             was already turned off before.
	 * @throws CancelException 
	 */
	public abstract void release() throws RemoteConnectionException,
			PortForwardingException, CancelException;
}
