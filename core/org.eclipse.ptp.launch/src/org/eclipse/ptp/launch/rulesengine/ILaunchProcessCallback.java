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
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.ptp.launch.data.ISynchronizationRule;
import org.eclipse.ptp.remote.core.IRemoteFileManager;



public interface ILaunchProcessCallback {
	//public PrintWriter getOutputWriter();
	//public PrintWriter getErrorWriter();
	//public ExecutionConfiguration getConfiguration();
	public IRemoteFileManager getLocalFileManager(ILaunchConfiguration configuration) throws CoreException;
	public IRemoteFileManager getRemoteFileManager(ILaunchConfiguration configuration) throws CoreException;
	//public IRemoteExecutionManager getExecutionManager();
	public void addSynchronizationRule(ISynchronizationRule downloadBackRule);
}
