/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.remote.rse.core;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.ptp.remote.core.AbstractRemoteProcessBuilder;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.IRemoteProcess;
import org.eclipse.ptp.remote.rse.core.messages.Messages;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
import org.eclipse.rse.services.shells.IHostShell;
import org.eclipse.rse.services.shells.IShellService;

public class RSEProcessBuilder extends AbstractRemoteProcessBuilder {
	private final static 	String EXIT_CMD = "exit"; //$NON-NLS-1$
	private final static 	String CMD_DELIMITER = ";"; //$NON-NLS-1$
	
	private RSEConnection connection;
	private Map<String, String> remoteEnv = new HashMap<String, String>();

	public RSEProcessBuilder(IRemoteConnection conn, List<String> command) {
		super(conn, command);
		this.connection = (RSEConnection)conn;
		
		IShellService shellService = connection.getRemoteShellService();
		
		try {
			String[] env = shellService.getHostEnvironment();
			populateEnvironmentMap(env);
			
		} catch (SystemMessageException e) {
		}
	}

	/**
	 * Convert environment strings into a map
	 * 
	 * @param env array containing environment variables
	 */
	private void populateEnvironmentMap(String[] env) {
		// env is of the form "var=value"
		
		for(int k = 0; k < env.length; k++) {
			
			// get the index of the "="
			int pos = env[k].indexOf("="); //$NON-NLS-1$
			assert(pos != -1);
			
			remoteEnv.put(env[k].substring(0, pos), env[k].substring(pos + 1));
		}
		
	}
	
	/**
	 * Convert environment map back to environment strings.
	 * 
	 * @return array of environment variables
	 */
	private String[] getEnvironment() {
		String[] env = new String[remoteEnv.size()];
		int pos = 0;
		for (Map.Entry<String, String> entry: remoteEnv.entrySet()) {
			env[pos++] = entry.getKey() + "=" + entry.getValue(); //$NON-NLS-1$
		}
		return env;
	}

	public RSEProcessBuilder(IRemoteConnection conn, String... command) {
		this(conn, Arrays.asList(command));
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.AbstractRemoteProcessBuilder#environment()
	 */
	@Override
	public Map<String, String> environment() {
		return remoteEnv;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.IRemoteProcessBuilder#start()
	 */
	public IRemoteProcess start() throws IOException {
		// The exit command is called to force the remote shell to close after our command 
		// is executed. This is to prevent a running process at the end of the debug session.
		// See Bug 158786.
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
		
		remoteCmd += CMD_DELIMITER + EXIT_CMD;
		
		IShellService shellService = connection.getRemoteShellService();
		if (shellService == null) {
			throw new IOException(Messages.RSEProcessBuilder_0);
		}
		
		// This is necessary because runCommand does not actually run the command right now.
		IHostShell hostShell = null;
		try {
			String initialDir = ""; //$NON-NLS-1$
			if (directory() != null) {
				initialDir = directory().toURI().getPath();
			}
			hostShell = shellService.runCommand(initialDir, remoteCmd, getEnvironment(), new NullProgressMonitor());
		} catch (SystemMessageException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		
		return new RSEProcess(hostShell, redirectErrorStream());
	}
	
	private String spaceEscapify(String inputString) {
		if(inputString == null)
			return null;
		return inputString.replaceAll(" ", "\\\\ "); //$NON-NLS-1$ //$NON-NLS-2$
	}

}