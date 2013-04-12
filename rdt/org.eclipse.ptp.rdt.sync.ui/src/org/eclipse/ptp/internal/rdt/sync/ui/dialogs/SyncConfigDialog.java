/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Mike Kucera (IBM Corporation) - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.internal.rdt.sync.ui.dialogs;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.ptp.internal.rdt.sync.ui.messages.Messages;
import org.eclipse.ptp.rdt.sync.core.SyncConfig;
import org.eclipse.ptp.rdt.sync.core.services.ISynchronizeServiceDescriptor;
import org.eclipse.ptp.rdt.sync.core.services.SynchronizeServiceRegistry;
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
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * Launches a dialog that contains the ServiceProviderConfigurationWidget
 * with OK and Cancel buttons. Also has a text field to allow the name
 * of the configuration to be changed.
 */
public class SyncConfigDialog extends Dialog {

	private Control fDialogControl;
	private Point fDialogSize;
	private Text fProjectLocationText;
	private Text fConfigNameText;

	private RemoteConnectionWidget fRemoteConnectioWidget;
	private Combo fSyncProviderCombo;

	private String fConfigName;
	private String fProjectLocation;
	private String fSyncProvider;
	private IRemoteConnection fSelectedConnection;
	private SyncConfig fSyncConfig;
	private ISynchronizeServiceDescriptor[] fProviders;

	public SyncConfigDialog(Shell parentShell) {
		super(parentShell);
		setShellStyle(getShellStyle() | SWT.RESIZE);
	}

	public SyncConfigDialog(Shell parentShell, SyncConfig config) {
		super(parentShell);
		setShellStyle(getShellStyle() | SWT.RESIZE);
		fSyncConfig = config;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
	 */
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(Messages.SyncConfigDialog_Sync_Config);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		final Composite dialogArea = (Composite) super.createDialogArea(parent);

		final Composite composite = new Composite(dialogArea, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		layout.marginHeight = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
		layout.marginWidth = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
		layout.verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
		layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
		composite.setLayout(layout);
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		composite.setLayoutData(gd);

		Label nameLabel = new Label(composite, SWT.NONE);
		nameLabel.setText(Messages.SyncConfigDialog_Configuration_name);

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
				updateControls();
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
				updateControls();
			}
		});

		Label projectLocationLabel = new Label(composite, SWT.LEFT);
		projectLocationLabel.setText(Messages.SyncConfigDialog_Project_location);

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
				updateControls();
			}
		});

		// browse button
		Button fBrowseButton = new Button(composite, SWT.PUSH);
		fBrowseButton.setText(Messages.SyncConfigDialog_Browse);
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
								String selectedPath = fileMgr.browseDirectory(
										fProjectLocationText.getShell(),
										"Project Location (" + fSelectedConnection.getName() + ")", correctPath, IRemoteUIConstants.NONE); //$NON-NLS-1$ //$NON-NLS-2$
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
		syncProviderLabel.setText(Messages.SyncConfigDialog_Synchronize_provider);

		fSyncProviderCombo = new Combo(composite, SWT.DROP_DOWN | SWT.READ_ONLY);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		fSyncProviderCombo.setLayoutData(gd);
		fSyncProviderCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				fSyncProvider = fSyncProviderCombo.getText();
				updateControls();
			}
		});

		initializeSyncProviders();
		fSyncProviderCombo.select(0);
		fSyncProvider = fSyncProviderCombo.getText();

		return dialogArea;
	}

	private void initializeSyncProviders() {
		fProviders = SynchronizeServiceRegistry.getSynchronizeServiceDescriptors();
		for (ISynchronizeServiceDescriptor provider : fProviders) {
			fSyncProviderCombo.add(provider.getName());
		}
	}

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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.Dialog#createContents(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createContents(Composite parent) {
		fDialogControl = super.createContents(parent);
		fDialogSize = fDialogControl.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		updateControls();
		return fDialogControl;
	}

	private void updateControls() {
		Button okButton = getButton(IDialogConstants.OK_ID);
		boolean enabled = (fConfigName != null && fProjectLocation != null && fSelectedConnection != null && fSyncProvider != null);
		okButton.setEnabled(enabled);
	}

	public SyncConfig getSyncConfig() {
		return fSyncConfig;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	@Override
	protected void okPressed() {
		IRemoteConnection conn = fRemoteConnectioWidget.getConnection();
		ISynchronizeServiceDescriptor provider = fProviders[fSyncProviderCombo.getSelectionIndex()];
		fSyncConfig = new SyncConfig(fConfigNameText.getText().trim(), provider.getId(), conn.getName(), conn.getRemoteServices()
				.getId(), fProjectLocationText.getText().trim());
		super.okPressed();
	}

	private void resizeDialog() {
		Point p = fDialogControl.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		if (p.x > fDialogSize.x || p.y > fDialogSize.y) {
			getShell().setSize(p);
			fDialogSize = p;
		}
	}
}
