package org.eclipse.ptp.rm.mpi.openmpi.ui.launch;

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
import org.eclipse.ptp.rm.mpi.openmpi.ui.OpenMPIUIPlugin;
import org.osgi.framework.Bundle;


public class OpenMPILaunchConfigurationDefaults {
	public static int ATTR_NUMPROCS;
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

		ATTR_NUMPROCS = getInteger(bundle, properties, "ATTR_NUMPROCS"); //$NON-NLS-1$
		ATTR_BYSLOT = getBoolean(bundle, properties, "ATTR_BYSLOT"); //$NON-NLS-1$
		ATTR_NOOVERSUBSCRIBE = getBoolean(bundle, properties, "ATTR_NOOVERSUBSCRIBE"); //$NON-NLS-1$
		ATTR_NOLOCAL = getBoolean(bundle, properties, "ATTR_NOLOCAL"); //$NON-NLS-1$
		ATTR_PREFIX = getString(bundle, properties, "ATTR_PREFIX"); //$NON-NLS-1$
		ATTR_USEPREFIX = getBoolean(bundle, properties, "ATTR_USEPREFIX"); //$NON-NLS-1$
		ATTR_HOSTFILE = getString(bundle, properties, "ATTR_HOSTFILE"); //$NON-NLS-1$
		ATTR_USEHOSTFILE = getBoolean(bundle, properties, "ATTR_USEHOSTFILE"); //$NON-NLS-1$
		ATTR_ARGUMENTS = getString(bundle, properties, "ATTR_ARGUMENTS"); //$NON-NLS-1$
		ATTR_USEDEFAULTARGUMENTS = getBoolean(bundle, properties, "ATTR_USEDEFAULTARGUMENTS"); //$NON-NLS-1$
		ATTR_USEDEFAULTPARAMETERS = getBoolean(bundle, properties, "ATTR_USEDEFAULTPARAMETERS"); //$NON-NLS-1$

		assert ATTR_PREFIX != null;
		assert ATTR_HOSTFILE != null;
		assert ATTR_ARGUMENTS != null;
		// TODO: read ATTR_PARAMETERS
	}

	public static Properties read(Path defaultsPropertiesPath, Bundle bundle)
	throws CoreException {
		InputStream inStream;
		Properties properties = new Properties();
		try {
			inStream = FileLocator.openStream(bundle, defaultsPropertiesPath, false);
			properties.load(inStream);
		} catch (IOException e) {
			throw OpenMPIUIPlugin.coreErrorException(Messages.OpenMPILaunchConfigurationDefaults_Exception_FailedReadFile, e);
		}
		return properties;
	}

	public static String getString(Bundle bundle, Properties properties, String key) throws CoreException {
		String value = properties.getProperty(key);
		if (value == null)
			throw new CoreException(new Status(IStatus.ERROR, bundle.getSymbolicName(), NLS.bind(Messages.OpenMPILaunchConfigurationDefaults_MissingValue, key)));
		else
			return value;
	}

	public static int getInteger(Bundle bundle, Properties properties, String key) throws CoreException {
		String value = getString(bundle, properties, key);
		try {
			return Integer.parseInt(value);
		} catch (NumberFormatException e) {
			throw new CoreException(new Status(IStatus.ERROR, bundle.getSymbolicName(), NLS.bind(Messages.OpenMPILaunchConfigurationDefaults_FailedParseInteger, key)));
		}
	}

	public static boolean getBoolean(Bundle bundle, Properties properties, String key) throws CoreException {
		String value = getString(bundle, properties, key);
		return Boolean.parseBoolean(value);
	}
}
