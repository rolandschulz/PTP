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
package org.eclipse.ptp.rdt.sync.core;

import java.util.List;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ptp.ems.core.EnvManagerConfigMap;
import org.eclipse.ptp.ems.core.IEnvManagerConfig;

/**
 * Provides access to environment management properties for a host configuration.
 * This data is accessed and stored as a map by synchronized projects, so a
 * {@link EnvManagerConfigMap} is used as an intermediary.
 * <p>
 * See {@link IEnvManagerConfig} for a description of the settings that are persisted.
 * <p>
 */
public final class EnvManagerHostConfigProperties implements IEnvManagerConfig {
	/** Project with which the Modules project properties are associated. */
	private final HostConfig config;
	private final EnvManagerConfigMap properties;
	private final BuildConfigurationManager bcm;

	/**
	 * Constructor. The constructed object provides access to the environment configuration project properties for the given
	 * project.
	 * 
	 * @param project
	 *            non-<code>null</code>
	 * @throws CoreException on problems reading property data
	 */
	public EnvManagerHostConfigProperties(HostConfig hc) throws CoreException {
		if (hc == null) {
			throw new IllegalArgumentException("config cannot be null"); //$NON-NLS-1$
		}

		this.config = hc;
		bcm = BuildConfigurationManager.getInstance();
		properties = new EnvManagerConfigMap(bcm.getEnvProperties(hc.getCDTConfig()));
	}

	private void saveProperties() {
		try {
			bcm.setEnvProperties(config.getCDTConfig(), properties.getAllProperties());
		} catch (CoreException e) {
			RDTSyncCorePlugin.log("Unable to save changes to environment properties", e); //$NON-NLS-1$
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.ems.core.IEnvManagerConfig#setEnvMgmtEnabled(boolean)
	 */
	@Override
	public void setEnvMgmtEnabled(boolean enabled) {
		properties.setEnvMgmtEnabled(enabled);
		this.saveProperties();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.ems.core.IEnvManagerConfig#isEnvMgmtEnabled()
	 */
	@Override
	public boolean isEnvMgmtEnabled() {
		return properties.isEnvMgmtEnabled();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.ems.core.IEnvManagerConfig#setManualConfig(boolean)
	 */
	@Override
	public void setManualConfig(boolean enabled) {
		properties.setManualConfig(enabled);
		this.saveProperties();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.ems.core.IEnvManagerConfig#isManualConfigEnabled()
	 */
	@Override
	public boolean isManualConfigEnabled() {
		return properties.isManualConfigEnabled();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.ems.core.IEnvManagerConfig#setManualConfigText(java.lang.String)
	 */
	@Override
	public void setManualConfigText(String manualConfigText) {
		properties.setManualConfigText(manualConfigText);
		this.saveProperties();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.ems.core.IEnvManagerConfig#getManualConfigText()
	 */
	@Override
	public String getManualConfigText() {
		return properties.getManualConfigText();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.ems.core.IEnvManagerConfig#setConnectionName(java.lang.String)
	 */
	@Override
	public void setConnectionName(String connectionName) {
		properties.setConnectionName(connectionName);
		this.saveProperties();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.ems.core.IEnvManagerConfig#getConnectionName()
	 */
	@Override
	public String getConnectionName() {
		return properties.getConnectionName();
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
		properties.setConfigElements(selectedModules);
		this.saveProperties();
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
		return properties.getConfigElements();
	}
}
