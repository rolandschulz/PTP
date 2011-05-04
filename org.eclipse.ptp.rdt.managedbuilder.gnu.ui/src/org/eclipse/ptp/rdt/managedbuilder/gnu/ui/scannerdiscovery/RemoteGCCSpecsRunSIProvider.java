/*******************************************************************************
 * Copyright (c) 2009, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.rdt.managedbuilder.gnu.ui.scannerdiscovery;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.make.core.scannerconfig.IExternalScannerInfoProvider;
import org.eclipse.cdt.make.core.scannerconfig.IScannerConfigBuilderInfo2;
import org.eclipse.cdt.managedbuilder.xlc.ui.XLCUIPlugin;
import org.eclipse.ptp.rdt.managedbuilder.gnu.ui.preferences.PreferenceConstants;
import org.eclipse.cdt.utils.EFSExtensionManager;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ptp.rdt.core.RDTLog;
import org.eclipse.ptp.rdt.core.remotemake.RemoteSpecsRunSIProvider;

/**
 * @author mlindo
 * @since 1.0
 * 
 */
public class RemoteGCCSpecsRunSIProvider extends RemoteSpecsRunSIProvider implements IExternalScannerInfoProvider {

	@Override
	protected List<String> getCommand(IProject project, String providerId, IScannerConfigBuilderInfo2 buildInfo) {
		// get the command that is provided in the extension point
		String gcc = buildInfo.getProviderRunCommand(providerId);

		// The CDT build macro system is busted in CDT 5.0.x, so resolve the
		// compiler command ourselves

		// figure out compiler path from properties and preferences
		String compilerPath = ""; //$NON-NLS-1$
		// search for property first

		try {
			compilerPath = project.getPersistentProperty(new QualifiedName("", //$NON-NLS-1$
					PreferenceConstants.P_GCC_COMPILER_ROOT));
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (compilerPath == null) {
			// use the workbench preference
			IPreferenceStore prefStore = XLCUIPlugin.getDefault().getPreferenceStore();
			compilerPath = prefStore.getString(PreferenceConstants.P_GCC_COMPILER_ROOT);
		}

		if (compilerPath == null) {
			compilerPath = ""; //$NON-NLS-1$
		}

		//gcc = gcc.replaceAll("\\$\\{XL_COMPILER_ROOT\\}", compilerPath); //$NON-NLS-1$

		String args = buildInfo.getProviderRunArguments(providerId);
		String specsFileName = getSpecsFileName(project);

		if (gcc == null || args == null || specsFileName == null)
			return null;

		IFileStore specsFilestore;
		try {
			specsFilestore = createSpecsFile(project, specsFileName, null);
		} catch (CoreException e) {
			RDTLog.logError(e);
			return null;
		} catch (IOException e) {
			RDTLog.logError(e);
			return null;
		}

		String specsFilePath = EFSExtensionManager.getDefault().getPathFromURI(specsFilestore.toURI());
		args = args.replace(SPECS_FILE_PATH_VAR, specsFilePath);

		List<String> command = new ArrayList<String>();
		command.add(gcc);
		for (String arg : args.split(" ")) //$NON-NLS-1$
			command.add(arg);

		return command;
	}

}
