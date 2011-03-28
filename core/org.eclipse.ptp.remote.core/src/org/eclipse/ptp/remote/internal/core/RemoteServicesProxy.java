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
package org.eclipse.ptp.remote.internal.core;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ptp.remote.core.IRemoteServices;
import org.eclipse.ptp.remote.core.IRemoteServicesDescriptor;
import org.eclipse.ptp.remote.core.IRemoteServicesFactory;
import org.eclipse.ptp.remote.core.PTPRemoteCorePlugin;
import org.eclipse.ptp.remote.core.messages.Messages;

public class RemoteServicesProxy implements IRemoteServicesDescriptor {
	private static final String ATTR_ID = "id"; //$NON-NLS-1$
	private static final String ATTR_NAME = "name"; //$NON-NLS-1$
	private static final String ATTR_SCHEME = "scheme"; //$NON-NLS-1$
	private static final String ATTR_CLASS = "class"; //$NON-NLS-1$
	private static final String ATTR_NEWCONNECTIONS = "newConnections"; //$NON-NLS-1$

	private static boolean getAttribute(IConfigurationElement configElement, String name, boolean defaultValue) {
		String attr = configElement.getAttribute(name);
		if (attr != null) {
			return Boolean.parseBoolean(attr);
		}
		return defaultValue;
	}

	private static String getAttribute(IConfigurationElement configElement, String name, String defaultValue) {
		String value = configElement.getAttribute(name);
		if (value != null) {
			return value;
		}
		if (defaultValue != null) {
			return defaultValue;
		}
		throw new IllegalArgumentException(NLS.bind(Messages.RemoteServicesProxy_0, name));
	}

	private final IConfigurationElement fConfigElement;

	private final String fId;
	private final String fName;
	private final String fScheme;
	private final boolean fNewConnections;
	private IRemoteServicesFactory fFactory;
	private IRemoteServices fDelegate = null;

	public RemoteServicesProxy(IConfigurationElement configElement) {
		fConfigElement = configElement;
		fId = getAttribute(configElement, ATTR_ID, null);
		fName = getAttribute(configElement, ATTR_NAME, fId);
		fScheme = getAttribute(configElement, ATTR_SCHEME, null);
		fNewConnections = getAttribute(configElement, ATTR_NEWCONNECTIONS, false);
		getAttribute(configElement, ATTR_CLASS, null);
		fFactory = null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.remote.core.IRemoteServicesDescriptor#canCreateConnections
	 * ()
	 */
	public boolean canCreateConnections() {
		return fNewConnections;
	}

	/**
	 * Get the factory from the plugin
	 * 
	 * @return instance of the factory
	 */
	public IRemoteServicesFactory getFactory() {
		if (fFactory != null) {
			return fFactory;
		}
		try {
			fFactory = (IRemoteServicesFactory) fConfigElement.createExecutableExtension(ATTR_CLASS);
		} catch (Exception e) {
			PTPRemoteCorePlugin
					.log(NLS.bind(Messages.RemoteServicesProxy_1, new Object[] { fConfigElement.getAttribute(ATTR_CLASS), fId,
							fConfigElement.getDeclaringExtension().getNamespaceIdentifier() }));
		}
		return fFactory;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.remote.core.IRemoteServices#getId()
	 */
	public String getId() {
		return fId;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.remote.core.IRemoteServices#getName()
	 */
	public String getName() {
		return fName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.remote.core.IRemoteServices#getScheme()
	 */
	public String getScheme() {
		return fScheme;
	}

	/**
	 * Get the remote services implementation for this descriptor.
	 * 
	 * @return the remote services implementation, or null if initialization
	 *         failed
	 */
	public IRemoteServices getServices() {
		loadServices();
		return fDelegate;
	}

	/**
	 * Create the remote services factory. Note that the services will not be
	 * initialized.
	 */
	private void loadServices() {
		if (fDelegate == null) {
			IRemoteServicesFactory factory = getFactory();
			if (factory != null) {
				fDelegate = factory.getServices(this);
			}
		}
	}
}
