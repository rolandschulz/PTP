package org.eclipse.ptp.cell.debug;

import org.eclipse.ptp.cell.Activator;
import org.eclipse.ptp.cell.utils.debug.DebugPolicy;


public class Debug {
    /* The helper class. */
    public final static DebugPolicy POLICY = new DebugPolicy(Activator.getDefault().getBundle().getSymbolicName());

    /* Debug options. */
    public static boolean DEBUG = false;
    public static boolean DEBUG_VARIABLES = false;
    public static boolean DEBUG_ERRORPARSERS = false;
//  ... more static boolean DEBUG_OPTIONs.

    /* Static initialization. */
    static {
            read();
    }

    /* Get debug options. */
    public static void read() {
            if (POLICY.read()) {
                    DEBUG = POLICY.DEBUG;
                    DEBUG_VARIABLES = DEBUG && POLICY.getBooleanOption("debug/variables"); //$NON-NLS-1$
                    DEBUG_ERRORPARSERS = DEBUG && POLICY.getBooleanOption("debug/errorparses"); //$NON-NLS-1$
            }
    }
}

