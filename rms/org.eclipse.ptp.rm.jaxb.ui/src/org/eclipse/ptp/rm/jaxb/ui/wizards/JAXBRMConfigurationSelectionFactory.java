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

import java.io.File;
import java.io.FilenameFilter;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ptp.rm.jaxb.core.IJAXBResourceManagerConfiguration;
import org.eclipse.ptp.rm.jaxb.core.utils.JAXBInitializationUtils;
import org.eclipse.ptp.rm.jaxb.ui.IJAXBUINonNLSConstants;
import org.eclipse.ptp.rm.jaxb.ui.JAXBUIPlugin;
import org.eclipse.ptp.rm.jaxb.ui.messages.Messages;
import org.eclipse.ptp.rm.jaxb.ui.util.WidgetActionUtils;
import org.eclipse.ptp.rmsystem.IResourceManagerConfiguration;
import org.eclipse.ptp.ui.wizards.RMConfigurationSelectionFactory;
import org.eclipse.swt.widgets.Display;
import org.osgi.framework.Bundle;

/**
 * For retrieving and loading configurations for the JAXB class of resource
 * managers. Looks for configurations in two ways: by accessing extension
 * providers, and by searching in the workspace for a project called
 * "resourceManagers". The latter allows the user to import custom XML
 * configurations.
 * 
 */
public class JAXBRMConfigurationSelectionFactory extends RMConfigurationSelectionFactory implements IJAXBUINonNLSConstants {

	/**
	 * For searching the "resourceManagers" project for .xml files.
	 */
	private static final FilenameFilter xmlFilter = new FilenameFilter() {
		public boolean accept(File dir, String name) {
			File f = new File(dir, name);
			return name.endsWith(DOT_XML) && f.isFile();
		}
	};

	private static Map<String, Map<String, URL>> fRMJAXBResourceManagers = null;

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
			jaxbConfiguration.setRMConfigurationURL(getJAXBResourceManagerConfiguration(name));
		}
		configuration.setName(name);
	}

	/**
	 * Looks up the configuration; called when the user selects a name from the
	 * wizard list.
	 * 
	 * @param name
	 * @return location of the configuration
	 */
	private URL getJAXBResourceManagerConfiguration(String name) {
		loadJAXBResourceManagers(false);
		Map<String, URL> info = fRMJAXBResourceManagers.get(getId());
		if (info != null) {
			return info.get(name);
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
		Map<String, URL> info = fRMJAXBResourceManagers.get(getId());
		if (info != null) {
			return info.keySet().toArray(new String[0]);
		}
		return new String[0];
	}

	/**
	 * Gets all extensions to the
	 * org.eclipse.ptp.rm.jaxb.core.JAXBResourceManagerConfigurations extension
	 * point and loads their names and locations.
	 * 
	 * @param resourceManagers
	 *            map of extId to map(name, URL)
	 */
	static void loadExtensions(Map<String, Map<String, URL>> resourceManagers) {
		IExtensionRegistry registry = Platform.getExtensionRegistry();
		IExtensionPoint extensionPoint = registry.getExtensionPoint(RM_CONFIG_EXTENSION_POINT);

		if (extensionPoint != null) {
			for (IExtension ext : extensionPoint.getExtensions()) {
				for (IConfigurationElement ce : ext.getConfigurationElements()) {
					String id = ce.getAttribute(ID);
					Map<String, URL> info = resourceManagers.get(id);
					if (info == null) {
						info = new HashMap<String, URL>();
						resourceManagers.put(id, info);
					}
					String name = ce.getAttribute(NAME);
					String configurationFile = ce.getAttribute(CONFIGURATION_FILE_ATTRIBUTE);
					String bundleId = ce.getDeclaringExtension().getContributor().getName();
					Bundle bundle = Platform.getBundle(bundleId);
					if (bundle != null) {
						URL url = bundle.getEntry(configurationFile);
						if (url != null) {
							info.put(name, url);
						}
					}
				}
			}
		}
	}

	/**
	 * Searches for .xml files contained in the user's workspace under the
	 * project named "resourceManagers". Validates each one found and on failed
	 * validation displays an error message (if so indicated).
	 * 
	 * @param showError
	 *            display an error message if any configuration is invalid
	 *            (against the internal XSD). Only true when loading the widget
	 *            the first time.
	 */
	private static void loadExternal(boolean showError) {
		Map<String, URL> info = fRMJAXBResourceManagers.get(JAXB_SERVICE_PROVIDER_EXTPT);
		if (info == null) {
			info = new HashMap<String, URL>();
			fRMJAXBResourceManagers.put(JAXB_SERVICE_PROVIDER_EXTPT, info);
		}
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(RESOURCE_MANAGERS);
		StringBuffer invalid = new StringBuffer();
		if (project != null) {
			IPath path = project.getLocation();
			if (path != null) {
				File dir = path.toFile();
				File[] custom = dir.listFiles(xmlFilter);
				for (File rm : custom) {
					try {
						String name = rm.getName();
						name = name.substring(0, name.length() - 4);
						URI uri = rm.toURI();
						URL url = uri.toURL();
						try {
							JAXBInitializationUtils.validate(url);
						} catch (Throwable t) {
							invalid.append(LINE_SEP).append(name);
							JAXBUIPlugin.log(t.getMessage());
							continue;
						}
						info.put(name, url);
					} catch (MalformedURLException t) {
						JAXBUIPlugin.log(t);
					}
				}
			}
		}
		if (showError && invalid.length() > 0) {
			WidgetActionUtils.errorMessage(Display.getCurrent().getActiveShell(), null,
					Messages.InvalidConfiguration + invalid.toString(), Messages.InvalidConfiguration_title, false);
		}
	}

	/**
	 * Wrapper method. Calls {@link #loadExtensions()} and
	 * {@link #loadExternal(boolean)}.
	 */
	private static void loadJAXBResourceManagers(boolean showError) {
		if (fRMJAXBResourceManagers == null) {
			fRMJAXBResourceManagers = new HashMap<String, Map<String, URL>>();
		} else {
			fRMJAXBResourceManagers.clear();
		}

		loadExtensions(fRMJAXBResourceManagers);

		/*
		 * Also search the workspace for managers. By convention these should
		 * all go in a directory called "resourceManagers". Loads only valid XML
		 */
		loadExternal(showError);
	}
}
