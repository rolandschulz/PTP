/**********************************************************************
 * Copyright (c) 2010,2011 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.pldt.common.prefs;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ptp.pldt.common.CommonPlugin;
import org.eclipse.ptp.pldt.common.messages.Messages;
import org.eclipse.ptp.pldt.internal.common.IDs;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * This class represents a preference page - for PTP's PLDT - that
 * is contributed to the Preferences dialog. By
 * subclassing <samp>FieldEditorPreferencePage</samp>, we
 * can use the field support built into JFace that allows
 * us to create a page that is small and knows how to
 * save, restore and apply itself.
 * <p>
 * This page is used to modify preferences only. They are stored in the preference store that belongs to the main plug-in class.
 * That way, preferences can be accessed directly via the preference store.
 */

public class PLDTPreferencePage
		extends FieldEditorPreferencePage
		implements IWorkbenchPreferencePage {

	public PLDTPreferencePage() {
		super(GRID);
		setPreferenceStore(CommonPlugin.getDefault().getPreferenceStore());
		setDescription(Messages.PLDTPreferencePage_ptp_par_lang_dev_tools);
	}

	/**
	 * Creates the field editors. Field editors are abstractions of
	 * the common GUI blocks needed to manipulate various types
	 * of preferences. Each field editor knows how to save and
	 * restore itself.
	 */
	public void createFieldEditors() {
		String conf = Messages.PLDTPreferencePage_show_confirmation_q;
		BooleanFieldEditor bed = new BooleanFieldEditor(IDs.SHOW_ANALYSIS_CONFIRMATION, conf, getFieldEditorParent());
		// StringFieldEditor sed = new StringFieldEditor(MpiIDs.MPI_BUILD_CMD, MPI_BUILD_COMMAND_LABEL,getFieldEditorParent());
		addField(bed);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
	}

}