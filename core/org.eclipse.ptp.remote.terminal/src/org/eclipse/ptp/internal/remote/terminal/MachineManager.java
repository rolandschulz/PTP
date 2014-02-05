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
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.ptp.internal.remote.terminal.scripts.Scripts;
import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.remote.core.IRemoteProcess;
import org.eclipse.remote.core.IRemoteProcessBuilder;

/**
 * This class is used to keep track of machines, how to
 * write to their screens, and their history threads.
 * 
 * @author Steven R. Brandt
 *
 */
public class MachineManager {

	public static class MachineInfo {
		public boolean hasTailF;
		public String shell;
		public OutputStream openStream;
		public Thread history;
		public boolean isBash, isCsh, init;
	}

	private static Map<String, MachineInfo> machineInfoTable = new HashMap<String, MachineInfo>();

	private static synchronized MachineInfo getMachineInfo(String connectionName) {
		MachineInfo minfo = machineInfoTable.get(connectionName);
		if (minfo == null) {
			minfo = new MachineInfo();
			machineInfoTable.put(connectionName, minfo);
		}
		return minfo;
	}

	/**
	 * Obtain the OutputStream for the machine/connection. May be
	 * null or closed.
	 * 
	 * @param address
	 *            the address of the machine you want the stream for
	 * @return the OutputStream connected to the terminal for the machine at this address
	 */
	public static OutputStream getOutputStream(String address) {
		MachineInfo minfo = getMachineInfo(address);
		return minfo.openStream;
	}

	/**
	 * Set the OutputStream for the machine/connection.
	 * 
	 * @param address
	 *            the address of the machine you to set the stream for
	 * @param out
	 *            the OutputStream for that machines terminal
	 */
	public static void setOutputStream(String address, OutputStream out) {
		MachineInfo minfo = getMachineInfo(address);
		minfo.openStream = out;
	}

	/**
	 * Determines what the shell is, whether the tailf command is available, and starts the history thread.
	 * 
	 * @param remoteConnection
	 *            the connection for which the MachineInfo data structure is to be setup
	 * @return the MachineData structure
	 * @throws IOException
	 */
	public static MachineInfo initializeMachine(IRemoteConnection remoteConnection) throws IOException {
		final String address = remoteConnection.getAddress();
		MachineInfo minfo = getMachineInfo(address);

		if (!minfo.init) {
			minfo.init = true;
			List<String> whichCommand = new ArrayList<String>();
			whichCommand.add("which"); //$NON-NLS-1$
			whichCommand.add("tailf"); //$NON-NLS-1$
			IRemoteProcessBuilder processBuilder =
					remoteConnection.getProcessBuilder(whichCommand);
			String remoteShell = processBuilder.environment().get("SHELL"); //$NON-NLS-1$
			IRemoteProcess whichResult = processBuilder.start();
			try {
				if (whichResult.waitFor() == 0) {
					minfo.hasTailF = true;
				} else {
					minfo.hasTailF = false;
				}
			} catch (InterruptedException e) {
				Activator.log(e);
				minfo.hasTailF = false;
			}
			if (remoteShell != null)
				minfo.shell = remoteShell;
			else
				minfo.shell = "/bin/bash"; //$NON-NLS-1$
			minfo.isBash = minfo.shell.contains("bash"); //$NON-NLS-1$
			minfo.isCsh = minfo.shell.contains("csh"); // csh or tcsh //$NON-NLS-1$
		}

		final String name = remoteConnection.getAddress();
		if (minfo.history == null || !minfo.history.isAlive()) {

			final List<String> tailCommand = new ArrayList<String>();
			// The "tailf" or "tail -f" command doesn't terminate, but continually
			// reads from a file as it gets updated.
			if (minfo.isCsh) {
				tailCommand.add("perl"); //$NON-NLS-1$
				tailCommand.add("-e"); //$NON-NLS-1$
				tailCommand.add(Scripts.WATCH_CSH_HISTORY_PERL_SCRIPT);
			} else if (minfo.hasTailF) {
				tailCommand.add("tailf"); //$NON-NLS-1$
			} else {
				tailCommand.add("tail"); //$NON-NLS-1$
				tailCommand.add("-f"); //$NON-NLS-1$
			}
			if (minfo.isBash)
				tailCommand.add(".bash_history"); //$NON-NLS-1$
			else
				tailCommand.add(".history"); //$NON-NLS-1$

			final IRemoteProcessBuilder builder =
					remoteConnection.getProcessBuilder(tailCommand);

			// This thread reads commands as they arrive
			// from the tail command just created.
			Thread commands = new Thread() {
				public void run() {
					IRemoteProcess remoteProc;
					InputStream in = null;
					try {
						builder.redirectErrorStream();
						remoteProc = builder.start();
						in = remoteProc.getInputStream();
						remoteProc.getOutputStream().close();
						StringBuilder sb = new StringBuilder();
						while (true) {
							int c = in.read();
							if (c < 0)
								break;
							char ch = (char) c;
							sb.append(ch);
							if (ch == 'K') {
								// When you type Ctrl-C in csh, it inserts \033[K in the log.
								int n = sb.length();
								if (n >= 3 && sb.charAt(n - 2) == '[' && sb.charAt(n - 3) == '\033') {
									sb.setLength(n - 3);
								}
							} else if (ch == '\n') {
								String s = sb.toString().trim();
								sb.setLength(0);
								if (s.startsWith("#")) { //$NON-NLS-1$
									; // Ignore comments
								} else if (s.startsWith("/bin/bash -l -c 'echo \"PID=$$")) { //$NON-NLS-1$
									; // Ignore commands sent by PTP
								} else {
									TerminalInfoView.addToHistory(name, s);
								}
							}
						}
					} catch (IOException e) {
						Activator.log(e);
					} finally {
						try {
							if (in != null)
								in.close();
						} catch (IOException e) {
							Activator.log(e);
						}
					}
				}
			};
			commands.start();
			minfo.history = commands;
		}
		return minfo;
	}
}
