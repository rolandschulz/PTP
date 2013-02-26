/*******************************************************************************
 * Copyright (c) 2012 University of Illinois and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Jeff Overbey (Illinois) - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.ems.core;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Provides methods to persist an environment management configuration to a string.
 * <p>
 * See {@link IEnvManagerConfig} for a description of the settings that are persisted.
 * 
 * @author Jeff Overbey
 * 
 * @since 6.0
 */
public final class EnvManagerConfigString implements IEnvManagerConfig {

	/** All valid environment management configuration strings start with this prefix. */
	private static final String MAGIC = "#%PTP_EMS_v1%#"; //$NON-NLS-1$

	/*
	 * Interesting Unicode characters:<br>
	 * U+001C file separator<br>
	 * U+001D group separator<br>
	 * U+001E record separator<br>
	 * U+001F unit separator
	 */

	/**
	 * Separator used to separate key-value pairs. Individual keys and values (and module names) must not contain this
	 * separator, and it must also serve as a regular expression for itself (suitable for use by {@link String#split(String)}).
	 */
	private static final String KV_SEPARATOR = "~#%#~"; //$NON-NLS-1$

	/** Separator character used to create a list of module names. Individual module names must not contain this character. */
	private static final String SEPARATOR = ";"; //$NON-NLS-1$

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

	/**
	 * Returns true iff <code>string</code> is a valid environment management configuration string
	 * 
	 * @param string
	 *            string to test
	 * 
	 * @return true iff <code>string</code> is a valid environment management configuration string
	 */
	public static boolean isEnvMgmtConfigString(String string) {
		return string != null && (string.equals("") || string.startsWith(MAGIC)); //$NON-NLS-1$
	}

	private final Map<String, String> settings = new TreeMap<String, String>();

	/**
	 * Default constructor.
	 */
	public EnvManagerConfigString() {
		settings.put(ENABLE_ENVCONFIG_PROPERTY_KEY, Boolean.FALSE.toString());
		settings.put(ENABLE_MANUAL_CONFIG_PROPERTY_KEY, Boolean.FALSE.toString());
		settings.put(ENVCONFIG_MANUAL_CONFIG_TEXT_PROPERTY_KEY, ""); //$NON-NLS-1$
		settings.put(ENVCONFIG_CONNECTION_NAME_PROPERTY_KEY, null);
		settings.put(ENVCONFIG_PROPERTY_KEY, toString(Collections.<String> emptyList()));
	}

	/**
	 * Creates an {@link EnvManagerConfigString} from a given configuration string.
	 * 
	 * @param configuration
	 *            an environment configuration string, as returned by {@link #toString()}
	 */
	public EnvManagerConfigString(String configuration) {
		if (configuration != null) {
			if (isEnvMgmtConfigString(configuration) && !configuration.equals("")) { //$NON-NLS-1$
				for (final String kvPair : removeMagicPrefix(configuration).split(KV_SEPARATOR)) {
					String key, value;
					final int index = kvPair.indexOf('=');
					if (index < 0) {
						key = kvPair;
						value = ""; //$NON-NLS-1$
					} else {
						key = kvPair.substring(0, index);
						value = kvPair.substring(index + 1);
					}

					// Ensure that the key is valid
					// assert new EnvManagerConfigString().settings.containsKey(key);

					settings.put(key, value);
				}
			}
		}
	}

	private String removeMagicPrefix(String configuration) {
		if (configuration.startsWith(MAGIC + KV_SEPARATOR)) {
			return configuration.substring(MAGIC.length() + KV_SEPARATOR.length());
		} else if (configuration.startsWith(MAGIC)) {
			return configuration.substring(MAGIC.length());
		} else {
			throw new IllegalStateException();
		}
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append(MAGIC);
		for (final String key : settings.keySet()) {
			sb.append(KV_SEPARATOR);
			sb.append(key);
			sb.append("="); //$NON-NLS-1$
			final String value = settings.get(key);
			sb.append(value == null ? "" : value); //$NON-NLS-1$
		}
		return sb.toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.ems.core.IEnvManagerConfig#setEnvMgmtEnabled(boolean)
	 */
	@Override
	public void setEnvMgmtEnabled(boolean enabled) {
		settings.put(ENABLE_ENVCONFIG_PROPERTY_KEY, Boolean.toString(enabled));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.ems.core.IEnvManagerConfig#isEnvMgmtEnabled()
	 */
	@Override
	public boolean isEnvMgmtEnabled() {
		return Boolean.parseBoolean(settings.get(ENABLE_ENVCONFIG_PROPERTY_KEY));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.ems.core.IEnvManagerConfig#setManualConfig(boolean)
	 */
	@Override
	public void setManualConfig(boolean enabled) {
		settings.put(ENABLE_MANUAL_CONFIG_PROPERTY_KEY, Boolean.toString(enabled));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.ems.core.IEnvManagerConfig#isManualConfigEnabled()
	 */
	@Override
	public boolean isManualConfigEnabled() {
		return Boolean.parseBoolean(settings.get(ENABLE_MANUAL_CONFIG_PROPERTY_KEY));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.ems.core.IEnvManagerConfig#setManualConfigText(java.lang.String)
	 */
	@Override
	public void setManualConfigText(String manualConfigText) {
		settings.put(ENVCONFIG_MANUAL_CONFIG_TEXT_PROPERTY_KEY, manualConfigText.replace("\r\n", "\r").replace('\n', '\r')); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.ems.core.IEnvManagerConfig#getManualConfigText()
	 */
	@Override
	public String getManualConfigText() {
		final String result = settings.get(ENVCONFIG_MANUAL_CONFIG_TEXT_PROPERTY_KEY);
		return result == null ? "" : result.replace('\r', '\n'); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.ems.core.IEnvManagerConfig#setConnectionName(java.lang.String)
	 */
	@Override
	public void setConnectionName(String connectionName) {
		settings.put(ENVCONFIG_CONNECTION_NAME_PROPERTY_KEY, connectionName);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.ems.core.IEnvManagerConfig#getConnectionName()
	 */
	@Override
	public String getConnectionName() {
		final String result = settings.get(ENVCONFIG_CONNECTION_NAME_PROPERTY_KEY);
		return result == null ? "" : result; //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.ems.core.IEnvManagerConfig#setConfigElements(java.util.Set)
	 */
	/**
	 * @since 2.0
	 */
	@Override
	public void setConfigElements(List<String> selectedModules) {
		settings.put(ENVCONFIG_PROPERTY_KEY, toString(selectedModules));
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
	/**
	 * @since 2.0
	 */
	@Override
	public List<String> getConfigElements() {
		final String modulesProperty = settings.get(ENVCONFIG_PROPERTY_KEY);
		return toList(modulesProperty);
	}

	private static List<String> toList(final String modulesProperty) {
		if (modulesProperty == null || modulesProperty.trim().equals("")) { //$NON-NLS-1$
			return Collections.<String> emptyList();
		} else {
			return Collections.unmodifiableList(Arrays.asList(modulesProperty.split(SEPARATOR)));
		}
	}
}
