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
package org.eclipse.ptp.remote.rse;

import java.io.IOException;
import java.util.List;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.ptp.remote.AbstractRemoteProcessBuilder;
import org.eclipse.ptp.remote.IRemoteConnection;
import org.eclipse.ptp.remote.IRemoteProcess;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.services.IService;
import org.eclipse.rse.services.shells.HostShellProcessAdapter;
import org.eclipse.rse.services.shells.IHostShell;
import org.eclipse.rse.services.shells.IShellService;
import org.eclipse.rse.subsystems.shells.core.subsystems.servicesubsystem.IShellServiceSubSystem;
import org.eclipse.swt.widgets.Display;

public class RSEProcessBuilder extends AbstractRemoteProcessBuilder {
	private final static 	String EXIT_CMD = "exit"; //$NON-NLS-1$
	private final static 	String CMD_DELIMITER = ";"; //$NON-NLS-1$

	public RSEProcessBuilder(IRemoteConnection conn, List<String> command) {
		super(conn, command);
	}
	
	public RSEProcessBuilder(IRemoteConnection conn, String... command) {
		super(conn, command);
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
		
		remoteCmd += CMD_DELIMITER + EXIT_CMD;
		
		IShellService shellService = (IShellService) getConnectedRemoteService();
		
		if (shellService == null) {
			throw new IOException("Remote service not found");
		}
		
		// This is necessary because runCommand does not actually run the command right now.
		String env[] = new String[0];
		IHostShell hostShell = shellService.launchShell("", env,new NullProgressMonitor()); //$NON-NLS-1$
		hostShell.writeToShell(remoteCmd);
		
		Process p = new HostShellProcessAdapter(hostShell);
		return new RSEProcess(p);
	}

	private IService getConnectedRemoteService() {
		IHost currentConnection = ((RSEConnection)connection()).getHost();
		if (currentConnection != null) {
			ISubSystem[] subSystems = currentConnection.getSubSystems();
			ISubSystem subSystem = null;
			for (ISubSystem sub : subSystems) {
				if (sub instanceof IShellServiceSubSystem) {
					subSystem = sub;
					break;
				}
			}
			
			final ISubSystem ss = subSystem;
			// Need to run this in the UI thread
			Display.getDefault().syncExec(new Runnable()
			{
				public void run()
				{	try {
						ss.connect(new NullProgressMonitor(), false);
					} catch (Exception e) {
						// Ignore
						e.printStackTrace();
					}
				}
			});
			
			if(!subSystem.isConnected()) {
				return null;
			}
			
			return ((IShellServiceSubSystem)subSystem).getShellService();
		}
		return null;
	}
}