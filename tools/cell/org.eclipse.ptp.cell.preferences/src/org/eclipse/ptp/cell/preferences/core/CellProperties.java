/******************************************************************************
 * Copyright (c) 2008 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *****************************************************************************/
package org.eclipse.ptp.cell.preferences.core;

import org.eclipse.osgi.util.NLS;

/**
 * @author everton
 *
 */
public class CellProperties extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.ptp.cell.preferences.core.cell"; //$NON-NLS-1$

	public static String separator;
	public static String sysroot;
	public static String sysrootSubdir;
	public static String mainLib;
	public static String searchInPackage;
	public static String searchRootDirectory;
	public static String sysrootLabel;
	
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, CellProperties.class);
	}

	private CellProperties() {
		// Hidden constructor to prevent instances.
	}
}
