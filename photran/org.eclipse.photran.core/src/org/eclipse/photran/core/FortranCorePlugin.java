package org.eclipse.photran.core;

import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;

/**
 * The main plug-in class for the Photran Core plug-in
 * 
 * @author joverbey
 */
public class FortranCorePlugin extends Plugin
{
    // The shared instance.
    private static FortranCorePlugin plugin;

    /**
     * The constructor.
     */
    public FortranCorePlugin()
    {
        plugin = this;
    }

    /**
     * This method is called upon plug-in activation
     */
    public void start(BundleContext context) throws Exception
    {
        super.start(context);
    }

    /**
     * This method is called when the plug-in is stopped
     */
    public void stop(BundleContext context) throws Exception
    {
        super.stop(context);
        plugin = null;
    }

    /**
     * Returns the shared instance.
     */
    public static FortranCorePlugin getDefault()
    {
        return plugin;
    }
}
