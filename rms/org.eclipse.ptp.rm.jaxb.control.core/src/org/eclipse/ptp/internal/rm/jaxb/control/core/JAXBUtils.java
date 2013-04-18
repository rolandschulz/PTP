/*******************************************************************************
 * Copyright (c) 2011, 2012 University of Illinois.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * 	Albert L. Rossi - design and implementation
 * 	Jeff Overbey - Environment Manager support
 ******************************************************************************/
package org.eclipse.ptp.internal.rm.jaxb.control.core;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.ptp.internal.rm.jaxb.control.core.messages.Messages;
import org.eclipse.ptp.internal.rm.jaxb.control.core.RemoteServicesDelegate;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.exception.RemoteConnectionException;

public final class JAXBUtils {

	/**
	 * tries to open connection if closed
	 * 
	 * @param connection
	 * @param progress
	 * @throws RemoteConnectionException
	 */
	public static void checkConnection(IRemoteConnection connection, IProgressMonitor monitor) throws RemoteConnectionException {
		if (connection != null) {
			if (!connection.isOpen()) {
				connection.open(monitor);
				if (!connection.isOpen()) {
					throw new RemoteConnectionException(Messages.RemoteConnectionError + connection.getAddress());
				}
			}
		} else {
			new RemoteConnectionException(Messages.RemoteConnectionError + connection);
		}
	}

	private static final Map<String, RemoteServicesDelegate> fDelegates = new HashMap<String, RemoteServicesDelegate>();

	/**
	 * @param jaxbServiceProvider
	 *            the configuration object containing resource manager specifics
	 */
	public JAXBUtils() {
	}

	private static String getName(String remoteId, String connName) {
		return remoteId + "." + connName; //$NON-NLS-1$
	}

	/**
	 * Reinitializes when the connection info has been changed on a cached resource manager.
	 * 
	 * @param monitor
	 * @return wrapper object for remote services, connections and file managers
	 * @throws CoreException
	 */
	public static RemoteServicesDelegate getRemoteServicesDelegate(String remoteId, String connName, IProgressMonitor monitor)
			throws CoreException {
		SubMonitor progress = SubMonitor.convert(monitor, 10);
		try {
			RemoteServicesDelegate delegate = fDelegates.get(getName(remoteId, connName));
			if (delegate == null) {
				delegate = new RemoteServicesDelegate(remoteId, connName);
				delegate.initialize(progress.newChild(5));
				fDelegates.put(getName(remoteId, connName), delegate);
			}
			/*
			 * Bug 370775 - Attempt to open the connection before using the delegate as the connection can be closed independently
			 * of the resource manager.
			 */
			IRemoteConnection conn = delegate.getRemoteConnection();
			if (!conn.isOpen()) {
				try {
					conn.open(progress.newChild(5));
				} catch (RemoteConnectionException e) {
					// Just use the closed connection
				}
			}
			return delegate;
		} finally {
			if (monitor != null) {
				monitor.done();
			}
		}
	}
}
