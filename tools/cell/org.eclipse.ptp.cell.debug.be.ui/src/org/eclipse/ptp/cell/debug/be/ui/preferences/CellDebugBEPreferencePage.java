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
package org.eclipse.ptp.cell.debug.be.ui.preferences;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ptp.cell.debug.be.preferences.BEDebugPreferencesConstants;
import org.eclipse.ptp.cell.debug.be.preferences.GdbClientSearcher;
import org.eclipse.ptp.cell.debug.be.ui.Activator;
import org.eclipse.ptp.cell.preferences.ui.AbstractBaseFieldEditorPreferencePage;
import org.eclipse.ptp.cell.preferences.ui.FileFieldEditorWithSearch;
import org.eclipse.ptp.cell.preferences.ui.SpacerFieldEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbench;


/**
 * 
 * @author Ricardo M. Matinata
 * @since 1.3
 */
public class CellDebugBEPreferencePage extends
		AbstractBaseFieldEditorPreferencePage {

	protected static final String EMPTY_STRING = ""; //$NON-NLS-1$

	private FileFieldEditorWithSearch gdbbin;

	public CellDebugBEPreferencePage() {
		this(EMPTY_STRING, null, FieldEditorPreferencePage.GRID);
	}

	public CellDebugBEPreferencePage(int style) {
		this(EMPTY_STRING, null, style);

	}

	public CellDebugBEPreferencePage(String title, int style) {
		this(title, null, style);

	}

	public CellDebugBEPreferencePage(String title, ImageDescriptor image,
			int style) {
		super(title, image, style);
		// Set the preference store for the preference page.
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		setPreferenceStore(store);
	}

	protected void createFieldEditors() {

		Composite fieldEditorParent = getFieldEditorParent();

		this.gdbbin = new FileFieldEditorWithSearch(
				BEDebugPreferencesConstants.BE_GDB_BIN,
				CellDebugBEPreferencesMessages
						.getString("CellDebugBEPreferencePage.0"), //$NON-NLS-1$
				fieldEditorParent);
		this.gdbbin.setEmptyStringAllowed(false);
		this.addField(this.gdbbin);

		addSpace(fieldEditorParent);
		
		StringFieldEditor gdbsbin = new StringFieldEditor(
				BEDebugPreferencesConstants.BE_GDBSERVER_BIN,
				CellDebugBEPreferencesMessages
						.getString("CellDebugBEPreferencePage.2"), //$NON-NLS-1$
				fieldEditorParent);
		gdbsbin.setEmptyStringAllowed(false);
		addField(gdbsbin);

		addSpace(fieldEditorParent);

		StringFieldEditor gdbsport = new StringFieldEditor(
				BEDebugPreferencesConstants.BE_GDBSERVER_PORT,
				CellDebugBEPreferencesMessages
						.getString("CellDebugBEPreferencePage.3"), //$NON-NLS-1$
				fieldEditorParent);
		gdbsport.setEmptyStringAllowed(false);
		addField(gdbsport);
	}

	/**
	 * Initializes all fields editors, searchers and property listeners.
	 */
	protected void initialize() {
		super.initialize();
		this.gdbbin.addSearcher(new GdbClientSearcher(this.gdbbin,
				getFieldEditorParent()));
	}

	protected void addSpace(Composite fieldEditorParent) {
		SpacerFieldEditor spacer = new SpacerFieldEditor(fieldEditorParent);
		addField(spacer);
	}

	public void init(IWorkbench workbench) {
	}

}
