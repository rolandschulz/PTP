package org.eclipse.ptp.cell.make.core.debug;

import org.eclipse.ptp.cell.make.core.Activator;
import org.eclipse.ptp.cell.utils.debug.DebugPolicy;


public class Debug {
	public final static DebugPolicy POLICY = new DebugPolicy(Activator.getDefault().getBundle().getSymbolicName());

	public static boolean DEBUG = false;
	public static boolean DEBUG_XL_PROVIDER = false;
	
	static {
		read();
	}

	public static void read() {
		if (POLICY.read()) {
			DEBUG = POLICY.DEBUG;
			DEBUG_XL_PROVIDER = DEBUG && POLICY.getBooleanOption("debug/xlprovider"); //$NON-NLS-1$
		}
	}
	
}
