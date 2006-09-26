package org.eclipse.photran.core;

import org.eclipse.core.runtime.Plugin;
import org.eclipse.photran.internal.core.parser.ParsingTable;
import org.eclipse.photran.internal.core.parser.Productions;
import org.osgi.framework.BundleContext;

/**
 * The main plug-in class for the Photran Core plug-in
 * 
 * @author joverbey
 */
public class FortranCorePlugin extends Plugin
{
    public static final String FIXED_FORM_CONTENT_TYPE = "org.eclipse.photran.core.fixedFormFortranSource";
    public static final String FREE_FORM_CONTENT_TYPE = "org.eclipse.photran.core.freeFormFortranSource";
    
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
        
        // Loading the huge stuff now so the user doesn't have to wait when they first open a file
        new ParsingTable(Productions.getInstance());
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
