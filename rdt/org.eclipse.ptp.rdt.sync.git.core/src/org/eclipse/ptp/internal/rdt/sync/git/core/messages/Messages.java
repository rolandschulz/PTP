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
package org.eclipse.ptp.internal.rdt.sync.git.core.messages;

import org.eclipse.osgi.util.NLS;

@SuppressWarnings("javadoc")
public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.ptp.internal.rdt.sync.git.core.messages.messages"; //$NON-NLS-1$
	public static String GitSyncFileFilter_UnableToLoad;
	public static String GitSyncFileFilter_UnableToSave;
	public static String CommandRunner_0;
	public static String CommandRunner_1;
	public static String CommandRunner_3;
	public static String CommandRunner_4;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
