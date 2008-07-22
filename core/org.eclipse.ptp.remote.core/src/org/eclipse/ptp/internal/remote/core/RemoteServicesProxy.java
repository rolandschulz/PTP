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
package org.eclipse.ptp.internal.remote.core;

import java.util.List;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.IRemoteConnectionManager;
import org.eclipse.ptp.remote.core.IRemoteFileManager;
import org.eclipse.ptp.remote.core.IRemoteProcessBuilder;
import org.eclipse.ptp.remote.core.IRemoteServices;
import org.eclipse.ptp.remote.core.IRemoteServicesDelegate;
import org.eclipse.ptp.remote.core.IRemoteServicesFactory;
import org.eclipse.ptp.remote.core.PTPRemoteCorePlugin;


public class RemoteServicesProxy implements IRemoteServices {
	private static final String ATTR_ID = "id";
	private static final String ATTR_NAME = "name";
	private static final String ATTR_CLASS = "class";
	
	private static String getAttribute(IConfigurationElement configElement, String name, String defaultValue) {
		String value = configElement.getAttribute(name);
		if (value != null) {
			return value;
		}
		if (defaultValue != null) {
			return defaultValue;
		}
		throw new IllegalArgumentException("Missing " + name + " attribute");
	}

	private boolean initialized;

	private final IConfigurationElement configElement;
	private final String id;
	private final String name;
	private IRemoteServicesFactory factory;
	private IRemoteServicesDelegate delegate;
	
	public RemoteServicesProxy(IConfigurationElement configElement) {
		this.configElement = configElement;
		this.id = getAttribute(configElement, ATTR_ID, null);
		this.name = getAttribute(configElement, ATTR_NAME, this.id);
		getAttribute(configElement, ATTR_CLASS, null);
		this.factory = null;
		this.delegate = null;
		this.initialized = false;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.core.IRemoteServices#getConnectionManager()
	 */
	public IRemoteConnectionManager getConnectionManager() {
		loadServices();
		return delegate.getConnectionManager();
	}
	
	/**
	 * Get the factory from the plugin
	 * 
	 * @return instance of the factory
	 */
	public IRemoteServicesFactory getFactory() {
		if (factory != null) {
			return factory;
		}
		try {
			factory = (IRemoteServicesFactory)configElement.createExecutableExtension(ATTR_CLASS);
		} catch (Exception e) {
			PTPRemoteCorePlugin.log(
					"Failed to instatiate factory: "
					+ configElement.getAttribute(ATTR_CLASS)
					+ " in type: "
					+ id
					+ " in plugin: "
					+ configElement.getDeclaringExtension().getNamespaceIdentifier());
		}
		return factory;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.core.IRemoteServicesDelegate#getFileManager(org.eclipse.ptp.remote.core.IRemoteConnection)
	 */
	public IRemoteFileManager getFileManager(IRemoteConnection conn) {
		loadServices();
		return delegate.getFileManager(conn);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.core.IRemoteServices#getId()
	 */
	public String getId() {
		return id;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.core.IRemoteServices#getName()
	 */
	public String getName() {
		return name;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.core.IRemoteServices#getProcessBuilder(org.eclipse.ptp.remote.core.IRemoteConnection, java.util.List)
	 */
	public IRemoteProcessBuilder getProcessBuilder(IRemoteConnection conn,
			List<String> command) {
		loadServices();
		return delegate.getProcessBuilder(conn, command);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.core.IRemoteServices#getProcessBuilder(org.eclipse.ptp.remote.core.IRemoteConnection, java.lang.String[])
	 */
	public IRemoteProcessBuilder getProcessBuilder(IRemoteConnection conn,
			String... command) {
		loadServices();
		return delegate.getProcessBuilder(conn, command);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.core.IRemoteServices#initialize()
	 */
	public boolean initialize() {
		loadServices();
		return initialized;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.core.IRemoteServices#isInitialized()
	 */
	public boolean isInitialized() {
		return initialized;
	}
	
	/**
	 * Create and initialize the remote services factory
	 */
	private void loadServices() {
		if (delegate == null) {
			IRemoteServicesFactory factory = getFactory();
			if (factory != null) {
				delegate = factory.getServices();
				if (delegate.initialize()) {
					initialized = true;
				}
			}
		}
	}
}
