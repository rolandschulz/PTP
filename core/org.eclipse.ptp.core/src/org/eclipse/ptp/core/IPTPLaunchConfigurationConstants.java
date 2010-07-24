/*******************************************************************************
 * Copyright (c) 2005 The Regents of the University of California. 
 * This material was produced under U.S. Government contract W-7405-ENG-36 
 * for Los Alamos National Laboratory, which is operated by the University 
 * of California for the U.S. Department of Energy. The U.S. Government has 
 * rights to use, reproduce, and distribute this software. NEITHER THE 
 * GOVERNMENT NOR THE UNIVERSITY MAKES ANY WARRANTY, EXPRESS OR IMPLIED, OR 
 * ASSUMES ANY LIABILITY FOR THE USE OF THIS SOFTWARE. If software is modified 
 * to produce derivative works, such modified software should be clearly marked, 
 * so as not to confuse it with the version available from LANL.
 * 
 * Additionally, this program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * LA-CC 04-115
 *******************************************************************************/
package org.eclipse.ptp.core;

/**
 *
 */
public interface IPTPLaunchConfigurationConstants {
	/**
	 * Plugin ID
	 */
	public static final String PTP_LAUNCHCONFIGURETYPE_ID = "org.eclipse.ptp.launch.parallelLaunch"; //$NON-NLS-1$

	/**
	 * Unique string for launch attributes
	 */
	public static final String PTP_LAUNCH_ID = "org.eclipse.ptp.launch"; //$NON-NLS-1$

	/**
	 * Launch configuration attribute value. The key is
	 * ATTR_DEBUGGER_STOP_AT_MAIN.
	 */
	public static final boolean DEBUGGER_STOP_AT_MAIN_DEFAULT = true;

	/**
	 * Launch configuration attribute value. The key is
	 * ATTR_DEBUGGER_START_MODE. Startup debugger running the program.
	 */
	public static final String DEBUGGER_MODE_RUN = "run"; //$NON-NLS-1$

	/**
	 * Launch configuration attribute value. The key is
	 * ATTR_DEBUGGER_START_MODE. Startup debugger and attach to running process.
	 */
	public static final String DEBUGGER_MODE_ATTACH = "attach"; //$NON-NLS-1$

	/**
	 * Launch configuration attribute key. The values is the selected resource
	 * manager.
	 */
	public static final String ATTR_RESOURCE_MANAGER_UNIQUENAME = PTP_LAUNCH_ID + ".RESOURCE_MANAGER_NAME"; //$NON-NLS-1$

	/**
	 * Launch configuration attribute key. The value is the selected queue name
	 */
	public static final String ATTR_QUEUE_NAME = PTP_LAUNCH_ID + ".QUEUE_NAME"; //$NON-NLS-1$

	/**
	 * Launch configuration attribute key. The value is the project name.
	 */
	public static final String ATTR_PROJECT_NAME = PTP_LAUNCH_ID + ".PROJECT_ATTR"; //$NON-NLS-1$

	/**
	 * Launch configuration attribute key. The value is the application
	 * (executable) name
	 */
	public static final String ATTR_APPLICATION_NAME = PTP_LAUNCH_ID + ".APPLICATION_NAME"; //$NON-NLS-1$

	/**
	 * Launch configuration attribute key. The value is the platform string of
	 * the launch configuration
	 */
	public static final String ATTR_PLATFORM = PTP_LAUNCH_ID + ".PLATFFORM"; //$NON-NLS-1$

	/**
	 * Launch configuration attribute key. The value is true if the debugger
	 * should stop in main().
	 */
	public static final String ATTR_STOP_IN_MAIN = PTP_LAUNCH_ID + ".STOP_IN_MAIN"; //$NON-NLS-1$

	/**
	 * Launch configuration attribute key. The value is the ID of the debugger
	 * used in launch
	 */
	public static final String ATTR_DEBUGGER_ID = PTP_LAUNCH_ID + ".DEBUGGER_ID"; //$NON-NLS-1$

	/**
	 * Launch configuration attribute key. The value is the program arguments
	 * that have been supplied.
	 */
	public static final String ATTR_ARGUMENTS = PTP_LAUNCH_ID + ".ARGUMENT_ATTR"; //$NON-NLS-1$

	/**
	 * Launch configuration attribute key. The value is the working directory.
	 */
	public static final String ATTR_WORK_DIRECTORY = PTP_LAUNCH_ID + ".WORK_DIRECTORY_ATTR"; //$NON-NLS-1$

