/*******************************************************************************
 * Copyright (c) 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.rm.lml.monitor.ui;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;

/**
 * For loading the configurations from extension definitions.
 * 
 */
public class ExtensionUtils {
	private static final String EXTENSION_POINT = "monitors"; //$NON-NLS-1$
	private static final String ATTR_TYPE = "type"; //$NON-NLS-1$
	private static final String ATTR_NAME = "name"; //$NON-NLS-1$

	private static Map<String, String> fMonitorsByName = new TreeMap<String, String>();
	private static Map<String, String> fMonitorsByType = new TreeMap<String, String>();

	public static String getMonitorName(String type) {
		loadExtensions();
		if (type != null) {
			return fMonitorsByType.get(type);
		}
		return null;
	}

	public static String[] getMonitorNames() {
		loadExtensions();
		Set<String> set = new TreeSet<String>();
		set.addAll(fMonitorsByName.keySet());
		return set.toArray(new String[0]);
	}

	public static String getMonitorType(String name) {
		loadExtensions();
		if (name != null) {
			return fMonitorsByName.get(name);
		}
		return null;
	}

	public static String[] getMonitorTypes() {
		loadExtensions();
		Set<String> set = new TreeSet<String>();
		set.addAll(fMonitorsByType.keySet());
		return set.toArray(new String[0]);
	}

	/**
	 * Gets all extensions to the monitors extension point and loads their names and types.
	 */
	private static void loadExtensions() {
		if (fMonitorsByName.isEmpty()) {
			IExtensionRegistry registry = Platform.getExtensionRegistry();
			IExtensionPoint extensionPoint = registry.getExtensionPoint(LMLMonitorUIPlugin.getUniqueIdentifier(), EXTENSION_POINT);

			if (extensionPoint != null) {
				for (IExtension ext : extensionPoint.getExtensions()) {
					for (IConfigurationElement ce : ext.getConfigurationElements()) {
						String type = ce.getAttribute(ATTR_TYPE);
						String name = ce.getAttribute(ATTR_NAME);
						if (type != null && name != null) {
							fMonitorsByName.put(name, type);
							fMonitorsByType.put(type, name);
						}
					}
				}
			}
		}
	}

	/**
	 * For static access only.
	 */
	private ExtensionUtils() {
	}
}
