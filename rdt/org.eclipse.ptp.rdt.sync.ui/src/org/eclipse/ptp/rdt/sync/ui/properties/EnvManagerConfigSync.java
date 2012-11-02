/*******************************************************************************
 * Copyright (c) 2012 University of Illinois and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    John Eblen (ORNL) - Modified to save properties to sync data structures
 *******************************************************************************/
package org.eclipse.ptp.rdt.sync.ui.properties;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.ptp.ems.core.IEnvManagerConfig;
import org.eclipse.ptp.rdt.sync.core.BuildConfigurationManager;

/**
 * Provides access to environment management properties for a build configuration of a sync project.
 * This class replaces {@link EnvManagerPropertiesPage}, which stored data at project scope (bug 393244).
 * <p>
 * See {@link IEnvManagerConfig} for a description of the settings that are persisted.
 */
public final class EnvManagerConfigSync implements IEnvManagerConfig {
	private static final String ENV_MGMT_ENABLED_KEY = "env-mgmt-enabled"; //$NON-NLS-1$
	private static final String MANUAL_CONFIG_ENABLED_KEY = "manual-config-enabled"; //$NON-NLS-1$
	private static final String MANUAL_CONFIG_TEXT_KEY = "manual-config-text"; //$NON-NLS-1$
	private static final String CONNECTION_NAME_KEY = "manual-config-text"; //$NON-NLS-1$
	private static final String ELEMENT_SEPARATOR = "$EMS-ELEMENT-SEPARATOR$"; //$NON-NLS-1$
	private static final String CONFIG_ELEMENTS_KEY = "config-elements-key"; //$NON-NLS-1$

	private final IConfiguration config;
	private final BuildConfigurationManager bcm;

	/**
	 * Constructor. The constructed object provides access to the environment management properties of the given config.
	 * 
	 * @param config
	 *            non-<code>null</code>
	 */
	public EnvManagerConfigSync(IConfiguration config) {
		if (config == null) {
			throw new IllegalArgumentException("config cannot be null"); //$NON-NLS-1$
		}

		this.config = config;
		this.bcm = BuildConfigurationManager.getInstance();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.ems.core.IEnvManagerConfig#setEnvMgmtEnabled(boolean)
	 */
	@Override
	public void setEnvMgmtEnabled(boolean enabled) {
		bcm.setEnvProperty(config, ENV_MGMT_ENABLED_KEY, Boolean.toString(enabled));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.ems.core.IEnvManagerConfig#isEnvMgmtEnabled()
	 */
	@Override
	public boolean isEnvMgmtEnabled() {
		return Boolean.parseBoolean(bcm.getEnvProperty(config, ENV_MGMT_ENABLED_KEY));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.ems.core.IEnvManagerConfig#setManualConfig(boolean)
	 */
	@Override
	public void setManualConfig(boolean enabled) {
		bcm.setEnvProperty(config, MANUAL_CONFIG_ENABLED_KEY, Boolean.toString(enabled));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.ems.core.IEnvManagerConfig#isManualConfigEnabled()
	 */
	@Override
	public boolean isManualConfigEnabled() {
		return Boolean.parseBoolean(bcm.getEnvProperty(config, MANUAL_CONFIG_ENABLED_KEY));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.ems.core.IEnvManagerConfig#setManualConfigText(java.lang.String)
	 */
	@Override
	public void setManualConfigText(String manualConfigText) {
		bcm.setEnvProperty(config, MANUAL_CONFIG_TEXT_KEY, manualConfigText);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.ems.core.IEnvManagerConfig#getManualConfigText()
	 */
	@Override
	public String getManualConfigText() {
		String text = bcm.getEnvProperty(config, MANUAL_CONFIG_TEXT_KEY);
		if (text == null) {
			return ""; //$NON-NLS-1$
		} else {
			return text;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.ems.core.IEnvManagerConfig#setConnectionName(java.lang.String)
	 */
	@Override
	public void setConnectionName(String connectionName) {
		bcm.setEnvProperty(config, CONNECTION_NAME_KEY, connectionName);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.ems.core.IEnvManagerConfig#getConnectionName()
	 */
	@Override
	public String getConnectionName() {
		String name = bcm.getEnvProperty(config, CONNECTION_NAME_KEY);
		if (name == null) {
			return ""; //$NON-NLS-1$
		} else {
			return name;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.ems.core.IEnvManagerConfig#setConfigElements(java.util.Set)
	 */
	@Override
	public void setConfigElements(Set<String> selectedModules) {
		String storageString = this.buildElementString(selectedModules);
		bcm.setEnvProperty(config, CONFIG_ELEMENTS_KEY, storageString);
	}

	// Compress the set of config elements into a single string for storage
	// Returns null if passed set is null or empty.
	private String buildElementString(Set<String> elementSet) {
		StringBuilder builder = new StringBuilder();
		if (elementSet == null || elementSet.size() == 0) {
			return null;
		}
		for (String s : elementSet) {
			builder.append(s);
			builder.append(ELEMENT_SEPARATOR);
		}
		builder.delete(builder.length()-ELEMENT_SEPARATOR.length(), builder.length());
		return builder.toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.ems.core.IEnvManagerConfig#getConfigElements()
	 */
	@Override
	public Set<String> getConfigElements() {
		String elementString = bcm.getEnvProperty(config, CONFIG_ELEMENTS_KEY);
		return this.buildElementSet(elementString);
	}
	
	// Decompress the config element string into a set of strings.
	// If elementString is null, returns an empty set.
	private Set<String> buildElementSet(String elementString) {
		Set<String> elementSet = new HashSet<String>();
		if (elementString == null) {
			return elementSet;
		}
		String[] elementArray = elementString.split(Pattern.quote(ELEMENT_SEPARATOR));
		for (String e : elementArray) {
			elementSet.add(e);
		}
		return elementSet;
	}
}
