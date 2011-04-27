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
    public static final String TRACE_NOTHING = "None"; //$NON-NLS-1$
    public static final String TRACE_FUNCTION = "Function"; //$NON-NLS-1$
    public static final String TRACE_DETAIL = "Detail"; //$NON-NLS-1$
    public static final String LOAD_LEVELER_OPTION = "PE_useLoadLeveler"; //$NON-NLS-1$
    public static final String TRACE_LEVEL = "PE_traceLevel"; //$NON-NLS-1$
    public static final String LOAD_LEVELER_MODE = "PE_LoadLevelerMode"; //$NON-NLS-1$
    public static final String LIBRARY_OVERRIDE = "PE_libraryOverride"; //$NON-NLS-1$
    public static final String NODE_MIN_POLL_INTERVAL = "PE_NodeMinPollInterval"; //$NON-NLS-1$
    public static final String NODE_MAX_POLL_INTERVAL = "PE_NodeMaxPollInterval"; //$NON-NLS-1$
    public static final String JOB_POLL_INTERVAL = "PE_JobPollInterval"; //$NON-NLS-1$
    public static final String OPTION_YES = "Y"; //$NON-NLS-1$
    public static final String OPTION_NO = "N"; //$NON-NLS-1$
}
