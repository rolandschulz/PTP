/*******************************************************************************
 * Copyright (c) 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.ptp.launch.internal.ui;

import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.ptp.core.PreferencesAdapter;
import org.eclipse.ptp.launch.PTPLaunchPlugin;
import org.eclipse.ptp.launch.PreferenceConstants;
import org.eclipse.ptp.launch.internal.messages.Messages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class LaunchPreferencesPage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

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
	 * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createContents(Composite parent) {
		Composite composite = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout(1, false);
		layout.numColumns = 1;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		composite.setLayout(layout);

		createControls(composite);

		applyDialogFont(composite);

		initialize();
		checkState();

		return composite;
	}

	public LaunchPreferencesPage() {
		super();
		setPreferenceStore(new PreferencesAdapter(PTPLaunchPlugin.getUniqueIdentifier()));
	}

	/**
	 * Create tooltip group composite
	 * 
	 * @param parent
	 */
	protected void createControls(Composite parent) {
		Composite controlComp = new Composite(parent, SWT.NULL);
		GridData data = new GridData(GridData.FILL, GridData.FILL, true, true);
		controlComp.setLayoutData(data);
		FieldEditor edit = new RadioGroupFieldEditor(PreferenceConstants.PREF_SWITCH_TO_MONITORING_PERSPECTIVE,
				Messages.LaunchPreferencesPage_OpenSystemMonitoringPerspective, 3, new String[][] {
						{ Messages.LaunchPreferencesPage_Always, MessageDialogWithToggle.ALWAYS },
						{ Messages.LaunchPreferencesPage_Never, MessageDialogWithToggle.NEVER },
						{ Messages.LaunchPreferencesPage_Prompt, MessageDialogWithToggle.PROMPT } }, controlComp, true);
		addField(edit);
		edit = new RadioGroupFieldEditor(PreferenceConstants.PREF_SWITCH_TO_DEBUG_PERSPECTIVE,
				Messages.LaunchPreferencesPage_OpenParallelDebugPerspective, 3, new String[][] {
						{ Messages.LaunchPreferencesPage_Always, MessageDialogWithToggle.ALWAYS },
						{ Messages.LaunchPreferencesPage_Never, MessageDialogWithToggle.NEVER },
						{ Messages.LaunchPreferencesPage_Prompt, MessageDialogWithToggle.PROMPT } }, controlComp, true);
		addField(edit);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.FieldEditorPreferencePage#createFieldEditors()
	 */
	@Override
	protected void createFieldEditors() {
		// TODO Auto-generated method stub
	}
}
