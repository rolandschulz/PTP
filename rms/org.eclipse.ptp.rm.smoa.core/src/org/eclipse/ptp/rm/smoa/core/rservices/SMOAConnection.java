/*******************************************************************************
 * Copyright (c) 2010 Poznan Supercomputing and Networking Center
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Jan Konczak (PSNC) - initial implementation
 ******************************************************************************/

package org.eclipse.ptp.rm.smoa.core.rservices;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Map;

import javax.xml.bind.JAXBException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.IRemoteConnectionChangeListener;
import org.eclipse.ptp.remote.core.IRemoteFileManager;
import org.eclipse.ptp.remote.core.IRemoteServices;
import org.eclipse.ptp.remote.core.exception.RemoteConnectionException;
import org.eclipse.ptp.rm.smoa.core.SMOAConfiguration.AuthType;

import com.smoa.comp.sdk.SMOAFactory;
import com.smoa.comp.sdk.SMOARsync;
import com.smoa.comp.sdk.SMOAStaging;
import com.smoa.comp.sdk.jsdl.JSDL;
import com.smoa.comp.stubs.staging.FileNotFoundFault;
import com.smoa.comp.stubs.staging.NotAuthorizedFault;
import com.smoa.core.sdk.attachments.FileStagingHandler;
import com.smoa.core.sdk.security.AuthenticationModule;
import com.smoa.core.sdk.security.anonymous.AnonymousAuthentication;
import com.smoa.core.sdk.security.gsi.GSIAuthentication;
import com.smoa.core.sdk.security.ssl.SSLAuthentication;
import com.smoa.core.sdk.security.wsse.WSSEAuthentication;
import com.smoa.core.sdk.security.wsse.username.UsernameToken;

/**
 * Main class for maintaining the real connection with SMOA Computing.
 * 
 * Keeps the SMOAFactory, SMOAStaging and SMOARsync objects.
 */
public class SMOAConnection implements IRemoteConnection {

	// Keys for map used to transfer connection attributes
	public static final String TAG_ADDRESS = "address"; //$NON-NLS-1$
	public static final String TAG_PORT = "port"; //$NON-NLS-1$
	public static final String TAG_AUTHTYPE = "auth"; //$NON-NLS-1$
	public static final String TAG_USERNAME = "user"; //$NON-NLS-1$
	public static final String TAG_PASSWORD = "pass"; //$NON-NLS-1$
	public static final String TAG_CACERT = "cacert"; //$NON-NLS-1$
	public static final String TAG_DN = "dn"; //$NON-NLS-1$

	// parent RemoteServices
	private final SMOARemoteServices remoteServices;

	// Connection state
	private boolean connectionIsOpen = false;

	// Crucial objects
	private SMOAFactory besFactory;
	private SMOAStaging smoaStaging;
	private SMOARsync rsync;

	// Connection info
	private String name = null;
	private String url = null;
	private int port = 0;
	private String resourceManagerName;

	// Authentication info
	private AuthType auth;
	private String username;
	private String password;
	private String dn;
	private String cacert;

	// Others
	private IRemoteFileManager fileManager = null;
	private String workDir = "."; //$NON-NLS-1$
	private String homeDir = null;
	private IRemoteConnection fileRemoteConnection = null;

	public SMOAConnection(SMOARemoteServices remoteServices, String name) {
		this.remoteServices = remoteServices;
		this.name = name;
	}

	public void addConnectionChangeListener(IRemoteConnectionChangeListener listener) {
		if (fileRemoteConnection != null) {
			fileRemoteConnection.addConnectionChangeListener(listener);
		}

	}

	public void close() {
		if (fileRemoteConnection != null) {
			fileRemoteConnection.close();
		}
	}

	public void forwardLocalPort(int localPort, String fwdAddress, int fwdPort) throws RemoteConnectionException {
		if (fileRemoteConnection != null) {
			fileRemoteConnection.forwardLocalPort(localPort, fwdAddress, fwdPort);
		}

	}

	public int forwardLocalPort(String fwdAddress, int fwdPort, IProgressMonitor monitor) throws RemoteConnectionException {
		if (fileRemoteConnection != null) {
			return fileRemoteConnection.forwardLocalPort(fwdAddress, fwdPort, monitor);
		}
		return 0;
	}

	public void forwardRemotePort(int remotePort, String fwdAddress, int fwdPort) throws RemoteConnectionException {
		if (fileRemoteConnection != null) {
			fileRemoteConnection.forwardRemotePort(remotePort, fwdAddress, fwdPort);
		}

	}

	public int forwardRemotePort(String fwdAddress, int fwdPort, IProgressMonitor monitor) throws RemoteConnectionException {
		if (fileRemoteConnection != null) {
			return fileRemoteConnection.forwardRemotePort(fwdAddress, fwdPort, monitor);
		}
		return 0;
	}

	public String getAddress() {
		return url;
	}

	public Map<String, String> getAttributes() {
		// TODO Auto-generated method stub
		return null;
	}

