/**********************************************************************
 * Copyright (c) 2004 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
***********************************************************************/

package org.eclipse.fdt.internal.ui.refactoring;

import org.eclipse.jface.preference.IPreferenceStore;


import org.eclipse.fdt.internal.corext.refactoring.base.RefactoringStatus;
import org.eclipse.fdt.ui.CUIPlugin;
import org.eclipse.fdt.ui.PreferenceConstants;

public class RefactoringPreferences {

	public static final String PREF_ERROR_PAGE_SEVERITY_THRESHOLD= PreferenceConstants.REFACTOR_ERROR_PAGE_SEVERITY_THRESHOLD;
	public static final String PREF_SAVE_ALL_EDITORS= PreferenceConstants.REFACTOR_SAVE_ALL_EDITORS;
	
	public static int getCheckPassedSeverity() {
		String value= CUIPlugin.getDefault().getPreferenceStore().getString(PREF_ERROR_PAGE_SEVERITY_THRESHOLD);
		try {
			return Integer.valueOf(value).intValue() - 1;
		} catch (NumberFormatException e) {
			return RefactoringStatus.ERROR;
		}
	}
	
	public static int getStopSeverity() {
		switch (getCheckPassedSeverity()) {
			case RefactoringStatus.OK:
				return RefactoringStatus.INFO;
			case RefactoringStatus.INFO:
				return RefactoringStatus.WARNING;
			case RefactoringStatus.WARNING:
				return RefactoringStatus.ERROR;
		}
		return RefactoringStatus.FATAL;
	}
	
	public static boolean getSaveAllEditors() {
		IPreferenceStore store= CUIPlugin.getDefault().getPreferenceStore();
		return store.getBoolean(PREF_SAVE_ALL_EDITORS);
	}
	
	public static void setSaveAllEditors(boolean save) {
		IPreferenceStore store= CUIPlugin.getDefault().getPreferenceStore();
		store.setValue(RefactoringPreferences.PREF_SAVE_ALL_EDITORS, save);
	}	
}
