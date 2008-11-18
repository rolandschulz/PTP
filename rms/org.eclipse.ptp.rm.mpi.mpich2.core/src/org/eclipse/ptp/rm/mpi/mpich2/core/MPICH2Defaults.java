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

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ptp.rm.mpi.mpich2.core.messages.Messages;
import org.osgi.framework.Bundle;

/**
 * 
 * @author Daniel Felix Ferber
 *
 */
public class MPICH2Defaults {

	public static String LAUNCH_CMD = null;
	public static String DEBUG_CMD = null;
	public static String DISCOVER_CMD = null;
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
		PATH = getString(bundle, properties, "PATH"); //$NON-NLS-1$
		USE_DEFAULTS = getBoolean(bundle, properties, "USE_DEFAULTS"); //$NON-NLS-1$

		assert LAUNCH_CMD != null;
		assert DEBUG_CMD != null;
		assert DISCOVER_CMD != null;
		assert PATH != null;
	}

	public static Properties read(Path defaultsPropertiesPath, Bundle bundle)
	throws CoreException {
		InputStream inStream;
		Properties properties = new Properties();
		try {
			inStream = FileLocator.openStream(bundle, defaultsPropertiesPath, false);
			properties.load(inStream);


		} catch (IOException e) {
			throw MPICH2Plugin.coreErrorException(Messages.MPICH2Defaults_Exception_FailedReadFile, e);
		}
		return properties;
	}

	public static String getString(Bundle bundle, Properties properties, String key) throws CoreException {
		String value = properties.getProperty(key);
		if (value == null) {
			throw new CoreException(new Status(IStatus.ERROR, bundle.getSymbolicName(), NLS.bind(Messages.MPICH2Defaults_MissingValue, key)));
		}
		
		return value;
	}

	public static int getInteger(Bundle bundle, Properties properties, String key) throws CoreException {
		String value = getString(bundle, properties, key);
		try {
			return Integer.parseInt(value);
		} catch (NumberFormatException e) {
			throw new CoreException(new Status(IStatus.ERROR, bundle.getSymbolicName(), NLS.bind(Messages.MPICH2Defaults_FailedParseInteger, key)));
		}
	}

	public static boolean getBoolean(Bundle bundle, Properties properties, String key) throws CoreException {
		String value = getString(bundle, properties, key);
		return Boolean.parseBoolean(value);
	}
}
