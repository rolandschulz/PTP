/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Albert L. Rossi - modifications to support JAXB RM commands (copied
 *                       code from org.eclipse.debug)
 *******************************************************************************/

package org.eclipse.ptp.rm.jaxb.core.runnable.command;

import java.io.BufferedInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.debug.core.IStreamListener;
import org.eclipse.ptp.remote.core.IRemoteProcess;
import org.eclipse.ptp.remote.core.IRemoteProcessBuilder;
import org.eclipse.ptp.rm.jaxb.core.ICommandJobStreamMonitor;
import org.eclipse.ptp.rm.jaxb.core.IJAXBNonNLSConstants;
import org.eclipse.ptp.rm.jaxb.core.IJAXBResourceManagerControl;
import org.eclipse.ptp.rm.jaxb.core.JAXBCorePlugin;
import org.eclipse.ptp.rm.jaxb.core.messages.Messages;
import org.eclipse.ptp.rm.jaxb.core.utils.CoreExceptionUtils;
import org.eclipse.ptp.rm.jaxb.core.utils.RemoteServicesDelegate;

/**
 * Monitors the output stream of a system process and notifies listeners of
 * additions to the stream.<br>
 * <br>
 * The output stream monitor reads system out (or err) via an input stream.<br>
 * <br>
 * This class has been adapted from
 * <code>org.eclipse.debug.internal.core.OutputStreamMonitor</code> (internal,
 * discouraged access).
 * 
 * @author arossi
 * 
 */
public class CommandJobStreamMonitor implements ICommandJobStreamMonitor, IJAXBNonNLSConstants {
	class ContentNotifier implements ISafeRunnable {

		private IStreamListener fListener;
		private String fText;

		/**
		 * @see org.eclipse.core.runtime.ISafeRunnable#handleException(java.lang.Throwable)
		 */
		public void handleException(Throwable exception) {
			JAXBCorePlugin.log(exception);
		}

		public void notifyAppend(String text) {
			if (text == null) {
				return;
			}
			fText = text;
			Object[] copiedListeners = fListeners.getListeners();
			for (int i = 0; i < copiedListeners.length; i++) {
				fListener = (IStreamListener) copiedListeners[i];
				SafeRunner.run(this);
			}
			fListener = null;
			fText = null;
		}

		/**
		 * @see org.eclipse.core.runtime.ISafeRunnable#run()
		 */
		public void run() throws Exception {
			fListener.streamAppended(fText, CommandJobStreamMonitor.this);
		}
	}

	private InputStream fStream;
	private ListenerList fListeners = new ListenerList();
	private final StringBuffer fContents;
	private Thread fThread;
	private boolean fKilled = false;
	private long lastSleep;
	private int bufferLimit;
	private final String fEncoding;
	private final IRemoteProcessBuilder fBuilder;
	private IRemoteProcess process;

	/**
	 * Registers a process which can be started to monitor a remote file via
	 * tail -f.
	 * 
	 * @param rm
	 *            resource manager providing remote service
	 * @param remoteFilePath
	 *            of the file to be monitored
	 */
	public CommandJobStreamMonitor(IJAXBResourceManagerControl rm, String remoteFilePath) {
		this(rm, remoteFilePath, null);
	}

	/**
	 * Registers a process which can be started to monitor a remote file via
	 * tail -f.
	 * 
	 * @param rm
	 *            resource manager providing remote service
	 * @param remoteFilePath
	 *            of the file to be monitored
	 * @param encoding
	 *            stream encoding or <code>null</code> for system default
	 */
	public CommandJobStreamMonitor(IJAXBResourceManagerControl rm, String remoteFilePath, String encoding) {
		fContents = new StringBuffer();
		fEncoding = encoding;
		String[] args = new String[] { TAIL, MINUS_F, remoteFilePath };
		RemoteServicesDelegate delegate = rm.getRemoteServicesDelegate();
		fBuilder = delegate.getRemoteServices().getProcessBuilder(delegate.getRemoteConnection(), args);
		bufferLimit = UNDEFINED;
	}

	public CommandJobStreamMonitor(InputStream stream) {
		this(stream, null);
	}

	/**
	 * Creates an output stream monitor on the given stream (connected to system
	 * out or err).
	 * 
	 * @param stream
	 *            input stream to read from
	 * @param encoding
	 *            stream encoding or <code>null</code> for system default
	 */
	public CommandJobStreamMonitor(InputStream stream, String encoding) {
		fStream = new BufferedInputStream(stream, STREAM_BUFFER_SIZE);
		fEncoding = encoding;
		fContents = new StringBuffer();
		fBuilder = null;
		bufferLimit = UNDEFINED;
	}

