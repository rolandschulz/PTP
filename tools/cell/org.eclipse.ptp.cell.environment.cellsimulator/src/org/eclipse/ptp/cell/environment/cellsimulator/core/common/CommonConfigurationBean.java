/**
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *
 */
package org.eclipse.ptp.cell.environment.cellsimulator.core.common;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.ptp.cell.environment.cellsimulator.conf.CommonDefaultValues;
import org.eclipse.ptp.remotetools.utils.verification.ControlAttributes;

/**
 * Defines configuration attributes to build the target configuration from an
 * attribute hash map. The attributes are divided into three groups. One that
 * are specific how to launch the simulator, another that are related to the
 * connection from the environment to the simulator.
 * 
 * Two maps are used. The {@link #currentMap} contains entries for attributes
 * that were provided by the user.
 * The {@link #defaultMap} contains default valid entries for all attributes
 * and is used for fallback when the entry in {@link #currentMap} is missing
 * or is damaged.
 * 
 * @author Daniel Felix Ferber
 * @since 1.0
 */
public abstract class CommonConfigurationBean {
	protected Map currentMap = null;
	protected Map defaultMap = null;
	
	protected ControlAttributes attributes = null;
	
	public CommonConfigurationBean() {
		createDefaultMap();
		createCurrentMapFromPreferences();
		
		attributes = new ControlAttributes(currentMap, defaultMap);
	}
	
	public CommonConfigurationBean(Map newMap) {
		createDefaultMap();
		
		if (newMap == null) {
			createCurrentMapFromPreferences();
		} else {
			currentMap = new HashMap(newMap);
		}
		attributes = new ControlAttributes(currentMap, defaultMap);
	}
	
	public Map getMap() {
		return currentMap;
	}
	
	public ControlAttributes getAttributes() {
		return attributes;
	}
	
	protected void createDefaultMap() {
		defaultMap = new HashMap();

		/* 
		 * Only initialize attributes that are equally handles on local and remote simulator.
		 * The other attributes are initialized the respective child classes.
		 */
		defaultMap.put(ATTR_SIMULATOR_BASE_DIRECTORY, CommonDefaultValues.SIMULATOR_BASE_DIRECTORY); 
		
		defaultMap.put(ATTR_ARCHITECTURE_ID, CommonDefaultValues.ARCHITECTURE_ID); 
		defaultMap.put(ATTR_MEMORY_SIZE, CommonDefaultValues.MEMORY_SIZE); 
		defaultMap.put(ATTR_PROFILE_ID, CommonDefaultValues.PROFILE_ID); 
		defaultMap.put(ATTR_EXTRA_COMMAND_LINE_SWITCHES, CommonDefaultValues.EXTRA_COMMAND_LINE_SWITCHES); 
		
		defaultMap.put(ATTR_AUTOMATIC_AUTHENTICATION, CommonDefaultValues.AUTOMATIC_AUTHENTICATION); 
		defaultMap.put(ATTR_USERNAME, CommonDefaultValues.USERNAME); 
		defaultMap.put(ATTR_PASSWORD, CommonDefaultValues.PASSWORD); 
		defaultMap.put(ATTR_TIMEOUT, CommonDefaultValues.TIMEOUT); 
		defaultMap.put(ATTR_SIMULATOR_CIPHER_TYPE, AbstractTargetControl.DEFAULT_SIMULATOR_CIPHER);
		
		defaultMap.put(ATTR_EXTRA_IMAGE_INIT, CommonDefaultValues.EXTRA_IMAGE_INIT); 
		defaultMap.put(ATTR_EXTRA_IMAGE_PATH, CommonDefaultValues.EXTRA_IMAGE_PATH); 
		defaultMap.put(ATTR_EXTRA_IMAGE_PERSISTENCE, CommonDefaultValues.EXTRA_IMAGE_PERSISTENCE); 
		defaultMap.put(ATTR_EXTRA_IMAGE_JOURNAL_PATH, CommonDefaultValues.EXTRA_IMAGE_JOURNAL_PATH); 
		defaultMap.put(ATTR_EXTRA_IMAGE_TYPE, CommonDefaultValues.EXTRA_IMAGE_TYPE); 
		defaultMap.put(ATTR_EXTRA_IMAGE_MOUNTPOINT, CommonDefaultValues.EXTRA_IMAGE_MOUNTPOINT); 

		defaultMap.put(ATTR_KERNEL_IMAGE_PATH, CommonDefaultValues.KERNEL_IMAGE_PATH); 
		defaultMap.put(ATTR_ROOT_IMAGE_PATH, CommonDefaultValues.ROOT_IMAGE_PATH); 
		defaultMap.put(ATTR_ROOT_IMAGE_PERSISTENCE, CommonDefaultValues.ROOT_IMAGE_PERSISTENCE); 
		defaultMap.put(ATTR_ROOT_IMAGE_JOURNAL_PATH, CommonDefaultValues.ROOT_IMAGE_JOURNAL_PATH);

		defaultMap.put(ATTR_CUSTOMIZATION_SCRIPT, CommonDefaultValues.CUSTOMIZATION_SCRIPT);
	}

