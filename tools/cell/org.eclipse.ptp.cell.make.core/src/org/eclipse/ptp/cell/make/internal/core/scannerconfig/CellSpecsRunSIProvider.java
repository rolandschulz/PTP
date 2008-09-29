/**
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *
 */
package org.eclipse.ptp.cell.make.internal.core.scannerconfig;

import java.io.File;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.envvar.IEnvironmentVariable;
import org.eclipse.cdt.core.envvar.IEnvironmentVariableManager;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.make.internal.core.scannerconfig2.GCCSpecsRunSIProvider;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.core.runtime.Path;
import org.eclipse.ptp.cell.make.core.debug.Debug;


/**
 * @author laggarcia
 * 
 */
public class CellSpecsRunSIProvider extends GCCSpecsRunSIProvider {

	protected static final String EMPTY_STRING = ""; //$NON-NLS-1$

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.make.internal.core.scannerconfig2.GCCSpecsRunSIProvider#initialize()
	 */
	protected boolean initialize() {
		Debug.read();
		Debug.POLICY.enter(Debug.DEBUG_XL_PROVIDER);
		
		boolean rc = super.initialize();

		if (rc) {
			try {
			    this.fCompileCommand = new Path( checkFileExistenceOnPath( this.fCompileCommand.toString() ) );
				Debug.POLICY.trace(Debug.DEBUG_XL_PROVIDER, "Command line: {0}", this.fCompileCommand);
			} catch (Exception e) {
				Debug.POLICY.error(Debug.DEBUG_XL_PROVIDER, e);
				e.printStackTrace();
				return false;
			}
		}

		Debug.POLICY.exit(Debug.DEBUG_XL_PROVIDER);
		return rc;
	}

	/**
	 * This method takes a file name and check it's existence using 
	 * the directories declared in the PATH environment variable.
	 * 
	 * @param fileName
	 * @return The full path name of the given file name, NULL if no
	 * valid match was found.
	 */
	private String checkFileExistenceOnPath(String fileName) {
		// Get build info
		IManagedBuildInfo managedBuildInfo = ManagedBuildManager.getBuildInfo(this.resource.getProject());

		// Prefer the selected configuration over the default.
		String currentProfileId = this.buildInfo.getSelectedProfileId();
		currentProfileId = currentProfileId.substring(
				currentProfileId.lastIndexOf('.') + 1, currentProfileId.length() );
		IConfiguration activeConfiguration;
		activeConfiguration = managedBuildInfo.getDefaultConfiguration();
		// Fetch the environment information on PATH variable
		String [] paths = getEnvironmentPath(activeConfiguration);
		for (String path : paths) {
			// Add path to first entry of the parameter array before executing.
			File pathEntryFile = new File(path);
			File compilerFile = new File(pathEntryFile, fileName);
			if (compilerFile.exists())
				// the path exists
				return compilerFile.getAbsolutePath();
		}
		return "";
	}
	
	/**
	 * Retrieve environment variables from a given configuration
	 * 
	 * @param activeConfiguration
	 * @return A vector of strings containing the variables and their values in the <var-name>=<var-value> format
	 */
	private String[] getEnvironmentPath(IConfiguration activeConfiguration) {
		// Fetch environment variable
		ICConfigurationDescription cfgDes = ManagedBuildManager.getDescriptionForConfiguration(activeConfiguration);
		IEnvironmentVariableManager mngr = CCorePlugin.getDefault().getBuildEnvironmentManager();
		IEnvironmentVariable path = mngr.getVariable("PATH", cfgDes, true);
		// Separate variable values
		String [] pathValues = path.getValue().split(path.getDelimiter());
		return pathValues;
	}
	
}
