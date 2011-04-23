/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.rm.ibm.ll.core.rmsystem;

import org.eclipse.ptp.core.Preferences;
import org.eclipse.ptp.rm.core.rmsystem.AbstractRemoteResourceManagerConfiguration;
import org.eclipse.ptp.rm.ibm.ll.core.IBMLLCorePlugin;
import org.eclipse.ptp.rm.ibm.ll.core.IBMLLPreferenceConstants;
import org.eclipse.ptp.services.core.IServiceProvider;

/**
 * Service provider for LoadLeveler
 */
public class IBMLLResourceManagerConfiguration extends AbstractRemoteResourceManagerConfiguration implements
		IIBMLLResourceManagerConfiguration {
	private static final String TAG_LL_LIBPATH = "LL_LibraryPath"; //$NON-NLS-1$
	private static final String TAG_TRACE_OPTION = "LL_Trace"; //$NON-NLS-1$
	private static final String TAG_INFO_MESSAGE = "LL_ProxyInfoMessage"; //$NON-NLS-1$
	private static final String TAG_WARNING_MESSAGE = "LL_ProxyWarningMessage"; //$NON-NLS-1$
	private static final String TAG_ERROR_MESSAGE = "LL_ProxyErrorMessage"; //$NON-NLS-1$
	private static final String TAG_FATAL_MESSAGE = "LL_ProxyFatalMessage"; //$NON-NLS-1$
	private static final String TAG_ARGS_MESSAGE = "LL_ProxyArgsMessage"; //$NON-NLS-1$
	private static final String TAG_DEBUG_SUSPEND = "LL_DebugSuspend"; //$NON-NLS-1$
	private static final String TAG_GUI_INFO_MESSAGE = "LL_GuiInfoMessage"; //$NON-NLS-1$
	private static final String TAG_GUI_WARNING_MESSAGE = "LL_GuiWarningMessage"; //$NON-NLS-1$
	private static final String TAG_GUI_ERROR_MESSAGE = "LL_GuiErrorMessage"; //$NON-NLS-1$
	private static final String TAG_GUI_FATAL_MESSAGE = "LL_GuiFatalMessage"; //$NON-NLS-1$
	private static final String TAG_GUI_TRACE_MESSAGE = "LL_GuiTraceMessage"; //$NON-NLS-1$
	private static final String TAG_GUI_ARGS_MESSAGE = "LL_GuiArgsMessage"; //$NON-NLS-1$
	private static final String TAG_DEFAULT_MULTICLUSTER = "LL_DefaultMulticluster"; //$NON-NLS-1$
	private static final String TAG_FORCE_PROXY_LOCAL = "LL_ForceProxyLocal"; //$NON-NLS-1$
	private static final String TAG_FORCE_PROXY_MULTICLUSTER = "LL_ForceProxyMulticluster"; //$NON-NLS-1$
	private static final String TAG_SUPPRESS_TEMPLATE_WRITE = "LL_SuppressTemplateWrite"; //$NON-NLS-1$
	private static final String TAG_TEMPLATE_WRITE_ALWAYS = "LL_TemplateWriteAlways"; //$NON-NLS-1$
	private static final String TAG_TEMPLATE_FILE = "LL_TemplateFile"; //$NON-NLS-1$
	private static final String TAG_MIN_NODE_POLL = "LL_MinNodePollInterval"; //$NON-NLS-1$
	private static final String TAG_MAX_NODE_POLL = "LL_MNodePollInterval"; //$NON-NLS-1$
	private static final String TAG_JOB_POLL = "LL_JobPollInterval"; //$NON-NLS-1$

	public IBMLLResourceManagerConfiguration(String namespace, IServiceProvider provider) {
		super(namespace, provider);
		setDescription("IBM LL Resource Manager"); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.ibm.ll.ui.IIBMLLResourceManagerConfiguration#
	 * getArgsMessage()
	 */
	public String getArgsMessage() {
		return getString(TAG_ARGS_MESSAGE,
				Preferences.getString(IBMLLCorePlugin.getUniqueIdentifier(), IBMLLPreferenceConstants.PROXY_ARGS_MESSAGE));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rm.ibm.ll.ui.IIBMLLResourceManagerConfiguration#getDebugLoop
	 * ()
	 */
	public String getDebugLoop() {
		return getString(TAG_DEBUG_SUSPEND,
				Preferences.getString(IBMLLCorePlugin.getUniqueIdentifier(), IBMLLPreferenceConstants.PROXY_DEBUG_LOOP));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.ibm.ll.ui.IIBMLLResourceManagerConfiguration#
	 * getDefaultMulticluster()
	 */
	public String getDefaultMulticluster() {
		return getString(TAG_DEFAULT_MULTICLUSTER,
				Preferences.getString(IBMLLCorePlugin.getUniqueIdentifier(), IBMLLPreferenceConstants.PROXY_DEFAULT_MULTICLUSTER));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.ibm.ll.ui.IIBMLLResourceManagerConfiguration#
	 * getErrorMessage()
	 */
	public String getErrorMessage() {
		return getString(TAG_ERROR_MESSAGE,
				Preferences.getString(IBMLLCorePlugin.getUniqueIdentifier(), IBMLLPreferenceConstants.PROXY_ERROR_MESSAGE));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.ibm.ll.ui.IIBMLLResourceManagerConfiguration#
	 * getFatalMessage()
	 */
	public String getFatalMessage() {
		return getString(TAG_FATAL_MESSAGE,
				Preferences.getString(IBMLLCorePlugin.getUniqueIdentifier(), IBMLLPreferenceConstants.PROXY_FATAL_MESSAGE));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.ibm.ll.ui.IIBMLLResourceManagerConfiguration#
	 * getForceProxyLocal()
	 */
	public String getForceProxyLocal() {
		return getString(TAG_FORCE_PROXY_LOCAL,
				Preferences.getString(IBMLLCorePlugin.getUniqueIdentifier(), IBMLLPreferenceConstants.PROXY_FORCE_LOCAL));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.ibm.ll.ui.IIBMLLResourceManagerConfiguration#
	 * getForceProxyMulticluster()
	 */
	public String getForceProxyMulticluster() {
		return getString(TAG_FORCE_PROXY_MULTICLUSTER,
				Preferences.getString(IBMLLCorePlugin.getUniqueIdentifier(), IBMLLPreferenceConstants.PROXY_FORCE_MULTICLUSTER));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.ibm.ll.ui.IIBMLLResourceManagerConfiguration#
	 * getGuiArgsMessage()
	 */
	public String getGuiArgsMessage() {
		return getString(TAG_GUI_ARGS_MESSAGE,
				Preferences.getString(IBMLLCorePlugin.getUniqueIdentifier(), IBMLLPreferenceConstants.GUI_ARGS_MESSAGE));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.ibm.ll.ui.IIBMLLResourceManagerConfiguration#
	 * getGuiErrorMessage()
	 */
	public String getGuiErrorMessage() {
		return getString(TAG_GUI_ERROR_MESSAGE,
				Preferences.getString(IBMLLCorePlugin.getUniqueIdentifier(), IBMLLPreferenceConstants.GUI_ERROR_MESSAGE));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.ibm.ll.ui.IIBMLLResourceManagerConfiguration#
	 * getGuiFatalMessage()
	 */
	public String getGuiFatalMessage() {
		return getString(TAG_GUI_FATAL_MESSAGE,
				Preferences.getString(IBMLLCorePlugin.getUniqueIdentifier(), IBMLLPreferenceConstants.GUI_FATAL_MESSAGE));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.ibm.ll.ui.IIBMLLResourceManagerConfiguration#
	 * getGuiInfoMessage()
	 */
	public String getGuiInfoMessage() {
		return getString(TAG_GUI_INFO_MESSAGE,
				Preferences.getString(IBMLLCorePlugin.getUniqueIdentifier(), IBMLLPreferenceConstants.GUI_INFO_MESSAGE));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.ibm.ll.ui.IIBMLLResourceManagerConfiguration#
	 * getGuiTraceMessage()
	 */
	public String getGuiTraceMessage() {
		return getString(TAG_GUI_TRACE_MESSAGE,
				Preferences.getString(IBMLLCorePlugin.getUniqueIdentifier(), IBMLLPreferenceConstants.GUI_TRACE_MESSAGE));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.ibm.ll.ui.IIBMLLResourceManagerConfiguration#
	 * getGuiWarningMessage()
	 */
	public String getGuiWarningMessage() {
		return getString(TAG_GUI_WARNING_MESSAGE,
				Preferences.getString(IBMLLCorePlugin.getUniqueIdentifier(), IBMLLPreferenceConstants.GUI_WARNING_MESSAGE));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.ibm.ll.ui.IIBMLLResourceManagerConfiguration#
	 * getInfoMessage()
	 */
	public String getInfoMessage() {
		return getString(TAG_INFO_MESSAGE,
				Preferences.getString(IBMLLCorePlugin.getUniqueIdentifier(), IBMLLPreferenceConstants.PROXY_INFO_MESSAGE));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rm.ibm.ll.ui.IIBMLLResourceManagerConfiguration#getJobPolling
	 * ()
	 */
	public int getJobPolling() {
		return getInt(TAG_JOB_POLL,
				Preferences.getInt(IBMLLCorePlugin.getUniqueIdentifier(), IBMLLPreferenceConstants.PROXY_JOB_POLLING));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.ibm.ll.ui.IIBMLLResourceManagerConfiguration#
	 * getLibraryPath()
	 */
	public String getLibraryPath() {
		return "";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.ibm.ll.ui.IIBMLLResourceManagerConfiguration#
	 * getMaxNodePolling()
	 */
	public int getMaxNodePolling() {
		return getInt(TAG_MAX_NODE_POLL,
				Preferences.getInt(IBMLLCorePlugin.getUniqueIdentifier(), IBMLLPreferenceConstants.PROXY_MAX_NODE_POLLING));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.ibm.ll.ui.IIBMLLResourceManagerConfiguration#
	 * getMinNodePolling()
	 */
	public int getMinNodePolling() {
		return getInt(TAG_MIN_NODE_POLL,
				Preferences.getInt(IBMLLCorePlugin.getUniqueIdentifier(), IBMLLPreferenceConstants.PROXY_MIN_NODE_POLLING));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.ibm.ll.ui.IIBMLLResourceManagerConfiguration#
	 * getSuppressTemplateWrite()
	 */
	public String getSuppressTemplateWrite() {
		return getString(TAG_SUPPRESS_TEMPLATE_WRITE,
				Preferences.getString(IBMLLCorePlugin.getUniqueIdentifier(), IBMLLPreferenceConstants.PROXY_WRITE_TEMPLATE_NEVER));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.ibm.ll.ui.IIBMLLResourceManagerConfiguration#
	 * getTemplateFile()
	 */
	public String getTemplateFile() {
		return "";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.ibm.ll.ui.IIBMLLResourceManagerConfiguration#
	 * getTemplateWriteAlways()
	 */
	public String getTemplateWriteAlways() {
		return getString(TAG_TEMPLATE_WRITE_ALWAYS,
				Preferences.getString(IBMLLCorePlugin.getUniqueIdentifier(), IBMLLPreferenceConstants.PROXY_WRITE_TEMPLATE_ALWAYS));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.ibm.ll.ui.IIBMLLResourceManagerConfiguration#
	 * getTraceOption()
	 */
	public String getTraceOption() {
		return getString(TAG_TRACE_OPTION,
				Preferences.getString(IBMLLCorePlugin.getUniqueIdentifier(), IBMLLPreferenceConstants.PROXY_TRACE_MESSAGE));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.ibm.ll.ui.IIBMLLResourceManagerConfiguration#
	 * getWarningMessage()
	 */
	public String getWarningMessage() {
		return getString(TAG_WARNING_MESSAGE,
				Preferences.getString(IBMLLCorePlugin.getUniqueIdentifier(), IBMLLPreferenceConstants.PROXY_WARNING_MESSAGE));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.services.core.IServiceProvider#isConfigured()
	 */
	@Override
	public boolean isConfigured() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.ibm.ll.ui.IIBMLLResourceManagerConfiguration#
	 * setArgsMessage(java.lang.String)
	 */
	public void setArgsMessage(String option) {
		putString(TAG_ARGS_MESSAGE, option);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rm.ibm.ll.ui.IIBMLLResourceManagerConfiguration#setDebugLoop
	 * (java.lang.String)
	 */
	public void setDebugLoop(String option) {
		putString(TAG_DEBUG_SUSPEND, option);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.ibm.ll.ui.IIBMLLResourceManagerConfiguration#
	 * setDefaultMulticluster(java.lang.String)
	 */
	public void setDefaultMulticluster(String option) {
		putString(TAG_DEFAULT_MULTICLUSTER, option);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rmsystem.IResourceManagerConfiguration#setDefaultNameAndDesc
	 * ()
	 */
	public void setDefaultNameAndDesc() {
		String name = "IBMLL"; //$NON-NLS-1$
		String conn = getConnectionName();
		if (conn != null && !conn.equals("")) { //$NON-NLS-1$
			name += "@" + conn; //$NON-NLS-1$
		}
		setName(name);
		setDescription("IBMLL Resource Manager"); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.ibm.ll.ui.IIBMLLResourceManagerConfiguration#
	 * setErrorMessage(java.lang.String)
	 */
	public void setErrorMessage(String option) {
		putString(TAG_ERROR_MESSAGE, option);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.ibm.ll.ui.IIBMLLResourceManagerConfiguration#
	 * setFatalMessage(java.lang.String)
	 */
	public void setFatalMessage(String option) {
		putString(TAG_FATAL_MESSAGE, option);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.ibm.ll.ui.IIBMLLResourceManagerConfiguration#
	 * setForceProxyLocal(java.lang.String)
	 */
	public void setForceProxyLocal(String option) {
		putString(TAG_FORCE_PROXY_LOCAL, option);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.ibm.ll.ui.IIBMLLResourceManagerConfiguration#
	 * setForceProxyMulticluster(java.lang.String)
	 */
	public void setForceProxyMulticluster(String option) {
		putString(TAG_FORCE_PROXY_MULTICLUSTER, option);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.ibm.ll.ui.IIBMLLResourceManagerConfiguration#
	 * setGuiArgsMessage(java.lang.String)
	 */
	public void setGuiArgsMessage(String option) {
		putString(TAG_GUI_ARGS_MESSAGE, option);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.ibm.ll.ui.IIBMLLResourceManagerConfiguration#
	 * setGuiErrorMessage(java.lang.String)
	 */
	public void setGuiErrorMessage(String option) {
		putString(TAG_GUI_ERROR_MESSAGE, option);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.ibm.ll.ui.IIBMLLResourceManagerConfiguration#
	 * setGuiFatalMessage(java.lang.String)
	 */
	public void setGuiFatalMessage(String option) {
		putString(TAG_GUI_FATAL_MESSAGE, option);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.ibm.ll.ui.IIBMLLResourceManagerConfiguration#
	 * setGuiInfoMessage(java.lang.String)
	 */
	public void setGuiInfoMessage(String option) {
		putString(TAG_GUI_INFO_MESSAGE, option);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.ibm.ll.ui.IIBMLLResourceManagerConfiguration#
	 * setGuiTraceMessage(java.lang.String)
	 */
	public void setGuiTraceMessage(String option) {
		putString(TAG_GUI_TRACE_MESSAGE, option);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.ibm.ll.ui.IIBMLLResourceManagerConfiguration#
	 * setGuiWarningMessage(java.lang.String)
	 */
	public void setGuiWarningMessage(String option) {
		putString(TAG_GUI_WARNING_MESSAGE, option);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.ibm.ll.ui.IIBMLLResourceManagerConfiguration#
	 * setInfoMessage(java.lang.String)
	 */
	public void setInfoMessage(String option) {
		putString(TAG_INFO_MESSAGE, option);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rm.ibm.ll.ui.IIBMLLResourceManagerConfiguration#setJobPolling
	 * (int)
	 */
	public void setJobPolling(int value) {
		putInt(TAG_JOB_POLL, value);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.ibm.ll.ui.IIBMLLResourceManagerConfiguration#
	 * setLibraryPath(java.lang.String)
	 */
	public void setLibraryPath(String path) {
		putString(TAG_LL_LIBPATH, path);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.ibm.ll.ui.IIBMLLResourceManagerConfiguration#
	 * setMaxNodePolling(int)
	 */
	public void setMaxNodePolling(int value) {
		putInt(TAG_MAX_NODE_POLL, value);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.ibm.ll.ui.IIBMLLResourceManagerConfiguration#
	 * setMinNodePolling(int)
	 */
	public void setMinNodePolling(int value) {
		putInt(TAG_MIN_NODE_POLL, value);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.ibm.ll.ui.IIBMLLResourceManagerConfiguration#
	 * setSuppressTemplateWrite(java.lang.String)
	 */
	public void setSuppressTemplateWrite(String option) {
		putString(TAG_SUPPRESS_TEMPLATE_WRITE, option);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.ibm.ll.ui.IIBMLLResourceManagerConfiguration#
	 * setTemplateFile(java.lang.String)
	 */
	public void setTemplateFile(String file) {
		putString(TAG_TEMPLATE_FILE, file);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.ibm.ll.ui.IIBMLLResourceManagerConfiguration#
	 * setTemplateWriteAlways(java.lang.String)
	 */
	public void setTemplateWriteAlways(String option) {
		putString(TAG_TEMPLATE_WRITE_ALWAYS, option);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.ibm.ll.ui.IIBMLLResourceManagerConfiguration#
	 * setTraceOption(java.lang.String)
	 */
	public void setTraceOption(String option) {
		putString(TAG_TRACE_OPTION, option);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.ibm.ll.ui.IIBMLLResourceManagerConfiguration#
	 * setWarningMessage(java.lang.String)
	 */
	public void setWarningMessage(String option) {
		putString(TAG_WARNING_MESSAGE, option);
	}
}
