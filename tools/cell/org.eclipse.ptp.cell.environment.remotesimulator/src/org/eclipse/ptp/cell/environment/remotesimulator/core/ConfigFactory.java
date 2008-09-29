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
package org.eclipse.ptp.cell.environment.remotesimulator.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ptp.cell.environment.remotesimulator.Activator;
import org.eclipse.ptp.remotetools.utils.verification.AttributeVerification;
import org.eclipse.ptp.remotetools.utils.verification.ControlAttributes;
import org.eclipse.ptp.remotetools.utils.verification.IllegalAttributeException;

/**
 * Defines rules to build the target configuration from an attribute hash map.
 * 
 * @author Daniel Felix Ferber
 * @since 1.2.0
 */
public class ConfigFactory {

	public static final String ATTR_REMOTE_LOGIN_USERNAME = "remote-login-username"; //$NON-NLS-1$
	public static final String ATTR_REMOTE_LOGIN_PASSWORD = "remote-login-password"; //$NON-NLS-1$
	public static final String ATTR_REMOTE_CONNECTION_ADDRESS = "remote-connection-address"; //$NON-NLS-1$
	public static final String ATTR_REMOTE_CONNECTION_PORT = "remote-connection-port"; //$NON-NLS-1$
	public static final String ATTR_REMOTE_KEY_PATH = "remote-key-path"; //$NON-NLS-1$
	public static final String ATTR_REMOTE_KEY_PASSPHRASE = "remote-key-passphrase"; //$NON-NLS-1$
	public static final String ATTR_REMOTE_IS_PASSWORD_AUTH = "remote-is-password-auth"; //$NON-NLS-1$
	public static final String ATTR_REMOTE_CONNECTION_TIMEOUT = "remote-timeout"; //$NON-NLS-1$
	public static final String ATTR_REMOTE_CIPHER_TYPE = "remote-cipher-type"; //$NON-NLS-1$
	public static final String ATTR_SIMULATOR_IS_AUTOMATIC_CONFIG = "simulator-is-automatic-config"; //$NON-NLS-1$
	public static final String ATTR_SIMULATOR_IS_PASSWORD_AUTH = "remote-is-password-auth"; //$NON-NLS-1$
	public static final String ATTR_SIMULATOR_LOGIN_USERNAME = "simulator-login-username"; //$NON-NLS-1$
	public static final String ATTR_SIMULATOR_LOGIN_PASSWORD = "simulator-login-password"; //$NON-NLS-1$
	public static final String ATTR_SIMULATOR_KEY_PATH = "simulator-key-path"; //$NON-NLS-1$
	public static final String ATTR_SIMULATOR_KEY_PASSPHRASE = "simulator-key-passphrase"; //$NON-NLS-1$
	public static final String ATTR_SIMULATOR_CONNECTION_ADDRESS = "simulator-connection-address"; //$NON-NLS-1$
	public static final String ATTR_SIMULATOR_CONNECTION_PORT = "simulator-connection-port"; //$NON-NLS-1$
	public static final String ATTR_SIMULATOR_CONNECTION_TIMEOUT = "simulator-connection-timeout"; //$NON-NLS-1$
	public static final String ATTR_SIMULATOR_CIPHER_TYPE = "simulator-cipher-type"; //$NON-NLS-1$
	
	// THe attribute corresponding to this attribute key won't be loaded here, but its key will be kept here because it's easy to mantain.
	public static final String ATTR_SYSTEM_WORKSPACE = "system-workspace-dir"; //$NON-NLS-1$
	
