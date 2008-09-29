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
package org.eclipse.ptp.cell.environment.launcher.cellbe.macros;

import org.eclipse.cdt.launch.AbstractCLaunchDelegate;
import org.eclipse.cdt.managedbuilder.internal.macros.BuildMacro;
import org.eclipse.cdt.managedbuilder.macros.IBuildMacro;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.ptp.cell.environment.launcher.cellbe.debug.Debug;
import org.eclipse.ptp.cell.environment.launcher.cellbe.internal.TargetLaunchDelegate;
import org.eclipse.ptp.remotetools.environment.control.ITargetControl;
import org.eclipse.ptp.remotetools.environment.extension.ITargetVariables;
import org.eclipse.ptp.remotetools.environment.launcher.macros.ILaunchConfigurationVariableSupplier;


/**
 * @author laggarcia
 * @since 3.0.0
 * 
 */
public class DefaultTargetLaunchMacroSupplier implements
		ILaunchConfigurationVariableSupplier {
	
	static TargetLaunchDelegate dummyDelegate = new TargetLaunchDelegate();

	protected static final String PROJECT_NAME_MACRO = "project_name"; //$NON-NLS-1$

	protected static final String SYSTEM_WORKSPACE_MACRO = "system_workspace"; //$NON-NLS-1$

	public DefaultTargetLaunchMacroSupplier() {
	}

	public IBuildMacro getVariable(String macroName,
			ILaunchConfiguration launchConfiguration) {
		if (macroName.equals(PROJECT_NAME_MACRO)) {
			try {
				return new BuildMacro(macroName, IBuildMacro.VALUE_TEXT,
						AbstractCLaunchDelegate
								.getProjectName(launchConfiguration));
			} catch (CoreException e) {
				Debug.POLICY.error(Debug.DEBUG_VARIABLES, e);
				Debug.POLICY.logError(e);
			}
		} else if (macroName.equals(SYSTEM_WORKSPACE_MACRO)) {
			try {
				ITargetControl targetControl = dummyDelegate.getTargetControl(launchConfiguration);
				if (targetControl instanceof ITargetVariables) {
					return new BuildMacro(macroName,
							IBuildMacro.VALUE_PATH_DIR,
							((ITargetVariables) targetControl)
									.getSystemWorkspace());
				}
			} catch (CoreException e) {
				Debug.POLICY.error(Debug.DEBUG_VARIABLES, e);
				Debug.POLICY.logError(e);
			}
			return null;
		}
		return null;
	}

	public IBuildMacro[] getVariables(ILaunchConfiguration launchConfiguration) {
		try {
			IBuildMacro[] macros = new IBuildMacro[2];
			// Get the value for PROJECT_NAME_MACRO
			macros[0] = new BuildMacro(PROJECT_NAME_MACRO,
					IBuildMacro.VALUE_TEXT, AbstractCLaunchDelegate
							.getProjectName(launchConfiguration));
			// Get the value for SYSTEM_WORKSPACE_MACRO
			ITargetControl targetControl = dummyDelegate.getTargetControl(launchConfiguration);
			macros[1] = new BuildMacro(SYSTEM_WORKSPACE_MACRO,
					IBuildMacro.VALUE_PATH_DIR,
					((ITargetVariables) targetControl).getSystemWorkspace());
			return macros;
		} catch (CoreException e) {
			Debug.POLICY.error(Debug.DEBUG_VARIABLES, e);
			Debug.POLICY.logError(e);
		}
		return null;
	}

}
