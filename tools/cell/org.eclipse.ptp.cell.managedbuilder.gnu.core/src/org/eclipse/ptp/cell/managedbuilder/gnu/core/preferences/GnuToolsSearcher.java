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
package org.eclipse.ptp.cell.managedbuilder.gnu.core.preferences;

import java.io.File;

import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ptp.cell.managedbuilder.gnu.core.debug.Debug;
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
public class GnuToolsSearcher extends StringFieldEditorPreferenceSearcher {

	public GnuToolsSearcher(StringFieldEditor stringFieldEditor,
			Composite parent) {
		super(stringFieldEditor, parent);
	}

	protected void fastSearch() throws SearchFailedException {

		// Try to find out if the current preference value is a valid install
		// directory
		String currentPreferenceValue = this.stringFieldEditor.getStringValue();
		if (isValidGNUToolsDirectory(currentPreferenceValue)) {
			// Current preference value is a valid install directory.
			Debug.POLICY.trace(Debug.DEBUG_SEARCHER, "Current value is valid"); //$NON-NLS-1$
			return;
		}
		// The current preference value is not valid. The search continues.
		// Compare the current value with the default value.
		String defaultPreferenceValue = this.preferenceStore
				.getDefaultString(this.preferenceName);
		if ((!currentPreferenceValue.equals(defaultPreferenceValue))
				&& (isValidGNUToolsDirectory(defaultPreferenceValue))) {
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
		String gnuToolsPath;
		if (Platform.getOS().equals(Platform.OS_LINUX)) {
			gnuToolsPath = new Path(Which
					.which(GnuToolsProperties.ppugccExecutable))
					.removeLastSegments(1).toOSString();
			if (isValidGNUToolsDirectory(gnuToolsPath)) {
				Debug.POLICY.trace(Debug.DEBUG_SEARCHER, "Value returned from 'which' is valid"); //$NON-NLS-1$
				setText(gnuToolsPath);
				return;
			}
		}
		// ppu-gcc are not in the $PATH environment variable. The search
		// continues
		// Verify if the package is installed or not
		gnuToolsPath = PackageManagementSystemManager.getPackageManager()
				.searchFileInQueryListAndReturnInitialPathSegments(
						GnuToolsProperties.ppugccPackage,
						File.separator
								+ GnuToolsProperties.ppugccExecutable);
		if (isValidGNUToolsDirectory(gnuToolsPath)) {
			Debug.POLICY.trace(Debug.DEBUG_SEARCHER, "Value returned from package management system is valid"); //$NON-NLS-1$
			setText(gnuToolsPath);
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
					Debug.POLICY.trace(Debug.DEBUG_SEARCHER, "Start long searcher for GNU tools. Start 'find'."); //$NON-NLS-1$
					if (Platform.getOS().equals(Platform.OS_LINUX)) {
						String systemsim_cellPath = new Path(Find.findFile(
								GnuToolsProperties.ppugccSearchRootDirectory,
								GnuToolsProperties.ppugccExecutable))
								.removeLastSegments(1).toOSString();
						Debug.POLICY.trace(Debug.DEBUG_SEARCHER, "'find' finished."); //$NON-NLS-1$
						if (isValidGNUToolsDirectory(systemsim_cellPath)) {
							setText(systemsim_cellPath);
							Debug.POLICY.trace(Debug.DEBUG_SEARCHER, "Long searcher successfull."); //$NON-NLS-1$
							showInfoMessage(
									SearcherMessages.searchSucceededDialogTitle,
									NLS
											.bind(
													SearcherMessages.searchSucceededDialogMessage,
													GnuToolsProperties.gnuToolsPathLabel));
							return;
						} else {
							Debug.POLICY.trace(Debug.DEBUG_SEARCHER, "Long searcher found nothing."); //$NON-NLS-1$
							showInfoMessage(SearcherMessages.searchFailedDialogTitle,
								NLS.bind(
										SearcherMessages.searchFailedDialogMessage,
										GnuToolsProperties.gnuToolsPathLabel));
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

	protected boolean isValidGNUToolsDirectory(String directoryPath) {
		if (directoryPath != null) {
			File gnuToolsDirectory = new File(directoryPath);
			if (gnuToolsDirectory.exists() && gnuToolsDirectory.isDirectory()) {
				// Test if the GNU executable files exist relative to this
				// directory
				String[] gnuExecutables = GnuToolsProperties.gnuTools
						.split(GnuToolsProperties.separator);
				for (int i = 0; i < gnuExecutables.length; i++) {
					File gnuExecutable = new File(gnuToolsDirectory,
							gnuExecutables[i]);
					if (!(gnuExecutable.exists() && gnuExecutable.isFile())) {
						return false;
					}
				}
				return true;
			}
		}
		return false;
	}
}