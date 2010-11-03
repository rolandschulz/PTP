/*******************************************************************************
 * Copyright (c) 2009, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.ptp.rdt.server.dstore.core;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.dstore.core.client.ClientConnection;
import org.eclipse.dstore.core.client.ConnectionStatus;
import org.eclipse.dstore.core.model.DataStore;
import org.eclipse.ptp.rdt.server.dstore.internal.core.DebugUtil;
import org.eclipse.ptp.rdt.server.dstore.messages.Messages;
import org.eclipse.ptp.remote.core.exception.RemoteConnectionException;
import org.eclipse.ptp.remote.launch.core.AbstractRemoteServerRunner;

public class DStoreServer extends AbstractRemoteServerRunner {
	private enum DStoreState {
		STARTING, WAITING, STARTED
	}

	public static String SERVER_ID = "org.eclipse.ptp.rdt.server.dstore.RemoteToolsDStoreServer"; //$NON-NLS-1$;

	private DStoreState fState = DStoreState.STARTING;
	private ClientConnection fDStoreConnection = null;
	private int fDStorePort = 0;

	private static final String SUCCESS_STRING = "Server Started Successfully"; //$NON-NLS-1$

	public DStoreServer() {
		super(Messages.DStoreServer_0);
	}

	/**
	 * Get the data store associated with the client connection.
	 * 
	 * @return DataStore
	 */
	public DataStore getDataStore() {
		ClientConnection conn = getClientConnection();
		assert conn != null;
		return conn.getDataStore();
	}

	/**
	 * Get the client connection. Creates a new client connection on first call.
	 * 
	 * @return client connection
	 */
	private ClientConnection getClientConnection() {
		if (fDStoreConnection == null && getRemoteConnection() != null) {
			fDStoreConnection = new ClientConnection(getRemoteConnection().getName());
		}
		return fDStoreConnection;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.remote.launch.core.AbstractRemoteServerRunner#doFinishServer
	 * ()
	 */
	@Override
	protected void doFinishServer(IProgressMonitor monitor) {
		try {
			if (fDStoreConnection != null) {
				fDStoreConnection.disconnect();
				fDStoreConnection = null;
			}
		} finally {
			if (monitor != null) {
				monitor.done();
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.remote.launch.core.AbstractRemoteServerRunner#doRestartServer
	 * ()
	 */
	@Override
	protected boolean doRestartServer(IProgressMonitor monitor) {
		try {
			fState = DStoreState.STARTING;
			return true;
		} finally {
			if (monitor != null) {
				monitor.done();
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.remote.launch.core.AbstractRemoteServerRunner#doStartServer
	 * ()
	 */
	@Override
	protected boolean doStartServer(IProgressMonitor monitor) {
		SubMonitor subMon = SubMonitor.convert(monitor, 100);
		try {
			int port;
			try {
				port = getRemoteConnection().forwardLocalPort("localhost", fDStorePort, subMon.newChild(10)); //$NON-NLS-1$
			} catch (RemoteConnectionException e) {
				if (DebugUtil.SERVER_TRACING) {
					System.err.println(Messages.DStoreServer_1 + e.getLocalizedMessage());
				}
				return false;
			}
			getClientConnection().setHost("localhost"); //$NON-NLS-1$
			getClientConnection().setPort(Integer.toString(port));
			if (DebugUtil.SERVER_TRACING) {
				System.out.println(Messages.DStoreServer_2);
			}
			ConnectionStatus status = getClientConnection().connect(null, 0);
			if (status.isConnected()) {
				DataStore dataStore = getClientConnection().getDataStore();
				dataStore.showTicket(null);
				dataStore.registerLocalClassLoader(getClass().getClassLoader());
				if (DebugUtil.SERVER_TRACING) {
					System.out.println(Messages.DStoreServer_3);
				}
				return true;
			}
			return false;
		} finally {
			if (monitor != null) {
				monitor.done();
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.remote.launch.core.AbstractRemoteServerRunner#
	 * doVerifyServerRunningFromStderr(java.lang.String)
	 */
	@Override
	protected boolean doVerifyServerRunningFromStderr(String output) {
		switch (fState) {
		case STARTING:
			if (output.startsWith(SUCCESS_STRING)) {
				fState = DStoreState.WAITING;
			}
			break;

		case WAITING:
			if (output.matches("^[0-9]+$")) { //$NON-NLS-1$
				fDStorePort = Integer.parseInt(output);
				fState = DStoreState.STARTED;
			}
			break;

		case STARTED:
			return true;
		}

		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.remote.launch.core.AbstractRemoteServerRunner#
	 * doVerifyServerRunningFromStdout(java.lang.String)
	 */
	@Override
	protected boolean doVerifyServerRunningFromStdout(String output) {
		return false;
	}
}
