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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.eclipse.ptp.cell.utils.debug.Debug;


/**
 * Connects an inputstream to an outputstream.
 * <p>
 * This is a general facility that allows to forward data received from an
 * inputstream to an outputstream.
 * <p>
 * An example would be to forward automatically data received from a socket
 * inputstream to a file outputstream.
 * <p>
 * Do not forget to call run() method!
 * 
 * @author Daniel Felix Ferber
 * @since 1.0
 */
public class StreamBridge  {
	/*
	 * TODO: Open issues
	 * - Handle exceptions
	 */
	IStreamListener dataForwarder = null;
	StreamObserver streamObserver = null;
	
	private class DataForwarder implements IStreamListener {
		OutputStream output;
		StreamBridge bridge;

		public DataForwarder(StreamBridge bridge, OutputStream output) {
			this.output = output;
			this.bridge = bridge;
		}

		public void newBytes(byte[] bytes, int length) {
			try {
				output.write(bytes, 0, length);
			} catch (IOException e) {
				Debug.POLICY.error(Debug.DEBUG_STREAM, e);
				bridge.kill();
			}
		}

		public void newChars(char[] chars, int length) {
			// Nothing to do
		}

		public void streamClosed() {
			// Dont forward close... only stop bridge
			bridge.kill();
		}

		public void streamError(Exception e) {	
			// Dont forward close... only stop bridge
			bridge.kill();
			Debug.POLICY.logError(e, Messages.StreamBridge_InterruptedByError);
		}
	}

	public StreamBridge(InputStream input, OutputStream output, String name) {
		dataForwarder = new DataForwarder(this, output);
		streamObserver = new StreamObserver(input, dataForwarder, name);
	}

	public StreamBridge(InputStream input, OutputStream output) {
		dataForwarder = new DataForwarder(this, output);
		streamObserver = new StreamObserver(input, dataForwarder);
	}

	public void kill() {
		if (streamObserver != null) {
			streamObserver.kill();
		}
		dataForwarder = null;
		streamObserver = null;
	}
	
	public void run() {
		streamObserver.start();
	}
}
