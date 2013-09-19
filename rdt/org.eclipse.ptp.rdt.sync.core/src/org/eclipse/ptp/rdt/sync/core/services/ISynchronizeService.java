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
import org.eclipse.ptp.rdt.sync.core.AbstractSyncFileFilter;
import org.eclipse.ptp.rdt.sync.core.SyncConfig;
import org.eclipse.ptp.rdt.sync.core.SyncFlag;
import org.eclipse.ptp.rdt.sync.core.exceptions.RemoteSyncException;
import org.eclipse.remote.core.IRemoteConnection;

/**
 * Provides synchronization services.
 * 
 * @since 3.0
 */
public interface ISynchronizeService extends ISynchronizeServiceDescriptor {

	/**
	 * Replace the current contents of the given paths with the previous versions in the repository
	 * 
	 * @param project
	 * @param syncConfig
	 * @param path
	 * @throws CoreException
	 */
	public void checkout(IProject project, SyncConfig syncConfig, IPath[] paths) throws CoreException;

	/**
	 * Replace the current contents of the given paths with the current local copies of the remote (not necessarily the same as what
	 * is on the remote site). This is useful in merge-conflict resolution.
	 * 
	 * @param project
	 * @param syncConfig
	 * @param path
	 * @throws CoreException
	 */
	public void checkoutRemoteCopy(IProject project, SyncConfig syncConfig, IPath[] paths) throws CoreException;

	/**
	 * Close any resources (files, sockets) that were open by the sync provider for the given project. Resources not open by the
	 * provider should not be touched. This is called, for example, when a project is about to be deleted.
	 */
	public void close(IProject project);

	/**
	 * Get the remote directory that will be used for synchronization
	 * 
	 * @return String
	 */
	public String getLocation();

	/**
	 * Get the current list of merge-conflicted files for the passed project and build scenario
	 * 
	 * @param project
	 * @param syncConfig
	 * @return set of files as project-relative IPaths. This may be an empty set but never null.
	 * @throws CoreException
	 *             for system-level problems retrieving merge information
	 */
	public Set<IPath> getMergeConflictFiles(IProject project, SyncConfig syncConfig) throws CoreException;

	/**
	 * Get the three parts of the merge-conflicted file (left, right, and ancestor, respectively)
	 * 
	 * @param project
	 * @param syncConfig
	 * @param file
	 * @return the three parts as strings. Either three strings (some may be empty) or null if file is not merge-conflicted.
	 * @throws CoreException
	 *             for system-level problems retrieving merge information
	 */
	public String[] getMergeConflictParts(IProject project, SyncConfig syncConfig, IFile file) throws CoreException;

	/**
	 * Get the remote connection used by this sync service provider.
	 * 
	 * @return connection
	 * @since 4.0
	 */
	public IRemoteConnection getRemoteConnection();

	/**
	 * Set the remote directory that will be used for synchronization
	 * 
	 * @param location
	 *            directory path
	 * @throws RuntimeException
	 *             if already set. Changing these local parameters is not currently supported but should be possible.
	 */
	public void setLocation(String location);

	/**
	 * Set the given file paths as resolved (merge conflict does not exist)
	 * 
	 * @param project
	 * @param syncConfig
	 * @param path
	 * @throws CoreException
	 *             for system-level problems setting the state
	 */
	public void setMergeAsResolved(IProject project, SyncConfig syncConfig, IPath[] paths) throws CoreException;

	/**
	 * set the remote connection used for synchronization
	 * 
	 * @param conn
	 *            remote connection
	 * @throws RuntimeException
	 *             if already set. Changing these local parameters is not currently supported but should be possible.
	 * @since 4.0
	 */
	public void setRemoteConnection(IRemoteConnection conn);

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
	public void synchronize(IProject project, SyncConfig syncConfig, IResourceDelta delta, IProgressMonitor monitor,
			EnumSet<SyncFlag> syncFlags) throws CoreException;

	/**
	 * Get SyncFileFilter. Empty if not initialized before
	 * 
	 * @param project
	 * 
	 * @return file filter
	 * @throws RemoteSyncException
	 */
	public AbstractSyncFileFilter getSyncFileFilter(IProject project);

	/**
	 * Set sync file filter for the given project
	 * 
	 * @param project
	 *            - cannot be null
	 * @param filter
	 *            generic file filter - cannot be null
	 */
	public void setSyncFileFilter(IProject project, AbstractSyncFileFilter filter);
}
