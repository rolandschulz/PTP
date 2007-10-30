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

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * A temporarily workaround to read lines of text.
 * In future, will be replaced by an IStreamListener that converts
 * blocks of bytes to lines.
 * 
 * @author Daniel Felix Ferber
 * @since 1.0
 * 
 */
public class TextStreamObserver extends Thread {
	private BufferedReader reader;
	private boolean killed;
	private String name;
	private ILineStreamListener listener;

	public TextStreamObserver(InputStream input, ILineStreamListener listener,
			String name) {
		this.name = name;
		reader = new BufferedReader(new InputStreamReader(input));
		this.listener = listener;
		setName(name);
	}

	public TextStreamObserver(InputStream input, ILineStreamListener listener) {
		this.name = this.getClass().getName();
		reader = new BufferedReader(new InputStreamReader(input));
		this.listener = listener;
		setName(name);
	}
	
	public synchronized void kill() {
		killed = true;
	}

	void log(String string) {
//		System.err.println(name + ": " + s);
	}
	
	void newLine(String line) {
		log("Received: " + Integer.toString(line.length()) + " bytes");
		listener.newLine(line);
	}

	void streamClosed() {
		log("Stream closed");
		listener.streamClosed();
	}
	
	void streamError(Exception e) {
		log("Recovered from exception: " + e.getMessage());
		listener.streamError(e);
	}
	
	public void run() {
		String line;

		while (!killed) {
			try {
				line = reader.readLine();
				if (line == null) {
					streamClosed();
					return;
				} else {
					newLine(line);
				}
			} catch (IOException e) {
				if (e instanceof EOFException) {
					streamClosed();
					break;
				} else {
					streamError(e);
					break;
				}
			} catch (NullPointerException e) {
				if (killed) {
					streamClosed();
					break;
				} else {
					streamError(e);
					break;
				}
			}
		}
	}

}