	protected void createCurrentMapFromPreferences() {
		currentMap = new HashMap();
		// IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		// There are no preferences for common attributes, yet
	}

	/**************************************************************************
	 * Attribute KEYs
	 */
	/* These attributes are equally handled for local and remote simulator. */
	public static final String ATTR_SIMULATOR_BASE_DIRECTORY = "simulator-base-directory"; //$NON-NLS-1$
	
	public static final String ATTR_ARCHITECTURE_ID = "architecture-id"; //$NON-NLS-1$
	public static final String ATTR_MEMORY_SIZE = "memory-size"; //$NON-NLS-1$
	public static final String ATTR_PROFILE_ID = "profile-id"; //$NON-NLS-1$
	public static final String ATTR_EXTRA_COMMAND_LINE_SWITCHES = "extra-command-line-switches"; //$NON-NLS-1$
	
	public static final String ATTR_AUTOMATIC_AUTHENTICATION = "automatic-authentication"; //$NON-NLS-1$
	public static final String ATTR_USERNAME = "username"; //$NON-NLS-1$
	public static final String ATTR_PASSWORD = "password"; //$NON-NLS-1$
	public static final String ATTR_TIMEOUT = "timeout"; //$NON-NLS-1$
	public static final String ATTR_SIMULATOR_CIPHER_TYPE = "simulator-cipher-type"; //$NON-NLS-1$
	
	public static final String ATTR_EXTRA_IMAGE_INIT = "extra-image-init"; //$NON-NLS-1$
	public static final String ATTR_EXTRA_IMAGE_PATH = "extra-image-path"; //$NON-NLS-1$
	public static final String ATTR_EXTRA_IMAGE_PERSISTENCE = "extra-image-persistence"; //$NON-NLS-1$
	public static final String ATTR_EXTRA_IMAGE_JOURNAL_PATH = "extra-image-journal-path"; //$NON-NLS-1$
	public static final String ATTR_EXTRA_IMAGE_TYPE = "extra-image-type"; //$NON-NLS-1$
	public static final String ATTR_EXTRA_IMAGE_MOUNTPOINT = "extra-image-mountpoint"; //$NON-NLS-1$
	
	public static final String ATTR_KERNEL_IMAGE_PATH = "kernel-image-path"; //$NON-NLS-1$
	public static final String ATTR_ROOT_IMAGE_PATH = "root-image-path"; //$NON-NLS-1$
	public static final String ATTR_ROOT_IMAGE_PERSISTENCE = "root-image-persistence"; //$NON-NLS-1$
	public static final String ATTR_ROOT_IMAGE_JOURNAL_PATH = "root-image-journal-path"; //$NON-NLS-1$
	
	public static final String ATTR_CUSTOMIZATION_SCRIPT = "customization-script"; //$NON-NLS-1$
	
	/* Although these attributes are common, they are handled different on local and remote. */
	public static final String ATTR_WORK_DIRECTORY = "work-directory"; //$NON-NLS-1$
	public static final String ATTR_SHOW_SIMULATOR_GUI = "show-simulator-gui"; //$NON-NLS-1$
	public static final String ATTR_CONSOLE_SHOW_LINUX = "console-show-linux"; //$NON-NLS-1$
	public static final String ATTR_CONSOLE_SHOW_SIMULATOR = "console-show-simulator"; //$NON-NLS-1$
	
