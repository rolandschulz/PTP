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
package org.eclipse.ptp.cell.managedbuilder.xl.core.preferences;

import org.eclipse.osgi.util.NLS;

/**
 * 
 * @author laggarcia
 * @since 3.0.0
 */
public class XlToolsProperties extends NLS {
	
	private static final String BUNDLE_NAME = "org.eclipse.ptp.cell.managedbuilder.xl.core.preferences.xlTools"; //$NON-NLS-1$

	public static String xlToolsPathLabel;

	public static String xlToolsPath;

	public static String xlToolsPathDefaultValue;
	
	public static String separator;
	
	public static String xlTools;
	
	public static String ppuxlcExecutable;
	
	public static String ppuxlcPackage;
	
	public static String ppuxlcSearchRootDirectory;
	
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, XlToolsProperties.class);
	}

	private XlToolsProperties() {
		// Hidden constructor to prevent instances.
	}
}
