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
package org.eclipse.ptp.debug.internal.ui.preferences;

import org.eclipse.ptp.debug.ui.IPTPDebugUIConstants;


/**
 * @author Clement chu
 *
 */
public interface IPDebugPreferenceConstants {
	public static final String PREF_SHOW_FULL_PATHS = IPTPDebugUIConstants.PLUGIN_ID + ".pDebug.show_full_paths";
	//TODO should be declared in debug.core 
	public static final String PREF_PTP_DEBUGGER = IPTPDebugUIConstants.PLUGIN_ID + ".pDebug.debuggers";
	public static final String PREF_PTP_DEBUG_COMM_TIMEOUT = IPTPDebugUIConstants.PLUGIN_ID + ".pDebug.timeout";
	public static final String PREF_PTP_DEBUG_EVENT_TIME = IPTPDebugUIConstants.PLUGIN_ID + ".pDebug.eventTime";

	public static final String PREF_PTP_DEBUG_REGISTER_PROC_0 = IPTPDebugUIConstants.PLUGIN_ID + ".pDebug.regPro0";
	
	public static final int DEFAULT_DEBUG_TIMEOUT = 0;
	public static final int DEFAULT_DEBUG_EVENTTIME = 0;
}
