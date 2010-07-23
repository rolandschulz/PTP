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
package org.eclipse.ptp.rm.mpi.openmpi.core;

import java.util.Properties;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.ptp.rm.core.AbstractRMDefaults;
import org.eclipse.ptp.rm.core.RMCorePlugin;
import org.osgi.framework.Bundle;

/**
 * 
 * @author Daniel Felix Ferber
 * 
 */
public class OpenMPIAutoDefaults extends AbstractRMDefaults {

	public static String LAUNCH_CMD = null;
	public static String DEBUG_CMD = null;
	public static String DISCOVER_CMD = null;
	public static String PATH = null;

	private static String defaultsResourcePath = "/data/defaults-OpenMPI-auto.properties"; //$NON-NLS-1$

	public static void loadDefaults() throws CoreException {
		Path defaultsPropertiesPath = new Path(defaultsResourcePath);
		Bundle bundle = RMCorePlugin.getDefault().getBundle();
		Properties properties = read(defaultsPropertiesPath, bundle);

		LAUNCH_CMD = getString(bundle, properties, "LAUNCH_CMD"); //$NON-NLS-1$
		DEBUG_CMD = getString(bundle, properties, "DEBUG_CMD"); //$NON-NLS-1$
		DISCOVER_CMD = getString(bundle, properties, "DISCOVER_CMD"); //$NON-NLS-1$
		PATH = getString(bundle, properties, "PATH"); //$NON-NLS-1$

		assert LAUNCH_CMD != null;
		assert DEBUG_CMD != null;
		assert DISCOVER_CMD != null;
		assert PATH != null;
	}
}
