/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.ptp.rdt.core.tests;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.rse.connectorservice.dstore.DStoreConnectorService;
import org.eclipse.rse.core.IRSECoreRegistry;
import org.eclipse.rse.core.IRSESystemType;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.model.ISystemRegistry;
import org.eclipse.rse.core.subsystems.IConnectorService;
import org.eclipse.rse.core.subsystems.IRemoteServerLauncher;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.core.subsystems.ISubSystemConfiguration;
import org.eclipse.rse.core.subsystems.ServerLaunchType;
import org.eclipse.rse.ui.ISystemPreferencesConstants;
import org.eclipse.rse.ui.RSEUIPlugin;


/**
 * IRDTTestConnect implementation that creates a DStore connection.
 * Creates a host then connects all the DStore based subsystems.
 */
public class DStoreTestConnection implements IRDTTestConnection {

	private String systemTypeId;
	private String hostname;
	private int port;
	private String username;
	private String password;
	
	private DStoreConnectorService connector = null;

	
	private void initialize(Properties properties) throws ConnectException {
		this.username = getProperty(PROPERTY_USERNAME, properties);
		this.password = getProperty(PROPERTY_PASSWORD, properties);
		this.hostname = getProperty(PROPERTY_HOSTNAME, properties);
		this.systemTypeId = getProperty(PROPERTY_SYSTEMTYPEID, properties);
		this.port = Integer.parseInt(getProperty(PROPERTY_PORT, properties));
	}
	
	
	private static String getProperty(String key, Properties properties) throws ConnectException {
		String value = properties.getProperty(key);
		if(value == null)
			throw new ConnectException("missing property: " + key);
		return value;
	}
	
	
	
	/**
	 * Creates and configures a host and then connects its DStore connection.
	 */
	public void connect(Properties properties) throws ConnectException {
		initialize(properties);
		
		try {
			// make sure RSE is ready to make a connection
			RSECorePlugin.waitForInitCompletion();
		} catch (InterruptedException e) {
			throw new ConnectException(e);
		}
		
		IRSECoreRegistry coreRegistry = RSECorePlugin.getTheCoreRegistry();
		IRSESystemType systemType = coreRegistry.getSystemTypeById(systemTypeId);
		
		// create a new connection to a Linux server
		ISystemRegistry systemRegistry = RSECorePlugin.getTheSystemRegistry();
		IHost host;
		try {
			host = systemRegistry.createHost(systemType, hostname, hostname, "BLAH");
		} catch (Exception e) {
			throw new ConnectException(e);
		} 
		
		// find the DStore connector service for the host
		connector = getDStoreConnectorService(host);
		if(connector == null)
			throw new ConnectException("DStoreConnectorService not available");
		
		// configure DStore to use running server
		IRemoteServerLauncher launcherProperties = (IRemoteServerLauncher)connector.getRemoteServerLauncherProperties();
		launcherProperties.setServerLaunchType(ServerLaunchType.RUNNING_LITERAL);
		
		// set authentication details
		connector.setUserId(username);
		connector.setPassword(username, password, true, false);
		
		// configure all dstore subsystems to use the given port
		for(ISubSystem subSystem : host.getSubSystems()) {
			if(subSystem.getConnectorService() instanceof DStoreConnectorService) {
				ISubSystemConfiguration configuration = subSystem.getSubSystemConfiguration();
				configuration.updateSubSystem(subSystem, false, subSystem.getLocalUserId(), true, port);
				subSystem.commit();
			}
		}
		
		// make sure RSE doesn't try to pop up a warning dialog about some SSL stuff
		IPreferenceStore store = RSEUIPlugin.getDefault().getPreferenceStore();
		store.setValue(ISystemPreferencesConstants.ALERT_NONSSL, false);
		
		// connect the subsystems that use DStore
		try {
			connector.connect(new NullProgressMonitor());
		} catch (Exception e) {
			throw new ConnectException(e);
		}
	}
	
	
	private static DStoreConnectorService getDStoreConnectorService(IHost host) {
		for(IConnectorService cs : host.getConnectorServices()) {
			if(cs instanceof DStoreConnectorService) {
				return (DStoreConnectorService)cs;
			}
		}
		return null;
	}
	
	/**
	 * Disconnects and then deletes the host.
	 */
	public void disconnect() throws ConnectException {
		try {
			connector.disconnect(null);
		} catch (Exception e) {
			throw new ConnectException(e);
		}
		ISystemRegistry systemRegistry = RSECorePlugin.getTheSystemRegistry();
		systemRegistry.deleteHost(connector.getHost());
		connector = null;
	}
	
	
	public URI getURI(String path) throws URISyntaxException {
		return new URI("rse", hostname, path, null);
	}


	public boolean isConnected() {
		return connector.isConnected();
	}


	public String getHostName() {
		return hostname;
	}
}
