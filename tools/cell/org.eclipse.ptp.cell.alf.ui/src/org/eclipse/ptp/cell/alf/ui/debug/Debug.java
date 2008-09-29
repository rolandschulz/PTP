package org.eclipse.ptp.cell.alf.ui.debug;

import org.eclipse.ptp.cell.alf.ui.Activator;
import org.eclipse.ptp.cell.utils.debug.DebugPolicy;


public class Debug {
    /* The helper class. */
    public final static DebugPolicy POLICY = new DebugPolicy(Activator .getDefault().getBundle().getSymbolicName());

    /* Debug options. */
    public static boolean DEBUG = false;
    public static boolean DEBUG_CREATE_ACTION = false;
    public static boolean DEBUG_CREATE_ACTION_MORE = false;
//  ... more static boolean DEBUG_OPTIONs.

    /* Static initialization. */
    static {
            read();
    }

    /* Get debug options. */
    public static void read() {
            if (POLICY.read()) {
                    DEBUG = POLICY.DEBUG;
                    DEBUG_CREATE_ACTION = DEBUG && POLICY.getBooleanOption("debug/createAction"); //$NON-NLS-1$
                    DEBUG_CREATE_ACTION_MORE = DEBUG && POLICY.getBooleanOption("debug/createAction/more"); //$NON-NLS-1$
//                  ...read more DEBUG_OPTIONs.
            }
    }
}

