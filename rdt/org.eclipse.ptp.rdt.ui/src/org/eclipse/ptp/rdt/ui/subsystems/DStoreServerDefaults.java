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
package org.eclipse.ptp.rdt.ui.subsystems;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ptp.rdt.ui.UIPlugin;
import org.osgi.framework.Bundle;

public class DStoreServerDefaults {

	public static String COMMAND = null;
	public static String CLASSPATH = null;
	public static boolean DSTORE_TRACING = false;

	private static final String DEFAULTS_RESOURCE_PATH = "/data/dstore-server.properties"; //$NON-NLS-1$
	private static final String DSTORE_TRACING_OPTION = "org.eclipse.ptp.rdt.ui/debug/dstore/tracing"; //$NON-NLS-1$

	public static void loadDefaults() throws CoreException {
		if (UIPlugin.getDefault().isDebugging()) {
			String option = Platform.getDebugOption(DSTORE_TRACING_OPTION);
			if (option != null) {
				DSTORE_TRACING = option.equalsIgnoreCase("true") ; //$NON-NLS-1$
			}
		}
		
		Bundle bundle = UIPlugin.getDefault().getBundle();
		Properties properties = read(new Path(DEFAULTS_RESOURCE_PATH), bundle);

		COMMAND = getString(bundle, properties, "COMMAND"); //$NON-NLS-1$
		CLASSPATH = getString(bundle, properties, "CLASSPATH"); //$NON-NLS-1$

		assert COMMAND != null;
		assert CLASSPATH != null;
	}

	public static Properties read(Path defaultsPropertiesPath, Bundle bundle)
			throws CoreException {
		InputStream inStream;
		Properties properties = new Properties();
		try {
			inStream = FileLocator.openStream(bundle, defaultsPropertiesPath,
					false);
			properties.load(inStream);

		} catch (IOException e) {
			throw new CoreException(new Status(IStatus.ERROR, bundle
					.getSymbolicName(),
					"Failed to read DStore Server properties file")); //$NON-NLS-1$
		}
		return properties;
	}

	public static String getString(Bundle bundle, Properties properties,
			String key) throws CoreException {
		String value = properties.getProperty(key);
		if (value == null) {
			throw new CoreException(
					new Status(
							IStatus.ERROR,
							bundle.getSymbolicName(),
							NLS
									.bind(
											"Missing default value for {0} while reading DStore Server properties", key))); //$NON-NLS-1$
		}
		return value;
	}

	public static int getInteger(Bundle bundle, Properties properties,
			String key) throws CoreException {
		String value = getString(bundle, properties, key);
		try {
			return Integer.parseInt(value);
		} catch (NumberFormatException e) {
			throw new CoreException(
					new Status(
							IStatus.ERROR,
							bundle.getSymbolicName(),
							NLS
									.bind(
											"Invalid value for {0} while reading DStore Server properties", key))); //$NON-NLS-1$
		}
	}

	public static boolean getBoolean(Bundle bundle, Properties properties,
			String key) throws CoreException {
		String value = getString(bundle, properties, key);
		return Boolean.parseBoolean(value);
	}
}
