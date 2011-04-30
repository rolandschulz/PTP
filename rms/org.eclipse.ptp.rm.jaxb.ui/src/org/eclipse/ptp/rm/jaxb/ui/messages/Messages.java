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

	public static String JAXBRMConfigurationImportWizard_createResourceManagersProject;

	public static String JAXBRMConfigurationSelectionWizardPage_0;
	public static String JAXBRMConfigurationSelectionWizardPage_1;
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

	public static String Project_required;
	public static String Enter_project_before_browsing_for_program;
	public static String Program_selection;
	public static String Choose_program_to_run_from_NAME;
	public static String Selection_must_be_file;

	public static String CustomBatchScriptTab_title;
	public static String MXGroupTitle;
	public static String ManualLaunch;

	public static String ClearScript;
	public static String BatchScriptPath;
	public static String RemoteScriptPath;
	public static String DefaultDynamicTab_title;
	public static String ViewScript;
	public static String ErrorOnLoadTitle;
	public static String ErrorOnLoadFromStore;
	public static String WidgetSelectedError;
	public static String WidgetSelectedErrorTitle;
	public static String ModifyErrorTitle;
	public static String ModifyError;
	public static String MissingLaunchConfigurationError;
	public static String JAXBRMLaunchConfigurationFactory_wrongRMType;
	public static String JAXBRMLaunchConfigurationFactory_doCreateError;

	public static String FileContentsDirty;
	public static String ErrorOnCopyFromFields;
	public static String WriteToFileCanceled;
	public static String RenameFile;
	public static String RenameFileTitle;
	public static String DefaultValues;
	public static String ToggleShowHideSelectedAttributes;
	public static String DialogClose;
	public static String DisplayScript;
	public static String DisplayScriptError;
	public static String DisplayScriptErrorTitle;
	public static String ViewerLabelProviderColumnError;
	public static String ScriptNotSupportedWarning;
	public static String ScriptNotSupportedWarning_title;
	public static String CreateControlConfigurableError;
	public static String ReadOnlyWarning;
	public static String ReadOnlyWarning_title;

	public static String ValidationError;
	public static String ValidationError_title;
	public static String InvalidConfiguration;
	public static String InvalidConfiguration_title;

	public static String ConfigurationImportWizardTitle;
	public static String ConfigurationImportWizardPageTitle;
	public static String ConfigurationImportWizardPageDescription;
	public static String ConfigurationImportWizardPageTooltip;
	public static String ConfigurationImportWizardPageLabel;
	public static String ResourceManagersNotExist;
	public static String ResourceManagersNotExist_title;
	public static String CaptureJobOutput;
	public static String CaptureJobOutputTooltip;
	public static String CaptureJobOutputError;
	public static String CaptureJobOutputError_title;

	public static String JOB_ID;
	public static String STATE;
	public static String STATE_DETAIL;
	public static String STDOUT_PATH;
	public static String STDERR_PATH;
	public static String STDOUT_READY;
	public static String STDERR_READY;
	public static String JobListUpdate;

	public static String OperationFailed;
	public static String DoControlError;

	public static String RemoveJobWarning;
	public static String CannotUndoOperation;

	public static String EnableStdoutFetch;
	public static String EnableStderrFetch;
	public static String RemotePathTooltip;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
		// Prevent instances.
	}
}
