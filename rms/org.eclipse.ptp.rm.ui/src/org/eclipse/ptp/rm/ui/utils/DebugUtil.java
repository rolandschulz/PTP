/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.rm.ui.utils;

import org.eclipse.core.runtime.Platform;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ptp.rm.ui.RMUIPlugin;

public class DebugUtil {
	private static final String DATASOURCE_OPTION = "org.eclipse.ptp.rm.ui/debug/dataSource"; //$NON-NLS-1$
	private static final String LISTENER_OPTION = "org.eclipse.ptp.rm.core/debug/widgetListener"; //$NON-NLS-1$

	public static boolean DATASOURCE_TRACING = false;
	public static boolean LISTENER_TRACING = false;

	public static void configurePluginDebugOptions() {
		if (RMUIPlugin.getDefault().isDebugging()) {
			String option = Platform.getDebugOption(DATASOURCE_OPTION);
			if (option != null) {
				DATASOURCE_TRACING = option.equalsIgnoreCase("true"); //$NON-NLS-1$
			}
			option = Platform.getDebugOption(LISTENER_OPTION);
			if (option != null) {
				LISTENER_TRACING = option.equalsIgnoreCase("true"); //$NON-NLS-1$
			}
		}
	}

	public static void trace(boolean option, String pattern, Object... arguments) {
		trace(option, NLS.bind(pattern, arguments));
	}

	public static void trace(boolean option, String message) {
		if (option) {
			System.out.println(message);
			System.out.flush();
		}
	}

	public static void error(boolean option, String pattern, Object... arguments) {
		error(option, NLS.bind(pattern, arguments));
	}

	public static void error(boolean option, String message) {
		if (option) {
			System.err.println(message);
		}
	}
}
