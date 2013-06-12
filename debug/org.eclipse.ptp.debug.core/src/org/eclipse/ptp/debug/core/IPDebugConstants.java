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
package org.eclipse.ptp.debug.core;

import org.eclipse.ptp.internal.debug.core.PTPDebugCorePlugin;

/**
 * Constant definitions for PTP debug implementations.
 */
public interface IPDebugConstants {
	/**
	 * Status handler codes.
	 */
	public static final int STATUS_CODE_QUESTION = 10000;
	public static final int STATUS_CODE_INFO = 10001;
	public static final int STATUS_CODE_ERROR = 10002;

	/**
	 * String preference for the common source containers.
	 */
	public static final String PREF_COMMON_SOURCE_CONTAINERS = PTPDebugCorePlugin.getUniqueIdentifier()
			+ ".pDebug.common_source_containers"; //$NON-NLS-1$

	/**
	 * C/C++ debug plug-in identifier (value <code>"org.eclipse.ptp.debug.core"</code>).
	 */
	public static final String PLUGIN_ID = PTPDebugCorePlugin.getUniqueIdentifier();

	/**
	 * The identifier of the default variable format to use in the variables view
	 */
	public static final String PREF_DEFAULT_VARIABLE_FORMAT = PLUGIN_ID + "pDebug.default_variable_format"; //$NON-NLS-1$

	/**
	 * The identifier of the default expression format to use in the expressions views
	 */
	public static final String PREF_DEFAULT_EXPRESSION_FORMAT = PLUGIN_ID + "pDebug.default_expression_format"; //$NON-NLS-1$

	/**
	 * The identifier of the common source locations list
	 */
	public static final String PREF_SOURCE_LOCATIONS = PLUGIN_ID + "pDebug.Source.source_locations"; //$NON-NLS-1$

	/**
	 * Preference for displaying full paths in breakpoints
	 */
	public static final String PREF_SHOW_FULL_PATHS = PLUGIN_ID + ".pDebug.show_full_paths"; //$NON-NLS-1$

	/**
	 * Preference setting for debugger communication timeout
	 * 
	 * @since 5.0
	 */
	public static final String PREF_DEBUG_COMM_TIMEOUT = PLUGIN_ID + ".pDebug.timeout"; //$NON-NLS-1$

	/**
	 * Preference setting for registering process 0 by default
	 * 
	 * @since 5.0
	 */
	public static final String PREF_DEBUG_REGISTER_PROC_0 = PLUGIN_ID + ".pDebug.regPro0"; //$NON-NLS-1$

	/**
	 * Preference setting for the default debugger type
	 */
	public static final String PREF_DEFAULT_DEBUGGER_TYPE = PLUGIN_ID + ".pDebug.defaultDebugger"; //$NON-NLS-1$

	/**
	 * Preference setting to update variables on suspend
	 */
	public static final String PREF_UPDATE_VARIABLES_ON_SUSPEND = PLUGIN_ID + ".pDebug.update_variables_on_suspend"; //$NON-NLS-1$

	/**
	 * Preference setting to update variables on change
	 */
	public static final String PREF_UPDATE_VARIABLES_ON_CHANGE = PLUGIN_ID + ".pDebug.update_variables_on_change"; //$NON-NLS-1$

	/**
	 * Preference setting for instruction stepping mode (not used)
	 */
	public static final String PREF_INSTRUCTION_STEP_MODE_ON = PLUGIN_ID + "pDebug.Disassembly.instructionStepOn"; //$NON-NLS-1$

	/**
	 * Preference setting for the default register format (not used)
	 */
	public static final String PREF_DEFAULT_REGISTER_FORMAT = PLUGIN_ID + "pDebug.default_register_format"; //$NON-NLS-1$

	/**
	 * Default instruction stepping mode
	 * 
	 * @since 5.0
	 */
	public static final boolean DEFAULT_INSTRUCTION_STEP_MODE = false;

	/**
	 * Default show full paths setting
	 * 
	 * @since 5.0
	 */
	public static final boolean DEFAULT_SHOW_FULL_PATHS = false;

	/**
	 * Default register proc 0 setting
	 * 
	 * @since 5.0
	 */
	public static final boolean DEFAULT_DEBUG_REGISTER_PROC_0 = true;

	/**
	 * Default update variable on suspend setting
	 * 
	 * @since 5.0
	 */
	public static final boolean DEFAULT_UPDATE_VARIABLES_ON_SUSPEND = true;

	/**
	 * Default update variable on change setting
	 * 
	 * @since 5.0
	 */
	public static final boolean DEFAULT_UPDATE_VARIABLES_ON_CHANGE = false;

	/**
	 * Default communication timeout
	 */
	public static final int DEFAULT_DEBUG_COMM_TIMEOUT = 60000;

	/**
	 * Minimum request timeout
	 */
	public static final int MIN_REQUEST_TIMEOUT = 10000;

	/**
	 * Maximum request timeout
	 */
	public static final int MAX_REQUEST_TIMEOUT = Integer.MAX_VALUE;
}
