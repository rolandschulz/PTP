/**********************************************************************
 * Copyright (c) 2007,2010 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.pldt.lapi.prefs;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PathEditor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ptp.pldt.lapi.Activator;
import org.eclipse.ptp.pldt.lapi.internal.core.IDs;
import org.eclipse.ptp.pldt.lapi.messages.Messages;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * Preference page based on FieldEditorPreferencePage
 * 
 * @author xue
 */

public class LAPIPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
	private static final String INCLUDES_PREFERENCE_LABEL = Messages.LAPIPreferencePage_includes_preference_label;
	private static final String INCLUDES_PREFERENCE_BROWSE = Messages.LAPIPreferencePage_includes_preference_browse_dialog_title;
	private static final String LAPI_RECOGNIZE_APIS_BY_PREFIX_ALONE_LABEL = Messages.LAPIPreferencePage_recognizeAPISByPrefixAlone;

	public LAPIPreferencePage() {
		super(FLAT);
		initPreferenceStore();
	}

	public LAPIPreferencePage(int style) {
		super(style);
		initPreferenceStore();
	}

	public LAPIPreferencePage(String title, ImageDescriptor image, int style) {
		super(title, image, style);
		initPreferenceStore();
	}

	public LAPIPreferencePage(String title, int style) {
		super(title, style);
		initPreferenceStore();
	}

	/**
	 * Init preference store and set the preference store for the preference
	 * page
	 */
	private void initPreferenceStore() {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		setPreferenceStore(store);
	}

	public void init(IWorkbench workbench) {
	}

	@Override
	protected void createFieldEditors() {
		BooleanFieldEditor bPrefix = new BooleanFieldEditor(IDs.RECOGNIZE_APIS_BY_PREFIX_ALONE,
				LAPI_RECOGNIZE_APIS_BY_PREFIX_ALONE_LABEL, getFieldEditorParent());
		addField(bPrefix);

		PathEditor pathEditor = new PathEditor(IDs.PREF_INCLUDES, INCLUDES_PREFERENCE_LABEL,
				INCLUDES_PREFERENCE_BROWSE, getFieldEditorParent());
		addField(pathEditor);

		// "Use default?"
		// BooleanFieldEditor bed = new
		// BooleanFieldEditor(LAPI_HELP_DEFAULT_ID,LAPI_HELP_DEFAULT,getFieldEditorParent());
		// addField(bed);
		/*
		 * int numCol=1; RadioGroupFieldEditor choiceFE = new
		 * RadioGroupFieldEditor(LAPI_WHICH_HELP_ID, LAPI_HELP, numCol, new
		 * String[][] { { LAPI_HELP_DEFAULT, "choice1" }, {
		 * LAPI_HELP_AIX+LAPI_LOCATION_AIX, "choice2" }, {
		 * LAPI_HELP_LINUX+LAPI_LOCATION_LINUX, "Choice3" }, { LAPI_HELP_OTHER,
		 * "Choice4" }}, getFieldEditorParent()); addField(choiceFE);
		 * 
		 * StringFieldEditor otherLoc=new StringFieldEditor(LAPI_HELP_OTHER_ID,
		 * LAPI_HELP_OTHER,getFieldEditorParent()); addField(otherLoc);
		 */

	}

}
