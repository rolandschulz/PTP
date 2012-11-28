package org.eclipse.ptp.rcp.sysmon;

import java.util.HashSet;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchDelegate;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ptp.core.IPTPLaunchConfigurationConstants;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.eclipse.ptp.rcp.sysmon"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;

	/**
	 * The constructor
	 */
	public Activator() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		setDefaultLaunchDelegates();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

	/**
	 * Returns an image descriptor for the image file at the given
	 * plug-in relative path
	 * 
	 * @param path
	 *            the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}

	/**
	 * Hack to get around broken preferences. Hopefully fixed at some point. See bug 380859.
	 */
	private void setDefaultLaunchDelegates() {
		ILaunchManager launchMgr = DebugPlugin.getDefault().getLaunchManager();

		ILaunchConfigurationType localCfg = launchMgr
				.getLaunchConfigurationType(IPTPLaunchConfigurationConstants.LAUNCH_APP_TYPE_ID);
		if (localCfg != null) {
			HashSet<String> runSet = new HashSet<String>();
			runSet.add(ILaunchManager.RUN_MODE);

			try {
				ILaunchDelegate[] delegates = localCfg.getDelegates(runSet);
				for (ILaunchDelegate delegate : delegates) {
					if ("org.eclipse.ptp.rcp.sysmon.launch".equals(delegate.getId())) {
						localCfg.setPreferredDelegate(runSet, delegate);
						break;
					}
				}
			} catch (CoreException e) {
			}
		}
	}
}
