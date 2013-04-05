/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.internal.remote.core;

import org.eclipse.core.runtime.Platform;

public class DebugUtil {
	private static final String SERVER_TRACING_OPTION = "org.eclipse.ptp.remote.core/debug/server/tracing"; //$NON-NLS-1$

	public static boolean SERVER_TRACING = false;

	public static void configurePluginDebugOptions() {
		if (PTPRemoteCorePlugin.getDefault().isDebugging()) {
			String option = Platform.getDebugOption(SERVER_TRACING_OPTION);
			if (option != null) {
				SERVER_TRACING = option.equalsIgnoreCase("true"); //$NON-NLS-1$
			}
		}
	}

}
