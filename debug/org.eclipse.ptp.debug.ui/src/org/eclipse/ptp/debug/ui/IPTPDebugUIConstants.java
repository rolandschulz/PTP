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
package org.eclipse.ptp.debug.ui;

/**
 * @author Clement chu
 *
 */
public interface IPTPDebugUIConstants {
	public static final String PLUGIN_ID = PTPDebugUIPlugin.getUniqueIdentifier();
	public static final String PREFIX = PLUGIN_ID + ".";
	
	public static final String ID_PERSPECTIVE_DEBUG = PREFIX + "PTPDebugPerspective";
	public static final String ID_VIEW_PARALLELDEBUG = PREFIX + "views.parallelDebugView";
	public static final String ID_VIEW_ARRAY = PREFIX + "views.ArrayView";
	public static final String ID_VIEW_SIGNAL = PREFIX + "views.SignalsView";
	public static final String ID_BREAKPOINT_ORGANIZER = PREFIX + "pBreakpointSetOrganizer";
	
	public static final String ACTION_BREAKPOINT_PROPERTIES = PREFIX + "breakpointProperties";
	public static final String ACTION_ENABLE_DISABLE_BREAKPOINT = PREFIX + "enableDisableBreakpoint";
	public static final String ACTION_SET_BREAKPOINT = PREFIX + "toggleBreakpointRulerAction";
		
	public static final String REG_ANN_INSTR_POINTER_CURRENT = PREFIX + "regCurrentIP";
	public static final String REG_ANN_INSTR_POINTER_SECONDARY = PREFIX + "regSecondaryIP";
	public static final String CURSET_ANN_INSTR_POINTER_CURRENT = PREFIX + "currentSetIP";
	public static final String SET_ANN_INSTR_POINTER_CURRENT = PREFIX + "setIP";
	
    public static final String IUITABEMPTYGROUP = "emptygroup";
    public static final String IUITABVARIABLEGROUP = "variablegroup";	
}