	public static final String NAME_REMOTE_LOGIN_USERNAME = "Username for remote host"; //$NON-NLS-1$
	public static final String NAME_REMOTE_LOGIN_PASSWORD = "Password for remote host"; //$NON-NLS-1$
	public static final String NAME_REMOTE_CONNECTION_ADDRESS = "Remote host address"; //$NON-NLS-1$
	public static final String NAME_REMOTE_CONNECTION_PORT = "Remote host port"; //$NON-NLS-1$
	public static final String NAME_REMOTE_CONNECTION_TIMEOUT = "Remote host connection timeout"; //$NON-NLS-1$
	public static final String NAME_REMOTE_CIPHER_TYPE = "Remote host connection cipher type"; //$NON-NLS-1$
	public static final String NAME_SIMULATOR_LOGIN_USERNAME = "Simulator login username"; //$NON-NLS-1$
	public static final String NAME_SIMULATOR_LOGIN_PASSWORD = "Simulator login password"; //$NON-NLS-1$
	public static final String NAME_SIMULATOR_CONNECTION_ADDRESS = "Simulator address on remote host"; //$NON-NLS-1$
	public static final String NAME_SIMULATOR_CONNECTION_PORT = "Simulator port on remote host"; //$NON-NLS-1$
	public static final String NAME_SIMULATOR_CONNECTION_TIMEOUT = "Simulator connection timeout"; //$NON-NLS-1$
	public static final String NAME_SIMULATOR_CIPHER_TYPE = "Simulator connection cipher type"; //$NON-NLS-1$
	public static final String NAME_SYSTEM_WORKSPACE = "Launch base directory"; //$NON-NLS-1$

	public static final String SHORT_NAME_REMOTE_LOGIN_USERNAME = "Username"; //$NON-NLS-1$
	public static final String SHORT_NAME_REMOTE_LOGIN_PASSWORD = "Password"; //$NON-NLS-1$
	public static final String SHORT_NAME_REMOTE_CONNECTION_ADDRESS = "Host address"; //$NON-NLS-1$
	public static final String SHORT_NAME_REMOTE_CONNECTION_PORT = "Port"; //$NON-NLS-1$
	public static final String SHORT_NAME_SIMULATOR_LOGIN_USERNAME = "Username"; //$NON-NLS-1$
	public static final String SHORT_NAME_SIMULATOR_LOGIN_PASSWORD = "Password"; //$NON-NLS-1$
	public static final String SHORT_NAME_SIMULATOR_CONNECTION_ADDRESS = "Simulator address"; //$NON-NLS-1$
	public static final String SHORT_NAME_SIMULATOR_CONNECTION_PORT = "Port"; //$NON-NLS-1$
	public static final String SHORT_NAME_SIMULATOR_CONNECTION_TIMEOUT = "Timeout (seconds)"; //$NON-NLS-1$
	public static final String SHORT_NAME_SYSTEM_WORKSPACE = "Launch base directory"; //$NON-NLS-1$

	public static final String DEFAULT_REMOTE_LOGIN_USERNAME = DefaultValues.DEFAULT_REMOTE_LOGIN_USERNAME;
	public static final String DEFAULT_REMOTE_LOGIN_PASSWORD = DefaultValues.DEFAULT_REMOTE_LOGIN_PASSWORD;
	public static final String DEFAULT_REMOTE_CONNECTION_ADDRESS = DefaultValues.DEFAULT_REMOTE_CONNECTION_ADDRESS;
	public static final int DEFAULT_REMOTE_CONNECTION_PORT = Integer.parseInt(DefaultValues.DEFAULT_REMOTE_CONNECTION_PORT);
	public static final String DEFAULT_REMOTE_KEY_PATH = DefaultValues.DEFAULT_REMOTE_KEY_PATH;
	public static final String DEFAULT_REMOTE_KEY_PASSPHRASE = DefaultValues.DEFAULT_REMOTE_KEY_PASSPHRASE;
	public static final boolean DEFAULT_REMOTE_IS_PASSWORD_AUTH = Boolean.valueOf(DefaultValues.DEFAULT_REMOTE_IS_PASSWORD_AUTH).booleanValue();
	public static final int DEFAULT_REMOTE_CONNECTION_TIMEOUT = Integer.parseInt(DefaultValues.DEFAULT_REMOTE_TIMEOUT);
	public static final String DEFAULT_REMOTE_CIPHER_TYPE = TargetControl.DEFAULT_HOST_CIPHER;
	public static final boolean DEFAULT_SIMULATOR_IS_AUTOMATIC_CONFIG = Boolean.valueOf(DefaultValues.DEFAULT_SIMULATOR_IS_AUTOMATIC_CONFIG).booleanValue();
	public static final boolean DEFAULT_SIMULATOR_IS_PASSWORD_AUTH = Boolean.valueOf(DefaultValues.DEFAULT_SIMULATOR_IS_PASSWORD_AUTH).booleanValue();
	public static final String DEFAULT_SIMULATOR_LOGIN_USERNAME = DefaultValues.DEFAULT_SIMULATOR_LOGIN_USERNAME;
	public static final String DEFAULT_SIMULATOR_LOGIN_PASSWORD = DefaultValues.DEFAULT_SIMULATOR_LOGIN_PASSWORD;
	public static final String DEFAULT_SIMULATOR_KEY_PATH = DefaultValues.DEFAULT_SIMULATOR_KEY_PATH;
	public static final String DEFAULT_SIMULATOR_KEY_PASSPHRASE = DefaultValues.DEFAULT_SIMULATOR_KEY_PASSPHRASE;
	public static final String DEFAULT_SIMULATOR_CONNECTION_ADDRESS = DefaultValues.DEFAULT_SIMULATOR_CONNECTION_ADDRESS;
	public static final int DEFAULT_SIMULATOR_CONNECTION_PORT = Integer.parseInt(DefaultValues.DEFAULT_SIMULATOR_CONNECTION_PORT);
	public static final int DEFAULT_SIMULATOR_CONNECTION_TIMEOUT = Integer.parseInt(DefaultValues.DEFAULT_SIMULATOR_CONNECTION_TIMEOUT);
	public static final String DEFAULT_SIMULATOR_CIPHER_TYPE = TargetControl.DEFAULT_SIMULATOR_CIPHER;
	public static final String DEFAULT_SYSTEM_WORKSPACE = DefaultValues.DEFAULT_SYSTEM_WORKSPACE;
	
