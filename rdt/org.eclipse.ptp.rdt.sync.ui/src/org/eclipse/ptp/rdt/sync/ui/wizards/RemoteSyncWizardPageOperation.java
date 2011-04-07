/*******************************************************************************
 * Copyright (c) 2008, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 * Roland Schulz, University of Tennessee
 *******************************************************************************/
package org.eclipse.ptp.rdt.sync.ui.wizards;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.cdt.internal.ui.wizards.ICDTCommonProjectWizard;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.ui.wizards.MBSCustomPageManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.ptp.rdt.core.BuildConfigurationManager;
import org.eclipse.ptp.rdt.core.BuildScenario;
import org.eclipse.ptp.rdt.core.services.IRDTServiceConstants;
import org.eclipse.ptp.rdt.sync.core.serviceproviders.ISyncServiceProvider;
import org.eclipse.ptp.rdt.sync.core.services.IRemoteSyncServiceConstants;
import org.eclipse.ptp.rdt.sync.ui.ISynchronizeParticipant;
import org.eclipse.ptp.rdt.sync.ui.RDTSyncUIPlugin;
import org.eclipse.ptp.rdt.ui.serviceproviders.RemoteBuildServiceProvider;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.services.core.IService;
import org.eclipse.ptp.services.core.IServiceConfiguration;
import org.eclipse.ptp.services.core.IServiceProviderDescriptor;
import org.eclipse.ptp.services.core.ServiceModelManager;

/**
 * An operation which handles configuring the remote portions of the Remote
 * C/C++ Project when the project is actually being created.
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as part
 * of a work in progress. There is no guarantee that this API will work or that
 * it will remain the same. Please do not use this API without consulting with
 * the RDT team.
 * 
 * 
 */
public class RemoteSyncWizardPageOperation implements IRunnableWithProgress {

	public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
		monitor.beginTask("configure model services", 100); //$NON-NLS-1$

		IWizard wizard = MBSCustomPageManager.getPageData(NewRemoteSyncProjectWizardPage.REMOTE_SYNC_WIZARD_PAGE_ID)
				.getWizardPage().getWizard();
		IProject project = ((ICDTCommonProjectWizard) wizard).getLastProject();

		ISynchronizeParticipant participant = (ISynchronizeParticipant) getMBSProperty(NewRemoteSyncProjectWizardPage.SERVICE_PROVIDER_PROPERTY);
		if (participant != null) {
			ServiceModelManager smm = ServiceModelManager.getInstance();
			IServiceConfiguration config = smm.newServiceConfiguration(getConfigName(project.getName()));
			IService syncService = smm.getService(IRemoteSyncServiceConstants.SERVICE_SYNC);
			config.setServiceProvider(syncService, participant.getProvider(project));
			
			IService buildService = smm.getService(IRDTServiceConstants.SERVICE_BUILD);
			IServiceProviderDescriptor descriptor = buildService.getProviderDescriptor(RemoteBuildServiceProvider.ID);
			RemoteBuildServiceProvider rbsp = (RemoteBuildServiceProvider) smm.getServiceProvider(descriptor);
			if (rbsp != null) {
				IRemoteConnection remoteConnection = participant.getProvider(project).getRemoteConnection();
				rbsp.setRemoteToolsProviderID(remoteConnection.getRemoteServices().getId());
				rbsp.setRemoteToolsConnection(remoteConnection);
				config.setServiceProvider(buildService, rbsp);
			}
			
			smm.addConfiguration(project, config);
			if (BuildConfigurationManager.getBuildSystemTemplateConfiguration() == null) {
				BuildConfigurationManager.setBuildSystemTemplateConfiguration(config);
			}

			try {
				smm.saveModelConfiguration();
			} catch (IOException e) {
				RDTSyncUIPlugin.log(e.toString(), e);
			}
		}
		
		// Register this initial build scenario with the service model manager
		ISyncServiceProvider provider = participant.getProvider(project);
		BuildScenario buildScenario = new BuildScenario(provider.getName(), provider.getRemoteConnection(),
																										provider.getLocation());

		// Add information about remote location to the initial build configurations (.cproject file)
		BuildConfigurationManager.setInitialBuildScenarioForAllConfigurations(project, buildScenario);
		
		// TODO: Either uncomment and use or delete
/*		
		// For each build configuration, set the build directory appropriately.
		
		// The only way to retrieve all configurations is by name, and there is no function for mapping names to configurations.
		// Thus, in the loop we set each configuration to the default and then use "getDefaultConfiguration" to retrieve it. Before
		// starting, we store the current default and restore it after the loop.
		IManagedBuildInfo buildInfo = ManagedBuildManager.getBuildInfo(project);
		if (buildInfo == null) {
			throw new RuntimeException("Build information for project not found. Project name: " + project.getName()); //$NON-NLS-1$
		}
		IConfiguration defaultConfig = buildInfo.getDefaultConfiguration();
		String[] allConfigNames = buildInfo.getConfigurationNames();
		for (String configName : allConfigNames) {
			buildInfo.setDefaultConfiguration(configName);
			IConfiguration config = buildInfo.getDefaultConfiguration();
			String buildPath = buildScenario.getLocation() + "/" + config.getName(); //$NON-NLS-1$
			config.getToolChain().getBuilder().setBuildPath(buildPath);
		}
		buildInfo.setDefaultConfiguration(defaultConfig);
		ManagedBuildManager.saveBuildInfo(project, true);
		monitor.done();
	*/
	}

	private static Object getMBSProperty(String propertyId) {
		return MBSCustomPageManager.getPageProperty(NewRemoteSyncProjectWizardPage.REMOTE_SYNC_WIZARD_PAGE_ID, propertyId);
	}

	/**
	 * Creates a name for the service configuration based on the remote
	 * connection name. If multiple names exist, appends a qualifier to the
	 * name.
	 * 
	 * @return new name guaranteed to be unique
	 */
	private String getConfigName(String candidateName) {
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
