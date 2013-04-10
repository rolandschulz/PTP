/*******************************************************************************
 * Copyright (c) 2011 IBM Corporation and others.
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

/**
 * Project nature for remote projects.
 */
public class RemoteSyncNature implements IProjectNature {

	public static final String NATURE_ID = "org.eclipse.ptp.rdt.sync.core.remoteSyncNature"; //$NON-NLS-1$

	/**
	 * Add the nature to a project
	 * 
	 * @param prj
	 * @param monitor
	 * @throws CoreException
	 */
	public static void addNature(IProject prj, IProgressMonitor monitor) throws CoreException {
		CProjectNature.addNature(prj, RemoteSyncNature.NATURE_ID, monitor);
	}

	/**
	 * Returns true if the given project has the remote nature.
	 * 
	 * @throws NullPointerException
	 *             if project is null
	 */
	public static boolean hasNature(IProject project) {
		try {
			return project.hasNature(NATURE_ID);
		} catch (CoreException e) {
			return false;
		}
	}

	private IProject fProject;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.resources.IProjectNature#configure()
	 */
	@Override
	public void configure() throws CoreException {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.resources.IProjectNature#deconfigure()
	 */
	@Override
	public void deconfigure() throws CoreException {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.resources.IProjectNature#getProject()
	 */
	@Override
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
	@Override
	public void setProject(IProject project) {
		fProject = project;
	}

}
