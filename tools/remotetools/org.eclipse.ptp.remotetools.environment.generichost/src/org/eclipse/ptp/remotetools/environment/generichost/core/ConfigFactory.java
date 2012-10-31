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
package org.eclipse.ptp.remotetools.environment.generichost.core;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ptp.remotetools.RemotetoolsPlugin;
import org.eclipse.ptp.remotetools.environment.generichost.Activator;
import org.eclipse.ptp.remotetools.environment.generichost.conf.AttributeNames;
import org.eclipse.ptp.remotetools.environment.generichost.conf.DefaultValues;
import org.eclipse.ptp.remotetools.utils.verification.ControlAttributes;
import org.eclipse.ptp.remotetools.utils.verification.IllegalAttributeException;

/**
 * Defines rules to build the target configuration from an attribute hash map.
 * 
 * @author Daniel Felix Ferber
 * @since 1.4
 */
public class ConfigFactory {
	private ControlAttributes attributes = null;

	private static final String PREFIX = "org.eclipse.ptp.remotetools.environment.generichost"; //$NON-NLS-1$
	public static final String ATTR_LOCALHOST_SELECTION = PREFIX + ".localhost-selection"; //$NON-NLS-1$
	public static final String ATTR_LOGIN_USERNAME = PREFIX + ".login-username"; //$NON-NLS-1$
	public static final String ATTR_LOGIN_PASSWORD = PREFIX + ".login-password"; //$NON-NLS-1$
	public static final String ATTR_CONNECTION_ADDRESS = PREFIX + ".connection-address"; //$NON-NLS-1$
	public static final String ATTR_CONNECTION_PORT = PREFIX + ".connection-port"; //$NON-NLS-1$
	public static final String ATTR_KEY_PATH = PREFIX + ".key-path"; //$NON-NLS-1$
	public static final String ATTR_KEY_PASSPHRASE = PREFIX + ".key-passphrase"; //$NON-NLS-1$
	public static final String ATTR_IS_PASSWORD_AUTH = PREFIX + ".is-passwd-auth"; //$NON-NLS-1$
	public static final String ATTR_CONNECTION_TIMEOUT = PREFIX + ".connection-timeout"; //$NON-NLS-1$
	public static final String ATTR_CIPHER_TYPE = PREFIX + ".cipher-type"; //$NON-NLS-1$

	private static final String ATTR_USE_LOGIN_SHELL = PREFIX + ".use-login-shell"; //$NON-NLS-1$

	public final static String[] KEY_ARRAY = { ATTR_LOCALHOST_SELECTION, ATTR_LOGIN_USERNAME, ATTR_CONNECTION_PORT,
			ATTR_CONNECTION_ADDRESS, ATTR_KEY_PATH, ATTR_IS_PASSWORD_AUTH, ATTR_CONNECTION_TIMEOUT, ATTR_CIPHER_TYPE,
			ATTR_USE_LOGIN_SHELL };

	public final static String[] KEY_CIPHERED_ARRAY = { ATTR_KEY_PASSPHRASE, ATTR_LOGIN_PASSWORD };

	public ConfigFactory() {
		this(null);
	}

	/**
	 * @since 5.0
	 */
	public ConfigFactory(ControlAttributes attrs) {
		if (attrs != null) {
			attributes = attrs;
		} else {
			attributes = new ControlAttributes();
		}
		createDefaultMap();
		if (attrs == null) {
			createCurrentMapFromPreferences();
		}
	}

	public ControlAttributes getAttributes() {
		return attributes;
	}

	private void createDefaultMap() {
		attributes.setDefaultString(ATTR_LOCALHOST_SELECTION, DefaultValues.LOCALHOST_SELECTION);
		attributes.setDefaultString(ATTR_LOGIN_USERNAME, DefaultValues.LOGIN_USERNAME);
		attributes.setDefaultString(ATTR_LOGIN_PASSWORD, DefaultValues.LOGIN_PASSWORD);
		attributes.setDefaultString(ATTR_CONNECTION_PORT, DefaultValues.CONNECTION_PORT);
		attributes.setDefaultString(ATTR_CONNECTION_ADDRESS, DefaultValues.CONNECTION_ADDRESS);
		attributes.setDefaultString(ATTR_KEY_PATH, DefaultValues.KEY_PATH);
		attributes.setDefaultString(ATTR_KEY_PASSPHRASE, DefaultValues.KEY_PASSPHRASE);
		attributes.setDefaultString(ATTR_IS_PASSWORD_AUTH, DefaultValues.IS_PASSWORD_AUTH);
		attributes.setDefaultString(ATTR_CONNECTION_TIMEOUT, DefaultValues.CONNECTION_TIMEOUT);
		attributes.setDefaultString(ATTR_CIPHER_TYPE, RemotetoolsPlugin.CIPHER_DEFAULT);
		attributes.setDefaultString(ATTR_USE_LOGIN_SHELL, DefaultValues.USE_LOGIN_SHELL);
	}

	private void createCurrentMapFromPreferences() {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		attributes.setString(ATTR_LOGIN_USERNAME, store.getString(ATTR_LOGIN_USERNAME));
		attributes.setString(ATTR_CONNECTION_ADDRESS, store.getString(ATTR_CONNECTION_ADDRESS));
		attributes.setString(ATTR_CONNECTION_PORT, store.getString(ATTR_CONNECTION_PORT));
	}

	public TargetConfig createTargetConfig() throws CoreException {
		try {
			attributes.verifyInt(AttributeNames.CONNECTION_PORT, ATTR_CONNECTION_PORT);
			attributes.verifyInt(AttributeNames.CONNECTION_TIMEOUT, ATTR_CONNECTION_TIMEOUT);
			if (attributes.getBoolean(ATTR_LOCALHOST_SELECTION)) {
				attributes.setString(ATTR_CONNECTION_ADDRESS, "localhost"); //$NON-NLS-1$
			}
			return new TargetConfig(attributes);
		} catch (IllegalAttributeException e) {
			throw new CoreException(new Status(Status.ERROR, Activator.getDefault().getBundle().getSymbolicName(), 0,
					e.getMessage(), e));
		}
	}
}
