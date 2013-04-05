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
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.ptp.rdt.sync.core.BuildConfigurationManager;
import org.eclipse.ptp.rdt.sync.core.BuildScenario;
import org.eclipse.ptp.rdt.sync.core.MissingConnectionException;
import org.eclipse.ptp.rdt.sync.core.SyncManager;
import org.eclipse.ptp.rdt.sync.ui.RDTSyncUIPlugin;
import org.eclipse.ptp.rdt.sync.ui.messages.Messages;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.IRemoteConnectionManager;
import org.eclipse.ptp.remote.core.IRemoteFileManager;
import org.eclipse.ptp.remote.core.IRemoteServices;
import org.eclipse.ptp.remote.core.RemoteServices;
import org.eclipse.ptp.remote.ui.IRemoteUIConnectionManager;
import org.eclipse.ptp.remote.ui.IRemoteUIConstants;
import org.eclipse.ptp.remote.ui.IRemoteUIFileManager;
import org.eclipse.ptp.remote.ui.IRemoteUIServices;
import org.eclipse.ptp.remote.ui.RemoteUIServices;
import org.eclipse.ptp.remote.ui.widgets.RemoteConnectionWidget;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.statushandlers.StatusManager;

public class BuildRemotePropertiesPage extends AbstractSingleBuildPage {
	private IRemoteConnection fSelectedConnection = null;
	private IConfiguration fConfigBeforeSwitch = null;
	private boolean fWidgetsReady = false;

	// Container for all information that appears on a page
	private static class PageSettings {
		String syncProvider;
		IRemoteConnection connection;
		String rootLocation;

