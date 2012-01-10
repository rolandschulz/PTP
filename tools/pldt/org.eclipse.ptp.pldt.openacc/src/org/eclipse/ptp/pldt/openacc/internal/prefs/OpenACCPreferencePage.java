/**********************************************************************
 * Copyright (c) 2007, 2010, 2011 IBM Corporation and University of Illinois.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Jeff Overbey (Illinois) - adaptation to OpenACC
 *******************************************************************************/
package org.eclipse.ptp.pldt.openacc.internal.prefs;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PathEditor;
import org.eclipse.ptp.pldt.openacc.internal.Activator;
import org.eclipse.ptp.pldt.openacc.internal.IDs;
import org.eclipse.ptp.pldt.openacc.internal.messages.Messages;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * Preference page based on FieldEditorPreferencePage
 * 
 * @author xue
 * @author Jeff Overbey (Illinois)
 */
public class OpenACCPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
	private static final String INCLUDES_PREFERENCE_LABEL = Messages.OpenACCPreferencePage_includes_preference_label;
	private static final String INCLUDES_PREFERENCE_BROWSE = Messages.OpenACCPreferencePage_includes_preference_browse_dialog_title;
	private static final String OpenACC_RECOGNIZE_APIS_BY_PREFIX_ALONE_LABEL = Messages.OpenACCPreferencePage_recognizeAPISByPrefixAlone;

	/**
	 * Constructor. Invoked dynamically due to reference in plugin.xml.
	 */
	public OpenACCPreferencePage() {
		super(FLAT);
		initPreferenceStore();
	}

	/**
	 * Init preference store and set the preference store for the preference
	 * page
	 */
	private void initPreferenceStore() {
		final IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		setPreferenceStore(store);
	}

	@Override
	public void init(IWorkbench workbench) {
	}

	@Override
	protected void createFieldEditors() {
		final BooleanFieldEditor bPrefix = new BooleanFieldEditor(IDs.PREF_RECOGNIZE_APIS_BY_PREFIX_ALONE,
				OpenACC_RECOGNIZE_APIS_BY_PREFIX_ALONE_LABEL, getFieldEditorParent());
		addField(bPrefix);

		final PathEditor pathEditor = new PathEditor(IDs.PREF_INCLUDES, INCLUDES_PREFERENCE_LABEL,
				INCLUDES_PREFERENCE_BROWSE, getFieldEditorParent());
		addField(pathEditor);
	}
}