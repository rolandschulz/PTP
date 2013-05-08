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
 * Currently only string values are supported, but this could be expanded to share other types of data.
 */
public class SyncWizardDataCache {
	private static Map<Integer, Map<String, String>> wizardIdToPropertiesMap = new HashMap<Integer, Map<String, String>>();
	private static Map<Integer, Map<String, Set<String>>> wizardIdToMultiValuePropertiesMap =
			new HashMap<Integer, Map<String, Set<String>>>();

	/**
	 * Clear all data stored for a particular wizard.
	 * Ideally, this method should be called both on wizard start and wizard finish.
	 *
	 * @param wizardId
	 * 			Unique id of the wizard 
	 */
	public static void clearProperties(int wizardId) {
		wizardIdToPropertiesMap.remove(wizardId);
	}

	/**
	 * Get a property for this wizard.
	 * @param wizardId
	 * @param key
	 * @param value
	 * @return value or null if not found.
	 */
	public static String getProperty(int wizardId, String key) {
		Map<String, String> wizardMap = wizardIdToPropertiesMap.get(wizardId);
		if (wizardMap == null) {
			return null;
		} else {
			return wizardMap.get(key);
		}
	}

	/**
	 * Get a property with multiple values for this wizard.
	 * @param wizardId
	 * @param key
	 * @param value
	 * @return a copy of the stored set of values or null if not found.
	 */
	public static Set<String> getMultiValueProperty(int wizardId, String key) {
		Map<String, Set<String>> wizardMap = wizardIdToMultiValuePropertiesMap.get(wizardId);
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
		if (!wizardIdToPropertiesMap.containsKey(wizardId)) {
			wizardIdToPropertiesMap.put(wizardId, new HashMap<String, String>());
		}
		wizardIdToPropertiesMap.get(wizardId).put(key, value);
	}
	
	/**
	 * Set a property with multiple values for this wizard. A copy of the given set is stored, not the set itself.
	 * @param wizardId
	 * @param key
	 * @param set of values
	 */
	public static void setMultiValueProperty(int wizardId, String key, Set<String> values) {
		if (!wizardIdToMultiValuePropertiesMap.containsKey(wizardId)) {
			wizardIdToMultiValuePropertiesMap.put(wizardId, new HashMap<String, Set<String>>());
		}
		wizardIdToMultiValuePropertiesMap.get(wizardId).put(key, new HashSet<String>(values));
	}
}