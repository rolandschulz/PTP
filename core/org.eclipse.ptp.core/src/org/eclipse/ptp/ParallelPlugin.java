package org.eclipse.ptp;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.ptp.launch.core.ILaunchManager;
import org.eclipse.ptp.launch.internal.LaunchManager;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IPerspectiveListener;
import org.eclipse.ui.internal.WorkbenchWindow;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * @author Clement
 * 
 */
public class ParallelPlugin extends AbstractUIPlugin {
    public static final String PLUGIN_ID = "org.eclipse.pdt";
    
    private ILaunchManager launchManager = null;
    
    //The shared instance.
    private static ParallelPlugin plugin;
    //Resource bundle.
    private ResourceBundle resourceBundle;
    
    /**
     * The constructor.
     */
    public ParallelPlugin() {
        super();
        plugin = this;        
        try {
            resourceBundle = ResourceBundle.getBundle(PLUGIN_ID + ".ParallelPluginResources");
        } catch (MissingResourceException x) {
            resourceBundle = null;
        }
    }

    /**
     * This method is called upon plug-in activation
     */
    public void start(BundleContext context) throws Exception {
        super.start(context);  
        launchManager = new LaunchManager();
    }

    /**
     * This method is called when the plug-in is stopped
     */
    public void stop(BundleContext context) throws Exception {
        launchManager.shutdown();
        super.stop(context);
    }

    /**
     * @return Returns the launchManager.
     */
    public ILaunchManager getLaunchManager() {
        return launchManager;
    }

    /**
     * Returns the shared instance.
     */
    public static ParallelPlugin getDefault() {
        return plugin;
    }
    
    public void refreshParallelPluginActions() {
        refreshPluginActions();
    }    
    
	public static String getUniqueIdentifier() {
		if (getDefault() == null) {
			// If the default instance is not yet initialized,
			// return a static identifier. This identifier must
			// match the plugin id defined in plugin.xml
			return PLUGIN_ID;
		}
		return getDefault().getBundle().getSymbolicName();
	}    

    /**
     * Returns the string from the plugin's resource bundle, or 'key' if not found.
     */
    public static String getResourceString(String key) {
        ResourceBundle bundle = ParallelPlugin.getDefault().getResourceBundle();
        try {
            return (bundle != null) ? bundle.getString(key) : key;
        } catch (MissingResourceException e) {
            return key;
        }
    }

    /**
     * Returns the plugin's resource bundle,
     */
    public ResourceBundle getResourceBundle() {
        return resourceBundle;
    }
 
	public static IWorkbenchWindow getActiveWorkbenchWindow() {
		return getDefault().getWorkbench().getActiveWorkbenchWindow();
	}	
	
	public static IWorkbenchPage getActivePage() {
		IWorkbenchWindow w = getActiveWorkbenchWindow();
		if (w != null) {
			return w.getActivePage();
		}
		return null;
	}	
	
	/**
	 * Returns the active workbench shell or <code>null</code> if none
	 * 
	 * @return the active workbench shell or <code>null</code> if none
	 */
	public static Shell getActiveWorkbenchShell() {
		IWorkbenchWindow window = getActiveWorkbenchWindow();
		if (window != null) {
			return window.getShell();
		}
		return null;
	}
	
    public void addPerspectiveListener(final IPerspectiveListener perspectiveListener) {
        IWorkbenchWindow workBenchWindow = ParallelPlugin.getActiveWorkbenchWindow();
        if (workBenchWindow instanceof WorkbenchWindow) {
            workBenchWindow.addPerspectiveListener(perspectiveListener);
        }
    }
    public void removePerspectiveListener(final IPerspectiveListener perspectiveListener) {
        IWorkbenchWindow workBenchWindow = ParallelPlugin.getActiveWorkbenchWindow();
        if (workBenchWindow instanceof WorkbenchWindow) {
            workBenchWindow.removePerspectiveListener(perspectiveListener);
        }
    }	
}