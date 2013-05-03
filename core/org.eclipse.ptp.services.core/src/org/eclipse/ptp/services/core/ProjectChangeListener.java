/*******************************************************************************
 * Copyright (c) 2009, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.services.core;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.ptp.services.internal.core.ServicesCorePlugin;

import static org.eclipse.core.resources.IResourceDelta.*;

/**
 * Removes deleted projects from the service model.
 * 
 * @author Mike Kucera
 * @since 2.0
 */
public class ProjectChangeListener {

	private ProjectChangeListener() {}
	
	
	public static void startListening() {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		workspace.addResourceChangeListener(listener, IResourceChangeEvent.POST_CHANGE);
	}
	
	public static void stopListening() {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		workspace.removeResourceChangeListener(listener);
	}
	

	/**
	 * Detects project rename and delete events and updates the
	 * service model accordingly.
	 */
	private static final IResourceChangeListener listener = new IResourceChangeListener(){
		public void resourceChanged(IResourceChangeEvent event) {
			Map<IProject,String> renamedFrom = new HashMap<IProject,String>();
			Map<String,IProject> renamedTo   = new HashMap<String,IProject>();
			
			for(IResourceDelta delta : event.getDelta().getAffectedChildren()) {
				if(delta.getResource() instanceof IProject) { // maybe not necessary?
					IProject project = (IProject)delta.getResource();
					if(delta.getKind() == ADDED && (delta.getFlags() & MOVED_FROM) != 0) { // rename
						renamedFrom.put(project, delta.getMovedFromPath().segment(0));
					}
					else if(delta.getKind() == REMOVED && (delta.getFlags() & MOVED_TO) != 0) { // rename
						renamedTo.put(project.getName(), project);
					}
					else if(delta.getKind() == REMOVED) { // delete
						ServiceModelManager.getInstance().remove(project);
					}
				}
			}
			
			for(Map.Entry<IProject,String> addedInfo : renamedFrom.entrySet()) {
				IProject addedProject = addedInfo.getKey();
				IProject removedProject = renamedTo.remove(addedInfo.getValue());
				// does nothing if the project is not part of the service model
				ServiceModelManager.getInstance().remap(removedProject, addedProject);
				try {
					ServiceModelManager.getInstance().saveModelConfiguration();
				} catch (IOException e) {
					ServicesCorePlugin.getDefault().log(e);
				}
			}
		}
	};
}
