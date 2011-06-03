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
import java.util.HashMap;
import java.util.Map;

import org.eclipse.ptp.rm.jaxb.core.IJAXBResourceManagerConfiguration;
import org.eclipse.ptp.rm.jaxb.ui.util.JAXBExtensionUtils;
import org.eclipse.ptp.rmsystem.IResourceManagerConfiguration;
import org.eclipse.ptp.ui.wizards.RMConfigurationSelectionFactory;

/**
 * For retrieving and loading configurations for the JAXB class of resource
 * managers. Looks for configurations in two ways: by accessing extension
 * providers, and by searching in the workspace for a project called
 * "resourceManagers". The latter allows the user to import custom XML
 * configurations.
 * 
 */
public class JAXBRMConfigurationSelectionFactory extends RMConfigurationSelectionFactory {

	private static Map<String, URL> fRMJAXBResourceManagers = null;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.ui.wizards.RMConfigurationSelectionFactory#
	 * getConfigurationTypes()
	 */
	@Override
	public String[] getConfigurationNames() {
		return getJAXBResourceManagerNames();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.ui.wizards.RMConfigurationSelectionFactory#
	 * setConfigurationType(java.lang.String,
	 * org.eclipse.ptp.rmsystem.IResourceManagerConfiguration)
	 */
	@Override
	public void setConfigurationName(String name, IResourceManagerConfiguration configuration) {
		if (configuration instanceof IJAXBResourceManagerConfiguration) {
			IJAXBResourceManagerConfiguration jaxbConfiguration = (IJAXBResourceManagerConfiguration) configuration;
			jaxbConfiguration.setRMConfigurationURL(getJAXBResourceManagerDefinition(name));
		}
		configuration.setName(name);
	}

	/**
	 * Looks up the XML configuration and returns its location; called when the
	 * user selects a name from the wizard list.
	 * 
	 * @param name
	 * @return URL of the configuration
	 */
	private URL getJAXBResourceManagerDefinition(String name) {
		loadJAXBResourceManagers(false);
		if (fRMJAXBResourceManagers != null) {
			return fRMJAXBResourceManagers.get(name);
		}
		return null;
	}

	/**
	 * Gathers together the names of all the available JAXB resource manager
	 * configurations from both plugins providing extensions to the extension
	 * point as well as from the user's workspace.
	 * 
	 * @return array of names to display in wizard table
	 */
	private String[] getJAXBResourceManagerNames() {
		loadJAXBResourceManagers(true);
		if (fRMJAXBResourceManagers != null) {
			return fRMJAXBResourceManagers.keySet().toArray(new String[0]);
		}
		return new String[0];
	}

	/**
	 * Wrapper method. Calls
	 * {@link JAXBExtensionUtils#loadJAXBResourceManagers(Map, boolean)}
	 */
	private static void loadJAXBResourceManagers(boolean showError) {
		if (fRMJAXBResourceManagers == null) {
			fRMJAXBResourceManagers = new HashMap<String, URL>();
		} else {
			fRMJAXBResourceManagers.clear();
		}

		JAXBExtensionUtils.loadJAXBResourceManagers(fRMJAXBResourceManagers, showError);
	}
}
