/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.ptp.rm.mpi.mpich2.core.rmsystem;

import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.ptp.core.PTPCorePlugin;
import org.eclipse.ptp.core.elementcontrols.IPUniverseControl;
import org.eclipse.ptp.core.elementcontrols.IResourceManagerControl;
import org.eclipse.ptp.rm.core.rmsystem.AbstractToolRMServiceProvider;
import org.eclipse.ptp.rm.mpi.mpich2.core.MPICH2Plugin;
import org.eclipse.ptp.rm.mpi.mpich2.core.MPICH2PreferenceManager;
import org.eclipse.ptp.rm.mpi.mpich2.core.messages.Messages;
import org.eclipse.ptp.services.core.IServiceProviderWorkingCopy;

public class MPICH2ServiceProvider extends AbstractToolRMServiceProvider implements IMPICH2ResourceManagerConfiguration {
	private static final String TAG_VERSION_ID = "versionId"; //$NON-NLS-1$
	private static final String EMPTY_STR = ""; //$NON-NLS-1$

	public MPICH2ServiceProvider() {
		super(MPICH2_CAPABILITIES);

		IPreferencesService prefs = MPICH2PreferenceManager.getPreferences();
		setLaunchCmd(prefs.getString(MPICH2Plugin.getUniqueIdentifier(), MPICH2PreferenceManager.PREFIX
				+ MPICH2PreferenceManager.PREFS_LAUNCH_CMD, EMPTY_STR, null));
		setDebugCmd(prefs.getString(MPICH2Plugin.getUniqueIdentifier(), MPICH2PreferenceManager.PREFIX
				+ MPICH2PreferenceManager.PREFS_DEBUG_CMD, EMPTY_STR, null));
		setDiscoverCmd(prefs.getString(MPICH2Plugin.getUniqueIdentifier(), MPICH2PreferenceManager.PREFIX
				+ MPICH2PreferenceManager.PREFS_DISCOVER_CMD, EMPTY_STR, null));
		setPeriodicMonitorCmd(prefs.getString(MPICH2Plugin.getUniqueIdentifier(), MPICH2PreferenceManager.PREFIX
				+ MPICH2PreferenceManager.PREFS_PERIODIC_MONITOR_CMD, EMPTY_STR, null));
		setPeriodicMonitorTime(prefs.getInt(MPICH2Plugin.getUniqueIdentifier(), MPICH2PreferenceManager.PREFIX
				+ MPICH2PreferenceManager.PREFS_PERIODIC_MONITOR_TIME, 0, null));
		setRemoteInstallPath(prefs.getString(MPICH2Plugin.getUniqueIdentifier(), MPICH2PreferenceManager.PREFIX
				+ MPICH2PreferenceManager.PREFS_REMOTE_INSTALL_PATH, EMPTY_STR, null));
	}

	/**
	 * Constructor for creating a working copy of the service provider
	 * 
	 * @param provider
	 *            provider we are making a copy from
	 */
	public MPICH2ServiceProvider(MPICH2ServiceProvider provider) {
		super(provider);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.services.core.ServiceProvider#copy()
	 */
	@Override
	public IServiceProviderWorkingCopy copy() {
		return new MPICH2ServiceProvider(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rmsystem.AbstractResourceManagerServiceProvider#
	 * createResourceManager()
	 */
	@Override
	public IResourceManagerControl createResourceManager() {
		IPUniverseControl universe = (IPUniverseControl) PTPCorePlugin.getDefault().getUniverse();
		return new MPICH2ResourceManager(Integer.valueOf(universe.getNextResourceManagerId()), universe, this);
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
	 * @see org.eclipse.ptp.rm.mpi.mpich2.core.rmsystem.
	 * IMPICH2ResourceManagerConfiguration#getVersionId()
	 */
	public String getVersionId() {
		return getString(TAG_VERSION_ID, ""); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rmsystem.AbstractResourceManagerServiceProvider#
	 * setDefaultNameAndDesc()
	 */
	public void setDefaultNameAndDesc() {
		String name = Messages.MPICH2ResourceManagerConfiguration_defaultName;
		String conn = getConnectionName();
		if (conn != null && !conn.equals(EMPTY_STRING)) {
			name += "@" + conn; //$NON-NLS-1$
		}
		setName(name);
		setDescription(Messages.MPICH2ResourceManagerConfiguration_defaultDescription);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.mpi.mpich2.core.rmsystem.
	 * IMPICH2ResourceManagerConfiguration#setVersionId(java.lang.String)
	 */
	public void setVersionId(String versionId) {
		putString(TAG_VERSION_ID, versionId);
	}
}
