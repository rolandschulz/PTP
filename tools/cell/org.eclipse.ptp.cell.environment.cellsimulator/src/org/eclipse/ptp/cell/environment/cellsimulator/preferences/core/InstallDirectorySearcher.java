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
package org.eclipse.ptp.cell.environment.cellsimulator.preferences.core;

import java.io.File;

import org.eclipse.cdt.core.envvar.IEnvironmentVariable;
import org.eclipse.cdt.internal.core.envvar.DefaultEnvironmentContextInfo;
import org.eclipse.cdt.internal.core.envvar.EnvironmentVariableManager;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ptp.cell.environment.cellsimulator.conf.AttributeNames;
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
public class InstallDirectorySearcher extends
		StringFieldEditorPreferenceSearcher {

	protected static final String BINARY_DIRECTORY = "bin"; //$NON-NLS-1$

	protected static final String WILDCARD = "*"; //$NON-NLS-1$

	/**
	 * @param stringFieldEditor
	 *            the StringFieldEditor this searcher will change
	 * @param parent
	 *            the parent Composite of the StringFieldEditor being changed.
	 *            This is necessary due to refresh the user interface.
	 */
	public InstallDirectorySearcher(StringFieldEditor stringFieldEditor,
			Composite parent) {
		super(stringFieldEditor, parent);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.cell.preferences.core.PreferenceSearcher#fastSearch()
	 */
	protected void fastSearch() throws SearchFailedException {
		// Try to find out if the current preference value is a valid install
		// directory
		String currentPreferenceValue = this.stringFieldEditor.getStringValue();
		if (isValidInstallDirectory(currentPreferenceValue)) {
			// Current preference value is a valid install directory.
			return;
		}
		// The current preference value is not valid. The search continues.
		// See if $SYSTEMSIM_TOP environment variable is defined
		IEnvironmentVariable systemsim_topEnvVar = EnvironmentVariableManager.getVariable(
						LocalSimulatorProperties.SYSTEMSIM_TOP_variableName,
						new DefaultEnvironmentContextInfo(null), true);
		String systemsim_top = null;
		if (systemsim_topEnvVar != null) {
			systemsim_top = systemsim_topEnvVar.getValue();
		}
		if (isValidInstallDirectory(systemsim_top)) {
			setText(systemsim_top);
			return;
		}
		// $SYSTEMSIM_TOP environment variable is not defined. The search
		// continues.
		// Compare the current value with the default value.
		String defaultPreferenceValue = this.preferenceStore
				.getDefaultString(this.preferenceName);
		if ((!currentPreferenceValue.equals(defaultPreferenceValue))
				&& (isValidInstallDirectory(defaultPreferenceValue))) {
			// Current preference value is different from default preference
			// value and the default preference value is a valid install
			// directory.
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
		if (Platform.getOS().equals(Platform.OS_LINUX)) {
			// The systemsim-cell executable is in bin/systemsim-cell relative
			// to $SYSTEMSIM_TOP (simulator top directory), so we have to remove
			// the two last segments
			String systemsim_cellPath = new Path(Which
					.which(LocalSimulatorProperties.systemsim_cell_executable))
					.removeLastSegments(2).toOSString();

			if (isValidInstallDirectory(systemsim_cellPath)) {
				// snif and systemsim-cell are present: the simulator should be
				// installed in this location
				setText(systemsim_cellPath);
				return;
			}
		}
		// snif and systemsim-cell are not in the $PATH environment
		// variable. The search continues
		// Verify if simulator rpm package is installed or not
		/*systemsim_top = PackageManagementSystemManager
				.getPackageManager()
				.searchFileInQueryListAndReturnInitialPathSegments(
						LocalSimulatorProperties.systemsim_cell_package,
						File.separator
								+ BINARY_DIRECTORY
								+ File.separator
								+ LocalSimulatorProperties.systemsim_cell_executable);
		if (isValidInstallDirectory(systemsim_top)) {
			setText(systemsim_top);
			return;
		} */
		// None of the searches returned successfully.
		throw new SearchFailedException(
				SearcherMessages.fastSearchFailedMessage);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.cell.preferences.core.PreferenceSearcher#longSearch()
	 */
	protected void longSearch() {
		new Thread() {
			public void run() {
				if (Platform.getOS().equals(Platform.OS_LINUX)) {
					String systemsim_cellPath = new Path(
					// The snif executable is in bin/snif relative to
							// $SYSTEMSIM_TOP
							// (simulator top directory), so we have to remote
							// the
							// last two
							// segments
							Find
									.findWholename(
											LocalSimulatorProperties.simulatorSearchRootDirectory,
											WILDCARD
													+ BINARY_DIRECTORY
													+ File.separator
													+ LocalSimulatorProperties.systemsim_cell_executable))
							.removeLastSegments(2).toOSString();
					if (isValidInstallDirectory(systemsim_cellPath)) {
						setText(systemsim_cellPath);
						showInfoMessage(
								SearcherMessages.searchSucceededDialogTitle,
								NLS
										.bind(
												SearcherMessages.searchSucceededDialogMessage,
												AttributeNames.SIMULATOR_BASE_DIRECTORY));
						return;
					}
					showInfoMessage(
							SearcherMessages.searchFailedDialogTitle,
							NLS
									.bind(
											SearcherMessages.searchFailedDialogMessage,
											AttributeNames.SIMULATOR_BASE_DIRECTORY));
				}
			}
		}.start();
	}

	protected boolean isValidInstallDirectory(String directoryPath) {
		if (directoryPath != null) {
			File installDirectory = new File(directoryPath);
			if (installDirectory.exists() && installDirectory.isDirectory()) {
				// Test if there is a bin/snif and bin/systemsim-cell files
				// relative to this directory
				File snif = new File(installDirectory, BINARY_DIRECTORY
						+ File.separator
						+ LocalSimulatorProperties.snifExecutable);
				File systemsim_cell = new File(
						installDirectory,
						BINARY_DIRECTORY
								+ File.separator
								+ LocalSimulatorProperties.systemsim_cell_executable);
				if (snif.exists() && snif.isFile() && systemsim_cell.exists()
						&& systemsim_cell.isFile()) {
					// The current value is a valid local simulator install
					// directory.
					return true;
				}
			}
		}
		return false;
	}

}
