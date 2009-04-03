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
package org.eclipse.ptp.remote.remotetools.core;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.ptp.remote.core.AbstractRemoteProcessBuilder;
import org.eclipse.ptp.remote.core.IRemoteProcess;
import org.eclipse.ptp.remotetools.core.IRemoteExecutionManager;
import org.eclipse.ptp.remotetools.core.IRemoteExecutionTools;
import org.eclipse.ptp.remotetools.core.IRemoteScript;
import org.eclipse.ptp.remotetools.core.RemoteProcess;

public class RemoteToolsProcessBuilder extends AbstractRemoteProcessBuilder {
	private RemoteToolsConnection connection;
	private Map<String, String> remoteEnv = new HashMap<String, String>();

	public RemoteToolsProcessBuilder(RemoteToolsConnection conn, List<String> command) {
		super(conn, command);
//		remoteEnv.putAll(System.getenv());
		this.connection = conn;
	}
	
	public RemoteToolsProcessBuilder(RemoteToolsConnection conn, String... command) {
		this(conn, Arrays.asList(command));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.core.AbstractRemoteProcessBuilder#environment()
	 */
	@Override
	public Map<String, String> environment() {
		return remoteEnv;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.core.IRemoteProcessBuilder#start()
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
		
		try {
			IRemoteExecutionManager exeMgr = connection.createExecutionManager();
			IRemoteExecutionTools exeTools = exeMgr.getExecutionTools();
			IRemoteScript script = exeTools.createScript();
			if(directory() != null) {
				String setWorkingDirStr = "cd " + directory().toURI().getPath(); //$NON-NLS-1$
				script.setScript(new String []{setWorkingDirStr, remoteCmd});
			} else {
				script.setScript(remoteCmd);
			}
			
			for (Entry<String,String>  entry : environment().entrySet()) {
				script.addEnvironment(entry.getKey()+"="+entry.getValue()); //$NON-NLS-1$
			}

			RemoteProcess process = exeTools.executeProcess(script);
			return new RemoteToolsProcess(process, redirectErrorStream());
		} catch (Exception e) {
			throw new IOException(e.getMessage());
		}
	}
	
	private String spaceEscapify(String inputString) {
		if(inputString == null)
			return null;
		return inputString.replaceAll(" ", "\\\\ "); //$NON-NLS-1$ //$NON-NLS-2$
	}
}