/******************************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *****************************************************************************/
package org.eclipse.ptp.cell.managedbuilder.internal.core;

import org.eclipse.osgi.util.NLS;

/**
 * @author laggarcia
 * @since 1.1.0
 */
public class ManagedMakeMessages extends NLS {

	private static final String BUNDLE_ID = "org.eclipse.ptp.cell.managedbuilder.internal.core.PluginResources"; //$NON-NLS-1$

	public static String copyFile;

	public static String customMakefileIncludes;

	public static String makefileIncludes;

	public static String topMakefileError;

	public static String fileDoesntExist;
	
	static {
		// load message values from bundle file
		NLS.initializeMessages(BUNDLE_ID, ManagedMakeMessages.class);
	}

	private ManagedMakeMessages() {
		// cannot create new instance
	}
}
