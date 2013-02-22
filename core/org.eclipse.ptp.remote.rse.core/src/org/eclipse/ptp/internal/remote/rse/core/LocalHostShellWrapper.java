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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;

import org.eclipse.ptp.remote.rse.core.RSEAdapterCorePlugin;
import org.eclipse.rse.internal.services.local.shells.LocalShellOutputReader;
import org.eclipse.rse.services.shells.AbstractHostShell;
import org.eclipse.rse.services.shells.IHostShell;
import org.eclipse.rse.services.shells.IHostShellOutputReader;

/**
 * 
 * Adapts a local process to an IHostShell
 * @author crecoskie
 *
 */
@SuppressWarnings("restriction")
public class LocalHostShellWrapper extends AbstractHostShell implements IHostShell {

	protected static final String NEWLINE = System.getProperty("line.separator"); //$NON-NLS-1$
	protected Process fProcess;
	protected ProcessMonitoringThread fProcessMonitoringThread;
	protected LocalShellOutputReader fStdoutHandler;
	protected LocalShellOutputReader fStderrHandler;
	protected BufferedReader fOutputReader, fErrReader;
	
	protected class ProcessMonitoringThread extends Thread {
		protected Process fProcessToMonitor;
		public ProcessMonitoringThread(Process process) {
			fProcessToMonitor = process;
		}
		/* (non-Javadoc)
		 * @see java.lang.Thread#run()
		 */
		@Override
		public void run() {
			try {
				fProcessToMonitor.waitFor();
			} catch (InterruptedException e) {
				RSEAdapterCorePlugin.log(e);
			}
		}
		
		
	}
	
	public LocalHostShellWrapper(Process process) {
		fProcess = process;
		fProcessMonitoringThread = new ProcessMonitoringThread(process);
		fProcessMonitoringThread.start();
		fOutputReader = new BufferedReader(new InputStreamReader(fProcess.getInputStream()));
		fErrReader = new BufferedReader(new InputStreamReader(fProcess.getErrorStream()));
		fStdoutHandler = new LocalShellOutputReader(this, fOutputReader, false);
		fStderrHandler = new LocalShellOutputReader(this, fErrReader, true);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.services.shells.IHostShell#isActive()
	 */
	public boolean isActive() {
		return fProcessMonitoringThread.isAlive();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.services.shells.IHostShell#writeToShell(java.lang.String)
	 */
	public void writeToShell(String command) {
		OutputStream outputStream = fProcess.getOutputStream();
		try {
			outputStream.write((command + NEWLINE).getBytes());
		} catch (IOException e) {
			RSEAdapterCorePlugin.log(e);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.services.shells.IHostShell#getStandardOutputReader()
	 */
	public IHostShellOutputReader getStandardOutputReader() {
		return fStdoutHandler;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.services.shells.IHostShell#getStandardErrorReader()
	 */
	public IHostShellOutputReader getStandardErrorReader() {
		return fStderrHandler;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.services.shells.IHostShell#exit()
	 */
	public void exit() {
		writeToShell("exit"); //$NON-NLS-1$
	}
	
	

}
