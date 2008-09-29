package org.eclipse.ptp.cell.sputiming.debug;

import org.eclipse.ptp.cell.sputiming.Activator;
import org.eclipse.ptp.cell.utils.debug.DebugPolicy;


public class Debug {
	public final static DebugPolicy POLICY = new DebugPolicy(Activator.getDefault().getBundle().getSymbolicName());

	
	public static boolean DEBUG = false;
	public static boolean DEBUG_VISUALIZATION = false;
	public static boolean DEBUG_COMPILER = false;
	public static boolean DEBUG_SPUTIMING = false;	
	public static boolean DEBUG_POPUP_ACTION = false;
	public static boolean DEBUG_LAUNCH_ACTION = false;

	static {
		read();
	}

	public static void read() {
		if (POLICY.read()) {
			DEBUG = POLICY.DEBUG;
			DEBUG_VISUALIZATION = DEBUG && POLICY.getBooleanOption("debug/visualization"); //$NON-NLS-1$
			DEBUG_COMPILER = DEBUG && POLICY.getBooleanOption("debug/execution/compiler"); //$NON-NLS-1$
			DEBUG_SPUTIMING = DEBUG && POLICY.getBooleanOption("debug/execution/execution"); //$NON-NLS-1$
			DEBUG_POPUP_ACTION = DEBUG && POLICY.getBooleanOption("debug/popupAction"); //$NON-NLS-1$
			DEBUG_LAUNCH_ACTION = DEBUG && POLICY.getBooleanOption("debug/launchAction"); //$NON-NLS-1$
		}
	}
	
}
