/*******************************************************************************
 * Copyright (c) 2000, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ptp.internal.rdt.sync.cdt.core.remotemake;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.MessageFormat;

import org.eclipse.ptp.internal.rdt.sync.cdt.core.messages.Messages;
import org.eclipse.remote.core.IRemoteProcess;

/**
 * @author crecoskie
 * 
 *         Bundled state of a launched process including the threads linking the process
 *         in/output to console documents.
 * 
 *         This class is a modified version of ProcessClosure from CDT that can handle remote processes.
 * 
 * @see org.eclipse.cdt.internal.core.ProcessClosure
 * @since 7.0
 */
public class RemoteProcessClosure {

	/**
	 * Thread which continuously reads from a input stream and pushes the read
	 * data to an output stream which is immediately flushed afterwards.
	 */
	protected static class ReaderThread extends Thread {

		private final InputStream fInputStream;
		private final OutputStream fOutputStream;
		private boolean fFinished = false;
		private final String lineSeparator;
		private final boolean fIsErrorReader;
		private final IRemoteProcess theProcess;

		/*
		 * outputStream can be null
		 */
		public ReaderThread(ThreadGroup group, String name, InputStream in, OutputStream out, boolean isErrorReader,
				IRemoteProcess fProcess) {
			super(group, name);
			fOutputStream = out;
			fInputStream = in;
			fIsErrorReader = isErrorReader;
			theProcess = fProcess;
			setDaemon(true);

			// TODO FIXME: line separator should be taken from the remote system... but how?
			lineSeparator = System.getProperty("line.separator"); //$NON-NLS-1$
		}

		public void close() {
			try {
				fOutputStream.close();
			} catch (IOException e) {
				// ignore
			}
		}

		public synchronized void complete() {
			fFinished = true;
			notify();
		}

		public synchronized boolean finished() {
			return fFinished;
		}

		@Override
		public void run() {
			String lastLine = null;
			try {
				try {
					BufferedReader reader = new BufferedReader(new InputStreamReader(fInputStream));
					String line;
					while ((line = reader.readLine()) != null) {
						lastLine = line;
						line += lineSeparator;

						if (fOutputStream != null) {
							fOutputStream.write(line.getBytes());
						}
					}
				} catch (IOException x) {
					// ignore
				} finally {
					try {

						if (!fIsErrorReader
								&& ((lastLine == null) || (!lastLine.contains(Messages.RemoteProcessClosure_exit_code)))) {
							// make sure the Spawner has finished up and that the exit code is retrievable
							int exit_code = 0;
							try {
								exit_code = theProcess.waitFor();
							} catch (InterruptedException e) {
								// ignore
							}

							// output a message saying we're done... remote processes do this for us but local ones don't
							String message = MessageFormat.format(Messages.RemoteProcessClosure_shell_completed, exit_code);
							fOutputStream.write(message.getBytes());
						}

						if (fOutputStream != null) {
							fOutputStream.flush();
						}
					} catch (IOException e) {
						// ignore
					}
				}
			} finally {
				complete();
			}
		}

		public synchronized void waitFor() {
			while (!fFinished) {
				try {
					wait();
				} catch (InterruptedException e) {
					// Ignore
				}
			}
		}
	}

	protected static int fCounter = 0;

	protected IRemoteProcess fProcess;

	protected OutputStream fOutput;
	protected OutputStream fError;

	protected ReaderThread fOutputReader;
	protected ReaderThread fErrorReader;

	/**
	 * Creates a process closure and connects the launched process with a
	 * console document.
	 * 
	 * @param outputStream
	 *            prcess stdout is written to this stream. Can be <code>null</code>, if not interested in reading the output
	 * @param errorStream
	 *            prcess stderr is written to this stream. Can be <code>null</code>, if not interested in reading the output
	 */
	public RemoteProcessClosure(IRemoteProcess process, OutputStream outputStream, OutputStream errorStream) {
		fProcess = process;
		fOutput = outputStream;
		fError = errorStream;
	}

	/**
	 * Tests if the process is alive.
	 * 
	 * @return
	 */
	public boolean isAlive() {
		if (fProcess != null) {
			if (fOutputReader.isAlive() || fErrorReader.isAlive()) {
				return true;
			}
			fProcess = null;
			fOutputReader.close();
			fErrorReader.close();
			fOutputReader = null;
			fErrorReader = null;
		}
		return false;
	}

	/**
	 * The same functionality as "isAlive()" but does not affect out streams,
	 * because they can be shared among processes
	 */
	public boolean isRunning() {
		if (fProcess != null) {
			if (fOutputReader.isAlive() || fErrorReader.isAlive()) {
				return true;
			}
			fProcess = null;
		}
		return false;
	}

	/**
	 * Run the process and block until completed.
	 */
	public void runBlocking() {
		runNonBlocking();

		boolean finished = false;
		while (!finished) {
			try {
				fProcess.waitFor();
			} catch (InterruptedException e) {
				// System.err.println("Closure exception " +e);
			}
			try {
				fProcess.exitValue();
				finished = true;
			} catch (IllegalThreadStateException e) {
				// System.err.println("Closure exception " +e);
			}
		}

		// @@@FIXME: Windows 2000 is screwed; double-check using output threads
		if (!fOutputReader.finished()) {
			fOutputReader.waitFor();
		}

		if (!fErrorReader.finished()) {
			fErrorReader.waitFor();
		}

		fOutputReader.close();
		fErrorReader.close();
		// it seems that thread termination and stream closing is working
		// without
		// any help
		fProcess = null;
		fOutputReader = null;
		fErrorReader = null;
	}

	/**
	 * Live links the launched process with the configured in/out streams using
	 * reader threads.
	 */
	public void runNonBlocking() {
		ThreadGroup group = new ThreadGroup("CBuilder" + fCounter++); //$NON-NLS-1$

		InputStream stdin = fProcess.getInputStream();
		InputStream stderr = fProcess.getErrorStream();

		fOutputReader = new ReaderThread(group, "OutputReader", stdin, fOutput, false, fProcess); //$NON-NLS-1$
		fErrorReader = new ReaderThread(group, "ErrorReader", stderr, fError, true, fProcess); //$NON-NLS-1$

		fOutputReader.start();
		fErrorReader.start();
	}

	/**
	 * Forces the termination the launched process
	 */
	public void terminate() {
		if (fProcess != null) {
			fProcess.destroy();
			fProcess = null;
		}
		if (!fOutputReader.finished()) {
			fOutputReader.waitFor();
		}
		if (!fErrorReader.finished()) {
			fErrorReader.waitFor();
		}
		fOutputReader.close();
		fErrorReader.close();
		fOutputReader = null;
		fErrorReader = null;
	}
}
