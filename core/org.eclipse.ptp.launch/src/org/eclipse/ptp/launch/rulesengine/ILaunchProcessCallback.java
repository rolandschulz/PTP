/******************************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *
 *****************************************************************************/
package org.eclipse.ptp.launch.rulesengine;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.remote.core.IRemoteFileManager;

/**
 * TODO NEEDS TO BE DOCUMENTED
 * 
 * @since 5.0
 */
public interface ILaunchProcessCallback {
	/**
	 * Get a file manager for handling local files
	 * 
	 * @param configuration
	 *            launch configuration
	 * @return local file manager
	 * @throws CoreException
	 *             if no local file manager can ge obtained
	 */
	public IRemoteFileManager getLocalFileManager(ILaunchConfiguration configuration) throws CoreException;

	/**
	 * Get a file manager for handling remote files. The connection used to manipulate the files will be obtained from the launch
	 * configuration. User's can cancel the progress monitor, in which case null will be returned.
	 * 
	 * @param configuration
	 *            launch configuration
	 * @param monitor
	 *            progress monitor
	 * @return remote file manager or null if progress monitor is cancelled
	 * @throws CoreException
	 *             if a remote file manager can't be obtained
	 */
	public IRemoteFileManager getRemoteFileManager(ILaunchConfiguration configuration, IProgressMonitor monitor)
			throws CoreException;

	public void addSynchronizationRule(ISynchronizationRule downloadBackRule);
}
