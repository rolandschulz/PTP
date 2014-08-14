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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ptp.internal.rdt.sync.cdt.ui.Activator;
import org.eclipse.ptp.internal.rdt.sync.cdt.ui.messages.Messages;
import org.eclipse.ptp.internal.rdt.sync.core.RDTSyncCorePlugin;
import org.eclipse.ptp.internal.rdt.sync.ui.wizards.SyncWizardDataCache;
import org.eclipse.ptp.rdt.sync.core.SyncConfig;
import org.eclipse.ptp.rdt.sync.core.SyncConfigManager;
import org.eclipse.ptp.rdt.sync.core.AbstractSyncFileFilter;
import org.eclipse.ptp.rdt.sync.core.SyncManager;
import org.eclipse.ptp.rdt.sync.core.SyncManager.SyncMode;
import org.eclipse.ptp.rdt.sync.core.resources.RemoteSyncNature;
import org.eclipse.ptp.rdt.sync.ui.ISynchronizeParticipant;

/**
 * Static class that houses the function ("run") for initializing a new synchronized project.
 */
public class NewRemoteSyncProjectWizardOperation {
	private static final String DEFAULT_BUILD_CONFIG_ID = "default-build-config-id"; //$NON-NLS-1$
	private static final String ConfigMapKey = "config-map"; //$NON-NLS-1$

	/**
	 * Does the actual initialization of a new synchronized project.
	 * Creates the service configuration, initializes the project with the BuildConfigurationManager, modifies and creates build
	 * configurations, and initializes file filtering.
	 * 
	 * @param project
	 * @param ISynchronizeParticipant
	 *            the participant created by the wizard page
	 * @param customFileFilter
	 *            File filter created by user on the wizard page or null if user made no changes
	 * @param localToolChains
	 *            Set of local tool chains selected by user
	 * @param remoteToolChains
	 *            Set of remote tool chains selected by user
	 * @param monitor
	 */
	public static void run(IProject project, ISynchronizeParticipant participant, AbstractSyncFileFilter customFileFilter,
			Set<String> localToolChains, Set<String> remoteToolChains, IProgressMonitor monitor) {
		// Change build configuration settings and find default configs
		IManagedBuildInfo buildInfo = ManagedBuildManager.getBuildInfo(project);
		if (buildInfo == null) {
			throw new RuntimeException("Build information for project not found. Project name: " + project.getName()); //$NON-NLS-1$
		}
		IConfiguration defaultLocalBuildConfig = null;
		IConfiguration defaultRemoteBuildConfig = null;
		IConfiguration[] allBuildConfigs = buildInfo.getManagedProject().getConfigurations();
		for (IConfiguration config : allBuildConfigs) {
			WizardUtil.modifyBuildConfigForSync(config);
			String toolChainName = config.getToolChain().getSuperClass().getName();
			if (remoteToolChains.contains(toolChainName)) {
				WizardUtil.modifyRemoteBuildConfigForSync(config);
				// Bug 434893: Favor debugging configs for default build configs
				if ((defaultRemoteBuildConfig == null) ||
						(!(defaultRemoteBuildConfig.getName().toLowerCase().contains("debug")))) { //$NON-NLS-1$
					defaultRemoteBuildConfig = config;
				}
			} else if (localToolChains.contains(toolChainName)){
				WizardUtil.modifyLocalBuildConfigForSync(config);
				// Bug 434893: Favor debugging configs for default build configs
				if ((defaultLocalBuildConfig == null) ||
						(!(defaultLocalBuildConfig.getName().toLowerCase().contains("debug")))) { //$NON-NLS-1$
					defaultLocalBuildConfig = config;
				}
			} else {
				assert false : Messages.NewRemoteSyncProjectWizardOperation_3;
				WizardUtil.modifyRemoteBuildConfigForSync(config);
				defaultRemoteBuildConfig = config;
			}

			// Bug 389899 - Synchronized project: "remote toolchain name" contains spaces
			config.setName(config.getName().replace(' ', '_'));
		}
		assert defaultRemoteBuildConfig != null : Messages.NewRemoteSyncProjectWizardOperation_0;
		// If user selects no local toolchain, use the remote default.
		if (defaultLocalBuildConfig == null) {
			defaultLocalBuildConfig = defaultRemoteBuildConfig;
		}

		// Add elements for a sync project
		if (!isSyncProject(project)) {
			try {
				SyncManager.makeSyncProject(project, participant.getSyncConfigName(), participant.getServiceId(),
						participant.getConnection(), participant.getLocation(), customFileFilter);
			} catch (CoreException e) {
				Activator.log(e);
				return;
			}
		}

		// Set active build config and the default build config for each sync config
		Map<String, String> configMap = SyncWizardDataCache.getMap(ConfigMapKey);
		if (configMap == null) {
			configMap = new HashMap<String, String>();
		}
		IConfiguration defaultBuildConfig;
		SyncConfig[] allSyncConfigs = SyncConfigManager.getConfigs(project);
		for (SyncConfig config : allSyncConfigs) {
			if (configMap.containsKey(config.getName())) {
				// Before project creation, wizard pages can only store the base build config id
				String buildConfigName = configMap.get(config.getName());
				buildConfigName = buildConfigName.replace(' ', '_');
				defaultBuildConfig = findBuildConfigByName(buildConfigName, allBuildConfigs);
				assert defaultBuildConfig != null : Messages.NewRemoteSyncProjectWizardOperation_2 + buildConfigName;
			} else if (SyncConfigManager.isLocal(config)) {
				defaultBuildConfig = defaultLocalBuildConfig;
			} else {
				defaultBuildConfig = defaultRemoteBuildConfig;
			}
			config.setProperty(DEFAULT_BUILD_CONFIG_ID, defaultBuildConfig.getId());
			if (SyncConfigManager.isActive(project, config)) {
				ManagedBuildManager.setDefaultConfiguration(project, defaultBuildConfig);
			}
		}

		// Save settings
		ManagedBuildManager.saveBuildInfo(project, true);
		try {
			SyncConfigManager.saveConfigs(project);
		} catch (CoreException e) {
			Activator.log(e);
		}

		if (customFileFilter != null) {
			try {
				SyncManager.saveFileFilter(project, customFileFilter);
			} catch (CoreException e) {
				RDTSyncCorePlugin.log(e);
			}
		}
		// monitor.done();

		// Enable sync'ing
		SyncManager.setSyncMode(project, SyncMode.ACTIVE);
	}

	/**
	 * Test if given project is a synchronized project
	 * 
	 * @param project
	 * @return whether a synchronized project
	 */
	private static boolean isSyncProject(IProject project) {
		return RemoteSyncNature.hasNature(project);
	}

	private static IConfiguration findBuildConfigByName(String name, IConfiguration[] configArray) {
		for (IConfiguration config : configArray) {
			if (config.getName().equals(name)) {
				return config;
			}
		}
		return null;
	}
}