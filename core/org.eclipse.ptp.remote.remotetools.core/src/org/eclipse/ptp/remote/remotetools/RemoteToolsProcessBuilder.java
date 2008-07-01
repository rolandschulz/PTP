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
package org.eclipse.ptp.remote.remotetools;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.eclipse.ptp.remote.AbstractRemoteProcessBuilder;
import org.eclipse.ptp.remote.IRemoteProcess;
import org.eclipse.ptp.remotetools.core.IRemoteExecutionManager;
import org.eclipse.ptp.remotetools.core.IRemoteExecutionTools;
import org.eclipse.ptp.remotetools.core.IRemoteScript;
import org.eclipse.ptp.remotetools.core.RemoteProcess;

public class RemoteToolsProcessBuilder extends AbstractRemoteProcessBuilder {
	private RemoteToolsConnection connection;

	public RemoteToolsProcessBuilder(RemoteToolsConnection conn, List<String> command) {
		super(conn, command);
		this.connection = conn;
	}
	
	public RemoteToolsProcessBuilder(RemoteToolsConnection conn, String... command) {
		super(conn, command);
		this.connection = conn;
	}
	

	private String spaceEscapify(String inputString) {
		if(inputString == null)
			return null;
		return inputString.replaceAll(" ", "\\\\ "); //$NON-NLS-1$ //$NON-NLS-2$
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
		
		String remoteCmd = "";
		
		for (int i = 0; i < cmdArgs.size(); i++) {
			if (i > 0) {
				remoteCmd += " ";
			}
			remoteCmd += spaceEscapify(cmdArgs.get(i));
		}
		
		try {
			IRemoteExecutionManager exeMgr = connection.createExecutionManager();
			IRemoteExecutionTools exeTools = exeMgr.getExecutionTools();
			IRemoteScript script = exeTools.createScript();
			script.setScript(remoteCmd);
			for (Entry<String,String>  entry : environment().entrySet()) {
				script.addEnvironment(entry.getKey()+"="+entry.getValue());
			}

			RemoteProcess process = exeTools.executeProcess(script);
			return new RemoteToolsProcess(process);
		} catch (Exception e) {
			throw new IOException(e.getMessage());
		}
	}
}