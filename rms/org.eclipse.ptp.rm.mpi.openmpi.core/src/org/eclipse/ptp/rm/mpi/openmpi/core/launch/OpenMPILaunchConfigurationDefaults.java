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
package org.eclipse.ptp.rm.mpi.openmpi.core.launch;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Properties;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ptp.rm.mpi.openmpi.core.OpenMPIPlugin;
import org.eclipse.ptp.rm.mpi.openmpi.core.messages.Messages;
import org.osgi.framework.Bundle;

/**
 * 
 * @author Daniel Felix Ferber
 * @since 4.0
 * 
 */
public class OpenMPILaunchConfigurationDefaults {
	public static int ATTR_NUMPROCS;
	public static boolean ATTR_BYNODE;
	public static boolean ATTR_BYSLOT;
	public static boolean ATTR_NOOVERSUBSCRIBE;
	public static boolean ATTR_NOLOCAL;
	public static String ATTR_PREFIX;
	public static boolean ATTR_USEPREFIX;
	public static String ATTR_HOSTFILE;
	public static boolean ATTR_USEHOSTFILE;
	public static String ATTR_HOSTLIST;
	public static boolean ATTR_USEHOSTLIST;
	public static String ATTR_ARGUMENTS;
	public static boolean ATTR_USEDEFAULTARGUMENTS;
	public static HashMap<String, String> ATTR_PARAMETERS = new HashMap<String, String>();
	public static boolean ATTR_USEDEFAULTPARAMETERS;

	private static String defaultsResourcePath = "/data/launch-defaults.properties"; //$NON-NLS-1$

	public static void loadDefaults() throws CoreException {
		Path defaultsPropertiesPath = new Path(defaultsResourcePath);
		Bundle bundle = OpenMPIPlugin.getDefault().getBundle();
		Properties properties = read(defaultsPropertiesPath, bundle);

		ATTR_NUMPROCS = getInteger(bundle, properties, "NUMPROCS"); //$NON-NLS-1$
		ATTR_BYNODE = getBoolean(bundle, properties, "BYNODE"); //$NON-NLS-1$
		ATTR_BYSLOT = getBoolean(bundle, properties, "BYSLOT"); //$NON-NLS-1$
		ATTR_NOOVERSUBSCRIBE = getBoolean(bundle, properties, "NOOVERSUBSCRIBE"); //$NON-NLS-1$
		ATTR_NOLOCAL = getBoolean(bundle, properties, "NOLOCAL"); //$NON-NLS-1$
		ATTR_PREFIX = getString(bundle, properties, "PREFIX"); //$NON-NLS-1$
		ATTR_USEPREFIX = getBoolean(bundle, properties, "USEPREFIX"); //$NON-NLS-1$
		ATTR_HOSTFILE = getString(bundle, properties, "HOSTFILE"); //$NON-NLS-1$
		ATTR_USEHOSTFILE = getBoolean(bundle, properties, "USEHOSTFILE"); //$NON-NLS-1$
		ATTR_ARGUMENTS = getString(bundle, properties, "ARGUMENTS"); //$NON-NLS-1$
		ATTR_USEDEFAULTARGUMENTS = getBoolean(bundle, properties, "USEDEFAULTARGUMENTS"); //$NON-NLS-1$
		ATTR_USEDEFAULTPARAMETERS = getBoolean(bundle, properties, "USEDEFAULTPARAMETERS"); //$NON-NLS-1$

		assert ATTR_PREFIX != null;
		assert ATTR_HOSTFILE != null;
		assert ATTR_ARGUMENTS != null;
		// TODO: read ATTR_PARAMETERS
	}

	public static Properties read(Path defaultsPropertiesPath, Bundle bundle) throws CoreException {
		InputStream inStream;
		Properties properties = new Properties();
		try {
			inStream = FileLocator.openStream(bundle, defaultsPropertiesPath, false);
			properties.load(inStream);
		} catch (IOException e) {
			throw OpenMPIPlugin.coreErrorException(Messages.OpenMPILaunchConfigurationDefaults_Exception_FailedReadFile, e);
		}
		return properties;
	}

	public static String getString(Bundle bundle, Properties properties, String key) throws CoreException {
		String value = properties.getProperty(key);
		if (value == null) {
			throw new CoreException(new Status(IStatus.ERROR, bundle.getSymbolicName(), NLS.bind(
					Messages.OpenMPILaunchConfigurationDefaults_MissingValue, key)));
		}
		return value;
	}

	public static int getInteger(Bundle bundle, Properties properties, String key) throws CoreException {
		String value = getString(bundle, properties, key);
		try {
			return Integer.parseInt(value);
		} catch (NumberFormatException e) {
			throw new CoreException(new Status(IStatus.ERROR, bundle.getSymbolicName(), NLS.bind(
					Messages.OpenMPILaunchConfigurationDefaults_FailedParseInteger, key)));
		}
	}

	public static boolean getBoolean(Bundle bundle, Properties properties, String key) throws CoreException {
		String value = getString(bundle, properties, key);
		return Boolean.parseBoolean(value);
	}
}
