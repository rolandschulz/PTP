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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.ptp.internal.rdt.sync.core.RDTSyncCorePlugin;

/**
 * Project nature for remote projects.
 * 
 * @since 3.0
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
		addNature(prj, RemoteSyncNature.NATURE_ID, monitor);
	}

	/**
	 * Utility method for adding a nature to a project.
	 * 
	 * @param project
	 *            the project to add the nature
	 * @param natureId
	 *            the id of the nature to assign to the project
	 * @param monitor
	 *            a progress monitor to indicate the duration of the operation,
	 *            or <code>null</code> if progress reporting is not required.
	 * 
	 */
	public static void addNature(IProject project, String natureId, IProgressMonitor monitor) throws CoreException {
		try {
			if (monitor == null) {
				monitor = new NullProgressMonitor();
			}
			IProjectDescription description = project.getDescription();
			String[] prevNatures = description.getNatureIds();
			for (String prevNature : prevNatures) {
				if (natureId.equals(prevNature)) {
					return;
				}
			}
			String[] newNatures = new String[prevNatures.length + 1];
			System.arraycopy(prevNatures, 0, newNatures, 0, prevNatures.length);
			newNatures[prevNatures.length] = natureId;
			description.setNatureIds(newNatures);
			project.setDescription(description, monitor);
		} catch (CoreException e) {
			RDTSyncCorePlugin.log(e);
		}

		finally {
			monitor.done();
		}
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

	/**
	 * Utility method for removing a project nature from a project.
	 * 
	 * @param project
	 *            the project to remove the nature from
	 * @param natureId
	 *            the nature id to remove
	 * @param monitor
	 *            a progress monitor to indicate the duration of the operation,
	 *            or <code>null</code> if progress reporting is not required.
	 */
	public static void removeNature(IProject project, String natureId, IProgressMonitor monitor) throws CoreException {
		IProjectDescription description = project.getDescription();
		String[] prevNatures = description.getNatureIds();
		List<String> newNatures = new ArrayList<String>(Arrays.asList(prevNatures));
		newNatures.remove(natureId);
		description.setNatureIds(newNatures.toArray(new String[newNatures.size()]));
		project.setDescription(description, monitor);
	}

	private IProject fProject;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.resources.IProjectNature#configure()
	 */
	@Override
	public void configure() throws CoreException {
		// nothing to do
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.resources.IProjectNature#deconfigure()
	 */
	@Override
	public void deconfigure() throws CoreException {
		// nothing to do
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
