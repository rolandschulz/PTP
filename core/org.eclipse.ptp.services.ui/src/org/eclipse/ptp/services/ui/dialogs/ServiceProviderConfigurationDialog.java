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
import org.eclipse.ptp.internal.services.ui.messages.Messages;
import org.eclipse.ptp.services.core.IServiceConfiguration;
import org.eclipse.ptp.services.ui.widgets.ServiceProviderConfigurationWidget;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
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
public class ServiceProviderConfigurationDialog extends Dialog {

	private ServiceProviderConfigurationWidget fServiceModelWidget;
	private IServiceConfiguration fConfig;
	private Control fDialogControl;
	private Point fDialogSize;
	private Text fNameText;
	
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
		newShell.setText(Messages.ServiceProviderConfigurationDialog_title);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		final Composite dialogArea = (Composite) super.createDialogArea(parent);
		
		final Composite textArea = new Composite(dialogArea, SWT.NONE);
		GridLayout textLayout = new GridLayout(2, false);
		textLayout.marginWidth = 0;
		textArea.setLayout(textLayout);
		GridData textAreaData = new GridData(SWT.FILL, SWT.FILL, true, false);
		textArea.setLayoutData(textAreaData);
		final Label nameLabel = new Label(textArea, SWT.NONE);
		nameLabel.setText(Messages.ServiceProviderConfigurationDialog_0);
		fNameText = new Text(textArea, SWT.BORDER);
		fNameText.setText(fConfig.getName());
		GridData textData = new GridData(SWT.FILL, SWT.FILL, true, true);
		fNameText.setLayoutData(textData);
		
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
		String name = fNameText.getText();
		if (!name.equals("")) { //$NON-NLS-1$
			fConfig.setName(name);
		}
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
