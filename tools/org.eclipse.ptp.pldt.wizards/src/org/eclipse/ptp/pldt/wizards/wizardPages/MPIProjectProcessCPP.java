/*******************************************************************************
 * Copyright (c) 2010,2011 IBM Corp. 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corp. - initial implementation
 *******************************************************************************/

package org.eclipse.ptp.pldt.wizards.wizardPages;

import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ptp.pldt.mpi.core.MpiIDs;
import org.eclipse.ptp.pldt.mpi.core.MpiPlugin;

/**
 * 
 * After the MPIProjectWizardPageCPP runs, and we get MPI include path from the user,
 * we have this opportunity to use that information to modify the include paths, build cmds etc.
 * in the project
 * 
 * <p>
 * This handles the special differences for C++ projects, over and above plain C projects.
 * 
 * @author Beth Tibbitts
 * 
 */
public class MPIProjectProcessCPP extends MPIProjectProcess {

	private static final boolean traceOn = false;

	/**
	 * Need to set both C and C++ build commands; the C++ build command is passed in as an arg.
	 * The C build command was not solicited from the user on the wizard page, so we just
	 * get the default value to use here.
	 * <p>
	 * C++ projects use both a C build command and a C++ build command in their settings.
	 */
	protected void setCompileCommand(IConfiguration cf, String buildCmd) {
		if (traceOn)
			System.out.println("compile cmd: " + buildCmd); //$NON-NLS-1$
		// access the preference store from the MPI plugin so we can get c compiler too
		IPreferenceStore preferenceStore = MpiPlugin.getDefault().getPreferenceStore();
		String c_build_cmd = preferenceStore.getString(MpiIDs.MPI_BUILD_CMD);

		// 'buildCmd' arg we assume to be the same as cpp_build_cmd unless the user changed it in the wizard page, so we use that
		// and so we don't use this
		// String cpp_build_cmd = preferenceStore.getString(MpiIDs.MPI_CPP_BUILD_CMD);

		ITool c_compiler = cf.getToolFromInputExtension("c"); //$NON-NLS-1$
		ITool cpp_compiler = cf.getToolFromInputExtension("cpp"); //$NON-NLS-1$

		c_compiler.setToolCommand(c_build_cmd);
		if (cpp_compiler != null) {
			cpp_compiler.setToolCommand(buildCmd);
		}
		if (traceOn)
			System.out.println("C compiler cmd: " + c_build_cmd + "  C++ compiler cmd: " + buildCmd); //$NON-NLS-1$ //$NON-NLS-2$
	}
}