	public Map<String, String> getEnv() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getEnv(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	public SMOAFactory getFactory() {
		return besFactory;
	}

	public IRemoteConnection getFileConnection() {
		return fileRemoteConnection;
	}

	public IRemoteFileManager getFileManager() {
		return fileManager;
	}

	public String getHomeDir() {
		if (homeDir != null) {
			return homeDir;
		}

		if (!isOpen()) {
			return "."; //$NON-NLS-1$
		}

		JSDL jsdl;
		try {
			jsdl = smoaStaging.listDirectory(".", null); //$NON-NLS-1$
		} catch (final FileNotFoundFault e) {
			throw new RuntimeException();
		} catch (final NotAuthorizedFault e) {
			throw new RuntimeException();
		}
		homeDir = jsdl.getWorkingDirectory();
		return homeDir;

	}

	public String getName() {
		return name;
	}

	public int getPort() {
		return port;
	}

	public String getProperty(String key) {
		// TODO Auto-generated method stub
		return null;
	}

	public IRemoteServices getRemoteServices() {
		return remoteServices;
	}

	public String getRMName() {
		return resourceManagerName;
	}

	public SMOARsync getRsync() {
		return rsync;
	}

	public SMOAStaging getSMOAStaging() {
		return smoaStaging;
	}

	public String getUsername() {
		return username;
	}

	public String getWorkingDirectory() {
		if (workDir.equals(".")) { //$NON-NLS-1$
			getHomeDir();
			if (homeDir != null) {
				workDir = getHomeDir();
			} else {
				return "."; //$NON-NLS-1$
			}
		}
		if (fileRemoteConnection != null) {
			fileRemoteConnection.setWorkingDirectory(workDir);
		}
		return workDir;
	}

	public boolean isOpen() {
		if (fileRemoteConnection != null && !fileRemoteConnection.isOpen()) {
			return false;
		}
		return connectionIsOpen;
	}

	public void open(IProgressMonitor monitor) throws RemoteConnectionException {
		try {
			final AnonymousAuthentication anonAuth = new AnonymousAuthentication();
			final FileStagingHandler fileStagingHandler = new SMOAFileStagingHandler();

			switch (auth) {
			case Anonymous:
				besFactory = new SMOAFactory(url, port, anonAuth, true);
				smoaStaging = new SMOAStaging(url, port, anonAuth, true, fileStagingHandler);
				rsync = new SMOARsync(url, port, anonAuth, true);
				break;
			case GSI:
				final GSIAuthentication gsi = new GSIAuthentication(dn != null && !dn.isEmpty() ? dn : null);
				besFactory = new SMOAFactory(url, port, gsi, true);
				smoaStaging = new SMOAStaging(url, port, gsi, true, fileStagingHandler);
				rsync = new SMOARsync(url, port, gsi, true);

				break;
			case UsernamePassword: {

				final WSSEAuthentication wsseAuth = new WSSEAuthentication(new UsernameToken(username, password, false, false));

				AuthenticationModule am;

				if (cacert == null) {
					am = anonAuth;
				} else {
					am = new SSLAuthentication(cacert, null, dn != null && !dn.isEmpty() ? dn : null);

				}

				rsync = new SMOARsync(url, port, am, wsseAuth);
				besFactory = new SMOAFactory(url, port, am, wsseAuth);
				smoaStaging = new SMOAStaging(url, port, am, wsseAuth, true, fileStagingHandler);

				break;
			}
			default:
				throw new RuntimeException(Messages.SMOAConnection_UnsupportedAuthType);
			}
			connectionIsOpen = true;

		} catch (final JAXBException e) {
			throw new RemoteConnectionException(e);
		} catch (final IOException e) {
			throw new RemoteConnectionException(e);
		} catch (final GeneralSecurityException e) {
			throw new RemoteConnectionException(e);
		}
		if (fileRemoteConnection != null) {
			fileRemoteConnection.open(monitor);
		}
	}

	public void removeConnectionChangeListener(IRemoteConnectionChangeListener listener) {
		if (fileRemoteConnection != null) {
			fileRemoteConnection.removeConnectionChangeListener(listener);
		}

	}

	public void setAddress(String address) {
		if (address == null) {
			return;
		}
		if (address.equals(url)) {
			return;
		}
		if (isOpen()) {
			throw new RuntimeException(Messages.SMOAConnection_CannotModifyOpenConnection);
		}
		url = address;
	}

	public void setAuthType(AuthType auth) {
		if (this.auth == auth) {
			return;
		}
		if (isOpen()) {
			throw new RuntimeException(Messages.SMOAConnection_CannotModifyOpenConnection);
		}
		this.auth = auth;
	}

	public void setCaCert(String string) {
		cacert = string;
	}

	public void setDN(String string) {
		dn = string;
	}

	public void setFileConnection(IRemoteConnection fileRemoteConnection) {
		if (isOpen()) {
			throw new RuntimeException(Messages.SMOAConnection_CannotModifyOpenConnection);
		}
		this.fileRemoteConnection = fileRemoteConnection;
	}

	public void setFileManager(IRemoteFileManager fileManager) {
		this.fileManager = fileManager;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setPort(int port) {
		if (this.port == port) {
			return;
		}
		if (isOpen()) {
			throw new RuntimeException(Messages.SMOAConnection_CannotModifyOpenConnection);
		}
		this.port = port;
	}

	public void setRMName(String rMName) {
		resourceManagerName = rMName;
	}

	public void setUsername(String username) {
		if (isOpen()) {
			throw new RuntimeException();
		}
		this.username = username;
	}

	public void setWorkingDirectory(String path) {
		workDir = path;
		if (fileRemoteConnection != null) {
			fileRemoteConnection.setWorkingDirectory(path);
		}

	}

	public boolean supportsTCPPortForwarding() {
		if (fileRemoteConnection != null) {
			return fileRemoteConnection.supportsTCPPortForwarding();
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.remote.core.IRemoteConnection#setAttribute(java.lang.
	 * String, java.lang.String)
	 */
	public void setAttribute(String key, String value) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.remote.core.IRemoteConnection#setPassword(java.lang.String
	 * )
	 */
	public void setPassword(String password) {
		if (isOpen()) {
			throw new RuntimeException(Messages.SMOAConnection_CannotModifyOpenConnection);
		}
		this.password = password;
	}
}
