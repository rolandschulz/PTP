/**
 * Copyright (c) 2011 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *
 */
package org.eclipse.ptp.rm.jaxb.ui.wizards;

import java.io.File;
import java.io.FilenameFilter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ptp.rm.jaxb.core.IJAXBResourceManagerConfiguration;
import org.eclipse.ptp.rm.jaxb.ui.IJAXBUINonNLSConstants;
import org.eclipse.ptp.rm.jaxb.ui.JAXBUIPlugin;
import org.eclipse.ptp.rmsystem.IResourceManagerConfiguration;
import org.eclipse.ptp.ui.wizards.RMConfigurationSelectionFactory;
import org.osgi.framework.Bundle;

public class JAXBRMConfigurationSelectionFactory extends RMConfigurationSelectionFactory implements IJAXBUINonNLSConstants {

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
			((IJAXBResourceManagerConfiguration) configuration).setRMConfigurationURL(getJAXBResourceManagerConfiguration(name));
			configuration.setName(name);
		}
	}

	private URL getJAXBResourceManagerConfiguration(String name) {
		loadJAXBResourceManagers();
		Map<String, URL> info = fRMJAXBResourceManagers.get(getId());
		if (info != null) {
			return info.get(name);
		}
		return null;
	}

	private String[] getJAXBResourceManagerNames() {
		loadJAXBResourceManagers();
		Map<String, URL> info = fRMJAXBResourceManagers.get(getId());
		if (info != null) {
			return info.keySet().toArray(new String[0]);
		}
		return new String[0];
	}

	private static void loadJAXBResourceManagers() {
		if (fRMJAXBResourceManagers == null) {
			fRMJAXBResourceManagers = new HashMap<String, Map<String, URL>>();
		} else {
			fRMJAXBResourceManagers.clear();
		}

		IExtensionRegistry registry = Platform.getExtensionRegistry();
		IExtensionPoint extensionPoint = registry.getExtensionPoint(RM_CONFIG_EXTENSION_POINT);

		if (extensionPoint != null) {
			for (IExtension ext : extensionPoint.getExtensions()) {
				for (IConfigurationElement ce : ext.getConfigurationElements()) {
					String id = ce.getAttribute(ID);
					Map<String, URL> info = fRMJAXBResourceManagers.get(id);
					if (info == null) {
						info = new HashMap<String, URL>();
						fRMJAXBResourceManagers.put(id, info);
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

		/*
		 * Also search the workspace for managers. By convention these should
		 * all go in a directory called "resourceManagers"
		 */
		Map<String, URL> info = fRMJAXBResourceManagers.get(JAXB_SERVICE_PROVIDER_EXTPT);
		if (info == null) {
			info = new HashMap<String, URL>();
			fRMJAXBResourceManagers.put(JAXB_SERVICE_PROVIDER_EXTPT, info);
		}
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(RESOURCE_MANAGERS);
		if (project != null) {
			File dir = project.getLocation().toFile();
			File[] custom = dir.listFiles(xmlFilter);
			for (File rm : custom) {
				try {
					String name = rm.getName();
					name = name.substring(0, name.length() - 4);
					info.put(name, rm.toURL());
				} catch (MalformedURLException t) {
					JAXBUIPlugin.log(t);
				}
			}
		}
	}
}
