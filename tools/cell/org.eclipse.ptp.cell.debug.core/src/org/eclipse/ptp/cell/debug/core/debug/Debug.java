package org.eclipse.ptp.cell.debug.core.debug;

import org.eclipse.ptp.cell.debug.CellDebugPlugin;
import org.eclipse.ptp.cell.utils.debug.DebugPolicy;


public class Debug {
    /* The helper class. */
    public final static DebugPolicy POLICY = new DebugPolicy(CellDebugPlugin.getDefault().getBundle().getSymbolicName());

    /* Debug options. */
    public static boolean DEBUG = false;
    public static boolean DEBUG_DELEGATE = false;
//  ... more static boolean DEBUG_OPTIONs.

    /* Static initialization. */
    static {
            read();
    }

    /* Get debug options. */
    public static void read() {
            if (POLICY.read()) {
                    DEBUG = POLICY.DEBUG;
                    DEBUG_DELEGATE = DEBUG && POLICY.getBooleanOption("debug/launch/delegate"); //$NON-NLS-1$
//                  ...read more DEBUG_OPTIONs.
            }
    }
}

