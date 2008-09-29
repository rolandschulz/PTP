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
package org.eclipse.ptp.cell.managedbuilder.xl.core.preferences;

import java.io.File;

import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ptp.cell.managedbuilder.xl.core.debug.Debug;
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
public class XlToolsSearcher extends StringFieldEditorPreferenceSearcher {

	public XlToolsSearcher(StringFieldEditor stringFieldEditor, Composite parent) {
		super(stringFieldEditor, parent);
	}

	protected void fastSearch() throws SearchFailedException {

		// Try to find out if the current preference value is a valid install
		// directory
		String currentPreferenceValue = this.stringFieldEditor.getStringValue();
		if (isValidXLToolsDirectory(currentPreferenceValue)) {
			// Current preference value is a valid install directory.
			Debug.POLICY.trace(Debug.DEBUG_SEARCHER, "Current value is valid"); //$NON-NLS-1$
			return;
		}
		// The current preference value is not valid. The serach continues.
		// Compare the current value with the default value.
		String defaultPreferenceValue = this.preferenceStore
				.getDefaultString(this.preferenceName);
		if ((!currentPreferenceValue.equals(defaultPreferenceValue))
				&& (isValidXLToolsDirectory(defaultPreferenceValue))) {
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
		String xlToolsPath;
		if (Platform.getOS().equals(Platform.OS_LINUX)) {
			xlToolsPath = new Path(Which
					.which(XlToolsProperties.ppuxlcExecutable))
					.removeLastSegments(1).toOSString();
			if (isValidXLToolsDirectory(xlToolsPath)) {
				Debug.POLICY.trace(Debug.DEBUG_SEARCHER, "Value returned from 'which' is valid"); //$NON-NLS-1$
				setText(xlToolsPath);
				return;
			}
		}
		// ppu-gcc are not in the $PATH environment variable. The search
		// continues
		// Verify if the package is installed or not
		xlToolsPath = PackageManagementSystemManager
				.getPackageManager()
				.searchFileInQueryListAndReturnInitialPathSegments(
						XlToolsProperties.ppuxlcPackage,
						File.separator + XlToolsProperties.ppuxlcExecutable);
		if (isValidXLToolsDirectory(xlToolsPath)) {
			Debug.POLICY.trace(Debug.DEBUG_SEARCHER, "Value returned from package management system is valid"); //$NON-NLS-1$
			setText(xlToolsPath);
			return;
		}
		Debug.POLICY.trace(Debug.DEBUG_SEARCHER, "All fast searches failed,"); //$NON-NLS-1$
		// None of the searches returned successfully.
		throw new SearchFailedException(
				SearcherMessages.fastSearchFailedMessage);

	}

	protected void longSearch() {
		new Thread() {
			public void run() {
				try {
					Debug.POLICY.trace(Debug.DEBUG_SEARCHER, "Start long searcher for XL tools. Start 'find'."); //$NON-NLS-1$
				if (Platform.getOS().equals(Platform.OS_LINUX)) {
					String systemsim_cellPath = new Path(Find.findFile(
							XlToolsProperties.ppuxlcSearchRootDirectory,
							XlToolsProperties.ppuxlcExecutable))
							.removeLastSegments(1).toOSString();
						Debug.POLICY.trace(Debug.DEBUG_SEARCHER, "'find' finished."); //$NON-NLS-1$
					if (isValidXLToolsDirectory(systemsim_cellPath)) {
						setText(systemsim_cellPath);
							Debug.POLICY.trace(Debug.DEBUG_SEARCHER, "Long searcher successfull."); //$NON-NLS-1$
						showInfoMessage(
								SearcherMessages.searchSucceededDialogTitle,
								NLS
										.bind(
												SearcherMessages.searchSucceededDialogMessage,
												XlToolsProperties.xlToolsPathLabel));
						return;
						} else {
							Debug.POLICY.trace(Debug.DEBUG_SEARCHER, "Long searcher found nothing."); //$NON-NLS-1$
					showInfoMessage(SearcherMessages.searchFailedDialogTitle,
							NLS.bind(
									SearcherMessages.searchFailedDialogMessage,
									XlToolsProperties.xlToolsPathLabel));
						}
					} else {
						Debug.POLICY.trace(Debug.DEBUG_SEARCHER, "Not a Linux platform, no long search is available."); //$NON-NLS-1$
					}
				} catch (Exception e) {
					Debug.POLICY.error(Debug.DEBUG_SEARCHER, e);
					Debug.POLICY.logError(e);
				} finally {
					Debug.POLICY.trace(Debug.DEBUG_SEARCHER, "Finished long searcher for XL tools."); //$NON-NLS-1$
				}
			}
		}.start();
	}

	protected boolean isValidXLToolsDirectory(String directoryPath) {
		if (directoryPath != null) {
			File xlToolsDirectory = new File(directoryPath);
			if (xlToolsDirectory.exists() && xlToolsDirectory.isDirectory()) {
				// Test if the GNU executable files exist relative to this
				// directory
				String[] xlExecutables = XlToolsProperties.xlTools
						.split(XlToolsProperties.separator);
				for (int i = 0; i < xlExecutables.length; i++) {
					File xlExecutable = new File(xlToolsDirectory,
							xlExecutables[i]);
					if (!(xlExecutable.exists() && xlExecutable.isFile())) {
						return false;
					}
				}
				return true;
			}
		}
		return false;
	}
}