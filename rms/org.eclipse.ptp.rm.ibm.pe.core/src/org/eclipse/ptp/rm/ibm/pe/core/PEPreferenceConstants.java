/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.ptp.rm.ibm.pe.core;

public interface PEPreferenceConstants
{
    // Values of constants used as preference keys should be prefixed with
    // 'PE_' to ensure no collisions with any other preference keys
    public static final String TRACE_NOTHING = "None";
    public static final String TRACE_FUNCTION = "Function";
    public static final String TRACE_DETAIL = "Detail";
    public static final String LOAD_LEVELER_OPTION = "PE_useLoadLeveler";
    public static final String TRACE_LEVEL = "PE_traceLevel";
    public static final String RUN_MINIPROXY = "PE_runMiniproxy";
    public static final String LOAD_LEVELER_MODE = "PE_LoadLevelerMode";
    public static final String LIBRARY_OVERRIDE = "PE_libraryOverride";
    public static final String NODE_MIN_POLL_INTERVAL = "PE_NodeMinPollInterval";
    public static final String NODE_MAX_POLL_INTERVAL = "PE_NodeMaxPollInterval";
    public static final String JOB_POLL_INTERVAL = "PE_JobPollInterval";
    public static final String OPTION_YES = "Y";
    public static final String OPTION_NO = "N";
}
