package org.eclipse.ptp.pldt.openmp.ui.pv;

import org.eclipse.ui.plugin.*;
import org.eclipse.jface.resource.ImageDescriptor;
import org.osgi.framework.BundleContext;

/**
 * The main plugin class to be used in the desktop.
 */
public class PvPlugin extends AbstractUIPlugin {

	//The shared instance.
	private static PvPlugin plugin;
    
    public static final String MARKER_ID       = "org.eclipse.ptp.pldt.openmp.ui.pv.openMPProblemMarker";
    
    public static final String VIEW_ID         = "org.eclipse.ptp.pldt.openmp.ui.pv.views.OpenMPProblemsView";
	
	/**
	 * The constructor.
	 */
	public PvPlugin() {
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
	public static PvPlugin getDefault() {
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
		return AbstractUIPlugin.imageDescriptorFromPlugin("org.eclipse.ptp.pldt.openmp.ui.pv", path);
	}
}
