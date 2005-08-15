package org.eclipse.ptp.ui;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ptp.internal.ui.JobManager;
import org.eclipse.ptp.internal.ui.MachineManager;
import org.eclipse.ptp.internal.ui.UISetManager;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IPerspectiveListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The main plugin class to be used in the desktop.
 */
public class PTPUIPlugin extends AbstractUIPlugin {
    public static final String PLUGIN_ID = "org.eclipse.ptp.ui";

	//The shared instance.
	private static PTPUIPlugin plugin;
	//Resource bundle.
	private ResourceBundle resourceBundle;
	
	private UISetManager uiManager = null;
	private MachineManager machineManager = null;
	private JobManager jobManager = null;

	/**
	 * The constructor.
	 */
	public PTPUIPlugin() {
		super();
		plugin = this;
	}

	/**
	 * This method is called upon plug-in activation
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		uiManager = new UISetManager();
		machineManager = new MachineManager();
		jobManager = new JobManager();
	}

	/**
	 * This method is called when the plug-in is stopped
	 */
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
		machineManager.shutdown();
		jobManager.shutdown();
		machineManager = null;
		jobManager = null;
		plugin = null;
		resourceBundle = null;
	}
	
	public static String getUniqueIdentifier() {
		if (getDefault() == null)
			return PLUGIN_ID;

		return getDefault().getBundle().getSymbolicName();
	}	
	
	public UISetManager getUIManager() {
		return uiManager;
	}
	public MachineManager getMachineManager() {
		return machineManager;
	}
	public JobManager getJobManager() {
		return jobManager;
	}

	/**
	 * Returns the shared instance.
	 */
	public static PTPUIPlugin getDefault() {
		return plugin;
	}

	/**
	 * Returns the string from the plugin's resource bundle,
	 * or 'key' if not found.
	 */
	public static String getResourceString(String key) {
		ResourceBundle bundle = PTPUIPlugin.getDefault().getResourceBundle();
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
		try {
			if (resourceBundle == null)
				resourceBundle = ResourceBundle.getBundle("org.eclipse.ptp.ui.UIPluginResources");
		} catch (MissingResourceException x) {
			resourceBundle = null;
		}
		return resourceBundle;
	}

	/**
	 * Returns an image descriptor for the image file at the given
	 * plug-in relative path.
	 *
	 * @param path the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return AbstractUIPlugin.imageDescriptorFromPlugin("org.eclipse.ptp.ui", path);
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
	
	public String getCurrentPerspectiveID() {
		return getActiveWorkbenchWindow().getActivePage().getPerspective().getId();
	}
	
	public void addPersepectiveListener(IPerspectiveListener listener) {
		getActiveWorkbenchWindow().addPerspectiveListener(listener);
	}
	public void removePersepectiveListener(IPerspectiveListener listener) {
		getActiveWorkbenchWindow().removePerspectiveListener(listener);
	}	
}
