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

package org.eclipse.ptp.cell.simulator.conf;

import org.eclipse.osgi.util.NLS;

/**
 * Defines names of attributes that must (might) be provided to the simulator
 * control class. Intended for an uniform and consistent use of names for
 * attributes in GUI, error messages and configuration files across plugins.
 * 
 * @author Daniel Felix Ferber
 * 
 */
public class AttributeNames extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.ptp.cell.simulator.conf.attribute_names"; //$NON-NLS-1$

	public static String LAUNCH_DELEGATE;
	public static String SIMULATOR_BASE_DIRECTORY;
	public static String SIMULATOR_EXECUTABLE;
	public static String SNIF_EXECUTABLE;
	public static String WORK_DIRECTORY;
	public static String LOG_PATH;
	public static String TAP_PATH;
	public static String ARCHITECTURE_TCL_STRING;
	public static String MEMORY_SIZE;
	public static String DEPLOY_FILE_NAMES;
	public static String DEPLOY_FILE_SOURCES;
	public static String TCL_SCRIPT_NAME;
	public static String TCL_SOURCE_NAME;
	public static String EXTRA_COMMAND_LINE_SWITCHES;
	public static String SSH_INIT;
	public static String NETWORK_INIT;
	public static String IP_HOST;
	public static String IP_SIMULATOR;
	public static String MAC_SIMULATOR;
	public static String NETMASK_SIMULATOR;
	public static String EXTRA_IMAGE_INIT;
	public static String EXTRA_IMAGE_PATH;
	public static String EXTRA_IMAGE_PERSISTENCE;
	public static String EXTRA_IMAGE_JOURNAL_PATH;
	public static String EXTRA_IMAGE_TYPE;
	public static String EXTRA_IMAGE_MOUNTPOINT;
	public static String JAVA_API_INIT;
	public static String JAVA_API_PORT;
	public static String JAVA_API_SOCKET_PORT;
	public static String JAVA_API_SOCKET_HOST;
	public static String JAVA_API_SOCKET_MAX_TRIES;
	public static String JAVA_API_SOCKET_TRY_WAIT;	
	public static String KERNEL_IMAGE_PATH;
	public static String ROOT_IMAGE_PATH;
	public static String ROOT_IMAGE_PERSISTENCE;
	public static String ROOT_IMAGE_JOURNAL_PATH;
	public static String CONSOLE_TERMINAL_INIT;
	public static String CONSOLE_ECHO_INIT;
	public static String CONSOLE_SOCKET_INIT;
	public static String CONSOLE_PORT;
	public static String CONSOLE_SOCKET_PORT;
	public static String CONSOLE_SOCKET_HOST;
	public static String CONSOLE_SOCKET_MAX_TRIES;
	public static String CONSOLE_SOCKET_TRY_WAIT;
	public static String CONSOLE_COMMANDS;
	public static String SHOW_SIMULATOR_GUI;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, AttributeNames.class);
	}

	private AttributeNames() {
	}
}
