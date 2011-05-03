package org.eclipse.ptp.rdt.sync.ui.properties;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.core.settings.model.ICResourceDescription;
import org.eclipse.cdt.managedbuilder.core.IMultiConfiguration;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.ui.properties.AbstractCBuildPropertyTab;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.ptp.rdt.sync.core.BuildConfigurationManager;
import org.eclipse.ptp.rdt.sync.core.BuildScenario;
import org.eclipse.ptp.rdt.sync.core.resources.RemoteSyncNature;
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

public class BuildRemotePropertiesTab extends AbstractCBuildPropertyTab {
	// Button and label names

	private IRemoteConnection fSelectedConnection;
	// TODO: Support other providers
	private IRemoteServices fSelectedProvider;

	private final Map<Integer, IRemoteServices> fComboIndexToRemoteServicesProviderMap = new HashMap<Integer, IRemoteServices>();
	private final Map<IRemoteServices, Integer> fComboRemoteServicesProviderToIndexMap = new HashMap<IRemoteServices, Integer>();
	private final Map<Integer, IRemoteConnection> fComboIndexToRemoteConnectionMap = new HashMap<Integer, IRemoteConnection>();
	private final Map<IRemoteConnection, Integer> fComboRemoteConnectionToIndexMap = new HashMap<IRemoteConnection, Integer>();

	private Button fBrowseButton;
	private Button fNewConnectionButton;
	private Combo fProviderCombo;
	private Combo fConnectionCombo;
	private Text fRootLocationText;
	private Text fBuildLocationText;
	private Composite parentComposite;

