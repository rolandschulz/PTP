/*******************************************************************************
 * Copyright (c) 2005 The Regents of the University of California. 
 * This material was produced under U.S. Government contract W-7405-ENG-36 
 * for Los Alamos National Laboratory, which is operated by the University 
 * of California for the U.S. Department of Energy. The U.S. Government has 
 * rights to use, reproduce, and distribute this software. NEITHER THE 
 * GOVERNMENT NOR THE UNIVERSITY MAKES ANY WARRANTY, EXPRESS OR IMPLIED, OR 
 * ASSUMES ANY LIABILITY FOR THE USE OF THIS SOFTWARE. If software is modified 
 * to produce derivative works, such modified software should be clearly marked, 
 * so as not to confuse it with the version available from LANL.
 * 
 * Additionally, this program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * LA-CC 04-115
 *******************************************************************************/
/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.debug.sdm.core;

/**
 * Constant definitions for PTP debug plug-in.
 */
public interface SDMPreferenceConstants {
	public static final String PLUGIN_ID = SDMDebugCorePlugin.getUniqueIdentifier();

	/**
	 * Location of the sdm
	 */
	//public static final String SDM_DEBUGGER_FILE = PLUGIN_ID + ".debugger_file";
	
	/**
	 * Extra arguments to pass to the sdm (e.g. debugging flags)
	 */
	public static final String SDM_DEBUGGER_ARGS = PLUGIN_ID + ".debugger_args";

	/**
	 * Host for the sdm client
	 */
	//public static final String SDM_DEBUGGER_HOST = PLUGIN_ID + ".debugger_host";
	//public static final String SDM_DEFAULT_DEUBGGER_HOST = "localhost";

	/**
	 * Debugger backend
	 */
	public static final String SDM_DEBUGGER_BACKEND_TYPE = PLUGIN_ID + ".debugger_backend";
	public static final int SDM_DEFAULT_DEDUGGER_BACKEND_INDEX = 0;
	public static final String[] SDM_DEBUGGER_BACKENDS = new String[] {
		"gdb-mi"
	};
	
	/**
	 * Path to backend debugger
	 */
	public static final String SDM_DEBUGGER_BACKEND_PATH = PLUGIN_ID + ".debugger_backend_path";
	public static final String SDM_DEFAULT_DEDUGGER_BACKEND_PATH = "";
}
