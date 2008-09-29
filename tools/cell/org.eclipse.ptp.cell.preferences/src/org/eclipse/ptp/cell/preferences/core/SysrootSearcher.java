/******************************************************************************
 * Copyright (c) 2008 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *****************************************************************************/
package org.eclipse.ptp.cell.preferences.core;

import java.io.File;

import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ptp.cell.preferences.debug.Debug;
import org.eclipse.ptp.cell.utils.linux.findutils.Find;
import org.eclipse.ptp.cell.utils.linux.which.Which;
import org.eclipse.ptp.cell.utils.packagemanager.PackageManagementSystemManager;
import org.eclipse.ptp.cell.utils.searcher.SearchFailedException;
import org.eclipse.swt.widgets.Composite;


/**
 * @author laggarcia
 *
 */
public class SysrootSearcher extends StringFieldEditorPreferenceSearcher {
	private String[] requiredFiles;
	
	/**
	 * @param stringFieldEditor
	 * @param parent
	 */
	public SysrootSearcher(StringFieldEditor stringFieldEditor, Composite parent) {
		super(stringFieldEditor, parent);
		
		requiredFiles = CellProperties.sysroot.split(CellProperties.separator);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.cell.preferences.core.StringFieldEditorPreferenceSearcher#fastSearch()
	 */
	@Override
	protected void fastSearch() throws SearchFailedException {

		// Try to find out if the current preference value is a valid install
		// directory
		String currentPreferenceValue = this.stringFieldEditor.getStringValue();
		if (isValidSysrootDirectory(currentPreferenceValue)) {
			// Current preference value is a valid install directory.
			Debug.POLICY.trace(Debug.DEBUG_SEARCHER, "Current value is valid"); //$NON-NLS-1$
			return;
		}
		// The current preference value is not valid. The search continues.
		// Compare the current value with the default value.
		String defaultPreferenceValue = this.preferenceStore
				.getDefaultString(this.preferenceName);
		if ((!currentPreferenceValue.equals(defaultPreferenceValue))
				&& (isValidSysrootDirectory(defaultPreferenceValue))) {
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
		// default values is also not valid.
		// Current and default preference values are invalid. The search
		// continues.
		// The next two searches are valid just for Linux systems
		String sysrootPath;
		if (Platform.getOS().equals(Platform.OS_LINUX)) {
			sysrootPath = new Path(Which
					.which(CellProperties.mainLib))
					.removeLastSegments(1).toOSString();
			if (isValidSysrootDirectory(sysrootPath)) {
				Debug.POLICY.trace(Debug.DEBUG_SEARCHER, "Value returned from 'which' is valid"); //$NON-NLS-1$
				setText(sysrootPath);
				return;
			}
		}
		// mainLib is not in the $PATH environment variable. The search
		// continues
		// Verify if the package is installed or not
		sysrootPath = PackageManagementSystemManager.getPackageManager()
				.searchFileInQueryListAndReturnInitialPathSegments(
						CellProperties.searchInPackage,
						File.separator
								+ CellProperties.mainLib);
		if (isValidSysrootDirectory(sysrootPath)) {
			Debug.POLICY.trace(Debug.DEBUG_SEARCHER, "Value returned from package management system is valid"); //$NON-NLS-1$
			setText(sysrootPath);
			return;
		}
		Debug.POLICY.trace(Debug.DEBUG_SEARCHER, "All fast searches failed,"); //$NON-NLS-1$
		
		// None of the searches returned successfully.
		throw new SearchFailedException(
				SearcherMessages.fastSearchFailedMessage);

	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.cell.preferences.core.StringFieldEditorPreferenceSearcher#longSearch()
	 */
	@Override
	protected void longSearch() {
		new Thread() {
			public void run() {
				try {
					Debug.POLICY.trace(Debug.DEBUG_SEARCHER, "Start long searcher for Sysroot. Start 'find'."); //$NON-NLS-1$
					if (Platform.getOS().equals(Platform.OS_LINUX)) {
						String systemsim_cellPath = new Path(Find.findFile(
								CellProperties.searchRootDirectory,
								CellProperties.mainLib))
								.removeLastSegments(1).toOSString();
							Debug.POLICY.trace(Debug.DEBUG_SEARCHER, "'find' finished."); //$NON-NLS-1$
						if (isValidSysrootDirectory(systemsim_cellPath)) {
							setText(systemsim_cellPath);
								Debug.POLICY.trace(Debug.DEBUG_SEARCHER, "Long searcher successfull."); //$NON-NLS-1$
							showInfoMessage(
									SearcherMessages.searchSucceededDialogTitle,
									NLS
											.bind(
													SearcherMessages.searchSucceededDialogMessage,
													CellProperties.sysrootLabel));
							return;
							} else {
								Debug.POLICY.trace(Debug.DEBUG_SEARCHER, "Long searcher found nothing."); //$NON-NLS-1$
						showInfoMessage(SearcherMessages.searchFailedDialogTitle,
								NLS.bind(
										SearcherMessages.searchFailedDialogMessage,
										CellProperties.sysrootLabel));
						}
					} else {
						Debug.POLICY.trace(Debug.DEBUG_SEARCHER, "Not a Linux platform, no long search is available."); //$NON-NLS-1$
					}
				} catch (Exception e) {
					Debug.POLICY.error(Debug.DEBUG_SEARCHER, e);
					Debug.POLICY.logError(e);
				} finally {
					Debug.POLICY.trace(Debug.DEBUG_SEARCHER, "Finished long searcher for GNU tools."); //$NON-NLS-1$
				}
			}
		}.start();

	}
	
	protected boolean isValidSysrootDirectory(String directoryPath) {
		if (directoryPath != null) {
			File sysrootDirectory = new File(directoryPath);
			if (sysrootDirectory.exists() && sysrootDirectory.isDirectory()) {
				/*
				 * Checks to see if all libraries are available at this directory
				 */
				for (int i = 0; i < requiredFiles.length; i++) {
					File requiredFile = new File(sysrootDirectory,
							CellProperties.sysrootSubdir+requiredFiles[i]);
					if (!(requiredFile.exists() && requiredFile.isFile())) {
						return false;
					}
				}
				return true;
			}
		}
		return false;
	}
	
	@Deprecated
	public static void main(String args[]){	
		SysrootSearcher ss = new SysrootSearcher(null,null);
		
		System.out.println(ss.isValidSysrootDirectory("/opt/cell/"));
	}

}
