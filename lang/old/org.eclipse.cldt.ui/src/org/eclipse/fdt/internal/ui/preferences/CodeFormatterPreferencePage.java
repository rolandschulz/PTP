/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.fdt.internal.ui.preferences;


import org.eclipse.core.runtime.IStatus;
import org.eclipse.fdt.internal.ui.ICHelpContextIds;
import org.eclipse.fdt.internal.ui.dialogs.StatusUtil;
import org.eclipse.fdt.ui.FortranUIPlugin;
import org.eclipse.fdt.ui.dialogs.CodeFormatterBlock;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;

/**
 * 
 */
public class CodeFormatterPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	CodeFormatterBlock fCodeFormatterBlock;
	
	public CodeFormatterPreferencePage() {
		setPreferenceStore(FortranUIPlugin.getDefault().getPreferenceStore());
		// only used when page is shown programatically
		setTitle(PreferencesMessages.getString("CodeFormatterPreferencePage.title"));		 //$NON-NLS-1$
		//setDescription(PreferencesMessages.getString("CodeFormatterPreferencePage.description")); //$NON-NLS-1$
		fCodeFormatterBlock= new CodeFormatterBlock(FortranUIPlugin.getDefault().getPluginPreferences());
	}
	

	/*
	 * @see IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */	
	public void init(IWorkbench workbench) {
	}

	/*
	 * @see PreferencePage#createContents(Composite)
	 */
	protected Control createContents(Composite parent) {
		Composite topPane = new Composite(parent, SWT.NONE);

		topPane.setLayout(new GridLayout());
		topPane.setLayoutData(new GridData(GridData.FILL_BOTH));

		
		applyDialogFont(parent);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(), ICHelpContextIds.CODEFORMATTER_PREFERENCE_PAGE);
		return fCodeFormatterBlock.createControl(topPane);
	}

	/*
	 * @see IPreferencePage#performOk()
	 */
	public boolean performOk() {
		fCodeFormatterBlock.performOk();
		return super.performOk();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#performDefaults()
	 */
	protected void performDefaults() {
		fCodeFormatterBlock.performDefaults();
		super.performDefaults();
	}
		
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.ui.wizards.IStatusChangeListener#statusChanged(org.eclipse.core.runtime.IStatus)
	 */
	public void statusChanged(IStatus status) {
		setValid(!status.matches(IStatus.ERROR));
		StatusUtil.applyToStatusLine(this, status);		
	}

}
