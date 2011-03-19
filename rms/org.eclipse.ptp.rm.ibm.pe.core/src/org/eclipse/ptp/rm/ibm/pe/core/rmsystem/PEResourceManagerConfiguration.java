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
package org.eclipse.ptp.rm.ibm.pe.core.rmsystem;

import org.eclipse.ptp.core.Preferences;
import org.eclipse.ptp.rm.core.rmsystem.AbstractRemoteResourceManagerConfiguration;
import org.eclipse.ptp.rm.ibm.pe.core.PECorePlugin;
import org.eclipse.ptp.rm.ibm.pe.core.PEPreferenceConstants;
import org.eclipse.ptp.services.core.IServiceProvider;

/**
 * Service provider for IBM Parallel Environment
 */
public class PEResourceManagerConfiguration extends AbstractRemoteResourceManagerConfiguration {
	private static final String TAG_USE_LOADLEVELER = "PE_UseLoadLeveler"; //$NON-NLS-1$
	private static final String TAG_DEBUG_LEVEL = "PE_DebugLevel"; //$NON-NLS-1$
	private static final String TAG_RUN_MINIPROXY = "PE_RunMiniproxy"; //$NON-NLS-1$
	private static final String TAG_SUSPEND_PROXY = "PE_SuspendProxy"; //$NON-NLS-1$
	private static final String TAG_LOADLEVELER_MODE = "PE_LoadLevelerMode"; //$NON-NLS-1$
	private static final String TAG_MIN_NODE_POLL_INTERVAL = "PE_NodeMinPollInterval"; //$NON-NLS-1$
	private static final String TAG_MAX_NODE_POLL_INTERVAL = "PE_NodeMaxPollInterval"; //$NON-NLS-1$
	private static final String TAG_JOB_POLL_INTERVAL = "PE_JobPollInterval"; //$NON-NLS-1$
	private static final String TAG_LIBRARY_OVERRIDE = "PE_LibraryOverride"; //$NON-NLS-1$

	public PEResourceManagerConfiguration() {
	}

