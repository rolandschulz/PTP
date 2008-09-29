package org.eclipse.ptp.cell.environment.launcher.cellbe.debug;

import org.eclipse.ptp.cell.environment.launcher.cellbe.RemoteTargetLauncherPlugin;
import org.eclipse.ptp.cell.utils.debug.DebugPolicy;


public class Debug {
	public final static DebugPolicy POLICY = new DebugPolicy(RemoteTargetLauncherPlugin.getDefault().getBundle().getSymbolicName());

	public static boolean DEBUG = false;
	public static boolean DEBUG_VARIABLES = false;
	public static boolean DEBUG_INTEGRATION = false;
	
	static {
		read();
	}

	public static void read() {
		if (POLICY.read()) {
			DEBUG = POLICY.DEBUG;
			DEBUG_VARIABLES = DEBUG && POLICY.getBooleanOption("debug/variables"); //$NON-NLS-1$
			DEBUG_INTEGRATION = DEBUG && POLICY.getBooleanOption("debug/integration"); //$NON-NLS-1$
		}
	}
	
}
