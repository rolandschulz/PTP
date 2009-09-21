/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.debug.core.messages;

import org.eclipse.osgi.util.NLS;

/**
 * @author greg
 *
 */
public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.ptp.debug.core.messages.messages"; //$NON-NLS-1$
	
	public static String PDebugConfiguration_1;
	public static String PSession_0;
	public static String PSession_1;
	public static String PSession_2;
	public static String PSession_3;
	public static String PSession_4;
	public static String PSession_5;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
