/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.ptp.rm.mpi.openmpi.ui.messages;

import org.eclipse.osgi.util.NLS;

/**
 * 
 * @author Daniel Felix Ferber
 * 
 */
public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.ptp.rm.mpi.openmpi.ui.messages.messages"; //$NON-NLS-1$
	public static String OpenMPIUIPlugin_Exception_InternalError;

	public static String AdvancedOpenMpiRMLaunchConfigurationDynamicTab_Label_Arguments;
	public static String AdvancedOpenMpiRMLaunchConfigurationDynamicTab_Label_DefaultArguments;
	public static String AdvancedOpenMpiRMLaunchConfigurationDynamicTab_Label_DefaultMCAParameters;
	public static String AdvancedOpenMpiRMLaunchConfigurationDynamicTab_Label_LaunchArguments;
	public static String AdvancedOpenMpiRMLaunchConfigurationDynamicTab_Label_MCAParameters;
	public static String AdvancedOpenMpiRMLaunchConfigurationDynamicTab_PArameterTable_Column_Name;
	public static String AdvancedOpenMpiRMLaunchConfigurationDynamicTab_ParameterTable_Column_Value;
	public static String AdvancedOpenMpiRMLaunchConfigurationDynamicTab_Title;
	public static String AdvancedOpenMpiRMLaunchConfigurationDynamicTab_Validation_EmptyArguments;
	public static String AdvancedOpenMpiRMLaunchConfigurationDynamicTab_Validation_EmptyParameter;

	public static String BasicOpenMpiRMLaunchConfigurationDynamicTab_Exception_InvalidConfiguration;
	public static String BasicOpenMpiRMLaunchConfigurationDynamicTab_Label_Browse;
	public static String BasicOpenMpiRMLaunchConfigurationDynamicTab_Label_ByNode;
	public static String BasicOpenMpiRMLaunchConfigurationDynamicTab_Label_BySlot;
	public static String BasicOpenMpiRMLaunchConfigurationDynamicTab_Label_HostFile;
	public static String BasicOpenMpiRMLaunchConfigurationDynamicTab_Label_NoLocal;
	public static String BasicOpenMpiRMLaunchConfigurationDynamicTab_Label_NoOversubscribe;
	public static String BasicOpenMpiRMLaunchConfigurationDynamicTab_Label_NumberProcesses;
	public static String BasicOpenMpiRMLaunchConfigurationDynamicTab_Label_Prefix;
	public static String BasicOpenMpiRMLaunchConfigurationDynamicTab_Title;
	public static String BasicOpenMpiRMLaunchConfigurationDynamicTab_Title_HostGroup;
	public static String BasicOpenMpiRMLaunchConfigurationDynamicTab_Title_HostList;
	public static String BasicOpenMpiRMLaunchConfigurationDynamicTab_Title_OptionsGroup;
	public static String BasicOpenMpiRMLaunchConfigurationDynamicTab_Validation_EmptyHostfile;
	public static String BasicOpenMpiRMLaunchConfigurationDynamicTab_Validation_EmptyHostList;
	public static String BasicOpenMpiRMLaunchConfigurationDynamicTab_Validation_EmptyPrefix;
	public static String BasicOpenMpiRMLaunchConfigurationDynamicTab_Validation_NoProcess;

	public static String OpenMPI12PreferencePage_Title;
	public static String OpenMPI13PreferencePage_Title;

	public static String OpenMPIConfigurationWizardPage_Description;
	public static String OpenMPIConfigurationWizardPage_Label_Version;
	public static String OpenMPIConfigurationWizardPage_Name;
	public static String OpenMPIConfigurationWizardPage_Title;
	public static String OpenMPIConfigurationWizardPage_Validation_NoVersionSelected;
	public static String OpenMPIConfigurationWizardPage_VersionCombo_Auto;
	public static String OpenMPIConfigurationWizardPage_VersionCombo_Version12;
	public static String OpenMPIConfigurationWizardPage_VersionCombo_Version13;
	public static String OpenMPIConfigurationWizardPage_VersionCombo_Version14;
	public static String OpenMPIConfigurationWizardPage_Version_Auto;
	public static String OpenMPIRMConfigurationWizardPage_Description;
	public static String OpenMPIRMConfigurationWizardPage_Title;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
		// Prevent instances.
	}
}
