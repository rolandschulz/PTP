/*******************************************************************************
 * Copyright (c) 2008, 2010, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 * Roland Schulz, University of Tennessee
 * John Eblen, Oak Ridge National Laboratory
 *******************************************************************************/
package org.eclipse.ptp.internal.rdt.sync.cdt.ui.wizards;

import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.internal.core.Configuration;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ptp.internal.rdt.sync.cdt.core.BuildConfigUtils;
import org.eclipse.ptp.internal.rdt.sync.cdt.ui.Activator;
import org.eclipse.ptp.rdt.sync.core.SyncConfig;
import org.eclipse.ptp.rdt.sync.core.SyncConfigManager;
import org.eclipse.ptp.rdt.sync.core.SyncFileFilter;
import org.eclipse.ptp.rdt.sync.core.SyncManager;
import org.eclipse.ptp.rdt.sync.core.SyncManager.SyncMode;
import org.eclipse.ptp.rdt.sync.ui.ISynchronizeParticipant;

/**
 * Static class that houses the function ("run") for initializing a new synchronized project.
 */
public class NewRemoteSyncProjectWizardOperation {
	/**
	 * Does the actual initialization of a new synchronized project.
	 * Creates the service configuration, initializes the project with the BuildConfigurationManager, modifies and creates build
	 * configurations, and initializes file filtering.
	 * 
	 * @param project
	 * @param mainPage
	 *            - the main wizard page, which contains the user's entries
	 * @param monitor
	 */
	public static void run(IProject project, SyncMainWizardPage mainPage, IProgressMonitor monitor) {

		ISynchronizeParticipant participant = mainPage.getSynchronizeParticipant();
		if (participant == null) {
			return;
		}
		try {
			SyncManager.makeSyncProject(project, participant.getProvider(project), mainPage.getCustomFileFilter());
		} catch (CoreException e) {
			Activator.log(e);
			return;
		}

		SyncConfig activeConfig = SyncConfigManager.getActive(project);

		// Initialize project with the active config, which will be applied to all current configurations.
		// Note then that we initially assume all configs are remote.
		BuildConfigUtils.setSyncConfigForAllBuildConfigurations(project, activeConfig);

		// Create a local config
		SyncConfig localSyncConfig = null;
		try {
			localSyncConfig = SyncConfigManager.createLocal(project);
		} catch (CoreException e) {
			// TODO: What to do here?
		}

		// Iterate through all configurations, modifying them as indicated based on their type.
		IManagedBuildInfo buildInfo = ManagedBuildManager.getBuildInfo(project);
		if (buildInfo == null) {
			throw new RuntimeException("Build information for project not found. Project name: " + project.getName()); //$NON-NLS-1$
		}
		boolean defaultConfigSet = false;
		IConfiguration[] allConfigs = buildInfo.getManagedProject().getConfigurations();
		for (IConfiguration config : allConfigs) {
			boolean isRemote = mainPage.isRemoteConfig(config);
			boolean isLocal = mainPage.isLocalConfig(config);

			// If config is both local and remote, then we need to create a config. Let the existing config be the remote and
			// create a new local config based on the remote config.
			if (isLocal && isRemote) {
				IConfiguration localConfig = BuildConfigUtils.createConfiguration(project, (Configuration) config, localSyncConfig,
						config.getName(), null);
				BuildConfigUtils.modifyConfigurationAsSyncLocal(localConfig);
			}

			// If local only, change its sync config to the local config
			if (isLocal && !isRemote) {
				BuildConfigUtils.setSyncConfigForBuildConfiguration(localSyncConfig, config);
				BuildConfigUtils.modifyConfigurationAsSyncLocal(config);
			}

			// If type is remote, change to the sync builder and set environment variable support.
			if (isRemote) {
				BuildConfigUtils.modifyConfigurationAsSyncRemote(config);

				// The first remote found will be the initial default (active) configuration.
				if (!defaultConfigSet) {
					ManagedBuildManager.setDefaultConfiguration(project, config);
					defaultConfigSet = true;
				}
			}

			// Bug 389899 - Synchronized project: "remote toolchain name" contains spaces
			config.setName(config.getName().replace(' ', '_'));
		}
		ManagedBuildManager.saveBuildInfo(project, true);

		SyncFileFilter customFilter = mainPage.getCustomFileFilter();
		if (customFilter != null) {
			SyncManager.saveFileFilter(project, customFilter);
		}
		// monitor.done();

		// Enable sync'ing
		SyncManager.setSyncMode(project, SyncMode.ACTIVE);
	}
}
