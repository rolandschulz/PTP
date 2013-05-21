/******************************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *
 *****************************************************************************/
package org.eclipse.ptp.launch.internal.messages;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.ptp.launch.internal.messages.messages"; //$NON-NLS-1$

	public static String Launch_common_DebuggerColon;
	public static String Launch_common_Error;

	public static String LaunchPreferencesPage_Always;
	public static String LaunchPreferencesPage_Auto_start_RM;
	public static String LaunchPreferencesPage_Never;
	public static String LaunchPreferencesPage_OpenParallelDebugPerspective;
	public static String LaunchPreferencesPage_OpenSystemMonitoringPerspective;
	public static String LaunchPreferencesPage_Prompt;

	public static String ApplicationTab_Program_selection;
	public static String ApplicationTab_Project_is_closed;
	public static String ApplicationTab_Application_program_not_specified;
	public static String ApplicationTab_Application;
	public static String ApplicationTab_Application_program;
	public static String ApplicationTab_Browse_1;
	public static String ApplicationTab_Browse_2;
	public static String ApplicationTab_Browse_3;
	public static String ApplicationTab_Cannot_read_configuration;
	public static String ApplicationTab_Choose_program;
	public static String ApplicationTab_Choose_the_project;
	public static String ApplicationTab_Copy_executable;
	public static String ApplicationTab_Display_output;
	public static String ApplicationTab_Enter_project;
	public static String ApplicationTab_Enter_project_before_browsing;
	public static String ApplicationTab_Invalid_project_name;
	public static String ApplicationTab_Local_file_must_exist;
	public static String ApplicationTab_Local_file_not_specified;
	public static String ApplicationTab_Local_file_path_must_be_absolute;
	public static String ApplicationTab_Path_to_local_executable;
	public static String ApplicationTab_Please_select_the_project_first;
	public static String ApplicationTab_Please_specify_remote_connection;
	public static String ApplicationTab_Project;
	public static String ApplicationTab_Project_does_not_exist;
	public static String ApplicationTab_Select_application;
	public static String ApplicationTab_Select_executable_to_be_copied;
	public static String ApplicationTab_Select_project;
	public static String ApplicationTab_Selection_must_be_a_file;
	public static String ApplicationTab_Unable_to_open_connection;

	public static String ResourcesTab_Connection_Type;

	public static String ResourcesTab_Dont_ask_to_run_command;

	public static String ResourcesTab_Resources;
	public static String ResourcesTab_No_Launch_Configuration;
	public static String ResourcesTab_InvalidConfig_message;
	public static String ResourcesTab_InvalidConfig_title;
	public static String ResourcesTab_No_Connection_name;
	public static String ResourcesTab_No_Target_Configuration;
	public static String ResourcesTab_noInformation;
	public static String ResourcesTab_openConnection;
	public static String ResourcesTab_pleaseSelectTargetSystem;
	public static String ResourcesTab_targetSystemConfiguration;

	public static String ArgumentsTab_Arguments;
	public static String ArgumentsTab_Cannot_read_configuration;
	public static String ArgumentsTab_Program_arguments;

	public static String EnvironmentTab_Tool_Tip;

	public static String AbstractDebuggerTab_No_debugger_available;
	public static String AbstractDebuggerTab_Debugger;
	public static String AbstractDebuggerTab_ErrorLoadingDebuggerPage;

	public static String DebuggerTab_Stop_at_main_on_startup;
	public static String DebuggerTab_Debugger_Options;
	public static String DebuggerTab_Mode_not_supported;
	public static String DebuggerTab_No_debugger_available;

	public static String WorkingDirectoryBlock_Browse;
	public static String WorkingDirectoryBlock_Cannot_read_configuration;
	public static String WorkingDirectoryBlock_Directory;
	public static String WorkingDirectoryBlock_Directory_cannot_be_empty;
	public static String WorkingDirectoryBlock_Please_select_remote_connection_first;
	public static String WorkingDirectoryBlock_Select_Working_Directory;
	public static String WorkingDirectoryBlock_Unable_to_open_connection;
	public static String WorkingDirectoryBlock_Use_default_working_directory;
	public static String WorkingDirectoryBlock_Working_directory;
	public static String WorkingDirectoryBlock_Working_Directory;

	public static String ParallelLaunchConfigurationDelegate_0;
	public static String ParallelLaunchConfigurationDelegate_1;
	public static String ParallelLaunchConfigurationDelegate_3;
	public static String ParallelLaunchConfigurationDelegate_4;
	public static String ParallelLaunchConfigurationDelegate_5;
	public static String ParallelLaunchConfigurationDelegate_6;
	public static String ParallelLaunchConfigurationDelegate_7;
	public static String ParallelLaunchConfigurationDelegate_Invalid_launch_object;
	public static String ParallelLaunchConfigurationDelegate_OpenDebugPerspective;

	public static String PTPLaunchShortcut_0;
	public static String PTPLaunchShortcut_1;

	public static String AbstractParallelLaunchConfigurationDelegate_0;
	public static String AbstractParallelLaunchConfigurationDelegate_1;
	public static String AbstractParallelLaunchConfigurationDelegate_Project_not_specified;
	public static String AbstractParallelLaunchConfigurationDelegate_Project_does_not_exist_or_is_not_a_project;
	public static String AbstractParallelLaunchConfigurationDelegate_Application_file_does_not_exist;
	public static String AbstractParallelLaunchConfigurationDelegate_Debugger_path_not_found;
	public static String AbstractParallelLaunchConfigurationDelegate_Operation_cancelled_by_user;
	public static String AbstractParallelLaunchConfigurationDelegate_Launch_was_cancelled;

	public static String AbstractParallelLaunchConfigurationDelegate_Local_resource_does_not_exist;
	public static String AbstractParallelLaunchConfigurationDelegate_Remote_resource_does_not_exist;
	public static String AbstractParallelLaunchConfigurationDelegate_debuggerPathNotSpecified;
	public static String AbstractParallelLaunchConfigurationDelegate_Error_converting_rules;
	public static String AbstractParallelLaunchConfigurationDelegate_UnableToDetermineJobStatus;
	public static String AbstractParallelLaunchConfigurationDelegate_launchType1;
	public static String AbstractParallelLaunchConfigurationDelegate_launchType2;
	public static String AbstractParallelLaunchConfigurationDelegate_launchType3;
	public static String AbstractParallelLaunchConfigurationDelegate_ConfirmActions;
	public static String AbstractParallelLaunchConfigurationDelegate_Specified_resource_manager_not_found;
	public static String AbstractParallelLaunchConfigurationDelegate_Path_not_found;
	public static String AbstractParallelLaunchConfigurationDelegate_unableToObtainConnectionInfo;

	public static String DownloadBackRule_0;

	public static String DownloadRule_0;
	public static String DownloadRule_1;
	public static String DownloadRule_2;
	public static String DownloadRule_3;
	public static String DownloadRule_4;
	public static String DownloadRule_5;

	public static String DownloadRuleDialog_AddFileDialog_Message;
	public static String DownloadRuleDialog_AddFileDialog_Title;
	public static String DownloadRuleDialog_DestinationFrame_FileSystemButton;
	public static String DownloadRuleDialog_DestinationFrame_Title;
	public static String DownloadRuleDialog_DestinationFrame_WorkspaceButton;
	public static String DownloadRuleDialog_DirectoryDialog_Message;
	public static String DownloadRuleDialog_DirectoryDialog_Title;
	public static String DownloadRuleDialog_EditFileDialog_Message;
	public static String DownloadRuleDialog_EditFileDialog_Title;
	public static String DownloadRuleDialog_FileListFrame_AddButton;
	public static String DownloadRuleDialog_FileListFrame_EditButton;
	public static String DownloadRuleDialog_FileListFrame_RemoveButton;
	public static String DownloadRuleDialog_FileListFrame_Title;
	public static String DownloadRuleDialog_Message;
	public static String DownloadRuleDialog_OptionsFrame_ExecutableCheck;
	public static String DownloadRuleDialog_OptionsFrame_OverwriteCombo_OverwriteAlwaysOption;
	public static String DownloadRuleDialog_OptionsFrame_OverwriteCombo_OverwriteIfNewerOption;
	public static String DownloadRuleDialog_OptionsFrame_OverwriteCombo_SkipOption;
	public static String DownloadRuleDialog_OptionsFrame_OverwriteLabel;
	public static String DownloadRuleDialog_OptionsFrame_PreserveTimeStampCheck;
	public static String DownloadRuleDialog_OptionsFrame_ReadonlyCheck;
	public static String DownloadRuleDialog_OptionsFrame_Title;
	public static String DownloadRuleDialog_Title;
	public static String DownloadRuleDialog_WorkspaceDialog_Title;

	public static String EnhancedSynchronizeTab_0;

	public static String EnhancedSynchronizeTab_DownloadLabel_DestinationLabel;
	public static String EnhancedSynchronizeTab_DownloadLabel_DestinationMissing;
	public static String EnhancedSynchronizeTab_DownloadLabel_FileListSeparator;
	public static String EnhancedSynchronizeTab_DownloadLabel_FromLabel;
	public static String EnhancedSynchronizeTab_DownloadLabel_MultipleFiles;
	public static String EnhancedSynchronizeTab_DownloadLabel_NoFiles;
	public static String EnhancedSynchronizeTab_DownloadLabel_OneFile;
	public static String EnhancedSynchronizeTab_DownloadLabel_Options_Executable;
	public static String EnhancedSynchronizeTab_DownloadLabel_Options_PreserveTimeStamp;
	public static String EnhancedSynchronizeTab_DownloadLabel_Options_Readonly;
	public static String EnhancedSynchronizeTab_DownloadLabel_OptionsLabel;
	public static String EnhancedSynchronizeTab_DownloadLabel_OptionsSeparator;
	public static String EnhancedSynchronizeTab_DownloadLabel_Type;
	public static String EnhancedSynchronizeTab_ErrorMessage_NewRule_DontKnowRuleType;
	public static String EnhancedSynchronizeTab_ErrorMessage_NewRule_Title;
	public static String EnhancedSynchronizeTab_RulesFrame_Actions_DownloadRule;
	public static String EnhancedSynchronizeTab_RulesFrame_Actions_EditSelectedRule;
	public static String EnhancedSynchronizeTab_RulesFrame_Actions_NewUploadRule;
	public static String EnhancedSynchronizeTab_RulesFrame_Actions_RemoveSelectedRules;
	public static String EnhancedSynchronizeTab_RulesFrame_Description;
	public static String EnhancedSynchronizeTab_RulesFrame_Options_DownloadEnabled;
	public static String EnhancedSynchronizeTab_RulesFrame_Options_UploadEnabled;
	public static String EnhancedSynchronizeTab_RulesFrame_Title;
	public static String EnhancedSynchronizeTab_Tab_Message;
	public static String EnhancedSynchronizeTab_Tab_Title;
	public static String EnhancedSynchronizeTab_UploadLabel_DestinationLabel;
	public static String EnhancedSynchronizeTab_UploadLabel_DestinationSeparator;
	public static String EnhancedSynchronizeTab_UploadLabel_FileListSeparator;
	public static String EnhancedSynchronizeTab_UploadLabel_FromLabel;
	public static String EnhancedSynchronizeTab_UploadLabel_MultipleFiles;
	public static String EnhancedSynchronizeTab_UploadLabel_NoFiles;
	public static String EnhancedSynchronizeTab_UploadLabel_OneFile;
	public static String EnhancedSynchronizeTab_UploadLabel_Options_DownloadBack;
	public static String EnhancedSynchronizeTab_UploadLabel_Options_Executable;
	public static String EnhancedSynchronizeTab_UploadLabel_Options_PreserveTimeStamp;
	public static String EnhancedSynchronizeTab_UploadLabel_Options_Readonly;
	public static String EnhancedSynchronizeTab_UploadLabel_OptionsLabel;
	public static String EnhancedSynchronizeTab_UploadLabel_Type;

	public static String UploadRule_0;
	public static String UploadRule_1;
	public static String UploadRule_2;
	public static String UploadRule_3;
	public static String UploadRule_4;
	public static String UploadRule_5;

	public static String UploadRuleDialog_FileButtonsFrame_AddDirectoryButton;
	public static String UploadRuleDialog_FileButtonsFrame_AddFilesButton;
	public static String UploadRuleDialog_FileButtonsFrame_AddWorkspaceButton;
	public static String UploadRuleDialog_FileButtonsFrame_Description;
	public static String UploadRuleDialog_FileButtonsFrame_RemoveButton;
	public static String UploadRuleDialog_FileButtonsFrame_RemoveFilesLabel;
	public static String UploadRuleDialog_FileButtonsFrame_Title;
	public static String UploadRuleDialog_Message;
	public static String UploadRuleDialog_OptionsFrame_AddDirectoryDialog_Title;
	public static String UploadRuleDialog_OptionsFrame_AddFileDialog_Description;
	public static String UploadRuleDialog_OptionsFrame_AddFileDialog_Title;
	public static String UploadRuleDialog_OptionsFrame_AddWorkspaceDialog_Description;
	public static String UploadRuleDialog_OptionsFrame_AddWorkspaceDialog_Title;
	public static String UploadRuleDialog_OptionsFrame_DownloadBackCheck;
	public static String UploadRuleDialog_OptionsFrame_ExecutableCheck;
	public static String UploadRuleDialog_OptionsFrame_OverwriteCombo_OverwriteIfNewerOption;
	public static String UploadRuleDialog_OptionsFrame_OverwriteCombo_OverwriteOption;
	public static String UploadRuleDialog_OptionsFrame_OverwriteCombo_SkipOption;
	public static String UploadRuleDialog_OptionsFrame_OverwriteLabel;
	public static String UploadRuleDialog_OptionsFrame_PreserveTimeStampCheck;
	public static String UploadRuleDialog_OptionsFrame_ReadonlyCheck;
	public static String UploadRuleDialog_OptionsFrame_Title;
	public static String UploadRuleDialog_RemoteDirectoryFrame_LabelDefaultButton;
	public static String UploadRuleDialog_RemoteDirectoryFrame_LabelDirectory;
	public static String UploadRuleDialog_RemoteDirectoryFrame_Title;
	public static String UploadRuleDialog_Title;

	public static String AbstractRMLaunchConfigurationFactory_0;

	public static String RuntimeProcess_Exit_value_not_available;

	public static String DefaultDynamicTab_title;
	public static String JAXBControllerLaunchConfigurationTab_Unable_to_obtain_remote_connection;

	public static String JAXBControllerLaunchConfigurationTab_unableToObtainConfigurationInfo;
	public static String JAXBControllerLaunchConfigurationTab_unableToObtainConnectionInfo;
	public static String VoidLaunchTabMessage;
	public static String VoidLaunchTabTitle;
	public static String UninitializedRemoteServices;
	public static String CreateControlConfigurableError;
	public static String ViewScript;
	public static String ViewConfig;
	public static String DisplayConfig;
	public static String DisplayScript;
	public static String ViewExcluded;
	public static String ScriptNotSupportedWarning;
	public static String ScriptNotSupportedWarning_title;
	public static String DisplayError;
	public static String DisplayErrorTitle;
	public static String ViewConfigTooltip;
	public static String ViewExcludedTooltip;
	public static String DefaultValues;
	public static String BatchScriptPath;
	public static String JAXBRMConfigurationSelectionWizardPage_1;
	public static String ClearScript;
	public static String OverrideEnvironment;
	public static String OverrideEnvironmentTooltip;
	public static String ReadOnlyWarning;
	public static String ReadOnlyWarning_title;
	public static String ErrorOnLoadTitle;
	public static String ErrorOnLoadFromStore;
	public static String WidgetSelectedError;
	public static String WidgetSelectedErrorTitle;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
