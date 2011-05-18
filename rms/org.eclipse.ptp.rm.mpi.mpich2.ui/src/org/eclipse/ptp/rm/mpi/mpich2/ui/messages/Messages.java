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
package org.eclipse.ptp.rm.mpi.mpich2.ui.messages;

import org.eclipse.osgi.util.NLS;

/**
 * 
 * @author Daniel Felix Ferber
 * 
 */
public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.ptp.rm.mpi.mpich2.ui.messages.messages"; //$NON-NLS-1$
	public static String MPICH2UIPlugin_Exception_InternalError;

	public static String AdvancedMPICH2RMLaunchConfigurationDynamicTab_Label_Arguments;
	public static String AdvancedMPICH2RMLaunchConfigurationDynamicTab_Label_DefaultArguments;
	public static String AdvancedMPICH2RMLaunchConfigurationDynamicTab_Label_LaunchArguments;
	public static String AdvancedMPICH2RMLaunchConfigurationDynamicTab_Title;
	public static String AdvancedMPICH2RMLaunchConfigurationDynamicTab_Validation_EmptyArguments;
	public static String BasicMPICH2RMLaunchConfigurationDynamicTab_Label_Browse;
	public static String BasicMPICH2RMLaunchConfigurationDynamicTab_Label_HostFile;
	public static String BasicMPICH2RMLaunchConfigurationDynamicTab_Label_NoLocal;
	public static String BasicMPICH2RMLaunchConfigurationDynamicTab_Label_NumberProcesses;
	public static String BasicMPICH2RMLaunchConfigurationDynamicTab_Label_Prefix;
	public static String BasicMPICH2RMLaunchConfigurationDynamicTab_Title;
	public static String BasicMPICH2RMLaunchConfigurationDynamicTab_Title_HostGroup;
	public static String BasicMPICH2RMLaunchConfigurationDynamicTab_Title_HostList;
	public static String BasicMPICH2RMLaunchConfigurationDynamicTab_Title_OptionsGroup;
	public static String BasicMPICH2RMLaunchConfigurationDynamicTab_Validation_EmptyHostfile;
	public static String BasicMPICH2RMLaunchConfigurationDynamicTab_Validation_EmptyHostList;
	public static String BasicMPICH2RMLaunchConfigurationDynamicTab_Validation_EmptyPrefix;
	public static String BasicMPICH2RMLaunchConfigurationDynamicTab_Validation_NoProcess;

	public static String MPICH2ConfigurationWizardPage_Description;
	public static String MPICH2ConfigurationWizardPage_Label_Version;
	public static String MPICH2ConfigurationWizardPage_Name;
	public static String MPICH2ConfigurationWizardPage_Title;
	public static String MPICH2ConfigurationWizardPage_VersionCombo_Version12;
	public static String MPICH2ConfigurationWizardPage_VersionCombo_Version13;
	public static String MPICH2RMConfigurationWizardPage_Description;
	public static String MPICH2RMConfigurationWizardPage_Title;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
		// Prevent instances.
	}
}
