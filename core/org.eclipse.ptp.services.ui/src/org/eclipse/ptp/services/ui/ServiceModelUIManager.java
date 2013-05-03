/*******************************************************************************
 * Copyright (c) 2007, 2009 IBM Corporation and others.
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
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.ptp.internal.services.ui.ServicesUIPlugin;
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
	private final static String WIZARD_EXTENSION_ID = "wizardExtensions"; //$NON-NLS-1$
	private final static String ATTR_ID = "id"; //$NON-NLS-1$
	private final static String ATTR_CLASS = "class"; //$NON-NLS-1$
	private final static String ATTR_UI_CLASS = "configurationUIClass"; //$NON-NLS-1$

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
		IExtensionPoint extensionPoint = Platform.getExtensionRegistry().getExtensionPoint(ServicesUIPlugin.PLUGIN_ID,	SERVICE_EXTENSION_ID);
		if (extensionPoint != null) {
			for (IExtension extension : extensionPoint.getExtensions()) {
				for (IConfigurationElement element : extension.getConfigurationElements()) {
					if (element.getAttribute(ATTR_ID).equals(service.getId())) {
						try {
							return (IServiceContributor) element.createExecutableExtension(ATTR_CLASS);
						} catch (Exception e) {
							ServicesUIPlugin.getDefault().log(e);
							return null;
						}
					}
				}
			}
		}
		return null;
	}
	
	/**
	 * Get the configuration UI associated with a service provider.
	 * 
	 * @param desc service provider descriptor
	 * @return class implementing IServiceProviderContributor
	 */
	public IServiceProviderContributor getServiceProviderContributor(IServiceProviderDescriptor desc) {
		IExtensionPoint extensionPoint = Platform.getExtensionRegistry().getExtensionPoint(ServicesUIPlugin.PLUGIN_ID,	PROVIDER_EXTENSION_ID);
		if (extensionPoint != null) {
			for (IExtension extension : extensionPoint.getExtensions()) {
				for (IConfigurationElement element : extension.getConfigurationElements()) {
					if (element.getAttribute(ATTR_ID).equals(desc.getId())) {
						if(element.getAttribute(ATTR_CLASS) != null) {
							try {
								return (IServiceProviderContributor) element.createExecutableExtension(ATTR_CLASS);
							} catch (Exception e) {
								ServicesUIPlugin.getDefault().log(e);
								return null;
							}
						}
					}
				}
			}
		}
		return null;
	}
	
	/**
	 * Get any wizard extensions.
	 * 
	 * @return IWizard providing a wizard extension
	 */
	public IWizard getWizardExtensions() {
		IExtensionPoint extensionPoint = Platform.getExtensionRegistry().getExtensionPoint(ServicesUIPlugin.PLUGIN_ID,	WIZARD_EXTENSION_ID);
		if (extensionPoint != null) {
			for (IExtension extension : extensionPoint.getExtensions()) {
				for (IConfigurationElement element : extension.getConfigurationElements()) {
					try {
						return (IWizard) element.createExecutableExtension(ATTR_CLASS);
					} catch (Exception e) {
						ServicesUIPlugin.getDefault().log(e);
						return null;
					}
				}
			}
		}
		return null;
	}
	
	/**
	 * @param desc
	 * @return
	 * @deprecated
	 */
	public IServiceProviderConfiguration getServiceProviderConfigurationUI(IServiceProviderDescriptor desc) {
		IServiceProviderConfiguration config = null;
		IExtensionPoint extensionPoint = Platform.getExtensionRegistry().getExtensionPoint(ServicesUIPlugin.PLUGIN_ID,	PROVIDER_EXTENSION_ID);
		if (extensionPoint != null) {
			for (IExtension extension : extensionPoint.getExtensions()) {
				for (IConfigurationElement element : extension.getConfigurationElements()) {
					if (element.getAttribute(ATTR_ID).equals(desc.getId())) {
						try {
							config = (IServiceProviderConfiguration) element.createExecutableExtension(ATTR_UI_CLASS);
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
