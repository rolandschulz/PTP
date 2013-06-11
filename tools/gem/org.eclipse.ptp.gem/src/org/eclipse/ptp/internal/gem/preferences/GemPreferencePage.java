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

package org.eclipse.ptp.internal.gem.preferences;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.ptp.internal.gem.GemPlugin;
import org.eclipse.ptp.internal.gem.messages.Messages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;

/**
 * This page is used to modify preferences for the GEM plug-in. They are stored
 * in the preference store that belongs to the main plug-in class.
 */
public class GemPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	public GemPreferencePage() {
		super(GRID);
		setPreferenceStore(GemPlugin.getDefault().getPreferenceStore());
	}

	/**
	 * Creates field editors and does layout for the main GEM preference page.
	 */
	@Override
	public void createFieldEditors() {
		final Composite fieldEditorParent = this.getFieldEditorParent();

		// Create group of miscellaneous FieldEditors
		final Group prefGroup = new Group(fieldEditorParent, SWT.NULL);
		prefGroup.setText(Messages.GemPreferencePage_0);
		addField(new IntegerFieldEditor(PreferenceConstants.GEM_PREF_NUMPROCS, Messages.GemPreferencePage_1, prefGroup));
		addField(new BooleanFieldEditor(PreferenceConstants.GEM_PREF_CLRCON, Messages.GemPreferencePage_3, prefGroup));
		addField(new BooleanFieldEditor(PreferenceConstants.GEM_PREF_REQUEST_ARGS, Messages.GemPreferencePage_10, prefGroup));
		addField(new RadioGroupFieldEditor(PreferenceConstants.GEM_ACTIVE_VIEW, Messages.GemPreferencePage_5, 3,
				new String[][] { { Messages.GemPreferencePage_7, PreferenceConstants.GEM_ANALYZER },
						{ Messages.GemPreferencePage_8, PreferenceConstants.GEM_BROWSER },
						{ Messages.GemPreferencePage_9, PreferenceConstants.GEM_CONSOLE }
				}, prefGroup, false));

		// Do the grid layout work for each group.
		doLayoutAndData(prefGroup, 3, 300);

		// Help button setup
		final Button helpButton = new Button(fieldEditorParent, SWT.PUSH);
		helpButton.setLayoutData(new GridData(SWT.LEFT, SWT.NULL, false, false, 3, 1));
		helpButton.setImage(GemPlugin.getImageDescriptor("icons/help-contents.gif").createImage()); //$NON-NLS-1$
		helpButton.setToolTipText(Messages.GemPreferencePage_4);

		helpButton.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				// do nothing
			}

			public void widgetSelected(SelectionEvent e) {
				PlatformUI.getWorkbench().getHelpSystem()
						.displayHelpResource("/org.eclipse.ptp.gem.help/html/preferences.html#gemPrefs"); //$NON-NLS-1$
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
