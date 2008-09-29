/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.cell.environment.cellsimulator.core.remote;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ptp.cell.environment.cellsimulator.CellSimulatorTargetPlugin;
import org.eclipse.ptp.cell.environment.cellsimulator.conf.RemoteDefaultValues;
import org.eclipse.ptp.cell.environment.cellsimulator.core.common.CommonConfigFactory;
import org.eclipse.ptp.cell.environment.cellsimulator.core.common.CommonConfigurationBean;


public class RemoteConfigurationBean extends CommonConfigurationBean {

	public static final String ATTR_PREFERENCES_PREFIX = "remote-"; //$NON-NLS-1$
	
	public static final String ATTR_REMOTE_LOGIN_USERNAME = "remote-login-username"; //$NON-NLS-1$
	public static final String ATTR_REMOTE_LOGIN_PASSWORD = "remote-login-password"; //$NON-NLS-1$
	public static final String ATTR_REMOTE_CONNECTION_ADDRESS = "remote-connection-address"; //$NON-NLS-1$
	public static final String ATTR_REMOTE_CONNECTION_PORT = "remote-connection-port"; //$NON-NLS-1$
	public static final String ATTR_REMOTE_KEY_PATH = "remote-key-path"; //$NON-NLS-1$
	public static final String ATTR_REMOTE_KEY_PASSPHRASE = "remote-key-passphrase"; //$NON-NLS-1$
	public static final String ATTR_REMOTE_IS_PASSWORD_AUTH = "remote-is-password-auth"; //$NON-NLS-1$
	public static final String ATTR_REMOTE_TIMEOUT = "remote-timeout"; //$NON-NLS-1$
	public static final String ATTR_REMOTE_CIPHER_TYPE = "remote-cipher-type"; //$NON-NLS-1$

	protected final static String [] SPECIFIC_KEY_ARRAY = { 
		ATTR_REMOTE_LOGIN_USERNAME,
		ATTR_REMOTE_CONNECTION_ADDRESS,
		ATTR_REMOTE_CONNECTION_PORT,
		ATTR_REMOTE_KEY_PATH,
		ATTR_REMOTE_IS_PASSWORD_AUTH,
		ATTR_REMOTE_TIMEOUT,
		ATTR_REMOTE_CIPHER_TYPE
	};

	protected final static String [] SPECIFIC_KEY_CIPHERED_ARRAY = {
		ATTR_REMOTE_LOGIN_PASSWORD,			
		ATTR_REMOTE_KEY_PASSPHRASE,
	};
	
	public final static String [] KEY_ARRAY;
	public final static String [] KEY_CIPHERED_ARRAY;

	
	static {
		List list;
		
		list = new Vector(Arrays.asList(CommonConfigurationBean.SPECIFIC_KEY_ARRAY));
		list.addAll(Arrays.asList(SPECIFIC_KEY_ARRAY));
		KEY_ARRAY= (String[]) list.toArray(new String [list.size()]);
		
		list = new Vector(Arrays.asList(CommonConfigurationBean.SPECIFIC_KEY_CIPHERED_ARRAY));
		list.addAll(Arrays.asList(SPECIFIC_KEY_CIPHERED_ARRAY));
		KEY_CIPHERED_ARRAY= (String[]) list.toArray(new String [list.size()]);
	}
	
	public RemoteConfigurationBean() {
		super();
	}

	public RemoteConfigurationBean(Map newMap) {
		super(newMap);
	}

