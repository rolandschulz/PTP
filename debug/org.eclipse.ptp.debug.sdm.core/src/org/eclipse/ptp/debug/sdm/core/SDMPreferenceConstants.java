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
	 * Extra arguments to pass to the sdm (e.g. debugging flags)
	 */
	public static final String SDM_DEBUGGER_ARGS = PLUGIN_ID + ".debugger_args"; //$NON-NLS-1$

	/**
	 * SDM debugging
	 */
	public static final String SDM_DEBUG_ENABLED = PLUGIN_ID + ".debug_enabled"; //$NON-NLS-1$
	public static final String SDM_DEBUG_LEVEL = PLUGIN_ID + ".debug_level"; //$NON-NLS-1$

	public static final int DEBUG_LEVEL_NONE = 0x00;
	public static final int DEBUG_LEVEL_STARTUP = 0x01;
	public static final int DEBUG_LEVEL_MESSAGES = 0x02;
	public static final int DEBUG_LEVEL_ROUTING = 0x04;
	/**
	 * @since 4.0
	 */
	public static final int DEBUG_LEVEL_MASTER = 0x08;
	public static final int DEBUG_LEVEL_SERVER = 0x10;
	public static final int DEBUG_LEVEL_BACKEND = 0x20;
	public static final int DEBUG_LEVEL_PROTOCOL = 0x40;

	/**
	 * @since 4.0
	 */
	public static final String SDM_DEBUG_CLIENT_ENABLED = PLUGIN_ID + ".debug_client_enabled"; //$NON-NLS-1$
	/**
	 * @since 4.0
	 */
	public static final String SDM_DEBUG_CLIENT_LEVEL = PLUGIN_ID + ".debug_client_level"; //$NON-NLS-1$

	/**
	 * @since 4.0
	 */
	public static final int DEBUG_CLIENT_NONE = 0x00;
	/**
	 * @since 4.0
	 */
	public static final int DEBUG_CLIENT_TRACING = 0x01;
	/**
	 * @since 4.0
	 */
	public static final int DEBUG_CLIENT_TRACING_MORE = 0x02;
	/**
	 * @since 4.0
	 */
	public static final int DEBUG_CLIENT_OUTPUT = 0x04;

	/**
	 * Debugger backend
	 */
	public static final String SDM_DEBUGGER_BACKEND_TYPE = PLUGIN_ID + ".debugger_backend"; //$NON-NLS-1$
	public static final int SDM_DEFAULT_DEDUGGER_BACKEND_INDEX = 0;
	public static final String[] SDM_DEBUGGER_BACKENDS = new String[] { "gdb-mi" //$NON-NLS-1$
	};

	/**
	 * Path to backend debugger
	 */
	public static final String SDM_DEBUGGER_BACKEND_PATH = PLUGIN_ID + ".debugger_backend_path"; //$NON-NLS-1$
	public static final String SDM_DEFAULT_DEDUGGER_BACKEND_PATH = ""; //$NON-NLS-1$
}
