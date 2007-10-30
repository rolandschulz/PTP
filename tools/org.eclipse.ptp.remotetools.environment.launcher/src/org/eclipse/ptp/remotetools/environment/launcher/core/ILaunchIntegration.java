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
package org.eclipse.ptp.remotetools.environment.launcher.core;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.ptp.remotetools.core.IRemoteExecutionManager;
import org.eclipse.ptp.remotetools.exception.CancelException;


/**
 * Provides implementation of additional operations that are required to launch the application.
 * The most important purpose is to provide the command line(s) that launch the application.
 * It has the opportunity to collaborate with customization on any step of the launch.
 * @author Daniel Felix Ferber
 */
public interface ILaunchIntegration {
	public void prepareUploadWorkingDir() throws CoreException, CancelException;
	public void finishUploadWorkingDir() throws CoreException, CancelException;
	public void prepareApplication() throws CoreException, CancelException;
	public void finalizeApplication() throws CoreException, CancelException;
	public void finalizeWorkingDir() throws CoreException, CancelException;	
	public void finalizeCleanup() throws CoreException, CancelException;	
	
	public void prepareLaunch() throws CoreException, CancelException;
	public void finalizeLaunch() throws CoreException, CancelException;
	
	/**
	 * The command line used to launch the application.
	 * Used to customize how the application shall be launched.
	 * Typically, the command line also launches a debugger or a profiler tool.
	 * @param applicationFullPath Full path where the application executable is stored on the remote host.
	 * @param arguments Parameters that shall be passed to the application.
	 * @return An array of strings, each element is a line of the script.
	 * @throws CoreException
	 */
	public String [] createLaunchScript(String applicationFullPath, String[] arguments) throws CoreException;
	/**
	 * Query if the application shall be launched. 
	 * By default, the launcher uploads and executes the application.
	 * Return false to implement an "attach to remote process" feature.
	 * @return true/false
	 * @throws CoreException
	 */
	public boolean getDoLaunchApplication() throws CoreException;
	
	/**
	 * Notifies the launch integration that the lauch has just started.
	 * @throws CoreException
	 */
	public void start() throws CoreException;
	/**
	 * Notifies the launch integration that the lauch is going to finish.
	 * @throws CoreException
	 */
	public void finish() throws CoreException;
	/**
	 * Notifies the launch integration which execution manager is being used to
	 * run commands on the remote host.
	 * @throws CoreException
	 */

	public void setExecutionManager(IRemoteExecutionManager manager);
	/**
	 * Notifies the launch integration that the lauch is going to clean up the remote working directory.
	 * @throws CoreException
	 */

	public void cleanup();
}
