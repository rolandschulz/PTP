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
package org.eclipse.ptp.cell.managedbuilder.xl.ui.preferences;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ptp.cell.managedbuilder.xl.core.preferences.XlToolsProperties;
import org.eclipse.ptp.cell.managedbuilder.xl.core.preferences.XlToolsSearcher;
import org.eclipse.ptp.cell.managedbuilder.xl.ui.XlManagedBuilderUIPlugin;
import org.eclipse.ptp.cell.preferences.ui.AbstractBaseFieldEditorPreferencePage;
import org.eclipse.ptp.cell.preferences.ui.DirectoryFieldEditorWithSearch;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbench;


/**
 * @author laggarcia
 * @since 3.0.0
 */
public class XlToolsPreferencesPage extends
		AbstractBaseFieldEditorPreferencePage {

	protected static final String EMPTY_STRING = ""; //$NON-NLS-1$

	private DirectoryFieldEditorWithSearch xlToolsDirectory;

	public XlToolsPreferencesPage() {
		this(GRID);
	}

	/**
	 * @param style
	 */
	public XlToolsPreferencesPage(int style) {
		this(EMPTY_STRING, style);
	}

	/**
	 * @param title
	 * @param style
	 */
	public XlToolsPreferencesPage(String title, int style) {
		this(title, null, style);
	}

	public XlToolsPreferencesPage(String title, ImageDescriptor image, int style) {
		super(title, image, style);
		// Set the preference store for the preference page.
		IPreferenceStore store = XlManagedBuilderUIPlugin.getDefault()
				.getPreferenceStore();
		setPreferenceStore(store);
	}

	protected void createFieldEditors() {

		Composite fieldEditorParent = getFieldEditorParent();

		// Field for XL tools path
		this.xlToolsDirectory = new DirectoryFieldEditorWithSearch(
				XlToolsProperties.xlToolsPath,
				XlToolsProperties.xlToolsPathLabel, fieldEditorParent);
		this.xlToolsDirectory.setEmptyStringAllowed(false);
		addField(this.xlToolsDirectory);

	}

	/**
	 * Initializes all fields editors, searchers and property listeners.
	 */
	protected void initialize() {
		super.initialize();
		this.xlToolsDirectory.addSearcher(new XlToolsSearcher(
				this.xlToolsDirectory, getFieldEditorParent()));
	}

	public void init(IWorkbench workbench) {
		// Nothing to do.
	}

}
