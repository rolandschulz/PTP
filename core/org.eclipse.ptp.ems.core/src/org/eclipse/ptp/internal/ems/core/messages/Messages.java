/*******************************************************************************
 * Copyright (c) 2012 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 *     Jeff Overbey (Illinois/NCSA) - Design and implementation
 ******************************************************************************/
package org.eclipse.ptp.internal.ems.core.messages;

import org.eclipse.osgi.util.NLS;

@SuppressWarnings("javadoc")
public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.ptp.internal.ems.core.messages.messages"; //$NON-NLS-1$

	public static String ModulesEnvManager_SelectModulesToBeLoaded;
	public static String NullEnvManager_NoSupportedEMSFound;
	public static String SoftEnvEnvManager_SelectSoftEnvCommands;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
