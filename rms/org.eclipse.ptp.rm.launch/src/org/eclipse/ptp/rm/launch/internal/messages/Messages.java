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

	public static String ResourcesTab_No_Resource_Manager_Available;
	public static String ResourcesTab_Resources;
	public static String ResourcesTab_No_Resource_Manager;
	public static String ResourcesTab_No_Launch_Configuration;
	public static String ResourcesTab_pleaseSelectRM;
	public static String ResourcesTab_Resource_Manager_Not_Started;

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
	public static String AbstractParallelLaunchConfigurationDelegate_Parallel_launcher_does_not_support;
	public static String AbstractParallelLaunchConfigurationDelegate_UnableToDetermineJobStatus;
	public static String AbstractParallelLaunchConfigurationDelegate_Unknown_remote_services;
	public static String AbstractParallelLaunchConfigurationDelegate_No_connection_manager_available;
	public static String AbstractParallelLaunchConfigurationDelegate_Unable_to_locate_connection;
	public static String AbstractParallelLaunchConfigurationDelegate_No_file_manager_available;
	public static String AbstractParallelLaunchConfigurationDelegate_Specified_resource_manager_not_found;
	public static String AbstractParallelLaunchConfigurationDelegate_Path_not_found;

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
	public static String ParallelLaunchConfigurationDelegate_RM_currently_stopped;
	public static String ParallelLaunchConfigurationDelegate_Start_rm;

	public static String RuntimeProcess_Exit_value_not_available;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
