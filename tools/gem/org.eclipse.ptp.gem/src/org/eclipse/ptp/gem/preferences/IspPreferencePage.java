/*******************************************************************************
 * Copyright (c) 2009, 2013 University of Utah School of Computing
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

package org.eclipse.ptp.gem.preferences;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.ptp.gem.GemPlugin;
import org.eclipse.ptp.gem.messages.Messages;
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
 * This page is used to modify ISP related preferences for the GEM plug-in. They
 * are stored in the preference store that belongs to the main plug-in class.
 */
public class IspPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	public IspPreferencePage() {
		super(GRID);
		setPreferenceStore(GemPlugin.getDefault().getPreferenceStore());
	}

	/**
	 * Creates the field editors. Field editors are abstractions of the common
	 * GUI blocks needed to manipulate various types of preferences. Each field
	 * editor knows how to save and restore itself.
	 */
	@Override
	public void createFieldEditors() {
		final Composite fieldEditorParent = this.getFieldEditorParent();

		// Create group for BooleanFieldEditors
		final Group prefsGroup = new Group(fieldEditorParent, SWT.NULL);
		prefsGroup.setText(Messages.IspPreferencePage_0);

		// Create BooleanFieldEditors for command line options.
		addField(new BooleanFieldEditor(PreferenceConstants.GEM_PREF_FIB, Messages.IspPreferencePage_1, prefsGroup));
		addField(new BooleanFieldEditor(PreferenceConstants.GEM_PREF_MPICALLS, Messages.IspPreferencePage_2, prefsGroup));
		addField(new BooleanFieldEditor(PreferenceConstants.GEM_PREF_OPENMP, Messages.IspPreferencePage_3, prefsGroup));
		addField(new BooleanFieldEditor(PreferenceConstants.GEM_PREF_BLOCK, Messages.IspPreferencePage_4, prefsGroup));
		addField(new BooleanFieldEditor(PreferenceConstants.GEM_PREF_REPORT, Messages.IspPreferencePage_5, prefsGroup));
		addField(new BooleanFieldEditor(PreferenceConstants.GEM_PREF_UNIXSOCKETS, Messages.IspPreferencePage_6, prefsGroup));
		addField(new BooleanFieldEditor(PreferenceConstants.GEM_PREF_VERBOSE, Messages.IspPreferencePage_7, prefsGroup));

		// Vertical spacer
		new Label(fieldEditorParent, SWT.NULL).setLayoutData(new GridData(SWT.FILL, SWT.NULL, true, false, 3, 1));

		// Create group of miscellaneous FieldEditors
		final Group miscGroup = new Group(fieldEditorParent, SWT.NULL);
		miscGroup.setText(Messages.IspPreferencePage_8);
		addField(new IntegerFieldEditor(PreferenceConstants.GEM_PREF_PORTNUM, Messages.IspPreferencePage_9, miscGroup));
		addField(new IntegerFieldEditor(PreferenceConstants.GEM_PREF_REPORTNUM, Messages.IspPreferencePage_10, miscGroup));
		addField(new StringFieldEditor(PreferenceConstants.GEM_PREF_HOSTNAME, Messages.IspPreferencePage_17, miscGroup));

		// Vertical spacer
		new Label(fieldEditorParent, SWT.NULL).setLayoutData(new GridData(SWT.FILL, SWT.NULL, true, false, 3, 1));

		// Create group for GEM-path FieldEditors
		final Group gemPathsGroup = new Group(fieldEditorParent, SWT.NULL);
		gemPathsGroup.setText(Messages.IspPreferencePage_11);
		addField(new DirectoryFieldEditor(PreferenceConstants.GEM_PREF_ISPEXE_PATH, Messages.IspPreferencePage_12, gemPathsGroup));
		addField(new DirectoryFieldEditor(PreferenceConstants.GEM_PREF_ISPCC_PATH, Messages.IspPreferencePage_13, gemPathsGroup));
		addField(new DirectoryFieldEditor(PreferenceConstants.GEM_PREF_ISPCPP_PATH, Messages.IspPreferencePage_14, gemPathsGroup));
		addField(new DirectoryFieldEditor(PreferenceConstants.GEM_PREF_HBV_PATH, Messages.IspPreferencePage_15, gemPathsGroup));

		// Vertical spacer
		new Label(fieldEditorParent, SWT.NULL).setLayoutData(new GridData(SWT.FILL, SWT.NULL, true, false, 3, 1));

		// Create group for GEM-path FieldEditors
		final Group remoteGemPathsGroup = new Group(fieldEditorParent, SWT.NULL);
		remoteGemPathsGroup.setText(Messages.IspPreferencePage_18);
		addField(new StringFieldEditor(PreferenceConstants.GEM_PREF_REMOTE_ISPEXE_PATH, Messages.IspPreferencePage_19,
				remoteGemPathsGroup));
		addField(new StringFieldEditor(PreferenceConstants.GEM_PREF_REMOTE_ISPCC_PATH, Messages.IspPreferencePage_20,
				remoteGemPathsGroup));
		addField(new StringFieldEditor(PreferenceConstants.GEM_PREF_REMOTE_ISPCPP_PATH, Messages.IspPreferencePage_21,
				remoteGemPathsGroup));

		// Do the grid layout work for each group.
		doLayoutAndData(prefsGroup, 1, 300);
		doLayoutAndData(miscGroup, 3, 300);
		doLayoutAndData(gemPathsGroup, 3, 300);
		doLayoutAndData(remoteGemPathsGroup, 3, 300);

		// Help button setup
		final Button helpButton = new Button(fieldEditorParent, SWT.PUSH);
		helpButton.setLayoutData(new GridData(SWT.LEFT, SWT.NULL, false, false, 3, 1));
		helpButton.setImage(GemPlugin.getImageDescriptor("icons/help-contents.gif").createImage()); //$NON-NLS-1$
		helpButton.setToolTipText(Messages.IspPreferencePage_16);

		helpButton.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				// do nothing
			}

			public void widgetSelected(SelectionEvent e) {
				PlatformUI.getWorkbench().getHelpSystem()
						.displayHelpResource("/org.eclipse.ptp.gem.help/html/preferences.html#ispPrefs"); //$NON-NLS-1$
			}
		});
	}

	/*
	 * Performs Layout of Grid and data therein for the specified group.
	 */
	private void doLayoutAndData(Group group, int numColumns, int widthHint) {
		final GridLayout gl = new GridLayout(numColumns, false);
		group.setLayout(gl);
		final GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 1;
		gd.widthHint = widthHint;
		group.setLayoutData(gd);
	}

	/*
	 * @see
	 * org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
		// do nothing
	}

}
