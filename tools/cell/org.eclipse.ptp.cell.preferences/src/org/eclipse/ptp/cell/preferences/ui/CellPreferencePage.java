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

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ptp.cell.preferences.PreferencesPlugin;
import org.eclipse.ptp.cell.preferences.core.SysrootSearcher;



/**
 * This class represents a preference page that
 * is contributed to the Preferences dialog. By 
 * subclassing <samp>FieldEditorPreferencePage</samp>, we
 * can use the field support built into JFace that allows
 * us to create a page that is small and knows how to 
 * save, restore and apply itself.
 * <p>
 * This page is used to modify preferences only. They
 * are stored in the preference store that belongs to
 * the main plug-in class. That way, preferences can
 * be accessed directly via the preference store.
 * 
 * @author Ricardo M. Matinata
 * @since 1.0
 */
public class CellPreferencePage
	extends AbstractBaseFieldEditorPreferencePage {

	private DirectoryFieldEditorWithSearch sysroot;
	
	public CellPreferencePage() {
		//this(GRID);
		super(GRID);
//		 Set the preference store for the preference page.
		IPreferenceStore store =
			PreferencesPlugin.getDefault().getPreferenceStore();
		setPreferenceStore(store);
		
		setDescription(Messages.CellIDEPreferencePageDescription);
	}
	
	@Override
	public void createFieldEditors() {
		SpacerFieldEditor spacer1 = new SpacerFieldEditor(
				getFieldEditorParent());
		addField(spacer1);
		
		// Insert field to get SDK SYSROOT
		this.sysroot = new DirectoryFieldEditorWithSearch(PreferenceConstants.SDK_SYSROOT, 
				Messages.SdkSysrootPath, getFieldEditorParent());
		this.sysroot.setEmptyStringAllowed(true);
		addField(sysroot);
	}
	
	/**
	 * Initializes all fields editors, searchers and property listeners.
	 */
	protected void initialize() {
		super.initialize();
		this.sysroot.addSearcher(new SysrootSearcher(
				this.sysroot, getFieldEditorParent()));
	}
	
}