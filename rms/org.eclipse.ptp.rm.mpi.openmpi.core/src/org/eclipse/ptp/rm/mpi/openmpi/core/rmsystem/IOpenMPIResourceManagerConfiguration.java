/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.rm.mpi.openmpi.core.rmsystem;

import org.eclipse.ptp.rm.core.rmsystem.IToolRMConfiguration;

public interface IOpenMPIResourceManagerConfiguration extends IToolRMConfiguration {
	public static final String VERSION_UNKNOWN = "0.0"; //$NON-NLS-1$
	public static final String VERSION_AUTO = "auto"; //$NON-NLS-1$
	public static final String VERSION_12 = "1.2"; //$NON-NLS-1$
	public static final String VERSION_13 = "1.3"; //$NON-NLS-1$
	public static final String VERSION_14 = "1.4"; //$NON-NLS-1$

	public static int OPENMPI_CAPABILITIES = CAP_LAUNCH | CAP_DISCOVER | CAP_REMOTE_INSTALL_PATH;


	/**
	 * Get the detected Open MPI version. Only the major and minor version
	 * numbers are used. Any point or beta release information is discarded.
	 * 
	 * @return string representing the detected version 
	 *         or "unknown" if no version has been detected
	 */
	public String getDetectedVersion();

	/**
	 * Get the detected Open MPI service version.
	 * 
	 * @return the detected point version (default 0)
	 */
	public int getServiceVersion();

	/**
	 * Get the version selected when configuring the RM
	 * 
	 * @return string representing the Open MPI version
	 */
	public String getVersionId();

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
	public boolean setDetectedVersion(String version);

	/**
	 * Set the version that is selected when configuring the RM
	 * 
	 * @param versionId string representing the Open MPI version
	 */
	public void setVersionId(String versionId);

}