	/**
	 * @see PreferencePage#createContents(Composite)
	 */
	@Override
	protected void createControls(Composite parent) {
		if (!(isApplicable())) {
			return;
		}
		parentComposite = parent;
		super.createControls(parent);
		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		usercomp.setLayout(layout);
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		usercomp.setLayoutData(gd);

		// Label for "Provider:"
		Label providerLabel = new Label(usercomp, SWT.LEFT);
		providerLabel.setText(Messages.BRPTab_RemoteProviderLabel);

		// combo for providers
		fProviderCombo = new Combo(usercomp, SWT.DROP_DOWN | SWT.READ_ONLY);
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
		Label connectionLabel = new Label(usercomp, SWT.LEFT);
		connectionLabel.setText(Messages.BRPTab_ConnectionLabel);

		// combo for providers
		fConnectionCombo = new Combo(usercomp, SWT.DROP_DOWN | SWT.READ_ONLY);
		// set layout to grab horizontal space
		fConnectionCombo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		fConnectionCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				handleConnectionSelected();
			}
		});

		// new connection button
		fNewConnectionButton = new Button(usercomp, SWT.PUSH);
		fNewConnectionButton.setText(Messages.BRPTab_ConnectionLabel);
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

		Label rootLocationLabel = new Label(usercomp, SWT.LEFT);
		rootLocationLabel.setText(Messages.BRPTab_RootLocation);

		fRootLocationText = new Text(usercomp, SWT.SINGLE | SWT.BORDER);
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
		fBrowseButton = new Button(usercomp, SWT.PUSH);
		fBrowseButton.setText(Messages.BRPTab_BrowseButton);
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
		Label buildLocationLabel = new Label(usercomp, SWT.LEFT);
		buildLocationLabel.setText(Messages.BRPTab_BuildSubdirLabel);

		fBuildLocationText = new Text(usercomp, SWT.SINGLE | SWT.BORDER);
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
		
		this.setValues();
	}

	/**
	 * Store remote location information for the selected build configuration
	 */
	public void performOK() {
		if (!(isApplicable())) {
			return;
		}
		// For now, do nothing for multi-configurations
		if (getCfg() instanceof IMultiConfiguration) {
			return;
		}
		// Change build path and save new configuration
		String buildPath = fRootLocationText.getText();
		if (buildPath.endsWith("/")) { //$NON-NLS-1$
			buildPath = buildPath.substring(0, buildPath.length() - 1);
		}
		if (fBuildLocationText.getText().startsWith("/")) { //$NON-NLS-1$
			buildPath = buildPath + fBuildLocationText.getText();
		} else {
			buildPath = buildPath + "/" + fBuildLocationText.getText(); //$NON-NLS-1$
		}
		getCfg().getToolChain().getBuilder().setBuildPath(buildPath);
		ManagedBuildManager.saveBuildInfo(getCfg().getOwner().getProject(), true);
		
		// Register with build configuration manager. This must be done after saving build info with ManagedBuildManager, as
		// the BuildConfigurationManager relies on the data being up-to-date.
		String syncProvider = BuildConfigurationManager.getBuildScenarioForBuildConfiguration(getCfg()).getSyncProvider();
		BuildScenario buildScenario = new BuildScenario(syncProvider, fSelectedConnection, fRootLocationText.getText());
		BuildConfigurationManager.setBuildScenarioForBuildConfiguration(buildScenario, getCfg());
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

	/**
	 * Just call "performOK". Parameters are ignored.
	 */
	@Override
	public void performApply(ICResourceDescription src, ICResourceDescription dst) {
		performOK();
	}

	@Override
	protected void performDefaults() {
		// TODO Auto-generated method stub
		
	}

	/**
	 * Main functionality inside "setValues". Parameter is ignored.
	 */
	@Override
	public void updateData(ICResourceDescription cfg) {
		if (!isApplicable()) {
			return;
		}
		this.setValues();
		parentComposite.update();
	}
	
	/**
	 * Main function for updating values as needed. It is important that this fill in the proper current values, or else
	 * configuration information will be overwritten when user hits "Apply" or "OK".
	 */
	private void setValues() {
		// Disable for multi-configurations. Note that we set usercomp to invisible, not the tab, because we want to reappear when
		// the configuration changes back to a single configuration. 
		if (getCfg() instanceof IMultiConfiguration) {
			usercomp.setVisible(false);
			return;
		} else {
			usercomp.setVisible(true);
		}
		populateRemoteProviderCombo(fProviderCombo);
		BuildScenario buildScenario = BuildConfigurationManager.getBuildScenarioForBuildConfiguration(getCfg());
		
		// Note that provider selection populates the local connection map variables as well as the connection combo. Thus, the
		// provider must be selected first. (Calling select invokes the "handle" listeners for each combo.)
		if (buildScenario != null) {
			Integer providerSelection = fComboRemoteServicesProviderToIndexMap.get(buildScenario.getRemoteConnection().getRemoteServices());
			if (providerSelection == null) {
				providerSelection = new Integer(0);
			}
			fProviderCombo.select(providerSelection.intValue());
			handleServicesSelected();

			Integer connectionSelection = fComboRemoteConnectionToIndexMap.get(buildScenario.getRemoteConnection());
			if (connectionSelection == null) {
				connectionSelection = new Integer(0);
			}
			fConnectionCombo.select(connectionSelection);
			handleConnectionSelected();

			fRootLocationText.setText(buildScenario.getLocation());
		} else {
			fProviderCombo.select(0);
			fConnectionCombo.select(0);
			fRootLocationText.setText(""); //$NON-NLS-1$
		}
		
		String buildSubDir = getCfg().getToolChain().getBuilder().getBuildPath();
		// TODO: Check that it really is a subdirectory, but it is not clear what to do if it is not.
		buildSubDir = buildSubDir.substring(buildScenario.getLocation().length());
		fBuildLocationText.setText(buildSubDir);
	}

	@Override
	protected void updateButtons() {
		if (!(isApplicable())) {
			return;
		}
		// TODO Auto-generated method stub
		
	}
	
	private boolean isApplicable() {
		IProject project = getCfg().getOwner().getProject();
		if (!(RemoteSyncNature.hasNature(project))) {
			this.setVisible(false);
			return false;
		}
		return true;
	}
}