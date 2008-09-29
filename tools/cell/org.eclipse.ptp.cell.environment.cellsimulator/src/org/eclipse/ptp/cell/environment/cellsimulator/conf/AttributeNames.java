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

package org.eclipse.ptp.cell.environment.cellsimulator.conf;

import org.eclipse.osgi.util.NLS;

/**
 * Defines names of attributes. Intended for an uniform and consistent use of names for
 * attributes in messages, error messages and configuration files across plugins.
 * It is not intended (but may be used) for displaying labels on GUI.
 * 
 * @author Daniel Felix Ferber
 * 
 */
public class AttributeNames extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.ptp.cell.environment.cellsimulator.conf.attribute_names"; //$NON-NLS-1$

	public static String SIMULATOR_BASE_DIRECTORY;
	public static String WORK_DIRECTORY;
	public static String AUTOMATIC_WORK_DIRECTORY;
	
	public static String ARCHITECTURE_ID;
	public static String MEMORY_SIZE;
	public static String PROFILE_ID;
	public static String EXTRA_COMMAND_LINE_SWITCHES;
	
	public static String AUTOMATIC_AUTHENTICATION;
	public static String USERNAME;
	public static String PASSWORD;
	public static String TIMEOUT;
	public static String CIPHER_TYPE;
	
	public static String EXTRA_IMAGE_INIT;
	public static String EXTRA_IMAGE_PATH;
	public static String EXTRA_IMAGE_PERSISTENCE;
	public static String EXTRA_IMAGE_JOURNAL_PATH;
	public static String EXTRA_IMAGE_JOURNAL_TYPE;
	public static String EXTRA_IMAGE_JOURNAL_MOUNTPOINT;

	public static String KERNEL_IMAGE_PATH;
	public static String ROOT_IMAGE_PATH;
	public static String ROOT_IMAGE_PERSISTENCE;
	public static String ROOT_IMAGE_JOURNAL_PATH;

	public static String CUSTOMIZATION_SCRIPT;

	public static String SHOW_SIMULATOR_GUI;
	public static String CONSOLE_SHOW_LINUX;
	public static String CONSOLE_SHOW_SIMULATOR;

	public static String AUTOMATIC_NETWORK;
	public static String IP_HOST;
	public static String IP_SIMULATOR;
	public static String MAC_SIMULATOR;

	public static String AUTOMATIC_PORTCONFIG;
	public static String JAVA_API_SOCKET_PORT;
	public static String CONSOLE_SOCKET_PORT;;

	public static String REMOTE_LOGIN_USERNAME;
	public static String REMOTE_LOGIN_PASSWORD;
	public static String REMOTE_CONNECTION_ADDRESS;
	public static String REMOTE_CONNECTION_PORT;
	public static String REMOTE_KEY_PATH;
	public static String REMOTE_KEY_PASSPHRASE;
	public static String REMOTE_IS_PASSWORD_AUTH;
	public static String REMOTE_TIMEOUT;
	public static String REMOTE_CIPHER_TYPE;
	
	public static String SYSTEM_WORKSPACE;
	
	static {
		NLS.initializeMessages(BUNDLE_NAME, AttributeNames.class);
	}

	private AttributeNames() {
	}
}

