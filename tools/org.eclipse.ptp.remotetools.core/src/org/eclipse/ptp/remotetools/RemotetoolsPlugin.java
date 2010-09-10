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
import org.eclipse.jsch.core.IJSchService;
import org.eclipse.ptp.remotetools.core.IRemoteConnection;
import org.eclipse.ptp.remotetools.internal.ssh.CipherTypes;
import org.eclipse.ptp.remotetools.internal.ssh.Connection;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

/**
 * The main plug-in class to be used in the desktop.
 */
public class RemotetoolsPlugin extends Plugin {

	// The shared instance.
	private static RemotetoolsPlugin plugin;
	// ServiceTracker for IJschService
	private ServiceTracker tracker;

	/**
	 * The constructor.
	 */
	public RemotetoolsPlugin() {
		plugin = this;
	}

	/**
	 * This method is called upon plug-in activation
	 */
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		tracker = new ServiceTracker(getBundle().getBundleContext(), IJSchService.class.getName(), null);
		tracker.open();
	}

	/**
	 * This method is called when the plug-in is stopped
	 */
	@Override
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
	 * Returns an instance of IJSchService from the OSGi Registry.
	 * 
	 * @return An instance of IJSchService, or <code>null</code> if no
	 *         IJschService service is available.
	 */
	public IJSchService getJSchService() {
		return (IJSchService) tracker.getService();
	}

	/**
	 * Returns an instance of {@link IRemoteConnection} using a ssh connection.
	 * 
	 * @since 3.0
	 */
	public static IRemoteConnection createSSHConnection() {
		return new Connection();
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

	public static Map<String, String> getCipherTypesMap() {
		return new HashMap<String, String>(CipherTypes.getCipherTypesMap());
	}
}
