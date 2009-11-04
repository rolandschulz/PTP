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
package org.eclipse.ptp.remote.rse.core;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.filesystem.IFileSystem;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.dstore.core.model.DataStore;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.IRemoteConnectionChangeEvent;
import org.eclipse.ptp.remote.core.IRemoteConnectionChangeListener;
import org.eclipse.ptp.remote.core.exception.RemoteConnectionException;
import org.eclipse.ptp.remote.core.exception.UnableToForwardPortException;
import org.eclipse.ptp.remote.rse.core.messages.Messages;
import org.eclipse.rse.connectorservice.dstore.DStoreConnectorService;
import org.eclipse.rse.connectorservice.dstore.DStoreConnectorServiceManager;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.subsystems.CommunicationsEvent;
import org.eclipse.rse.core.subsystems.ICommunicationsListener;
import org.eclipse.rse.core.subsystems.IConnectorService;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
import org.eclipse.rse.services.dstore.IDStoreService;
import org.eclipse.rse.services.shells.IShellService;
import org.eclipse.rse.subsystems.shells.core.subsystems.servicesubsystem.IShellServiceSubSystem;

public class RSEConnection implements IRemoteConnection {
	private IShellService shellService = null;

	private ISubSystem subSystem = null;
	private Map<String, String> fEnv = null;
	private Map<String, String> fProperties = null;
	private final IHost rseHost;
	
	private final IFileSystem fileSystem;
	private final IRemoteConnection fConnection = this;
	private final ListenerList fListeners = new ListenerList();
	
