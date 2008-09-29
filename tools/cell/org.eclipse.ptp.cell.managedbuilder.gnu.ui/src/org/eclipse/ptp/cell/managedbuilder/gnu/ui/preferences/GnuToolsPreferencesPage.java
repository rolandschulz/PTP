/******************************************************************************
 * Copyright (c) 2007 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *****************************************************************************/
package org.eclipse.ptp.cell.managedbuilder.gnu.ui.preferences;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ptp.cell.managedbuilder.gnu.core.preferences.GnuToolsProperties;
import org.eclipse.ptp.cell.managedbuilder.gnu.core.preferences.GnuToolsSearcher;
import org.eclipse.ptp.cell.managedbuilder.gnu.ui.GnuManagedBuilderUIPlugin;
import org.eclipse.ptp.cell.preferences.ui.AbstractBaseFieldEditorPreferencePage;
import org.eclipse.ptp.cell.preferences.ui.DirectoryFieldEditorWithSearch;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbench;


/**
 * @author laggarcia
 * @sice 3.0.0
 */
public class GnuToolsPreferencesPage extends
		AbstractBaseFieldEditorPreferencePage {

	protected static final String EMPTY_STRING = ""; //$NON-NLS-1$

	private DirectoryFieldEditorWithSearch gnuToolsDirectory;

	public GnuToolsPreferencesPage() {
		this(GRID);
	}

	/**
	 * @param style
	 */
	public GnuToolsPreferencesPage(int style) {
		this(EMPTY_STRING, style);
	}

	/**
	 * @param title
	 * @param style
	 */
	public GnuToolsPreferencesPage(String title, int style) {
		this(title, null, style);
	}

	public GnuToolsPreferencesPage(String title, ImageDescriptor image,
			int style) {
		super(title, image, style);
		// Set the preference store for the preference page.
		IPreferenceStore store = GnuManagedBuilderUIPlugin.getDefault()
				.getPreferenceStore();
		setPreferenceStore(store);
	}

	protected void createFieldEditors() {

		Composite fieldEditorParent = getFieldEditorParent();

		// Field for GNU tools path
		this.gnuToolsDirectory = new DirectoryFieldEditorWithSearch(
				GnuToolsProperties.gnuToolsPath,
				GnuToolsProperties.gnuToolsPathLabel, fieldEditorParent);
		this.gnuToolsDirectory.setEmptyStringAllowed(false);
		addField(this.gnuToolsDirectory);

	}

	/**
	 * Initializes all fields editors, searchers and property listeners.
	 */
	protected void initialize() {
		super.initialize();
		this.gnuToolsDirectory.addSearcher(new GnuToolsSearcher(
				this.gnuToolsDirectory, getFieldEditorParent()));
	}

	public void init(IWorkbench workbench) {
		// Nothing to do.
	}

}
