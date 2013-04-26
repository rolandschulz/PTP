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

import java.io.InputStream;
import java.io.OutputStream;

import org.eclipse.ptp.remote.core.IRemoteProcess;

/**
 * 
 * Implementation of IRemoteProcess that wraps a local process
 * 
 * @author crecoskie
 *
 */
public class LocalProcessWrapper implements IRemoteProcess {

	protected Process fProcess;
	protected ProcessMonitoringThread fMonitoringThread;
	
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
	
	
	@SuppressWarnings("unused")
	private LocalProcessWrapper() {		
	}
	
	public LocalProcessWrapper(Process process) {
		if(process == null) {
			throw new IllegalArgumentException("process argument to LocalProcessWrapper must not be null"); //$NON-NLS-1$
		}
		
		fProcess = process;
		fMonitoringThread = new ProcessMonitoringThread(process);
		fMonitoringThread.start();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.core.IRemoteProcess#destroy()
	 */
	public void destroy() {
		fProcess.destroy();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.core.IRemoteProcess#exitValue()
	 */
	public int exitValue() {
		return fProcess.exitValue();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.core.IRemoteProcess#getErrorStream()
	 */
	public InputStream getErrorStream() {
		return fProcess.getErrorStream();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.core.IRemoteProcess#getInputStream()
	 */
	public InputStream getInputStream() {
		return fProcess.getInputStream();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.core.IRemoteProcess#getOutputStream()
	 */
	public OutputStream getOutputStream() {
		return fProcess.getOutputStream();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.core.IRemoteProcess#waitFor()
	 */
	public int waitFor() throws InterruptedException {
		return fProcess.waitFor();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.core.IRemoteProcess#isCompleted()
	 */
	public boolean isCompleted() {
		return !fMonitoringThread.isAlive();
	}

}
