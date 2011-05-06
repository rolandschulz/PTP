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
import org.eclipse.ptp.rdt.sync.core.BuildConfigurationManager;
import org.eclipse.ptp.rdt.sync.core.BuildScenario;
import org.eclipse.ptp.rdt.sync.ui.messages.Messages;
import org.eclipse.ptp.remote.core.IRemoteConnection;
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

public class BuildRemotePropertiesPage extends AbstractSingleBuildPage {
	private IRemoteConnection fSelectedConnection;
	private IRemoteServices fSelectedProvider;
	private IConfiguration fConfigBeforeSwitch = null;
	private boolean fWidgetsReady = false;

	private static class PageSettings {
		Integer connectionIndex;
		Integer remoteServicesIndex;
		String rootLocation;
		String buildLocation;
		
		public boolean equals(PageSettings otherSettings) {
			if (!(this.connectionIndex.equals(otherSettings.connectionIndex))) {
				return false;
			}
			if (!(this.remoteServicesIndex.equals(otherSettings.remoteServicesIndex))) {
				return false;
			}
			if (!(this.rootLocation.equals(otherSettings.rootLocation))) {
				return false;
			}
			if (!(this.buildLocation.equals(otherSettings.buildLocation))) {
				return false;
			}
			
			return true;
		}
	}

	private final Map<Integer, IRemoteServices> fComboIndexToRemoteServicesProviderMap = new HashMap<Integer, IRemoteServices>();
	private final Map<IRemoteServices, Integer> fComboRemoteServicesProviderToIndexMap = new HashMap<IRemoteServices, Integer>();
	private final Map<Integer, IRemoteConnection> fComboIndexToRemoteConnectionMap = new HashMap<Integer, IRemoteConnection>();
	private final Map<IRemoteConnection, Integer> fComboRemoteConnectionToIndexMap = new HashMap<IRemoteConnection, Integer>();

	private Button fBrowseButton;
	private Button fNewConnectionButton;
	private Combo fProviderCombo;
	private Combo fConnectionCombo;
	private final Map<String, PageSettings> fConfigToPageSettings = new HashMap<String, PageSettings>();
	private Text fRootLocationText;
	private Text fBuildLocationText;
	private Composite parentComposite;
	private Composite composite;

	/**
	 * Constructor for BuildRemotePropertiesPage
	 */
	public BuildRemotePropertiesPage() {
		super();
	}