	protected void createDefaultMap() {
		super.createDefaultMap();
		
		defaultMap.put(ATTR_WORK_DIRECTORY, RemoteDefaultValues.WORK_DIRECTORY); 

		defaultMap.put(ATTR_SHOW_SIMULATOR_GUI, RemoteDefaultValues.SHOW_SIMULATOR_GUI); 
		defaultMap.put(ATTR_CONSOLE_SHOW_LINUX, RemoteDefaultValues.CONSOLE_SHOW_LINUX); 
		defaultMap.put(ATTR_CONSOLE_SHOW_SIMULATOR, RemoteDefaultValues.CONSOLE_SHOW_SIMULATOR); 

		defaultMap.put(ATTR_AUTOMATIC_NETWORK, RemoteDefaultValues.AUTOMATIC_NETWORK); 
		defaultMap.put(ATTR_IP_HOST, RemoteDefaultValues.IP_HOST); 
		defaultMap.put(ATTR_IP_SIMULATOR, RemoteDefaultValues.IP_SIMULATOR); 
		defaultMap.put(ATTR_MAC_SIMULATOR, RemoteDefaultValues.MAC_SIMULATOR); 
		
		defaultMap.put(ATTR_AUTOMATIC_PORTCONFIG, RemoteDefaultValues.AUTOMATIC_PORTCONFIG);
		defaultMap.put(ATTR_JAVA_API_SOCKET_PORT, RemoteDefaultValues.JAVA_API_SOCKET_PORT); 
		defaultMap.put(ATTR_CONSOLE_SOCKET_PORT, RemoteDefaultValues.CONSOLE_SOCKET_PORT); 
		
		defaultMap.put(ATTR_SYSTEM_WORKSPACE, RemoteDefaultValues.SYSTEM_WORKSPACE); 
		
		defaultMap.put(ATTR_REMOTE_LOGIN_USERNAME, RemoteDefaultValues.REMOTE_LOGIN_USERNAME);
		defaultMap.put(ATTR_REMOTE_LOGIN_PASSWORD, RemoteDefaultValues.REMOTE_LOGIN_PASSWORD);
		defaultMap.put(ATTR_REMOTE_CONNECTION_ADDRESS, RemoteDefaultValues.REMOTE_CONNECTION_ADDRESS);
		defaultMap.put(ATTR_REMOTE_CONNECTION_PORT, RemoteDefaultValues.REMOTE_CONNECTION_PORT);
		defaultMap.put(ATTR_REMOTE_KEY_PATH, RemoteDefaultValues.REMOTE_KEY_PATH);
		defaultMap.put(ATTR_REMOTE_KEY_PASSPHRASE, RemoteDefaultValues.REMOTE_KEY_PASSPHRASE);
		defaultMap.put(ATTR_REMOTE_IS_PASSWORD_AUTH, RemoteDefaultValues.REMOTE_IS_PASSWORD_AUTH);
		defaultMap.put(ATTR_REMOTE_TIMEOUT, RemoteDefaultValues.REMOTE_TIMEOUT);
		defaultMap.put(ATTR_REMOTE_CIPHER_TYPE, RemoteTargetControl.DEFAULT_HOST_CIPHER);
	}

	protected void createCurrentMapFromPreferences() {
		super.createCurrentMapFromPreferences();
		IPreferenceStore store = CellSimulatorTargetPlugin.getDefault().getPreferenceStore();
		
		currentMap.put(ATTR_SIMULATOR_BASE_DIRECTORY, store.getString(ATTR_PREFERENCES_PREFIX + ATTR_SIMULATOR_BASE_DIRECTORY));
		currentMap.put(ATTR_WORK_DIRECTORY, store.getString(ATTR_PREFERENCES_PREFIX + ATTR_WORK_DIRECTORY));
		currentMap.put(ATTR_ROOT_IMAGE_PATH, store.getString(ATTR_PREFERENCES_PREFIX + ATTR_ROOT_IMAGE_PATH));
		currentMap.put(ATTR_KERNEL_IMAGE_PATH, store.getString(ATTR_PREFERENCES_PREFIX + ATTR_KERNEL_IMAGE_PATH));

		currentMap.put(ATTR_SHOW_SIMULATOR_GUI, store.getString(ATTR_PREFERENCES_PREFIX + ATTR_SHOW_SIMULATOR_GUI));
		currentMap.put(ATTR_CONSOLE_SHOW_LINUX, store.getString(ATTR_PREFERENCES_PREFIX + ATTR_CONSOLE_SHOW_LINUX));
		currentMap.put(ATTR_CONSOLE_SHOW_SIMULATOR, store.getString(ATTR_PREFERENCES_PREFIX + ATTR_CONSOLE_SHOW_SIMULATOR));			

		currentMap.put(ATTR_SYSTEM_WORKSPACE, store.getString(ATTR_PREFERENCES_PREFIX + ATTR_SYSTEM_WORKSPACE));			
	}

	public CommonConfigFactory createFactory() {
		return new RemoteConfigFactory(this);
	}

}
