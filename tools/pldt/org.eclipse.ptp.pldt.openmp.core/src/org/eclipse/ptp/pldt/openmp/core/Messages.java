/**********************************************************************
 * Copyright (c) 2009,2010 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.pldt.openmp.core;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.ptp.pldt.openmp.core.messages"; //$NON-NLS-1$
	public static String OpenMPCASTVisitor_OPENMP_CALL;
	public static String OpenMPCASTVisitor_OPENMP_CONSTANT;
	public static String OpenMPIDs_OPENMP_INCLUDES;
	public static String OpenMPPlugin_OPENMP_INCLUDES;
	public static String OpenMPPreferencePage_OPENMP_INCLUDE_PATHS;
	public static String OpenMPPreferencePage_OPENMP_INCLUDES_LABEL;
	public static String RunAnalyseOpenMPcommandHandler_OPENMP_DIRECTIVE;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
