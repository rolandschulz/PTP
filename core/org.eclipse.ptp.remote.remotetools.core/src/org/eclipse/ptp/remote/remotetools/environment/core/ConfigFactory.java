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
package org.eclipse.ptp.remote.remotetools.environment.core;


import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ptp.remote.remotetools.Activator;
import org.eclipse.ptp.remote.remotetools.environment.conf.AttributeNames;
import org.eclipse.ptp.remote.remotetools.environment.conf.DefaultValues;
import org.eclipse.ptp.remotetools.utils.verification.ControlAttributes;
import org.eclipse.ptp.remotetools.utils.verification.IllegalAttributeException;


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
	
	private static final String PREFIX = "ptp."; //$NON-NLS-1$
	public static final String ATTR_LOCALHOST_SELECTION = PREFIX + "localhost-selection"; //$NON-NLS-1$
	public static final String ATTR_LOGIN_USERNAME = PREFIX + "login-username"; //$NON-NLS-1$
	public static final String ATTR_LOGIN_PASSWORD = PREFIX + "login-password"; //$NON-NLS-1$
	public static final String ATTR_CONNECTION_ADDRESS = PREFIX + "connection-address"; //$NON-NLS-1$
	public static final String ATTR_CONNECTION_PORT = PREFIX + "connection-port"; //$NON-NLS-1$
	public static final String ATTR_KEY_PATH = PREFIX + "key-path"; //$NON-NLS-1$
	public static final String ATTR_KEY_PASSPHRASE = PREFIX + "key-passphrase"; //$NON-NLS-1$
	public static final String ATTR_IS_PASSWORD_AUTH = PREFIX + "is-passwd-auth"; //$NON-NLS-1$
	public static final String ATTR_CONNECTION_TIMEOUT = PREFIX + "connection-timeout"; //$NON-NLS-1$
	public static final String ATTR_CIPHER_TYPE = PREFIX + "cipher-type"; //$NON-NLS-1$
	
	public final static String[] KEY_ARRAY = { ATTR_LOCALHOST_SELECTION,
			ATTR_LOGIN_USERNAME, ATTR_CONNECTION_PORT, ATTR_CONNECTION_ADDRESS,
			ATTR_KEY_PATH, ATTR_IS_PASSWORD_AUTH, ATTR_CONNECTION_TIMEOUT};
	
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
		defaultMap.put(ATTR_LOCALHOST_SELECTION, DefaultValues.LOCALHOST_SELECTION);
		defaultMap.put(ATTR_LOGIN_USERNAME, DefaultValues.LOGIN_USERNAME);
		defaultMap.put(ATTR_LOGIN_PASSWORD, DefaultValues.LOGIN_PASSWORD);
		defaultMap.put(ATTR_CONNECTION_PORT, DefaultValues.CONNECTION_PORT);
		defaultMap.put(ATTR_CONNECTION_ADDRESS, DefaultValues.CONNECTION_ADDRESS);
		defaultMap.put(ATTR_KEY_PATH, DefaultValues.KEY_PATH);
		defaultMap.put(ATTR_KEY_PASSPHRASE, DefaultValues.KEY_PASSPHRASE);
		defaultMap.put(ATTR_IS_PASSWORD_AUTH, DefaultValues.IS_PASSWORD_AUTH);
		defaultMap.put(ATTR_CONNECTION_TIMEOUT, DefaultValues.CONNECTION_TIMEOUT);
		defaultMap.put(ATTR_CIPHER_TYPE, PTPTargetControl.DEFAULT_CIPHER);
	}
	
	private void createCurrentMapFromPreferences() {
		currentMap = new HashMap<String, String>();
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		currentMap.put(ATTR_LOGIN_USERNAME, store.getString(ATTR_LOGIN_USERNAME));
		currentMap.put(ATTR_CONNECTION_ADDRESS, store.getString(ATTR_CONNECTION_ADDRESS));
		currentMap.put(ATTR_CONNECTION_PORT, store.getString(ATTR_CONNECTION_PORT));
	}
	
	public TargetConfig createTargetConfig() throws CoreException {
		try {
			TargetConfig config = new TargetConfig();
			config.setLoginUserName(attributes.getString(ATTR_LOGIN_USERNAME));
			config.setLoginPassword(attributes.getString(ATTR_LOGIN_PASSWORD));
			config.setConnectionPort(attributes.verifyInt(AttributeNames.CONNECTION_PORT, ATTR_CONNECTION_PORT));
			if (attributes.getBoolean(ATTR_LOCALHOST_SELECTION)) {
				config.setConnectionAddress("localhost"); //$NON-NLS-1$
			} else {
				config.setConnectionAddress(attributes.getString(ATTR_CONNECTION_ADDRESS));
			}
			config.setConnectionTimeout(attributes.verifyInt(AttributeNames.CONNECTION_TIMEOUT, ATTR_CONNECTION_TIMEOUT));
			config.setKeyPassphrase(attributes.getString(ATTR_KEY_PASSPHRASE));
			config.setKeyPath(attributes.getString(ATTR_KEY_PATH));
			config.setIsPasswordAuth(attributes.getBoolean(ATTR_IS_PASSWORD_AUTH));
			config.setCipherType(attributes.getString(ATTR_CIPHER_TYPE));
			return config;
		} catch (IllegalAttributeException e) {
			throw new CoreException(new Status(Status.ERROR, Activator.getDefault().getBundle().getSymbolicName(), 0, e.getMessage(), e));
		}
	}
}
