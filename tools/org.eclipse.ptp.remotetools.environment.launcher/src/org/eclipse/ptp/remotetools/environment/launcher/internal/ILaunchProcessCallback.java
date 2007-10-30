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
package org.eclipse.ptp.remotetools.environment.launcher.internal;

import java.io.PrintWriter;

import org.eclipse.ptp.remotetools.core.IRemoteExecutionManager;
import org.eclipse.ptp.remotetools.environment.launcher.data.ExecutionConfiguration;
import org.eclipse.ptp.remotetools.environment.launcher.data.ISynchronizationRule;



public interface ILaunchProcessCallback {
	public PrintWriter getOutputWriter();
	public PrintWriter getErrorWriter();
	public ExecutionConfiguration getConfiguration();
	public IRemoteExecutionManager getExecutionManager();
	public void addSynchronizationRule(ISynchronizationRule downloadBackRule);
}
