/*******************************************************************************
 * Copyright (c) 2007, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM - Initial API and implementation
 *******************************************************************************/

package org.eclipse.ptp.internal.services.core.messages;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.ptp.internal.services.core.messages.messages";//$NON-NLS-1$

	public static String ServiceModelManager_0;
	public static String ServiceModelManager_2;
	public static String ServiceModelManager_3;
	public static String ServiceProvider_0;
	public static String ServiceProvider_1;
	public static String Services_invalidServiceId;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}

}
