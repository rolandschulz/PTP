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
package org.eclipse.ptp.rm.ui.wizards;

import org.eclipse.osgi.util.NLS;

/**
 * 
 * @author Daniel Felix Ferber
 *
 */
public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.ptp.rm.ui.wizards.messages"; //$NON-NLS-1$
	public static String AbstractToolRMConfigurationWizardPage_Exception_ConnectionError;
	public static String AbstractToolRMConfigurationWizardPage_Exception_ConnectionErrorDescription;
	public static String AbstractToolRMConfigurationWizardPage_Label_ContinuousMinitorCommand;
	public static String AbstractToolRMConfigurationWizardPage_Label_DebugCommand;
	public static String AbstractToolRMConfigurationWizardPage_Label_DiscoverCommand;
	public static String AbstractToolRMConfigurationWizardPage_Label_LaunchCommand;
	public static String AbstractToolRMConfigurationWizardPage_Label_PathInstallation;
	public static String AbstractToolRMConfigurationWizardPage_Label_PeriodicMonitorCommand;
	public static String AbstractToolRMConfigurationWizardPage_Label_PeriodicMonitorCommandPeriod;
	public static String AbstractToolRMConfigurationWizardPage_Label_UseDefaultSettings;
	public static String AbstractToolRMConfigurationWizardPage_Title_PathSelectionDialog;
	public static String AbstractToolRMConfigurationWizardPage_Validation_InvalidPeriodicMonitorCommandTimeRange;
	public static String AbstractToolRMConfigurationWizardPage_Validation_MissingDebugCommand;
	public static String AbstractToolRMConfigurationWizardPage_Validation_MissingDiscoverCommand;
	public static String AbstractToolRMConfigurationWizardPage_Validation_MissingLaunchCommand;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
		// Prevent instances.
	}
}
