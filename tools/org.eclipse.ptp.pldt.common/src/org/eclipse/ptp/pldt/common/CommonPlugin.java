package org.eclipse.ptp.pldt.common;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.plugin.*;
import org.eclipse.jface.resource.ImageDescriptor;
import org.osgi.framework.BundleContext;



/**
 * The main plugin class to be used in the desktop.
 */
public class CommonPlugin extends AbstractUIPlugin {

	//The shared instance.
	private static CommonPlugin plugin;
    // Resource bundle.
    private ResourceBundle       resourceBundle;

	
	/**
	 * The constructor.
	 */
	public CommonPlugin() {
		plugin = this;
	}

	/**
	 * This method is called upon plug-in activation
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
	}

	/**
	 * This method is called when the plug-in is stopped
	 */
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
		plugin = null;
	}

	/**
	 * Returns the shared instance.
	 */
	public static CommonPlugin getDefault() {
		return plugin;
	}

	/**
	 * Returns an image descriptor for the image file at the given
	 * plug-in relative path.
	 *
	 * @param path the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return AbstractUIPlugin.imageDescriptorFromPlugin("org.eclipse.ptp.pldt.common", path);
	}
	
    /**
     * Returns the standard display to be used. The method first checks, if the thread calling this method has an
     * associated display. If so, this display is returned. Otherwise the method returns the default display.
     */
    public static Display getStandardDisplay()
    {
        Display display;
        display = Display.getCurrent();
        if (display == null) display = Display.getDefault();
        return display;
    }
    /**
     * BRT using this?
     * Returns the plugin's resource bundle,
     */
    public ResourceBundle getResourceBundle()
    {
        try {
            if (resourceBundle == null)
                resourceBundle = ResourceBundle.getBundle("org.ptp.pldt.common.CommonPluginResources");
        } catch (MissingResourceException x) {
            resourceBundle = null;
        }
        return resourceBundle;
    }

    /**
     * BRT using this?
     * Returns the string from the plugin's resource bundle, or 'key' if not found.
     */
    public static String getResourceString(String key)
    {
        ResourceBundle bundle = CommonPlugin.getDefault().getResourceBundle();
        try {
            return (bundle != null) ? bundle.getString(key) : key;
        } catch (MissingResourceException e) {
            return key;
        }
    }
}
