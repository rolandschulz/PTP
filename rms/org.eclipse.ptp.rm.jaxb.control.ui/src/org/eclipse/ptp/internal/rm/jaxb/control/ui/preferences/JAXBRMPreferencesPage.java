/*******************************************************************************
 * Copyright (c) 2011 University of Illinois All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html 
 * 	
 * Contributors: 
 * 	Albert L. Rossi - design and implementation
 ******************************************************************************/
package org.eclipse.ptp.internal.rm.jaxb.control.ui.preferences;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.ptp.core.Preferences;
import org.eclipse.ptp.internal.rm.jaxb.control.core.JAXBControlCorePlugin;
import org.eclipse.ptp.internal.rm.jaxb.control.core.JAXBRMPreferenceConstants;
import org.eclipse.ptp.internal.rm.jaxb.control.ui.messages.Messages;
import org.eclipse.ptp.internal.rm.jaxb.ui.JAXBUIConstants;
import org.eclipse.ptp.internal.rm.jaxb.ui.util.WidgetBuilderUtils;
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
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * 
 * @author arossi
 * 
 */
public class JAXBRMPreferencesPage extends PreferencePage implements IWorkbenchPreferencePage, SelectionListener, ModifyListener {

	private Button reloadOption;
	private Button segmentPattern;
	private Button matchStatus;
	private Button actions;
	private Button createdProperties;
	private Button showCommandOutput;
	private Button showCommand;
	private Text logFile;

	@Override
	protected Control createContents(Composite parent) {
		Composite preferences = new Composite(parent, SWT.NONE);
		preferences.setLayout(createGridLayout(1, true, 0, 0));
		preferences.setLayoutData(spanGridData(GridData.FILL_HORIZONTAL, 2));

		Group optionsGroup = new Group(preferences, SWT.SHADOW_ETCHED_IN);
		optionsGroup.setText(Messages.JAXBRMPreferencesPage_Preferences_options);
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

		Group parserOptionsGroup = new Group(preferences, SWT.SHADOW_ETCHED_IN);
		parserOptionsGroup.setText(Messages.JAXBRMPreferencesPage_ParserDebug_options);
		parserOptionsGroup.setLayout(new GridLayout(1, true));
		gd = new GridData();
		gd.horizontalAlignment = GridData.BEGINNING;
		gd.horizontalSpan = 1;
		gd.grabExcessHorizontalSpace = true;
		gd.grabExcessVerticalSpace = false;
		optionsGroup.setLayoutData(gd);

		segmentPattern = new Button(parserOptionsGroup, SWT.CHECK | SWT.LEFT);
		segmentPattern.addSelectionListener(this);
		segmentPattern.setText(JAXBRMPreferenceConstants.SEGMENT_PATTERN);

		matchStatus = new Button(parserOptionsGroup, SWT.CHECK | SWT.LEFT);
		matchStatus.addSelectionListener(this);
		matchStatus.setText(JAXBRMPreferenceConstants.MATCH_STATUS);

		actions = new Button(parserOptionsGroup, SWT.CHECK | SWT.LEFT);
		actions.addSelectionListener(this);
		actions.setText(JAXBRMPreferenceConstants.ACTIONS);

		createdProperties = new Button(parserOptionsGroup, SWT.CHECK | SWT.LEFT);
		createdProperties.addSelectionListener(this);
		createdProperties.setText(JAXBRMPreferenceConstants.CREATED_PROPERTIES);

		Group commandOptionsGroup = new Group(preferences, SWT.SHADOW_ETCHED_IN);
		commandOptionsGroup.setText(Messages.JAXBRMPreferencesPage_CommandDebug_options);
		commandOptionsGroup.setLayout(new GridLayout(1, true));
		gd = new GridData();
		gd.horizontalAlignment = GridData.BEGINNING;
		gd.horizontalSpan = 1;
		gd.grabExcessHorizontalSpace = true;
		gd.grabExcessVerticalSpace = false;
		commandOptionsGroup.setLayoutData(gd);

		showCommand = new Button(commandOptionsGroup, SWT.CHECK | SWT.LEFT);
		showCommand.addSelectionListener(this);
		showCommand.setText(JAXBRMPreferenceConstants.SHOW_COMMAND);
		showCommandOutput = new Button(commandOptionsGroup, SWT.CHECK | SWT.LEFT);
		showCommandOutput.addSelectionListener(this);
		showCommandOutput.setText(JAXBRMPreferenceConstants.SHOW_COMMAND_OUTPUT);

		Composite c = new Composite(preferences, SWT.NONE);
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
		logFile = WidgetBuilderUtils.createText(c, SWT.BORDER, gd, false, null, this, null);

		loadSaved();
		return preferences;
	}

