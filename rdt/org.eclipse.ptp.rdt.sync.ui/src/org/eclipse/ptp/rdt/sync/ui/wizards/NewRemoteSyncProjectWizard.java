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

import org.eclipse.cdt.core.CCProjectNature;
import org.eclipse.cdt.core.CProjectNature;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.internal.core.envvar.EnvironmentVariableManager;
import org.eclipse.cdt.internal.ui.wizards.ICDTCommonProjectWizard;
import org.eclipse.cdt.managedbuilder.core.IBuilder;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.ui.wizards.MBSCustomPageManager;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.wizards.CDTCommonProjectWizard;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.ptp.internal.rdt.sync.ui.SyncPluginImages;
import org.eclipse.ptp.rdt.core.services.IRDTServiceConstants;
import org.eclipse.ptp.rdt.sync.core.BuildConfigurationManager;
import org.eclipse.ptp.rdt.sync.core.BuildScenario;
import org.eclipse.ptp.rdt.sync.core.resources.RemoteSyncNature;
import org.eclipse.ptp.rdt.sync.core.serviceproviders.ISyncServiceProvider;
import org.eclipse.ptp.rdt.sync.core.serviceproviders.SyncBuildServiceProvider;
import org.eclipse.ptp.rdt.sync.core.services.IRemoteSyncServiceConstants;
import org.eclipse.ptp.rdt.sync.ui.ISynchronizeParticipant;
import org.eclipse.ptp.rdt.sync.ui.RDTSyncUIPlugin;
import org.eclipse.ptp.rdt.sync.ui.messages.Messages;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.services.core.IService;
import org.eclipse.ptp.services.core.IServiceConfiguration;
import org.eclipse.ptp.services.core.IServiceProviderDescriptor;
import org.eclipse.ptp.services.core.ServiceModelManager;
import org.eclipse.ui.statushandlers.StatusManager;

/**
 * A wizard for creating new Synchronized Projects
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as part
 * of a work in progress. There is no guarantee that this API will work or that
 * it will remain the same. Please do not use this API without consulting with
 * the RDT team.
 * 
 * 
 */
public class NewRemoteSyncProjectWizard extends CDTCommonProjectWizard {
	private static final String PREFIX = "CProjectWizard"; //$NON-NLS-1$
	private static final String wz_title = Messages.NewRemoteSyncProjectWizard_title;
	private static final String wz_desc = Messages.NewRemoteSyncProjectWizard_description;

