/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.cell.utils.platform;

import org.eclipse.osgi.util.NLS;

public class IdentityInformation extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.ptp.cell.utils.platform.identity"; //$NON-NLS-1$

	public static String Fedora;

	public static String Fedora7;
	
	public static String Fedora8;
	
	public static String Fedora9;

	public static String FedoraCore6;

	public static String RedHatEnterpriseLinux5;
	
	public static String RedHatEnterpriseLinux5U1;
	
	public static String RedHatEnterpriseLinux5U2;

	public static String RHEL;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, IdentityInformation.class);
	}

	private IdentityInformation() {
		// Private constructor to prevent instances of this class.
	}
}
