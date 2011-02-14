package org.eclipse.ptp.rm.jaxb.core;

import java.util.Properties;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.ptp.rm.core.AbstractRMDefaults;
import org.osgi.framework.Bundle;

public class JAXBDefaults extends AbstractRMDefaults {

	public static String LAUNCH_CMD = null;
	public static String DEBUG_CMD = null;
	public static String PATH = null;

	private static String defaultsResourcePath = "/data/defaults.properties"; //$NON-NLS-1$

	public static void loadDefaults() throws CoreException {
		Path defaultsPropertiesPath = new Path(defaultsResourcePath);
		Bundle bundle = JAXBCorePlugin.getDefault().getBundle();
		Properties properties = read(defaultsPropertiesPath, bundle);

		LAUNCH_CMD = getString(bundle, properties, "LAUNCH_CMD"); //$NON-NLS-1$
		DEBUG_CMD = getString(bundle, properties, "DEBUG_CMD"); //$NON-NLS-1$
		PATH = getString(bundle, properties, "PATH"); //$NON-NLS-1$

		assert LAUNCH_CMD != null;
		assert DEBUG_CMD != null;
		assert PATH != null;
	}
}
