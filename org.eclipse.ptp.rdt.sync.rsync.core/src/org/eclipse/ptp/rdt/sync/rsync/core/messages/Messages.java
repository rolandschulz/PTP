/*******************************************************************************
 * Copyright (c) 2011 University of Tennessee and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     University of Tennessee (Roland Schulz) - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.rdt.sync.rsync.core.messages;

import org.eclipse.osgi.util.NLS;

@SuppressWarnings("javadoc")
public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.ptp.rdt.sync.git.core.messages.messages"; //$NON-NLS-1$
	public static String CommandRunner_0;
	public static String CR_CreateInstanceError;
	public static String ChangeConnectionError;
	public static String ChangeLocationError;
	public static String ChangeProjectError;
	public static String SyncErrorMessage;
	public static String SyncTaskName;
	public static String ServiceProvider_1;
	public static String ServiceProvider_2;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}