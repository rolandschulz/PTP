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

package org.eclipse.ptp.rdt.sync.git.ui;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.wizard.IWizardContainer;
import org.eclipse.ptp.rdt.sync.core.serviceproviders.ISyncServiceProvider;
import org.eclipse.ptp.rdt.sync.core.services.IRemoteSyncServiceConstants;
import org.eclipse.ptp.rdt.sync.git.ui.messages.Messages;
import org.eclipse.ptp.rdt.sync.git.core.GitServiceProvider;
import org.eclipse.ptp.rdt.sync.ui.ISynchronizeParticipant;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.IRemoteFileManager;
import org.eclipse.ptp.remote.core.IRemoteServices;
import org.eclipse.ptp.remote.ui.IRemoteUIConnectionManager;
import org.eclipse.ptp.remote.ui.IRemoteUIConstants;
import org.eclipse.ptp.remote.ui.IRemoteUIFileManager;
import org.eclipse.ptp.remote.ui.IRemoteUIServices;
import org.eclipse.ptp.remote.ui.PTPRemoteUIPlugin;
import org.eclipse.ptp.services.core.IService;
import org.eclipse.ptp.services.core.ServiceModelManager;
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

/**
 * Launches a dialog that configures a remote sync target with OK and Cancel
 * buttons. Also has a text field to allow the name of the configuration to be
 * changed.
 */
public class GitParticipant implements ISynchronizeParticipant {
	private static final String FILE_SCHEME = "file"; //$NON-NLS-1$

	// private IServiceConfiguration fConfig;
	private IRemoteConnection fSelectedConnection;
	private IRemoteServices fSelectedProvider;
	// private final IRunnableContext fContext;
	private String fProjectName = ""; //$NON-NLS-1$

	private final Map<Integer, IRemoteServices> fComboIndexToRemoteServicesProviderMap = new HashMap<Integer, IRemoteServices>();
	private final Map<Integer, IRemoteConnection> fComboIndexToRemoteConnectionMap = new HashMap<Integer, IRemoteConnection>();

//	private Control fDialogControl;
//	private Point fDialogSize;
//	private Text fNameText;
	private Button fBrowseButton;
	private Button fNewConnectionButton;
	private Combo fProviderCombo;
	private Combo fConnectionCombo;
	private Text fLocationText;

	private IWizardContainer container;
	
	// If false, automatically select "Remote Tools" provider instead of letting the user select the provider.
	private boolean showProviderCombo = false;

