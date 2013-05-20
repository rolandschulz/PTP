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
package org.eclipse.ptp.internal.rdt.sync.cdt.core;

import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.ptp.rdt.sync.core.SyncConfig;
import org.eclipse.ptp.rdt.sync.core.listeners.ISyncConfigListener;

/**
 * Singleton class responsible for adjusting CDT build configurations in response to changes to the sync configurations.
 */
public class SyncConfigListenerCDT implements ISyncConfigListener {
	public static final String DEFAULT_BUILD_CONFIG_ID = "default-build-config-id"; //$NON-NLS-1$

	// Setup as a singleton
	private SyncConfigListenerCDT() {
	}

	private static SyncConfigListenerCDT fInstance = null;

	/**
	 * Get the single instance of a CDT sync config listener
	 * 
	 * @return the instance
	 */
	public static synchronized SyncConfigListenerCDT getInstance() {
		if (fInstance == null) {
			fInstance = new SyncConfigListenerCDT();
		}
		return fInstance;
	}

	@Override
	public void configAdded(IProject project, SyncConfig config) {
		// nothing to do
	}

	@Override
	public void configRemoved(IProject project, SyncConfig config) {
		// nothing to do
	}

	/**
	 * Switch to the new sync config's default build config, if it has one and if it still resides in CDT.
	 */
	@Override
	public void configSelected(IProject project, SyncConfig newConfig, SyncConfig oldConfig) {
		String newDefaultConfigId = newConfig.getProperty(DEFAULT_BUILD_CONFIG_ID);
		if (newDefaultConfigId == null) {
			return;
		}
		IManagedBuildInfo buildInfo = ManagedBuildManager.getBuildInfo(project);
		IConfiguration newDefaultConfig = buildInfo.getManagedProject().getConfiguration(newDefaultConfigId);
		if (newDefaultConfig != null) {
			ManagedBuildManager.setDefaultConfiguration(project, newDefaultConfig);
			ManagedBuildManager.saveBuildInfo(project, true);
		}
	}
}