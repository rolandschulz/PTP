/*******************************************************************************
 * Copyright (c) 2010 Poznan Supercomputing and Networking Center
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Jan Konczak (PSNC) - initial implementation
 ******************************************************************************/

package org.eclipse.ptp.rm.smoa.ui.wizards;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.ptp.rm.smoa.ui.wizards.messages"; //$NON-NLS-1$
	public static String SMOAResourceManagerConfigurationWizardPage_AuthTypeAnonymous;
	public static String SMOAResourceManagerConfigurationWizardPage_AuthTypeGSI;
	public static String SMOAResourceManagerConfigurationWizardPage_AuthTypeUserTokenProfile;
	public static String SMOAResourceManagerConfigurationWizardPage_Browse;
	public static String SMOAResourceManagerConfigurationWizardPage_CaCert;
	public static String SMOAResourceManagerConfigurationWizardPage_ChooseAuthenticationType;
	public static String SMOAResourceManagerConfigurationWizardPage_Hostname;
	public static String SMOAResourceManagerConfigurationWizardPage_Password;
	public static String SMOAResourceManagerConfigurationWizardPage_Port;
	public static String SMOAResourceManagerConfigurationWizardPage_SecureStorageError;
	public static String SMOAResourceManagerConfigurationWizardPage_ServiceDN;
	public static String SMOAResourceManagerConfigurationWizardPage_SmoaComputingConfiguration;
	public static String SMOAResourceManagerConfigurationWizardPage_SmoaComputingConfiguration_description;
	public static String SMOAResourceManagerConfigurationWizardPage_Username;
	public static String SMOAResourceManagerFileServicesPage_ChooseFileTransferMethods;
	public static String SMOAResourceManagerFileServicesPage_Connection;
	public static String SMOAResourceManagerFileServicesPage_FileServicesChoice;
	public static String SMOAResourceManagerFileServicesPage_FileServicesChoice_description;
	public static String SMOAResourceManagerFileServicesPage_New;
	public static String SMOAResourceManagerFileServicesPage_RemoteServices;
	public static String SMOAResourceManagerFileServicesPage_TextForChoosingAdditionalSsh;
	public static String SMOAResourceManagerFileServicesPage_TextForChoosingSmoaOnly;
	public static String SMOAResourceManagerFileServicesPage_WarningAboutUsingSmoaOnly;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
