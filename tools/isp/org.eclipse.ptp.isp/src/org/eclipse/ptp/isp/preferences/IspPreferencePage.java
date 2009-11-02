/*******************************************************************************
 * Copyright (c) 2009 University of Utah School of Computing
 * 50 S Central Campus Dr. 3190 Salt Lake City, UT 84112
 * http://www.cs.utah.edu/formal_verification/
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alan Humphrey - Initial API and implementation
 *    Christopher Derrick - Initial API and implementation
 *    Prof. Ganesh Gopalakrishnan - Project Advisor
 *******************************************************************************/

package org.eclipse.ptp.isp.preferences;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.ptp.isp.ISPPlugin;
import org.eclipse.ptp.isp.messages.Messages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;

/**
 * This page is used to modify preferences for the ISP plug-in. They are stored
 * in the preference store that belongs to the main plug-in class.
 */

public class IspPreferencePage extends FieldEditorPreferencePage implements
		IWorkbenchPreferencePage {

	public IspPreferencePage() {
		super(GRID);
		setPreferenceStore(ISPPlugin.getDefault().getPreferenceStore());
	}

	/**
	 * Creates the field editors. Field editors are abstractions of the common
	 * GUI blocks needed to manipulate various types of preferences. Each field
	 * editor knows how to save and restore itself.
	 * 
	 * @param none
	 * @return void
	 */
	public void createFieldEditors() {
		final Composite fieldEditorParent = this.getFieldEditorParent();

		// Create group for BooleanFieldEditors
		Group prefsGroup = new Group(fieldEditorParent, SWT.NULL);
		prefsGroup.setText("Command Line Options"); //$NON-NLS-1$

		// Create BooleanFieldEditors for command line options.
		addField(new BooleanFieldEditor(
				PreferenceConstants.ISP_PREF_FIB_OPTION, "Enable FIB", //$NON-NLS-1$
				prefsGroup));
		addField(new BooleanFieldEditor(
				PreferenceConstants.ISP_PREF_MPICALLS_OPTION,
				"Log Total MPI Calls", prefsGroup)); //$NON-NLS-1$
		addField(new BooleanFieldEditor(
				PreferenceConstants.ISP_PREF_OPENMP_OPTION, "Enable OpenMP", //$NON-NLS-1$
				prefsGroup));
		addField(new BooleanFieldEditor(
				PreferenceConstants.ISP_PREF_BLOCK_OPTION,
				"Use Blocking sends", prefsGroup)); //$NON-NLS-1$
		addField(new BooleanFieldEditor(
				PreferenceConstants.ISP_PREF_REPORT_OPTION, "Report Progress", //$NON-NLS-1$
				prefsGroup));
		addField(new BooleanFieldEditor(PreferenceConstants.ISP_PREF_VERBOSE,
				"Verbose Mode", prefsGroup)); //$NON-NLS-1$

		// Vertical spacer
		new Label(fieldEditorParent, SWT.NULL).setLayoutData(new GridData(
				SWT.FILL, SWT.NULL, true, false, 3, 1));

		// Create group of miscellaneous FieldEditors
		Group miscGroup = new Group(fieldEditorParent, SWT.NULL);
		miscGroup.setText("Miscellaneous Options"); //$NON-NLS-1$

		addField(new IntegerFieldEditor(PreferenceConstants.ISP_PREF_PORTNUM,
				"Port:", miscGroup)); //$NON-NLS-1$
		addField(new IntegerFieldEditor(PreferenceConstants.ISP_PREF_NUMPROCS,
				"Number of Processes:", miscGroup)); //$NON-NLS-1$
		addField(new IntegerFieldEditor(PreferenceConstants.ISP_PREF_REPORTNUM,
				"Report Progress Every (n) MPI Calls:", miscGroup)); //$NON-NLS-1$
		addField(new BooleanFieldEditor(PreferenceConstants.ISP_PREF_CLRCON,
				"Clear ISP Console on Each Run", miscGroup)); //$NON-NLS-1$

		// Vertical spacer
		new Label(fieldEditorParent, SWT.NULL).setLayoutData(new GridData(
				SWT.FILL, SWT.NULL, true, false, 3, 1));

		// Create group for ISP-path FieldEditors
		Group ispPathsGroup = new Group(fieldEditorParent, SWT.NULL);
		ispPathsGroup.setText("ISP Paths"); //$NON-NLS-1$

		addField(new DirectoryFieldEditor(
				PreferenceConstants.ISP_PREF_ISPEXE_PATH, "isp executable:", //$NON-NLS-1$
				ispPathsGroup));
		addField(new DirectoryFieldEditor(
				PreferenceConstants.ISP_PREF_ISPCC_PATH, "ispcc script:", //$NON-NLS-1$
				ispPathsGroup));
		addField(new DirectoryFieldEditor(PreferenceConstants.ISP_PREF_UI_PATH,
				"ispUI script:", ispPathsGroup)); //$NON-NLS-1$

		// Do the grid layout work for each group.
		doLayoutAndData(prefsGroup, 1, 300);
		doLayoutAndData(miscGroup, 3, 300);
		doLayoutAndData(ispPathsGroup, 3, 300);

		// Help button setup
		final Button helpButton = new Button(fieldEditorParent, SWT.PUSH);
		helpButton.setLayoutData(new GridData(SWT.LEFT, SWT.NULL, false, false,
				3, 1));
		helpButton.setImage(ISPPlugin.getImageDescriptor(
				"icons/help-contents.gif").createImage()); //$NON-NLS-1$
		helpButton.setToolTipText(Messages.IspPreferencePage_17);

		helpButton.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				PlatformUI.getWorkbench().getHelpSystem().displayHelpResource(
						"/org.eclipse.ptp.isp.help/html/preferences.html"); //$NON-NLS-1$
			}
		});
	}

	/*
	 * @see
	 * org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
	}

	/*
	 * Performs Layout of Grid and data therein for the specified group.
	 */
	private void doLayoutAndData(Group group, int numColumns, int widthHint) {
		GridLayout gl = new GridLayout(numColumns, false);
		group.setLayout(gl);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 1;
		gd.widthHint = widthHint;
		group.setLayoutData(gd);
	}

}
