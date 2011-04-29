/*******************************************************************************
 * Copyright (c) 2007, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ptp.rdt.managedbuilder.gnu.ui.preferences;


/**
 * Constant definitions for plug-in preferences
 */
public class PreferenceConstants {

	public static final String P_GCC_COMPILER_ROOT = "XL_compilerRoot"; //$NON-NLS-1$

	public static final String P_GCC_COMPILER_VERSION = "GCC_compilerVersion";  //$NON-NLS-1$
		
	public static final String P_GCC_COMPILER_VERSION_NAME = "Unknown";  //$NON-NLS-1$
	
	public static String getVersion (String label) {
		return P_GCC_COMPILER_VERSION_NAME;
	}
	
	public static String getVersionLabel (String version) {
		return P_GCC_COMPILER_VERSION_NAME;
	}

}
