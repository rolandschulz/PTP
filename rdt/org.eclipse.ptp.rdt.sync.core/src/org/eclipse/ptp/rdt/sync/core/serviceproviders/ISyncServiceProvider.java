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
package org.eclipse.ptp.rdt.sync.core.serviceproviders;

import java.io.InputStream;
import java.util.EnumSet;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ptp.rdt.core.serviceproviders.IRemoteExecutionServiceProvider;
import org.eclipse.ptp.rdt.sync.core.BuildScenario;
import org.eclipse.ptp.rdt.sync.core.SyncFileFilter;
import org.eclipse.ptp.rdt.sync.core.SyncFlag;
import org.eclipse.ptp.remote.core.IRemoteConnection;

/**
 * Provides synchronization services.
 */
public interface ISyncServiceProvider extends IRemoteExecutionServiceProvider {

	/**
	 * Get the build location specified by this sync service provider.
	 * 
	 * @return
	 */
	public String getLocation();

	/**
	 * Get the remote connection used by this sync service provider.
	 * 
	 * @return
	 */
	public IRemoteConnection getRemoteConnection();

	/**
	 * Perform synchronization
	 *
	 * @param project
	 * 			  project to sync
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
	
	/**
	 * Close any resources (files, sockets) that were open by the sync provider. Resources not open by the provider should not be
	 * touched. This is called, for example, when a project is about to be deleted.
	 */
	public void close();

	public IFile [] getMergeConflictFiles() throws CoreException;
	public InputStream getMergeConflictRemote(IFile file) throws CoreException;
	public InputStream getMergeConflictAncestor(IFile file) throws CoreException;	
}
