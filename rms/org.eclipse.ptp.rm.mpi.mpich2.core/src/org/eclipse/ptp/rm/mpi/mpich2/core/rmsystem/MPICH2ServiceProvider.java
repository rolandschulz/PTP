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

import org.eclipse.core.runtime.Preferences;
import org.eclipse.ptp.rm.core.rmsystem.AbstractToolRMServiceProvider;
import org.eclipse.ptp.rm.mpi.mpich2.core.MPICH2PreferenceManager;
import org.eclipse.ptp.rm.mpi.mpich2.core.messages.Messages;

public class MPICH2ServiceProvider extends AbstractToolRMServiceProvider implements IMPICH2ResourceManagerConfiguration {
	private static final String TAG_VERSION_ID = "versionId"; //$NON-NLS-1$

	/*
	 * Actual version that is used to select correct commands. This version
	 * only persists while the RM is running.
	 */
	private int majorVersion = 0;
	private int minorVersion = 0;
	private int serviceVersion = 0;
	
	public MPICH2ServiceProvider() {
		super(MPICH2_CAPABILITIES);

		Preferences prefs = MPICH2PreferenceManager.getPreferences();
		setLaunchCmd(prefs.getString(MPICH2PreferenceManager.PREFIX + MPICH2PreferenceManager.PREFS_LAUNCH_CMD));
		setDebugCmd(prefs.getString(MPICH2PreferenceManager.PREFIX + MPICH2PreferenceManager.PREFS_DEBUG_CMD));
		setDiscoverCmd(prefs.getString(MPICH2PreferenceManager.PREFIX + MPICH2PreferenceManager.PREFS_DISCOVER_CMD));
		setPeriodicMonitorCmd(prefs.getString(MPICH2PreferenceManager.PREFIX + MPICH2PreferenceManager.PREFS_PERIODIC_MONITOR_CMD));
		setPeriodicMonitorTime(prefs.getInt(MPICH2PreferenceManager.PREFIX + MPICH2PreferenceManager.PREFS_PERIODIC_MONITOR_TIME));
		setRemoteInstallPath(prefs.getString(MPICH2PreferenceManager.PREFIX + MPICH2PreferenceManager.PREFS_REMOTE_INSTALL_PATH));
	}

	public MPICH2ServiceProvider(MPICH2ServiceProvider provider) {
		super(provider);
		provider.setLaunchCmd(getLaunchCmd());
		provider.setDebugCmd(getDebugCmd());
		provider.setDiscoverCmd(getDiscoverCmd());
		provider.setPeriodicMonitorCmd(getPeriodicMonitorCmd());
		provider.setPeriodicMonitorTime(getPeriodicMonitorTime());
		provider.setRemoteInstallPath(getRemoteInstallPath());
		provider.setVersionId(getVersionId());
		provider.setDescription(getDescription());
	}

	@Override
	public Object clone() {
		return new MPICH2ServiceProvider(this);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rmsystem.AbstractResourceManagerServiceProvider#getResourceManagerId()
	 */
	@Override
	public String getResourceManagerId() {
		return MPICH2RMServiceProviderFactory.RM_FACTORY_ID;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rm.mpi.mpich2.core.rmsystem.IMPICH2ResourceManagerConfiguration#getVersionId()
	 */
	public String getVersionId() {
		return getString(TAG_VERSION_ID, ""); //$NON-NLS-1$
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rmsystem.AbstractResourceManagerServiceProvider#setDefaultNameAndDesc()
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
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rm.mpi.mpich2.core.rmsystem.IMPICH2ResourceManagerConfiguration#setVersionId(java.lang.String)
	 */
	public void setVersionId(String versionId) {
		putString(TAG_VERSION_ID, versionId);
	}
}
