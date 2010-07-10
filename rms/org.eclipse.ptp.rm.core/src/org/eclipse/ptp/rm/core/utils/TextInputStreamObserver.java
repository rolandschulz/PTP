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

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.IOException;

/**
 * Connects an BufferedReader a text listener.
 * <p>
 * This is a general facility that allows to forward data received from an
 * buffered reader to a listener object. Note that only one listener is
 * connected to the observer.
 *
 * Copied and adapted from remote tools.
 *
 * @author Daniel Felix Ferber
 * @since 1.0
 *
 */
public class TextInputStreamObserver extends Thread {
	/**
	 * Stream that is been observed and read.
	 */
	private final BufferedReader reader;

	/**
	 * Signals that bridge should stop.
	 */
	private boolean killed;

	/**
	 * Listener that is called when data is received.
	 */
	private final ITextInputStreamListener listener;

	public TextInputStreamObserver(BufferedReader input, ITextInputStreamListener listener) {
		this.reader = input;
		this.listener = listener;
		setName(this.getClass().getName());
	}

	public TextInputStreamObserver(BufferedReader input, ITextInputStreamListener listener,
			String name) {
		this.reader = input;
		this.listener = listener;
		setName(name);
	}

	/**
	 * Stop observing data and terminate thread.
	 * Since InputStream cannot be interrupted, it will not interrupt immediately, but only when next input is received.
	 */
	public synchronized void kill() {
		killed = true;
		interrupt();
	}

	void log(String string) {
		//		System.err.println(name + ": " + s);
	}

	void newLine(String line) {
		log("Received: " + Integer.toString(line.length()) + " bytes"); //$NON-NLS-1$ //$NON-NLS-2$
		listener.newLine(line);
	}

	@Override
	public void run() {
		String line;

		while (!killed) {
			try {
				line = reader.readLine();
				if (line == null) {
					streamClosed();
					return;
				}
				newLine(line);
			} catch (IOException e) {
				if (e instanceof EOFException) {
					streamClosed();
					break;
				}
				streamError(e);
				break;
			} catch (NullPointerException e) {
				if (killed) {
					streamClosed();
					break;
				}
				streamError(e);
				break;
			}
		}
	}

	void streamClosed() {
		log("Stream closed"); //$NON-NLS-1$
		listener.streamClosed();
	}

	void streamError(Exception e) {
		log("Recovered from exception: " + e.getMessage()); //$NON-NLS-1$
		listener.streamError(e);
	}

}
