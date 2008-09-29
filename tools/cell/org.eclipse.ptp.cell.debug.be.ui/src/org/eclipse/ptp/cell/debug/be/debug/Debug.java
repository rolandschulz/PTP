package org.eclipse.ptp.cell.debug.be.debug;

import org.eclipse.ptp.cell.debug.be.ui.Activator;
import org.eclipse.ptp.cell.utils.debug.DebugPolicy;


public class Debug {
	public final static DebugPolicy POLICY = new DebugPolicy(Activator.getDefault().getBundle().getSymbolicName());

	public static boolean DEBUG = false;
	public static boolean DEBUG_SEARCHER = false;

	static {
		read();
	}

	public static void read() {
		if (POLICY.read()) {
			DEBUG = POLICY.DEBUG;
			DEBUG_SEARCHER = DEBUG && POLICY.getBooleanOption("debug/searcher"); //$NON-NLS-1$
		}
	}
	
}
