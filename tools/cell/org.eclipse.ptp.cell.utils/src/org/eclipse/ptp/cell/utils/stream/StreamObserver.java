/******************************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *
 *****************************************************************************/

package org.eclipse.ptp.cell.utils.stream;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

import org.eclipse.ptp.cell.utils.debug.Debug;


/**
 * Connects an inputstream to a stream listener.
 * <p>
 * This is a general facility that allows to forward data received from an
 * inputstream to a listener object. Note that only one listener is connected
 * to the observer.
 * 
 * @author Daniel Felix Ferber
 * @since 1.0
 */
public class StreamObserver extends Thread {
	/*
	 * TODO: Open issues 
	 * - Does access to 'killed' attribute need synchronized access?
	 * - On sockets, often one byte is read one after the other, instead of
	 *   blocks of bytes. Maybe adding some delay might force to read
	 *   more than one byte.
	 */
	/**
	 * Stream that is been observed and read.
	 */
	private InputStream input;

	/**
	 * Signals that bridge should stop.
	 */
	private boolean killed;

	/**
	 * Name to identify bridge when debugging.
	 */
	private String name;

	/**
	 * Listener that is called when data is received.
	 */
	private IStreamListener listener;
	
	boolean hasclosed = false;

	static final int BUFFER_SIZE = 100;

	public StreamObserver(InputStream input, IStreamListener listener,
			String name) {
		Debug.read();
		this.name = name;
		this.input = input;
		this.listener = listener;
		setName(name);
	}

	public StreamObserver(InputStream input, IStreamListener listener) {
		Debug.read();
		this.name = this.getClass().getName();
		this.input = input;
		this.listener = listener;
		setName(name);
	}

	/**
	 * Stop observing data and terminate thread.
	 */
	public synchronized void kill() {
		Debug.POLICY.trace(Debug.DEBUG_STREAM, "Marked as killed."); //$NON-NLS-1$
		killed = true;
	}
	
	void streamClosed() {
		if (hasclosed) return;
		hasclosed = true;
		Debug.POLICY.trace(Debug.DEBUG_STREAM, "Notify stream closed."); //$NON-NLS-1$
		try {
			listener.streamClosed();
		} catch (Exception e) {
			Debug.POLICY.error(Debug.DEBUG_STREAM, e);
			Debug.POLICY.logError(e, Messages.StreamObserver_FailedDelegateMethod);
		}
	}
	
	void streamError(Exception e) {
		Debug.POLICY.error(Debug.DEBUG_STREAM, "Notify exception: {0}.", e.getMessage()); //$NON-NLS-1$
		try {
			listener.streamError(e);
		} catch (Exception ee) {
			Debug.POLICY.error(Debug.DEBUG_STREAM, ee);
			Debug.POLICY.logError(ee, Messages.StreamObserver_FailedDelegateMethod);
		}
	}
	
	void newBytes(byte buffer[], int length) {
		if (Debug.DEBUG_STREAM) {
			Debug.POLICY.trace("Notify received line: " + Integer.toString(length) + " bytes"); //$NON-NLS-1$ //$NON-NLS-2$
			if (Debug.DEBUG_STREAM_MORE) {
				Debug.POLICY.trace(new String(buffer, 0, length));
			}
		}
		try {
			listener.newBytes(buffer, length);
		} catch (Exception e) {
			Debug.POLICY.error(Debug.DEBUG_STREAM, e);
			Debug.POLICY.logError(e, Messages.StreamObserver_FailedDelegateMethod);
		}
	}

	/**
	 * Run in background.
	 */
	public void run() {
		Debug.read();
		byte buffer[] = new byte[250];
		Debug.POLICY.trace(Debug.DEBUG_STREAM, "Started."); //$NON-NLS-1$
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
					Debug.POLICY.trace(Debug.DEBUG_STREAM, "Expected EOFException."); //$NON-NLS-1$
					streamClosed();
					break;
				} else {
					streamError(e);
					break;
				}
			} catch (NullPointerException e) {
				/*
				 * When the stream is closed, Java may raise a
				 * NullPointerException. This case is handled like a
				 * IOException, as above.
				 */
				if (killed) {
					Debug.POLICY.trace(Debug.DEBUG_STREAM, "Expected NullPointerException."); //$NON-NLS-1$
					streamClosed();
					break;
				} else {
					streamError(e);
					break;
				}
			}
		}
		if (killed) {
			streamClosed();
		}
		Debug.POLICY.trace(Debug.DEBUG_STREAM, "Finished.");	 //$NON-NLS-1$
	}
}