	/**
	 * Attempt to open a connection.
	 */
	private void checkConnection() {
		IRemoteUIConnectionManager mgr = getUIConnectionManager();
		if (mgr != null) {
			mgr.openConnectionWithProgress(fConnectionCombo.getShell(), null, fSelectedConnection);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rdt.sync.ui.ISynchronizeParticipant#createConfigurationArea
	 * (org.eclipse.swt.widgets.Composite,
	 * org.eclipse.jface.operation.IRunnableContext)
	 */
	public void createConfigurationArea(Composite parent, IRunnableContext context) {
		this.container = (IWizardContainer)context;
		final Composite configArea = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		configArea.setLayout(layout);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		configArea.setLayoutData(gd);

		if (showProviderCombo) {
			// Label for "Provider:"
			Label providerLabel = new Label(configArea, SWT.LEFT);
			providerLabel.setText(Messages.GitParticipant_remoteProvider);

			// combo for providers
			fProviderCombo = new Combo(configArea, SWT.DROP_DOWN | SWT.READ_ONLY);
			gd = new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false);
			gd.horizontalSpan = 2;
			fProviderCombo.setLayoutData(gd);
			fProviderCombo.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					handleServicesSelected();
				}
			});
		}

		// attempt to restore settings from saved state
		// IRemoteServices providerSelected = fProvider.getRemoteServices();

		// populate the combo with a list of providers
		IRemoteServices[] providers = PTPRemoteUIPlugin.getDefault().getRemoteServices(context);
		int toSelect = 0;

		for (int k = 0; k < providers.length; k++) {
			if (showProviderCombo) {
				fProviderCombo.add(providers[k].getName(), k);
			}
			if  (providers[k].getName().equals("Remote Tools")) { //$NON-NLS-1$
				toSelect = k;
			}
			fComboIndexToRemoteServicesProviderMap.put(k, providers[k]);
		}

		if (showProviderCombo) {
			fProviderCombo.select(toSelect);
		}
		fSelectedProvider = fComboIndexToRemoteServicesProviderMap.get(toSelect);

		// connection combo
		// Label for "Connection:"
		Label connectionLabel = new Label(configArea, SWT.LEFT);
		connectionLabel.setText(Messages.GitParticipant_connection);

		// combo for providers
		fConnectionCombo = new Combo(configArea, SWT.DROP_DOWN | SWT.READ_ONLY);
		// set layout to grab horizontal space
		fConnectionCombo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		fConnectionCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				handleConnectionSelected();
			}
		});

		// populate the combo with a list of providers
		populateConnectionCombo(fConnectionCombo);

		// new connection button
		fNewConnectionButton = new Button(configArea, SWT.PUSH);
		fNewConnectionButton.setText(Messages.GitParticipant_new);
		updateNewConnectionButtonEnabled(fNewConnectionButton);
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

		Label locationLabel = new Label(configArea, SWT.LEFT);
		locationLabel.setText(Messages.GitParticipant_location);

		fLocationText = new Text(configArea, SWT.SINGLE | SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 1;
		gd.grabExcessHorizontalSpace = true;
		gd.widthHint = 250;
		fLocationText.setLayoutData(gd);
		fLocationText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				// MBSCustomPageManager.addPageProperty(REMOTE_SYNC_WIZARD_PAGE_ID,
				// PATH_PROPERTY, fLocationText.getText());
				update();
			}
		});
		handleConnectionSelected();

		// new connection button
		fBrowseButton = new Button(configArea, SWT.PUSH);
		fBrowseButton.setText(Messages.GitParticipant_browse);
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
								String correctPath = fLocationText.getText();
								String selectedPath = fileMgr.browseDirectory(
										fLocationText.getShell(),
										"Project Location (" + fSelectedConnection.getName() + ")", correctPath, IRemoteUIConstants.NONE); //$NON-NLS-1$ //$NON-NLS-2$
								if (selectedPath != null) {
									fLocationText.setText(selectedPath);
								}
							}
						}
					}
				}
			}
		});
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
//		String projectName = ""; //$NON-NLS-1$
		// IWizardPage page = getWizard().getStartingPage();
		// if (page instanceof CDTMainWizardPage) {
		// projectName = ((CDTMainWizardPage) page).getProjectName();
		// }
		if (fSelectedConnection != null && fSelectedConnection.isOpen()) {
			IRemoteFileManager fileMgr = fSelectedProvider.getFileManager(fSelectedConnection);
			URI defaultURI = fileMgr.toURI(fSelectedConnection.getWorkingDirectory());

			// Handle files specially. Assume a file if there is no project to
			// query
			if (defaultURI != null && defaultURI.getScheme().equals(FILE_SCHEME)) {
				return Platform.getLocation().append(fProjectName).toString();
			}
			if (defaultURI == null) {
				return ""; //$NON-NLS-1$
			}
			return new Path(defaultURI.getPath()).append(fProjectName).toString();
		}
		return ""; //$NON-NLS-1$
	}
	
	/** 
	 * @see ISynchronizeParticipant#getErrorMessage()
	 */
	public String getErrorMessage() {
		if (fSelectedProvider == null) 
			return Messages.GitParticipant_0;
		if (fSelectedConnection == null) 
			return Messages.GitParticipant_1;
		if (fLocationText.getText().length() == 0 ) 
			return Messages.GitParticipant_2;
		IRemoteFileManager fileManager = fSelectedProvider.getFileManager(fSelectedConnection);
		if ( fileManager.toURI(fLocationText.getText()) == null) 
			return Messages.GitParticipant_3;
		// should we check permissions of: fileManager.getResource(fLocationText.getText()).getParent() ?
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rdt.sync.ui.ISynchronizeParticipant#getProvider(org.eclipse.core.resources.IProject)
	 */
	public ISyncServiceProvider getProvider(IProject project) {
		ServiceModelManager smm = ServiceModelManager.getInstance();
		IService syncService = smm.getService(IRemoteSyncServiceConstants.SERVICE_SYNC);
		GitServiceProvider provider = (GitServiceProvider) smm.getServiceProvider(syncService
				.getProviderDescriptor(GitServiceProvider.ID));
		provider.setLocation(fLocationText.getText());
		provider.setRemoteConnection(fSelectedConnection);
		provider.setRemoteServices(fSelectedProvider);
		return provider;
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
		fLocationText.setText(getDefaultPathDisplayString());
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
		handleConnectionSelected();
		update();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rdt.sync.ui.ISynchronizeParticipant#isConfigComplete()
	 */
	public boolean isConfigComplete() {
		return getErrorMessage() == null;
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
		}

		connectionCombo.select(0);
		fSelectedConnection = fComboIndexToRemoteConnectionMap.get(0);
	}

	private void update() {
		container.updateMessage();
		// updateButtons() may fail if current page is null. This can happen if update() is called during wizard/page creation. 
		if (container.getCurrentPage() == null) {
			return;
		}
		container.updateButtons();
	}

	/**
	 * @param button
	 */
	private void updateNewConnectionButtonEnabled(Button button) {
		IRemoteUIConnectionManager connectionManager = getUIConnectionManager();
		button.setEnabled(connectionManager != null);
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rdt.sync.ui.ISynchronizeParticipant#setProjectName(String projectName)
	 */
	public void setProjectName(String projectName) {
		fProjectName = projectName;
		fLocationText.setText(getDefaultPathDisplayString());
	}
}
