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

import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.internal.core.envvar.EnvironmentVariableManager;
import org.eclipse.cdt.managedbuilder.core.IBuilder;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ptp.internal.rdt.sync.cdt.ui.Activator;
import org.eclipse.ptp.internal.rdt.sync.cdt.ui.messages.Messages;
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
	private static final String DEFAULT_BUILD_CONFIG_ID = "default-build-config-id"; //$NON-NLS-1$
	private static final String SYNC_BUILDER_CLASS = "org.eclipse.ptp.rdt.sync.cdt.core.SyncBuilder"; //$NON-NLS-1$

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

		// Change build configuration settings and find the initial default configurations
		IConfiguration defaultLocal = null;
		IConfiguration defaultRemote = null;
		IManagedBuildInfo buildInfo = ManagedBuildManager.getBuildInfo(project);
		if (buildInfo == null) {
			throw new RuntimeException("Build information for project not found. Project name: " + project.getName()); //$NON-NLS-1$
		}
		IBuilder syncBuilder = ManagedBuildManager.getExtensionBuilder(SYNC_BUILDER_CLASS);
		IConfiguration[] allBuildConfigs = buildInfo.getManagedProject().getConfigurations();
		for (IConfiguration config : allBuildConfigs) {
			boolean isRemote = mainPage.isRemoteConfig(config);
			boolean isLocal = mainPage.isLocalConfig(config);

			// Set all configs to use the sync builder, which ensures the build always occurs at the active sync config location.
			config.changeBuilder(syncBuilder, SYNC_BUILDER_CLASS, Messages.NewRemoteSyncProjectWizardOperation_1);
			// turn off append contributed (local) environment variables for remote configs
			if (isRemote) {
				ICConfigurationDescription c_mb_confgDes = ManagedBuildManager.getDescriptionForConfiguration(config);
				if (c_mb_confgDes != null) {
					EnvironmentVariableManager.fUserSupplier.setAppendContributedEnvironment(false, c_mb_confgDes);
				}
			}
			
			// Set default build configurations
			if (isRemote && defaultRemote == null) {
				defaultRemote = config;
			}
			
			if (isLocal && defaultLocal == null) {
				defaultLocal = config;
			}

            // Bug 389899 - Synchronized project: "remote toolchain name" contains spaces
            config.setName(config.getName().replace(' ', '_'));
		}
		
		assert defaultRemote != null : Messages.NewRemoteSyncProjectWizardOperation_0;
		// If user selects no local toolchain, use the remote default.
		if (defaultLocal == null) {
			defaultLocal = defaultRemote;
		}


		// Add elements for a sync project
		try {
			SyncManager.makeSyncProject(project, participant.getProvider(project), mainPage.getCustomFileFilter());
		} catch (CoreException e) {
			Activator.log(e);
			return;
		}

		// Set active build config and the default build config for each sync config
		IConfiguration defaultConfig;
		SyncConfig[] allSyncConfigs = SyncConfigManager.getConfigs(project);
		for (SyncConfig config : allSyncConfigs) {
			if (SyncConfigManager.isLocal(config)) {
				defaultConfig = defaultLocal;
			} else {
				defaultConfig = defaultRemote;
			}
			config.setProperty(DEFAULT_BUILD_CONFIG_ID, defaultConfig.getId());
			if (SyncConfigManager.isActive(project, config)) {
				ManagedBuildManager.setDefaultConfiguration(project, defaultConfig);
			}
		}

		// Save settings
		ManagedBuildManager.saveBuildInfo(project, true);
		try {
			SyncConfigManager.saveConfigs(project);
		} catch (CoreException e) {
			Activator.log(e);
		}

		SyncFileFilter customFilter = mainPage.getCustomFileFilter();
		if (customFilter != null) {
			SyncManager.saveFileFilter(project, customFilter);
		}
		// monitor.done();

		// Enable sync'ing
		SyncManager.setSyncMode(project, SyncMode.ACTIVE);
	}
}