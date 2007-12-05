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
package org.eclipse.ptp.remote.remotetools.ui;


import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ptp.remote.remotetools.Activator;
import org.eclipse.ptp.remotetools.RemotetoolsPlugin;
import org.eclipse.ptp.remotetools.utils.verification.ControlAttributes;


/**
 * Defines rules to build the target configuration from an attribute hash map.
 * 
 * @author Daniel Felix Ferber
 * @since 1.2
 */
public class ConfigFactory {
	Map<String, String> currentMap = null;
	Map<String, String> defaultMap = null;
	
	ControlAttributes attributes = null;
	
	private static final String PREFIX = "remotetools."; //$NON-NLS-1$
	public static final String ATTR_LOCALHOST_SELECTION = PREFIX + "localhost-selection"; //$NON-NLS-1$
	public static final String ATTR_LOGIN_USERNAME = PREFIX + "login-username"; //$NON-NLS-1$
	public static final String ATTR_LOGIN_PASSWORD = PREFIX + "login-password"; //$NON-NLS-1$
	public static final String ATTR_CONNECTION_ADDRESS = PREFIX + "connection-address"; //$NON-NLS-1$
	public static final String ATTR_CONNECTION_PORT = PREFIX + "connection-port"; //$NON-NLS-1$
	public static final String ATTR_KEY_PATH = PREFIX + "key-path"; //$NON-NLS-1$
	public static final String ATTR_KEY_PASSPHRASE = PREFIX + "key-passphrase"; //$NON-NLS-1$
	public static final String ATTR_IS_PASSWORD_AUTH = PREFIX + "is-passwd-auth"; //$NON-NLS-1$
	public static final String ATTR_CONNECTION_TIMEOUT = PREFIX + "connection-timeout"; //$NON-NLS-1$
	public static final String ATTR_SYSTEM_WORKSPACE = PREFIX + "system-workspace-dir"; //$NON-NLS-1$
	public static final String ATTR_CIPHER_TYPE = PREFIX + "cipher-type"; //$NON-NLS-1$
	
	public static final String DEFAULT_LOCALHOST_SELECTION = "true";
	public static final String DEFAULT_LOGIN_USERNAME = "";
	public static final String DEFAULT_LOGIN_PASSWORD = "";
	public static final String DEFAULT_CONNECTION_ADDRESS = "";
	public static final String DEFAULT_CONNECTION_PORT = "22";
	public static final String DEFAULT_CONNECTION_TIMEOUT = "5";
	public static final String DEFAULT_KEY_PATH = "";
	public static final String DEFAULT_KEY_PASSPHRASE= "";
	public static final String DEFAULT_IS_PASSWORD_AUTH = "true";
		
	public final static String[] KEY_ARRAY = { ATTR_LOCALHOST_SELECTION,
			ATTR_LOGIN_USERNAME, ATTR_CONNECTION_PORT, ATTR_CONNECTION_ADDRESS,
			ATTR_KEY_PATH, ATTR_IS_PASSWORD_AUTH, ATTR_CONNECTION_TIMEOUT,
			ATTR_SYSTEM_WORKSPACE, ATTR_CIPHER_TYPE};
	
	public final static String[] KEY_CIPHERED_ARRAY = { ATTR_KEY_PASSPHRASE,
			ATTR_LOGIN_PASSWORD };
	
	public ConfigFactory() {
		createDefaultMap();
		createCurrentMapFromPreferences();
		attributes = new ControlAttributes(currentMap, defaultMap);
	}
	
	public ConfigFactory(Map<String, String> newMap) {
		createDefaultMap();
		if (newMap == null) {
			createCurrentMapFromPreferences();
		} else {
			currentMap = new HashMap<String, String>(newMap);
		}
		attributes = new ControlAttributes(currentMap, defaultMap);
	}
	
	public Map<String, String> getMap() {
		return currentMap;
	}
	
	public ControlAttributes getAttributes() {
		return attributes;
	}
	
	private void createDefaultMap() {
		defaultMap = new HashMap<String, String>();
		defaultMap.put(ATTR_LOCALHOST_SELECTION, DEFAULT_LOCALHOST_SELECTION);
		defaultMap.put(ATTR_LOGIN_USERNAME, DEFAULT_LOGIN_USERNAME);
		defaultMap.put(ATTR_LOGIN_PASSWORD, DEFAULT_LOGIN_PASSWORD);
		defaultMap.put(ATTR_CONNECTION_PORT, DEFAULT_CONNECTION_PORT);
		defaultMap.put(ATTR_CONNECTION_ADDRESS, DEFAULT_CONNECTION_ADDRESS);
		defaultMap.put(ATTR_KEY_PATH, DEFAULT_KEY_PATH);
		defaultMap.put(ATTR_KEY_PASSPHRASE, DEFAULT_KEY_PASSPHRASE);
		defaultMap.put(ATTR_IS_PASSWORD_AUTH, DEFAULT_IS_PASSWORD_AUTH);
		defaultMap.put(ATTR_CONNECTION_TIMEOUT, DEFAULT_CONNECTION_TIMEOUT);
		defaultMap.put(ATTR_CIPHER_TYPE, RemotetoolsPlugin.CIPHER_DEFAULT);
	}
	
	private void createCurrentMapFromPreferences() {
		currentMap = new HashMap<String, String>();
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		currentMap.put(ATTR_LOGIN_USERNAME, store.getString(ATTR_LOGIN_USERNAME));
		currentMap.put(ATTR_CONNECTION_ADDRESS, store.getString(ATTR_CONNECTION_ADDRESS));
		currentMap.put(ATTR_CONNECTION_PORT, store.getString(ATTR_CONNECTION_PORT));
		currentMap.put(ATTR_SYSTEM_WORKSPACE, store.getString(ATTR_SYSTEM_WORKSPACE));
	}
}