	public static final String ATTR_AUTOMATIC_NETWORK = "automatic-network"; //$NON-NLS-1$
	public static final String ATTR_IP_HOST = "ip-host"; //$NON-NLS-1$
	public static final String ATTR_IP_SIMULATOR = "ip-simulator"; //$NON-NLS-1$
	public static final String ATTR_MAC_SIMULATOR = "mac-simulator"; //$NON-NLS-1$

	public static final String ATTR_AUTOMATIC_PORTCONFIG = "automatic-portconfig"; //$NON-NLS-1$
	public static final String ATTR_JAVA_API_SOCKET_PORT = "java-api-socket-port"; //$NON-NLS-1$
	public static final String ATTR_CONSOLE_SOCKET_PORT = "console-socket-port"; //$NON-NLS-1$
	
	public static final String ATTR_SYSTEM_WORKSPACE = "system-workspace-dir"; //$NON-NLS-1$
	
	/* Possible valid values for persistence attributes. */
	public static final String ID_PERSISTENCE_DISCARD = "discard"; //$NON-NLS-1$
	public static final String ID_PERSISTENCE_WRITE = "write"; //$NON-NLS-1$
	public static final String ID_PERSISTENCE_JOURNAL = "journal"; //$NON-NLS-1$
	
	/* Possible valid values for filesystem type attribute */
	public static final String ID_FILESYSTEM_EXT3 = "ext3"; //$NON-NLS-1$
	public static final String ID_FILESYSTEM_EXT2 = "ext2"; //$NON-NLS-1$
	public static final String ID_FILESYSTEM_ISO9660 = "iso9660"; //$NON-NLS-1$
	
	protected final static String [] SPECIFIC_KEY_ARRAY = {
		/* Common values. */
		ATTR_SIMULATOR_BASE_DIRECTORY,
		
		ATTR_ARCHITECTURE_ID,
		ATTR_MEMORY_SIZE,
		ATTR_PROFILE_ID,
		ATTR_EXTRA_COMMAND_LINE_SWITCHES,
		
		ATTR_AUTOMATIC_AUTHENTICATION,
		ATTR_USERNAME,
		ATTR_TIMEOUT,
		ATTR_SIMULATOR_CIPHER_TYPE,
		
		ATTR_EXTRA_IMAGE_INIT,
		ATTR_EXTRA_IMAGE_PATH,
		ATTR_EXTRA_IMAGE_PERSISTENCE,
		ATTR_EXTRA_IMAGE_JOURNAL_PATH,
		ATTR_EXTRA_IMAGE_TYPE,
		ATTR_EXTRA_IMAGE_MOUNTPOINT,
		
		/* Common values, but that are handled by specific beans */
		ATTR_WORK_DIRECTORY,
		ATTR_SHOW_SIMULATOR_GUI,
		ATTR_CONSOLE_SHOW_LINUX,
		ATTR_CONSOLE_SHOW_SIMULATOR,
		
		ATTR_KERNEL_IMAGE_PATH,
		ATTR_ROOT_IMAGE_PATH,
		ATTR_ROOT_IMAGE_PERSISTENCE,
		ATTR_ROOT_IMAGE_JOURNAL_PATH,
		
		ATTR_CUSTOMIZATION_SCRIPT,
		
		ATTR_AUTOMATIC_NETWORK,
		ATTR_IP_HOST,
		ATTR_IP_SIMULATOR,
		ATTR_MAC_SIMULATOR,
		
		ATTR_AUTOMATIC_PORTCONFIG,
		ATTR_JAVA_API_SOCKET_PORT,
		ATTR_CONSOLE_SOCKET_PORT,
		
		ATTR_SYSTEM_WORKSPACE,
	};
	
	protected final static String [] SPECIFIC_KEY_CIPHERED_ARRAY = { 
		ATTR_PASSWORD,
	};
		
	public abstract CommonConfigFactory createFactory();
}
