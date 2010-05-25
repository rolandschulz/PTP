/*******************************************************************************
 * Copyright (c) 2008, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.rdt.ui.subsystems;

import java.net.URI;

import org.eclipse.cdt.utils.EFSExtensionManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ptp.internal.rdt.core.model.Scope;
import org.eclipse.ptp.internal.rdt.core.subsystems.ICIndexSubsystem;
import org.eclipse.ptp.rdt.core.RDTLog;
import org.eclipse.ptp.rdt.core.resources.RemoteNature;
import org.eclipse.ptp.rdt.ui.messages.Messages;

/**
 * Synchronizes state of remote indices by responding to changes to
 * <code>IProject</code>s in the workspace.
 * 
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is no guarantee that this API will work or
 * that it will remain the same. Please do not use this API without consulting
 * with the RDT team.
 * 
 */
public class ProjectChangeListener implements IResourceChangeListener {

	private IProgressMonitor NULL_PROGRESS_MONITOR = new NullProgressMonitor();
	private ICIndexSubsystem fSubsystem;
	
	private class ProjectInitializationJob extends Job {
		private IProject fProject;
		public ProjectInitializationJob(IProject project) {
			super(Messages.getString("ProjectChangeListener.0")); //$NON-NLS-1$
			fProject = project;
		}

		public IStatus run(IProgressMonitor monitor) {
			fSubsystem.checkProject(fProject, monitor);
			return Status.OK_STATUS;	
		}
	}

	public ProjectChangeListener(ICIndexSubsystem subsystem) {
		fSubsystem = subsystem;
	}

	public void resourceChanged(IResourceChangeEvent event) {
		if (event.getType() != IResourceChangeEvent.POST_CHANGE) {
			return;
		}
		
		IResourceDelta delta = event.getDelta();
		try {
			delta.accept(new IResourceDeltaVisitor() {
				public boolean visit(IResourceDelta delta) throws CoreException {
					IResource resource = delta.getResource();
					if (resource instanceof IProject) {
						IProject project = (IProject) resource;
						
						//the scope is only used when the project is deleted. We do not need to set the other
						//information since it is not available when the project is deleted. In any case, this information is not needed for the
						//unregisterScope and removeIndexFile calls below.
						Scope scope = new Scope(project.getName(), null, null, null, null);
						
						switch (delta.getKind()) {
						case IResourceDelta.ADDED:
							// New projects don't need to be checked.  Hopefully, the
							// new project wizard initialized it properly.
							break;
							
						case IResourceDelta.REMOVED:
							fSubsystem.unregisterScope(scope, NULL_PROGRESS_MONITOR);
							fSubsystem.removeIndexFile(scope, NULL_PROGRESS_MONITOR);
							break;
							
						case IResourceDelta.CHANGED:
							if ((delta.getFlags() & IResourceDelta.OPEN) != 0) {
								//do not check project unless it has a remote nature
								if (project.isOpen() && project.hasNature(RemoteNature.REMOTE_NATURE_ID)) {
									// Project was just opened.
									
									// we have to push off initialization into a workspace job because the project may
									// not be done loading yet, and if we try to access its data, it will trigger a concurrent
									// load of the project data, which will then deadlock the UI thread from which the project is
									// being opened
									
									ProjectInitializationJob job = new ProjectInitializationJob(project);
									job.setRule(project);
									job.schedule();
									
								}
							}
							break;
						}	
						return false;
					}
					return true;
				}
			});
		} catch (CoreException e) {
			RDTLog.logError(e);
		}
	}

}
