/*******************************************************************************
 * Copyright (c) 2009, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.ptp.internal.rdt.server.dstore.core;

import java.io.IOException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.dstore.core.client.ClientConnection;
import org.eclipse.dstore.core.client.ConnectionStatus;
import org.eclipse.dstore.core.model.DataElement;
import org.eclipse.dstore.core.model.DataStore;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.ptp.internal.rdt.server.dstore.messages.Messages;
import org.eclipse.ptp.rdt.ui.subsystems.StatusMonitor;
import org.eclipse.ptp.remote.core.exception.RemoteConnectionException;
import org.eclipse.ptp.remote.server.core.AbstractRemoteServerRunner;
import org.eclipse.swt.widgets.Display;

public class DStoreServer extends AbstractRemoteServerRunner {
	private enum DStoreState {
		WAITING_FOR_SUCCESS_STRING, WAITING_FOR_PORT, COMPLETED
	}

	public static String SERVER_ID = "org.eclipse.ptp.rdt.server.dstore.RemoteToolsDStoreServer"; //$NON-NLS-1$;

	private DStoreState fState = DStoreState.WAITING_FOR_SUCCESS_STRING;
	private ClientConnection fDStoreConnection = null;
	private int fDStorePort = 0;
	private final int TIMEOUT = 60000; // wait 60 seconds for server to start

	private static final String SUCCESS_STRING = "Server Started Successfully"; //$NON-NLS-1$

	public DStoreServer() {
		super(Messages.DStoreServer_0);
	}

	/**
	 * Get the data store associated with the client connection. Currently, if
	 * the server fails to start we will return a null. This may cause an NPE in
	 * the indexer subsystem.
	 * 
	 * @return DataStore
	 */
	public DataStore getDataStore() {
		if (getServerState() != ServerState.RUNNING) {
			try {
				if (getServerState() == ServerState.STOPPED) {
					startServer(new NullProgressMonitor());
				}
				waitForServerStart(TIMEOUT);
			} catch (IOException e) {
				final IStatus status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, 0, e.getMessage(), e);
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						ErrorDialog.openError(Display.getDefault().getActiveShell(), Messages.DStoreServer_serverError,
								Messages.DStoreServer_unableToStart, status);
					}
				});
			}
		}
		if (getServerState() == ServerState.RUNNING) {
			ClientConnection conn = getClientConnection();
			assert conn != null;
			return conn.getDataStore();
		}
		return null;
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
	protected void doServerFinished(IProgressMonitor monitor) {
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
	protected boolean doServerStarting(IProgressMonitor monitor) {
		try {
			fState = DStoreState.WAITING_FOR_SUCCESS_STRING;
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
	protected boolean doServerStarted(IProgressMonitor monitor) {
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
				DataElement res = dataStore.activateMiner("org.eclipse.ptp.internal.rdt.core.miners.CDTMiner"); //$NON-NLS-1$
				StatusMonitor smonitor = StatusMonitor.getStatusMonitorFor(getRemoteConnection(), dataStore);
				try {
					smonitor.waitForUpdate(res, subMon.newChild(5));
				} catch (InterruptedException e) {
					// Data store will be disconnected if error occurs
					return false;
				}
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
		case WAITING_FOR_SUCCESS_STRING:
			if (output.startsWith(SUCCESS_STRING)) {
				fState = DStoreState.WAITING_FOR_PORT;
				if (DebugUtil.SERVER_TRACING) {
					System.out.println(Messages.DStoreServer_4);
				}
			}
			break;

		case WAITING_FOR_PORT:
			if (output.matches("^[0-9]+$")) { //$NON-NLS-1$
				fDStorePort = Integer.parseInt(output);
				fState = DStoreState.COMPLETED;
				if (DebugUtil.SERVER_TRACING) {
					System.out.println(Messages.DStoreServer_5 + fDStorePort);
				}
			}
			break;

		case COMPLETED:
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
