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

import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.ptp.remotetools.core.IRemoteExecutionManager;
import org.eclipse.ptp.remotetools.environment.launcher.data.ExecutionConfiguration;
import org.eclipse.ptp.remotetools.environment.launcher.data.ExecutionResult;


/**
 * Definition of the remote launch.
 * @author Daniel Felix Ferber
 *
 */
public interface ILaunchProcess  {
	public void addProgressListener(ILaunchProgressListener progressListener);
	public void removeProgressListener(ILaunchProgressListener progressListener);
	
	public void setLaunchObserver(ILaunchObserver launchObserver);
	
	public ILaunchObserver getObserver();
	public ILaunchIntegration getLaunchIntegration();
	public ExecutionConfiguration getConfiguration();
	public IRemoteExecutionManager getExecutionManager();
	public ILaunchConfiguration getLaunchConfiguration();
	
	public int getCurrentProgress();
	public ExecutionResult getFinalResult();

	public ILaunch getLaunch();
	
	public void run(IRemoteExecutionManager manager);
	public void markAsCanceled();
	
	public void showProcessConsole();
	public void showLaunchConsole();

}
