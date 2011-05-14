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

import java.io.File;
import java.io.FilenameFilter;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ptp.rm.jaxb.core.JAXBInitializationUtils;
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

	/**
	 * For searching the "resourceManagers" project for .xml files.
	 */
	private static final FilenameFilter xmlFilter = new FilenameFilter() {
		public boolean accept(File dir, String name) {
			File f = new File(dir, name);
			return name.endsWith(JAXBUIConstants.DOT_XML) && f.isFile();
		}
	};

	/**
	 * For static access only.
	 */
	private JAXBExtensionUtils() {
	}

	/**
	 * Gets all extensions to the
	 * org.eclipse.ptp.rm.jaxb.core.JAXBResourceManagerConfigurations extension
	 * point and loads their names and locations.
	 * 
	 * @param resourceManagers
	 *            map of extId to map(name, URL)
	 */
	public static void loadExtensions(Map<String, URL> resourceManagers) {
		IExtensionRegistry registry = Platform.getExtensionRegistry();
		IExtensionPoint extensionPoint = registry.getExtensionPoint(JAXBUIConstants.RM_CONFIG_EXTENSION_POINT);

		if (extensionPoint != null) {
			for (IExtension ext : extensionPoint.getExtensions()) {
				for (IConfigurationElement ce : ext.getConfigurationElements()) {
					ce.getAttribute(JAXBUIConstants.ID);
					String name = ce.getAttribute(JAXBUIConstants.NAME);
					String configurationFile = ce.getAttribute(JAXBUIConstants.CONFIGURATION_FILE_ATTRIBUTE);
					String bundleId = ce.getDeclaringExtension().getContributor().getName();
					Bundle bundle = Platform.getBundle(bundleId);
					if (bundle != null) {
						URL url = bundle.getEntry(configurationFile);
						if (url != null) {
							resourceManagers.put(name, url);
						}
					}
				}
			}
		}
	}

	/**
	 * Wrapper method. Calls {@link #loadExtensions()} and
	 * {@link #loadExternal(boolean)}.
	 * 
	 * @param resourceManagers
	 *            map of extId to map(name, URL)
	 * @param showError
	 *            display an error message if any configuration is invalid
	 *            (against the internal XSD). Only true when loading the widget
	 *            the first time.
	 */
	public static void loadJAXBResourceManagers(Map<String, URL> resourceManagers, boolean showError) {
		loadExtensions(resourceManagers);

		/*
		 * Also search the workspace for managers. By convention these should
		 * all go in a directory called "resourceManagers". Loads only valid XML
		 */
		loadExternal(resourceManagers, showError);
	}

	/**
	 * Searches for .xml files contained in the user's workspace under the
	 * project named "resourceManagers". Validates each one found and on failed
	 * validation displays an error message (if so indicated).
	 * 
	 * @param resourceManagers
	 *            map of extId to map(name, URL)
	 * @param showError
	 *            display an error message if any configuration is invalid
	 *            (against the internal XSD). Only true when loading the widget
	 *            the first time.
	 */
	private static void loadExternal(Map<String, URL> resourceManagers, boolean showError) {
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(JAXBUIConstants.RESOURCE_MANAGERS);
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
							invalid.append(JAXBUIConstants.LINE_SEP).append(name);
							JAXBUIPlugin.log(t.getMessage());
							continue;
						}
						resourceManagers.put(name, url);
					} catch (MalformedURLException t) {
						JAXBUIPlugin.log(t);
					}
				}
			}
		}
		if (showError && invalid.length() > 0) {
			MessageDialog.openError(Display.getCurrent().getActiveShell(), Messages.InvalidConfiguration_title,
					Messages.InvalidConfiguration + invalid.toString());
		}
	}
}
