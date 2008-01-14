/**
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *
 */
package org.eclipse.ptp.remote.remotetools.environment.preferences.ui;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.ptp.remote.remotetools.Activator;
import org.eclipse.ptp.remote.remotetools.environment.core.ConfigFactory;
import org.eclipse.ptp.remotetools.preferences.ui.AbstractBaseFieldEditorPreferencePage;
import org.eclipse.ptp.remotetools.preferences.ui.LabelFieldEditor;
import org.eclipse.ptp.remotetools.preferences.ui.SpacerFieldEditor;
import org.eclipse.ui.IWorkbench;


/**
 * Preference page for the Remote Host default values for new targets.
 * @author Richard Maciel, Daniel Felix Ferber
 * @since 1.2.1
 */
public class PreferencePage extends AbstractBaseFieldEditorPreferencePage {

	public PreferencePage() {
		super(GRID);

		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		setPreferenceStore(store);
		
		// setDescription must be called here or it wont work
		setDescription(Messages.PreferencePage_Description);
	}

	protected void createFieldEditors() {
		// setTitle must be called here or it wont work
		setTitle(Messages.PreferencePage_Title);
		
		addField(new LabelFieldEditor(Messages.PreferencePage_HeaderConnection, getFieldEditorParent()));

		StringFieldEditor addrfield = new StringFieldEditor(ConfigFactory.ATTR_CONNECTION_ADDRESS,
				Messages.PreferencePage_LabelConnectionAddress, getFieldEditorParent());
		addrfield.setEmptyStringAllowed(true);
		addField(addrfield);

		IntegerFieldEditor portfield = new IntegerFieldEditor(ConfigFactory.ATTR_CONNECTION_PORT,
				Messages.PreferencePage_LabelConnectionPort, getFieldEditorParent());
		portfield.setEmptyStringAllowed(false);
		addField(portfield);

		StringFieldEditor userfield = new StringFieldEditor(ConfigFactory.ATTR_LOGIN_USERNAME,
				Messages.PreferencePage_LabelLoginUserName, getFieldEditorParent());
		userfield.setEmptyStringAllowed(true);
		addField(userfield);

		addField(new LabelFieldEditor(Messages.PreferencePage_HeaderLaunch, getFieldEditorParent()));
	}

	public void init(IWorkbench workbench) {
	}

	void addSpace() {
		SpacerFieldEditor spacer1 = new SpacerFieldEditor(getFieldEditorParent());
		addField(spacer1);
	}

}
