/*******************************************************************************
 * Copyright (c) 2008, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.rdt.ui.subsystems;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.ptp.internal.rdt.core.model.Scope;
import org.eclipse.ptp.internal.rdt.core.subsystems.ICIndexSubsystem;
import org.eclipse.ptp.rdt.core.RDTLog;

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
						Scope scope = new Scope(project.getName());
						
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
								if (project.isOpen()) {
									// Project was just opened.
									fSubsystem.checkProject(project, NULL_PROGRESS_MONITOR);
								}
							}
							break;
						}
						
						// We're not interested in the project's contents.
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
