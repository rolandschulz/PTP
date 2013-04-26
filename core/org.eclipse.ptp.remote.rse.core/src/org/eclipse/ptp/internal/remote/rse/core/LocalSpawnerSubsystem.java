/*******************************************************************************
 * Copyright (c) 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ptp.internal.remote.rse.core;

import java.io.File;
import java.io.IOException;

import org.eclipse.cdt.utils.spawner.ProcessFactory;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.subsystems.IConnectorService;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
import org.eclipse.rse.services.shells.IHostShell;

/**
 * Version of SpawnerSubsystem that launches local processes.
 * 
 * @author crecoskie
 * 
 */
public class LocalSpawnerSubsystem extends SpawnerSubsystem {

	protected LocalSpawnerSubsystem(IHost host, IConnectorService connectorService) {
		super(host, connectorService);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.internal.remote.rse.core.SpawnerSubsystem#initializeSubSystem(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public synchronized void initializeSubSystem(IProgressMonitor monitor) throws SystemMessageException {
		// do nothing... we're not remote so there's nothing to do here
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.internal.remote.rse.core.SpawnerSubsystem#spawnRedirected(java.lang.String, java.lang.String,
	 * java.lang.String, java.lang.String[], org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public synchronized IHostShell spawnRedirected(String cmd, String workingDirectory, String encoding, String[] envp,
			IProgressMonitor monitor) throws IOException {

		String[] cmdArray = cmd.split(" "); //$NON-NLS-1$
		File dir = new File(workingDirectory);

		// use the CDT spawner to run the command, and wrap it in an IHostShell
		// NOTE: PTYs behave weirdly in the local case (can look like they are at EOF when they are not)
		// and also it seems spawned processes are line buffered in the local case already, so we
		// don't use a PTY here
		Process process = ProcessFactory.getFactory().exec(cmdArray, envp, dir);
		return new LocalHostShellWrapper(process);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.internal.remote.rse.core.SpawnerSubsystem#spawnLocalRedirected(java.lang.String, java.lang.String,
	 * java.lang.String, java.lang.String[], org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public synchronized Process spawnLocalRedirected(String cmd, String workingDirectory, String encoding, String[] envp,
			IProgressMonitor monitor) throws IOException {
		String[] cmdArray = cmd.split(" "); //$NON-NLS-1$
		File dir = new File(workingDirectory);

		// use the CDT spawner to run the command, and wrap it in an IHostShell
		// NOTE: PTYs behave weirdly in the local case (can look like they are at EOF when they are not)
		// and also it seems spawned processes are line buffered in the local case already, so we
		// don't use a PTY here
		Process process = ProcessFactory.getFactory().exec(cmdArray, envp, dir);
		return process;
	}

}
