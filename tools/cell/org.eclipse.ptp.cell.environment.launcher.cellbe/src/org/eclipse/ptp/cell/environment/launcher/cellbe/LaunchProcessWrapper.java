/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.cell.environment.launcher.cellbe;

import org.eclipse.ptp.remotetools.core.IRemoteExecutionManager;
import org.eclipse.ptp.remotetools.environment.control.ITargetJob;
import org.eclipse.ptp.remotetools.environment.launcher.core.ILaunchProcess;


public class LaunchProcessWrapper implements ITargetJob {

	ILaunchProcess process;
	
	public LaunchProcessWrapper(ILaunchProcess process) {
		super();
		this.process = process;
	}

	public void run(IRemoteExecutionManager manager) {
		process.run(manager);
	}

}
