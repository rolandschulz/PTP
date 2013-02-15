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

package org.eclipse.ptp.internal.rm.jaxb.control.core.runnable.command;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.debug.core.IStreamListener;
import org.eclipse.ptp.core.util.CoreExceptionUtils;
import org.eclipse.ptp.internal.rm.jaxb.control.core.ICommandJobStreamMonitor;
import org.eclipse.ptp.internal.rm.jaxb.control.core.JAXBControlConstants;
import org.eclipse.ptp.internal.rm.jaxb.control.core.JAXBControlCorePlugin;
import org.eclipse.ptp.internal.rm.jaxb.control.core.messages.Messages;

/**
 * Monitors the output stream of a system process and notifies listeners of additions to the stream.<br>
 * <br>
 * The output stream monitor reads system out (or err) via an input stream.<br>
 * <br>
 * This class has been adapted from <code>org.eclipse.debug.internal.core.OutputStreamMonitor</code> (internal, discouraged access).
 * 
 * @author arossi
 * 
 */
public class CommandJobStreamMonitor implements ICommandJobStreamMonitor {
	class ContentNotifier implements ISafeRunnable {

		private IStreamListener fListener;
		private String fText;

		/**
		 * @see org.eclipse.core.runtime.ISafeRunnable#handleException(java.lang.Throwable)
		 */
		public void handleException(Throwable exception) {
			JAXBControlCorePlugin.log(exception);
		}

		public void notifyAppend(String text) {
			if (text == null) {
				return;
			}
			fText = text;
			Object[] copiedListeners = fListeners.getListeners();
			for (Object copiedListener : copiedListeners) {
				fListener = (IStreamListener) copiedListener;
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

	protected final StringBuffer fContents;
	protected final String fEncoding;

	protected ListenerList fListeners;
	protected Thread fThread;
	protected boolean fKilled = false;
	protected int bufferLimit;
	private final InputStream fStream;
	private long lastSleep;

	public CommandJobStreamMonitor() {
		this(null, null);
	}

	public CommandJobStreamMonitor(InputStream stream) {
		this(stream, null);
	}

	/**
	 * Creates an output stream monitor on the given stream (connected to system out or err).
	 * 
	 * @param stream
	 *            input stream to read from
	 * @param encoding
	 *            stream encoding or <code>null</code> for system default
	 */
	public CommandJobStreamMonitor(InputStream stream, String encoding) {
		fStream = stream;
		fEncoding = encoding;
		fContents = new StringBuffer();
		bufferLimit = JAXBControlConstants.UNDEFINED;
		fListeners = new ListenerList();
	}

	/**
	 * @param listener
	 *            from client
	 */
	public synchronized void addListener(IStreamListener listener) {
		fListeners.add(listener);
	}

	public void append(String text) {
		synchronized (this) {
			fContents.append(text);
		}
		fireStreamAppended(text);
	}

	/**
	 * Causes the monitor to close all communications between it and the underlying stream by waiting for the thread to terminate.
	 */
	public synchronized void close() {
		fKilled = true;
		if (fThread != null) {
			Thread thread = fThread;
			fThread = null;
			try {
				thread.interrupt();
				thread.join();
			} catch (InterruptedException ie) {
				// Ignore
			}
			fContents.setLength(0);
			fListeners = new ListenerList();
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
	 * @see org.eclipse.debug.core.model.IStreamMonitor#removeListener(org.eclipse .debug.core.IStreamListener)
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
	protected void fireStreamAppended(String text) {
		getNotifier().notifyAppend(text);
	}

	protected ContentNotifier getNotifier() {
		return new ContentNotifier();
	}

	/**
	 * Continually reads from the stream.
	 * <p>
	 * This method, along with the <code>startReading</code> method is used to allow <code>OutputStreamMonitor</code> to implement
	 * <code>Runnable</code> without publicly exposing a <code>run</code> method.
	 */
	protected void read() {
		if (fStream == null) {
			JAXBControlCorePlugin.log(CoreExceptionUtils.getErrorStatus(Messages.CommandJobNullMonitorStreamError, null));
			return;
		}

		lastSleep = System.currentTimeMillis();
		long currentTime = lastSleep;
		byte[] bytes = new byte[JAXBControlConstants.STREAM_BUFFER_SIZE];
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
						if (bufferLimit != JAXBControlConstants.UNDEFINED && len > bufferLimit) {
							fContents.delete(0, len - bufferLimit);
						}
					}
					fireStreamAppended(text);
				}
			} catch (EOFException eof) {
				break;
			} catch (IOException ioe) {
				if (!fKilled) {
					JAXBControlCorePlugin.log(ioe);
				}
				return;
			} catch (NullPointerException e) {
				// killing the stream monitor while reading can cause an NPE
				// when reading from the stream
				if (!fKilled && fThread != null) {
					JAXBControlCorePlugin.log(e);
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
					// Ignore
				}
			}
		}

		try {
			fStream.close();
		} catch (IOException e) {
			JAXBControlCorePlugin.log(e);
		}
	}
}