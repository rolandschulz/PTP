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

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.eclipse.ptp.cell.utils.debug.Debug;


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
		Debug.read();
		this.name = name;
		reader = new BufferedReader(new InputStreamReader(input));
		this.listener = listener;
		setName(name);
	}

	public TextStreamObserver(InputStream input, ILineStreamListener listener) {
		Debug.read();
		this.name = this.getClass().getName();
		reader = new BufferedReader(new InputStreamReader(input));
		this.listener = listener;
		setName(name);
	}
	
	public synchronized void kill() {
		Debug.POLICY.trace(Debug.DEBUG_STREAM, "Marked as killed."); //$NON-NLS-1$
		killed = true;
	}

	void newLine(String line) {
		if (Debug.DEBUG_STREAM) {
			Debug.POLICY.trace("Notify received line: " + Integer.toString(line.length()) + " bytes"); //$NON-NLS-1$ //$NON-NLS-2$
			if (Debug.DEBUG_STREAM_MORE) {
				Debug.POLICY.trace(line);
			}
		}
		try {
			listener.newLine(line);
		} catch (Exception e) {
			Debug.POLICY.error(Debug.DEBUG_STREAM, e);
			Debug.POLICY.logError(e, Messages.TextStreamObserver_FailedDelegateMethod);
		}
	}

	void streamClosed() {
		Debug.POLICY.trace(Debug.DEBUG_STREAM, "Notify stream closed."); //$NON-NLS-1$
		try {
			listener.streamClosed();
		} catch (Exception e) {
			Debug.POLICY.error(Debug.DEBUG_STREAM, e);
			Debug.POLICY.logError(e, Messages.TextStreamObserver_FailedDelegateMethod);
		}
	}
	
	void streamError(Exception e) {
		Debug.POLICY.error(Debug.DEBUG_STREAM, "Notify exception: {0}.", e.getMessage()); //$NON-NLS-1$
		try {
			listener.streamError(e);
		} catch (Exception ee) {
			Debug.POLICY.error(Debug.DEBUG_STREAM, ee);
			Debug.POLICY.logError(ee, Messages.TextStreamObserver_FailedDelegateMethod);
		}
	}
	
	public void run() {
		Debug.read();
		Debug.POLICY.trace(Debug.DEBUG_STREAM, "Started."); //$NON-NLS-1$
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
					Debug.POLICY.trace(Debug.DEBUG_STREAM, "Expected EOFException."); //$NON-NLS-1$
					streamClosed();
					break;
				} else {
					streamError(e);
					break;
				}
			} catch (NullPointerException e) {
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
		Debug.POLICY.trace(Debug.DEBUG_STREAM, "Finished."); //$NON-NLS-1$
	}

}
