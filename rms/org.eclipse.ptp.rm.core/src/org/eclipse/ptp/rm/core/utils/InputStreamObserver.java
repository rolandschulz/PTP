/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.ptp.rm.core.utils;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.ptp.rm.core.RMCorePlugin;

/**
 * A thread that forwards data from an inputstream to a {@link IInputStreamListener}.
 * <p>
 * This is a general facility that allows to forward data received from an inputstream to a listener object.
 * 
 * Copied and adapted from remote tools. Multiple listeners are now supported.
 * 
 * @author Daniel Felix Ferber
 */
public class InputStreamObserver extends Thread {
	private boolean closed = false;
	/*
	 * TODO: Open issues - Does access to 'killed' attribute need synchronized
	 * access? - On sockets, often one byte is read one after the other, instead
	 * of blocks of bytes. Maybe adding some delay might force to read more than
	 * one byte.
	 */
	/**
	 * Stream that is been observed and read.
	 */
	private final InputStream input;

	/**
	 * Signals that observer should stop running.
	 */
	private boolean killed;

	/**
	 * Listeners that are called when data is received.
	 */
	private final ListenerList listeners = new ListenerList();

	private static final int BUFFER_SIZE = 100;

	public InputStreamObserver(InputStream input) {
		this.input = input;
		setName(this.getClass().getName());
	}

	public InputStreamObserver(InputStream input, String name) {
		this.input = input;
		setName(name);
	}

	public InputStreamObserver(InputStream input, IInputStreamListener listener, String name) {
		this.input = input;
		this.listeners.add(listener);
		setName(name);
	}

	public InputStreamObserver(InputStream input, IInputStreamListener listener) {
		this.input = input;
		this.listeners.add(listener);
		setName(this.getClass().getName());
	}

	/**
	 * Stop observing data and terminate thread. Since InputStream cannot be
	 * interrupted, it will not interrupt immediately, but only when next input
	 * is received.
	 */
	public synchronized void kill() {
		killed = true;
		interrupt();
	}

	protected void log(String s) {
		// PTPCorePlugin.log(s);
	}

	protected void log(Throwable e) {
		RMCorePlugin.log(e);
	}

	private void streamClosed() {
		if (closed) {
			return;
		}
		log("Stream closed"); //$NON-NLS-1$
		closed = true;
		for (Object listener : listeners.getListeners()) {
			try {
				((IInputStreamListener) listener).streamClosed();
			} catch (Exception e) {
				log(e);
				listeners.remove(listener);
			}
		}
	}

	private void streamError(Exception e) {
		if (closed) {
			return;
		}
		log("Recovered from exception: " + e.getMessage()); //$NON-NLS-1$
		for (Object listener : listeners.getListeners()) {
			try {
				((IInputStreamListener) listener).streamError(e);
			} catch (Exception ee) {
				log(ee);
				listeners.remove(listener);
			}
		}
	}

	private void newBytes(byte buffer[], int length) {
		if (closed) {
			return;
		}
		log("Received: " + Integer.toString(length) + " bytes"); //$NON-NLS-1$ //$NON-NLS-2$
		for (Object listener : listeners.getListeners()) {
			try {
				((IInputStreamListener) listener).newBytes(buffer, length);
			} catch (Exception e) {
				log(e);
				listeners.remove(listener);
			}
		}
	}

	/**
	 * Run stream observer.
	 */
	@Override
	public void run() {
		byte buffer[] = new byte[BUFFER_SIZE];
		log("Started observing"); //$NON-NLS-1$
		while (!killed) {
			try {
				int bytes = input.read(buffer);
				if (bytes > 0) {
					newBytes(buffer, bytes);
				} else if (bytes == -1) {
					streamClosed();
					break;
				}
			} catch (IOException e) {
				if (killed) {
					streamClosed();
					break;
				}

				if (e instanceof EOFException) {
					streamClosed();
					break;
				}

				streamError(e);
				break;
			} catch (NullPointerException e) {
				/*
				 * When the stream is closed, Java may raise a
				 * NullPointerException. This case is handled like a
				 * IOException, as above.
				 */
				if (killed) {
					streamClosed();
					break;
				}

				streamError(e);
				break;
			}
		}
		log("Finished observing"); //$NON-NLS-1$
	}

	public void addListener(IInputStreamListener listener) {
		listeners.add(listener);
	}

	public void removeListener(IInputStreamListener listener) {
		listeners.remove(listener);
	}

}
