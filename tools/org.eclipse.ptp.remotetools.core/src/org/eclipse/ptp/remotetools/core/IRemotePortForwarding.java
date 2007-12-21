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

/**
 * Represents a remote port forwarding created by {@link IRemotePortForwardingTools}.
 * <p>
 * The remote port is forwarded to a host:port accessibly to the local host.
 * <p>
 * Check inherited {@link IPortForwarding} for more methods.
 */
public interface IRemotePortForwarding extends IPortForwarding {
	/** The port on the host where the remote port is forwarded to. */
	public abstract int getLocalPort();

	/** The host where the remote port is forwarded to. */
	public abstract String getLocalAddress();

	/** The remote port that is being forwarded. */
	public abstract int getRemotePort();
}
