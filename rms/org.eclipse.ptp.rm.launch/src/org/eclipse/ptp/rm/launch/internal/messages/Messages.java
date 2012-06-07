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
package org.eclipse.ptp.rm.launch.internal.messages;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.ptp.rm.launch.internal.messages.messages"; //$NON-NLS-1$

	public static String Launch_common_DebuggerColon;
	public static String Launch_common_Error;

	public static String ResourcesTab_InvalidConfig_message;

	public static String ResourcesTab_InvalidConfig_title;

	public static String ResourcesTab_No_Connection_name;

	public static String ResourcesTab_No_Resource_Manager_Available;
	public static String ResourcesTab_Resources;
	public static String ResourcesTab_No_Target_Configuration;
	public static String ResourcesTab_No_Launch_Configuration;

	public static String ResourcesTab_noInformation;

	public static String ResourcesTab_openConnection;
	public static String ResourcesTab_pleaseSelectRM;

	public static String ResourcesTab_pleaseSelectTargetSystem;
	public static String ResourcesTab_Resource_Manager_Not_Started;

	public static String ResourcesTab_targetSystemConfiguration;

	public static String AbstractParallelLaunchConfigurationDelegate_0;
	public static String AbstractParallelLaunchConfigurationDelegate_1;
	public static String AbstractParallelLaunchConfigurationDelegate_Project_not_specified;
	public static String AbstractParallelLaunchConfigurationDelegate_Project_does_not_exist_or_is_not_a_project;
	public static String AbstractParallelLaunchConfigurationDelegate_Application_file_does_not_exist;
	public static String AbstractParallelLaunchConfigurationDelegate_Manager_Not_Started;
	public static String AbstractParallelLaunchConfigurationDelegate_Debugger_path_not_found;
	public static String AbstractParallelLaunchConfigurationDelegate_Operation_cancelled_by_user;
	public static String AbstractParallelLaunchConfigurationDelegate_Local_resource_does_not_exist;
	public static String AbstractParallelLaunchConfigurationDelegate_Remote_resource_does_not_exist;
	public static String AbstractParallelLaunchConfigurationDelegate_debuggerPathNotSpecified;
	public static String AbstractParallelLaunchConfigurationDelegate_Error_converting_rules;
	public static String AbstractParallelLaunchConfigurationDelegate_launchType1;
	public static String AbstractParallelLaunchConfigurationDelegate_launchType2;
	public static String AbstractParallelLaunchConfigurationDelegate_launchType3;
	public static String AbstractParallelLaunchConfigurationDelegate_ConfirmActions;
	public static String AbstractParallelLaunchConfigurationDelegate_Parallel_launcher_does_not_support;
	public static String AbstractParallelLaunchConfigurationDelegate_UnableToDetermineJobStatus;
	public static String AbstractParallelLaunchConfigurationDelegate_Unknown_remote_services;
	public static String AbstractParallelLaunchConfigurationDelegate_No_connection_manager_available;
	public static String AbstractParallelLaunchConfigurationDelegate_Unable_to_locate_connection;
	public static String AbstractParallelLaunchConfigurationDelegate_No_file_manager_available;
	public static String AbstractParallelLaunchConfigurationDelegate_Specified_resource_manager_not_found;
	public static String AbstractParallelLaunchConfigurationDelegate_Path_not_found;

	public static String AbstractParallelLaunchConfigurationDelegate_unableToObtainConnectionInfo;

	public static String ApplicationTab_Application;

	public static String ApplicationTab_Application_program;

	public static String ApplicationTab_Application_program_not_specified;

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

	public static String ApplicationTab_Program_selection;

	public static String ApplicationTab_Project;

	public static String ApplicationTab_Project_does_not_exist;

	public static String ApplicationTab_Project_is_closed;

	public static String ApplicationTab_Select_application;

	public static String ApplicationTab_Select_executable_to_be_copied;

	public static String ApplicationTab_Select_project;

	public static String ApplicationTab_Selection_must_be_a_file;

	public static String ApplicationTab_Unable_to_open_connection;

	public static String ArgumentsTab_Arguments;

	public static String ArgumentsTab_Cannot_read_configuration;

	public static String ArgumentsTab_Program_arguments;

	public static String ParallelLaunchConfigurationDelegate_0;
	public static String ParallelLaunchConfigurationDelegate_1;
	public static String ParallelLaunchConfigurationDelegate_3;
	public static String ParallelLaunchConfigurationDelegate_4;
	public static String ParallelLaunchConfigurationDelegate_5;
	public static String ParallelLaunchConfigurationDelegate_6;
	public static String ParallelLaunchConfigurationDelegate_7;
	public static String ParallelLaunchConfigurationDelegate_Always_start;
	public static String ParallelLaunchConfigurationDelegate_Confirm_start;
	public static String ParallelLaunchConfigurationDelegate_Failed_to_start;
	public static String ParallelLaunchConfigurationDelegate_Invalid_launch_object;

	public static String ParallelLaunchConfigurationDelegate_OpenDebugPerspective;
	public static String ParallelLaunchConfigurationDelegate_RM_currently_stopped;
	public static String ParallelLaunchConfigurationDelegate_Start_rm;

	public static String RuntimeProcess_Exit_value_not_available;

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

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
