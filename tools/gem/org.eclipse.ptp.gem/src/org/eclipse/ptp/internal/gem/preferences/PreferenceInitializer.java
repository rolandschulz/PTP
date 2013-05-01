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

package org.eclipse.ptp.internal.gem.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ptp.internal.gem.GemPlugin;

/**
 * Class used to initialize default preference values for this plug-in.
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {

	/**
	 * Initializes default preference values for the GEM plug-in.
	 * 
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#
	 */
	@Override
	public void initializeDefaultPreferences() {

		// Preference store for the GEM plug-in
		final IPreferenceStore pstore = GemPlugin.getDefault().getPreferenceStore();

		// Command line options
		pstore.setDefault(PreferenceConstants.GEM_PREF_FIB, true);
		pstore.setDefault(PreferenceConstants.GEM_PREF_MPICALLS, false);
		pstore.setDefault(PreferenceConstants.GEM_PREF_OPENMP, false);
		pstore.setDefault(PreferenceConstants.GEM_PREF_BLOCK, true);
		pstore.setDefault(PreferenceConstants.GEM_PREF_REPORT, false);
		pstore.setDefault(PreferenceConstants.GEM_PREF_VERBOSE, false);
		pstore.setDefault(PreferenceConstants.GEM_PREF_UNIXSOCKETS, true);

		// Miscellaneous options
		pstore.setDefault(PreferenceConstants.GEM_PREF_PORTNUM, 9999);
		pstore.setDefault(PreferenceConstants.GEM_PREF_NUMPROCS, 2);
		pstore.setDefault(PreferenceConstants.GEM_PREF_REPORTNUM, 4);
		pstore.setDefault(PreferenceConstants.GEM_PREF_HOSTNAME, ""); //$NON-NLS-1$
		pstore.setDefault(PreferenceConstants.GEM_PREF_CLRCON, true);
		pstore.setDefault(PreferenceConstants.GEM_ACTIVE_VIEW, "analyzer"); //$NON-NLS-1$

		// ISP paths, scripts and file names
		pstore.setDefault(PreferenceConstants.GEM_PREF_ISPEXE_PATH, ""); //$NON-NLS-1$
		pstore.setDefault(PreferenceConstants.GEM_PREF_ISPCPP_PATH, ""); //$NON-NLS-1$
		pstore.setDefault(PreferenceConstants.GEM_PREF_ISPCC_PATH, ""); //$NON-NLS-1$
		pstore.setDefault(PreferenceConstants.GEM_PREF_HBV_PATH, ""); //$NON-NLS-1$

		// Remote ISP paths, scripts and file names
		pstore.setDefault(PreferenceConstants.GEM_PREF_REMOTE_ISPEXE_PATH, ""); //$NON-NLS-1$
		pstore.setDefault(PreferenceConstants.GEM_PREF_REMOTE_ISPCPP_PATH, ""); //$NON-NLS-1$
		pstore.setDefault(PreferenceConstants.GEM_PREF_REMOTE_ISPCC_PATH, ""); //$NON-NLS-1$

		// Hidden preference for the order MPI calls are stepped
		pstore.setDefault(PreferenceConstants.GEM_PREF_STEP_ORDER, "issueOrder"); //$NON-NLS-1$

		// Hidden preference for last file accessed - URI.toString
		pstore.setDefault(PreferenceConstants.GEM_PREF_MOST_RECENT_FILE, ""); //$NON-NLS-1$

		// Hidden preference for name of child processes
		pstore.setDefault(PreferenceConstants.GEM_PREF_PROCESS_NAME, ""); //$NON-NLS-1$

		// Preferences for command line arguments
		pstore.setDefault(PreferenceConstants.GEM_PREF_ARGS, ""); //$NON-NLS-1$
		pstore.setDefault(PreferenceConstants.GEM_PREF_REQUEST_ARGS, false);
	}

}
