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

import org.eclipse.ptp.core.Preferences;
import org.eclipse.ptp.rm.core.rmsystem.AbstractToolRMConfiguration;
import org.eclipse.ptp.rm.mpi.openmpi.core.OpenMPIPlugin;
import org.eclipse.ptp.rm.mpi.openmpi.core.OpenMPIPreferenceManager;
import org.eclipse.ptp.rm.mpi.openmpi.core.messages.Messages;
import org.eclipse.ptp.services.core.IServiceProvider;

public class OpenMPIResourceManagerConfiguration extends AbstractToolRMConfiguration implements
		IOpenMPIResourceManagerConfiguration {

	public static int OPENMPI_CAPABILITIES = CAP_LAUNCH | CAP_DISCOVER | CAP_REMOTE_INSTALL_PATH;

	private static final String TAG_VERSION_ID = "versionId"; //$NON-NLS-1$

	/*
	 * Actual version that is used to select correct commands. This version only
	 * persists while the RM is running.
	 */
	private int majorVersion = 0;

	private int minorVersion = 0;

	private int serviceVersion = 0;

	public OpenMPIResourceManagerConfiguration(String namespace, IServiceProvider provider) {
		super(OPENMPI_CAPABILITIES, namespace, provider);
		/*
		 * By default, assume openmpi auto configuration.
		 */
		setLaunchCmd(Preferences.getString(OpenMPIPlugin.getUniqueIdentifier(), OpenMPIPreferenceManager.PREFIX_AUTO
				+ OpenMPIPreferenceManager.PREFS_LAUNCH_CMD));
		setDebugCmd(Preferences.getString(OpenMPIPlugin.getUniqueIdentifier(), OpenMPIPreferenceManager.PREFIX_AUTO
				+ OpenMPIPreferenceManager.PREFS_DEBUG_CMD));
		setDiscoverCmd(Preferences.getString(OpenMPIPlugin.getUniqueIdentifier(), OpenMPIPreferenceManager.PREFIX_AUTO
				+ OpenMPIPreferenceManager.PREFS_DISCOVER_CMD));
		setRemoteInstallPath(Preferences.getString(OpenMPIPlugin.getUniqueIdentifier(), OpenMPIPreferenceManager.PREFIX_AUTO
				+ OpenMPIPreferenceManager.PREFS_REMOTE_INSTALL_PATH));
		setVersionId(VERSION_AUTO);
		setUseInstallDefaults(true);
		setUseToolDefaults(true);
		setCommandsEnabled(false);
		setDescription(Messages.OpenMPIResourceManagerConfiguration_defaultDescription);
	}

	/**
	 * Get the detected Open MPI version. Only the major and minor version
	 * numbers are used. Any point or beta release information is discarded.
	 * 
	 * @return string representing the detected version or "unknown" if no
	 *         version has been detected
	 */
	public String getDetectedVersion() {
		return majorVersion + "." + minorVersion; //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.mpi.openmpi.core.rmsystem.
	 * IOpenMPIResourceManagerConfiguration#getMajorVersion()
	 */
	public int getMajorVersion() {
		return majorVersion;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.mpi.openmpi.core.rmsystem.
	 * IOpenMPIResourceManagerConfiguration#getMinorVersion()
	 */
	public int getMinorVersion() {
		return minorVersion;
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rmsystem.IResourceManagerConfiguration#setDefaultNameAndDesc
	 * ()
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.mpi.openmpi.core.rmsystem.
	 * IOpenMPIResourceManagerConfiguration#setDetectedVersion(java.lang.String)
	 */
	public boolean setDetectedVersion(String version) {
		Pattern p = Pattern.compile("^(\\d+)\\.(\\d+)[^.]*(\\.(\\d+))?"); //$NON-NLS-1$
		Matcher m = p.matcher(version);
		if (m.matches()) {
			majorVersion = Integer.valueOf(m.group(1)).intValue();
			minorVersion = Integer.valueOf(m.group(2)).intValue();
			/*
			 * Assume any versions above 1.4 are the same as 1.4. NOTE: this may
			 * need to be changed in the future
			 */
			if (majorVersion > 1 || minorVersion > 4) {
				majorVersion = 1;
				minorVersion = 4;
			}
			if (m.group(3) != null) {
				serviceVersion = Integer.valueOf(m.group(4)).intValue();
			}
			if (!validateVersion()) {
				return false;
			}
			if (!getVersionId().equals(OpenMPIResourceManagerConfiguration.VERSION_AUTO)) {
				return getVersionId().equals(getDetectedVersion());
			}
			return true;
		}
		return false;
	}

	/**
	 * Set the version that is selected when configuring the RM
	 * 
	 * @param versionId
	 *            string representing the Open MPI version
	 */
	public void setVersionId(String versionId) {
		putString(TAG_VERSION_ID, versionId);
	}

	private boolean validateVersion() {
		return getDetectedVersion().equals(VERSION_12) || getDetectedVersion().equals(VERSION_13)
				|| getDetectedVersion().equals(VERSION_14);
	}

	/**
	 * @param majorVersion
	 *            the majorVersion to set
	 */
	protected void setMajorVersion(int majorVersion) {
		this.majorVersion = majorVersion;
	}

	/**
	 * @param minorVersion
	 *            the minorVersion to set
	 */
	protected void setMinorVersion(int minorVersion) {
		this.minorVersion = minorVersion;
	}

	/**
	 * @param serviceVersion
	 *            the serviceVersion to set
	 */
	protected void setServiceVersion(int serviceVersion) {
		this.serviceVersion = serviceVersion;
	}
}
