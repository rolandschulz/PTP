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
package org.eclipse.ptp.rm.mpi.openmpi.core.rmsystem;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.Preferences;
import org.eclipse.ptp.rm.core.rmsystem.AbstractToolRMServiceProvider;
import org.eclipse.ptp.rm.mpi.openmpi.core.OpenMPIPreferenceManager;
import org.eclipse.ptp.rm.mpi.openmpi.core.messages.Messages;

public class OpenMPIServiceProvider extends AbstractToolRMServiceProvider implements IOpenMPIResourceManagerConfiguration {

	public static int OPENMPI_CAPABILITIES = CAP_LAUNCH | CAP_DISCOVER | CAP_REMOTE_INSTALL_PATH;

	private static final String TAG_VERSION_ID = "versionId"; //$NON-NLS-1$

	/*
	 * Actual version that is used to select correct commands. This version
	 * only persists while the RM is running.
	 */
	private int majorVersion = 0;

	private int minorVersion = 0;
	private int serviceVersion = 0;
	public OpenMPIServiceProvider() {
		super(OPENMPI_CAPABILITIES);
		
		/*
		 * By default, assume openmpi auto configuration.
		 */
		Preferences prefs = OpenMPIPreferenceManager.getPreferences();
		setLaunchCmd(prefs.getString(OpenMPIPreferenceManager.PREFIX_AUTO + OpenMPIPreferenceManager.PREFS_LAUNCH_CMD));
		setDebugCmd(prefs.getString(OpenMPIPreferenceManager.PREFIX_AUTO + OpenMPIPreferenceManager.PREFS_DEBUG_CMD));
		setDiscoverCmd(prefs.getString(OpenMPIPreferenceManager.PREFIX_AUTO + OpenMPIPreferenceManager.PREFS_DISCOVER_CMD));
		setRemoteInstallPath(prefs.getString(OpenMPIPreferenceManager.PREFIX_AUTO + OpenMPIPreferenceManager.PREFS_REMOTE_INSTALL_PATH));
		setVersionId(VERSION_AUTO);
		setUseInstallDefaults(true);
		setUseToolDefaults(true);
		setCommandsEnabled(false);
		setDescription(Messages.OpenMPIResourceManagerConfiguration_defaultDescription);
	}

	public OpenMPIServiceProvider(OpenMPIServiceProvider provider) {
		super(provider);
		provider.setLaunchCmd(getLaunchCmd());
		provider.setDebugCmd(getDebugCmd());
		provider.setDiscoverCmd(getDiscoverCmd());
		provider.setRemoteInstallPath(getRemoteInstallPath());
		provider.setVersionId(getVersionId());
		provider.setUseInstallDefaults(getUseInstallDefaults());
		provider.setUseToolDefaults(getUseToolDefaults());
		provider.setCommandsEnabled(getCommandsEnabled());
		provider.setDescription(getDescription());
	}

	@Override
	public Object clone() {
		return new OpenMPIServiceProvider(this);
	}
	
	/**
	 * Get the detected Open MPI version. Only the major and minor version
	 * numbers are used. Any point or beta release information is discarded.
	 * 
	 * @return string representing the detected version 
	 *         or "unknown" if no version has been detected
	 */
	public String getDetectedVersion() {
		return majorVersion + "." + minorVersion; //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rmsystem.AbstractResourceManagerServiceProvider#getResourceManagerId()
	 */
	@Override
	public String getResourceManagerId() {
		return OpenMPIRMServiceProviderFactory.RM_FACTORY_ID;
	}

	/**
	 * Get the detected Open MPI service version.
	 * 
	 * @return the detected point version (default 0)
	 */
	public int getServiceVersion() {
		return serviceVersion;
	}

	/**
	 * Get the version selected when configuring the RM
	 * 
	 * @return string representing the Open MPI version
	 */
	public String getVersionId() {
		return getString(TAG_VERSION_ID, ""); //$NON-NLS-1$
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rmsystem.IResourceManagerConfiguration#setDefaultNameAndDesc()
	 */
	public void setDefaultNameAndDesc() {
		String name = Messages.OpenMPIResourceManagerConfiguration_defaultName;
		String conn = getConnectionName();
		if (conn != null && !conn.equals(EMPTY_STRING)) {
			name += "@" + conn; //$NON-NLS-1$
		}
		setName(name);
		setDescription(Messages.OpenMPIResourceManagerConfiguration_defaultDescription);
	}

	/**
	 * Set the detected Open MPI version. Allowable version formats are:
	 * 
	 * 1.3		-> major=1, minor=3, point=0
	 * 1.2.8	-> major=1, minor=2, point=8
	 * 1.2b1	-> major=1, minor=2, point=0
	 * 
	 * Currently only 1.2 and 1.3 versions are valid.
	 * 
	 * If the versionId is not VERSION_AUTO, then the detected version
	 * must match the versionId.
	 * 
	 * @param version string representing the detected version
	 * @return true if version was correct
	 */
	public boolean setDetectedVersion(String version) {
		Pattern p = Pattern.compile("^(\\d+)\\.(\\d+)[^.]*(\\.(\\d+))?"); //$NON-NLS-1$
		Matcher m = p.matcher(version);
		if (m.matches()) {
			majorVersion = Integer.valueOf(m.group(1)).intValue();
			minorVersion = Integer.valueOf(m.group(2)).intValue();
			if (m.group(3) != null) {
				serviceVersion = Integer.valueOf(m.group(4)).intValue();
			}
			if (!validateVersion()) {
				return false;
			}
			if (!getVersionId().equals(OpenMPIServiceProvider.VERSION_AUTO)) {
				return getVersionId().equals(getDetectedVersion());
			}
			return true;
		}
		return false;
	}

	/**
	 * Set the version that is selected when configuring the RM
	 * 
	 * @param versionId string representing the Open MPI version
	 */
	public void setVersionId(String versionId) {
		putString(TAG_VERSION_ID, versionId);
	}
	
	private boolean validateVersion() {
		return getDetectedVersion().equals(VERSION_12)
			|| getDetectedVersion().equals(VERSION_13)
			|| getDetectedVersion().equals(VERSION_14);
	}
}
