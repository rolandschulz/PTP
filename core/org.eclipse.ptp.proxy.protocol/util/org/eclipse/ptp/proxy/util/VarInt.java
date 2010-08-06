/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.proxy.util;

import java.nio.ByteBuffer;

/**
 * Represent the specified integer value in varint format.
 * 
 * The varint format is described at
 * http://code.google.com/apis/protocolbuffers/docs/encoding.html#varints Note
 * that this function is not thread safe.
 * 
 * @since 5.0
 */
public class VarInt {
	private int fValue = 0;
	private boolean fValid = false;
	private ByteBuffer fBytes = null;

	public VarInt(ByteBuffer buffer) {
		int shift = 0;
		while (buffer.hasRemaining()) {
			byte b = buffer.get();
			fValue |= (b & 0x7f) << shift;
			shift += 7;
			if ((b & 0x80) == 0) {
				fValid = true;
				break;
			}
		}
	}

	public VarInt(int value) {
		fValue = value;
		fValid = true;
	}

	/**
	 * Get a byte buffer representation of this varint. Only valid if
	 * {@link #isValid()} is true.
	 * 
	 * @return byte buffer representation of varint
	 */
	public ByteBuffer getBytes() {
		if (fBytes == null && fValid) {
			fBytes = ByteBuffer.allocate(5); // maximum size
			int val = fValue;
			while (val > 0) {
				byte b = (byte) (val & 0x7f);
				val >>= 7;
				if (val > 0) {
					b |= 0x80;
				}
				fBytes.put(b);
				fBytes.limit(fBytes.position()); // set limit to the size of the
													// buffer
			}
		}
		fBytes.rewind();
		return fBytes;
	}

	/**
	 * Get the integer value of this varin. Only valid if {@link #isValid()} is
	 * true.
	 * 
	 * @return integer value of varint
	 */
	public int getValue() {
		return fValue;
	}

	/**
	 * Test if this varint is valid.
	 * 
	 * @return true if the varint is valid, false otherwise
	 */
	public boolean isValid() {
		return fValid;
	}
}
