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
package org.eclipse.ptp.remotetools.environment.launcher.internal.macros;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.core.cdtvariables.ICdtVariable;
import org.eclipse.cdt.internal.core.cdtvariables.CoreMacroSupplierBase;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.ptp.remotetools.environment.launcher.macros.ILaunchConfigurationVariableSupplier;
import org.eclipse.ptp.remotetools.environment.launcher.macros.ILaunchVariableContextInfo;
import org.eclipse.ptp.remotetools.utils.extensionpoints.IProcessMemberVisitor;
import org.eclipse.ptp.remotetools.utils.extensionpoints.ProcessExtensions;


/**
 * @author laggarcia
 * @since 3.0.0
 */
public class LaunchMacroSupplier extends CoreMacroSupplierBase {

	public static final String EXT_LAUNCH_CONFIGURATION_MACRO_SUPPLIER = "org.eclipse.ptp.remotetools.environment.launcher.launchConfigurationMacroSupplier"; //$NON-NLS-1$

	private static LaunchMacroSupplier instance;

	private final Map auxiliarySuppliers = new HashMap();

	private static final String CONFIG_TYPE_ID = "configTypeId"; //$NON-NLS-1$

	private static final String CLASS = "class"; //$NON-NLS-1$

	private LaunchMacroSupplier() {
	}

	public static LaunchMacroSupplier getInstance() {
		if (instance == null) {
			instance = new LaunchMacroSupplier();
		}
		return instance;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.managedbuilder.macros.IBuildMacroSupplier#getMacro(java.lang.String,
	 *      int, java.lang.Object)
	 */
	public ICdtVariable getMacro(String macroName, int contextType,
			Object contextData) {
		switch (contextType) {
		case ILaunchVariableContextInfo.CONTEXT_LAUNCH:
			if (contextData instanceof ILaunchConfiguration) {
				loadLaunchConfigurationMacroSuppliers();
				try {
					ILaunchConfigurationType launchConfigurationType = ((ILaunchConfiguration) contextData)
							.getType();
					ILaunchConfigurationVariableSupplier launchConfigurationMacroSupplier = (ILaunchConfigurationVariableSupplier) this.auxiliarySuppliers
							.get(launchConfigurationType.getIdentifier());
					return launchConfigurationMacroSupplier.getVariable(
							macroName, (ILaunchConfiguration) contextData);
				} catch (CoreException e) {
					e.printStackTrace();
				}
			}
			break;
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.managedbuilder.macros.IBuildMacroSupplier#getMacros(int,
	 *      java.lang.Object)
	 */
	public ICdtVariable[] getMacros(int contextType, Object contextData) {
		switch (contextType) {
		case ILaunchVariableContextInfo.CONTEXT_LAUNCH:
			if (contextData instanceof ILaunchConfiguration) {
				loadLaunchConfigurationMacroSuppliers();
				try {
					ILaunchConfigurationType launchConfigurationType = ((ILaunchConfiguration) contextData)
							.getType();
					ILaunchConfigurationVariableSupplier launchConfigurationMacroSupplier = (ILaunchConfigurationVariableSupplier) this.auxiliarySuppliers
							.get(launchConfigurationType.getIdentifier());
					return launchConfigurationMacroSupplier
							.getVariables((ILaunchConfiguration) contextData);
				} catch (CoreException e) {
					e.printStackTrace();
				}
			}
		}
		return null;
	}

	private void loadLaunchConfigurationMacroSuppliers() {
		if (this.auxiliarySuppliers.isEmpty()) {
			ProcessExtensions.process(EXT_LAUNCH_CONFIGURATION_MACRO_SUPPLIER,
					new IProcessMemberVisitor() {
						public Object process(IExtension extension,
								IConfigurationElement member) {
							try {
								LaunchMacroSupplier.this.auxiliarySuppliers
										.put(
												member
														.getAttribute(CONFIG_TYPE_ID),
												member
														.createExecutableExtension(CLASS));
							} catch (Exception e) {
								e.printStackTrace();
							}
							return null;
						}
					});
		}
	}

}
