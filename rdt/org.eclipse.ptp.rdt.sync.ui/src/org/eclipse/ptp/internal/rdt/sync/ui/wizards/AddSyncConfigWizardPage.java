/*******************************************************************************
 * Copyright (c) 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.internal.rdt.sync.ui.wizards;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ptp.internal.rdt.sync.core.services.SynchronizeServiceRegistry;
import org.eclipse.ptp.internal.rdt.sync.ui.messages.Messages;
import org.eclipse.ptp.rdt.sync.core.SyncConfig;
import org.eclipse.ptp.rdt.sync.core.SyncConfigManager;
import org.eclipse.ptp.rdt.sync.core.services.ISynchronizeServiceDescriptor;
import org.eclipse.ptp.remote.core.IRemoteConnection;
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
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * @noextend This class is not intended to be subclassed by clients.
 */
public class AddSyncConfigWizardPage extends WizardPage {
	private Text fProjectLocationText;
	private Text fConfigNameText;

	private RemoteConnectionWidget fRemoteConnectioWidget;
	private Combo fSyncProviderCombo;

	private String fConfigName;
	private String fProjectLocation;
	private IRemoteConnection fSelectedConnection;
	private ISynchronizeServiceDescriptor[] fProviders;
	private ISynchronizeServiceDescriptor fSyncProvider;

	private final IProject fProject;

	public AddSyncConfigWizardPage(String pageName, IProject project) {
		super(pageName);
		fProject = project;
	}

	private void checkConnection() {
		IRemoteUIConnectionManager mgr = getUIConnectionManager();
		if (mgr != null) {
			mgr.openConnectionWithProgress(fRemoteConnectioWidget.getShell(), null, fSelectedConnection);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createControl(Composite parent) {
		final Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(3, false);
		composite.setLayout(layout);
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		composite.setLayoutData(gd);

		Label nameLabel = new Label(composite, SWT.NONE);
		nameLabel.setText(Messages.AddSyncConfigWizardPage_Configuration_name);

		fConfigNameText = new Text(composite, SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		fConfigNameText.setLayoutData(gd);
		fConfigNameText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				fConfigName = fConfigNameText.getText().trim();
				if (fConfigName.equals("")) { //$NON-NLS-1$
					fConfigName = null;
				}
				setPageComplete(validatePage());
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
				fSelectedConnection = fRemoteConnectioWidget.getConnection();
				setPageComplete(validatePage());
			}
		});

		Label projectLocationLabel = new Label(composite, SWT.LEFT);
		projectLocationLabel.setText(Messages.AddSyncConfigWizardPage_Project_location);

		fProjectLocationText = new Text(composite, SWT.SINGLE | SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 1;
		gd.grabExcessHorizontalSpace = true;
		gd.widthHint = 250;
		fProjectLocationText.setLayoutData(gd);
		fProjectLocationText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				fProjectLocation = fProjectLocationText.getText().trim();
				if (fProjectLocation.equals("")) { //$NON-NLS-1$
					fProjectLocation = null;
				}
				setPageComplete(validatePage());
			}
		});

		// browse button
		Button fBrowseButton = new Button(composite, SWT.PUSH);
		fBrowseButton.setText(Messages.AddSyncConfigWizardPage_Browse);
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
								String correctPath = fProjectLocationText.getText();
								String selectedPath = fileMgr.browseDirectory(fProjectLocationText.getShell(),
										NLS.bind(Messages.AddSyncConfigWizardPage_browse_message, fSelectedConnection.getName()),
										correctPath, IRemoteUIConstants.NONE);
								if (selectedPath != null) {
									fProjectLocationText.setText(selectedPath);
								}
							}
						}
					}
				}
			}
		});

		Label syncProviderLabel = new Label(composite, SWT.LEFT);
		syncProviderLabel.setText(Messages.AddSyncConfigWizardPage_Synchronize_provider);

		fSyncProviderCombo = new Combo(composite, SWT.DROP_DOWN | SWT.READ_ONLY);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		fSyncProviderCombo.setLayoutData(gd);
		fSyncProviderCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				int index = fSyncProviderCombo.getSelectionIndex();
				if (index >= 0) {
					fSyncProvider = fProviders[index];
				} else {
					fSyncProvider = null;
				}
				setPageComplete(validatePage());
			}
		});

		initializeSyncProviders();
		fSyncProviderCombo.select(0);
		fSyncProvider = fProviders[0];

		setControl(composite);
		setPageComplete(false);
	}

	/**
	 * Get the sync config defined by this wizard page. This will only be valid if the page is complete.
	 * 
	 * @return sync config
	 */
	public SyncConfig getSyncConfig() {
		if (isPageComplete()) {
			SyncConfig config = SyncConfigManager.newConfig(fConfigName, fSyncProvider.getId(), fSelectedConnection.getName(),
					fSelectedConnection.getRemoteServices().getId(), fProjectLocation);
			config.setProject(fProject);
			return config;
		}
		return null;
	}

	private IRemoteUIConnectionManager getUIConnectionManager() {
		if (fSelectedConnection == null) {
			return null;
		}
		IRemoteUIConnectionManager connectionManager = RemoteUIServices
				.getRemoteUIServices(fSelectedConnection.getRemoteServices()).getUIConnectionManager();
		return connectionManager;
	}

	private void initializeSyncProviders() {
		fProviders = SynchronizeServiceRegistry.getSynchronizeServiceDescriptors();
		for (ISynchronizeServiceDescriptor provider : fProviders) {
			fSyncProviderCombo.add(provider.getName());
		}
	}

	private boolean validatePage() {
		if (fConfigName != null) {
			if (SyncConfigManager.getConfig(fProject, fConfigName) != null) {
				setErrorMessage(Messages.AddSyncConfigWizardPage_Name_already_exists);
				return false;
			}
		} else {
			setErrorMessage(Messages.AddSyncConfigWizardPage_Name_must_be_specified);
			return false;
		}
		if (fProjectLocation == null || fSelectedConnection == null) {
			setErrorMessage(Messages.AddSyncConfigWizardPage_Connection_and_location_must_be_sepecified);
			return false;
		}
		if (fSyncProvider == null) {
			setErrorMessage(Messages.AddSyncConfigWizardPage_Provider_must_be_selected);
			return false;
		}
		setErrorMessage(null);
		return true;
	}
}