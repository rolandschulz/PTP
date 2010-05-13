/*******************************************************************************
 * Copyright (c) 2009, 2010 University of Utah School of Computing
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
 * Constant definitions for GEM plug-in preference page.
 */
public class PreferenceConstants {

	// Command line options
	public static final String GEM_PREF_FIB_OPTION = "fibPreference"; //$NON-NLS-1$
	public static final String GEM_PREF_MPICALLS_OPTION = "mpicallsPreference"; //$NON-NLS-1$
	public static final String GEM_PREF_OPENMP_OPTION = "openmpPreference"; //$NON-NLS-1$
	public static final String GEM_PREF_BLOCK_OPTION = "blockingsendsPreference"; //$NON-NLS-1$
	public static final String GEM_PREF_REPORT_OPTION = "reportPreference"; //$NON-NLS-1$
	public static final String GEM_PREF_VERBOSE = "verbosePreference"; //$NON-NLS-1$

	// Miscellaneous options
	public static final String GEM_PREF_PORTNUM = "portPreference"; //$NON-NLS-1$
	public static final String GEM_PREF_NUMPROCS = "numprocsPreference"; //$NON-NLS-1$
	public static final String GEM_PREF_REPORTNUM = "reportnumPreference"; //$NON-NLS-1$
	public static final String GEM_PREF_CLRCON = "clrconPreference"; //$NON-NLS-1$

	// GEM paths, scripts and file names
	public static final String GEM_PREF_ISPEXE_PATH = "ispPath"; //$NON-NLS-1$
	public static final String GEM_PREF_ISPCC_PATH = "ispccPath"; //$NON-NLS-1$
	public static final String GEM_PREF_ISPCPP_PATH = "ispcppPath"; //$NON-NLS-1$
	public static final String GEM_PREF_HBV_PATH = "hbvPath"; //$NON-NLS-1$

	// Hidden preference for the order MPI calls are stepped
	public static final String GEM_PREF_STEP_ORDER = "steporderPreference"; //$NON-NLS-1$

	// Hidden preference for last open file to be accessed
	public static final String GEM_PREF_LAST_FILE = "lastFile"; //$NON-NLS-1$

}
