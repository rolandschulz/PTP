/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.internal.rdt.core;

public class RemoteUtil {

	private static Boolean remote;
	
	/**
	 * Returns true if the code is running on the server, false
	 * if the code is running on the client.
	 */
	public static boolean isRemote() {
		if(remote == null) {
			// Platform.isRunning() does not seem to work, it throws a NoClassDefFoundError on the server
			// If the cdtminer is in the classpath then we must be running on the server.
			// This might not be the best way of doing this.
			// DO NOT rename the cdtminer.jar file as RDT will stop working.
			String classpath = System.getProperty("java.class.path"); //$NON-NLS-1$
			remote = classpath.contains("cdtminer.jar") || classpath.contains("rdt-server"); //$NON-NLS-1$ //$NON-NLS-2$
			System.out.println("classpath=" + classpath); //$NON-NLS-1$
		}
		return remote;
	}
	
	public static boolean isClient() {
		return !isRemote();
	}
}
