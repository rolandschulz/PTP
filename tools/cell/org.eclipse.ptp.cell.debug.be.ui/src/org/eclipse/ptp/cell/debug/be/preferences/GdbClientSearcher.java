/******************************************************************************
 * Copyright (c) 2007 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *****************************************************************************/
package org.eclipse.ptp.cell.debug.be.preferences;

import java.io.File;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ptp.cell.debug.be.debug.Debug;
import org.eclipse.ptp.cell.debug.be.ui.preferences.CellDebugBEPreferencesMessages;
import org.eclipse.ptp.cell.preferences.core.SearcherMessages;
import org.eclipse.ptp.cell.preferences.core.StringFieldEditorPreferenceSearcher;
import org.eclipse.ptp.cell.utils.linux.findutils.Find;
import org.eclipse.ptp.cell.utils.linux.which.Which;
import org.eclipse.ptp.cell.utils.packagemanager.PackageManagementSystemManager;
import org.eclipse.ptp.cell.utils.searcher.SearchFailedException;
import org.eclipse.swt.widgets.Composite;


/**
 * @author laggarcia
 * @since 3.0.0
 */
public class GdbClientSearcher extends StringFieldEditorPreferenceSearcher {

	protected static final String GDB_CLIENT_EXECUTABLE = BEDebugPreferencesDefaults.getString("gdbClientExecutable"); //$NON-NLS-1$

	protected static final String GDB_PACKAGE_NAME = BEDebugPreferencesDefaults.getString("gdbPackageName"); //$NON-NLS-1$

	protected static final String GDB_SEARCH_ROOT_DIRECTORY = BEDebugPreferencesDefaults.getString("gdbSearchRootDirectory"); //$NON-NLS-1$

	/**
	 * @param stringFieldEditor
	 */
	public GdbClientSearcher(StringFieldEditor stringFieldEditor, Composite parent) {
		super(stringFieldEditor, parent);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.cell.preferences.core.StringFieldEditorPreferenceSearcher#fastSearch()
	 */
	protected void fastSearch() throws SearchFailedException {
		// Try to find out if the current preference value is a valid install
		// directory
		String currentPreferenceValue = this.stringFieldEditor.getStringValue();
		if (isValidGdbClient(currentPreferenceValue)) {
			// Current preference value is a valid install directory.
			Debug.POLICY.trace(Debug.DEBUG_SEARCHER, "Current value is valid"); //$NON-NLS-1$
			return;
		}
		// The current preference value is not valid. The serach continues.
		// Compare the current value with the default value.
		String defaultPreferenceValue = this.preferenceStore.getDefaultString(this.preferenceName);
		if ((!currentPreferenceValue.equals(defaultPreferenceValue)) && (isValidGdbClient(defaultPreferenceValue))) {
			// Current preference value is different from default preference
			// value and the default preference value is a valid install
			// directory.
			Debug.POLICY.trace(Debug.DEBUG_SEARCHER, "Default value is valid"); //$NON-NLS-1$
			setText(defaultPreferenceValue);
			return;
		}
		// If current and default values are the same, that means that the
		// default value is not valid as we knew the current value was not valid
		// If the values are different and you reached here, that means that the
		// default valus is also not valid.
		// Current and default preference values are invalid. The search
		// continues.
		// The next two searches are valid just for Linux systems
		String gdbClient;
		if (Platform.getOS().equals(Platform.OS_LINUX)) {
			gdbClient = Which.which(GDB_CLIENT_EXECUTABLE);
			if (isValidGdbClient(gdbClient)) {
				Debug.POLICY.trace(Debug.DEBUG_SEARCHER, "Value returned from 'which' is valid"); //$NON-NLS-1$
				setText(gdbClient);
				return;
			}
		}
		// ppu-gdb is not in the $PATH environment variable. The search
		// continues
		// Verify if ppu-gdb rpm package is installed or not
		/*gdbClient = PackageManagementSystemManager.getPackageManager().searchFileInQueryListAndReturnFullPath(GDB_PACKAGE_NAME, GDB_CLIENT_EXECUTABLE);
		if (isValidGdbClient(gdbClient)) {
			Debug.POLICY.trace(Debug.DEBUG_SEARCHER, "Value returned from package management system is valid"); //$NON-NLS-1$
			setText(gdbClient);
			return;
		}*/
		Debug.POLICY.trace(Debug.DEBUG_SEARCHER, "All fast searches failed,"); //$NON-NLS-1$
		// None of the searches returned successfully.
		throw new SearchFailedException(SearcherMessages.fastSearchFailedMessage);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.cell.preferences.core.StringFieldEditorPreferenceSearcher#longSearch()
	 */
	protected void longSearch() {
		new Thread() {
			public void run() {
				try {
					Debug.POLICY.trace(Debug.DEBUG_SEARCHER, "Start long searcher for GNU tools. Start 'find'."); //$NON-NLS-1$
					if (Platform.getOS().equals(Platform.OS_LINUX)) {
						String gdbClient = Find.findFile(GDB_SEARCH_ROOT_DIRECTORY, GDB_CLIENT_EXECUTABLE);
						Debug.POLICY.trace(Debug.DEBUG_SEARCHER, "'find' finished."); //$NON-NLS-1$
						if (isValidGdbClient(gdbClient)) {
							setText(gdbClient);
							Debug.POLICY.trace(Debug.DEBUG_SEARCHER, "Long searcher successfull."); //$NON-NLS-1$
							showInfoMessage(SearcherMessages.searchSucceededDialogTitle, NLS.bind(SearcherMessages.searchSucceededDialogMessage,
									CellDebugBEPreferencesMessages.getString("CellDebugBEPreferencePage.0"))); //$NON-NLS-1$
							return;
						} else {
							Debug.POLICY.trace(Debug.DEBUG_SEARCHER, "Long searcher found nothing."); //$NON-NLS-1$
							showInfoMessage(SearcherMessages.searchFailedDialogTitle, NLS.bind(SearcherMessages.searchFailedDialogMessage,
									CellDebugBEPreferencesMessages.getString("CellDebugBEPreferencePage.0"))); //$NON-NLS-1$
						}
					} else {
						Debug.POLICY.trace(Debug.DEBUG_SEARCHER, "Not a Linux platform, no long search is available."); //$NON-NLS-1$
					}
				} catch (Exception e) {
					Debug.POLICY.error(Debug.DEBUG_SEARCHER, e);
					Debug.POLICY.logError(e);
				} finally {
					Debug.POLICY.trace(Debug.DEBUG_SEARCHER, "Finished long searcher for GDB tools."); //$NON-NLS-1$
				}
			}
		}.start();
	}

	protected boolean isValidGdbClient(String gdbClientPath) {
		if (gdbClientPath != null) {
			File gdbClient = new File(gdbClientPath);
			if (gdbClient.exists() && gdbClient.isFile()) {
				return true;
			}
		}
		return false;
	}

}
