/*******************************************************************************
 * Copyright (c) 2012 IBM and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.ptp.internal.remote.terminal;

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;

import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.remote.core.IRemoteConnectionManager;
import org.eclipse.remote.core.IRemoteProcess;
import org.eclipse.remote.core.IRemoteProcessBuilder;
import org.eclipse.remote.core.IRemoteServices;
import org.eclipse.remote.core.RemoteServices;
import org.eclipse.remote.core.exception.RemoteConnectionException;
import org.eclipse.swt.widgets.Display;
import org.eclipse.tm.internal.terminal.provisional.api.ITerminalControl;
import org.eclipse.tm.internal.terminal.provisional.api.Logger;
import org.eclipse.tm.internal.terminal.provisional.api.TerminalState;

class RemoteConnection extends Thread {
	private static int fgNo;

	protected static Display getStandardDisplay() {
		Display display = Display.getCurrent();
		if (display == null) {
			display = Display.getDefault();
		}
		return display;
	}

	private final ITerminalControl fControl;
	private final RemoteConnector fConn;
	private IRemoteConnection fRemoteConnection;
	private IRemoteProcess fProcess;

	protected RemoteConnection(RemoteConnector conn, ITerminalControl control) {
		super("RemoteConnection-" + fgNo++); //$NON-NLS-1$
		fControl = control;
		fConn = conn;
		fControl.setState(TerminalState.CONNECTING);
	}

	public void run() {
		try {
			IRemoteServices services = RemoteServices.getRemoteServices(fConn.getSshSettings().getRemoteServices());
			if (services != null) {
				IRemoteConnectionManager connMgr = services.getConnectionManager();
				if (connMgr != null) {
					fRemoteConnection = connMgr.getConnection(fConn.getSshSettings().getConnectionName());
				}
			}
			if (fRemoteConnection != null && !fRemoteConnection.isOpen()) {
				try {
					fRemoteConnection.open(null); // XXX
				} catch (RemoteConnectionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if (!fRemoteConnection.isOpen()) {
				return;
			}
			if ((fRemoteConnection.getRemoteServices().getCapabilities() & IRemoteServices.CAPABILITY_SUPPORTS_COMMAND_SHELL) != 0) {
				fProcess = fRemoteConnection.getCommandShell(IRemoteProcessBuilder.ALLOCATE_PTY);
			} else {
				IRemoteProcessBuilder processBuilder = fRemoteConnection.getProcessBuilder(new String[] { "/bin/bash", "-l" });
				fProcess = processBuilder.start(IRemoteProcessBuilder.ALLOCATE_PTY);
			}
			fConn.setInputStream(fProcess.getInputStream());
			fConn.setOutputStream(fProcess.getOutputStream());
			fControl.setState(TerminalState.CONNECTED);
			try {
				// read data until the connection gets terminated
				readDataForever(fConn.getInputStream());
			} catch (InterruptedIOException e) {
				// we got interrupted: we are done...
			}
		} catch (IOException e) {
			fControl.setState(TerminalState.CLOSED);
		} finally {
			// make sure the terminal is disconnected when the thread ends
			try {
				disconnect();
			} finally {
				// when reading is done, we set the state to closed
				fControl.setState(TerminalState.CLOSED);
			}
		}
	}

	public void shutdown() {
		disconnect();
	}

	private void connectFailed(String terminalText, String msg) {
		Logger.log(terminalText);
		fControl.displayTextInTerminal(terminalText);
		// fControl.setMsg(msg);
	}

	/**
	 * disconnect the session
	 */
	private void disconnect() {
		interrupt();
		synchronized (this) {
			if (fProcess != null && !fProcess.isCompleted()) {
				fProcess.destroy();
			}
		}
	}

	/**
	 * Read the data from the connection and display it in the terminal.
	 * 
	 * @param in
	 * @throws IOException
	 */
	private void readDataForever(InputStream in) throws IOException {
		// read the data
		byte bytes[] = new byte[32 * 1024];
		int n;
		// read until the thread gets interrupted....
		while ((n = in.read(bytes)) != -1) {
			fControl.getRemoteToTerminalOutputStream().write(bytes, 0, n);
		}
	}

}