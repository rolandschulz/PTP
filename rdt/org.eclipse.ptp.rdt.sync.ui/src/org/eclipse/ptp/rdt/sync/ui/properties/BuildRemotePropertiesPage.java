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
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
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
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.ptp.rdt.sync.core.BuildConfigurationManager;
import org.eclipse.ptp.rdt.sync.core.BuildScenario;
import org.eclipse.ptp.rdt.sync.core.CommandRunner;
import org.eclipse.ptp.rdt.sync.core.MissingConnectionException;
import org.eclipse.ptp.rdt.sync.core.RemoteExecutionException;
import org.eclipse.ptp.rdt.sync.core.RemoteSyncException;
import org.eclipse.ptp.rdt.sync.core.SyncManager;
import org.eclipse.ptp.rdt.sync.core.CommandRunner.CommandResults;
import org.eclipse.ptp.rdt.sync.ui.RDTSyncUIPlugin;
import org.eclipse.ptp.rdt.sync.ui.messages.Messages;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.IRemoteFileManager;
import org.eclipse.ptp.remote.core.IRemoteServices;
import org.eclipse.ptp.remote.core.PTPRemoteCorePlugin;
import org.eclipse.ptp.remote.core.exception.RemoteConnectionException;
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
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.statushandlers.StatusManager;

public class BuildRemotePropertiesPage extends AbstractSingleBuildPage {
	private static final String FILE_SCHEME = "file"; //$NON-NLS-1$
	private static final String TOUCH_TEST_FILE = ".touch_test_file_ptp_sync"; //$NON-NLS-1$
	private static final Display display = Display.getCurrent();

	private IRemoteConnection fSelectedConnection = null;
	private IRemoteServices fSelectedProvider = null;
	private IConfiguration fConfigBeforeSwitch = null;
	private boolean fWidgetsReady = false;
	
	// Assume values are valid by default
	private boolean fGitValidated = true;
	private boolean fRemoteValidated = true;

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

	private final Map<Integer, IRemoteServices> fComboIndexToRemoteServicesProviderMap = new HashMap<Integer, IRemoteServices>();
	private final Map<IRemoteServices, Integer> fComboRemoteServicesProviderToIndexMap = new HashMap<IRemoteServices, Integer>();
	private final Map<Integer, IRemoteConnection> fComboIndexToRemoteConnectionMap = new HashMap<Integer, IRemoteConnection>();
	private final Map<IRemoteConnection, Integer> fComboRemoteConnectionToIndexMap = new HashMap<IRemoteConnection, Integer>();

	// Cache of page settings for each configuration accessed.
	private final Map<String, PageSettings> fConfigToPageSettings = new HashMap<String, PageSettings>();

	private Button fSyncToggleButton;
	private Button fBrowseButton;
	private Button fRemoteLocationValidationButton;
	private Button fGitLocationBrowseButton;
	private Button fNewConnectionButton;
	private Button fUseGitDefaultLocationButton;
	private Button fGitLocationValidationButton;
	private Combo fProviderCombo;
	private Combo fConnectionCombo;
	private Text fRootLocationText;
	private Text fGitLocationText;
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

