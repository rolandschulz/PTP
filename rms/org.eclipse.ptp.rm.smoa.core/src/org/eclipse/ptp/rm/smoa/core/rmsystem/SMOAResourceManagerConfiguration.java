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

package org.eclipse.ptp.rm.smoa.core.rmsystem;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.equinox.security.storage.ISecurePreferences;
import org.eclipse.equinox.security.storage.SecurePreferencesFactory;
import org.eclipse.equinox.security.storage.StorageException;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.IRemoteServices;
import org.eclipse.ptp.remote.core.PTPRemoteCorePlugin;
import org.eclipse.ptp.remote.core.exception.RemoteConnectionException;
import org.eclipse.ptp.rm.core.rmsystem.AbstractRemoteResourceManagerConfiguration;
import org.eclipse.ptp.rm.smoa.core.SMOAConfiguration;
import org.eclipse.ptp.rm.smoa.core.rservices.SMOAConnection;
import org.eclipse.ptp.rm.smoa.core.rservices.SMOARemoteServices;
import org.eclipse.ptp.rm.smoa.core.util.NotifyShell;
import org.eclipse.ptp.services.core.IServiceProvider;

import com.smoa.comp.stubs.factory.ApplicationsType.Application;

/**
 * Persistently keeps data about one {@link SMOAResourceManager} configuration,
 * i.e. connection data, authentication methods
 * 
 * Keeps also other things related to the RM, like available applications or the
 * connection
 */
public class SMOAResourceManagerConfiguration extends AbstractRemoteResourceManagerConfiguration implements SMOAConfiguration {

	// Keys used for storing persistent data
	private final static String URL_KEY = "url"; //$NON-NLS-1$
	private final static String PORT_KEY = "port"; //$NON-NLS-1$
	private static final String AUTH_KEY = "auth"; //$NON-NLS-1$
	private static final String USER_KEY = "user"; //$NON-NLS-1$
	private static final String CACERT_KEY = "cacert"; //$NON-NLS-1$
	private static final String DN_KEY = "dn"; //$NON-NLS-1$
	private static final String RS_ID_KEY = "rs_id"; //$NON-NLS-1$

	private static final String RS_CONN_KEY = "rs_conn"; //$NON-NLS-1$

	// Place for passwords
	ISecurePreferences securePrefs = SecurePreferencesFactory.getDefault().node("smoa"); //$NON-NLS-1$
	// Made to differentiate connections with same name, but different data
	private static int nextConnectionId = 0;

	private static Object nextConnectionIdLock = new Object();
	private boolean isConectionInitialized = false;
	private SMOAConnection connection = null;
	private final SMOAResourceManager resourceManager = null;

	private Map<String, Application> apps;

	boolean notifiedAboutConnLoss = true;

	public SMOAResourceManagerConfiguration() {
		super();
	}

	protected SMOAResourceManagerConfiguration(String namespace, IServiceProvider provider) {
		super(namespace, provider);
		setDescription(Messages.SMOAResourceManagerConfiguration_SmoaRmDescription);
		setRemoteServicesId("org.eclipse.ptp.remote.SMOARemoteServices"); //$NON-NLS-1$
	}

	public Application getAppForName(String name) {
		if (apps == null) {
			return null;
		}
		return apps.get(name);
	}

	public AuthType getAuthType() {
		final String auth = getString(AUTH_KEY, AuthType.Anonymous.toString());
		return AuthType.valueOf(auth);
	}

	// SettersAndGetters

	public List<String> getAvailableAppList() {
		final Vector<String> _ = new Vector<String>();
		if (apps != null) {
			_.addAll(apps.keySet());
		}
		return _;
	}

	public String getCaCertPath() {
		return getString(CACERT_KEY, null);
	}

	public SMOAConnection getConnection() {
		return connection;
	}

	@Override
	public String getConnectionName() {
		if (!isConectionInitialized) {
			initConnection();
		}
		return super.getConnectionName();
	}

