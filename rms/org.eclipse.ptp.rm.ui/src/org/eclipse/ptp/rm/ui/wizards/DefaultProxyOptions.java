/*******************************************************************************
 * Copyright (c) 2011 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.ptp.rm.ui.wizards;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.ptp.rm.core.rmsystem.IRemoteResourceManagerConfiguration;
import org.eclipse.ptp.rm.ui.messages.Messages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Text;

/**
 * @since 2.0
 */
public class DefaultProxyOptions extends AbstractProxyOptions {

	private Text fOptionsText;

	public DefaultProxyOptions(WizardPage wizardPage, IRemoteResourceManagerConfiguration config) {
		super(wizardPage, config);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.ui.wizards.AbstractProxyOptions#save()
	 */
	@Override
	public void save() {
		if (fOptionsText != null) {
			getConfiguration().setInvocationOptions(fOptionsText.getText());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rm.ui.wizards.AbstractProxyOptions#createContents(org
	 * .eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Composite createContents(Composite parent) {
		Composite optionsComp = new Composite(parent, SWT.NONE);
		Layout layout = new GridLayout();
		optionsComp.setLayout(layout);
		Label optionsLabel = new Label(optionsComp, SWT.NONE);
		optionsLabel.setText(Messages.DefaultProxyOptions_ExtraCommandLineArgs);
		final Text optionsText = new Text(optionsComp, SWT.SINGLE | SWT.BORDER);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		optionsText.setLayoutData(gd);
		return optionsComp;
	}
}
