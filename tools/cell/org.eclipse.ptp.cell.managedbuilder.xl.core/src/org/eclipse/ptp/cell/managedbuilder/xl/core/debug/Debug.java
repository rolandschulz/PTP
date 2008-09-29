package org.eclipse.ptp.cell.managedbuilder.xl.core.debug;

import org.eclipse.ptp.cell.managedbuilder.xl.core.Activator;
import org.eclipse.ptp.cell.utils.debug.DebugPolicy;


public class Debug {
	public final static DebugPolicy POLICY = new DebugPolicy(Activator.getDefault().getBundle().getSymbolicName());

	public static boolean DEBUG = false;
	public static boolean DEBUG_SUPPLIER = false;
	public static boolean DEBUG_SEARCHER = false;

	static {
		read();
	}

	public static void read() {
		if (POLICY.read()) {
			DEBUG = POLICY.DEBUG;
			DEBUG_SUPPLIER = DEBUG && POLICY.getBooleanOption("debug/supplier"); //$NON-NLS-1$
			DEBUG_SEARCHER = DEBUG && POLICY.getBooleanOption("debug/searcher"); //$NON-NLS-1$
		}
	}
	
}