	public IRemoteConnection getFileRemoteConnection() {
		IRemoteConnection fileRemoteConnection = null;
		final String id = getString(RS_ID_KEY, null);
		final String name = getString(RS_CONN_KEY, null);
		if (id != null && name != null) {
			final IRemoteServices rs = PTPRemoteCorePlugin.getDefault().getRemoteServices(id);
			if (rs != null) {
				fileRemoteConnection = rs.getConnectionManager().getConnection(name);
			}

			if (fileRemoteConnection == null) {
				notifyConnLoss();
			}
		}
		return fileRemoteConnection;
	}

	public String getPassword() {
		if (getUrl() == null) {
			return null;
		}
		String pass = null;
		try {
			pass = securePrefs.get(getUrl(), null);
		} catch (final StorageException e) {
			Logger.getLogger(getClass().getCanonicalName()).log(Level.SEVERE,
					Messages.SMOAResourceManagerConfiguration_CouldNotRetreivePassword, e);
		}
		return pass;
	}

	public Integer getPort() {
		final int port = getInt(PORT_KEY, -1);
		return port == -1 ? null : port;
	}

	@Override
	public String getResourceManagerId() {
		return getId();
	}

	public String getServiceDN() {
		return getString(DN_KEY, null);
	}

	public String getUrl() {
		return getString(URL_KEY, null);
	}

	public String getUser() {
		return getString(USER_KEY, null);
	}

	/**
	 * Creates a connection in the SMOAConnectionManager
	 */
	private void initConnection() {

		final SMOARemoteServices remoteServices = (SMOARemoteServices) PTPRemoteCorePlugin.getDefault().getRemoteServices(
				getRemoteServicesId());

		final IRemoteConnection existingConnection = remoteServices.getConnectionManager().getConnection(super.getConnectionName());
		if (existingConnection != null) {
			connection = (SMOAConnection) existingConnection;
			Logger.getLogger(getClass().getName()).info(Messages.SMOAResourceManagerConfiguration_ReusingConnection);
			return;
		}

		try {
			final SMOAConnection c = remoteServices.getConnectionManager().newConnection(super.getConnectionName());
			c.setAddress(getUrl());
			c.setFileConnection(getFileRemoteConnection());
			c.setRMName(this.getName());

			if (getAuthType() == AuthType.UsernamePassword) {
				c.setUsername(getUser());
				c.setPassword(getPassword());
			}

			c.setPort(getPort());
			c.setAuthType(getAuthType());

			if (getCaCertPath() != null && !getCaCertPath().isEmpty()) {
				c.setCaCert(getCaCertPath());
				if (getServiceDN() != null && !getServiceDN().isEmpty()) {
					c.setDN(getServiceDN());
				}
			}
			connection = c;
		} catch (final RemoteConnectionException e) {
			// should never happen
			throw new RuntimeException(e);
		}

		isConectionInitialized = true;
	}

	@Override
	public boolean isConfigured() {
		return true;
	}

	@Override
	public boolean needsDebuggerLaunchHelp() {
		return true;
	}

	private void notifyConnLoss() {
		if (notifiedAboutConnLoss) {
			notifiedAboutConnLoss = false;
			NotifyShell.open(Messages.SMOAResourceManagerConfiguration_AdditionalConnectionNotAvailable_title,
					Messages.SMOAResourceManagerConfiguration_AdditionalConnectionNotAvailable_text);
		}
	}

	public void setAuthType(AuthType type) {
		putString(AUTH_KEY, type.toString());
	}

	public void setAvailableAppList(List<Application> appList) {
		apps = new HashMap<String, Application>();
		for (final Application a : appList) {
			final String version = a.getVersion() == null ? "" : (" " + a //$NON-NLS-1$ //$NON-NLS-2$
					.getVersion());
			apps.put(a.getName() + version, a);
		}
	}

