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
package org.eclipse.ptp.remotetools;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.Plugin;
import org.eclipse.ptp.remotetools.core.AuthToken;
import org.eclipse.ptp.remotetools.core.IRemoteConnection;
import org.eclipse.ptp.remotetools.internal.ssh.CipherTypes;
import org.eclipse.ptp.remotetools.internal.ssh.Connection;
import org.osgi.framework.BundleContext;


/**
 * The main plug-in class to be used in the desktop.
 */
public class RemotetoolsPlugin extends Plugin {

	//The shared instance.
	private static RemotetoolsPlugin plugin;
	
	/**
	 * The constructor.
	 */
	public RemotetoolsPlugin() {
		plugin = this;
	}

	/**
	 * This method is called upon plug-in activation
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
	}

	/**
	 * This method is called when the plug-in is stopped
	 */
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
		plugin = null;
	}

	/**
	 * Returns the shared instance.
	 */
	public static RemotetoolsPlugin getDefault() {
		return plugin;
	}
	
	
	/**
	 * Returns an instance of {@link IRemoteConnection} using a ssh connection.
	 */
	public static IRemoteConnection createSSHConnection(AuthToken authToken, String hostname,
			int port, String cipherType, int timeout)
	{		
		return new Connection(authToken, hostname, port, cipherType, timeout);
	}

	/**
	 * Returns an instance of {@link IRemoteConnection} using a ssh connection.
	 */
	public static IRemoteConnection createSSHConnection(AuthToken authToken, String hostname,
			int port, String cipherType)
	{		
		return new Connection(authToken, hostname, port, cipherType);
	}
	
	/**
	 * Returns an instance of {@link IRemoteConnection} using a ssh connection.
	 */
	public static IRemoteConnection createSSHConnection(AuthToken authToken, String hostname)
	{		
		return new Connection(authToken, hostname);
	}

	public static IRemoteConnection createSSHConnection(AuthToken authToken, String hostname, int port) {
		return new Connection(authToken, hostname, port);
	}
	
	/**
	 * 3DES cipher type
	 */
	public static final String CIPHER_3DES = org.eclipse.ptp.remotetools.internal.ssh.CipherTypes.CIPHER_3DES;
	/**
	 * AES128 cipher type
	 */
	public static final String CIPHER_AES128 = org.eclipse.ptp.remotetools.internal.ssh.CipherTypes.CIPHER_AES128;
	/**
	 * AES192 cipher type
	 */
	public static final String CIPHER_AES192 = org.eclipse.ptp.remotetools.internal.ssh.CipherTypes.CIPHER_AES192;
	/**
	 * AES256 cipher type
	 */
	public static final String CIPHER_AES256 = org.eclipse.ptp.remotetools.internal.ssh.CipherTypes.CIPHER_AES256;
	/**
	 * Blowfish cipher type
	 */
	public static final String CIPHER_BLOWFISH = org.eclipse.ptp.remotetools.internal.ssh.CipherTypes.CIPHER_BLOWFISH;
	/**
	 * Default cipher type
	 */
	public static final String CIPHER_DEFAULT = org.eclipse.ptp.remotetools.internal.ssh.CipherTypes.CIPHER_DEFAULT;
	
	public static Map getCipherTypesMap() {
		HashMap map = new HashMap(CipherTypes.getCipherTypesMap());
		
		return map;
	}
	
	/**
	 * Returns an instance of {@link IRemoteConnection} using a ssh connection.
	 *//*
	public static IRemoteConnection createSSHConnection(String username, String password, String hostname,
			int port, int timeout)
	{		
		return new Connection(username, password, hostname, port, timeout);
	}

	*//**
	 * Returns an instance of {@link IRemoteConnection} using a ssh connection.
	 *//*
	public static IRemoteConnection createSSHConnection(String username, String password, String hostname)
	{		
		return new Connection(username, password, hostname);
	}

	public static IRemoteConnection createSSHConnection(String username, String password, String hostname, int port) {
		return new Connection(username, password, hostname, port);
	}*/
}