	public final static String [] KEY_ARRAY = { 
			ATTR_REMOTE_LOGIN_USERNAME,
			ATTR_REMOTE_CONNECTION_PORT,
			ATTR_REMOTE_CONNECTION_ADDRESS,
			ATTR_REMOTE_KEY_PATH,
			ATTR_REMOTE_IS_PASSWORD_AUTH,
			ATTR_REMOTE_CONNECTION_TIMEOUT,
			ATTR_REMOTE_CIPHER_TYPE,
			ATTR_SIMULATOR_IS_AUTOMATIC_CONFIG,
			ATTR_SIMULATOR_LOGIN_USERNAME,
			ATTR_SIMULATOR_CONNECTION_PORT,
			ATTR_SIMULATOR_CONNECTION_ADDRESS,
			ATTR_SIMULATOR_CONNECTION_TIMEOUT,
			ATTR_SIMULATOR_CIPHER_TYPE,
			ATTR_SIMULATOR_KEY_PATH,
			ATTR_SYSTEM_WORKSPACE,
			/*ATTR_TUNNEL_PORT_MIN,
			ATTR_TUNNEL_PORT_MAX*/
		};
	
	public final static String [] KEY_CIPHERED_ARRAY = {
			ATTR_REMOTE_LOGIN_PASSWORD,			
			ATTR_REMOTE_KEY_PASSPHRASE,
			ATTR_SIMULATOR_LOGIN_PASSWORD,
			ATTR_SIMULATOR_KEY_PASSPHRASE
		};

	private ControlAttributes attributes;
	
	public ConfigFactory(ControlAttributes attributes) {
		this.attributes = attributes;
	}
		
