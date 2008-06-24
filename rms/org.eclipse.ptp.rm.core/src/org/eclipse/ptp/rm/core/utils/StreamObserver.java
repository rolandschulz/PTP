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

/**
 * Connects an inputstream to a stream listener.
 * <p>
 * This is a general facility that allows to forward data received from an
 * inputstream to a listener object. Note that only one listener is connected
 * to the observer.
 *
 * Copied and adapted from remote tools.
 *
 * @author Daniel Felix Ferber
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
	 * Listener that is called when data is received.
	 */
	private IStreamListener listener;

	private static final int BUFFER_SIZE = 100;

	public StreamObserver(InputStream input, IStreamListener listener, String name) {
		this.input = input;
		this.listener = listener;
		setName(name);
	}

	public StreamObserver(InputStream input, IStreamListener listener) {
		this.input = input;
		this.listener = listener;
		setName(this.getClass().getName());
	}

	/**
	 * Stop observing data and terminate thread.
	 * Since InputStream cannot be interrupted, it will not interrupt immediately, but only when next input is received.
	 */
	public synchronized void kill() {
		killed = true;
		interrupt();
	}

	void log(String s) {
//		System.err.println(name + ": " + s);
	}

	void streamClosed() {
		log("Stream closed");
		listener.streamClosed();
	}

	void streamError(Exception e) {
		log("Recovered from exception: " + e.getMessage());
		listener.streamError(e);
	}

	void newBytes(byte buffer[], int length) {
		log("Received: " + Integer.toString(length) + " bytes");
		listener.newBytes(buffer, length);
	}

	/**
	 * Run in background.
	 */
	@Override
	public void run() {
		byte buffer[] = new byte[BUFFER_SIZE];
		log("Started observing");
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
				} else if (e instanceof EOFException) {
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
					streamClosed();
					break;
				} else {
					streamError(e);
					break;
				}
			}
		}
		log("Finished observing");
	}
}
