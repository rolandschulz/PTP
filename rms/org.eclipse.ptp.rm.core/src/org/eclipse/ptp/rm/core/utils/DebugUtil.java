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
package org.eclipse.ptp.rm.core.utils;

import java.text.MessageFormat;

import org.eclipse.core.runtime.Platform;
import org.eclipse.ptp.core.PTPCorePlugin;

public class DebugUtil {
	private static final String COMMAND_TRACING_OPTION = "org.eclipse.ptp.rm.core/debug/command"; //$NON-NLS-1$
	private static final String COMMAND_TRACING_OPTION_MORE = "org.eclipse.ptp.rm.core/debug/command/more"; //$NON-NLS-1$
	private static final String JOB_TRACING_OPTION = "org.eclipse.ptp.rm.core/debug/job"; //$NON-NLS-1$
	private static final String RTS_TRACING_OPTION = "org.eclipse.ptp.rm.core/debug/rts"; //$NON-NLS-1$
	private static final String RTS_DISCOVER_TRACING_OPTION = "org.eclipse.ptp.rm.core/debug/rts/discover"; //$NON-NLS-1$
	private static final String RTS_MONITOR_TRACING_OPTION = "org.eclipse.ptp.rm.core/debug/rts/monitor"; //$NON-NLS-1$
	private static final String RTS_JOB_TRACING_OPTION = "org.eclipse.ptp.rm.core/debug/rts/job"; //$NON-NLS-1$
	private static final String RTS_JOB_TRACING_OPTION_MORE = "org.eclipse.ptp.rm.core/debug/rts/job/more"; //$NON-NLS-1$
	private static final String RTS_JOB_OUTPUT_TRACING_OPTION = "org.eclipse.ptp.rm.core/debug/rts/job/output"; //$NON-NLS-1$

	public static boolean COMMAND_TRACING = false;
	public static boolean COMMAND_TRACING_MORE = false;
	public static boolean JOB_TRACING = false;
	public static boolean RTS_TRACING = false;
	public static boolean RTS_DISCOVER_TRACING = false;
	public static boolean RTS_MONITOR_TRACING = false;
	public static boolean RTS_JOB_TRACING = false;
	public static boolean RTS_JOB_TRACING_MORE = false;
	public static boolean RTS_JOB_OUTPUT_TRACING = false;

	public static void configurePluginDebugOptions() {
		if (PTPCorePlugin.getDefault().isDebugging()) {
			org.eclipse.ptp.core.util.DebugUtil.configurePluginDebugOptions();
			if (org.eclipse.ptp.core.util.DebugUtil.RM_TRACING) {
				String option = Platform.getDebugOption(COMMAND_TRACING_OPTION);
				if (option != null) {
					COMMAND_TRACING = option.equalsIgnoreCase("true") ; //$NON-NLS-1$
				}
				option = Platform.getDebugOption(COMMAND_TRACING_OPTION_MORE);
				if (option != null) {
					COMMAND_TRACING_MORE = option.equalsIgnoreCase("true") ; //$NON-NLS-1$
				}
				option = Platform.getDebugOption(JOB_TRACING_OPTION);
				if (option != null) {
					JOB_TRACING = option.equalsIgnoreCase("true") ; //$NON-NLS-1$
				}
				option = Platform.getDebugOption(RTS_TRACING_OPTION);
				if (option != null) {
					RTS_TRACING = option.equalsIgnoreCase("true") ; //$NON-NLS-1$
				}
				option = Platform.getDebugOption(RTS_DISCOVER_TRACING_OPTION);
				if (option != null) {
					RTS_DISCOVER_TRACING = option.equalsIgnoreCase("true") ; //$NON-NLS-1$
				}
				option = Platform.getDebugOption(RTS_MONITOR_TRACING_OPTION);
				if (option != null) {
					RTS_MONITOR_TRACING = option.equalsIgnoreCase("true") ; //$NON-NLS-1$
				}
				option = Platform.getDebugOption(RTS_JOB_TRACING_OPTION);
				if (option != null) {
					RTS_JOB_TRACING = option.equalsIgnoreCase("true") ; //$NON-NLS-1$
				}
				option = Platform.getDebugOption(RTS_JOB_TRACING_OPTION_MORE);
				if (option != null) {
					RTS_JOB_TRACING_MORE = option.equalsIgnoreCase("true") ; //$NON-NLS-1$
				}
				option = Platform.getDebugOption(RTS_JOB_OUTPUT_TRACING_OPTION);
				if (option != null) {
					RTS_JOB_OUTPUT_TRACING = option.equalsIgnoreCase("true") ; //$NON-NLS-1$
				}
			}
		}
	}

	public static void trace(boolean option, String pattern, Object ... arguments) {
		trace(option, MessageFormat.format(pattern, arguments));
	}

	public static void trace(boolean option, String message) {
		if (option) {
			System.out.println(message);
			System.out.flush();
		}
	}

	public static void error(boolean option, String pattern, Object ... arguments) {
		error(option, MessageFormat.format(pattern, arguments));
	}

	public static void error(boolean option, String message) {
		if (option) {
			System.err.println(message);
		}
	}
}
