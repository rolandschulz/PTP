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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICResourceDescription;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IMultiConfiguration;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.internal.core.MultiConfiguration;
import org.eclipse.cdt.managedbuilder.ui.properties.AbstractSingleBuildPage;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ptp.rdt.sync.core.BuildConfigurationManager;
import org.eclipse.ptp.rdt.sync.core.BuildScenario;
import org.eclipse.ptp.rdt.sync.ui.RDTSyncUIPlugin;
import org.eclipse.ptp.rdt.sync.ui.messages.Messages;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.IRemoteFileManager;
import org.eclipse.ptp.remote.core.IRemoteServices;
import org.eclipse.ptp.remote.ui.IRemoteUIConnectionManager;
import org.eclipse.ptp.remote.ui.IRemoteUIConstants;
import org.eclipse.ptp.remote.ui.IRemoteUIFileManager;
import org.eclipse.ptp.remote.ui.IRemoteUIServices;
import org.eclipse.ptp.remote.ui.PTPRemoteUIPlugin;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.statushandlers.StatusManager;

public class BuildRemotePropertiesPage extends AbstractSingleBuildPage {
	private IRemoteConnection fSelectedConnection = null;
	private IRemoteServices fSelectedProvider = null;
	private IConfiguration fConfigBeforeSwitch = null;
	private boolean fWidgetsReady = false;

	// Container for all information that appears on a page
	private static class PageSettings {
		IRemoteConnection connection;
		IRemoteServices remoteProvider;
		String rootLocation;
		