	private GridLayout createGridLayout(int columns, boolean isEqual, int mh, int mw) {
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = columns;
		gridLayout.makeColumnsEqualWidth = isEqual;
		gridLayout.marginHeight = mh;
		gridLayout.marginWidth = mw;
		return gridLayout;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
		// Nothing to do
	}

	/**
	 * Load values from preference store
	 */
	private void loadSaved() {
		boolean def = Preferences.getDefaultBoolean(JAXBControlCorePlugin.getUniqueIdentifier(),
				JAXBRMPreferenceConstants.FORCE_XML_RELOAD, false);
		boolean b = Platform.getPreferencesService().getBoolean(JAXBControlCorePlugin.getUniqueIdentifier(),
				JAXBRMPreferenceConstants.FORCE_XML_RELOAD, def, null);
		reloadOption.setSelection(b);
		def = Preferences.getDefaultBoolean(JAXBControlCorePlugin.getUniqueIdentifier(), JAXBRMPreferenceConstants.SEGMENT_PATTERN,
				false);
		b = Platform.getPreferencesService().getBoolean(JAXBControlCorePlugin.getUniqueIdentifier(),
				JAXBRMPreferenceConstants.SEGMENT_PATTERN, def, null);
		segmentPattern.setSelection(b);
		def = Preferences.getDefaultBoolean(JAXBControlCorePlugin.getUniqueIdentifier(), JAXBRMPreferenceConstants.MATCH_STATUS,
				false);
		b = Platform.getPreferencesService().getBoolean(JAXBControlCorePlugin.getUniqueIdentifier(),
				JAXBRMPreferenceConstants.MATCH_STATUS, def, null);
		matchStatus.setSelection(b);
		def = Preferences.getDefaultBoolean(JAXBControlCorePlugin.getUniqueIdentifier(), JAXBRMPreferenceConstants.ACTIONS, false);
		b = Platform.getPreferencesService().getBoolean(JAXBControlCorePlugin.getUniqueIdentifier(),
				JAXBRMPreferenceConstants.ACTIONS, def, null);
		actions.setSelection(b);
		def = Preferences.getDefaultBoolean(JAXBControlCorePlugin.getUniqueIdentifier(),
				JAXBRMPreferenceConstants.CREATED_PROPERTIES, false);
		b = Platform.getPreferencesService().getBoolean(JAXBControlCorePlugin.getUniqueIdentifier(),
				JAXBRMPreferenceConstants.CREATED_PROPERTIES, def, null);
		createdProperties.setSelection(b);
		def = Preferences.getDefaultBoolean(JAXBControlCorePlugin.getUniqueIdentifier(), JAXBRMPreferenceConstants.SHOW_COMMAND,
				false);
		b = Platform.getPreferencesService().getBoolean(JAXBControlCorePlugin.getUniqueIdentifier(),
				JAXBRMPreferenceConstants.SHOW_COMMAND, def, null);
		showCommand.setSelection(b);
		def = Preferences.getDefaultBoolean(JAXBControlCorePlugin.getUniqueIdentifier(),
				JAXBRMPreferenceConstants.SHOW_COMMAND_OUTPUT, false);
		b = Platform.getPreferencesService().getBoolean(JAXBControlCorePlugin.getUniqueIdentifier(),
				JAXBRMPreferenceConstants.SHOW_COMMAND_OUTPUT, def, null);
		showCommandOutput.setSelection(b);
		String defText = Preferences.getDefaultString(JAXBControlCorePlugin.getUniqueIdentifier(),
				JAXBRMPreferenceConstants.LOG_FILE, JAXBUIConstants.ZEROSTR);
		String text = Platform.getPreferencesService().getString(JAXBControlCorePlugin.getUniqueIdentifier(),
				JAXBRMPreferenceConstants.LOG_FILE, defText, null);
		logFile.setText(text);
	}

	/*
	 * Serves a listener for the preference widgets. (non-Javadoc) (non-Javadoc)
	 * 
	 * @see org.eclipse.swt.events.ModifyListener#modifyText(org.eclipse.swt.events .ModifyEvent)
	 */
	public void modifyText(ModifyEvent e) {
		Text source = (Text) e.getSource();
		if (source == logFile) {
			String text = logFile.getText();
			Preferences.setString(JAXBControlCorePlugin.getUniqueIdentifier(), JAXBRMPreferenceConstants.LOG_FILE, text);
		}
	}

