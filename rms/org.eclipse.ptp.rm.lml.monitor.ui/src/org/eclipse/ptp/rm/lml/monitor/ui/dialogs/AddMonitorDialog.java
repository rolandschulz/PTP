/*******************************************************************************
 * Copyright (c) 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.rm.lml.monitor.ui.dialogs;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.ui.widgets.RemoteConnectionWidget;
import org.eclipse.ptp.rm.jaxb.control.LaunchControllerManager;
import org.eclipse.ptp.rm.lml.monitor.core.MonitorControlManager;
import org.eclipse.ptp.rm.lml.monitor.ui.messages.Messages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

public class AddMonitorDialog extends TitleAreaDialog {

	private Combo fConfigNameCombo;
	private RemoteConnectionWidget fRemoteConnectionWidget;
	private String fConfigName;
	private IRemoteConnection fRemoteConnection;

	public AddMonitorDialog(Shell parentShell) {
		super(parentShell);
	}

	/**
	 * Get the remote connection selected in the dialog, or null if no
	 * connection selected
	 * 
	 * @return remote connection, or null
	 */
	public IRemoteConnection getRemoteConnection() {
		return fRemoteConnection;
	}

	/**
	 * Get the configuration name selected in the dialog, or null if no configuration
	 * selected
	 * 
	 * @return system type or null
	 */
	public String getConfigurationName() {
		return fConfigName;
	}

	private void updateEnablement() {
		Boolean valid = false;
		fRemoteConnection = fRemoteConnectionWidget.getConnection();
		if (fRemoteConnection != null) {
			try {
				valid = (LaunchControllerManager.getInstance().getLaunchController(fRemoteConnection.getRemoteServices().getId(),
						fRemoteConnection.getName(), getConfigurationName()) != null);
			} catch (CoreException e) {
			}
		}
		fRemoteConnectionWidget.setEnabled(fConfigNameCombo.getSelectionIndex() > 0);
		Button button = getButton(IDialogConstants.OK_ID);
		if (button != null) {
			button.setEnabled(valid && fConfigNameCombo.getSelectionIndex() > 0 && fRemoteConnectionWidget.getConnection() != null);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.dialogs.TitleAreaDialog#createContents(org.eclipse.
	 * swt.widgets.Composite)
	 */
	@Override
	protected Control createContents(Composite parent) {
		Control control = super.createContents(parent);
		updateEnablement();
		return control;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.dialogs.TitleAreaDialog#createDialogArea(org.eclipse
	 * .swt.widgets.Composite)
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		getShell().setText(Messages.AddMonitorDialog_Add_Monitor);
		setTitle(Messages.AddMonitorDialog_Select_new_monitor);
		setMessage(Messages.AddMonitorDialog_Choose_a_monitor_type);

		Composite composite = new Composite(parent, SWT.NULL);
		GridData gd = new GridData(GridData.FILL_BOTH);
		GridLayout topLayout = new GridLayout();
		topLayout.numColumns = 2;
		composite.setLayout(topLayout);
		composite.setLayoutData(gd);

		new Label(composite, SWT.NONE).setText(Messages.AddMonitorDialog_Monitor_Type);

		fConfigNameCombo = new Combo(composite, SWT.READ_ONLY);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		fConfigNameCombo.setLayoutData(gd);
		fConfigNameCombo.setItems(MonitorControlManager.getConfigurationNames());
		fConfigNameCombo.add(Messages.AddMonitorDialog_Please_select_a_monitor_type, 0);
		fConfigNameCombo.select(0);
		fConfigNameCombo.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				if (fConfigNameCombo.getSelectionIndex() > 0) {
					fConfigName = fConfigNameCombo.getItem(fConfigNameCombo.getSelectionIndex());
				}
				updateEnablement();
			}
		});

		fRemoteConnectionWidget = new RemoteConnectionWidget(composite, SWT.NONE, null, null);
		fRemoteConnectionWidget.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
		fRemoteConnectionWidget.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				updateEnablement();
			}
		});
		fRemoteConnectionWidget.setEnabled(false);

		Dialog.applyDialogFont(composite);

		return composite;
	}
}
