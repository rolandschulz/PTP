/*******************************************************************************
 * Copyright (c) 2011 Oak Ridge National Laboratory and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    John Eblen - initial implementation
 *******************************************************************************/
package org.eclipse.ptp.rdt.sync.ui.properties;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICResourceDescription;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IMultiConfiguration;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.internal.core.MultiConfiguration;
import org.eclipse.cdt.managedbuilder.ui.properties.AbstractSingleBuildPage;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.IWizardContainer;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.ptp.rdt.sync.core.BuildConfigurationManager;
import org.eclipse.ptp.rdt.sync.core.BuildScenario;
import org.eclipse.ptp.rdt.sync.core.MissingConnectionException;
import org.eclipse.ptp.rdt.sync.core.SyncManager;
import org.eclipse.ptp.rdt.sync.ui.ISynchronizeParticipant;
import org.eclipse.ptp.rdt.sync.ui.ISynchronizeParticipantDescriptor;
import org.eclipse.ptp.rdt.sync.ui.RDTSyncUIPlugin;
import org.eclipse.ptp.rdt.sync.ui.SynchronizeParticipantRegistry;
import org.eclipse.ptp.rdt.sync.ui.messages.Messages;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.IRemoteServices;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.statushandlers.StatusManager;

public class BuildRemotePropertiesPage extends AbstractSingleBuildPage {
	private ISynchronizeParticipant fSelectedParticipant = null;
	private ArrayList<ISynchronizeParticipant> fProviders = new ArrayList<ISynchronizeParticipant>();
	private ArrayList<Composite> fProviderControls = new ArrayList<Composite>();
	private IConfiguration fConfigBeforeSwitch = null;
	// Local configs have all settings set to null.
	private final PageSettings fLocalSettings = new PageSettings();
	private boolean fWidgetsReady = false;

	// Container for all information that appears on a page
	private static class PageSettings {
		String syncProvider;
		String syncProviderPath;
		IRemoteConnection connection;
		IRemoteServices remoteProvider;
		String rootLocation;
		
		public boolean equals(PageSettings otherSettings) {
			if (otherSettings == null) {
				return false;
			}
			if (this.syncProvider != otherSettings.syncProvider) {
				return false;
			}
			if (this.syncProviderPath != otherSettings.syncProviderPath) {
				return false;
			}
			if (this.connection != otherSettings.connection) {
				return false;
			}
			if (this.remoteProvider != otherSettings.remoteProvider) {
				return false;
			}
			if (!(this.rootLocation.equals(otherSettings.rootLocation))) {
				return false;
			}
			
			return true;
		}
	}
	
	// Cache of page settings for each configuration accessed.
	private final Map<String, PageSettings> fConfigToPageSettings = new HashMap<String, PageSettings>();

	// SWT Elements
	private Button fSyncToggleButton;
	private Composite fProviderArea;
	private StackLayout fProviderStack;
	private Composite fComposite;
	private Composite fLocalProviderComposite;

	/**
	 * Constructor for BuildRemotePropertiesPage
	 */
	public BuildRemotePropertiesPage() {
		super();
	}

