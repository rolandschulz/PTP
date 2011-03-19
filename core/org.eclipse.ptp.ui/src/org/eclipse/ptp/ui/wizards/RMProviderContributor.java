/*******************************************************************************
 * Copyright (c) 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.ui.wizards;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.ptp.core.ModelManager;
import org.eclipse.ptp.core.PTPCorePlugin;
import org.eclipse.ptp.rmsystem.IResourceManagerComponentConfiguration;
import org.eclipse.ptp.services.core.IServiceProvider;
import org.eclipse.ptp.services.core.IServiceProviderWorkingCopy;
import org.eclipse.ptp.services.ui.IServiceProviderContributor;
import org.eclipse.swt.widgets.Composite;

/**
 * @since 5.0
 */
public class RMProviderContributor implements IServiceProviderContributor {

	private static String ID_ATTRIBUTE = "id"; //$NON-NLS-1$
	private static String CLASS_ATTRIBUTE = "class"; //$NON-NLS-1$
	private static String EXTENSION_POINT = "org.eclipse.ptp.ui.rmConfigurationWizards"; //$NON-NLS-1$

	private static Map<String, RMConfigurationWizardPageFactory> fRMConfigurationWizardPageFactories = null;

	private static void getRMConfigurationWizardPageFactories() {
		if (fRMConfigurationWizardPageFactories == null) {
			fRMConfigurationWizardPageFactories = new HashMap<String, RMConfigurationWizardPageFactory>();

			IExtensionRegistry registry = Platform.getExtensionRegistry();
			IExtensionPoint extensionPoint = registry.getExtensionPoint(EXTENSION_POINT);

			for (IExtension ext : extensionPoint.getExtensions()) {
				for (IConfigurationElement ce : ext.getConfigurationElements()) {
					String id = ce.getAttribute(ID_ATTRIBUTE);
					if (ce.getAttribute(CLASS_ATTRIBUTE) != null) {
						try {
							RMConfigurationWizardPageFactory factory = (RMConfigurationWizardPageFactory) ce
									.createExecutableExtension(CLASS_ATTRIBUTE);
							fRMConfigurationWizardPageFactories.put(id, factory);
						} catch (Exception e) {
							PTPCorePlugin.log(e);
						}
					}
				}
			}
		}
	}

	private static RMConfigurationWizardPageFactory getRMConfigurationWizardPageFactory(String id) {
		getRMConfigurationWizardPageFactories();
		return fRMConfigurationWizardPageFactories.get(id);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.services.ui.IServiceProviderContributor#
	 * configureServiceProvider
	 * (org.eclipse.ptp.services.core.IServiceProviderWorkingCopy,
	 * org.eclipse.swt.widgets.Composite)
	 */
	/**
	 * @since 5.0
	 */
	public void configureServiceProvider(IServiceProviderWorkingCopy provider, Composite comp) {
		// Nothing
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.services.ui.IServiceProviderContributor#getWizard(org
	 * .eclipse.ptp.services.core.IServiceProvider,
	 * org.eclipse.jface.wizard.IWizardPage)
	 */
	public IWizard getWizard(IServiceProvider provider, IWizardPage page) {
		return new RMServiceProviderConfigurationWizard(provider, page);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.services.ui.IServiceProviderContributor#getWizardPages
	 * (org.eclipse.ptp.services.core.IServiceProvider)
	 */
	public WizardPage[] getWizardPages(IWizard wizard, IServiceProvider provider) {
		/*
		 * Standard behavior is look up the resource manager factory who's ID is
		 * the same as the service provider ID. Once the RM factory is found, we
		 * can obtain the ID of the control side and use this to locate it's
		 * wizard page factory. The same is then done for the monitor side, and
		 * finally for the RM itself.
		 */
		List<RMConfigurationWizardPage> wizardPages = new ArrayList<RMConfigurationWizardPage>();

		/*
		 * Create wizard pages for control component and set the component
		 * configuration on each page
		 */
		String controlId = ModelManager.getInstance().getControlFactoryId(provider.getId());
		if (controlId != null) {
			RMConfigurationWizardPageFactory factory = getRMConfigurationWizardPageFactory(controlId);
			if (factory != null) {
				IResourceManagerComponentConfiguration config = ModelManager.getInstance().createControlConfiguration(provider);
				RMConfigurationWizardPage[] pages = factory.getPages((IRMConfigurationWizard) wizard);
				for (RMConfigurationWizardPage page : pages) {
					page.setConfiguration(config);
				}
				wizardPages.addAll(Arrays.asList(pages));
			}
		}
		/*
		 * Create wizard pages for monitor component and set the component
		 * configuration on each page
		 */
		String monitorId = ModelManager.getInstance().getMonitorFactoryId(provider.getId());
		if (monitorId != null) {
			RMConfigurationWizardPageFactory factory = getRMConfigurationWizardPageFactory(monitorId);
			if (factory != null) {
				IResourceManagerComponentConfiguration config = ModelManager.getInstance().createMonitorConfiguration(provider);
				RMConfigurationWizardPage[] pages = factory.getPages((IRMConfigurationWizard) wizard);
				for (RMConfigurationWizardPage page : pages) {
					page.setConfiguration(config);
				}
				wizardPages.addAll(Arrays.asList(pages));
			}
		}
		/*
		 * Create wizard pages for base resource manager component
		 */
		RMConfigurationWizardPageFactory factory = getRMConfigurationWizardPageFactory(provider.getId());
		if (factory != null) {
			wizardPages.addAll(Arrays.asList(factory.getPages((IRMConfigurationWizard) wizard)));
		}

		return wizardPages.toArray(new RMConfigurationWizardPage[0]);
	}

}
