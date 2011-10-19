/*******************************************************************************
 * Copyright (c) 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.ptp.launch.internal.ui;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.ptp.core.Preferences;
import org.eclipse.ptp.launch.PTPLaunchPlugin;
import org.eclipse.ptp.launch.internal.PreferenceConstants;
import org.eclipse.ptp.launch.messages.Messages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class LaunchPreferencesPage extends PreferencePage implements IWorkbenchPreferencePage {
	private Button autoLaunchButton = null;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
		// TODO Auto-generated method stub
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.IPreferencePage#isValid()
	 */
	@Override
	public boolean isValid() {
		setErrorMessage(null);
		setMessage(null);
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.PreferencePage#performDefaults()
	 */
	@Override
	public void performDefaults() {
		autoLaunchButton.setSelection(Preferences.getDefaultBoolean(PTPLaunchPlugin.getUniqueIdentifier(),
				PreferenceConstants.PREFS_AUTO_START, PreferenceConstants.DEFAULT_AUTO_START));
		super.performDefaults();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.IPreferencePage#performOk()
	 */
	@Override
	public boolean performOk() {
		Preferences.setBoolean(PTPLaunchPlugin.getUniqueIdentifier(), PreferenceConstants.PREFS_AUTO_START,
				autoLaunchButton.getSelection());
		super.performOk();
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createContents(Composite parent) {
		// getWorkbench().getHelpSystem().setHelp(getControl(), IPDebugHelpContextIds.P_DEBUG_PREFERENCE_PAGE);
		Composite composite = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout(1, false);
		layout.numColumns = 1;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		composite.setLayout(layout);

		createControls(composite);

		applyDialogFont(composite);

		loadValues();

		return composite;
	}

	/**
	 * Create tooltip group composite
	 * 
	 * @param parent
	 */
	protected void createControls(Composite parent) {
		Composite controlComp = new Composite(parent, SWT.NULL);
		controlComp.setLayout(new GridLayout(1, false));
		controlComp.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, true, true));
		autoLaunchButton = new Button(controlComp, SWT.CHECK);
		autoLaunchButton.setText(Messages.LaunchPreferencesPage_Auto_start_RM);
		autoLaunchButton.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));
	}

	protected void loadValues() {
		if (autoLaunchButton != null) {
			autoLaunchButton.setSelection(Preferences.getBoolean(PTPLaunchPlugin.getUniqueIdentifier(),
					PreferenceConstants.PREFS_AUTO_START));
		}
	}
}