	/**
	 * @param listener
	 *            from client
	 */
	public synchronized void addListener(IStreamListener listener) {
		listener.streamAppended(fContents.toString(), CommandJobStreamMonitor.this);
		fListeners.add(listener);
	}

	/**
	 * Causes the monitor to close all communications between it and the
	 * underlying stream by waiting for the thread to terminate.
	 */
	public synchronized void close() {
		fKilled = true;
		if (fThread != null) {
			Thread thread = fThread;
			fThread = null;
			try {
				thread.join();
			} catch (InterruptedException ie) {
			}
			fContents.setLength(0);
			fListeners = new ListenerList();
			maybeKillProcess();
		}
	}

	@Override
	public void finalize() throws Throwable {
		close();
		super.finalize();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.core.model.IStreamMonitor#getContents()
	 */
	public synchronized String getContents() {
		return fContents.toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.debug.core.model.IStreamMonitor#removeListener(org.eclipse
	 * .debug.core.IStreamListener)
	 */
	public synchronized void removeListener(IStreamListener listener) {
		fListeners.remove(listener);
	}

	/**
	 * Tune the monitor's buffer size
	 * 
	 * @param bufferLimit
	 *            in chars
	 * 
	 */
	public void setBufferLimit(int bufferLimit) {
		this.bufferLimit = bufferLimit;
	}

	/**
	 * Starts a thread which reads from the stream
	 */
	public synchronized void startMonitoring() {
		if (fThread == null) {
			maybeStartRemoteProcess();
			fThread = new Thread(new Runnable() {
				public void run() {
					read();
				}
			}, Messages.CommandJobStreamMonitor_label);
			fThread.setDaemon(true);
			fThread.setPriority(Thread.MIN_PRIORITY);
			fThread.start();
		}
	}

	/**
	 * Notifies the listeners that text has been appended to the stream.
	 */
	private void fireStreamAppended(String text) {
		getNotifier().notifyAppend(text);
	}

	private ContentNotifier getNotifier() {
		return new ContentNotifier();
	}

	private void maybeKillProcess() {
		if (process != null && !process.isCompleted()) {
			process.destroy();
			process = null;
		}
	}

	private void maybeStartRemoteProcess() {
		if (fBuilder == null) {
			return;
		}

		try {
			process = fBuilder.start();
			fStream = process.getInputStream();
		} catch (IOException t) {
			JAXBCorePlugin.log(t);
		}
	}

	/**
	 * Continually reads from the stream.
	 * <p>
	 * This method, along with the <code>startReading</code> method is used to
	 * allow <code>OutputStreamMonitor</code> to implement <code>Runnable</code>
	 * without publicly exposing a <code>run</code> method.
	 */
	private void read() {
		if (fStream == null) {
			JAXBCorePlugin.log(CoreExceptionUtils.getErrorStatus(Messages.CommandJobNullMonitorStreamError, null));
			return;
		}
		lastSleep = System.currentTimeMillis();
		long currentTime = lastSleep;
		byte[] bytes = new byte[STREAM_BUFFER_SIZE];
		int read = 0;
		while (read >= 0) {
			try {
				if (fKilled) {
					break;
				}
				read = fStream.read(bytes);
				if (read > 0) {
					String text;
					if (fEncoding != null) {
						text = new String(bytes, 0, read, fEncoding);
					} else {
						text = new String(bytes, 0, read);
					}
					synchronized (this) {
						fContents.append(text);
						int len = fContents.length();
						if (bufferLimit != UNDEFINED && len > bufferLimit) {
							fContents.delete(0, len - bufferLimit);
						}
						fireStreamAppended(text);
					}
				}
			} catch (EOFException eof) {
				break;
			} catch (IOException ioe) {
				if (!fKilled) {
					JAXBCorePlugin.log(ioe);
				}
				return;
			} catch (NullPointerException e) {
				// killing the stream monitor while reading can cause an NPE
				// when reading from the stream
				if (!fKilled && fThread != null) {
					JAXBCorePlugin.log(e);
				}
				return;
			}

			currentTime = System.currentTimeMillis();
			if (currentTime - lastSleep > 1000) {
				lastSleep = currentTime;
				try {
					Thread.sleep(1); // just give up CPU to maintain UI
										// responsiveness.
				} catch (InterruptedException e) {
				}
			}
		}
		try {
			fStream.close();
		} catch (IOException e) {
			JAXBCorePlugin.log(e);
		}
	}
}