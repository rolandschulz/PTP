/*******************************************************************************
 * Copyright (c) 2013 Oak Ridge National Laboratory and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    John Eblen - initial implementation
 *******************************************************************************/
package org.eclipse.ptp.internal.rdt.sync.ui.wizards;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * A utility class for storing and exchanging data between the pages of a synchronized project wizard.
 * Currently it supports storing properties with a single value, multiple values, or a map (table) of values. Only string
 * properties and values are supported.
 */
public class SyncWizardDataCache {
	private static Map<String, String> propertyMap = new HashMap<String, String>();
	private static Map<String, Set<String>> multiValuePropertyMap = new HashMap<String, Set<String>>();
	private static Map<String, Map<String, String>> tableMap = new HashMap<String, Map<String, String>>();

	/**
	 * Clear all data stored. This method should be called on wizard start.
	 */
	public static void clearProperties() {
		propertyMap.clear();
		multiValuePropertyMap.clear();
		tableMap.clear();
	}

	/**
	 * Get a property for this wizard.
	 * @param key
	 * @return value or null if not found.
	 */
	public static String getProperty(String key) {
		return propertyMap.get(key);
	}

	/**
	 * Get a property table for this wizard.
	 * @param key
	 * @return a copy of the property table or null if not found.
	 */
	public static Map<String, String> getMap(String key) {
		if (!tableMap.containsKey(key)) {
			return null;
		} else {
			return new HashMap<String, String>(tableMap.get(key));
		}
	}

	/**
	 * Get a property with multiple values for this wizard.
	 * @param key
	 * @return a copy of the stored set of values or null if not found.
	 */
	public static Set<String> getMultiValueProperty(String key) {
		if (!multiValuePropertyMap.containsKey(key)) {
			return null;
		} else {
			return new HashSet<String>(multiValuePropertyMap.get(key));
		}
	}

	/**
	 * Set a property for this wizard.
	 * @param key
	 * @param value
	 */
	public static void setProperty(String key, String value) {
		propertyMap.put(key, value);
	}

	/**
	 * Set a property table for this wizard. A copy of the given map is stored, not the map itself.
	 * @param key
	 * @param map
	 */
	public static void setMap(String key, Map<String, String> map) {
		tableMap.put(key, new HashMap<String, String>(map));
	}
	/**
	 * Set a property with multiple values for this wizard. A copy of the given set is stored, not the set itself.
	 * @param key
	 * @param values
	 */
	public static void setMultiValueProperty(String key, Set<String> values) {
		multiValuePropertyMap.put(key, new HashSet<String>(values));
	}
}