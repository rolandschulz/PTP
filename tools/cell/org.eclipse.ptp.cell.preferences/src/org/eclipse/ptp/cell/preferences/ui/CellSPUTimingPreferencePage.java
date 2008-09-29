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
package org.eclipse.ptp.cell.preferences.ui;

import org.eclipse.jface.preference.FileFieldEditor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ptp.cell.preferences.PreferencesPlugin;
import org.eclipse.ui.IWorkbench;



/**
 * 
 * @author Ricardo M. Matinata
 * @since 1.0
 */
public class CellSPUTimingPreferencePage extends AbstractBaseFieldEditorPreferencePage {
	
	public CellSPUTimingPreferencePage() {
		this(GRID);
//		 Set the preference store for the preference page.
		IPreferenceStore store =
			PreferencesPlugin.getDefault().getPreferenceStore();
		setPreferenceStore(store);
	}
	
	public CellSPUTimingPreferencePage(int style) {
		super(style);
		
	}

	public CellSPUTimingPreferencePage(String title, int style) {
		super(title, style);
		
	}

	public CellSPUTimingPreferencePage(String title,
			ImageDescriptor image, int style) {
		super(title, image, style);
		
	}

	protected void createFieldEditors() {
		FileFieldEditor gdbbin = new FileFieldEditor(PreferenceConstants.TIMING_SPUBIN,
                Messages.SPUTimingBinaryPreferenceLabel,
                getFieldEditorParent());
		gdbbin.setEmptyStringAllowed(false);
		addField(gdbbin);
		
		SpacerFieldEditor spacer1 = new SpacerFieldEditor(
				getFieldEditorParent());
		addField(spacer1);
		

	}

	public void init(IWorkbench workbench) {
		// TODO Auto-generated method stub

	}


}
