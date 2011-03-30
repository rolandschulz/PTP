/*******************************************************************************
 * Copyright (c) 2011 University of Illinois All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html 
 * 	
 * Contributors: 
 * 	Albert L. Rossi - design and implementation
 ******************************************************************************/

package org.eclipse.ptp.rm.jaxb.ui.messages;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.ptp.rm.jaxb.ui.messages.messages"; //$NON-NLS-1$

	public static String JAXBUIPlugin_Exception_InternalError;
	public static String JAXBConnectionWizardPage_Description;
	public static String JAXBRMControlConfigurationWizardPage_Title;
	public static String JAXBRMMonitoringConfigurationWizardPage_Title;

	public static String JAXBRMConfigurationSelectionWizardPage_0;
	public static String JAXBRMConfigurationSelectionWizardPage_1;
	public static String JAXBRMConfigurationSelectionWizardPage_2;
	public static String JAXBRMConfigurationSelectionWizardPage_Project_Selection_Title;
	public static String JAXBRMConfigurationSelectionWizardPage_Project_Selection_Message;

	public static String AbstractRemoteProxyResourceManagerConfigurationWizardPage_0;
	public static String AbstractRemoteProxyResourceManagerConfigurationWizardPage_1;
	public static String AbstractRemoteProxyResourceManagerConfigurationWizardPage_2;
	public static String AbstractRemoteProxyResourceManagerConfigurationWizardPage_3;
	public static String AbstractRemoteProxyResourceManagerConfigurationWizardPage_3b;
	public static String AbstractRemoteProxyResourceManagerConfigurationWizardPage_4;
	public static String AbstractRemoteProxyResourceManagerConfigurationWizardPage_5;
	public static String AbstractRemoteProxyResourceManagerConfigurationWizardPage_6;
	public static String AbstractRemoteProxyResourceManagerConfigurationWizardPage_7;
	public static String AbstractRemoteProxyResourceManagerConfigurationWizardPage_8;
	public static String AbstractRemoteProxyResourceManagerConfigurationWizardPage_9;
	public static String AbstractRemoteProxyResourceManagerConfigurationWizardPage_10;
	public static String AbstractRemoteProxyResourceManagerConfigurationWizardPage_11;
	public static String AbstractRemoteProxyResourceManagerConfigurationWizardPage_12;
	public static String AbstractRemoteProxyResourceManagerConfigurationWizardPage_13;
	public static String AbstractRemoteProxyResourceManagerConfigurationWizardPage_14;
	public static String AbstractRemoteProxyResourceManagerConfigurationWizardPage_15;
	public static String AbstractRemoteProxyResourceManagerConfigurationWizardPage_16;
	public static String AbstractRemoteProxyResourceManagerConfigurationWizardPage_17;

	public static String CustomBatchScriptTab_title;
	public static String MXGroupTitle;
	public static String ManualLaunch;

	public static String ClearScript;
	public static String BatchScriptPath;
	public static String DefaultDynamicTab_title;
	public static String ViewScript;
	public static String ViewValuesReplaced;
	public static String ErrorOnLoadTitle;
	public static String ErrorOnLoadFromStore;
	public static String WidgetSelectedError;
	public static String WidgetSelectedErrorTitle;
	public static String MissingLaunchConfigurationError;
	public static String JAXBRMLaunchConfigurationFactory_doCreateError;

	public static String FileContentsDirty;
	public static String ErrorOnCopyToStorageTitle;
	public static String ErrorOnCopyToStorage;
	public static String InvalidOptionIndex;
	public static String InvalidOption;
	public static String TreeDataLabelProviderColumnError;
	public static String TableDataLabelProviderColumnError;
	public static String ErrorOnCopyFromFields;
	public static String WriteToFileCanceled;
	public static String RenameFile;
	public static String RenameFileTitle;
	public static String RestoreDefaultValues;
	public static String DefaultValues;
	public static String ToggleVisibleAttributes;
	public static String DialogClose;
	public static String DisplayScript;
	public static String DisplayScriptError;
	public static String DisplayScriptErrorTitle;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
		// Prevent instances.
	}
}
