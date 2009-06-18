/*******************************************************************************
 * Copyright (c) 2007, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.services.ui;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ptp.services.core.IService;
import org.eclipse.ptp.services.core.IServiceProviderDescriptor;

/**
 * A singleton class which is the entry point to a service model which represents:
 * - the set of contributed services
 * - the set of providers which provide those services
 * - the service configurations for each project which specify which services are
 * 		mapped to which providers.
 *
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is no guarantee that this API will work or
 * that it will remain the same. Please do not use this API without consulting
 * with the RDT team.
 * 
 */
public class ServiceModelUIManager {
	private final static String SERVICE_EXTENSION_ID = "serviceContributors"; //$NON-NLS-1$
	private final static String PROVIDER_EXTENSION_ID = "providerContributors"; //$NON-NLS-1$
	private final static String ATTR_ID = "id"; //$NON-NLS-1$
	private final static String ATTR_CLASS = "class"; //$NON-NLS-1$

	private static ServiceModelUIManager fInstance;
	
	public static synchronized ServiceModelUIManager getInstance() {
		if(fInstance == null)
			fInstance = new ServiceModelUIManager();
		return fInstance;
	}
	
	/**
	 * Get the configuration UI associated with a service.
	 * 
	 * @param service service
	 * @return class implementing IServiceContributor
	 */
	public IServiceContributor getServiceContributor(IService service) {
		IServiceContributor contrib = null;
		IExtensionPoint extensionPoint = Platform.getExtensionRegistry().getExtensionPoint(Activator.PLUGIN_ID,	SERVICE_EXTENSION_ID);
		if (extensionPoint != null) {
			for (IExtension extension : extensionPoint.getExtensions()) {
				for (IConfigurationElement element : extension.getConfigurationElements()) {
					if (element.getAttribute(ATTR_ID).equals(service.getId())) {
						try {
							contrib = (IServiceContributor) element.createExecutableExtension(ATTR_CLASS);
						} catch (Exception e) {
							return null;
						}
					}
				}
			}
		}
		return contrib;
	}
	
	/**
	 * Get the configuration UI associated with a service provider.
	 * 
	 * @param desc service provider descriptor
	 * @return class implementing IServiceProviderContributor
	 */
	public IServiceProviderContributor getServiceProviderConfigurationUI(IServiceProviderDescriptor desc) {
		IServiceProviderContributor config = null;
		IExtensionPoint extensionPoint = Platform.getExtensionRegistry().getExtensionPoint(Activator.PLUGIN_ID,	PROVIDER_EXTENSION_ID);
		if (extensionPoint != null) {
			for (IExtension extension : extensionPoint.getExtensions()) {
				for (IConfigurationElement element : extension.getConfigurationElements()) {
					if (element.getAttribute(ATTR_ID).equals(desc.getId())) {
						try {
							config = (IServiceProviderContributor) element.createExecutableExtension(ATTR_CLASS);
						} catch (Exception e) {
							return null;
						}
					}
				}
			}
		}
		return config;
	}
}
