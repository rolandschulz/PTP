/******************************************************************************
 * Copyright (c) 2007 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *****************************************************************************/
package org.eclipse.ptp.cell.utils.linux.packagemanager.rpm;

import org.eclipse.osgi.util.NLS;

/**
 * 
 * @author laggarcia
 * @since 3.0.0
 */
public class RPMPackageManagerMessages extends NLS {

	private static final String BUNDLE_NAME = "org.eclipse.ptp.cell.utils.linux.packagemanager.rpm.messages"; //$NON-NLS-1$

	// The next two strings must be translated to the same text provided in the
	// translated rpm package installed in the system
	public static String rpmIsNotInstalledErrorMessage;
	
	public static String rpmIsNotInstalledErrorMessagePattern;

	public static String RPMPackageManager_FailedExcecution;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, RPMPackageManagerMessages.class);
	}

	private RPMPackageManagerMessages() {
		// Private constructor to prevent instances of this class.
	}
}
