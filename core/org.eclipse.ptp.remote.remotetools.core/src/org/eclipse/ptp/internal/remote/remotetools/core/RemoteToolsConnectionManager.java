/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.internal.remote.remotetools.core;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.osgi.util.NLS;
import org.eclipse.ptp.internal.remote.remotetools.core.messages.Messages;
import org.eclipse.ptp.remotetools.environment.EnvironmentPlugin;
import org.eclipse.ptp.remotetools.environment.core.ITargetElement;
import org.eclipse.ptp.remotetools.environment.core.ITargetElementStatus;
import org.eclipse.ptp.remotetools.environment.core.ITargetEventListener;
import org.eclipse.ptp.remotetools.environment.core.TargetElement;
import org.eclipse.ptp.remotetools.environment.core.TargetEnvironmentManager;
import org.eclipse.ptp.remotetools.environment.core.TargetTypeElement;
import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.remote.core.IRemoteConnectionChangeEvent;
import org.eclipse.remote.core.IRemoteConnectionManager;
import org.eclipse.remote.core.IRemoteConnectionWorkingCopy;
import org.eclipse.remote.core.IRemoteServices;
import org.eclipse.remote.core.exception.RemoteConnectionException;

public class RemoteToolsConnectionManager implements IRemoteConnectionManager, ITargetEventListener {
	private final IRemoteServices fRemoteServices;
	private final TargetTypeElement fRemoteHost;
	private final Map<String, IRemoteConnection> fConnections = new HashMap<String, IRemoteConnection>();

	/**
	 * @since 4.0
	 */
	public RemoteToolsConnectionManager(IRemoteServices services) {
		fRemoteServices = services;
		fRemoteHost = RemoteToolsServices.getTargetTypeElement();
		TargetEnvironmentManager targetMgr = EnvironmentPlugin.getDefault().getTargetsManager();
		targetMgr.addModelEventListener(this);
		refreshConnections();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.remote.core.IRemoteConnectionManager#getConnection(java
	 * .lang.String)
	 */
	public IRemoteConnection getConnection(String name) {
		refreshConnections();
		return fConnections.get(name);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.remote.core.IRemoteConnectionManager#getConnection(java
	 * .net.URI)
	 */
	/**
	 * @since 4.0
	 */
	public IRemoteConnection getConnection(URI uri) {
		String connName = RemoteToolsFileSystem.getConnectionNameFor(uri);
		if (connName != null) {
			return getConnection(connName);
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.remote.core.IRemoteConnectionManager#getConnections()
	 */
	public List<IRemoteConnection> getConnections() {
		refreshConnections();
		return new ArrayList<IRemoteConnection>(fConnections.values());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.remotetools.environment.core.ITargetEventListener#
	 * handleStateChangeEvent(int,
	 * org.eclipse.ptp.remotetools.environment.core.ITargetElement)
	 */
	/**
	 * @since 5.0
	 */
	public void handleStateChangeEvent(int event, ITargetElement element) {
		IRemoteConnection conn = fConnections.get(element.getName());
		if (conn != null) {
			switch (event) {
			case ITargetElementStatus.RESUMED:
				((RemoteToolsConnection) conn).fireConnectionChangeEvent(IRemoteConnectionChangeEvent.CONNECTION_OPENED);
				break;
			case ITargetElementStatus.STOPPED:
				((RemoteToolsConnection) conn).fireConnectionChangeEvent(IRemoteConnectionChangeEvent.CONNECTION_CLOSED);
				break;
			default:
				/*
				 * Ignore other status types
				 */
				break;
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.remote.core.IRemoteConnectionManager#newConnection(java
	 * .lang.String, java.util.Map)
	 */
	/**
	 * @since 5.0
	 */
	public IRemoteConnectionWorkingCopy newConnection(String name) throws RemoteConnectionException {
		if (getConnection(name) != null) {
			throw new RemoteConnectionException(NLS.bind(Messages.RemoteToolsConnectionManager_Connection_with_name_already_exists, name));
		}
		TargetElement element = newTargetElement(name);
		return createConnection(element).getWorkingCopy();
	}

	public TargetElement newTargetElement(String name) {
		String id = EnvironmentPlugin.getDefault().getEnvironmentUniqueID();
		return new TargetElement(fRemoteHost, name, id);
	}

	public void addConnection(RemoteToolsConnection connection) {
		fRemoteHost.addElement(connection.getTargetElement());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.remote.core.IRemoteConnectionManager#removeConnection
	 * (org.eclipse.ptp.remote.core.IRemoteConnection)
	 */
	public void removeConnection(IRemoteConnection conn) throws RemoteConnectionException {
		if (!(conn instanceof RemoteToolsConnection)) {
			throw new RemoteConnectionException(Messages.RemoteToolsConnectionManager_invalidConnectionType);

		}
		if (conn.isOpen()) {
			throw new RemoteConnectionException(Messages.RemoteToolsConnectionManager_cannotRemoveOpenConnection);
		}
		fConnections.remove(conn);
		((RemoteToolsConnection) conn).dispose();
	}

	/**
	 * Create a connection using information from the target element.
	 * 
	 * @param element
	 *            target element
	 * @return new remote tools connection
	 * @throws RemoteConnectionException
	 */
	private IRemoteConnection createConnection(TargetElement element) {
		return new RemoteToolsConnection(element.getName(), element, fRemoteServices);
	}

	/**
	 * Refresh the list of fConnections that we know about. Deals with
	 * connection that are added or deleted by another entity.
	 */
	private void refreshConnections() {
		if (fRemoteHost != null) {
			Map<String, IRemoteConnection> newConns = new HashMap<String, IRemoteConnection>();
			for (Object obj : fRemoteHost.getElements()) {
				if (obj instanceof TargetElement) {
					TargetElement element = (TargetElement) obj;
					IRemoteConnection conn = fConnections.get(element.getName());
					if (conn == null) {
						conn = createConnection(element);
					}
					newConns.put(element.getName(), conn);
				}
			}
			fConnections.clear();
			fConnections.putAll(newConns);
		}
	}
}
