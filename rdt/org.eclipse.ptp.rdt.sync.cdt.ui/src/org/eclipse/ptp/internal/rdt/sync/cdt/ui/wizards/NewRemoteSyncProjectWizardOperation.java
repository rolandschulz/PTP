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

import java.util.HashSet;
import java.util.Set;

import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.internal.core.Configuration;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.ptp.internal.rdt.sync.cdt.core.BuildConfigUtils;
import org.eclipse.ptp.rdt.sync.core.SyncConfig;
import org.eclipse.ptp.rdt.sync.core.SyncConfigManager;
import org.eclipse.ptp.rdt.sync.core.SyncFileFilter;
import org.eclipse.ptp.rdt.sync.core.SyncManager;
import org.eclipse.ptp.rdt.sync.core.SyncManager.SyncMode;
import org.eclipse.ptp.rdt.sync.core.resources.RemoteSyncNature;
import org.eclipse.ptp.rdt.sync.core.services.ISynchronizeService;
import org.eclipse.ptp.rdt.sync.ui.ISynchronizeParticipant;
import org.eclipse.ptp.services.core.IServiceConfiguration;
import org.eclipse.ptp.services.core.ServiceModelManager;

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
		// monitor.beginTask("configure model services", 100); //$NON-NLS-1$

		// Add sync nature here so that "Synchronize" properties page does not appear inside wizard, before sync data has been
		// added.
		try {
			RemoteSyncNature.addNature(project, new NullProgressMonitor());
		} catch (CoreException e1) {
			// TODO: What to do here?
		}
		ISynchronizeParticipant participant = mainPage.getSynchronizeParticipant();
		if (participant == null) {
			// monitor.done();
			return;
		}

		// Create build scenario based on initial remote location information
		ISynchronizeService provider = participant.getProvider(project);
		SyncConfig remoteBuildScenario = new SyncConfig(null, provider.getId(), provider.getRemoteConnection(),
				provider.getLocation());

		// Initialize project with this build scenario, which will be applied to all current configurations.
		// Note then that we initially assume all configs are remote.
		BuildConfigUtils.setBuildScenarioForAllBuildConfigurations(project, remoteBuildScenario);

		// Create a local build scenario
		SyncConfig localBuildScenario = null;
		try {
			localBuildScenario = SyncConfigManager.createLocal(project);
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
				IConfiguration localConfig = BuildConfigUtils.createConfiguration(project, (Configuration) config, localBuildScenario,
						config.getName(), null);
				BuildConfigUtils.modifyConfigurationAsSyncLocal(localConfig);
			}

			// If local only, change its build scenario to the local build scenario.
			if (isLocal && !isRemote) {
				BuildConfigUtils.setBuildScenarioForBuildConfiguration(localBuildScenario, config);
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

	/**
	 * Creates a name for the service configuration based on the remote
	 * connection name. If multiple names exist, appends a qualifier to the
	 * name.
	 * 
	 * @return new name guaranteed to be unique
	 */
	private static String getConfigName(String candidateName) {
		Set<IServiceConfiguration> configs = ServiceModelManager.getInstance().getConfigurations();
		Set<String> existingNames = new HashSet<String>();
		for (IServiceConfiguration config : configs) {
			existingNames.add(config.getName());
		}

		int i = 2;
		String newConfigName = candidateName;
		while (existingNames.contains(newConfigName)) {
			newConfigName = candidateName + " (" + (i++) + ")"; //$NON-NLS-1$ //$NON-NLS-2$
		}

		return newConfigName;
	}
}
