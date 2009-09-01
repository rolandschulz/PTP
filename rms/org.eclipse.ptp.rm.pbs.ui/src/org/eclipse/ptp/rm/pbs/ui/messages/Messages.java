/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.ptp.rm.pbs.ui.messages;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.ptp.rm.pbs.ui.messages.messages"; //$NON-NLS-1$

	public static String PBSResourceManagerConfigurationWizardPage_name;
	public static String PBSResourceManagerConfigurationWizardPage_title;
	public static String PBSResourceManagerConfigurationWizardPage_description;

	public static String PBSRMLaunchConfigurationDynamicTab_0;
	public static String PBSRMLaunchConfigurationDynamicTab_1;
	public static String PBSRMLaunchConfigurationDynamicTab_2;
	public static String PBSRMLaunchConfigurationDynamicTab_3;
	public static String PBSRMLaunchConfigurationDynamicTab_4;
	public static String PBSRMLaunchConfigurationDynamicTab_5;
	public static String PBSRMLaunchConfigurationDynamicTab_6;
	public static String PBSRMLaunchConfigurationDynamicTab_7;
	public static String PBSRMLaunchConfigurationDynamicTab_8;
	public static String PBSRMLaunchConfigurationDynamicTab_9;
	public static String PBSRMLaunchConfigurationDynamicTab_10;

	public static String PBSConfigurationWizardPage_name;
	public static String PBSConfigurationWizardPage_title;
	public static String PBSConfigurationWizardPage_description;
	public static String PBSConfigurationWizardPage_defaultButton;
	public static String PBSConfigurationWizardPage_browseButton;
	public static String PBSConfigurationWizardPage_path;
	public static String PBSConfigurationWizardPage_arguments;
	public static String PBSConfigurationWizardPage_invalid;
	public static String PBSConfigurationWizardPage_select;
	public static String PBSConfigurationWizardPage_connection_error;
	public static String PBSConfigurationWizardPage_connection_error_msg;
	public static String PBSConfigurationWizardPage_numProcsInvalid;
	public static String PBSConfigurationWizardPage_timeLimitInvalid;

	public static String PBSPreferencesPage_group_pbsd;
	public static String PBSPreferencesPage_pbsdFile_text;
	public static String PBSPreferencesPage_Select_PBSd_FILE;
	public static String PBSPreferencesPage_Incorrect_PBSd_file;
	public static String PBSPreferencesPage_pbsdArgs_text;
	public static String PBSPreferencesPage_pbsdFull_text;
	public static String PBSPreferencesPage_group_proxy;
	public static String PBSPreferencesPage_pbsServer_text;
	public static String PBSPreferencesPage_Select_PBS_PROXY_FILE;
	public static String PBSPreferencesPage_Incorrect_server_file;
	public static String PBSPreferencesPage_manual;
	public static String PBSPreferencesPage_browseButton;
	
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
		// Prevent instances.
	}
}