	/**
	 * @see PreferencePage#createContents(Composite)
	 */
	public void createWidgets(Composite parent) {
		parentComposite = parent;
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
			}
		});
			

		// new connection button
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
		
		// Build subdirectory label and text box
		Label buildLocationLabel = new Label(composite, SWT.LEFT);
		buildLocationLabel.setText(Messages.BRPPage_BuildSubdirLabel);

		fBuildLocationText = new Text(composite, SWT.SINGLE | SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 1;
		gd.grabExcessHorizontalSpace = true;
		gd.widthHint = 250;
		fBuildLocationText.setLayoutData(gd);
		fBuildLocationText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				// MBSCustomPageManager.addPageProperty(REMOTE_SYNC_WIZARD_PAGE_ID,
				// PATH_PROPERTY, fLocationText.getText());
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
	}
	
	/**
	 * Handle new remote services selected
	 */
	private void handleServicesSelected() {
		int selectionIndex = fProviderCombo.getSelectionIndex();
		fSelectedProvider = fComboIndexToRemoteServicesProviderMap.get(selectionIndex);
		populateConnectionCombo(fConnectionCombo);
		updateNewConnectionButtonEnabled(fNewConnectionButton);
	}

	/**
	 * @param connectionCombo
	 */
	private void populateConnectionCombo(final Combo connectionCombo) {
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

	public void performDefaults() {
		super.performDefaults();
	}
	
	@Override
	public boolean performOk() {
		super.performOk();
		this.updateSettings(getCfg());
		for (ICConfigurationDescription desc : getCfgsReadOnly(getProject())) {
			IConfiguration config = getCfg(desc);
			if (config == null || config instanceof MultiConfiguration) {
				continue;
			}
			PageSettings settings = this.getSettings(config);
			if (settings == null) {
				continue;
			}
			if (this.isConfigAltered(config, settings)) {
				this.saveConfig(config, settings);
			}
		}
		
		return true;
	}
	
	private boolean isConfigAltered(IConfiguration config, PageSettings settings) {
		PageSettings systemSettings = this.loadSettings(config);
		// TODO: null return?
		if (settings.equals(systemSettings)) {
			return false;
		} else {
			return true;
		}
	}

	private void saveConfig(IConfiguration config, PageSettings settings) {
        // Change build path and save new configuration
        String buildPath = settings.rootLocation;
        if (buildPath.endsWith("/")) { //$NON-NLS-1$
                buildPath = buildPath.substring(0, buildPath.length() - 1);
        }
        if (settings.buildLocation.startsWith("/")) { //$NON-NLS-1$
                buildPath = buildPath + settings.buildLocation;
        } else {
                buildPath = buildPath + "/" + settings.buildLocation; //$NON-NLS-1$
        }
        config.getToolChain().getBuilder().setBuildPath(buildPath);
        ManagedBuildManager.saveBuildInfo(config.getOwner().getProject(), true);

        // Register with build configuration manager. This must be done after saving build info with ManagedBuildManager, as
        // the BuildConfigurationManager relies on the data being up-to-date.
        String syncProvider = BuildConfigurationManager.getBuildScenarioForBuildConfiguration(config).getSyncProvider();
        IRemoteConnection conn = fComboIndexToRemoteConnectionMap.get(settings.connectionIndex);
        BuildScenario buildScenario = new BuildScenario(syncProvider, conn, settings.rootLocation);
        BuildConfigurationManager.setBuildScenarioForBuildConfiguration(buildScenario, config);
        try {
                BuildConfigurationManager.saveConfigurationData();
        } catch (IOException e) {
                // TODO What to do in this case?
        }
	}
	
	private void checkConnection() {
		IRemoteUIConnectionManager mgr = getUIConnectionManager();
		if (mgr != null) {
			mgr.openConnectionWithProgress(fConnectionCombo.getShell(), null, fSelectedConnection);
		}
	}
	
	/**
	 * @return
	 */
	private IRemoteUIConnectionManager getUIConnectionManager() {
		IRemoteUIConnectionManager connectionManager = PTPRemoteUIPlugin.getDefault().getRemoteUIServices(fSelectedProvider)
				.getUIConnectionManager();
		return connectionManager;
	}
	
	/**
	 * Main function for updating values as needed. It is important that this fill in the proper current values, or else
	 * configuration information will be overwritten when user hits "Apply" or "OK".
	 */
	private void setValues(IConfiguration config) {
		// Disable for multi-configurations. Note that we set parentComposite to invisible, not the tab, because we want to reappear when
		// the configuration changes back to a single configuration.
		if (config instanceof IMultiConfiguration) {
			parentComposite.setVisible(false);
			return;
		}
		parentComposite.setVisible(true);		
		populateRemoteProviderCombo(fProviderCombo);
		PageSettings settings = getSettings(getCfg());
		if (settings == null) {
			// Log, this should never happen
			return;
		}

		// Note that provider selection populates the local connection map variables as well as the connection combo. Thus, the
		// provider must be selected first. (Calling select invokes the "handle" listeners for each combo.)
		fProviderCombo.select(settings.remoteServicesIndex);
		handleServicesSelected();
		fConnectionCombo.select(settings.connectionIndex);
		handleConnectionSelected();
		fRootLocationText.setText(settings.rootLocation);
		fBuildLocationText.setText(settings.buildLocation);
	}
	
	@Override
	protected void cfgChanged(ICConfigurationDescription cfg) {
		super.cfgChanged(cfg);
		// This method is called before createWidgets, so ignore this initial call.
		if (fWidgetsReady == false) {
			return;
		}
		// Update settings for previous configuration first
		this.updateSettings(fConfigBeforeSwitch);
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
	 * Return the page settings for the current configuration. If the configuration has not yet been accessed, load settings from the
	 * BuildConfigurationManager.
	 *
	 * @return settings for this page or null if this is a multiconfiguration or current configuration not available
	 */
	private PageSettings getSettings(IConfiguration config) {
		if (config == null || config instanceof MultiConfiguration) {
			return null;
		}

		if (!(fConfigToPageSettings.containsKey(config.getId()))) {
			PageSettings settings = this.loadSettings(config);
			if (settings == null) {
				// TODO: What to do in this case?
			}
			fConfigToPageSettings.put(config.getId(), settings);
		}
		
		return fConfigToPageSettings.get(config.getId());
	}
	
	/**
	 * Load settings from the BuildConfigurationManager for the given configuration
	 *
	 * @param config
	 * 				The configuration
	 * @return Configuration settings or null if config not found in BuildConfigurationManager
	 */
	private PageSettings loadSettings(IConfiguration config) {
		BuildScenario buildScenario = BuildConfigurationManager.getBuildScenarioForBuildConfiguration(config);
		if (buildScenario == null) {
			// Should never happen - need to log
			return null;
		}
		PageSettings settings = new PageSettings();
		Integer providerSelection = fComboRemoteServicesProviderToIndexMap.get(buildScenario.getRemoteConnection().
																											getRemoteServices());
		if (providerSelection == null) {
			providerSelection = new Integer(0);
		}
		settings.remoteServicesIndex = providerSelection;

		Integer connectionSelection = fComboRemoteConnectionToIndexMap.get(buildScenario.getRemoteConnection());
		if (connectionSelection == null) {
			connectionSelection = new Integer(0);
		}
		settings.connectionIndex = connectionSelection;

		settings.rootLocation = buildScenario.getLocation();

		String buildSubDir = config.getToolChain().getBuilder().getBuildPath();
		// TODO: Check that it really is a subdirectory, but it is not clear what to do if it is not.
		buildSubDir = buildSubDir.substring(buildScenario.getLocation().length());
		settings.buildLocation = buildSubDir;

		return settings;
	}

	private void updateSettings(IConfiguration config) {
		if (config == null || config instanceof MultiConfiguration) {
			return;
		}

		PageSettings settings = new PageSettings();
		settings.remoteServicesIndex = fProviderCombo.getSelectionIndex();
		settings.connectionIndex = fConnectionCombo.getSelectionIndex();
		settings.rootLocation = fRootLocationText.getText();
		settings.buildLocation = fBuildLocationText.getText();
		
		fConfigToPageSettings.put(config.getId(), settings);
	}
}