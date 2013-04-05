/*******************************************************************************
 * Copyright (c) 2008, 2010 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.ptp.rm.lml.da.server.internal.core;

import org.eclipse.core.runtime.Platform;
import org.eclipse.ptp.rm.lml.da.server.Activator;

public class DebugUtil {
	public static boolean SERVER_TRACING = false;

	private static final String SERVER_TRACING_OPTION = "org.eclipse.ptp.rm.lml.da.server/debug/tracing"; //$NON-NLS-1$

	public static void configurePluginDebugOptions() {
		if (Activator.getDefault().isDebugging()) {
			String option = Platform.getDebugOption(SERVER_TRACING_OPTION);
			if (option != null) {
				SERVER_TRACING = option.equalsIgnoreCase("true"); //$NON-NLS-1$
			}
		}
	}
}
