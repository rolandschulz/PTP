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

import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.ptp.ems.internal.core.EMSCorePlugin;

/**
 * Provides access to environment management project properties for a synchronized remote project.
 * <p>
 * See {@link IEnvManagerConfig} for a description of the settings that are persisted.
 * <p>
 * Internally, only one project property is persisted; it stores all of the environment manager settings using an
 * {@link EnvManagerConfigString}.
 * 
 * @author Jeff Overbey
 * 
 * @since 6.0
 */
public final class EnvManagerProjectProperties implements IEnvManagerConfig {

	/** Key used to persist the environment configuration properties. */
	private static final QualifiedName PROPERTY_KEY = new QualifiedName(EMSCorePlugin.PLUGIN_ID, ".envConfig"); //$NON-NLS-1$

	/** Project with which the Modules project properties are associated. */
	private final IProject project;

	/**
	 * Constructor. The constructed object provides access to the environment configuration project properties for the given
	 * project.
	 * 
	 * @param project
	 *            non-<code>null</code>
	 */
	public EnvManagerProjectProperties(IProject project) {
		if (project == null) {
			throw new IllegalArgumentException("project cannot be null"); //$NON-NLS-1$
		}

		this.project = project;
	}

	private EnvManagerConfigString loadProperties() {
		try {
			return new EnvManagerConfigString(project.getPersistentProperty(PROPERTY_KEY));
		} catch (final CoreException e) {
			EMSCorePlugin.log(e);
			throw new Error(e);
		}
	}

	private void save(EnvManagerConfigString properties) {
		try {
			project.setPersistentProperty(PROPERTY_KEY, properties.toString());
		} catch (final CoreException e) {
			EMSCorePlugin.log(e);
			throw new Error(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.ems.core.IEnvManagerConfig#setEnvMgmtEnabled(boolean)
	 */
	@Override
	public void setEnvMgmtEnabled(boolean enabled) {
		final EnvManagerConfigString properties = loadProperties();
		properties.setEnvMgmtEnabled(enabled);
		save(properties);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.ems.core.IEnvManagerConfig#isEnvMgmtEnabled()
	 */
	@Override
	public boolean isEnvMgmtEnabled() {
		return loadProperties().isEnvMgmtEnabled();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.ems.core.IEnvManagerConfig#setManualConfig(boolean)
	 */
	@Override
	public void setManualConfig(boolean enabled) {
		final EnvManagerConfigString properties = loadProperties();
		properties.setManualConfig(enabled);
		save(properties);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.ems.core.IEnvManagerConfig#isManualConfigEnabled()
	 */
	@Override
	public boolean isManualConfigEnabled() {
		return loadProperties().isManualConfigEnabled();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.ems.core.IEnvManagerConfig#setManualConfigText(java.lang.String)
	 */
	@Override
	public void setManualConfigText(String manualConfigText) {
		final EnvManagerConfigString properties = loadProperties();
		properties.setManualConfigText(manualConfigText);
		save(properties);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.ems.core.IEnvManagerConfig#getManualConfigText()
	 */
	@Override
	public String getManualConfigText() {
		return loadProperties().getManualConfigText();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.ems.core.IEnvManagerConfig#setConnectionName(java.lang.String)
	 */
	@Override
	public void setConnectionName(String connectionName) {
		final EnvManagerConfigString properties = loadProperties();
		properties.setConnectionName(connectionName);
		save(properties);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.ems.core.IEnvManagerConfig#getConnectionName()
	 */
	@Override
	public String getConnectionName() {
		return loadProperties().getConnectionName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.ems.core.IEnvManagerConfig#setConfigElements(java.util.Set)
	 */
	@Override
	public void setConfigElements(Set<String> selectedModules) {
		final EnvManagerConfigString properties = loadProperties();
		properties.setConfigElements(selectedModules);
		save(properties);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.ems.core.IEnvManagerConfig#getConfigElements()
	 */
	@Override
	public Set<String> getConfigElements() {
		return loadProperties().getConfigElements();
	}
}
