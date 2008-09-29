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
package org.eclipse.ptp.cell.debug.ui.preferences;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ptp.cell.debug.preferences.DebugPreferencesConstants;
import org.eclipse.ptp.cell.debug.ui.DebugUiPlugin;
import org.eclipse.ptp.cell.preferences.ui.AbstractBaseFieldEditorPreferencePage;
import org.eclipse.ptp.cell.preferences.ui.SpacerFieldEditor;
import org.eclipse.ui.IWorkbench;



/**
 * 
 * @author Ricardo M. Matinata
 * @since 1.3
 */
public class CellDebugPPUPreferencePage extends AbstractBaseFieldEditorPreferencePage {

	public CellDebugPPUPreferencePage() {
		super(FieldEditorPreferencePage.GRID);
		
		// Set the preference store for the preference page.
		IPreferenceStore store =
			DebugUiPlugin.getDefault().getPreferenceStore();
		setPreferenceStore(store);
	}
	
	public CellDebugPPUPreferencePage(int style) {
		super(style);
		
	}

	public CellDebugPPUPreferencePage(String title, int style) {
		super(title, style);
		
	}

	public CellDebugPPUPreferencePage(String title, ImageDescriptor image,
			int style) {
		super(title, image, style);
	}

	protected void createFieldEditors() {
		StringFieldEditor gdbbin = new StringFieldEditor(DebugPreferencesConstants.PPU_GDB_BIN,
                DebugPreferencesMessages.getString("CellDebugPPUPreferencePage.0"), //$NON-NLS-1$
                getFieldEditorParent());
		gdbbin.setEmptyStringAllowed(false);
		addField(gdbbin);
		
		SpacerFieldEditor spacer1 = new SpacerFieldEditor(
				getFieldEditorParent());
		addField(spacer1);
		
		StringFieldEditor gdbsbin = new StringFieldEditor(DebugPreferencesConstants.PPU_GDBSERVER_BIN,
                DebugPreferencesMessages.getString("CellDebugPPUPreferencePage.1"), //$NON-NLS-1$
                getFieldEditorParent());
		gdbsbin.setEmptyStringAllowed(false);
		addField(gdbsbin);
		
		SpacerFieldEditor spacer2 = new SpacerFieldEditor(
				getFieldEditorParent());
		addField(spacer2);

		StringFieldEditor gdbsport = new StringFieldEditor(DebugPreferencesConstants.PPU_GDBSERVER_PORT,
                DebugPreferencesMessages.getString("CellDebugPPUPreferencePage.2"), //$NON-NLS-1$
                getFieldEditorParent());
		gdbsport.setEmptyStringAllowed(false);
		addField(gdbsport);
	}

	public void init(IWorkbench workbench) {

	}

}
