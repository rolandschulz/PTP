/*******************************************************************************
 * Copyright (c) 2007, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.internal.remote.rse.core;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.dstore.core.model.DataStore;
import org.eclipse.ptp.internal.remote.rse.core.messages.Messages;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.IRemoteConnectionChangeEvent;
import org.eclipse.ptp.remote.core.IRemoteConnectionChangeListener;
import org.eclipse.ptp.remote.core.IRemoteServices;
import org.eclipse.ptp.remote.core.IUserAuthenticator;
import org.eclipse.ptp.remote.core.exception.RemoteConnectionException;
import org.eclipse.ptp.remote.core.exception.UnableToForwardPortException;
import org.eclipse.rse.connectorservice.dstore.DStoreConnectorService;
import org.eclipse.rse.connectorservice.dstore.DStoreConnectorServiceManager;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.subsystems.CommunicationsEvent;
import org.eclipse.rse.core.subsystems.ICommunicationsListener;
import org.eclipse.rse.core.subsystems.IConnectorService;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
import org.eclipse.rse.services.dstore.IDStoreService;
import org.eclipse.rse.services.shells.IShellService;
import org.eclipse.rse.subsystems.files.core.model.RemoteFileUtility;
import org.eclipse.rse.subsystems.files.core.servicesubsystem.IFileServiceSubSystem;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFileSubSystem;
import org.eclipse.rse.subsystems.shells.core.subsystems.servicesubsystem.IShellServiceSubSystem;

public class RSEConnection implements IRemoteConnection {
	private IShellService fShellService = null;
	private ISubSystem fSubSystem = null;
	private Map<String, String> fEnv = null;
	private Map<String, String> fProperties = null;
	private IPath fWorkingDir = null;

	private final IHost fRseHost;
	private final IRemoteConnection fConnection = this;
	private final IRemoteServices fRemoteServices;
	private final ListenerList fListeners = new ListenerList();

	private final ICommunicationsListener commsListener = new ICommunicationsListener() {

		public void communicationsStateChange(CommunicationsEvent event) {
			switch (event.getState()) {
			case CommunicationsEvent.AFTER_CONNECT:
				fireConnectionChangeEvent(IRemoteConnectionChangeEvent.CONNECTION_OPENED);
				break;

			case CommunicationsEvent.AFTER_DISCONNECT:
				fireConnectionChangeEvent(IRemoteConnectionChangeEvent.CONNECTION_CLOSED);
				break;

			case CommunicationsEvent.CONNECTION_ERROR:
				fireConnectionChangeEvent(IRemoteConnectionChangeEvent.CONNECTION_ABORTED);
				break;
			}
		}

		public boolean isPassiveCommunicationsListener() {
			return true;
		}
	};

	/**
	 * @since 4.0
	 */
	public RSEConnection(IHost host, IRemoteServices services) {
		fRseHost = host;
		fRemoteServices = services;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.remote.core.IRemoteConnection#addConnectionChangeListener
	 * (org.eclipse.ptp.remote.core.IRemoteConnectionChangeListener)
	 */
	public void addConnectionChangeListener(IRemoteConnectionChangeListener listener) {
		fListeners.add(listener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.remote.IRemoteConnection#close()
	 */
	public void close() {
		if (fSubSystem != null && fSubSystem.isConnected()) {
			try {
				fSubSystem.disconnect();
			} catch (Exception e) {
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.remote.IRemoteConnection#forwardLocalPort(int, java.lang.String, int)
	 */
	public void forwardLocalPort(int localPort, String fwdAddress, int fwdPort) throws RemoteConnectionException {
		throw new UnableToForwardPortException(Messages.RSEConnection_noPortFwd);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.remote.IRemoteConnection#forwardLocalPort(java.lang.String , int,
	 * org.eclipse.core.runtime.IProgressMonitor)
	 */
	public int forwardLocalPort(String fwdAddress, int fwdPort, IProgressMonitor monitor) throws RemoteConnectionException {
		throw new UnableToForwardPortException(Messages.RSEConnection_noPortFwd);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.remote.IRemoteConnection#forwardRemotePort(int, java.lang.String, int)
	 */
	public void forwardRemotePort(int remotePort, String fwdAddress, int fwdPort) throws RemoteConnectionException {
		throw new UnableToForwardPortException(Messages.RSEConnection_noPortFwd);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.remote.IRemoteConnection#forwardRemotePort(java.lang. String, int,
	 * org.eclipse.core.runtime.IProgressMonitor)
	 */
	public int forwardRemotePort(String fwdAddress, int fwdPort, IProgressMonitor monitor) throws RemoteConnectionException {
		throw new UnableToForwardPortException(Messages.RSEConnection_noPortFwd);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.remote.IRemoteConnection#getAddress()
	 */
	public String getAddress() {
		return fRseHost.getHostName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.remote.core.IRemoteConnection#getAttributes()
	 */
	public Map<String, String> getAttributes() {
		// TODO implement
		return new HashMap<String, String>();
	}

	/**
	 * Get the DStore connector service for this fConnection.
	 * 
	 * @return connector service for DStores or null if this fConnection does not support DStore
	 */
	public DataStore getDataStore() {
		IConnectorService connector = DStoreConnectorServiceManager.getInstance().getConnectorService(fRseHost,
				IDStoreService.class);
		if (connector != null && connector instanceof DStoreConnectorService) {
			return ((DStoreConnectorService) connector).getDataStore();
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.remote.core.IRemoteConnection#getEnv()
	 */
	public Map<String, String> getEnv() {
		if (fEnv == null) {
			fEnv = new HashMap<String, String>();

			try {
				String[] env = fShellService.getHostEnvironment();
				for (String var : env) {
					String[] kv = var.split("="); //$NON-NLS-1$
					if (kv.length == 2) {
						fEnv.put(kv[0], kv[1]);
					}
				}
			} catch (SystemMessageException e) {
			}
		}
		return Collections.unmodifiableMap(fEnv);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.remote.core.IRemoteConnection#getEnv(java.lang.String)
	 */
	public String getEnv(String name) {
		if (fEnv == null) {
			getEnv();
		}
		return fEnv.get(name);
	}

	/**
	 * Get RSE host object
	 * 
	 * @return IHost
	 */
	public IHost getHost() {
		return fRseHost;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.remote.IRemoteConnection#getName()
	 */
	public String getName() {
		return fRseHost.getAliasName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.remote.core.IRemoteConnection#getPort()
	 */
	public int getPort() {
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.remote.core.IRemoteConnection#getProperty(java.lang.String )
	 */
	public String getProperty(String key) {
		loadProperties();
		return fProperties.get(key);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.remote.core.IRemoteConnection#getRemoteServices()
	 */
	/**
	 * @since 4.0
	 */
	public IRemoteServices getRemoteServices() {
		return fRemoteServices;
	}

	/**
	 * Get the shell service for this fConnection
	 * 
	 * @return shell service
	 */
	public IShellService getRemoteShellService() {
		return fShellService;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.remote.core.IRemoteConnection#getUsername()
	 */
	public String getUsername() {
		return fRseHost.getDefaultUserId();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.remote.IRemoteFileManager#getWorkingDirectory(org.eclipse .core.runtime.IProgressMonitor)
	 */
	/**
	 * @since 4.0
	 */
	public String getWorkingDirectory() {
		if (fWorkingDir == null) {
			IFileServiceSubSystem fileService = getFileServiceSubSystem(getHost());
			if (fileService != null) {
				fWorkingDir = new Path(fileService.getFileService().getUserHome().getAbsolutePath());
			} else {
				fWorkingDir = new Path("/"); //$NON-NLS-1$
			}
		}
		return fWorkingDir.toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.remote.IRemoteConnection#isOpen()
	 */
	public boolean isOpen() {
		return fSubSystem != null && fSubSystem.isConnected();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.remote.IRemoteConnection#open()
	 */
	public void open(IProgressMonitor monitor) throws RemoteConnectionException {
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}

		try {
			RSECorePlugin.waitForInitCompletion();
		} catch (InterruptedException e) {
			RSEAdapterCorePlugin.log(e);
			return;
		}

		if (!fSubSystem.isConnected()) {
			try {
				SubMonitor subMon = SubMonitor.convert(monitor);
				fSubSystem.connect(subMon.newChild(1), false);
			} catch (Exception e) {
				throw new RemoteConnectionException(e.getMessage());
			}

			if (!fSubSystem.isConnected()) {
				throw new RemoteConnectionException(Messages.RSEConnection_noShellService);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.remote.core.IRemoteConnection#open(org.eclipse.ptp.remote.core.IUserAuthenticator,
	 * org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void open(IUserAuthenticator authenticator, IProgressMonitor monitor) throws RemoteConnectionException {
		throw new RemoteConnectionException(Messages.RSEConnection_Operation_not_supported);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.remote.core.IRemoteConnection#removeConnectionChangeListener
	 * (org.eclipse.ptp.remote.core.IRemoteConnectionChangeListener)
	 */
	public void removeConnectionChangeListener(IRemoteConnectionChangeListener listener) {
		fListeners.remove(listener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.remote.core.IRemoteConnection#removeLocalPortForwarding(int)
	 */
	public void removeLocalPortForwarding(int port) throws RemoteConnectionException {
		// Do nothing
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.remote.core.IRemoteConnection#removeRemotePortForwarding(int)
	 */
	public void removeRemotePortForwarding(int port) throws RemoteConnectionException {
		// Do nothing
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.remote.IRemoteConnection#setAddress(java.lang.String)
	 */
	public void setAddress(String address) {
		fRseHost.setHostName(address);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.remote.core.IRemoteConnection#setAttribute(java.lang. String, java.lang.String)
	 */
	public void setAttribute(String key, String value) {
		// TODO implement
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.remote.core.IRemoteConnection#setAttributes(java.util .Map)
	 */
	/**
	 * @since 4.1
	 */
	public void setAttributes(Map<String, String> attributes) {
		// TODO Auto-generated method stub
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.remote.core.IRemoteConnection#setName(java.lang.String)
	 */
	public void setName(String name) {
		fRseHost.setAliasName(name);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.remote.core.IRemoteConnection#setPassword(java.lang.String )
	 */
	public void setPassword(String password) {
		// TODO is this possible?
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.remote.core.IRemoteConnection#setPort(int)
	 */
	public void setPort(int port) {
		// Not supported
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.remote.IRemoteConnection#setUsername(java.lang.String)
	 */
	public void setUsername(String username) {
		fRseHost.setDefaultUserId(username);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.remote.core.IRemoteFileManager#setWorkingDirectory(java .lang.String)
	 */
	/**
	 * @since 4.0
	 */
	public void setWorkingDirectory(String pathStr) {
		IPath path = new Path(pathStr);
		if (path.isAbsolute()) {
			fWorkingDir = path;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.remote.IRemoteConnection#supportsTCPPortForwarding()
	 */
	public boolean supportsTCPPortForwarding() {
		return false;
	}

	/**
	 * Notify all fListeners when this fConnection's status changes.
	 * 
	 * @param event
	 */
	private void fireConnectionChangeEvent(final int type) {
		IRemoteConnectionChangeEvent event = new IRemoteConnectionChangeEvent() {
			public IRemoteConnection getConnection() {
				return fConnection;
			}

			public int getType() {
				return type;
			}
		};
		for (Object listener : fListeners.getListeners()) {
			((IRemoteConnectionChangeListener) listener).connectionChanged(event);
		}
	}

	/**
	 * Find the file service subsystem from the IHost
	 * 
	 * @param host
	 * @return
	 */
	private IFileServiceSubSystem getFileServiceSubSystem(IHost host) {
		IRemoteFileSubSystem[] fileSubsystems = RemoteFileUtility.getFileSubSystems(host);
		for (IRemoteFileSubSystem subsystem : fileSubsystems) {
			if (subsystem instanceof IFileServiceSubSystem && subsystem.isConnected()) {
				return (IFileServiceSubSystem) subsystem;
			}
		}
		return null;
	}

	private void loadProperties() {
		if (fProperties == null) {
			fProperties = new HashMap<String, String>();
			fProperties.put(FILE_SEPARATOR_PROPERTY, "/"); //$NON-NLS-1$
			fProperties.put(PATH_SEPARATOR_PROPERTY, ":"); //$NON-NLS-1$
			fProperties.put(LINE_SEPARATOR_PROPERTY, "\n"); //$NON-NLS-1$

			// check HOME first for UNIX systems
			String homeDir = getEnv("HOME"); //$NON-NLS-1$
			if (homeDir == null) {
				homeDir = getEnv("USERPROFILE"); //$NON-NLS-1$
			}
			if (homeDir != null) {
				IPath homePath = new Path(homeDir);
				fProperties.put(USER_HOME_PROPERTY, homePath.makeAbsolute().toString());
			}
		}
	}

	/**
	 * Called to release any resources
	 */
	protected void dispose() {
		fSubSystem.getConnectorService().removeCommunicationsListener(commsListener);
	}

	/**
	 * Initialize the fConnection
	 * 
	 * @return true if initialization succeeded
	 */
	protected boolean initialize() {
		if (fSubSystem == null) {
			ISubSystem[] subSystems = fRseHost.getSubSystems();
			for (ISubSystem sub : subSystems) {
				if (sub instanceof IShellServiceSubSystem) {
					fSubSystem = sub;
					break;
				}
			}
			if (fSubSystem == null) {
				return false;
			}

			fSubSystem.getConnectorService().addCommunicationsListener(commsListener);
			fShellService = ((IShellServiceSubSystem) fSubSystem).getShellService();
		}
		return true;
	}
}
