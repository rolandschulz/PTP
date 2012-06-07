/**
 * Copyright (c) 2011 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *     Albert L. Rossi - modifications to support loading external XML; 
 *                       also included the unmarshaling call here so the 
 *                       data is available to the other wizard pages.
 */
package org.eclipse.ptp.rm.jaxb.ui.wizards;

import java.net.URL;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ptp.rm.jaxb.core.IJAXBResourceManagerConfiguration;
import org.eclipse.ptp.rm.jaxb.core.JAXBExtensionUtils;
import org.eclipse.ptp.rm.jaxb.ui.messages.Messages;
import org.eclipse.ptp.rmsystem.IResourceManagerConfiguration;
import org.eclipse.ptp.ui.wizards.RMConfigurationSelectionFactory;
import org.eclipse.swt.widgets.Display;

/**
 * For retrieving and loading configurations for the JAXB class of resource managers. Looks for configurations in two ways: by
 * accessing extension providers, and by searching in the workspace for a project called "resourceManagers". The latter allows the
 * user to import custom XML configurations.
 * 
 */
public class JAXBRMConfigurationSelectionFactory extends RMConfigurationSelectionFactory {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.ui.wizards.RMConfigurationSelectionFactory# getConfigurationTypes()
	 */
	@Override
	public String[] getConfigurationNames() {
		String[] configs = JAXBExtensionUtils.getConfiguationNames();
		if (JAXBExtensionUtils.getInvalid() != null) {
			MessageDialog.openError(Display.getCurrent().getActiveShell(), Messages.InvalidConfiguration_title,
					Messages.InvalidConfiguration + JAXBExtensionUtils.getInvalid());
		}

		return configs;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.ui.wizards.RMConfigurationSelectionFactory# setConfigurationType(java.lang.String,
	 * org.eclipse.ptp.rmsystem.IResourceManagerConfiguration)
	 */
	@Override
	public void setConfigurationName(String name, IResourceManagerConfiguration configuration) {
		if (configuration instanceof IJAXBResourceManagerConfiguration) {
			IJAXBResourceManagerConfiguration jaxbConfiguration = (IJAXBResourceManagerConfiguration) configuration;
			URL url = JAXBExtensionUtils.getConfigurationURL(name);
			if (JAXBExtensionUtils.getInvalid() != null) {
				MessageDialog.openError(Display.getCurrent().getActiveShell(), Messages.InvalidConfiguration_title,
						Messages.InvalidConfiguration + JAXBExtensionUtils.getInvalid());
			}
			jaxbConfiguration.setRMConfigurationURL(url);
		}
		configuration.setName(name);
	}
}
