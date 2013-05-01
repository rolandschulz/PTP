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
package org.eclipse.ptp.internal.debug.ui;

/**
 * @author Clement chu
 * 
 */
public interface IPTPDebugUIConstants {
	public static final String PLUGIN_ID = PTPDebugUIPlugin.getUniqueIdentifier();
	public static final String PREFIX = PLUGIN_ID + "."; //$NON-NLS-1$

	public static final String ID_PERSPECTIVE_DEBUG = PREFIX + "PTPDebugPerspective"; //$NON-NLS-1$
	public static final String ID_VIEW_PARALLELDEBUG = PREFIX + "views.parallelDebugView"; //$NON-NLS-1$
	public static final String ID_VIEW_ARRAY = PREFIX + "views.ArrayView"; //$NON-NLS-1$
	public static final String ID_VIEW_SIGNAL = PREFIX + "views.SignalsView"; //$NON-NLS-1$
	public static final String ID_VIEW_VARIABLE = PREFIX + "views.PVariableView"; //$NON-NLS-1$
	public static final String ID_BREAKPOINT_ORGANIZER = PREFIX + "pBreakpointSetOrganizer"; //$NON-NLS-1$

	public static final String ACTION_BREAKPOINT_PROPERTIES = PREFIX + "breakpointProperties"; //$NON-NLS-1$
	public static final String ACTION_ENABLE_DISABLE_BREAKPOINT = PREFIX + "enableDisableBreakpoint"; //$NON-NLS-1$
	public static final String ACTION_SET_BREAKPOINT = PREFIX + "toggleBreakpointRulerAction"; //$NON-NLS-1$

	public static final String REG_ANN_INSTR_POINTER_CURRENT = PREFIX + "regCurrentIP"; //$NON-NLS-1$
	public static final String REG_ANN_INSTR_POINTER_SECONDARY = PREFIX + "regSecondaryIP"; //$NON-NLS-1$
	public static final String CURSET_ANN_INSTR_POINTER_CURRENT = PREFIX + "currentSetIP"; //$NON-NLS-1$
	public static final String SET_ANN_INSTR_POINTER_CURRENT = PREFIX + "setIP"; //$NON-NLS-1$

	public static final String THREAD_GROUP = "threadGroup"; //$NON-NLS-1$
	public static final String STEP_GROUP = "stepGroup"; //$NON-NLS-1$
	public static final String STEP_INTO_GROUP = "stepIntoGroup"; //$NON-NLS-1$
	public static final String STEP_OVER_GROUP = "stepOverGroup"; //$NON-NLS-1$
	public static final String STEP_RETURN_GROUP = "stepReturnGroup"; //$NON-NLS-1$
	public static final String EMPTY_STEP_GROUP = "emptyStepGroup"; //$NON-NLS-1$
	public static final String REG_GROUP = "regGroup"; //$NON-NLS-1$

	public static final String VAR_GROUP = "VarGroup"; //$NON-NLS-1$

	public static final String IUITABEMPTYGROUP = "emptygroup"; //$NON-NLS-1$
	public static final String IUITABVARIABLEGROUP = "variablegroup"; //$NON-NLS-1$
}
