package org.eclipse.ptp.cell.sputiming.ui.debug;

import org.eclipse.ptp.cell.sputiming.ui.Activator;
import org.eclipse.ptp.cell.utils.debug.DebugPolicy;


public class Debug {
	public final static DebugPolicy POLICY = new DebugPolicy(Activator.getDefault().getBundle().getSymbolicName());

	
	public static boolean DEBUG = false;
	public static boolean DEBUG_NOTIFICATIONS = false;
	public static boolean DEBUG_UI = false;

	static {
		read();
	}

	public static void read() {
		if (POLICY.read()) {
			DEBUG = POLICY.DEBUG;
			DEBUG_NOTIFICATIONS = DEBUG && POLICY.getBooleanOption("debug/notification"); //$NON-NLS-1$
			DEBUG_UI = DEBUG && POLICY.getBooleanOption("debug/ui"); //$NON-NLS-1$
		}
	}
	
}