	/**
	 * 
	 */
	public NewRemoteSyncProjectWizard() {
		super(wz_title, wz_desc);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.ui.wizards.CDTCommonProjectWizard#addPages()
	 */
	@Override
	public void addPages() {
		fMainPage = new SyncMainWizardPage(CUIPlugin.getResourceString(PREFIX));
		fMainPage.setTitle(wz_title);
		fMainPage.setDescription(wz_desc);
		addPage(fMainPage);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.cdt.ui.wizards.CDTCommonProjectWizard#continueCreation(org
	 * .eclipse.core.resources.IProject)
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.ui.wizards.CDTCommonProjectWizard#getNatures()
	 */
	@Override
	public String[] getNatures() {
		return new String[] { CProjectNature.C_NATURE_ID, CCProjectNature.CC_NATURE_ID, RemoteSyncNature.NATURE_ID };
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.ui.wizards.CDTCommonProjectWizard#performFinish()
	 */
	@Override
	public boolean performFinish() {
		boolean success = super.performFinish();
		if (success) {
			IProject project = this.getProject(true);
			// Uncomment try/catch statements if run is ever changed to spawn a thread.
//			try {
				this.run(project, null);
//			} catch (InvocationTargetException e) {
//				success = false;
//			} catch (InterruptedException e) {
//				success = false;
//			}
		}

		return success;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.cdt.ui.wizards.CDTCommonProjectWizard#continueCreation(org
	 * .eclipse.core.resources.IProject)
	 */
	@Override
	protected IProject continueCreation(IProject prj) {
		try {
			CProjectNature.addCNature(prj, new NullProgressMonitor());
			CCProjectNature.addCCNature(prj, new NullProgressMonitor());
		} catch (CoreException e) {
			// TODO: What to do here?
		}
		return prj;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.wizards.newresource.BasicNewResourceWizard#
	 * initializeDefaultPageImageDescriptor()
	 */
	@Override
	protected void initializeDefaultPageImageDescriptor() {
		setDefaultPageImageDescriptor(SyncPluginImages.DESC_WIZBAN_NEW_REMOTE_C_PROJ);
	}
	
	public void run(IProject project, IProgressMonitor monitor) {
		// monitor.beginTask("configure model services", 100); //$NON-NLS-1$

		// Add sync nature here so that "Synchronize" properties page does not appear inside wizard, before sync data has been added.
		try {
			RemoteSyncNature.addNature(project, new NullProgressMonitor());
		} catch (CoreException e1) {
			// TODO: What to do here?
		}
		ISynchronizeParticipant participant = ((SyncMainWizardPage) fMainPage).getSynchronizeParticipant();
		if (participant == null) {
			// monitor.done();
			return;
		}

		// Build the service configuration
		ServiceModelManager smm = ServiceModelManager.getInstance();
		IServiceConfiguration serviceConfig = smm.newServiceConfiguration(getConfigName(project.getName()));
		IService syncService = smm.getService(IRemoteSyncServiceConstants.SERVICE_SYNC);
		serviceConfig.setServiceProvider(syncService, participant.getProvider(project));

		IService buildService = smm.getService(IRDTServiceConstants.SERVICE_BUILD);
		IServiceProviderDescriptor descriptor = buildService.getProviderDescriptor(SyncBuildServiceProvider.ID);
		SyncBuildServiceProvider rbsp = (SyncBuildServiceProvider) smm.getServiceProvider(descriptor);
		if (rbsp != null) {
			IRemoteConnection remoteConnection = participant.getProvider(project).getRemoteConnection();
			rbsp.setRemoteToolsConnection(remoteConnection);
			serviceConfig.setServiceProvider(buildService, rbsp);
		}

		smm.addConfiguration(project, serviceConfig);
		try {
			smm.saveModelConfiguration();
		} catch (IOException e) {
			RDTSyncUIPlugin.log(e.toString(), e);
		}

		BuildConfigurationManager bcm = BuildConfigurationManager.getInstance();	
		// Create build scenario based on initial remote location information
		ISyncServiceProvider provider = participant.getProvider(project);
		BuildScenario remoteBuildScenario = new BuildScenario(provider.getName(), provider.getRemoteConnection(),
				provider.getLocation());
		
		// Initialize project with this build scenario, which will be applied to all current configurations.
		bcm.initProject(project, serviceConfig, remoteBuildScenario);
		
		// For each original configuration do the following:
		// 1) Create a corresponding local configuration
		// 2) Change its toolchain to the remote toolchain
		// 2) Set builder to the sync builder
		// 3) Append environment variables
		IManagedBuildInfo buildInfo = ManagedBuildManager.getBuildInfo(project);
		if (buildInfo == null) {
			throw new RuntimeException("Build information for project not found. Project name: " + project.getName()); //$NON-NLS-1$
		}
		IConfiguration[] allRemoteConfigs = buildInfo.getManagedProject().getConfigurations();
		IToolChain remoteToolChain = ((SyncMainWizardPage) fMainPage).getRemoteToolChain();
		for (IConfiguration remoteConfig : allRemoteConfigs) {
			BuildConfigurationManager.getInstance().createLocalConfiguration(project, remoteConfig.getName() + "-local"); //$NON-NLS-1$
			if (remoteToolChain != null) {
				remoteConfig.createToolChain(remoteToolChain, ManagedBuildManager.calculateChildId(remoteToolChain.getId(), null),
						remoteToolChain.getId(), false);
			}
			IBuilder syncBuilder = ManagedBuildManager.getExtensionBuilder("org.eclipse.ptp.rdt.sync.core.SyncBuilder"); //$NON-NLS-1$
			remoteConfig.changeBuilder(syncBuilder, "org.eclipse.ptp.rdt.sync.core.SyncBuilder", "Sync Builder"); //$NON-NLS-1$ //$NON-NLS-2$
			// turn off append contributed(local) environment variables for the build configuration of the remote project
			ICConfigurationDescription c_mb_confgDes = ManagedBuildManager.getDescriptionForConfiguration(remoteConfig);
			if (c_mb_confgDes != null) {
				EnvironmentVariableManager.fUserSupplier.setAppendContributedEnvironment(false, c_mb_confgDes);
				// EnvironmentVariableManager.fUserSupplier.setAppendEnvironment(false, c_mb_confgDes);
			}
		}
		ManagedBuildManager.saveBuildInfo(project, true);

		// monitor.done();
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
