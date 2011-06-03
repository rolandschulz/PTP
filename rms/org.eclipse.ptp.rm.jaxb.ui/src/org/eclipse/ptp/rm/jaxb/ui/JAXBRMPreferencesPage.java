/*******************************************************************************
 * Copyright (c) 2011 University of Illinois All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html 
 * 	
 * Contributors: 
 * 	Albert L. Rossi - design and implementation
 ******************************************************************************/
package org.eclipse.ptp.rm.jaxb.ui;

import org.eclipse.ptp.core.Preferences;
import org.eclipse.ptp.rm.jaxb.core.JAXBCorePlugin;
import org.eclipse.ptp.rm.jaxb.core.JAXBRMPreferenceConstants;
import org.eclipse.ptp.rm.jaxb.core.JAXBRMPreferenceManager;
import org.eclipse.ptp.rm.jaxb.ui.messages.Messages;
import org.eclipse.ptp.rm.ui.preferences.AbstractRemoteRMPreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

public class JAXBRMPreferencesPage extends AbstractRemoteRMPreferencePage implements SelectionListener {

	private Button reloadOption;

	@Override
	public String getPreferenceQualifier() {
		return JAXBCorePlugin.getUniqueIdentifier();
	}

	@Override
	public void performApply() {
		savePreferences();
		super.performApply();
	}

	@Override
	public void performDefaults() {
		reloadOption.setSelection(Preferences.getDefaultBoolean(getPreferenceQualifier(),
				JAXBRMPreferenceConstants.FORCE_XML_RELOAD, false));
		updateApplyButton();
	}

	@Override
	public boolean performOk() {
		return true;
	}

	@Override
	public void savePreferences() {
		JAXBRMPreferenceManager.savePreferences();
	}

	public void widgetDefaultSelected(SelectionEvent e) {
		widgetSelected(e);
	}

	public void widgetSelected(SelectionEvent e) {
		Preferences.setBoolean(getPreferenceQualifier(), JAXBRMPreferenceConstants.FORCE_XML_RELOAD, reloadOption.getSelection());
		setValid(true);
	}

	@Override
	protected Control createContents(Composite parent) {
		Group optionsGroup = new Group(parent, SWT.SHADOW_ETCHED_IN);
		optionsGroup.setText(Messages.Preferences_options);
		optionsGroup.setLayout(new GridLayout(2, true));
		GridData gd = new GridData();
		gd.horizontalAlignment = GridData.FILL;
		gd.horizontalIndent = 20;
		gd.horizontalSpan = 2;
		optionsGroup.setLayoutData(gd);
		new Label(optionsGroup, SWT.NONE).setText(JAXBRMPreferenceConstants.FORCE_XML_RELOAD);
		reloadOption = new Button(optionsGroup, SWT.CHECK);
		reloadOption.addSelectionListener(this);
		loadSaved();
		return optionsGroup;
	}

	/**
	 * Load values from preference store
	 */
	private void loadSaved() {
		reloadOption.setSelection(Preferences.getBoolean(getPreferenceQualifier(), JAXBRMPreferenceConstants.FORCE_XML_RELOAD));
	}
}