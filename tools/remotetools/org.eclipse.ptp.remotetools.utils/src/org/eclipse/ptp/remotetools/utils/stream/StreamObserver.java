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

package org.eclipse.ptp.remotetools.utils.stream;

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

	static final int BUFFER_SIZE = 100;

	public StreamObserver(InputStream input, IStreamListener listener,
			String name) {
		this.name = name;
		this.input = input;
		this.listener = listener;
		setName(name);
	}

	public StreamObserver(InputStream input, IStreamListener listener) {
		this.name = this.getClass().getName();
		this.input = input;
		this.listener = listener;
		setName(name);
	}

	/**
	 * Stop observing data and terminate thread.
	 */
	public synchronized void kill() {
		killed = true;
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
	public void run() {
		byte buffer[] = new byte[250];
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
				}
				/*
				 * If an IOException is raised, then the stream between Eclipse
				 * and Cell Simulator is broken. This happens when the Cell
				 * Simulator is closed or killed unexpectedly. Notify the
				 * control that the simulator is not running anymore.
				 */
				if (e instanceof EOFException) {
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
