/*******************************************************************************
 * Copyright (c) 2012 University of Illinois and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Jeff Overbey (Illinois) - initial API and implementation
 *    John Eblen (ORNL) - modifications to use a map rather than a string
 *******************************************************************************/
package org.eclipse.ptp.ems.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Provides methods to persist an environment management configuration to a map.
 * Modification of EnvManagerConfigString to store data to map instead of string.
 * <p>
 * See {@link IEnvManagerConfig} for a description of the settings that are persisted.
 * @since 2.0
 */
public final class EnvManagerConfigMap implements IEnvManagerConfig {
	/** String to separate module names */
	private static final String SEPARATOR = "$MODULE_NAME_SEPARATOR$"; //$NON-NLS-1$

	/** Key used to persist whether modules should be enabled. */
	private static final String ENABLE_ENVCONFIG_PROPERTY_KEY = "enableEnvConfig"; //$NON-NLS-1$

	/** Key used to persist whether manually-entered configuration commands should be used. */
	private static final String ENABLE_MANUAL_CONFIG_PROPERTY_KEY = "envEnableManualConfig"; //$NON-NLS-1$

	/** Key used to persist manually-entered configuration commands. */
	private static final String ENVCONFIG_MANUAL_CONFIG_TEXT_PROPERTY_KEY = "envManualConfig"; //$NON-NLS-1$

	/** Key used to persist the environment configuration elements selected from a list. */
	private static final String ENVCONFIG_PROPERTY_KEY = "envConfig"; //$NON-NLS-1$

	/** Key used to persist the connection name. */
	private static final String ENVCONFIG_CONNECTION_NAME_PROPERTY_KEY = "envConfigConnectionName"; //$NON-NLS-1$

	private final Map<String, String> defaultProperties = new HashMap<String, String>();
	private final Map<String, String> envProperties = new HashMap<String, String>();

	/**
	 * Default constructor.
	 */
	public EnvManagerConfigMap() {
		defaultProperties.put(ENABLE_ENVCONFIG_PROPERTY_KEY, Boolean.FALSE.toString());
		defaultProperties.put(ENABLE_MANUAL_CONFIG_PROPERTY_KEY, Boolean.FALSE.toString());
		defaultProperties.put(ENVCONFIG_MANUAL_CONFIG_TEXT_PROPERTY_KEY, ""); //$NON-NLS-1$
		defaultProperties.put(ENVCONFIG_CONNECTION_NAME_PROPERTY_KEY, null);
		defaultProperties.put(ENVCONFIG_PROPERTY_KEY, toString(Collections.<String> emptyList()));
	}

	/**
	 * Creates an {@link EnvManagerConfigMap} from a plain map.
	 * 
	 * @param envMap a plain map of properties
	 */
	public EnvManagerConfigMap(Map<String, String> envMap) {
		super();
		envProperties.putAll(envMap);
	}
	
