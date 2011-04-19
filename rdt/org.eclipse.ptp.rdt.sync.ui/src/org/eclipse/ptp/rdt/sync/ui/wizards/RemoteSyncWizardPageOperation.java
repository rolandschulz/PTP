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

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.WriteAccessException;
import org.eclipse.cdt.core.settings.model.extension.CConfigurationData;
import org.eclipse.cdt.internal.ui.wizards.ICDTCommonProjectWizard;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.internal.core.Configuration;
import org.eclipse.cdt.managedbuilder.internal.core.ManagedProject;
import org.eclipse.cdt.managedbuilder.ui.wizards.MBSCustomPageManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
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
import org.eclipse.ptp.remote.core.IRemoteServices;
import org.eclipse.ptp.remote.core.PTPRemoteCorePlugin;
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
		if (participant == null) {
			monitor.done();
			return;
		}

		// BUild the service configuration
		ServiceModelManager smm = ServiceModelManager.getInstance();
		IServiceConfiguration serviceConfig = smm.newServiceConfiguration(getConfigName(project.getName()));
		IService syncService = smm.getService(IRemoteSyncServiceConstants.SERVICE_SYNC);
		serviceConfig.setServiceProvider(syncService, participant.getProvider(project));

		IService buildService = smm.getService(IRDTServiceConstants.SERVICE_BUILD);
		IServiceProviderDescriptor descriptor = buildService.getProviderDescriptor(RemoteBuildServiceProvider.ID);
		RemoteBuildServiceProvider rbsp = (RemoteBuildServiceProvider) smm.getServiceProvider(descriptor);
		if (rbsp != null) {
			IRemoteConnection remoteConnection = participant.getProvider(project).getRemoteConnection();
			rbsp.setRemoteToolsProviderID(remoteConnection.getRemoteServices().getId());
			rbsp.setRemoteToolsConnection(remoteConnection);
			serviceConfig.setServiceProvider(buildService, rbsp);
		}

		smm.addConfiguration(project, serviceConfig);
		try {
			smm.saveModelConfiguration();
		} catch (IOException e) {
			RDTSyncUIPlugin.log(e.toString(), e);
		}

		// Create build scenario based on initial remote location information
		ISyncServiceProvider provider = participant.getProvider(project);
		BuildScenario buildScenario = new BuildScenario(provider.getName(), provider.getRemoteConnection(),
				provider.getLocation());

		// For each build configuration, set the build directory appropriately.
		IManagedBuildInfo buildInfo = ManagedBuildManager.getBuildInfo(project);
		if (buildInfo == null) {
			throw new RuntimeException("Build information for project not found. Project name: " + project.getName()); //$NON-NLS-1$
		}
		IConfiguration[] allConfigs = buildInfo.getManagedProject().getConfigurations();
		String buildPath = buildScenario.getLocation();
		for (IConfiguration config : allConfigs) {
			config.getToolChain().getBuilder().setBuildPath(buildPath);
		}
		ManagedBuildManager.saveBuildInfo(project, true);

		// Add information about remote location to the initial build configurations.
		// Do this last (except for adding local configuration) so that project is not flagged as initialized prematurely.
		BuildConfigurationManager.initProject(project, serviceConfig, buildScenario);

		// Create a local build configuration. The corresponding build scenario has no sync provider and points to the project's
		// working directory. We take a conservative approach. Failure at any point (null return) means we abort the attempt to
		// create a local configuration.
		ManagedProject managedProject = (ManagedProject) buildInfo.getManagedProject();
		Configuration localConfigParent = (Configuration) buildInfo.getDefaultConfiguration();
		String localConfigId = ManagedBuildManager.calculateChildId(localConfigParent.getId(), null);
		Configuration localConfig = new Configuration(managedProject, localConfigParent, localConfigId, true, false);
		if (localConfig != null) {
			CConfigurationData localConfigData = localConfig.getConfigurationData();
			ICProjectDescription projectDes = CoreModel.getDefault().getProjectDescription(project);
			ICConfigurationDescription localConfigDes = null;
			try {
				localConfigDes = projectDes.createConfiguration(ManagedBuildManager.CFG_DATA_PROVIDER_ID, localConfigData);
			} catch (WriteAccessException e) {
				// Nothing to do
			} catch (CoreException e) {
				// Nothing to do
			}

			if (localConfigDes != null) {
				boolean configAdded = false;
//				localConfig.setConfigurationDescription(localConfigDes);
				localConfigDes.setName("Workspace"); //$NON-NLS-1$
//				localConfigDes.setDescription("Build inside Eclipse workspace"); //$NON-NLS-1$
				localConfig.getToolChain().getBuilder().setBuildPath(project.getLocation().toString());
				IRemoteServices localService = PTPRemoteCorePlugin.getDefault().
				getRemoteServices("org.eclipse.ptp.remote.LocalServices", null); //$NON-NLS-1$
				if (localService != null) {
					IRemoteConnection localConnection = localService.getConnectionManager().getConnection("local"); //$NON-NLS-1$
					if (localConnection != null) {
						BuildScenario localBuildScenario = new BuildScenario(null, localConnection, project.getLocation().toString());
						BuildConfigurationManager.setBuildScenarioForBuildConfiguration(localBuildScenario, localConfig);
						configAdded = true;
					}
				}
				if (!configAdded) {
					projectDes.removeConfiguration(localConfigDes);
				} else {
					try {
						// ManagedBuildManager.resetConfiguration(project, localConfig);
						CoreModel.getDefault().setProjectDescription(project, projectDes, true, null);
						// ManagedBuildManager.addExtensionConfiguration(localConfig);
						// ManagedBuildManager.saveBuildInfo(project, true);
					} catch (CoreException e) {
						projectDes.removeConfiguration(localConfigDes);
					}
				}
			}
		}
		try {
			BuildConfigurationManager.saveConfigurationData();
		} catch (IOException e) {
			// TODO: What to do in this case?
		}
		monitor.done();
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
