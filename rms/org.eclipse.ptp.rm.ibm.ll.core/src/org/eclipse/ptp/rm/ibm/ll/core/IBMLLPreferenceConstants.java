/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.ptp.rm.ibm.ll.core;

public interface IBMLLPreferenceConstants
{

    public static final String LL_YES = "Y"; //$NON-NLS-1$
    public static final String LL_NO = "N"; //$NON-NLS-1$
    
    public static final String PROXY_INFO_MESSAGE = "ProxyInfoMessage"; //$NON-NLS-1$
    public static final String PROXY_TRACE_MESSAGE = "ProxyTraceMessage"; //$NON-NLS-1$
    public static final String PROXY_WARNING_MESSAGE = "ProxyWarningMessage"; //$NON-NLS-1$
    public static final String PROXY_ERROR_MESSAGE = "ProxyErrorMessage"; //$NON-NLS-1$
    public static final String PROXY_FATAL_MESSAGE = "ProxyFatalMessage"; //$NON-NLS-1$
    public static final String PROXY_ARGS_MESSAGE = "ProxyArgsMessage"; //$NON-NLS-1$
    public static final String PROXY_DEBUG_LOOP="ProxyDebugLoop"; //$NON-NLS-1$
    public static final String PROXY_DEFAULT_MULTICLUSTER="ProxyLoadLevelerMulticlusterDefault"; //$NON-NLS-1$
    public static final String PROXY_FORCE_LOCAL="ProxyLoadLevelerMulticlusterForceLocal"; //$NON-NLS-1$
    public static final String PROXY_FORCE_MULTICLUSTER="ProxyLoadLevelerMulticlusterForceMulticluster"; //$NON-NLS-1$
    public static final String PROXY_WRITE_TEMPLATE_ALWAYS="ProxyLoadLevelerTemplateAlways"; //$NON-NLS-1$
    public static final String PROXY_WRITE_TEMPLATE_NEVER="ProxyLoadLevelerTemplateNever"; //$NON-NLS-1$
    public static final String PROXY_MIN_NODE_POLLING="ProxyMinimumNodePolling"; //$NON-NLS-1$
    public static final String PROXY_MAX_NODE_POLLING="ProxyMaximumNodePolling"; //$NON-NLS-1$
    public static final String PROXY_JOB_POLLING="ProxyJobPolling"; //$NON-NLS-1$
    
    public static final String GUI_INFO_MESSAGE = "GuiInfoMessage"; //$NON-NLS-1$
    public static final String GUI_TRACE_MESSAGE = "GuiTraceMessage"; //$NON-NLS-1$
    public static final String GUI_WARNING_MESSAGE = "GuiWarningMessage"; //$NON-NLS-1$
    public static final String GUI_ERROR_MESSAGE = "GuiErrorMessage"; //$NON-NLS-1$
    public static final String GUI_FATAL_MESSAGE = "GuiFatalMessage"; //$NON-NLS-1$
    public static final String GUI_ARGS_MESSAGE = "GuiArgsMessage"; //$NON-NLS-1$
}