	public PEResourceManagerConfiguration(String namespace, IServiceProvider provider) {
		super(namespace, provider);
		setDescription("IBM PE Resource Manager"); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rm.ibm.pe.core.rmsystem.IPEResourceManagerConfiguration
	 * #getDebugLevel()
	 */
	public String getDebugLevel() {
		return getString(TAG_DEBUG_LEVEL,
				Preferences.getString(PECorePlugin.getUniqueIdentifier(), PEPreferenceConstants.TRACE_LEVEL));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rm.ibm.pe.core.rmsystem.IPEResourceManagerConfiguration
	 * #getJobPollInterval()
	 */
	public String getJobPollInterval() {
		return getString(TAG_JOB_POLL_INTERVAL,
				Preferences.getString(PECorePlugin.getUniqueIdentifier(), PEPreferenceConstants.JOB_POLL_INTERVAL));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rm.ibm.pe.core.rmsystem.IPEResourceManagerConfiguration
	 * #getLibraryOverride()
	 */
	public String getLibraryOverride() {
		return getString(TAG_LIBRARY_OVERRIDE,
				Preferences.getString(PECorePlugin.getUniqueIdentifier(), PEPreferenceConstants.LIBRARY_OVERRIDE));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rm.ibm.pe.core.rmsystem.IPEResourceManagerConfiguration
	 * #getLoadLevelerMode()
	 */
	public String getLoadLevelerMode() {
		return getString(TAG_LOADLEVELER_MODE,
				Preferences.getString(PECorePlugin.getUniqueIdentifier(), PEPreferenceConstants.LOAD_LEVELER_MODE));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rm.ibm.pe.core.rmsystem.IPEResourceManagerConfiguration
	 * #getNodeMaxPollInterval()
	 */
	public String getNodeMaxPollInterval() {
		return getString(TAG_MAX_NODE_POLL_INTERVAL,
				Preferences.getString(PECorePlugin.getUniqueIdentifier(), PEPreferenceConstants.NODE_MAX_POLL_INTERVAL));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rm.ibm.pe.core.rmsystem.IPEResourceManagerConfiguration
	 * #getNodeMinPollInterval()
	 */
	public String getNodeMinPollInterval() {
		return getString(TAG_MIN_NODE_POLL_INTERVAL,
				Preferences.getString(PECorePlugin.getUniqueIdentifier(), PEPreferenceConstants.NODE_MIN_POLL_INTERVAL));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rmsystem.AbstractResourceManagerServiceProvider#
	 * getResourceManagerId()
	 */
	@Override
	public String getResourceManagerId() {
		return getId();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rm.ibm.pe.core.rmsystem.IPEResourceManagerConfiguration
	 * #getRunMiniproxy()
	 */
	public String getRunMiniproxy() {
		return getString(TAG_RUN_MINIPROXY,
				Preferences.getString(PECorePlugin.getUniqueIdentifier(), PEPreferenceConstants.RUN_MINIPROXY));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rm.ibm.pe.core.rmsystem.IPEResourceManagerConfiguration
	 * #getSuspendProxy()
	 */
	public String getSuspendProxy() {
		return getString(TAG_SUSPEND_PROXY, "n"); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rm.ibm.pe.core.rmsystem.IPEResourceManagerConfiguration
	 * #getUseLoadLeveler()
	 */
	public String getUseLoadLeveler() {
		return getString(TAG_USE_LOADLEVELER,
				Preferences.getString(PECorePlugin.getUniqueIdentifier(), PEPreferenceConstants.LOAD_LEVELER_OPTION));
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
	 * @see
	 * org.eclipse.ptp.rm.ibm.pe.core.rmsystem.IPEResourceManagerConfiguration
	 * #setDebugLevel(java.lang.String)
	 */
	public void setDebugLevel(String debugLevel) {
		putString(TAG_DEBUG_LEVEL, debugLevel);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rmsystem.IResourceManagerConfiguration#setDefaultNameAndDesc
	 * ()
	 */
	public void setDefaultNameAndDesc() {
		String name = "IBM PE"; //$NON-NLS-1$
		String conn = getConnectionName();
		if (conn != null && !conn.equals("")) { //$NON-NLS-1$
			name += "@" + conn; //$NON-NLS-1$
		}
		setName(name);
		setDescription("IBM PE Resource Manager"); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rm.ibm.pe.core.rmsystem.IPEResourceManagerConfiguration
	 * #setJobPollInterval(java.lang.String)
	 */
	public void setJobPollInterval(String interval) {
		putString(TAG_JOB_POLL_INTERVAL, interval);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rm.ibm.pe.core.rmsystem.IPEResourceManagerConfiguration
	 * #setLibraryOverride(java.lang.String)
	 */
	public void setLibraryOverride(String override) {
		putString(TAG_LIBRARY_OVERRIDE, override);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rm.ibm.pe.core.rmsystem.IPEResourceManagerConfiguration
	 * #setLoadLevelerMode(java.lang.String)
	 */
	public void setLoadLevelerMode(String mode) {
		putString(TAG_LOADLEVELER_MODE, mode);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rm.ibm.pe.core.rmsystem.IPEResourceManagerConfiguration
	 * #setNodeMaxPollInterval(java.lang.String)
	 */
	public void setNodeMaxPollInterval(String interval) {
		putString(TAG_MAX_NODE_POLL_INTERVAL, interval);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rm.ibm.pe.core.rmsystem.IPEResourceManagerConfiguration
	 * #setNodeMinPollInterval(java.lang.String)
	 */
	public void setNodeMinPollInterval(String interval) {
		putString(TAG_MIN_NODE_POLL_INTERVAL, interval);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rm.ibm.pe.core.rmsystem.IPEResourceManagerConfiguration
	 * #setRunMiniproxy(java.lang.String)
	 */
	public void setRunMiniproxy(String flag) {
		putString(TAG_RUN_MINIPROXY, flag);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rm.ibm.pe.core.rmsystem.IPEResourceManagerConfiguration
	 * #setSuspendProxy(java.lang.String)
	 */
	public void setSuspendProxy(String flag) {
		putString(TAG_SUSPEND_PROXY, flag);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rm.ibm.pe.core.rmsystem.IPEResourceManagerConfiguration
	 * #setUseLoadLeveler(java.lang.String)
	 */
	public void setUseLoadLeveler(String flag) {
		putString(TAG_USE_LOADLEVELER, flag);
	}
}
