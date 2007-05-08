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
	public static final String PTP_LAUNCHCONFIGURETYPE_ID = "org.eclipse.ptp.launch.parallelLaunch";

	public static final String PTP_LAUNCH_ID = "org.eclipse.ptp.launch";

	public static final String P_TYPE = "P";

	public static final String G_TYPE = "G";

	public static final String ATTR_PROJECT_NAME = PTP_LAUNCH_ID + ".PROJECT_ATTR";

	/**
	 * Launch configuration attribute key. The value is a string specifying
	 * application a C/C++ launch configuration.
	 */
	//public static final String ATTR_PROGRAM_NAME = PTP_LAUNCH_ID + ".PROGRAM_NAME";

	public static final String ATTR_APPLICATION_NAME = PTP_LAUNCH_ID + ".APPLICATION_NAME";
	
	public static final String RESOURCE_MANAGER_UNIQUENAME = PTP_LAUNCH_ID + ".RESOURCE_MANAGER_NAME";

	public static final String QUEUE_NAME = PTP_LAUNCH_ID + ".QUEUE_NAME";
	
	public static final String LAUNCH_ATTR_MAP = PTP_LAUNCH_ID + ".LAUNCH_ATTR_MAP";

	public static final String ATTR_STOP_IN_MAIN = PTP_LAUNCH_ID + ".STOP_IN_MAIN";

	public static final String ATTR_DEBUGGER_ID = PTP_LAUNCH_ID + ".DEBUGGER_ID";

	// public static final String NETWORK_TYPE = PTP_LAUNCH_ID + ".COMMUNICATION";
	public static final String ATTR_ARGUMENT = PTP_LAUNCH_ID + ".ARGUMENT_ATTR";

	public static final String ATTR_WORK_DIRECTORY = PTP_LAUNCH_ID + ".WORK_DIRECTORY_ATTR";

	/**
	 * Launch configuration attribute key. The value is the platform string of
	 * the launch configuration
	 */
	public static final String ATTR_PLATFORM = PTP_LAUNCH_ID + ".PLATFFORM";

	public static final String DEF_NETWORK_TYPE = P_TYPE;

	/**
	 * Launch configuration attribute key. The value is the platform string of
	 * the launch configuration
	 */
	public static final String ATTR_DEBUGGER_SPECIFIC_ATTRS_MAP = PTP_LAUNCH_ID + ".DEBUGGER_SPECIFIC_ATTRS_MAP";
	
	/**
	 * Launch configuration attribute key. The value is a global variables'
	 * memento.
	 */
	public static final String ATTR_DEBUGGER_GLOBAL_VARIABLES = PTP_LAUNCH_ID + ".GLOBAL_VARIABLES";

	/**
	 * Launch configuration attribute key. The value is the startup mode for the
	 * debugger.
	 */
	public static final String ATTR_DEBUGGER_START_MODE = PTP_LAUNCH_ID + ".DEBUGGER_START_MODE";

	/**
	 * Launch configuration attribute key. The value is the location of the executable
	 * being debugged.
	 */
	public static final String ATTR_DEBUGGER_EXECUTABLE_PATH = PTP_LAUNCH_ID + ".DEBUGGER_EXECUTABLE_PATH";

	/**
	 * Launch configuration attribute key. The value is the working directory from
	 * which to run the debugger.
	 */
	public static final String ATTR_DEBUGGER_WORKING_DIR = PTP_LAUNCH_ID + ".DEBUGGER_WORKING_DIR";

	/**
	 * Launch configuration attribute value. The key is
	 * ATTR_DEBUGGER_STOP_AT_MAIN.
	 */
	public static boolean DEBUGGER_STOP_AT_MAIN_DEFAULT = true;

	/**
	 * Launch configuration attribute value. The key is
	 * ATTR_DEBUGGER_START_MODE. Startup debugger running the program.
	 */
	public static String DEBUGGER_MODE_RUN = "run";

	/**
	 * Launch configuration attribute value. The key is
	 * ATTR_DEBUGGER_START_MODE. Startup debugger and attach to running process.
	 */
	public static String DEBUGGER_MODE_ATTACH = "attach";
	
	/**
	 * error code
	 */
	public static final int ERR_PROGRAM_NOT_EXIST = 104;
	public static final int ERR_PROGRAM_NOT_BINARY = 107;

	public static final String ATTR_DEBUGGER_MEMORY_BLOCKS = PTP_LAUNCH_ID + ".MEMORY_BLOCKS";
}