		public boolean equals(PageSettings otherSettings) {
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

	private final Map<Integer, IRemoteServices> fComboIndexToRemoteServicesProviderMap = new HashMap<Integer, IRemoteServices>();
	private final Map<IRemoteServices, Integer> fComboRemoteServicesProviderToIndexMap = new HashMap<IRemoteServices, Integer>();
	private final Map<Integer, IRemoteConnection> fComboIndexToRemoteConnectionMap = new HashMap<Integer, IRemoteConnection>();
	private final Map<IRemoteConnection, Integer> fComboRemoteConnectionToIndexMap = new HashMap<IRemoteConnection, Integer>();

	// Cache of page settings for each configuration accessed.
	private final Map<String, PageSettings> fConfigToPageSettings = new HashMap<String, PageSettings>();

	private Button fBrowseButton;
	private Button fNewConnectionButton;
	private Combo fProviderCombo;
	private Combo fConnectionCombo;
	private Text fRootLocationText;
	private Composite composite;

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
		composite = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		composite.setLayout(layout);
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		composite.setLayoutData(gd);

		// Label for "Provider:"
		Label providerLabel = new Label(composite, SWT.LEFT);
		providerLabel.setText(Messages.BRPPage_RemoteProviderLabel);

		// combo for providers
		fProviderCombo = new Combo(composite, SWT.DROP_DOWN | SWT.READ_ONLY);
		gd = new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false);
		gd.horizontalSpan = 2;
		fProviderCombo.setLayoutData(gd);
		fProviderCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				handleServicesSelected();
			}
		});

		// connection combo
		// Label for "Connection:"
		Label connectionLabel = new Label(composite, SWT.LEFT);
		connectionLabel.setText(Messages.BRPPage_ConnectionLabel);

		// combo for providers
		fConnectionCombo = new Combo(composite, SWT.DROP_DOWN | SWT.READ_ONLY);
		// set layout to grab horizontal space
		fConnectionCombo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		fConnectionCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				handleConnectionSelected();
			}
		});

		// new connection button
		fNewConnectionButton = new Button(composite, SWT.PUSH);
		fNewConnectionButton.setText(Messages.BRPPage_ConnectionButton);
		fNewConnectionButton.setEnabled(false);
		fNewConnectionButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				IRemoteUIConnectionManager connectionManager = getUIConnectionManager();
				if (connectionManager != null) {
					connectionManager.newConnection(fNewConnectionButton.getShell());
				}
				// refresh list of connections
				populateConnectionCombo(fConnectionCombo);
				update();
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
			public void modifyText(ModifyEvent e) {
				// MBSCustomPageManager.addPageProperty(REMOTE_SYNC_WIZARD_PAGE_ID,
				// PATH_PROPERTY, fLocationText.getText());
				update();
			}
		});
			

		// browse button
		fBrowseButton = new Button(composite, SWT.PUSH);
		fBrowseButton.setText(Messages.BRPPage_BrowseButton);
		fBrowseButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (fSelectedConnection != null) {
					checkConnection();
					if (fSelectedConnection.isOpen()) {
						IRemoteUIServices remoteUIServices = PTPRemoteUIPlugin.getDefault().getRemoteUIServices(fSelectedProvider);
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
		int selectionIndex = fConnectionCombo.getSelectionIndex();
		fSelectedConnection = fComboIndexToRemoteConnectionMap.get(selectionIndex);
		updateNewConnectionButtonEnabled(fNewConnectionButton);
		update();
	}
	
	/**
	 * Handle new remote services selected
	 */
	private void handleServicesSelected() {
		int selectionIndex = fProviderCombo.getSelectionIndex();
		fSelectedProvider = fComboIndexToRemoteServicesProviderMap.get(selectionIndex);
		populateConnectionCombo(fConnectionCombo);
		updateNewConnectionButtonEnabled(fNewConnectionButton);
		update();
	}

	/**
	 * @param connectionCombo
	 */
	private void populateConnectionCombo(final Combo connectionCombo) {
		fSelectedConnection = null;
		connectionCombo.removeAll();
		IRemoteConnection[] connections = fSelectedProvider.getConnectionManager().getConnections();
		for (int k = 0; k < connections.length; k++) {
			connectionCombo.add(connections[k].getName(), k);
			fComboIndexToRemoteConnectionMap.put(k, connections[k]);
			fComboRemoteConnectionToIndexMap.put(connections[k], k);
		}
	}
	
	private void populateRemoteProviderCombo(final Combo providerCombo) {
		providerCombo.removeAll();
		IRemoteServices[] providers = PTPRemoteUIPlugin.getDefault().getRemoteServices(null);
		for (int k = 0; k < providers.length; k++) {
			providerCombo.add(providers[k].getName(), k);
			fComboIndexToRemoteServicesProviderMap.put(k, providers[k]);
			fComboRemoteServicesProviderToIndexMap.put(providers[k], k);
		}
	}
	
	/**
	 * @param button
	 */
	private void updateNewConnectionButtonEnabled(Button button) {
		IRemoteUIConnectionManager connectionManager = getUIConnectionManager();
		button.setEnabled(connectionManager != null);
	}

	/**
	 * Save each visited configuration
	 */
	@Override
	public boolean performOk() {
		super.performOk();
		if (fWidgetsReady == false) {
			return true;
		}
		// Don't forget to save changes made to the current configuration before proceeding
		this.storeSettings(getCfg());
		for (ICConfigurationDescription desc : getCfgsReadOnly(getProject())) {
			IConfiguration config = getCfg(desc);
			if (config == null || config instanceof MultiConfiguration) {
				continue;
			}

			PageSettings settings = fConfigToPageSettings.get(config.getId());
			if (settings != null && this.isConfigAltered(config, settings)) {
				this.saveConfig(config, settings);
			}
		}
		
		return true;
	}
	
	/**
	 * Load system settings for config and see if they differ from passed settings
	 *
	 * @param config
	 * 				the configuration to load and compare
	 * @param settings
	 * 				settings to compare (presumably the settings entered by the user)
	 * @return whether or not settings differ
	 */
	private boolean isConfigAltered(IConfiguration config, PageSettings settings) {
		PageSettings systemSettings = this.loadSettings(config);
		if (systemSettings == null) {
			return true; // Is logged inside loadSettings
		} else if (settings.equals(systemSettings)) {
			return false;
		} else {
			return true;
		}
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
		ManagedBuildManager.saveBuildInfo(config.getOwner().getProject(), true);

        // Register with build configuration manager. This must be done after saving build info with ManagedBuildManager, as
        // the BuildConfigurationManager relies on the data being up-to-date.
        BuildConfigurationManager bcm = BuildConfigurationManager.getInstance();
        String syncProvider = bcm.getBuildScenarioForBuildConfiguration(config).getSyncProvider();
        BuildScenario buildScenario = new BuildScenario(syncProvider, settings.connection, settings.rootLocation);
        bcm.setBuildScenarioForBuildConfiguration(buildScenario, config);
        try {
                bcm.saveConfigurationData();
        } catch (IOException e) {
        	IStatus status = new Status(IStatus.ERROR, RDTSyncUIPlugin.PLUGIN_ID, "Error saving configuration data: " +  //$NON-NLS-1$
        																										e.getMessage(), e);
        	StatusManager.getManager().handle(status, StatusManager.SHOW);
        }
	}
	
	// Connection button handling
	private void checkConnection() {
		IRemoteUIConnectionManager mgr = getUIConnectionManager();
		if (mgr != null) {
			mgr.openConnectionWithProgress(fConnectionCombo.getShell(), null, fSelectedConnection);
		}
	}
	
	private IRemoteUIConnectionManager getUIConnectionManager() {
		IRemoteUIConnectionManager connectionManager = PTPRemoteUIPlugin.getDefault().getRemoteUIServices(fSelectedProvider)
				.getUIConnectionManager();
		return connectionManager;
	}
	
	/**
	 * Set page values based on passed configuration
	 *
	 * @param config
	 * 				the configuration 
	 */
	private void setValues(IConfiguration config) {
		// Disable for multi-configurations. Note that we set parentComposite to invisible, not the tab, because we want to reappear when
		// the configuration changes back to a single configuration.
		if (config instanceof IMultiConfiguration) {
			composite.setEnabled(false);
			return;
		}
		composite.setEnabled(true);
		populateRemoteProviderCombo(fProviderCombo);
		PageSettings settings = fConfigToPageSettings.get(getCfg().getId());
		if (settings == null) {
			settings = this.loadSettings(getCfg());
			if (settings == null) {
				return; // Is logged inside loadSettings
			}
			fConfigToPageSettings.put(getCfg().getId(), settings);
		}

		// Note that provider selection populates the local connection map variables as well as the connection combo. Thus, the
		// provider must be selected first. (Calling select invokes the "handle" listeners for each combo.)
		fProviderCombo.select(fComboRemoteServicesProviderToIndexMap.get(settings.remoteProvider));
		handleServicesSelected();
		if (settings.connection!=null) {
			Integer index = fComboRemoteConnectionToIndexMap.get(settings.connection);
			if (index!=null) {
				fConnectionCombo.select(index);
			}
		}
		handleConnectionSelected();
		fRootLocationText.setText(settings.rootLocation);
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
			return null;
		}
		PageSettings settings = new PageSettings();
		settings.remoteProvider = buildScenario.getRemoteConnection().getRemoteServices();
		settings.connection = buildScenario.getRemoteConnection();
		settings.rootLocation = buildScenario.getLocation();

		return settings;
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

		PageSettings settings = new PageSettings();
		Integer remoteServicesIndex = fProviderCombo.getSelectionIndex();
		Integer connectionIndex = fConnectionCombo.getSelectionIndex();
		settings.remoteProvider = fComboIndexToRemoteServicesProviderMap.get(remoteServicesIndex);
		settings.connection = fComboIndexToRemoteConnectionMap.get(connectionIndex);
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
	
	public String getErrorMessage() {
		if (super.getErrorMessage() != null) 
			return super.getErrorMessage();
		if (fSelectedProvider == null) 
			return Messages.GitParticipant_0;
		if (fSelectedConnection == null) 
			return Messages.GitParticipant_1;
		if (fRootLocationText.getText().length() == 0 ) 
			return Messages.GitParticipant_2;
		IRemoteFileManager fileManager = fSelectedProvider.getFileManager(fSelectedConnection);
		if ( fileManager.toURI(fRootLocationText.getText()) == null) 
			return Messages.GitParticipant_3;
		// should we check permissions of: fileManager.getResource(fLocationText.getText()).getParent() ?
		return null;
	}
	
	public boolean isValid() {
		return super.isValid() && getErrorMessage()==null;
	}
	
}