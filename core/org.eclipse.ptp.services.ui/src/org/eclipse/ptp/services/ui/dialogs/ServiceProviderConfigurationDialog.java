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
package org.eclipse.ptp.services.ui.dialogs;


import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.ptp.services.core.IServiceConfiguration;
import org.eclipse.ptp.services.ui.messages.Messages;
import org.eclipse.ptp.services.ui.widgets.ServiceProviderConfigurationWidget;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

/**
 * Launches a dialog that contains the ServiceProviderConfigurationWidget
 * with OK and Cancel buttons.
 */
public class ServiceProviderConfigurationDialog extends Dialog {

	private ServiceProviderConfigurationWidget fServiceModelWidget;
	private IServiceConfiguration fConfig;
	private Control fDialogControl;
	private Point fDialogSize;
	
	public ServiceProviderConfigurationDialog(IShellProvider parentShell, IServiceConfiguration config) {
		super(parentShell);
		setShellStyle(getShellStyle() | SWT.RESIZE);
		fConfig = config;
	}

	public ServiceProviderConfigurationDialog(Shell parentShell, IServiceConfiguration config) {
		super(parentShell);
		setShellStyle(getShellStyle() | SWT.RESIZE);
		fConfig = config;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
	 */
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(Messages.NewServiceModelWidgetDialog_title);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		final Composite dialogArea = (Composite) super.createDialogArea(parent);
		
		fServiceModelWidget = new ServiceProviderConfigurationWidget(dialogArea, SWT.NONE);
		fServiceModelWidget.setServiceConfiguration(fConfig);
 		GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
 		data.minimumHeight = 400;
 		fServiceModelWidget.setLayoutData(data);
 		fServiceModelWidget.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				resizeDialog();
			}
 		});
 		return fServiceModelWidget;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#createContents(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createContents(Composite parent) {
		fDialogControl = super.createContents(parent);
		fDialogSize = fDialogControl.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		return fDialogControl;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	@Override
	protected void okPressed() {
		fServiceModelWidget.applyChangesToConfiguration();
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
