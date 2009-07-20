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
package org.eclipse.ptp.remote.internal.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import org.eclipse.ptp.remote.core.AbstractRemoteProcess;
import org.eclipse.ptp.remote.core.NullInputStream;

public class LocalProcess extends AbstractRemoteProcess {
	private static int refCount = 0;

	private Process localProcess;
	private InputStream procStdout;
	private InputStream procStderr;
	private Thread stdoutReader;
	private Thread stderrReader;

	/**
	 * Thread to merge stdout and stderr. Keeps refcount so that output stream
	 * is not closed too early.
	 * 
	 * @author greg
	 *
	 */
	private class ProcOutputMerger implements Runnable {
		private final static int BUF_SIZE = 8192;
		
		private InputStream input;
		private OutputStream output;
		
		public ProcOutputMerger(InputStream input, OutputStream output) {
			this.input = input;
			this.output = output;
			synchronized (this.output) {
				refCount++;
			}
		}
		
		public void run() {
			int len;
			byte b[] = new byte[BUF_SIZE];
			
			try {
				while ((len = input.read(b)) > 0) {
					output.write(b, 0, len);
				}
			} catch (IOException e) {
			}
			synchronized (output) {
				if (--refCount == 0) {
					try {
						output.close();
					} catch (IOException e) {
					}
				}
			}
		}
	}
	
	public LocalProcess(Process proc, boolean merge) throws IOException {
		localProcess = proc;
		
		if (merge) {
			PipedOutputStream pipedOutput = new PipedOutputStream();
			
			procStderr = new NullInputStream();
			procStdout = new PipedInputStream(pipedOutput);

			stderrReader = new Thread(new ProcOutputMerger(proc.getErrorStream(), pipedOutput));
			stdoutReader = new Thread(new ProcOutputMerger(proc.getInputStream(), pipedOutput));
			
			stderrReader.start();
			stdoutReader.start();
		} else {
			procStderr = localProcess.getErrorStream();
			procStdout = localProcess.getInputStream();
		}

	}
	
	/* (non-Javadoc)
	 * @see java.lang.Process#destroy()
	 */
	@Override
	public void destroy() {
		localProcess.destroy();
	}

	/* (non-Javadoc)
	 * @see java.lang.Process#exitValue()
	 */
	@Override
	public int exitValue() {
		return localProcess.exitValue();
	}

	/* (non-Javadoc)
	 * @see java.lang.Process#getErrorStream()
	 */
	@Override
	public InputStream getErrorStream() {
		return procStderr;
	}

	/* (non-Javadoc)
	 * @see java.lang.Process#getInputStream()
	 */
	@Override
	public InputStream getInputStream() {
		return procStdout;
	}

	/* (non-Javadoc)
	 * @see java.lang.Process#getOutputStream()
	 */
	@Override
	public OutputStream getOutputStream() {
		return localProcess.getOutputStream();
	}

	/* (non-Javadoc)
	 * @see java.lang.Process#waitFor()
	 */
	@Override
	public int waitFor() throws InterruptedException {
		return localProcess.waitFor();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.core.AbstractRemoteProcess#isCompleted()
	 */
	public boolean isCompleted() {
		try {
			localProcess.exitValue();
			return true;
		} catch (IllegalThreadStateException e) {
			return false;
		}
	}
}
