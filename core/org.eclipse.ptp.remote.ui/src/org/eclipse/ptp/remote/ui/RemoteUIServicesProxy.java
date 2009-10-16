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
package org.eclipse.ptp.remote.ui;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ptp.remote.core.IRemoteServices;
import org.eclipse.ptp.remote.core.PTPRemoteCorePlugin;
import org.eclipse.ptp.remote.ui.messages.Messages;

public class RemoteUIServicesProxy implements IRemoteUIServices {
	private static final String ATTR_ID = "id"; //$NON-NLS-1$
	private static final String ATTR_NAME = "name"; //$NON-NLS-1$
	private static final String ATTR_CLASS = "class"; //$NON-NLS-1$
	
	private static String getAttribute(IConfigurationElement configElement, String name, String defaultValue) {
		String value = configElement.getAttribute(name);
		if (value != null) {
			return value;
		}
		if (defaultValue != null) {
			return defaultValue;
		}
		throw new IllegalArgumentException(NLS.bind(Messages.RemoteUIServicesProxy_1, name));
	}

	private final IConfigurationElement configElement;
	private final String id;
	private final String name;
	private IRemoteUIServicesFactory factory = null;
	private IRemoteUIServicesDelegate delegate = null;
	private IRemoteServices services = null;
	
	public RemoteUIServicesProxy(IConfigurationElement configElement) {
		this.configElement = configElement;
		this.id = getAttribute(configElement, ATTR_ID, null);
		this.name = getAttribute(configElement, ATTR_NAME, this.id);
		getAttribute(configElement, ATTR_CLASS, null);
	}
	
	/**
	 * Get the factory from the plugin
	 * 
	 * @return instance of the factory
	 */
	public IRemoteUIServicesFactory getFactory() {
		if (factory != null) {
			return factory;
		}
		try {
			factory = (IRemoteUIServicesFactory)configElement.createExecutableExtension(ATTR_CLASS);
		} catch (Exception e) {
			PTPRemoteCorePlugin.log(
					NLS.bind(Messages.RemoteUIServicesProxy_2,
					new Object[] {
						configElement.getAttribute(ATTR_CLASS),
						id,
						configElement.getDeclaringExtension().getNamespaceIdentifier()
					}));
		}
		return factory;
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
	 * @see org.eclipse.ptp.remote.ui.IRemoteUIServicesDelegate#getUIConnectionManager(org.eclipse.ptp.remote.core.IRemoteConnectionManager)
	 */
	public IRemoteUIConnectionManager getUIConnectionManager() {
		loadServices();
		if (delegate == null) {
			return null;
		}
		return delegate.getUIConnectionManager();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.ui.IRemoteUIServicesDelegate#getUIFileManager()
	 */
	public IRemoteUIFileManager getUIFileManager() {
		loadServices();
		if (delegate == null) {
			return null;
		}
		return delegate.getUIFileManager();
	}

	/**
	 * Set the remote services associated with this proxy.
	 * 
	 * @param services
	 */
	public void setServices(IRemoteServices services) {
		this.services = services;
	}
	
	/**
	 * Create and initialize the remote UI services factory
	 */
	private void loadServices() {
		if (delegate == null) {
			IRemoteUIServicesFactory factory = getFactory();
			if (factory != null) {
				delegate = factory.getServices(services);
			}
		}
	}
}
