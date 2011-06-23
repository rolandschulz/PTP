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

import org.eclipse.core.runtime.Platform;
import org.eclipse.ptp.core.Preferences;
import org.eclipse.ptp.rm.jaxb.core.JAXBCorePlugin;
import org.eclipse.ptp.rm.jaxb.core.JAXBRMPreferenceConstants;
import org.eclipse.ptp.rm.jaxb.core.JAXBRMPreferenceManager;
import org.eclipse.ptp.rm.jaxb.ui.messages.Messages;
import org.eclipse.ptp.rm.jaxb.ui.util.WidgetBuilderUtils;
import org.eclipse.ptp.rm.ui.preferences.AbstractRemoteRMPreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;

/**
 * 
 * @author arossi
 * 
 */
public class JAXBRMPreferencesPage extends AbstractRemoteRMPreferencePage implements SelectionListener, ModifyListener {

	private Button reloadOption;
	private Button segmentPattern;
	private Button matchStatus;
	private Button actions;
	private Button createdProperties;
	private Text tokenizerLogFile;

	@Override
	public String getPreferenceQualifier() {
		return JAXBCorePlugin.getUniqueIdentifier();
	}

	/*
	 * Serves a listener for the preference widgets. (non-Javadoc) (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.swt.events.ModifyListener#modifyText(org.eclipse.swt.events
	 * .ModifyEvent)
	 */
	public void modifyText(ModifyEvent e) {
		Text source = (Text) e.getSource();
		if (source == tokenizerLogFile) {
			String text = tokenizerLogFile.getText();
			Preferences.setString(getPreferenceQualifier(), JAXBRMPreferenceConstants.LOG_FILE, text);
		}
	}

	@Override
	public void performApply() {
		savePreferences();
	}

