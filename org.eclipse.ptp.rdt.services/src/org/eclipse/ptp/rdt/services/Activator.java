package org.eclipse.ptp.rdt.services;

import org.eclipse.core.resources.ISavedState;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.ptp.rdt.services.core.ServiceModelManager;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.eclipse.ptp.rdt.services"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;

	static final String MODEL_FILE_BASE = "service-model"; //$NON-NLS-1$

	static final String MODEL_FILE_PREFIX = MODEL_FILE_BASE + "-"; //$NON-NLS-1$

	/**
	 * The constructor
	 */
	public Activator() {
	}

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		ISavedState savedState = workspace.addSaveParticipant(this, new ServiceModelSaveParticipant());
		
		if (savedState != null) {
			IPath statePath = savedState.lookup(getServiceModelStateFilePath());
			ServiceModelManager manager = ServiceModelManager.getInstance();
			manager.loadModelConfiguration(statePath.toFile());
		}
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
		
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		workspace.removeSaveParticipant(this);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

	public void log(Throwable e) {
		log(new Status(IStatus.ERROR, PLUGIN_ID, IStatus.ERROR, "Error", e)); //$NON-NLS-1$
	}

	public void log(IStatus status) {
		getLog().log(status);
	}
	
	public void logErrorMessage(String message) {
		log(new Status(IStatus.ERROR, PLUGIN_ID, IStatus.ERROR, message, null));
	}

	/**
	 * Returns the physical path to the service model state file
	 * that corresponds to the given <code>saveNumber</code>.
	 * 
	 * @param saveNumber
	 * @return the physical path to the service model state file.
	 */
	static IPath getServiceModelStateFilePath(int saveNumber) {
		Activator plugin = Activator.getDefault();
		String saveFileName = MODEL_FILE_PREFIX + saveNumber;
		IPath path = plugin.getStateLocation().append(saveFileName);
		return path;
	}

	/**
	 * Returns the logical path to the service model state file.
	 * This logical path is mapped to the physical state file that resulted
	 * from the most recent successful save operation on the workspace.
	 * 
	 * @return the logical path to the service model state file.
	 */
	public static IPath getServiceModelStateFilePath() {
		return new Path(Activator.MODEL_FILE_BASE);
	}
}
