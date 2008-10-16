package org.eclipse.ptp.rdt.services.core;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.ResourcesPlugin;

/**
 * Removes deleted projects from the service model.
 * 
 * @author Mike Kucera
 *
 */
public class ProjectDeletionListener implements IResourceChangeListener {

	private ProjectDeletionListener() {}
	
	private static final ProjectDeletionListener instance = new ProjectDeletionListener();
	
	
	public static void startListening() {
		// before-the-fact report of pending deletion of a single project
		ResourcesPlugin.getWorkspace().addResourceChangeListener(instance, IResourceChangeEvent.PRE_DELETE);
	}
	
	public static void stopListening() {
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(instance);
	}
	
	/**
	 * Will get notified about project deletion before the project is actually
	 * deleted, simply remove the project from the service model.
	 */
	public void resourceChanged(IResourceChangeEvent event) {
		IProject project = (IProject) event.getResource();
		// does nothing if the project is not part of the service model
		ServiceModelManager.getInstance().remove(project);
	}

}
