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

import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.Status;
import org.eclipse.ptp.internal.ems.core.EMSCorePlugin;

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

	/**
	 * Keys used to persist the environment configuration properties.
	 * <p>
	 * Since {@link IResource#setPersistentProperty(QualifiedName, String)} sets an upper limit on the number of characters that can
	 * be stored in a property (and this may easily be exceeded in practice; see Bug 377986), the environment configuration string
	 * is split into substrings of no more than {@link #MAX_PROJECT_PROPERTY_LENGTH} characters each, each of which is stored as a
	 * separate project property.
	 */
	private static final QualifiedName[] PROPERTY_KEYS = new QualifiedName[]
	{
			new QualifiedName(EMSCorePlugin.PLUGIN_ID, ".envConfig"), //$NON-NLS-1$
			new QualifiedName(EMSCorePlugin.PLUGIN_ID, ".envConfig2"), //$NON-NLS-1$
			new QualifiedName(EMSCorePlugin.PLUGIN_ID, ".envConfig3"), //$NON-NLS-1$
			new QualifiedName(EMSCorePlugin.PLUGIN_ID, ".envConfig4"), //$NON-NLS-1$
			new QualifiedName(EMSCorePlugin.PLUGIN_ID, ".envConfig5"), //$NON-NLS-1$
	};

	/** The maximum length of a string storable by {@link IResource#setPersistentProperty(QualifiedName, String)} */
	private static final int MAX_PROJECT_PROPERTY_LENGTH = 2048;

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
			return new EnvManagerConfigString(assembleStringFromProperties());
		} catch (final CoreException e) {
			EMSCorePlugin.log(e);
			throw new Error(e);
		}
	}

	/** Concatenates the values of the {@link #PROPERTY_KEYS} project properties into a single string. */
	private String assembleStringFromProperties() throws CoreException {
		final StringBuilder sb = new StringBuilder();
		for (QualifiedName key : PROPERTY_KEYS) {
			sb.append(emptyIfNull(project.getPersistentProperty(key)));
		}
		return sb.toString();
	}

	/** @return the empty string if <i>string</i> is <code>null</code>, or <i>string</i> otherwise */
	private String emptyIfNull(String string) {
		return string == null ? "" : string; //$NON-NLS-1$
	}

	private void save(EnvManagerConfigString properties) {
		try {
			final String[] strings = splitString(properties.toString());
			assert PROPERTY_KEYS.length == strings.length;
			for (int i = 0; i < strings.length; i++) {
				project.setPersistentProperty(PROPERTY_KEYS[i], strings[i]);
			}
		} catch (final CoreException e) {
			EMSCorePlugin.log(e);
			throw new Error(e);
		}
	}

	/**
	 * Splits a string into substrings of length <= {@value #MAX_PROJECT_PROPERTY_LENGTH}.
	 * 
	 * @throws CoreException
	 *             if the number of substrings required is greater than {@link #PROPERTY_KEYS}.length
	 */
	private String[] splitString(String string) throws CoreException {
		final String[] result = new String[PROPERTY_KEYS.length];

		final int maxLength = PROPERTY_KEYS.length * MAX_PROJECT_PROPERTY_LENGTH;
		if (string.length() > maxLength) {
			throw new CoreException(
					new Status(IStatus.ERROR,
							EMSCorePlugin.PLUGIN_ID,
							"Environment configuration text cannot exceed " + maxLength + " characters")); //$NON-NLS-1$ //$NON-NLS-2$
		}

		for (int i = 0; i < result.length; i++) {
			final int startIndex = i * MAX_PROJECT_PROPERTY_LENGTH;
			if (startIndex < string.length()) {
				final int endIndex = Math.min(startIndex + MAX_PROJECT_PROPERTY_LENGTH, string.length());
				result[i] = string.substring(startIndex, endIndex);
			} else {
				result[i] = ""; //$NON-NLS-1$
			}
		}

		return result;
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
	/**
	 * @since 2.0
	 */
	@Override
	public void setConfigElements(List<String> selectedModules) {
		final EnvManagerConfigString properties = loadProperties();
		properties.setConfigElements(selectedModules);
		save(properties);
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
		return loadProperties().getConfigElements();
	}
}