	@Override
	public void performApply() {
		JAXBRMPreferenceManager.savePreferences();
	}

	@Override
	public void performDefaults() {
		reloadOption.setSelection(Preferences.getDefaultBoolean(JAXBControlCorePlugin.getUniqueIdentifier(),
				JAXBRMPreferenceConstants.FORCE_XML_RELOAD, false));
		segmentPattern.setSelection(Preferences.getDefaultBoolean(JAXBControlCorePlugin.getUniqueIdentifier(),
				JAXBRMPreferenceConstants.SEGMENT_PATTERN, false));
		matchStatus.setSelection(Preferences.getDefaultBoolean(JAXBControlCorePlugin.getUniqueIdentifier(),
				JAXBRMPreferenceConstants.MATCH_STATUS, false));
		actions.setSelection(Preferences.getDefaultBoolean(JAXBControlCorePlugin.getUniqueIdentifier(),
				JAXBRMPreferenceConstants.ACTIONS, false));
		createdProperties.setSelection(Preferences.getDefaultBoolean(JAXBControlCorePlugin.getUniqueIdentifier(),
				JAXBRMPreferenceConstants.CREATED_PROPERTIES, false));
		showCommand.setSelection(Preferences.getDefaultBoolean(JAXBControlCorePlugin.getUniqueIdentifier(),
				JAXBRMPreferenceConstants.SHOW_COMMAND, false));
		showCommandOutput.setSelection(Preferences.getDefaultBoolean(JAXBControlCorePlugin.getUniqueIdentifier(),
				JAXBRMPreferenceConstants.SHOW_COMMAND_OUTPUT, false));
		logFile.setText(Preferences.getDefaultString(JAXBControlCorePlugin.getUniqueIdentifier(),
				JAXBRMPreferenceConstants.LOG_FILE, JAXBUIConstants.ZEROSTR));
		updateApplyButton();
	}

	@Override
	public boolean performOk() {
		return true;
	}

	private GridData spanGridData(int style, int space) {
		GridData gd = null;
		if (style == -1) {
			gd = new GridData();
		} else {
			gd = new GridData(style);
		}
		gd.horizontalSpan = space;
		return gd;
	}

	/*
	 * Serves a listener for the preference widgets. (non-Javadoc)
	 * 
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(org.eclipse .swt.events.SelectionEvent)
	 */
	public void widgetDefaultSelected(SelectionEvent e) {
		widgetSelected(e);
	}

	/*
	 * Serves a listener for the preference widgets. (non-Javadoc)
	 * 
	 * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt .events.SelectionEvent)
	 */
	public void widgetSelected(SelectionEvent e) {
		Button source = (Button) e.getSource();
		if (source == reloadOption) {
			boolean b = reloadOption.getSelection();
			Preferences.setBoolean(JAXBControlCorePlugin.getUniqueIdentifier(), JAXBRMPreferenceConstants.FORCE_XML_RELOAD, b);
		} else if (source == segmentPattern) {
			boolean b = segmentPattern.getSelection();
			Preferences.setBoolean(JAXBControlCorePlugin.getUniqueIdentifier(), JAXBRMPreferenceConstants.SEGMENT_PATTERN, b);
		} else if (source == matchStatus) {
			boolean b = matchStatus.getSelection();
			Preferences.setBoolean(JAXBControlCorePlugin.getUniqueIdentifier(), JAXBRMPreferenceConstants.MATCH_STATUS, b);
		} else if (source == actions) {
			boolean b = actions.getSelection();
			Preferences.setBoolean(JAXBControlCorePlugin.getUniqueIdentifier(), JAXBRMPreferenceConstants.ACTIONS, b);
		} else if (source == createdProperties) {
			boolean b = createdProperties.getSelection();
			Preferences.setBoolean(JAXBControlCorePlugin.getUniqueIdentifier(), JAXBRMPreferenceConstants.CREATED_PROPERTIES, b);
		} else if (source == showCommand) {
			boolean b = showCommand.getSelection();
			Preferences.setBoolean(JAXBControlCorePlugin.getUniqueIdentifier(), JAXBRMPreferenceConstants.SHOW_COMMAND, b);
		} else if (source == showCommandOutput) {
			boolean b = showCommandOutput.getSelection();
			Preferences.setBoolean(JAXBControlCorePlugin.getUniqueIdentifier(), JAXBRMPreferenceConstants.SHOW_COMMAND_OUTPUT, b);
		}
	}
}