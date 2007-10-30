/******************************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *
 *****************************************************************************/
package org.eclipse.ptp.remotetools.environment.launcher.preferences;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class LauncherPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	IPreferenceStore store;
	private StringFieldEditor workingDirectory;
	
	public LauncherPreferencePage() {
		super(GRID);

		store = LaunchPreferences.getPreferenceStore();
		setPreferenceStore(store);
		
		setDescription(Messages.LauncherPreferencePage_Description);
		setTitle(Messages.LauncherPreferencePage_Title);
	}


	protected void createFieldEditors() {
		workingDirectory = new StringFieldEditor(LaunchPreferences.ATTR_WORKING_DIRECTORY, Messages.LauncherPreferencePage_RemoteWorkingDirectoryLabel, getFieldEditorParent());
		addField(workingDirectory);
	}

	public void init(IWorkbench workbench) {
	}

}
