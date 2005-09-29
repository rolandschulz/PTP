package org.eclipse.photran.ui;

import org.eclipse.photran.internal.ui.preferences.ColorPreferencePage;
import org.eclipse.photran.internal.ui.preferences.FortranFreeFormSpacePreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The main plug-in class for the Photran UI plug-in
 * 
 * @author joverbey
 */
public class FortranUIPlugin extends AbstractUIPlugin
{
    // The shared instance.
    private static FortranUIPlugin plugin;

    /**
     * The constructor.
     */
    public FortranUIPlugin()
    {
        super();
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
    public static FortranUIPlugin getDefault()
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
        return AbstractUIPlugin.imageDescriptorFromPlugin("org.eclipse.photran.ui", path);
    }

    /**
     * Set the default preferences plugin values here.
     */
    protected void initializeDefaultPluginPreferences()
    {
        ColorPreferencePage.setDefaultColors();
        FortranFreeFormSpacePreferencePage.setDefaultSpaces();
    }
}
