/*******************************************************************************
 * Copyright (c) 2008, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.rdt.sync.core.messages;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

	private static final String BUNDLE_NAME = "org.eclipse.ptp.rdt.sync.core.messages.messages"; //$NON-NLS-1$

	public static String RemoteCIndexServiceProvider_0;
	public static String RemoteRunSiProvider_taskName;
	public static String PathEntryValidationListener_jobName;
	public static String ResourceRefreshJob_0;
	public static String ResourceRefreshJob_1;

	static {
		initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
