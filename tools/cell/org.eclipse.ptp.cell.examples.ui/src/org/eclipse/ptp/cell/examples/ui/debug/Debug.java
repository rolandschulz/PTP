package org.eclipse.ptp.cell.examples.ui.debug;

import org.eclipse.ptp.cell.examples.ui.internal.ExampleUIPlugin;
import org.eclipse.ptp.cell.utils.debug.DebugPolicy;


public class Debug {
	public final static DebugPolicy POLICY = new DebugPolicy(ExampleUIPlugin.getDefault().getBundle().getSymbolicName());

	public static boolean DEBUG = false;
	
	static {
		read();
	}

	public static void read() {
		if (POLICY.read()) {
			DEBUG = POLICY.DEBUG;
		}
	}
	
}
