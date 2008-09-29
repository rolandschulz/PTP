package org.eclipse.ptp.cell.environment.ui.deploy.debug;

import org.eclipse.ptp.cell.environment.ui.deploy.DeployPlugin;
import org.eclipse.ptp.cell.utils.debug.DebugPolicy;


public class Debug {
	public final static DebugPolicy POLICY = new DebugPolicy(DeployPlugin.getDefault().getBundle().getSymbolicName());

	public static boolean DEBUG = false;
	public static boolean DEBUG_JOBS = false;
	
	static {
		read();
	}

	public static void read() {
		if (POLICY.read()) {
			DEBUG = POLICY.DEBUG;
			DEBUG_JOBS = DEBUG && POLICY.getBooleanOption("debug/jobs"); //$NON-NLS-1$
		}
	}
	
}
