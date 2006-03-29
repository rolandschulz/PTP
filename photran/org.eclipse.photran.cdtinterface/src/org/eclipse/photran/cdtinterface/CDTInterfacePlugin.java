package org.eclipse.photran.cdtinterface;

import org.eclipse.ui.plugin.*;
import org.eclipse.jface.resource.ImageDescriptor;
import org.osgi.framework.BundleContext;

/**
 * Main plug-in class for the Fortran Model Icons plug-in
 * 
 * @author joverbey
 */
public class CDTInterfacePlugin extends AbstractUIPlugin
{
    // The shared instance.
    private static CDTInterfacePlugin plugin;

    /**
     * The constructor.
     */
    public CDTInterfacePlugin()
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
    public static CDTInterfacePlugin getDefault()
    {
        return plugin;
    }

    /**
     * Returns an image descriptor for the image file at the given plug-in relative path.
     * 
     * @param path the path
     * @return the image descriptor
     */
    public static ImageDescriptor getImageDescriptor(String path)
    {
        return AbstractUIPlugin.imageDescriptorFromPlugin("org.eclipse.photran.cdtinterface", path);
    }
}
