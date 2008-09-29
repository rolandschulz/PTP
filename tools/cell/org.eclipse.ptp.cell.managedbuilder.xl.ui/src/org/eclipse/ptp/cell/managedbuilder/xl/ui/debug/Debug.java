package org.eclipse.ptp.cell.managedbuilder.xl.ui.debug;

import org.eclipse.ptp.cell.managedbuilder.xl.ui.XlManagedBuilderUIPlugin;
import org.eclipse.ptp.cell.utils.debug.DebugPolicy;


public class Debug {
	public final static DebugPolicy POLICY = new DebugPolicy(XlManagedBuilderUIPlugin.getDefault().getBundle().getSymbolicName());

	public static boolean DEBUG = false;
	public static boolean DEBUG_HANDLER = false;
	public static boolean DEBUG_SUPPORT = false;

	static {
		read();
	}

	public static void read() {
		if (POLICY.read()) {
			DEBUG = POLICY.DEBUG;
			DEBUG_HANDLER = DEBUG && POLICY.getBooleanOption("debug/handler"); //$NON-NLS-1$
			DEBUG_SUPPORT = DEBUG && POLICY.getBooleanOption("debug/support"); //$NON-NLS-1$
		}
	}
	
}
