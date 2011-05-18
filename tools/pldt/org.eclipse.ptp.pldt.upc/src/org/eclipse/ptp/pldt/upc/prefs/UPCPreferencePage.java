/**********************************************************************
 * Copyright (c) 2008,2011 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.pldt.upc.prefs;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PathEditor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ptp.pldt.upc.UPCPlugin;
import org.eclipse.ptp.pldt.upc.internal.UPCIDs;
import org.eclipse.ptp.pldt.upc.messages.Messages;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * Preference page based on FieldEditorPreferencePage
 * 
 * @author xue
 */

public class UPCPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
	private static final String INCLUDES_PREFERENCE_LABEL = Messages.UPCPreferencePage_preference_label_upc_include_paths;
	private static final String INCLUDES_PREFERENCE_BROWSE = Messages.UPCPreferencePage_please_choose_a_directory;
	private static final String UPC_HELP = Messages.UPCPreferencePage_location_of_upc_help_files;
	private static final String UPC_HELP_DEFAULT = Messages.UPCPreferencePage_use_default;
	private static final String UPC_HELP_DEFAULT_ID = "upcHelpUseDefault"; //$NON-NLS-1$
	// private static final String UPC_HELP_LINUX="Use Linux location: ";
	// private static final String UPC_HELP_AIX="Use AIX location: ";
	// private static final String UPC_HELP_OTHER="Other:";
	// private static final String UPC_HELP_OTHER_ID="upcHelpOther";

	// private static final String
	// UPC_LOCATION_AIX="/opt/rsct/lapi/eclipse/help";
	// private static final String
	// UPC_LOCATION_LINUX="opt/ibmhpc/lapi/eclipse/help";

	private static final String UPC_RECOGNIZE_APIS_BY_PREFIX_ALONE_LABEL = Messages.UPCPreferencePage_recognize_APIs_by_prefix_alone; //$NON-NLS-1$

	private static final String UPC_WHICH_HELP_ID = "default"; // alternatives are: default, aix, linux, other //$NON-NLS-1$

	public UPCPreferencePage() {
		super(FLAT);
		initPreferenceStore();
	}

	public UPCPreferencePage(int style) {
		super(style);
		initPreferenceStore();
	}

	public UPCPreferencePage(String title, ImageDescriptor image, int style) {
		super(title, image, style);
		initPreferenceStore();
	}

	public UPCPreferencePage(String title, int style) {
		super(title, style);
		initPreferenceStore();
	}

	/**
	 * Init preference store and set the preference store for the preference
	 * page
	 */
	private void initPreferenceStore() {
		IPreferenceStore store = UPCPlugin.getDefault().getPreferenceStore();
		setPreferenceStore(store);
	}

	public void init(IWorkbench workbench) {
	}

	@Override
	protected void createFieldEditors() {
		BooleanFieldEditor bPrefix = new BooleanFieldEditor(UPCIDs.UPC_RECOGNIZE_APIS_BY_PREFIX_ALONE,
				UPC_RECOGNIZE_APIS_BY_PREFIX_ALONE_LABEL, getFieldEditorParent());
		addField(bPrefix);

		PathEditor pathEditor = new PathEditor(UPCIDs.UPC_INCLUDES, INCLUDES_PREFERENCE_LABEL, INCLUDES_PREFERENCE_BROWSE,
				getFieldEditorParent());
		addField(pathEditor);

		// "Use default?"
		// BooleanFieldEditor bed = new
		// BooleanFieldEditor(UPC_HELP_DEFAULT_ID,UPC_HELP_DEFAULT,getFieldEditorParent());
		// addField(bed);
		/*
		 * int numCol=1; RadioGroupFieldEditor choiceFE = new
		 * RadioGroupFieldEditor(UPC_WHICH_HELP_ID, UPC_HELP, numCol, new
		 * String[][] { { UPC_HELP_DEFAULT, "choice1" }, {
		 * UPC_HELP_AIX+UPC_LOCATION_AIX, "choice2" }, {
		 * UPC_HELP_LINUX+UPC_LOCATION_LINUX, "Choice3" }, { UPC_HELP_OTHER,
		 * "Choice4" }}, getFieldEditorParent()); addField(choiceFE);
		 * 
		 * StringFieldEditor otherLoc=new StringFieldEditor(UPC_HELP_OTHER_ID,
		 * UPC_HELP_OTHER,getFieldEditorParent()); addField(otherLoc);
		 */

	}

}