	/**
	 * Creates a copy of an existing {@link EnvManagerConfigMap}
	 *
	 * @param envMap an existing {@link EnvManagerConfigMap}
	 */
	public EnvManagerConfigMap(EnvManagerConfigMap envMap) {
		super();
		envProperties.putAll(envMap.getAllProperties());
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.ems.core.IEnvManagerConfig#setEnvMgmtEnabled(boolean)
	 */
	@Override
	public void setEnvMgmtEnabled(boolean enabled) {
		envProperties.put(ENABLE_ENVCONFIG_PROPERTY_KEY, Boolean.toString(enabled));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.ems.core.IEnvManagerConfig#isEnvMgmtEnabled()
	 */
	@Override
	public boolean isEnvMgmtEnabled() {
		return Boolean.parseBoolean(this.getProperty(ENABLE_ENVCONFIG_PROPERTY_KEY));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.ems.core.IEnvManagerConfig#setManualConfig(boolean)
	 */
	@Override
	public void setManualConfig(boolean enabled) {
		envProperties.put(ENABLE_MANUAL_CONFIG_PROPERTY_KEY, Boolean.toString(enabled));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.ems.core.IEnvManagerConfig#isManualConfigEnabled()
	 */
	@Override
	public boolean isManualConfigEnabled() {
		return Boolean.parseBoolean(this.getProperty(ENABLE_MANUAL_CONFIG_PROPERTY_KEY));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.ems.core.IEnvManagerConfig#setManualConfigText(java.lang.String)
	 */
	@Override
	public void setManualConfigText(String manualConfigText) {
		envProperties.put(ENVCONFIG_MANUAL_CONFIG_TEXT_PROPERTY_KEY, manualConfigText.replace("\r\n", "\r").replace('\n', '\r')); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.ems.core.IEnvManagerConfig#getManualConfigText()
	 */
	@Override
	public String getManualConfigText() {
		final String result = this.getProperty(ENVCONFIG_MANUAL_CONFIG_TEXT_PROPERTY_KEY);
		return result == null ? "" : result.replace('\r', '\n'); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.ems.core.IEnvManagerConfig#setConnectionName(java.lang.String)
	 */
	@Override
	public void setConnectionName(String connectionName) {
		envProperties.put(ENVCONFIG_CONNECTION_NAME_PROPERTY_KEY, connectionName);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.ems.core.IEnvManagerConfig#getConnectionName()
	 */
	@Override
	public String getConnectionName() {
		final String result = this.getProperty(ENVCONFIG_CONNECTION_NAME_PROPERTY_KEY);
		return result == null ? "" : result; //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.ems.core.IEnvManagerConfig#setConfigElements(java.util.Set)
	 */
	@Override
	public void setConfigElements(List<String> selectedModules) {
		envProperties.put(ENVCONFIG_PROPERTY_KEY, toString(selectedModules));
	}

	private static String toString(final List<String> list) {
		if (list == null) {
			return ""; //$NON-NLS-1$
		}

		final StringBuilder sb = new StringBuilder();

		final Iterator<String> it = list.iterator();
		if (it.hasNext()) {
			sb.append(it.next());
		}
		while (it.hasNext()) {
			sb.append(SEPARATOR);
			sb.append(it.next());
		}

		return sb.toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.ems.core.IEnvManagerConfig#getConfigElements()
	 */
	@Override
	public List<String> getConfigElements() {
		final String modulesProperty = this.getProperty(ENVCONFIG_PROPERTY_KEY);
		return toList(modulesProperty);
	}

	private static List<String> toList(final String modulesProperty) {
		if (modulesProperty == null || modulesProperty.trim().equals("")) { //$NON-NLS-1$
			return Collections.<String> emptyList();
		} else {
			return toList(modulesProperty.split(Pattern.quote(SEPARATOR)));
		}
	}

	private static List<String> toList(String[] array) {
		if (array == null) {
			return Collections.<String> emptyList();
		} else {
			final List<String> result = new ArrayList<String>();
			for (final String module : array) {
				result.add(module);
			}
			return Collections.unmodifiableList(result);
		}
	}

	// Additional functionality specific to map implementation

	/**
	 * Get a copy of the underlying map that contains all properties
	 * @return a copy of the stored map.
	 */
	public Map<String, String> getAllProperties() {
		return new HashMap<String, String>(envProperties);
	}
	
	/**
	 * Store additional elements
	 *
	 * @param key
	 * @return value
	 */
	public String getElement(String key) {
		return this.getProperty(key);
	}
	
	/**
	 * Retrieve additional elements
	 *
	 * @param key
	 * @param value
	 */
	public void setElement(String key, String value) {
		envProperties.put(key, value);
	}

	// This method should always be used instead of accessing property map directly.
	// It handles default values and possibly other subtleties.
	private String getProperty(String key) {
		if (envProperties.containsKey(key)) {
			return envProperties.get(key);
		} else {
			return defaultProperties.get(key);
		}
	}

	@Override
	// Hash code calculation that correctly handles maps with missing default values.
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		Map<String, String> myProperties = new HashMap<String, String>(defaultProperties);
		myProperties.putAll(envProperties);	
		result = prime * result + ((myProperties == null) ? 0 : myProperties.hashCode());
		return result;
	}

	@Override
	// Equality definition that correctly handles maps with missing default values.
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		EnvManagerConfigMap other = (EnvManagerConfigMap) obj;
		// Must account for default properties, which may not be stored in the map.
		Map<String, String> myProperties = new HashMap<String, String>(defaultProperties);
		Map<String, String> otherProperties = new HashMap<String, String>(defaultProperties);
		myProperties.putAll(envProperties);
		otherProperties.putAll(other.envProperties);
		if (!myProperties.equals(otherProperties)) {
			return false;
		}
		return true;
	}
}