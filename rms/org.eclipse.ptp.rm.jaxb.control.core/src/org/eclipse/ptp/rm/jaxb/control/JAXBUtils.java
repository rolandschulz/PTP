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
package org.eclipse.ptp.rm.jaxb.control;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.ptp.core.util.CoreExceptionUtils;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.IRemoteConnectionChangeEvent;
import org.eclipse.ptp.remote.core.IRemoteConnectionChangeListener;
import org.eclipse.ptp.remote.core.RemoteServicesDelegate;
import org.eclipse.ptp.remote.core.exception.RemoteConnectionException;
import org.eclipse.ptp.rm.jaxb.control.internal.ICommandJob;
import org.eclipse.ptp.rm.jaxb.control.internal.messages.Messages;

public final class JAXBUtils {

	/*
	 * copied from AbstractToolRuntimeSystem; the RM should shut down when the remote connection is closed
	 */
	private class ConnectionChangeListener implements IRemoteConnectionChangeListener {
		public ConnectionChangeListener() {
			// Nothing
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ptp.remote.core.IRemoteConnectionChangeListener# connectionChanged
		 * (org.eclipse.ptp.remote.core.IRemoteConnectionChangeEvent)
		 */
		public void connectionChanged(IRemoteConnectionChangeEvent event) {
			if (event.getType() == IRemoteConnectionChangeEvent.CONNECTION_ABORTED
					|| event.getType() == IRemoteConnectionChangeEvent.CONNECTION_CLOSED) {
			}
		}
	}

	/**
	 * tries to open connection if closed
	 * 
	 * @param connection
	 * @param progress
	 * @throws RemoteConnectionException
	 */
	public static void checkConnection(IRemoteConnection connection, SubMonitor progress) throws RemoteConnectionException {
		if (connection != null) {
			if (!connection.isOpen()) {
				connection.open(progress.newChild(25));
				if (!connection.isOpen()) {
					throw new RemoteConnectionException(Messages.RemoteConnectionError + connection.getAddress());
				}
			}
		} else {
			new RemoteConnectionException(Messages.RemoteConnectionError + connection);
		}
	}

	/**
	 * Checks to see if there was an exception thrown by the run method.
	 * 
	 * @param job
	 * @throws CoreException
	 *             if the job execution raised and exception
	 */
	private static void checkJobForError(ICommandJob job) throws CoreException {
		IStatus status = job.getRunStatus();
		if (status != null && status.getSeverity() == IStatus.ERROR) {
			Throwable t = status.getException();
			if (t instanceof CoreException) {
				throw (CoreException) t;
			} else {
				throw CoreExceptionUtils.newException(status.getMessage(), t);
			}
		}
	}

	private ConnectionChangeListener connectionListener;
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
