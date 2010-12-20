/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.rdt.sync.core.resources;

import org.eclipse.cdt.core.CProjectNature;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ptp.rdt.sync.core.RDTSyncCorePlugin;

/**
 * Project nature for remote projects.
 * 
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as part
 * of a work in progress. There is no guarantee that this API will work or that
 * it will remain the same. Please do not use this API without consulting with
 * the RDT team.
 * 
 * @author crecoskie
 * 
 */
public class RemoteSyncNature implements IProjectNature {

	public static final String REMOTE_NATURE_ID = "org.eclipse.ptp.rdt.sync.core.remoteSyncNature"; //$NON-NLS-1$

	private IProject fProject;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.resources.IProjectNature#configure()
	 */
	public void configure() throws CoreException {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.resources.IProjectNature#deconfigure()
	 */
	public void deconfigure() throws CoreException {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.resources.IProjectNature#getProject()
	 */
	public IProject getProject() {
		return fProject;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.core.resources.IProjectNature#setProject(org.eclipse.core
	 * .resources.IProject)
	 */
	public void setProject(IProject project) {
		fProject = project;
	}

	public static void addRemoteNature(IProject prj, IProgressMonitor monitor) throws CoreException {
		CProjectNature.addNature(prj, RemoteSyncNature.REMOTE_NATURE_ID, monitor);
	}

	/**
	 * Returns true if the given project has the remote nature.
	 * 
	 * @throws NullPointerException
	 *             if project is null
	 */
	public static boolean hasRemoteNature(IProject project) {
		try {
			return project.hasNature(REMOTE_NATURE_ID);
		} catch (CoreException e) {
			RDTSyncCorePlugin.log(e);
			return false;
		}
	}

}
