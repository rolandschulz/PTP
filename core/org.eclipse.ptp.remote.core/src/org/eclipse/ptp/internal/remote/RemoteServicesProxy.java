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
package org.eclipse.ptp.internal.remote;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.ptp.remote.AbstractRemoteServicesFactory;
import org.eclipse.ptp.remote.IRemoteServices;
import org.eclipse.ptp.remote.PTPRemotePlugin;


public class RemoteServicesProxy {
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
	
	private final IConfigurationElement configElement;
	private final String id;
	private final String name;
	private AbstractRemoteServicesFactory factory;
	
	public RemoteServicesProxy(IConfigurationElement configElement) {
		this.configElement = configElement;
		this.id = getAttribute(configElement, ATTR_ID, null);
		this.name = getAttribute(configElement, ATTR_NAME, this.id);
		getAttribute(configElement, ATTR_CLASS, null);
		this.factory = null;
	}
	
	public String getId() {
		return id;
	}
	
	public String getName() {
		return name;
	}
	
	public IRemoteServices loadRemoteServices() {
		AbstractRemoteServicesFactory factory = getFactory();
		if (factory == null) {
			return null;
		}
		return factory.create();
	}
	
	public AbstractRemoteServicesFactory getFactory() {
		if (factory != null) {
			return factory;
		}
		try {
			factory = (AbstractRemoteServicesFactory)configElement.createExecutableExtension(ATTR_CLASS);
		} catch (Exception e) {
			PTPRemotePlugin.log(
					"Failed to instatiate factory: "
					+ configElement.getAttribute(ATTR_CLASS)
					+ " in type: "
					+ id
					+ " in plugin: "
					+ configElement.getDeclaringExtension().getNamespaceIdentifier());
		}
		return factory;
	}
}