	public IStatus fillTargetConfig(TargetConfig config) {
		ArrayList errors = new ArrayList();
		AttributeVerification verification = new AttributeVerification(Activator.PLUGIN_ID);

		config.setRemoteConnectionAddress(attributes.getStringAttribute(ATTR_REMOTE_CONNECTION_ADDRESS, DEFAULT_REMOTE_CONNECTION_ADDRESS));
		try {
			config.setRemoteConnectionPort(attributes.verifyIntAttribute(NAME_REMOTE_CONNECTION_PORT, ATTR_REMOTE_CONNECTION_PORT, DEFAULT_REMOTE_CONNECTION_PORT));
			config.setRemoteConnectionTimeout(attributes.verifyIntAttribute(NAME_REMOTE_CONNECTION_TIMEOUT, ATTR_REMOTE_CONNECTION_TIMEOUT, DEFAULT_REMOTE_CONNECTION_TIMEOUT));
		} catch (IllegalAttributeException e) {
			errors.add(new Status(Status.ERROR, Activator.getDefault().getBundle().getSymbolicName(),0, e.getMessage(), e));
		}
		
		config.setRemoteIsPasswordAuth(attributes.getBooleanAttribute(ATTR_REMOTE_IS_PASSWORD_AUTH, DEFAULT_REMOTE_IS_PASSWORD_AUTH));
		
		//if()
		config.setRemoteLoginUserName(attributes.getStringAttribute(ATTR_REMOTE_LOGIN_USERNAME, DEFAULT_REMOTE_LOGIN_USERNAME));
		config.setRemoteLoginPassword(attributes.getStringAttribute(ATTR_REMOTE_LOGIN_PASSWORD, DEFAULT_REMOTE_LOGIN_PASSWORD));
		
		config.setRemoteKeyPath(attributes.getStringAttribute(ATTR_REMOTE_KEY_PATH, DEFAULT_REMOTE_KEY_PATH));
		config.setRemoteKeyPassphrase(attributes.getStringAttribute(ATTR_REMOTE_KEY_PASSPHRASE, DEFAULT_REMOTE_KEY_PASSPHRASE));
		config.setRemoteCipherType(attributes.getString(ATTR_REMOTE_CIPHER_TYPE, DEFAULT_REMOTE_CIPHER_TYPE));
		
		/*config.setRemoteKeyPath(attributes.getStringAttribute(ATTR_REMOTE_KEY_PATH, DEFAULT_REMOTE_KEY_PATH));
		config.setRemoteKeyPassphrase(attributes.getStringAttribute(ATTR_REMOTE_KEY_PASSPHRASE, DEFAULT_REMOTE_KEY_PASSPHRASE));*/
		
		boolean isAutomatic = attributes.getBooleanAttribute(ATTR_SIMULATOR_IS_AUTOMATIC_CONFIG, DEFAULT_SIMULATOR_IS_AUTOMATIC_CONFIG);
		
		if(isAutomatic) {
			config.setSimulatorLoginUserName(Parameters.AUTOMATIC_USERNAME);
			config.setSimulatorLoginPassword(Parameters.AUTOMATIC_PASSWORD);
			config.setSimulatorConnectionAddress(Parameters.AUTOMATIC_IP_SIMULATOR);
			config.setSimulatorIsPasswordAuth(true);
			
			config.setSimulatorConnectionPort(Parameters.getNUMERIC_PORT_SIMULATOR());
			config.setSimulatorConnectionTimeout(Parameters.getNUMERIC_TIMEOUT());
			config.setSimulatorCipherType(DEFAULT_SIMULATOR_CIPHER_TYPE);
			
		} else {
			config.setSimulatorIsPasswordAuth(attributes.getBooleanAttribute(ATTR_SIMULATOR_IS_PASSWORD_AUTH, DEFAULT_SIMULATOR_IS_PASSWORD_AUTH));
			config.setSimulatorLoginUserName(attributes.getStringAttribute(ATTR_SIMULATOR_LOGIN_USERNAME, DEFAULT_SIMULATOR_LOGIN_USERNAME));
			config.setSimulatorLoginPassword(attributes.getStringAttribute(ATTR_SIMULATOR_LOGIN_PASSWORD, DEFAULT_SIMULATOR_LOGIN_PASSWORD));
			config.setSimulatorKeyPath(attributes.getStringAttribute(ATTR_SIMULATOR_KEY_PATH, DEFAULT_SIMULATOR_KEY_PATH));
			config.setSimulatorPassphrase(attributes.getStringAttribute(ATTR_SIMULATOR_KEY_PASSPHRASE, DEFAULT_SIMULATOR_KEY_PASSPHRASE));
			
			config.setSimulatorConnectionAddress(attributes.getStringAttribute(ATTR_SIMULATOR_CONNECTION_ADDRESS, DEFAULT_SIMULATOR_CONNECTION_ADDRESS));
			config.setSimulatorCipherType(attributes.getString(ATTR_SIMULATOR_CIPHER_TYPE, DEFAULT_SIMULATOR_CIPHER_TYPE));
			try {
				config.setSimulatorConnectionPort(attributes.verifyIntAttribute(NAME_SIMULATOR_CONNECTION_PORT, ATTR_SIMULATOR_CONNECTION_PORT, DEFAULT_SIMULATOR_CONNECTION_PORT));
				config.setSimulatorConnectionTimeout(attributes.verifyIntAttribute(NAME_SIMULATOR_CONNECTION_TIMEOUT, ATTR_SIMULATOR_CONNECTION_TIMEOUT, DEFAULT_SIMULATOR_CONNECTION_TIMEOUT));
			} catch (IllegalAttributeException e) {
				errors.add(new Status(Status.ERROR, Activator.getDefault().getBundle().getSymbolicName(),0, e.getMessage(), e));
			}
		}
		
		config.setSystemWorkspace(attributes.getStringAttribute(ATTR_SYSTEM_WORKSPACE, DEFAULT_SYSTEM_WORKSPACE));
		return verification.createResultStatus(errors);
	}
	
