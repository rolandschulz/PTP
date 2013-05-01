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
package org.eclipse.ptp.internal.ui;

import org.eclipse.core.runtime.Platform;

public class DebugUtil {
	private static final String JOBS_VIEW_TRACING_OPTION = "org.eclipse.ptp.ui/views/jobs/tracing"; //$NON-NLS-1$
	private static final String MACHINES_VIEW_TRACING_OPTION = "org.eclipse.ptp.ui/views/machines/tracing"; //$NON-NLS-1$

	public static boolean JOBS_VIEW_TRACING = false;
	public static boolean MACHINES_VIEW_TRACING = false; 

	public static void configurePluginDebugOptions() {
		if (PTPUIPlugin.getDefault().isDebugging()) {
			String option = Platform.getDebugOption(JOBS_VIEW_TRACING_OPTION);
			if (option != null) {
				JOBS_VIEW_TRACING = option.equalsIgnoreCase("true") ; //$NON-NLS-1$
			}
			option = Platform.getDebugOption(MACHINES_VIEW_TRACING_OPTION);
			if (option != null) {
				MACHINES_VIEW_TRACING = option.equalsIgnoreCase("true") ; //$NON-NLS-1$
			}
		}
	}

}
