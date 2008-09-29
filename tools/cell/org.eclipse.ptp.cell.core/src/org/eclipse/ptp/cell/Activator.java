package org.eclipse.ptp.cell;
import java.util.Date;

import org.eclipse.core.runtime.Plugin;
import org.eclipse.ptp.cell.debug.Debug;
import org.osgi.framework.BundleContext;



public class Activator extends Plugin {
    private static Activator plugin;
    
    public Activator() {
            plugin = this;
    }
    
    public static Activator getDefault() {
            return plugin;
    }
    
    public void start(BundleContext context) throws Exception {
            super.start(context);
            
            Debug.read();
            if (Debug.DEBUG) {
                    Date date = new Date();
                    Debug.POLICY.trace("Bundle started at {0}", date.toString()); //$NON-NLS-1$
            }
    }
    
    public void stop(BundleContext context) throws Exception {
            super.stop(context);
            plugin = null;
            
            Debug.read();
            if (Debug.DEBUG) {
                    Date date = new Date();
                    Debug.POLICY.trace("Bundle stopped at {0}", date.toString()); //$NON-NLS-1$
            }
    }

}