	@Override
	public void performDefaults() {
		reloadOption.setSelection(Preferences.getDefaultBoolean(getPreferenceQualifier(),
				JAXBRMPreferenceConstants.FORCE_XML_RELOAD, false));
		segmentPattern.setSelection(Preferences.getDefaultBoolean(getPreferenceQualifier(),
				JAXBRMPreferenceConstants.SEGMENT_PATTERN, false));
		matchStatus.setSelection(Preferences.getDefaultBoolean(getPreferenceQualifier(), JAXBRMPreferenceConstants.MATCH_STATUS,
				false));
		actions.setSelection(Preferences.getDefaultBoolean(getPreferenceQualifier(), JAXBRMPreferenceConstants.ACTIONS, false));
		createdProperties.setSelection(Preferences.getDefaultBoolean(getPreferenceQualifier(),
				JAXBRMPreferenceConstants.CREATED_PROPERTIES, false));
		tokenizerLogFile.setText(Preferences.getDefaultString(getPreferenceQualifier(), JAXBRMPreferenceConstants.LOG_FILE,
				JAXBUIConstants.ZEROSTR));
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

	/*
	 * Serves a listener for the preference widgets. (non-Javadoc)
	 * 
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(org.eclipse
	 * .swt.events.SelectionEvent)
	 */
	public void widgetDefaultSelected(SelectionEvent e) {
		widgetSelected(e);
	}

	/*
	 * Serves a listener for the preference widgets. (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt
	 * .events.SelectionEvent)
	 */
	public void widgetSelected(SelectionEvent e) {
		Button source = (Button) e.getSource();
		if (source == reloadOption) {
			boolean b = reloadOption.getSelection();
			Preferences.setBoolean(getPreferenceQualifier(), JAXBRMPreferenceConstants.FORCE_XML_RELOAD, b);
		} else if (source == segmentPattern) {
			boolean b = segmentPattern.getSelection();
			Preferences.setBoolean(getPreferenceQualifier(), JAXBRMPreferenceConstants.SEGMENT_PATTERN, b);
		} else if (source == matchStatus) {
			boolean b = matchStatus.getSelection();
			Preferences.setBoolean(getPreferenceQualifier(), JAXBRMPreferenceConstants.MATCH_STATUS, b);
		} else if (source == actions) {
			boolean b = actions.getSelection();
			Preferences.setBoolean(getPreferenceQualifier(), JAXBRMPreferenceConstants.ACTIONS, b);
		} else if (source == createdProperties) {
			boolean b = createdProperties.getSelection();
			Preferences.setBoolean(getPreferenceQualifier(), JAXBRMPreferenceConstants.CREATED_PROPERTIES, b);
		}
	}

	@Override
	protected Control createContents(Composite parent) {
		Group optionsGroup = new Group(parent, SWT.SHADOW_ETCHED_IN);
		optionsGroup.setText(Messages.Preferences_options);
		optionsGroup.setLayout(new GridLayout(1, true));
		GridData gd = new GridData();
		gd.horizontalAlignment = GridData.BEGINNING;
		gd.horizontalSpan = 1;
		gd.grabExcessHorizontalSpace = true;
		gd.grabExcessVerticalSpace = false;
		optionsGroup.setLayoutData(gd);
		reloadOption = new Button(optionsGroup, SWT.CHECK | SWT.LEFT);
		reloadOption.addSelectionListener(this);
		reloadOption.setText(JAXBRMPreferenceConstants.FORCE_XML_RELOAD);

		optionsGroup = new Group(parent, SWT.SHADOW_ETCHED_IN);
		optionsGroup.setText(Messages.Debug_options);
		optionsGroup.setLayout(new GridLayout(1, true));
		gd = new GridData();
		gd.horizontalAlignment = GridData.BEGINNING;
		gd.horizontalSpan = 1;
		gd.grabExcessHorizontalSpace = true;
		gd.grabExcessVerticalSpace = false;
		optionsGroup.setLayoutData(gd);

		segmentPattern = new Button(optionsGroup, SWT.CHECK | SWT.LEFT);
		segmentPattern.addSelectionListener(this);
		segmentPattern.setText(JAXBRMPreferenceConstants.SEGMENT_PATTERN);

		matchStatus = new Button(optionsGroup, SWT.CHECK | SWT.LEFT);
		matchStatus.addSelectionListener(this);
		matchStatus.setText(JAXBRMPreferenceConstants.MATCH_STATUS);

		actions = new Button(optionsGroup, SWT.CHECK | SWT.LEFT);
		actions.addSelectionListener(this);
		actions.setText(JAXBRMPreferenceConstants.ACTIONS);

		createdProperties = new Button(optionsGroup, SWT.CHECK | SWT.LEFT);
		createdProperties.addSelectionListener(this);
		createdProperties.setText(JAXBRMPreferenceConstants.CREATED_PROPERTIES);

		Composite c = new Composite(optionsGroup, SWT.NONE);
		c.setLayout(new GridLayout(7, false));
		gd = new GridData();
		gd.horizontalAlignment = GridData.BEGINNING;
		gd.horizontalSpan = 7;
		gd.grabExcessHorizontalSpace = false;
		gd.grabExcessVerticalSpace = false;
		c.setLayoutData(gd);
		WidgetBuilderUtils.createLabel(c, JAXBRMPreferenceConstants.LOG_FILE, SWT.LEFT, 1);
		gd = new GridData();
		gd.horizontalAlignment = GridData.FILL;
		gd.horizontalSpan = 6;
		gd.grabExcessHorizontalSpace = true;
		gd.grabExcessVerticalSpace = false;
		gd.widthHint = 175;
		tokenizerLogFile = WidgetBuilderUtils.createText(c, SWT.BORDER, gd, false, null, this, null);

		loadSaved();
		return optionsGroup;
	}

	/**
	 * Load values from preference store
	 */
	private void loadSaved() {
		boolean def = Preferences.getDefaultBoolean(getPreferenceQualifier(), JAXBRMPreferenceConstants.FORCE_XML_RELOAD, false);
		boolean b = Platform.getPreferencesService().getBoolean(getPreferenceQualifier(),
				JAXBRMPreferenceConstants.FORCE_XML_RELOAD, def, null);
		reloadOption.setSelection(b);
		def = Preferences.getDefaultBoolean(getPreferenceQualifier(), JAXBRMPreferenceConstants.SEGMENT_PATTERN, false);
		b = Platform.getPreferencesService().getBoolean(getPreferenceQualifier(), JAXBRMPreferenceConstants.SEGMENT_PATTERN, def,
				null);
		segmentPattern.setSelection(b);
		def = Preferences.getDefaultBoolean(getPreferenceQualifier(), JAXBRMPreferenceConstants.MATCH_STATUS, false);
		b = Platform.getPreferencesService()
				.getBoolean(getPreferenceQualifier(), JAXBRMPreferenceConstants.MATCH_STATUS, def, null);
		matchStatus.setSelection(b);
		def = Preferences.getDefaultBoolean(getPreferenceQualifier(), JAXBRMPreferenceConstants.ACTIONS, false);
		b = Platform.getPreferencesService().getBoolean(getPreferenceQualifier(), JAXBRMPreferenceConstants.ACTIONS, def, null);
		actions.setSelection(b);
		def = Preferences.getDefaultBoolean(getPreferenceQualifier(), JAXBRMPreferenceConstants.CREATED_PROPERTIES, false);
		b = Platform.getPreferencesService().getBoolean(getPreferenceQualifier(), JAXBRMPreferenceConstants.CREATED_PROPERTIES,
				def, null);
		createdProperties.setSelection(b);
		String defText = Preferences.getDefaultString(getPreferenceQualifier(), JAXBRMPreferenceConstants.LOG_FILE,
				JAXBUIConstants.ZEROSTR);
		String text = Platform.getPreferencesService().getString(getPreferenceQualifier(), JAXBRMPreferenceConstants.LOG_FILE,
				defText, null);
		tokenizerLogFile.setText(text);
	}
}