/******************************************************************************
 * Copyright (c) 2007 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *

*****************************************************************************/
package org.eclipse.ptp.cell.pdt.xml.preferences.ui;

import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ptp.cell.pdt.xml.Activator;
import org.eclipse.ptp.cell.pdt.xml.core.PdtXmlBean;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;


/**
 * 
 * @author Richard Maciel
 *
 */
public class PdtPreferencesPage extends FieldEditorPreferencePage implements
		IWorkbenchPreferencePage {

	DirectoryFieldEditor eventGroupDirectory;
	
	public PdtPreferencesPage() {
		super(Messages.PdtPreferencesPage_PageTitle, null, GRID);
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		setPreferenceStore(store);
	}
	
	@Override
	protected void createFieldEditors() {
		setTitle(Messages.PdtPreferencesPage_PageTitle);
		eventGroupDirectory = new DirectoryFieldEditor(PdtXmlBean.ATTR_EVENT_GROUP_DIR, Messages.PdtPreferencesPage_FieldEditor_EventGroupsDir, 
				getFieldEditorParent());
		eventGroupDirectory.setEmptyStringAllowed(false);
		addField(eventGroupDirectory);
		
	}

	public void init(IWorkbench workbench) {
		
	}

	

}
