/**
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *
 */
package org.eclipse.ptp.remotetools.internal.ssh;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.eclipse.ptp.remotetools.core.IRemoteScriptExecution;
import org.eclipse.ptp.remotetools.core.RemoteProcess;
import org.eclipse.ptp.remotetools.exception.CancelException;
import org.eclipse.ptp.remotetools.exception.RemoteConnectionException;
import org.eclipse.ptp.remotetools.exception.RemoteExecutionException;


public class SSHRemoteProcess extends RemoteProcess {
	
	IRemoteScriptExecution execution;
	private InputStream errorStream;
	private InputStream inputStream;
	private OutputStream outputStream;
	
	class ExecutionCloser extends Thread {
		public void run() {
			try {
				execution.waitForEndOfExecution();
			} catch (RemoteConnectionException e) {
			} catch (RemoteExecutionException e) {
			} catch (CancelException e) {
			}
			execution.close();
		}
	}
	
	public SSHRemoteProcess(IRemoteScriptExecution execution) {
		this.execution = execution;
		try {
			this.errorStream = execution.getInputStreamFromProcessErrorStream();
			this.outputStream = execution.getOutputStreamToProcessInputStream();
			this.inputStream = execution.getInputStreamFromProcessOutputStream();
		} catch (IOException e) {
			throw new IllegalArgumentException(e.getLocalizedMessage());
		}
		new ExecutionCloser().start();
	}

	/* (non-Javadoc)
	 * @see java.lang.Process#destroy()
	 */
	public void destroy() {
		if (! execution.wasFinished()) {
			execution.cancel();
		}
	}

	/* (non-Javadoc)
	 * @see java.lang.Process#exitValue()
	 */
	public int exitValue() {
		if (execution.wasCanceled()) {
			return -1;
		} else if (execution.wasFinished()) {
			return execution.getReturnCode();
		} else {
			throw new IllegalThreadStateException();
		}
	}

	/* (non-Javadoc)
	 * @see java.lang.Process#getErrorStream()
	 */
	public InputStream getErrorStream() {
		return errorStream;
	}

	/* (non-Javadoc)
	 * @see java.lang.Process#getInputStream()
	 */
	public InputStream getInputStream() {
		return inputStream;
	}

	/* (non-Javadoc)
	 * @see java.lang.Process#getOutputStream()
	 */
	public OutputStream getOutputStream() {
		return outputStream;
	}

	/* (non-Javadoc)
	 * @see java.lang.Process#waitFor()
	 */
	public int waitFor() throws InterruptedException {
		try {
			execution.waitForEndOfExecution();			
		} catch (RemoteConnectionException e) {
			return -1;
		} catch (CancelException e) {
			return -1;
		}  catch (RemoteExecutionException e) {
			return -1;
		}
		return exitValue();
	}

	public IRemoteScriptExecution getRemoteExecution()
	{
		return execution;
	}
}
