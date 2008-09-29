
package org.eclipse.ptp.cell.environment.launcher.pdt.debug;

import org.eclipse.ptp.cell.environment.launcher.pdt.Activator;
import org.eclipse.ptp.cell.utils.debug.DebugPolicy;


public class Debug {
	/* The utility class. */
	public final static DebugPolicy POLICY = new DebugPolicy(Activator.getDefault().getBundle().getSymbolicName());

	/* Debug options. */
	public static boolean DEBUG = false;
	
	public static boolean DEBUG_LAUNCHER = false;
	public static boolean DEBUG_LAUNCHER_VARIABLES = false;
	public static boolean DEBUG_PROFILE_INTEGRATION = false;
	public static boolean DEBUG_PROFILE_INTEGRATION_VARIABLES = false;
//	... more static boolean DEBUG_OPTIONs.

	

	/* Static initialization. */
	static {
		read();
	}

	/* Get debug options. */
	public static void read() {
		if (POLICY.read()) {
			DEBUG = POLICY.DEBUG;
			DEBUG_LAUNCHER = DEBUG && POLICY.getBooleanOption("debug/launcher"); //$NON-NLS-1$
			DEBUG_LAUNCHER_VARIABLES = DEBUG  && DEBUG_LAUNCHER && POLICY.getBooleanOption("debug/launcher/variables"); //$NON-NLS-1$
			DEBUG_PROFILE_INTEGRATION = DEBUG && POLICY.getBooleanOption("debug/profileintegration"); //$NON-NLS-1$
			DEBUG_PROFILE_INTEGRATION_VARIABLES = DEBUG && DEBUG_PROFILE_INTEGRATION && POLICY.getBooleanOption("debug/profileintegration/variables"); //$NON-NLS-1$
//			...read more DEBUG_OPTIONs.
		}
	}
}