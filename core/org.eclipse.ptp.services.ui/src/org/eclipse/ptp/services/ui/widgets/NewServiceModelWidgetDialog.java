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
package org.eclipse.ptp.services.ui.widgets;


import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.window.IShellProvider;

import org.eclipse.ptp.services.core.IServiceConfiguration;
import org.eclipse.ptp.services.ui.messages.Messages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

/**
 * Launches a dialog that contains the NewServiceModelWidget
 * with OK and Cancel buttons.
 */
public class NewServiceModelWidgetDialog extends Dialog {

	private NewServiceModelWidget serviceModelWidget;
	private IServiceConfiguration config;
	
	
	public NewServiceModelWidgetDialog(IShellProvider parentShell, IServiceConfiguration config) {
		super(parentShell);
		this.config = config;
	}

	public NewServiceModelWidgetDialog(Shell parentShell, IServiceConfiguration config) {
		super(parentShell);
		this.config = config;
	}
	
	
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(Messages.NewServiceModelWidgetDialog_title);
	}
	
	
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite dialogArea = (Composite) super.createDialogArea(parent);
		
		serviceModelWidget = new NewServiceModelWidget(dialogArea, SWT.NONE);
 		serviceModelWidget.setServiceConfiguration(config);
 		GridData data = new GridData(GridData.FILL_BOTH);
 		data.minimumHeight = 400;
 		serviceModelWidget.setLayoutData(data);
 		return serviceModelWidget;
	}


	@Override
	protected void okPressed() {
		serviceModelWidget.applyChangesToConfiguration();
		super.okPressed();
	}

}
