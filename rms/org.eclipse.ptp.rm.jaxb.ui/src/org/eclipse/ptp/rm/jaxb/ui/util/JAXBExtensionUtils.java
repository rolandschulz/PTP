/*******************************************************************************
 * Copyright (c) 2011 University of Illinois All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html 
 * 	
 * Contributors: 
 * 	Albert L. Rossi - design and implementation
 ******************************************************************************/
package org.eclipse.ptp.rm.jaxb.ui.util;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ptp.rm.jaxb.core.JAXBInitializationUtils;
import org.eclipse.ptp.rm.jaxb.core.data.ResourceManagerData;
import org.eclipse.ptp.rm.jaxb.ui.JAXBUIConstants;
import org.eclipse.ptp.rm.jaxb.ui.JAXBUIPlugin;
import org.eclipse.ptp.rm.jaxb.ui.messages.Messages;
import org.eclipse.swt.widgets.Display;
import org.osgi.framework.Bundle;

/**
 * For loading the configurations from extension definitions.
 * 
 * @author arossi
 * 
 */
public class JAXBExtensionUtils {
	private static Map<String, URL> fPluginConfigurations = new HashMap<String, URL>();
	private static Map<String, URL> fExternalConfigurations = new HashMap<String, URL>();
	private static Set<String> fMonitorTypes = new TreeSet<String>();

	public static String[] getConfiguationNames() {
		loadExtensions(true);
		Set<Map.Entry<String, URL>> set = fPluginConfigurations.entrySet();
		set.addAll(fExternalConfigurations.entrySet());
		return set.toArray(new String[0]);
	}

	public static URL getConfigurationURL(String name) {
		loadExtensions(true);
		URL url = fPluginConfigurations.get(name);
		if (url == null) {
			url = fExternalConfigurations.get(name);
		}
		return url;
	}

	public static String[] getMonitorTypes() {
		loadExtensions(true);
		return fMonitorTypes.toArray(new String[0]);
	}

	public static Map<String, URL> getPluginConfiguations() {
		loadExtensions(true);
		Map<String, URL> map = new HashMap<String, URL>();
		map.putAll(fPluginConfigurations);
		map.putAll(fExternalConfigurations);
		return map;
	}

	/**
	 * Wrapper method. Calls {@link #loadExtensions()} and {@link #loadExternal(boolean)}.
	 * 
	 * @param showError
	 *            display an error message if any configuration is invalid (against the internal XSD). Only true when loading the
	 *            widget the first time.
	 */
	public static void loadExtensions(boolean showError) {
		loadPlugins();

		/*
		 * Also search the workspace for managers. By convention these should all go in a directory called "resourceManagers". Loads
		 * only valid XML
		 */
		loadExternal(showError);
	}

	/**
	 * Searches for .xml files contained in the user's workspace under the project named "resourceManagers". Validates each one
	 * found and on failed validation displays an error message (if so indicated).
	 * 
	 * @param showError
	 *            display an error message if any configuration is invalid (against the internal XSD). Only true when loading the
	 *            widget the first time.
	 */
	private static void loadExternal(boolean showError) {
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(JAXBUIConstants.RESOURCE_MANAGERS);
		StringBuffer invalid = new StringBuffer();
		if (project != null) {
			try {
				IResource[] resources = project.members();
				for (IResource resource : resources) {
					if (resource instanceof IFile) {
						IFile file = (IFile) resource;
						if (file.exists() && file.getName().endsWith(JAXBUIConstants.DOT_XML)) {
							try {
								ResourceManagerData data;
								URI uri = file.getLocationURI();
								URL url = uri.toURL();
								try {
									data = JAXBInitializationUtils.initializeRMData(url);
								} catch (Throwable t) {
									invalid.append(JAXBUIConstants.LINE_SEP).append(file.getName());
									JAXBUIPlugin.log(t.getMessage());
									continue;
								}
								fExternalConfigurations.put(data.getName(), url);
								if (data.getMonitorData() != null) {
									fMonitorTypes.add(data.getMonitorData().getSchedulerType());
								}
							} catch (MalformedURLException t) {
								JAXBUIPlugin.log(t);
							}

						}
					}
				}
			} catch (CoreException e) {
				JAXBUIPlugin.log(e);
			}
		}
		if (showError && invalid.length() > 0) {
			MessageDialog.openError(Display.getCurrent().getActiveShell(), Messages.InvalidConfiguration_title,
					Messages.InvalidConfiguration + invalid.toString());
		}
	}

	/**
	 * Gets all extensions to the org.eclipse.ptp.rm.jaxb.core.JAXBResourceManagerConfigurations extension point and loads their
	 * names and locations.
	 * 
	 * @param resourceManagers
	 *            map of extId to map(name, URL)
	 */
	private static void loadPlugins() {
		if (fPluginConfigurations.isEmpty()) {
			IExtensionRegistry registry = Platform.getExtensionRegistry();
			IExtensionPoint extensionPoint = registry.getExtensionPoint(JAXBUIConstants.RM_CONFIG_EXTENSION_POINT);

			if (extensionPoint != null) {
				for (IExtension ext : extensionPoint.getExtensions()) {
					for (IConfigurationElement ce : ext.getConfigurationElements()) {
						ce.getAttribute(JAXBUIConstants.ID);
						String name = ce.getAttribute(JAXBUIConstants.NAME);
						String monitorType = ce.getAttribute(JAXBUIConstants.MONITOR_TYPE);
						if (monitorType != null) {
							fMonitorTypes.add(monitorType);
						}
						String configurationFile = ce.getAttribute(JAXBUIConstants.CONFIGURATION_FILE_ATTRIBUTE);
						String bundleId = ce.getDeclaringExtension().getContributor().getName();
						Bundle bundle = Platform.getBundle(bundleId);
						if (bundle != null) {
							URL url = bundle.getEntry(configurationFile);
							if (url != null) {
								fPluginConfigurations.put(name, url);
							}
						}
					}
				}
			}
		}
	}

	/**
	 * For static access only.
	 */
	private JAXBExtensionUtils() {
	}
}