	private ICommunicationsListener commsListener = new ICommunicationsListener() {

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
	
	public RSEConnection(IHost host, IFileSystem fileSystem) {
		this.rseHost = host;
		this.fileSystem = fileSystem;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.core.IRemoteConnection#addConnectionChangeListener(org.eclipse.ptp.remote.core.IRemoteConnectionChangeListener)
	 */
	public void addConnectionChangeListener(IRemoteConnectionChangeListener listener) {
		fListeners.add(listener);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.IRemoteConnection#close()
	 */
	public void close() {
		if (subSystem != null && subSystem.isConnected()) {
			try {
				subSystem.disconnect();
			} catch (Exception e) {
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.IRemoteConnection#forwardLocalPort(int, java.lang.String, int)
	 */
	public void forwardLocalPort(int localPort, String fwdAddress,
			int fwdPort) throws RemoteConnectionException {
		throw new UnableToForwardPortException(Messages.RSEConnection_noPortFwd);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.IRemoteConnection#forwardLocalPort(java.lang.String, int, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public int forwardLocalPort(String fwdAddress, int fwdPort,
			IProgressMonitor monitor) throws RemoteConnectionException {
		throw new UnableToForwardPortException(Messages.RSEConnection_noPortFwd);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.IRemoteConnection#forwardRemotePort(int, java.lang.String, int)
	 */
	public void forwardRemotePort(int remotePort, String fwdAddress,
			int fwdPort) throws RemoteConnectionException {
		throw new UnableToForwardPortException(Messages.RSEConnection_noPortFwd);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.IRemoteConnection#forwardRemotePort(java.lang.String, int, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public int forwardRemotePort(String fwdAddress, int fwdPort,
			IProgressMonitor monitor) throws RemoteConnectionException {
		throw new UnableToForwardPortException(Messages.RSEConnection_noPortFwd);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.IRemoteConnection#getAddress()
	 */
	public String getAddress() {
		return rseHost.getHostName();
	}

	/* (non-Javadoc)
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
		IConnectorService connector = DStoreConnectorServiceManager.getInstance().getConnectorService(rseHost, IDStoreService.class);
		if (connector != null && connector instanceof DStoreConnectorService) {
			return ((DStoreConnectorService) connector).getDataStore();
		}
		return null;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.core.IRemoteConnection#getEnv()
	 */
	public Map<String, String> getEnv() {
		if (fEnv == null) {
			fEnv = new HashMap<String, String>();
			
			try {
				String[] env = shellService.getHostEnvironment();
				for (String var : env) {
					String[] kv = var.split("="); //$NON-NLS-1$
					if (kv.length == 2) {
						fEnv.put(kv[0], kv[1]);
					}
				}
			} catch (SystemMessageException e) {
			}
		}
		return fEnv;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.core.IRemoteConnection#getEnv(java.lang.String)
	 */
	public String getEnv(String name) {
		if (fEnv == null) {
			getEnv();
		}
		return fEnv.get(name);
	}
	
	/**
	 * Get the file system for this fConnection
	 * 
	 * @return
	 */
	public IFileSystem getFileSystem() {
		return fileSystem;
	}

	/**
	 * Get RSE host object 
	 * 
	 * @return IHost
	 */
	public IHost getHost() {
		return rseHost;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.IRemoteConnection#getName()
	 */
	public String getName() {
		return rseHost.getAliasName();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.core.IRemoteConnection#getProperty(java.lang.String)
	 */
	public String getProperty(String key) {
		loadProperties();
		return fProperties.get(key);
	}

	/**
	 * Get the shell service for this fConnection
	 * 
	 * @return shell service
	 */
	public IShellService getRemoteShellService() {
		return shellService;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.core.IRemoteConnection#getUsername()
	 */
	public String getUsername() {
		return rseHost.getDefaultUserId();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.IRemoteConnection#isOpen()
	 */
	public boolean isOpen() {
		return subSystem != null && subSystem.isConnected();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.IRemoteConnection#open()
	 */
	public void open(IProgressMonitor monitor) throws RemoteConnectionException {
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		
		if (!subSystem.isConnected()) {
			try {
				SubMonitor subMon = SubMonitor.convert(monitor);
				subSystem.connect(subMon.newChild(1), false);
			} catch (Exception e) {
				throw new RemoteConnectionException(e.getMessage());
			}
			
			if(!subSystem.isConnected()) {
				throw new RemoteConnectionException(Messages.RSEConnection_noShellService);
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.core.IRemoteConnection#removeConnectionChangeListener(org.eclipse.ptp.remote.core.IRemoteConnectionChangeListener)
	 */
	public void removeConnectionChangeListener(IRemoteConnectionChangeListener listener) {
		fListeners.remove(listener);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.IRemoteConnection#setAddress(java.lang.String)
	 */
	public void setAddress(String address) {
		rseHost.setHostName(address);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.core.IRemoteConnection#setName(java.lang.String)
	 */
	public void setName(String name) {
		rseHost.setAliasName(name);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.IRemoteConnection#setUsername(java.lang.String)
	 */
	public void setUsername(String username) {
		rseHost.setDefaultUserId(username);
	}

	/* (non-Javadoc)
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
			((IRemoteConnectionChangeListener)listener).connectionChanged(event);
		}
	}
	
	private void loadProperties() {
		if (fProperties == null) {
			fProperties = new HashMap<String, String>();
			fProperties.put(FILE_SERPARATOR_PROPERTY, "/"); //$NON-NLS-1$
			fProperties.put(PATH_SERPARATOR_PROPERTY, ":"); //$NON-NLS-1$
			fProperties.put(LINE_SERPARATOR_PROPERTY, "\n"); //$NON-NLS-1$
		}
	}
	
	/**
	 * Called to release any resources
	 */
	protected void dispose() {
		subSystem.getConnectorService().removeCommunicationsListener(commsListener);
	}
	
	/**
	 * Initialize the fConnection
	 * 
	 * @return true if initialization succeeded
	 */
	protected boolean initialize() {
		if (subSystem == null) {
			ISubSystem[] subSystems = rseHost.getSubSystems();
			for (ISubSystem sub : subSystems) {
				if (sub instanceof IShellServiceSubSystem) {
					subSystem = sub;
					break;
				}
			}
			if (subSystem == null) {
				return false;
			}
			
			subSystem.getConnectorService().addCommunicationsListener(commsListener);
			shellService = ((IShellServiceSubSystem)subSystem).getShellService();
		}
		return true;
	}
}
