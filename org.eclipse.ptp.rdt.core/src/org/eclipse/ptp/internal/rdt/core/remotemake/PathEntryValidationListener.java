/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Mike Kucera (IBM)
 *******************************************************************************/
package org.eclipse.ptp.internal.rdt.core.remotemake;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.cdt.core.model.ICModelMarker;
import org.eclipse.cdt.internal.core.model.PathEntryManager;
import org.eclipse.cdt.internal.core.model.PathEntryUtil;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ptp.rdt.core.RDTLog;
import org.eclipse.ptp.rdt.core.messages.Messages;
import org.eclipse.ptp.rdt.core.resources.RemoteNature;


/**
 * CDT tries to validate include paths locally. This causes CDT
 * to report remote paths as warning markers on the project even if
 * the paths are valid on the remote machine.
 * 
 * This listener detects when CDT creates path entry problem markers,
 * and deletes them if they have been added to resources in a remote project.
 * 
 * @see PathEntryManager
 * @see PathEntryUtil
 */
public class PathEntryValidationListener {

	private PathEntryValidationListener() {}
	
	public static void startListening() {
		ResourcesPlugin.getWorkspace().addResourceChangeListener(invalidPathEntryDeleter, IResourceChangeEvent.POST_CHANGE);
	}
	
	public static void stopListening() {
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(invalidPathEntryDeleter);
	}
	
	
	private static class MarkerDeletionJob extends Job {
		private final List<IMarker> markersToDelete = new LinkedList<IMarker>();
		
		public MarkerDeletionJob() {
			super(Messages.PathEntryValidationListener_jobName);
		}

		public void addMarker(IMarker marker) {
			markersToDelete.add(marker);
		}
		
		@Override
		protected IStatus run(IProgressMonitor monitor) {
			monitor.beginTask(Messages.PathEntryValidationListener_jobName, markersToDelete.size());
			for(IMarker marker : markersToDelete) {
				monitor.worked(1);
				try {
					marker.delete();
				} catch (CoreException e) {
					RDTLog.logError(e);
				}
			}
			monitor.done();
			return Status.OK_STATUS;
		}
		
	};
	
	
	private static IResourceChangeListener invalidPathEntryDeleter = new IResourceChangeListener() {
		public void resourceChanged(IResourceChangeEvent event) {
			IMarkerDelta[] markerDeltas = event.findMarkerDeltas(ICModelMarker.PATHENTRY_PROBLEM_MARKER, true);
			if(markerDeltas == null || markerDeltas.length == 0)
				return;
			
			MarkerDeletionJob job = new MarkerDeletionJob();
			
			for(IMarkerDelta delta : markerDeltas) {
				if(RemoteNature.hasRemoteNature(delta.getResource().getProject())) {
					job.addMarker(delta.getMarker());
				}
			}
			
			job.schedule();
		}
	};
}
