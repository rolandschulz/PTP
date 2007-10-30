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
import org.eclipse.ptp.remotetools.environment.launcher.internal.RemoteLaunchProcess;
import org.eclipse.ptp.remotetools.exception.CancelException;


/**
 * A dummy void launch integration that can be used as base class for further specialization.
 * @author Daniel Felix Ferber
 */
public class NullLaunchIntegration implements ILaunchIntegration {

	public String[] createLaunchScript(String applicationFullPath, String[] arguments) throws CoreException {
		String result [] = new String [1];
		result[0] = RemoteLaunchProcess.createCommandLine(applicationFullPath, arguments);
		return result;
	}

	public void cleanup() {
	}

	public void finalizeCleanup() throws CoreException, CancelException {
	}

	public void finalizeApplication() throws CoreException, CancelException {
	}

	public void finalizeWorkingDir() throws CoreException, CancelException {
	}

	public void finish() throws CoreException {
	}

	public void finishUploadWorkingDir() throws CoreException, CancelException {
	}

	public void prepareApplication() throws CoreException, CancelException {
	}

	public void prepareUploadWorkingDir() throws CoreException, CancelException {
	}

	public void setExecutionManager(IRemoteExecutionManager manager) {
	}

	public void start() throws CoreException {
	}

	public boolean getDoLaunchApplication() throws CoreException {
		return true;
	}

	public void finalizeLaunch() throws CoreException, CancelException {
	}

	public void prepareLaunch() throws CoreException, CancelException {
	}

}
