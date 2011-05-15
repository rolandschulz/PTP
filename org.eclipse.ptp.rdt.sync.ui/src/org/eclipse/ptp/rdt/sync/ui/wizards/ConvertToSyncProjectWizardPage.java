/*******************************************************************************
 * Copyright (c) 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/

package org.eclipse.ptp.rdt.sync.ui.wizards;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.cdt.core.CCProjectNature;
import org.eclipse.cdt.core.CProjectNature;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.ui.wizards.conversion.ConvertProjectWizardPage;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.ptp.rdt.core.services.IRDTServiceConstants;
import org.eclipse.ptp.rdt.sync.core.BuildConfigurationManager;
import org.eclipse.ptp.rdt.sync.core.BuildScenario;
import org.eclipse.ptp.rdt.sync.core.resources.RemoteSyncNature;
import org.eclipse.ptp.rdt.sync.core.serviceproviders.ISyncServiceProvider;
import org.eclipse.ptp.rdt.sync.core.services.IRemoteSyncServiceConstants;
import org.eclipse.ptp.rdt.sync.ui.ISynchronizeParticipant;
import org.eclipse.ptp.rdt.sync.ui.ISynchronizeParticipantDescriptor;
import org.eclipse.ptp.rdt.sync.ui.RDTSyncUIPlugin;
import org.eclipse.ptp.rdt.sync.ui.SynchronizeParticipantRegistry;
import org.eclipse.ptp.rdt.sync.ui.messages.Messages;
import org.eclipse.ptp.rdt.ui.serviceproviders.RemoteBuildServiceProvider;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.services.core.IService;
import org.eclipse.ptp.services.core.IServiceConfiguration;
import org.eclipse.ptp.services.core.IServiceProviderDescriptor;
import org.eclipse.ptp.services.core.ServiceModelManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

/**
 * Converts existing CDT projects to sync projects.
 */
public class ConvertToSyncProjectWizardPage extends ConvertProjectWizardPage {

	private Combo fProviderCombo;
	private Composite fProviderArea;
	private StackLayout fProviderStack;
	private final List<Composite> fProviderControls = new ArrayList<Composite>();
	private ISynchronizeParticipantDescriptor fSelectedProvider;
	private final Map<Integer, ISynchronizeParticipantDescriptor> fComboIndexToDescriptorMap = new HashMap<Integer, ISynchronizeParticipantDescriptor>();

	/**
	 * @since 2.0
	 */
	protected Map<IProject, IServiceConfiguration> projectConfigs = new HashMap<IProject, IServiceConfiguration>();

	/**
	 * Constructor for ConvertToRemoteWizardPage.
	 * 
	 * @param pageName
	 */
	public ConvertToSyncProjectWizardPage(String pageName) {
		super(pageName);
	}

	/**
	 * Method getWzTitleResource returns the correct Title Label for this class
	 * overriding the default in the superclass.
	 */
	@Override
	protected String getWzTitleResource() {
		return "Convert to a synchronized project"; //$NON-NLS-1$
	}

	/**
	 * Method getWzDescriptionResource returns the correct description Label for
	 * this class overriding the default in the superclass.
	 */
	@Override
	protected String getWzDescriptionResource() {
		return "Converts a managed or unmanaged project to a synchronized project by adding a sync nature"; //$NON-NLS-1$
	}

