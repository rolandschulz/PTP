/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 * 
 * Modified by:
 * 		Claudia Knobloch, Forschungszentrum Juelich GmbH
 *******************************************************************************/
package org.eclipse.ptp.internal.rm.lml.core;

import org.eclipse.core.runtime.Platform;

public class DebugUtil {
	private static final String PROTOCOL_TRACING_OPTION = "org.eclipse.ptp.core/debug/proxy/protocol/tracing"; //$NON-NLS-1$
	private static final String PROXY_CLIENT_TRACING_OPTION = "org.eclipse.ptp.core/debug/proxy/client/tracing"; //$NON-NLS-1$
	private static final String PROXY_SERVER_DEBUG_LEVEL_OPTION = "org.eclipse.ptp.core/debug/proxy/server/debug_level"; //$NON-NLS-1$
	private static final String RM_TRACING_OPTION = "org.eclipse.ptp.core/debug/rm/tracing"; //$NON-NLS-1$

	public static boolean PROTOCOL_TRACING = false;
	public static boolean PROXY_CLIENT_TRACING = false;
	public static int PROXY_SERVER_DEBUG_LEVEL = 0;
	public static boolean RM_TRACING = false;;

	public static void configurePluginDebugOptions() {
		if (LMLCorePlugin.getDefault().isDebugging()) {
			String option = Platform.getDebugOption(PROTOCOL_TRACING_OPTION);
			if (option != null) {
				PROTOCOL_TRACING = option.equalsIgnoreCase("true"); //$NON-NLS-1$
			}
			option = Platform.getDebugOption(PROXY_CLIENT_TRACING_OPTION);
			if (option != null) {
				PROXY_CLIENT_TRACING = option.equalsIgnoreCase("true"); //$NON-NLS-1$
			}
			option = Platform.getDebugOption(PROXY_SERVER_DEBUG_LEVEL_OPTION);
			if (option != null) {
				try {
					PROXY_SERVER_DEBUG_LEVEL = Integer.parseInt(option);
				} catch (NumberFormatException e) {
					PROXY_SERVER_DEBUG_LEVEL = 0;
				}
			}
			option = Platform.getDebugOption(RM_TRACING_OPTION);
			if (option != null) {
				RM_TRACING = option.equalsIgnoreCase("true"); //$NON-NLS-1$
			}
		}
	}

}