	public void setCacertPath(String cacert) {
		if (cacert == null) {
			return;
		}
		if (cacert.equals(getCaCertPath())) {
			return;
		}
		putString(CACERT_KEY, cacert);

		updateConnectionName();
	}

	public void setDefaultNameAndDesc() {

		final StringBuilder name = new StringBuilder("SMOA Comp"); //$NON-NLS-1$
		if (getUrl() != null) {
			name.append(" - "); //$NON-NLS-1$

			switch (getAuthType()) {
			case Anonymous:
				name.append(Messages.SMOAResourceManagerConfiguration_PrefixAnonymous);
				break;
			case GSI:
				name.append(Messages.SMOAResourceManagerConfiguration_PrefixGsi);
				break;
			case UsernamePassword:
				if (getUser() != null) {
					name.append(getUser());
				}
				name.append("@"); //$NON-NLS-1$
				break;
			}

			if (getUrl() != null) {
				name.append(getUrl());
			}
			name.append(":"); //$NON-NLS-1$
			if (getPort() != null) {
				name.append(getPort());
			}
		}

		setName(name.toString());

		setDescription("SMOA Computing Resource Manager"); //$NON-NLS-1$
	}

	public void setFileRemoteConnection(IRemoteConnection rconn) {
		if (rconn != null) {
			putString(RS_ID_KEY, rconn.getRemoteServices().getId());
			putString(RS_CONN_KEY, rconn.getName());
		} else {
			putString(RS_ID_KEY, null);
			putString(RS_CONN_KEY, null);
		}
	}

	@Override
	public void setName(String name) {
		if (connection != null) {
			connection.setRMName(name);
		}
		super.setName(name);
	}

	public void setPassword(String passwd) {
		if (passwd == null) {
			return;
		}
		if (passwd.equals(getPassword())) {
			return;
		}
		if (getUrl() == null) {
			return;
		}

		try {
			securePrefs.put(getUrl(), passwd, true);
		} catch (final StorageException e) {
			Logger.getLogger(getClass().getCanonicalName()).log(Level.SEVERE,
					Messages.SMOAResourceManagerConfiguration_CouldNotStorePassword, e);
		}

		updateConnectionName();
	}

	public void setPort(Integer port) {
		if (port == getPort()) {
			return;
		}
		putInt(PORT_KEY, port);
		updateConnectionName();
	}

	public void setServiceDn(String dn) {
		if (dn == null) {
			return;
		}
		if (dn.equals(getServiceDN())) {
			return;
		}
		putString(DN_KEY, dn);

		updateConnectionName();
	}

	public void setUrl(String url) {
		if (url.equals(getUrl())) {
			return;
		}

		final String pass = getPassword();
		if (getUrl() != null) {
			securePrefs.remove(getUrl());
		}

		putString(URL_KEY, url);

		if (pass == null) {
			setPassword(pass);
		}

		updateConnectionName();
	}

	public void setUser(String user) {
		if (user == null) {
			return;
		}
		if (user.equals(getUser())) {
			return;
		}
		putString(USER_KEY, user);

		updateConnectionName();
	}

	public void trigerSecureStorage() throws StorageException {
		// Just do something in order to force asking for password
		securePrefs.put("How should I init the storage without using put method?", //$NON-NLS-1$
				"No idea.", true); //$NON-NLS-1$
	}

	private void updateConnectionName() {

		synchronized (nextConnectionIdLock) {
			setConnectionName(getUrl() + nextConnectionId);
			nextConnectionId++;
		}

		if (resourceManager != null) {
			resourceManager.setConfiguration(this);
		}

		if (connection == null) {
			return;
		}

		final SMOARemoteServices smoaRemoteServices = (SMOARemoteServices) PTPRemoteCorePlugin.getDefault().getRemoteServices(
				getRemoteServicesId());

		smoaRemoteServices.getConnectionManager().removeConnection(connection);
		connection = null;
		isConectionInitialized = false;
	}
}
