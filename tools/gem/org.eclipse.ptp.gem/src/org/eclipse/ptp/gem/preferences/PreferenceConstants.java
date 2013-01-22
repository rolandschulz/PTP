/*******************************************************************************
 * Copyright (c) 2009, 2013 University of Utah School of Computing
 * 50 S Central Campus Dr. 3190 Salt Lake City, UT 84112
 * http://www.cs.utah.edu/formal_verification/
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alan Humphrey - Initial API and implementation
 *    Christopher Derrick - Initial API and implementation
 *    Prof. Ganesh Gopalakrishnan - Project Advisor
 *******************************************************************************/

package org.eclipse.ptp.gem.preferences;

/**
 * Class used to define preference page constants for this plug-in.
 */
public class PreferenceConstants {

	// Command line options
	public static final String GEM_PREF_FIB = "fibPreference"; //$NON-NLS-1$
	public static final String GEM_PREF_MPICALLS = "mpiCallsPreference"; //$NON-NLS-1$
	public static final String GEM_PREF_OPENMP = "openmpPreference"; //$NON-NLS-1$
	public static final String GEM_PREF_BLOCK = "blockingSendsPreference"; //$NON-NLS-1$
	public static final String GEM_PREF_REPORT = "reportPreference"; //$NON-NLS-1$
	public static final String GEM_PREF_VERBOSE = "verbosePreference"; //$NON-NLS-1$
	public static final String GEM_PREF_UNIXSOCKETS = "unixSockets"; //$NON-NLS-1$

	// Miscellaneous options
	public static final String GEM_PREF_PORTNUM = "portPreference"; //$NON-NLS-1$
	public static final String GEM_PREF_NUMPROCS = "numprocsPreference"; //$NON-NLS-1$
	public static final String GEM_PREF_REPORTNUM = "reportNumPreference"; //$NON-NLS-1$
	public static final String GEM_PREF_HOSTNAME = "hostName"; //$NON-NLS-1$
	public static final String GEM_PREF_CLRCON = "clearConsolePreference"; //$NON-NLS-1$
	public static final String GEM_ACTIVE_VIEW = "activeViewPreference"; //$NON-NLS-1$
	public static final String GEM_CONSOLE = "console"; //$NON-NLS-1$
	public static final String GEM_BROWSER = "browser"; //$NON-NLS-1$
	public static final String GEM_ANALYZER = "analyzer"; //$NON-NLS-1$
	public static final String GEM_PREF_REQUEST_ARGS = "requestArgs"; //$NON-NLS-1$

	// ISP paths, scripts and file names
	public static final String GEM_PREF_ISPEXE_PATH = "ispPath"; //$NON-NLS-1$
	public static final String GEM_PREF_ISPCC_PATH = "ispccPath"; //$NON-NLS-1$
	public static final String GEM_PREF_ISPCPP_PATH = "ispcppPath"; //$NON-NLS-1$
	public static final String GEM_PREF_HBV_PATH = "hbvPath"; //$NON-NLS-1$

	// Remote ISP paths, scripts and file names
	public static final String GEM_PREF_REMOTE_ISPEXE_PATH = "remoteIspPath"; //$NON-NLS-1$
	public static final String GEM_PREF_REMOTE_ISPCC_PATH = "remoteIspccPath"; //$NON-NLS-1$
	public static final String GEM_PREF_REMOTE_ISPCPP_PATH = "remoteIspcppPath"; //$NON-NLS-1$

	// Hidden preference for the order MPI calls are stepped
	public static final String GEM_PREF_STEP_ORDER = "stepOrderPreference"; //$NON-NLS-1$

	// Hidden preference for last file accessed - String representation of URI
	public static final String GEM_PREF_MOST_RECENT_FILE = "mostRecentFile"; //$NON-NLS-1$

	// Hidden preference for name of child processes
	public static final String GEM_PREF_PROCESS_NAME = "processName"; //$NON-NLS-1$

	// Hidden preference holding the previously used cmd line arguments
	public static final String GEM_PREF_ARGS = "cmdArgs"; //$NON-NLS-1$
}