	public void createTargetConfig(TargetConfig config) throws CoreException {
		IStatus status = fillTargetConfig(config);
		if (! status.isOK()) {
			throw new CoreException(status);
		}
	}
	
	public TargetConfig createTargetConfig() throws CoreException {
		TargetConfig config = new TargetConfig();
		IStatus status = fillTargetConfig(config);
		if (! status.isOK()) {
			throw new CoreException(status);
		}
		return config;
	}

	public IStatus checkTargetConfig() {
		TargetConfig config = new TargetConfig();
		return fillTargetConfig(config);
	}
	
	public static Map createDefaultConfig() {
		Map attributes = new HashMap();
		
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		attributes.put(ATTR_REMOTE_IS_PASSWORD_AUTH, Boolean.toString(DEFAULT_REMOTE_IS_PASSWORD_AUTH));
		attributes.put(ATTR_REMOTE_LOGIN_USERNAME, DEFAULT_REMOTE_LOGIN_USERNAME);
		attributes.put(ATTR_REMOTE_LOGIN_PASSWORD, DEFAULT_REMOTE_LOGIN_PASSWORD);
		attributes.put(ATTR_REMOTE_KEY_PATH, DEFAULT_REMOTE_KEY_PATH);
		attributes.put(ATTR_REMOTE_KEY_PASSPHRASE, DEFAULT_REMOTE_KEY_PASSPHRASE);
		attributes.put(ATTR_REMOTE_CONNECTION_ADDRESS, DEFAULT_REMOTE_CONNECTION_ADDRESS);
		attributes.put(ATTR_REMOTE_CONNECTION_PORT, Integer.toString(DEFAULT_REMOTE_CONNECTION_PORT));
		attributes.put(ATTR_REMOTE_CIPHER_TYPE, DEFAULT_REMOTE_CIPHER_TYPE);

		attributes.put(ATTR_SIMULATOR_IS_AUTOMATIC_CONFIG, Boolean.toString(DEFAULT_SIMULATOR_IS_AUTOMATIC_CONFIG));
		attributes.put(ATTR_SIMULATOR_LOGIN_USERNAME, DEFAULT_SIMULATOR_LOGIN_USERNAME);
		attributes.put(ATTR_SIMULATOR_LOGIN_PASSWORD, DEFAULT_SIMULATOR_LOGIN_PASSWORD);
		attributes.put(ATTR_SIMULATOR_CONNECTION_ADDRESS, DEFAULT_SIMULATOR_CONNECTION_ADDRESS);
		attributes.put(ATTR_SIMULATOR_CONNECTION_PORT, Integer.toString(DEFAULT_SIMULATOR_CONNECTION_PORT));
		attributes.put(ATTR_SIMULATOR_CONNECTION_TIMEOUT, Integer.toString(DEFAULT_SIMULATOR_CONNECTION_TIMEOUT));
		attributes.put(ATTR_SIMULATOR_CIPHER_TYPE, DEFAULT_SIMULATOR_CIPHER_TYPE);

		attributes.put(ATTR_SYSTEM_WORKSPACE, DEFAULT_SYSTEM_WORKSPACE);
		return attributes;
	}
	
}
