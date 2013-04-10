/*******************************************************************************
 * Copyright (c) 2008, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.rdt.sync.core.services;

import java.util.EnumSet;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ptp.rdt.sync.core.BuildScenario;
import org.eclipse.ptp.rdt.sync.core.SyncFileFilter;
import org.eclipse.ptp.rdt.sync.core.SyncFlag;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.IRemoteServices;
import org.eclipse.ptp.services.core.IServiceProvider;

/**
 * Provides synchronization services.
 */
public interface ISyncServiceProvider extends IServiceProvider {

	/**
	 * Replace the current contents of the given paths with the previous versions in the repository
	 * 
	 * @param project
	 * @param buildScenario
	 * @param path
	 * @throws CoreException
	 */
	public void checkout(IProject project, BuildScenario buildScenario, IPath[] paths) throws CoreException;

	/**
	 * Replace the current contents of the given paths with the current local copies of the remote (not necessarily the same as what
	 * is on the remote site). This is useful in merge-conflict resolution.
	 * 
	 * @param project
	 * @param buildScenario
	 * @param path
	 * @throws CoreException
	 */
	public void checkoutRemoteCopy(IProject project, BuildScenario buildScenario, IPath[] paths) throws CoreException;

	/**
	 * Close any resources (files, sockets) that were open by the sync provider for the given project. Resources not open by the
	 * provider should not be touched. This is called, for example, when a project is about to be deleted.
	 */
	public void close(IProject project);

	/**
	 * Gets the path to the configuration area on the server.
	 * 
	 * @return String
	 * @since 2.0
	 */
	public String getConfigLocation();

	/**
	 * Gets the connection to use for this service. The connection may not be
	 * open, so clients should check to make sure it is open before using it.
	 * 
	 * @return IRemoteConnection
	 */
	public IRemoteConnection getConnection();

	/**
	 * Get the build location specified by this sync service provider.
	 * 
	 * @return location
	 */
	public String getLocation();

	/**
	 * Get the current list of merge-conflicted files for the passed project and build scenario
	 * 
	 * @param project
	 * @param buildScenario
	 * @return set of files as project-relative IPaths. This may be an empty set but never null.
	 * @throws CoreException
	 *             for system-level problems retrieving merge information
	 */
	public Set<IPath> getMergeConflictFiles(IProject project, BuildScenario buildScenario) throws CoreException;

	/**
	 * Get the three parts of the merge-conflicted file (left, right, and ancestor, respectively)
	 * 
	 * @param project
	 * @param buildScenario
	 * @param file
	 * @return the three parts as strings. Either three strings (some may be empty) or null if file is not merge-conflicted.
	 * @throws CoreException
	 *             for system-level problems retrieving merge information
	 */
	public String[] getMergeConflictParts(IProject project, BuildScenario buildScenario, IFile file) throws CoreException;

	/**
	 * Get the remote connection used by this sync service provider.
	 * 
	 * @return connection
	 */
	public IRemoteConnection getRemoteConnection();

	/**
	 * Gets the provider of remote services.
	 * 
	 * @return IRemoteServices
	 */
	public IRemoteServices getRemoteServices();

	/**
	 * Set the path to the configuration area on the server.
	 * 
	 * @param configLocation
	 *            path to the configuration area
	 * @since 3.1
	 */
	public void setConfigLocation(String configLocation);

	/**
	 * Set the given file paths as resolved (merge conflict does not exist)
	 * 
	 * @param project
	 * @param buildScenario
	 * @param path
	 * @throws CoreException
	 *             for system-level problems setting the state
	 */
	public void setMergeAsResolved(IProject project, BuildScenario buildScenario, IPath[] paths) throws CoreException;

	/**
	 * Set the connection to use for this service.
	 * 
	 * @param connection
	 *            remote connection
	 * @since 3.1
	 */
	public void setRemoteToolsConnection(IRemoteConnection connection);

	/**
	 * Perform synchronization
	 * 
	 * @param project
	 *            project to sync
	 * @param delta
	 *            resources requiring synchronization
	 * @param monitor
	 *            progress monitor for monitoring or canceling sync
	 * @param syncFlags
	 *            Various flags for the sync call. For example, the sync can be
	 *            forced, either to local (from remote) or to remote (from
	 *            local). If forced, it is guaranteed to happen before
	 *            returning. Otherwise, it may happen at any time.
	 * @throws CoreException
	 *             if synchronization fails
	 */
	public void synchronize(IProject project, BuildScenario buildScenario, IResourceDelta delta, SyncFileFilter filter,
			IProgressMonitor monitor, EnumSet<SyncFlag> syncFlags) throws CoreException;
}
