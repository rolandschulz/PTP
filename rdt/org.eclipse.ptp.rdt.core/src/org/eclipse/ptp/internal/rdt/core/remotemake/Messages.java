/*******************************************************************************
 * Copyright (c) 2010,2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/

package org.eclipse.ptp.internal.rdt.core.remotemake;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.ptp.internal.rdt.core.remotemake.messages"; //$NON-NLS-1$
	public static String RemoteProcessClosure_0;
	public static String RemoteProcessClosure_1;
	public static String ResourceRefreshJob_0;
	public static String ResourceRefreshJob_1;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