	/**
	 * Launch configuration attribute key. The value is the platform string of
	 * the launch configuration
	 */
	public static final String ATTR_DEBUGGER_SPECIFIC_ATTRS_MAP = PTP_LAUNCH_ID + ".DEBUGGER_SPECIFIC_ATTRS_MAP"; //$NON-NLS-1$

	/**
	 * Launch configuration attribute key. The value is a global variables'
	 * memento.
	 */
	public static final String ATTR_DEBUGGER_GLOBAL_VARIABLES = PTP_LAUNCH_ID + ".GLOBAL_VARIABLES"; //$NON-NLS-1$

	/**
	 * Launch configuration attribute key. The value is the startup mode for the
	 * debugger.
	 */
	public static final String ATTR_DEBUGGER_START_MODE = PTP_LAUNCH_ID + ".DEBUGGER_START_MODE"; //$NON-NLS-1$

	/**
	 * Launch configuration attribute key. The value is the location of the
	 * executable being debugged.
	 */
	public static final String ATTR_DEBUGGER_EXECUTABLE_PATH = PTP_LAUNCH_ID + ".DEBUGGER_EXECUTABLE_PATH"; //$NON-NLS-1$

	/**
	 * Launch configuration attribute key. The value is the working directory
	 * from which to run the debugger.
	 */
	public static final String ATTR_DEBUGGER_WORKING_DIR = PTP_LAUNCH_ID + ".DEBUGGER_WORKING_DIR"; //$NON-NLS-1$

	/**
	 * TODO: Please document what this is
	 */
	public static final String ATTR_DEBUGGER_MEMORY_BLOCKS = PTP_LAUNCH_ID + ".MEMORY_BLOCKS"; //$NON-NLS-1$

	/**
	 * TODO: Please document what this is
	 */
	public static final String ATTR_DEBUGGER_REGISTER_GROUPS = PTP_LAUNCH_ID + ".DEBUGGER_REGISTER_GROUPS"; //$NON-NLS-1$

	/**
	 * Launch configuration attribute key. The value is a boolean that states if
	 * the executable will be copied from the local machine to the remote
	 * machine.
	 */
	public static final String ATTR_COPY_EXECUTABLE = PTP_LAUNCH_ID + ".ATTR_COPY_EXECUTABLE_FROM_LOCAL"; //$NON-NLS-1$

	/**
	 * Launch configuration attribute key. The value is the absolute path to the
	 * executable that will be copied to the remote machine.
	 */
	public static final String ATTR_LOCAL_EXECUTABLE_PATH = PTP_LAUNCH_ID + ".ATTR_LOCAL_EXECUTABLE_PATH"; //$NON-NLS-1$

	/**
	 * Launch configuration attribute key. The value is the absolute path to the
	 * executable that is to be launched. If the executable is located on a
	 * remote machine, it is the path to the executable on this machine.
	 */
	public static final String ATTR_EXECUTABLE_PATH = PTP_LAUNCH_ID + ".ATTR_REMOTE_EXECUTABLE_PATH"; //$NON-NLS-1$

	/**
	 * Launch configuration attribute key. The value is the address of the host
	 * that will be contacted by the debug server when it starts.
	 */
	public static final String ATTR_DEBUGGER_HOST = PTP_LAUNCH_ID + ".ATTR_DEBUGGER_HOST"; //$NON-NLS-1$

	/**
	 * Launch configuration attribute key. The value is a boolean flag
	 * indicating if output from all processes should be combined into a single
	 * console.
	 */
	public static final String ATTR_CONSOLE = PTP_LAUNCH_ID + ".ATTR_CONSOLE"; //$NON-NLS-1$

	/**
	 * Error codes
	 */
	public static final int ERR_PROGRAM_NOT_EXIST = 104;
	public static final int ERR_PROGRAM_NOT_BINARY = 107;

	/**
	 * Launch configuration attribute key. The value is a boolean flag
	 * indicating if file synchronization will occur after the execution.
	 */
	public static final String ATTR_SYNC_AFTER = PTP_LAUNCH_ID + ".ATTR_SYNC_AFTER"; //$NON-NLS-1$

	/**
	 * Launch configuration attribute key. The value is a boolean flag
	 * indicating if file synchronization will occur before the execution.
	 */
	public static final String ATTR_SYNC_BEFORE = PTP_LAUNCH_ID + ".ATTR_SYNC_BEFORE"; //$NON-NLS-1$

	/**
	 * Launch configuration attribute key. The value is a list of rules of
	 * synchronization.
	 */
	public static final String ATTR_SYNC_RULES = PTP_LAUNCH_ID + ".ATTR_SYNC_RULES"; //$NON-NLS-1$
}
