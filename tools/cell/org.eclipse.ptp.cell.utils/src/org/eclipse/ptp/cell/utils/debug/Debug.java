/******************************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *
 *****************************************************************************/
package org.eclipse.ptp.cell.utils.debug;

import org.eclipse.ptp.cell.utils.UtilsPlugin;

public class Debug {
	public final static DebugPolicy POLICY;
	static {
		DebugPolicy candidate = null;
		try {
			candidate = new DebugPolicy(UtilsPlugin.getDefault().getBundle().getSymbolicName());
		} catch (ExceptionInInitializerError e) {
			candidate = new DebugPolicy(null);
		}
		POLICY = candidate;
	}
	
	public static boolean DEBUG = false;
	public static boolean DEBUG_TERMINAL = false;
	public static boolean DEBUG_PLATFORM = false;
	public static boolean  DEBUG_PLATFORM_MORE = false;
	public static boolean DEBUG_PROCESS = false;
	public static boolean DEBUG_VT100 = false;
	public static boolean DEBUG_VT100_MORE = false;
	public static boolean DEBUG_STREAM = false;
	public static boolean DEBUG_STREAM_MORE = false;
	public static boolean DEBUG_LINUX = false;
	public static boolean DEBUG_LINUX_MORE = false;
	public static boolean DEBUG_LINUX_ARGUMENT = false;
	public static boolean DEBUG_FILE = false;
	public static boolean DEBUG_EXTENSION = false;
	public static boolean DEBUG_PROGRESS = false;

	static {
		read();
	}

	public static void read() {
		if (POLICY.read()) {
			DEBUG = POLICY.DEBUG;
			DEBUG_PLATFORM = DEBUG && POLICY.getBooleanOption("debug/utils/platform"); //$NON-NLS-1$
			DEBUG_PLATFORM_MORE = DEBUG && DEBUG_PLATFORM_MORE && POLICY.getBooleanOption("debug/utils/platform/more"); //$NON-NLS-1$
			DEBUG_PROCESS = DEBUG && POLICY.getBooleanOption("debug/utils/process"); //$NON-NLS-1$
			DEBUG_VT100 = DEBUG && POLICY.getBooleanOption("debug/utils/vt100"); //$NON-NLS-1$
			DEBUG_VT100_MORE = DEBUG && DEBUG_VT100_MORE && POLICY.getBooleanOption("debug/utils/vt100/more"); //$NON-NLS-1$
			DEBUG_STREAM= DEBUG && POLICY.getBooleanOption("debug/utils/stream"); //$NON-NLS-1$
			DEBUG_STREAM_MORE= DEBUG && DEBUG_STREAM_MORE && POLICY.getBooleanOption("debug/utils/stream/more"); //$NON-NLS-1$
			DEBUG_TERMINAL= DEBUG && POLICY.getBooleanOption("debug/utils/terminal"); //$NON-NLS-1$
			DEBUG_LINUX= DEBUG && POLICY.getBooleanOption("debug/utils/linux"); //$NON-NLS-1$
			DEBUG_LINUX_MORE= DEBUG && DEBUG_LINUX && POLICY.getBooleanOption("debug/utils/linux/more"); //$NON-NLS-1$
			DEBUG_LINUX_ARGUMENT= DEBUG && DEBUG_LINUX && POLICY.getBooleanOption("debug/utils/linux/argument"); //$NON-NLS-1$
			DEBUG_FILE= DEBUG && POLICY.getBooleanOption("debug/utils/file"); //$NON-NLS-1$
			DEBUG_EXTENSION= DEBUG && POLICY.getBooleanOption("debug/utils/extension"); //$NON-NLS-1$
			DEBUG_PROGRESS= DEBUG && POLICY.getBooleanOption("debug/ui/debug"); //$NON-NLS-1$
		}
	}
	
}
