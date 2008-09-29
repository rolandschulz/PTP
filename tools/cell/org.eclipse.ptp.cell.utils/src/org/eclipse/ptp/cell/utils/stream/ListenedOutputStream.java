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

import java.io.OutputStream;

import org.eclipse.ptp.cell.utils.debug.Debug;


public class ListenedOutputStream extends OutputStream {

	private IStreamListener listener;

	public ListenedOutputStream(IStreamListener listener) {
		this.listener = listener;
		Debug.read();
	}
	
	public class IOException extends java.io.IOException {
		private static final long serialVersionUID = 1L;
		Exception e;
		
		public IOException(Exception e) {
			super(e.getMessage());
			this.e = e;
		}
		
		@Override
		public Throwable getCause() {
			return e;
		}
	}
	
	public void write(byte[] b, int off, int len) throws java.io.IOException {
		byte [] bytes = new byte[len];
		System.arraycopy(b, off, bytes, 0, len);
		try {
			listener.newBytes(bytes, len);
		} catch (Exception e) {
			Debug.POLICY.error(Debug.DEBUG_STREAM, e);
			if (e instanceof java.io.IOException) {
				throw (java.io.IOException)e;
			} else {
				throw new IOException(e);
			}
		}
	}

	public void write(byte[] b) throws java.io.IOException {
		byte [] bytes = new byte[b.length];
		System.arraycopy(b, 0, bytes, 0, b.length);
		try {
			listener.newBytes(b, b.length);
		} catch (Exception e) {
			Debug.POLICY.error(Debug.DEBUG_STREAM, e);
			if (e instanceof java.io.IOException) {
				throw (java.io.IOException)e;
			} else {
				throw new IOException(e);
			}
		}

	}

	public void write(int b) throws java.io.IOException {
		byte [] bytes = new byte[1];
		bytes[0] = (byte) b;
		try {
			listener.newBytes(bytes, bytes.length);
		} catch (Exception e) {
			Debug.POLICY.error(Debug.DEBUG_STREAM, e);
			if (e instanceof java.io.IOException) {
				throw (java.io.IOException)e;
			} else {
				throw new IOException(e);
			}
		}
	}
	
	public void close() throws java.io.IOException {
		listener.streamClosed();
		super.close();
	}

}
