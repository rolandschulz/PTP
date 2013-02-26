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

/**
 * A configuration of an environment management system.
 * <p>
 * The following configuration settings are persisted.
 * <ul>
 * <li><b>Enablement.</b> This is true iff custom environment manager settings should be used (i.e., a custom set of environment
 * modules or SoftEnv macros should be used, rather than the defaults).
 * <li><b>A list of configuration elements.</b> These are the names of the modules/SoftEnv macros/etc. that should be loaded (if
 * enabled).
 * <li><b>A connection name.</b> This is the name of the connection for which this configuration was constructed. This is used
 * exclusively for sanity checking: if the stored connection name is different than the actual connection name, then the stored
 * configuration is probably for a different machine, and so the configuration is probably incomplete or invalid for the actual
 * machine.
 * </ul>
 * 
 * @author Jeff Overbey
 * 
 * @since 6.0
 */
public interface IEnvManagerConfig {

	/**
	 * Sets a configuration setting indicating whether environment management support is enabled.
	 * 
	 * @param enabled
	 *            <code>true</code> to use a custom set of modules; <code>false</code> to use defaults
	 */
	void setEnvMgmtEnabled(boolean enabled);

	/**
	 * Returns the value of a configuration setting indicating whether environment management support is enabled for this project.
	 * 
	 * @return non-<code>null</code>
	 */
	boolean isEnvMgmtEnabled();

	/**
	 * Sets a configuration setting indicating whether manually-entered configuration commands should be used.
	 * 
	 * @param enabled
	 *            <code>true</code> to use a custom set of modules; <code>false</code> to use defaults
	 */
	void setManualConfig(boolean enabled);

	/**
	 * Returns the value of a configuration setting indicating whether manually-entered configuration commands should be used.
	 * 
	 * @return non-<code>null</code>
	 */
	boolean isManualConfigEnabled();

	/**
	 * Sets a configuration setting containing manually-entered configuration commands.
	 * <p>
	 * Note that <code>null</code> is indistinguishable from the empty string: in both cases, {@link #getManualConfigText()} will
	 * return the empty string.
	 * 
	 * @param manualConfigText
	 */
	void setManualConfigText(String manualConfigText);

	/**
	 * Returns the value of a configuration setting containing manually-entered configuration commands.
	 * 
	 * @return non-<code>null</code>
	 */
	String getManualConfigText();

	/**
	 * Sets a configuration setting containing the list of selected environment configuration elements (environment modules/SoftEnv
	 * macros/etc.).
	 * <p>
	 * Note that <code>null</code> is indistinguishable from the empty string: in both cases, {@link #getConfigElements()} will
	 * return an empty list.
	 * 
	 * @param selectedModules
	 *            (may be <code>null</code>)
	 * @since 2.0
	 */
	void setConfigElements(List<String> selectedModules);

	/**
	 * Returns the value of a configuration setting containing the list of selected environment configuration elements
	 * (modules/SoftEnv
	 * macros/etc.).
	 * <p>
	 * This may return an empty set but will never return <code>null</code>.
	 * 
	 * @return non-<code>null</code>
	 * @since 2.0
	 */
	List<String> getConfigElements();

	/**
	 * Sets the connection name configuration setting.
	 * <p>
	 * Note that <code>null</code> is indistinguishable from the empty string: in both cases, {@link #getConnectionName()} will
	 * return the empty string.
	 * 
	 * @param connectionName
	 *            (may be <code>null</code>)
	 */
	void setConnectionName(String connectionName);

	/**
	 * Returns the value of the connection name configuration setting.
	 * <p>
	 * This may return the empty string but will never return <code>null</code>.
	 * 
	 * @return non-<code>null</code>
	 */
	String getConnectionName();
}
