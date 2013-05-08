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
	private static Map<Integer, Map<String, String>> wizardIdToPropertyMap = new HashMap<Integer, Map<String, String>>();
	private static Map<Integer, Map<String, Set<String>>> wizardIdToMultiValuePropertyMap =
			new HashMap<Integer, Map<String, Set<String>>>();
	private static Map<Integer, Map<String, Map<String, String>>> wizardIdToTableMap = new HashMap<Integer, Map<String, Map<String, String>>>();

	/**
	 * Clear all data stored for a particular wizard.
	 * Ideally, this method should be called both on wizard start and wizard finish.
	 *
	 * @param wizardId
	 * 			Unique id of the wizard 
	 */
	public static void clearProperties(int wizardId) {
		wizardIdToPropertyMap.remove(wizardId);
		wizardIdToMultiValuePropertyMap.remove(wizardId);
		wizardIdToTableMap.remove(wizardId);
	}

	/**
	 * Get a property for this wizard.
	 * @param wizardId
	 * @param key
	 * @return value or null if not found.
	 */
	public static String getProperty(int wizardId, String key) {
		Map<String, String> wizardMap = wizardIdToPropertyMap.get(wizardId);
		if (wizardMap == null) {
			return null;
		} else {
			return wizardMap.get(key);
		}
	}

	/**
	 * Get a property table for this wizard.
	 * @param wizardId
	 * @param key
	 * @return a copy of the property table or null if not found.
	 */
	public static Map<String, String> getMap(int wizardId, String key) {
		Map<String, Map<String, String>> wizardMap = wizardIdToTableMap.get(wizardId);
		if ((wizardMap == null) || (!wizardMap.containsKey(key))) {
			return null;
		} else {
			return new HashMap<String, String>(wizardMap.get(key));
		}
	}

	/**
	 * Get a property with multiple values for this wizard.
	 * @param wizardId
	 * @param key
	 * @return a copy of the stored set of values or null if not found.
	 */
	public static Set<String> getMultiValueProperty(int wizardId, String key) {
		Map<String, Set<String>> wizardMap = wizardIdToMultiValuePropertyMap.get(wizardId);
		if ((wizardMap == null) || (!wizardMap.containsKey(key))) {
			return null;
		} else {
			return new HashSet<String>(wizardMap.get(key));
		}
	}

	/**
	 * Set a property for this wizard.
	 * @param wizardId
	 * @param key
	 * @param value
	 */
	public static void setProperty(int wizardId, String key, String value) {
		if (!wizardIdToPropertyMap.containsKey(wizardId)) {
			wizardIdToPropertyMap.put(wizardId, new HashMap<String, String>());
		}
		wizardIdToPropertyMap.get(wizardId).put(key, value);
	}

	/**
	 * Set a property table for this wizard. A copy of the given map is stored, not the map itself.
	 * @param wizardId
	 * @param key
	 * @param map
	 */
	public static void setMap(int wizardId, String key, Map<String, String> map) {
		if (!wizardIdToTableMap.containsKey(wizardId)) {
			wizardIdToTableMap.put(wizardId, new HashMap<String, Map<String, String>>());
		}
		wizardIdToTableMap.get(wizardId).put(key, new HashMap<String, String>(map));
	}
	/**
	 * Set a property with multiple values for this wizard. A copy of the given set is stored, not the set itself.
	 * @param wizardId
	 * @param key
	 * @param values
	 */
	public static void setMultiValueProperty(int wizardId, String key, Set<String> values) {
		if (!wizardIdToMultiValuePropertyMap.containsKey(wizardId)) {
			wizardIdToMultiValuePropertyMap.put(wizardId, new HashMap<String, Set<String>>());
		}
		wizardIdToMultiValuePropertyMap.get(wizardId).put(key, new HashSet<String>(values));
	}
}