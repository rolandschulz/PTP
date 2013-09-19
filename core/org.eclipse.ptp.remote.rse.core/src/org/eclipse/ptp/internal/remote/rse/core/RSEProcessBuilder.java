/*******************************************************************************
 * Copyright (c) 2007, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.internal.remote.rse.core;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.ptp.internal.remote.rse.core.messages.Messages;
import org.eclipse.remote.core.AbstractRemoteProcessBuilder;
import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.remote.core.IRemoteFileManager;
import org.eclipse.remote.core.IRemoteProcess;
import org.eclipse.rse.connectorservice.dstore.DStoreConnectorService;
import org.eclipse.rse.core.subsystems.IConnectorService;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.internal.connectorservice.local.LocalConnectorService;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
import org.eclipse.rse.services.shells.IHostShell;
import org.eclipse.rse.services.shells.IShellService;

@SuppressWarnings("restriction")
public class RSEProcessBuilder extends AbstractRemoteProcessBuilder {
	private final static String EXIT_CMD = "exit"; //$NON-NLS-1$
	private final static String CMD_DELIMITER = ";"; //$NON-NLS-1$

	private final RSEConnection fConnection;
	private final RSEFileManager fFileMgr;

	private Map<String, String> fRemoteEnv = new HashMap<String, String>();

	/**
	 * @since 4.0
	 */
	public RSEProcessBuilder(IRemoteConnection conn, IRemoteFileManager fileMgr, List<String> command) {
		super(command);
		fConnection = (RSEConnection) conn;
		fFileMgr = (RSEFileManager) fileMgr;
		fRemoteEnv = new HashMap<String, String>(conn.getEnv());
	}

	/**
	 * @since 4.0
	 */
	public RSEProcessBuilder(IRemoteConnection conn, IRemoteFileManager fileMgr, String... command) {
		this(conn, fileMgr, Arrays.asList(command));
	}

	/**
	 * Convert environment map back to environment strings.
	 * 
	 * @return array of environment variables
	 */
	private String[] getEnvironment() {
		String[] env = new String[fRemoteEnv.size()];
		int pos = 0;
		for (Map.Entry<String, String> entry : fRemoteEnv.entrySet()) {
			env[pos++] = entry.getKey() + "=" + entry.getValue(); //$NON-NLS-1$
		}
		return env;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.remote.AbstractRemoteProcessBuilder#environment()
	 */
	@Override
	public Map<String, String> environment() {
		return fRemoteEnv;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.remote.core.AbstractRemoteProcessBuilder#getSupportedFlags
	 * ()
	 */
	@Override
	public int getSupportedFlags() {
		return NONE;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.remote.IRemoteProcessBuilder#start(int)
	 */
	@Override
	public IRemoteProcess start(int flags) throws IOException {
		List<String> cmdArgs = command();
		if (cmdArgs.size() < 1) {
			throw new IndexOutOfBoundsException();
		}

		String remoteCmd = ""; //$NON-NLS-1$

		for (int i = 0; i < cmdArgs.size(); i++) {
			if (i > 0) {
				remoteCmd += " "; //$NON-NLS-1$
			}
			remoteCmd += spaceEscapify(cmdArgs.get(i));
		}

		IHostShell hostShell = null;
		try {
			String initialDir = ""; //$NON-NLS-1$
			if (directory() != null) {
				initialDir = directory().toURI().getPath();
			}

			SpawnerSubsystem subsystem = getSpawnerSubsystem();

			if (subsystem instanceof LocalSpawnerSubsystem) {
				Process process = subsystem.spawnLocalRedirected(remoteCmd, initialDir, null, getEnvironment(),
						new NullProgressMonitor());
				return new LocalProcessWrapper(process);
			}

			else {
				if (subsystem != null) {
					hostShell = subsystem.spawnRedirected(remoteCmd, initialDir, null, getEnvironment(), new NullProgressMonitor());

					if (hostShell == null) {
						// fall back to old method of using RSE
						hostShell = launchCommandWithRSE(remoteCmd, initialDir);
					}
				} else {
					// fall back to old method of using RSE
					hostShell = launchCommandWithRSE(remoteCmd, initialDir);
				}
			}
		} catch (SystemMessageException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}

		return new RSEProcess(hostShell, redirectErrorStream());
	}

	private IHostShell launchCommandWithRSE(String remoteCmd, String initialDir) throws IOException, SystemMessageException {
		// The exit command is called to force the remote shell to close after
		// our command
		// is executed. This is to prevent a running process at the end of the
		// debug session.
		// See Bug 158786.
		IHostShell hostShell;
		remoteCmd += CMD_DELIMITER + EXIT_CMD;

		IShellService shellService = fConnection.getRemoteShellService();
		if (shellService == null) {
			throw new IOException(Messages.RSEProcessBuilder_0);
		}
		hostShell = shellService.runCommand(initialDir, remoteCmd, getEnvironment(), new NullProgressMonitor());
		return hostShell;
	}

	private SpawnerSubsystem getSpawnerSubsystem() {
		IConnectorService connectorService = getDStoreConnectorService();

		if (connectorService == null) {
			// try getting the local connector service
			connectorService = getLocalConnectorService();
			if (connectorService == null) {
				return null;
			}
		}

		ISubSystem subsystems[] = connectorService.getSubSystems();

		for (ISubSystem subsystem : subsystems) {
			if (subsystem instanceof SpawnerSubsystem) {
				return (SpawnerSubsystem) subsystem;
			}
		}

		return null;
	}

	private LocalConnectorService getLocalConnectorService() {
		for (IConnectorService service : fConnection.getHost().getConnectorServices()) {
			if (service instanceof LocalConnectorService) {
				return (LocalConnectorService) service;
			}
		}

		return null;
	}

	private DStoreConnectorService getDStoreConnectorService() {
		for (IConnectorService service : fConnection.getHost().getConnectorServices()) {
			if (service instanceof DStoreConnectorService) {
				return (DStoreConnectorService) service;
			}
		}

		return null;
	}

	private String spaceEscapify(String inputString) {
		if (inputString == null) {
			return null;
		}
		return inputString.replaceAll(" ", "\\\\ "); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.remote.core.AbstractRemoteProcessBuilder#directory()
	 */
	@Override
	public IFileStore directory() {
		if (super.directory() == null) {
			// check PWD first for UNIX systems
			String cwd = environment().get("PWD"); //$NON-NLS-1$

			// if that didn't work, try %CD% for Windows systems
			if (cwd == null) {
				cwd = environment().get("CD"); //$NON-NLS-1$
			}

			if (cwd != null) {
				return fFileMgr.getResource(cwd);
			}

		}
		return super.directory();
	}

}