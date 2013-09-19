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
package org.eclipse.ptp.internal.remote.remotetools.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import org.eclipse.remote.core.AbstractRemoteProcess;

public class RemoteToolsProcess extends AbstractRemoteProcess {
	private static int refCount = 0;

	private final Process remoteProcess;
	private InputStream procStdout;
	private InputStream procStderr;
	private Thread stdoutReader;
	private Thread stderrReader;

	private class ProcReader implements Runnable {
		private final static int BUF_SIZE = 8192;

		private final InputStream input;
		private final OutputStream output;

		public ProcReader(InputStream input, OutputStream output) {
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
				// Ignore
			}
			synchronized (output) {
				if (--refCount == 0) {
					try {
						output.close();
					} catch (IOException e) {
						// Ignore
					}
				}
			}
		}
	}

	private class NullInputStream extends InputStream {
		@Override
		public int read() throws IOException {
			return -1;
		}

		@Override
		public int available() {
			return 0;
		}
	}

	public RemoteToolsProcess(Process proc, boolean merge) throws IOException {
		remoteProcess = proc;

		if (merge) {
			PipedOutputStream pipedOutput = new PipedOutputStream();

			procStdout = new PipedInputStream(pipedOutput);
			procStderr = new NullInputStream();

			stderrReader = new Thread(new ProcReader(proc.getErrorStream(), pipedOutput));
			stdoutReader = new Thread(new ProcReader(proc.getInputStream(), pipedOutput));

			stderrReader.start();
			stdoutReader.start();
		} else {
			procStdout = proc.getInputStream();
			procStderr = proc.getErrorStream();
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Process#destroy()
	 */
	@Override
	public void destroy() {
		remoteProcess.destroy();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Process#exitValue()
	 */
	@Override
	public int exitValue() {
		return remoteProcess.exitValue();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Process#getErrorStream()
	 */
	@Override
	public InputStream getErrorStream() {
		return procStderr;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Process#getInputStream()
	 */
	@Override
	public InputStream getInputStream() {
		return procStdout;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Process#getOutputStream()
	 */
	@Override
	public OutputStream getOutputStream() {
		return remoteProcess.getOutputStream();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Process#waitFor()
	 */
	@Override
	public int waitFor() throws InterruptedException {
		return remoteProcess.waitFor();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.remote.core.AbstractRemoteProcess#isCompleted()
	 */
	@Override
	public boolean isCompleted() {
		try {
			remoteProcess.exitValue();
			return true;
		} catch (IllegalThreadStateException e) {
			return false;
		}
	}
}
