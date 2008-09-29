package org.eclipse.ptp.cell.managedbuilder.debug;

import org.eclipse.ptp.cell.managedbuilder.core.ManagedBuilderCorePlugin;
import org.eclipse.ptp.cell.utils.debug.DebugPolicy;


public class Debug {
	public final static DebugPolicy POLICY = new DebugPolicy(ManagedBuilderCorePlugin.getDefault().getBundle().getSymbolicName());

	public static boolean DEBUG = false;
	public static boolean DEBUG_MAKEFILE = false;
	public static boolean DEBUG_HANDLER = false;
	public static boolean DEBUG_APPLICABILITY = false;
	public static boolean DEBUG_SUPPLIER = false;
	
	static {
		read();
	}

	public static void read() {
		if (POLICY.read()) {
			DEBUG = POLICY.DEBUG;
			DEBUG_MAKEFILE = DEBUG && POLICY.getBooleanOption("debug/makefile"); //$NON-NLS-1$
			DEBUG_HANDLER = DEBUG && POLICY.getBooleanOption("debug/handler"); //$NON-NLS-1$
			DEBUG_APPLICABILITY = DEBUG && POLICY.getBooleanOption("debug/applicability"); //$NON-NLS-1$
			DEBUG_SUPPLIER = DEBUG && POLICY.getBooleanOption("debug/supplier"); //$NON-NLS-1$
		}
	}
	
}
