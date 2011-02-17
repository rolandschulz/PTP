/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.ptp.rm.mpi.mpich2.core.launch;

import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.ptp.rm.mpi.mpich2.core.MPICH2Plugin;

/**
 * 
 * @author Daniel Felix Ferber
 * @since 2.0
 * 
 */
public class MPICH2LaunchConfiguration {
	public static final String ATTR_BASE = MPICH2Plugin.getUniqueIdentifier() + ".launchAttributes"; //$NON-NLS-1$
	public static final String ATTR_NUMPROCS = ATTR_BASE + ".numProcs"; //$NON-NLS-1$
	public static final String ATTR_NOLOCAL = ATTR_BASE + ".noLocal"; //$NON-NLS-1$
	public static final String ATTR_PREFIX = ATTR_BASE + ".prefix"; //$NON-NLS-1$
	public static final String ATTR_USEPREFIX = ATTR_BASE + ".usePrefix"; //$NON-NLS-1$
	public static final String ATTR_HOSTFILE = ATTR_BASE + ".hostFile"; //$NON-NLS-1$
	public static final String ATTR_USEHOSTFILE = ATTR_BASE + ".useHostFile"; //$NON-NLS-1$
	public static final String ATTR_HOSTLIST = ATTR_BASE + ".hostList"; //$NON-NLS-1$
	public static final String ATTR_USEHOSTLIST = ATTR_BASE + ".useHostList"; //$NON-NLS-1$
	public static final String ATTR_ARGUMENTS = ATTR_BASE + ".arguments"; //$NON-NLS-1$
	public static final String ATTR_USEDEFAULTARGUMENTS = ATTR_BASE + ".useDefaultArguments"; //$NON-NLS-1$
	public static final String ATTR_PARAMETERS = ATTR_BASE + ".parameters"; //$NON-NLS-1$
	public static final String ATTR_USEDEFAULTPARAMETERS = ATTR_BASE + ".useDefaultParameters"; //$NON-NLS-1$

	public static String calculateArguments(ILaunchConfiguration configuration) throws CoreException {
		if (configuration.getAttribute(ATTR_USEDEFAULTARGUMENTS, MPICH2LaunchConfigurationDefaults.ATTR_USEDEFAULTARGUMENTS)) {
			String launchArgs = "-np " + Integer.toString(configuration.getAttribute(ATTR_NUMPROCS, MPICH2LaunchConfigurationDefaults.ATTR_NUMPROCS)); //$NON-NLS-1$
			if (configuration.getAttribute(ATTR_NOLOCAL, MPICH2LaunchConfigurationDefaults.ATTR_NOLOCAL)) {
				launchArgs += " -nolocal"; //$NON-NLS-1$
			}
			if (configuration.getAttribute(ATTR_USEPREFIX, MPICH2LaunchConfigurationDefaults.ATTR_USEPREFIX)) {
				launchArgs += " --prefix " + fixString(configuration.getAttribute(ATTR_PREFIX, MPICH2LaunchConfigurationDefaults.ATTR_PREFIX)); //$NON-NLS-1$
			}
			if (configuration.getAttribute(ATTR_USEHOSTFILE, MPICH2LaunchConfigurationDefaults.ATTR_USEHOSTFILE)) {
				launchArgs += " -hostfile " + fixString(configuration.getAttribute(ATTR_HOSTFILE, MPICH2LaunchConfigurationDefaults.ATTR_HOSTFILE)); //$NON-NLS-1$
			}
			if (configuration.getAttribute(ATTR_USEHOSTLIST, MPICH2LaunchConfigurationDefaults.ATTR_USEHOSTLIST)) {
				launchArgs += " -host " + textToHostList(fixString(configuration.getAttribute(ATTR_HOSTLIST, MPICH2LaunchConfigurationDefaults.ATTR_HOSTLIST))); //$NON-NLS-1$
			}

			if (!configuration.getAttribute(ATTR_USEDEFAULTPARAMETERS, MPICH2LaunchConfigurationDefaults.ATTR_USEDEFAULTPARAMETERS)) {
				Map<String, String> params = configuration.getAttribute(ATTR_PARAMETERS,
						MPICH2LaunchConfigurationDefaults.ATTR_PARAMETERS);
				for (Entry<String, String> param : params.entrySet()) {
					launchArgs += " -mca " + param.getKey() + " " + fixString(param.getValue()); //$NON-NLS-1$ //$NON-NLS-2$
				}
			}
			return launchArgs;
		}

		return configuration.getAttribute(ATTR_ARGUMENTS, MPICH2LaunchConfigurationDefaults.ATTR_ARGUMENTS);
	}

	/**
	 * Make string suitable for passing as an argument
	 * 
	 * @param s
	 * @return
	 */
	private static String fixString(String s) {
		// TODO is that right and escaped correctly?
		if (s == null) {
			return "\"\""; //$NON-NLS-1$
		}
		return "\"" + s + "\""; //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * Convert a one-host-per-line string into a comma separated list
	 * 
	 * @param text
	 * @return
	 */
	private static String textToHostList(String text) {
		if (text == null) {
			return ""; //$NON-NLS-1$
		}
		String result = ""; //$NON-NLS-1$
		String[] values = text.split("\n"); //$NON-NLS-1$
		for (int i = 0; i < values.length; i++) {
			if (!values[i].equals("")) { //$NON-NLS-1$
				if (i > 0) {
					result += ","; //$NON-NLS-1$
				}
				result += values[i];
			}
		}
		return result;
	}
}