	/**
	 * Create widgets on given composite
	 *
	 * @param parent
	 * 				The parent composite
	 */
	public void createWidgets(Composite parent) {
		// For now, a blank composite to use when no provider is selected.
		fLocalProviderComposite = new Composite(parent, SWT.NULL);
		fComposite = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		fComposite.setLayout(layout);
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		fComposite.setLayoutData(gd);

		// Sync toggle
		fSyncToggleButton = new Button(fComposite, SWT.CHECK);
		fSyncToggleButton.setText(Messages.BuildRemotePropertiesPage_0);
		fSyncToggleButton.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false, 3, 1));
		fSyncToggleButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// TODO: Assumes a single sync provider
				if (fSyncToggleButton.getSelection()) {
					setParticipant(0);
				} else {
					setNoParticipant();
				}
				update();
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO: Assumes a single sync provider
				if (fSyncToggleButton.getSelection()) {
					setParticipant(0);
				} else {
					setNoParticipant();
				}
				update();
			}
		});

		// Sync provider area
		fProviderArea = new Group(fComposite, SWT.SHADOW_ETCHED_IN);
		fProviderStack = new StackLayout();
		fProviderArea.setLayout(fProviderStack);
        GridData providerAreaData = new GridData(SWT.FILL, SWT.FILL, true, true);
        providerAreaData.horizontalSpan = 3;
        fProviderArea.setLayoutData(providerAreaData);

        // Store providers and their controls
		ISynchronizeParticipantDescriptor[] providers = SynchronizeParticipantRegistry.getDescriptors();
		for (ISynchronizeParticipantDescriptor desc : providers) {
			ISynchronizeParticipant participant = desc.getParticipant();
			participant.setIsPropertyPage(true);
			fProviders.add(participant);
			fProviderControls.add(this.createControl(participant));
		}

		// Switch to the current configuration
		fConfigBeforeSwitch = getCfg();
		this.setValues(getCfg());
		fWidgetsReady = true;
	}

	/**
	 * Save each visited configuration
	 */
	@Override
	public boolean performOk() {
		// Disable sync auto while changing config files but make sure the previous setting is restored before exiting.
		boolean syncAutoSetting = SyncManager.getSyncAuto();
		SyncManager.setSyncAuto(false);
		try {
			super.performOk();
			if (fWidgetsReady == false) {
				return true;
			}
			// Don't forget to save changes made to the current configuration before proceeding
			this.storeSettings(fConfigBeforeSwitch);
			IProject project = getProject();
			for (ICConfigurationDescription desc : getCfgsReadOnly(project)) {
				IConfiguration config = getCfg(desc);
				if (config == null || config instanceof MultiConfiguration) {
					continue;
				}

				// Save settings for changed configs. Call modify functions only when necessary.
				BuildConfigurationManager bcm = BuildConfigurationManager.getInstance();
				PageSettings settings = fConfigToPageSettings.get(config.getId());
				if (settings != null) {
					PageSettings systemSettings = this.loadSettings(config);
					if (!settings.equals(systemSettings)) {
						this.saveConfig(config,  settings);
						if ((systemSettings == null) || (settings.syncProvider != systemSettings.syncProvider)) {
							if (settings.syncProvider == null) {
								bcm.modifyConfigurationAsSyncLocal(config);
								try {
									BuildScenario localBuildScenario = bcm.createLocalBuildScenario(project);
									bcm.setBuildScenarioForBuildConfiguration(localBuildScenario, config);
								} catch (CoreException e) {
									RDTSyncUIPlugin.log(Messages.BuildRemotePropertiesPage_2, e);
								}
							} else {
								bcm.modifyConfigurationAsSyncRemote(config);
							}
						}
					}
				}
			}
		} finally {
			SyncManager.setSyncAuto(syncAutoSetting);
		}
		return true;
	}

	/**
	 * Save new settings for the configuration to the BuildConfigurationManager
	 *
	 * @param config
	 * 				configuration
	 * @param settings
	 * 				new settings
	 */
	private void saveConfig(IConfiguration config, PageSettings settings) {
		// Set build path in build configuration to appropriate directory
		IProject project = config.getOwner().getProject();
		ManagedBuildManager.saveBuildInfo(project, true);

        // Register with build configuration manager. This must be done after saving build info with ManagedBuildManager, as
        // the BuildConfigurationManager relies on the data being up-to-date.
        BuildConfigurationManager bcm = BuildConfigurationManager.getInstance();
        BuildScenario buildScenario = new BuildScenario(settings.syncProvider, settings.syncProviderPath, settings.connection, 
        		settings.rootLocation);
        bcm.setBuildScenarioForBuildConfiguration(buildScenario, config);
	}

	/**
	 * Set page values based on passed configuration
	 *
	 * @param config
	 * 				the configuration 
	 */
	private void setValues(IConfiguration config) {
		// Disable for multi-configurations.
		if (config instanceof IMultiConfiguration) {
			fSyncToggleButton.setSelection(false);
			this.setNoParticipant();
			return;
		}

		PageSettings settings = fConfigToPageSettings.get(getCfg().getId());
		if (settings == null) {
			settings = this.loadSettings(getCfg());
			if (settings == null) {
				fSyncToggleButton.setSelection(false);
				this.setNoParticipant();
				return; // Error logged inside loadSettings
			}
			fConfigToPageSettings.put(getCfg().getId(), settings);
		}

		if (settings.syncProvider == null) {
			fSyncToggleButton.setSelection(false);
			this.setNoParticipant();
			return;
		} else {
			fSyncToggleButton.setSelection(true);
			// TODO: Change to support multiple sync providers by mapping the provider name to the provider.
			// TODO: Add error checking, probably in initialization, when no providers are available.
			this.setParticipant(0);
			// Note: Set provider and connection first, as they may erase other settings.
			fSelectedParticipant.setRemoteProvider(settings.remoteProvider);
			fSelectedParticipant.setRemoteConnection(settings.connection);
			fSelectedParticipant.setLocation(settings.rootLocation);
			fSelectedParticipant.setToolLocation(settings.syncProviderPath);

		}

		update();
	}

	/**
	 * Load settings from the BuildConfigurationManager for the given configuration
	 * Note that this works even for new configurations, as the manager will return a build scenario for the closest known
	 * ancestor configuration in that case.
	 *
	 * @param config
	 * 				the configuration
	 * @return Configuration settings or null if config not found in BuildConfigurationManager
	 */
	private PageSettings loadSettings(IConfiguration config) {
		BuildScenario buildScenario = BuildConfigurationManager.getInstance().getBuildScenarioForBuildConfiguration(config);
		if (buildScenario == null) {
	       	IStatus status = new Status(IStatus.ERROR, RDTSyncUIPlugin.PLUGIN_ID, "Error loading configuration data"); //$NON-NLS-1$
	       	StatusManager.getManager().handle(status, StatusManager.SHOW);
			return fLocalSettings;
		}
		PageSettings settings = new PageSettings();
		settings.syncProvider = buildScenario.getSyncProvider();
		settings.syncProviderPath = buildScenario.getSyncProviderPath();
		settings.remoteProvider = buildScenario.getRemoteProvider();
		IProject project = config.getOwner().getProject();
		settings.rootLocation = buildScenario.getLocation(project);
		try {
			settings.connection = buildScenario.getRemoteConnection();
		} catch (MissingConnectionException e) {
			// nothing to do
		}

		return settings;
	}

	/**
	 * Handle change of configuration. Current page values must be stored and then updated
	 * 
	 * @param cfg
	 * 			the new configuration. Passed to superclass but otherwise ignored.
	 */
	@Override
	protected void cfgChanged(ICConfigurationDescription cfg) {
		super.cfgChanged(cfg);
		// This method is called before createWidgets, so ignore this initial call.
		if (fWidgetsReady == false) {
			return;
		}
		// Update settings for previous configuration first
		this.storeSettings(fConfigBeforeSwitch);
		fConfigBeforeSwitch = getCfg();
		this.setValues(getCfg());
	}

	/**
	 * Simply call performOk() and ignore arguments
	 */
	@Override
	protected void performApply(ICResourceDescription src, ICResourceDescription dst) {
		this.performOk();
	}
	


	/**
	 * Store the current page values as the settings for the passed configuration
	 *
	 * @param config
	 * 				the configuration
	 */
	private void storeSettings(IConfiguration config) {
		if (config == null || config instanceof MultiConfiguration) {
			return;
		}
		IProject project = config.getOwner().getProject();

		PageSettings settings;
		if (!fSyncToggleButton.getSelection()) {
			settings = fLocalSettings;
		} else {
			settings = new PageSettings();
			settings.syncProvider = BuildConfigurationManager.getInstance().getProjectSyncProvider(project);
			settings.rootLocation = fSelectedParticipant.getLocation();
			settings.syncProviderPath = fSelectedParticipant.getToolLocation();
			settings.connection = fSelectedParticipant.getRemoteConnection();
			settings.remoteProvider = fSelectedParticipant.getRemoteProvider();
		}

		fConfigToPageSettings.put(config.getId(), settings);
	}

	private Composite createControl(ISynchronizeParticipant part) {
		Composite comp = new Composite(fProviderArea, SWT.NONE);
		comp.setLayout(new GridLayout(1, false));
		comp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		part.createConfigurationArea(comp, new MockWizardContainer());
		// Without this, participant uses the old project name from the last time it was invoked.
		part.setProjectName(""); //$NON-NLS-1$
		return comp;
	}

	private void setParticipant(int index) {
		fSelectedParticipant = fProviders.get(index);
		fProviderStack.topControl = fProviderControls.get(index);
		fProviderArea.layout();
	}

	private void setNoParticipant() {
		fSelectedParticipant = null;
		fProviderStack.topControl = fLocalProviderComposite;
		fProviderArea.layout();
	}
	
	/**
	 * Reload settings for current configuration and update page accordingly.
	 */
	@Override
	public void performDefaults() {
		if (fWidgetsReady == false) {
			return;
		}
		PageSettings settings = this.loadSettings(getCfg());
		if (settings == null) {
			// Handled inside loadSettings
		}
		fConfigToPageSettings.put(getCfg().getId(), settings);
		this.setValues(getCfg());
	}
	
	private void update() {
		getContainer().updateMessage();
		getContainer().updateButtons();
		updateApplyButton();
		enableConfigSelection(isValid());
	}

	public String getErrorMessage() {
		if (super.getErrorMessage() != null) 
			return super.getErrorMessage();
		if (fSelectedParticipant == null) {
			return null;
		} else {
			return fSelectedParticipant.getErrorMessage();
		}
	}
	
	public boolean isValid() {
		return super.isValid() && getErrorMessage()==null;
	}

	// A mock wizard container that provides a minimum of wizard container functionality needed by the participant.
	class MockWizardContainer implements IWizardContainer {
		@Override
		public void run(boolean fork, boolean cancelable, IRunnableWithProgress runnable) throws InvocationTargetException,
		InterruptedException {
			ProgressMonitorDialog dialog = new ProgressMonitorDialog(fComposite.getShell());
			dialog.run(true, true, runnable);
		}

		@Override
		public IWizardPage getCurrentPage() {
			// do nothing
			return null;
		}

		@Override
		public void showPage(IWizardPage page) {
			// do nothing
		}

		@Override
		public void updateTitleBar() {
			getContainer().updateTitle();
		}

		@Override
		public void updateWindowTitle() {
			// do nothing
		}

		@Override
		public Shell getShell() {
			return BuildRemotePropertiesPage.this.getShell();
		}

		@Override
		public void updateButtons() {
			// Update everything. This is a hack to work around not having an "update" callback from the participant.
			BuildRemotePropertiesPage.this.update();
		}

		@Override
		public void updateMessage() {
			// Update everything. This is a hack to work around not having an "update" callback from the participant.
			BuildRemotePropertiesPage.this.update();	
		}
	}
}