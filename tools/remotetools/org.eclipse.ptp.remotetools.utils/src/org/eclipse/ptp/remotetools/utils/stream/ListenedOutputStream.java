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

import java.io.IOException;
import java.io.OutputStream;

public class ListenedOutputStream extends OutputStream {

	private IStreamListener listener;

	public ListenedOutputStream(IStreamListener listener) {
		this.listener = listener;
	}
	
	public void write(byte[] b, int off, int len) throws IOException {
		byte [] bytes = new byte[len];
		System.arraycopy(b, off, bytes, 0, len);
		listener.newBytes(bytes, len);
	}

	public void write(byte[] b) throws IOException {
		listener.newBytes(b, b.length);
	}

	public void write(int b) throws IOException {
		byte [] bytes = new byte[1];
		bytes[0] = (byte) b;
		listener.newBytes(bytes, bytes.length);
	}

}