		// Sync toggle
		fSyncToggleButton = new Button(composite, SWT.CHECK);
		fSyncToggleButton.setText(Messages.BuildRemotePropertiesPage_0);
		fSyncToggleButton.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false, 3, 1));
		fSyncToggleButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				setIsRemoteConfig(fSyncToggleButton.getSelection());
				update();
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				setIsRemoteConfig(fSyncToggleButton.getSelection());
				update();
			}
		});

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
		gd = new GridData(GridData.END, GridData.CENTER, false, false);
		fNewConnectionButton.setLayoutData(gd);
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
				setRemoteIsValid(false);
				update();
			}
		});

		// browse button
		fBrowseButton = new Button(composite, SWT.PUSH);
		fBrowseButton.setText(Messages.BuildRemotePropertiesPage_5);
		gd = new GridData(GridData.END, GridData.CENTER, false, false);
		fBrowseButton.setLayoutData(gd);
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
		
        // Remote location validation button
        fRemoteLocationValidationButton = new Button(composite, SWT.PUSH);
        fRemoteLocationValidationButton.setText(Messages.BuildRemotePropertiesPage_11);
        gd = new GridData(GridData.END, GridData.CENTER, true, false);
        gd.horizontalSpan = 3;
        fRemoteLocationValidationButton.setLayoutData(gd);
        fRemoteLocationValidationButton.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                        setRemoteIsValid(isRemoteValid());
                        update();
                }
        });
		
		// Git location
		// "Use default location" button
		fUseGitDefaultLocationButton = new Button(composite, SWT.CHECK);
		fUseGitDefaultLocationButton.setText(Messages.BuildRemotePropertiesPage_3);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 3;
		fUseGitDefaultLocationButton.setLayoutData(gd);
		fUseGitDefaultLocationButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				handleGitDefaultLocationButtonPushed();
				update();
			}
		});
		fUseGitDefaultLocationButton.setSelection(false);

		// Git location label
		Label gitLocationLabel = new Label(composite, SWT.NONE);
		gitLocationLabel.setText(Messages.BuildRemotePropertiesPage_4);

		// Git location entry field
		fGitLocationText = new Text(composite, SWT.SINGLE | SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 1;
		gd.grabExcessHorizontalSpace = true;
		gd.widthHint = 250;
		fGitLocationText.setLayoutData(gd);
		fGitLocationText.setEnabled(false);
		fGitLocationText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				setGitIsValid(false);
				update();
			}
		});
		
		// Git location browse button
		fGitLocationBrowseButton = new Button(composite, SWT.PUSH);
		fGitLocationBrowseButton.setText(Messages.BuildRemotePropertiesPage_5);
		gd = new GridData(GridData.END);
		fGitLocationBrowseButton.setLayoutData(gd);
		fGitLocationBrowseButton.addSelectionListener(new SelectionAdapter() {
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
								String selectedPath = fileMgr.browseFile(
										fGitLocationText.getShell(),
										"Project Location (" + fSelectedConnection.getName() + ")", null, IRemoteUIConstants.NONE); //$NON-NLS-1$ //$NON-NLS-2$
								if (selectedPath != null) {
									fGitLocationText.setText(selectedPath);
								}
							}
						}
					}
				}
			}
		});
		fGitLocationBrowseButton.setSelection(false);
		
        // Git location validation button
        fGitLocationValidationButton = new Button(composite, SWT.PUSH);
        fGitLocationValidationButton.setText(Messages.BuildRemotePropertiesPage_11);
        gd = new GridData(GridData.END, GridData.CENTER, true, false);
        gd.horizontalSpan = 3;
        fGitLocationValidationButton.setLayoutData(gd);
        fGitLocationValidationButton.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                        setGitIsValid(isGitValid());
                        update();
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
		fRootLocationText.setText(getDefaultPathDisplayString());
		this.setRemoteIsValid(false);
		this.changeGitLocationUIForConnection();
		update();
	}
	
	/**
	 * Handle new remote services selected
	 */
	private void handleServicesSelected() {
		int selectionIndex = fProviderCombo.getSelectionIndex();
		fSelectedProvider = fComboIndexToRemoteServicesProviderMap.get(selectionIndex);
		populateConnectionCombo(fConnectionCombo);
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
	private boolean isConnectionManagerAvailable() {
		IRemoteUIConnectionManager connectionManager = getUIConnectionManager();
		if (connectionManager == null) {
			return false;
		} else {
			return true;
		}
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
	
	// Connection button handling
	private void checkConnection() {
		IRemoteUIConnectionManager mgr = getUIConnectionManager();
		if (mgr != null) {
			mgr.openConnectionWithProgress(fConnectionCombo.getShell(), null, fSelectedConnection);
		}
	}
	
	private IRemoteUIConnectionManager getUIConnectionManager() {
		if (fSelectedProvider == null) {
			return null;
		}
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
		// Disable for multi-configurations.
		if (config instanceof IMultiConfiguration) {
			composite.setEnabled(false);
			return;
		} else {
			composite.setEnabled(true);
		}

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
		this.setRemoteIsValid(true);
		
		// Git location must be set before calling setIsRemoteConfig()
		if (settings.syncProviderPath == null) {
			fGitLocationText.setText(""); //$NON-NLS-1$
		} else {
			fGitLocationText.setText(settings.syncProviderPath);
		}

		if (settings.syncProvider != null) {
			fSyncToggleButton.setSelection(true);
			this.setIsRemoteConfig(true);
		} else {
			fSyncToggleButton.setSelection(false);
			this.setIsRemoteConfig(false);
		}

		update();
	}

	private void setIsRemoteConfig(boolean isRemote) {
		fProviderCombo.setEnabled(isRemote);
		fConnectionCombo.setEnabled(isRemote);
		fRootLocationText.setEnabled(isRemote);
		fBrowseButton.setEnabled(isRemote);
		if (isRemote && this.isConnectionManagerAvailable()) {
			fNewConnectionButton.setEnabled(true);
		} else {
			fNewConnectionButton.setEnabled(false);
		}
		this.setGitLocationUI(isRemote, fGitLocationText.getText());
		fUseGitDefaultLocationButton.setEnabled(isRemote);
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
		settings.syncProvider = buildScenario.getSyncProvider();
		settings.syncProviderPath = buildScenario.getSyncProviderPath();
		settings.remoteProvider = buildScenario.getRemoteProvider();
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
	 * 				the configuration
	 */
	private void storeSettings(IConfiguration config) {
		if (config == null || config instanceof MultiConfiguration) {
			return;
		}
		IProject project = config.getOwner().getProject();

		PageSettings settings = new PageSettings();
		Integer remoteServicesIndex = fProviderCombo.getSelectionIndex();
		Integer connectionIndex = fConnectionCombo.getSelectionIndex();
		if (fSyncToggleButton.getSelection()) {
			settings.syncProvider = BuildConfigurationManager.getInstance().getProjectSyncProvider(project);
			settings.syncProviderPath = fGitLocationText.getText();
		} else {
			settings.syncProvider = null;
			settings.syncProviderPath = null;
		}
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
	
    /**
     * Return the path we are going to display. If it is a file URI then remove
     * the file prefix.
     *
     * Only do this if the connection is open. Otherwise we will attempt to
     * connect to the first machine in the list, which is annoying.
     *
     * @return String
     */
    private String getDefaultPathDisplayString() {
            if (fSelectedConnection != null && fSelectedConnection.isOpen()) {
                    IRemoteFileManager fileMgr = fSelectedProvider.getFileManager(fSelectedConnection);
                    URI defaultURI = fileMgr.toURI(fSelectedConnection.getWorkingDirectory());

                    // Handle files specially. Assume a file if there is no project to
                    // query
                    String projectName = getCfg().getOwner().getProject().toString();
                    if (defaultURI != null && defaultURI.getScheme().equals(FILE_SCHEME)) {
                            return Platform.getLocation().append(projectName).toString();
                    }
                    if (defaultURI == null) {
                            return ""; //$NON-NLS-1$
                    }
                    return new Path(defaultURI.getPath()).append(projectName).toString();
            }
            return ""; //$NON-NLS-1$
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
		if (fSyncToggleButton.getSelection() && locationIsInWorkspace())
			return Messages.BuildRemotePropertiesPage_1;
		if (!fGitValidated)
			return Messages.BuildRemotePropertiesPage_15;
		if (!fRemoteValidated)
			return Messages.BuildRemotePropertiesPage_16;
		return null;
	}
	
	// Test whether the sync location is inside the local Eclipse workspace
	private boolean locationIsInWorkspace() {
		// Check if connection is the local connection. Only continue if we know this is the local workspace. (Give user the
		// benefit of the doubt.)
		IRemoteServices localService = PTPRemoteCorePlugin.getDefault().getRemoteServices(
				"org.eclipse.ptp.remote.LocalServices", null); //$NON-NLS-1$
		if (localService != null) {
			IRemoteConnection localConnection = localService.getConnectionManager().getConnection("Local"); //$NON-NLS-1$
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
	
	public boolean isValid() {
		return super.isValid() && getErrorMessage()==null;
	}
	
	// Functions for setting Git location UI elements
	// The first three functions are the outer interface to change the elements. The next two functions are helpers that should
	// only be invoked by the first three.
	
	private void changeGitLocationUIForConnection() {
		if (!fUseGitDefaultLocationButton.getSelection()) {
			return;
		} else {	
			this.changeToDefaultGitLocation();
		}
	}

	// Alter content and UI elements in response to button push
	private void handleGitDefaultLocationButtonPushed() {
		if (fUseGitDefaultLocationButton.getSelection()) {
			this.changeToDefaultGitLocation();
		} else {
			fGitLocationText.setText(""); //$NON-NLS-1$
			this.setUsingDefaultGitLocation(false);
		}
	}

	// Fill in Git location text and set the related UI elements based on the given information.
	// syncProviderPath may be null or empty, in which case this function attempts to set the default Git.
	private void setGitLocationUI(boolean isRemote, String syncProviderPath) {
		// For a local project, make blank
		if (!isRemote) {
			this.setUsingDefaultGitLocation(true);
			fGitLocationText.setText(""); //$NON-NLS-1$
		// If path already available, use it and assume it is not default.
		} else if (syncProviderPath != null && syncProviderPath.length() > 0) {
			this.setUsingDefaultGitLocation(false);
			fGitLocationText.setText(syncProviderPath);
		// Otherwise, attempt to retrieve default path
		} else {
			this.changeToDefaultGitLocation();
		}
		
		// Assume values are valid by default
		this.setGitIsValid(true);
		this.setRemoteIsValid(true);
	}
	
	// Attempt to retrieve default Git on remote and set UI elements appropriately.
	// Fills in textbox only if default Git found.
	private void changeToDefaultGitLocation() {
		String errorMessage = null;
		CommandResults cr = null;
		if (fSelectedConnection == null) {
			errorMessage = Messages.BuildRemotePropertiesPage_6 + ": " + Messages.BuildRemotePropertiesPage_9; //$NON-NLS-1$
		} else {
			// Run command to get Git path
			List<String> args = Arrays.asList("which", "git"); //$NON-NLS-1$ //$NON-NLS-2$
			try {
				cr = this.runRemoteCommand(args, Messages.BuildRemotePropertiesPage_10);
				errorMessage = this.buildErrorMessage(cr, Messages.BuildRemotePropertiesPage_6, null);
			} catch (RemoteExecutionException e) {
				errorMessage = this.buildErrorMessage(null, Messages.BuildRemotePropertiesPage_7, e);
			}
		}

		// Unable to find Git location
		if (errorMessage != null) {
			this.setUsingDefaultGitLocation(false);
			MessageDialog.openError(null, Messages.BuildRemotePropertiesPage_8, errorMessage);
			this.setGitIsValid(false);
		// Git location found
		} else {
			this.setUsingDefaultGitLocation(true);
			fGitLocationText.setText(cr.getStdout().trim());
			this.setGitIsValid(true);
		}
	}
	
	// Set UI elements either for using default Git or not using default Git.
	// Enforces the following invariant: Other elements disabled if and only if "use default" selected.
	private void setUsingDefaultGitLocation(boolean useDefault) {
		if (useDefault) {
			fUseGitDefaultLocationButton.setSelection(true);
			fGitLocationText.setEnabled(false);
			fGitLocationBrowseButton.setEnabled(false);
		} else {
			fUseGitDefaultLocationButton.setSelection(false);
			fGitLocationText.setEnabled(true);
			fGitLocationBrowseButton.setEnabled(true);
		}
	}
	
	// End functions for setting Git location UI elements
	
	// Check if the remote location is valid (does not actually set it as valid)
	private boolean isRemoteValid() {
		IPath parentPath = new Path(fRootLocationText.getText());
		boolean isValid;
		if (!parentPath.isAbsolute()) {
			return false;
		}

		// Find the lowest-level file in the path that exist.
		while(!parentPath.isRoot()) {
			List<String> args = Arrays.asList("test", "-e", parentPath.toString()); //$NON-NLS-1$ //$NON-NLS-2$
			String errorMessage = null;
			CommandResults cr = null;
			try {
				cr = this.runRemoteCommand(args, Messages.BuildRemotePropertiesPage_12);
			} catch (RemoteExecutionException e) {
				errorMessage = this.buildErrorMessage(null, Messages.BuildRemotePropertiesPage_13, e);
			}

			if (errorMessage != null) {
				MessageDialog.openError(null, Messages.BuildRemotePropertiesPage_14, errorMessage);
				return false;
			} else if (cr.getExitCode() == 0) {
				break;
			}

			parentPath = parentPath.removeLastSegments(1);
		}

		// Assume parent path is a directory and see if we can write a test file to it.
		// Note that this test fails if parent path is not a directory, so no need to test that case.
		String touchFile = parentPath.append(new Path(TOUCH_TEST_FILE)).toString();
		List<String> args = Arrays.asList("touch", touchFile); //$NON-NLS-1$
		String errorMessage = null;
		CommandResults cr = null;
		try {
			cr = this.runRemoteCommand(args, Messages.BuildRemotePropertiesPage_12);
		} catch (RemoteExecutionException e) {
			errorMessage = this.buildErrorMessage(null, Messages.BuildRemotePropertiesPage_13, e);
		}

		if (errorMessage != null) {
			MessageDialog.openError(null, Messages.BuildRemotePropertiesPage_14, errorMessage);
			return false;
		} else if (cr.getExitCode() == 0) {
			isValid = true;
		} else {
			isValid = false;;
		}

		// Remove the test file
		args = Arrays.asList("rm", "-f", touchFile); //$NON-NLS-1$ //$NON-NLS-2$
		errorMessage = null;
		cr = null;
		try {
			cr = this.runRemoteCommand(args, Messages.BuildRemotePropertiesPage_18);
			errorMessage = this.buildErrorMessage(cr, Messages.BuildRemotePropertiesPage_19 + touchFile, null);
		} catch (RemoteExecutionException e) {
			errorMessage = this.buildErrorMessage(null, Messages.BuildRemotePropertiesPage_19 + touchFile, e);
		}

		if (errorMessage != null) {
			MessageDialog.openError(null, Messages.BuildRemotePropertiesPage_14, errorMessage);
		}

		return isValid;
	}

    // Set the remote location as valid
    private void setRemoteIsValid(boolean isValid) {
    	fRemoteValidated = isValid;
    	if (isValid) {
    		fRootLocationText.setForeground(display.getSystemColor(SWT.COLOR_BLACK));
    	} else {
    		fRootLocationText.setForeground(display.getSystemColor(SWT.COLOR_DARK_RED));
    	}
    }

    // Check if the Git location is valid (does not actually set it as valid)
    private boolean isGitValid() {
		IPath gitPath = new Path(fGitLocationText.getText());
    	if (!gitPath.isAbsolute()) {
    		return false;
    	}
    	List<String> args = Arrays.asList("test", "-f", gitPath.toString()); //$NON-NLS-1$ //$NON-NLS-2$
    	String errorMessage = null;
    	CommandResults cr = null;
    	try {
    		cr = this.runRemoteCommand(args, Messages.BuildRemotePropertiesPage_22);
    	} catch (RemoteExecutionException e) {
    		errorMessage = this.buildErrorMessage(null, Messages.BuildRemotePropertiesPage_23, e);
    	}

    	if (errorMessage != null) {
    		MessageDialog.openError(null, Messages.BuildRemotePropertiesPage_14, errorMessage);
    		return false;
    	} else if (cr.getExitCode() != 0) {
    		return false;
    	} else {
    		return true;
    	}
    }

    // Set the Git location as valid
    private void setGitIsValid(boolean isValid) {
    	fGitValidated = isValid;
    	if (isValid) {
    		fGitLocationText.setForeground(display.getSystemColor(SWT.COLOR_BLACK));
    	} else {
    		fGitLocationText.setForeground(display.getSystemColor(SWT.COLOR_DARK_RED));
    	}
    }

	
	// Wrapper for running commands - wraps exceptions and invoking of command runner inside container run command.
	private CommandResults remoteCommandResults;
	private CommandResults runRemoteCommand(final List<String> command, final String commandDesc) throws RemoteExecutionException {
		try {
			ProgressMonitorDialog dialog = new ProgressMonitorDialog(composite.getShell());
			dialog.run(true, true, new IRunnableWithProgress() {
				@Override
				public void run(IProgressMonitor monitor) throws InvocationTargetException {
					monitor.beginTask(commandDesc, 100);
					SubMonitor progress = SubMonitor.convert(monitor, 100);
					try {
						remoteCommandResults = CommandRunner.executeRemoteCommand(fSelectedConnection, command, null, progress);
					} catch (RemoteSyncException e) {
						throw new InvocationTargetException(e);
					} catch (IOException e) {
						throw new InvocationTargetException(e);
					} catch (InterruptedException e) {
						throw new InvocationTargetException(e);
					} catch (RemoteConnectionException e) {
						throw new InvocationTargetException(e);
					} finally {
						monitor.done();
					}
				}
			});
		} catch (InvocationTargetException e) {
			throw new RemoteExecutionException(e.getCause());
		} catch (InterruptedException e) {
			throw new RemoteExecutionException(e);
		}
		return remoteCommandResults;
	}
	
	// Builds error message for command.
	// Either the command result or the exception should be null, but not both.
	// baseMessage cannot be null.
	// Returns error message or null if no error occurred (can only occur if cr is not null).
	private String buildErrorMessage(CommandResults cr, String baseMessage, RemoteExecutionException e) {
		// Command successful
		if (cr != null && cr.getExitCode() == 0) {
			return null;
		}

		// Command runs but unsuccessfully
		if (cr != null) {
			return baseMessage + ": " + cr.getStderr(); //$NON-NLS-1$
		}

		// Command did not run - exception thrown
		String errorMessage = baseMessage;
		if (e.getMessage() != null) {
			errorMessage += ": " + e.getMessage(); //$NON-NLS-1$
		}

		return errorMessage;
	}
}