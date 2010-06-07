/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.services.core;

import java.io.IOException;

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
		ServiceModelManager manager = ServiceModelManager.getInstance();
		
		// does nothing if the project is not part of the service model
		manager.remove(project);
		try {
			manager.saveModelConfiguration();
		} catch (IOException e) {
			ServicesCorePlugin.getDefault().log(e);
		}
	}

}