		public boolean equals(PageSettings otherSettings) {
			if (otherSettings == null) {
				return false;
			}
			if (this.syncProvider != otherSettings.syncProvider) {
				return false;
			}
			if (this.connection != otherSettings.connection) {
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

	private Button fSyncToggleButton;
	private Button fBrowseButton;
	private Text fRootLocationText;
	private Composite composite;
	private RemoteConnectionWidget fRemoteConnectioWidget;

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
	 *            The parent composite
	 */
	@Override
	public void createWidgets(Composite parent) {
		composite = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		composite.setLayout(layout);
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		composite.setLayoutData(gd);

		// Sync toggle
		fSyncToggleButton = new Button(composite, SWT.CHECK);
		fSyncToggleButton.setText(Messages.BuildRemotePropertiesPage_0);
		fSyncToggleButton.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false, 3, 1));
		fSyncToggleButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				setEnabledForAllWidgets(fSyncToggleButton.getSelection());
				update();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				setEnabledForAllWidgets(fSyncToggleButton.getSelection());
				update();
			}
		});

		fRemoteConnectioWidget = new RemoteConnectionWidget(composite, SWT.NONE, null,
				RemoteConnectionWidget.FLAG_NO_LOCAL_SELECTION, null);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 3;
		fRemoteConnectioWidget.setLayoutData(gd);
		fRemoteConnectioWidget.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				handleConnectionSelected();
			}
		});

		Label rootLocationLabel = new Label(composite, SWT.LEFT);
		rootLocationLabel.setText(Messages.BRPPage_RootLocation);

		fRootLocationText = new Text(composite, SWT.SINGLE | SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 1;
		gd.grabExcessHorizontalSpace = true;
		gd.widthHint = 250;
		fRootLocationText.setLayoutData(gd);
		fRootLocationText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				// MBSCustomPageManager.addPageProperty(REMOTE_SYNC_WIZARD_PAGE_ID,
				// PATH_PROPERTY, fLocationText.getText());
				update();
			}
		});

		// browse button
		fBrowseButton = new Button(composite, SWT.PUSH);
		fBrowseButton.setText(Messages.BuildRemotePropertiesPage_5);
		fBrowseButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (fSelectedConnection != null) {
					checkConnection();
					if (fSelectedConnection.isOpen()) {
						IRemoteUIServices remoteUIServices = RemoteUIServices.getRemoteUIServices(fSelectedConnection
								.getRemoteServices());
						if (remoteUIServices != null) {
							IRemoteUIFileManager fileMgr = remoteUIServices.getUIFileManager();
							if (fileMgr != null) {
								fileMgr.setConnection(fSelectedConnection);
								String correctPath = fRootLocationText.getText();
								String selectedPath = fileMgr.browseDirectory(
										fRootLocationText.getShell(),
										"Project Location (" + fSelectedConnection.getName() + ")", correctPath, IRemoteUIConstants.NONE); //$NON-NLS-1$ //$NON-NLS-2$
								if (selectedPath != null) {
									fRootLocationText.setText(selectedPath);
								}
							}
						}
					}
				}
			}
		});

		fConfigBeforeSwitch = getCfg();
		this.setValues(getCfg());
		fWidgetsReady = true;
	}

	/**
	 * Handle new connection selected
	 */
	private void handleConnectionSelected() {
		fSelectedConnection = fRemoteConnectioWidget.getConnection();
		update();
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
						this.saveConfig(config, settings);
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
	 *            configuration
	 * @param settings
	 *            new settings
	 */
	private void saveConfig(IConfiguration config, PageSettings settings) {
		// Set build path in build configuration to appropriate directory
		IProject project = config.getOwner().getProject();
		ManagedBuildManager.saveBuildInfo(project, true);

		// Register with build configuration manager. This must be done after saving build info with ManagedBuildManager, as
		// the BuildConfigurationManager relies on the data being up-to-date.
		BuildConfigurationManager bcm = BuildConfigurationManager.getInstance();
		BuildScenario buildScenario = new BuildScenario(settings.syncProvider, settings.connection, settings.rootLocation);
		bcm.setBuildScenarioForBuildConfiguration(buildScenario, config);
	}

	// Connection button handling
	private void checkConnection() {
		IRemoteUIConnectionManager mgr = getUIConnectionManager();
		if (mgr != null) {
			mgr.openConnectionWithProgress(fRemoteConnectioWidget.getShell(), null, fSelectedConnection);
		}
	}

	private IRemoteUIConnectionManager getUIConnectionManager() {
		if (fSelectedConnection == null) {
			return null;
		}
		IRemoteUIConnectionManager connectionManager = RemoteUIServices
				.getRemoteUIServices(fSelectedConnection.getRemoteServices()).getUIConnectionManager();
		return connectionManager;
	}

	/**
	 * Set page values based on passed configuration
	 * 
	 * @param config
	 *            the configuration
	 */
	private void setValues(IConfiguration config) {
		// Disable for multi-configurations.
		if (config instanceof IMultiConfiguration) {
			composite.setEnabled(false);
			return;
		} else {
			composite.setEnabled(true);
		}

		PageSettings settings = fConfigToPageSettings.get(getCfg().getId());
		if (settings == null) {
			settings = this.loadSettings(getCfg());
			if (settings == null) {
				return; // Is logged inside loadSettings
			}
			fConfigToPageSettings.put(getCfg().getId(), settings);
		}

		if (settings.syncProvider != null) {
			fSyncToggleButton.setSelection(true);
			this.setEnabledForAllWidgets(true);
		} else {
			fSyncToggleButton.setSelection(false);
			this.setEnabledForAllWidgets(false);
		}

		fRemoteConnectioWidget.setConnection(settings.connection);
		handleConnectionSelected();
		fRootLocationText.setText(settings.rootLocation);
	}

	private void setEnabledForAllWidgets(boolean shouldBeEnabled) {
		fRemoteConnectioWidget.setEnabled(shouldBeEnabled);
		fRootLocationText.setEnabled(shouldBeEnabled);
		fBrowseButton.setEnabled(shouldBeEnabled);
	}

	/**
	 * Handle change of configuration. Current page values must be stored and then updated
	 * 
	 * @param cfg
	 *            the new configuration. Passed to superclass but otherwise ignored.
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
	 * Load settings from the BuildConfigurationManager for the given configuration
	 * Note that this works even for new configurations, as the manager will return a build scenario for the closest known
	 * ancestor configuration in that case.
	 * 
	 * @param config
	 *            the configuration
	 * @return Configuration settings or null if config not found in BuildConfigurationManager
	 */
	private PageSettings loadSettings(IConfiguration config) {
		BuildScenario buildScenario = BuildConfigurationManager.getInstance().getBuildScenarioForBuildConfiguration(config);
		if (buildScenario == null) {
			IStatus status = new Status(IStatus.ERROR, RDTSyncUIPlugin.PLUGIN_ID, "Error loading configuration data"); //$NON-NLS-1$
			StatusManager.getManager().handle(status, StatusManager.SHOW);
			return null;
		}
		PageSettings settings = new PageSettings();
		settings.syncProvider = buildScenario.getSyncProvider();
		try {
			settings.connection = buildScenario.getRemoteConnection();
		} catch (MissingConnectionException e) {
			// nothing to do
		}
		IProject project = config.getOwner().getProject();
		settings.rootLocation = buildScenario.getLocation(project);

		return settings;
	}

	/**
	 * Store the current page values as the settings for the passed configuration
	 * 
	 * @param config
	 *            the configuration
	 */
	private void storeSettings(IConfiguration config) {
		if (config == null || config instanceof MultiConfiguration) {
			return;
		}
		IProject project = config.getOwner().getProject();

		PageSettings settings = new PageSettings();
		if (fSyncToggleButton.getSelection()) {
			settings.syncProvider = BuildConfigurationManager.getInstance().getProjectSyncProvider(project);
		} else {
			settings.syncProvider = null;
		}
		settings.connection = fSelectedConnection;
		settings.rootLocation = fRootLocationText.getText();

		fConfigToPageSettings.put(config.getId(), settings);
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

	@Override
	public String getErrorMessage() {
		if (super.getErrorMessage() != null) {
			return super.getErrorMessage();
		}
		if (fSelectedConnection == null) {
			return Messages.GitParticipant_1;
		}
		if (fRootLocationText.getText().length() == 0) {
			return Messages.GitParticipant_2;
		}
		IRemoteFileManager fileManager = fSelectedConnection.getRemoteServices().getFileManager(fSelectedConnection);
		if (fileManager.toURI(fRootLocationText.getText()) == null) {
			return Messages.GitParticipant_3;
		}
		// should we check permissions of: fileManager.getResource(fLocationText.getText()).getParent() ?
		if (fSyncToggleButton.getSelection() && locationIsInWorkspace()) {
			return Messages.BuildRemotePropertiesPage_1;
		}
		return null;
	}

	// Test whether the sync location is inside the local Eclipse workspace
	private boolean locationIsInWorkspace() {
		// Check if connection is the local connection. Only continue if we know this is the local workspace. (Give user the
		// benefit of the doubt.)
		IRemoteServices localService = RemoteServices.getLocalServices();
		if (localService != null) {
			IRemoteConnection localConnection = localService.getConnectionManager().getConnection(
					IRemoteConnectionManager.LOCAL_CONNECTION_NAME);
			if (localConnection != null) {
				if (fSelectedConnection != localConnection) {
					return false;
				}
			} else {
				return false;
			}
		} else {
			return false;
		}

		// Check path
		IProject project = getCfg().getOwner().getProject();
		Path locationPath = new Path(fRootLocationText.getText());
		if (project.getLocation().isPrefixOf(locationPath)) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean isValid() {
		return super.isValid() && getErrorMessage() == null;
	}

}