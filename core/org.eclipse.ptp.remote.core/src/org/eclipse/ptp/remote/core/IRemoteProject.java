/*******************************************************************************
 * Copyright (c) 2011 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.remote.core;

import java.net.URI;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;

/**
 * Interface to a remote project. There are currently two types of remote projects: fully remote and synchronized. This interface
 * provides a common mechanism for accessing project information from either type.
 * 
 * Usage:
 * 
 * <code>
 * 	IRemoteProject remoteProj = (IRemoteProject)project.getAdapter(IRemoteProject.class);
 * 	if (remoteProj != null) {
 * 		URI location = remoteProj.getDefaultLocationURI(resource);
 * 		...
 * 	}
 * </code>
 * 
 * @author greg
 * 
 */
public interface IRemoteProject {
	/**
	 * Get the active location URI of the resource in the remote project. Returns null if the URI can't be obtained (@see
	 * {@link IResource#getLocationURI()}).
	 * 
	 * For fully remote projects, this is just the URI of the remote resource. For synchronized projects, this is the URI of the
	 * resource from the active synchronization target.
	 * 
	 * @param resource
	 *            target resource
	 * @return URI or null if URI can't be obtained
	 */
	public URI getActiveLocationURI(IResource resource);

	/**
	 * Get the platform project corresponding to the remote project
	 * 
	 * @return IProject
	 */
	public IProject getProject();

	/**
	 * Set the platform project
	 * 
	 * @param project
	 *            platform project corresponding to this remote project
	 */
	public void setProject(IProject project);
}