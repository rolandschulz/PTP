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
package org.eclipse.ptp.rm.mpi.mpich2.core;

import java.util.Properties;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.ptp.rm.core.AbstractRMDefaults;
import org.osgi.framework.Bundle;

/**
 * 
 * @author Daniel Felix Ferber
 * 
 */
public class MPICH2Defaults extends AbstractRMDefaults {

	public static String LAUNCH_CMD = null;
	public static String DEBUG_CMD = null;
	public static String DISCOVER_CMD = null;
	public static String PERIODIC_CMD = null;
	public static int PERIODIC_TIME = 0;
	public static String PATH = null;
	public static boolean USE_DEFAULTS = false;

	private static String defaultsResourcePath = "/data/defaults.properties"; //$NON-NLS-1$

	public static void loadDefaults() throws CoreException {
		Path defaultsPropertiesPath = new Path(defaultsResourcePath);
		Bundle bundle = MPICH2Plugin.getDefault().getBundle();
		Properties properties = read(defaultsPropertiesPath, bundle);

		LAUNCH_CMD = getString(bundle, properties, "LAUNCH_CMD"); //$NON-NLS-1$
		DEBUG_CMD = getString(bundle, properties, "DEBUG_CMD"); //$NON-NLS-1$
		DISCOVER_CMD = getString(bundle, properties, "DISCOVER_CMD"); //$NON-NLS-1$
		PERIODIC_CMD = getString(bundle, properties, "PERIODIC_CMD"); //$NON-NLS-1$
		PERIODIC_TIME = getInteger(bundle, properties, "PERIODIC_TIME"); //$NON-NLS-1$
		PATH = getString(bundle, properties, "PATH"); //$NON-NLS-1$
		USE_DEFAULTS = getBoolean(bundle, properties, "USE_DEFAULTS"); //$NON-NLS-1$

		assert LAUNCH_CMD != null;
		assert DEBUG_CMD != null;
		assert DISCOVER_CMD != null;
		assert PERIODIC_CMD != null;
		assert PATH != null;
	}
}