	/**
	 * Returns true for: - non-hidden projects - non-RDT projects - projects
	 * that does not have remote systems temporary nature - projects that are
	 * located remotely
	 */
	@Override
	public boolean isCandidate(IProject project) {
		boolean a = false;
		boolean b = false;
		boolean c = false;
		a = !project.isHidden();
		try {
			b = project.hasNature(CProjectNature.C_NATURE_ID) || project.hasNature(CCProjectNature.CC_NATURE_ID);
			c = !project.hasNature(RemoteSyncNature.NATURE_ID);
		} catch (CoreException e) {
			RDTSyncUIPlugin.log(e);
		}

		return a && b && c;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.cdt.ui.wizards.conversion.ConvertProjectWizardPage#convertProject
	 * (org.eclipse.core.resources.IProject, java.lang.String,
	 * org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public void convertProject(IProject project, String bsId, IProgressMonitor monitor) throws CoreException {
		convertProject(project, monitor);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.cdt.ui.wizards.conversion.ConvertProjectWizardPage#convertProject
	 * (org.eclipse.core.resources.IProject,
	 * org.eclipse.core.runtime.IProgressMonitor, java.lang.String)
	 */
	@Override
	public void convertProject(IProject project, IProgressMonitor monitor, String projectID) throws CoreException {
		convertProject(project, monitor);
	}

	/**
	 * @since 2.0
	 */
	protected void convertProject(IProject project, IProgressMonitor monitor) throws CoreException {
		monitor.beginTask(Messages.ConvertToSyncProjectWizardPage_convertingToSyncProject, 3);
		try {
			ISynchronizeParticipant participant = fSelectedProvider.getParticipant();

			// Build the service configuration
			ServiceModelManager smm = ServiceModelManager.getInstance();
			IServiceConfiguration serviceConfig = smm.newServiceConfiguration(getConfigName(project.getName()));
			IService syncService = smm.getService(IRemoteSyncServiceConstants.SERVICE_SYNC);
			serviceConfig.setServiceProvider(syncService, participant.getProvider(project));

			IService buildService = smm.getService(IRDTServiceConstants.SERVICE_BUILD);
			IServiceProviderDescriptor descriptor = buildService.getProviderDescriptor(RemoteBuildServiceProvider.ID);
			RemoteBuildServiceProvider rbsp = (RemoteBuildServiceProvider) smm.getServiceProvider(descriptor);
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

			// Create build scenario based on initial remote location
			// information
			ISyncServiceProvider provider = participant.getProvider(project);
			BuildScenario buildScenario = new BuildScenario(provider.getName(), provider.getRemoteConnection(),
					provider.getLocation());

			// For each build configuration, set the build directory
			// appropriately.
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

			// Add information about remote location to the initial build
			// configurations.
			// Do this last (except for adding local configuration) so that
			// project is not flagged as initialized prematurely.
			BuildConfigurationManager.initProject(project, serviceConfig, buildScenario);
			try {
				BuildConfigurationManager.saveConfigurationData();
			} catch (IOException e) {
				// TODO: What to do in this case?
			}
			monitor.done();
		} finally {
			monitor.done();
		}
	}

	@Override
	protected void addToMainPage(Composite container) {
		Composite comp = new Composite(container, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		comp.setLayout(layout);
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		comp.setLayoutData(gd);

		// Label for "Provider:"
		Label providerLabel = new Label(comp, SWT.LEFT);
		providerLabel.setText(Messages.NewRemoteSyncProjectWizardPage_syncProvider);

		// combo for providers
		fProviderCombo = new Combo(comp, SWT.DROP_DOWN | SWT.READ_ONLY);
		// set layout to grab horizontal space
		fProviderCombo.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false));
		gd = new GridData();
		gd.horizontalSpan = 2;
		fProviderCombo.setLayoutData(gd);
		fProviderCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				handleProviderSelected();
			}
		});

		fProviderArea = new Group(comp, SWT.SHADOW_ETCHED_IN);
		fProviderStack = new StackLayout();
		fProviderArea.setLayout(fProviderStack);
		GridData providerAreaData = new GridData(SWT.FILL, SWT.FILL, true, true);
		providerAreaData.horizontalSpan = 3;
		fProviderArea.setLayoutData(providerAreaData);

		// populate the combo with a list of providers
		ISynchronizeParticipantDescriptor[] providers = SynchronizeParticipantRegistry.getDescriptors();

		fProviderCombo.add(Messages.NewRemoteSyncProjectWizardPage_selectSyncProvider, 0);
		for (int k = 0; k < providers.length; k++) {
			fProviderCombo.add(providers[k].getName(), k + 1);
			fComboIndexToDescriptorMap.put(k, providers[k]);
			addProviderControl(providers[k]);
		}

		if (providers.length == 1) {
			fProviderCombo.select(1);
			handleProviderSelected();
		} else {
			fProviderCombo.select(0);
			fSelectedProvider = null;
		}

	}

	@Override
	public void doRun(IProgressMonitor monitor, String projectID, String bsId) throws CoreException {
		monitor.beginTask(Messages.ConvertToRemoteWizardPage_0, 2);
		super.doRun(new SubProgressMonitor(monitor, 1), projectID, bsId);
		try {
			ServiceModelManager.getInstance().saveModelConfiguration();
		} catch (IOException e) {
			RDTSyncUIPlugin.log(e);
		} finally {
			monitor.done();
		}
	}

	/**
	 * @since 2.0
	 */
	protected IServiceConfiguration getConfig(IProject project) {
		IServiceConfiguration config = projectConfigs.get(project);
		if (config == null) {
			config = ServiceModelManager.getInstance().newServiceConfiguration(project.getName());
			projectConfigs.put(project, config);
		}
		return config;
	}

	/**
	 * Handle synchronize provider selected.
	 */
	private void handleProviderSelected() {
		int index = fProviderCombo.getSelectionIndex() - 1;
		if (index >= 0) {
			fProviderStack.topControl = fProviderControls.get(index);
			fSelectedProvider = fComboIndexToDescriptorMap.get(index);
		} else {
			fProviderStack.topControl = null;
			fSelectedProvider = null;
		}
		fProviderArea.layout();
	}

	private void addProviderControl(ISynchronizeParticipantDescriptor desc) {
		Composite comp = null;
		ISynchronizeParticipant part = desc.getParticipant();
		if (part != null) {
			comp = new Composite(fProviderArea, SWT.NONE);
			comp.setLayout(new GridLayout(1, false));
			comp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			part.createConfigurationArea(comp, getWizard().getContainer());
		}
		fProviderControls.add(comp);
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